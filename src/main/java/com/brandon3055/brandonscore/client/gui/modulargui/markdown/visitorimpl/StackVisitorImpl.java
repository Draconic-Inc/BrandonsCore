package com.brandon3055.brandonscore.client.gui.modulargui.markdown.visitorimpl;

import com.brandon3055.brandonscore.client.gui.modulargui.markdown.mdelements.StackElement;
import com.brandon3055.brandonscore.client.gui.modulargui.markdown.reader.visitor.property.StackVisitor;

import java.util.List;

/**
 * Created by brandon3055 on 5/30/2018.
 */
public class StackVisitorImpl extends StackVisitor {

    private StackElement element;

    public StackVisitorImpl(StackElement element) {
        this.element = element;
    }

    @Override
    public void visitSize(int size, boolean screenRelative) {
        element.size = size;
        element.screenRelativeSize = screenRelative;
    }

    @Override
    public void visitDrawSlot(boolean drawSlot) {
        element.drawSlot = drawSlot;
    }

    @Override
    public void visitEnabledTooltip(boolean enableTooltip) {
        element.enableTooltip = enableTooltip;
    }

    @Override
    public void visitTooltip(List<String> tooltip) {
        element.tooltip = tooltip;
    }

    @Override
    public void endVisit() {
        element.invalidProps.putAll(invalidCalls);
    }
}
