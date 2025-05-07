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

import java.util.EnumSet;
import java.util.UUID;

/**
 * This packet was very simplified and using only for ADD_PLAYER action
 */
public class PacketPlayerInfo implements PacketOut {

    private int gameMode = 3;
    private String username = "";
    private UUID uuid;

    public void setGameMode(int gameMode) {
        this.gameMode = gameMode;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public void encode(ByteMessage msg) {
        EnumSet<Action> actions = EnumSet.noneOf(Action.class);
        actions.add(Action.ADD_PLAYER);
        actions.add(Action.UPDATE_LISTED);
        actions.add(Action.UPDATE_GAMEMODE);
        msg.writeEnumSet(actions, Action.class);

        msg.writeVarInt(1); // Array length (1 element)
        msg.writeUuid(uuid); // UUID
        msg.writeString(username); //Username
        msg.writeVarInt(0); //Properties (0 is empty)

        msg.writeBoolean(true); //Update listed
        msg.writeVarInt(gameMode); //Gamemode
    }

    public enum Action {
        ADD_PLAYER,
        INITIALIZE_CHAT,
        UPDATE_GAMEMODE,
        UPDATE_LISTED,
        UPDATE_LATENCY,
        UPDATE_DISPLAY_NAME;
    }
}
