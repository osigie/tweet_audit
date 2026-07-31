package org.osigie.io.extractor;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import java.util.zip.CRC32;

public class Entry implements Serializable {
  @Serial private static final long serialVersionUID = 1L;

  final long entryIndex;
  final byte[] data;
  final long checkSum;

  public Entry(long entryIndex, byte[] data) {
    this.entryIndex = entryIndex;
    this.data = Objects.requireNonNull(data, "data cannot be null");
    this.checkSum = calculateCheckSum(data);
  }

  public long getEntryIndex() {
    return entryIndex;
  }

  public byte[] getData() {
    return data;
  }

  /** Computes a CRC32 checksum over the given byte array for data integrity. */
  public static long calculateCheckSum(byte[] data) {
    CRC32 crc = new CRC32();
    crc.update(data);
    return crc.getValue();
  }
}
