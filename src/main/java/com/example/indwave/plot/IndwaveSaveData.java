package com.example.indwave.plot;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class IndwaveSaveData extends SavedData {
    private final Map<PlotId, UUID> plotOwners = new HashMap<>();
    private final Map<PlotId, Set<UUID>> plotMembers = new HashMap<>();

    public static IndwaveSaveData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(IndwaveSaveData::new, IndwaveSaveData::load, null),
                "indwave_plots"
        );
    }

    public UUID getOwner(PlotId id) {
        return plotOwners.get(id);
    }

    public void setOwner(PlotId id, UUID owner) {
        plotOwners.put(id, owner);
        setDirty();
    }

    public Set<UUID> getMembers(PlotId id) {
        return plotMembers.computeIfAbsent(id, k -> new HashSet<>());
    }

    public void addMember(PlotId id, UUID member) {
        getMembers(id).add(member);
        setDirty();
    }

    public void removeMember(PlotId id, UUID member) {
        getMembers(id).remove(member);
        setDirty();
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        CompoundTag ownersTag = new CompoundTag();
        plotOwners.forEach((id, uuid) -> ownersTag.putUUID(id.gx() + "_" + id.gz(), uuid));
        tag.put("Owners", ownersTag);

        CompoundTag membersTag = new CompoundTag();
        plotMembers.forEach((id, set) -> {
            CompoundTag list = new CompoundTag();
            int i = 0;
            for (UUID uuid : set) {
                list.putUUID("p" + i++, uuid);
            }
            membersTag.put(id.gx() + "_" + id.gz(), list);
        });
        tag.put("Members", membersTag);
        return tag;
    }

    public static IndwaveSaveData load(CompoundTag tag, HolderLookup.Provider registries) {
        IndwaveSaveData data = new IndwaveSaveData();
        CompoundTag ownersTag = tag.getCompound("Owners");
        for (String key : ownersTag.getAllKeys()) {
            String[] parts = key.split("_");
            data.plotOwners.put(new PlotId(Integer.parseInt(parts[0]), Integer.parseInt(parts[1])), ownersTag.getUUID(key));
        }

        CompoundTag membersTag = tag.getCompound("Members");
        for (String key : membersTag.getAllKeys()) {
            String[] parts = key.split("_");
            PlotId id = new PlotId(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
            CompoundTag list = membersTag.getCompound(key);
            Set<UUID> set = new HashSet<>();
            for (String pKey : list.getAllKeys()) {
                set.add(list.getUUID(pKey));
            }
            data.plotMembers.put(id, set);
        }
        return data;
    }
}