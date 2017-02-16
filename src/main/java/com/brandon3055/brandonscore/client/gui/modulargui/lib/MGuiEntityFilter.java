package com.brandon3055.brandonscore.client.gui.modulargui.lib;

import com.brandon3055.brandonscore.client.gui.modulargui.IModularGui;
import com.brandon3055.brandonscore.client.gui.modulargui.MGuiElementBase;
import com.brandon3055.brandonscore.client.gui.modulargui.lib.ModuleBuilder.EqualColumns;
import com.brandon3055.brandonscore.client.gui.modulargui.modularelements.*;
import com.brandon3055.brandonscore.lib.EntityFilter;
import com.brandon3055.brandonscore.utils.LinkedHashList;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StringUtils;
import net.minecraft.util.text.TextFormatting;

import java.io.IOException;
import java.util.*;

/**
 * Created by brandon3055 on 23/10/2016.
 * This is the config element used for {@link com.brandon3055.brandonscore.lib.EntityFilter}
 */
public class MGuiEntityFilter extends MGuiElementBase implements IMGuiListener {
    private final EntityFilter filter;
    public List<String> playerNames = new ArrayList<>();

    private MGuiButtonToggle detectPassive;
    private MGuiButtonToggle detectHostile;
    private MGuiButtonToggle detectPlayer;
    private MGuiButtonToggle detectOther;
    private MGuiButton toggleList;
    private MGuiButton addEntity;
    private MGuiButton addPlayer;
    private MGuiButton addCustom;
    private MGuiLabel listLabel;
    private MGuiList list;
    private MGuiSelectDialog selector = null;
    private LinkedHashList<String> lastTickList = new LinkedHashList<>();

    private static List<Entity> entityList;

    public MGuiEntityFilter(IModularGui modularGui, EntityFilter filter) {
        super(modularGui);
        this.filter = filter;
    }

    public MGuiEntityFilter(IModularGui modularGui, EntityFilter filter, int xPos, int yPos) {
        super(modularGui, xPos, yPos);
        this.filter = filter;
    }

    public MGuiEntityFilter(IModularGui modularGui, EntityFilter filter, int xPos, int yPos, int xSize, int ySize) {
        super(modularGui, xPos, yPos, xSize, ySize);
        this.filter = filter;
    }

