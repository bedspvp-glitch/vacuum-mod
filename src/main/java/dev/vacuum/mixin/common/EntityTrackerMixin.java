package dev.vacuum.mixin.common;

import dev.vacuum.VacuumMod;
import dev.vacuum.config.VacuumConfig;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * EntityTrackerMixin — caps the number of entity tracker entries created per tick.
 *
 * Vanilla tracks every entity individually and sends update packets for each one
 * every tick to every nearby player. This caps how many new tracker entries can
 * be created in a single tick, preventing entity-spawning lag spikes (e.g. mob
 * farms, item rain).
 */
@Mixin(targets = "net.minecraft.server.world.ServerWorld$EntityCallbacks")
public class EntityTrackerMixin {

    private static int vacuum$trackersThisTick = 0;
    private static long vacuum$lastReset = 0L;

    @Inject(method = "onStartedTracking", at = @At("HEAD"), cancellable = true)
    private void vacuum$capTrackers(net.minecraft.entity.Entity entity, CallbackInfo ci) {
        VacuumConfig cfg = VacuumMod.getConfig();

        long now = System.currentTimeMillis();
        if (now - vacuum$lastReset > 50L) {
            vacuum$trackersThisTick = 0;
            vacuum$lastReset = now;
        }

        if (vacuum$trackersThisTick >= cfg.entityTrackingCap) {
            if (cfg.logOptimizationEvents) {
                VacuumMod.LOGGER.debug("[Vacuum] Entity tracking deferred — cap {} reached", cfg.entityTrackingCap);
            }
            ci.cancel();
            return;
        }

        vacuum$trackersThisTick++;
    }
}
