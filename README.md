# Vortexia Storage Addon

> [!WARNING]
> This is an **experimental** build of the Vortexia Storage Addon under active development. Features are subject to change, and APIs or behaviors may break at any time. Use at your own risk in production environments!

A digital network-based item storage system built specifically for the **Vortexia** Minecraft Plugin ecosystem (Paper 1.21+). Inspired by modern technical mods, it features grid-solving network routing, local database serialization, and fully synced cloud network channels.

## Basic Setup Guide

To get started, you need three core components:

1. **ME Controller**: The brain of the system. All devices must be connected directly or indirectly (through adjacent blocks) to the Controller.
2. **ME Drive**: Houses the **Storage Cells** where items are actually stored.
3. **ME Terminal**: The access interface used to view, deposit, and withdraw items.

**Setup Instructions**: Place the Controller, Drive, and Terminal adjacent to each other. Once correctly placed, you will receive a chat notification: `"Connected ... to the network."`

## Features

- **Grid Network Solver**: Uses Vortexia Core's grid manager to tick and route data packages through connected storage nodes.
- **Upgradable Storage Cells**:
  - **Copper Tier Cell**
  - **Iron Tier Cell**
  - **Gold Tier Cell**
  - **Diamond Tier Cell**
  - **Netherite Tier Cell**
- **Terminal Upgrades**: Supports **Crafting Upgrades** to enable in-terminal crafting grids.
- **Cloud Storage Channels**: Sync storage data across networks seamlessly! Create cloud channels, set passwords, and connect multiple Cloud Drive Nodes via the Cloud Config and Cloud Member GUIs.
- **Wireless Remote**: Access your digital network wirelessly using the Wireless Remote item.
- **Asynchronous Update Checker**: Integrated background check system ensuring your storage network node software is always up to date.

## Crafting Recipes

All custom recipes are dynamically registered inside the Vortexia Core Guide. Here is a baseline reference:

### 1. ME Controller
- **IGI**
- **GRG**
- **IGI**
- *(I: Iron Ingot, G: Gold Ingot, R: Redstone Block)*

### 2. ME Drive
- **III**
- **RCR**
- **III**
- *(I: Iron Ingot, R: Redstone, C: Chest)*

### 3. ME Terminal
- **GGG**
- **RPR**
- **III**
- *(G: Glass Block, R: Redstone, P: Glass Pane, I: Iron Ingot)*

### 4. Storage Cells & Upgrades
- **Copper Cell**: 8 Copper Ingots surrounding 1 Glass Block.
- **Iron Cell**: 8 Iron Ingots surrounding 1 Copper Cell.
- **Gold Cell**: 8 Gold Ingots surrounding 1 Iron Cell.
- **Diamond Cell**: 8 Diamonds surrounding 1 Gold Cell.
- **Netherite Cell**: 8 Netherite Ingots surrounding 1 Diamond Cell.
- **Crafting Upgrade**: 8 Oak Planks surrounding 1 Crafting Table.

## Commands & Permissions

| Command | Permission | Description |
|:---|:---|:---|
| `/vstorage creative` (aliases: `/vs`, `/storage`) | `vortexia.storage.creative` | Opens a creative menu to quickly obtain all storage addon items. |
| `/vstorage getremote` | `vortexia.storage.admin` | Withdraws a Wireless Remote item. |
| `/vstorage cloud create <name> <password>` | `none` (default: true) | Creates a new Cloud Storage channel. |
| `/vstorage cloud join <id> [password]` | `none` (default: true) | Links the currently edited node to a Cloud Storage channel. |
| `/vstorage update` | `vortexia.storage.admin` | Forces a manual update check for the Storage Addon. |

---

> *Copyright belongs to Team Vortexia | Storage Addon developed by: @alikuxac.*
