package com.brandon3055.brandonscore.client.gui.modulargui.markdown.visitorimpl;

import com.brandon3055.brandonscore.client.gui.modulargui.markdown.mdelements.RecipeElement;
import com.brandon3055.brandonscore.client.gui.modulargui.markdown.reader.visitor.property.RecipeVisitor;

/**
 * Created by brandon3055 on 5/30/2018.
 */
public class RecipeVisitorImpl extends RecipeVisitor {

    private RecipeElement element;

    public RecipeVisitorImpl(RecipeElement element) {
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
    public void visitSpacing(int spacing) {
        element.spacing = spacing;
    }

    @Override
    public void endVisit() {
        element.invalidProps.putAll(invalidCalls);
    }
}
