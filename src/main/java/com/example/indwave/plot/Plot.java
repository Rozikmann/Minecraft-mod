package com.example.indwave.plot;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/** Данные участка. Хранить в файле участка; здесь только UUID, без ссылок на объекты Minecraft. */
public final class Plot {
    public enum Role { OWNER, MEMBER, GUEST }

    private final PlotId id;
    private final UUID owner;
    private final Set<UUID> members = new HashSet<>();

    public Plot(PlotId id, UUID owner) {
        this.id = id;
        this.owner = owner;
    }

    public PlotId id() {
        return id;
    }

    public UUID owner() {
        return owner;
    }

    public void addMember(UUID player) {
        members.add(player);
    }

    public void removeMember(UUID player) {
        members.remove(player);
    }

    public Role roleOf(UUID player) {
        if (owner.equals(player)) {
            return Role.OWNER;
        }
        return members.contains(player) ? Role.MEMBER : Role.GUEST;
    }

    /**
     * Единая проверка доступа. Все блоки и механизмы всех модов вызывают только её.
     * Гость не может ничего; админ с флагом обходит проверку.
     */
    public boolean canAccess(UUID player, boolean adminBypass) {
        return adminBypass || roleOf(player) != Role.GUEST;
    }
}
