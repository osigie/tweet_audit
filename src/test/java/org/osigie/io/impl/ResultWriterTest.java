package org.osigie.io.impl;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.osigie.model.TweetRecord;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ResultWriterTest {

    @DisplayName("Append result to result sheet")
    @Test
    public void givenTweetRecord_whenAppend_thenShouldWriteHeadAndRecord(@TempDir Path tempDir) throws Exception {


        Path resultPath = tempDir.resolve("result.csv");
        TweetRecord tweetRecord = new TweetRecord(1L, 0, "id-test", "hey");

        try (FileResultWriter writer = new FileResultWriter(resultPath.toString(), "osigie")) {
            writer.append(tweetRecord, true);
            writer.flush();
        }

        assertEquals(
                """
                        tweet_url,deleted
                        https://x.com/osigie/status/id-test,true
                        """.stripTrailing(),
                Files.readString(resultPath).stripTrailing());

    }


    @DisplayName("Does not duplicate the header when appending")
    @Test
    void givenExistingResultFile_whenAppend_thenDoesNotWriteHeaderAgain(
            @TempDir Path tempDir) throws Exception {

        Path result = tempDir.resolve("result.csv");

        try (FileResultWriter writer =
                     new FileResultWriter(result.toString(), "osigie")) {
            writer.append(new TweetRecord(1L, 0, "1", ""), true);
            writer.flush();
        }

        try (FileResultWriter writer =
                     new FileResultWriter(result.toString(), "osigie")) {
            writer.append(new TweetRecord(2L, 1, "2", ""), false);
            writer.flush();
        }

        assertEquals(
                3,
                Files.readAllLines(result).size());
    }
}
