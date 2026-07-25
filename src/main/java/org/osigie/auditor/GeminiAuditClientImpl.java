package org.osigie.auditor;

import org.osigie.model.TweetRecord;

public class GeminiAuditClientImpl implements AuditClient {
    @Override
    public boolean audit(TweetRecord tweet) {
        System.out.println(tweet.entryIndex());
        return false;
    }
}
