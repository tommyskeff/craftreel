# CraftReel

A library for recording and replaying live Minecraft 1.8 gameplay to a compact,
seekable `.reel` file.

CraftReel ties [Reel](https://github.com/tommyskeff/reel)'s game-agnostic
record/replay format to Minecraft. It captures a running world — blocks,
entities, players, sounds, particles, explosions, weather, chat, titles,
sidebars, and the tab header — into the layered, compressed `.reel` format, then
replays it back to spectators inside a throwaway [DynWorld](https://github.com/tommyskeff/dynworld)
world using client-side packet entities. Nothing in a replay touches the real
server: viewers are dropped into a generated world and everything they see is
sent as packets, so playback is cheap and never interferes with live gameplay.

Recordings seek in any direction, loop, and play at arbitrary speed, because the
underlying `.reel` format is chunked and layered rather than a linear event log.

## Installation

Maven, Java 17+. CraftReel is split into modules:

- `craftreel-common` — protocol models and codecs, free of any Bukkit dependency. Depend on this to read or write `.reel` files off-server.
- `craftreel-record` — server-side recording.
- `craftreel-replay` — server-side replay.
- `craftreel-all` — pulls in `record` and `replay` plus the `CraftReel` entry point.

To record and replay on a server, depend on `craftreel-all`:

```xml
<dependency>
    <groupId>dev.tommyjs</groupId>
    <artifactId>craftreel-all</artifactId>
    <version>0.3.0</version>
</dependency>
```

To only read or write recordings, depend on `craftreel-common`:

```xml
<dependency>
    <groupId>dev.tommyjs</groupId>
    <artifactId>craftreel-common</artifactId>
    <version>0.3.0</version>
</dependency>
```

CraftReel runs on a Spigot 1.8.8 server and uses
[packetevents](https://github.com/retrooper/packetevents) for packet I/O. It
pulls in `reel`, `dynworld`, and [Adventure](https://github.com/KyoriPowered/adventure)
for text. The `spigot-api`, `authlib`, and `packetevents-spigot` dependencies are
`provided` — supply them from the server at runtime.

## Recording

Start a recording, point it at a `.reel` writer, then attach recorders for the
parts of the game you want to capture. `CraftReel.record()` builds a
`MinecraftRecording`; the default track registry covers the full protocol, so you
normally only configure the writer.

```java
MinecraftRecording recording = CraftReel.record()
    .setPlugin(this)
    .setReelWriter(w -> w
        .setFile(new File(getDataFolder(), "match.reel"))
        .setLayerStrategy(new LayerStrategy(new long[]{1, 4, 16})))
    .build();

recording.start();
```

A recording is just a frame clock — recorders attach to it and write tracks each
tick. The `WorldRecorder` captures a rectangle of chunks (and the entities,
blocks, sounds, and explosions inside it). Bounds can move while recording, so
you can follow a player around:

```java
int cx = player.getLocation().getBlockX() >> 4;
int cz = player.getLocation().getBlockZ() >> 4;

WorldRecorder worldRecorder = WorldRecorder.attach(recording,
    Identifier.random("world"), player.getWorld(),
    cx - 1, cz - 1, cx + 2, cz + 2,
    player.getLocation().toVector());

recording.addTickListener(() -> {
    int x = player.getLocation().getBlockX() >> 4;
    int z = player.getLocation().getBlockZ() >> 4;
    worldRecorder.setBounds(x - 1, z - 1, x + 2, z + 2);
});
```

The text-layer recorders capture HUD elements as Adventure components. Each has
an `attachDefault(recording)` for the common single-instance case:

```java
TextRecorder text = TextRecorder.attachDefault(recording);
text.title(Component.text("Round start", NamedTextColor.GREEN), Component.empty(), 10, 40, 10);
text.chat(Component.text("GG!"));

SidebarRecorder.attachDefault(recording).recordSidebar(lines);
TabHeaderRecorder.attachDefault(recording).recordHeader(header, footer);
```

When you're finished, `stop()` flushes and closes the file. `getCurrentFrame()`
reports how many frames were written.

```java
long frames = recording.getCurrentFrame();
recording.stop();
```

## Replaying

`CraftReel.replay()` builds a `MinecraftReplay` from a `.reel` reader.
`addDefaultHandlers()` wires up the standard scene handlers (world, entities,
text, controls, spectator protection); add viewers and they're teleported into
the replay world to watch.

```java
MinecraftReplay replay = CraftReel.replay()
    .setPlugin(this)
    .addDefaultHandlers()
    .setReelReader(r -> r.setFile(new File(getDataFolder(), "match.reel")))
    .setLooping(true)
    .setSpeed(1.0)
    .build();

replay.start();
replay.addViewer(player);
```

Each viewer is a spectator in a freshly generated DynWorld world; the replay
rebuilds blocks and entities from the recording and shows them client-side.
Removing a viewer returns them to where they were, and `close()` tears down the
world:

```java
replay.removeViewer(player);
replay.close();
```

To customise playback, swap out handlers (`removeHandlers` / `replaceHandlers` /
`addHandler`) or reach the lower-level `reel` APIs through `setReplayScene` and
`setReplayCursor`. The default handlers freeze world ticking, pin spectators, and
expose replay controls (play/pause, seek, speed) — see
[`replay/base`](src/main/java/dev/tommyjs/craftreel/replay/base).

## How it works

CraftReel models each recordable concern as a `reel` entity carrying typed
tracks, defined in
[`CraftReelProtocol`](src/main/java/dev/tommyjs/craftreel/protocol/CraftReelProtocol.java):
world metadata and environment, chunk-section content (deltas against the legacy
1.8 `char[]` block format), per-entity pose / metadata / equipment / animations /
velocity / potion effects, plus world events (sound, particle, explosion, block
break) and the text layers (chat, title, sidebar, tab header).

Block and world changes are captured by injecting into the NMS world access path
rather than polling — see
[docs/injection-design.md](docs/injection-design.md) for the design.

## Example

A runnable Bukkit plugin lives under [example/](example) — two commands,
`/reelrecord [name]` and `/reelplay [name]`, that record the 4×4 chunk area
around you and play it back on a loop:
[CraftReelExamplePlugin.java](example/src/main/java/dev/tommyjs/craftreelexample/CraftReelExamplePlugin.java).
