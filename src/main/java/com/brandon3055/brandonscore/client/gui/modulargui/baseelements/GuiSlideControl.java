package com.brandon3055.brandonscore.client.gui.modulargui.baseelements;

import codechicken.lib.math.MathHelper;
import com.brandon3055.brandonscore.client.gui.modulargui.GuiElement;
import com.brandon3055.brandonscore.client.gui.modulargui.IModularGui;
import com.brandon3055.brandonscore.client.gui.modulargui.guielements.GuiBorderedRect;
import com.brandon3055.brandonscore.client.gui.modulargui.lib.GuiEvent.SliderMoveEvent;
import com.brandon3055.brandonscore.client.gui.modulargui.lib.IGuiEventDispatcher;
import com.brandon3055.brandonscore.client.gui.modulargui.lib.IGuiEventListener;
import com.brandon3055.brandonscore.client.gui.modulargui.lib.IMouseOver;
import com.brandon3055.brandonscore.lib.functions.TriPredicate;
import com.brandon3055.brandonscore.utils.DataUtils;
import net.minecraft.client.Minecraft;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static com.brandon3055.brandonscore.client.gui.modulargui.baseelements.GuiSlideControl.SliderRotation.HORIZONTAL;

/**
 * Created by brandon3055 on 10/09/2016.
 * This is the base slider control element. Used ether in its base form or extended by elements such as the GuiScrollBar
 * Note: By default this slider has its insets set to 1 all round so there will be a 1 pixel buffer around the slider element.
 */
public class GuiSlideControl extends GuiElement<GuiSlideControl> implements IGuiEventDispatcher {

    private double dragStartX = 0;
    private double dragStartY = 0;
    private double dragStartElementX = 0;
    private double dragStartElementY = 0;
    private boolean parentScroll = false;
    private IMouseOver parentScrollable = null;
    private List<TriPredicate<GuiSlideControl, Double, Double>> scrollChecks = new ArrayList<>();

    public IGuiEventListener listener;
    public Consumer<GuiSlideControl> inputListener;

    protected int sliderSize = 10;
    protected double mouseDragOffset = 0;
    protected double dragOutResetThreshold = 100;

    protected double position = 0;
    protected double scrollSpeed = 0.04;
    protected double posRangeMin = 0;
    protected double posRangeMax = 1;
    /**
     * This is the position the slider was in before the user started dragging it. Not to be confused with the prevPosition provided by {@link SliderMoveEvent}
     */
    protected double prevPosition = 0;
    protected boolean isHidden = false;
    protected boolean isDragging = false;
    protected boolean allowMiddleClickDrag = false;
    protected boolean isMiddleClickDragging = false;
    protected boolean lockSliderWidthAndPos = true;
    protected boolean lockBackgroundWidthAndPos = true;
    protected SliderRotation rotation = HORIZONTAL;

    protected GuiElement<? extends GuiElement> sliderElement;
    protected GuiElement<? extends GuiElement> backgroundElement;

    public GuiSlideControl() {
        setInsets(1, 1, 1, 1);
    }

    public GuiSlideControl(int xPos, int yPos) {
        super(xPos, yPos);
        setInsets(1, 1, 1, 1);
    }

    public GuiSlideControl(int xPos, int yPos, int xSize, int ySize) {
        super(xPos, yPos, xSize, ySize);
        setInsets(1, 1, 1, 1);
    }

    public GuiSlideControl(SliderRotation rotation) {
        this.rotation = rotation;
        setInsets(1, 1, 1, 1);
    }

    public GuiSlideControl(SliderRotation rotation, int xPos, int yPos) {
        super(xPos, yPos);
        this.rotation = rotation;
        setInsets(1, 1, 1, 1);
    }

    public GuiSlideControl(SliderRotation rotation, int xPos, int yPos, int xSize, int ySize) {
        super(xPos, yPos, xSize, ySize);
        this.rotation = rotation;
        setInsets(1, 1, 1, 1);
    }

