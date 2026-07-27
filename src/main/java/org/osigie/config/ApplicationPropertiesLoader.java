package org.osigie.config;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.osigie.exception.IllegalConfigException;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class ApplicationPropertiesLoader {

    //SOURCE FILE - JSON
    //CRITERIA - JSON
    //CHUNKS - Binary
    //CHECKPOINT - UTF-8
    //RESULT - CSV
   /*
   - I have to be able to read from JSON
   - I have to be able to write to binary
   - I have to be able to write to UTF-8
   - I have to be able to write to CSV
   * */

    private ApplicationPropertiesLoader() {
    }

    public static ApplicationProperties load() {
        return load(System.getenv(), Path.of(Constants.DEFAULT_CONFIG_PATH));
    }

    public static ApplicationProperties load(Map<String, String> env, Path configPath) {
        String geminiApiKey = env.get(Constants.GEMINI_API_KEY);
        String geminiModel = env.get(Constants.GEMINI_MODEL);
        String xUsername = env.get(Constants.X_USERNAME);

        Criteria criteria = loadCriteria(configPath);

        return ApplicationProperties.Builder()
                .setGeminiApiKey(geminiApiKey)
                .setGeminiModel(geminiModel)
                .setxUserName(xUsername)
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
