package com.indwave.plot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.example.indwave.plot.Plot;
import com.example.indwave.plot.PlotActivation;
import com.example.indwave.plot.PlotGrid;
import com.example.indwave.plot.PlotId;
import org.junit.jupiter.api.Test;

class PlotCoreTest {

    @Test
    void gridMapsPointsAndVoid() {
        PlotGrid grid = new PlotGrid(); // Новый конструктор без аргументов
        PlotId id = grid.idFromIndex(12); // ряд 1, столбец 2
        assertEquals(new PlotId(2, 1), id);
        assertEquals(2000, grid.originX(id));
        assertEquals(1000, grid.originZ(id));
        assertEquals(id, grid.plotAt(2100, 1100).orElseThrow());
        assertTrue(grid.plotAt(2500, 1100).isEmpty()); // пустота между участками
        assertTrue(grid.plotAt(-1, 0).isEmpty());
    }

    @Test
    void guestCannotAccessMemberCan() {
        UUID owner = UUID.randomUUID();
        UUID friend = UUID.randomUUID();
        UUID stranger = UUID.randomUUID();
        Plot plot = new Plot(new PlotId(0, 0), owner);
        plot.addMember(friend);
        assertTrue(plot.canAccess(owner, false));
        assertTrue(plot.canAccess(friend, false));
        assertFalse(plot.canAccess(stranger, false));
        assertTrue(plot.canAccess(stranger, true)); // обход для админа
    }

    @Test
    void activationWithGracePeriod() {
        List<String> log = new ArrayList<>();
        PlotActivation act = new PlotActivation(200, new PlotActivation.Listener() {
            public void onActivate(PlotId id) { log.add("on"); }
            public void onDeactivate(PlotId id) { log.add("off"); }
        });
        PlotId id = new PlotId(0, 0);
        UUID a = UUID.randomUUID();
        UUID b = UUID.randomUUID();

        act.enter(id, a, 0);
        act.enter(id, b, 5); // второй игрок не вызывает повторной активации
        assertEquals(List.of("on"), log);

        act.leave(id, a, 10);
        act.leave(id, b, 20); // участок опустел, старт ожидания до тика 220
        act.tick(100);
        assertTrue(act.isActive(id));

        act.enter(id, a, 150); // вернулись в период ожидания
        act.tick(300);
        assertTrue(act.isActive(id));
        assertEquals(List.of("on"), log);

        act.leave(id, a, 310);
        act.tick(509);
        assertTrue(act.isActive(id));
        act.tick(510);
        assertFalse(act.isActive(id));
        assertEquals(List.of("on", "off"), log);
    }
}
