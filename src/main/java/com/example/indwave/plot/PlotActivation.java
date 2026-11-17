package com.example.indwave.plot;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Участок активен, пока на нём есть хотя бы один игрок (любой).
 * После ухода последнего игрока деактивация откладывается на graceTicks,
 * чтобы выход-вход не вызывал лишних сохранений и пересборки сетей.
 *
 * Вызывать только из основного потока сервера.
 */
public final class PlotActivation {
    public interface Listener {
        /** Загрузить чанки, собрать сети, включить тик. */
        void onActivate(PlotId id);

        /** Сохранить данные, остановить тик, снять тикеты чанков. */
        void onDeactivate(PlotId id);
    }

    private final long graceTicks;
    private final Listener listener;
    private final Map<PlotId, Set<UUID>> present = new HashMap<>();
    private final Map<PlotId, Long> deactivateAt = new HashMap<>();
    private final Set<PlotId> active = new HashSet<>();

    public PlotActivation(long graceTicks, Listener listener) {
        this.graceTicks = graceTicks;
        this.listener = listener;
    }

    public void enter(PlotId id, UUID player, long nowTick) {
        present.computeIfAbsent(id, k -> new HashSet<>()).add(player);
        deactivateAt.remove(id); // вернулись в период ожидания - отмена
        if (active.add(id)) {
            listener.onActivate(id);
        }
    }

    public void leave(PlotId id, UUID player, long nowTick) {
        Set<UUID> players = present.get(id);
        if (players == null || !players.remove(player)) {
            return;
        }
        if (players.isEmpty()) {
            present.remove(id);
            deactivateAt.put(id, nowTick + graceTicks);
        }
    }

    /** Вызывать каждый тик (или раз в несколько тиков). */
    public void tick(long nowTick) {
        Iterator<Map.Entry<PlotId, Long>> it = deactivateAt.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<PlotId, Long> e = it.next();
            if (nowTick >= e.getValue()) {
                it.remove();
                if (active.remove(e.getKey())) {
                    listener.onDeactivate(e.getKey());
                }
            }
        }
    }

    public boolean isActive(PlotId id) {
        return active.contains(id);
    }

    public int playersOn(PlotId id) {
        Set<UUID> players = present.get(id);
        return players == null ? 0 : players.size();
    }
}
