package com.brandon3055.brandonscore.client.gui.modulargui;

import codechicken.lib.vec.Vector3;
import com.brandon3055.brandonscore.client.ResourceHelperBC;
import com.brandon3055.brandonscore.client.gui.modulargui.baseelements.GuiScrollElement;
import com.brandon3055.brandonscore.client.gui.modulargui.lib.*;
import com.brandon3055.brandonscore.client.gui.modulargui.lib.GuiAlign.TextRotation;
import com.brandon3055.brandonscore.client.utils.GuiHelper;
import com.brandon3055.brandonscore.utils.DataUtils;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.List;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Created by brandon3055 on 30/08/2016. <br>
 * Revision 2: 5/07/2017. (Technically this is revision 3 if you count the terrible system i had back in 1.7.10)
 * <p>
 * This is the foundation of the entire Modular Gui System.
 * <p>
 * This modular gui system is designed to make the construction of minecraft gui's extremely easy and fast.
 * This system can be used to build anything from the most basic gui to a mod encyclopedia inside minecraft!
 * I am of course referring to Project Intelligence which has the most advanced modular gui i have built with
 * this system so far.
 * <p>
 * This system uses a Parent/Child design structure where your Modular Gui has a number of elements
 * and each of those elements can have child elements and so on.
 * All child elements are position bound to their parent. This allows for some very powerful functionality
 * such as the {@link GuiScrollElement} which works pretty much like a JScrollPane but with this gui system.
 * <p>
 * But these are just some examples of what you can do with the base elements. The real power of this system
 * comes from what you can do with custom elements. You can create your own elements for custom functionality and
 * there are also a large number of built in elements. Everything from The most universal button you could possibly
 * imagine to Particle effect renderers! Labels, Slider controls, Texture Renderers, Colour Pickers and Stack Renderers
 * just to name a few.
 */
public class GuiElement<E extends GuiElement<E>> implements IMouseOver, IGuiParentElement<E> {
    protected static final ResourceLocation WIDGETS_TEXTURES = new ResourceLocation("textures/gui/widgets.png");

    /*
     * Idea for new position system when i get around to re writing modular gui. On the front end everything would still use absolute positioning
     * But behind the scenes all positions would be stored as relative positions. Relative to the the position of the parent element.
     * The getter's would just return the parent's position plus a relative offset. This means things like the current position modifiers would
     * simply just works with child nodes.
     *
     * I may expand this idea into a full geometry system that includes both pos and size. I could do things like allow you to specify what side of the parent
     * and element is attached to. So for example expanding width of tha parent element could keep the child attached to the right side of the element rather than leaving it where it is.
     *
     * Another idea would be to add an anchor system that allows me to anchor a side of an element to another element with some offset.
     * Something like element.geometry.anchor(otherElement, Side.RIGHT, Side.RIGHT, -10)
     *
     *
     * */

    private int xPos;
    private int yPos;
    private int lastTickXPos;
    private int lastTickYPos;
    private int animFrameX;
    private int animFrameY;
    private int animTranslateX;
    private int animTranslateY;
    private int animSpeed;
    private int xSize;
    private int ySize;
    private Point parentRelPos;
    private Point parentInsetRelPos;
    private Insets insets = new Insets(0, 0, 0, 0);
    private boolean enabled = true;
    private boolean elementInitialized = false;
    private Rectangle insetRectangle = new Rectangle();
    private List<String> groups = new ArrayList<>();
    private GuiElement parentElement = null;
    private Supplier<Boolean> enabledCallback = null;
    private Rectangle rectangle = new Rectangle();

    protected int hoverTime = 0;
    protected int hoverTextDelay = 0;
    protected IDrawCallback preDrawCallback = null;
    protected IDrawCallback postDrawCallback = null;

    /**
     * An id that is unique to this element (may or may not be used. If unused will be an empty string)
     */
    protected String id = "";
    /**
     * If true {@link #addBoundsToRect(Rectangle)} will ignore the bounds of this element and only check child elements.
     */
    protected boolean boundless = false;
    /**
     * Offsets the zLevel when rendering
     */
    protected boolean drawHoverText = false;
    protected boolean capturesClicks = false;
    protected boolean frameAnimation = false;
    /**
     * When true child elements will be disabled when they are removed.
     * This is useful for edge cases where the 1 tick delay in removing an element can cause issues.
     */
    protected boolean disableOnRemove = false;
    protected boolean animatedTranslating = false;
    protected List<GuiElement> toRemove = new ArrayList<>();
    protected List<GuiElement> boundSizeElements = new ArrayList<>();
    protected List<GuiElement> boundInsetSizeElements = new ArrayList<>();
    protected LinkedList<GuiElement> childElements = new LinkedList<>();

    //TODO Is this scaled or absolute?
    public int screenWidth;
    public int screenHeight;
    /**
     * For use by ModuleManager ONLY
     */
    //TODO When i re-write this i need to nuke the entire display level system and implement something better that
    // supports not only offsetting the position of an element but its child elements as well. It should probably
    // also be a separate system not tied to GuiElementManager (if i even keep GuiElementManager)
    public int displayZLevel = 0;
    public double zOffset = 0;
    /**
     * Can simply be used to store an object reference on this element. Use for whatever you like.
     */
    public Object linkedObject = null;
    public boolean reportXSizeChange = false;
    public boolean reportYSizeChange = false;
    /**
     * If enabled this element will return true in renderOverlay when the mouse is over this element.
     */
    public boolean consumeHoverOverlay = false;
    public Minecraft mc = Minecraft.getInstance();
    public IModularGui modularGui;
    public BCFontRenderer fontRenderer = BCFontRenderer.convert(mc.fontRenderer);

    //Lambdas!
    protected Consumer<E> onReload = null;
    protected Consumer<E> onInit = null;
    protected HoverTextSupplier<?, E> hoverText = null;
    protected BiFunction<E, Integer, Integer> xPosModifier = null;
    protected BiFunction<E, Integer, Integer> yPosModifier = null;
    protected BiFunction<E, Integer, Integer> xSizeModifier = null;
    protected BiFunction<E, Integer, Integer> ySizeModifier = null;
    protected BiConsumer<Integer, Integer> posChangeListener = null;

    public GuiElement() {
    }

    public GuiElement(int xPos, int yPos) {
        setPos(xPos, yPos);
    }

    public GuiElement(int xPos, int yPos, int xSize, int ySize) {
        this(xPos, yPos);
        setSize(xSize, ySize);
    }

    //# Init & Reload
    //region //############################################################################

    /**
     * Called once after creation, Use this to add any child elements.
     * Note: the modularGui, mc, screenWidth and screenHeight fields *Should* be initialized at this point.
     * Assuming the parent modular gui is setup correctly.
     */
    @SuppressWarnings("unchecked")
    public void addChildElements() {
        if (elementInitialized) {
            throw new RuntimeException("MGuiElementBase.addChildElements was fired but child elements have already been added!");
        }

        if (onInit != null) {
            onInit.accept((E) this);
        }

        elementInitialized = true;
    }

    /**
     * Can be called more then once, Called when gui init fires and can be called manually.
     * This should generally be used to set thinks like size and position of elements if they need to be set relative to
     * screen size for example.
     * To use this you can ether override this method or for convenience you can attach a reload handler when you create the element
     * via the addReloadCallback() method.
     * <p>
     * Its important to remember that this method must be able to be called more than once where the addChildElements method can ONLY be fired once.
     * This is called immediately after addChildElements fires and whenever the initGui method of the parent gui fires.
     */
    @SuppressWarnings("unchecked")
    public void reloadElement() {
        if (onReload != null) {
            onReload.accept((E) this);
        }
        for (GuiElement element : childElements) {
            element.reloadElement();
        }
    }

    /**
     * This method allows you to add a consumer that will be called every time this element is reloaded.
     * The consumer will be called with this as its input.
     * You can add more than one callback and they will be called one after the other.
     * <p>
     * Its worth remembering that reload is also called when the element is added to its parent.
     */
    @SuppressWarnings("unchecked")
    @Deprecated
    public E addReloadCallback(Consumer<E> callBack) {
        onReload = onReload != null ? onReload.andThen(callBack) : callBack;
        return (E) this;
    }

    /**
     * This is the same as {@link #addReloadCallback(Consumer)} except this method also immediately fires the callback.
     * This is useful for situations where the callback sets critical values such as the size and pos which need to be set before child elements are added.
     */
    @Deprecated
    public E addAndFireReloadCallback(Consumer<E> callBack) {
        onReload = onReload != null ? onReload.andThen(callBack) : callBack;
        onReload.accept((E) this);
        return (E) this;
    }

    /**
     * Adds a reload call back that will be fired on creation of the callback and then every time the element is reloaded.
     */
    public E onReload(Consumer<E> callBack) {
        return onReload(callBack, true);
    }

    /**
     * Adds a reload call back that will be fired every time the element is reloaded. With the option of firing the callback now
     */
    public E onReload(Consumer<E> callBack, boolean callNow) {
        onReload = onReload != null ? onReload.andThen(callBack) : callBack;
        if (callNow) {
            onReload.accept((E) this);
        }
        return (E) this;
    }

    /**
     * This method allows you to add a consumer that will be called when the element is initialized.
     * The consumer will be called with this as its input.
     * You can add more than one callback and they will be called one after the other.
     */
    @SuppressWarnings("unchecked")
    public E addInitCallback(Consumer<E> callBack) {
        onInit = onInit != null ? onInit.andThen(callBack) : callBack;
        return (E) this;
    }

    //endregion

    //# Child Elements
    //region //############################################################################

    /**
     * Adds a new child element to this element.
     *
     * @return The child element that was added.
     */
    @Override
    public <C extends GuiElement> C addChild(C child) {
        if (child == this) throw new InvalidParameterException("Attempted to add element to itself as a child element.");
        if (childElements.contains(child)) {
            return child;
        }
        childElements.add(child);
        onChildAdded(child);
        return child;
    }

    /**
     * Adds a new child element to this element at index 0 so it will render behind all other elements previously added.
     *
     * @return The child element that was added.
     */
    @Override
    public <C extends GuiElement> C addBackGroundChild(C child) {
        if (childElements.contains(child)) {
            return child;
        }
        childElements.addFirst(child);
        onChildAdded(child);
        return child;
    }

    /**
     * Adds a Collection of child elements to this element.
     */
    @SuppressWarnings("unchecked")
    @Override
    public E addChildren(Collection<? extends GuiElement> elements) {
        childElements.addAll(elements);
        for (GuiElement element : elements) {
            onChildAdded(element);
        }
        return (E) this;
    }

    /**
     * Called when a child element is added to this element. Used to initialize the child element and
     * tell it to add its own child elements.
     * It should be noted that this will only call addChildElements if it has not already been called.
     * Meaning it is safe to add an element to more than one parent(Though that may break in other ways)
     * and its also save to remove an element from its parent and re add it or add it to another parent.
     */
    protected void onChildAdded(GuiElement childElement) {
        childElement.applyGeneralElementData(modularGui, mc, screenWidth, screenHeight, fontRenderer);
        childElement.setParent(this);
        if (!childElement.elementInitialized) {
            childElement.addChildElements();
        }
        childElement.reloadElement();
        addDefaultListener(childElement);
    }

