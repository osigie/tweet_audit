package org.osigie;

import java.nio.file.Path;

import org.osigie.application.TweetAnalysisApplication;
import org.osigie.config.ApplicationPropertiesLoader;
import org.osigie.config.Constants;

public final class TweetAudit {
  private TweetAudit() { }
  static void main() throws Exception {
    var props =
        ApplicationPropertiesLoader.load(System.getenv(), Path.of(Constants.DEFAULT_CONFIG_PATH));
    new TweetAnalysisApplication(props).run();
  }
}
