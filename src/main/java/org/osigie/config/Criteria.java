package org.osigie.config;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public record Criteria(

        @SerializedName("forbidden_words")
        List<String> forbiddenWords,

        @SerializedName("professional_check")
        boolean professionalCheck,

        @SerializedName("tone")
        String tone,

        @SerializedName("exclude_politics")
        boolean excludePolitics
) {
}
