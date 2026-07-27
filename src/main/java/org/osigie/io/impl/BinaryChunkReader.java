package org.osigie.io.impl;

import org.osigie.Util;
import org.osigie.io.ChunkReader;
import org.osigie.io.extractor.Entry;
import org.osigie.model.CheckpointEntry;
import org.osigie.model.TweetRecord;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class BinaryChunkReader implements ChunkReader {
    private final DataInputStream input;
    private final CheckpointEntry checkpointEntry;
    private final long segmentBaseOffset;

    public BinaryChunkReader(InputStream input, CheckpointEntry checkpointEntry, long segmentBaseOffset) {
        this.checkpointEntry = checkpointEntry;
        this.input = new DataInputStream(new BufferedInputStream(input));
        this.segmentBaseOffset = segmentBaseOffset;
    }

    public List<TweetRecord> nextBatch(int batchSize) throws IOException {
        List<TweetRecord> records = new ArrayList<>(batchSize);

        while (records.size() < batchSize) {
            try {

                long entryIndex = input.readLong();

                long savedCheckSum = input.readLong();

                int length = input.readInt();

                byte[] data = new byte[length];

                input.readFully(data);

                if (entryIndex < checkpointEntry.entryIndex()) {
                    continue;
                }

                if (savedCheckSum != Entry.calculateCheckSum(data)) {
                    throw new IOException("Checksum mismatch");
                }

                try (DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data))) {

                    String entry = dis.readUTF();

                    String[] entryData = Util.deserializeChunkData(entry);
                    records.add(new TweetRecord(entryIndex, segmentBaseOffset, entryData[0], entryData[1]));
                }

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
