package com.brandon3055.brandonscore.api;

import com.brandon3055.brandonscore.api.TechLevel;
import net.minecraft.item.ItemStack;

/**
 * Created by brandon3055 on 8/02/19.
 *
 * This interface will eventually be expanded and extended to add additional functionality as needed.
 */
@Deprecated//I may not actually end up using this.
public interface ITechItem {

    TechLevel getTechLevel(ItemStack stack);
}
