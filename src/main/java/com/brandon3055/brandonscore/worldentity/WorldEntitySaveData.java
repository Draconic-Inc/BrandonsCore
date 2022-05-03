package com.brandon3055.brandonscore.worldentity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.saveddata.SavedData;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by brandon3055 on 16/12/20
 */
public class WorldEntitySaveData extends SavedData {
    public static final String FILE_ID = "brandonscore_world_entity";
    private List<WorldEntity> entities = new ArrayList<>();
    private Runnable saveCallback;

    public void updateEntities(List<WorldEntity> entities) {
        this.entities.clear();
        if (entities != null){
            this.entities.addAll(entities);
        }
    }

    public void setSaveCallback(Runnable saveCallback) {
        this.saveCallback = saveCallback;
    }

    public List<WorldEntity> getEntities() {
        return entities;
    }

    public static WorldEntitySaveData load(CompoundTag nbt) {
        WorldEntitySaveData data = new WorldEntitySaveData();
        ListTag list = nbt.getList("entities", 10);
        for (Tag inbt : list) {
            data.entities.add(WorldEntity.readWorldEntity((CompoundTag) inbt));
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag compound) {
        ListTag list = new ListTag();
        for (WorldEntity entity : entities) {
            CompoundTag entityTag = new CompoundTag();
            entity.write(entityTag);
            list.add(entityTag);
        }
        compound.put("entities", list);
        return compound;
    }

    @Override
    public void save(File fileIn) {
        setDirty(true);
        saveCallback.run();
        super.save(fileIn);
    }
}
