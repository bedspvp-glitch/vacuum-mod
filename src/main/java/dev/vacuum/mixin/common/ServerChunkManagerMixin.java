package dev.vacuum.mixin.common;

import dev.vacuum.VacuumMod;
import dev.vacuum.config.VacuumConfig;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.util.math.ChunkPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * ServerChunkManagerMixin — caps simultaneous chunk loads per tick on the server.
 *
 * Sodium and Starlight implement similar ideas on the client; this extends the
 * concept to the server tick loop so chunk generation never monopolises the CPU
 * and causes TPS drops.
 */
@Mixin(ServerChunkManager.class)
public class ServerChunkManagerMixin {

    private static final AtomicInteger LOADS_THIS_TICK = new AtomicInteger(0);
    private static long lastTickTime = 0L;

    @Inject(
        method = "getChunk(IILnet/minecraft/world/chunk/ChunkStatus;Z)Lnet/minecraft/world/chunk/Chunk;",
        at = @At("HEAD"),
        cancellable = true
    )
    private void vacuum$capChunkLoad(int x, int z, net.minecraft.world.chunk.ChunkStatus status, boolean create,
                                     CallbackInfoReturnable<?> cir) {
        VacuumConfig cfg = VacuumMod.getConfig();
        if (!cfg.dynamicChunkCap) return;

        long now = System.currentTimeMillis();
        if (now - lastTickTime > 50L) {
            // New tick window — reset counter
            LOADS_THIS_TICK.set(0);
            lastTickTime = now;
        }

        int cap = cfg.chunkLoadingCap;
        if (LOADS_THIS_TICK.getAndIncrement() >= cap) {
            // Defer this chunk load to the next tick window
            if (cfg.logOptimizationEvents) {
                VacuumMod.LOGGER.debug("[Vacuum] Chunk load deferred ({},{}) — cap {} reached", x, z, cap);
            }
            cir.setReturnValue(null);
        }
    }
}
