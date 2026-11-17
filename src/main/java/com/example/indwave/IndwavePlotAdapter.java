package com.example.indwave;

import com.example.indwave.particle.ModParticleTypes;
import com.example.indwave.plot.PlotActivation;
import com.example.indwave.plot.PlotGrid;
import com.example.indwave.plot.PlotId;
import com.example.indwave.plot.PlotManager;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class IndwavePlotAdapter {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final ResourceKey<Level> PLOTS = ResourceKey.create(
            Registries.DIMENSION, ResourceLocation.fromNamespaceAndPath("indwave", "plots"));

    private static final int GRID_STEP = 352;
    private static final int PLOT_SIZE = 256;
    private static final int GRID_WIDTH = 1000;
    private static final long GRACE_TICKS = 200;

    private static final int FLOOR_Y = 64;
    private static final int WALL_HEIGHT = 50;
    private static final int BARRIER_DISTANCE = 20;

    public final PlotGrid grid = new PlotGrid();
    public final PlotManager manager;

    private long tickCounter = 0;
    private MinecraftServer server;
    private final Set<PlotId> builtPlots = new HashSet<>();
    private final Map<UUID, Integer> lastZone = new HashMap<>();

    private static IndwavePlotAdapter INSTANCE;

    public static IndwavePlotAdapter getInstance() {
        return INSTANCE;
    }

    public IndwavePlotAdapter() {
        INSTANCE = this;
        PlotActivation activation = new PlotActivation(GRACE_TICKS, new PlotActivation.Listener() {
            @Override
            public void onActivate(PlotId id) {
                LOGGER.info("Plot activated: {}", id);
                ensurePlotBuilt(id);
            }

            @Override
            public void onDeactivate(PlotId id) {
                LOGGER.info("Plot deactivated: {}", id);
            }
        });
        this.manager = new PlotManager(grid, activation);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            // Запускаем телепорт через 1 тик (когда игрок полностью загрузится)
            if (server != null) {
                server.execute(() -> {
                    ServerLevel level = server.getLevel(PLOTS);
                    if (level != null) {
                        int index = Math.abs(player.getUUID().hashCode() % 100);
                        PlotId myPlot = grid.idFromIndex(index);

                        int tx = grid.originX(myPlot) + 128;
                        int tz = grid.originZ(myPlot) + 128;

                        player.teleportTo(level, tx, FLOOR_Y + 2, tz, player.getYRot(), player.getXRot());
                        player.sendSystemMessage(Component.literal("§aДобро пожаловать на ваш участок!"));
                    }
                });
            }
        }
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent.Post event) {
        if (this.server == null) {
            this.server = event.getServer();
        }
        tickCounter++;

        if (tickCounter % 5 == 0) {
            manager.tick(tickCounter);
        }

        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            boolean inPlots = player.level().dimension().equals(PLOTS);
            int x = inPlots ? player.getBlockX() : -1;
            int z = inPlots ? player.getBlockZ() : -1;

            if (inPlots && grid.plotNear(x, z, 50).isPresent()) {
                if (tickCounter % 5 == 0) {
                    manager.onPositionUpdate(player.getUUID(), x, z, tickCounter);
                }

                grid.plotNear(x, z, 50).ifPresent(id -> {
                    int d = distanceToBorder(player.getX(), player.getZ(),
                            grid.originX(id), grid.originZ(id), grid.size());

                    // 1. Внутри участка (d > 0) - чисто
                    if (d > 0) {
                        player.removeEffect(MobEffects.DARKNESS);
                    }
                    // 2. Глич-зона (от 0 до -17) - тьма и частицы
                    else if (d > -17) {
                        // Накладываем тьму
                        player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 40, 3, false, false, true));

                        // Спавним частицы вокруг игрока
                        if (tickCounter % 5 == 0) {
                            spawnAreaParticles(player.serverLevel(), player);
                        }
                    }
                    // 3. Дальше глич-зоны (d <= -17) - телепорт на свой участок
                    else {
                        teleportHome(player, id); // <-- ТЕПЕРЬ НА ТЕКУЩИЙ УЧАСТОК
                        player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 100, 3, false, false, true));
                    }
                });
            } else if (inPlots) {
                if (tickCounter % 5 == 0) manager.onPositionUpdate(player.getUUID(), -1, -1, tickCounter);
                player.removeEffect(MobEffects.DARKNESS);
            } else {
                if (tickCounter % 5 == 0) manager.onPositionUpdate(player.getUUID(), -1, -1, tickCounter);
                player.removeEffect(MobEffects.DARKNESS);
            }
        }
    }

    @SubscribeEvent
    public void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        manager.onDisconnect(event.getEntity().getUUID(), tickCounter);
    }

    private void spawnAreaParticles(ServerLevel level, ServerPlayer player) {
        double px = player.getX();
        double pz = player.getZ();
        double py = player.getY() + 10;

        level.sendParticles(
                ModParticleTypes.GLITCH_SQUARE.get(),
                px, py, pz,
                500,
                40.0, 25.0, 40.0,
                0.0
        );
    }

    private void ensurePlotBuilt(PlotId id) {
        if (!builtPlots.add(id)) return;
        if (server == null) return;
        ServerLevel level = server.getLevel(PLOTS);
        if (level == null) return;

        int originX = grid.originX(id);
        int originZ = grid.originZ(id);
        int size = grid.size();

        int minX = originX - BARRIER_DISTANCE;
        int maxX = originX + size - 1 + BARRIER_DISTANCE;
        int minZ = originZ - BARRIER_DISTANCE;
        int maxZ = originZ + size - 1 + BARRIER_DISTANCE;
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                boolean onRing = (x == minX || x == maxX || z == minZ || z == maxZ);
                if (!onRing) continue;
                for (int y = FLOOR_Y - 2; y <= FLOOR_Y + WALL_HEIGHT; y++) {
                    level.setBlock(new BlockPos(x, y, z), Blocks.BARRIER.defaultBlockState(), 2);
                }
            }
        }
    }

    public static int distanceToBorder(double x, double z, int originX, int originZ, int size) {
        double minX = originX;
        double maxX = originX + size - 1;
        double minZ = originZ;
        double maxZ = originZ + size - 1;

        double insideX = Math.min(x - minX, maxX - x);
        double insideZ = Math.min(z - minZ, maxZ - z);
        double inside = Math.min(insideX, insideZ);

        if (inside >= 0) {
            return (int) Math.floor(inside);
        }
        double outsideX = Math.max(minX - x, x - maxX);
        double outsideZ = Math.max(minZ - z, z - maxZ);
        return -(int) Math.ceil(Math.max(outsideX, outsideZ));
    }

    private void teleportHome(ServerPlayer player, PlotId id) {
        int originX = grid.originX(id);
        int originZ = grid.originZ(id);
        int size = grid.size();
        double tx = originX + size / 2.0;
        double tz = originZ + size / 2.0;
        player.teleportTo(player.serverLevel(), tx, FLOOR_Y + 2, tz,
                player.getYRot(), player.getXRot());
    }

    private void teleportToOwnPlot(ServerPlayer player) {
        // Определяем индекс участка по UUID игрока (как при автовыдаче)
        int index = Math.abs(player.getUUID().hashCode() % 100);
        PlotId myPlot = grid.idFromIndex(index);

        int tx = grid.originX(myPlot) + 128;
        int tz = grid.originZ(myPlot) + 128;

        player.teleportTo(player.serverLevel(), tx, FLOOR_Y + 2, tz, player.getYRot(), player.getXRot());
    }

}