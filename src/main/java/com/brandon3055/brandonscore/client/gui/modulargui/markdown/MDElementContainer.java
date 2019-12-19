package com.brandon3055.brandonscore.client.gui.modulargui.markdown;

import com.brandon3055.brandonscore.client.gui.modulargui.GuiElement;
import com.brandon3055.brandonscore.client.gui.modulargui.markdown.mdelements.MDElementBase;
import com.brandon3055.brandonscore.client.gui.modulargui.markdown.mdelements.MarkerElement;
import com.brandon3055.brandonscore.client.gui.modulargui.markdown.reader.lib.HAlign;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * Created by brandon3055 on 5/31/2018.
 * This is the main container component that will contain all of the markdown elements for the page.
 * This container will define the entire page width and height with its height dependent on the contained content.
 * The only time there should ever be a sub container in this container is as part of a table because each cell
 * in a table is an MDElementContainer.
 */
public class MDElementContainer extends GuiElement<MDElementContainer> {

    private LinkedList<MDElementBase> elements = new LinkedList<>();
    protected BiConsumer<String, Integer> linkClickCallback = null;
    /**
     * When the user hovers their cursor over a link the link target will be displayed in the bottom left corner of this element.
     * Similar to what most browsers do when you hover over a link.
     */
    public GuiElement linkDisplayTarget = this;
    public int linkDisplayZOffset = 600;
    public MDElementFactory lastFactory = null;
    public HAlign defaultAlignment = HAlign.LEFT;
    private MDElementContainer parentContainer = null;

    /**
     * @param initializer can be any initialized MGuiElementBase. This is just used for initialize this element before layout occurs.
     */
    public MDElementContainer(GuiElement initializer) {
        initializeElementData(initializer);
        reportYSizeChange = true;
    }

    /**
     * @param initializer can be any initialized MGuiElementBase. This is just used for initialize this element before layout occurs.
     */
    public MDElementContainer(GuiElement initializer, int xPos, int yPos, int xSize) {
        super(xPos, yPos);
        initializeElementData(initializer);
        setXSize(xSize);
        reportYSizeChange = true;
    }

    /**
     * @param initializer can be any initialized MGuiElementBase. This is just used for initialize this element before layout occurs.
     */
    public MDElementContainer(GuiElement initializer, int xSize) {
        initializeElementData(initializer);
        setXSize(xSize);
        reportYSizeChange = true;
    }

    @Override
    public void reloadElement() {
        super.reloadElement();
//        requiresUpdate = true;
//        if (onReload != null) {
//            onReload.accept(this);
//        }
//
//        LayoutHelper layout = new LayoutHelper(getInsetRect().x, getInsetRect().y, getInsetRect().width);
//        List<MDElementBase> currentLine = new ArrayList<>();
//
//        //Layout Pass
//        for (MDElementBase element : elements) {
//            boolean endOfLine = false;
//            if (element instanceof MarkerElement) {
//                if (((MarkerElement) element).getType() == MarkerElement.Type.NEW_LINE) {
//                    endOfLine = true;
//                }
//            }
//
//            element.layoutElement(layout, currentLine);
//            if (endOfLine) {
//                currentLine.clear();
//            }
//            else {
//                currentLine.add(element);
//            }
//        }
//
//        currentLine.clear();
//        HAlign currentAlignment = HAlign.LEFT;
//
//        //Alignment Pass
//        for (MDElementBase element : getDisplayElements()) {
//            boolean endOfLine = false;
//            if (element instanceof MarkerElement) {
//                if (((MarkerElement) element).isAlign()){
//                    currentAlignment = ((MarkerElement) element).getAlign();
//                }
//                else if (((MarkerElement) element).getType() == MarkerElement.Type.NEW_LINE && currentAlignment != HAlign.LEFT) {
//                    int lineWidth = 0;
//                    for (MDElementBase lineElement : currentLine) {
//                        lineWidth += lineElement.xSize();
//                    }
//                    int offset = layout.getWidth() - lineWidth;
//                    if (currentAlignment == HAlign.CENTER) {
//                        offset /= 2;
//                    }
//                    for (MDElementBase lineElement : currentLine) {
//                        lineElement.translate(offset, 0);
//                    }
//                    endOfLine = true;
//                }
//            }
//
//            if (endOfLine) {
//                currentLine.clear();
//            }
//            else {
//                currentLine.add(element);
//            }
//        }
//
//        setYSize(layout.getContainerHeight());
//        LogHelperBC.dev("Y-Size: " + ySize()+", Parent: " + ((GuiScrollElement) getParent()).getScrollBounds());
//        for (MGuiElementBase element : childElements) {
//            element.reloadElement();
//        }
    }

