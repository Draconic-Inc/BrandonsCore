package com.brandon3055.brandonscore.api.power;

/**
 * Created by brandon3055 on 8/02/19.
 */
public enum PowerTier {
    TIER_1(0, 1),
    TIER_2(1, 10),
    TIER_3(2, 1000),
    TIER_4(3, 1000000);

    // |===================|
    // | Energy: 32,000 OP |
    // | Density: Chaotic  |
    // |===================|

    public final int index;
    public final int energyDensity;
    public static final PowerTier[] VALUES = new PowerTier[4];

    static {
        for (PowerTier tier : values()) {
            VALUES[tier.index] = tier;
        }
    }

    PowerTier(int index, int energyDensity) {
        this.index = index;
        this.energyDensity = energyDensity;
    }
}
