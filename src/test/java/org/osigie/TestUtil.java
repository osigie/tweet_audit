package org.osigie;

import org.osigie.analysis.AnalysisClient;
import org.osigie.analysis.GeminiClient;
import org.osigie.exception.AnalysisException;
import org.osigie.io.FileHandler;
import org.osigie.model.CheckpointEntry;
import org.osigie.model.TweetRecord;

import java.util.*;

public final class TestUtil {


    private TestUtil() {
    }

    public static class MockAnalysisClient implements AnalysisClient {

        @Override
        public boolean analyze(TweetRecord tweet) {
            if (tweet.entryIndex() == -1) throw new AnalysisException("test");
            return tweet.entryIndex() == 1;
        }
    }

    public static class MockGeminiClient extends GeminiClient {

        public MockGeminiClient(String model, String apiKey, String instruction) {
            super(model, apiKey, instruction);
        }

        @Override
        public String generateContent(String tweet) {
            if (tweet.equals("pass")) {
                return """
                        {"result":true}
                        """;
            }

            if (tweet.equals("fail")) {
                return """
                        {"result":false}
                        """;
            }

            if (tweet.equals("null")) {
                return "null";
            }

            return "{";
        }

    }

    public static class MockFilehandler implements FileHandler {

        private final Queue<List<TweetRecord>> batches;

        private final List<TweetRecord> writtenResults = new ArrayList<>();
        private final List<CheckpointEntry> checkpoints = new ArrayList<>();
        private int flushCount;


        public MockFilehandler(List<List<TweetRecord>> batches) {
            this.batches = new LinkedList<>(batches);
        }

        @Override
        public List<TweetRecord> nextBatch(int batchSize) {
            return batches.isEmpty() ? List.of() : batches.poll();
        }

        @Override
        public boolean nextSegment() {
            return false;
        }

        @Override
        public void saveCheckpoint(CheckpointEntry entry) {
            checkpoints.add(entry);
        }

        @Override
        public void writeResult(TweetRecord record) {
            writtenResults.add(record);
        }

        @Override
        public void flush() {
            flushCount++;

        }

        @Override
        public void close() {

        }

        public List<TweetRecord> writtenResults() {
            return writtenResults;
        }

        public List<CheckpointEntry> checkpoints() {
            return checkpoints;
        }

        public int flushCount() {
            return flushCount;
        }

    }
}
