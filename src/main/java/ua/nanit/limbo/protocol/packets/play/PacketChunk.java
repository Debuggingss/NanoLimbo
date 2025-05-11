package ua.nanit.limbo.protocol.packets.play;

import net.querz.mca.Chunk;
import net.querz.mca.Section;
import net.querz.nbt.tag.CompoundTag;
import net.querz.nbt.tag.ListTag;
import ua.nanit.limbo.protocol.ByteMessage;
import ua.nanit.limbo.protocol.PacketOut;
import ua.nanit.limbo.util.BitsUtils;
import ua.nanit.limbo.util.DataTypeIO;
import ua.nanit.limbo.world.GeneratedBlockDataMappings;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.*;

public class PacketChunk implements PacketOut {

    private final int x;
    private final int z;
    private final Chunk chunk;
    private final List<Byte[]> skylightArrays;
    private final List<Byte[]> blocklightArrays;

    private final long[] skyLightBitMasks;
    private final long[] blockLightBitMasks;
    private final long[] skyLightBitMasksEmpty;
    private final long[] blockLightBitMasksEmpty;

    public PacketChunk() {
        this(0, 0, null, Collections.emptyList(), Collections.emptyList());
    }

    public PacketChunk(int chunkX, int chunkZ, Chunk chunk, List<Byte[]> skylightArrays, List<Byte[]> blocklightArrays) {
        this.x = chunkX;
        this.z = chunkZ;
        this.chunk = chunk;
        this.skylightArrays = skylightArrays;
        this.blocklightArrays = blocklightArrays;

        BitSet skyLightBitSet = new BitSet();
        BitSet skyLightBitSetInverse = new BitSet();
        for (int i = Math.min(17, skylightArrays.size() - 1); i >= 0; i--) {
            skyLightBitSet.set(i, skylightArrays.get(i) != null);
            skyLightBitSetInverse.set(i, skylightArrays.get(i) == null);
        }
        this.skyLightBitMasks = skyLightBitSet.toLongArray();
        this.skyLightBitMasksEmpty = skyLightBitSetInverse.toLongArray();

        BitSet blockLightBitSet = new BitSet();
        BitSet blockLightBitSetInverse = new BitSet();
        for (int i = Math.min(17, blocklightArrays.size() - 1); i >= 0; i--) {
            blockLightBitSet.set(i, blocklightArrays.get(i) != null);
            blockLightBitSetInverse.set(i, blocklightArrays.get(i) == null);
        }
        this.blockLightBitMasks = blockLightBitSet.toLongArray();
        this.blockLightBitMasksEmpty = blockLightBitSetInverse.toLongArray();
    }


