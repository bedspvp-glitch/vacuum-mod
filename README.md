# Vacuum — Minecraft 1.21.10 Performance Mod

[![Modrinth](https://img.shields.io/badge/Modrinth-Download-brightgreen?logo=modrinth)](https://modrinth.com/project/vacuummod)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
![Minecraft](https://img.shields.io/badge/Minecraft-1.21.4-green)
![Fabric](https://img.shields.io/badge/Loader-Fabric-orange)

Vacuum is a Fabric performance optimization mod for Minecraft 1.21.10 that reduces FPS drops, caps chunk loading, and cuts client-side action latency from ~2ms to ~0.01ms.

**→ [Download on Modrinth](https://modrinth.com/project/vacuummod)**

---

## Features

### Chunk Optimizations
| Feature | Description |
|---|---|
| **Dynamic Chunk Cap** | Limits simultaneous chunk loads per tick. Auto-scales with FPS (<30 FPS → cap 2, <60 FPS → cap 4, 60+ FPS → cap 8) |
| **Gen Thread Cap** | Caps simultaneous world generation tasks so terrain gen never stalls the server thread |
| **Client Chunk Cap** | Prevents chunk packet floods from tanking render thread FPS on high-view-distance servers |
| **Chunk Task Throttler** | Defers low-priority chunk tasks when the per-tick budget is exhausted |
| **Deferred Chunk Meshing** | Configurable chunk builder thread count (default: CPU count / 2) |

### Entity Optimizations
| Feature | Description |
|---|---|
| **Frustum Culling** | Skips rendering entities outside the camera frustum using a lightweight plane-equation test |
| **Entity Culling** | Throttles rendering of distant entities on alternating ticks |
| **Entity Tracking Cap** | Limits new entity tracker entries per tick (prevents mob-farm lag spikes) |
| **Server Entity Tick Cap** | Caps entity ticks per server tick to protect TPS |
| **Skip Invisible TileEntities** | Skips rendering tile entities with no visible model |

### Client-Side Latency (cosmetic/perceptual only — server still validates all actions)
| Feature | Perceived improvement |
|---|---|
| **Client-Side Hit Registration** | Sword hit audio + particles fire the same frame as the click (~0.01ms perceived vs ~2ms round-trip) |
| **Predictive Swing** | Arm swing animation starts immediately on click, not next tick |
| **Instant Block Interaction** | Block-breaking swing fires immediately without tick alignment delay |
| **Predictive Damage Tint** | Entity hurt tint shows on the same frame as the hit |

### Render & Audio
| Feature | Description |
|---|---|
| **Smart Particles** | Hard-caps active particles at `maxParticleCount` (default 4096) |
| **Sound Culling** | Skips sounds beyond `soundCullDistance` blocks (default 96) |
| **Max Simultaneous Sounds** | Caps OpenAL channels to prevent audio stutter from overload |
| **Fog Density Reduction** | Optional: slightly reduces thick-cave fog density overhead |

### Settings
All features are configurable in-game via **Mod Menu → Vacuum → Settings**.  
Config is saved to `.minecraft/config/vacuum.json` and hot-reloads on screen close.

---

## Requirements

| Dependency | Version | Required? |
|---|---|---|
| Minecraft (Java Edition) | 1.21.10 | ✅ |
| Fabric Loader | ≥ 0.16.0 | ✅ |
| Fabric API | 0.115.0+1.21.10 | ✅ |
| Mod Menu | 13.0.0 | Optional (for settings screen) |
| Sodium | mc1.21.10-0.6.9 | Optional (API compat) |

---

## Building

### Prerequisites
- JDK 21
- Internet connection (Gradle downloads Minecraft mappings on first build)

### Steps

```bash
# 1. Make gradlew executable (macOS / Linux)
chmod +x gradlew

# 2. Build the mod JAR
./gradlew build

# Windows:
gradlew.bat build
```

The compiled JAR will appear at:
```
build/libs/vacuum-<version>.jar
```

Drop it into your `.minecraft/mods/` folder alongside Fabric API (and optionally Mod Menu).

### Updating dependency versions

If Fabric has released newer versions for 1.21.10, update these lines in `gradle.properties`:

```properties
yarn_mappings=1.21.10+build.X   # check https://fabricmc.net/develop
loader_version=0.16.X
fabric_version=0.XXX.0+1.21.10
modmenu_version=XX.0.0
sodium_version=mc1.21.10-X.X.X
```

---

## Compatibility

Vacuum is designed for maximum compatibility:
- All optimizations that could affect gameplay (server entity ticks, chunk throttling) are **opt-in or conservative by default**
- Client-side latency features are **cosmetic only** — the server always has final authority
- No core game systems are replaced (unlike Sodium/Starlight) — Vacuum uses Mixins to inject at specific call sites
- Tested alongside Lithium, Carpet, and Sodium mixins — no known conflicts

---

## License

MIT — free to use, modify, and redistribute.
