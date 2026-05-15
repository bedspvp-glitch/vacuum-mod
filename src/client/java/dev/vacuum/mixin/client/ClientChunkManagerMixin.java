package dev.vacuum.mixin.client;

import dev.vacuum.VacuumMod;
import dev.vacuum.config.VacuumConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.world.ClientChunkManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * ClientChunkManagerMixin — caps how many chunks the client processes per tick
 * on the render thread. High server view distances can flood the client with
 * chunk packets; without a cap this stalls the render thread and tanks FPS.
 *
 * This is the client-side counterpart to ServerChunkManagerMixin.
 */
@Environment(EnvType.CLIENT)
@Mixin(ClientChunkManager.class)
public class ClientChunkManagerMixin {

    private static final AtomicInteger CLIENT_LOADS_THIS_TICK = new AtomicInteger(0);
    private static long lastReset = 0L;

    @Inject(method = "loadChunkFromPacket", at = @At("HEAD"), cancellable = true)
    private void vacuum$capClientChunkLoad(int x, int z,
                                            net.minecraft.util.math.ChunkPos chunkPos,
                                            net.minecraft.nbt.NbtCompound nbt,
                                            CallbackInfo ci) {
        VacuumConfig cfg = VacuumMod.getConfig();
        if (!cfg.dynamicChunkCap) return;

        long now = System.currentTimeMillis();
        if (now - lastReset > 50L) {
            CLIENT_LOADS_THIS_TICK.set(0);
            lastReset = now;
        }

        // Allow at least 2× the server cap on the client to avoid visual gaps
        int clientCap = cfg.chunkLoadingCap * 2;
        if (CLIENT_LOADS_THIS_TICK.getAndIncrement() >= clientCap) {
            if (cfg.logOptimizationEvents) {
                VacuumMod.LOGGER.debug("[Vacuum] Client chunk load deferred ({},{}) — cap {} reached", x, z, clientCap);
            }
            ci.cancel();
        }
    }
}
