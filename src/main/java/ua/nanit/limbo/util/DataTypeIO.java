package ua.nanit.limbo.util;

import net.querz.nbt.io.NBTOutputStream;
import net.querz.nbt.tag.EndTag;
import net.querz.nbt.tag.Tag;

import java.io.DataOutputStream;
import java.io.IOException;

public class DataTypeIO {

    public static void writeVarInt(DataOutputStream out, int value) throws IOException {
        while ((value & -128) != 0) {
            out.writeByte(value & 127 | 128);
            value >>>= 7;
        }
        out.writeByte(value);
    }

    public static void writeTag(DataOutputStream out, Tag<?> tag) throws IOException {
        if (tag == null) {
            tag = EndTag.INSTANCE;
        }
        out.writeByte(tag.getID());
        if (tag.getID() != EndTag.ID) {
            new NBTOutputStream(out).writeRawTag(tag, Tag.DEFAULT_MAX_DEPTH);
        }
    }
}
