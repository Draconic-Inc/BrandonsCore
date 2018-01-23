package com.brandon3055.brandonscore.client.gui.modulargui.baseelements;

import com.brandon3055.brandonscore.client.gui.modulargui.MGuiElementBase;
import com.brandon3055.brandonscore.client.gui.modulargui.lib.GuiEvent;
import com.brandon3055.brandonscore.client.gui.modulargui.lib.IGuiEventDispatcher;
import com.brandon3055.brandonscore.client.gui.modulargui.lib.IGuiEventListener;
import com.brandon3055.brandonscore.lib.ScissorHelper;
import com.brandon3055.brandonscore.utils.DataUtils;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

import java.awt.*;
import java.io.IOException;
import java.util.LinkedList;

import static com.brandon3055.brandonscore.client.gui.modulargui.baseelements.GuiScrollElement.ListMode.DISABLED;
import static com.brandon3055.brandonscore.client.gui.modulargui.baseelements.GuiSlideControl.SliderRotation.VERTICAL;

/**
 * Created by brandon3055 on 4/07/2017.
 */
public class GuiScrollElement extends MGuiElementBase<GuiScrollElement> implements IGuiEventListener {
//public class GuiScrollElement<T extends com.brandon3055.brandonscore.client.gui.modulargui.baseelements.GuiScrollElement<T>> extends MGuiElementBase<T> implements IGuiEventListener {
    public static final String DEFAULT_SCROLL_BAR_GROUP = "GuiScrollElement_Default_Scroll_Bars";

    protected int listSpacing = 0;
    /**
     * For internal use only! Setting this manually will break scrolling.
     */
    protected int verticalScrollPos = 0;
    /**
     * For internal use only! Setting this manually will break scrolling.
     */
    protected int horizontalScrollPos = 0;

    protected int animSpeed = 1;
    protected Insets defaultInsets = new Insets(0, 0, 0, 0);
    protected boolean smoothScroll = false;
    protected boolean enableVerticalScroll = true;
    protected boolean useAbsoluteElementSize = false;
    protected boolean enableHorizontalScroll = true;
    protected boolean scrollBarExclusionMode = true;
    protected ListMode listMode = DISABLED;
    protected Rectangle scrollBounds = new Rectangle();
    protected MGuiElementBase backgroundElement = null;
    protected GuiSlideControl verticalScrollBar = null;
    protected GuiSlideControl horizontalScrollBar = null;

    protected LinkedList<MGuiElementBase> scrollingElements = new LinkedList<>();
    protected LinkedList<MGuiElementBase> foregroundElements = new LinkedList<>();
    protected LinkedList<MGuiElementBase> backgroundElements = new LinkedList<>();

    public GuiScrollElement() { }

    public GuiScrollElement(int xPos, int yPos) {
        super(xPos, yPos);
    }

    public GuiScrollElement(int xPos, int yPos, int xSize, int ySize) {
        super(xPos, yPos, xSize, ySize);
    }

    @Override
    public void reloadElement() {
        super.reloadElement();
        resetScrollPositions();
        updateScrollElement();
        resetScrollPositions();
    }

    //region Scroll Bar Configuration

    /**
     * Allows you to set the allowed scrolling axes.
     *
     * @param vertical   Allow vertical scrolling (Default true)
     * @param horizontal Allow horizontal scrolling (Default true)
     */
    public GuiScrollElement setAllowedScrollAxes(boolean vertical, boolean horizontal) {
        this.enableVerticalScroll = vertical;
        this.enableHorizontalScroll = horizontal;
        return this;
    }

    /**
     * Allows you to set a custom scroll bar for the vertical axis.
     */
    public GuiScrollElement setVerticalScrollBar(GuiSlideControl verticalScrollBar) {
        if (this.verticalScrollBar != null) {
            removeChild(this.verticalScrollBar);
        }
        this.verticalScrollBar = verticalScrollBar;
        this.verticalScrollBar.setListener(this);
        addChild(this.verticalScrollBar);
        return this;
    }

    public GuiSlideControl getVerticalScrollBar() {
        validateScrollBarExistence();
        return verticalScrollBar;
    }

