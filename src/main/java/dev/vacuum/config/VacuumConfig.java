package dev.vacuum.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.vacuum.VacuumMod;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public class VacuumConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir().resolve("vacuum.json");

    // ── Chunk optimizations ───────────────────────────────────────────────────
    public int chunkLoadingCap = 4;
    public int chunkUnloadDelay = 60;
    public boolean dynamicChunkCap = true;
    public int maxSimultaneousChunkGenerations = 2;

    // ── Entity optimizations ──────────────────────────────────────────────────
    public int entityTrackingCap = 512;
    public boolean entityCulling = true;
    public boolean frustumCulling = true;
    public int entityUpdateThrottleDistance = 48;

    // ── Client-side latency ───────────────────────────────────────────────────
    public boolean clientSideHitRegistration = true;
    public boolean predictiveSwing = true;
    public boolean instantBlockInteraction = true;
    public boolean predictiveDamage = true;

    // ── Render optimizations ──────────────────────────────────────────────────
    public boolean smartParticles = true;
    public int maxParticleCount = 4096;
    public boolean reduceOpaqueFogDensity = false;
    public boolean deferredChunkMeshBuilding = true;
    public boolean skipInvisibleTileEntityRender = true;
    public boolean reducedSkyRendering = false;
    public boolean cacheEntityRendering = true;

    // ── Chunk mesh / build ────────────────────────────────────────────────────
    public int chunkBuilderThreads = Math.max(1, Runtime.getRuntime().availableProcessors() / 2);
    public boolean sortTranslucentGeometry = true;

    // ── Sound ─────────────────────────────────────────────────────────────────
    public int maxSimultaneousSounds = 64;
    public boolean cullDistantSounds = true;
    public int soundCullDistance = 96;

    // ── Misc ──────────────────────────────────────────────────────────────────
    public boolean showDebugOverlay = false;
    public boolean logOptimizationEvents = false;

    public static VacuumConfig load() {
        if (Files.exists(CONFIG_PATH)) {
            try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
                return GSON.fromJson(reader, VacuumConfig.class);
            } catch (IOException e) {
                VacuumMod.LOGGER.warn("[Vacuum] Failed to read config, using defaults: {}", e.getMessage());
            }
        }
        VacuumConfig defaults = new VacuumConfig();
        save(defaults);
        return defaults;
    }

    public static void save(VacuumConfig config) {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
                GSON.toJson(config, writer);
            }
        } catch (IOException e) {
            VacuumMod.LOGGER.error("[Vacuum] Failed to save config: {}", e.getMessage());
        }
    }
}
