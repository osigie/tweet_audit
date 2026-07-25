package org.osigie.auditor;

import org.osigie.model.CheckpointEntry;
import org.osigie.model.TweetRecord;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ChunkReader implements AutoCloseable {
    private final DataInputStream input;
    private final CheckpointEntry checkpointEntry;

    public ChunkReader(Path path, CheckpointEntry checkpointEntry) throws IOException {
        this.checkpointEntry = checkpointEntry;
        this.input = new DataInputStream(new BufferedInputStream(Files.newInputStream(path)));
    }

    public List<TweetRecord> nextBatch(int batchSize) throws IOException {
        List<TweetRecord> records = new ArrayList<>(batchSize);

        while (records.size() < batchSize) {
            try {
                long entryIndex = input.readLong();

                if (entryIndex < checkpointEntry.entryIndex()) {
                    continue;
                }

                String id = input.readUTF();
                String text = input.readUTF();

                records.add(new TweetRecord(entryIndex, checkpointEntry.chunkIndex(), id, text));
            } catch (EOFException e) {
                break;
            }
        }
        return records;
    }


    @Override
    public void close() throws Exception {
        input.close();
    }
}
