# Changelog

## [1.0.0] — Initial Release

First public release of Vacuum for Minecraft 1.21.4 (Fabric).

### ✨ Added

#### Chunk Optimizations
- Dynamic chunk loading cap — auto-adjusts based on current FPS to protect frame time during fast travel
- Chunk generation thread cap — prevents world gen from starving the main server thread
- Client-side chunk packet rate limiter — stops high view-distance servers from flooding the render thread
- Low-priority chunk task throttler — defers background chunk tasks when the per-tick budget is exhausted
- Configurable chunk builder thread count — override vanilla's conservative default from the settings screen

#### Entity Optimizations
- Camera frustum culling — entities outside your field of view are skipped entirely, saving draw calls
- Distance-based entity render throttling — distant entities rendered on alternating frames
- Server-side entity tracking cap — smooths out lag spikes from mob farms and item explosions
- Server-side entity tick cap — prevents excessive entity counts from consuming full server ticks
- Invisible tile entity render skip — skips tile entities with no visible geometry

#### Client-Side Latency (cosmetic/perceptual only)
- Client-side hit registration — sword hit sound and particles fire the same frame as your click
- Predictive arm swing — arm animation starts immediately on input, not next tick
- Instant block interaction — block-breaking swing fires without tick alignment delay
- Predictive damage tint — entity hurt flash shows on the same frame as your hit

#### Render & Audio
- Smart particle cap — hard ceiling on active particles (default 4096, configurable)
- Distant sound culling — skips sounds beyond a configurable radius (default 96 blocks)
- OpenAL channel cap — prevents audio stutter from simultaneous sound overload
- Optional fog density reduction for lower-end GPUs

#### Settings
- Full in-game settings screen via Mod Menu (Mod Menu → Vacuum → Settings)
- Five organized sections: Chunk, Entity, Latency, Render & Audio, Debug
- Live debug HUD overlay showing entity cull counts, active chunk cap, particle and sound counts
- Config saved to `.minecraft/config/vacuum.json`, reloads on screen close

### 🔧 Technical
- All gameplay-affecting features use conservative defaults
- Client latency features are cosmetic only — safe on any server, undetectable
- No core systems replaced — Mixin injection at specific call sites only
- Compatible with Sodium, Lithium, Iris, Carpet, Continuity, LambDynamicLights
