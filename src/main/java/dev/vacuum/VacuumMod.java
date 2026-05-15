package dev.vacuum;

import dev.vacuum.config.VacuumConfig;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
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

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            LOGGER.info("[Vacuum] Server started — applying server-side optimizations.");
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            VacuumConfig.save(config);
            LOGGER.info("[Vacuum] Server stopping — config saved.");
        });

        LOGGER.info("[Vacuum] Initialized. Version 1.0.0 for Minecraft 1.21.10");
    }

    public static VacuumConfig getConfig() {
        return config;
    }
}
