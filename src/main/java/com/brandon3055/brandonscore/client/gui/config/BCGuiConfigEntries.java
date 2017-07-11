/*
 * Minecraft Forge
 * Copyright (c) 2016.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation version 2.1
 * of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package com.brandon3055.brandonscore.client.gui.config;

import com.brandon3055.brandonscore.registry.ModConfigParser;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.config.*;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import org.lwjgl.input.Keyboard;

import java.util.*;

import static net.minecraftforge.fml.client.config.GuiUtils.RESET_CHAR;
import static net.minecraftforge.fml.client.config.GuiUtils.UNDO_CHAR;

/**
 * This class implements the scrolling list functionality of the config GUI screens. It also provides all the default control handlers
 * for the various property types.
 *
 * <p>
 * This class is copied from forge credit for the creation of this class goes to its original author.
 * I could not use the version of this class in forge because it does not support disabling properties in the way i require.
 *
 * @author bspkrs
 */
public class BCGuiConfigEntries extends GuiConfigEntries {
    public BCGuiConfigEntries(GuiConfig parent, Minecraft mc) {
        super(parent, mc);
        this.setShowSelectionBox(false);
        this.listEntries = new ArrayList<>();

        for (IConfigElement configElement : parent.configElements) {
            if (configElement != null) {
                if (configElement.isProperty() && configElement.showInGui()) // as opposed to being a child category entry
                {
                    int length;

                    // protects against language keys that are not defined in the .lang file
                    if (!I18n.format(configElement.getLanguageKey()).equals(configElement.getLanguageKey())) {
                        length = mc.fontRenderer.getStringWidth(I18n.format(configElement.getLanguageKey()));
                    }
                    else {
                        length = mc.fontRenderer.getStringWidth(configElement.getName());
                    }

                    if (length > this.maxLabelTextWidth) {
                        this.maxLabelTextWidth = length;
                    }
                }
            }
        }

        int viewWidth = this.maxLabelTextWidth + 8 + (width / 2);
        labelX = (this.width / 2) - (viewWidth / 2);
        controlX = labelX + maxLabelTextWidth + 8;
        resetX = (this.width / 2) + (viewWidth / 2) - 45;
        controlWidth = resetX - controlX - 5;
        scrollBarX = this.width;

        for (IConfigElement configElement : parent.configElements) {
            if (configElement != null && configElement.showInGui()) {
                Property prop = configElement instanceof BCModConfigGui.BCConfigElement ? ((BCModConfigGui.BCConfigElement) configElement).getProp() : null;
                if (configElement.getConfigEntryClass() != null)
                    try
                    {
                       this.listEntries.add(configElement.getConfigEntryClass().getConstructor(GuiConfig.class, BCGuiConfigEntries.class, IConfigElement.class).newInstance(this.owningScreen, this, configElement));
                    }
                    catch (Throwable e)
                    {
                        FMLLog.severe("There was a critical error instantiating the custom IConfigEntry for config element %s.", configElement.getName());
                        e.printStackTrace();
                    }
                else if (configElement.isProperty())
                {
                    if (configElement.isList()) {
                        this.listEntries.add(new BCGuiConfigEntries.ArrayEntry(this.owningScreen, this, configElement, prop));
                    }
                    else if (configElement.getType() == ConfigGuiType.BOOLEAN) {
                        this.listEntries.add(new BCGuiConfigEntries.BooleanEntry(this.owningScreen, this, configElement, prop));
                    }
                    else if (configElement.getType() == ConfigGuiType.INTEGER) {
                        this.listEntries.add(new BCGuiConfigEntries.IntegerEntry(this.owningScreen, this, configElement, prop));
                    }
                    else if (configElement.getType() == ConfigGuiType.DOUBLE) {
                        this.listEntries.add(new BCGuiConfigEntries.DoubleEntry(this.owningScreen, this, configElement, prop));
                    }
                    else if (configElement.getType() == ConfigGuiType.COLOR)
                    {
                        if (configElement.getValidValues() != null && configElement.getValidValues().length > 0) {
                            this.listEntries.add(new BCGuiConfigEntries.ChatColorEntry(this.owningScreen, this, configElement, prop));
                        }
                        else {
                            this.listEntries.add(new BCGuiConfigEntries.StringEntry(this.owningScreen, this, configElement, prop));
                        }
                    }
                    else if (configElement.getType() == ConfigGuiType.MOD_ID)
                    {
                        Map<Object, String> values = new TreeMap<>();
                        for (ModContainer mod : Loader.instance().getActiveModList()) {
                            values.put(mod.getModId(), mod.getName());
                        }
                        values.put("minecraft", "Minecraft");
                        this.listEntries.add(new SelectValueEntry(this.owningScreen, this, configElement, values, prop));
                    }
                    else if (configElement.getType() == ConfigGuiType.STRING)
                    {
                        if (configElement.getValidValues() != null && configElement.getValidValues().length > 0) {
                            this.listEntries.add(new BCGuiConfigEntries.CycleValueEntry(this.owningScreen, this, configElement, prop));
                        }
                        else {
                            this.listEntries.add(new BCGuiConfigEntries.StringEntry(this.owningScreen, this, configElement, prop));
                        }
                    }
                }
                else if (configElement.getType() == ConfigGuiType.CONFIG_CATEGORY){
                    this.listEntries.add(new CategoryEntry(this.owningScreen, this, configElement, prop));
                }
            }
        }
    }

    public void initOverride() {
        initGui();
    }

    protected void initGui() {
        this.width = owningScreen.width;
        this.height = owningScreen.height;

        this.maxLabelTextWidth = 0;
        for (IConfigEntry entry : this.listEntries)
            if (entry.getLabelWidth() > this.maxLabelTextWidth) this.maxLabelTextWidth = entry.getLabelWidth();

        this.top = owningScreen.titleLine2 != null ? 33 : 23;
        this.bottom = owningScreen.height - 32;
        this.left = 0;
        this.right = width;
        int viewWidth = this.maxLabelTextWidth + 8 + (width / 2);
        labelX = (this.width / 2) - (viewWidth / 2);
        controlX = labelX + maxLabelTextWidth + 8;
        resetX = (this.width / 2) + (viewWidth / 2) - 45;

        this.maxEntryRightBound = 0;
        for (IConfigEntry entry : this.listEntries)
            if (entry.getEntryRightBound() > this.maxEntryRightBound) this.maxEntryRightBound = entry.getEntryRightBound();

        scrollBarX = this.maxEntryRightBound + 5;
        controlWidth = maxEntryRightBound - controlX - 45;
    }

    @Override
    public int getSize() {
        return this.listEntries.size();
    }

    /**
     * Gets the IGuiListEntry object for the given index
     */
    @Override
    public IConfigEntry getListEntry(int index) {
        return this.listEntries.get(index);
    }

    @Override
    public int getScrollBarX() {
        return scrollBarX;
    }

    /**
     * Gets the width of the list
     */
    @Override
    public int getListWidth() {
        return owningScreen.width;
    }

    /**
     * This method is a pass-through for IConfigEntry objects that require keystrokes. Called from the parent GuiConfig screen.
     */
    public void keyTyped(char eventChar, int eventKey) {
        for (IConfigEntry entry : this.listEntries)
            entry.keyTyped(eventChar, eventKey);
    }

