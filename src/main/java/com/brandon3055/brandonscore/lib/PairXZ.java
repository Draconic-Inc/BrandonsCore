package com.brandon3055.brandonscore.lib;

/**
 * Created by brandon3055 on 19/4/2016.
 * Just a generic pair class.
 */
public class PairXZ<K, V> {
    public K x;
    public V z;

    public PairXZ(K x, V z) {
        this.x = x;
        this.z = z;
    }

    @Override
    public int hashCode() {
        return this.x.hashCode() * 13 + (this.z == null ? 0 : this.z.hashCode());
    }

    public boolean equals(Object var1) {
        if (this == var1) {
            return true;
        } else if (!(var1 instanceof PairXZ)) {
            return false;
        } else {
            PairXZ var2 = (PairXZ) var1;
            if (this.x != null) {
                if (!this.x.equals(var2.x)) {
                    return false;
                }
            } else if (var2.x != null) {
                return false;
            }

            if (this.z != null) {
                if (!this.z.equals(var2.z)) {
                    return false;
                }
            } else if (var2.z != null) {
                return false;
            }

            return true;
        }
    }
}
