package ua.nanit.limbo.protocol;

@FunctionalInterface
public interface MetadataWriter {

    void writeData(ByteMessage message);

}