    /**
     * Allows you to set a custom scroll bar for the horizontal axis.
     */
    public GuiScrollElement setHorizontalScrollBar(GuiSlideControl horizontalScrollBar) {
        if (this.horizontalScrollBar != null) {
            removeChild(this.horizontalScrollBar);
        }
        this.horizontalScrollBar = horizontalScrollBar;
        this.horizontalScrollBar.setListener(this);
        addChild(this.horizontalScrollBar);
        return this;
    }

    public GuiSlideControl getHorizontalScrollBar() {
        validateScrollBarExistence();
        return horizontalScrollBar;
    }

    /**
     * Enabled by default.
     * When enabled the scrolling area will be adjusted to not include the scroll bars.
     * Supports scroll bars positioned at the left, right, top or bottom of the element.
     */
    public GuiScrollElement setScrollBarExclusionMode(boolean scrollBarExclusionMode) {
        this.scrollBarExclusionMode = scrollBarExclusionMode;
        return this;
    }

    public GuiScrollElement setSmoothScroll(boolean smoothScroll, int speed) {
        this.smoothScroll = smoothScroll;
        animSpeed = speed;
        return this;
    }

    /**
     * If enabled the size of the element plus all of its children will be used when positioning elements.
     */
    public GuiScrollElement useAbsoluteElementSize(boolean useAbsoluteElementSize) {
        this.useAbsoluteElementSize = useAbsoluteElementSize;
        return this;
    }

    /**
     * Applies standard mouse scroll wheel behavior for the scroll bars.
     * Vertical scrolling when cursor is over the GuiScrollElement or the vertical scroll bar.
     * Horizontal scrolling when cursor is over the GuiScrollElement and shift key is pressed
     * or the cursor is over the horizontal scroll bar.
     * This also enables the ability to middle click and drag the scrolling area.
     */
    public GuiScrollElement setStandardScrollBehavior() {
        validateScrollBarExistence();
        verticalScrollBar.setParentScroll(true);
        verticalScrollBar.allowMiddleClickDrag(true);
        verticalScrollBar.clearScrollChecks();
        verticalScrollBar.addScrollCheck((slider, mouseX, mouseY) -> slider.isMouseOver(mouseX, mouseY) || !GuiScreen.isShiftKeyDown());
        horizontalScrollBar.setParentScroll(true);
        horizontalScrollBar.allowMiddleClickDrag(true);
        horizontalScrollBar.clearScrollChecks();
        horizontalScrollBar.addScrollCheck((slider, mouseX, mouseY) -> slider.isMouseOver(mouseX, mouseY) || GuiScreen.isShiftKeyDown());
        return this;
    }

    /**
     * Adjusts this scroll element's scissor area. Note this has no effect on the positioning of
     * elements themselves. So if for example you add an inset of 10 to the left side and you place your
     * elements at a relative x position of 0 then your elements will be at a position of -10 relative
     * to the scissor area. Meaning you will need to scroll left to view the 10 pixels that are within the scissor area.
     */
    @Override
    public GuiScrollElement setInsets(int top, int left, int bottom, int right) {
        defaultInsets.set(top, left, bottom, right);
        return super.setInsets(top, left, bottom, right);
    }

    @Override
    public GuiScrollElement setInsets(Insets insets) {
        defaultInsets = new Insets(insets.top, insets.left, insets.bottom, insets.right);
        return super.setInsets(insets);
    }

    //endregion

    //region Scrolling Elements

    /**
     * This is the method you must use to add a child element that is to be managed by this ScrollElement.
     * You can still add child elements however these will not be managed by the ScrollElement and will
     * just remain as static elements wherever you put them.
     *
     * @param element
     * @see #setListMode(ListMode)
     */
    public GuiScrollElement addElement(MGuiElementBase element) {
        scrollingElements.add(element);
        super.addChild(element);
        updateScrollElement();
        return this;
    }

    public GuiScrollElement removeElement(MGuiElementBase element) {
        removeChild(element);
        updateScrollElement();
        return this;
    }

    @Override
    public <C extends MGuiElementBase> C removeChild(C child) {
        scrollingElements.remove(child);
        backgroundElements.remove(child);
        return super.removeChild(child);
    }

