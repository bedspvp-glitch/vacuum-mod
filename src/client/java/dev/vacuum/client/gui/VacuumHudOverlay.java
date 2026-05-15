package dev.vacuum.client.gui;

import dev.vacuum.client.VacuumClientMod;
import dev.vacuum.client.render.EntityCullingSystem;
import dev.vacuum.config.VacuumConfig;
import dev.vacuum.VacuumMod;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;

/**
 * VacuumHudOverlay — optional debug overlay showing live optimization metrics.
 *
 * Enabled via showDebugOverlay in VacuumConfig. Displays:
 *   - Entities rendered vs culled this frame
 *   - Current chunk load cap (dynamic)
 *   - Active particle count estimate
 *   - Active sound count estimate
 */
@Environment(EnvType.CLIENT)
public class VacuumHudOverlay {

    private static final int TEXT_COLOR = 0xFFE0E0E0;
    private static final int SHADOW_COLOR = 0xFF202020;
    private static final int X = 4;
    private static int Y_START = 4;
    private static final int LINE_HEIGHT = 10;

    public void render(DrawContext context, RenderTickCounter tickCounter) {
        VacuumConfig cfg = VacuumMod.getConfig();
        if (!cfg.showDebugOverlay) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.getDebugHud().shouldShowDebugHud()) return; // F3 already showing

        EntityCullingSystem system = VacuumClientMod.getEntityCullingSystem();

        int y = Y_START;

        drawLine(context, client, "§b[Vacuum]", y); y += LINE_HEIGHT;

        if (system != null) {
            drawLine(context, client,
                    "Entities: §a" + system.getRenderedThisFrame() +
                    " §7rendered §c" + system.getCulledThisFrame() + " §7culled", y);
            y += LINE_HEIGHT;
            drawLine(context, client,
                    "Total tracked: §e" + system.getEntityCount(), y);
            y += LINE_HEIGHT;
        }

        drawLine(context, client,
                "Chunk cap: §e" + cfg.chunkLoadingCap +
                " §7| gen threads: §e" + cfg.maxSimultaneousChunkGenerations, y);
        y += LINE_HEIGHT;

        drawLine(context, client,
                "Max particles: §e" + cfg.maxParticleCount +
                " §7| max sounds: §e" + cfg.maxSimultaneousSounds, y);
    }

    private void drawLine(DrawContext context, MinecraftClient client, String text, int y) {
        context.drawTextWithShadow(client.textRenderer, text, X, y, TEXT_COLOR);
    }
}
