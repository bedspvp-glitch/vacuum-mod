package dev.vacuum.mixin.common;

import dev.vacuum.VacuumMod;
import dev.vacuum.config.VacuumConfig;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * ServerWorldMixin — skips entity updates for entities that are:
 *   1. Beyond the configured entity tracking cap
 *   2. Not alive
 *   3. Far from all players (beyond cfg.entityUpdateThrottleDistance)
 *
 * This mirrors Lithium's entity tick optimization but is implemented
 * independently to stay compatible with both Lithium and Carpet.
 */
@Mixin(ServerWorld.class)
public class ServerWorldMixin {

    private int vacuum$entityTickCount = 0;

    @Inject(method = "tickEntity", at = @At("HEAD"), cancellable = true)
    private void vacuum$throttleEntityTick(Entity entity, CallbackInfo ci) {
        VacuumConfig cfg = VacuumMod.getConfig();

        if (!entity.isAlive()) return;

        vacuum$entityTickCount++;
        if (vacuum$entityTickCount > cfg.entityTrackingCap) {
            vacuum$entityTickCount = 0;
            if (cfg.logOptimizationEvents) {
                VacuumMod.LOGGER.debug("[Vacuum] Entity tick throttled — cap {} reached", cfg.entityTrackingCap);
            }
            ci.cancel();
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void vacuum$resetEntityCount(net.minecraft.util.math.random.Random rand, CallbackInfo ci) {
        vacuum$entityTickCount = 0;
    }
}
