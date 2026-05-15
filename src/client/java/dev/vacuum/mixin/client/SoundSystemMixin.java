package dev.vacuum.mixin.client;

import dev.vacuum.VacuumMod;
import dev.vacuum.config.VacuumConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.sound.SoundSystem;
import net.minecraft.sound.SoundCategory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * SoundSystemMixin — limits simultaneous sounds and culls distant ones.
 *
 * LWJGL / OpenAL has a fixed hardware channel limit (~256 on most systems).
 * Exceeding it causes sounds to be silently dropped or, worse, causes latency
 * spikes. Vacuum caps the count and skips sounds beyond cfg.soundCullDistance.
 */
@Environment(EnvType.CLIENT)
@Mixin(SoundSystem.class)
public class SoundSystemMixin {

    private static final AtomicInteger ACTIVE_SOUNDS = new AtomicInteger(0);

    @Inject(method = "play(Lnet/minecraft/client/sound/SoundInstance;)V", at = @At("HEAD"), cancellable = true)
    private void vacuum$capSounds(net.minecraft.client.sound.SoundInstance sound, CallbackInfo ci) {
        VacuumConfig cfg = VacuumMod.getConfig();

        if (ACTIVE_SOUNDS.get() >= cfg.maxSimultaneousSounds) {
            if (cfg.logOptimizationEvents) {
                VacuumMod.LOGGER.debug("[Vacuum] Sound capped — {} active (max {})",
                        ACTIVE_SOUNDS.get(), cfg.maxSimultaneousSounds);
            }
            ci.cancel();
            return;
        }

        if (cfg.cullDistantSounds) {
            net.minecraft.client.MinecraftClient client = net.minecraft.client.MinecraftClient.getInstance();
            if (client.player != null) {
                double dx = sound.getX() - client.player.getX();
                double dy = sound.getY() - client.player.getY();
                double dz = sound.getZ() - client.player.getZ();
                double distSq = dx * dx + dy * dy + dz * dz;
                double cullDistSq = (double) cfg.soundCullDistance * cfg.soundCullDistance;
                if (distSq > cullDistSq && sound.getCategory() != SoundCategory.MUSIC
                        && sound.getCategory() != SoundCategory.RECORDS) {
                    ci.cancel();
                }
            }
        }

        ACTIVE_SOUNDS.incrementAndGet();
    }

    @Inject(method = "stop(Lnet/minecraft/client/sound/SoundInstance;)V", at = @At("RETURN"))
    private void vacuum$decrementCount(net.minecraft.client.sound.SoundInstance sound, CallbackInfo ci) {
        ACTIVE_SOUNDS.updateAndGet(v -> Math.max(0, v - 1));
    }
}
