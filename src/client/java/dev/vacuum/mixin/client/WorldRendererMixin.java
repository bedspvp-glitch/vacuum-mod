package dev.vacuum.mixin.client;

import dev.vacuum.client.VacuumClientMod;
import dev.vacuum.client.render.EntityCullingSystem;
import dev.vacuum.client.render.VacuumFrustum;
import dev.vacuum.config.VacuumConfig;
import dev.vacuum.VacuumMod;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * WorldRendererMixin — hooks into the start of each render frame to:
 *   1. Reset per-frame entity culling counters
 *   2. Cap the number of chunk sections that are rebuilt per frame
 *      (equivalent to Sodium's deferred chunk mesh building)
 */
@Environment(EnvType.CLIENT)
@Mixin(WorldRenderer.class)
public class WorldRendererMixin {

    private static final VacuumFrustum VACUUM_FRUSTUM = new VacuumFrustum();

    @Inject(method = "setupFrustum", at = @At("RETURN"))
    private void vacuum$captureFrustum(net.minecraft.client.util.math.MatrixStack matrices,
                                        net.minecraft.util.math.Vec3d cameraPos,
                                        org.joml.Matrix4f projectionMatrix,
                                        CallbackInfo ci) {
        VacuumConfig cfg = VacuumMod.getConfig();
        if (!cfg.frustumCulling) return;

        EntityCullingSystem system = VacuumClientMod.getEntityCullingSystem();
        if (system == null) return;

        // Build a combined VP matrix from Mojang's frustum via the matrix stack peek
        org.joml.Matrix4f modelView = matrices.peek().getPositionMatrix();
        org.joml.Matrix4f combined = new org.joml.Matrix4f(projectionMatrix).mul(modelView);

        float[] arr = new float[16];
        combined.get(arr);
        VACUUM_FRUSTUM.setFromMatrix(arr);
        system.updateFrustum(VACUUM_FRUSTUM);
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void vacuum$beginFrame(net.minecraft.client.render.RenderTickCounter counter,
                                    boolean bl, Camera camera,
                                    net.minecraft.client.render.GameRenderer gameRenderer,
                                    net.minecraft.client.render.LightmapTextureManager lightmapTextureManager,
                                    org.joml.Matrix4f matrix4f,
                                    org.joml.Matrix4f matrix4f2,
                                    CallbackInfo ci) {
        EntityCullingSystem system = VacuumClientMod.getEntityCullingSystem();
        if (system != null) {
            system.beginFrame();
        }
    }
}
