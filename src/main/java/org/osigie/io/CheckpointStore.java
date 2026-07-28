package org.osigie.io;

import java.io.IOException;
import java.util.Optional;

import org.osigie.model.CheckpointEntry;

public interface CheckpointStore {
  /** Saves a checkpoint so processing can resume from this position. */
  void save(CheckpointEntry entry) throws IOException;

  /** Loads the most recent checkpoint, if one exists. */
  Optional<CheckpointEntry> load() throws IOException;
}