    /**
     * Add a child element that will NOT be bound to the scrollable area.
     * To add actual scrollable elements use {@link #addElement(MGuiElementBase)}
     * This should only be used to add things like buttons or scroll bars which need
     * to be excluded from the scrolling logic, Though if adding scroll bars you
     * should use {@link #setVerticalScrollBar(GuiSlideControl)} or {@link #setHorizontalScrollBar(GuiSlideControl)}<br>
     * Note: child elements will always be rendered after scrollable elements and so will render on top in most cases.
     * If you need to add an element that renders before the scrolling elements use {@link #addBackgroundChild(MGuiElementBase)}
     */
    @Override
    public final <C extends MGuiElementBase> C addChild(C child) {
        foregroundElements.add(child);
        return super.addChild(child);
    }

    /**
     * Use this method to add any child elements that need to render BEFORE the scrolling elements.
     */
    public <C extends MGuiElementBase> C addBackgroundChild(C child) {
        backgroundElements.add(child);
        return super.addChild(child);
    }

    /**
     * Removes all scrolling elements.
     */
    public void clearElements() {
        scrollingElements.forEach(super::removeChild);
        scrollingElements.clear();
        updateScrollElement();
    }

    /**
     * @return the list of scrolling elements as an Immutable list.
     */
    public ImmutableList<MGuiElementBase> getScrollingElements() {
        return ImmutableList.copyOf(scrollingElements);
    }

    protected int elementXSize(MGuiElementBase element) {
        if (useAbsoluteElementSize) {
            return element.getEnclosingRect().width;
        }
        return element.xSize();
    }

    protected int elementYSize(MGuiElementBase element) {
        if (useAbsoluteElementSize) {
            return element.getEnclosingRect().height;
        }
        return element.ySize();
    }

    //endregion

    //region Scrolling Logic

    /**
     * Updates the position of all size/pos of all contained elements if in list mode
     * then recalculates the bounds of the scrolling area.
     * And finally updates the scroll bars.
     */
    public void updateScrollElement() {
        if (listMode != DISABLED) {
            int lastPos = listMode.horizontal() ? getInsetRect().x : getInsetRect().y;
            for (MGuiElementBase element : scrollingElements) {
                if (!element.isEnabled() || element == backgroundElement) continue;
                if (listMode.horizontal()) {
                    if (listMode.lockPos()) {
                        element.setYPos(getInsetRect().y);
                    }
                    if (listMode.lockWidth()) {
                        element.setYSize(getInsetRect().height);
                    }
                    element.setXPos(lastPos);
                    lastPos += elementXSize(element) + listSpacing;
                }
                else {
                    if (listMode.lockPos()) {
                        element.setXPos(getInsetRect().x);
                    }
                    if (listMode.lockWidth()) {
                        element.setXSize(getInsetRect().width);
                    }
                    element.setYPos(lastPos);
                    lastPos += elementYSize(element) + listSpacing;
                }
            }
        }

        updateScrollBounds();
        updateScrollbars();
        if (backgroundElement != null) {
            backgroundElement.setPosAndSize(scrollBounds);
        }
    }

    protected void updateScrollBounds() {
        int xMin = getInsetRect().x;
        int xMax = xMin;
        int yMin = getInsetRect().y;
        int yMax = yMin;

        if (DataUtils.firstMatch(scrollingElements, elementBase -> elementBase.isEnabled() || elementBase != backgroundElement) == null) {
            xMin = xPos();
            xMax = maxXPos();
            yMin = yPos();
            yMax = maxYPos();
        }
        else {
            for (MGuiElementBase element : scrollingElements) {
                if (!element.isEnabled() || element == backgroundElement) continue;
                Rectangle rect = element.getEnclosingRect();
                if (rect.x < xMin) xMin = (int) rect.getX();
                if (rect.getMaxX() > xMax) xMax = (int) rect.getMaxX();
                if (rect.y < yMin) yMin = (int) rect.getY();
                if (rect.getMaxY() > yMax) yMax = (int) rect.getMaxY();
            }
        }

        scrollBounds.setBounds(xMin, yMin, xMax - xMin, yMax - yMin);
    }