    /**
     * This method is a pass-through for IConfigEntry objects that contain GuiTextField elements. Called from the parent GuiConfig
     * screen.
     */
    public void updateScreen() {
        for (IConfigEntry entry : this.listEntries)
            entry.updateCursorCounter();
    }

    /**
     * This method is a pass-through for IConfigEntry objects that contain GuiTextField elements. Called from the parent GuiConfig
     * screen.
     */
    public void mouseClickedPassThru(int mouseX, int mouseY, int mouseEvent) {
        for (IConfigEntry entry : this.listEntries)
            entry.mouseClicked(mouseX, mouseY, mouseEvent);
    }

    /**
     * This method is a pass-through for IConfigEntry objects that need to perform actions when the containing GUI is closed.
     */
    public void onGuiClosed() {
        for (IConfigEntry entry : this.listEntries)
            entry.onGuiClosed();
    }

    /**
     * Saves all properties on this screen / child screens. This method returns true if any elements were changed that require
     * a restart for proper handling.
     */
    public boolean saveConfigElements() {
        boolean requiresRestart = false;
        for (IConfigEntry entry : this.listEntries)
            if (entry.saveConfigElement()) requiresRestart = true;

        return requiresRestart;
    }

    /**
     * Returns true if all IConfigEntry objects on this screen are set to default. If includeChildren is true sub-category
     * objects are checked as well.
     */
    public boolean areAllEntriesDefault(boolean includeChildren) {
        for (IConfigEntry entry : this.listEntries)
            if ((includeChildren || !(entry instanceof CategoryEntry)) && !entry.isDefault()) return false;

        return true;
    }

    /**
     * Sets all IConfigEntry objects on this screen to default. If includeChildren is true sub-category objects are set as
     * well.
     */
    public void setAllToDefault(boolean includeChildren) {
        for (IConfigEntry entry : this.listEntries)
            if ((includeChildren || !(entry instanceof CategoryEntry))) entry.setToDefault();
    }

    /**
     * Returns true if any IConfigEntry objects on this screen are changed. If includeChildren is true sub-category objects
     * are checked as well.
     */
    public boolean hasChangedEntry(boolean includeChildren) {
        for (IConfigEntry entry : this.listEntries)
            if ((includeChildren || !(entry instanceof CategoryEntry)) && entry.isChanged()) return true;

        return false;
    }

    /**
     * Returns true if any IConfigEntry objects on this screen are enabled. If includeChildren is true sub-category objects
     * are checked as well.
     */
    public boolean areAnyEntriesEnabled(boolean includeChildren) {
        for (IConfigEntry entry : this.listEntries)
            if ((includeChildren || !(entry instanceof CategoryEntry)) && entry.enabled()) return true;

        return false;
    }

    /**
     * Reverts changes to all IConfigEntry objects on this screen. If includeChildren is true sub-category objects are
     * reverted as well.
     */
    public void undoAllChanges(boolean includeChildren) {
        for (IConfigEntry entry : this.listEntries)
            if ((includeChildren || !(entry instanceof CategoryEntry))) entry.undoChanges();
    }

    /**
     * Calls the drawToolTip() method for all IConfigEntry objects on this screen. This is called from the parent GuiConfig screen
     * after drawing all other elements.
     */
    public void drawScreenPost(int mouseX, int mouseY, float partialTicks) {
        for (IConfigEntry entry : this.listEntries)
            entry.drawToolTip(mouseX, mouseY);
    }

    /**
     * BooleanPropEntry
     * <p>
     * Provides a GuiButton that toggles between true and false.
     */
    public static class BooleanEntry extends ButtonEntry {
        protected final boolean beforeValue;
        protected boolean currentValue;

        private BooleanEntry(GuiConfig owningScreen, BCGuiConfigEntries owningEntryList, IConfigElement configElement, Property property) {
            super(owningScreen, owningEntryList, configElement, property);
            this.beforeValue = Boolean.valueOf(configElement.get().toString());
            this.currentValue = beforeValue;
            this.btnValue.enabled = enabled();
            updateValueButtonText();
        }

        @Override
        public void updateValueButtonText() {
            this.btnValue.displayString = I18n.format(String.valueOf(currentValue));
            btnValue.packedFGColour = currentValue ? GuiUtils.getColorCode('2', true) : GuiUtils.getColorCode('4', true);
        }

        @Override
        public void valueButtonPressed(int slotIndex) {
            if (enabled()) currentValue = !currentValue;
        }

        @Override
        public boolean isDefault() {
            return currentValue == Boolean.valueOf(configElement.getDefault().toString());
        }

        @Override
        public void setToDefault() {
            if (enabled()) {
                currentValue = Boolean.valueOf(configElement.getDefault().toString());
                updateValueButtonText();
            }
        }

        @Override
        public boolean isChanged() {
            return currentValue != beforeValue;
        }

        @Override
        public void undoChanges() {
            if (enabled()) {
                currentValue = beforeValue;
                updateValueButtonText();
            }
        }

        @Override
        public boolean saveConfigElement() {
            if (enabled() && isChanged()) {
                configElement.set(currentValue);
                return configElement.requiresMcRestart();
            }
            return false;
        }

        @Override
        public Boolean getCurrentValue() {
            return currentValue;
        }

        @Override
        public Boolean[] getCurrentValues() {
            return new Boolean[]{getCurrentValue()};
        }

        @Override
        public void updatePosition(int p_192633_1_, int p_192633_2_, int p_192633_3_, float p_192633_4_) {

        }
    }

    /**
     * CycleValueEntry
     * <p>
     * Provides a GuiButton that cycles through the prop's validValues array. If the current prop value is not a valid value, the first
     * entry replaces the current value.
     */
    public static class CycleValueEntry extends ButtonEntry {
        protected final int beforeIndex;
        protected final int defaultIndex;
        protected int currentIndex;

        private CycleValueEntry(GuiConfig owningScreen, BCGuiConfigEntries owningEntryList, IConfigElement configElement, Property property) {
            super(owningScreen, owningEntryList, configElement, property);
            beforeIndex = getIndex(configElement.get().toString());
            defaultIndex = getIndex(configElement.getDefault().toString());
            currentIndex = beforeIndex;
            this.btnValue.enabled = enabled();
            updateValueButtonText();
        }

        private int getIndex(String s) {
            for (int i = 0; i < configElement.getValidValues().length; i++)
                if (configElement.getValidValues()[i].equalsIgnoreCase(s)) {
                    return i;
                }

            return 0;
        }

        @Override
        public void updateValueButtonText() {
            this.btnValue.displayString = I18n.format(configElement.getValidValues()[currentIndex]);
        }

        @Override
        public void valueButtonPressed(int slotIndex) {
            if (enabled()) {
                if (++this.currentIndex >= configElement.getValidValues().length) this.currentIndex = 0;

                updateValueButtonText();
            }
        }

        @Override
        public boolean isDefault() {
            return currentIndex == defaultIndex;
        }

        @Override
        public void setToDefault() {
            if (enabled()) {
                currentIndex = defaultIndex;
                updateValueButtonText();
            }
        }

        @Override
        public boolean isChanged() {
            return currentIndex != beforeIndex;
        }

