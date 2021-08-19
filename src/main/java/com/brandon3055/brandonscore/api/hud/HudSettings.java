//package com.brandon3055.brandonscore.api.hud;
//
//import com.brandon3055.brandonscore.api.math.Vector2;
//
///**
// * Created by brandon3055 on 1/8/21
// * These are the user modifiable settings for a single {@link AbstractHudElement} implementation.
// */
//@Deprecated //New idea!
//public class HudSettings {
//
//    private Anchor horizontalAnchor = Anchor.NONE;
//    private Anchor verticalAnchor = Anchor.NONE;
//    private Vector2 pos = new Vector2();
//    private double scale = 1;
//    private boolean rotated = false;
//    private double fade = 1; //Fade out to this alpha
//
//    public void setHorizontalAnchor(Anchor horizontalAnchor) {
//        this.horizontalAnchor = horizontalAnchor;
//    }
//
//    public Anchor getHorizontalAnchor() {
//        return horizontalAnchor;
//    }
//
//    public void setVerticalAnchor(Anchor verticalAnchor) {
//        this.verticalAnchor = verticalAnchor;
//    }
//
//    public Anchor getVerticalAnchor() {
//        return verticalAnchor;
//    }
//
//    public void setPos(Vector2 pos) {
//        this.pos = pos;
//        onChange();
//    }
//
//    public Vector2 getPos() {
//        return pos;
//    }
//
//    public void setScale(double scale) {
//        this.scale = scale;
//        onChange();
//    }
//
//    public double getScale() {
//        return scale;
//    }
//
//    public void setRotated(boolean rotated) {
//        this.rotated = rotated;
//        onChange();
//    }
//
//    public boolean isRotated() {
//        return rotated;
//    }
//
//    public void setFade(double fade) {
//        this.fade = fade;
//        onChange();
//    }
//
//    public double getFade() {
//        return fade;
//    }
//
//    //########### Internal ###########
//
//    //Transient so Gson won't try to serialize it
//    private transient Runnable changeListener;
//
//    /**
//     * This is for internal use by the HudManager in BrandosCore. This gives the handler a way to detect when changes are made (usually by the user)
//     * and save the new settings to disk
//     */
//    public void setChangeListener(Runnable changeListener) {
//        this.changeListener = changeListener;
//    }
//
//    private void onChange() {
//        if (changeListener != null) {
//            changeListener.run();
//        }
//    }
//
//    public enum Anchor {
//        MIN, //Left / Top
//        NONE,
//        MAX  //Right / Bottom
//    }
//}