    /**
     * Adds default scroll bars if no scroll bars have been set then updates the scroll bars as needed.
     */
    public void updateScrollbars() {
        validateScrollBarExistence();
        setInsets(defaultInsets.top, defaultInsets.left, defaultInsets.bottom, defaultInsets.right);

        double verticalMinScroll = Math.min(0, scrollBounds.y - getInsetRect().y);
        double verticalMaxScroll = Math.max(0, scrollBounds.getMaxY() - getInsetRect().getMaxY());
        boolean vertNoExc = Math.abs(verticalMinScroll) + Math.abs(verticalMaxScroll) <= 0;

        double horizontalMinScroll = Math.min(0, scrollBounds.x - getInsetRect().x);
        double horizontalMaxScroll = Math.max(0, scrollBounds.getMaxX() - getInsetRect().getMaxX());
        boolean hozNoExc = Math.abs(horizontalMinScroll) + Math.abs(horizontalMaxScroll) <= 0;

        verticalMinScroll = Math.min(0, scrollBounds.y - getInsetRect().y);
        verticalMaxScroll = Math.max(0, scrollBounds.getMaxY() - getInsetRect().getMaxY());
        boolean vertExc = Math.abs(verticalMinScroll) + Math.abs(verticalMaxScroll) <= 0;

        horizontalMinScroll = Math.min(0, scrollBounds.x - getInsetRect().x);
        horizontalMaxScroll = Math.max(0, scrollBounds.getMaxX() - getInsetRect().getMaxX());
        boolean hozExc = Math.abs(horizontalMinScroll) + Math.abs(horizontalMaxScroll) <= 0;

        boolean canDisVert = vertExc || !enableVerticalScroll; //If true then will be disabled regardless
        boolean canDisHoz = hozExc || !enableHorizontalScroll;   //If true then will be disabled regardless

        if (!canDisVert && vertNoExc) {
            canDisVert = canDisHoz || hozNoExc;
        }

        if (!canDisHoz && hozNoExc) {
            canDisHoz = canDisVert;
        }

        verticalScrollBar.setEnabled(!canDisVert);
        horizontalScrollBar.setEnabled(!canDisHoz && enableHorizontalScroll);
        setInsets(defaultInsets.top, defaultInsets.left, defaultInsets.bottom, defaultInsets.right);
        updateScrollbarExclusion();

        if ((!verticalScrollBar.isEnabled() || verticalScrollBar.isHidden()) && horizontalScrollBar.isEnabled() && horizontalScrollBar.isInGroup(DEFAULT_SCROLL_BAR_GROUP)) {
            horizontalScrollBar.setXSize(xSize()).setYPos(maxYPos() - horizontalScrollBar.ySize()).updateElements();
        }
        else if (horizontalScrollBar.isInGroup(DEFAULT_SCROLL_BAR_GROUP)) {
            horizontalScrollBar.setXSize(xSize() - verticalScrollBar.xSize()).setYPos(maxYPos() - horizontalScrollBar.ySize()).updateElements();
        }

        if ((!horizontalScrollBar.isEnabled() || horizontalScrollBar.isHidden()) && verticalScrollBar.isEnabled() && verticalScrollBar.isInGroup(DEFAULT_SCROLL_BAR_GROUP)) {
            verticalScrollBar.setYSize(ySize()).setXPos(maxXPos() - verticalScrollBar.xSize()).updateElements();
        }
        else if (verticalScrollBar.isInGroup(DEFAULT_SCROLL_BAR_GROUP)) {
            verticalScrollBar.setYSize(ySize() - horizontalScrollBar.ySize()).setXPos(maxXPos() - verticalScrollBar.xSize()).updateElements();
        }

        double contentHeight = Math.abs(verticalMinScroll) + getInsetRect().height + Math.abs(verticalMaxScroll);
        verticalScrollBar.setRange(verticalMinScroll, verticalMaxScroll);
        verticalScrollBar.setScaledSliderSize(getInsetRect().height / contentHeight);
        verticalScrollBar.updatePos(verticalScrollPos, false);

        double contentWidth = Math.abs(horizontalMinScroll) + getInsetRect().width + Math.abs(horizontalMaxScroll);
        horizontalScrollBar.setRange(horizontalMinScroll, horizontalMaxScroll);
        horizontalScrollBar.setScaledSliderSize(getInsetRect().width / contentWidth);
        horizontalScrollBar.updatePos(horizontalScrollPos, false);
    }


