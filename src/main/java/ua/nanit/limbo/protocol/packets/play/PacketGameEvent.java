package ua.nanit.limbo.protocol.packets.play;

import ua.nanit.limbo.protocol.ByteMessage;
import ua.nanit.limbo.protocol.PacketOut;

public class PacketGameEvent implements PacketOut {

    private final byte type;
    private final float value;

    public PacketGameEvent() {
        this((byte) 0, 0);
    }

    public PacketGameEvent(byte type, float value) {
        this.type = type;
        this.value = value;
    }

    @Override
    public void encode(ByteMessage msg) {
        msg.writeByte(type);
        msg.writeFloat(value);
    }
}
