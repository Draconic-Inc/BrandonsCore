package com.brandon3055.brandonscore.handlers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

/**
 * Created by brandon3055 on 25/3/2016.
 * This class is to help out with all of the duel wielding stuff in 1.9
 */
public class HandHelper {

	/**Returns the first item found in ether of the players hands starting with the main hand*/
	public static ItemStack getMainFirst(EntityPlayer player){
		if (player.getHeldItemMainhand() != null) return player.getHeldItemMainhand();
		else return player.getHeldItemOffhand();
	}

	/**Returns the first item found in ether of the players hands starting with the off hand*/
	public static ItemStack getOffFirst(EntityPlayer player){
		if (player.getHeldItemOffhand() != null) return player.getHeldItemOffhand();
		else return player.getHeldItemMainhand();
	}

	/**Returns the first item found in ether of the players hands that is an instance of the given class, starting with the main hand*/
	public static <E> ItemStack getInstanceOf(EntityPlayer player, Class<E> clazz){
		if (player.getHeldItemMainhand() != null && clazz.isAssignableFrom(player.getHeldItemMainhand().getItem().getClass())){
			return player.getHeldItemMainhand();
		}
		else if (player.getHeldItemOffhand() != null && clazz.isAssignableFrom(player.getHeldItemOffhand().getItem().getClass())){
			return player.getHeldItemOffhand();
		}
		return null;
	}
}
