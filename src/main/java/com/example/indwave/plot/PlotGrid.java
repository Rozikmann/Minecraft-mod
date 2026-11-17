package com.example.indwave.plot;

import java.util.Optional;

public final class PlotGrid {
    // Шаг сетки: 256 (размер) + 96 (отступ) = 352.
    private final int step = 512;
    private final int size = 256;
    private final int width = 1000;

    public PlotId idFromIndex(int index) {
        if (index < 0) throw new IllegalArgumentException("index < 0");
        return new PlotId(index % width, index / width);
    }

    public int originX(PlotId id) { return id.gx() * step; }
    public int originZ(PlotId id) { return id.gz() * step; }
    public int size() { return size; }

    public Optional<PlotId> plotAt(int x, int z) {
        if (x < 0 || z < 0) return Optional.empty();
        int gx = x / step;
        int gz = z / step;
        if (gx >= width) return Optional.empty();
        if (x - gx * step >= size || z - gz * step >= size) {
            return Optional.empty();
        }
        return Optional.of(new PlotId(gx, gz));
    }

    public Optional<PlotId> plotNear(int x, int z, int radius) {
        Optional<PlotId> exact = plotAt(x, z);
        if (exact.isPresent()) return exact;

        int gx = Math.floorDiv(x, step);
        int gz = Math.floorDiv(z, step);
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                int cx = gx + dx;
                int cz = gz + dz;
                if (cx < 0 || cz < 0 || cx >= width) continue;
                int minX = cx * step;
                int minZ = cz * step;
                int maxX = minX + size - 1;
                int maxZ = minZ + size - 1;
                int ox = Math.max(minX - x, x - maxX);
                int oz = Math.max(minZ - z, z - maxZ);
                if (Math.max(ox, oz) <= radius) {
                    return Optional.of(new PlotId(cx, cz));
                }
            }
        }
        return Optional.empty();
    }
}