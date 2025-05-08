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

package ua.nanit.limbo.connection;

import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.ListBinaryTag;
import ua.nanit.limbo.protocol.PacketOut;
import ua.nanit.limbo.protocol.packets.configuration.PacketFinishConfiguration;
import ua.nanit.limbo.protocol.packets.configuration.PacketKnownPacks;
import ua.nanit.limbo.protocol.packets.configuration.PacketRegistryData;
import ua.nanit.limbo.protocol.packets.configuration.PacketUpdateTags;
import ua.nanit.limbo.protocol.packets.login.PacketLoginSuccess;
import ua.nanit.limbo.protocol.packets.play.*;
import ua.nanit.limbo.server.LimboServer;
import ua.nanit.limbo.util.UuidUtil;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public final class PacketSnapshots {

    public static PacketOut PACKET_LOGIN_SUCCESS;
    public static PacketOut PACKET_JOIN_GAME;
    public static PacketOut PACKET_SPAWN_POSITION;
    public static PacketOut PACKET_PLUGIN_MESSAGE;
    public static PacketOut PACKET_PLAYER_ABILITIES;
    public static PacketOut PACKET_PLAYER_INFO;
    public static PacketOut PACKET_DECLARE_COMMANDS;

    // For 1.19 we need to spawn player outside the world to avoid stuck in terrain loading
    public static PacketOut PACKET_PLAYER_POS_AND_LOOK;

    public static PacketOut PACKET_REGISTRY_DATA;

    public static PacketOut PACKET_KNOWN_PACKS;

    public static PacketOut PACKET_UPDATE_TAGS_1_21_5;

    public static List<PacketOut> PACKETS_REGISTRY_DATA_1_21_5;

    public static PacketOut PACKET_FINISH_CONFIGURATION;

    public static List<PacketOut> PACKETS_EMPTY_CHUNKS;
    public static PacketOut PACKET_START_WAITING_CHUNKS;

    private PacketSnapshots() {
    }

    public static void initPackets(LimboServer server) {
        final String username = server.getConfig().getPingData().getVersion();
        final UUID uuid = UuidUtil.getOfflineModeUuid(username);

        PacketLoginSuccess loginSuccess = new PacketLoginSuccess();
        loginSuccess.setUsername(username);
        loginSuccess.setUuid(uuid);

        PacketJoinGame joinGame = new PacketJoinGame();
        String worldName = "minecraft:" + server.getConfig().getDimensionType().toLowerCase(Locale.ROOT);
        joinGame.setEntityId(0);
        joinGame.setEnableRespawnScreen(true);
        joinGame.setFlat(false);
        joinGame.setGameMode(server.getConfig().getGameMode());
        joinGame.setHardcore(false);
        joinGame.setMaxPlayers(server.getConfig().getMaxPlayers());
        joinGame.setPreviousGameMode(-1);
        joinGame.setReducedDebugInfo(true);
        joinGame.setDebug(false);
        joinGame.setViewDistance(0);
        joinGame.setWorldName(worldName);
        joinGame.setWorldNames(worldName);
        joinGame.setHashedSeed(0);
        joinGame.setDimensionRegistry(server.getDimensionRegistry());

        PacketPlayerAbilities playerAbilities = new PacketPlayerAbilities();
        playerAbilities.setFlyingSpeed(0.0F);
        playerAbilities.setFlags(0x02);
        playerAbilities.setFieldOfView(0.1F);

        int teleportId = ThreadLocalRandom.current().nextInt();

        PacketPlayerPositionAndLook positionAndLook
                = new PacketPlayerPositionAndLook(0, 400, 0, 0, 0, teleportId);

        PacketSpawnPosition packetSpawnPosition = new PacketSpawnPosition(0, 400, 0);

        PacketDeclareCommands declareCommands = new PacketDeclareCommands();
        declareCommands.setCommands(Collections.emptyList());

        PacketPlayerInfo info = new PacketPlayerInfo();
        info.setGameMode(server.getConfig().getGameMode());
        info.setUuid(uuid);

        PACKET_LOGIN_SUCCESS = loginSuccess;
        PACKET_JOIN_GAME = joinGame;
        PACKET_PLAYER_POS_AND_LOOK = positionAndLook;
        PACKET_SPAWN_POSITION = packetSpawnPosition;
        PACKET_PLAYER_ABILITIES = playerAbilities;
        PACKET_PLAYER_INFO = info;

        PACKET_DECLARE_COMMANDS = declareCommands;

        PacketKnownPacks packetKnownPacks = new PacketKnownPacks();
        PACKET_KNOWN_PACKS = packetKnownPacks;

        PACKET_UPDATE_TAGS_1_21_5 = createTagData(server.getDimensionRegistry().getTags_1_21_5());

        PacketRegistryData packetRegistryData = new PacketRegistryData();
        packetRegistryData.setDimensionRegistry(server.getDimensionRegistry());

        PACKET_REGISTRY_DATA = packetRegistryData;

        PACKETS_REGISTRY_DATA_1_21_5 = createRegistryData(server, server.getDimensionRegistry().getCodec_1_21_5());

        PACKET_FINISH_CONFIGURATION = new PacketFinishConfiguration();

        PacketGameEvent packetGameEvent = new PacketGameEvent();
        packetGameEvent.setType((byte) 13); // Waiting for chunks type
        packetGameEvent.setValue(0);
        PACKET_START_WAITING_CHUNKS = packetGameEvent;

        int chunkXOffset = (int) 0 >> 4; // Default x position is 0
        int chunkZOffset = (int) 0 >> 4; // Default z position is 0
        int chunkEdgeSize = 1; // TODO Make configurable?

        List<PacketOut> emptyChunks = new ArrayList<>();
        // Make multiple chunks for edges
        for (int chunkX = chunkXOffset - chunkEdgeSize; chunkX <= chunkXOffset + chunkEdgeSize; ++chunkX) {
            for (int chunkZ = chunkZOffset - chunkEdgeSize; chunkZ <= chunkZOffset + chunkEdgeSize; ++chunkZ) {
                PacketEmptyChunk packetEmptyChunk = new PacketEmptyChunk();
                packetEmptyChunk.setX(chunkX);
                packetEmptyChunk.setZ(chunkZ);

                emptyChunks.add(packetEmptyChunk);
            }
        }
        PACKETS_EMPTY_CHUNKS = emptyChunks;
    }

    private static PacketOut createTagData(CompoundBinaryTag tags) {
        PacketUpdateTags packetUpdateTags = new PacketUpdateTags();
        packetUpdateTags.setTags(tags);
        return packetUpdateTags;
    }

    private static List<PacketOut> createRegistryData(LimboServer server, CompoundBinaryTag dimensionTag) {
        List<PacketOut> packetRegistries = new ArrayList<>();
        for (String registryType : dimensionTag.keySet()) {
            CompoundBinaryTag compoundRegistryType = dimensionTag.getCompound(registryType);

            PacketRegistryData registryData = new PacketRegistryData();
            registryData.setDimensionRegistry(server.getDimensionRegistry());

            ListBinaryTag values = compoundRegistryType.getList("value");
            registryData.setMetadataWriter(message -> {
                message.writeString(registryType);

                message.writeVarInt(values.size());
                for (BinaryTag entry : values) {
                    CompoundBinaryTag entryTag = (CompoundBinaryTag) entry;

                    String name = entryTag.getString("name");
                    CompoundBinaryTag element = entryTag.getCompound("element", null);

                    message.writeString(name);
                    if (element != null) {
                        message.writeBoolean(true);
                        message.writeNamelessCompoundTag(element);
                    } else {
                        message.writeBoolean(false);
                    }
                }
            });

            packetRegistries.add(registryData);
        }

        return packetRegistries;
    }
}
