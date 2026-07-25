package org.osigie.config;

public final class Constants {
    private Constants() {
    }

    //ENV KEYS
    public static final String GEMINI_API_KEY = "GEMINI_API_KEY";
    public static final String GEMINI_MODEL = "GEMINI_MODEL";
    public static final String X_USERNAME = "X_USERNAME";


    public static final String DEFAULT_CONFIG_PATH = "config.json";
    public static final String DEFAULT_CHUNKS_DIR = "./data/chunks/";
    public static final String DEFAULT_CHECKPOINT_PATH = "./data/checkpoint.txt";
    public static final String DEFAULT_SOURCE_PATH = "./data/source/tweets.json";
    public static final String DEFAULT_RESULT_PATH = "./data/analyzed/results.csv";
    public static final String DEFAULT_APP_NAME = "TweetAudit";
    public static final long DEFAULT_CHUNKS_MAX_SIZE_IN_BYTES = 1024;

    //    APP CONSTANTS
    public static final String CHUNKS_PATH_PREFIX = "tweets_";


}
