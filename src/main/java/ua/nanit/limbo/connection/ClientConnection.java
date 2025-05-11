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

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import net.querz.mca.Chunk;
import org.jetbrains.annotations.NotNull;
import ua.nanit.limbo.connection.pipeline.PacketDecoder;
import ua.nanit.limbo.connection.pipeline.PacketEncoder;
import ua.nanit.limbo.protocol.ByteMessage;
import ua.nanit.limbo.protocol.Packet;
import ua.nanit.limbo.protocol.PacketOut;
import ua.nanit.limbo.protocol.packets.login.PacketDisconnect;
import ua.nanit.limbo.protocol.packets.play.*;
import ua.nanit.limbo.protocol.registry.State;
import ua.nanit.limbo.server.LimboServer;
import ua.nanit.limbo.server.Log;
import ua.nanit.limbo.util.UuidUtil;
import ua.nanit.limbo.world.World;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class ClientConnection extends ChannelInboundHandlerAdapter {

    private final LimboServer server;
    private final Channel channel;
    private final GameProfile gameProfile;

    private final PacketDecoder decoder;
    private final PacketEncoder encoder;

    private State state;
    private int protocolVersion;
    private SocketAddress address;

    private int velocityLoginMessageId = -1;

    public ClientConnection(Channel channel, LimboServer server, PacketDecoder decoder, PacketEncoder encoder) {
        this.server = server;
        this.channel = channel;
        this.decoder = decoder;
        this.encoder = encoder;
        this.address = channel.remoteAddress();
        this.gameProfile = new GameProfile();
    }

    public UUID getUuid() {
        return gameProfile.getUuid();
    }

    public String getUsername() {
        return gameProfile.getUsername();
    }

    public SocketAddress getAddress() {
        return address;
    }

    public int getProtocolVersion() {
        return protocolVersion;
    }

    public GameProfile getGameProfile() {
        return gameProfile;
    }

    @Override
    public void channelInactive(@NotNull ChannelHandlerContext ctx) throws Exception {
        if (state.equals(State.PLAY) || state.equals(State.CONFIGURATION)) {
            server.getConnections().removeConnection(this);
        }
        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if (channel.isActive()) {
            Log.error("Unhandled exception: ", cause);
        }
    }

    @Override
    public void channelRead(@NotNull ChannelHandlerContext ctx, @NotNull Object msg) {
        handlePacket(msg);
    }

    public void handlePacket(Object packet) {
        if (packet instanceof Packet) {
            ((Packet) packet).handle(this, server);
        }
    }

    public void fireLoginSuccess() {
        if (server.getConfig().getInfoForwarding().isModern() && velocityLoginMessageId == -1) {
            disconnectLogin("You need to connect with Velocity");
            return;
        }

        sendPacket(PacketSnapshots.PACKET_LOGIN_SUCCESS);

        server.getConnections().addConnection(this);

        updateEncoderState(State.CONFIGURATION);
    }

    public void spawnPlayer() {
        updateState(State.PLAY);

        Runnable sendPlayPackets = () -> {
            writePacket(PacketSnapshots.PACKET_JOIN_GAME);
            writePacket(PacketSnapshots.PACKET_PLAYER_ABILITIES);
            writePacket(PacketSnapshots.PACKET_PLAYER_POS_AND_LOOK);
            writePacket(PacketSnapshots.PACKET_DECLARE_COMMANDS);

            writePacket(PacketSnapshots.PACKET_START_WAITING_CHUNKS);

            World world = server.getWorld();

            if (world != null) {
                for (Chunk[] chunkArray : world.getChunks()) {
                    for (Chunk chunk : chunkArray) {
                        int x = world.getChunkX(chunk);
                        int z = world.getChunkZ(chunk);
                        List<Byte[]> blockChunk = world.getLightEngineBlock().getBlockLightBitMask(x, z);
                        writePacket(new PacketChunk(x, z, chunk, new ArrayList<>(), blockChunk));
                    }
                }
            }

            sendKeepAlive();
        };

        sendPlayPackets.run();
    }

    public void onLoginAcknowledgedReceived() {
        updateState(State.CONFIGURATION);

        writePacket(PacketSnapshots.PACKET_KNOWN_PACKS);
        writePackets(PacketSnapshots.PACKETS_REGISTRY_DATA_1_21_5);
        writePacket(PacketSnapshots.PACKET_UPDATE_TAGS_1_21_5);
        sendPacket(PacketSnapshots.PACKET_FINISH_CONFIGURATION);
    }

    private void writePackets(List<PacketOut> packets) {
        for (PacketOut packet : packets) {
            writePacket(packet);
        }
    }

    public void disconnectLogin(String reason) {
        if (isConnected() && state == State.LOGIN) {
            PacketDisconnect disconnect = new PacketDisconnect();
            disconnect.setReason(reason);
            sendPacketAndClose(disconnect);
        }
    }

    public void sendKeepAlive() {
        if (state.equals(State.PLAY)) {
            PacketKeepAlive keepAlive = new PacketKeepAlive();
            keepAlive.setId(ThreadLocalRandom.current().nextLong());
            sendPacket(keepAlive);
        }
    }

    public void sendPacket(Object packet) {
        if (isConnected())
            channel.writeAndFlush(packet, channel.voidPromise());
    }

    public void sendPacketAndClose(Object packet) {
        if (isConnected())
            channel.writeAndFlush(packet).addListener(ChannelFutureListener.CLOSE);
    }

    public void writePacket(Object packet) {
        if (isConnected())
            channel.write(packet, channel.voidPromise());
    }

    public boolean isConnected() {
        return channel.isActive();
    }

    public void updateState(State state) {
        this.state = state;
        decoder.updateState(state);
        encoder.updateState(state);
    }

    public void updateEncoderState(State state) {
        encoder.updateState(state);
    }

    public void updateProtocolVersion(int protocolVersion) {
        this.protocolVersion = protocolVersion;
    }

    public void setAddress(String host) {
        this.address = new InetSocketAddress(host, ((InetSocketAddress) this.address).getPort());
    }

    boolean checkBungeeGuardHandshake(String handshake) {
        String[] split = handshake.split("\00");

        if (split.length != 4)
            return false;

        String socketAddressHostname = split[1];
        UUID uuid = UuidUtil.fromString(split[2]);
        JsonArray arr;

        try {
            arr = JsonParser.array().from(split[3]);
        } catch (JsonParserException e) {
            return false;
        }

        String token = null;

        for (Object obj : arr) {
            if (obj instanceof JsonObject) {
                JsonObject prop = (JsonObject) obj;
                if (prop.getString("name").equals("bungeeguard-token")) {
                    token = prop.getString("value");
                    break;
                }
            }
        }

        if (!server.getConfig().getInfoForwarding().hasToken(token))
            return false;

        setAddress(socketAddressHostname);
        gameProfile.setUuid(uuid);

        Log.debug("Successfully verified BungeeGuard token");

        return true;
    }

    int getVelocityLoginMessageId() {
        return velocityLoginMessageId;
    }

    void setVelocityLoginMessageId(int velocityLoginMessageId) {
        this.velocityLoginMessageId = velocityLoginMessageId;
    }

    boolean checkVelocityKeyIntegrity(ByteMessage buf) {
        byte[] signature = new byte[32];
        buf.readBytes(signature);
        byte[] data = new byte[buf.readableBytes()];
        buf.getBytes(buf.readerIndex(), data);
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(server.getConfig().getInfoForwarding().getSecretKey(), "HmacSHA256"));
            byte[] mySignature = mac.doFinal(data);
            if (!MessageDigest.isEqual(signature, mySignature))
                return false;
        } catch (InvalidKeyException | java.security.NoSuchAlgorithmException e) {
            throw new AssertionError(e);
        }
        int version = buf.readVarInt();
        if (version != 1)
            throw new IllegalStateException("Unsupported forwarding version " + version + ", wanted " + '\001');
        return true;
    }
}
