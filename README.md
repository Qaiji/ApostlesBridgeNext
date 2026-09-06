# ApostlesBridgeNext

[![Modrinth Version](https://img.shields.io/modrinth/v/apostlesbridgenext?logo=modrinth&label=Modrinth)](https://modrinth.com/mod/apostlesbridgenext)
[![Modrinth Downloads](https://img.shields.io/modrinth/dt/apostlesbridgenext?logo=modrinth&label=downloads)](https://modrinth.com/mod/apostlesbridgenext)
[![Minecraft](https://img.shields.io/badge/Minecraft-26.1.x%20%7C%2026.2.x-62B47A)](#supported-versions)
[![Fabric](https://img.shields.io/badge/loader-Fabric-DBD0B4)](https://fabricmc.net/)
[![Environment](https://img.shields.io/badge/environment-client--side-blue)](#requirements)

A Fabric client-side mod for Hypixel SkyBlock that bridges Hypixel guild chat with a Discord channel through a WebSocket connection.

ApostlesBridgeNext is the modern Fabric successor to [Qaiji/ApostlesBridge](https://github.com/Qaiji/ApostlesBridge), which was originally built for Forge 1.8.9.

## Links

- [Install guide](https://apostles.gg/bridge-install/)
- [Modrinth page](https://modrinth.com/mod/apostlesbridgenext)
- [GitHub releases](https://github.com/Qaiji/ApostlesBridgeNext/releases)

## Supported Versions

| Minecraft version | Build target | Notes |
| --- | --- | --- |
| `26.1.x` | `mc26.1` | Tested on `26.1.1` and `26.1.2` |
| `26.2.x` | `mc26.2` | Supported |

Use the JAR that matches your Minecraft version. The `mc26.1` build is intended for the `26.1.x` line, and the `mc26.2` build is intended for the `26.2.x` line.

ApostlesBridgeNext support follows the Minecraft versions that are useful for playing on Hypixel. Older build targets may be removed once Hypixel rotates them out of its supported version range.

## Features

- Bridge Hypixel guild chat messages into Minecraft through the Apostles bridge WebSocket
- Choose where bridge messages appear: Hypixel only, everywhere, or off
- Respect Hypixel's `/g toggle` and disconnect/reconnect the bridge when guild chat is disabled or enabled
- Customize bridge message formatting in the `/bridge` settings menu
- Preview image, GIF, and video thumbnails by hovering supported bridge links in chat
- Convert supported Discord emoji shortcodes into colored emoji previews
- Ignore specific players or message origins
- Debug, status, reconnect, and disconnect commands for troubleshooting

## Installation

The recommended setup path is the [Apostles bridge install guide](https://apostles.gg/bridge-install/).

Manual install:

1. Download the correct release JAR from [Modrinth](https://modrinth.com/mod/apostlesbridgenext) or [GitHub releases](https://github.com/Qaiji/ApostlesBridgeNext/releases).
2. Install Fabric Loader and Fabric API for your Minecraft version.
3. Drop the ApostlesBridgeNext JAR into your Minecraft `mods` folder.
4. Start Minecraft and open `/bridge`.

Bridge connection details such as the WebSocket URL and token are provided through the Apostles bridge setup flow.

## Commands

| Command | Description |
| --- | --- |
| `/bridge` | Opens the settings menu |
| `/bridge reconnect` | Clears the current session and reconnects the WebSocket |
| `/bridge disconnect` | Disconnects the WebSocket |
| `/bridge status` | Shows the current WebSocket status |
| `/bridge debug <message/urls>` | Sends a local debug-style bridge message |
| `/bridge ignore add player <name>` | Ignores bridge messages from a player |
| `/bridge ignore add origin <name>` | Ignores bridge messages from an origin |
| `/bridge ignore remove player <name>` | Removes a player from the ignore list |
| `/bridge ignore remove origin <name>` | Removes an origin from the ignore list |
| `/bridge ignore list` | Shows ignored players and origins |
| `/bridge help` | Shows command usage in chat |

## Requirements

| Minecraft version | Java | Fabric Loader | Fabric API |
| --- | --- | --- | --- |
| `26.1.x` | Java 25 | `0.19.1+` | Required |
| `26.2.x` | Java 25 | `0.19.3+` | Required |

This is a client-side mod.

## Configuration

Open `/bridge` in game to configure the mod. Settings are saved automatically to:

```text
<minecraft>/config/apostles.json
```

## Building

To build and collect all configured Minecraft targets locally:

```bash
./gradlew :26.1:collectReleaseJars
```

Final release JARs are collected in `build/release`.
