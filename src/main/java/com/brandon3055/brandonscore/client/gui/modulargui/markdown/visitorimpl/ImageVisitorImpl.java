package com.brandon3055.brandonscore.client.gui.modulargui.markdown.visitorimpl;

import com.brandon3055.brandonscore.client.gui.modulargui.markdown.mdelements.ImageElement;
import com.brandon3055.brandonscore.client.gui.modulargui.markdown.reader.visitor.property.ImageVisitor;

import java.util.List;

/**
 * Created by brandon3055 on 5/30/2018.
 */
public class ImageVisitorImpl extends ImageVisitor {

    private ImageElement element;

    public ImageVisitorImpl(ImageElement element) {
        this.element = element;
    }

    @Override
    public void visitBorderColour(int argb) {
        element.setColourBorder(argb);
    }

    @Override
    public void visitBorderColourHover(int argb) {
        element.setColourBorderHover(argb);
    }

    @Override
    public void visitLeftPad(int leftPadding) {
        element.leftPad = leftPadding;
    }

    @Override
    public void visitRightPad(int rightPadding) {
        element.rightPad = rightPadding;
    }

    @Override
    public void visitTopPad(int topPadding) {
        element.topPad = topPadding;
    }

    @Override
    public void visitBottomPad(int bottomPadding) {
        element.bottomPad = bottomPadding;
    }

    @Override
    public void visitWidth(int width, boolean screenRelative) {
        element.width = width;
        element.screenRelativeSize = screenRelative;
    }

    @Override
    public void visitHeight(int height) {
        element.height = height;
    }

    @Override
    public void visitTooltip(List<String> tooltip) {
        element.tooltip = tooltip;
    }

    @Override
    public void visitLinkTo(String linkTo) {
        element.linkTo = linkTo;
    }

    @Override
    public void endVisit() {
        element.invalidProps.addAll(invalidCalls.keySet());
    }
}
