package org.osigie.io.impl;

import org.osigie.io.ResultWriter;
import org.osigie.model.TweetRecord;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class FileResultWriter implements ResultWriter {
    private static final String[] HEADERS = {"tweet_url", "deleted"};

    private final BufferedWriter writer;
    private final String xUserName;

    public FileResultWriter(String resultPath, String xUserName) throws IOException {

        if (resultPath == null || resultPath.isBlank()) {
            throw new IllegalArgumentException("resultPath path cannot be null or blank");
        }

        if (xUserName == null || xUserName.isBlank()) {
            throw new IllegalArgumentException("xUsername path cannot be null or blank");
        }

        Path path = Path.of(resultPath);
        this.xUserName = xUserName;

        boolean writeHeader = Files.notExists(path) || Files.size(path) == 0;

        Files.createDirectories(path.getParent());

        this.writer = Files.newBufferedWriter(path, StandardOpenOption.CREATE, StandardOpenOption.APPEND);

        if (writeHeader) {
            writer.write(String.join(",", HEADERS));
            writer.newLine();
            writer.flush();
        }
    }

    public void append(TweetRecord tweet, boolean deleted)
            throws IOException {

        writer.write(createURL(tweet.id()) + "," + deleted);
        writer.newLine();
    }

    public void flush() throws IOException {
        writer.flush();
    }

    @Override
    public void close() throws IOException {
        writer.close();
    }

    private String createURL(String tweetId) {
        return String.format("https://x.com/%s/status/%s", xUserName, tweetId);
    }
}
