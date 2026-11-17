package com.example.indwave.plot;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Связывает сетку участков с активацией: следит, на каком участке находится
 * каждый игрок, и вызывает enter/leave при смене участка.
 *
 * Ничего не знает про Minecraft: координаты передаются как обычные int.
 * Вызывать только из основного потока сервера.
 */
public final class PlotManager {
    private final PlotGrid grid;
    private final PlotActivation activation;
    private final Map<UUID, PlotId> lastPlot = new HashMap<>();

    public PlotManager(PlotGrid grid, PlotActivation activation) {
        this.grid = grid;
        this.activation = activation;
    }

    /** Вызывать при каждом обновлении позиции игрока (например, раз в тик). */
    public void onPositionUpdate(UUID player, int x, int z, long nowTick) {
        Optional<PlotId> plotOpt = grid.plotAt(x, z);
        PlotId previous = lastPlot.get(player);
        PlotId current = plotOpt.orElse(null);

        if (Objects.equals(previous, current)) {
            return; // участок не сменился, ничего не делаем
        }
        if (previous != null) {
            activation.leave(previous, player, nowTick);
        }
        if (current != null) {
            activation.enter(current, player, nowTick);
            lastPlot.put(player, current);
        } else {
            lastPlot.remove(player);
        }
    }

    /** Вызывать при выходе игрока с сервера. */
    public void onDisconnect(UUID player, long nowTick) {
        PlotId previous = lastPlot.remove(player);
        if (previous != null) {
            activation.leave(previous, player, nowTick);
        }
    }

    /** Вызывать каждый тик для обработки задержки деактивации. */
    public void tick(long nowTick) {
        activation.tick(nowTick);
    }

    public Optional<PlotId> plotOf(UUID player) {
        return Optional.ofNullable(lastPlot.get(player));
    }
}
