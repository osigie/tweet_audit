package org.osigie.auditor;

import org.osigie.model.TweetRecord;

public interface AuditClient {

    boolean audit(TweetRecord tweet);
}