    @Override
    public void encode(ByteMessage msg) {
        try {
            encodeShit(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void encodeShit(ByteMessage msg) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        DataOutputStream output = new DataOutputStream(buffer);

        output.writeInt(x);
        output.writeInt(z);

        DataTypeIO.writeVarInt(output, 1);
        DataTypeIO.writeVarInt(output, 4);
        long[] motionBlocking = chunk.getHeightMaps().getLongArray("MOTION_BLOCKING");
        DataTypeIO.writeVarInt(output, motionBlocking.length);
        for (long l : motionBlocking) {
            output.writeLong(l);
        }

        ByteArrayOutputStream dataBuffer = new ByteArrayOutputStream();
        DataOutputStream dataOut = new DataOutputStream(dataBuffer);
        for (int i = 0; i < 16; i++) {
            Section section = chunk.getSection(i);
            if (section != null) {
                short counter = 0;
                for (int x = 0; x < 16; x++) {
                    for (int z = 0; z < 16; z++) {
                        for (int y = 0; y < 16; y++) {
                            CompoundTag tag = section.getBlockStateAt(x, y, z);
                            if (tag != null && !tag.getString("Name").equals("minecraft:air")) {
                                counter++;
                            }
                        }
                    }
                }
                dataOut.writeShort(counter);

                int newBits = 32 - Integer.numberOfLeadingZeros(section.getPalette().size() - 1);
                newBits = Math.max(newBits, 4);
                if (newBits <= 8) {
                    dataOut.writeByte(newBits);

                    DataTypeIO.writeVarInt(dataOut, section.getPalette().size());
                    for (CompoundTag tag : section.getPalette()) {
                        DataTypeIO.writeVarInt(dataOut, GeneratedBlockDataMappings.getGlobalPaletteIDFromState(tag));
                    }

                    BitSet bits = BitSet.valueOf(section.getBlockStates());
                    int shift = 64 % newBits;
                    int longsNeeded = (int) Math.ceil(4096 / (double) (64 / newBits));
                    for (int u = 64; u <= bits.length(); u += 64) {
                        BitsUtils.shiftAfter(bits, u - shift, shift);
                    }

                    long[] formattedLongs = bits.toLongArray();

                    for (int u = 0; u < longsNeeded; u++) {
                        if (u < formattedLongs.length) {
                            dataOut.writeLong(formattedLongs[u]);
                        } else {
                            dataOut.writeLong(0);
                        }
                    }
                } else {
                    try {
                        dataOut.writeByte(16);
                        section.getBlockStates();
                        int longsNeeded = 1024;
                        List<Integer> list = new LinkedList<>();
                        for (int y = 0; y < 16; y++) {
                            for (int z = 0; z < 16; z++) {
                                for (int x = 0; x < 16; x++) {
                                    list.add(GeneratedBlockDataMappings.getGlobalPaletteIDFromState(section.getBlockStateAt(x, y, z)));
                                }
                            }
                        }
                        List<Long> globalLongs = new ArrayList<>();
                        long currentLong = 0;
                        int pos = 0;
                        int u = 0;
                        while (pos < longsNeeded) {
                            if (u == 3) {
                                globalLongs.add(currentLong);
                                currentLong = 0;
                                u = 0;
                                pos++;
                            } else {
                                u++;
                            }
                            int id = list.isEmpty() ? 0 : list.remove(0);
                            currentLong = currentLong << 16;
                            currentLong |= id;
                        }
                        for (int j = 0; j < longsNeeded; j++) {
                            if (j < globalLongs.size()) {
                                dataOut.writeLong(globalLongs.get(j));
                            } else {
                                dataOut.writeLong(0);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else {
                dataOut.writeShort(0);
                dataOut.writeByte(0);
                DataTypeIO.writeVarInt(dataOut, 0);
            }
            int biome = 56;

            dataOut.writeByte(0);
            DataTypeIO.writeVarInt(dataOut, biome);
        }

        byte[] data = dataBuffer.toByteArray();
        DataTypeIO.writeVarInt(output, data.length);
        output.write(data);

        ListTag<CompoundTag> tileEntities = chunk.getTileEntities();
        DataTypeIO.writeVarInt(output, tileEntities.size());

        for (CompoundTag tag : tileEntities) {
            int x = tag.getInt("x") % 16;
            int y = tag.getInt("y");
            int z = tag.getInt("z") % 16;
            String id = tag.getString("id");

            tag.remove("x");
            tag.remove("y");
            tag.remove("z");

            int intId;

            if (id.equals("minecraft:sign")) {
                intId = 7;
            } else {
                System.out.println("not a sign: " + id);
                continue;
            }

            output.writeByte(((x & 15) << 4) | (z & 15));
            output.writeShort(y);
            DataTypeIO.writeVarInt(output, intId);
            DataTypeIO.writeTag(output, tag);
        }

        DataTypeIO.writeVarInt(output, skyLightBitMasks.length);
        for (long l : skyLightBitMasks) {
            output.writeLong(l);
        }
        DataTypeIO.writeVarInt(output, blockLightBitMasks.length);
        for (long l : blockLightBitMasks) {
            output.writeLong(l);
        }
        DataTypeIO.writeVarInt(output, skyLightBitMasksEmpty.length);
        for (long l : skyLightBitMasksEmpty) {
            output.writeLong(l);
        }
        DataTypeIO.writeVarInt(output, blockLightBitMasksEmpty.length);
        for (long l : blockLightBitMasksEmpty) {
            output.writeLong(l);
        }

        DataTypeIO.writeVarInt(output, skylightArrays.stream().mapToInt(each -> each == null ? 0 : 1).sum());
        for (int i = skylightArrays.size() - 1; i >= 0; i--) {
            Byte[] array = skylightArrays.get(i);
            if (array != null) {
                DataTypeIO.writeVarInt(output, 2048);
                for (Byte aByte : array) {
                    output.writeByte(aByte);
                }
            }
        }

        DataTypeIO.writeVarInt(output, blocklightArrays.stream().mapToInt(each -> each == null ? 0 : 1).sum());
        for (int i = blocklightArrays.size() - 1; i >= 0; i--) {
            Byte[] array = blocklightArrays.get(i);
            if (array != null) {
                DataTypeIO.writeVarInt(output, 2048);
                for (Byte aByte : array) {
                    output.writeByte(aByte);
                }
            }
        }

        msg.writeBytes(buffer.toByteArray());
    }
}