        @Override
        public void undoChanges() {
            if (enabled()) {
                currentIndex = beforeIndex;
                updateValueButtonText();
            }
        }

        @Override
        public boolean saveConfigElement() {
            if (enabled() && isChanged()) {
                configElement.set(configElement.getValidValues()[currentIndex]);
                return configElement.requiresMcRestart();
            }
            return false;
        }

        @Override
        public String getCurrentValue() {
            return configElement.getValidValues()[currentIndex];
        }

        @Override
        public String[] getCurrentValues() {
            return new String[]{getCurrentValue()};
        }

        @Override
        public void updatePosition(int p_192633_1_, int p_192633_2_, int p_192633_3_, float p_192633_4_) {

        }
    }

    /**
     * ChatColorEntry
     * <p>
     * Provides a GuiButton that cycles through the list of chat color codes.
     */
    public static class ChatColorEntry extends CycleValueEntry {
        ChatColorEntry(GuiConfig owningScreen, BCGuiConfigEntries owningEntryList, IConfigElement configElement, Property property) {
            super(owningScreen, owningEntryList, configElement, property);
            this.btnValue.enabled = enabled();
            updateValueButtonText();
        }

        @Override
        public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float pt) {
            this.btnValue.packedFGColour = GuiUtils.getColorCode(this.configElement.getValidValues()[currentIndex].charAt(0), true);
            super.drawEntry(slotIndex, x, y, listWidth, slotHeight, mouseX, mouseY, isSelected, pt);
        }

