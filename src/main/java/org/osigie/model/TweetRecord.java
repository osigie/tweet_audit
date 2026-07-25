package org.osigie.model;

import java.nio.file.Path;

public record TweetRecord(long entryIndex, int chunkIndex, String id, String text) {
}
