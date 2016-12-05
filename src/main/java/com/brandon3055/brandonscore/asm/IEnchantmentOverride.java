package com.brandon3055.brandonscore.asm;

import net.minecraft.enchantment.EnumEnchantmentType;

/**
 * Created by brandon3055 on 30/06/2016.
 */
@Deprecated //This will not be needed in 1.11
public interface IEnchantmentOverride {
    boolean checkEnchantTypeValid(EnumEnchantmentType type);
}
