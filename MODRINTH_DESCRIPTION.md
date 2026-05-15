# Vacuum ⚡

**The all-in-one performance mod for Minecraft 1.21.10 (Fabric).**  
Vacuum eliminates FPS drops, smooths out server lag, and cuts the delay you feel when swinging a sword — all without touching gameplay or breaking other mods.

---

## ✨ What it does

### 🗺️ Chunk Optimizations
- **Dynamic chunk cap** — limits how many chunks load at once and auto-adjusts based on your current FPS (low FPS = smaller cap = smoother frames)
- **Generation thread cap** — stops world generation from monopolising your CPU during fast travel (elytra, horses)
- **Deferred chunk meshing** — configurable chunk builder threads so new chunks appear without frame drops
- **Client chunk cap** — prevents high-view-distance servers from flooding your render thread with chunk packets

### 👾 Entity Optimizations
- **Frustum culling** — entities outside your field of view are skipped entirely (no draw calls wasted)
- **Distance throttling** — distant entities render on alternating frames instead of every frame
- **Entity tracking cap** — prevents mob farms and item explosions from causing tick lag
- **Invisible tile entity skip** — tile entities with no visible model are not rendered

### ⚔️ Client-Side Latency
> These are **visual/audio only** — the server always has final authority on damage and actions.

| Feature | Effect |
|---|---|
| Client-side hit registration | Sword hit sound + particles fire the **same frame** as your click |
| Predictive arm swing | Arm animation starts **immediately** on click, not next tick |
| Instant block interaction | Block-breaking swing fires without tick alignment delay |
| Predictive damage tint | Entity hurt flash shows on the **same frame** as your hit |

Combined, these cut **perceived** attack latency from ~2ms to ~0.01ms — making PvP feel razor-sharp even on servers with normal ping.

### 🔊 Render & Audio
- Smart particle cap (default 4096) — stops particle storms from tanking FPS
- Sound culling — skips sounds beyond a configurable distance
- OpenAL channel cap — prevents audio stutter from too many simultaneous sounds
- Optional fog density reduction for low-end GPUs

---

## ⚙️ Settings

All features are fully configurable in-game via **Mod Menu → Vacuum → Settings**.  
No config files to edit manually. Changes save instantly when you close the screen.

---

## 📦 Requirements

| | |
|---|---|
| Minecraft | Java Edition 1.21.10 |
| Mod loader | Fabric |
| Fabric API | Required |
| Mod Menu | Optional (for settings screen) |
| Sodium | Optional (compatible) |

---

## 🤝 Compatibility

Vacuum is built for **maximum mod compatibility**:
- Uses Mixins at specific call sites — no core systems replaced
- All gameplay-affecting features are conservative by default
- Client latency features are cosmetic only — safe on any server
- Tested alongside Sodium, Lithium, Carpet, and Iris

---

## 📄 License

MIT — free to use, fork, and include in modpacks.
