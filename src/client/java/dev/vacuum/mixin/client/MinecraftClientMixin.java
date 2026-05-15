package dev.vacuum.mixin.client;

import dev.vacuum.VacuumMod;
import dev.vacuum.config.VacuumConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * MinecraftClientMixin — hooks into the main game loop tick to apply
 * per-frame Vacuum bookkeeping: resetting counters, applying dynamic chunk
 * cap adjustments based on current FPS, and logging debug stats.
 */
@Environment(EnvType.CLIENT)
@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {

    @Shadow private int currentFps;

    @Inject(method = "tick", at = @At("HEAD"))
    private void vacuum$onTick(CallbackInfo ci) {
        VacuumConfig cfg = VacuumMod.getConfig();

        // Dynamic chunk cap: reduce cap when FPS is low to protect frame time
        if (cfg.dynamicChunkCap && currentFps > 0) {
            if (currentFps < 30) {
                cfg.chunkLoadingCap = 2;
            } else if (currentFps < 60) {
                cfg.chunkLoadingCap = 4;
            } else {
                cfg.chunkLoadingCap = 8;
            }
        }
    }
}
