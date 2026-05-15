package dev.vacuum.mixin.client;

import dev.vacuum.VacuumMod;
import dev.vacuum.config.VacuumConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.chunk.ChunkBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * ChunkBuilderMixin — overrides the chunk mesh builder thread count with the
 * value from VacuumConfig, letting users tune it to their CPU.
 *
 * Vanilla computes this from available processors but uses a conservative
 * formula. Vacuum lets power users push it higher for faster chunk pop-in.
 */
@Environment(EnvType.CLIENT)
@Mixin(ChunkBuilder.class)
public class ChunkBuilderMixin {

    @ModifyArg(
        method = "<init>",
        at = @At(
            value = "INVOKE",
            target = "Ljava/util/concurrent/Executors;newFixedThreadPool(ILjava/util/concurrent/ThreadFactory;)Ljava/util/concurrent/ExecutorService;"
        ),
        index = 0
    )
    private int vacuum$overrideThreadCount(int original) {
        VacuumConfig cfg = VacuumMod.getConfig();
        int configured = cfg.chunkBuilderThreads;
        if (configured > 0 && configured != original) {
            VacuumMod.LOGGER.info("[Vacuum] Chunk builder threads: {} (vanilla: {})", configured, original);
            return configured;
        }
        return original;
    }
}
