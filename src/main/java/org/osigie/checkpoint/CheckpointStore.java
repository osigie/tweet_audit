package org.osigie.checkpoint;

import org.osigie.model.CheckpointEntry;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Optional;

public class CheckpointStore {
    private final Path checkpoint;

    public CheckpointStore(String checkpointPath) {
        this.checkpoint = Path.of(checkpointPath);
    }

    public void save(CheckpointEntry entry) throws IOException {
        Files.writeString(checkpoint, serialize(entry), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    private String serialize(CheckpointEntry entry) {
        return String.format("%s,%s", entry.entryIndex(), entry.chunkIndex());
    }

    private CheckpointEntry deserialize(String entry) {
        String[] parts = entry.split(",");
        return new CheckpointEntry(Long.parseLong(parts[0]), Integer.parseInt(parts[1]));
    }

    public Optional<CheckpointEntry> load() throws IOException {
        if (!Files.exists(checkpoint)) return Optional.empty();
        String content = Files.readString(checkpoint).trim();
        return Optional.of(deserialize(content));
    }
}
