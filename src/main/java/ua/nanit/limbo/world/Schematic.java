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

import net.querz.mca.Chunk;
import net.querz.nbt.tag.CompoundTag;
import net.querz.nbt.tag.ListTag;
import ua.nanit.limbo.util.SchematicConversionUtils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Schematic {

    // God knows what this is
    private static final long[] MOTION_BLOCKING = new long[] {1371773531765642314L,1389823183635651148L,1371738278539598925L,1389823183635388492L,1353688558756731469L,1389823114781694027L,1317765589597723213L,1371773531899860042L,1389823183635651149L,1371773462911685197L,1389823183635650636L,1353688626805119565L,1371773531900123211L,1335639250618849869L,1371738278674077258L,1389823114781694028L,1353723811310638154L,1371738278674077259L,1335674228429068364L,1335674228429067338L,1335674228698027594L,1317624576693539402L,1335709481520370249L,1299610178184057417L,1335638906349064264L,1299574993811968586L,1299574924958011464L,1299610178184056904L,1299574924958011464L,1299610109330100296L,1299574924958011464L,1299574924823793736L,1299574924958011465L,1281525273222484040L,1299574924958011464L,1281525273222484040L,9548107335L};

    // TODO: cleanup
    public static World toWorld(String name, boolean skyLight, CompoundTag nbt) {
        short width = nbt.getShort("Width");
        short length = nbt.getShort("Length");

        byte[] blockdata = nbt.getByteArray("BlockData");
        CompoundTag palette = nbt.getCompoundTag("Palette");
        ListTag<CompoundTag> blockEntities = nbt.containsKey("BlockEntities") ? nbt.getListTag("BlockEntities").asTypedList(CompoundTag.class) : null;
        Map<Integer, String> mapping = new HashMap<>();
        for (String key : palette.keySet()) {
            mapping.put(palette.getInt(key), key);
        }

        World world = new World(name, width, length, skyLight);

        int index = 0;
        int i = 0;
        int value;
        int varint_length;

        while (i < blockdata.length) {
            value = 0;
            varint_length = 0;

            while (true) {
                value |= (blockdata[i] & 127) << (varint_length++ * 7);
                if (varint_length > 5) {
                    throw new RuntimeException("VarInt too big (probably corrupted data)");
                }
                if ((blockdata[i] & 128) != 128) {
                    i++;
                    break;
                }
                i++;
            }
            // index = (y * length + z) * width + x
            int y = index / (width * length);
            int z = (index % (width * length)) / width;
            int x = (index % (width * length)) % width;
            world.setBlock(x, y, z, mapping.get(value));

            Chunk chunk = world.getChunkAtWorldPos(x, z);

            if (blockEntities != null) {
                Iterator<CompoundTag> itr = blockEntities.iterator();
                while (itr.hasNext()) {
                    CompoundTag tag = itr.next();
                    int[] pos = tag.getIntArray("Pos");

                    if (pos[0] == x && pos[1] == y && pos[2] == z) {
                        ListTag<CompoundTag> newTag = chunk.getTileEntities();
                        newTag.add(SchematicConversionUtils.toTileEntityTag(tag));
                        chunk.setTileEntities(newTag);
                        itr.remove();
                        break;
                    }
                }
            }

            index++;
        }

        for (Chunk[] chunkarray : world.getChunks()) {
            for (Chunk chunk : chunkarray) {
                if (chunk != null) {
                    CompoundTag heightMap = new CompoundTag();
                    heightMap.putLongArray("MOTION_BLOCKING", MOTION_BLOCKING);
                    chunk.setHeightMaps(heightMap);
                    chunk.setBiomes(new int[256]);
                    chunk.cleanupPalettesAndBlockStates();
                }
            }
        }

        world.getLightEngineBlock().updateWorld();
        if (world.hasSkyLight()) {
            world.getLightEngineSky().updateWorld();
        }

        return world;
    }
}
