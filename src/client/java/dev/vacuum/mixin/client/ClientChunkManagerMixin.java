package dev.vacuum.mixin.client;

import dev.vacuum.VacuumMod;
import dev.vacuum.config.VacuumConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.world.ClientChunkManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.concurrent.atomic.AtomicInteger;

@Environment(EnvType.CLIENT)
@Mixin(ClientChunkManager.class)
public class ClientChunkManagerMixin {
    private static final AtomicInteger CLIENT_LOADS_THIS_TICK = new AtomicInteger(0);
    private static long lastReset = 0L;

    // require=0: loadChunkFromPacket signature varies by version
    @Inject(method = "loadChunkFromPacket", at = @At("HEAD"), cancellable = true, require = 0)
    private void vacuum$capClientChunkLoad(CallbackInfo ci) {
        VacuumConfig cfg = VacuumMod.getConfig();
        if (!cfg.dynamicChunkCap) return;
        long now = System.currentTimeMillis();
        if (now - lastReset > 50L) { CLIENT_LOADS_THIS_TICK.set(0); lastReset = now; }
        int clientCap = cfg.chunkLoadingCap * 2;
        if (CLIENT_LOADS_THIS_TICK.getAndIncrement() >= clientCap) {
            if (cfg.logOptimizationEvents) VacuumMod.LOGGER.debug("[Vacuum] Client chunk load deferred — cap {} reached", clientCap);
            ci.cancel();
        }
    }
}
