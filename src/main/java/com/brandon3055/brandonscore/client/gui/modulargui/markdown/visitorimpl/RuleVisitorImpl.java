package com.brandon3055.brandonscore.client.gui.modulargui.markdown.visitorimpl;

import com.brandon3055.brandonscore.client.gui.modulargui.markdown.mdelements.RuleElement;
import com.brandon3055.brandonscore.client.gui.modulargui.markdown.reader.visitor.property.RuleVisitor;

/**
 * Created by brandon3055 on 5/30/2018.
 */
public class RuleVisitorImpl extends RuleVisitor {

    private RuleElement element;

    public RuleVisitorImpl(RuleElement element) {
        this.element = element;
    }

    @Override
    public void visitColour(int argb) {
        element.setColour(argb);
    }

    @Override
    public void visitBorderColour(int argb) {
        element.setColourBorder(argb);
    }

    @Override
    public void visitHeight(int height) {
        element.height = height;
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
    public void visitLeftPad(int leftPadding) {
        element.leftPad = leftPadding;
    }

    @Override
    public void visitRightPad(int rightPadding) {
        element.rightPad = rightPadding;
    }

    @Override
    public void visitPadding(int padding) {
        element.leftPad = element.rightPad = element.topPad = element.bottomPad = padding;
    }

    @Override
    public void visitWidth(int width, boolean screenRelative) {
        element.width = width;
        element.screenRelativeSize= screenRelative;
    }

    @Override
    public void endVisit() {
        element.invalidProps.addAll(invalidCalls.keySet());
    }
}
