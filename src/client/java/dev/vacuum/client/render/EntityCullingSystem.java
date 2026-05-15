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
    private VacuumFrustum currentFrustum;
    private volatile int entityCount = 0;

    publi
