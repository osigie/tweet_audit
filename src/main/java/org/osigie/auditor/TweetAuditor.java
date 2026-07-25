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
    private final int batchSize;

    public TweetAuditor(AuditClient auditClient, String chunkPath,
                        AuditResultWriter auditResultWriter,
                        CheckpointStore checkpointStore, int batchSize) {
        this.auditClient = auditClient;
        this.chunkPath = chunkPath;
        this.auditResultWriter = auditResultWriter;
        this.checkpointStore = checkpointStore;
        this.batchSize = batchSize;
    }

    public void run() throws Exception {

        CheckpointEntry checkpoint =
                checkpointStore.load()
                        .orElse(new CheckpointEntry(0, 0));

        List<Segment> segments = loadSegments();

        for (Segment segment : segments) {

            if (segment.getBaseOffset() < checkpoint.segmentBaseOffset()) {
                continue;
            }
            processSegment(segment, checkpoint);

            checkpoint = new CheckpointEntry(0, 0);
        }
    }

    private void processSegment(
            Segment segment,
            CheckpointEntry checkpoint
    ) throws Exception {

        try (ChunkReader reader = new ChunkReader(segment.getPath(), checkpoint, segment.getBaseOffset())) {

            List<TweetRecord> batch;
            TweetRecord current = null;

            while (!(batch = reader.nextBatch(batchSize)).isEmpty()) {

                for (TweetRecord record : batch) {
                    boolean deleted = auditClient.audit(record);

                    auditResultWriter.append(record, deleted);

                    current = record;
                }
                auditResultWriter.flush();

                checkpointStore.save(
                        new CheckpointEntry(
                                current.entryIndex() + 1,
                                current.segmentBaseOffset()
                        )
                );
            }
        }
    }


    private List<Segment> loadSegments() throws IOException {
        try (Stream<Path> path = Files.list(Path.of(chunkPath))) {
            return path.filter(p -> p.getFileName().toString().startsWith(Constants.CHUNKS_PATH_PREFIX))
                    .map(p -> {
                        try {
                            return new Segment(p, extractOffSetFromFileName(p));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .sorted(Comparator.comparingLong(Segment::getBaseOffset))
                    .toList();
        }
    }

    private long extractOffSetFromFileName(Path path) {
        return Long.parseLong(path.getFileName().toString().split("_")[1].split("\\.")[0]);
    }
}
