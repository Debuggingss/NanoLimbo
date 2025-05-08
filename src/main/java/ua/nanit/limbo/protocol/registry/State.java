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

package ua.nanit.limbo.protocol.registry;

import ua.nanit.limbo.protocol.Packet;
import ua.nanit.limbo.protocol.packets.PacketHandshake;
import ua.nanit.limbo.protocol.packets.configuration.PacketFinishConfiguration;
import ua.nanit.limbo.protocol.packets.configuration.PacketKnownPacks;
import ua.nanit.limbo.protocol.packets.configuration.PacketRegistryData;
import ua.nanit.limbo.protocol.packets.configuration.PacketUpdateTags;
import ua.nanit.limbo.protocol.packets.login.*;
import ua.nanit.limbo.protocol.packets.play.*;
import ua.nanit.limbo.protocol.packets.status.PacketStatusPing;
import ua.nanit.limbo.protocol.packets.status.PacketStatusRequest;
import ua.nanit.limbo.protocol.packets.status.PacketStatusResponse;

import java.util.*;
import java.util.function.Supplier;

public enum State {

    HANDSHAKING(0) {
        {
            serverBound.register(PacketHandshake::new, 0x00);
        }
    },
    STATUS(1) {
        {
            serverBound.register(PacketStatusRequest::new, 0x00);
            serverBound.register(PacketStatusPing::new, 0x01);
            clientBound.register(PacketStatusResponse::new, 0x00);
            clientBound.register(PacketStatusPing::new, 0x01);
        }
    },
    LOGIN(2) {
        {
            serverBound.register(PacketLoginStart::new, 0x00);
            serverBound.register(PacketLoginPluginResponse::new, 0x02);
            serverBound.register(PacketLoginAcknowledged::new, 0x03);
            clientBound.register(PacketDisconnect::new, 0x00);
            clientBound.register(PacketLoginSuccess::new, 0x02);
            clientBound.register(PacketLoginPluginRequest::new, 0x04);
        }
    },
    CONFIGURATION(3) {
        {
            clientBound.register(PacketPluginMessage::new, 0x01);
            clientBound.register(PacketDisconnect::new, 0x02);
            clientBound.register(PacketFinishConfiguration::new, 0x03);
            clientBound.register(PacketKeepAlive::new, 0x04);
            clientBound.register(PacketKnownPacks::new, 0x0E);
            clientBound.register(PacketUpdateTags::new, 0x0D);
            clientBound.register(PacketRegistryData::new, 0x07);
            serverBound.register(PacketPluginMessage::new, 0x02);
            serverBound.register(PacketFinishConfiguration::new, 0x03);
            serverBound.register(PacketKeepAlive::new, 0x04);
        }
    },
    PLAY(4) {
        {
            serverBound.register(PacketKeepAlive::new, 0x1A);
            clientBound.register(PacketDeclareCommands::new, 0x10);
            clientBound.register(PacketJoinGame::new, 0x2B);
            clientBound.register(PacketPluginMessage::new, 0x18);
            clientBound.register(PacketPlayerAbilities::new, 0x39);
            clientBound.register(PacketPlayerPositionAndLook::new, 0x41);
            clientBound.register(PacketKeepAlive::new, 0x26);
            clientBound.register(PacketPlayerInfo::new, 0x3F);
            clientBound.register(PacketSpawnPosition::new, 0x5A);
            clientBound.register(PacketGameEvent::new, 0x22);
            clientBound.register(PacketEmptyChunk::new, 0x27);
        }
    };

    private static final Map<Integer, State> STATE_BY_ID = new HashMap<>();

    static {
        for (State registry : values()) {
            STATE_BY_ID.put(registry.stateId, registry);
        }
    }

    private final int stateId;
    public final ProtocolMappings serverBound = new ProtocolMappings();
    public final ProtocolMappings clientBound = new ProtocolMappings();

    State(int stateId) {
        this.stateId = stateId;
    }

    public static State getById(int stateId) {
        return STATE_BY_ID.get(stateId);
    }

    public static class ProtocolMappings {

        private final PacketRegistry registry = new PacketRegistry();

        public PacketRegistry getRegistry() {
            return registry;
        }

        public void register(Supplier<?> packet, int packetId) {
            registry.register(packetId, packet);
        }
    }

    public static class PacketRegistry {

        private final Map<Integer, Supplier<?>> packetsById = new HashMap<>();
        private final Map<Class<?>, Integer> packetIdByClass = new HashMap<>();

        public Packet getPacket(int packetId) {
            Supplier<?> supplier = packetsById.get(packetId);
            return supplier == null ? null : (Packet) supplier.get();
        }

        public int getPacketId(Class<?> packetClass) {
            return packetIdByClass.getOrDefault(packetClass, -1);
        }

        public void register(int packetId, Supplier<?> supplier) {
            packetsById.put(packetId, supplier);
            packetIdByClass.put(supplier.get().getClass(), packetId);
        }
    }
}
