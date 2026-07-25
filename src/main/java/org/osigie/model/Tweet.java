package org.osigie.model;

import com.google.gson.annotations.SerializedName;

public record Tweet(
        @SerializedName("id")
        String id,
        @SerializedName("full_text")
        String fullText
) {
}
