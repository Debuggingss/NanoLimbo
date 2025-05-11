/*
 * Copyright (C) 2020 Nan1t
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ua.nanit.limbo.protocol.packets.play;

import ua.nanit.limbo.protocol.ByteMessage;
import ua.nanit.limbo.protocol.PacketOut;

public class PacketPlayerPositionAndLook implements PacketOut {

    private double x;
    private double y;
    private double z;
    private float yaw;
    private float pitch;
    private int teleportId;

    public PacketPlayerPositionAndLook() {
    }

    public PacketPlayerPositionAndLook(double x, double y, double z, float yaw, float pitch, int teleportId) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.teleportId = teleportId;
    }

    @Override
    public void encode(ByteMessage msg) {
        msg.writeVarInt(teleportId);

        msg.writeDouble(x);
        msg.writeDouble(y);
        msg.writeDouble(z);

        msg.writeDouble(0);
        msg.writeDouble(0);
        msg.writeDouble(0);

        msg.writeFloat(yaw);
        msg.writeFloat(pitch);

        msg.writeInt(0);
    }
}