    /**
     * Called when a child element is added to this element.
     * By default this checks if <br>
     * A. The child is an IGuiEventDispatcher <br>
     * B. The child does not already have a listener assigned <br>
     * C. This element is an {@link IGuiEventListener} <br>
     * If these three conditions are med then this element is set as the child element's event listener. <br>
     * If only A and B are met then this also checks if the parent modular gui is an {@link IGuiEventListener}
     * and if so adds it as the listener.
     *
     * @param childElement the child element that was added to this element.
     */
    protected void addDefaultListener(GuiElement childElement) {
        if (childElement instanceof IGuiEventDispatcher && ((IGuiEventDispatcher) childElement).getListener() == null) {
            if (this instanceof IGuiEventListener) {
                ((IGuiEventDispatcher) childElement).setListener((IGuiEventListener) this);
            } else if (modularGui instanceof IGuiEventListener) {
                ((IGuiEventDispatcher) childElement).setListener((IGuiEventListener) modularGui);
            }
        }
    }

    /**
     * This schedules a child element to be removed at the start of the next update tick.
     * The reason this works this way is to avoid concurrent modification exceptions.
     * If this 1 tick delay is an issue then you can set disableOnRemove to true for the parent element
     * which disables all child elements the instant they are scheduled for removal by this method.
     *
     * @param child the child element to remove.
     * @return the element that will be removed or null if the element was not a child of this element.
     */
    @Override
    public <C extends GuiElement> C removeChild(C child) {
        if (child != null && childElements.contains(child)) {
            toRemove.add(child);
            if (disableOnRemove) {
                child.setEnabled(false);
            }
            return child;
        }
        return null;
    }

    /**
     * Remove an element by its 'unique' id.
     * Please see setId before using this.
     *
     * @param id The id of the element to remove.
     * @see #setId(String)
     */
    @SuppressWarnings("unchecked")
    @Override
    public E removeChildByID(String id) {
        for (GuiElement element : childElements) {
            if (element.id != null && element.id.equals(id)) {
                toRemove.add(element);
//                return (E) this; //Removed this just in case someone durps and adds more than 1 child with the same id
            }
        }
        return (E) this;
    }

    /**
     * Remove all elements that are assigned to the specified group.
     *
     * @param group the name of the element group to remove.
     * @see #addToGroup(String)
     */
    @SuppressWarnings("unchecked")
    @Override
    public E removeChildByGroup(String group) {
        for (GuiElement element : childElements) {
            if (element.isInGroup(group)) {
                toRemove.add(element);
            }
        }
        return (E) this;
    }

    /**
     * Set en elements enabled state by its id.
     *
     * @see #setId(String)
     */
    @SuppressWarnings("unchecked")
    @Override
    public E setChildIDEnabled(String id, boolean enabled) {
        for (GuiElement element : childElements) {
            if (element.id != null && element.id.equals(id)) {
                element.enabled = enabled;
                return (E) this;
            }
        }
        return (E) this;
    }

    /**
     * Set the enabled state for all elements that are part of the specified group.
     *
     * @see #addToGroup(String)
     */
    @SuppressWarnings("unchecked")
    @Override
    public E setChildGroupEnabled(String group, boolean enabled) {
        for (GuiElement element : childElements) {
            if (element.isInGroup(group)) {
                element.enabled = enabled;
            }
        }
        return (E) this;
    }

    @SuppressWarnings("unchecked")
    public E setParent(GuiElement parent) {
        this.parentElement = parent;
        if (parentRelPos != null) {
            setRelPos(parentRelPos.x, parentRelPos.y);
            parentRelPos = null;
        }
        if (parentInsetRelPos != null) {
            setInsetRelPos(parentInsetRelPos.x, parentInsetRelPos.y);
            parentInsetRelPos = null;
        }
        return (E) this;
    }

    @Nullable
    public GuiElement getParent() {
        return parentElement;
    }

    /**
     * Returns a unmodifiable view of the children.
     *
     * @return The children.
     */
    public List<GuiElement> getChildElements() {
        return Collections.unmodifiableList(childElements);
    }

    //endregion

    //# Group & ID Stuff
    //region //############################################################################

    /**
     * Adds this element to the specified "group" You can then perform certain actions on this element group.
     * <p>
     * e.g. Say you create a gui with 2 different "screens" where each screen needs to display different elements.
     * You can assign all of the elements for screen 1 to a group called "screen1" and elements for screen 2 could be assigned to
     * "screen2" you can then simply call {@link #setChildGroupEnabled(String, boolean)} on the parent to enable/disable
     * each group depending on which screen you want to display.
     *
     * @param group the group name.
     * @see #setChildGroupEnabled(String, boolean)
     * @see #removeFromGroup(String)
     * @see #removeFromAllGroups()
     * @see #isInGroup(String)
     * @see #getGroups()
     */
    @SuppressWarnings("unchecked")
    public E addToGroup(String group) {
        groups.add(group);
        return (E) this;
    }

    /**
     * Removed this element from the specified group.
     *
     * @param group the group to remove this element from.
     * @see #addToGroup(String)
     */
    @SuppressWarnings("unchecked")
    public E removeFromGroup(String group) {
        if (groups.contains(group)) {
            groups.remove(group);
        }
        return (E) this;
    }

    /**
     * Remove this element from all groups that is is currently assigned to.
     *
     * @see #addToGroup(String)
     */
    @SuppressWarnings("unchecked")
    public E removeFromAllGroups() {
        groups.clear();
        return (E) this;
    }

    /**
     * Checks if this element is assigned to the specified group.
     *
     * @param group The group to check.
     * @return true if this element is assigned to the specified group.
     * @see #addToGroup(String)
     */
    public boolean isInGroup(String group) {
        return groups.contains(group);
    }

    /**
     * @return a list of all groups this element is assigned to.
     * @see #addToGroup(String)
     */
    public List<String> getGroups() {
        return groups;
    }

    /**
     * Returns a list of all child elements that are in the specified group.
     *
     * @param group the group.
     * @return a list of all child elements in the specified group. Or an empty list if there are none.
     */
    public List<GuiElement> getChildGroup(String group) {
        List<GuiElement> list = new ArrayList<>();
        DataUtils.forEachMatch(childElements, elementBase -> elementBase.isInGroup(group), list::add);
        return list;
    }

    /**
     * This can be used to set a unique id for this element. This id can then be used to retrieve the element from
     * its parent via that id as well as some other useful functions.
     * The default id is an empty string.
     * Note: there should only ever be 1 child element with a given id but this is not enforced in any way.
     *
     * @param id The new id for this element.
     * @see #findChildById(String)
     * @see #getId()
     * @see #removeChildByID(String)
     * @see #setChildIDEnabled(String, boolean)
     */
    @SuppressWarnings("unchecked")
    public E setId(@Nonnull String id) {
        this.id = id;
        return (E) this;
    }

    /**
     * @see #setId(String)
     */
    public String getId() {
        return id;
    }

    /**
     * Used to find a child element with the specified id.
     *
     * @param id The child id to search for.
     * @return The child element with the specified id or null if no child element exists with that id.
     */
    public GuiElement findChildById(String id) {
        return DataUtils.firstMatch(childElements, elementBase -> elementBase.getId().equals(id));
    }

    /**
     * Can be used to find a child with a given id and that is an instance of the specified class.
     * If found that child will then be returns as an instance of the specified class.<br>
     * Note: This casting method is slightly less efficient then a normal instanceof then (cast) check
     * but in most cases this shouldn't be a problem.
     *
     * @param id    The child id to search for.
     * @param clazz The child class to search for.
     * @return The child element cast to clszz or null if there is no child element matching the search criteria.
     * @see #findChildById(String)
     */
    public <C extends GuiElement> C findChildById(String id, Class<C> clazz) {
        GuiElement element = DataUtils.firstMatch(childElements, elementBase -> elementBase.getId().equals(id) && clazz.isAssignableFrom(elementBase.getClass()));
        return element == null ? null : clazz.cast(element);
    }

    //endregion

    //# Mouse
    //region //############################################################################

