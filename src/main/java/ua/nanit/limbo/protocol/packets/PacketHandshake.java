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

package ua.nanit.limbo.protocol.packets;

import ua.nanit.limbo.connection.ClientConnection;
import ua.nanit.limbo.protocol.ByteMessage;
import ua.nanit.limbo.protocol.PacketIn;
import ua.nanit.limbo.protocol.registry.State;
import ua.nanit.limbo.server.LimboServer;

public class PacketHandshake implements PacketIn {

    private int protocolVersion;
    private String host;
    private int port;
    private State nextState;

    public int getProtocolVersion() {
        return protocolVersion;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public State getNextState() {
        return nextState;
    }

    @Override
    public void decode(ByteMessage msg) {
        try {
            this.protocolVersion = msg.readVarInt();
        } catch (IllegalArgumentException e) {
            this.protocolVersion = 0;
        }

        this.host = msg.readString();
        this.port = msg.readUnsignedShort();
        this.nextState = State.getById(msg.readVarInt());
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    @Override
    public void handle(ClientConnection conn, LimboServer server) {
        server.getPacketHandler().handle(conn, this);
    }
}
