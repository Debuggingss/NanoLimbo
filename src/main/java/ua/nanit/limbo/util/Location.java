package ua.nanit.limbo.util;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;

public class Location {

    private final double x;
    private final double y;
    private final double z;
    private final float yaw;
    private final float pitch;

    public Location(double x, double y, double z, float yaw, float pitch) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public static class Serializer implements TypeSerializer<Location> {

        @Override
        public Location deserialize(Type type, ConfigurationNode node) {
            return new Location(
                    node.node("x").getDouble(0.0),
                    node.node("y").getDouble(0.0),
                    node.node("z").getDouble(0.0),
                    node.node("yaw").getFloat(0F),
                    node.node("pitch").getFloat(0F)
            );
        }

        @Override
        public void serialize(Type type, @Nullable Location obj, ConfigurationNode node) {

        }
    }
}
