package com.brandon3055.brandonscore.client.gui.modulargui.oldelements;

import com.brandon3055.brandonscore.client.gui.modulargui.MGuiElementBase;
import com.brandon3055.brandonscore.client.gui.modulargui.baseelements.GuiLabel;
import com.brandon3055.brandonscore.client.gui.modulargui.lib.GuiEvent;
import com.brandon3055.brandonscore.client.gui.modulargui.lib.IGuiEventListener;
import net.minecraft.client.Minecraft;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by brandon3055 on 10/09/2016.
 */
//TODO make this more dynamic. Currently there is no way to remove or modify the background
//Also the extra width assigned for the scroll bar is added even if there is no scroll bar
public class MGuiSelectDialog extends MGuiList {

    public IGuiEventListener listener;
    public GuiLabel label;
    public MGuiElementBase selected = null;
    private List<MGuiElementBase> options = new ArrayList<MGuiElementBase>();

    public MGuiSelectDialog() {
    }

    public MGuiSelectDialog(int xPos, int yPos) {
        super(xPos, yPos);
    }

    public MGuiSelectDialog(int xPos, int yPos, int xSize, int ySize) {
        super(xPos, yPos, xSize, ySize);
    }

    @Override
    public void addChildElements() {
        super.addChildElements();
        if (scrollBar != null) {
            scrollBar.parentScrollable = this;
        }
    }

    @Override
    public void renderElement(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
        drawBorderedRect(xPos(), yPos(), xSize(), ySize(), 1, 0xFF707070, 0xFF000000);
        super.renderElement(minecraft, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (isMouseOver(mouseX, mouseY)) {
            for (MGuiElementBase option : options) {
                if (option.isMouseOver(mouseX, mouseY)) {
                    if (listener != null) {
                        listener.onMGuiEvent(new GuiEvent.SelectEvent(this, option), this);
                    }
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    public MGuiSelectDialog setListener(IGuiEventListener listener) {
        this.listener = listener;
        return this;
    }

    public MGuiSelectDialog setOptions(List<MGuiElementBase> options) {
        return setOptions(options, false);
    }

    public MGuiSelectDialog setOptions(List<MGuiElementBase> options, boolean lockXPos) {
        this.options = options;
        childElements.clear();
        setXSize(10);

        for (MGuiElementBase option : options) {
            if (lockXPos) {
                int offset = option.xPos() - xPos();
                if (option.xSize() + offset > xSize() - 11) {
                    setXSize(option.xSize() + 11 + offset);
                }
            }
            else {
                if (option.xSize() > xSize() - 12) {
                    setXSize(option.xSize() + 12);
                }
            }

            MGuiListEntryWrapper wrapper = new MGuiListEntryWrapper(option);
            wrapper.setLockXPos(lockXPos);
            addEntry(wrapper);
        }

        initScrollBar();
        scrollBar.parentScrollable = this;
        scrollBar.translate(-1, 0);
        return this;
    }

    public void setLabel(GuiLabel label) {
        this.label = label;
    }
}
