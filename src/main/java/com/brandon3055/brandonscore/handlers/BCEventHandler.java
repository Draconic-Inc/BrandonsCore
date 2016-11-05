package com.brandon3055.brandonscore.handlers;

import com.brandon3055.brandonscore.inventory.BlockToStackHelper;
import net.minecraft.entity.item.EntityItem;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Created by brandon3055 on 26/08/2016.
 */
public class BCEventHandler {

    @SubscribeEvent(priority = EventPriority.LOW)
    public void entityJoinWorld(EntityJoinWorldEvent event) {
        if (event.getEntity() instanceof EntityItem && BlockToStackHelper.itemCollection != null && !event.isCanceled()) {
            BlockToStackHelper.itemCollection.add(((EntityItem)event.getEntity()).getEntityItem());
            event.setCanceled(true);
        }
    }
}
