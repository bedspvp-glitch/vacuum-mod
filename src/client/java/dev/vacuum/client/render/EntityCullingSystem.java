package dev.vacuum.client.render;

import dev.vacuum.VacuumMod;
import dev.vacuum.config.VacuumConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Vacuum EntityCullingSystem
 *
 * Tracks which entities are visible to the camera frustum and within the
 * configured tracking distance cap. Entities that fail both checks are
 * skipped by the render mixin, saving draw calls without affecting gameplay.
 *
 * The frustum reference is updated every frame by WorldRendererMixin.
 */
@Environment(EnvType.CLIENT)
public class EntityCullingSystem {

    private final AtomicInteger culledThisFrame = new AtomicInteger(0);
    private final AtomicInteger renderedThisFrame = new AtomicInteger(0);

    private VacuumFrustum currentFrustum;
    private volatile int entityCount = 0;

    public void updateFrustum(VacuumFrustum frustum) {
        this.currentFrustum = frustum;
    }

    public void beginFrame() {
        culledThisFrame.set(0);
        renderedThisFrame.set(0);
    }

    /**
     * Returns true if the entity should be rendered.
     * Returns false if it should be skipped (culled).
     */
    public boolean shouldRenderEntity(Entity entity) {
        VacuumConfig cfg = VacuumMod.getConfig();

        if (!cfg.entityCulling) return true;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return true;

        // Never cull the player entity or the camera entity
        if (entity == client.player || entity == client.getCameraEntity()) return true;

        // Distance-based entity cap: beyond throttle distance, only render every other tick
        if (cfg.entityUpdateThrottleDistance > 0) {
            Vec3d playerPos = client.player.getPos();
            double distSq = entity.squaredDistanceTo(playerPos.x, playerPos.y, playerPos.z);
            double capDistSq = (double) cfg.entityUpdateThrottleDistance * cfg.entityUpdateThrottleDistance;
            if (distSq > capDistSq) {
                // Throttle rendering for distant entities using entity ID parity
                boolean skipThisTick = (entity.getId() & 1) == (client.world != null
                        ? (int)(client.world.getTime() & 1) : 0);
                if (skipThisTick) {
                    culledThisFrame.incrementAndGet();
                    return false;
                }
            }
        }

        // Frustum culling
        if (cfg.frustumCulling && currentFrustum != null) {
            Box bb = entity.getVisibilityBoundingBox();
            if (!currentFrustum.isVisible(bb)) {
                culledThisFrame.incrementAndGet();
                return false;
            }
        }

        renderedThisFrame.incrementAndGet();
        return true;
    }

    public void setEntityCount(int count) {
        this.entityCount = count;
    }

    public int getEntityCount() { return entityCount; }
    public int getCulledThisFrame() { return culledThisFrame.get(); }
    public int getRenderedThisFrame() { return renderedThisFrame.get(); }
}
