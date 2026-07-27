package org.osigie.io;

import org.osigie.model.CheckpointEntry;

import java.io.IOException;
import java.util.Optional;

public interface CheckpointStore {
    void save(CheckpointEntry entry) throws IOException;
    Optional<CheckpointEntry> load() throws IOException;
}
