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
        String geminiApiKey = System.getenv(Constants.GEMINI_API_KEY);
        String geminiModel = System.getenv(Constants.GEMINI_MODEL);
        String xUsername = System.getenv(Constants.X_USERNAME);

        Criteria criteria = setUpCriteria();

        return ApplicationProperties.Builder()
                .setGeminiApiKey(geminiApiKey)
                .setGeminiModel(geminiModel)
                .setxUserName(xUsername)
                .setCriteria(criteria)
                .build();

    }

    private static Criteria setUpCriteria() {
        Path path = Path.of(Constants.DEFAULT_CONFIG_PATH);

        if (!Files.exists(path)) {
            throw new IllegalConfigException("Configuration file does not exist");
        }

        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            Gson gson = new Gson();
            JsonObject jsonObject = gson.fromJson(reader, JsonObject.class);
            JsonElement criteria = jsonObject.get("criteria");
            return gson.fromJson(criteria, Criteria.class);
        } catch (IOException ex) {
            throw new IllegalConfigException("Configuration file is invalid");
        }
    }
}
