package com.brandon3055.brandonscore.client.gui.modulargui.baseelements;

import com.brandon3055.brandonscore.client.gui.modulargui.GuiElement;
import com.brandon3055.brandonscore.client.gui.modulargui.lib.GuiEvent;
import com.brandon3055.brandonscore.client.gui.modulargui.lib.IGuiEventDispatcher;
import com.brandon3055.brandonscore.client.gui.modulargui.lib.IGuiEventListener;
import com.brandon3055.brandonscore.lib.ScissorHelper;
import com.brandon3055.brandonscore.utils.DataUtils;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;

import java.awt.*;
import java.util.LinkedList;

import static com.brandon3055.brandonscore.client.gui.modulargui.baseelements.GuiScrollElement.ListMode.DISABLED;
import static com.brandon3055.brandonscore.client.gui.modulargui.baseelements.GuiSlideControl.SliderRotation.VERTICAL;

/**
 * Created by brandon3055 on 4/07/2017.
 */
public class GuiScrollElement extends GuiElement<GuiScrollElement> implements IGuiEventListener {
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
    protected boolean insetScrollBars = false;
    protected boolean reloadOnUpdate = false;
    protected boolean enableVerticalScroll = true;
    protected boolean useAbsoluteElementSize = false;
    protected boolean enableHorizontalScroll = true;
    protected boolean scrollBarExclusionMode = true;
    protected boolean disableOffScreenElements = false;
    protected ListMode listMode = DISABLED;
    protected Rectangle scrollBounds = new Rectangle();
    protected GuiElement backgroundElement = null;
    protected GuiSlideControl verticalScrollBar = null;
    protected GuiSlideControl horizontalScrollBar = null;
    protected Runnable scrollBarStateChangingListener = null;

    protected LinkedList<GuiElement> scrollingElements = new LinkedList<>();
    protected LinkedList<GuiElement> foregroundElements = new LinkedList<>();
    protected LinkedList<GuiElement> backgroundElements = new LinkedList<>();

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

    public GuiScrollElement setInsetScrollBars(boolean insetScrollBars) {
        this.insetScrollBars = insetScrollBars;
        return this;
    }

    public GuiScrollElement setDisableOffScreenElements(boolean disableOffScreenElements) {
        this.disableOffScreenElements = disableOffScreenElements;
        return this;
    }

    /**
     * Called when a scroll bar state is ABOUT to change from disabled to enabled or vice versa.
     * If you need to reconfigure elements to fit withing the new bounds then do it the tick after this is called.
     * (There is really no better way to handle this at this point)
     */
    public GuiScrollElement setScrollBarStateChangingListener(Runnable scrollBarStateChangingListener) {
        this.scrollBarStateChangingListener = scrollBarStateChangingListener;
        return this;
    }

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
        verticalScrollBar.addScrollCheck((slider, mouseX, mouseY) -> slider.isMouseOver(mouseX, mouseY) || !Screen.hasShiftDown());
        horizontalScrollBar.setParentScroll(true);
        horizontalScrollBar.allowMiddleClickDrag(true);
        horizontalScrollBar.clearScrollChecks();
        horizontalScrollBar.addScrollCheck((slider, mouseX, mouseY) -> slider.isMouseOver(mouseX, mouseY) || Screen.hasShiftDown());
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
    public GuiScrollElement addElement(GuiElement element) {
        scrollingElements.add(element);
        super.addChild(element);
        updateScrollElement();
        return this;
    }

    public GuiScrollElement removeElement(GuiElement element) {
        removeChild(element);
        updateScrollElement();
        return this;
    }

    @Override
    public <C extends GuiElement> C removeChild(C child) {
        scrollingElements.remove(child);
        backgroundElements.remove(child);
        return super.removeChild(child);
    }

    /**
     * Add a child element that will NOT be bound to the scrollable area.
     * To add actual scrollable elements use {@link #addElement(GuiElement)}
     * This should only be used to add things like buttons or scroll bars which need
     * to be excluded from the scrolling logic, Though if adding scroll bars you
     * should use {@link #setVerticalScrollBar(GuiSlideControl)} or {@link #setHorizontalScrollBar(GuiSlideControl)}<br>
     * Note: child elements will always be rendered after scrollable elements and so will render on top in most cases.
     * If you need to add an element that renders before the scrolling elements use {@link #addBackgroundChild(GuiElement)}
     */
    @Override
    public final <C extends GuiElement> C addChild(C child) {
        foregroundElements.add(child);
        return super.addChild(child);
    }

