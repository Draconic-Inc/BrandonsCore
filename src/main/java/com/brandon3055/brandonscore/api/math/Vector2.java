package com.brandon3055.brandonscore.api.math;

import net.minecraft.nbt.CompoundTag;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

//TODO move to CCL as soon as CCL gets a Vector2
public class Vector2 {
    public double x;
    public double y;

    public Vector2() {
    }

    public Vector2(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Vector2(Vector2 vec) {
        x = vec.x;
        y = vec.y;
    }

    public static Vector2 fromNBT(CompoundTag tag) {
        return new Vector2(tag.getDouble("x"), tag.getDouble("y"));
    }

    public CompoundTag writeToNBT(CompoundTag tag) {
        tag.putDouble("x", x);
        tag.putDouble("y", y);
        return tag;
    }

    public double[] toArrayD() {
        return new double[]{x, y};
    }

    public float[] toArrayF() {
        return new float[]{(float) x, (float) y};
    }

    public Vector2 set(double x1, double y1) {
        x = x1;
        y = y1;
        return this;
    }

    public Vector2 set(double d) {
        return set(d, d);
    }

    public Vector2 set(Vector2 vec) {
        return set(vec.x, vec.y);
    }

    public Vector2 set(double[] da) {
        return set(da[0], da[1]);
    }

    public Vector2 set(float[] fa) {
        return set(fa[0], fa[1]);
    }

    public Vector2 add(double dx, double dy) {
        x += dx;
        y += dy;
        return this;
    }

    public Vector2 add(double d) {
        return add(d, d);
    }

    public Vector2 add(Vector2 vec) {
        return add(vec.x, vec.y);
    }

    public Vector2 subtract(double dx, double dy) {
        x -= dx;
        y -= dy;
        return this;
    }

    public Vector2 subtract(double d) {
        return subtract(d, d);
    }

    public Vector2 subtract(Vector2 vec) {
        return subtract(vec.x, vec.y);
    }

    public Vector2 multiply(double fx, double fy) {
        x *= fx;
        y *= fy;
        return this;
    }

    public Vector2 multiply(double f) {
        return multiply(f, f);
    }

    public Vector2 multiply(Vector2 f) {
        return multiply(f.x, f.y);
    }

    public Vector2 divide(double fx, double fy) {
        x /= fx;
        y /= fy;
        return this;
    }

    public Vector2 divide(double f) {
        return divide(f, f);
    }

    public Vector2 divide(Vector2 vec) {
        return divide(vec.x, vec.y);
    }

    public Vector2 floor() {
        x = Math.floor(x);
        y = Math.floor(y);
        return this;
    }

    public Vector2 ceil() {
        x = Math.ceil(x);
        y = Math.ceil(y);
        return this;
    }

    public double mag() {
        return Math.sqrt(x * x + y * y);
    }

    public double magSquared() {
        return x * x + y * y;
    }

    public Vector2 negate() {
        x = -x;
        y = -y;
        return this;
    }

    public Vector2 normalize() {
        double d = mag();
        if (d != 0) {
            multiply(1 / d);
        }
        return this;
    }

    public boolean isZero() {
        return x == 0 && y == 0;
    }

    @Override
    public int hashCode() {
        long j = Double.doubleToLongBits(x);
        int i = (int) (j ^ j >>> 32);
        j = Double.doubleToLongBits(y);
        i = 31 * i + (int) (j ^ j >>> 32);
        return i;
    }

    @Override
    public boolean equals(Object o) {
        if (super.equals(o)) {
            return true;
        }
        if (!(o instanceof Vector2)) {
            return false;
        }
        Vector2 v = (Vector2) o;
        return x == v.x && y == v.y;
    }

    public Vector2 copy() {
        return new Vector2(this);
    }

    @Override
    public String toString() {
        MathContext cont = new MathContext(4, RoundingMode.HALF_UP);
        return "Vector2(" + new BigDecimal(x, cont) + ", " + new BigDecimal(y, cont) + ")";
    }

    public double distanceSq(double toX, double toY) {
        double dx = x - toX;
        double dy = y - toY;
        return dx * dx + dy * dy;
    }

    public double distanceSq(Vector2 other) {
        double dx = x - other.x;
        double dy = y - other.y;
        return dx * dx + dy * dy;
    }

    public double distance(Vector2 other) {
        return Math.sqrt(distanceSq(other));
    }

    public double distance(double toX, double toY) {
        return Math.sqrt(distanceSq(toX, toY));
    }
}