    protected void updateScrollbarExclusion() {
        if (scrollBarExclusionMode) {
            //Check if the scroll bar is on the right side of the element
            if (verticalScrollBar.isEnabled() && !verticalScrollBar.isHidden()) {
                if (verticalScrollBar.getRect().getCenterX() > getRect().getCenterX()) {
                    if (getInsetRect().getMaxX() > verticalScrollBar.xPos()) {
                        getInsets().right = maxXPos() - verticalScrollBar.xPos();
                    }
                }
                else {
                    if (getInsetRect().getX() < verticalScrollBar.maxXPos()) {
                        getInsets().left = verticalScrollBar.maxXPos() - xPos();
                    }
                }
            }
            //Check if the scroll bar is in the bottom half of the element
            if (horizontalScrollBar.isEnabled() && !horizontalScrollBar.isHidden()) {
                if (horizontalScrollBar.getRect().getCenterY() > getRect().getCenterY()) {
                    if (getInsetRect().getMaxY() > horizontalScrollBar.yPos()) {
                        getInsets().bottom = maxYPos() - horizontalScrollBar.yPos();
                    }
                }
                else {
                    if (getInsetRect().getY() < horizontalScrollBar.maxYPos()) {
                        getInsets().top = horizontalScrollBar.maxYPos() - yPos();
                    }
                }
            }
        }
    }

    /**
     * Checks the existence of each scroll bar and adds the default scroll bab if no scroll bar has been added.
     * Default scroll bars will be at the bottom and left sides of the element.
     * Default sliders will use white and black for the background and 2 shades of grey for the slider knob.
     */
    private void validateScrollBarExistence() {
        if (verticalScrollBar == null) {
            setVerticalScrollBar(new GuiSlideControl(VERTICAL).setPos(maxXPos() - 10, yPos()).setSize(10, ySize() - 10).setDefaultBackground(0xFF000000, 0xFFFFFFFF).setDefaultSlider(0xFFA0A0A0, 0xFF707070));
            verticalScrollBar.addToGroup(DEFAULT_SCROLL_BAR_GROUP);
        }
        if (horizontalScrollBar == null) {
            setHorizontalScrollBar(new GuiSlideControl().setPos(xPos(), maxYPos() - 10).setSize(xSize() - 10, 10).setDefaultBackground(0xFF000000, 0xFFFFFFFF).setDefaultSlider(0xFFA0A0A0, 0xFF707070));
            horizontalScrollBar.addToGroup(DEFAULT_SCROLL_BAR_GROUP);
        }
    }