    /**
     * Use this method to add any child elements that need to render BEFORE the scrolling elements.
     */
    public <C extends GuiElement> C addBackgroundChild(C child) {
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
    public ImmutableList<GuiElement> getScrollingElements() {
        return ImmutableList.copyOf(scrollingElements);
    }

    protected int elementXSize(GuiElement element) {
        if (useAbsoluteElementSize) {
            return element.getEnclosingRect().width;
        }
        return element.xSize();
    }

    protected int elementYSize(GuiElement element) {
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
            for (GuiElement element : scrollingElements) {
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
                } else {
                    if (listMode.lockPos()) {
                        element.setXPos(getInsetRect().x);
                    }
                    if (listMode.lockWidth()) {
                        element.setXSize(getInsetRect().width);
                    }
                    element.setYPos(lastPos);
                    lastPos += elementYSize(element) + listSpacing;
                }
                if (reloadOnUpdate) {
                    element.reloadElement();
                }
            }
        }

        updateScrollBounds();
        updateScrollbars();
        if (backgroundElement != null) {
            backgroundElement.setPosAndSize(scrollBounds);
        }

        if (disableOffScreenElements) {
            scrollingElements.forEach(e -> e.setEnabled(getInsetRect().intersects(e.getRect())));
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
        } else {
            for (GuiElement element : scrollingElements) {
                if (!element.isEnabled() || element == backgroundElement) continue;
                Rectangle rect = element.getEnclosingRect();
                if (rect.x < xMin) xMin = (int) rect.x;
                if (rect.getMaxX() > xMax) xMax = (int) rect.getMaxX();
                if (rect.y < yMin) yMin = (int) rect.y;
                if (rect.getMaxY() > yMax) yMax = (int) rect.getMaxY();
            }
        }

