package dev.vacuum.mixin.client;

import dev.vacuum.client.VacuumClientMod;
import dev.vacuum.client.render.EntityCullingSystem;
import dev.vacuum.config.VacuumConfig;
import dev.vacuum.VacuumMod;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * EntityRenderDispatcherMixin — gates entity rendering behind the Vacuum
 * EntityCullingSystem. Entities that are outside the frustum or beyond the
 * throttle distance are skipped entirely, saving vertex uploads and draw calls.
 */
@Environment(EnvType.CLIENT)
@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin {

    @Inject(
        method = "render",
        at = @At("HEAD"),
        cancellable = true
    )
    private <E extends Entity> void vacuum$cullEntity(
            E entity, double x, double y, double z, float yaw, float tickDelta,
            MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light,
            CallbackInfo ci) {

        VacuumConfig cfg = VacuumMod.getConfig();
        if (!cfg.entityCulling && !cfg.frustumCulling) return;

        EntityCullingSystem system = VacuumClientMod.getEntityCullingSystem();
        if (system == null) return;

        if (!system.shouldRenderEntity(entity)) {
            ci.cancel();
        }
    }
}
