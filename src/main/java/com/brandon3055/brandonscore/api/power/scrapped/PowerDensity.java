//package com.brandon3055.brandonscore.api.power;
//
///**
// * Created by brandon3055 on 8/02/19.
// */
//public enum PowerDensity {
//    Dx1(0, 1),
//    Dx10(1, 10),
//    Dx1000(2, 1000),
//    Dx1000000(3, 1000000);
//
//    // |===================|
//    // | Energy: 32,000 OP |
//    // | Density: Chaotic  |
//    // |===================|
//
//    public final int index;
//    public final int energyDensity;
//    public static final PowerDensity[] VALUES = new PowerDensity[4];
//
//    static {
//        for (PowerDensity tier : values()) {
//            VALUES[tier.index] = tier;
//        }
//    }
//
//    PowerDensity(int index, int energyDensity) {
//        this.index = index;
//        this.energyDensity = energyDensity;
//    }
//}
