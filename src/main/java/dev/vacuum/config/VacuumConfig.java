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

    // Chunk optimizations
    public int    chunkLoadingCap                  = 4;
    public int    chunkUnloadDelay                 = 60;
    public boolean dynamicChunkCap                 = true;
    public int    maxSimultaneousChunkGenerations  = 2;

    // Chunk pre-loader
    public boolean enableChunkPreloader  = true;
    public int     chunkPreloadRadius    = 2;

    // Entity optimizations
    public int     entityTrackingCap               = 512;
    public boolean entityCulling                   = true;
    public boolean frustumCulling                  = true;
    public int     entityUpdateThrottleDistance    = 48;

    // Client-side latency
    public boolean clientSideHitRegistration = true;
    public boolean predictiveSwing           = true;
    public boolean instantBlockInteraction   = true;
    public boolean predictiveDamage          = true;

    // Render optimizations
    public boolean smartParticles               = true;
    public int     maxParticleCount             = 4096;
    public boolean reduceOpaqueFogDensity       = false;
    public boolean deferredChunkMeshBuilding    = true;
    public boolean skipInvisibleTileEntityRender= true;
    public boolean reducedSkyRendering          = false;
    public boolean cacheEntityRendering         = true;

    // Chunk mesh / build
    public int     chunkBuilderThreads       = Math.max(1, Runtime.getRuntime().availableProcessors() / 2);
    public boolean sortTranslucentGeometry   = true;

    // Sound
    public int     maxSimultaneousSounds  = 64;
    public boolean cullDistantSounds      = true;
    public int     soundCullDistance      = 96;

    // Auto-performance mode
    public boolean autoPerformanceMode    = true;
    public int     autoLowFpsThreshold    = 30;
    public int     autoHighFpsThreshold   = 60;

    // Lag spike detector
    public boolean lagSpikeDetector       = true;
    public int     lagSpikeThresholdMs    = 50;

    // Reduced packet spam
    public boolean reducePacketSpam           = false;
    public int     positionUpdateIntervalTicks = 1;

    // Debug / misc
    public boolean showDebugOverlay        = false;
    public boolean logOptimizationEvents   = false;

    // ── Presets ───────────────────────────────────────────────────────────────

    public static VacuumConfig potato() {
        VacuumConfig c = new VacuumConfig();
        c.chunkLoadingCap               = 2;
        c.maxSimultaneousChunkGenerations = 1;
        c.enableChunkPreloader          = false;
        c.entityTrackingCap             = 128;
        c.entityCulling                 = true;
        c.frustumCulling                = true;
        c.smartParticles                = true;
        c.maxParticleCount              = 512;
        c.maxSimultaneousSounds         = 16;
        c.soundCullDistance             = 48;
        c.reduceOpaqueFogDensity        = true;
        c.reducedSkyRendering           = true;
        c.deferredChunkMeshBuilding     = true;
        c.chunkBuilderThreads           = 1;
        c.autoPerformanceMode           = true;
        c.autoLowFpsThreshold           = 20;
        c.autoHighFpsThreshold          = 40;
        c.reducePacketSpam              = true;
        c.positionUpdateIntervalTicks   = 2;
        return c;
    }

    public static VacuumConfig balanced() {
        return new VacuumConfig(); // balanced is the default
    }

    public static VacuumConfig ultra() {
        VacuumConfig c = new VacuumConfig();
        c.chunkLoadingCap               = 8;
        c.maxSimultaneousChunkGenerations = 4;
        c.enableChunkPreloader          = true;
        c.chunkPreloadRadius            = 3;
        c.entityTrackingCap             = 1024;
        c.entityCulling                 = true;
        c.frustumCulling                = true;
        c.smartParticles                = true;
        c.maxParticleCount              = 8192;
        c.maxSimultaneousSounds         = 128;
        c.soundCullDistance             = 128;
        c.deferredChunkMeshBuilding     = true;
        c.chunkBuilderThreads           = Math.max(2, Runtime.getRuntime().availableProcessors() - 2);
        c.autoPerformanceMode           = false;
        c.lagSpikeDetector              = true;
        c.lagSpikeThresholdMs           = 30;
        return c;
    }

    // ── Persistence ───────────────────────────────────────────────────────────

    public static VacuumConfig load() {
        if (Files.exists(CONFIG_PATH)) {
            try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
                VacuumConfig loaded = GSON.fromJson(reader, VacuumConfig.class);
                if (loaded != null) return loaded;
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
