package com.brandon3055.brandonscore.client.gui.modulargui.markdown.reader.visitor.property;

/**
 * Created by brandon3055 on 5/30/2018.
 */
public abstract class RecipeVisitor extends PropertyVisitor {

    @Override
    public abstract void visitBorderColour(int argb);

    @Override
    public abstract void visitBorderColourHover(int argb);

    @Override
    public abstract void visitLeftPad(int leftPadding);

    @Override
    public abstract void visitRightPad(int rightPadding);

    @Override
    public abstract void visitTopPad(int topPadding);

    @Override
    public abstract void visitBottomPad(int bottomPadding);

    public abstract void visitSpacing(int spacing);
}