package  org.osigie.config;

public class ApplicationProperties {
    private final String name;
    private final String sourcePath;
    private final String chunksPath;
    private final String checkpointPath;
    private final String resultPath;
    private final String configPath;
    private final String geminiApiKey;
    private final String geminiModel;
    private final String xUserName;
    private final Criteria criteria;

    public ApplicationProperties(Builder builder) {
        this.name = builder.name;
        this.sourcePath = builder.sourcePath;
        this.chunksPath = builder.chunksPath;
        this.checkpointPath = builder.checkpointPath;
        this.resultPath = builder.resultPath;
        this.configPath = builder.configPath;
        this.geminiApiKey = builder.geminiApiKey;
        this.geminiModel = builder.geminiModel;
        this.xUserName = builder.xUserName;
        this.criteria = builder.criteria;
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

    public String getConfigPath() {
        return configPath;
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

    public static class Builder {

        //required parameters
        private final String geminiApiKey;
        private final String geminiModel;
        private final String xUserName;
        private final Criteria criteria;


        //optional parameters
        private String name = "TweetApp";
        private String sourcePath = "./data/source.json";
        private String chunksPath = "./data/chunks/*.bin";
        private String checkpointPath = "./data/checkpoint.txt";
        private String resultPath = "./data/analyzed/results.csv";
        private String configPath = "";

        private Builder(String geminiApiKey, String geminiModel, String xUserName, Criteria criteria) {
            this.geminiApiKey = geminiApiKey;
            this.geminiModel = geminiModel;
            this.xUserName = xUserName;
            this.criteria = criteria;
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

        public Builder setConfigPath(String configPath) {
            this.configPath = configPath;
            return this;
        }

        public ApplicationProperties build() {
            return new ApplicationProperties(this);
        }

    }


}