        @Override
        public void updateValueButtonText() {
            this.btnValue.displayString = I18n.format(configElement.getValidValues()[currentIndex]) + " - " + I18n.format("fml.configgui.sampletext");
        }
    }

    /**
     * SelectValueEntry
     * <p>
     * Provides a GuiButton with the current value as the displayString. Accepts a Map of selectable values with the signature <Object,
     * String> where the key is the Object to be selected and the value is the String that will show on the selection list. EG: a map of Mod
     * ID values where the key is the Mod ID and the value is the Mod Name.
     */
    public static class SelectValueEntry extends ButtonEntry {
        protected final String beforeValue;
        protected Object currentValue;
        protected Map<Object, String> selectableValues;

        public SelectValueEntry(GuiConfig owningScreen, BCGuiConfigEntries owningEntryList, IConfigElement configElement, Map<Object, String> selectableValues, Property property) {
            super(owningScreen, owningEntryList, configElement, property);
            beforeValue = configElement.get().toString();
            currentValue = configElement.get().toString();
            this.selectableValues = selectableValues;
            updateValueButtonText();
        }

        @Override
        public void updateValueButtonText() {
            this.btnValue.displayString = currentValue.toString();
        }

        @Override
        public void valueButtonPressed(int slotIndex) {
            mc.displayGuiScreen(new GuiSelectString(this.owningScreen, configElement, slotIndex, selectableValues, currentValue, enabled()));
        }

        public void setValueFromChildScreen(Object newValue) {
            if (enabled() && currentValue != null ? !currentValue.equals(newValue) : newValue != null) {
                currentValue = newValue;
                updateValueButtonText();
            }
        }

        @Override
        public boolean isDefault() {
            if (configElement.getDefault() != null) return configElement.getDefault().equals(currentValue);
            else return currentValue == null;
        }

        @Override
        public void setToDefault() {
            if (enabled()) {
                this.currentValue = configElement.getDefault().toString();
                updateValueButtonText();
            }
        }

        @Override
        public boolean isChanged() {
            if (beforeValue != null) return !beforeValue.equals(currentValue);
            else return currentValue == null;
        }

        @Override
        public void undoChanges() {
            if (enabled()) {
                currentValue = beforeValue;
                updateValueButtonText();
            }
        }

        @Override
        public boolean saveConfigElement() {
            if (enabled() && isChanged()) {
                this.configElement.set(currentValue);
                return configElement.requiresMcRestart();
            }
            return false;
        }

        @Override
        public String getCurrentValue() {
            return this.currentValue.toString();
        }

        @Override
        public String[] getCurrentValues() {
            return new String[]{getCurrentValue()};
        }

        @Override
        public void updatePosition(int p_192633_1_, int p_192633_2_, int p_192633_3_, float p_192633_4_) {

        }
    }

    /**
     * ArrayEntry
     * <p>
     * Provides a GuiButton with the list contents as the displayString. Clicking the button navigates to a screen where the list can be
     * edited.
     */
    public static class ArrayEntry extends ButtonEntry {
        protected final Object[] beforeValues;
        protected Object[] currentValues;

        public ArrayEntry(GuiConfig owningScreen, BCGuiConfigEntries owningEntryList, IConfigElement configElement, Property property) {
            super(owningScreen, owningEntryList, configElement, property);
            beforeValues = configElement.getList();
            currentValues = configElement.getList();
            updateValueButtonText();
        }

        @Override
        public void updateValueButtonText() {
            this.btnValue.displayString = "";
            for (Object o : currentValues)
                this.btnValue.displayString += ", [" + o + "]";

            this.btnValue.displayString = this.btnValue.displayString.replaceFirst(", ", "");
        }

        @Override
        public void valueButtonPressed(int slotIndex) {
            mc.displayGuiScreen(new GuiEditArray(this.owningScreen, configElement, slotIndex, currentValues, enabled()){

                public GuiScreen getParent() {
                    return parentScreen;
                }

                public int getSlotIndex() {
                    return slotIndex;
                }

                @Override
                public void initGui()
                {
                    this.entryList = new GuiEditArrayEntries(this, this.mc, this.configElement, this.beforeValues, this.currentValues) {
                        protected void saveListChanges()
                        {
                            int listLength = configElement.isListLengthFixed() ? listEntries.size() : listEntries.size() - 1;

                            if (getSlotIndex() != -1 && getParent() != null
                                    && getParent() instanceof GuiConfig
                                    && ((GuiConfig) getParent()).entryList.getListEntry(getSlotIndex()) instanceof ArrayEntry)
                            {
                                ArrayEntry entry = (ArrayEntry) ((GuiConfig) getParent()).entryList.getListEntry(getSlotIndex());

                                Object[] ao = new Object[listLength];
                                for (int i = 0; i < listLength; i++)
                                    ao[i] = listEntries.get(i).getValue();

                                entry.setListFromChildScreen(ao);
                            }
                            else
                            {
                                if (configElement.isList() && configElement.getType() == ConfigGuiType.BOOLEAN)
                                {
                                    Boolean[] abol = new Boolean[listLength];
                                    for (int i = 0; i < listLength; i++)
                                        abol[i] = Boolean.valueOf(listEntries.get(i).getValue().toString());

                                    configElement.set(abol);
                                }
                                else if (configElement.isList() && configElement.getType() == ConfigGuiType.INTEGER)
                                {
                                    Integer[] ai = new Integer[listLength];
                                    for (int i = 0; i < listLength; i++)
                                        ai[i] = Integer.valueOf(listEntries.get(i).getValue().toString());

                                    configElement.set(ai);
                                }
                                else if (configElement.isList() && configElement.getType() == ConfigGuiType.DOUBLE)
                                {
                                    Double[] ad = new Double[listLength];
                                    for (int i = 0; i < listLength; i++)
                                        ad[i] = Double.valueOf(listEntries.get(i).getValue().toString());

                                    configElement.set(ad);
                                }
                                else if (configElement.isList())
                                {
                                    String[] as = new String[listLength];
                                    for (int i = 0; i < listLength; i++)
                                        as[i] = listEntries.get(i).getValue().toString();

                                    configElement.set(as);
                                }
                            }
                        }
                    };

                    int undoGlyphWidth = mc.fontRenderer.getStringWidth(UNDO_CHAR) * 2;
                    int resetGlyphWidth = mc.fontRenderer.getStringWidth(RESET_CHAR) * 2;
                    int doneWidth = Math.max(mc.fontRenderer.getStringWidth(I18n.format("gui.done")) + 20, 100);
                    int undoWidth = mc.fontRenderer.getStringWidth(" " + I18n.format("fml.configgui.tooltip.undoChanges")) + undoGlyphWidth + 20;
                    int resetWidth = mc.fontRenderer.getStringWidth(" " + I18n.format("fml.configgui.tooltip.resetToDefault")) + resetGlyphWidth + 20;
                    int buttonWidthHalf = (doneWidth + 5 + undoWidth + 5 + resetWidth) / 2;
                    this.buttonList.add(btnDone = new GuiButtonExt(2000, this.width / 2 - buttonWidthHalf, this.height - 29, doneWidth, 20, I18n.format("gui.done")));
                    this.buttonList.add(btnDefault = new GuiUnicodeGlyphButton(2001, this.width / 2 - buttonWidthHalf + doneWidth + 5 + undoWidth + 5,
                            this.height - 29, resetWidth, 20, " " + I18n.format("fml.configgui.tooltip.resetToDefault"), RESET_CHAR, 2.0F));
                    this.buttonList.add(btnUndoChanges = new GuiUnicodeGlyphButton(2002, this.width / 2 - buttonWidthHalf + doneWidth + 5,
                            this.height - 29, undoWidth, 20, " " + I18n.format("fml.configgui.tooltip.undoChanges"), UNDO_CHAR, 2.0F));
                }
            });
        }

        public void setListFromChildScreen(Object[] newList) {
            if (enabled() && !Arrays.deepEquals(currentValues, newList)) {
                currentValues = newList;
                updateValueButtonText();
            }
        }

        @Override
        public boolean isDefault() {
            return Arrays.deepEquals(configElement.getDefaults(), currentValues);
        }

        @Override
        public void setToDefault() {
            if (enabled()) {
                this.currentValues = configElement.getDefaults();
                updateValueButtonText();
            }
        }

        @Override
        public boolean isChanged() {
            return !Arrays.deepEquals(beforeValues, currentValues);
        }

        @Override
        public void undoChanges() {
            if (enabled()) {
                currentValues = beforeValues;
                updateValueButtonText();
            }
        }

        @Override
        public boolean saveConfigElement() {
            if (enabled() && isChanged()) {
                this.configElement.set(currentValues);
                return configElement.requiresMcRestart();
            }
            return false;
        }

        @Override
        public Object getCurrentValue() {
            return this.btnValue.displayString;
        }

        @Override
        public Object[] getCurrentValues() {
            return this.currentValues;
        }

        @Override
        public void updatePosition(int p_192633_1_, int p_192633_2_, int p_192633_3_, float p_192633_4_) {

        }
    }

    /**
     * NumberSliderEntry
     * <p>
     * Provides a slider for numeric properties.
     */
    public static class NumberSliderEntry extends ButtonEntry {
        protected final double beforeValue;

        public NumberSliderEntry(GuiConfig owningScreen, BCGuiConfigEntries owningEntryList, IConfigElement configElement, Property property) {
            super(owningScreen, owningEntryList, configElement, new GuiSlider(0, owningEntryList.controlX, 0, owningEntryList.controlWidth, 18, "", "", Double.valueOf(configElement.getMinValue().toString()), Double.valueOf(configElement.getMaxValue().toString()), Double.valueOf(configElement.get().toString()), configElement.getType() == ConfigGuiType.DOUBLE, true), property);

            if (configElement.getType() == ConfigGuiType.INTEGER) this.beforeValue = Integer.valueOf(configElement.get().toString());
            else this.beforeValue = Double.valueOf(configElement.get().toString());
        }

        @Override
        public void updateValueButtonText() {
            ((GuiSlider) this.btnValue).updateSlider();
        }

        @Override
        public void valueButtonPressed(int slotIndex) {
        }

        @Override
        public boolean isDefault() {
            if (configElement.getType() == ConfigGuiType.INTEGER) return ((GuiSlider) this.btnValue).getValueInt() == Integer.valueOf(configElement.getDefault().toString());
            else return ((GuiSlider) this.btnValue).getValue() == Double.valueOf(configElement.getDefault().toString());
        }

        @Override
        public void setToDefault() {
            if (this.enabled()) {
                ((GuiSlider) this.btnValue).setValue(Double.valueOf(configElement.getDefault().toString()));
                ((GuiSlider) this.btnValue).updateSlider();
            }
        }

        @Override
        public boolean isChanged() {
            if (configElement.getType() == ConfigGuiType.INTEGER) return ((GuiSlider) this.btnValue).getValueInt() != (int) Math.round(beforeValue);
            else return ((GuiSlider) this.btnValue).getValue() != beforeValue;
        }

        @Override
        public void undoChanges() {
            if (this.enabled()) {
                ((GuiSlider) this.btnValue).setValue(beforeValue);
                ((GuiSlider) this.btnValue).updateSlider();
            }
        }

        @Override
        public boolean saveConfigElement() {
            if (this.enabled() && this.isChanged()) {
                if (configElement.getType() == ConfigGuiType.INTEGER) configElement.set(((GuiSlider) this.btnValue).getValueInt());
                else configElement.set(((GuiSlider) this.btnValue).getValue());
                return configElement.requiresMcRestart();
            }
            return false;
        }

        @Override
        public Object getCurrentValue() {
            if (configElement.getType() == ConfigGuiType.INTEGER) return ((GuiSlider) this.btnValue).getValueInt();
            else return ((GuiSlider) this.btnValue).getValue();
        }

        @Override
        public Object[] getCurrentValues() {
            return new Object[]{getCurrentValue()};
        }

        @Override
        public void updatePosition(int p_192633_1_, int p_192633_2_, int p_192633_3_, float p_192633_4_) {

        }
    }

    /**
     * ButtonEntry
     * <p>
     * Provides a basic GuiButton entry to be used as a base for other entries that require a button for the value.
     */
    public static abstract class ButtonEntry extends ListEntryBase {
        protected final GuiButtonExt btnValue;

        public ButtonEntry(GuiConfig owningScreen, BCGuiConfigEntries owningEntryList, IConfigElement configElement, Property property) {
            this(owningScreen, owningEntryList, configElement, new GuiButtonExt(0, owningEntryList.controlX, 0, owningEntryList.controlWidth, 18, configElement.get() != null ? I18n.format(String.valueOf(configElement.get())) : ""), property);
        }

        public ButtonEntry(GuiConfig owningScreen, BCGuiConfigEntries owningEntryList, IConfigElement configElement, GuiButtonExt button, Property property) {
            super(owningScreen, owningEntryList, configElement, property);
            this.btnValue = button;
        }

        /**
         * Updates the displayString of the value button.
         */
        public abstract void updateValueButtonText();

        /**
         * Called when the value button has been clicked.
         */
        public abstract void valueButtonPressed(int slotIndex);

        @Override
        public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float pt) {
            super.drawEntry(slotIndex, x, y, listWidth, slotHeight, mouseX, mouseY, isSelected, pt);
            this.btnValue.width = this.owningEntryList.controlWidth;
            this.btnValue.x = this.owningScreen.entryList.controlX;
            this.btnValue.y = y;
            this.btnValue.enabled = enabled() && !locked;
            this.btnValue.drawButton(this.mc, mouseX, mouseY, pt);
        }

        /**
         * Returns true if the mouse has been pressed on this control.
         */
        @Override
        public boolean mousePressed(int index, int x, int y, int mouseEvent, int relativeX, int relativeY) {
            if (locked) {
                return true;
            }
            if (this.btnValue.mousePressed(this.mc, x, y)) {
                btnValue.playPressSound(mc.getSoundHandler());
                valueButtonPressed(index);
                updateValueButtonText();
                return true;
            }
            else return super.mousePressed(index, x, y, mouseEvent, relativeX, relativeY);
        }

        /**
         * Fired when the mouse button is released. Arguments: index, x, y, mouseEvent, relativeX, relativeY
         */
        @Override
        public void mouseReleased(int index, int x, int y, int mouseEvent, int relativeX, int relativeY) {
            super.mouseReleased(index, x, y, mouseEvent, relativeX, relativeY);
            this.btnValue.mouseReleased(x, y);
        }

        @Override
        public void keyTyped(char eventChar, int eventKey) {
        }

        @Override
        public void updateCursorCounter() {
        }

        @Override
        public void mouseClicked(int x, int y, int mouseEvent) {
        }
    }

    /**
     * IntegerEntry
     * <p>
     * Provides a GuiTextField for user input. Input is restricted to ensure the value can be parsed using Integer.parseInteger().
     */
    public static class IntegerEntry extends StringEntry {
        protected final int beforeValue;

        public IntegerEntry(GuiConfig owningScreen, BCGuiConfigEntries owningEntryList, IConfigElement configElement, Property property) {
            super(owningScreen, owningEntryList, configElement, property);
            this.beforeValue = Integer.valueOf(configElement.get().toString());
        }

        @Override
        public void keyTyped(char eventChar, int eventKey) {
            if (enabled() || eventKey == Keyboard.KEY_LEFT || eventKey == Keyboard.KEY_RIGHT || eventKey == Keyboard.KEY_HOME || eventKey == Keyboard.KEY_END) {
                String validChars = "0123456789";
                String before = this.textFieldValue.getText();
                if (validChars.contains(String.valueOf(eventChar)) || (!before.startsWith("-") && this.textFieldValue.getCursorPosition() == 0 && eventChar == '-') || eventKey == Keyboard.KEY_BACK || eventKey == Keyboard.KEY_DELETE || eventKey == Keyboard.KEY_LEFT || eventKey == Keyboard.KEY_RIGHT || eventKey == Keyboard.KEY_HOME || eventKey == Keyboard.KEY_END) this.textFieldValue.textboxKeyTyped((enabled() ? eventChar : Keyboard.CHAR_NONE), eventKey);

                if (!textFieldValue.getText().trim().isEmpty() && !textFieldValue.getText().trim().equals("-")) {
                    try {
                        long value = Long.parseLong(textFieldValue.getText().trim());
                        if (value < Integer.valueOf(configElement.getMinValue().toString()) || value > Integer.valueOf(configElement.getMaxValue().toString())) this.isValidValue = false;
                        else this.isValidValue = true;
                    }
                    catch (Throwable e) {
                        this.isValidValue = false;
                    }
                }
                else this.isValidValue = false;
            }
        }

        @Override
        public boolean isChanged() {
            try {
                return this.beforeValue != Integer.parseInt(textFieldValue.getText().trim());
            }
            catch (Throwable e) {
                return true;
            }
        }

        @Override
        public void undoChanges() {
            if (enabled()) this.textFieldValue.setText(String.valueOf(beforeValue));
        }

        @Override
        public boolean saveConfigElement() {
            if (enabled()) {
                if (isChanged() && this.isValidValue) try {
                    int value = Integer.parseInt(textFieldValue.getText().trim());
                    this.configElement.set(value);
                    return configElement.requiresMcRestart();
                }
                catch (Throwable e) {
                    this.configElement.setToDefault();
                }
                else if (isChanged() && !this.isValidValue) try {
                    int value = Integer.parseInt(textFieldValue.getText().trim());
                    if (value < Integer.valueOf(configElement.getMinValue().toString())) this.configElement.set(configElement.getMinValue());
                    else this.configElement.set(configElement.getMaxValue());

                }
                catch (Throwable e) {
                    this.configElement.setToDefault();
                }

                return configElement.requiresMcRestart() && beforeValue != Integer.parseInt(configElement.get().toString());
            }
            return false;
        }
    }

    /**
     * DoubleEntry
     * <p>
     * Provides a GuiTextField for user input. Input is restricted to ensure the value can be parsed using Double.parseDouble().
     */
    public static class DoubleEntry extends StringEntry {
        protected final double beforeValue;

        public DoubleEntry(GuiConfig owningScreen, BCGuiConfigEntries owningEntryList, IConfigElement configElement, Property property) {
            super(owningScreen, owningEntryList, configElement, property);
            this.beforeValue = Double.valueOf(configElement.get().toString());
        }

        @Override
        public void keyTyped(char eventChar, int eventKey) {
            if (enabled() || eventKey == Keyboard.KEY_LEFT || eventKey == Keyboard.KEY_RIGHT || eventKey == Keyboard.KEY_HOME || eventKey == Keyboard.KEY_END) {
                String validChars = "0123456789";
                String before = this.textFieldValue.getText();
                if (validChars.contains(String.valueOf(eventChar)) || (!before.startsWith("-") && this.textFieldValue.getCursorPosition() == 0 && eventChar == '-') || (!before.contains(".") && eventChar == '.') || eventKey == Keyboard.KEY_BACK || eventKey == Keyboard.KEY_DELETE || eventKey == Keyboard.KEY_LEFT || eventKey == Keyboard.KEY_RIGHT || eventKey == Keyboard.KEY_HOME || eventKey == Keyboard.KEY_END)
                    this.textFieldValue.textboxKeyTyped((enabled() ? eventChar : Keyboard.CHAR_NONE), eventKey);

                if (!textFieldValue.getText().trim().isEmpty() && !textFieldValue.getText().trim().equals("-")) {
                    try {
                        double value = Double.parseDouble(textFieldValue.getText().trim());
                        if (value < Double.valueOf(configElement.getMinValue().toString()) || value > Double.valueOf(configElement.getMaxValue().toString())) this.isValidValue = false;
                        else this.isValidValue = true;
                    }
                    catch (Throwable e) {
                        this.isValidValue = false;
                    }
                }
                else this.isValidValue = false;
            }
        }

        @Override
        public boolean isChanged() {
            try {
                return this.beforeValue != Double.parseDouble(textFieldValue.getText().trim());
            }
            catch (Throwable e) {
                return true;
            }
        }

        @Override
        public void undoChanges() {
            if (enabled()) this.textFieldValue.setText(String.valueOf(beforeValue));
        }

        @Override
        public boolean saveConfigElement() {
            if (enabled()) {
                if (isChanged() && this.isValidValue) try {
                    double value = Double.parseDouble(textFieldValue.getText().trim());
                    this.configElement.set(value);
                    return configElement.requiresMcRestart();
                }
                catch (Throwable e) {
                    this.configElement.setToDefault();
                }
                else if (isChanged() && !this.isValidValue) try {
                    double value = Double.parseDouble(textFieldValue.getText().trim());
                    if (value < Double.valueOf(configElement.getMinValue().toString())) this.configElement.set(configElement.getMinValue());
                    else this.configElement.set(configElement.getMaxValue());
                }
                catch (Throwable e) {
                    this.configElement.setToDefault();
                }

                return configElement.requiresMcRestart() && beforeValue != Double.parseDouble(configElement.get().toString());
            }
            return false;
        }
    }

    /**
     * StringEntry
     * <p>
     * Provides a GuiTextField for user input.
     */
    public static class StringEntry extends ListEntryBase {
        protected final GuiTextField textFieldValue;
        protected final String beforeValue;

        public StringEntry(GuiConfig owningScreen, BCGuiConfigEntries owningEntryList, IConfigElement configElement, Property property) {
            super(owningScreen, owningEntryList, configElement, property);
            beforeValue = configElement.get().toString();
            this.textFieldValue = new GuiTextField(10, this.mc.fontRenderer, this.owningEntryList.controlX + 1, 0, this.owningEntryList.controlWidth - 3, 16);
            this.textFieldValue.setMaxStringLength(10000);
            this.textFieldValue.setText(configElement.get().toString());
        }

        @Override
        public void updatePosition(int p_192633_1_, int p_192633_2_, int p_192633_3_, float p_192633_4_) {

        }

        @Override
        public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float pt) {
            super.drawEntry(slotIndex, x, y, listWidth, slotHeight, mouseX, mouseY, isSelected, pt);
            this.textFieldValue.x = this.owningEntryList.controlX + 2;
            this.textFieldValue.y = y + 1;
            this.textFieldValue.width = this.owningEntryList.controlWidth - 4;
            this.textFieldValue.setEnabled(enabled() && !locked);
            this.textFieldValue.drawTextBox();
        }

        @Override
        public void keyTyped(char eventChar, int eventKey) {
            if (enabled() || eventKey == Keyboard.KEY_LEFT || eventKey == Keyboard.KEY_RIGHT || eventKey == Keyboard.KEY_HOME || eventKey == Keyboard.KEY_END) {
                this.textFieldValue.textboxKeyTyped((enabled() ? eventChar : Keyboard.CHAR_NONE), eventKey);

                if (configElement.getValidationPattern() != null) {
                    if (configElement.getValidationPattern().matcher(this.textFieldValue.getText().trim()).matches()) isValidValue = true;
                    else isValidValue = false;
                }
            }
        }

        @Override
        public void updateCursorCounter() {
            this.textFieldValue.updateCursorCounter();
        }

        @Override
        public void mouseClicked(int x, int y, int mouseEvent) {
            this.textFieldValue.mouseClicked(x, y, mouseEvent);
        }

        @Override
        public boolean isDefault() {
            return configElement.getDefault() != null ? configElement.getDefault().toString().equals(this.textFieldValue.getText()) : this.textFieldValue.getText().trim().isEmpty();
        }

        @Override
        public void setToDefault() {
            if (enabled()) {
                this.textFieldValue.setText(this.configElement.getDefault().toString());
                keyTyped((char) Keyboard.CHAR_NONE, Keyboard.KEY_HOME);
            }
        }

        @Override
        public boolean isChanged() {
            return beforeValue != null ? !this.beforeValue.equals(textFieldValue.getText()) : this.textFieldValue.getText().trim().isEmpty();
        }

        @Override
        public void undoChanges() {
            if (enabled()) this.textFieldValue.setText(beforeValue);
        }

        @Override
        public boolean saveConfigElement() {
            if (enabled()) {
                if (isChanged() && this.isValidValue) {
                    this.configElement.set(this.textFieldValue.getText());
                    return configElement.requiresMcRestart();
                }
                else if (isChanged() && !this.isValidValue) {
                    this.configElement.setToDefault();
                    return configElement.requiresMcRestart() && beforeValue != null ? beforeValue.equals(configElement.getDefault()) : configElement.getDefault() == null;
                }
            }
            return false;
        }

        @Override
        public Object getCurrentValue() {
            return this.textFieldValue.getText();
        }

        @Override
        public Object[] getCurrentValues() {
            return new Object[]{getCurrentValue()};
        }
    }

    /**
     * CategoryEntry
     * <p>
     * Provides an entry that consists of a GuiButton for navigating to the child category GuiConfig screen.
     */
    public static class CategoryEntry extends ListEntryBase {
        protected GuiScreen childScreen;
        protected final GuiButtonExt btnSelectCategory;

        public CategoryEntry(GuiConfig owningScreen, BCGuiConfigEntries owningEntryList, IConfigElement configElement, Property property) {
            super(owningScreen, owningEntryList, configElement, property);

            this.childScreen = this.buildChildScreen();

            this.btnSelectCategory = new GuiButtonExt(0, 0, 0, 300, 18, I18n.format(name));
            this.tooltipHoverChecker = new HoverChecker(this.btnSelectCategory, 800);

            this.drawLabel = false;
        }

        /**
         * This method is called in the constructor and is used to set the childScreen field.
         */
        protected GuiScreen buildChildScreen() {
            return new BCModConfigGui(this.owningScreen, this.configElement.getChildElements(), this.owningScreen.modID, owningScreen.allRequireWorldRestart || this.configElement.requiresWorldRestart(), owningScreen.allRequireMcRestart || this.configElement.requiresMcRestart(), this.owningScreen.title, ((this.owningScreen.titleLine2 == null ? "" : this.owningScreen.titleLine2) + " > " + this.name));
        }

        @Override
        public void updatePosition(int p_192633_1_, int p_192633_2_, int p_192633_3_, float p_192633_4_) {

        }

        @Override
        public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float pt) {
            this.btnSelectCategory.x = listWidth / 2 - 150;
            this.btnSelectCategory.y = y;
            this.btnSelectCategory.enabled = enabled();
            this.btnSelectCategory.drawButton(this.mc, mouseX, mouseY, pt);

            super.drawEntry(slotIndex, x, y, listWidth, slotHeight, mouseX, mouseY, isSelected, pt);
        }

        @Override
        public void drawToolTip(int mouseX, int mouseY) {
            boolean canHover = mouseY < this.owningScreen.entryList.bottom && mouseY > this.owningScreen.entryList.top;

            if (this.tooltipHoverChecker.checkHover(mouseX, mouseY, canHover)) this.owningScreen.drawToolTip(toolTip, mouseX, mouseY);

            super.drawToolTip(mouseX, mouseY);
        }

        @Override
        public boolean mousePressed(int index, int x, int y, int mouseEvent, int relativeX, int relativeY) {
            if (this.btnSelectCategory.mousePressed(this.mc, x, y)) {
                btnSelectCategory.playPressSound(mc.getSoundHandler());
                Minecraft.getMinecraft().displayGuiScreen(childScreen);
                return true;
            }
            else return super.mousePressed(index, x, y, mouseEvent, relativeX, relativeY);
        }

        @Override
        public void mouseReleased(int index, int x, int y, int mouseEvent, int relativeX, int relativeY) {
            this.btnSelectCategory.mouseReleased(x, y);
        }

        @Override
        public boolean isDefault() {
            if (childScreen instanceof GuiConfig && ((GuiConfig) childScreen).entryList != null) return ((GuiConfig) childScreen).entryList.areAllEntriesDefault(true);

            return true;
        }

        @Override
        public void setToDefault() {
            if (childScreen instanceof GuiConfig && ((GuiConfig) childScreen).entryList != null) ((GuiConfig) childScreen).entryList.setAllToDefault(true);
        }

        @Override
        public void keyTyped(char eventChar, int eventKey) {
        }

        @Override
        public void updateCursorCounter() {
        }

        @Override
        public void mouseClicked(int x, int y, int mouseEvent) {
        }

        @Override
        public boolean saveConfigElement() {
            boolean requiresRestart = false;

            if (childScreen instanceof GuiConfig && ((GuiConfig) childScreen).entryList != null) {
                requiresRestart = configElement.requiresMcRestart() && ((GuiConfig) childScreen).entryList.hasChangedEntry(true);

                if (((GuiConfig) childScreen).entryList.saveConfigElements()) requiresRestart = true;
            }

            return requiresRestart;
        }

        @Override
        public boolean isChanged() {
            if (childScreen instanceof GuiConfig && ((GuiConfig) childScreen).entryList != null) return ((GuiConfig) childScreen).entryList.hasChangedEntry(true);
            else return false;
        }

        @Override
        public void undoChanges() {
            if (childScreen instanceof GuiConfig && ((GuiConfig) childScreen).entryList != null) ((GuiConfig) childScreen).entryList.undoAllChanges(true);
        }

        @Override
        public boolean enabled() {
            return true;
        }

        @Override
        public int getLabelWidth() {
            return 0;
        }

        @Override
        public int getEntryRightBound() {
            return this.owningEntryList.width / 2 + 155 + 22 + 18;
        }

        @Override
        public String getCurrentValue() {
            return "";
        }

        @Override
        public String[] getCurrentValues() {
            return new String[]{getCurrentValue()};
        }
    }

    /**
     * ListEntryBase
     * <p>
     * Provides a base entry for others to extend. Handles drawing the prop label (if drawLabel == true) and the Undo/Default buttons.
     */
    public static abstract class ListEntryBase implements IConfigEntry {
        protected final GuiConfig owningScreen;
        protected final BCGuiConfigEntries owningEntryList;
        protected final IConfigElement configElement;
        protected final Minecraft mc;
        protected final String name;
        protected final GuiButtonExt btnUndoChanges;
        protected final GuiButtonExt btnDefault;
        protected List<String> toolTip;
        protected List<String> undoToolTip;
        protected List<String> defaultToolTip;
        protected boolean isValidValue = true;
        protected HoverChecker tooltipHoverChecker;
        protected HoverChecker undoHoverChecker;
        protected HoverChecker defaultHoverChecker;
        protected boolean drawLabel;
        protected boolean locked = false;
        protected boolean isHovering = false;

        public ListEntryBase(GuiConfig owningScreen, BCGuiConfigEntries owningEntryList, IConfigElement configElement, Property property) {
            this.owningScreen = owningScreen;
            this.owningEntryList = owningEntryList;
            this.configElement = configElement;
            this.mc = Minecraft.getMinecraft();
            String trans = I18n.format(configElement.getLanguageKey());
            if (!trans.equals(configElement.getLanguageKey())) this.name = trans;
            else this.name = configElement.getName();
            this.btnUndoChanges = new GuiButtonExt(0, 0, 0, 18, 18, UNDO_CHAR);
            this.btnDefault = new GuiButtonExt(0, 0, 0, 18, 18, RESET_CHAR);

            this.undoHoverChecker = new HoverChecker(this.btnUndoChanges, 800);
            this.defaultHoverChecker = new HoverChecker(this.btnDefault, 800);
            this.undoToolTip = Arrays.asList(new String[]{I18n.format("fml.configgui.tooltip.undoChanges")});
            this.defaultToolTip = Arrays.asList(new String[]{I18n.format("fml.configgui.tooltip.resetToDefault")});
            this.toolTip = new ArrayList<String>();

            this.drawLabel = true;

            String comment;

            comment = I18n.format(configElement.getLanguageKey() + ".tooltip").replace("\\n", "\n");

            if (!comment.equals(configElement.getLanguageKey() + ".tooltip")) Collections.addAll(toolTip, (TextFormatting.GREEN + name + "\n" + TextFormatting.YELLOW + removeTag(comment, "[default:", "]")).split("\n"));
            else if (configElement.getComment() != null && !configElement.getComment().trim().isEmpty()) Collections.addAll(toolTip, (TextFormatting.GREEN + name + "\n" + TextFormatting.YELLOW + removeTag(configElement.getComment(), "[default:", "]")).split("\n"));
            else Collections.addAll(toolTip, (TextFormatting.GREEN + name + "\n" + TextFormatting.RED + "No tooltip defined.").split("\n"));

            if ((configElement.getType() == ConfigGuiType.INTEGER && (Integer.valueOf(configElement.getMinValue().toString()) != Integer.MIN_VALUE || Integer.valueOf(configElement.getMaxValue().toString()) != Integer.MAX_VALUE)) || (configElement.getType() == ConfigGuiType.DOUBLE && (Double.valueOf(configElement.getMinValue().toString()) != -Double.MAX_VALUE || Double.valueOf(configElement.getMaxValue().toString()) != Double.MAX_VALUE)))
                Collections.addAll(toolTip, (TextFormatting.AQUA + I18n.format("fml.configgui.tooltip.defaultNumeric", configElement.getMinValue(), configElement.getMaxValue(), configElement.getDefault())).split("\n"));
            else if (configElement.getType() != ConfigGuiType.CONFIG_CATEGORY) Collections.addAll(toolTip, (TextFormatting.AQUA + I18n.format("fml.configgui.tooltip.default", configElement.getDefault())).split("\n"));

            if (configElement.requiresMcRestart() || owningScreen.allRequireMcRestart) toolTip.add(TextFormatting.RED + "[" + I18n.format("fml.configgui.gameRestartTitle") + "]");

            this.locked = property != null && ModConfigParser.isPropLocked(owningScreen.modID, property);
        }

        @Override
        public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float partialTicks) {
            boolean isChanged = isChanged();

            if (drawLabel) {
                String label = (!isValidValue ? TextFormatting.RED.toString() : (isChanged ? TextFormatting.WHITE.toString() : TextFormatting.GRAY.toString())) + (isChanged ? TextFormatting.ITALIC.toString() : "") + this.name;
                this.mc.fontRenderer.drawString(label, this.owningScreen.entryList.labelX, y + slotHeight / 2 - this.mc.fontRenderer.FONT_HEIGHT / 2, 16777215);
            }

            this.btnUndoChanges.x = this.owningEntryList.scrollBarX - 44;
            this.btnUndoChanges.y = y;
            this.btnUndoChanges.enabled = enabled() && isChanged;
            this.btnUndoChanges.drawButton(this.mc, mouseX, mouseY, partialTicks);

            this.btnDefault.x = this.owningEntryList.scrollBarX - 22;
            this.btnDefault.y = y;
            this.btnDefault.enabled = enabled() && !isDefault();
            this.btnDefault.drawButton(this.mc, mouseX, mouseY, partialTicks);

            if (this.tooltipHoverChecker == null) this.tooltipHoverChecker = new HoverChecker(y, y + slotHeight, x, this.owningScreen.entryList.controlX - 8, 800);
            else this.tooltipHoverChecker.updateBounds(y, y + slotHeight, x, this.owningScreen.entryList.controlX - 8);

            isHovering = isSelected;
        }

        @Override
        public void drawToolTip(int mouseX, int mouseY) {
            boolean canHover = mouseY < this.owningScreen.entryList.bottom && mouseY > this.owningScreen.entryList.top;
            if (toolTip != null && this.tooltipHoverChecker != null) {
                if (this.tooltipHoverChecker.checkHover(mouseX, mouseY, canHover)) {
                    this.owningScreen.drawToolTip(toolTip, mouseX, mouseY);
                }
            }

            if (this.undoHoverChecker.checkHover(mouseX, mouseY, canHover)) {
                this.owningScreen.drawToolTip(undoToolTip, mouseX, mouseY);
            }

            if (this.defaultHoverChecker.checkHover(mouseX, mouseY, canHover)) {
                this.owningScreen.drawToolTip(defaultToolTip, mouseX, mouseY);
            }

            if (isHovering && locked) {
                owningScreen.drawToolTip(Collections.singletonList("This config property is currently controlled by the server you are connected to!"), mouseX, mouseY);
            }
        }

        @Override
        public boolean mousePressed(int index, int x, int y, int mouseEvent, int relativeX, int relativeY) {
            if (locked) {
                return true;
            }
            if (this.btnDefault.mousePressed(this.mc, x, y)) {
                btnDefault.playPressSound(mc.getSoundHandler());
                setToDefault();
                return true;
            }
            else if (this.btnUndoChanges.mousePressed(this.mc, x, y)) {
                btnUndoChanges.playPressSound(mc.getSoundHandler());
                undoChanges();
                return true;
            }
            return false;
        }

        @Override
        public void mouseReleased(int index, int x, int y, int mouseEvent, int relativeX, int relativeY) {
            this.btnDefault.mouseReleased(x, y);
        }

        @Override
        public abstract boolean isDefault();

        @Override
        public abstract void setToDefault();

        @Override
        public abstract void keyTyped(char eventChar, int eventKey);

        @Override
        public abstract void updateCursorCounter();

        @Override
        public abstract void mouseClicked(int x, int y, int mouseEvent);

        @Override
        public abstract boolean isChanged();

        @Override
        public abstract void undoChanges();

        @Override
        public abstract boolean saveConfigElement();


        @Override
        public boolean enabled() {
            return owningScreen.isWorldRunning ? !owningScreen.allRequireWorldRestart && !configElement.requiresWorldRestart() : true;
        }

        @Override
        public int getLabelWidth() {
            return this.mc.fontRenderer.getStringWidth(this.name);
        }

        @Override
        public int getEntryRightBound() {
            return this.owningEntryList.resetX + 40;
        }

        @Override
        public IConfigElement getConfigElement() {
            return configElement;
        }

        @Override
        public String getName() {
            return configElement.getName();
        }

        @Override
        public abstract Object getCurrentValue();

        @Override
        public abstract Object[] getCurrentValues();

        @Override
        public void onGuiClosed() {
        }

        /**
         * Get string surrounding tagged area.
         */
        private String removeTag(String target, String tagStart, String tagEnd) {
            int tagStartPosition = tagStartPosition = target.indexOf(tagStart);
            int tagEndPosition = tagEndPosition = target.indexOf(tagEnd, tagStartPosition + tagStart.length());

            if (-1 == tagStartPosition || -1 == tagEndPosition) return target;

            String taglessResult = target.substring(0, tagStartPosition);
            taglessResult += target.substring(tagEndPosition + 1, target.length());

            return taglessResult;
        }
    }
