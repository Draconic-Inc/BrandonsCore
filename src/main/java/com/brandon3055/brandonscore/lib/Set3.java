package com.brandon3055.brandonscore.lib;

/**
 * Created by brandon3055 on 19/4/2016.
 *
 */
public class Set3<A, B, C> {
    private A a;
    private B b;
    private C c;

    public Set3(A x, B y, C z) {
        this.a = x;
        this.b = y;
        this.c = z;
    }

    public A getA() {
        return a;
    }

    public B getB() {
        return b;
    }

    public C getC() {
        return c;
    }

    public void setA(A x) {
        this.a = x;
    }

    public void setB(B y) {
        this.b = y;
    }

    public void setC(C z) {
        this.c = z;
    }

    @Override
    public int hashCode() {
        return (b.hashCode() + c.hashCode() * 31) * 31 + a.hashCode();
    }

    public boolean equals(Object var1) {
        if (this == var1) {
            return true;
        } else if (!(var1 instanceof Set3)) {
            return false;
        } else {
            Set3 var2 = (Set3) var1;
            if (this.a != null) {
                if (!this.a.equals(var2.a)) {
                    return false;
                }
            } else if (var2.a != null) {
                return false;
            }

            if (this.b != null) {
                if (!this.b.equals(var2.b)) {
                    return false;
                }
            } else if (var2.b != null) {
                return false;
            }

            if (this.c != null) {
                if (!this.c.equals(var2.c)) {
                    return false;
                }
            } else if (var2.c != null) {
                return false;
            }

            return true;
        }
    }
}
