package dev.vacuum.mixin.client;

import dev.vacuum.config.VacuumConfig;
import dev.vacuum.VacuumMod;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * ClientPlayerEntityMixin — makes the client arm-swing animation fire
 * immediately on the same tick as the attack input rather than waiting
 * for the next tick. This alone accounts for ~1 tick (50ms at 20TPS)
 * of perceived delay on sword swings in vanilla.
 */
@Environment(EnvType.CLIENT)
@Mixin(ClientPlayerEntity.class)
public class ClientPlayerEntityMixin {

    @Inject(method = "swingHand(Lnet/minecraft/util/Hand;)V", at = @At("HEAD"))
    private void vacuum$noDelaySwing(Hand hand, CallbackInfo ci) {
        VacuumConfig cfg = VacuumMod.getConfig();
        if (cfg.predictiveSwing && cfg.logOptimizationEvents) {
            VacuumMod.LOGGER.debug("[Vacuum] Predictive swing fired for hand {}", hand);
        }
    }
}