    @Override
    public void initElement() {
        childElements.clear();
        addChild(new MGuiLabel(modularGui, xPos, yPos, xSize, 12, I18n.format("gui.entityFilter.filter.txt")));
        int buttons = (filter.isTypeSelectionEnabled() ? 1 : 0) + (filter.isTypeSelectionEnabled() ? 1 : 0) + (filter.isTypeSelectionEnabled() ? 1 : 0);//TODO clean up enableTypeSelection
        if (buttons <= 0) {
            buttons = 1;
        }
        int width = xSize / buttons;
        int offset = xSize - (width * buttons);
        EqualColumns builder = new EqualColumns(xPos + offset, yPos + 12, 2, xSize / 2 - 1, 12, 1);

        if (filter.isTypeSelectionEnabled()) {
            builder.add(detectHostile = (MGuiButtonToggle) new MGuiButtonToggle(modularGui){
                @Override
                public List<String> getToolTip() {
                    return Collections.singletonList(detectHostile.isPressed() ? I18n.format("gui.de.button.toggleOff") : I18n.format("gui.de.button.toggleOn"));
                }

                @Override
                public boolean isPressed() {
                    return filter.detectHostile;
                }
            }.setListener(this).setDisplayString(I18n.format("gui.entityFilter.button.hostile")));
            detectHostile.toolTipDelay = 5;
        }
        if (filter.isTypeSelectionEnabled()) {
            builder.add(detectPassive = (MGuiButtonToggle) new MGuiButtonToggle(modularGui){
                @Override
                public List<String> getToolTip() {
                    return Collections.singletonList(detectPassive.isPressed() ? I18n.format("gui.de.button.toggleOff") : I18n.format("gui.de.button.toggleOn"));
                }

                @Override
                public boolean isPressed() {
                    return filter.detectPassive;
                }
            }.setListener(this).setDisplayString(I18n.format("gui.entityFilter.button.passive")));
            detectPassive.toolTipDelay = 5;
        }
        if (filter.isTypeSelectionEnabled()) {
            builder.add(detectPlayer = (MGuiButtonToggle) new MGuiButtonToggle(modularGui){
                @Override
                public List<String> getToolTip() {
                    return Collections.singletonList(detectPlayer.isPressed() ? I18n.format("gui.de.button.toggleOff") : I18n.format("gui.de.button.toggleOn"));
                }

                @Override
                public boolean isPressed() {
                    return filter.detectPlayer;
                }
            }.setListener(this).setDisplayString(I18n.format("gui.entityFilter.button.players")));
            detectPlayer.toolTipDelay = 5;
        }
        if (filter.isOtherSelectorEnabled()) {
            builder.add(detectOther = (MGuiButtonToggle) new MGuiButtonToggle(modularGui){
                @Override
                public List<String> getToolTip() {
                    return Collections.singletonList(detectOther.isPressed() ? I18n.format("gui.de.button.toggleOff") : I18n.format("gui.de.button.toggleOn"));
                }

                @Override
                public boolean isPressed() {
                    return filter.detectOther;
                }
            }.setListener(this).setDisplayString(I18n.format("gui.entityFilter.button.other")));
            detectOther.toolTipDelay = 5;
        }

        addChildren(builder.finish());
        addExtraElements(this);

        if (filter.isListEnabled()) {
            addChild(addEntity = new MGuiButton(modularGui, xPos, yPos + ySize - 12, 46, 12, "+Entity").setListener(this));
            addChild(addPlayer = new MGuiButton(modularGui, addEntity.xPos + addEntity.xSize + 1, yPos + ySize - 12, 46, 12, "+Player").setListener(this));
            addChild(addCustom = new MGuiButton(modularGui, addPlayer.xPos + addPlayer.xSize + 1, yPos + ySize - 12, 46, 12, "+Custom").setListener(this));

            addChild(listLabel = new MGuiLabel(modularGui, xPos, builder.builderEndY + 4, xSize, 14, "") {
                @Override
                public String getDisplayString() {
                    return filter.isWhiteList ? I18n.format("gui.entityFilter.button.whiteList") : I18n.format("gui.entityFilter.button.blackList");
                }
            });
            addChild(toggleList = new MGuiButton(modularGui, xPos + xSize - 45, listLabel.yPos + 1, 45, listLabel.ySize - 2, I18n.format("gui.de.button.toggle")){
                @Override
                public List<String> getToolTip() {
                    return Collections.singletonList(filter.isWhiteList ? I18n.format("gui.entityFilter.button.blackList") : I18n.format("gui.entityFilter.button.whiteList"));
                }
            }.setListener(this));
            addChild(list = new MGuiList(modularGui, xPos, listLabel.yPos + listLabel.ySize, xSize - 1, ySize - (listLabel.yPos - yPos + listLabel.ySize) - 13));
            listLabel.addChild(new MGuiBorderedRect(modularGui, list.xPos, list.yPos, list.xSize + 1, list.ySize).setBorderColour(0xFF000000).setFillColour(0x30000000));
        }

        super.initElement();
    }

    public void addExtraElements(MGuiEntityFilter filter) {}

