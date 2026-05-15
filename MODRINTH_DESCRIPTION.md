# Vacuum ⚡
### The performance mod that actually explains what it does.

Most performance mods are a black box — you install them and hope for the best. Vacuum is different. Every single feature is documented, configurable, and designed to be compatible with everything else in your modpack.

**Vacuum targets the three biggest sources of lag in Minecraft 1.21.10:**
1. Chunk loading overwhelming the CPU
2. Rendering entities you can't even see
3. The tiny but noticeable delay between clicking and feeling something happen

---

## 🗺️ Chunk System Optimizations

### Dynamic Chunk Cap
Vanilla Minecraft will happily try to load and generate dozens of chunks in a single tick if you're moving fast. This causes the characteristic "stutter" when flying with an elytra or riding across open terrain. Vacuum puts a per-tick ceiling on how many chunks can be processed at once.

What makes Vacuum's approach smarter than a static cap: **it reads your current FPS and adjusts automatically.** If you're running at 100+ FPS it opens the cap wide for fast chunk reveal. If you drop below 60 it tightens the cap to protect your frame time. Below 30 FPS it gets very conservative. You always get the best tradeoff for your current hardware load.

### Chunk Generation Thread Cap
Even if chunk *loading* is capped, the background generation threads can still pin your CPU cores and cause the main thread to starve. Vacuum limits how many chunk generation tasks can run simultaneously. The default is 2 — high enough for smooth chunk reveal during normal travel, low enough that a mob farm or explosion won't suddenly kick off 12 generation jobs and freeze your game for a second.

This is fully adjustable in the settings screen. If you have a powerful CPU and want faster chunk generation, you can push it higher.

### Client Chunk Packet Cap
Playing on a server with a high view distance like 32 chunks? When you first join or teleport, the server sends an enormous burst of chunk packets all at once. Vanilla's render thread tries to process all of them immediately — and your FPS tanks to single digits for 3–5 seconds while it catches up. Vacuum rate-limits client chunk processing so the render thread is never overwhelmed, trading a slightly slower chunk reveal for consistently smooth frames throughout the load.

### Configurable Chunk Builder Threads
The chunk mesh builder (the system that turns raw chunk data into geometry your GPU can draw) runs on background threads. Vanilla calculates a conservative thread count from your processor. Vacuum lets you override it directly in the settings — useful if you have a high core-count CPU that vanilla significantly underutilises.

---

## 👾 Entity System Optimizations

### Frustum Culling
This is one of the biggest FPS wins in Vacuum. Every frame, Minecraft asks each entity renderer to draw its entity — even if that entity is directly behind you and you'll never see it. Vacuum builds a camera frustum (the pyramid-shaped volume of what you can actually see) and skips the render call entirely for any entity outside it.

The frustum is recalculated every frame so it's always accurate, and it handles entities at all distances. On a crowded server with 50+ players and mobs around you, this can cut entity render time by 40–60% by eliminating the draw calls for everything behind and beside you.

### Distance-Based Entity Throttling
For entities beyond a configurable distance (default 48 blocks), Vacuum renders them on alternating frames instead of every frame. At 60 FPS, a mob 60 blocks away is rendered 30 times per second instead of 60 — completely unnoticeable to the human eye but a significant reduction in GPU work when there are many distant entities.

### Server-Side Entity Tracking Cap
Vanilla creates an entity tracker entry for every loaded entity and sends position/state updates for each one to every nearby player every tick. On servers with mob farms this can mean thousands of tracking updates per tick. Vacuum caps how many new tracker entries can be created per tick, smoothing out the lag spikes that happen when large numbers of entities spawn at once.

### Server-Side Entity Tick Cap
Similarly, the server-side entity tick loop is capped so that an abnormally large number of entities (from a farm, item drops from an explosion, etc.) can't consume an entire server tick and cause everyone's ping to spike.

### Invisible Tile Entity Skip
Certain tile entities (like invisible item frames, hidden hoppers behind blocks, or off-screen chests) still get rendered every frame in vanilla even though they're completely hidden. Vacuum skips the render call for tile entities that have no visible geometry to draw.

---

## ⚔️ Client-Side Latency Reduction
> **Important:** All of the following are purely cosmetic/perceptual changes. No damage is applied client-side. The server always validates and applies real damage. These features only affect what *you see and hear* on your screen.

### The Problem
When you click to attack in vanilla Minecraft, here's what happens:
1. Your client registers the click
2. It waits for the next game tick (up to 50ms at 20TPS)
3. It sends the attack packet to the server
4. The server processes it
5. The server sends back a response
6. Your client plays the hit sound and shows the hurt flash

The total perceptible delay from click to feedback can be 2–4ms even on a LAN server. On a real server it's much more. This makes PvP feel "floaty" — like your hits aren't registering when they actually are.

### Vacuum's Solution: Client-Side Prediction
Vacuum fires the hit feedback **the same frame as your click** — before any packet is sent. You hear the impact sound, see the hit particles, and see the entity flash red at the exact moment you click. Meanwhile the actual attack packet goes to the server and real damage is applied normally.

