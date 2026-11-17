package com.example.indwave.plot;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class PlotData {
    private final PlotId id;
    private UUID owner;
    private final Set<UUID> members = new HashSet<>();

    public PlotData(PlotId id) {
        this.id = id;
    }

    public PlotId getId() { return id; }
    public UUID getOwner() { return owner; }
    public void setOwner(UUID owner) { this.owner = owner; }

    public Set<UUID> getMembers() { return members; }
    public void addMember(UUID player) { members.add(player); }
    public void removeMember(UUID player) { members.remove(player); }

    public boolean canAccess(UUID player) {
        if (owner == null) return true; // Если владельца нет, участок свободен
        return owner.equals(player) || members.contains(player);
    }
}