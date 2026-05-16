package dev.vacuum.mixin.client;

import dev.vacuum.VacuumMod;
import dev.vacuum.config.VacuumConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.concurrent.atomic.AtomicInteger;

@Environment(EnvType.CLIENT)
@Mixin(ParticleManager.class)
public abstract class ParticleManagerMixin {

    // Track particle count ourselves since ParticleManager.getCount() was removed in 1.21.4
    private static final AtomicInteger VACUUM_PARTICLE_COUNT = new AtomicInteger(0);

    @Inject(method = "addParticle(Lnet/minecraft/client/particle/Particle;)V", at = @At("HEAD"), cancellable = true)
    private void vacuum$capParticles(Particle particle, CallbackInfo ci) {
        VacuumConfig cfg = VacuumMod.getConfig();
        if (!cfg.smartParticles) return;

        int total = VACUUM_PARTICLE_COUNT.get();
        if (total >= cfg.maxParticleCount) {
            if (cfg.logOptimizationEvents) {
                VacuumMod.LOGGER.debug("[Vacuum] Particle capped {} active (max {})", total, cfg.maxParticleCount);
            }
            ci.cancel();
            return;
        }
        VACUUM_PARTICLE_COUNT.incrementAndGet();
    }

    @Inject(method = "clearParticles", at = @At("HEAD"))
    private void vacuum$resetCount(CallbackInfo ci) {
        VACUUM_PARTICLE_COUNT.set(0);
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void vacuum$decayCount(CallbackInfo ci) {
        // Decay count each tick to account for particles that expired naturally
        VACUUM_PARTICLE_COUNT.updateAndGet(v -> Math.max(0, v - 10));
    }
}
