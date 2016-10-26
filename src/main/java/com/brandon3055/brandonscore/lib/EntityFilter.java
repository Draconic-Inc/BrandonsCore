package com.brandon3055.brandonscore.lib;

import com.brandon3055.brandonscore.utils.LinkedHashList;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.IAnimals;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by brandon3055 on 23/10/2016.
 */
public abstract class EntityFilter {

    public boolean detectPassive = true;
    public boolean detectHostile = true;
    public boolean detectPlayer = true;
    public boolean detectOther = true;
    public boolean isWhiteList = false;
    public LinkedHashList<String> entityList = new LinkedHashList<>();

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
        if ((entity instanceof EntityAnimal || (entity instanceof IAnimals && !(entity instanceof IMob))) && !detectPassive) {
            return false;
        }

        if (isTypeSelectionEnabled()) {
            if ((entity instanceof EntityMob || entity instanceof IMob) && !detectHostile) {
                return false;
            }

            if (entity instanceof EntityPlayer && !detectPlayer) {
                return false;
            }
        }

        if (!(entity instanceof EntityAnimal || (entity instanceof IAnimals && !(entity instanceof IMob))) && !(entity instanceof EntityMob || entity instanceof IMob) && !(entity instanceof EntityPlayer) && (!detectOther || !isOtherSelectorEnabled())) {
            return false;
        }

        if (!isListEnabled()) {
            return true;
        }

        if (entity instanceof EntityPlayer && entityList.contains("[player]:" + entity.getName())) {
            return isWhiteList;
        }

        if (entityList.contains(EntityList.getEntityString(entity))) {
            return isWhiteList;
        }

        return !isWhiteList;
    }

    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setBoolean("detectPassive", detectPassive);
        compound.setBoolean("detectHostile", detectHostile);
        compound.setBoolean("detectPlayer", detectPlayer);
        compound.setBoolean("detectOther", detectOther);
        compound.setBoolean("isWhiteList", isWhiteList);

        NBTTagList list = new NBTTagList();
        for (String entity : entityList) {
            list.appendTag(new NBTTagString(entity));
        }
        compound.setTag("entityList", list);

        return compound;
    }

    public NBTTagCompound readFromNBT(NBTTagCompound compound) {
        detectPassive = compound.getBoolean("detectPassive");
        detectHostile = compound.getBoolean("detectHostile");
        detectPlayer = compound.getBoolean("detectPlayer");
        detectOther = compound.getBoolean("detectOther");
        isWhiteList = compound.getBoolean("isWhiteList");

        if (compound.hasKey("entityList")) {
            entityList.clear();
            NBTTagList list = compound.getTagList("entityList", 8);
            for (int i = 0; i < list.tagCount(); i++) {
                entityList.add(list.getStringTagAt(i));
            }
        }

        return compound;
    }

    public void sendConfigToServer() {
        NBTTagCompound compound = new NBTTagCompound();
        compound.setBoolean("detectPassive", detectPassive);
        compound.setBoolean("detectHostile", detectHostile);
        compound.setBoolean("detectPlayer", detectPlayer);
        compound.setBoolean("detectOther", detectOther);
        compound.setBoolean("isWhiteList", isWhiteList);

        NBTTagList list = new NBTTagList();
        for (String entity : entityList) {
            list.appendTag(new NBTTagString(entity));
        }
        compound.setTag("entityList", list);

        sendConfigToServer(compound);
    }

    /**
     * Used by the GUI to send the config to the server.
     * */
    protected abstract void sendConfigToServer(NBTTagCompound compound);

    public void receiveConfigFromClient(NBTTagCompound compound) {
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

            if (compound.hasKey("entityList")) {
                entityList.clear();
                NBTTagList list = compound.getTagList("entityList", 8);
                for (int i = 0; i < list.tagCount(); i++) {
                    entityList.add(list.getStringTagAt(i));
                }
            }
        }
    }

    public abstract boolean isListEnabled();

    public abstract boolean isTypeSelectionEnabled();

    public abstract boolean isOtherSelectorEnabled();
}
