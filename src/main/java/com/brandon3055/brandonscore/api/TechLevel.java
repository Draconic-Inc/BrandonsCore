package com.brandon3055.brandonscore.api;

import net.minecraft.item.IItemTier;
import net.minecraft.item.Rarity;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

import static net.minecraft.item.Rarity.*;
import static net.minecraft.util.text.TextFormatting.*;

/**
 * Created by brandon3055 on 8/02/19.
 * <p>
 * These are the definitions for the different tech levels in Draconic Evolution.
 * Also to make this a little less confusing i will be switching up the core names a little in 1.14+
 * They are now
 * Tier 0: Draconium Core
 * Tier 1: Wyvern Core
 * Tier 2: Draconic Core
 * Tier 3: Chaotic Core
 */
public enum TechLevel {
    //@formatter:off
    /**
     * Basic / Draconium level.
     */
    DRACONIUM   (0, WHITE,       COMMON,   3),
    /**
     * Wyvern can be thought of as "Nether Star tier"
     * Though that does not necessarily mean all wyvern tier items
     * require nether stars. Take wyvern energy crystals for example.
     */
    WYVERN      (1, BLUE,        UNCOMMON, 32),
    /**
     * AKA Awakened. Pretty self explanatory. Draconic is the tier above wyvern and in most cases
     * draconic tier items should require awakened draconium to craft.
     */
    DRACONIC    (2, GOLD,        RARE,     128),
    /**
     * Chaotic is the ultimate end game tier.
     * Obviously all chaotic tier items require chaos shards or fragments to craft.
     */
    CHAOTIC     (3, DARK_PURPLE, EPIC,     512);
    //@formatter:on

    public final int index;
    private final TextFormatting textColour;
    private Rarity rarity;
    private final int harvestLevel;
    private IItemTier itemTier;
    public static final TechLevel[] VALUES = new TechLevel[4];
    public static final TechLevel[] TOOL_LEVELS = new TechLevel[3];

    static {
        for (TechLevel tier : values()) {
            VALUES[tier.index] = tier;
            if (tier != DRACONIUM) {
                TOOL_LEVELS[tier.index - 1] = tier;
            }
        }
    }

    TechLevel(int index, TextFormatting colour, Rarity rarity, int harvestLevel) {
        this.index = index;
        this.textColour = colour;
        this.rarity = rarity;
        this.harvestLevel = harvestLevel;
    }

    public TextFormatting getTextColour() {
        return textColour;
    }

    public TextComponent getDisplayName() {
        return new TranslationTextComponent("tech_level.draconicevolution." + name().toLowerCase(Locale.ENGLISH));
    }

    public Rarity getRarity() {
        return rarity;
    }

    //The mining level does not really mean much as most mods dont do anything special with it.
    public int getHarvestLevel() {
        return harvestLevel;
    }

    public static TechLevel byIndex(int index) {
        return index >= 0 && index < VALUES.length ? VALUES[index] : DRACONIUM;
    }
}
