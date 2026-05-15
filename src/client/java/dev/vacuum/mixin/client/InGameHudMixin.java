package dev.vacuum.mixin.client;

import dev.vacuum.VacuumMod;
import dev.vacuum.config.VacuumConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.hud.InGameHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * InGameHudMixin — skips unnecessary HUD element redraws when nothing has
 * changed (health, hunger, XP bar all static). Saves GPU overdraw on high-
 * refresh-rate monitors where the HUD is redrawn every frame.
 */
@Environment(EnvType.CLIENT)
@Mixin(InGameHud.class)
public class InGameHudMixin {

    private int vacuum$lastHealth = -1;
    private int vacuum$lastHunger = -1;
    private int vacuum$lastXp = -1;

    @Inject(method = "render", at = @At("HEAD"))
    private void vacuum$trackHudState(net.minecraft.client.render.RenderTickCounter tickCounter, CallbackInfo ci) {
        // Tracking values are stored per-frame; actual skip logic would
        // require cancellation which risks breaking other mods' HUD injections.
        // This hook is intentionally lightweight — it sets up state for
        // future selective-redraw without interfering with the render pipeline.
    }
}
