package dev.vacuum;

import dev.vacuum.config.VacuumConfig;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.ChunkPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VacuumMod implements ModInitializer {

    public static final String MOD_ID = "vacuum";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static VacuumConfig config;
    private static long tickStartNanos = 0L;
    private static long lastPreloadTick = 0L;

    @Override
    public void onInitialize() {
        config = VacuumConfig.load();
        LOGGER.info("[Vacuum] Loaded config. Chunk cap: {}, Entity tracking cap: {}",
                config.chunkLoadingCap, config.entityTrackingCap);

        registerCommands();
        registerTickEvents();

        ServerLifecycleEvents.SERVER_STARTED.register(server ->
                LOGGER.info("[Vacuum] Server started — optimizations active."));

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            VacuumConfig.save(config);
            LOGGER.info("[Vacuum] Server stopping — config saved.");
        });

        LOGGER.info("[Vacuum] Initialized v1.1.0 for Minecraft 1.21.4");
    }

    // ── Commands ──────────────────────────────────────────────────────────────

    private void registerCommands() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
            dispatcher.register(
                CommandManager.literal("vacuum")
                    .requires(src -> src.hasPermissionLevel(2))

                    .then(CommandManager.literal("reload")
                        .executes(ctx -> {
                            config = VacuumConfig.load();
                            LOGGER.info("[Vacuum] Config reloaded via command.");
                            ctx.getSource().sendFeedback(
                                () -> Text.literal("[Vacuum] Config reloaded successfully."), true);
                            return 1;
                        }))

                    .then(CommandManager.literal("status")
                        .executes(ctx -> {
                            ctx.getSource().sendFeedback(() -> Text.literal(
                                "[Vacuum] chunk-cap=" + config.chunkLoadingCap +
                                " entity-cap=" + config.entityTrackingCap +
                                " dynamic=" + config.dynamicChunkCap +
                                " culling=" + config.entityCulling +
                                " auto-perf=" + config.autoPerformanceMode), false);
                            return 1;
                        }))

                    .then(CommandManager.literal("save")
                        .executes(ctx -> {
                            VacuumConfig.save(config);
                            ctx.getSource().sendFeedback(
                                () -> Text.literal("[Vacuum] Config saved to disk."), false);
                            return 1;
                        }))

                    .then(CommandManager.literal("preset")
                        .then(CommandManager.literal("potato")
                            .executes(ctx -> applyPreset(ctx, "potato")))
                        .then(CommandManager.literal("balanced")
                            .executes(ctx -> applyPreset(ctx, "balanced")))
                        .then(CommandManager.literal("ultra")
                            .executes(ctx -> applyPreset(ctx, "ultra"))))
            )
        );
    }

    private static int applyPreset(
            com.mojang.brigadier.context.CommandContext<net.minecraft.server.command.ServerCommandSource> ctx,
            String name) {
        config = switch (name) {
            case "potato"   -> VacuumConfig.potato();
            case "ultra"    -> VacuumConfig.ultra();
            default         -> VacuumConfig.balanced();
        };
        VacuumConfig.save(config);
        LOGGER.info("[Vacuum] Preset '{}' applied.", name);
        ctx.getSource().sendFeedback(
            () -> Text.literal("[Vacuum] Preset '" + name + "' applied and saved."), true);
        return 1;
    }

    // ── Tick events ───────────────────────────────────────────────────────────

    private void registerTickEvents() {
        // Mark tick start time for lag spike detection
        ServerTickEvents.START_SERVER_TICK.register(server -> {
            if (config.lagSpikeDetector) tickStartNanos = System.nanoTime();
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            // Lag spike detector
            if (config.lagSpikeDetector && tickStartNanos > 0) {
                long elapsedMs = (System.nanoTime() - tickStartNanos) / 1_000_000L;
                if (elapsedMs > config.lagSpikeThresholdMs) {
                    LOGGER.warn("[Vacuum] Lag spike detected: {}ms (threshold: {}ms)",
                            elapsedMs, config.lagSpikeThresholdMs);
                }
            }

            // Chunk preloader (runs once per second to avoid overhead)
            if (config.enableChunkPreloader) {
                long tick = server.getTicks();
                if (tick - lastPreloadTick >= 20L) {
                    lastPreloadTick = tick;
                    preloadChunksNearPlayers(server);
                }
            }
        });
    }

    private static void preloadChunksNearPlayers(MinecraftServer server) {
        int radius = Math.min(config.chunkPreloadRadius, 3);
        for (ServerWorld world : server.getWorlds()) {
            for (ServerPlayerEntity player : world.getPlayers()) {
                ChunkPos center = player.getChunkPos();
                for (int dx = -radius; dx <= radius; dx++) {
                    for (int dz = -radius; dz <= radius; dz++) {
                        int cx = center.x + dx;
                        int cz = center.z + dz;
                        if (!world.isChunkLoaded(cx, cz)) {
                            // Non-blocking: request chunk load at low priority
                            world.getChunkManager().getWorldChunk(cx, cz);
                        }
                    }
                }
            }
        }
    }

    public static VacuumConfig getConfig() { return config; }
}
