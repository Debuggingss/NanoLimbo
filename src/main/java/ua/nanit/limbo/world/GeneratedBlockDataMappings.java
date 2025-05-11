/*
 * This file is part of Limbo.
 *
 * Copyright (C) 2022. LoohpJames <jamesloohp@gmail.com>
 * Copyright (C) 2022. Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ua.nanit.limbo.world;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import net.querz.nbt.io.SNBTUtil;
import net.querz.nbt.tag.CompoundTag;
import ua.nanit.limbo.server.Log;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class GeneratedBlockDataMappings {

    private GeneratedBlockDataMappings() {}

    private static final JsonObject globalPalette;

    static {
        String block = "blocks.json";
        InputStream inputStream = GeneratedBlockDataMappings.class.getClassLoader().getResourceAsStream(block);
        if (inputStream == null) {
            throw new IllegalStateException("Failed to load " + block + " from jar!");
        }
        try (InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
            globalPalette = JsonParser.parseReader(reader).getAsJsonObject();
        } catch (Exception e) {
            throw new JsonParseException("Error parsing blocks.json", e);
        }
    }

    public static int getGlobalPaletteIDFromState(CompoundTag tag) {
        try {
            String blockname = tag.getString("Name");

            JsonObject data = globalPalette.getAsJsonObject(blockname);
            JsonElement propsElement = data.get("properties");

            if (propsElement == null || propsElement.isJsonNull()) {
                return data.getAsJsonArray("states").get(0).getAsJsonObject().get("id").getAsInt();
            }

            if (tag.containsKey("Properties")) {
                CompoundTag blockProp = tag.get("Properties", CompoundTag.class);
                Map<String, String> blockstate = new HashMap<>();
                for (String key : blockProp.keySet()) {
                    blockstate.put(key, blockProp.getString(key));
                }

                for (JsonElement entryElem : data.getAsJsonArray("states")) {
                    JsonObject entry = entryElem.getAsJsonObject();
                    JsonObject entryProps = entry.getAsJsonObject("properties");

                    boolean matches = entryProps.entrySet().stream()
                            .allMatch(e -> Objects.equals(blockstate.get(e.getKey()), e.getValue().getAsString()));

                    if (matches) {
                        return entry.get("id").getAsInt();
                    }
                }
            }

            for (JsonElement entryElem : data.getAsJsonArray("states")) {
                JsonObject entry = entryElem.getAsJsonObject();
                if (entry.has("default") && entry.get("default").getAsBoolean()) {
                    return entry.get("id").getAsInt();
                }
            }

            throw new IllegalStateException();
        } catch (Exception e) {
            String snbt;
            try {
                snbt = SNBTUtil.toSNBT(tag);
            } catch (Exception e1) {
                snbt = tag.valueToString();
            }
            Log.error(new IllegalStateException("Unable to get global palette id for " + snbt + " (Is this scheme created in the same Minecraft version as Limbo?)", e));
        }
        return 0;
    }
}
