package org.osigie;

import org.osigie.config.ApplicationProperties;
import org.osigie.config.ApplicationPropertiesLoader;

public class TweetAuditApp {
    static void main() {
        /*
        FILE -> SEGMENTED FILES (CHUNKS)[] -> CHECKPOINT(SKIP) -> ANALYZE -> WRITE RESULT

        * */

        ApplicationProperties ppties = ApplicationPropertiesLoader.load();
        System.out.println(ppties.getCriteria());
    }
}
