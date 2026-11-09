package org.osigie.io.impl;

import java.nio.file.Path;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.osigie.model.CheckpointEntry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FileCheckpointStoreTest {


    @DisplayName("Returns empty when checkpoint file does not exist")
    @Test
    void givenMissingCheckpoint_whenLoad_thenReturnsEmpty(@TempDir Path tempDir) throws Exception {
        Path checkpoint = tempDir.resolve("checkpoint.txt");
        FileCheckpointStore store =
                new FileCheckpointStore(checkpoint.toString());

        assertTrue(store.load().isEmpty());
    }

    @DisplayName("Saves and loads checkpoint")
    @Test
    void givenCheckpoint_whenSave_thenLoadReturnsSameCheckpoint(@TempDir Path tempDir) throws Exception {

        Path checkpoint = tempDir.resolve("checkpoint.txt");
        FileCheckpointStore store =
                new FileCheckpointStore(checkpoint.toString());

        CheckpointEntry expected =
                new CheckpointEntry(15, 1024);
        store.save(expected);

        Optional<CheckpointEntry> actual = store.load();

        assertTrue(actual.isPresent());
        assertEquals(expected, actual.get());
    }

    @DisplayName("Overwrites existing checkpoint")
    @Test
    void givenExistingCheckpoint_whenSave_thenOverwritesPreviousCheckpoint(@TempDir Path tempDir) throws Exception {
        Path checkpoint = tempDir.resolve("checkpoint.txt");
        FileCheckpointStore store =
                new FileCheckpointStore(checkpoint.toString());
        store.save(new CheckpointEntry(5, 100));
        store.save(new CheckpointEntry(20, 500));

        assertEquals(
                new CheckpointEntry(20, 500),
                store.load().orElseThrow());
    }
}
