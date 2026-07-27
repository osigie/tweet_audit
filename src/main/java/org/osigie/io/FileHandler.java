package org.osigie.io;

import org.osigie.model.CheckpointEntry;
import org.osigie.model.TweetRecord;

import java.io.IOException;
import java.util.List;

public interface FileHandler extends AutoCloseable {
    List<TweetRecord> nextBatch(int batchSize) throws IOException;
    boolean nextSegment() throws IOException;
    void saveCheckpoint(CheckpointEntry entry) throws IOException;
    void writeResult(TweetRecord record, boolean deleted) throws IOException;
    void flush() throws IOException;
}
