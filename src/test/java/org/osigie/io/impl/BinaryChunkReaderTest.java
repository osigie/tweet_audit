package org.osigie.io.impl;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.osigie.Util;
import org.osigie.io.extractor.Entry;
import org.osigie.io.extractor.Segment;
import org.osigie.model.CheckpointEntry;
import org.osigie.model.TweetRecord;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BinaryChunkReaderTest {

    @DisplayName("Read binary chunk")
    @Test
    void givenInputStreamWithBinary_whenNextBatch_thenShouldReturnListOfTweet(
            @TempDir Path tempDir) throws Exception {

        Path source = tempDir.resolve("tweets_0.bin");

        writeEntry(source, 0, "id-test", "text");

        try (BinaryChunkReader reader =
                     new BinaryChunkReader(
                             Files.newInputStream(source),
                             new CheckpointEntry(0, 0),
                             0)) {

            List<TweetRecord> tweetRecords = reader.nextBatch(10);

            assertEquals(1, tweetRecords.size());

            TweetRecord tweet = tweetRecords.getFirst();

            assertEquals(0, tweet.entryIndex());
            assertEquals("id-test", tweet.id());
            assertEquals("text", tweet.text());
        }
    }

    @DisplayName("Skip entries before checkpoint")
    @Test
    void givenCheckpoint_whenNextBatch_thenShouldSkipProcessedEntries(
            @TempDir Path tempDir) throws Exception {

        Path source = tempDir.resolve("tweets_0.bin");

        writeEntry(source, 0, "id-1", "first");
        writeEntry(source, 1, "id-2", "second");

        try (BinaryChunkReader reader =
                     new BinaryChunkReader(
                             Files.newInputStream(source),
                             new CheckpointEntry(1, 0),
                             0)) {

            List<TweetRecord> tweets = reader.nextBatch(10);

            assertEquals(1, tweets.size());

            TweetRecord tweet = tweets.getFirst();

            assertEquals(1, tweet.entryIndex());
            assertEquals("id-2", tweet.id());
            assertEquals("second", tweet.text());
        }
    }

    @DisplayName("Return batch up to requested size")
    @Test
    void givenMoreEntriesThanBatchSize_whenNextBatch_thenReturnBatchSizeRecords(
            @TempDir Path tempDir) throws Exception {

        Path source = tempDir.resolve("tweets_0.bin");

        writeEntry(source, 0, "1", "one");
        writeEntry(source, 1, "2", "two");
        writeEntry(source, 2, "3", "three");

        try (BinaryChunkReader reader =
                     new BinaryChunkReader(
                             Files.newInputStream(source),
                             new CheckpointEntry(0, 0),
                             0)) {

            List<TweetRecord> first = reader.nextBatch(2);
            List<TweetRecord> second = reader.nextBatch(2);

            assertEquals(2, first.size());
            assertEquals(1, second.size());

            assertEquals("3", second.getFirst().id());
        }
    }

    @DisplayName("Return empty batch after reaching end of file")
    @Test
    void givenEndOfFile_whenNextBatch_thenReturnEmptyList(
            @TempDir Path tempDir) throws Exception {

        Path source = tempDir.resolve("tweets_0.bin");

        writeEntry(source, 0, "id-test", "text");

        try (BinaryChunkReader reader =
                     new BinaryChunkReader(
                             Files.newInputStream(source),
                             new CheckpointEntry(0, 0),
                             0)) {

            assertEquals(1, reader.nextBatch(10).size());
            assertTrue(reader.nextBatch(10).isEmpty());
        }
    }

    @DisplayName("Throw IOException when checksum does not match")
    @Test
    void givenCorruptedChunk_whenNextBatch_thenThrowIOException(
            @TempDir Path tempDir) throws Exception {

        Path source = tempDir.resolve("tweets_0.bin");

        writeEntry(source, 0, "id-test", "text");

        byte[] bytes = Files.readAllBytes(source);

        bytes[bytes.length - 1] ^= 0x01;

        Files.write(source, bytes);

        try (BinaryChunkReader reader =
                     new BinaryChunkReader(
                             Files.newInputStream(source),
                             new CheckpointEntry(0, 0),
                             0)) {

            Exception ex = assertThrows(
                    IOException.class,
                    () -> reader.nextBatch(10));

            assertEquals("Checksum mismatch", ex.getMessage());
        }
    }

    private void writeEntry(
            Path source,
            long entryIndex,
            String id,
            String text) throws Exception {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try (DataOutputStream dos = new DataOutputStream(baos)) {
            dos.writeUTF(Util.serializeChunkData(id, text));
        }

        Entry entry = new Entry(entryIndex, baos.toByteArray());

        new Segment(source, 0).writeEntry(entry);
    }
}
