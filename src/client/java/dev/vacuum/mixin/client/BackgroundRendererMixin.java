package dev.vacuum.mixin.client;

import dev.vacuum.config.VacuumConfig;
import dev.vacuum.VacuumMod;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.BackgroundRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(BackgroundRenderer.class)
public class BackgroundRendererMixin {
    // require=0: applyFog signature varies across MC versions; skip gracefully if not found
    @Inject(method = "applyFog", at = @At("RETURN"), require = 0)
    private static void vacuum$reduceFog(CallbackInfo ci) {
        VacuumConfig cfg = VacuumMod.getConfig();
        if (!cfg.reduceOpaqueFogDensity) return;
        // Future: manipulate RenderSystem fog uniforms once confirmed safe with Iris/Sodium
    }
}
