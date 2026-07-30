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
    private final AnalysisClient analysisClient;

    public TweetAnalysisApplication(ApplicationProperties properties) {
        if (properties == null) {
            throw new IllegalArgumentException("properties cannot be null");
        }

        AnalysisClient analysisClient = GeminiAnalysisClient
                .fromCriteria(properties.getGeminiModel(),
                        properties.getGeminiApiKey(), properties.getCriteria());

        this(properties, analysisClient);
    }

    //package private constructor for testing to allow mocking AnalysisClient
    TweetAnalysisApplication(ApplicationProperties properties, AnalysisClient analysisClient) {

        if (properties == null) {
            throw new IllegalArgumentException("properties cannot be null");
        }

        if (analysisClient == null) {
            throw new IllegalArgumentException("analysisClient cannot be null");
        }
        this.properties = properties;
        this.analysisClient = analysisClient;
    }

    public void run() throws Exception {
        TweetExtractor tweetExtractor =
                new TweetExtractor(
                        properties.getChunksPath(),
                        properties.getSourcePath(),
                        properties.getChunksMaxSizeInBytes());

        tweetExtractor.initialize();


        FileHandler fileHandler =
                new DiskFileHandler(
                        properties.getChunksPath(),
                        properties.getCheckpointPath(),
                        properties.getResultPath(),
                        properties.getUsername());

        TweetAnalyzer tweetAnalyzer =
                new TweetAnalyzer(analysisClient, fileHandler, properties.getBatchSize());

        tweetAnalyzer.run();
    }
}
