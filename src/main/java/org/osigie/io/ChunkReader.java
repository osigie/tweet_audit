package org.osigie.io;

import java.io.IOException;
import java.util.List;

import org.osigie.model.TweetRecord;

public interface ChunkReader extends AutoCloseable {
  /** Reads up to batchSize tweet records, skipping already-checkpointed entries. */
  List<TweetRecord> nextBatch(int batchSize) throws IOException;
}
