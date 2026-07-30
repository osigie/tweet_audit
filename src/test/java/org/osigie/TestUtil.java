package org.osigie;

import org.osigie.analysis.AnalysisClient;
import org.osigie.analysis.GeminiClient;
import org.osigie.model.TweetRecord;

public final class TestUtil {
    private TestUtil() {
    }

    public static class MockAnalysisClient implements AnalysisClient {

        @Override
        public boolean analyze(TweetRecord tweet) {
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
}
