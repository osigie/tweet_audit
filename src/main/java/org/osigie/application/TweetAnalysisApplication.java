package org.osigie.application;

import org.osigie.analysis.AnalysisClient;
import org.osigie.analysis.GeminiAnalysisClient;
import org.osigie.analysis.TweetAnalyzer;
import org.osigie.config.ApplicationProperties;
import org.osigie.io.FileHandler;
import org.osigie.io.extractor.TweetExtractor;
import org.osigie.io.impl.DiskFileHandler;

public class TweetAnalysisApplication {
    private final ApplicationProperties properties;

    public TweetAnalysisApplication(ApplicationProperties properties) {
        this.properties = properties;
    }

    public void run() throws Exception {
        TweetExtractor tweetExtractor = new TweetExtractor(
                properties.getChunksPath(),
                properties.getSourcePath(),
                properties.getChunksMaxSizeInBytes()
        );

        tweetExtractor.initialize();

        AnalysisClient analysisClient = new GeminiAnalysisClient();

        FileHandler fileHandler = new DiskFileHandler(
                properties.getChunksPath(),
                properties.getCheckpointPath(),
                properties.getResultPath(),
                properties.getxUserName()
        );

        TweetAnalyzer tweetAnalyzer = new TweetAnalyzer(
                analysisClient,
                fileHandler,
                properties.getBatchSize()
        );

        tweetAnalyzer.run();
    }
}
