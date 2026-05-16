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

@Environment(EnvType.CLIENT)
public class EntityCullingSystem {

    private final AtomicInteger culledThisFrame = new AtomicInteger(0);
    private final AtomicInteger renderedThisFrame = new AtomicInteger(0);
    private volatile VacuumFrustum currentFrustum;
    private volatile int entityCount = 0;

    /** Called by WorldRendererMixin each frame with the updated frustum. */
    public void updateFrustum(VacuumFrustum frustum) {
        this.currentFrustum = frustum;
    }

    public void beginFrame() {
        culledThisFrame.set(0);
        renderedThisFrame.set(0);
    }

    /** Called by EntityRenderDispatcherMixin -- returns true if the entity should be rendered. */
    public boolean shouldRenderEntity(Entity entity) {
        VacuumConfig config = VacuumMod.getConfig();
        if (!config.entityCulling && !config.frustumCulling) {
            return true;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            return true;
        }

        // Always render the local player
        if (entity == client.player) {
            return true;
        }

        // Distance culling (only when entityCulling enabled)
        if (config.entityCulling) {
            Vec3d cameraPos = client.gameRenderer.getCamera().getPos();
            double distSq = entity.squaredDistanceTo(cameraPos.x, cameraPos.y, cameraPos.z);
            double maxDist = 512.0; // hard cap
            if (distSq > maxDist * maxDist) {
                culledThisFrame.incrementAndGet();
                return false;
            }
        }

        // Frustum culling (only when frustumCulling enabled)
        if (config.frustumCulling && currentFrustum != null) {
            Box box = entity.getBoundingBox().expand(0.5);
            if (!currentFrustum.isVisible(box)) {
                culledThisFrame.incrementAndGet();
                return false;
            }
        }

        renderedThisFrame.incrementAndGet();
        return true;
    }

    public void endFrame() {
        entityCount = renderedThisFrame.get() + culledThisFrame.get();
    }

    public int getCulledThisFrame()   { return culledThisFrame.get(); }
    public int getRenderedThisFrame() { return renderedThisFrame.get(); }
    public int getEntityCount()       { return entityCount; }
}
