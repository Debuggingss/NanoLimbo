package ua.nanit.limbo.world;

import net.querz.mca.Chunk;
import net.querz.nbt.tag.CompoundTag;
import net.querz.nbt.tag.ListTag;
import net.querz.nbt.tag.Tag;
import ua.nanit.limbo.util.SchematicConversionUtils;

import java.util.*;

public class Litematic {

    private Litematic() {}

    public static World toWorld(String name, boolean skyLight, CompoundTag litematicRoot) {
        CompoundTag regions = litematicRoot.getCompoundTag("Regions");
        World world = null;

        for (Map.Entry<String, Tag<?>> entry : regions) {
            CompoundTag region = (CompoundTag) entry.getValue();

            CompoundTag size = region.getCompoundTag("Size");
            int width = Math.abs(size.getInt("x"));
            int height = Math.abs(size.getInt("y"));
            int length = Math.abs(size.getInt("z"));

            long[] blockStates = region.getLongArray("BlockStates");
            ListTag<CompoundTag> paletteTag = region.getListTag("BlockStatePalette").asCompoundTagList();

            if (world == null) {
                world = new World(name, width, length, skyLight);
            }

            CompoundTag[] blocks = SchematicConversionUtils.unpackBlockStates(blockStates, paletteTag, width, height, length);

            int index = 0;

            for (int y = 0; y < height; y++) {
                for (int z = 0; z < length; z++) {
                    for (int x = 0; x < width; x++) {
                        world.setBlock(x, y, z, new BlockState(blocks[index]));
                        index++;
                    }
                }
            }

            ListTag<CompoundTag> tileEntities = region.getListTag("TileEntities").asCompoundTagList();

            Map<Chunk, ListTag<CompoundTag>> tileEntitiesMap = new HashMap<>();

            for (CompoundTag tileEntity : tileEntities) {
                int x = tileEntity.getInt("x");
                int z = tileEntity.getInt("z");
                Chunk chunk = world.getChunkAtWorldPos(x, z);

                if (!tileEntitiesMap.containsKey(chunk)) {
                    ListTag<CompoundTag> tags = ListTag.createUnchecked(CompoundTag.class).asCompoundTagList();
                    tags.add(tileEntity);
                    tileEntitiesMap.put(chunk, tags);
                } else {
                    tileEntitiesMap.get(chunk).add(tileEntity);
                }

                chunk.setTileEntities(tileEntitiesMap.get(chunk));
            }
        }

        return world;
    }
}