    /**
     * Resets the scroll positions back to the default positions.
     */
    public void resetScrollPositions() {
        validateScrollBarExistence();
        verticalScrollBar.updatePos(0);
        horizontalScrollBar.updatePos(0);
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (getInsetRect().contains(mouseX, mouseY)) {
            for (MGuiElementBase element : scrollingElements) {
                if (element.isEnabled() && element.mouseClicked(mouseX, mouseY, mouseButton)) {
                    return true;
                }
            }
        }

        for (MGuiElementBase element : foregroundElements) {
            if (element.isEnabled() && !scrollingElements.contains(element) && element.mouseClicked(mouseX, mouseY, mouseButton)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        for (MGuiElementBase element : scrollingElements) {
            if (element.isEnabled() && element.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick)) {
                return true;
            }
        }

        for (MGuiElementBase element : foregroundElements) {
            if (element.isEnabled() && !scrollingElements.contains(element) && element.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean handleMouseScroll(int mouseX, int mouseY, int scrollDirection) {
        for (MGuiElementBase element : scrollingElements) {
            if (element.isEnabled() && element.handleMouseScroll(mouseX, mouseY, scrollDirection)) {
                return true;
            }
        }

        for (MGuiElementBase element : foregroundElements) {
            if (element.isEnabled() && !scrollingElements.contains(element) && element.handleMouseScroll(mouseX, mouseY, scrollDirection)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onMGuiEvent(GuiEvent event, MGuiElementBase eventSource) {
        int xAdjustment = 0;
        int yAdjustment = 0;
        if (eventSource == verticalScrollBar) {
            yAdjustment = verticalScrollPos - (int) verticalScrollBar.getPosition();
            verticalScrollPos = (int) verticalScrollBar.getPosition();
        }
        else if (eventSource == horizontalScrollBar) {
            xAdjustment = horizontalScrollPos - (int) horizontalScrollBar.getPosition();
            horizontalScrollPos = (int) horizontalScrollBar.getPosition();
        }

        for (MGuiElementBase scrollableElement : scrollingElements) {
//            scrollableElement.animateMoveFrames();
            if (smoothScroll) {
                scrollableElement.translateAnim(xAdjustment, yAdjustment, animSpeed);
            }
            else {
                scrollableElement.translate(xAdjustment, yAdjustment);
            }
        }
    }

    //endregion

    //region Setup

    /**
     * By default you are not restricted in any way as far as how you can position elements within the scrollable
     * area. You simple place elements wherever you like and the scroll bounds will automatically update to accommodate
     * that element.
     * <p>
     * But maby you want to do something a little simpler like create a simple list of elements and have them automatically
     * positioned and resized to fit your needs. Thats where list mode can help.
     * List mode allows the ScrollElement to operate as ether a horizontal or vertical list.
     * <p>
     * Each mode (horizontal/vertical) has 4 variants that work as follows. <br>
     * Note: the following is assuming vertical mode. In horizontal mode x and y are reversed<br>
     * Default:          With all 4 modes each new element added to the list will be placed below the previous element.<br>
     * LOCK_POS:         The xPosition of the elements is locked to the the left x pos of the scrolling area.<br>
     * LOCK_POS_WIDTH:   LOCK_POS + The width of the element is locked to the width of the scroll area and the horizontal scroll bar is disabled.
     *
     * @param listMode
     */
    public GuiScrollElement setListMode(ListMode listMode) {
        this.listMode = listMode;
        if (listMode != DISABLED) {
            setAllowedScrollAxes(!listMode.horizontal(), listMode.horizontal());
        }
        return this;
    }

    /**
     * For use with list mode. Allows you to ass a space between each element in the list.
     *
     * @see #setListMode(ListMode)
     */
    public GuiScrollElement setListSpacing(int listSpacing) {
        this.listSpacing = listSpacing;
        return this;
    }

    /**
     * Applies an element to be used as the background of this GuiScrollElement.
     * This element will be locked to the same size and position as the scrollable area.
     */
    public GuiScrollElement applyBackgroundElement(MGuiElementBase backgroundElement) {
        if (this.backgroundElement != null) {
            removeChild(this.backgroundElement);
        }
        this.backgroundElement = backgroundElement;
        addElement(this.backgroundElement);
        updateScrollElement();
        return this;
    }

    //endregion

    //region Rendering

    @Override
    public void renderElement(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
        for (MGuiElementBase element : backgroundElements) {
            if (element.isEnabled()) {
                element.renderElement(minecraft, mouseX, mouseY, partialTicks);
            }
        }

        double xPos = getInsetRect().x;
        double yPos = getInsetRect().y;
        double xSize = getInsetRect().width;
        double ySize = getInsetRect().height;

//        LogHelperBC.dev(minecraft.displayHeight +" "+ screenHeight);
        double yResScale = (double) minecraft.displayHeight / (screenHeight);
        double xResScale = (double) minecraft.displayWidth / (screenWidth);
        double scaledWidth = xSize * xResScale;
        double scaledHeight = ySize * yResScale;
        int x = (int) (xPos * xResScale);
        int y = (int) (minecraft.displayHeight - (yPos * yResScale) - scaledHeight);

//        GlStateManager.pushMatrix();
//        double scale = 0.5;
//        GlStateManager.translate(xPos(), yPos(), 0);
//        GlStateManager.scale(scale, scale, 1);
//        GlStateManager.translate(-xPos(), -yPos(), 0);
        ScissorHelper.pushScissor(x, y, (int) scaledWidth, (int) scaledHeight);

        if (backgroundElement != null) {
            backgroundElement.renderElement(minecraft, mouseX, mouseY, partialTicks);
        }

        for (MGuiElementBase element : scrollingElements) {
            if (element.isEnabled() && element != backgroundElement) {
                element.renderElement(minecraft, mouseX, mouseY, partialTicks);
            }
        }

        ScissorHelper.popScissor();
//        GlStateManager.popMatrix();

        for (MGuiElementBase element : foregroundElements) {
            if (element.isEnabled() && !scrollingElements.contains(element)) {
                element.renderElement(minecraft, mouseX, mouseY, partialTicks);
            }
        }

//        drawBorderedRect(scrollBounds.x, scrollBounds.y, scrollBounds.width, scrollBounds.height, 1, 0, 0xFF0000FF);
//        drawBorderedRect(getInsetRect().x, getInsetRect().y, getInsetRect().width, getInsetRect().height, 1, 0, 0xFF00FFFF);
//        drawBorderedRect(getRect().x, getRect().y, getRect().width, getRect().height, 1, 0, 0xFFFF00FF);
    }

    //endregion

    //region Misc

    @Override
    protected void addDefaultListener(MGuiElementBase childElement) {
        if (childElement instanceof IGuiEventDispatcher && ((IGuiEventDispatcher) childElement).getListener() == null) {
            if (getParent() instanceof IGuiEventListener) {
                ((IGuiEventDispatcher) childElement).setListener((IGuiEventListener) getParent());
            }
            else if (modularGui instanceof IGuiEventListener) {
                ((IGuiEventDispatcher) childElement).setListener((IGuiEventListener) modularGui);
            }
        }
    }

    @Override
    public Rectangle addBoundsToRect(Rectangle enclosingRect) {
        int enRectMaxX = (int) enclosingRect.getMaxX();
        int enRectMaxY = (int) enclosingRect.getMaxY();

        if (getRect().x < enclosingRect.x) {
            enclosingRect.x = getRect().x;
            enclosingRect.width = enRectMaxX - enclosingRect.x;
        }
        if (getRect().getMaxX() > enRectMaxX) {
            enclosingRect.width = (int) getRect().getMaxX() - enclosingRect.x;
        }

        if (getRect().y < enclosingRect.y) {
            enclosingRect.y = getRect().y;
            enclosingRect.height = enRectMaxY - enclosingRect.y;
        }
        if (getRect().getMaxY() > enRectMaxY) {
            enclosingRect.height = (int) getRect().getMaxY() - enclosingRect.y;
        }

        for (MGuiElementBase element : childElements) {
            if (!scrollingElements.contains(element)) {
                element.addBoundsToRect(enclosingRect);
            }
        }
        return enclosingRect;
    }

    @Override
    public boolean allowMouseOver(MGuiElementBase elementRequesting, int mouseX, int mouseY) {
        if (scrollingElements.contains(elementRequesting)) {
            return getInsetRect().contains(mouseX, mouseY);
        }
        return super.allowMouseOver(elementRequesting, mouseX, mouseY);
    }

    @Override
    public void xSizeChanged(MGuiElementBase elementChanged) {
        if (scrollingElements.contains(elementChanged) && elementChanged.reportXSizeChange) {
            int vsp = verticalScrollPos;
            int hsp = horizontalScrollPos;
            resetScrollPositions();
            updateScrollElement();
            resetScrollPositions();
            verticalScrollBar.updatePos(vsp);
            horizontalScrollBar.updatePos(hsp);
        }
    }

    @Override
    public void ySizeChanged(MGuiElementBase elementChanged) {
        if (scrollingElements.contains(elementChanged) && elementChanged.reportYSizeChange) {
            int vsp = verticalScrollPos;
            int hsp = horizontalScrollPos;
            resetScrollPositions();
            updateScrollBounds();
            updateScrollElement();
            resetScrollPositions();
            verticalScrollBar.updatePos(vsp);
            horizontalScrollBar.updatePos(hsp);
        }
    }

    //endregion

    public static enum ListMode {
        DISABLED(false, false, false), VERTICAL(false, false, false), VERT_LOCK_POS(false, false, true), VERT_LOCK_POS_WIDTH(false, true, true), HORIZONTAL(true, false, false), HORIZ_LOCK_POS(true, false, true), HORIZ_LOCK_POS_HEIGHT(true, true, true);

        private final boolean horizontal;
        private final boolean lockWidth;
        private final boolean lockPos;

        ListMode(boolean horizontal, boolean lockWidth, boolean lockPos) {
            this.horizontal = horizontal;
            this.lockWidth = lockWidth;
            this.lockPos = lockPos;
        }

        public boolean horizontal() {
            return horizontal;
        }

        public boolean lockPos() {
            return lockPos;
        }

        public boolean lockWidth() {
            return lockWidth;
        }
    }
}