    public void layoutMarkdownElements() {
        LayoutHelper layout = new LayoutHelper(getInsetRect().x, getInsetRect().y, getInsetRect().width);
        List<MDElementBase> currentLine = new ArrayList<>();

        //Layout Pass
        for (MDElementBase element: elements) {
            boolean endOfLine = false;
            if (element instanceof MarkerElement) {
                if (((MarkerElement) element).getType() == MarkerElement.Type.NEW_LINE) {
                    endOfLine = true;
                }
            }

            element.layoutElement(layout, currentLine);
            if (endOfLine) {
                currentLine.clear();
            }
            else {
                currentLine.add(element);
            }
        }

        currentLine.clear();
        HAlign currentAlignment = defaultAlignment;

        //Alignment Pass
        LinkedList<MDElementBase> display = getDisplayElements();
        int lineY = getInsetRect().y;
        MDElementBase last = display.isEmpty() ? null : display.getLast(); //Used to find the end of the md
        for (int i = 0; i < display.size(); i++) {
            MDElementBase element = display.get(i);
            boolean endOfLine = false;
            boolean newLine = element instanceof MarkerElement && ((MarkerElement) element).getType() == MarkerElement.Type.NEW_LINE;

            if (element instanceof MarkerElement && !newLine && element != last) {
                if (((MarkerElement) element).isAlign()) {
                    currentAlignment = ((MarkerElement) element).getAlign();
                }
            }
            else if (element.yPos() > lineY || last == element || newLine) {
                if (currentAlignment != HAlign.LEFT) {
                    int lineWidth = 0;
                    for (MDElementBase lineElement : currentLine) {
                        lineWidth += lineElement.xSize();
                    }
                    int offset = layout.getWidth() - lineWidth;
                    if (currentAlignment == HAlign.CENTER) {
                        offset /= 2;
                    }
                    for (MDElementBase lineElement : currentLine) {
                        lineElement.translate(offset, 0);
                    }
                }
                lineY = element.yPos();
                endOfLine = true;
            }

            if (endOfLine) {
                currentLine.clear();
            }

            currentLine.add(element);
        }

//  The marker based system had issues because in order for it to work properly the layout manager would have to be in charge
//  of adding the markers but that isn't as simple as it sounds because text elements need to set their own markers.
//  The new method should fix this but i'm keeping this here in case i run into issues and need to revert.
//        for (MDElementBase element : getDisplayElements()) {
//            boolean endOfLine = false;
//            if (element instanceof MarkerElement) {
//                if (((MarkerElement) element).isAlign()){
//                    currentAlignment = ((MarkerElement) element).getAlign();
//                }
//                else if (((MarkerElement) element).getType() == MarkerElement.Type.NEW_LINE) {
//                    if (currentAlignment != HAlign.LEFT) {
//                        int lineWidth = 0;
//                        for (MDElementBase lineElement : currentLine) {
//                            lineWidth += lineElement.xSize();
//                        }
//                        int offset = layout.getWidth() - lineWidth;
//                        if (currentAlignment == HAlign.CENTER) {
//                            offset /= 2;
//                        }
//                        for (MDElementBase lineElement : currentLine) {
//                            lineElement.translate(offset, 0);
//                        }
//                    }
//                    endOfLine = true;
//                }
//            }
//
//            if (endOfLine) {
//                currentLine.clear();
//            }
//            else {
//                currentLine.add(element);
//            }
//        }

        setYSize(layout.getContainerHeight() + getInsets().bottom + getInsets().top);
    }

    public void addElement(MDElementBase element) {
        if (!(element instanceof MarkerElement)) {
            addChild(element);
        }
        elements.add(element);
    }

    public LinkedList<MDElementBase> getElements() {
        return elements;
    }

    public void clearContainer() {
        toRemove.addAll(elements);
        elements.clear();
    }

    /**
     * @return a list of all display elements. In the case of elements such as text and recipe's this will add
     * the sub elements for those elements but not the elements themselves.
     */
    public LinkedList<MDElementBase> getDisplayElements() {
        LinkedList<MDElementBase> displayElements = new LinkedList<>();
        for (MDElementBase element: elements) {
            if (element.hasSubParts) {
                displayElements.addAll(element.subParts);
            }
            else {
                displayElements.add(element);
            }
        }
        return displayElements;
    }

    @Override
    public boolean onUpdate() {
        return super.onUpdate();
    }

    @Override
    public void renderElement(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
        super.renderElement(minecraft, mouseX, mouseY, partialTicks);
        GlStateManager.color(1, 1, 1, 1);
//        drawBorderedRect(xPos(), yPos(), xSize(), ySize(), 1, 0, 0x8000FFFF);
//        drawBorderedRect(getInsetRect().x, getInsetRect().y, getInsetRect().width, getInsetRect().height, 1, 0, 0x8000FF00);
    }

    @Override
    public void ySizeChanged(GuiElement elementChanged) {
        //The parent scroll element does not need to be informed when markdown elements have their size assigned.
        if (elementChanged == this) {
            super.ySizeChanged(elementChanged);
        }
    }

    public void handleLinkClick(String linkURL, int button) {
        if (linkClickCallback != null) {
            linkClickCallback.accept(linkURL, button);
        }
    }

    public void setLinkClickCallback(BiConsumer<String, Integer> linkClickCallback) {
        this.linkClickCallback = linkClickCallback;
    }

    public void inherit(MDElementContainer parent) {
        linkClickCallback = parent.linkClickCallback;
        linkDisplayTarget = parent.linkDisplayTarget;
        linkDisplayZOffset = parent.linkDisplayZOffset;
        parentContainer = parent;
    }

    public MDElementContainer getTopLevelContainer() {
        return parentContainer == null ? this : parentContainer.getTopLevelContainer();
    }
}
