package com.brandon3055.brandonscore.client.gui.modulargui.oldelements;

import com.brandon3055.brandonscore.client.gui.modulargui.MGuiElementBase;
import net.minecraft.client.Minecraft;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

/**
 * Created by brandon3055 on 30/08/2016.
 * Can be added as a child to almost any other element to give it hover text
 */
@Deprecated //This is now built into MGuiElementBase
public class MGuiHoverText extends MGuiElementBase<MGuiHoverText> {

    public List<String> hoverText = null;
    public int hoverDelay = 10;
    public int hoverTime;
    public boolean mouseOver = false;
    private Supplier toolTipSupplier;
//    public MGuiElementBase parent;

    public MGuiHoverText() {}

    public MGuiHoverText(MGuiElementBase parent) {
        this.setParent(parent);
    }

    public MGuiHoverText(String[] toolTip) {
        this.hoverText = Arrays.asList(toolTip);
    }

    public MGuiHoverText(List<String> toolTip) {
        this.hoverText = toolTip;
    }

    public MGuiHoverText(String[] toolTip, MGuiElementBase parent) {
        this.hoverText = Arrays.asList(toolTip);
        this.setParent(parent);
    }

    public MGuiHoverText(List<String> toolTip, MGuiElementBase parent) {
        this.hoverText = toolTip;
        this.setParent(parent);

    }

    @Override
    public boolean renderOverlayLayer(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
        mouseOver = getParent() != null && getParent().isMouseOver(mouseX, mouseY);

        List<String> hoverText = getToolTip();
        if (mouseOver && hoverTime >= hoverDelay && hoverText != null && !hoverText.isEmpty()) {
            drawHoveringText(hoverText, mouseX, mouseY, minecraft.fontRendererObj, screenWidth, screenHeight);
            return true;
        }

        return super.renderOverlayLayer(minecraft, mouseX, mouseY, partialTicks);
    }

    public MGuiHoverText setHoverText(String[] toolTip) {
        this.hoverText = Arrays.asList(toolTip);
        return this;
    }

    public MGuiHoverText setHoverText(List<String> toolTip) {
        this.hoverText = toolTip;
        return this;
    }

    /**
     * Allows you to set a supplier that will override the default tool tip.
     */
    public MGuiHoverText setToolTipArraySupplier(Supplier<String[]> supplier) {
        this.toolTipSupplier = supplier;
        return this;
    }

    /**
     * Allows you to set a supplier that will override the default tool tip.
     */
    public MGuiHoverText setToolTipListSupplier(Supplier<List<String>> supplier) {
        this.toolTipSupplier = supplier;
        return this;
    }

    /**
     * Allows you to set a supplier that will override the default tool tip.
     */
    public MGuiHoverText setToolTipSupplier(Supplier<String> supplier) {
        this.toolTipSupplier = supplier;
        return this;
    }

    public List<String> getToolTip() {
        if (toolTipSupplier != null) {
            Object tt = toolTipSupplier.get();
            if (tt instanceof String) {
                return Collections.singletonList((String) tt);
            }
            else if (tt instanceof String[]) {
                return Arrays.asList((String[]) tt);
            }
            else if (tt instanceof List) {
                return (List<String>) tt;
            }
        }
        return hoverText;
    }

    public MGuiHoverText setHoverDelay(int hoverDelay) {
        this.hoverDelay = hoverDelay;
        return this;
    }

    @Override
    public boolean onUpdate() {
        if (mouseOver) {
            hoverTime++;
        }
        else {
            hoverTime = 0;
        }

        return super.onUpdate();
    }
}
