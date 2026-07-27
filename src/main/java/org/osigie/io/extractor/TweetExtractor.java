package org.osigie.io.extractor;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import org.osigie.config.Constants;
import org.osigie.model.TweetWrapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class TweetExtractor {
    private final TweetsChunks tweetsChunks;
    private final String sourcePath;
    private final String destinationPath;

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
        Path path = Path.of(this.sourcePath);

        if (!Files.exists(path)) {
            throw new RuntimeException("Source file does not exist.");
        }

        if (isExtracted()) {
            System.out.println("Tweets already extracted.");
            return;
        }

        Gson gson = new Gson();
        try (JsonReader jsonReader = new JsonReader(Files.newBufferedReader(path))) {
            jsonReader.beginArray();
            while (jsonReader.hasNext()) {
                TweetWrapper wrapper = gson.fromJson(jsonReader, TweetWrapper.class);
                this.tweetsChunks.append(wrapper.tweet().id(), wrapper.tweet().fullText());
            }

            jsonReader.endArray();

        } catch (IOException e) {
            throw new RuntimeException(e);
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

}
