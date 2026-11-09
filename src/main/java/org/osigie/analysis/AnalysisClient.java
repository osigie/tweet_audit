package org.osigie.analysis;

import org.osigie.model.TweetRecord;

public interface AnalysisClient {
  /** @return true if the tweet violates a moderation criterion and should be flagged */
  boolean analyze(TweetRecord tweet);
}
