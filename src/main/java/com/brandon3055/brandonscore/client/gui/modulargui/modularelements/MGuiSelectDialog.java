package com.brandon3055.brandonscore.client.gui.modulargui.modularelements;

import com.brandon3055.brandonscore.client.gui.modulargui.IModularGui;
import com.brandon3055.brandonscore.client.gui.modulargui.MGuiElementBase;
import com.brandon3055.brandonscore.client.gui.modulargui.lib.IMGuiListener;
import net.minecraft.client.Minecraft;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by brandon3055 on 10/09/2016.
 */
public class MGuiSelectDialog extends MGuiList {

    public IMGuiListener listener;
    public MGuiLabel label;
    public MGuiElementBase selected = null;
    private List<MGuiElementBase> options = new ArrayList<MGuiElementBase>();

    public MGuiSelectDialog(IModularGui modularGui) {
        super(modularGui);
    }

    public MGuiSelectDialog(IModularGui modularGui, int xPos, int yPos) {
        super(modularGui, xPos, yPos);
    }

    public MGuiSelectDialog(IModularGui modularGui, int xPos, int yPos, int xSize, int ySize) {
        super(modularGui, xPos, yPos, xSize, ySize);
    }

    @Override
    public void initElement() {
        super.initElement();
        if (scrollBar != null) {
            scrollBar.parentScrollable = this;
        }
    }

    @Override
    public void renderBackgroundLayer(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
        drawBorderedRect(xPos, yPos, xSize, ySize, 1, 0xFF707070, 0xFF000000);
        super.renderBackgroundLayer(minecraft, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (isMouseOver(mouseX, mouseY)) {
            for (MGuiElementBase option : options) {
                if (option.isMouseOver(mouseX, mouseY)) {
                    if (listener != null) {
                        listener.onMGuiEvent("SELECTOR_PICK", option);
                    }
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    public MGuiSelectDialog setListener(IMGuiListener listener) {
        this.listener = listener;
        return this;
    }

    public MGuiSelectDialog setOptions(List<MGuiElementBase> options) {
        return setOptions(options, false);
    }

    public MGuiSelectDialog setOptions(List<MGuiElementBase> options, boolean lockXPos) {
        this.options = options;
        childElements.clear();
        xSize = 10;

        for (MGuiElementBase option : options) {
            if (lockXPos) {
                int offset = option.xPos - xPos;
                if (option.xSize + offset > xSize - 11) {
                    xSize = option.xSize + 11 + offset;
                }
            }
            else {
                if (option.xSize > xSize - 12) {
                    xSize = option.xSize + 12;
                }
            }

            MGuiListEntryWrapper wrapper = new MGuiListEntryWrapper(modularGui, option);
            wrapper.setLockXPos(lockXPos);
            addEntry(wrapper);
        }

        initScrollBar();
        scrollBar.parentScrollable = this;
        scrollBar.xPos -= 1;
        return this;
    }

    public void setLabel(MGuiLabel label) {
        this.label = label;
    }
}
