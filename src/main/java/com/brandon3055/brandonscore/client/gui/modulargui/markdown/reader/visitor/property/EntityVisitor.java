package com.brandon3055.brandonscore.client.gui.modulargui.markdown.reader.visitor.property;

import java.util.List;

/**
 * Created by brandon3055 on 5/30/2018.
 */
public abstract class EntityVisitor extends PropertyVisitor {

    @Override
    public abstract void visitSize(int size, boolean screenRelative);

    public abstract void visitXOffset(int xOffset);

    public abstract void visitYOffset(int yOffset);

    public abstract void visitRotateSpeed(double rotateSpeed);

    public abstract void visitRotation(double rotation);

    public abstract void visitScale(double scale);

    @Override
    public abstract void visitTooltip(List<String> tooltip);

    public abstract void visitTrackMouse(boolean trackMouse);

    public abstract void visitDrawName(boolean drawName);

    public abstract void visitAnimate(boolean animate);

    public abstract void visitMainHand(String mainHand);

    public abstract void visitOffHand(String offhand);

    public abstract void visitHead(String head);

    public abstract void visitChest(String chest);

    public abstract void visitLegs(String legs);

    public abstract void visitBoots(String boots);


}