    @Override
    public void onMGuiEvent(String eventString, MGuiElementBase eventElement) {
        if (eventElement == toggleList) {
            filter.isWhiteList = !filter.isWhiteList;
        }
        else if (eventElement == detectHostile) {
            filter.detectHostile = !filter.detectHostile;
        }
        else if (eventElement == detectPassive) {
            filter.detectPassive = !filter.detectPassive;
        }
        else if (eventElement == detectPlayer) {
            filter.detectPlayer = !filter.detectPlayer;
        }
        else if (eventElement == detectOther) {
            filter.detectOther = !filter.detectOther;
        }
        //region Entity Selector
        else if ((eventElement == addEntity || eventElement == addPlayer) && list != null) {
            if (selector != null) {
                modularGui.getManager().remove(selector);
                selector = null;
                return;
            }

            selector = new MGuiSelectDialog(modularGui, list.xPos, list.yPos, list.xSize, list.ySize).setListener(this);
            selector.addChild(new MGuiButton(modularGui, xPos, selector.yPos - 12, 40, 12, TextFormatting.DARK_RED + I18n.format("gui.cancel")).setListener(this).setId("SELECT_CANCEL"));
            selector.addChild(new MGuiBorderedRect(modularGui, list.xPos, list.yPos, list.xSize + 1, list.ySize).setFillColour(0xFF909090).setBorderColour(0xFF000000));

            List<MGuiElementBase> elementBases = new ArrayList<>();

            if (eventElement == addEntity) {
                for (Entity entity : getEntityList()) {
                    if (entity == null) {
                        continue;
                    }
                    String name = entity.getDisplayName() == null ? "[unknown]" : entity.getDisplayName().getFormattedText();
                    MGuiElementBase container = new MGuiElementBase(modularGui, 0, 0, xSize - 13, 20);
                    container.setLinkedObject(EntityList.getEntityString(entity));

                    MGuiEntityRenderer renderer = new MGuiEntityRenderer(modularGui, 10, 5, 12, 10).setEntity(entity);
                    container.addChild(renderer);

                    MGuiLabel label = new MGuiLabel(modularGui, 0 + 30, 0, xSize - 42, 20, name).setAlignment(EnumAlignment.LEFT).setTrim(true);
                    if (fontRenderer.getStringWidth(name) > xSize - 40) {
                        label.addChild(new MGuiHoverPopup(modularGui, new String[]{name}, label));
                    }

                    container.addChild(label);

                    MGuiButtonSolid back = new MGuiButtonSolid(modularGui, 0 + 30, 0, xSize - 42, 20, "") {
                        @Override
                        public int getFillColour(boolean hovering, boolean disabled) {
                            return hovering ? 0x80FFFFFF : 0;
                        }

                        @Override
                        public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
                            return false;
                        }
                    }.setColours(0, 0, 0xFFFFFFFF);
                    container.addChild(back);
                    elementBases.add(container);
                }
            }
            else if (eventElement == addPlayer) {
                for (String name : playerNames) {
                    name = "[player]:"+name;
                    MGuiLabel label = new MGuiLabel(modularGui, 0, 0, xSize - 12, 20, name).setAlignment(EnumAlignment.LEFT).setTrim(true);
                    if (fontRenderer.getStringWidth(name) > xSize - 40) {
                        label.addChild(new MGuiHoverPopup(modularGui, new String[] {name}, label));
                    }
                    label.setLinkedObject(name);
                    MGuiButtonSolid button = new MGuiButtonSolid(modularGui, 0, 0, xSize - 12, 20, ""){
                        @Override
                        public int getFillColour(boolean hovering, boolean disabled) {
                            return hovering ? 0x80FFFFFF : 0;
                        }

                        @Override
                        public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
                            return false;
                        }
                    }.setColours(0, 0, 0xFFFFFFFF);
                    label.addChild(button);
                    elementBases.add(label);
                }
            }


