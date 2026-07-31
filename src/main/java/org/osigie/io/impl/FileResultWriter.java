package org.osigie.io.impl;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import org.osigie.io.ResultWriter;
import org.osigie.model.TweetRecord;

public class FileResultWriter implements ResultWriter {
  private static final String[] HEADERS = {"tweet_url", "deleted"};

  private final BufferedWriter writer;
  private final String username;

  public FileResultWriter(String resultPath, String username) throws IOException {

    if (resultPath == null || resultPath.isBlank()) {
      throw new IllegalArgumentException("resultPath path cannot be null or blank");
    }

    if (username == null || username.isBlank()) {
      throw new IllegalArgumentException("username cannot be null or blank");
    }

    Path path = Path.of(resultPath);
    this.username = username;

    boolean writeHeader = Files.notExists(path) || Files.size(path) == 0;

    Files.createDirectories(path.getParent());

    this.writer =
        Files.newBufferedWriter(path, StandardOpenOption.CREATE, StandardOpenOption.APPEND);

    if (writeHeader) {
      writer.write(String.join(",", HEADERS));
      writer.newLine();
      writer.flush();
    }
  }

  public void append(TweetRecord tweet, boolean deleted) throws IOException {

    writer.write(createUrl(tweet.id()) + "," + deleted);
    writer.newLine();
  }

  public void flush() throws IOException {
    writer.flush();
  }

  @Override
  public void close() throws IOException {
    writer.close();
  }

  private String createUrl(String tweetId) {
    return String.format("https://x.com/%s/status/%s", username, tweetId);
  }
}
