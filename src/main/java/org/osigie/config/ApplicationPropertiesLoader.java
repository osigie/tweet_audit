package org.osigie.config;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.osigie.exception.IllegalConfigException;

public final class ApplicationPropertiesLoader {

    private ApplicationPropertiesLoader() {
    }

    public static ApplicationProperties load() {
        return load(System.getenv(), Path.of(Constants.DEFAULT_CONFIG_PATH));
    }

    public static ApplicationProperties load(Map<String, String> env, Path configPath) {
        String geminiApiKey = env.get(Constants.GEMINI_API_KEY);
        String geminiModel = env.getOrDefault(Constants.GEMINI_MODEL, "gemini-2.5-flash");
        String username = env.get(Constants.X_USERNAME);
        int batchSize = Integer.parseInt(
                env.getOrDefault(Constants.BATCH_SIZE, "10"));


        Criteria criteria = loadCriteria(configPath);

        return ApplicationProperties.builder()
                .setGeminiApiKey(geminiApiKey)
                .setGeminiModel(geminiModel)
                .setUsername(username)
                .setBatchSize(batchSize)
                .setCriteria(criteria)
                .build();
    }

    private static Criteria loadCriteria(Path configPath) {
        if (!Files.exists(configPath)) {
            throw new IllegalConfigException("Configuration file does not exist: " + configPath);
        }

        try (Reader reader = Files.newBufferedReader(configPath, StandardCharsets.UTF_8)) {
            Gson gson = new Gson();
            JsonObject jsonObject = gson.fromJson(reader, JsonObject.class);
            JsonElement criteria = jsonObject.get("criteria");
            return gson.fromJson(criteria, Criteria.class);
        } catch (IOException ex) {
            throw new IllegalConfigException("Configuration file is invalid");
        }
    }

}
