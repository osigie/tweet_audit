package org.osigie.config;

import java.util.Objects;

public class ApplicationProperties {
    private final String name;
    private final String sourcePath;
    private final String chunksPath;
    private final String checkpointPath;
    private final String resultPath;
    private final String geminiApiKey;
    private final String geminiModel;
    private final String xUserName;
    private final Criteria criteria;
    private final long chunksMaxSizeInBytes;


    private ApplicationProperties(Builder builder) {
        this.name = builder.name;
        this.sourcePath = builder.sourcePath;
        this.chunksPath = builder.chunksPath;
        this.checkpointPath = builder.checkpointPath;
        this.resultPath = builder.resultPath;
        this.geminiApiKey = builder.geminiApiKey;
        this.geminiModel = builder.geminiModel;
        this.xUserName = builder.xUserName;
        this.criteria = builder.criteria;
        this.chunksMaxSizeInBytes = builder.chunksMaxSizeInBytes;
    }


    public String getName() {
        return name;
    }

    public String getSourcePath() {
        return sourcePath;
    }

    public String getChunksPath() {
        return chunksPath;
    }

    public String getCheckpointPath() {
        return checkpointPath;
    }

    public String getResultPath() {
        return resultPath;
    }


    public String getGeminiApiKey() {
        return geminiApiKey;
    }

    public String getGeminiModel() {
        return geminiModel;
    }

    public String getxUserName() {
        return xUserName;
    }

    public Criteria getCriteria() {
        return criteria;
    }

    public long getChunksMaxSizeInBytes() {
        return chunksMaxSizeInBytes;
    }

    public static class Builder {

        private Criteria criteria;
        private String geminiApiKey;
        private String geminiModel;
        private String xUserName;


        private String name = Constants.DEFAULT_APP_NAME;
        private String sourcePath = Constants.DEFAULT_SOURCE_PATH;
        private String chunksPath = Constants.DEFAULT_CHUNKS_DIR;
        private String checkpointPath = Constants.DEFAULT_CHECKPOINT_PATH;
        private String resultPath = Constants.DEFAULT_RESULT_PATH;
        private long chunksMaxSizeInBytes = Constants.DEFAULT_CHUNKS_MAX_SIZE_IN_BYTES;

        public Builder() {
        }


        public Builder setChunksMaxSizeInBytes(long chunksMaxSizeInBytes) {
            this.chunksMaxSizeInBytes = chunksMaxSizeInBytes;
            return this;
        }

        public Builder setCriteria(Criteria criteria) {
            this.criteria = criteria;
            return this;
        }

        public Builder setGeminiApiKey(String geminiApiKey) {
            this.geminiApiKey = geminiApiKey;
            return this;
        }

        public Builder setGeminiModel(String geminiModel) {
            this.geminiModel = geminiModel;
            return this;
        }

        public Builder setxUserName(String xUserName) {
            this.xUserName = xUserName;
            return this;
        }


        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setSourcePath(String sourcePath) {
            this.sourcePath = sourcePath;
            return this;
        }

        public Builder setChunksPath(String chunksPath) {
            this.chunksPath = chunksPath;
            return this;
        }

        public Builder setCheckpointPath(String checkpointPath) {
            this.checkpointPath = checkpointPath;
            return this;
        }

        public Builder setResultPath(String resultPath) {
            this.resultPath = resultPath;
            return this;
        }


        public ApplicationProperties build() {
            Objects.requireNonNull(criteria, "criteria cannot be null");
            Objects.requireNonNull(geminiApiKey, "geminiApiKey cannot be null");
            Objects.requireNonNull(geminiModel, "geminiModel cannot be null");
            Objects.requireNonNull(xUserName, "xUserName cannot be null");
            return new ApplicationProperties(this);
        }

    }

    public static Builder Builder() {
        return new Builder();
    }


}
