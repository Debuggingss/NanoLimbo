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

public class PacketPlayerAbilities implements PacketOut {

    // https://minecraft.wiki/w/Java_Edition_protocol/Packets#Player_Abilities_(clientbound)
    private int flags = 0;
    private float flyingSpeed = 0.05F;
    private float fieldOfView = 0.1F;

    public void setFlags(int flags) {
        this.flags = flags;
    }

    public void setFlyingSpeed(float flyingSpeed) {
        this.flyingSpeed = flyingSpeed;
    }

    public void setFieldOfView(float fieldOfView) {
        this.fieldOfView = fieldOfView;
    }

    @Override
    public void encode(ByteMessage msg) {
        msg.writeByte(flags);
        msg.writeFloat(flyingSpeed);
        msg.writeFloat(fieldOfView);
    }
}
