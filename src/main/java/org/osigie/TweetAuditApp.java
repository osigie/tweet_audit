package org.osigie;

import org.osigie.auditor.AuditClient;
import org.osigie.auditor.AuditResultWriter;
import org.osigie.auditor.GeminiAuditClientImpl;
import org.osigie.auditor.TweetAuditor;
import org.osigie.checkpoint.CheckpointStore;
import org.osigie.config.ApplicationProperties;
import org.osigie.config.ApplicationPropertiesLoader;
import org.osigie.extractor.TweetExtractor;

import java.io.IOException;

public class TweetAuditApp {
    static void main() throws IOException {
        /*
        FILE -> SEGMENTED FILES (CHUNKS)[] -> CHECKPOINT(SKIP) -> ANALYZE -> WRITE RESULT

        * */

        ApplicationProperties ppties = ApplicationPropertiesLoader.load();

        TweetExtractor tweetExtractor = new TweetExtractor(ppties.getChunksPath(), ppties.getSourcePath(), ppties.getChunksMaxSizeInBytes());

        tweetExtractor.initialize();

        AuditClient auditClient = new GeminiAuditClientImpl();
        AuditResultWriter auditResultWriter = new AuditResultWriter(ppties.getResultPath(), ppties.getxUserName());
        CheckpointStore checkpointStore = new CheckpointStore(ppties.getCheckpointPath());

        TweetAuditor tweetAuditor = new TweetAuditor(auditClient, ppties.getChunksPath(), auditResultWriter, checkpointStore);

        tweetAuditor.run();
    }
}
