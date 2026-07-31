package org.osigie.io.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Optional;

import org.osigie.io.CheckpointStore;
import org.osigie.model.CheckpointEntry;

public class FileCheckpointStore implements CheckpointStore {
  private final Path checkpoint;

  public FileCheckpointStore(String checkpointPath) {

    if (checkpointPath == null || checkpointPath.isBlank()) {
      throw new IllegalArgumentException("checkpoint path cannot be null or blank");
    }
    this.checkpoint = Path.of(checkpointPath);
  }

  public void save(CheckpointEntry entry) throws IOException {
    Files.writeString(
        checkpoint,
        serialize(entry),
        StandardOpenOption.CREATE,
        StandardOpenOption.TRUNCATE_EXISTING);
  }

  private String serialize(CheckpointEntry entry) {
    return String.format("%s,%s", entry.entryIndex(), entry.segmentBaseOffset());
  }

  private CheckpointEntry deserialize(String entry) {
    String[] parts = entry.split(",");
    return new CheckpointEntry(Long.parseLong(parts[0]), Long.parseLong(parts[1]));
  }

  public Optional<CheckpointEntry> load() throws IOException {
    if (!Files.exists(checkpoint)) {
      return Optional.empty();
    }
    String content = Files.readString(checkpoint).trim();
    return Optional.of(deserialize(content));
  }
}
