package org.osigie;

public class Util {


    public static  String serializeChunkData(String id, String text) {
        return String.format("%s,%s", id, text);
    }

    public static  String[] deserializeChunkData(String data) {
        String[] parts = data.split(",", 2);

        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid chunk data: " + data);
        }

        return parts;
    }

}
