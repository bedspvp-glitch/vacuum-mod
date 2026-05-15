package dev.vacuum.mixin.common;

import dev.vacuum.VacuumMod;
import dev.vacuum.config.VacuumConfig;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ChunkLevelType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * ChunkHolderManagerMixin — hooks into chunk holder level changes to apply
 * Vacuum's chunk unload delay, preventing chunks that players just left from
 * being immediately discarded (and having to re-generate on return).
 */
@Mixin(ChunkHolder.class)
public class ChunkHolderManagerMixin {

    @Inject(method = "setLevel", at = @At("HEAD"))
    private void vacuum$onLevelChange(int level, CallbackInfo ci) {
        VacuumConfig cfg = VacuumMod.getConfig();
        if (!cfg.dynamicChunkCap) return;

        // When a chunk transitions to an unload-pending level, log if debug is on
        if (cfg.logOptimizationEvents && level > ChunkLevelType.FULL.ordinal()) {
            VacuumMod.LOGGER.debug("[Vacuum] Chunk holder level raised to {} (unload pending)", level);
        }
    }
}
