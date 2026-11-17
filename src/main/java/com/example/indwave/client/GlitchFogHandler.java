package com.example.indwave.client;

import com.example.indwave.IndwavePlotAdapter;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "indwave", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class GlitchFogHandler {

    @SubscribeEvent
    public static void onRenderFog(ViewportEvent.RenderFog event) {
        var player = Minecraft.getInstance().player;
        if (player == null || player.level() == null) return;
        if (!player.level().dimension().equals(IndwavePlotAdapter.PLOTS)) return;

        var adapter = IndwavePlotAdapter.getInstance();
        if (adapter == null) return;

        int x = player.getBlockX();
        int z = player.getBlockZ();
        var plotOpt = adapter.grid.plotNear(x, z, 50);
        if (plotOpt.isEmpty()) return;

        var id = plotOpt.get();
        int d = IndwavePlotAdapter.distanceToBorder(player.getX(), player.getZ(),
                adapter.grid.originX(id), adapter.grid.originZ(id), adapter.grid.size());

        // Если мы в глич-зоне (снаружи, но не до телепорта)
        if (d <= 0 && d > -17) {
            // Обязательно отменяем стандартный туман
            event.setCanceled(true);

            // Задаем свои дистанции (в блоках)
            event.setNearPlaneDistance(2.0f);
            event.setFarPlaneDistance(15.0f); // Все, что дальше 15 блоков, скроется в тумане
        }
    }
}