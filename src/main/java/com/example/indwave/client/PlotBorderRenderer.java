package com.example.indwave.client;

import com.example.indwave.IndwavePlotAdapter;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "indwave", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class PlotBorderRenderer {

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;
        if (!mc.player.level().dimension().equals(IndwavePlotAdapter.PLOTS)) return;

        var adapter = IndwavePlotAdapter.getInstance();
        if (adapter == null) return;

        int px = mc.player.getBlockX();
        int pz = mc.player.getBlockZ();
        var plotOpt = adapter.grid.plotNear(px, pz, 300);
        if (plotOpt.isEmpty()) return;

        var id = plotOpt.get();

        // Координаты краев участка
        float minX = adapter.grid.originX(id) + 0.5f;
        float minZ = adapter.grid.originZ(id) + 0.5f;
        float maxX = minX + adapter.grid.size() - 1.0f;
        float maxZ = minZ + adapter.grid.size() - 1.0f;

        // ВЫСОТА ЛИНИИ (на 1 блок выше травы, трава на 64)
        float y = 65.0f;

        Vec3 cam = event.getCamera().getPosition();

        // Создаем PoseStack и применяем позицию камеры
        PoseStack poseStack = new PoseStack();
        poseStack.mulPose(event.getPoseStack());
        poseStack.translate(-cam.x, -cam.y, -cam.z);

        // Настройки рендера (отключаем глубину, чтобы линия была видна сквозь блоки)
        RenderSystem.disableDepthTest();
        RenderSystem.lineWidth(2.0f);

        // Рисуем рамку (AABB)
        AABB border = new AABB(minX, y, minZ, maxX, y, maxZ);
        VertexConsumer consumer = mc.renderBuffers().bufferSource().getBuffer(RenderType.lines());

        // 1.0f, 1.0f, 1.0f, 1.0f — белый цвет с полной непрозрачностью
        LevelRenderer.renderLineBox(poseStack, consumer, border, 1.0f, 1.0f, 1.0f, 1.0f);

        mc.renderBuffers().bufferSource().endBatch(RenderType.lines());

        RenderSystem.enableDepthTest();
    }
}