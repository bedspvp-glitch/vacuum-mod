package dev.vacuum.client.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.math.Box;

/**
 * Lightweight frustum implementation used by EntityCullingSystem.
 * Populated each frame from the camera's projection + view matrices via WorldRendererMixin.
 */
@Environment(EnvType.CLIENT)
public class VacuumFrustum {

    // 6 planes × 4 coefficients (a, b, c, d)
    private final float[] planes = new float[24];

    /**
     * Extract frustum planes from a combined projection-view matrix (column-major, OpenGL order).
     * The matrix array should be a float[16].
     */
    public void setFromMatrix(float[] m) {
        // Left
        planes[0]  = m[3]  + m[0];
        planes[1]  = m[7]  + m[4];
        planes[2]  = m[11] + m[8];
        planes[3]  = m[15] + m[12];
        // Right
        planes[4]  = m[3]  - m[0];
        planes[5]  = m[7]  - m[4];
        planes[6]  = m[11] - m[8];
        planes[7]  = m[15] - m[12];
        // Bottom
        planes[8]  = m[3]  + m[1];
        planes[9]  = m[7]  + m[5];
        planes[10] = m[11] + m[9];
        planes[11] = m[15] + m[13];
        // Top
        planes[12] = m[3]  - m[1];
        planes[13] = m[7]  - m[5];
        planes[14] = m[11] - m[9];
        planes[15] = m[15] - m[13];
        // Near
        planes[16] = m[3]  + m[2];
        planes[17] = m[7]  + m[6];
        planes[18] = m[11] + m[10];
        planes[19] = m[15] + m[14];
        // Far
        planes[20] = m[3]  - m[2];
        planes[21] = m[7]  - m[6];
        planes[22] = m[11] - m[10];
        planes[23] = m[15] - m[14];

        normalizePlanes();
    }

    private void normalizePlanes() {
        for (int i = 0; i < 6; i++) {
            int base = i * 4;
            float len = (float) Math.sqrt(
                    planes[base] * planes[base] +
                    planes[base + 1] * planes[base + 1] +
                    planes[base + 2] * planes[base + 2]);
            if (len > 0f) {
                planes[base]     /= len;
                planes[base + 1] /= len;
                planes[base + 2] /= len;
                planes[base + 3] /= len;
            }
        }
    }

    /**
     * Returns true if the given axis-aligned bounding box is (at least partially) visible.
     */
    public boolean isVisible(Box box) {
        float minX = (float) box.minX;
        float minY = (float) box.minY;
        float minZ = (float) box.minZ;
        float maxX = (float) box.maxX;
        float maxY = (float) box.maxY;
        float maxZ = (float) box.maxZ;

        for (int i = 0; i < 6; i++) {
            int b = i * 4;
            float a = planes[b], bv = planes[b + 1], c = planes[b + 2], d = planes[b + 3];
            // Pick the positive vertex (farthest along the plane normal)
            float px = a > 0 ? maxX : minX;
            float py = bv > 0 ? maxY : minY;
            float pz = c > 0 ? maxZ : minZ;
            if (a * px + bv * py + c * pz + d < 0) {
                return false;
            }
        }
        return true;
    }
}
