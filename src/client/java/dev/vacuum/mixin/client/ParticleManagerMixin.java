package dev.vacuum.mixin.client;

import dev.vacuum.VacuumMod;
import dev.vacuum.config.VacuumConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Queue;

/**
 * ParticleManagerMixin — limits the total number of active particles.
 *
 * Vanilla has a configurable particle count but applies it loosely. This mixin
 * hard-caps particle creation once cfg.maxParticleCount is reached, preventing
 * mob farms or explosion chains from tanking FPS through particle overflow.
 */
@Environment(EnvType.CLIENT)
@Mixin(ParticleManager.class)
public abstract class ParticleManagerMixin {

    @Shadow
    private Queue<Particle>[] particles;

    @Inject(method = "addParticle(Lnet/minecraft/client/particle/Particle;)V", at = @At("HEAD"), cancellable = true)
    private void vacuum$capParticles(Particle particle, CallbackInfo ci) {
        VacuumConfig cfg = VacuumMod.getConfig();
        if (!cfg.smartParticles) return;

        int total = 0;
        if (particles != null) {
            for (Queue<Particle> queue : particles) {
                if (queue != null) total += queue.size();
            }
        }

        if (total >= cfg.maxParticleCount) {
            if (cfg.logOptimizationEvents) {
                VacuumMod.LOGGER.debug("[Vacuum] Particle capped — {} active (max {})", total, cfg.maxParticleCount);
            }
            ci.cancel();
        }
    }
}
