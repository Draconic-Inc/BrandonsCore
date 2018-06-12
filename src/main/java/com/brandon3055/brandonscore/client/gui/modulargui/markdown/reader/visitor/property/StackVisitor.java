package com.brandon3055.brandonscore.client.gui.modulargui.markdown.reader.visitor.property;

import java.util.List;

/**
 * Created by brandon3055 on 5/30/2018.
 */
public abstract class StackVisitor extends PropertyVisitor {

    public StackVisitor() {

    }

    @Override
    public abstract void visitSize(int size, boolean screenRelative);

    public abstract void visitDrawSlot(boolean drawSlot);

    @Override
    public abstract void visitEnabledTooltip(boolean enableTooltip);

    @Override
    public abstract void visitTooltip(List<String> tooltip);
}
