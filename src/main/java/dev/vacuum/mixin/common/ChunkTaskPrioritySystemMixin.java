package dev.vacuum.mixin.common;

import dev.vacuum.VacuumMod;
import dev.vacuum.config.VacuumConfig;
import net.minecraft.server.world.ChunkTaskScheduler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChunkTaskScheduler.class)
public class ChunkTaskPrioritySystemMixin {

    private static int vacuum$tasksThisTick = 0;
    private static long vacuum$lastReset = 0L;

    @Inject(method = "scheduleChunkTask", at = @At("HEAD"), cancellable = true)
    private void vacuum$throttleTask(CallbackInfo ci) {
        VacuumConfig cfg = VacuumMod.getConfig();
        if (!cfg.dynamicChunkCap) return;

        long now = System.currentTimeMillis();
        if (now - vacuum$lastReset > 50L) {
            vacuum$tasksThisTick = 0;
            vacuum$lastReset = now;
        }

        if (vacuum$tasksThisTick >= cfg.chunkLoadingCap * 2) {
            if (cfg.logOptimizationEvents) {
                VacuumMod.LOGGER.debug("[Vacuum] Chunk task throttled");
            }
            ci.cancel();
            return;
        }

        vacuum$tasksThisTick++;
    }
}
