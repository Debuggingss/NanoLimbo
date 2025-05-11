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

package ua.nanit.limbo.configuration;

import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.serialize.TypeSerializerCollection;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;
import ua.nanit.limbo.server.data.InfoForwarding;
import ua.nanit.limbo.server.data.PingData;
import ua.nanit.limbo.util.Location;

import java.io.*;
import java.net.SocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class LimboConfig {

    private final Path root;

    private SocketAddress address;
    private int maxPlayers;
    private PingData pingData;

    private String dimensionType;
    private File schematicFile;
    private int gameMode;
    private Location spawnPosition;
    private boolean freezePlayer;

    private InfoForwarding infoForwarding;
    private long readTimeout;
    private int debugLevel;

    private boolean useEpoll;
    private int bossGroupSize;
    private int workerGroupSize;

    public LimboConfig(Path root) {
        this.root = root;
    }

    public void load() throws Exception {
        ConfigurationOptions options = ConfigurationOptions.defaults().serializers(getSerializers());
        YamlConfigurationLoader loader = YamlConfigurationLoader.builder()
                .source(this::getReader)
                .defaultOptions(options)
                .build();

        ConfigurationNode conf = loader.load();

        address = conf.node("bind").get(SocketAddress.class);
        maxPlayers = conf.node("maxPlayers").getInt(-1);
        pingData = conf.node("ping").get(PingData.class);

        dimensionType = conf.node("dimension").getString("the_end");
        if (dimensionType.equalsIgnoreCase("nether")) {
            dimensionType = "the_nether";
        }
        if (dimensionType.equalsIgnoreCase("end")) {
            dimensionType = "the_end";
        }

        String schematicFileName = conf.node("schematic").getString("");
        if (schematicFileName.isEmpty()) {
            schematicFile = null;
        } else {
            schematicFile = new File(schematicFileName);
            if (!schematicFile.exists()) {
                throw new FileNotFoundException("Could not load schematic file: '" + schematicFileName + "'!");
            }
        }

        gameMode = conf.node("gameMode").getInt(0);
        spawnPosition = conf.node("spawnPos").get(Location.class);
        freezePlayer = conf.node("freezePlayer").getBoolean(false);

        infoForwarding = conf.node("infoForwarding").get(InfoForwarding.class);
        readTimeout = conf.node("readTimeout").getLong(30000);
        debugLevel = conf.node("debugLevel").getInt(2);

        useEpoll = conf.node("netty", "useEpoll").getBoolean(true);
        bossGroupSize = conf.node("netty", "threads", "bossGroup").getInt(1);
        workerGroupSize = conf.node("netty", "threads", "workerGroup").getInt(4);
    }

    private BufferedReader getReader() throws IOException {
        String name = "settings.yml";
        Path filePath = Paths.get(root.toString(), name);

        if (!Files.exists(filePath)) {
            InputStream stream = getClass().getResourceAsStream( "/" + name);

            if (stream == null)
                throw new FileNotFoundException("Cannot find settings resource file");

            Files.copy(stream, filePath);
        }

        return Files.newBufferedReader(filePath);
    }

    private TypeSerializerCollection getSerializers() {
        return TypeSerializerCollection.builder()
                .register(SocketAddress.class, new SocketAddressSerializer())
                .register(InfoForwarding.class, new InfoForwarding.Serializer())
                .register(PingData.class, new PingData.Serializer())
                .register(Location.class, new Location.Serializer())
                .build();
    }

    public SocketAddress getAddress() {
        return address;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public PingData getPingData() {
        return pingData;
    }

    public String getDimensionType() {
        return dimensionType;
    }

    public File getSchematicFile() {
        return schematicFile;
    }

    public int getGameMode() {
        return gameMode;
    }

    public Location getSpawnPosition() {
        return spawnPosition;
    }

    public boolean isFreezePlayer() {
        return freezePlayer;
    }

    public InfoForwarding getInfoForwarding() {
        return infoForwarding;
    }

    public long getReadTimeout() {
        return readTimeout;
    }

    public int getDebugLevel() {
        return debugLevel;
    }

    public boolean isUseEpoll() {
        return useEpoll;
    }

    public int getBossGroupSize() {
        return bossGroupSize;
    }

    public int getWorkerGroupSize() {
        return workerGroupSize;
    }
}
