package com.brandon3055.brandonscore.worldentity;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.world.storage.WorldSavedData;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by brandon3055 on 16/12/20
 */
public class WorldEntitySaveData extends WorldSavedData {
    //This is the save data file name
    public static final String ID = "brandonscore_world_entity";
    private List<WorldEntity> entities = new ArrayList<>();
    private Runnable saveCallback;

    public WorldEntitySaveData(String name) {
        super(name);
    }

    public WorldEntitySaveData() {
        super(ID);
    }

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

    @Override
    public void read(CompoundNBT nbt) {
        ListNBT list = nbt.getList("entities", 10);
        for (INBT inbt : list) {
            entities.add(WorldEntity.readWorldEntity((CompoundNBT) inbt));
        }
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        ListNBT list = new ListNBT();
        for (WorldEntity entity : entities) {
            CompoundNBT entityTag = new CompoundNBT();
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
