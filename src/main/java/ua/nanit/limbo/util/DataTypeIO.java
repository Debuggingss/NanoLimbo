package ua.nanit.limbo.util;

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
}
