package org.osigie.model;

public record TweetRecord(long entryIndex, long segmentBaseOffset, String id, String text) { }
