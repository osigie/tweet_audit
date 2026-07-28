package org.osigie.io.impl;

import org.osigie.config.Constants;
import org.osigie.io.CheckpointStore;
import org.osigie.io.ChunkReader;
import org.osigie.io.FileHandler;
import org.osigie.io.ResultWriter;
import org.osigie.io.extractor.Segment;
import org.osigie.model.CheckpointEntry;
import org.osigie.model.TweetRecord;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

public class DiskFileHandler implements FileHandler {
    private final Queue<Segment> segments;
    private final CheckpointStore checkpointStore;
    private final ResultWriter resultWriter;
    private final CheckpointEntry savedCheckpoint;
    private ChunkReader currentReader;
    private boolean onFirstSegment;

    public DiskFileHandler(String chunkDir, String checkpointPath, String resultPath, String xUserName) throws IOException {
        this.checkpointStore = new FileCheckpointStore(checkpointPath);
        this.resultWriter = new FileResultWriter(resultPath, xUserName);
        this.segments = loadSegments(chunkDir);
        this.savedCheckpoint = loadSavedCheckpoint();
        this.onFirstSegment = true;
        openReader();
    }

    @Override
    public List<TweetRecord> nextBatch(int batchSize) throws IOException {
        return currentReader != null ? currentReader.nextBatch(batchSize) : List.of();
    }

    @Override
    public boolean nextSegment() throws IOException {
        if (currentReader != null) {
            try {
                currentReader.close();
            } catch (Exception e) {
                throw new IOException("Failed to close chunk reader", e);
            }
        }
        return openReader();
    }

    @Override
    public void saveCheckpoint(CheckpointEntry entry) throws IOException {
        checkpointStore.save(entry);
    }

    @Override
    public void writeResult(TweetRecord record) throws IOException {
        resultWriter.append(record, false);
    }

    @Override
    public void flush() throws IOException {
        resultWriter.flush();
    }

    @Override
    public void close() throws Exception {
        if (currentReader != null) {
            currentReader.close();
        }
        resultWriter.close();
    }

    private boolean openReader() throws IOException {
        while (!segments.isEmpty()) {
            Segment segment = segments.poll();

            if (segment.getBaseOffset() < savedCheckpoint.segmentBaseOffset()) {
                continue;
            }

            CheckpointEntry effective = onFirstSegment
                    ? savedCheckpoint
                    : new CheckpointEntry(0, 0);
            onFirstSegment = false;

            currentReader = new BinaryChunkReader(
                    Files.newInputStream(segment.getPath()),
                    effective,
                    segment.getBaseOffset()
            );
            return true;
        }

        currentReader = null;
        return false;
    }

    private CheckpointEntry loadSavedCheckpoint() {
        try {
            return checkpointStore.load()
                    .orElse(new CheckpointEntry(0, 0));
        } catch (IOException e) {
            return new CheckpointEntry(0, 0);
        }
    }

    private Queue<Segment> loadSegments(String chunkDir) throws IOException {
        try (Stream<Path> paths = Files.list(Path.of(chunkDir))) {
            List<Segment> segmentList = paths
                    .filter(p -> p.getFileName().toString().startsWith(Constants.CHUNKS_PATH_PREFIX))
                    .map(p -> {
                        try {
                            return new Segment(p, extractOffset(p));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .sorted(Comparator.comparingLong(Segment::getBaseOffset))
                    .toList();
            return new LinkedList<>(segmentList);
        }
    }

    private long extractOffset(Path path) {
        return Long.parseLong(path.getFileName().toString().split("_")[1].split("\\.")[0]);
    }
}
