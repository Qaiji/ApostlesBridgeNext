
# ApostlesBridgeNext 🌉

**ApostlesBridgeNext**  
A Fabric 1.21.5+ client-side mod designed for Hypixel SkyBlock. It’s an updated migration of [Qaiji/ApostlesBridge](https://github.com/Qaiji/ApostlesBridge), originally built for Forge 1.8.9. The mod is a bridge (using WebSocket technology) between Minecraft "Guild Chat" on Hypixel Skyblock and a Discord channel.

## Features

- Settings to configurate the mod are accessed with `/bridge`
- Modes to even access bridge messages outside of Hypixel's server
- A formatting screen to customize your bridge visuals
- Ignore lists for players and origins:
  - `/bridge ignore add player <name>`
  - `/bridge ignore remove origin <name>`
  - `/bridge ignore list`
- Other commands:
  - `/bridge reconnect` — Reconnects the WebSocket session
  - `/bridge status` — Shows the current WebSocket status
  - `/bridge help` — Shows command usages

_(WebSocket connection properties are found on the discord)_

---

## Installation

1. Download the latest release JAR or build it yourself via Gradle.
2. Drop it into your <minecraft>/mods folder.
3. Run Minecraft with Fabric 1.21.5+ (tested with Fabric Loader 0.16.14+ and Fabric API).

---

## Usage

- Open the settings via `/bridge`
- Within the GUI:
  - Set up the WebSocket URL, Token, or Guild ID (optional)
  - Choose the Mode:
    - HYPIXEL_ONLY — the bridge messages are only displayed if connected the hypixel.net
    - EVERYWHERE — the messages are displayed no matter where you are (even in singleplayer)
    - OFF — the bridge is turned off
  - (Optional) Update your formatting settings
- Save and exit to apply changes (ESC or close the GUI).
- You're good to go!

---

## Configuration Storage

Settings are automatically saved to the mod’s config files when changed and persist across sessions. (<minecraft>/config/apostles.json)

---

## Requirements

- Minecraft 1.21.5+
- Fabric Loader 0.16.14+
- Fabric API
