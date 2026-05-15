package dev.vacuum.client.gui;

import dev.vacuum.VacuumMod;
import dev.vacuum.config.VacuumConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

/**
 * VacuumOptionsScreen — the full in-game settings screen for Vacuum.
 *
 * Opened via Mod Menu (preferred) or the Minecraft Options > Mods button.
 * Organised into four sections:
 *   1. Chunk Optimizations
 *   2. Entity Optimizations
 *   3. Client Latency
 *   4. Render & Audio
 */
@Environment(EnvType.CLIENT)
public class VacuumOptionsScreen extends Screen {

    private final Screen parent;
    private final VacuumConfig config;

    private static final int BUTTON_WIDTH  = 200;
    private static final int BUTTON_HEIGHT = 20;
    private static final int PADDING       = 4;
    private static final int COL_LEFT      = 10;
    private static final int COL_RIGHT     = 220;

    public VacuumOptionsScreen(Screen parent) {
        super(Text.literal("Vacuum Settings"));
        this.parent = parent;
        this.config = VacuumMod.getConfig();
    }

    @Override
    protected void init() {
        int y = 30;

        // ── Section: Chunk Optimizations ──────────────────────────────────────
        addSectionLabel("Chunk Optimizations", COL_LEFT, y); y += 14;

        addToggle("Dynamic Chunk Cap", config.dynamicChunkCap,
                v -> config.dynamicChunkCap = v, COL_LEFT, y);
        addIntSlider("Chunk Load Cap", config.chunkLoadingCap, 1, 16,
                v -> config.chunkLoadingCap = v, COL_RIGHT, y); y += BUTTON_HEIGHT + PADDING;

        addIntSlider("Max Gen Threads", config.maxSimultaneousChunkGenerations, 1, 8,
                v -> config.maxSimultaneousChunkGenerations = v, COL_LEFT, y); y += BUTTON_HEIGHT + PADDING;

        addToggle("Deferred Chunk Mesh", config.deferredChunkMeshBuilding,
                v -> config.deferredChunkMeshBuilding = v, COL_LEFT, y);
        addIntSlider("Chunk Builder Threads", config.chunkBuilderThreads, 1,
                Runtime.getRuntime().availableProcessors(),
                v -> config.chunkBuilderThreads = v, COL_RIGHT, y); y += BUTTON_HEIGHT + PADDING;

        // ── Section: Entity Optimizations ────────────────────────────────────
        y += 6;
        addSectionLabel("Entity Optimizations", COL_LEFT, y); y += 14;

        addToggle("Entity Culling", config.entityCulling,
                v -> config.entityCulling = v, COL_LEFT, y);
        addToggle("Frustum Culling", config.frustumCulling,
                v -> config.frustumCulling = v, COL_RIGHT, y); y += BUTTON_HEIGHT + PADDING;

        addIntSlider("Entity Tracking Cap", config.entityTrackingCap, 64, 2048,
                v -> config.entityTrackingCap = v, COL_LEFT, y);
        addIntSlider("Throttle Distance", config.entityUpdateThrottleDistance, 0, 128,
                v -> config.entityUpdateThrottleDistance = v, COL_RIGHT, y); y += BUTTON_HEIGHT + PADDING;

        addToggle("Skip Invisible TileEntities", config.skipInvisibleTileEntityRender,
                v -> config.skipInvisibleTileEntityRender = v, COL_LEFT, y); y += BUTTON_HEIGHT + PADDING;

        // ── Section: Client Latency ───────────────────────────────────────────
        y += 6;
        addSectionLabel("Client Latency", COL_LEFT, y); y += 14;

        addToggle("Client-Side Hit Registration", config.clientSideHitRegistration,
                v -> config.clientSideHitRegistration = v, COL_LEFT, y);
        addToggle("Predictive Swing", config.predictiveSwing,
                v -> config.predictiveSwing = v, COL_RIGHT, y); y += BUTTON_HEIGHT + PADDING;

        addToggle("Instant Block Interaction", config.instantBlockInteraction,
                v -> config.instantBlockInteraction = v, COL_LEFT, y);
        addToggle("Predictive Damage Tint", config.predictiveDamage,
                v -> config.predictiveDamage = v, COL_RIGHT, y); y += BUTTON_HEIGHT + PADDING;

        // ── Section: Render & Audio ───────────────────────────────────────────
        y += 6;
        addSectionLabel("Render & Audio", COL_LEFT, y); y += 14;

        addToggle("Smart Particles", config.smartParticles,
                v -> config.smartParticles = v, COL_LEFT, y);
        addIntSlider("Max Particles", config.maxParticleCount, 256, 16384,
                v -> config.maxParticleCount = v, COL_RIGHT, y); y += BUTTON_HEIGHT + PADDING;

        addToggle("Cull Distant Sounds", config.cullDistantSounds,
                v -> config.cullDistantSounds = v, COL_LEFT, y);
        addIntSlider("Sound Cull Distance", config.soundCullDistance, 16, 256,
                v -> config.soundCullDistance = v, COL_RIGHT, y); y += BUTTON_HEIGHT + PADDING;

        addIntSlider("Max Simultaneous Sounds", config.maxSimultaneousSounds, 16, 256,
                v -> config.maxSimultaneousSounds = v, COL_LEFT, y); y += BUTTON_HEIGHT + PADDING;

        addToggle("Reduce Fog Density", config.reduceOpaqueFogDensity,
                v -> config.reduceOpaqueFogDensity = v, COL_LEFT, y);
        addToggle("Reduced Sky Rendering", config.reducedSkyRendering,
                v -> config.reducedSkyRendering = v, COL_RIGHT, y); y += BUTTON_HEIGHT + PADDING;

        // ── Section: Debug ────────────────────────────────────────────────────
        y += 6;
        addSectionLabel("Debug", COL_LEFT, y); y += 14;

        addToggle("Show HUD Overlay", config.showDebugOverlay,
                v -> config.showDebugOverlay = v, COL_LEFT, y);
        addToggle("Log Optimization Events", config.logOptimizationEvents,
                v -> config.logOptimizationEvents = v, COL_RIGHT, y); y += BUTTON_HEIGHT + PADDING;

        // ── Done button ───────────────────────────────────────────────────────
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Done"), btn -> {
            VacuumConfig.save(config);
            if (this.client != null) this.client.setScreen(parent);
        }).dimensions(this.width / 2 - 100, this.height - 28, 200, 20).build());
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private void addSectionLabel(String label, int x, int y) {
        // Labels are rendered in render() using the stored list
        // For now we use a disabled button as a visual separator
        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("§l" + label), btn -> {}
        ).dimensions(x, y, BUTTON_WIDTH * 2 + 10, 12).build()).active = false;
    }

    private void addToggle(String label, boolean initial, java.util.function.Consumer<Boolean> setter,
                            int x, int y) {
        this.addDrawableChild(CyclingButtonWidget.onOffBuilder(initial)
                .build(x, y, BUTTON_WIDTH, BUTTON_HEIGHT, Text.literal(label), (btn, val) -> setter.accept(val)));
    }

    private void addIntSlider(String label, int initial, int min, int max,
                               java.util.function.Consumer<Integer> setter, int x, int y) {
        double progress = max == min ? 0.0 : (double)(initial - min) / (double)(max - min);
        this.addDrawableChild(new SliderWidget(x, y, BUTTON_WIDTH, BUTTON_HEIGHT,
                Text.literal(label + ": " + initial), progress) {
            int value = initial;
            @Override
            protected void updateMessage() {
                setMessage(Text.literal(label + ": " + value));
            }
            @Override
            protected void applyValue() {
                value = (int) Math.round(min + this.value * (max - min));
                setter.accept(value);
            }
        });
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 10, 0xFFFFFF);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void close() {
        VacuumConfig.save(config);
        if (this.client != null) this.client.setScreen(parent);
    }
}
