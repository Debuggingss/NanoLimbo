package ua.nanit.limbo.protocol.packets.configuration;

import ua.nanit.limbo.protocol.ByteMessage;
import ua.nanit.limbo.protocol.MetadataWriter;
import ua.nanit.limbo.protocol.PacketOut;
import ua.nanit.limbo.world.DimensionRegistry;

public class PacketRegistryData implements PacketOut {

    private final DimensionRegistry dimensionRegistry;
    private MetadataWriter metadataWriter;

    public PacketRegistryData() {
        this(null);
    }

    public PacketRegistryData(DimensionRegistry dimensionRegistry) {
        this.dimensionRegistry = dimensionRegistry;
    }

    public void setMetadataWriter(MetadataWriter metadataWriter) {
        this.metadataWriter = metadataWriter;
    }

    @Override
    public void encode(ByteMessage msg) {
        if (metadataWriter != null) {
            metadataWriter.writeData(msg);
            return;
        }
        msg.writeNamelessCompoundTag(dimensionRegistry.getCodec_1_20());
    }
}
