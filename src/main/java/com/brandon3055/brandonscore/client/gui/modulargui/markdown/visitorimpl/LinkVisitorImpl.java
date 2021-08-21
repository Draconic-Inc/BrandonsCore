package com.brandon3055.brandonscore.client.gui.modulargui.markdown.visitorimpl;

import com.brandon3055.brandonscore.client.gui.modulargui.markdown.mdelements.LinkElement;
import com.brandon3055.brandonscore.client.gui.modulargui.markdown.reader.visitor.property.LinkVisitor;

import java.util.List;

import static com.brandon3055.brandonscore.client.gui.modulargui.markdown.mdelements.LinkElement.Style.SOLID;
import static com.brandon3055.brandonscore.client.gui.modulargui.markdown.mdelements.LinkElement.Style.TEXT;
import static com.brandon3055.brandonscore.client.gui.modulargui.markdown.mdelements.LinkElement.Style.VANILLA;

/**
 * Created by brandon3055 on 5/30/2018.
 */
public class LinkVisitorImpl extends LinkVisitor {

    private LinkElement element;

    public LinkVisitorImpl(LinkElement element) {
        this.element = element;
    }

    @Override
    public void visitColour(int argb) {
        element.setColour(argb);
    }

    @Override
    public void visitColourHover(int argb) {
        element.setColourHover(argb);
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
    public void visitLinkStyle(String linkStyle) {
        linkStyle = linkStyle.toLowerCase(Locale.ENGLISH);
        element.linkStyle = linkStyle.equals("vanilla") ? VANILLA : linkStyle.equals("solid") ? SOLID : TEXT;
    }

    @Override
    public void visitAltText(String altText) {
        element.altText = altText;
    }

    @Override
    public void visitTooltip(List<String> tooltip) {
        element.tooltip = tooltip;
    }

    @Override
    public void endVisit() {
        element.invalidProps.addAll(invalidCalls.keySet());
    }
}
