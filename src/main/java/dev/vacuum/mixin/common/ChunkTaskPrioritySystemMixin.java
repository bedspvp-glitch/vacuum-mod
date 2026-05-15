package dev.vacuum.mixin.common;

import dev.vacuum.VacuumMod;
import dev.vacuum.config.VacuumConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.util.thread.TaskQueue;

/**
 * ChunkTaskPrioritySystemMixin — intercepts chunk task scheduling to apply
 * Vacuum's dynamic load cap. When the per-tick budget is exhausted, new
 * low-priority chunk tasks are pushed back rather than executed immediately,
 * smoothing out CPU spikes from burst chunk-load events.
 */
@Mixin(targets = "net.minecraft.server.world.ChunkTaskPrioritySystem")
public class ChunkTaskPrioritySystemMixin {

    private static int vacuum$tasksThisTick = 0;
    private static long vacuum$lastReset = 0L;

    @Inject(method = "add", at = @At("HEAD"), cancellable = true)
    private void vacuum$throttleTask(net.minecraft.util.thread.MessageListener<?> executor,
                                     Runnable task, long priority, CallbackInfo ci) {
        VacuumConfig cfg = VacuumMod.getConfig();
        if (!cfg.dynamicChunkCap) return;

        long now = System.currentTimeMillis();
        if (now - vacuum$lastReset > 50L) {
            vacuum$tasksThisTick = 0;
            vacuum$lastReset = now;
        }

        // Cap only low-priority (higher number = lower priority) tasks
        if (priority > 1000L && vacuum$tasksThisTick >= cfg.chunkLoadingCap * 2) {
            if (cfg.logOptimizationEvents) {
                VacuumMod.LOGGER.debug("[Vacuum] Chunk task throttled (priority={})", priority);
            }
            ci.cancel();
            return;
        }

        vacuum$tasksThisTick++;
    }
}
