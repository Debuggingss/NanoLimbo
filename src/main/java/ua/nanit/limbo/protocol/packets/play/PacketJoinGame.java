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
import ua.nanit.limbo.world.DimensionRegistry;

public class PacketJoinGame implements PacketOut {

    private int entityId;
    private boolean isHardcore = false;
    private int gameMode = 2;
    private int previousGameMode = -1;
    private String[] worldNames;
    private DimensionRegistry dimensionRegistry;
    private String worldName;
    private long hashedSeed;
    private int maxPlayers;
    private int viewDistance = 2;
    private boolean reducedDebugInfo;
    private boolean enableRespawnScreen;
    private boolean isDebug;
    private boolean isFlat;
    private boolean limitedCrafting;
    private boolean secureProfile;

    public void setEntityId(int entityId) {
        this.entityId = entityId;
    }

    public void setHardcore(boolean hardcore) {
        isHardcore = hardcore;
    }

    public void setGameMode(int gameMode) {
        this.gameMode = gameMode;
    }

    public void setPreviousGameMode(int previousGameMode) {
        this.previousGameMode = previousGameMode;
    }

    public void setWorldNames(String... worldNames) {
        this.worldNames = worldNames;
    }

    public void setDimensionRegistry(DimensionRegistry dimensionRegistry) {
        this.dimensionRegistry = dimensionRegistry;
    }

    public void setWorldName(String worldName) {
        this.worldName = worldName;
    }

    public void setHashedSeed(long hashedSeed) {
        this.hashedSeed = hashedSeed;
    }

    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    public void setViewDistance(int viewDistance) {
        this.viewDistance = viewDistance;
    }

    public void setReducedDebugInfo(boolean reducedDebugInfo) {
        this.reducedDebugInfo = reducedDebugInfo;
    }

    public void setEnableRespawnScreen(boolean enableRespawnScreen) {
        this.enableRespawnScreen = enableRespawnScreen;
    }

    public void setDebug(boolean debug) {
        isDebug = debug;
    }

    public void setFlat(boolean flat) {
        isFlat = flat;
    }

    public void setLimitedCrafting(boolean limitedCrafting) {
        this.limitedCrafting = limitedCrafting;
    }

    public void setSecureProfile(boolean secureProfile) {
        this.secureProfile = secureProfile;
    }

    @Override
    public void encode(ByteMessage msg) {
        msg.writeInt(entityId);
        msg.writeBoolean(isHardcore);
        msg.writeStringsArray(worldNames);
        msg.writeVarInt(maxPlayers);
        msg.writeVarInt(viewDistance);
        msg.writeVarInt(viewDistance); // Simulation Distance
        msg.writeBoolean(reducedDebugInfo);
        msg.writeBoolean(enableRespawnScreen);
        msg.writeBoolean(limitedCrafting);
        msg.writeVarInt(dimensionRegistry.getDimension_1_21_4().getId());
        msg.writeString(worldName);
        msg.writeLong(hashedSeed);
        msg.writeByte(gameMode);
        msg.writeByte(previousGameMode);
        msg.writeBoolean(isDebug);
        msg.writeBoolean(isFlat);
        msg.writeBoolean(false);
        msg.writeVarInt(0);
        msg.writeVarInt(0);
        msg.writeBoolean(secureProfile);
    }

}