        scrollBounds.setBounds(xMin, yMin, xMax - xMin, yMax - yMin);
    }

    /**
     * Adds default scroll bars if no scroll bars have been set then updates the scroll bars as needed.
     */
    public boolean updateScrollbars() {
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

        boolean scrollStateChange = canDisVert == verticalScrollBar.isEnabled() || (!canDisHoz && enableHorizontalScroll) != horizontalScrollBar.isEnabled();
        if (scrollStateChange && scrollBarStateChangingListener != null) {
            scrollBarStateChangingListener.run();
        }

        verticalScrollBar.setEnabled(!canDisVert);
        horizontalScrollBar.setEnabled(!canDisHoz && enableHorizontalScroll);
        setInsets(defaultInsets.top, defaultInsets.left, defaultInsets.bottom, defaultInsets.right);
        updateScrollbarExclusion();

        if ((!verticalScrollBar.isEnabled() || verticalScrollBar.isHidden()) && horizontalScrollBar.isEnabled() && horizontalScrollBar.isInGroup(DEFAULT_SCROLL_BAR_GROUP)) {
            horizontalScrollBar
                    .setXSize(insetScrollBars ? getActualInsetRect().width : xSize())
                    .setYPos(maxYPos() - (horizontalScrollBar.ySize() + (insetScrollBars ? defaultInsets.bottom : 0)))
                    .updateElements();
        } else if (horizontalScrollBar.isInGroup(DEFAULT_SCROLL_BAR_GROUP)) {
            if (insetScrollBars) {
                horizontalScrollBar
                        .setXPos(getActualInsetRect().x)
                        .setXSize(xSize() - verticalScrollBar.xSize())
                        .setYPos(getActualInsetRect().y + getActualInsetRect().height - horizontalScrollBar.ySize())
                        .updateElements();
            } else {
                horizontalScrollBar
                        .setXSize(xSize() - verticalScrollBar.xSize())
                        .setYPos(maxYPos() - horizontalScrollBar.ySize())
                        .updateElements();
            }
        }

        if ((!horizontalScrollBar.isEnabled() || horizontalScrollBar.isHidden()) && verticalScrollBar.isEnabled() && verticalScrollBar.isInGroup(DEFAULT_SCROLL_BAR_GROUP)) {
            verticalScrollBar
                    .setYSize(insetScrollBars ? getActualInsetRect().height : ySize())
                    .setXPos(maxXPos() - (verticalScrollBar.xSize() + (insetScrollBars ? defaultInsets.right : 0)))
                    .updateElements();
        } else if (verticalScrollBar.isInGroup(DEFAULT_SCROLL_BAR_GROUP)) {
            if (insetScrollBars) {
                verticalScrollBar
                        .setYPos(getActualInsetRect().y)
                        .setYSize(getActualInsetRect().height - horizontalScrollBar.ySize())
                        .setXPos(getActualInsetRect().x + getActualInsetRect().width - verticalScrollBar.xSize())
                        .updateElements();
            } else {
                verticalScrollBar.setYSize(ySize() - horizontalScrollBar.ySize()).setXPos(maxXPos() - verticalScrollBar.xSize()).updateElements();
            }
        }

        double contentHeight = Math.abs(verticalMinScroll) + getInsetRect().height + Math.abs(verticalMaxScroll);
        verticalScrollBar.setRange(verticalMinScroll, verticalMaxScroll);
        verticalScrollBar.setScaledSliderSize(getInsetRect().height / contentHeight);
        verticalScrollBar.updatePos(verticalScrollPos, false);

        double contentWidth = Math.abs(horizontalMinScroll) + getInsetRect().width + Math.abs(horizontalMaxScroll);
        horizontalScrollBar.setRange(horizontalMinScroll, horizontalMaxScroll);
        horizontalScrollBar.setScaledSliderSize(getInsetRect().width / contentWidth);
        horizontalScrollBar.updatePos(horizontalScrollPos, false);

        return scrollStateChange;
    }

    private Rectangle getActualInsetRect() {
        return new Rectangle(xPos() + defaultInsets.left,
                yPos() + defaultInsets.top,
                xSize() - defaultInsets.left - defaultInsets.right,
                ySize() - defaultInsets.top - defaultInsets.bottom);
    }

    protected void updateScrollbarExclusion() {
        if (scrollBarExclusionMode) {
            //Check if the scroll bar is on the right side of the element
            if (verticalScrollBar.isEnabled() && !verticalScrollBar.isHidden()) {
                if (verticalScrollBar.getRect().getCenterX() > getRect().getCenterX()) {
                    if (getInsetRect().getMaxX() > verticalScrollBar.xPos()) {
                        getInsets().right = maxXPos() - verticalScrollBar.xPos();
                    }
                } else {
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
                } else {
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
            setVerticalScrollBar(new GuiSlideControl(VERTICAL)
                    .setPos(maxXPos() - (10 + (insetScrollBars ? getInsets().right : 0)), yPos() + (insetScrollBars ? getInsets().top : 0))
                    .setSize(10, (insetScrollBars ? getInsetRect().height : ySize()) - 10)
                    .setDefaultBackground(0xFF000000, 0xFFFFFFFF)
                    .setDefaultSlider(0xFFA0A0A0, 0xFF707070));
            verticalScrollBar.addToGroup(DEFAULT_SCROLL_BAR_GROUP);
        }
        if (horizontalScrollBar == null) {
            setHorizontalScrollBar(new GuiSlideControl()
                    .setPos(xPos(), maxYPos() - 10)
                    .setSize(xSize() - 10, 10)
                    .setDefaultBackground(0xFF000000, 0xFFFFFFFF)
                    .setDefaultSlider(0xFFA0A0A0, 0xFF707070));
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
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        if (getInsetRect().contains(mouseX, mouseY)) {
            for (GuiElement element : scrollingElements) {
                if (element.isEnabled() && element.mouseClicked(mouseX, mouseY, mouseButton)) {
                    return true;
                }
            }
        }

        for (GuiElement element : foregroundElements) {
            if (element.isEnabled() && !scrollingElements.contains(element) && element.mouseClicked(mouseX, mouseY, mouseButton)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int clickedMouseButton, double dragX, double dragY) {
        for (GuiElement element : scrollingElements) {
            if (element.isEnabled() && element.mouseDragged(mouseX, mouseY, clickedMouseButton, dragX, dragY)) {
                return true;
            }
        }

        for (GuiElement element : foregroundElements) {
            if (element.isEnabled() && !scrollingElements.contains(element) && element.mouseDragged(mouseX, mouseY, clickedMouseButton, dragX, dragY)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean handleMouseScroll(double mouseX, double mouseY, double scrollDirection) {
        for (GuiElement element : scrollingElements) {
            if (element.isEnabled() && element.handleMouseScroll(mouseX, mouseY, scrollDirection)) {
                return true;
            }
        }

        for (GuiElement element : foregroundElements) {
            if (element.isEnabled() && !scrollingElements.contains(element) && element.handleMouseScroll(mouseX, mouseY, scrollDirection)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onMGuiEvent(GuiEvent event, GuiElement eventSource) {
        int xAdjustment = 0;
        int yAdjustment = 0;
        if (eventSource == verticalScrollBar) {
            yAdjustment = verticalScrollPos - (int) verticalScrollBar.getPosition();
            verticalScrollPos = (int) verticalScrollBar.getPosition();
        } else if (eventSource == horizontalScrollBar) {
            xAdjustment = horizontalScrollPos - (int) horizontalScrollBar.getPosition();
            horizontalScrollPos = (int) horizontalScrollBar.getPosition();
        }

        for (GuiElement scrollableElement : scrollingElements) {
//            scrollableElement.animateMoveFrames();
            if (smoothScroll) {
                scrollableElement.translateAnim(xAdjustment, yAdjustment, animSpeed);
            } else {
                scrollableElement.translate(xAdjustment, yAdjustment);
            }
            if (disableOffScreenElements) {
                scrollingElements.forEach(e -> e.setEnabled(getInsetRect().intersects(e.getRect())));
            }
        }
    }

    public GuiScrollElement setReloadOnUpdate(boolean reloadOnUpdate) {
        this.reloadOnUpdate = reloadOnUpdate;
        return this;
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
    public GuiScrollElement applyBackgroundElement(GuiElement backgroundElement) {
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
        for (GuiElement element : backgroundElements) {
            if (element.isEnabled()) {
                element.renderElement(minecraft, mouseX, mouseY, partialTicks);
            }
        }

        double xPos = getInsetRect().x;
        double yPos = getInsetRect().y;
        double xSize = getInsetRect().width;
        double ySize = getInsetRect().height;

        double yResScale = (double) displayHeight() / (screenHeight);
        double xResScale = (double) displayWidth() / (screenWidth);
        double scaledWidth = xSize * xResScale;
        double scaledHeight = ySize * yResScale;
        int x = (int) (xPos * xResScale);
        int y = (int) (displayHeight() - (yPos * yResScale) - scaledHeight);

        ScissorHelper.pushScissor(x, y, (int) scaledWidth, (int) scaledHeight);

        if (backgroundElement != null) {
            backgroundElement.renderElement(minecraft, mouseX, mouseY, partialTicks);
        }

        for (GuiElement element : scrollingElements) {
            if (element.isEnabled() && element != backgroundElement) {
                element.renderElement(minecraft, mouseX, mouseY, partialTicks);
            }
        }

        ScissorHelper.popScissor();
        for (GuiElement element : foregroundElements) {
            if (element.isEnabled() && !scrollingElements.contains(element)) {
                element.renderElement(minecraft, mouseX, mouseY, partialTicks);
            }
        }
//        drawBorderedRect(xPos(), yPos(), xSize(), ySize(), 1, 0, 0x8000FFFF);
    }

    //endregion

    //region Misc

    @Override
    protected void addDefaultListener(GuiElement childElement) {
        if (childElement instanceof IGuiEventDispatcher && ((IGuiEventDispatcher) childElement).getListener() == null) {
            if (getParent() instanceof IGuiEventListener) {
                ((IGuiEventDispatcher) childElement).setListener((IGuiEventListener) getParent());
            } else if (modularGui instanceof IGuiEventListener) {
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

        for (GuiElement element : childElements) {
            if (!scrollingElements.contains(element)) {
                element.addBoundsToRect(enclosingRect);
            }
        }
        return enclosingRect;
    }

    @Override
    public boolean allowMouseOver(GuiElement elementRequesting, double mouseX, double mouseY) {
        if (scrollingElements.contains(elementRequesting)) {
            return getInsetRect().contains(mouseX, mouseY);
        }
        return super.allowMouseOver(elementRequesting, mouseX, mouseY);
    }

    @Override
    public void xSizeChanged(GuiElement elementChanged) {
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
    public void ySizeChanged(GuiElement elementChanged) {
        if (scrollingElements.contains(elementChanged) && elementChanged.reportYSizeChange) {
            int vsp = verticalScrollPos;
            int hsp = horizontalScrollPos;
            resetScrollPositions();
            updateScrollBounds();
            updateScrollElement();

//            reloadElement();
            resetScrollPositions();
            verticalScrollBar.updatePos(vsp);
            horizontalScrollBar.updatePos(hsp);
        }
    }

    public Rectangle getScrollBounds() {
        return scrollBounds;
    }


    //endregion

    public enum ListMode {
        DISABLED(false, false, false),
        VERTICAL(false, false, false),
        VERT_LOCK_POS(false, false, true),
        VERT_LOCK_POS_WIDTH(false, true, true),
        HORIZONTAL(true, false, false),
        HORIZ_LOCK_POS(true, false, true),
        HORIZ_LOCK_POS_HEIGHT(true, true, true);

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