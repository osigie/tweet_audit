package org.osigie;

import org.osigie.application.TweetAnalysisApplication;
import org.osigie.config.ApplicationPropertiesLoader;
import org.osigie.config.Constants;

import java.nio.file.Path;

public class TweetAudit {
    static void main() throws Exception {
        var props = ApplicationPropertiesLoader.load(
                System.getenv(),
                Path.of(Constants.DEFAULT_CONFIG_PATH)
        );
        new TweetAnalysisApplication(props).run();
    }
}
