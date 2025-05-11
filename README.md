## NanoLimbo Lite

This fork of [NanoLimbo](https://github.com/Nan1t/NanoLimbo) does **NOT** provide multi-version support. Only **1.21.5** is supported.

This is an *even lighter* lightweight Minecraft limbo server, written in Java with Netty.

General features:
* High performance. The server doesn't save or cache any useless (for limbo) data.
* Doesn't spawn threads per player. Use a fixed thread pool.
* Support for **BungeeCord** and **Velocity** info forwarding.
* Support for [BungeeGuard](https://www.spigotmc.org/resources/79601/) handshake format.
* Fully configurable.
* Lightweight. App size around **4MB**.
* Litematic and Schematic world loading

### Commands

* `help` - Show help message
* `conn` - Display number of connections
* `mem` - Display memory usage stats
* `stop` - Stop the server

Note that the server also will be closed correctly if you just press `Ctrl+C`.

### Installation

Required software: JRE 11+

The installation process is *almost* simple.

1. Build the jar.
2. Put the jar file in the folder you want.
3. Create a start script as you did for Bukkit or BungeeCord, with a command like this:
   `java -jar NanoLimbo-<version>.jar`
4. The server will create `settings.yml` file, which is the server configuration. 
5. Configure it as you want and restart the server.

### Player info forwarding

The server supports player info forwarding from the proxy. There are several types of info forwarding:

* `LEGACY` - The **BungeeCord** IP forwarding.
* `MODERN` - **Velocity** native info forwarding type.
* `BUNGEE_GUARD` - **BungeeGuard** forwarding type.

If you use BungeeCord, or Velocity with `LEGACY` forwarding, just set this type in the config.  
If you use Velocity with `MODERN` info forwarding, set this type and paste the secret key from
Velocity config into `secret` field.
If you installed BungeeGuard on your proxy, then use `BUNGEE_GUARD` forwarding type.
Then add your tokens to `tokens` list.

### Building

Required software:

* JDK 11+
* Gradle 7+ (optional)

To build a minimized jar, go to the project root directory and run in the terminal:

```
./gradlew shadowJar
```