            selector.setOptions(elementBases);
            selector.initElement();
            modularGui.getManager().add(selector, displayLevel + 1);
            return;
        }
        //endregion
        else if (eventElement == addCustom) {
            if (selector != null) {
                modularGui.getManager().remove(selector);
                selector = null;
            }

            MGuiPopupTextField textField = (MGuiPopupTextField) new MGuiPopupTextField(modularGui, xPos + (xSize / 2) - 50, yPos + (ySize / 2) - 6, 100, 12, this).setId("ADD_CUSTOM");
            textField.addChild(new MGuiHoverPopup(modularGui, new String[] {I18n.format("gui.entityFilter.customTip.txt")}, textField));
            textField.show();
        }
        else if (eventString.equals("SELECTOR_PICK")) {
            if (eventElement.linkedObject instanceof String) {
                String name = (String) eventElement.linkedObject;
                filter.entityList.add(name);
                modularGui.getManager().remove(selector);
                selector = null;
            }
        }
        else if (eventElement.id.equals("SELECT_CANCEL")) {
            modularGui.getManager().remove(selector);
            selector = null;
            return;
        }
        else if (eventElement instanceof MGuiButton && ((MGuiButton) eventElement).buttonName.equals("REMOVE_ENTRY")) {
            if (eventElement.linkedObject instanceof String) {
                String name = (String) eventElement.linkedObject;
                filter.entityList.remove(name);
            }
        }
        else if (eventElement.id.equals("ADD_CUSTOM")) {
            if (!StringUtils.isNullOrEmpty(eventString)) {
                filter.entityList.add(eventString);
            }
        }
        else if (eventElement instanceof MGuiButton && ((MGuiButton) eventElement).buttonName.equals("EDIT_ENTRY")) {
            if (eventElement.linkedObject instanceof String) {
                String name = (String) eventElement.linkedObject;
                MGuiPopupTextField textField = (MGuiPopupTextField) new MGuiPopupTextField(modularGui, xPos + (xSize / 2) - 50, yPos + (ySize / 2) - 6, 100, 12, this).setId("EDIT_RESULT");
                textField.textField.setText(name);
                textField.setLinkedObject(name);
                textField.addChild(new MGuiHoverPopup(modularGui, new String[] {I18n.format("gui.entityFilter.customTip.txt")}, textField));
                textField.show();
            }
        }
        else if (eventElement.id.equals("EDIT_RESULT")) {
            if (!StringUtils.isNullOrEmpty(eventString) && eventElement.linkedObject instanceof String) {
                String name = (String) eventElement.linkedObject;
                if (filter.entityList.contains(name)) {
                    filter.entityList.remove(name);
                    filter.entityList.add(eventString);
                }
            }
        }

        filter.sendConfigToServer();
    }

    private List<Entity> getEntityList() {
        if (entityList == null) {
            entityList = new LinkedList<>();
            ArrayList<Entity> listCanRender = new ArrayList<>();
            ArrayList<Entity> listCantRender = new ArrayList<>();

            for (String name : EntityList.getEntityNameList()) {
                Entity entity = EntityList.createEntityByName(name, modularGui.getMinecraft().theWorld);
                if (entity == null) {
                    continue;
                }

                if (entity instanceof EntityItem) {
                    ((EntityItem) entity).setEntityItemStack(new ItemStack(Items.APPLE));
                    entity.setCustomNameTag("ItemStack");
                }

                try {
                    entity.getName();
                }
                catch (Throwable e) {
                    continue;
                }

                if (entity.isNonBoss() && (entity instanceof EntityLivingBase || entity instanceof EntityMinecart)) {
                    listCanRender.add(entity);
                }
                else {
                    listCantRender.add(entity);
                }
            }

            Iterator<Entity> i1 = listCanRender.iterator();
            Iterator<Entity> i2 = listCantRender.iterator();

            while (i1.hasNext() || i2.hasNext()) {
                if (i1.hasNext()) {
                    entityList.add(i1.next());
                }
                if (i2.hasNext()) {
                    entityList.add(i2.next());
                }
            }
        }
        return entityList;
    }

    @Override
    public boolean onUpdate() {
        if (list != null) {
            if (list.listEntries.size() != filter.entityList.size() || filter.entityList.hashCode() != lastTickList.hashCode()) {
                lastTickList.clear();
                lastTickList.addAll(filter.entityList);
                list.clear();
                for (String name : filter.entityList) {
                    MGuiLabel label = new MGuiLabel(modularGui, 0, 0, xSize - 35, 12, name).setAlignment(EnumAlignment.LEFT).setTrim(true);
                    label.addChild(new MGuiButtonSolid(modularGui, "REMOVE_ENTRY", list.xSize - 22, 1, 10, 9, TextFormatting.RED + "x").setToolTip(new String[]{I18n.format("generic.remove.txt")}).setListener(this).setLinkedObject(name));
                    label.addChild(new MGuiButtonSolid(modularGui, "EDIT_ENTRY", list.xSize - 32, 1, 10, 9, TextFormatting.GREEN + "e").setToolTip(new String[]{I18n.format("generic.edit.txt")}).setListener(this).setLinkedObject(name));

                    if (fontRenderer.getStringWidth(name) > xSize - 35) {
                        label.addChild(new MGuiHoverPopup(modularGui, new String[]{name}, label));
                    }

                    list.addEntry(new MGuiListEntryWrapper(modularGui, label));
                }
            }
        }

        return super.onUpdate();
    }

    public void onClose() {
        if (selector != null) {
            modularGui.getManager().remove(selector);
            selector = null;
        }
    }
}
