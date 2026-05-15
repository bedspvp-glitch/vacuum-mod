package dev.vacuum.mixin.client;

import dev.vacuum.client.latency.ClientHitPredictor;
import dev.vacuum.config.VacuumConfig;
import dev.vacuum.VacuumMod;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * ClientInteractionManagerMixin — injects into the attack method to trigger
 * ClientHitPredictor immediately before the packet is sent to the server.
 *
 * This gives the player instant visual + audio feedback on sword hits, reducing
 * perceived latency from ~2ms (round-trip + processing) to effectively 0ms.
 */
@Environment(EnvType.CLIENT)
@Mixin(ClientPlayerInteractionManager.class)
public class ClientInteractionManagerMixin {

    @Inject(
        method = "attackEntity",
        at = @At("HEAD")
    )
    private void vacuum$onAttack(PlayerEntity player, Entity target, CallbackInfo ci) {
        VacuumConfig cfg = VacuumMod.getConfig();
        if (!cfg.clientSideHitRegistration) return;
        ClientHitPredictor.getInstance().onClientAttack(target);
    }

    @Inject(
        method = "attackBlock",
        at = @At("HEAD")
    )
    private void vacuum$onAttackBlock(net.minecraft.util.math.BlockPos pos,
                                      net.minecraft.util.math.Direction direction, CallbackInfo ci) {
        VacuumConfig cfg = VacuumMod.getConfig();
        if (!cfg.instantBlockInteraction) return;
        ClientHitPredictor.getInstance().onClientBlockInteraction();
    }
}
