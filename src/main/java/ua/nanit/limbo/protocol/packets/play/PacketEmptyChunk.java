package ua.nanit.limbo.protocol.packets.play;

import ua.nanit.limbo.protocol.ByteMessage;
import ua.nanit.limbo.protocol.PacketOut;

public class PacketEmptyChunk implements PacketOut {

    private int x;
    private int z;

    public void setX(int x) {
        this.x = x;
    }

    public void setZ(int z) {
        this.z = z;
    }

    @Override
    public void encode(ByteMessage msg) {
        msg.writeInt(x);
        msg.writeInt(z);

        msg.writeVarInt(1); // Array length
        msg.writeVarInt(4); // Motionblock type
        long[] motionBlockins = new long[37];
        msg.writeVarInt(motionBlockins.length);
        for (long data : motionBlockins) {
            msg.writeLong(data);
        }

        int sections = 24;
        byte[] sectionData = new byte[]{0, 0, 0, 0, 0, 0, 0, 0};

        msg.writeVarInt(sectionData.length * sections);
        for (int i = 0; i < sections; i++) {
            msg.writeBytes(sectionData);
        }

        msg.writeVarInt(0);

        byte[] lightData = new byte[]{1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 3, -1, -1, 0, 0};
        msg.ensureWritable(lightData.length);
        msg.writeBytes(lightData, 1, lightData.length - 1);
    }

}
