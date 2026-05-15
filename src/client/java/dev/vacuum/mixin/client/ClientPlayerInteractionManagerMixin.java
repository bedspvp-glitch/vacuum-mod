package dev.vacuum.mixin.client;

import dev.vacuum.client.latency.ClientHitPredictor;
import dev.vacuum.config.VacuumConfig;
import dev.vacuum.VacuumMod;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * ClientPlayerInteractionManagerMixin — ensures block interaction packets
 * are sent immediately without waiting for client-side tick alignment,
 * cutting the block-break start delay.
 */
@Environment(EnvType.CLIENT)
@Mixin(ClientPlayerInteractionManager.class)
public class ClientPlayerInteractionManagerMixin {

    @Inject(method = "updateBlockBreakingProgress", at = @At("HEAD"))
    private void vacuum$immediateBreak(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        VacuumConfig cfg = VacuumMod.getConfig();
        if (cfg.instantBlockInteraction) {
            ClientHitPredictor.getInstance().onClientBlockInteraction();
        }
    }
}
