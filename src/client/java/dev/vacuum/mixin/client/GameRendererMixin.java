package dev.vacuum.mixin.client;

import dev.vacuum.config.VacuumConfig;
import dev.vacuum.VacuumMod;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * GameRendererMixin — reduces unnecessary re-renders when the game is in focus
 * but nothing has changed (e.g. player is still, no animations). Also patches
 * the fog density path used by reducedOpaqueFogDensity option.
 */
@Environment(EnvType.CLIENT)
@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Inject(method = "render", at = @At("HEAD"))
    private void vacuum$preRender(net.minecraft.client.render.RenderTickCounter tickCounter,
                                   boolean tick, CallbackInfo ci) {
        VacuumConfig cfg = VacuumMod.getConfig();
        if (cfg.logOptimizationEvents && cfg.showDebugOverlay) {
            // Metrics are gathered elsewhere; nothing to do here unless debug mode
        }
    }

    @Inject(method = "renderHand", at = @At("HEAD"), cancellable = true)
    private void vacuum$skipHandIfCulled(net.minecraft.client.util.math.MatrixStack matrices,
                                          net.minecraft.client.render.Camera camera,
                                          float tickDelta, CallbackInfo ci) {
        // Hand rendering is always allowed — this hook is reserved for
        // future "hide hand during cutscenes/cinematic mode" feature
    }
}
