package com.example.indwave.plot;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlotDataManager {
    private static final Map<PlotId, PlotData> plots = new HashMap<>();

    public static PlotData getPlot(PlotId id) {
        return plots.computeIfAbsent(id, PlotData::new);
    }

    public static boolean isOwner(PlotId id, UUID player) {
        PlotData data = getPlot(id);
        return data.getOwner() != null && data.getOwner().equals(player);
    }
}