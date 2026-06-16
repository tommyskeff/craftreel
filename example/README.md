# craftreel example

A minimal Spigot/Paper 1.8 plugin demonstrating craftreel recording and playback.

## Commands

| Command             | Description                                                                 |
|---------------------|-----------------------------------------------------------------------------|
| `/reelrecord [name]`| Toggle recording the 4x4 chunk area around you to `<name>.reel`.            |
| `/reelplay [name]`  | Toggle a looping replay of `<name>.reel` for yourself.                       |

`name` defaults to `demo`. Recordings are saved in `server/plugins/craftreel-example/`.

## Building

```sh
# From this directory. Builds a fat jar straight into server/plugins/craftreel-example.jar.
mvn package
```

The example depends on `craftreel:0.1.0`. If you are working against unreleased
changes, install the library to your local Maven repo first:

```sh
mvn -f ../pom.xml install
```

## Running

A Paper 1.8.8 server lives in `server/` (gitignored). It already includes the
required `packetevents` plugin. After `mvn package`:

```sh
cd server
start.bat
```

Then join the server and try `/reelrecord`, move around, `/reelrecord` again to
save, and `/reelplay` to watch it back.
