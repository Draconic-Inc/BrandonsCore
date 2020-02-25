package com.brandon3055.brandonscore.lib;

import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by brandon3055 on 23/10/2016.
 */
@Deprecated
public abstract class EntityFilter {

    public boolean detectPassive = true;
    public boolean detectHostile = true;
    public boolean detectPlayer = true;
    public boolean detectOther = true;
    public boolean isWhiteList = false;
    public LinkedList<String> entityList = new LinkedList<>();

    public EntityFilter() {}

    /**
     * @param entities A list of entities.
     * @return true if any of the entities in the list match this filter.
     */
    public boolean containsMatch(List<Entity> entities) {
        for (Entity entity : entities) {
            if (isMatch(entity)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param entities A list of entities.
     * @return a new list containing all entities that match this filter.
     */
    public List<Entity> filterEntities(List<Entity> entities) {
        List<Entity> newList = new ArrayList<>();

        for (Entity entity : entities) {
            if (isMatch(entity)) {
                newList.add(entity);
            }
        }

        return newList;
    }

    /**
     * @param entity an entity.
     * @return Returns true if the given entity matches this filter.
     */
    public boolean isMatch(Entity entity) {
        if ((entity instanceof AnimalEntity && !(entity instanceof IMob)) && !detectPassive) {
            return false;
        }

        if (isTypeSelectionEnabled()) {
            if ((entity instanceof MobEntity || entity instanceof IMob) && !detectHostile) {
                return false;
            }

            if (entity instanceof PlayerEntity && !detectPlayer) {
                return false;
            }
        }

        if (!(entity instanceof MobEntity || entity instanceof IMob) && !(entity instanceof PlayerEntity) && (!detectOther || !isOtherSelectorEnabled())) {
            return false;
        }

        if (!isListEnabled()) {
            return true;
        }

        if (entity instanceof PlayerEntity && entityList.contains("[player]:" + entity.getName())) {
            return isWhiteList;
        }

//        if (entityList.contains(EntityList.getEntityString(entity)) || (entity instanceof ItemEntity && entityList.contains("Item"))) {
//            return isWhiteList;
//        }

        return !isWhiteList;
    }

    public CompoundNBT writeToNBT(CompoundNBT compound) {
        compound.putBoolean("detectPassive", detectPassive);
        compound.putBoolean("detectHostile", detectHostile);
        compound.putBoolean("detectPlayer", detectPlayer);
        compound.putBoolean("detectOther", detectOther);
        compound.putBoolean("isWhiteList", isWhiteList);

        ListNBT list = new ListNBT();
        for (String entity : entityList) {
//            list.appendTag(new NBTTagString(entity));
        }
        compound.put("entityList", list);

        return compound;
    }

    public CompoundNBT readFromNBT(CompoundNBT compound) {
        detectPassive = compound.getBoolean("detectPassive");
        detectHostile = compound.getBoolean("detectHostile");
        detectPlayer = compound.getBoolean("detectPlayer");
        detectOther = compound.getBoolean("detectOther");
        isWhiteList = compound.getBoolean("isWhiteList");
//
//        if (compound.hasKey("entityList")) {
//            entityList.clear();
//            ListNBT list = compound.getTagList("entityList", 8);
//            for (int i = 0; i < list.tagCount(); i++) {
//                entityList.add(list.getStringTagAt(i));
//            }
//        }

        return compound;
    }

    public void sendConfigToServer() {
        CompoundNBT compound = new CompoundNBT();
        compound.putBoolean("detectPassive", detectPassive);
        compound.putBoolean("detectHostile", detectHostile);
        compound.putBoolean("detectPlayer", detectPlayer);
        compound.putBoolean("detectOther", detectOther);
        compound.putBoolean("isWhiteList", isWhiteList);

        ListNBT list = new ListNBT();
        for (String entity : entityList) {
//            list.appendTag(new NBTTagString(entity));
        }
        compound.put("entityList", list);

        sendConfigToServer(compound);
    }

    /**
     * Used by the GUI to send the config to the server.
     * */
    protected abstract void sendConfigToServer(CompoundNBT compound);

    public void receiveConfigFromClient(CompoundNBT compound) {
        if (isTypeSelectionEnabled()) {
            detectPassive = compound.getBoolean("detectPassive");
            detectHostile = compound.getBoolean("detectHostile");
            detectPlayer = compound.getBoolean("detectPlayer");
        }

        if (isOtherSelectorEnabled()) {
            detectOther = compound.getBoolean("detectOther");
        }
        if (isListEnabled()) {
            isWhiteList = compound.getBoolean("isWhiteList");

//            if (compound.hasKey("entityList")) {
//                entityList.clear();
//                ListNBT list = compound.getTagList("entityList", 8);
//                for (int i = 0; i < list.tagCount(); i++) {
//                    entityList.add(list.getStringTagAt(i));
//                }
//            }
        }
    }

    public abstract boolean isListEnabled();

    public abstract boolean isTypeSelectionEnabled();

    public abstract boolean isOtherSelectorEnabled();
}
