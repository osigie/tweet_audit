package org.osigie.io.extractor;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import org.osigie.config.Constants;
import org.osigie.model.TweetWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class TweetExtractor {
    private final TweetsChunks tweetsChunks;
    private final String sourcePath;
    private final String destinationPath;
    private static final Logger log = LoggerFactory.getLogger(TweetExtractor.class);
    private final Gson gson = new Gson();

    public TweetExtractor(String destinationPath, String sourcePath, long maxChunkSize) throws IOException {

        if (destinationPath == null || destinationPath.isBlank()) {
            throw new IllegalArgumentException("Chunk directory path cannot be blank.");
        }

        if (sourcePath == null || sourcePath.isBlank()) {
            throw new IllegalArgumentException("Chunk directory path cannot be blank.");
        }

        this.sourcePath = sourcePath;
        this.destinationPath = destinationPath;

        this.tweetsChunks = new TweetsChunks(destinationPath, maxChunkSize);
    }

    public void initialize() throws IOException {
        log.info("Initializing tweet extractor using source: {}", sourcePath);

        Path path = Path.of(this.sourcePath);

        if (!Files.exists(path)) {
            throw new IOException("Source file does not exist.");
        }

        if (isExtracted()) {
            log.info("Tweets already initialized, skipping tweets initialization.");
            return;
        }
        long extractedTweetCount = 0;

        try (JsonReader jsonReader = new JsonReader(Files.newBufferedReader(path))) {
            jsonReader.beginArray();
            while (jsonReader.hasNext()) {
                TweetWrapper wrapper = gson.fromJson(jsonReader, TweetWrapper.class);

                if (isRetweeted(wrapper.tweet().fullText())) {
                    continue;
                }

                this.tweetsChunks.append(wrapper.tweet().id(), wrapper.tweet().fullText());
                extractedTweetCount++;
            }

            jsonReader.endArray();

            log.info("Successfully extracted {} tweets", extractedTweetCount);

        } catch (IOException e) {
            log.error("Error while extracting tweets: {}", e.getMessage());
            throw e;
        }
    }

    private boolean isExtracted() throws IOException {
        Path path = Path.of(destinationPath);

        if (Files.notExists(path)) {
            return false;
        }

        try (Stream<Path> paths = Files.list(path)) {
            return paths.anyMatch(p ->
                    p.getFileName().toString().startsWith(Constants.CHUNKS_PATH_PREFIX));
        }
    }


    private boolean isRetweeted(String text) throws IOException {
        return text.startsWith("RT @");
    }
}
