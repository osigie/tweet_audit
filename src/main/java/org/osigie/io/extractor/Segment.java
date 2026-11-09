package org.osigie.io.extractor;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;

public class Segment {

  private final Path path;
  private final long baseOffset;
  private long currentSize;

  public Segment(Path path, long baseOffset) throws IOException {
    this.path = Objects.requireNonNull(path, "path cannot be null");
    this.baseOffset = baseOffset;
    this.currentSize = Files.exists(path) ? Files.size(path) : 0;
  }

  public synchronized void writeEntry(Entry entry) throws IOException {
    try (OutputStream os =
            new BufferedOutputStream(
                Files.newOutputStream(path, StandardOpenOption.CREATE, StandardOpenOption.APPEND));
        DataOutputStream dos = new DataOutputStream(os)) {

      dos.writeLong(entry.entryIndex);
      dos.writeLong(entry.checkSum);
      dos.writeInt(entry.data.length);
      dos.write(entry.data);

      os.flush();

      this.currentSize = Files.size(path);
    }
  }

  public long getBaseOffset() {
    return baseOffset;
  }

  public long getCurrentSize() {
    return currentSize;
  }

  public Path getPath() {
    return path;
  }
}
