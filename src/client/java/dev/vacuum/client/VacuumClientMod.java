package dev.vacuum.client;

import dev.vacuum.VacuumMod;
import dev.vacuum.client.gui.VacuumHudOverlay;
import dev.vacuum.client.render.EntityCullingSystem;
import dev.vacuum.config.VacuumConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

@Environment(EnvType.CLIENT)
public class VacuumClientMod implements ClientModInitializer {

    private static EntityCullingSystem entityCullingSystem;
    private static VacuumHudOverlay hudOverlay;

    @Override
    public void onInitializeClient() {
        VacuumConfig cfg = VacuumMod.getConfig();

        entityCullingSystem = new EntityCullingSystem();
        hudOverlay = new VacuumHudOverlay();

        // Always register the overlay — it checks cfg.showDebugOverlay internally each frame
        // so toggling it in the settings screen works without a restart.
        HudRenderCallback.EVENT.register(hudOverlay::render);

        VacuumMod.LOGGER.info("[Vacuum] Client initialized. Entity culling: {}, Client-side hits: {}",
                cfg.entityCulling, cfg.clientSideHitRegistration);
    }

    public static EntityCullingSystem getEntityCullingSystem() {
        return entityCullingSystem;
    }
}
