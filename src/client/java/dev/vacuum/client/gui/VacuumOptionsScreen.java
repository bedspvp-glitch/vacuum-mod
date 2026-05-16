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

@Environment(EnvType.CLIENT)
public class VacuumOptionsScreen extends Screen {

    private final Screen parent;
    private VacuumConfig config;

    private static final int BW  = 200;
    private static final int BH  = 20;
    private static final int PAD = 4;
    private static final int COL_L = 10;
    private static final int COL_R = 220;

    public VacuumOptionsScreen(Screen parent) {
        super(Text.literal("Vacuum Settings"));
        this.parent = parent;
        this.config = VacuumMod.getConfig();
    }

    @Override
    protected void init() {
        int y = 28;

        // ── Presets ───────────────────────────────────────────────────────────
        addSectionLabel("Presets", COL_L, y); y += 14;
        int pw = 130;
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Potato"),
            btn -> applyPreset(VacuumConfig.potato())).dimensions(COL_L, y, pw, BH).build());
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Balanced"),
            btn -> applyPreset(VacuumConfig.balanced())).dimensions(COL_L + pw + 4, y, pw, BH).build());
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Ultra"),
            btn -> applyPreset(VacuumConfig.ultra())).dimensions(COL_L + (pw + 4) * 2, y, pw, BH).build());
        y += BH + PAD + 4;

        // ── Auto-performance & Lag ────────────────────────────────────────────
        addSectionLabel("Performance Automation", COL_L, y); y += 14;
        addToggle("Auto-Performance Mode", config.autoPerformanceMode,
            v -> config.autoPerformanceMode = v, COL_L, y);
        addToggle("Lag Spike Detector", config.lagSpikeDetector,
            v -> config.lagSpikeDetector = v, COL_R, y); y += BH + PAD;
        addIntSlider("Low FPS Threshold", config.autoLowFpsThreshold, 5, 60,
            v -> config.autoLowFpsThreshold = v, COL_L, y);
        addIntSlider("Lag Threshold (ms)", config.lagSpikeThresholdMs, 20, 200,
            v -> config.lagSpikeThresholdMs = v, COL_R, y); y += BH + PAD + 4;

        // ── Chunk Optimizations ───────────────────────────────────────────────
        addSectionLabel("Chunk Optimizations", COL_L, y); y += 14;
        addToggle("Dynamic Chunk Cap", config.dynamicChunkCap,
            v -> config.dynamicChunkCap = v, COL_L, y);
        addIntSlider("Chunk Load Cap", config.chunkLoadingCap, 1, 16,
            v -> config.chunkLoadingCap = v, COL_R, y); y += BH + PAD;
        addToggle("Chunk Pre-loader", config.enableChunkPreloader,
            v -> config.enableChunkPreloader = v, COL_L, y);
        addIntSlider("Preload Radius", config.chunkPreloadRadius, 1, 5,
            v -> config.chunkPreloadRadius = v, COL_R, y); y += BH + PAD;
        addIntSlider("Max Gen Threads", config.maxSimultaneousChunkGenerations, 1, 8,
            v -> config.maxSimultaneousChunkGenerations = v, COL_L, y);
        addToggle("Deferred Chunk Mesh", config.deferredChunkMeshBuilding,
            v -> config.deferredChunkMeshBuilding = v, COL_R, y); y += BH + PAD + 4;

        // ── Entity Optimizations ──────────────────────────────────────────────
        addSectionLabel("Entity Optimizations", COL_L, y); y += 14;
        addToggle("Entity Culling", config.entityCulling,
            v -> config.entityCulling = v, COL_L, y);
        addToggle("Frustum Culling", config.frustumCulling,
            v -> config.frustumCulling = v, COL_R, y); y += BH + PAD;
        addIntSlider("Entity Tracking Cap", config.entityTrackingCap, 64, 2048,
            v -> config.entityTrackingCap = v, COL_L, y);
        addIntSlider("Throttle Distance", config.entityUpdateThrottleDistance, 0, 128,
            v -> config.entityUpdateThrottleDistance = v, COL_R, y); y += BH + PAD + 4;

        // ── Client Latency ────────────────────────────────────────────────────
        addSectionLabel("Client Latency", COL_L, y); y += 14;
        addToggle("Client-Side Hit Reg", config.clientSideHitRegistration,
            v -> config.clientSideHitRegistration = v, COL_L, y);
        addToggle("Predictive Swing", config.predictiveSwing,
            v -> config.predictiveSwing = v, COL_R, y); y += BH + PAD;
        addToggle("Instant Block Interaction", config.instantBlockInteraction,
            v -> config.instantBlockInteraction = v, COL_L, y);
        addToggle("Reduce Packet Spam", config.reducePacketSpam,
            v -> config.reducePacketSpam = v, COL_R, y); y += BH + PAD + 4;

        // ── Render & Audio ────────────────────────────────────────────────────
        addSectionLabel("Render and Audio", COL_L, y); y += 14;
        addToggle("Smart Particles", config.smartParticles,
            v -> config.smartParticles = v, COL_L, y);
        addIntSlider("Max Particles", config.maxParticleCount, 256, 16384,
            v -> config.maxParticleCount = v, COL_R, y); y += BH + PAD;
        addToggle("Cull Distant Sounds", config.cullDistantSounds,
            v -> config.cullDistantSounds = v, COL_L, y);
        addIntSlider("Sound Cull Distance", config.soundCullDistance, 16, 256,
            v -> config.soundCullDistance = v, COL_R, y); y += BH + PAD;
        addIntSlider("Max Simultaneous Sounds", config.maxSimultaneousSounds, 16, 256,
            v -> config.maxSimultaneousSounds = v, COL_L, y); y += BH + PAD + 4;

        // ── Debug ─────────────────────────────────────────────────────────────
        addSectionLabel("Debug", COL_L, y); y += 14;
        addToggle("Show HUD Overlay", config.showDebugOverlay,
            v -> config.showDebugOverlay = v, COL_L, y);
        addToggle("Log Optimization Events", config.logOptimizationEvents,
            v -> config.logOptimizationEvents = v, COL_R, y); y += BH + PAD;

        // ── Done ──────────────────────────────────────────────────────────────
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Done"), btn -> close())
            .dimensions(this.width / 2 - 100, this.height - 28, 200, 20).build());
    }

    private void applyPreset(VacuumConfig preset) {
        this.config = preset;
        VacuumConfig.save(preset);
        // Reload screen to reflect new values
        if (this.client != null) {
            this.client.setScreen(new VacuumOptionsScreen(parent));
        }
    }

    private void addSectionLabel(String label, int x, int y) {
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("§l" + label), btn -> {}
        ).dimensions(x, y, BW * 2 + 10, 12).build()).active = false;
    }

    private void addToggle(String label, boolean initial,
            java.util.function.Consumer<Boolean> setter, int x, int y) {
        this.addDrawableChild(CyclingButtonWidget.onOffBuilder(initial)
            .build(x, y, BW, BH, Text.literal(label), (btn, val) -> setter.accept(val)));
    }

    private void addIntSlider(String label, int initial, int min, int max,
            java.util.function.Consumer<Integer> setter, int x, int y) {
        double progress = max == min ? 0.0 : (double)(initial - min) / (double)(max - min);
        this.addDrawableChild(new SliderWidget(x, y, BW, BH,
                Text.literal(label + ": " + initial), progress) {
            int value = initial;
            @Override protected void updateMessage() { setMessage(Text.literal(label + ": " + value)); }
            @Override protected void applyValue() {
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