The result: attacking feels **instant**. The gap between clicking and feeling your hit land drops from several milliseconds to effectively 0. This is the same technique competitive FPS games use for client-side hit registration.

| Feature | What changes |
|---|---|
| **Client-side hit registration** | Hit sound and particles fire the same frame as your click |
| **Predictive arm swing** | Your arm swings immediately — not next tick |
| **Instant block interaction** | Block breaking starts the frame you click — no tick alignment wait |
| **Predictive damage tint** | Entity flashes red the moment you click, not after server round-trip |

Each of these can be toggled independently in the settings screen.

---

## 🔊 Render & Audio Optimizations

### Smart Particle Cap
Particle effects are cheap individually but they stack. A mob farm producing hundreds of death particles, a large explosion, or a beacon with max effects can push particle counts into the tens of thousands and obliterate frame time. Vacuum hard-caps the active particle count (default 4096, adjustable) and simply doesn't spawn new particles once the cap is hit. This is more aggressive than vanilla's "minimal/decreased/all" slider — it's an absolute ceiling that protects you no matter what's happening in the world.

### Distant Sound Culling
Sounds beyond a configurable radius (default 96 blocks) are skipped entirely. Minecraft's sound system runs on the CPU and processes every sound source each tick — including ones you're far too away to hear. Culling them reduces CPU audio processing time, especially in areas with many entities or machines.

### OpenAL Channel Cap
LWJGL's OpenAL has a hardware limit on simultaneous audio channels (typically 256). When vanilla exceeds this, sounds are silently dropped — but the overhead of attempting to play them still exists. Vacuum caps the count before it hits the hardware limit, ensuring clean audio and preventing the audio thread from becoming a bottleneck.

### Optional Fog Density Reduction
For players on lower-end GPUs, thick fog (deep underground, inside clouds) can cause noticeable frame drops because of per-vertex fog calculations across all terrain geometry. Vacuum's optional fog density reduction slightly decreases thick-fog intensity, saving GPU time at the cost of slightly thinner fog. Off by default.

---

## ⚙️ Settings Screen

Every single feature above is individually toggleable and most have a slider to tune the intensity. The settings screen is accessible from:

**Mod Menu → Vacuum → Settings**

Settings are organised into five sections:
- **Chunk Optimizations** — caps, thread counts, deferred meshing
- **Entity Optimizations** — culling, throttle distance, tracking cap
- **Client Latency** — hit registration, swing, block interaction, damage tint
- **Render & Audio** — particles, sounds, fog
- **Debug** — live HUD overlay showing culled entities, active chunk cap, particle and sound counts in real time

Changes take effect immediately and are saved to `.minecraft/config/vacuum.json` when you close the screen.

---

## 🤝 Compatibility & Design Philosophy

Vacuum is designed from the ground up to coexist with other mods.

**No core system replacements.** Mods like Sodium, Starlight, and Lithium replace entire rendering or lighting engines. This makes them very powerful but also a potential source of conflicts. Vacuum only injects at specific, narrow call sites using Mixins — it never replaces a class wholesale.

**Conservative defaults.** Every gameplay-affecting feature (server tick caps, chunk throttling) uses conservative defaults that prioritise stability over maximum performance. You can push the sliders harder if your server can handle it.

**Client-side features are cosmetic only.** The latency features work on any server — vanilla, Paper, Spigot, modded — because they never send any extra packets or modify server-side logic.

**Tested alongside:** Sodium, Lithium, Iris, Carpet, Fabric API, Mod Menu, Continuity, LambDynamicLights.

---

## 📦 Installation

1. Install [Fabric Loader](https://fabricmc.net/use/installer/) for Minecraft 1.21.10
2. Download [Fabric API](https://modrinth.com/mod/fabric-api)
3. Download Vacuum (this mod)
4. Optionally download [Mod Menu](https://modrinth.com/mod/modmenu) for the in-game settings screen
5. Drop all `.jar` files into your `.minecraft/mods/` folder
6. Launch and play — Vacuum works out of the box with sensible defaults

---

## ❓ FAQ

**Q: Will this get me banned on servers?**
No. The client-side latency features are purely visual — they don't send extra packets, don't modify your attack range or damage, and don't give you any gameplay advantage the server can detect.

**Q: Does this work with Sodium?**
Yes. Vacuum's render optimizations are complementary to Sodium's. Sodium replaces the chunk renderer; Vacuum handles entity culling and audio — they don't overlap.

**Q: Can I use this in my modpack?**
Yes, MIT license — no credit required (though appreciated).

**Q: The chunk cap makes chunks load slowly. How do I fix it?**
Open Mod Menu → Vacuum → Settings → Chunk Optimizations → increase the Chunk Load Cap slider, or turn off Dynamic Chunk Cap to use a fixed value.

---

## 📄 License

MIT — free to use, modify, redistribute, and include in any modpack.  
Source code: [github.com/bedspvp-glitch/vacuum-mod](https://github.com/bedspvp-glitch/vacuum-mod)
