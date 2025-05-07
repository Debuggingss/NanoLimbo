package ua.nanit.limbo.protocol.packets.play;

import ua.nanit.limbo.protocol.ByteMessage;
import ua.nanit.limbo.protocol.PacketOut;

public class PacketGameEvent implements PacketOut {

    private byte type;
    private float value;

    public void setType(byte type) {
        this.type = type;
    }

    public void setValue(float value) {
        this.value = value;
    }

    @Override
    public void encode(ByteMessage msg) {
        msg.writeByte(type);
        msg.writeFloat(value);
    }
}
