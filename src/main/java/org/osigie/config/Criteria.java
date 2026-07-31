package org.osigie.config;

import java.util.List;

import com.google.gson.annotations.SerializedName;

/** Moderation criteria loaded from the configuration file. */
public record Criteria(
    @SerializedName("forbidden_words") List<String> forbiddenWords,
    @SerializedName("professional_check") boolean professionalCheck,
    @SerializedName("tone") String tone,
    @SerializedName("exclude_politics") boolean excludePolitics) { }
