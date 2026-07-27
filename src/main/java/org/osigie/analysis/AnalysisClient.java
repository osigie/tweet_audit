package org.osigie.analysis;

import org.osigie.model.TweetRecord;

public interface AnalysisClient {

    boolean analyze(TweetRecord tweet);
}
