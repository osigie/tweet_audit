package org.osigie.io.extractor;

import org.osigie.Util;
import org.osigie.config.Constants;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class TweetsChunks {
    private final String chunkDirectory;
    private final long maxSegmentSize;
    private Segment openSegment;
    private long nextIndex;


    public TweetsChunks(String directory, long maxSegmentSize) throws IOException {
        this.chunkDirectory = directory;
        this.maxSegmentSize = maxSegmentSize;

        createNewSegment(0);
    }


    private void createNewSegment(long startIndex) throws IOException {
        Path path = Path.of(chunkDirectory, Constants.CHUNKS_PATH_PREFIX + startIndex + ".bin");
        Files.createDirectories(path.getParent());
        openSegment = new Segment(path, startIndex);
    }

    public synchronized void append(String id, String text) throws IOException {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try (DataOutputStream dos = new DataOutputStream(baos)) {
            dos.writeUTF(Util.serializeChunkData(id, text));
        }

        if (openSegment.getCurrentSize() >= maxSegmentSize) {
            createNewSegment(nextIndex);
        }

        Entry entry = new Entry(nextIndex++, baos.toByteArray());
        openSegment.writeEntry(entry);

    }

}
