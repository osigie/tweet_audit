package org.osigie.io;

import java.io.IOException;
import java.util.List;

import org.osigie.model.CheckpointEntry;
import org.osigie.model.TweetRecord;

public interface FileHandler extends AutoCloseable {
  /** Reads the next batch of tweet records from the current segment. */
  List<TweetRecord> nextBatch(int batchSize) throws IOException;

  /** Advances to the next segment and returns true if one exists. */
  boolean nextSegment() throws IOException;

  /** Persists the current processing position so work can resume after interruption. */
  void saveCheckpoint(CheckpointEntry entry) throws IOException;

  /** Writes a tweet result to the output. */
  void writeResult(TweetRecord record) throws IOException;

  /** Flushes any buffered output to disk. */
  void flush() throws IOException;
}
