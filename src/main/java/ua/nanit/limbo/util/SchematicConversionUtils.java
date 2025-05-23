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

package ua.nanit.limbo.util;

import net.kyori.adventure.key.Key;
import net.querz.nbt.tag.CompoundTag;
import net.querz.nbt.tag.ListTag;
import net.querz.nbt.tag.StringTag;
import net.querz.nbt.tag.Tag;

public class SchematicConversionUtils {

    public static CompoundTag toTileEntityTag(CompoundTag tag) {
        int[] pos = tag.getIntArray("Pos");

        tag.remove("Pos");
        tag.remove("Id");

        tag.putInt("x", pos[0]);
        tag.putInt("y", pos[1]);
        tag.putInt("z", pos[2]);

        for (Tag<?> subTag : tag.values()) {
            removeStringTagQuote(subTag);
        }

        return tag;
    }

    public static Tag<?> removeStringTagQuote(Tag<?> tag) {
        if (tag instanceof StringTag) {
            String value = ((StringTag) tag).getValue();
            if (value.startsWith("\"") && value.endsWith("\"")) {
                ((StringTag) tag).setValue(value.substring(1, value.length() - 1));
            }
        } else if (tag instanceof CompoundTag) {
            for (Tag<?> subTag : ((CompoundTag) tag).values()) {
                removeStringTagQuote(subTag);
            }
        } else if (tag instanceof ListTag<?>) {
            for (Tag<?> subTag : (ListTag<?>) tag) {
                removeStringTagQuote(subTag);
            }
        }
        return tag;
    }

    public static CompoundTag toBlockTag(String input) {
        int index = input.indexOf("[");
        CompoundTag tag = new CompoundTag();
        if (index < 0) {
            tag.putString("Name", Key.key(input).toString());
            return tag;
        }

        tag.putString("Name", Key.key(input.substring(0, index)).toString());

        String[] states = input.substring(index + 1, input.lastIndexOf("]")).replace(" ", "").split(",");

        CompoundTag properties = new CompoundTag();
        for (String state : states) {
            String key = state.substring(0, state.indexOf("="));
            String value = state.substring(state.indexOf("=") + 1);
            properties.putString(key, value);
        }

        tag.put("Properties", properties);

        return tag;
    }

    public static CompoundTag[] unpackBlockStates(long[] blockStates, ListTag<CompoundTag> palette, int width, int height, int length) {
        int totalBlocks = width * height * length;
        int paletteSize = palette.size();
        int bitsPerEntry = Math.max(4, 32 - Integer.numberOfLeadingZeros(paletteSize - 1));
        int mask = (1 << bitsPerEntry) - 1;

        CompoundTag[] blockNames = new CompoundTag[totalBlocks];

        int bitIndex = 0;
        for (int i = 0; i < totalBlocks; i++) {
            int longIndex = bitIndex / 64;
            int startBit = bitIndex % 64;

            long value;
            if (startBit + bitsPerEntry <= 64) {
                value = (blockStates[longIndex] >>> startBit) & mask;
            } else {
                int bitsLeft = 64 - startBit;
                long low = blockStates[longIndex] >>> startBit;
                long high = blockStates[longIndex + 1] & ((1L << (bitsPerEntry - bitsLeft)) - 1);
                value = (high << bitsLeft) | low;
            }

            blockNames[i] = palette.get((int) value);

            bitIndex += bitsPerEntry;
        }

        return blockNames;
    }
}
