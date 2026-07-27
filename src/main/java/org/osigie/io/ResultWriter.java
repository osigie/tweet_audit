package org.osigie.io;

import org.osigie.model.TweetRecord;

import java.io.IOException;

public interface ResultWriter extends AutoCloseable {
    void append(TweetRecord record, boolean deleted) throws IOException;
    void flush() throws IOException;
}
