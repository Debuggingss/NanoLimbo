/*
 * Copyright (C) 2020 Nan1t
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ua.nanit.limbo.world;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.ListBinaryTag;
import net.kyori.adventure.nbt.TagStringIO;
import ua.nanit.limbo.server.LimboServer;
import ua.nanit.limbo.server.Log;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public final class DimensionRegistry {

    private final LimboServer server;

    private CompoundBinaryTag codec_1_21_5;
    private CompoundBinaryTag tags_1_21_5;
    private Dimension dimension_1_21_5;

    public DimensionRegistry(LimboServer server) {
        this.server = server;
    }

    public CompoundBinaryTag getCodec_1_21_5() {
        return codec_1_21_5;
    }

    public CompoundBinaryTag getTags_1_21_5() {
        return tags_1_21_5;
    }

    public Dimension getDimension_1_21_5() {
        return dimension_1_21_5;
    }

    public void load(String def) throws IOException {
        codec_1_21_5 = readSnbtFile("/dimension/codec_1_21_5.snbt");

        tags_1_21_5 = readSnbtFile("/dimension/tags_1_21_5.snbt");

        dimension_1_21_5 = getModernDimension(def, codec_1_21_5);
    }

    private Dimension getModernDimension(String def, CompoundBinaryTag tag) {
        ListBinaryTag dimensions = tag.getList("minecraft:dimension_type");

        for (int i = 0; i < dimensions.size(); i++) {
            CompoundBinaryTag dimension = (CompoundBinaryTag) dimensions.get(i);

            String name = dimension.getString("id");
            CompoundBinaryTag world = (CompoundBinaryTag) dimension.get("value");

            if (name.startsWith(def)) {
                return new Dimension(i, name, world);
            }
        }

        CompoundBinaryTag overWorld = (CompoundBinaryTag) ((CompoundBinaryTag) dimensions.get(0)).get("element");
        Log.warning("Undefined dimension type: '%s'. Using OVERWORLD as default", def);
        return new Dimension(0, "minecraft:overworld", overWorld);
    }

    private CompoundBinaryTag readSnbtFile(String resPath) throws IOException {
        InputStream in = server.getClass().getResourceAsStream(resPath);

        if (in == null) {
            throw new FileNotFoundException("Cannot find snbt file " + resPath);
        }

        return TagStringIO.get().asCompound(streamToString(in));
    }

    private String streamToString(InputStream in) throws IOException {
        try (BufferedReader bufReader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            return bufReader.lines().collect(Collectors.joining("\n"));
        }
    }
}
