package ua.nanit.limbo.protocol.packets.play;

import ua.nanit.limbo.protocol.ByteMessage;
import ua.nanit.limbo.protocol.PacketOut;

public class PacketBlockUpdate implements PacketOut {

    private final int x;
    private final int y;
    private final int z;
    private final int stateId;

    public PacketBlockUpdate() {
        this(0, 0, 0, 0);
    }

    public PacketBlockUpdate(int x, int y, int z, int stateId) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.stateId = stateId;
    }

    @Override
    public void encode(ByteMessage msg) {
        msg.writeLong(encodePosition(x, y, z));
        msg.writeVarInt(stateId);
    }

    private static long encodePosition(long x, long y, long z) {
        return ((x & 0x3FFFFFF) << 38) | ((z & 0x3FFFFFF) << 12) | (y & 0xFFF);
    }
}
