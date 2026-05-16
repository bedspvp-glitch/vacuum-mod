package dev.vacuum;

import dev.vacuum.config.VacuumConfig;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VacuumMod implements ModInitializer {

    public static final String MOD_ID = "vacuum";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static VacuumConfig config;

    @Override
    public void onInitialize() {
        config = VacuumConfig.load();
        LOGGER.info("[Vacuum] Loaded config. Chunk cap: {}, Entity tracking cap: {}",
                config.chunkLoadingCap, config.entityTrackingCap);

        registerCommands();

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            LOGGER.info("[Vacuum] Server started - applying optimizations.");
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            VacuumConfig.save(config);
            LOGGER.info("[Vacuum] Server stopping - config saved.");
        });

        LOGGER.info("[Vacuum] Initialized v1.1.0 for Minecraft 1.21.4");
    }

    private void registerCommands() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(
                CommandManager.literal("vacuum")
                    .requires(src -> src.hasPermissionLevel(2))

                    // /vacuum reload
                    .then(CommandManager.literal("reload")
                        .executes(ctx -> {
                            config = VacuumConfig.load();
                            LOGGER.info("[Vacuum] Config reloaded via command.");
                            ctx.getSource().sendFeedback(
                                () -> Text.literal("[Vacuum] Config reloaded successfully."),
                                true
                            );
                            return 1;
                        })
                    )

                    // /vacuum status
                    .then(CommandManager.literal("status")
                        .executes(ctx -> {
                            ctx.getSource().sendFeedback(() -> Text.literal(
                                "[Vacuum] Chunk cap: " + config.chunkLoadingCap +
                                " | Entity cap: " + config.entityTrackingCap +
                                " | Dynamic cap: " + config.dynamicChunkCap +
                                " | Entity culling: " + config.entityCulling
                            ), false);
                            return 1;
                        })
                    )

                    // /vacuum save
                    .then(CommandManager.literal("save")
                        .executes(ctx -> {
                            VacuumConfig.save(config);
                            ctx.getSource().sendFeedback(
                                () -> Text.literal("[Vacuum] Config saved to disk."),
                                false
                            );
                            return 1;
                        })
                    )
            );
        });
    }

    public static VacuumConfig getConfig() {
        return config;
    }
}
