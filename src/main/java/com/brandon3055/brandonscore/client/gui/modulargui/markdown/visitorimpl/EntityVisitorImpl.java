package com.brandon3055.brandonscore.client.gui.modulargui.markdown.visitorimpl;

import com.brandon3055.brandonscore.client.gui.modulargui.markdown.mdelements.EntityElement;
import com.brandon3055.brandonscore.client.gui.modulargui.markdown.reader.visitor.property.EntityVisitor;

import java.util.List;

/**
 * Created by brandon3055 on 5/30/2018.
 */
public class EntityVisitorImpl extends EntityVisitor {

    private EntityElement element;

    public EntityVisitorImpl(EntityElement element) {
        this.element = element;
    }

    @Override
    public void visitSize(int size, boolean screenRelative) {
        element.size = size;
        element.screenRelativeSize = screenRelative;
    }

    @Override
    public void visitXOffset(int xOffset) {
        element.xOffset = xOffset;
    }

    @Override
    public void visitYOffset(int yOffset) {
        element.yOffset = yOffset;
    }

    @Override
    public void visitRotateSpeed(double rotateSpeed) {
        element.rotateSpeed = rotateSpeed;
    }

    @Override
    public void visitRotation(double rotation) {
        element.rotation = rotation;
    }

    @Override
    public void visitScale(double scale) {
        element.drawScale = scale;
    }

    @Override
    public void visitTooltip(List<String> tooltip) {
        element.tooltip = tooltip;
    }

    @Override
    public void visitTrackMouse(boolean trackMouse) {
        element.trackMouse = trackMouse;
    }

    @Override
    public void visitDrawName(boolean drawName) {
        element.drawName = drawName;
    }

    @Override
    public void visitAnimate(boolean animate) {
        element.animate = animate;
    }

    @Override
    public void visitMainHand(String mainHand) {
        element.mainHand = mainHand;
    }

    @Override
    public void visitOffHand(String offhand) {
        element.offHand = offhand;
    }

    @Override
    public void visitHead(String head) {
        element.head = head;
    }

    @Override
    public void visitChest(String chest) {
        element.chest = chest;
    }

    @Override
    public void visitLegs(String legs) {
        element.legs = legs;
    }

    @Override
    public void visitBoots(String boots) {
        element.boots = boots;
    }

    @Override
    public void endVisit() {
        element.invalidProps.addAll(invalidCalls.keySet());
    }
}
