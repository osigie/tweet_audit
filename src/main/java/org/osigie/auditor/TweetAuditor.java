package org.osigie.auditor;

import org.osigie.checkpoint.CheckpointStore;
import org.osigie.config.Constants;
import org.osigie.extractor.chunks.Segment;
import org.osigie.model.CheckpointEntry;
import org.osigie.model.TweetRecord;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

public class TweetAuditor {
    private final AuditClient auditClient;
    private final String chunkPath;
    private final AuditResultWriter auditResultWriter;
    private final CheckpointStore checkpointStore;

    public TweetAuditor(AuditClient auditClient, String chunkPath,
                        AuditResultWriter auditResultWriter,
                        CheckpointStore checkpointStore) {
        this.auditClient = auditClient;
        this.chunkPath = chunkPath;
        this.auditResultWriter = auditResultWriter;
        this.checkpointStore = checkpointStore;
    }

    public void run() throws IOException {
        CheckpointEntry checkpointEntry = checkpointStore.load().orElse(new CheckpointEntry(0, 0));

        Segment segment = loadSegment(checkpointEntry.chunkIndex());

        try (ChunkReader reader = new ChunkReader(segment.getPath(), checkpointEntry)) {

            List<TweetRecord> batch;
            TweetRecord current = null;

            while (!(batch = reader.nextBatch(10)).isEmpty()) {

                for (TweetRecord record : batch) {

                    boolean deleted = auditClient.audit(record);

                    auditResultWriter.append(record, deleted);

                    current = record;

                }
                auditResultWriter.flush();

                checkpointStore.save(new CheckpointEntry(current.entryIndex(), current.chunkIndex()));

            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Segment loadSegment(int chunkIndex) throws IOException {
        Path chunkDirPath = Path.of(chunkPath);

        try (Stream<Path> paths = Files.list(chunkDirPath)) {

            Path path = paths
                    .filter(p -> p.getFileName().toString().startsWith(Constants.CHUNKS_PATH_PREFIX))
                    .filter(p -> extractOffSetFromFileName(p) == chunkIndex)
                    .findFirst()
                    .orElseThrow(() ->
                            new IllegalStateException("No segment found for chunk index " + chunkIndex));

            return new Segment(path, chunkIndex);

        }


    }

    private long extractOffSetFromFileName(Path path) {
        return Long.parseLong(path.getFileName().toString().split("_")[1].split("\\.")[0]);
    }
}
