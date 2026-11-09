package org.osigie.io;

import java.io.IOException;

import org.osigie.model.TweetRecord;

public interface ResultWriter extends AutoCloseable {
  /** Appends a result line for the given tweet. */
  void append(TweetRecord record, boolean deleted) throws IOException;

  /** Flushes any buffered output to disk. */
  void flush() throws IOException;
}