    //region Events

    @Override
    public GuiSlideControl setListener(IGuiEventListener listener) {
        this.listener = listener;
        return this;
    }

    /**
     * Basically the same as setListener except it uses a convenient Consumer
     */
    public GuiSlideControl setInputListener(Consumer<GuiSlideControl> inputListener) {
        this.inputListener = inputListener;
        return this;
    }

    public Consumer<GuiSlideControl> getInputListener() {
        return inputListener;
    }

    @Nullable
    @Override
    public IGuiEventListener getListener() {
        return listener;
    }

    //endregion

    //region Mouse Input

    @Override
    public boolean handleMouseScroll(double mouseX, double mouseY, double scrollDirection) {
        //Check if there are any scrolling checks that may restrict scrolling.
        for (TriPredicate<GuiSlideControl, Double, Double> check : scrollChecks) {
            if (!check.test(this, mouseX, mouseY)) {
                return super.handleMouseScroll(mouseX, mouseY, scrollDirection);
            }
        }

        //If the mouse is actually over this element then we want to
        if (isMouseOver(mouseX, mouseY)) {
            updateRawPos(position + (scrollDirection > 0 ? -scrollSpeed : scrollSpeed));
            if (inputListener != null) {
                inputListener.accept(this);
            }
            return true;
        }
        else if (parentScroll) {
            if ((parentScrollable == null || !parentScrollable.isMouseOver(mouseX, mouseY)) && (getParent() == null || !getParent().isMouseOver(mouseX, mouseY))) {
                return super.handleMouseScroll(mouseX, mouseY, scrollDirection);
            }

            //Make sure the mouse is not hovering over another slider element before capturing the event.
            List<GuiSlideControl> slidersMouseOver = modularGui.getManager().getElementsAtPosition(mouseX, mouseY, GuiSlideControl.class);
            if (DataUtils.firstMatch(slidersMouseOver, slider -> slider != this && slider.isEnabled()) != null) {
                return super.handleMouseScroll(mouseX, mouseY, scrollDirection);
            }

            updateRawPos(position + (scrollDirection > 0 ? -scrollSpeed : scrollSpeed));
            if (inputListener != null) {
                inputListener.accept(this);
            }
            return true;
        }

        return super.handleMouseScroll(mouseX, mouseY, scrollDirection);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        if (((isMouseOver(mouseX, mouseY) && !isHidden()) || (mouseButton == 2 && allowMiddleClickDrag && getParentScrollable() != null && getParentScrollable().isMouseOver(mouseX, mouseY))) && sliderElement != null && !isDragging && !isMiddleClickDragging) {
            if (mouseButton == 2 && getParent() != null) {
                List<GuiSlideControl> list = getParent().findChildElementsByClass(GuiSlideControl.class, new ArrayList());
                if (DataUtils.firstMatch(list, e -> e != this && e.isMiddleClickDragging && e.getParentScrollable() != getParentScrollable()) != null) {
                    return false;
                }
            }

            prevPosition = getRawPos();
            if ((sliderElement.isMouseOver(mouseX, mouseY) && mouseButton == 0) || mouseButton == 2) {
                if (rotation == HORIZONTAL) {
                    mouseDragOffset = mouseX - sliderElement.xPos();
                }
                else {
                    mouseDragOffset = mouseY - sliderElement.yPos();
                }
                if (mouseButton == 2) {
                    isMiddleClickDragging = true;
                    dragStartX = mouseX;
                    dragStartY = mouseY;
                    dragStartElementX = sliderElement.xPos();
                    dragStartElementY = sliderElement.yPos();
                    return false;
                }
            }
            else if (mouseButton == 0) {
                mouseDragOffset = sliderSize / 2;
                if (rotation == HORIZONTAL) {
                    sliderElement.setXPos((int)mouseX - (sliderElement.xSize() / 2));
                    double maxXOffset = getInsetRect().width - sliderElement.xSize();
                    double xOffset = sliderElement.xPos() - getInsetRect().x;
                    updateRawPos(xOffset / maxXOffset);
                    if (inputListener != null) {
                        inputListener.accept(this);
                    }
                }
                else {
                    sliderElement.setYPos((int)mouseY - (sliderElement.ySize() / 2));
                    double maxYOffset = getInsetRect().height - sliderElement.ySize();
                    double yOffset = sliderElement.yPos() - getInsetRect().y;
                    updateRawPos(yOffset / maxYOffset);
                    if (inputListener != null) {
                        inputListener.accept(this);
                    }
                }
            }
            else {
                return super.mouseClicked(mouseX, mouseY, mouseButton);
            }

            isDragging = true;
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }


    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int clickedMouseButton, double dragX, double dragY) {
//        double doubleMouseX = (double) Mouse.getEventX() * (double) modularGui.xSize() / (double) this.mc.displayWidth;
//        double doubleMouseY = (double) modularGui.ySize() - (double) Mouse.getEventY() * modularGui.ySize() / (double) this.mc.displayHeight - 1;

//        if (isDragging || isMiddleClickDragging) { //TODO figure out why this is borked
//            if (distFromElement(mouseX, mouseY) > dragOutResetThreshold && isDragging) {
//                updateRawPos(prevPosition);
//                return true;
//            }
//
//            if (rotation == HORIZONTAL) {
//                if (isMiddleClickDragging) {
//                    sliderElement.setXPos(dragStartElementX - (int) ((mouseX - dragStartX) * scrollSpeed * 2));
//                }
//                else {
//                    sliderElement.setXPos((int) (doubleMouseX - mouseDragOffset));
//                }
//                double maxXOffset = getInsetRect().width - sliderElement.xSize();
//                double xOffset = (doubleMouseX - mouseDragOffset) - getInsetRect().x;//sliderElement.xPos() - getInsetRect().x;
//                updateRawPos(xOffset / maxXOffset);
//            }
//            else {
//                if (isMiddleClickDragging) {
//                    sliderElement.setYPos(dragStartElementY - (int) ((doubleMouseY - dragStartY) * scrollSpeed * 2));
//                }
//                else {
//                    sliderElement.setYPos((int) (doubleMouseY - mouseDragOffset));
//                }
//                double maxYOffset = getInsetRect().height - sliderElement.ySize();
//                double yOffset = (doubleMouseY - mouseDragOffset) - getInsetRect().y;//sliderElement.yPos() - getInsetRect().y;
//                updateRawPos(yOffset / maxYOffset);
//            }
//            return !isMiddleClickDragging;
//        }

        if (isDragging || isMiddleClickDragging) {
            if (distFromElement(mouseX, mouseY) > dragOutResetThreshold && isDragging) {
                updateRawPos(prevPosition);
                if (inputListener != null) {
                    inputListener.accept(this);
                }
                return true;
            }

            if (rotation == HORIZONTAL) {
                if (isMiddleClickDragging) {
                    sliderElement.setXPos((int)dragStartElementX - (int) ((mouseX - dragStartX) * scrollSpeed * 2));
                }
                else {
                    sliderElement.setXPos((int) (mouseX - mouseDragOffset));
                }
                double maxXOffset = getInsetRect().width - sliderElement.xSize();
                double xOffset = sliderElement.xPos() - getInsetRect().x;
                updateRawPos(xOffset / maxXOffset);
                if (inputListener != null) {
                    inputListener.accept(this);
                }
            }
            else {
                if (isMiddleClickDragging) {
                    sliderElement.setYPos((int)dragStartElementY - (int) ((mouseY - dragStartY) * scrollSpeed * 2));
                }
                else {
                    sliderElement.setYPos((int) (mouseY - mouseDragOffset));
                }
                double maxYOffset = getInsetRect().height - sliderElement.ySize();
                double yOffset = sliderElement.yPos() - getInsetRect().y;
                updateRawPos(yOffset / maxYOffset);
                if (inputListener != null) {
                    inputListener.accept(this);
                }
            }
            return !isMiddleClickDragging;
        }
        return super.mouseDragged(mouseX, mouseY, clickedMouseButton, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int state) {
        if (isDragging || isMiddleClickDragging) {
            isDragging = false;
            isMiddleClickDragging = false;
            if (listener != null) {
                listener.onMGuiEvent(new SliderMoveEvent(this, getPosition(), getPosition(), false), this);
            }
            if (inputListener != null) {
                inputListener.accept(this);
            }
        }
        return super.mouseReleased(mouseX, mouseY, state);
    }

    //endregion

    //region Implementation Setters & Getters

    public GuiSlideControl setRotation(SliderRotation rotation) {
        this.rotation = rotation;
        return this;
    }

    /**
     * @return the current position of this slider element. The return value will be between posRangeMin and posRangeMax
     * with min at top/left and max at bottom/right (depending on slider orientation)
     * @see #setRange
     */
    public double getPosition() {
        return MathHelper.map(getRawPos(), 0, 1, posRangeMin, posRangeMax);
    }

    /**
     * @return the scroll position in its raw form which will always be a value between 0 and 1.
     */
    public double getRawPos() {
        return Double.isNaN(position) ? 0 : position;
    }

    /**
     * Updates the raw position of the slider and sends an update to the listener.
     *
     * @param position  a value between 0 and 1
     * @param fireEvent Sets whether or not the listener should be notified on this update
     */
    public GuiSlideControl updateRawPos(double position, boolean fireEvent) {
        if (this.position == position) return this;
        double previousPos = getPosition();
        this.position = MathHelper.clip(position, 0, 1);
        updateSliderPos();
        if (fireEvent) {
            if (listener != null) {
                listener.onMGuiEvent(new SliderMoveEvent(this, getPosition(), previousPos, isDragging), this);
            }
        }
        return this;
    }

    public GuiSlideControl updateRawPos(double position) {
        return updateRawPos(position, true);
    }

    /**
     * Updates the raw position of the slider and sends an update to the listener.
     *
     * @param position  a value between posRangeMin and posRangeMax
     * @param fireEvent Sets whether or not the listener should be notified on this update
     */
    public GuiSlideControl updatePos(double position, boolean fireEvent) {
        double value = MathHelper.map(position, posRangeMin, posRangeMax, 0, 1);
        updateRawPos(Double.isNaN(value) ? 0 : value, fireEvent);
        return this;
    }

    public GuiSlideControl updatePos(double position) {
        return updatePos(position, true);
    }

    /**
     * Add an element to be used as the actual slider (the part that the user clicks and drags).
     * This element should to be resizable as its x and y size may be manipulated by the SliderControl
     * <p>
     * If you require a buffer around the scroll element e.g. if you have a bordered rectangle as the background
     * and you want the Slider element to remain within this rectangle you can set bounds for the Slider element
     * using setInsets.
     *
     * @param sliderElement an element to be used as the slider element.
     */
    public GuiSlideControl setSliderElement(GuiElement sliderElement) {
        if (this.sliderElement != null) {
            removeChild(this.sliderElement);
        }

        this.sliderElement = sliderElement;
        addChild(sliderElement);
        updateElements();
        return this;
    }

    /**
     * @see #setSliderElement(GuiElement)
     */
    public GuiElement<? extends GuiElement> getSliderElement() {
        return sliderElement;
    }

    /**
     * Add an element to be used as the actual background of this slider.
     * This element should to be resizable as its x and y size will be set to match the x and y size of the slider control.
     *
     * @param backgroundElement an element to be used as the background element.
     */
    public GuiSlideControl setBackgroundElement(GuiElement backgroundElement) {
        if (this.backgroundElement != null) {
            removeChild(this.backgroundElement);
        }
        this.backgroundElement = backgroundElement;
        addChild(backgroundElement);
        updateElements();
        return this;
    }

    /**
     * @see #setBackgroundElement(GuiElement)
     */
    public GuiElement<? extends GuiElement> getBackgroundElement() {
        return backgroundElement;
    }

    /**
     * Sets the size of the slider. Can not be larger than the actual slider control size.
     * This is ether the xSize or the ySize of the slider element depending in whether the
     * control is in HORIZONTAL or VERTICAL mode respectively.
     */
    public GuiSlideControl setSliderSize(int sliderSize) {
        int maxSize = rotation == HORIZONTAL ? getInsetRect().width : getInsetRect().height;
        this.sliderSize = MathHelper.clip(sliderSize, 0, maxSize);
        updateElements();
        return this;
    }

    public int getSliderSize() {
        return sliderSize;
    }

    /**
     * This can be used for things like the scroll bar on a scrolling page where the scroll bar needs to get
     * smaller as the length of the page gets larger. This takes a value between 0 and 1 where 1 is full size
     * resulting in the slider being unmovable with its position locked to 0.5. A value of 0 would result in
     * a size of 0 however as this would make the slider impossible to click the actual size is limited to
     * 3 pixels minimum. This is still very small but keep in mind this would require an exceptionally long page
     * to get a size that small and it will still be possible to move the bar because clicking anywhere on
     * the slider element automatically sets the slider pos to that location.
     *
     * @param scaledSize        A scale value between 0 and 1.
     * @param updateScrollSpeed Updates the scroll speed based on the scaled size.
     */
    public GuiSlideControl setScaledSliderSize(double scaledSize, boolean updateScrollSpeed) {
        int maxSize = rotation == HORIZONTAL ? getInsetRect().width : getInsetRect().height;
        this.sliderSize = MathHelper.clip((int) (maxSize * scaledSize), 3, maxSize);
        updateSliderPos();

        if (updateScrollSpeed) {
            double scrollSpeed = scaledSize / 2;
            setScrollSpeed(scrollSpeed);
        }

        return this;
    }

    /**
     * @see #setScaledSliderSize(double, boolean)
     */
    public GuiSlideControl setScaledSliderSize(double scaledSize) {
        return setScaledSliderSize(scaledSize, true);
    }

    /**
     * Enabled by default.
     * When this is enabled the width and position (on the stationary axis) of the slider element will be locked to the width and pos of the the slider control
     * (with insets applied). If this is false then you need to handle these manually. Meaning if you need to manually set the ySize and yPos (if in HORIZONTAL mode)
     * of the xSize and xPos (if in VERTICAL mode)
     */
    public GuiSlideControl lockSliderWidthPos(boolean lockSliderWidth) {
        this.lockSliderWidthAndPos = lockSliderWidth;
        return this;
    }

    /**
     * Enabled by default.
     * When this is enabled the width and position of the slider element will be locked to the width and pos of the the slider control
     * If this is false then you need to handle these manually.
     */
    public GuiSlideControl lockBackgroundWidthPos(boolean lockBackgroundWidth) {
        this.lockBackgroundWidthAndPos = lockBackgroundWidth;
        return this;
    }

    /**
     * Applied the default background which is a bordered rectangle with the given colours.
     * Note: this also sets lockBackgroundWidthPos to true.
     */
    public GuiSlideControl setDefaultBackground(int fillColour, int borderColour) {
        lockBackgroundWidthPos(true);
        setBackgroundElement(new GuiBorderedRect().setColours(fillColour, borderColour));
        return this;
    }

    /**
     * Sets a solid colour background.
     *
     * @see #setDefaultBackground(int, int)
     */
    public GuiSlideControl setDefaultBackground(int colour) {
        return setDefaultBackground(colour, colour);
    }

    /**
     * Applied the default slider which is a bordered rectangle with the given colours including
     * optionally different colours when the mouse is over the element.
     * Note: this also sets lockSliderWidthPos to true.
     */
    public GuiSlideControl setDefaultSlider(int fillColour, int hoverFillColour, int borderColour, int hoverBorderColour) {
        lockSliderWidthPos(true);
        setSliderElement(new GuiBorderedRect().setColours(fillColour, hoverFillColour, borderColour, hoverBorderColour));
        return this;
    }

    /**
     * Applied the default slider which is a bordered rectangle with the given colours.
     * Note: this also sets lockSliderWidthPos to true.
     *
     * @see #setDefaultSlider(int, int, int, int)
     */
    public GuiSlideControl setDefaultSlider(int fillColour, int borderColour) {
        lockSliderWidthPos(true);
        setSliderElement(new GuiBorderedRect().setColours(fillColour, borderColour));
        return this;
    }

    /**
     * Sets a solid colour slider knob.
     *
     * @see #setDefaultBackground(int, int)
     */
    public GuiSlideControl setDefaultSlider(int colour) {
        return setDefaultSlider(colour, colour);
    }

    /**
     * When the user is dragging the slider element the element will continue to track the cursor position even if the cursor
     * moves outside of the slider element. However if the user drags the cursor more than a certain distance from the
     * element the slider position will snap back to where it was before the user clicked it. This sets the distance at
     * which that occurs.
     * This is standard behavior for scroll bars in most applications.
     * The default threshold is 100.
     */
    public GuiSlideControl setDragOutResetThreshold(int dragOutResetThreshold) {
        this.dragOutResetThreshold = dragOutResetThreshold;
        return this;
    }

    /**
     * This allows toy to add predicated that specify whether or not the mouse scroll wheel can currently scroll the slider.
     * This can be used for example to only allow scrolling when a certain key is or isnt pressed.
     * The values passed into the predicate are the slide control, mouseX and mouseY
     */
    public GuiSlideControl addScrollCheck(TriPredicate<GuiSlideControl, Double, Double> predicate) {
        scrollChecks.add(predicate);
        return this;
    }

    public GuiSlideControl clearScrollChecks() {
        scrollChecks.clear();
        return this;
    }

    /**
     * @param parentScroll Allow this element to scroll when the cursor is over its parent element.
     */
    public GuiSlideControl setParentScroll(boolean parentScroll) {
        this.parentScroll = parentScroll;
        return this;
    }

    /**
     * Allows you to override the parent scrollable element, This is the element who while the mouse is over allows the
     * mouse scroll wheel to adjust the slider position.<br>
     * Example: Say you are making a scrolling Gui and you want to be able to scroll in the gui without needing to
     * hover the mouse over the the scroll bar. Then you can add your window element as the parentScrollable
     * and as long as the cursor is over the window element you will be able to scroll.<br>
     * Note: If the GuiSlideControl is already a child of your window element the you do not need this. Instead
     * you can just use {@link #setParentScroll(boolean)}
     *
     * @param parentScrollable an {@link IMouseOver} element. By default this Interface is implemented on {@link GuiElement} and {@link IModularGui}
     * @see #setParentScroll(boolean)
     */
    public GuiSlideControl setParentScrollable(IMouseOver parentScrollable) {
        this.parentScrollable = parentScrollable;
        return setParentScroll(true);
    }

    /**
     * Allows you to override the default position range of 0 to 1 with your own range.
     * The range can include negative values.
     *
     * @param min The min slider value.
     * @param max The max slider value.
     */
    public GuiSlideControl setRange(double min, double max) {
        posRangeMin = min;
        posRangeMax = max;
        return this;
    }

    /**
     * When enabled this allows you to middle click and drag the parent window to adjust this scroll bar.
     * The scroll direction is inverted relative to the normal click drag functionality.
     * This is meant to be used with things like the {@link GuiScrollElement}
     * Default disabled.
     */
    public GuiSlideControl allowMiddleClickDrag(boolean allowMiddleClickDrag) {
        this.allowMiddleClickDrag = allowMiddleClickDrag;
        return this;
    }

    /**
     * Allows you to hide the scroll bar. When hidden it will not be possible to click or drag the scroll bar
     * directly but it will still be possible to use the mouse scroll wheel or middle click+drag to adjust the scroll position.
     */
    public GuiSlideControl setHidden(boolean hidden) {
        isHidden = hidden;
        return this;
    }

    public boolean isHidden() {
        return isHidden;
    }

    /**
     * Applies a bar style background. This is a generic slider background that looks something like this<br>
     * |------------|
     */
    public GuiSlideControl setBarStyleBackground(int colour) {
        setBackgroundElement(new GuiElement() {
            @Override
            public void renderElement(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
                if (rotation == HORIZONTAL) {
                    drawColouredRect(xPos(), yPos() + ySize() / 2, xSize(), 1, colour);
                    drawColouredRect(xPos(), yPos(), 1, ySize(), colour);
                    drawColouredRect(xPos() + xSize() - 1, yPos(), 1, ySize(), colour);
                }
                else {
                    drawColouredRect(xPos() + xSize() / 2, yPos(), 1, ySize(), colour);
                    drawColouredRect(xPos(), yPos(), xSize(), 1, colour);
                    drawColouredRect(xPos(), yPos() + ySize() - 1, xSize(), 1, colour);
                }
            }
        });
        lockSliderWidthPos(true);
        updateElements();
        return this;
    }

    public boolean isDragging() {
        return isDragging;
    }

    //endregion

    //region Logic

    /**
     * Updates the size and position's of the slider and background elements.
     */
    public void updateElements() {
        if (backgroundElement != null && lockBackgroundWidthAndPos) {
            backgroundElement.setPos(this).setSize(this);
        }
        if (sliderElement != null && lockSliderWidthAndPos) {
            if (rotation == HORIZONTAL) {
                sliderElement.setYSize(getInsetRect().height);
                sliderElement.setYPos(getInsetRect().y);
            }
            else {
                sliderElement.setXSize(getInsetRect().width);
                sliderElement.setXPos(getInsetRect().x);
            }
        }
        updateSliderPos();
    }

    /**
     * Updates the position of the slider element.
     */
    public void updateSliderPos() {
        if (sliderElement != null) {
            if (rotation == HORIZONTAL) {
                sliderElement.setXSize(sliderSize);
                int maxMove = getInsetRect().width - sliderElement.xSize();
                sliderElement.setXPos(getInsetRect().x + (int) (maxMove * getRawPos()));
            }
            else {
                sliderElement.setYSize(sliderSize);
                int maxMove = getInsetRect().height - sliderElement.ySize();
                sliderElement.setYPos(getInsetRect().y + (int) (maxMove * getRawPos()));
            }
        }
    }

    public IMouseOver getParentScrollable() {
        return parentScrollable == null ? getParent() : parentScrollable;
    }

    //endregion

    @Override
    public void renderElement(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
        if (isHidden()) return;
        if (backgroundElement != null && backgroundElement.isEnabled()) {
            backgroundElement.renderElement(minecraft, mouseX, mouseY, partialTicks);
        }
        for (GuiElement element : childElements) {
            if (element.isEnabled() && element != backgroundElement) {
                element.renderElement(minecraft, mouseX, mouseY, partialTicks);
            }
        }
    }

    /**
     * Set how much the slider increments by each click when scrolling with the mouse wheel.
     * 1 "click" is usually 1 tactile bump when rotating the scroll wheel.
     * TODO need a better way to manage this because this is kinda dumb
     */
    public GuiSlideControl setScrollSpeed(double increment) {
        this.scrollSpeed = increment;
        return this;
    }

    public double getScrollSpeed() {
        return scrollSpeed;
    }

    public enum SliderRotation {
        HORIZONTAL, VERTICAL
    }
}