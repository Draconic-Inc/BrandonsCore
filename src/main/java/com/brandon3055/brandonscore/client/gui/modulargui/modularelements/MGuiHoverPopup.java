package com.brandon3055.brandonscore.client.gui.modulargui.modularelements;

import com.brandon3055.brandonscore.client.gui.modulargui.IModularGui;
import com.brandon3055.brandonscore.client.gui.modulargui.MGuiElementBase;
import net.minecraft.client.Minecraft;

import java.util.Arrays;
import java.util.List;

/**
 * Created by brandon3055 on 30/08/2016.
 * Can be added as a child to almost any other element to give it hover text
 */
public class MGuiHoverPopup extends MGuiElementBase {

    public List<String> hoverText = null;
    public int hoverDelay = 10;
    public int hoverTime;
    public boolean mouseOver = false;
//    public MGuiElementBase parent;

    public MGuiHoverPopup(IModularGui modularGui) {
        super(modularGui);
    }

    public MGuiHoverPopup(IModularGui modularGui, MGuiElementBase parent) {
        super(modularGui);
        this.parent = parent;
    }

    public MGuiHoverPopup(IModularGui gui, String[] toolTip) {
        super(gui);
        this.hoverText = Arrays.asList(toolTip);
    }

    public MGuiHoverPopup(IModularGui gui, List<String> toolTip) {
        super(gui);
        this.hoverText = toolTip;
    }

    public MGuiHoverPopup(IModularGui gui, String[] toolTip, MGuiElementBase parent) {
        super(gui);
        this.hoverText = Arrays.asList(toolTip);
        this.parent = parent;
    }

    public MGuiHoverPopup(IModularGui gui, List<String> toolTip, MGuiElementBase parent) {
        super(gui);
        this.hoverText = toolTip;
        this.parent = parent;
    }

    public MGuiHoverPopup setParent(MGuiElementBase parent) {
        this.parent = parent;
        return this;
    }

    @Override
    public boolean renderOverlayLayer(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
        mouseOver = parent != null && parent.isMouseOver(mouseX, mouseY);

        List<String> hoverText = getToolTip();
        if (hoverTime >= hoverDelay && hoverText != null && !hoverText.isEmpty()) {
            drawHoveringText(hoverText, mouseX, mouseY, minecraft.fontRendererObj, modularGui.screenWidth(), modularGui.screenHeight());
            return true;
        }

        return super.renderOverlayLayer(minecraft, mouseX, mouseY, partialTicks);
    }

    public MGuiHoverPopup setHoverText(String[] toolTip) {
        this.hoverText = Arrays.asList(toolTip);
        return this;
    }

    public MGuiHoverPopup setHoverText(List<String> toolTip) {
        this.hoverText = toolTip;
        return this;
    }

    public List<String> getToolTip() {
        return hoverText;
    }

    public MGuiHoverPopup setHoverDelay(int hoverDelay) {
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
