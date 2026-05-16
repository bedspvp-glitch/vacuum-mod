# Changelog — Vacuum Mod

## [1.1.0] — 2026-05-16

### Added
- `/vacuum reload` — reload config from disk without restarting (op level 2)
- `/vacuum status` — show current optimization settings in chat
- `/vacuum save` — write current settings to disk immediately
- `/vacuum preset potato|balanced|ultra` — apply one-click performance presets
- **Config Presets**: Potato (max performance), Balanced (default), Ultra (max quality)
  - Accessible via in-game Mod Menu settings screen and the `/vacuum preset` command
- **Auto-performance mode**: automatically adjusts chunk cap, particle count and sound limit
  when FPS drops below configurable thresholds (default: low=30, high=60)
- **Lag spike detector**: logs a warning to console whenever a server tick exceeds the
  configured threshold (default: 50ms)
- **Chunk preloader**: every second, pre-loads chunks within a configurable radius around
  each player to reduce pop-in during exploration (default radius: 2 chunks)
- **Reduced packet spam**: optional setting to reduce position update frequency
  (off by default — experimental)

### Fixed
- Build: replaced removed `ParticleManager.getCount()` API with own AtomicInteger counter
- All mixin targets now use `require = 0` so unknown method names fail gracefully
- `EntityCullingSystem` method names now match all their callers
- `fabric.mod.json` correctly targets `~1.21.4`
- HUD overlay is now always registered; toggling it in settings takes effect immediately
- Race condition in CI: version bump and Modrinth publish now run atomically in one workflow

### Changed
- Version bumped to 1.1.0

## [1.0.0] — 2026-05-15
- Initial release with chunk caps, entity culling, client-side hit prediction,
  frustum culling, particle cap, sound cap, and Mod Menu settings screen
