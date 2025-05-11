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

import java.util.Objects;

public class World {

    public static final CompoundTag HEIGHT_MAP = new CompoundTag();
    public static final Chunk EMPTY_CHUNK = Chunk.newChunk();

    static {
        HEIGHT_MAP.putLongArray("MOTION_BLOCKING",
                new long[] {1371773531765642314L, 1389823183635651148L, 1371738278539598925L,
                        1389823183635388492L, 1353688558756731469L, 1389823114781694027L, 1317765589597723213L,
                        1371773531899860042L, 1389823183635651149L, 1371773462911685197L, 1389823183635650636L,
                        1353688626805119565L, 1371773531900123211L, 1335639250618849869L, 1371738278674077258L,
                        1389823114781694028L, 1353723811310638154L, 1371738278674077259L, 1335674228429068364L,
                        1335674228429067338L, 1335674228698027594L, 1317624576693539402L, 1335709481520370249L,
                        1299610178184057417L, 1335638906349064264L, 1299574993811968586L, 1299574924958011464L,
                        1299610178184056904L, 1299574924958011464L, 1299610109330100296L, 1299574924958011464L,
                        1299574924823793736L, 1299574924958011465L, 1281525273222484040L, 1299574924958011464L,
                        1281525273222484040L, 9548107335L});

        EMPTY_CHUNK.cleanupPalettesAndBlockStates();
        EMPTY_CHUNK.setHeightMaps(HEIGHT_MAP.clone());
        EMPTY_CHUNK.setBiomes(new int[1024]);
        EMPTY_CHUNK.setTileEntities(new ListTag<>(CompoundTag.class));
    }

    private String name;
    private Chunk[][] chunks;
    private int width;
    private int length;
    private LightEngineBlock lightEngineBlock;
    private LightEngineSky lightEngineSky;

    public World(String name, int width, int length, boolean skyLight) {
        this.name = name;
        this.chunks = new Chunk[(width >> 4) + 1][(length >> 4) + 1];
        this.width = width;
        this.length = length;

        for (int x = 0; x < chunks.length; x++) {
            for (int z = 0; z < chunks[x].length; z++) {
                chunks[x][z] = Chunk.newChunk();
                Chunk chunk = chunks[x][z];
                chunk.cleanupPalettesAndBlockStates();
                chunk.setHeightMaps(HEIGHT_MAP.clone());
                chunk.setBiomes(new int[1024]);
                chunk.setTileEntities(new ListTag<>(CompoundTag.class));
            }
        }

        this.lightEngineBlock = new LightEngineBlock(this);

        if (skyLight) {
            this.lightEngineSky = new LightEngineSky(this);
        }
    }

    public LightEngineBlock getLightEngineBlock() {
        return lightEngineBlock;
    }

    public LightEngineSky getLightEngineSky() {
        return lightEngineSky;
    }

    public boolean hasSkyLight() {
        return lightEngineSky != null;
    }

    public void setBlock(int x, int y, int z, String blockdata) {
        Chunk chunk = this.chunks[(x >> 4)][(z >> 4)];
        if (chunk == null) {
            chunk = Chunk.newChunk();
            this.chunks[(x >> 4)][(z >> 4)] = chunk;
        }
        CompoundTag block = SchematicConversionUtils.toBlockTag(blockdata);
        chunk.setBlockStateAt(x, y, z, block, false);
    }

    public BlockState getBlock(BlockPosition blockPosition) {
        return getBlock(blockPosition.getX(), blockPosition.getY(), blockPosition.getZ());
    }

    public BlockState getBlock(int x, int y, int z) {
        Chunk chunk = this.chunks[(x >> 4)][(z >> 4)];
        if (chunk == null) {
            chunk = Chunk.newChunk();
            this.chunks[(x >> 4)][(z >> 4)] = chunk;
        }

        CompoundTag tag = chunk.getBlockStateAt(x, y, z);
        if (tag == null) {
            tag = new CompoundTag();
            tag.putString("Name", "minecraft:air");
        }
        return new BlockState(tag);
    }

    public void setBlock(BlockPosition blockPosition, BlockState state) {
        setBlock(blockPosition.getX(), blockPosition.getY(), blockPosition.getZ(), state);
    }

    public void setBlock(int x, int y, int z, BlockState state) {
        Chunk chunk = this.chunks[(x >> 4)][(z >> 4)];
        if (chunk == null) {
            chunk = Chunk.newChunk();
            this.chunks[(x >> 4)][(z >> 4)] = chunk;
        }
        chunk.setBlockStateAt(x, y, z, state.toCompoundTag(), false);
    }

    public Chunk[][] getChunks() {
        return this.chunks;
    }

    public Chunk getChunkAtWorldPos(int x, int z) {
        return this.chunks[(x >> 4)][(z >> 4)];
    }

    public Chunk getChunkAt(int x, int z) {
        if (x < 0 || z < 0 || x >= chunks.length || z >= chunks[x].length) {
            return null;
        }
        return this.chunks[x][z];
    }

    public int getChunkX(Chunk chunk) {
        for (int x = 0; x < chunks.length; x++) {
            for (int z = 0; z < chunks[x].length; z++) {
                Chunk c = getChunkAt(x, z);
                if (c.equals(chunk)) {
                    return x;
                }
            }
        }
        return Integer.MIN_VALUE;
    }

    public int getChunkZ(Chunk chunk) {
        for (int x = 0; x < chunks.length; x++) {
            for (int z = 0; z < chunks[x].length; z++) {
                Chunk c = getChunkAt(x, z);
                if (c.equals(chunk)) {
                    return z;
                }
            }
        }
        return Integer.MIN_VALUE;
    }

    public int[] getChunkXZ(Chunk chunk) {
        for (int x = 0; x < chunks.length; x++) {
            for (int z = 0; z < chunks[x].length; z++) {
                Chunk c = getChunkAt(x, z);
                if (c.equals(chunk)) {
                    return new int[] {x, z};
                }
            }
        }
        return null;
    }

    public String getName() {
        return name;
    }

    public int getWidth() {
        return width;
    }

    public int getLength() {
        return length;
    }

    public int getChunkWidth() {
        return (width >> 4) + 1;
    }

    public int getChunkLength() {
        return (length >> 4) + 1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        World world = (World) o;
        return Objects.equals(name, world.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
