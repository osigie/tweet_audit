package org.osigie.io;

import org.osigie.model.TweetRecord;

import java.io.IOException;
import java.util.List;

public interface ChunkReader extends AutoCloseable {
    List<TweetRecord> nextBatch(int batchSize) throws IOException;
}
