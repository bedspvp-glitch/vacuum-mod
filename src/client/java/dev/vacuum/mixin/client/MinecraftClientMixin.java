package dev.vacuum.mixin.client;

import dev.vacuum.VacuumMod;
import dev.vacuum.config.VacuumConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {

    @Shadow private int currentFps;

    @Inject(method = "tick", at = @At("HEAD"))
    private void vacuum$onTick(CallbackInfo ci) {
        VacuumConfig cfg = VacuumMod.getConfig();
        if (!cfg.autoPerformanceMode || currentFps <= 0) return;

        if (currentFps < cfg.autoLowFpsThreshold) {
            // Emergency low-FPS mode: reduce everything
            cfg.chunkLoadingCap    = Math.max(1, cfg.chunkLoadingCap - 1 > 2 ? 2 : cfg.chunkLoadingCap);
            cfg.maxParticleCount   = Math.min(cfg.maxParticleCount, 512);
            cfg.maxSimultaneousSounds = Math.min(cfg.maxSimultaneousSounds, 16);
            if (cfg.logOptimizationEvents) {
                VacuumMod.LOGGER.debug("[Vacuum] Auto-perf: LOW FPS ({}) - tightening limits", currentFps);
            }
        } else if (currentFps < cfg.autoHighFpsThreshold) {
            // Medium FPS: moderate adjustments
            cfg.chunkLoadingCap    = 4;
            cfg.maxParticleCount   = Math.min(cfg.maxParticleCount, 2048);
            cfg.maxSimultaneousSounds = Math.min(cfg.maxSimultaneousSounds, 32);
        } else {
            // Good FPS: relax all limits to configured maximums
            cfg.chunkLoadingCap    = 8;
        }
    }
}
