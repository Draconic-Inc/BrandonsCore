package com.brandon3055.brandonscore.lib;

/**
 * Created by brandon3055 on 19/4/2016.
 *
 */
public class Set3<X, Y, Z> {
    private X x;
    private Y y;
    private Z z;

    public Set3(X x, Y y, Z z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public X getX() {
        return x;
    }

    public Y getY() {
        return y;
    }

    public Z getZ() {
        return z;
    }

    public void setX(X x) {
        this.x = x;
    }

    public void setY(Y y) {
        this.y = y;
    }

    public void setZ(Z z) {
        this.z = z;
    }

    @Override
    public int hashCode() {
        return (y.hashCode() + z.hashCode() * 31) * 31 + x.hashCode();
    }

    public boolean equals(Object var1) {
        if (this == var1) {
            return true;
        } else if (!(var1 instanceof Set3)) {
            return false;
        } else {
            Set3 var2 = (Set3) var1;
            if (this.x != null) {
                if (!this.x.equals(var2.x)) {
                    return false;
                }
            } else if (var2.x != null) {
                return false;
            }

            if (this.y != null) {
                if (!this.y.equals(var2.y)) {
                    return false;
                }
            } else if (var2.y != null) {
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
