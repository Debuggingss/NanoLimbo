package ua.nanit.limbo.protocol.packets.configuration;

import ua.nanit.limbo.LimboConstants;
import ua.nanit.limbo.protocol.ByteMessage;
import ua.nanit.limbo.protocol.PacketOut;

public class PacketKnownPacks implements PacketOut {

    @Override
    public void encode(ByteMessage msg) {
        msg.writeVarInt(1);
        msg.writeString("minecraft");
        msg.writeString("core");
        msg.writeString(LimboConstants.SUPPORTED_VERSION_NAME);
    }

}
