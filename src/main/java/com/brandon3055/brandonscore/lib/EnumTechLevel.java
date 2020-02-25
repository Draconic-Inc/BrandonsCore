package com.brandon3055.brandonscore.lib;

/**
 * Created by brandon3055 on 8/02/19.
 */
//TODO think of a better name for this
public enum EnumTechLevel {
    DRACONIUM(0),
    WYVERN(1),
    DRACONIC(2),
    CHAOTIC(3);

    public final int index;
    public static final EnumTechLevel[] VALUES = new EnumTechLevel[4];
    public static final EnumTechLevel[] TOOL_LEVELS = new EnumTechLevel[3];

    static {
        for (EnumTechLevel tier : values()) {
            VALUES[tier.index] = tier;
            if (tier != DRACONIUM) {
                VALUES[tier.index - 1] = tier;
            }
        }
    }

    EnumTechLevel(int index) {
        this.index = index;
    }

    /**
     * @return the name used in localization / resource names
     */
    public String localName() {
        return name().toLowerCase();
    }
}
