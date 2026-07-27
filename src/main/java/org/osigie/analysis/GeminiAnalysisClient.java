package org.osigie.analysis;

import org.osigie.model.TweetRecord;

public class GeminiAnalysisClient implements AnalysisClient {
    @Override
    public boolean analyze(TweetRecord tweet) {
        System.out.println(tweet.entryIndex());
        return false;
    }
}
