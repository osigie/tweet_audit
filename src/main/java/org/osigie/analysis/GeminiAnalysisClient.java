package org.osigie.analysis;

import org.osigie.model.TweetRecord;

public class GeminiAnalysisClient implements AnalysisClient {
    private final GeminiClient geminiClient;

    public GeminiAnalysisClient(GeminiClient geminiClient) {
        this.geminiClient = geminiClient;
    }

    @Override
    public boolean analyze(TweetRecord tweet) {
        return geminiClient.generateContent(tweet.text()).result();
    }
}
