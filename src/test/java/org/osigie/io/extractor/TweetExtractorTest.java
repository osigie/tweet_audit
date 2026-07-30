package org.osigie.io.extractor;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.osigie.io.ChunkReader;
import org.osigie.io.impl.BinaryChunkReader;
import org.osigie.model.CheckpointEntry;
import org.osigie.model.TweetRecord;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TweetExtractorTest {


    @DisplayName("Extracts all non-retweeted tweets into chunks")
    @Test
    public void givenCorrectPathsAndSource_whenInitialize_thenCompleteExtraction(@TempDir Path tempDir) throws Exception {


        Path destinationPath = tempDir.resolve("chunks");
        Path sourcePath = tempDir.resolve("tweets.json");

        Files.writeString(
                sourcePath,
                """
                        [
                          {
                            "tweet": {
                              "id": "1",
                              "full_text": "Hello world"
                            }
                          },
                          {
                            "tweet": {
                              "id": "2",
                              "full_text": "RT @someone Retweet"
                            }
                          }
                        ]
                        """);

        new TweetExtractor(destinationPath.toString(), sourcePath.toString(), 1).initialize();

        Path chunk = destinationPath.resolve("tweets_0.bin");
        assertTrue(Files.exists(chunk));
        try (ChunkReader reader = new BinaryChunkReader(Files.newInputStream(chunk), new CheckpointEntry(0, 0), 0)) {

            List<TweetRecord> tweets = reader.nextBatch(10);

            assertEquals(1, tweets.size());
            assertEquals("Hello world", tweets.getFirst().text());
        }


    }


    @DisplayName("Skip when tweets are already initialized")
    @Test
    public void givenInitializedTweets_whenInitialize_thenShouldSkip(@TempDir Path tempDir) throws Exception {

        String directory = tempDir.toString();
        new TweetsChunks(directory, 1024 ).append("id-101","text" );

        Path destinationPath = tempDir.resolve("chunks");
        Path sourcePath = tempDir.resolve("tweets.json");

        Files.createDirectories(destinationPath);
        Path chunk = destinationPath.resolve("tweets_0.bin");

        Files.writeString(chunk, """
                test binary
                """);

        Files.writeString(
                sourcePath,
                """
                        [
                          {
                            "tweet": {
                              "id": "1",
                              "full_text": "Hello world"
                            }
                          },
                          {
                            "tweet": {
                              "id": "2",
                              "full_text": "RT @someone Retweet"
                            }
                          }
                        ]
                        """);


        new TweetExtractor(destinationPath.toString(), sourcePath.toString(), 1).initialize();

        try (Stream<Path> files = Files.list(destinationPath)) {
            assertEquals(1, files.count());
        }

        assertFalse(Files.exists(destinationPath.resolve("tweets_1.bin")));


    }

}