    /**
     * @param mouseX Mouse x position
     * @param mouseY Mouse y position
     * @return true is the mouse is over this element
     */
    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return GuiHelper.isInRect(xPos(), yPos(), xSize(), ySize(), mouseX, mouseY) && allowMouseOver(this, mouseX, mouseY);
    }

    /**
     * Called whenever the mouse is clicked regardless of weather or not the mouse is over this element.
     *
     * @param mouseX      Mouse x position
     * @param mouseY      Mouse y position
     * @param button Mouse mutton pressed
     * @return Return true to prevent any further processing for this mouse action.
     * @throws IOException
     */
    //TODO in re write: Add some sort of click result enum. PASS, CONSUMED, BLOCKED (Or maybe just passed and consumed)
    // Certain things still need the call even if something else has consumed it. e.g text fields still need to get the click in order to un-focus
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (GuiElement element : childElements) {
            if (element.isEnabled() && element.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }
        return isMouseOver(mouseX, mouseY) && capturesClicks;
    }

    /**
     * Called whenever the mouse is released regardless of weather or not the mouse is over this element.
     *
     * @param mouseX      Mouse x position
     * @param mouseY      Mouse y position
     * @param button the mouse state.
     * @return Return true to prevent any further processing for this mouse action.
     */
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        for (GuiElement element : childElements) {
            if (element.isEnabled() && element.mouseReleased(mouseX, mouseY, button)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param mouseX             Mouse x position
     * @param mouseY             Mouse y position
     * @param clickedMouseButton
     * @return Return true to prevent any further processing for this mouse action.
     */
    public boolean mouseDragged(double mouseX, double mouseY, int clickedMouseButton, double dragX, double dragY) {
        for (GuiElement element : childElements) {
            if (element.isEnabled() && element.mouseDragged(mouseX, mouseY, clickedMouseButton, dragX, dragY)) {
                return true;
            }
        }
        return false;
    }

    public boolean mouseMoved(double mouseX, double mouseY) {
        for (GuiElement element : childElements) {
            if (element.isEnabled() && element.mouseMoved(mouseX, mouseY)) {
                return true;
            }
        }
        return false;
    }

//    /**
//     * Called whenever a mouse event is fired.
//     *
//     * @return Return true to prevent any further processing for this mouse action.
//     */
//    public boolean handleMouseInput() {
//        int mouseX = Mouse.getEventX() * screenWidth / mc.displayWidth;
//        int mouseY = screenHeight - Mouse.getEventY() * screenHeight / mc.displayHeight - 1;
//        int scrollDirection = Mouse.getEventDWheel();
//
//        if (scrollDirection != 0) {
//            if (handleMouseScroll(mouseX, mouseY, scrollDirection)) {
//                return true;
//            }
//            for (GuiElement element : childElements) {
//                if (element.isEnabled() && element.handleMouseScroll(mouseX, mouseY, scrollDirection)) {
//                    return true;
//                }
//            }
//        }
//
//        for (GuiElement element : childElements) {
//            if (element.isEnabled() && element.handleMouseInput()) {
//                return true;
//            }
//        }
//        return false;
//    }

    /**
     * Called whenever the scroll wheel is active.
     *
     * @param mouseX          Mouse x position
     * @param mouseY          Mouse y position
     * @param scrollDirection will ether be a positive or a negative number
     * @return true to prevent further processing on this mouse action.
     */
    public boolean handleMouseScroll(double mouseX, double mouseY, double scrollDirection) {
        for (GuiElement element : childElements) {
            if (element.isEnabled() && element.handleMouseScroll(mouseX, mouseY, scrollDirection)) {
                return true;
            }
        }
        return false;
    }

    /**
     * This is used primarily by GuiScrollElement to disable {@link #isMouseOver(double, double)} (int, int)} for areas of elements
     * that are outside the view bounds.
     */
    public boolean allowMouseOver(GuiElement elementRequesting, double mouseX, double mouseY) {
        return getParent() == null || getParent().allowMouseOver(this, mouseX, mouseY);
    }

    /**
     * Setting this to true will cause this element to capture all mouse clicks within its bounds regardless of whether or
     * not an action was performed due to the click.
     * This can be useful in cases where you have multiple child elements overlapping and you do not want a click to pass though
     * the an element on to and be captured by an element bellow.<br>
     * Note: For elements added directly to the module manager on different display levels this is not required because
     * the module manager will not pass a click to an element if the click occurred within the bounds of an element on a higher
     * display levelZLevel.
     */
    public E setCapturesClicks(boolean capturesClicks) {
        this.capturesClicks = capturesClicks;
        return (E) this;
    }

    //endregion

    //# Keyboard
    //region //############################################################################

    /**
     * Called whenever a key is typed. Return true to cancel further processing.
     */
    protected boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        for (GuiElement element : childElements) {
            if (element.isEnabled() && element.keyPressed(keyCode, scanCode, modifiers)) {
                return true;
            }
        }
        return false;
    }

    protected boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        for (GuiElement element : childElements) {
            if (element.isEnabled() && element.keyReleased(keyCode, scanCode, modifiers)) {
                return true;
            }
        }
        return false;
    }

    public boolean charTyped(char charTyped, int charCode) {
        for (GuiElement element : childElements) {
            if (element.isEnabled() && element.charTyped(charTyped, charCode)) {
                return true;
            }
        }
        return false;
    }

    //endregion

    //# Update
    //region //############################################################################

    /**
     * Called every tick to update the element. Note this is called regardless of weather or not the element is actually enabled.
     * Return true to cancel the remainder of this update call. Used primarily to avoid concurrent modification exceptions.
     */
    public boolean onUpdate() {
        if (frameAnimation && animFrameX == 0 && animFrameY == 0) {
            frameAnimation = false;
        }
        lastTickXPos = xPos;
        lastTickYPos = yPos;
        if (frameAnimation) {
            frameAnimation = false;
            translate(animFrameX, animFrameY);
            animFrameX = animFrameY = 0;
            frameAnimation = true;
        }


        if (!toRemove.isEmpty()) {
            childElements.removeAll(toRemove);
            toRemove.clear();
            return true;
        }


        //TODO Check This (mouse pos)
        int mouseX = (int) (mc.mouseHelper.getMouseX() * screenWidth / this.mc.mainWindow.getWidth());
        int mouseY = (int) (screenHeight - mc.mouseHelper.getMouseY() * screenHeight / this.mc.mainWindow.getHeight() - 1);
        if (isMouseOver(mouseX, mouseY)) {
            hoverTime++;
        } else {
            hoverTime = 0;
        }

        if (animatedTranslating) {
            int x = MathHelper.clamp(animTranslateX, -animSpeed, animSpeed);
            int y = MathHelper.clamp(animTranslateY, -animSpeed, animSpeed);
            animTranslateX -= x;
            animTranslateY -= y;
            animateMoveFrames();
            translate(x, y);
            if (animTranslateX == 0 && animTranslateY == 0) {
                animatedTranslating = false;
            }
        }


        for (GuiElement element : childElements) {
            if (element.onUpdate()) {
                return true;
            }
        }

        return false;
    }

    //endregion

    //# Render
    //region //############################################################################

    /**
     * Used only for the pre draw callback.
     */
    public final void preDraw(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
        if (preDrawCallback != null) preDrawCallback.call(minecraft, mouseX, mouseY, partialTicks, isMouseOver(mouseX, mouseY));
    }

    public void renderElement(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
        if (frameAnimation) {
            GlStateManager.pushMatrix();
            double x = ((double) lastTickXPos - xPos) * partialTicks;
            double y = ((double) lastTickYPos - yPos) * partialTicks;
            GlStateManager.translated(x, y, 0);
        }

//        if (y != 0) LogHelperBC.dev(y);

        for (GuiElement element : childElements) {
            if (element.isEnabled()) {
                element.preDraw(minecraft, mouseX, mouseY, partialTicks);
                element.renderElement(minecraft, mouseX, mouseY, partialTicks);
                element.postDraw(minecraft, mouseX, mouseY, partialTicks);
            }
        }

        if (frameAnimation) {
            GlStateManager.popMatrix();
        }
//        drawBorderedRect(xPos(), yPos(), xSize(), ySize(), 0.5, 0, 0xFF00FF00);
    }

    /**
     * Used only for the post draw callback.
     */
    public final void postDraw(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
        if (postDrawCallback != null) postDrawCallback.call(minecraft, mouseX, mouseY, partialTicks, isMouseOver(mouseX, mouseY));
    }

    /**
     * This should only be used to render things like toolTips.
     * If you return true no further renderOverlayLayer calls will occur.
     * This is useful for preventing overlapping tool tips in the event of more than 1 overlapping element rendering a tooltip.
     */
    public boolean renderOverlayLayer(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
        for (GuiElement element : childElements) {
            if (element.isEnabled() && element.renderOverlayLayer(minecraft, mouseX, mouseY, partialTicks)) {
                return true;
            }
        }

        if (isHoverTextEnabled() && isMouseOver(mouseX, mouseY) && hoverTime >= hoverTextDelay) {
            List<String> hoverText = getHoverText();
            if (!hoverText.isEmpty()) {
                drawHoveringText(hoverText, mouseX, mouseY, fontRenderer, screenWidth, screenHeight);
                return true;
            }
        }

        return isMouseOver(mouseX, mouseY) && consumeHoverOverlay;
    }

    //endregion

    //# Position
    //region //############################################################################

    //Position

    /**
     * Returns the xPosition of this element.
     * If overridden this may break compatibility with things like scroll elements which need to be able to
     * update the element position via setPos/translate.
     */
    @SuppressWarnings("unchecked")
    public int xPos() {
        return xPosModifier != null ? xPosModifier.apply((E) this, xPos) : xPos;
    }

    /**
     * Convenience method.
     * Returns the largest X coordinate of the area this element occupies
     *
     * @return the position of the right edge of this element. (xPos + xSize)
     */
    public int maxXPos() {
        return xPos() + xSize();
    }

    /**
     * Returns the yPosition of this element.
     * If overridden this may break compatibility with things like scroll elements which need to be able to
     * update the element position via setPos/translate.
     */
    @SuppressWarnings("unchecked")
    public int yPos() {
        return yPosModifier != null ? yPosModifier.apply((E) this, yPos) : yPos;
    }

    /**
     * Convenience method.
     * Returns the largest Y coordinate of the area this element occupies
     *
     * @return the position of the bottom edge of this element. (yPos + ySize)
     */
    public int maxYPos() {
        return yPos() + ySize();
    }

    /**
     * Translates this element and all of its children by the given amount.
     * This method is used by all of the setPos methods to update the position of this
     * element and all child elements.
     */
    @SuppressWarnings("unchecked")
    public E translate(int xAmount, int yAmount) {
        if (frameAnimation) {
            animFrameX += xAmount;
            animFrameY += yAmount;
            return (E) this;
        }
        xPos += xAmount;
        yPos += yAmount;
        for (GuiElement element : childElements) {
            element.translate(xAmount, yAmount);
        }
        if (posChangeListener != null) {
            posChangeListener.accept(xPos(), yPos());
        }
        return (E) this;
    }

    public E translateAnim(int xAmount, int yAmount, int speed) {
        animTranslateX += xAmount;
        animTranslateY += yAmount;
        animSpeed = speed;
        animatedTranslating = true;
        return (E) this;
    }

    @Deprecated //This is experimental code that has some issues.
    public E animateMoveFrames() {
        frameAnimation = true;
        return (E) this;
    }

    /**
     * Sets the x position of this element and also moves all of this elements children along with it.
     *
     * @param x the new x position.
     */
    @SuppressWarnings("unchecked")
    public E setXPos(int x) {
        translate(x - xPos(), 0);
        return (E) this;
    }

    /**
     * Sets the y position of this element and also moves all of this elements children along with it.
     *
     * @param y the new y position.
     */
    @SuppressWarnings("unchecked")
    public E setYPos(int y) {
        translate(0, y - yPos());
        return (E) this;
    }

    /**
     * Sets the x and y positions of this element and also moves all of this elements children along with it.
     *
     * @param x the new x position.
     * @param y the new y position.
     */
    @SuppressWarnings("unchecked")
    public E setPos(int x, int y) {
        translate(x - xPos(), y - yPos());
        return (E) this;
    }

    /**
     * Sets the position of this element to that of another element.
     */
    @SuppressWarnings("unchecked")
    public E setPos(GuiElement element) {
        setPos(element.xPos(), element.yPos());
        return (E) this;
    }

    /**
     * Sets the max x position of this element by ether moving the element or by manipulating its width.
     *
     * @param x      the new max x position.
     * @param resize If true the element will be resized so that its max x matches the specified value. If false the element will be moved instead.
     */
    public E setMaxXPos(int x, boolean resize) {
        if (resize) {
            return setXSize(x - xPos());
        } else {
            return setXPos(x - xSize());
        }
    }

    /**
     * Sets the max y position of this element by ether moving the element or by manipulating its height.
     *
     * @param y      the new max y position.
     * @param resize If true the element will be resized so that its max y matches the specified value. If false the element will be moved instead.
     */
    public E setMaxYPos(int y, boolean resize) {
        if (resize) {
            return setYSize(y - yPos());
        } else {
            return setYPos(y - ySize());
        }
    }

    /**
     * Sets the max x and y positions of this element by ether moving the element or by manipulating its size.
     *
     * @param x      the new max x position.
     * @param y      the new max y position.
     * @param resize If true the element will be resized so that its max x and y matches the specified values. If false the element will be moved instead.
     */
    @SuppressWarnings("unchecked")
    public E setMaxPos(int x, int y, boolean resize) {
        setMaxXPos(x, resize);
        setMaxYPos(y, resize);
        return (E) this;
    }

    /**
     * Sets the x and y positions of this element directly without modifying the position of child elements.
     *
     * @param x The new x position.
     * @param y the new y position.
     */
    @SuppressWarnings("unchecked")
    public E setRawPos(int x, int y) {
        xPos = x;
        yPos = y;
        return (E) this;
    }

    /**
     * This can be used to apply a function that will be called every time the x or y position of this element is requested.
     * The function is given 'this' and the current x or y pos. The value returned by the function will be the x or y pos
     * returned by the respective pos getter.
     * Note: As this is called every single time the x or y position of this element is requested
     * you should try to avoid running complex code in this method as it could generate a lot of overhead.
     * Note2: using this to alter the position of this element may break compatibility with other elements that
     * need to control the position of this element. e.g. the ScrollPane
     */
    @SuppressWarnings("unchecked")
    public E setPosModifiers(BiFunction<E, Integer, Integer> xMod, BiFunction<E, Integer, Integer> yMod) {
        this.xPosModifier = xMod;
        this.yPosModifier = yMod;
        return (E) this;
    }

    @SuppressWarnings("unchecked")
    public E setPosModifiers(Supplier<Integer> xMod, Supplier<Integer> yMod) {
        this.xPosModifier = (e, e2) -> xMod.get();
        this.yPosModifier = (e, e2) -> yMod.get();
        return (E) this;
    }

    @SuppressWarnings("unchecked")
    public E setXPosMod(BiFunction<E, Integer, Integer> xMod) {
        this.xPosModifier = xMod;
        return (E) this;
    }

    @SuppressWarnings("unchecked")
    public E setYPosMod(BiFunction<E, Integer, Integer> yMod) {
        this.yPosModifier = yMod;
        return (E) this;
    }

    @SuppressWarnings("unchecked")
    public E setXPosMod(Supplier<Integer> xMod) {
        return setXPosMod((e, integer) -> xMod.get());
    }

    @SuppressWarnings("unchecked")
    public E setYPosMod(Supplier<Integer> yMod) {
        return setYPosMod((e, integer) -> yMod.get());
    }

    /**
     * Set the position of this element relative to its parent.
     * If parent has not yet been assigned this will be applied when the parent is assigned.
     */
    @SuppressWarnings("unchecked")
    public E setRelPos(int xOffset, int yOffset) {
        if (getParent() == null) {
            parentRelPos = new Point(xOffset, yOffset);
        } else {
            setPos(getParent().xPos() + xOffset, getParent().yPos() + yOffset);
        }
        return (E) this;
    }

    /**
     * Set the position of this element relative to another element.
     */
    @SuppressWarnings("unchecked")
    public E setRelPos(@Nonnull GuiElement relativeTo, int xOffset, int yOffset) {
        setPos(relativeTo.xPos() + xOffset, relativeTo.yPos() + yOffset);
        return (E) this;
    }

    /**
     * Set the position of this element relative to the top right corner of another element.
     */
    @SuppressWarnings("unchecked")
    public E setRelPosRight(@Nonnull GuiElement relativeTo, int xOffset, int yOffset) {
        setPos(relativeTo.maxXPos() + xOffset, relativeTo.yPos() + yOffset);
        return (E) this;
    }

    /**
     * Set the position of this element relative to the bottom left corner of another element.
     */
    @SuppressWarnings("unchecked")
    public E setRelPosBottom(@Nonnull GuiElement relativeTo, int xOffset, int yOffset) {
        setPos(relativeTo.xPos() + xOffset, relativeTo.maxYPos() + yOffset);
        return (E) this;
    }

    /**
     * Set the position of this element relative to the bottom right corner of another element.
     */
    @SuppressWarnings("unchecked")
    public E setRelPosBottomRight(@Nonnull GuiElement relativeTo, int xOffset, int yOffset) {
        setPos(relativeTo.maxXPos() + xOffset, relativeTo.maxYPos() + yOffset);
        return (E) this;
    }

    /**
     * Set the position of this element relative to its parents position taking into account the parents insets.
     * If parent has not yet been assigned this will be applied when the parent is assigned.
     */
    @SuppressWarnings("unchecked")
    public E setInsetRelPos(int xOffset, int yOffset) {
        if (getParent() == null) {
            parentInsetRelPos = new Point(xOffset, yOffset);
        } else {
            setPos(getParent().getInsetRect().x + xOffset, getParent().getInsetRect().y + yOffset);
        }
        return (E) this;
    }

    /**
     * If required this adjusts the element position so that no part of it is outside of the screen.
     */
    public E normalizePosition() {
        if (xPos() < 0) setXPos(0);
        if (yPos() < 0) setYPos(0);
        if (maxXPos() > screenWidth) setXPos(screenWidth - xSize());
        if (maxYPos() > screenHeight) setYPos(screenHeight - ySize());
        return (E) this;
    }

    public E addPosChangeListener(BiConsumer<Integer, Integer> posChangeListener) {
        this.posChangeListener = this.posChangeListener == null ? posChangeListener : this.posChangeListener.andThen(posChangeListener);
        return (E) this;
    }

    //endregion

    //# Size
    //region //############################################################################

    /**
     * @return the xSize of this element
     */
    @SuppressWarnings("unchecked")
    public int xSize() {
        return xSizeModifier != null ? xSizeModifier.apply((E) this, xSize) : xSize;
    }

    /**
     * @return the ySize of this element
     */
    @SuppressWarnings("unchecked")
    public int ySize() {
        return ySizeModifier != null ? ySizeModifier.apply((E) this, ySize) : ySize;
    }

    @SuppressWarnings("unchecked")
    public E setXSize(int xSize) {
        if (animatedTranslating) {
            animatedTranslating = false;
            translate(animTranslateX, animTranslateY);
            animTranslateY = animTranslateX = 0;
        }
        this.xSize = xSize;
        boundSizeElements.forEach(elementBase -> elementBase.setXSize(xSize()));
        boundInsetSizeElements.forEach(elementBase -> elementBase.setXSize(getInsetRect().width));
        xSizeChanged(this);
        return (E) this;
    }

    @SuppressWarnings("unchecked")
    public E setYSize(int ySize) {
        if (animatedTranslating) {
            animatedTranslating = false;
            translate(animTranslateX, animTranslateY);
            animTranslateY = animTranslateX = 0;
        }
        this.ySize = ySize;
        boundSizeElements.forEach(elementBase -> elementBase.setYSize(ySize()));
        boundInsetSizeElements.forEach(elementBase -> elementBase.setYSize(getInsetRect().height));
        ySizeChanged(this);
        return (E) this;
    }

    @SuppressWarnings("unchecked")
    public E setSize(int xSize, int ySize) {
        setXSize(xSize);
        setYSize(ySize);
        return (E) this;
    }

    @SuppressWarnings("unchecked")
    public E setSize(GuiElement element) {
        setSize(element.xSize(), element.ySize());
        return (E) this;
    }

    @SuppressWarnings("unchecked")
    public E setSize(Rectangle rect) {
        setSize(rect.width, rect.height);
        return (E) this;
    }

    @SuppressWarnings("unchecked")
    public E addToXSize(int x) {
        setXSize(xSize() + x);
        return (E) this;
    }

    @SuppressWarnings("unchecked")
    public E addToYSize(int y) {
        setYSize(ySize() + y);
        return (E) this;
    }

    @SuppressWarnings("unchecked")
    public E addToSize(int x, int y) {
        addToXSize(x);
        addToYSize(y);
        return (E) this;
    }

    /**
     * Bind the size of this element to the specified element.
     *
     * @param element   The element to bind to.
     * @param insetSize Bind to inset rect size @see {@link #setInsets(Insets)}
     */
    @SuppressWarnings("unchecked")
    public E bindSize(GuiElement element, boolean insetSize) {
        if (insetSize) {
            element.boundInsetSizeElements.add(this);
        } else {
            element.boundSizeElements.add(this);
        }
        return (E) this;
    }

    /**
     * Bind the size of the specified element to the size of this element.
     *
     * @param element   The element.
     * @param insetSize Bind to inset rect size @see {@link #setInsets(Insets)}
     */
    @SuppressWarnings("unchecked")
    public E imposeSize(GuiElement element, boolean insetSize) {
        if (insetSize) {
            boundInsetSizeElements.add(element);
        } else {
            boundSizeElements.add(element);
        }
        return (E) this;
    }

    //lambda Callbacks

    /**
     * This can be used to apply a function that will be called every time the x or y size of this element is requested.
     * The function is given 'this' and the current x or y size. The value returned by the function will be the x or y size
     * returned by the respective size getter.
     * Note: As this is called every single time the x or y size of this element is requested
     * you should try to avoid running complex code in this method as it could generate a lot of overhead.
     */
    @SuppressWarnings("unchecked")
    public E setSizeModifiers(BiFunction<E, Integer, Integer> xMod, BiFunction<E, Integer, Integer> yMod) {
        this.xSizeModifier = xMod;
        this.ySizeModifier = yMod;
        return (E) this;
    }

    @SuppressWarnings("unchecked")
    public E setSizeModifiers(Supplier<Integer> xMod, Supplier<Integer> yMod) {
        this.xSizeModifier = (e, e2) -> xMod.get();
        this.ySizeModifier = (e, e2) -> yMod.get();
        ;
        return (E) this;
    }

    @SuppressWarnings("unchecked")
    public E setXSizeMod(BiFunction<E, Integer, Integer> xMod) {
        this.xSizeModifier = xMod;
        return (E) this;
    }

    @SuppressWarnings("unchecked")
    public E setYSizeMod(BiFunction<E, Integer, Integer> yMod) {
        this.ySizeModifier = yMod;
        return (E) this;
    }

    @SuppressWarnings("unchecked")
    public E setXSizeMod(Supplier<Integer> xMod) {
        return setXSizeMod((e, integer) -> xMod.get());
    }

    @SuppressWarnings("unchecked")
    public E setYSizeMod(Supplier<Integer> yMod) {
        return setYSizeMod((e, integer) -> yMod.get());
    }

    /**
     * Informs the parent when the xSize of this element changes. Used by things like GuiScrollElement
     */
    public void xSizeChanged(GuiElement elementChanged) {
        if (getParent() != null) {
            getParent().xSizeChanged(this);
        }
    }

    /**
     * Informs the parent when the ySize of this element changes. Used by things like GuiScrollElement
     */
    public void ySizeChanged(GuiElement elementChanged) {
        if (getParent() != null) {
            getParent().ySizeChanged(this);
        }
    }

    //endregion

    //# Size & Position
    //region //############################################################################

    @SuppressWarnings("unchecked")
    public E setPosAndSize(GuiElement element) {
        setPos(element);
        setSize(element);
        return (E) this;
    }

    @SuppressWarnings("unchecked")
    public E setPosAndSize(Rectangle rect) {
        setPos(rect.x, rect.y);
        setSize(rect.width, rect.height);
        return (E) this;
    }

    @SuppressWarnings("unchecked")
    public E setPosAndSize(int xPos, int yPos, int xSize, int ySize) {
        setPos(xPos, yPos);
        setSize(xSize, ySize);
        return (E) this;
    }

    /**
     * @return the size and pos of this element as a {@link Rectangle}
     */
    public Rectangle getRect() {
        rectangle.setBounds(xPos(), yPos(), xSize(), ySize());
        return rectangle;
    }

    /**
     * Returns a new {@link Rectangle} the bounds of which will enclose this element and all of its child
     * elements recursively.
     */
    public Rectangle getEnclosingRect() {
        return addBoundsToRect(getRect().getBounds());
    }

    /**
     * Expands the bounds of the given rectangle (if needed) so that they enclose this element.
     * And all of its child elements recursively.
     */
    public Rectangle addBoundsToRect(Rectangle enclosingRect) {
        if (!boundless) {
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
        }

        for (GuiElement element : childElements) {
            if (!toRemove.contains(element) && element.isEnabled()) {
                element.addBoundsToRect(enclosingRect);
            }
        }
        return enclosingRect;
    }

    /**
     * Set the insets used when rendering some elements. This will apply a border/empty space around the element.
     * Only works with elements that implement getInsetRect() or getInsets().
     *
     * @return this
     */
    public E setInsets(Insets insets) {
        this.insets = insets;
        return (E) this;
    }

    /**
     * Set the insets used when rendering some elements. This will apply a border/empty space around the element.
     * Only works with elements that implement getInsetRect() or getInsets().
     *
     * @return this
     */
    public E setInsets(int top, int left, int bottom, int right) {
        this.insets.set(top, left, bottom, right);
        return (E) this;
    }

    /**
     * @return Insets applied to this element. These are basically just empty space around the element.
     */
    public Insets getInsets() {
        return insets;
    }

    /**
     * Returns a rectangle with the current insets applied. There are no strict requirements as to how this should be used.
     * One example usage is in GuiScrollElement where it is used to set the boarder area around the element.
     *
     * @return a {@link Rectangle} with the current {@link Insets} applied to it. This can be used for all rendering and is by default implemented in isMouseOver.
     */
    public Rectangle getInsetRect() {
        insetRectangle.setBounds(getRect());
        insetRectangle.setLocation(xPos() + getInsets().left, yPos() + getInsets().top);
        insetRectangle.setSize(xSize() - (getInsets().left + getInsets().right), ySize() - (getInsets().top + getInsets().bottom));
        return insetRectangle;
    }

    //endregion

    //# Misc
    //region //############################################################################

    public Screen getScreen() {
        return modularGui.getScreen();
    }

    /**
     * I was going to cache this to avoid rebinding the same texture multiple times but it turns out vanilla already does this.
     *
     * @param texture The texture to bind.
     */
    public void bindTexture(ResourceLocation texture) {
        if (mc != null) {
            mc.getTextureManager().bindTexture(texture);
        }
    }

    public void applyGeneralElementData(IModularGui modularGui, Minecraft mc, int width, int height, BCFontRenderer fontRenderer) {
        this.mc = mc;
        this.fontRenderer = fontRenderer;
        this.screenWidth = width;
        this.screenHeight = height;
        this.modularGui = modularGui;
        for (GuiElement element : childElements) {
            element.applyGeneralElementData(modularGui, mc, width, height, fontRenderer);
        }
    }

    /**
     * This method allows you to initialize the element using another already initialized element.
     *
     * @param initializer the initialized element from which to retrieve the initialization variables.
     */
    public void initializeElementData(GuiElement initializer) {
        this.mc = initializer.mc;
        if (mc == null) mc = Minecraft.getInstance();
        this.fontRenderer = initializer.fontRenderer;
        this.screenWidth = initializer.screenWidth;
        this.screenHeight = initializer.screenHeight;
        this.modularGui = initializer.modularGui;
        for (GuiElement element : childElements) {
            element.applyGeneralElementData(modularGui, mc, screenWidth, screenHeight, fontRenderer);
        }
    }

    @SuppressWarnings("unchecked")
    public E setLinkedObject(Object linkedObject) {
        this.linkedObject = linkedObject;
        return (E) this;
    }

    @SuppressWarnings("unchecked")
    public E setEnabled(boolean enabled) {
        this.enabled = enabled;
        return (E) this;
    }

    public E setEnabledCallback(Supplier<Boolean> enabledCallback) {
        this.enabledCallback = enabledCallback;
        return (E) this;
    }

    public boolean isEnabled() {
        return enabledCallback == null ? enabled : enabledCallback.get();
    }

    public boolean isElementInitialized() {
        return elementInitialized;
    }

    /**
     * Returns how far from this element the given point is.
     */
    public int distFromElement(double x, double y) {
        if (x >= xPos() && x <= xPos() + xSize() && y >= yPos() && y <= yPos() + ySize()) {
            return 0;
        }

        double xDist = x < xPos() ? xPos() - x : x - (xPos() + xSize());
        double yDist = y < yPos() ? yPos() - y : y - (yPos() + ySize());
        return (int) Math.max(xDist, yDist);
    }

    /**
     * Used by {@link GuiElementManager} to generate a list of all elements who's bounds contain the given position.
     * This can for example be used to find all elements that are currently under the mouse cursor.
     *
     * @param posX The x position.
     * @param posY The y position.
     * @param list The list of elements.
     * @return the list of elements.
     */
    public List<GuiElement> getElementsAtPosition(double posX, double posY, List<GuiElement> list) {
        if (isMouseOver(posX, posY)) {
            list.add(this);
        }

        for (GuiElement element : childElements) {
            element.getElementsAtPosition(posX, posY, list);
        }

        return list;
    }

    /**
     * Recursively finds all child elements that are an instance of the specified class and adds them
     * to the given list.
     */
    public <C extends GuiElement> List<C> findChildElementsByClass(Class<C> clazz, List<C> list) {
        if (clazz.isAssignableFrom(this.getClass())) {
            list.add(clazz.cast(this));
        }

        for (GuiElement element : childElements) {
            element.findChildElementsByClass(clazz, list);
        }
        return list;
    }

    public E setPreDrawCallback(IDrawCallback preDrawCallback) {
        this.preDrawCallback = preDrawCallback;
        return (E) this;
    }

    public E setPostDrawCallback(IDrawCallback postDrawCallback) {
        this.postDrawCallback = postDrawCallback;
        return (E) this;
    }

    @Override
    public String toString() {
        return String.format("%s:[x=%s,y=%s,w=%s,h=%s|ix=%s,iy=%s,iw=%s,ih=%s]", //
                getClass().getSimpleName(), //
                xPos(), yPos(), xSize(), ySize(), //
                getInsetRect().x, getInsetRect().y, getInsetRect().width, getInsetRect().height);
    }

    public E modifyZOffset(double zOffset) {
        this.zOffset += zOffset;
        childElements.forEach(e -> e.modifyZOffset(zOffset));
        return (E) this;
    }

    public double getZOffset() {
        return zOffset;
    }

    //TODO verify this is correct
    /**The 'Actual' width in 'real' pixels of the Minecraft window*/
    public int displayWidth() {
        return mc.mainWindow.getWidth();
    }

    /**The 'Actual' height in 'real' pixels of the Minecraft window*/
    public int displayHeight() {
        return mc.mainWindow.getHeight();
    }

    //TODO verify this is correct
    public double getMouseX() {
        return mc.mouseHelper.getMouseX() * screenWidth / displayWidth();
    }

    public double getMouseY() {
        return screenHeight - mc.mouseHelper.getMouseY() * screenHeight / displayHeight() - 1;
    }

    public List<String> getTooltipFromItem(ItemStack stack) {
        List<ITextComponent> list = stack.getTooltip(mc.player, mc.gameSettings.advancedItemTooltips ? ITooltipFlag.TooltipFlags.ADVANCED : ITooltipFlag.TooltipFlags.NORMAL);
        List<String> list1 = Lists.newArrayList();

        for(ITextComponent itextcomponent : list) {
            list1.add(itextcomponent.getFormattedText());
        }

        return list1;
    }

    //endregion

    //# GUI Render Helper ports
    //region //############################################################################

    //TODO change to render depth
    public double getRenderZLevel() {
        return modularGui.getZLevel() + zOffset;// + (parentElement == null ? 0 : parentElement.getRenderZLevel());
    }

    public void drawHorizontalLine(double startX, double endX, double y, int color) {
        if (endX < startX) {
            double i = startX;
            startX = endX;
            endX = i;
        }

        drawRect(startX, y, endX + 1, y + 1, color);
    }

    public void drawVerticalLine(double x, double startY, double endY, int color) {
        if (endY < startY) {
            double i = startY;
            startY = endY;
            endY = i;
        }

        drawRect(x, startY + 1, x + 1, endY, color);
    }

    public void drawRect(double left, double top, double right, double bottom, int color) {
        double zLevel = getRenderZLevel();
        if (left < right) {
            double i = left;
            left = right;
            right = i;
        }

        if (top < bottom) {
            double j = top;
            top = bottom;
            bottom = j;
        }

        float f3 = (float) (color >> 24 & 255) / 255.0F;
        float f = (float) (color >> 16 & 255) / 255.0F;
        float f1 = (float) (color >> 8 & 255) / 255.0F;
        float f2 = (float) (color & 255) / 255.0F;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture();
        GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.color4f(f, f1, f2, f3);
        buffer.begin(7, DefaultVertexFormats.POSITION);
        buffer.pos(left, bottom, zLevel).endVertex();
        buffer.pos(right, bottom, zLevel).endVertex();
        buffer.pos(right, top, zLevel).endVertex();
        buffer.pos(left, top, zLevel).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture();
        GlStateManager.disableBlend();
    }

    public void drawTexturedModalRect(int x, int y, int textureX, int textureY, int width, int height) {
        double zLevel = getRenderZLevel();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(7, DefaultVertexFormats.POSITION_TEX);
        buffer.pos((double) (x), (double) (y + height), zLevel).tex((double) ((float) (textureX) * 0.00390625F), (double) ((float) (textureY + height) * 0.00390625F)).endVertex();
        buffer.pos((double) (x + width), (double) (y + height), zLevel).tex((double) ((float) (textureX + width) * 0.00390625F), (double) ((float) (textureY + height) * 0.00390625F)).endVertex();
        buffer.pos((double) (x + width), (double) (y), zLevel).tex((double) ((float) (textureX + width) * 0.00390625F), (double) ((float) (textureY) * 0.00390625F)).endVertex();
        buffer.pos((double) (x), (double) (y), zLevel).tex((double) ((float) (textureX) * 0.00390625F), (double) ((float) (textureY) * 0.00390625F)).endVertex();
        tessellator.draw();
    }

    public void drawTexturedModalRect(double xCoord, double yCoord, int minU, int minV, int maxU, int maxV) {
        double zLevel = getRenderZLevel();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(7, DefaultVertexFormats.POSITION_TEX);
        buffer.pos((xCoord + 0.0F), (yCoord + (float) maxV), zLevel).tex((double) ((float) (minU) * 0.00390625F), (double) ((float) (minV + maxV) * 0.00390625F)).endVertex();
        buffer.pos((xCoord + (float) maxU), (yCoord + (float) maxV), zLevel).tex((double) ((float) (minU + maxU) * 0.00390625F), (double) ((float) (minV + maxV) * 0.00390625F)).endVertex();
        buffer.pos((xCoord + (float) maxU), (yCoord + 0.0F), zLevel).tex((double) ((float) (minU + maxU) * 0.00390625F), (double) ((float) (minV) * 0.00390625F)).endVertex();
        buffer.pos((xCoord + 0.0F), (yCoord + 0.0F), zLevel).tex((double) ((float) (minU) * 0.00390625F), (double) ((float) (minV) * 0.00390625F)).endVertex();
        tessellator.draw();
    }

    public void drawTexturedModalRect(int xCoord, int yCoord, TextureAtlasSprite textureSprite, int widthIn, int heightIn) {
        double zLevel = getRenderZLevel();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(7, DefaultVertexFormats.POSITION_TEX);
        buffer.pos((double) (xCoord), (double) (yCoord + heightIn), zLevel).tex((double) textureSprite.getMinU(), (double) textureSprite.getMaxV()).endVertex();
        buffer.pos((double) (xCoord + widthIn), (double) (yCoord + heightIn), zLevel).tex((double) textureSprite.getMaxU(), (double) textureSprite.getMaxV()).endVertex();
        buffer.pos((double) (xCoord + widthIn), (double) (yCoord), zLevel).tex((double) textureSprite.getMaxU(), (double) textureSprite.getMinV()).endVertex();
        buffer.pos((double) (xCoord), (double) (yCoord), zLevel).tex((double) textureSprite.getMinU(), (double) textureSprite.getMinV()).endVertex();
        tessellator.draw();
    }

    public void drawModalRectWithCustomSizedTexture(double x, double y, double u, double v, double width, double height, double textureWidth, double textureHeight) {
        double zLevel = getRenderZLevel();
        double f = 1.0D / textureWidth;
        double f1 = 1.0D / textureHeight;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(7, DefaultVertexFormats.POSITION_TEX);
        buffer.pos(x, (y + height), zLevel).tex((u * f), ((v + height) * f1)).endVertex();
        buffer.pos((x + width), (y + height), zLevel).tex(((u + width) * f), ((v + height) * f1)).endVertex();
        buffer.pos((x + width), y, zLevel).tex(((u + width) * f), (v * f1)).endVertex();
        buffer.pos(x, y, zLevel).tex((u * f), (v * f1)).endVertex();
        tessellator.draw();
    }

    public void drawScaledCustomSizeModalRect(double xPos, double yPos, double u, double v, double uWidth, double vHeight, double width, double height, double textureSheetWidth, double testureSheetHeight) {
        double zLevel = getRenderZLevel();
        double f = 1.0F / textureSheetWidth;
        double f1 = 1.0F / testureSheetHeight;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(7, DefaultVertexFormats.POSITION_TEX);
        buffer.pos(xPos, (yPos + height), zLevel).tex((u * f), ((v + vHeight) * f1)).endVertex();
        buffer.pos((xPos + width), (yPos + height), zLevel).tex(((u + uWidth) * f), ((v + vHeight) * f1)).endVertex();
        buffer.pos((xPos + width), yPos, zLevel).tex(((u + uWidth) * f), (v * f1)).endVertex();
        buffer.pos(xPos, yPos, zLevel).tex((u * f), (v * f1)).endVertex();
        tessellator.draw();
    }

    /**
     * Forges drawHoveringText for drawing item tool tips. Modified to work with the modular GUI system.
     * My only concern with implementing forge events in the modular gui system is that other mods may try to replace this call with their own renderer
     * which in the context of a modular gui would probably break.
     */
    public void drawHoveringText(@Nonnull final ItemStack stack, List<String> textLines, int mouseX, int mouseY, int screenWidth, int screenHeight, int maxTextWidth, BCFontRenderer font) {
        if (!textLines.isEmpty()) {
            RenderTooltipEvent.Pre event = new RenderTooltipEvent.Pre(stack, textLines, mouseX, mouseY, screenWidth, screenHeight, maxTextWidth, font);
            if (MinecraftForge.EVENT_BUS.post(event)) {
                return;
            }
            mouseX = event.getX();
            mouseY = event.getY();
            screenWidth = event.getScreenWidth();
            screenHeight = event.getScreenHeight();
            maxTextWidth = event.getMaxWidth();
            font = event.getFontRenderer() == font ? font : BCFontRenderer.convert(event.getFontRenderer());

            GlStateManager.disableRescaleNormal();
            RenderHelper.disableStandardItemLighting();
            GlStateManager.disableLighting();
            GlStateManager.disableDepthTest();
            int tooltipTextWidth = 0;

            for (String textLine : textLines) {
                int textLineWidth = font.getStringWidth(textLine);

                if (textLineWidth > tooltipTextWidth) {
                    tooltipTextWidth = textLineWidth;
                }
            }

            boolean needsWrap = false;

            int titleLinesCount = 1;
            int tooltipX = mouseX + 12;
            if (tooltipX + tooltipTextWidth + 4 > screenWidth) {
                tooltipX = mouseX - 16 - tooltipTextWidth;
                if (tooltipX < 4) // if the tooltip doesn't fit on the screen
                {
                    if (mouseX > screenWidth / 2) {
                        tooltipTextWidth = mouseX - 12 - 8;
                    } else {
                        tooltipTextWidth = screenWidth - 16 - mouseX;
                    }
                    needsWrap = true;
                }
            }

            if (maxTextWidth > 0 && tooltipTextWidth > maxTextWidth) {
                tooltipTextWidth = maxTextWidth;
                needsWrap = true;
            }

            if (needsWrap) {
                int wrappedTooltipWidth = 0;
                List<String> wrappedTextLines = new ArrayList<String>();
                for (int i = 0; i < textLines.size(); i++) {
                    String textLine = textLines.get(i);
                    List<String> wrappedLine = font.listFormattedStringToWidth(textLine, tooltipTextWidth);
                    if (i == 0) {
                        titleLinesCount = wrappedLine.size();
                    }

                    for (String line : wrappedLine) {
                        int lineWidth = font.getStringWidth(line);
                        if (lineWidth > wrappedTooltipWidth) {
                            wrappedTooltipWidth = lineWidth;
                        }
                        wrappedTextLines.add(line);
                    }
                }
                tooltipTextWidth = wrappedTooltipWidth;
                textLines = wrappedTextLines;

                if (mouseX > screenWidth / 2) {
                    tooltipX = mouseX - 16 - tooltipTextWidth;
                } else {
                    tooltipX = mouseX + 12;
                }
            }

            int tooltipY = mouseY - 12;
            int tooltipHeight = 8;

            if (textLines.size() > 1) {
                tooltipHeight += (textLines.size() - 1) * 10;
                if (textLines.size() > titleLinesCount) {
                    tooltipHeight += 2; // gap between title lines and next lines
                }
            }

            if (tooltipY + tooltipHeight + 6 > screenHeight) {
                tooltipY = screenHeight - tooltipHeight - 6;
            }

            zOffset++;
            final int backgroundColor = 0xF0100010;
            drawGradientRect(tooltipX - 3, tooltipY - 4, tooltipX + tooltipTextWidth + 3, tooltipY - 3, backgroundColor, backgroundColor);
            drawGradientRect(tooltipX - 3, tooltipY + tooltipHeight + 3, tooltipX + tooltipTextWidth + 3, tooltipY + tooltipHeight + 4, backgroundColor, backgroundColor);
            drawGradientRect(tooltipX - 3, tooltipY - 3, tooltipX + tooltipTextWidth + 3, tooltipY + tooltipHeight + 3, backgroundColor, backgroundColor);
            drawGradientRect(tooltipX - 4, tooltipY - 3, tooltipX - 3, tooltipY + tooltipHeight + 3, backgroundColor, backgroundColor);
            drawGradientRect(tooltipX + tooltipTextWidth + 3, tooltipY - 3, tooltipX + tooltipTextWidth + 4, tooltipY + tooltipHeight + 3, backgroundColor, backgroundColor);
            final int borderColorStart = 0x505000FF;
            final int borderColorEnd = (borderColorStart & 0xFEFEFE) >> 1 | borderColorStart & 0xFF000000;
            drawGradientRect(tooltipX - 3, tooltipY - 3 + 1, tooltipX - 3 + 1, tooltipY + tooltipHeight + 3 - 1, borderColorStart, borderColorEnd);
            drawGradientRect(tooltipX + tooltipTextWidth + 2, tooltipY - 3 + 1, tooltipX + tooltipTextWidth + 3, tooltipY + tooltipHeight + 3 - 1, borderColorStart, borderColorEnd);
            drawGradientRect(tooltipX - 3, tooltipY - 3, tooltipX + tooltipTextWidth + 3, tooltipY - 3 + 1, borderColorStart, borderColorStart);
            drawGradientRect(tooltipX - 3, tooltipY + tooltipHeight + 2, tooltipX + tooltipTextWidth + 3, tooltipY + tooltipHeight + 3, borderColorEnd, borderColorEnd);

            MinecraftForge.EVENT_BUS.post(new RenderTooltipEvent.PostBackground(stack, textLines, tooltipX, tooltipY, font, tooltipTextWidth, tooltipHeight));
            int tooltipTop = tooltipY;

            for (int lineNumber = 0; lineNumber < textLines.size(); ++lineNumber) {
                String line = textLines.get(lineNumber);
//                font.drawStringWithShadow(line, (float) tooltipX, (float) tooltipY, -1);
                drawString(font, line, (float) tooltipX, (float) tooltipY, -1, true);

                if (lineNumber + 1 == titleLinesCount) {
                    tooltipY += 2;
                }

                tooltipY += 10;
            }

            zOffset--;

            MinecraftForge.EVENT_BUS.post(new RenderTooltipEvent.PostText(stack, textLines, tooltipX, tooltipTop, font, tooltipTextWidth, tooltipHeight));

            GlStateManager.enableLighting();
            GlStateManager.enableDepthTest();
            RenderHelper.enableStandardItemLighting();
            GlStateManager.enableRescaleNormal();
        }
    }


    //endregion

    //# Custom Render Helpers
    //region //############################################################################

    /**
     * Simply draws a string with the given colour and no shadow.
     */
    public int drawString(BCFontRenderer fontRenderer, String text, float x, float y, int colour) {
        return drawString(fontRenderer, text, x, y, colour, false);
    }

    /**
     * Draws a string with the given colour and optional shadow.
     */
    public int drawString(FontRenderer fontRenderer, String text, float x, float y, int colour, boolean dropShadow) {
        GlStateManager.pushMatrix();
        GlStateManager.translated(0, 0, getRenderZLevel() + 1);
        int i;
        if (dropShadow) {
            i = fontRenderer.drawStringWithShadow(text, x, y, colour);
        } else {
            i = fontRenderer.drawString(text, x, y, colour);
        }
        GlStateManager.popMatrix();
        return i;
    }

    /**
     * Draws a centered string
     */
    public void drawCenteredString(BCFontRenderer fontRenderer, String text, float x, float y, int colour, boolean dropShadow) {
        GlStateManager.pushMatrix();
        GlStateManager.translated(0, 0, getRenderZLevel() + 1);
        if (dropShadow) {
            fontRenderer.drawStringWithShadow(text, x - fontRenderer.getStringWidth(text) / 2F, y, colour);
        } else {
            fontRenderer.drawString(text, x - fontRenderer.getStringWidth(text) / 2F, y, colour);
        }
        GlStateManager.popMatrix();
    }

    /**
     * Draws a split string (multi line string)
     */
    public void drawSplitString(BCFontRenderer fontRenderer, String text, float x, float y, int wrapWidth, int colour, boolean dropShadow) {
        for (String s : fontRenderer.listFormattedStringToWidth(text, wrapWidth)) {
            drawString(fontRenderer, s, x, y, colour, dropShadow);
            y += fontRenderer.FONT_HEIGHT;
        }
    }

    /**
     * Draws a centered split string
     */
    public void drawCenteredSplitString(BCFontRenderer fontRenderer, String str, float x, float y, int wrapWidth, int colour, boolean dropShadow) {
        for (String s : fontRenderer.listFormattedStringToWidth(str, wrapWidth)) {
            drawCenteredString(fontRenderer, s, x, y, colour, dropShadow);
            y += fontRenderer.FONT_HEIGHT;
        }
    }

    /**
     * This is an advanced draw string method with all sorts of built in fancy stuff.
     *
     * @param width     This xSize is used for alignment, Wrapping and trimming. (or technically ySize if at a 90 degree rotation)
     * @param alignment Allows you to align the text ether to the left, in the middle or to the right ("right" is defined by x + xSize)
     * @param rotation  Allows you to rotate the text
     * @param wrap      if true the text will wrap (milty line text) if the text is longer than xSize. (Not compatible with trim)
     * @param trim      if true the text will be trimmed to xSize if it is too long. When trimmed "..." will be appended to the end of the string.
     */
    public void drawCustomString(BCFontRenderer fr, String text, float x, float y, int width, int colour, GuiAlign alignment, TextRotation rotation, boolean wrap, boolean trim, boolean dropShadow) {
        if (width <= 0) return;
        if (trim && fr.getStringWidth(text) > width) {
            text = fr.trimStringToWidth(text, width - 8) + "..";
        }

        if (rotation == TextRotation.NORMAL) {
            if (wrap) {
                drawAlignedSplitString(fr, text, x, y, width, alignment, colour, dropShadow);
            } else {
                drawAlignedString(fr, text, x, y, width, alignment, colour, dropShadow, trim);
            }
        } else {
            GlStateManager.pushMatrix();
            if (rotation == TextRotation.ROT_C) {
                GlStateManager.translated(x, y, 0);
                GlStateManager.rotated(90, 0, 0, 1);
            } else if (rotation == TextRotation.ROT_CC) {
                GlStateManager.translated(x, y + width, 0);
                GlStateManager.rotated(-90, 0, 0, 1);
            } else if (rotation == TextRotation.ROT_180) {
                GlStateManager.translated(x + width, y + fr.getWordWrappedHeight(text, width), 0);
                GlStateManager.rotated(180, 0, 0, 1);
            }

            if (wrap) {
                drawAlignedSplitString(fr, text, 0, 0, width, alignment, colour, dropShadow);
            } else {
                drawAlignedString(fr, text, 0, 0, width, alignment, colour, dropShadow, trim);
            }

            GlStateManager.popMatrix();
        }
    }

    /**
     * Allows you to draw a split string aligned to the left, middle or right of the specified area.
     */
    public void drawAlignedSplitString(BCFontRenderer fontRenderer, String text, float x, float y, int width, GuiAlign alignment, int colour, boolean dropShadow) {
        for (String s : fontRenderer.listFormattedStringToWidth(text, width)) {
            drawAlignedString(fontRenderer, s, x, y, width, alignment, colour, dropShadow, false);
            y += fontRenderer.FONT_HEIGHT;
        }
    }

    /**
     * Allows you to draw a string aligned to the left, middle or right of the specified area.
     */
    public void drawAlignedString(BCFontRenderer fr, String text, float x, float y, int width, GuiAlign alignment, int colour, boolean dropShadow, boolean trim) {
        if (trim && fr.getStringWidth(text) > width) {
            text = fr.trimStringToWidth(text, width - 8) + "..";
        }

        int stringWidth = fr.getStringWidth(text);
        switch (alignment) {
            case LEFT:
                drawString(fontRenderer, text, x, y, colour, dropShadow);
                break;
            case CENTER:
                drawString(fontRenderer, text, x + ((width - stringWidth) / 2), y, colour, dropShadow);
                break;
            case RIGHT:
                drawString(fontRenderer, text, x + (width - stringWidth), y, colour, dropShadow);
                break;
        }
//        drawString(fontRenderer, s, x, y, colour, dropShadow);
    }

    public void drawHoveringText(List<String> textLines, int mouseX, int mouseY, BCFontRenderer font, int screenWidth, int screenHeight) {
//        double oldOffset = zOffset;
//        zOffset = 190;
        drawHoveringText(textLines, mouseX, mouseY, font, screenWidth, screenHeight, -1);
//        zOffset = oldOffset;
    }

    /**
     * This is almost an exact copy of forges code except it respects zLevel.
     */
    public void drawHoveringText(List<String> textLines, int mouseX, int mouseY, FontRenderer font, int screenWidth, int screenHeight, int maxTextWidth) {
        font = mc.fontRenderer;
        if (!textLines.isEmpty()) {
            GlStateManager.disableRescaleNormal();
            RenderHelper.disableStandardItemLighting();
            GlStateManager.disableLighting();
            GlStateManager.disableDepthTest();
            int tooltipTextWidth = 0;

            for (String textLine : textLines) {
                int textLineWidth = font.getStringWidth(textLine);

                if (textLineWidth > tooltipTextWidth) {
                    tooltipTextWidth = textLineWidth;
                }
            }

            boolean needsWrap = false;

            int titleLinesCount = 1;
            int tooltipX = mouseX + 12;
            if (tooltipX + tooltipTextWidth + 4 > screenWidth) {
                tooltipX = mouseX - 16 - tooltipTextWidth;
                if (tooltipX < 4) // if the tooltip doesn't fit on the screen
                {
                    if (mouseX > screenWidth / 2) {
                        tooltipTextWidth = mouseX - 12 - 8;
                    } else {
                        tooltipTextWidth = screenWidth - 16 - mouseX;
                    }
                    needsWrap = true;
                }
            }

            if (maxTextWidth > 0 && tooltipTextWidth > maxTextWidth) {
                tooltipTextWidth = maxTextWidth;
                needsWrap = true;
            }

            if (needsWrap) {
                int wrappedTooltipWidth = 0;
                List<String> wrappedTextLines = new ArrayList<>();
                for (int i = 0; i < textLines.size(); i++) {
                    String textLine = textLines.get(i);
                    List<String> wrappedLine = font.listFormattedStringToWidth(textLine, tooltipTextWidth);
                    if (i == 0) {
                        titleLinesCount = wrappedLine.size();
                    }

                    for (String line : wrappedLine) {
                        int lineWidth = font.getStringWidth(line);
                        if (lineWidth > wrappedTooltipWidth) {
                            wrappedTooltipWidth = lineWidth;
                        }
                        wrappedTextLines.add(line);
                    }
                }
                tooltipTextWidth = wrappedTooltipWidth;
                textLines = wrappedTextLines;

                if (mouseX > screenWidth / 2) {
                    tooltipX = mouseX - 16 - tooltipTextWidth;
                } else {
                    tooltipX = mouseX + 12;
                }
            }

            int tooltipY = mouseY - 12;

            int tooltipHeight = 8;

            if (textLines.size() > 1) {
                tooltipHeight += (textLines.size() - 1) * 10;
                if (textLines.size() > titleLinesCount) {
                    tooltipHeight += 2; // gap between title lines and next lines
                }
            }

            if (tooltipY + tooltipHeight + 6 > screenHeight) {
                tooltipY = screenHeight - tooltipHeight - 6;
            }

            if (tooltipY < 4) {
                tooltipY = 4;
            }

            zOffset += 1;
            final int backgroundColor = 0xF0100010;
            drawGradientRect(tooltipX - 3, tooltipY - 4, tooltipX + tooltipTextWidth + 3, tooltipY - 3, backgroundColor, backgroundColor);
            drawGradientRect(tooltipX - 3, tooltipY + tooltipHeight + 3, tooltipX + tooltipTextWidth + 3, tooltipY + tooltipHeight + 4, backgroundColor, backgroundColor);
            drawGradientRect(tooltipX - 3, tooltipY - 3, tooltipX + tooltipTextWidth + 3, tooltipY + tooltipHeight + 3, backgroundColor, backgroundColor);
            drawGradientRect(tooltipX - 4, tooltipY - 3, tooltipX - 3, tooltipY + tooltipHeight + 3, backgroundColor, backgroundColor);
            drawGradientRect(tooltipX + tooltipTextWidth + 3, tooltipY - 3, tooltipX + tooltipTextWidth + 4, tooltipY + tooltipHeight + 3, backgroundColor, backgroundColor);
            final int borderColorStart = 0x505000FF;
            final int borderColorEnd = (borderColorStart & 0xFEFEFE) >> 1 | borderColorStart & 0xFF000000;
            drawGradientRect(tooltipX - 3, tooltipY - 3 + 1, tooltipX - 3 + 1, tooltipY + tooltipHeight + 3 - 1, borderColorStart, borderColorEnd);
            drawGradientRect(tooltipX + tooltipTextWidth + 2, tooltipY - 3 + 1, tooltipX + tooltipTextWidth + 3, tooltipY + tooltipHeight + 3 - 1, borderColorStart, borderColorEnd);
            drawGradientRect(tooltipX - 3, tooltipY - 3, tooltipX + tooltipTextWidth + 3, tooltipY - 3 + 1, borderColorStart, borderColorStart);
            drawGradientRect(tooltipX - 3, tooltipY + tooltipHeight + 2, tooltipX + tooltipTextWidth + 3, tooltipY + tooltipHeight + 3, borderColorEnd, borderColorEnd);

            for (int lineNumber = 0; lineNumber < textLines.size(); ++lineNumber) {
                String line = textLines.get(lineNumber);
                drawString(font, line, (float) tooltipX, (float) tooltipY, -1, true);

                if (lineNumber + 1 == titleLinesCount) {
                    tooltipY += 2;
                }

                tooltipY += 10;
            }
            zOffset -= 1;

            GlStateManager.enableLighting();
            GlStateManager.enableDepthTest();
            RenderHelper.enableStandardItemLighting();
            GlStateManager.enableRescaleNormal();
        }
    }

    public void drawGradientRect(double left, double top, double right, double bottom, int colour1, int colour2) {
        if (colour1 == colour2 && colour2 == 0) return;
        double zLevel = getRenderZLevel();
        float alpha1 = ((colour1 >> 24 & 255) / 255.0F);
        float red1 = (float) (colour1 >> 16 & 255) / 255.0F;
        float green1 = (float) (colour1 >> 8 & 255) / 255.0F;
        float blue1 = (float) (colour1 & 255) / 255.0F;
        float alpha2 = ((colour2 >> 24 & 255) / 255.0F);
        float red2 = (float) (colour2 >> 16 & 255) / 255.0F;
        float green2 = (float) (colour2 >> 8 & 255) / 255.0F;
        float blue2 = (float) (colour2 & 255) / 255.0F;
        GlStateManager.disableTexture();
        GlStateManager.enableBlend();
        GlStateManager.disableAlphaTest();
        GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        buffer.pos(right, top, zLevel).color(red1, green1, blue1, alpha1).endVertex();
        buffer.pos(left, top, zLevel).color(red1, green1, blue1, alpha1).endVertex();
        buffer.pos(left, bottom, zLevel).color(red2, green2, blue2, alpha2).endVertex();
        buffer.pos(right, bottom, zLevel).color(red2, green2, blue2, alpha2).endVertex();
        tessellator.draw();
        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.disableBlend();
        GlStateManager.enableAlphaTest();
        GlStateManager.enableTexture();
    }

    public void drawMultiPassGradientRect(double left, double top, double right, double bottom, int colour1, int colour2, int layers) {
        if (colour1 == colour2 && colour2 == 0) return;
        double zLevel = getRenderZLevel();
        float alpha1 = ((colour1 >> 24 & 255) / 255.0F);
        float red1 = (float) (colour1 >> 16 & 255) / 255.0F;
        float green1 = (float) (colour1 >> 8 & 255) / 255.0F;
        float blue1 = (float) (colour1 & 255) / 255.0F;
        float alpha2 = ((colour2 >> 24 & 255) / 255.0F);
        float red2 = (float) (colour2 >> 16 & 255) / 255.0F;
        float green2 = (float) (colour2 >> 8 & 255) / 255.0F;
        float blue2 = (float) (colour2 & 255) / 255.0F;
        GlStateManager.disableTexture();
        GlStateManager.enableBlend();
        GlStateManager.disableAlphaTest();
        GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        for (int i = 0; i < layers; i++) {
            buffer.pos(right, top, zLevel).color(red1, green1, blue1, alpha1).endVertex();
            buffer.pos(left, top, zLevel).color(red1, green1, blue1, alpha1).endVertex();
            buffer.pos(left, bottom, zLevel).color(red2, green2, blue2, alpha2).endVertex();
            buffer.pos(right, bottom, zLevel).color(red2, green2, blue2, alpha2).endVertex();
        }
        tessellator.draw();
        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.disableBlend();
        GlStateManager.enableAlphaTest();
        GlStateManager.enableTexture();
    }

    public void drawColouredRect(double posX, double posY, double xSize, double ySize, int colour) {
        drawGradientRect(posX, posY, posX + xSize, posY + ySize, colour, colour);
    }

    public void drawBorderedRect(double posX, double posY, double xSize, double ySize, double borderWidth, int fillColour, int borderColour) {
        drawColouredRect(posX, posY, xSize, borderWidth, borderColour);
        drawColouredRect(posX, posY + ySize - borderWidth, xSize, borderWidth, borderColour);
        drawColouredRect(posX, posY + borderWidth, borderWidth, ySize - (2 * borderWidth), borderColour);
        drawColouredRect(posX + xSize - borderWidth, posY + borderWidth, borderWidth, ySize - (2 * borderWidth), borderColour);
        drawColouredRect(posX + borderWidth, posY + borderWidth, xSize - (2 * borderWidth), ySize - (2 * borderWidth), fillColour);
    }

    public void drawShadedRect(double x, double y, double width, double height, double borderWidth, int fill, int topLeftColour, int bottomRightColour, int cornerMixColour) {
        //Fill
        drawColouredRect(x + borderWidth, y + borderWidth, width - (borderWidth * 2), height - (borderWidth * 2), fill);
        //Top
        drawColouredRect(x, y, width - borderWidth, borderWidth, topLeftColour);
        //Left
        drawColouredRect(x, y + borderWidth, borderWidth, height - (borderWidth * 2), topLeftColour);
        //Bottom
        drawColouredRect(x + borderWidth, y + height - borderWidth, width - borderWidth, borderWidth, bottomRightColour);
        //Right
        drawColouredRect(x + width - borderWidth, y + borderWidth, borderWidth, height - (borderWidth * 2), bottomRightColour);
        //Top Right Corner
        drawColouredRect(x + width - borderWidth, y, borderWidth, borderWidth, cornerMixColour);
        //Bottom Left Corner
        drawColouredRect(x, y + height - borderWidth, borderWidth, borderWidth, cornerMixColour);
    }

    public static int mixColours(int colour1, int colour2) {
        return mixColours(colour1, colour2, false);
    }

    public static int mixColours(int colour1, int colour2, boolean subtract) {
        int alpha1 = colour1 >> 24 & 255;
        int alpha2 = colour2 >> 24 & 255;
        int red1 = colour1 >> 16 & 255;
        int red2 = colour2 >> 16 & 255;
        int green1 = colour1 >> 8 & 255;
        int green2 = colour2 >> 8 & 255;
        int blue1 = colour1 & 255;
        int blue2 = colour2 & 255;

        int alpha = MathHelper.clamp(alpha1 + (subtract ? -alpha2 : alpha2), 0, 255);
        int red = MathHelper.clamp(red1 + (subtract ? -red2 : red2), 0, 255);
        int green = MathHelper.clamp(green1 + (subtract ? -green2 : green2), 0, 255);
        int blue = MathHelper.clamp(blue1 + (subtract ? -blue2 : blue2), 0, 255);

        return (alpha & 0xFF) << 24 | (red & 0xFF) << 16 | (green & 0xFF) << 8 | (blue & 0xFF);
    }

    public static int midColour(int colour1, int colour2) {
        int alpha1 = colour1 >> 24 & 255;
        int alpha2 = colour2 >> 24 & 255;
        int red1 = colour1 >> 16 & 255;
        int red2 = colour2 >> 16 & 255;
        int green1 = colour1 >> 8 & 255;
        int green2 = colour2 >> 8 & 255;
        int blue1 = colour1 & 255;
        int blue2 = colour2 & 255;
        return (alpha2 + ((alpha1 - alpha2) / 2) & 0xFF) << 24 | (red2 + ((red1 - red2) / 2) & 0xFF) << 16 | (green2 + ((green1 - green2) / 2) & 0xFF) << 8 | (blue2 + ((blue1 - blue2) / 2) & 0xFF);
    }

    public void renderVanillaButtonTexture(int xPos, int yPos, int xSize, int ySize, boolean hovered, boolean disabled) {
        ResourceHelperBC.bindTexture(WIDGETS_TEXTURES);
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);

        int k = 1;
        if (disabled) {
            k = 0;
        } else if (hovered) {
            k = 2;
        }

        GlStateManager.enableBlend();
        GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

        int texHeight = Math.min(20, ySize);
        int texPos = 46 + k * 20;

        drawTexturedModalRect(xPos, yPos, 0, texPos, xSize % 2 + xSize / 2, texHeight);
        drawTexturedModalRect(xSize % 2 + xPos + xSize / 2, yPos, 200 - xSize / 2, texPos, xSize / 2, texHeight);

        if (ySize < 20) {
            drawTexturedModalRect(xPos, yPos + 3, 0, texPos + 20 - ySize + 3, xSize % 2 + xSize / 2, ySize - 3);
            drawTexturedModalRect(xSize % 2 + xPos + xSize / 2, yPos + 3, 200 - xSize / 2, texPos + 20 - ySize + 3, xSize / 2, ySize - 3);
        } else if (ySize > 20) {
            for (int y = yPos + 17; y + 15 < yPos + ySize; y += 15) {
                drawTexturedModalRect(xPos, y, 0, texPos + 2, xSize % 2 + xSize / 2, 15);
                drawTexturedModalRect(xSize % 2 + xPos + xSize / 2, y, 200 - xSize / 2, texPos + 2, xSize / 2, 15);
            }

            drawTexturedModalRect(xPos, yPos + ySize - 15, 0, texPos + 5, xSize % 2 + xSize / 2, 15);
            drawTexturedModalRect(xSize % 2 + xPos + xSize / 2, yPos + ySize - 15, 200 - xSize / 2, texPos + 5, xSize / 2, 15);
        }
    }

    public void drawTiledTextureRectWithTrim(int xPos, int yPos, int xSize, int ySize, int topTrim, int leftTrim, int bottomTrim, int rightTrim, int texU, int texV, int texWidth, int texHeight) {
        int trimWidth = texWidth - leftTrim - rightTrim;
        int trimHeight = texHeight - topTrim - bottomTrim;
        if (xSize <= texWidth) trimWidth = Math.min(trimWidth, xSize - rightTrim);
        if (xSize <= 0 || ySize <= 0 || trimWidth <= 0 || trimHeight <= 0) return;

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(0x07, DefaultVertexFormats.POSITION_TEX);

        for (int x = 0; x < xSize; ) {
            int rWidth = Math.min(xSize - x, trimWidth);
            int trimU;
            if (x == 0) {
                trimU = texU;
            } else if (x + trimWidth <= xSize) {
                trimU = texU + leftTrim;
            } else {
                trimU = texU + (texWidth - (xSize - x));
            }

            //Top & Bottom trim
            bufferTexturedModalRect(buffer, xPos + x, yPos, trimU, texV, rWidth, topTrim);
            bufferTexturedModalRect(buffer, xPos + x, yPos + ySize - bottomTrim, trimU, texV + texHeight - bottomTrim, rWidth, bottomTrim);


            rWidth = Math.min(xSize - x - leftTrim - rightTrim, trimWidth);
            for (int y = 0; y < ySize; ) {
                int rHeight = Math.min(ySize - y - topTrim - bottomTrim, trimHeight);
                int trimV = y + texHeight <= ySize ? texV + topTrim : texV + (texHeight - (ySize - y));

                //Left & Right trim
                if (x == 0) {
                    bufferTexturedModalRect(buffer, xPos, yPos + y + topTrim, texU, trimV, leftTrim, rHeight);
                    bufferTexturedModalRect(buffer, xPos + xSize - rightTrim, yPos + y + topTrim, trimU + texWidth - rightTrim, trimV, rightTrim, rHeight);
                }

                //Core
                bufferTexturedModalRect(buffer, xPos + x + leftTrim, yPos + y + topTrim, texU + leftTrim, texV + topTrim, rWidth, rHeight);
                y += trimHeight;
            }
            x += trimWidth;
        }

        tessellator.draw();
    }

    private void bufferTexturedModalRect(BufferBuilder buffer, int x, int y, int textureX, int textureY, int width, int height) {
        double zLevel = getRenderZLevel();
        buffer.pos((double) (x), (double) (y + height), zLevel).tex((double) ((float) (textureX) * 0.00390625F), (double) ((float) (textureY + height) * 0.00390625F)).endVertex();
        buffer.pos((double) (x + width), (double) (y + height), zLevel).tex((double) ((float) (textureX + width) * 0.00390625F), (double) ((float) (textureY + height) * 0.00390625F)).endVertex();
        buffer.pos((double) (x + width), (double) (y), zLevel).tex((double) ((float) (textureX + width) * 0.00390625F), (double) ((float) (textureY) * 0.00390625F)).endVertex();
        buffer.pos((double) (x), (double) (y), zLevel).tex((double) ((float) (textureX) * 0.00390625F), (double) ((float) (textureY) * 0.00390625F)).endVertex();
    }

    private Vector3 colourRatio = new Vector3();

    /**
     * Lightens or darkens a colour by the given amount.
     */
    public int changeShade(int colour, double shade) {
        double r = ((colour >> 16) & 0xFF) / 255D;
        double g = ((colour >> 8) & 0xFF) / 255D;
        double b = (colour & 0xFF) / 255D;
        double a = ((colour >> 24) & 0xFF) / 255D;

        colourRatio.set(r, g, b);
        if (colourRatio.magSquared() == 0) colourRatio.set(1);
        colourRatio.normalize();

        r = codechicken.lib.math.MathHelper.clip(r + (colourRatio.x * shade), 0, 1);
        g = codechicken.lib.math.MathHelper.clip(g + (colourRatio.y * shade), 0, 1);
        b = codechicken.lib.math.MathHelper.clip(b + (colourRatio.z * shade), 0, 1);

        return ((int) (a * 0xFF) & 0xFF) << 24 | ((int) (r * 0xFF) & 0xFF) << 16 | ((int) (g * 0xFF) & 0xFF) << 8 | ((int) (b * 0xFF) & 0xFF);
    }

    //endregion

    //# Hover Text
    //region //############################################################################

    /**
     * Enables or disables hover text for this element.
     * Note: Calling any of the setHoverText methods will automatically enable hover text.
     */
    @SuppressWarnings("unchecked")
    public E setHoverTextEnabled(boolean enableHoverText) {
        this.drawHoverText = enableHoverText;
        return (E) this;
    }

    public boolean isHoverTextEnabled() {
        return drawHoverText;
    }

    /**
     * This allows you to set a delay for hover text. THis is how long the cursor needs to be over the element
     * before the hover text will be displayed. The default delay is 0 (no delay).
     *
     * @param hoverTextDelay Hover text delay in ticks.
     */
    @SuppressWarnings("unchecked")
    public E setHoverTextDelay(int hoverTextDelay) {
        this.hoverTextDelay = hoverTextDelay;
        return (E) this;
    }

    /**
     * Allows toy to add a hover text supplier for this gui element.
     * Hover text is disabled by default however calling any of the setHoverText methods
     * will enable hover text.
     *
     * @param hoverText
     * @return
     */
    @SuppressWarnings("unchecked")
    public E setHoverTextList(HoverTextSupplier<List<String>, E> hoverText) {
        this.hoverText = hoverText;
        setHoverTextEnabled(true);
        return (E) this;
    }

    @SuppressWarnings("unchecked")
    public E setHoverTextArray(HoverTextSupplier<String[], E> hoverText) {
        this.hoverText = hoverText;
        setHoverTextEnabled(true);
        return (E) this;
    }

    @SuppressWarnings("unchecked")
    public E setHoverText(HoverTextSupplier<String, E> hoverText) {
        this.hoverText = hoverText;
        setHoverTextEnabled(true);
        return (E) this;
    }

    @SuppressWarnings("unchecked")
    public E setHoverText(String singleLine) {
        setHoverText(element -> singleLine);
        return (E) this;
    }

    @SuppressWarnings("unchecked")
    public E setHoverText(String... textLines) {
        setHoverTextArray(element -> textLines);
        return (E) this;
    }

    @SuppressWarnings("unchecked")
    public E setHoverText(List<String> textLines) {
        setHoverTextList(element -> textLines);
        return (E) this;
    }

    //TODO I can probably expend on this. Maybe re write things to use text components or some custom system that supports text components.
    public E setComponentHoverText(List<ITextComponent> textLines) {
        setHoverTextList(element -> textLines.stream().map(ITextComponent::getFormattedText).collect(Collectors.toList()));
        return (E) this;
    }

    @SuppressWarnings("unchecked")
    public List<String> getHoverText() {
        return hoverText == null || !drawHoverText ? Collections.emptyList() : hoverText.getHoverText((E) this);
    }

    public int getHoverTime() {
        return hoverTime;
    }

    //endregion

    public static interface IDrawCallback {
        void call(Minecraft minecraft, int mouseX, int mouseY, float partialTicks, boolean mouseOver);

        static void resetColour(Minecraft minecraft, int mouseX, int mouseY, float partialTicks, boolean mouseOver) {
            GlStateManager.color4f(1F, 1F, 1F, 1F);
        }
    }

    public GuiElement<E> $plus$eq(GuiElement<?> other) {
        addChild(other);
        return this;
    }
}
