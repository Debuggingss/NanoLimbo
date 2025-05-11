package ua.nanit.limbo.world;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class BlockEntityUtils {

    private BlockEntityUtils() {}

    private static final Gson gson = new Gson();
    private static final Map<String, Integer> mappings;

    static {
        String fileName = "block_entity.json";
        InputStream inputStream = GeneratedBlockDataMappings.class.getClassLoader().getResourceAsStream(fileName);

        if (inputStream == null) {
            throw new IllegalStateException("Failed to load " + fileName + " from jar!");
        }

        try (InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
            Type type = new TypeToken<Map<String, Integer>>() {}.getType();
            mappings = gson.fromJson(reader, type);
        } catch (Exception e) {
            throw new JsonParseException("Error parsing blocks.json", e);
        }
    }

    public static int getBlockEntityId(String name) {
        return mappings.getOrDefault(name, -1);
    }
}