//
//    /**
//     * Provides an interface for defining GuiConfigEntry.listEntry objects.
//     */
//    public static interface IConfigEntry extends GuiListExtended.IGuiListEntry
//    {
//        /**
//         * Gets the IConfigElement object owned by this entry.
//         * @return
//         */
//        public IConfigElement getConfigElement();
//
//        /**
//         * Gets the name of the ConfigElement owned by this entry.
//         */
//        public String getName();
//
//        /**
//         * Gets the current value of this entry.
//         */
//        public Object getCurrentValue();
//
//        /**
//         * Gets the current values of this list entry.
//         */
//        public Object[] getCurrentValues();
//
//        /**
//         * Is this list entry enabled?
//         *
//         * @return true if this entry's controls should be enabled, false otherwise.
//         */
//        public boolean enabled();
//
//        /**
//         * Handles user keystrokes for any GuiTextField objects in this entry. Call {@link GuiTextField#textboxKeyTyped(char, int)} for any GuiTextField
//         * objects that should receive the input provided.
//         */
//        public void keyTyped(char eventChar, int eventKey);
//
//        /**
//         * Call {@link GuiTextField#updateCursorCounter()} for any GuiTextField objects in this entry.
//         */
//        public void updateCursorCounter();
//
//        /**
//         * Call {@link GuiTextField#mouseClicked(int, int, int)} for and GuiTextField objects in this entry.
//         */
//        public void mouseClicked(int x, int y, int mouseEvent);
//
//        /**
//         * Is this entry's value equal to the default value? Generally true should be returned if this entry is not a property or category
//         * entry.
//         *
//         * @return true if this entry's value is equal to this entry's default value.
//         */
//        public boolean isDefault();
//
//        /**
//         * Sets this entry's value to the default value.
//         */
//        public void setToDefault();
//
//        /**
//         * Handles reverting any changes that have occurred to this entry.
//         */
//        public void undoChanges();
//
//        /**
//         * Has the value of this entry changed?
//         *
//         * @return true if changes have been made to this entry's value, false otherwise.
//         */
//        public boolean isChanged();
//
//        /**
//         * Handles saving any changes that have been made to this entry back to the underlying object. It is a good practice to check
//         * isChanged() before performing the save action. This method should return true if the element has changed AND REQUIRES A RESTART.
//         */
//        public boolean saveConfigElement();
//
//        /**
//         * Handles drawing any tooltips that apply to this entry. This method is called after all other GUI elements have been drawn to the
//         * screen, so it could also be used to draw any GUI element that needs to be drawn after all entries have had drawEntry() called.
//         */
//        public void drawToolTip(int mouseX, int mouseY);
//
//        /**
//         * Gets this entry's label width.
//         */
//        public int getLabelWidth();
//
//        /**
//         * Gets this entry's right-hand x boundary. This value is used to control where the scroll bar is placed.
//         */
//        public int getEntryRightBound();
//
//        /**
//         * This method is called when the parent GUI is closed. Most handlers won't need this; it is provided for special cases.
//         */
//        public void onGuiClosed();
//    }
}
