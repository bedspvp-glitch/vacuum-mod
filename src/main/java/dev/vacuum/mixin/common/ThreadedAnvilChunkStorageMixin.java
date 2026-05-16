package dev.vacuum.mixin.common;

import dev.vacuum.VacuumMod;
import dev.vacuum.config.VacuumConfig;
import net.minecraft.server.world.ServerChunkLoadingManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.concurrent.Semaphore;

@Mixin(ServerChunkLoadingManager.class)
public class ThreadedAnvilChunkStorageMixin {
    private static volatile Semaphore vacuum$genSemaphore = null;

    private static Semaphore getGenSemaphore() {
        if (vacuum$genSemaphore == null) {
            synchronized (ThreadedAnvilChunkStorageMixin.class) {
                if (vacuum$genSemaphore == null) {
                    int permits = VacuumMod.getConfig().maxSimultaneousChunkGenerations;
                    vacuum$genSemaphore = new Semaphore(Math.max(1, permits), true);
                }
            }
        }
        return vacuum$genSemaphore;
    }

    @Inject(method = "scheduleChunkLoad", at = @At("HEAD"), require = 0)
    private void vacuum$beforeGenerate(CallbackInfo ci) {
        VacuumConfig cfg = VacuumMod.getConfig();
        if (cfg.maxSimultaneousChunkGenerations <= 0) return;
        try { getGenSemaphore().acquire(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

    @Inject(method = "scheduleChunkLoad", at = @At("RETURN"), require = 0)
    private void vacuum$afterGenerate(CallbackInfo ci) {
        VacuumConfig cfg = VacuumMod.getConfig();
        if (cfg.maxSimultaneousChunkGenerations <= 0) return;
        getGenSemaphore().release();
    }
}
