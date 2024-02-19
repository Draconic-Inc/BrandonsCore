package com.brandon3055.brandonscore.client.gui.modulargui;

import codechicken.lib.gui.modular.ModularGuiContainer;
import codechicken.lib.gui.modular.elements.*;
import codechicken.lib.gui.modular.lib.BackgroundRender;
import codechicken.lib.gui.modular.lib.Constraints;
import codechicken.lib.gui.modular.lib.GuiRender;
import codechicken.lib.gui.modular.lib.TextState;
import codechicken.lib.gui.modular.lib.geometry.*;
import codechicken.lib.math.MathHelper;
import com.brandon3055.brandonscore.client.BCGuiTextures;
import com.brandon3055.brandonscore.client.render.RenderUtils;
import com.brandon3055.brandonscore.lib.entityfilter.*;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.covers1624.quack.collection.FastStream;
import net.covers1624.quack.util.SneakyUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.extensions.IForgeLivingEntity;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static codechicken.lib.gui.modular.lib.geometry.Constraint.*;
import static codechicken.lib.gui.modular.lib.geometry.GeoParam.*;

/**
 * Created by brandon3055 on 18/11/19.
 */
public class GuiEntityFilter extends GuiElement<GuiEntityFilter> {
    private final EntityFilter entityFilter;
    private GuiScrolling scrollElement;
    private GuiSlider scrollBar;
    private GroupNode rootNode;
    private Function<BaseNode<?>, GuiElement<?>> nodeBgBuilder = GuiRectangle::invertedSlot;
    private Function<GuiButton, GuiText> textBtnBuilder = e -> new GuiText(e).setAlignment(Align.LEFT).setTextColour(() -> e.isMouseOver() ? 0xffffa0 : 0xe0e0e0);
    private Supplier<Integer> titleTextColour = () -> 0xFFFFFF;
    private Consumer<GuiSlider> scrollBarCustomizer = bar -> Constraints.bind(new GuiRectangle(bar).fill(0x80FFFFFF), bar.getSlider());

    /**
     * @param entityFilter The client side entity filter instance.
     */
    public GuiEntityFilter(@NotNull GuiParent<?> parent, EntityFilter entityFilter) {
        super(parent);
        this.entityFilter = entityFilter;

        this.scrollElement = new GuiScrolling(this);
        Constraints.bind(scrollElement, this);
        scrollElement.installContainerElement(new GuiElement<>(scrollElement));
        scrollElement.getContentElement()
                .constrain(WIDTH, null)
                .constrain(LEFT, match(scrollElement.get(LEFT)))
                .constrain(RIGHT, match(scrollElement.get(RIGHT)));

        initFilter();
    }

    public GuiEntityFilter initFilter() {
        GuiElement<?> content = scrollElement.getContentElement();
        if (rootNode != null) content.removeChild(rootNode);
        rootNode = new GroupNode(content, entityFilter, this)
                .constrain(TOP, match(content.get(TOP)))
                .constrain(LEFT, match(content.get(LEFT)))
                .constrain(RIGHT, match(content.get(RIGHT)))
                .reloadGroup();

        if (scrollBar != null) removeChild(scrollBar);
        scrollBar = new GuiSlider(this, Axis.Y)
                .setEnabled(() -> scrollElement.hiddenSize(Axis.Y) > 0)
                .setSliderState(scrollElement.scrollState(Axis.Y))
                .setScrollableElement(this)
                .constrain(TOP, match(get(TOP)))
                .constrain(RIGHT, match(get(RIGHT)))
                .constrain(BOTTOM, match(get(BOTTOM)))
                .constrain(WIDTH, literal(5));

        scrollElement.constrain(RIGHT, relative(get(RIGHT), () -> scrollElement.hiddenSize(Axis.Y) > 0 ? -scrollBar.xSize() - 1 : 0D));
        scrollBarCustomizer.accept(scrollBar);
        return this;
    }

    public GuiEntityFilter setNodeBgBuilder(Function<BaseNode<?>, GuiElement<?>> nodeBgBuilder) {
        this.nodeBgBuilder = nodeBgBuilder;
        return this;
    }

    public GuiEntityFilter setScrollBarCustomizer(Consumer<GuiSlider> scrollBarCustomizer) {
        this.scrollBarCustomizer = scrollBarCustomizer;
        return this;
    }

    public GuiEntityFilter setTitleTextColour(Supplier<Integer> titleTextColour) {
        this.titleTextColour = titleTextColour;
        return this;
    }

    public GuiEntityFilter setTextBtnBuilder(Function<GuiButton, GuiText> textBtnBuilder) {
        this.textBtnBuilder = textBtnBuilder;
        return this;
    }

    private <T extends FilterBase> void layoutFilter(FilterNode<T> node, FilterType type) {
        switch (type) {
            case HOSTILE -> layoutHostile(SneakyUtils.unsafeCast(node));
            case TAMED -> layoutTamed(SneakyUtils.unsafeCast(node));
            case ADULTS -> layoutAdults(SneakyUtils.unsafeCast(node));
            case PLAYER -> layoutPlayer(SneakyUtils.unsafeCast(node));
            case ENTITY_TYPE -> layoutEntityType(SneakyUtils.unsafeCast(node));
            case ITEM_FILTER -> layoutItemFilter(SneakyUtils.unsafeCast(node));
        }
    }

    private GuiButton textButton(GuiElement<?> parent, Supplier<Component> label) {
        GuiButton button = new GuiButton(parent);
        button.setLabel(textBtnBuilder.apply(button).setTextSupplier(label));
        Constraints.bind(button.getLabel(), button, 0, 2, 0, 2);
        button.constrain(WIDTH, dynamic(() -> font().width(label.get()) + 4D));
        return button;
    }

    private void layoutHostile(FilterNode<FilterHostile> node) {
        node.constrain(HEIGHT, literal(12));

        GuiButton toggleInclude = textButton(node, () -> Component.translatable("mod_gui.brandonscore.entity_filter.hostile." + node.getNode().isWhitelistHostile()));
        Constraints.bind(toggleInclude, node, 0, 0, 1, 12);
        toggleInclude.onPress(() -> node.getNode().setWhitelistHostile(!node.getNode().isWhitelistHostile()));
    }

    private void layoutTamed(FilterNode<FilterTamed> node) {
        node.constrain(HEIGHT, literal(22));

        GuiButton toggleInclude = textButton(node, () -> Component.translatable("mod_gui.brandonscore.entity_filter.tamed." + node.getNode().isWhitelistTamed()))
                .constrain(HEIGHT, literal(11))
                .onPress(() -> node.getNode().setWhitelistTamed(!node.getNode().isWhitelistTamed()));
        Constraints.placeInside(toggleInclude, node, Constraints.LayoutPos.TOP_LEFT);

        GuiButton toggleTamable = textButton(node, () -> Component.literal("[ ").append(Component.translatable("mod_gui.brandonscore.entity_filter.tamable." + node.getNode().isIncludeTamable())).append(" ]"))
                .constrain(HEIGHT, literal(9))
                .setTooltip(Component.translatable("mod_gui.brandonscore.entity_filter.tamable.info"))
                .onPress(() -> node.getNode().setIncludeTamable(!node.getNode().isIncludeTamable()));
        Constraints.placeInside(toggleTamable, node, Constraints.LayoutPos.TOP_LEFT, 6, 11);
    }

    private void layoutAdults(FilterNode<FilterAdults> node) {
        node.constrain(HEIGHT, literal(22));

        GuiButton toggleInclude = textButton(node, () -> Component.translatable("mod_gui.brandonscore.entity_filter.adults." + node.getNode().isWhitelistAdults()))
                .constrain(HEIGHT, literal(11))
                .onPress(() -> node.getNode().setWhitelistAdults(!node.getNode().isWhitelistAdults()));
        Constraints.placeInside(toggleInclude, node, Constraints.LayoutPos.TOP_LEFT);

        GuiButton toggleAgeable = textButton(node, () -> Component.literal("[ ").append(Component.translatable("mod_gui.brandonscore.entity_filter.non_ageable." + node.getNode().isIncludeNonAgeable())).append(" ]"))
                .constrain(HEIGHT, literal(9))
                .setTooltip(Component.translatable("mod_gui.brandonscore.entity_filter.non_ageable.info"))
                .onPress(() -> node.getNode().setIncludeNonAgeable(!node.getNode().isIncludeNonAgeable()));
        Constraints.placeInside(toggleAgeable, node, Constraints.LayoutPos.TOP_LEFT, 6, 11);
    }

    private void layoutPlayer(FilterNode<FilterPlayer> node) {
        node.constrain(HEIGHT, literal(22));

        GuiButton toggleInclude = textButton(node, () -> Component.translatable("mod_gui.brandonscore.entity_filter.player." + node.getNode().isWhitelistPlayers()))
                .constrain(HEIGHT, literal(11))
                .setTooltip(Component.translatable("mod_gui.brandonscore.entity_filter.player.info"))
                .onPress(() -> node.getNode().setWhitelistPlayers(!node.getNode().isWhitelistPlayers()));
        Constraints.placeInside(toggleInclude, node, Constraints.LayoutPos.TOP_LEFT);

        Component nameComponent = Component.translatable("mod_gui.brandonscore.entity_filter.player.name");
        GuiText nameLabel = new GuiText(node, nameComponent)
                .constrain(WIDTH, dynamic(() -> (double) font().width(nameComponent)))
                .constrain(HEIGHT, literal(9))
                .setShadow(false)
                .setTextColour(titleTextColour);
        Constraints.placeInside(nameLabel, node, Constraints.LayoutPos.BOTTOM_LEFT, 2, -2);

        GuiTextField nameField = new GuiTextField(node)
                .setTextState(TextState.create(() -> node.getNode().getPlayerName(), s -> node.getNode().setPlayerName(s)))
                .setFilter(s -> FilterPlayer.namePattern.matcher(s).find())
                .setMaxLength(16)
                .setSuggestion(Component.translatable("mod_gui.brandonscore.entity_filter.player.suggestion"))
                .setSuggestionColour(0x202020)
                .setSuggestionShadow(false)
                .constrain(TOP, relative(nameLabel.get(TOP), 0))
                .constrain(LEFT, relative(nameLabel.get(RIGHT), 3))
                .constrain(HEIGHT, literal(9))
                .constrain(RIGHT, relative(node.get(RIGHT), -2));
        Constraints.bind(new GuiRectangle(nameField).fill(0x30FFFFFF), nameField, 0, -1, 0, 0);
    }

    private void layoutEntityType(FilterNode<FilterEntity> node) {
        node.constrain(HEIGHT, literal(22));

        GuiButton toggleInclude = textButton(node, () -> Component.translatable("mod_gui.brandonscore.entity_filter.entity_type." + node.getNode().isWhitelistEntity()))
                .constrain(HEIGHT, literal(11))
                .onPress(() -> node.getNode().setWhitelistEntity(!node.getNode().isWhitelistEntity()));
        Constraints.placeInside(toggleInclude, node, Constraints.LayoutPos.TOP_LEFT);

        Component nameComponent = Component.translatable("mod_gui.brandonscore.entity_filter.entity_type.name");
        GuiText nameLabel = new GuiText(node, nameComponent)
                .constrain(WIDTH, dynamic(() -> (double) font().width(nameComponent)))
                .constrain(HEIGHT, literal(9))
                .setShadow(false)
                .setTextColour(titleTextColour);
        Constraints.placeInside(nameLabel, node, Constraints.LayoutPos.BOTTOM_LEFT, 2, -2);

        GuiTextField nameField = new GuiTextField(node)
                .setTextState(TextState.create(() -> node.getNode().getEntityName(), s -> node.getNode().setEntityName(s)))
                .setFilter(ResourceLocation::isValidResourceLocation)
                .setTextColor(() -> ForgeRegistries.ENTITY_TYPES.containsKey(ResourceLocation.tryParse(node.getNode().getEntityName())) ? 0x00FF00 : 0xFF0000)
                .setTooltip(() -> {
                    ResourceLocation name = ResourceLocation.tryParse(node.getNode().getEntityName());
                    if (name == null || !ForgeRegistries.ENTITY_TYPES.containsKey(name)) {
                        return Collections.singletonList(Component.translatable("mod_gui.brandonscore.entity_filter.entity_type.unknown").withStyle(ChatFormatting.RED));
                    }
                    EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(name);
                    return List.of(type.getDescription().copy(), Component.literal(name.getNamespace()).withStyle(ChatFormatting.BLUE, ChatFormatting.ITALIC));
                })
                .setMaxLength(1024)
                .setSuggestion(Component.translatable("mod_gui.brandonscore.entity_filter.entity_type.suggestion"))
                .setSuggestionColour(0x202020)
                .setSuggestionShadow(false)
                .constrain(TOP, relative(nameLabel.get(TOP), 0))
                .constrain(LEFT, relative(nameLabel.get(RIGHT), 3))
                .constrain(HEIGHT, literal(9))
                .constrain(RIGHT, relative(node.get(RIGHT), -12));
        Constraints.bind(new GuiRectangle(nameField).fill(0x30FFFFFF), nameField, 0, -1, 0, 0);

        GuiButton select = GuiButton.flatColourButton(node, () -> Component.literal("..."), h -> 0xFF000000, h -> h ? 0xFF808080 : 0xFF303030);
        select.getLabel().setScroll(false);
        Constraints.bind(select.getLabel(), select, 0, 1, 6, 0);
        Constraints.size(select, 8, 8);
        Constraints.placeInside(select, node, Constraints.LayoutPos.BOTTOM_RIGHT, -2, -2);
        select.setTooltip(Component.translatable("mod_gui.brandonscore.entity_filter.entity_type.find"));
        select.onPress(() -> {
            GuiListDialog<EntityType<?>> dialog = GuiListDialog.create(node);
            Constraints.size(dialog, 160, 200);
            dialog.placeCenter();
            dialog.getList()
                    .setItemSpacing(1)
                    .setDisplayBuilder((entityTypeGuiList, entityType) -> {
                        GuiRectangle bg = new GuiRectangle(entityTypeGuiList)
                                .constrain(HEIGHT, literal(24));
                        bg.fill(() -> bg.isMouseOver() ? 0x80303030 : 0x80000000);

                        GuiButton button = new GuiButton(bg)
                                .onPress(() -> {
                                    node.getNode().setEntityName(String.valueOf(ForgeRegistries.ENTITY_TYPES.getKey(entityType)));
                                    dialog.close();
                                });
                        Constraints.bind(button, bg);

                        GuiEntityRenderer render = new GuiEntityRenderer(bg)
                                .setEntity(ForgeRegistries.ENTITY_TYPES.getKey(entityType))
                                .constrain(WIDTH, literal(20))
                                .constrain(HEIGHT, literal(20));
                        Constraints.placeInside(render, bg, Constraints.LayoutPos.MIDDLE_LEFT, 6, 0);

                        new GuiText(bg, entityType.getDescription())
                                .constrain(TOP, relative(bg.get(TOP), 2))
                                .constrain(BOTTOM, relative(bg.get(BOTTOM), -2))
                                .constrain(LEFT, relative(render.get(RIGHT), 4))
                                .constrain(RIGHT, relative(bg.get(RIGHT), -2))
                                .setWrap(true);
                        return bg;
                    });

            dialog.setSearchStringFunc(entityType -> entityType.getDescription().getString());
            if (entityFilter.isLivingOnly()) {
                dialog.addItems(ForgeRegistries.ENTITY_TYPES.getValues()
                        .stream()
                        .filter(e -> {
                            try {
                                return e.create(mc().level) instanceof IForgeLivingEntity;
                            } catch (Throwable ex) {
                                return false;
                            }
                        })
                        .toList());
            } else {
                dialog.addItems(ForgeRegistries.ENTITY_TYPES.getValues());
            }
        });
    }

    private void layoutItemFilter(FilterNode<FilterItem> node) {
        node.constrain(HEIGHT, literal(42D));

        GuiButton toggleInclude = textButton(node, () -> Component.translatable("mod_gui.brandonscore.entity_filter.item." + node.getNode().isWhitelistMode()))
                .constrain(HEIGHT, literal(11))
                .onPress(() -> node.getNode().setWhitelistMode(!node.getNode().isWhitelistMode()));
        Constraints.placeInside(toggleInclude, node, Constraints.LayoutPos.TOP_LEFT);

        GuiButton toggleTagMode = textButton(node, () -> Component.translatable("mod_gui.brandonscore.entity_filter.item.tag." + node.getNode().isTagMode()))
                .constrain(HEIGHT, literal(9))
                .onPress(() -> node.getNode().setTagMode(!node.getNode().isTagMode()));
        Constraints.placeInside(toggleTagMode, node, Constraints.LayoutPos.TOP_LEFT, 0, 11);

        //Stack Mode
        GuiRectangle slot = GuiRectangle.vanillaSlot(node)
                .setTooltip(Component.translatable("mod_gui.brandonscore.entity_filter.set_stack"))
                .setEnabled(() -> !node.getNode().isTagMode());
        Constraints.size(slot, 18, 18);
        Constraints.placeInside(slot, node, Constraints.LayoutPos.BOTTOM_LEFT, 2, -2);
        GuiItemStack stack = new GuiItemStack(slot)
                .setStack(() -> node.getNode().getFilterStack());
        Constraints.bind(stack, slot, 1);
        GuiButton setStack = new GuiButton(slot)
                .onPress(() -> node.getNode().setFilterStack(ItemStack.EMPTY), GuiButton.RIGHT_CLICK)
                .onPress(() -> {
                    if (getModularGui().getScreen() instanceof ModularGuiContainer<?> screen) {
                        node.getNode().setFilterStack(screen.getMenu().getCarried());
                    }
                });
        Constraints.bind(setStack, slot);
        slot.setJeiDropTarget(node.getNode()::setFilterStack, true);

        GuiButton matchMode = textButton(node, () -> Component.translatable("mod_gui.brandonscore.entity_filter.item.fussy." + node.getNode().isFussyMatch()))
                .setEnabled(() -> slot.isEnabled() && !node.getNode().getFilterStack().isEmpty())
                .constrain(HEIGHT, literal(9))
                .setTooltip(Component.translatable("mod_gui.brandonscore.entity_filter.item.fussy.info"))
                .onPress(() -> node.getNode().setFussyMatch(!node.getNode().isFussyMatch()));
        Constraints.placeOutside(matchMode, slot, Constraints.LayoutPos.TOP_RIGHT, 1, 9);

        GuiButton matchCount = textButton(node, () -> Component.translatable("mod_gui.brandonscore.entity_filter.item.match_count." + node.getNode().isMatchCount()))
                .setEnabled(() -> slot.isEnabled() && !node.getNode().getFilterStack().isEmpty())
                .constrain(HEIGHT, literal(9))
                .onPress(() -> node.getNode().setMatchCount(!node.getNode().isMatchCount()));
        Constraints.placeOutside(matchCount, slot, Constraints.LayoutPos.BOTTOM_RIGHT, 1, -9);

        //No Stack Set (Block / Items)
        GuiButton blockMode = textButton(node, () -> Component.translatable("mod_gui.brandonscore.entity_filter.item." + (node.getNode().isFilterBlocks() ? "blocks_only" : node.getNode().isFilterItems() ? "items_only" : "items_or_blocks")))
                .setEnabled(() -> slot.isEnabled() && node.getNode().getFilterStack().isEmpty())
                .constrain(HEIGHT, literal(11))
                .setTooltip(Component.translatable("mod_gui.brandonscore.entity_filter.item.items_blocks.info"))
                .onPress(() -> node.getNode().cycleItemsBlocks());
        Constraints.placeOutside(blockMode, slot, Constraints.LayoutPos.MIDDLE_RIGHT, 1, 0);

        //Tag Field
        GuiTextField tagField = new GuiTextField(node)
                .setEnabled(() -> node.getNode().isTagMode())
                .setTextState(TextState.create(() -> node.getNode().getTagString(), s -> node.getNode().setTagString(s)))
                .setFilter(ResourceLocation::isValidResourceLocation)
                .setTextColor(() -> ForgeRegistries.ITEMS.tags().getTagNames().anyMatch(e -> e.equals(node.getNode().getTag())) ? 0x00FF00 : 0xFF0000)
                .setTooltipSingle(() -> {
                    boolean match = ForgeRegistries.ITEMS.tags().getTagNames().anyMatch(e -> e.equals(node.getNode().getTag()));
                    if (match) {
                        int count = FastStream.of(ForgeRegistries.ITEMS.getValues()).filter(e -> e.builtInRegistryHolder().is(node.getNode().getTag())).count();
                        return Component.translatable("mod_gui.brandonscore.entity_filter.item.tag_matches", count).withStyle(ChatFormatting.GREEN);
                    } else {
                        return Component.translatable("mod_gui.brandonscore.entity_filter.item.invalid_tag").withStyle(ChatFormatting.RED);
                    }
                })
                .setMaxLength(1024)
                .setSuggestion(Component.translatable("mod_gui.brandonscore.entity_filter.item.tag.suggestion"))
                .setSuggestionColour(0x202020)
                .setSuggestionShadow(false)
                .constrain(WIDTH, relative(node.get(WIDTH), -21))
                .constrain(HEIGHT, literal(12));
        Constraints.placeInside(tagField, node, Constraints.LayoutPos.BOTTOM_LEFT, 4, -2);
        Constraints.bind(new GuiRectangle(tagField).fill(0x30FFFFFF), tagField, 0, -2, 0, -2);

        GuiButton select = GuiButton.flatColourButton(node, () -> Component.literal("..."), h -> 0xFF000000, h -> h ? 0xFF808080 : 0xFF303030);
        select.getLabel().setScroll(false);
        Constraints.bind(select.getLabel(), select, 0, 1, 6, 0);
        Constraints.size(select, 12, 12);
        Constraints.placeInside(select, node, Constraints.LayoutPos.BOTTOM_RIGHT, -2, -2);
        select.setTooltip(Component.translatable("mod_gui.brandonscore.entity_filter.item.find_tag"));

        select.onPress(() -> {
            GuiListDialog<TagKey<Item>> dialog = GuiListDialog.create(node);
            Constraints.size(dialog, 160, 200);
            dialog.placeCenter();
            dialog.getList()
                    .setItemSpacing(1)
                    .setDisplayBuilder((entityTypeGuiList, tag) -> {
                        GuiRectangle bg = new GuiRectangle(entityTypeGuiList);
                        bg.fill(() -> bg.isMouseOver() ? 0x80303030 : 0x80000000);

                        GuiText text = new GuiText(bg, Component.literal(String.valueOf(tag.location())))
                                .setWrap(true)
                                .autoHeight()
                                .constrain(TOP, relative(bg.get(TOP), 1))
                                .constrain(LEFT, relative(bg.get(LEFT), 2))
                                .constrain(RIGHT, relative(bg.get(RIGHT), -2));
                        bg.constrain(HEIGHT, relative(text.get(HEIGHT), 2));

                        GuiButton button = new GuiButton(bg)
                                .onPress(() -> {
                                    node.getNode().setTagString(String.valueOf(tag.location()));
                                    dialog.close();
                                });
                        Constraints.bind(button, bg);

                        return bg;
                    });

            dialog.setSearchStringFunc(tag -> String.valueOf(tag.location()));
            dialog.addItems(ForgeRegistries.ITEMS.tags().getTagNames().toList());
            dialog.getList().markDirty();
        });
    }

    public static abstract class BaseNode<T extends GuiElement<T>> extends GuiElement<T> {
        protected final GuiElement<?> background;
        protected int trashState = 0;
        protected GroupNode parentGroup;
        protected double trashAnim = 0;
        protected long trashTime = 0;
        protected int nodeID;
        protected FilterBase filterCache;
        protected final GuiEntityFilter filterGui;

        public BaseNode(@NotNull GuiParent<?> parent, FilterBase filter, GuiEntityFilter filterGui) {
            super(parent);
            this.nodeID = filter.getNodeId();
            this.filterCache = filter;
            this.filterGui = filterGui;
            boolean root = filter.getNodeId() == 0;
            Constraints.bind(background = filterGui.nodeBgBuilder.apply(this), this);
            GuiButton delete = new GuiButton(this);
            delete.setTooltip(Component.translatable("mod_gui.brandonscore.entity_filter.delete." + (nodeID == 0 ? "all" : "node")));
            Constraints.size(delete, 10, 10);
            Constraints.placeInside(delete, this, Constraints.LayoutPos.TOP_RIGHT, -1, 1);
            Constraints.bind(new GuiTexture(delete, BCGuiTextures.get(root ? "delete_all" : "delete")), delete);
            Constraints.bind(new PiCover(delete, () -> trashAnim), delete);
            delete.onPress(() -> {
                trashState++;
                trashTime = System.currentTimeMillis() - 4800;
                if (trashState == 3 && root) {
                    filterGui.entityFilter.clientClearFilter();
                } else if (trashState == 2 && !root) {
                    filterGui.entityFilter.clientRemoveNode(filter.getNodeId());
                } else {
                    trashTime = System.currentTimeMillis();
                }
            });
        }

        @Override
        public void tick(double mouseX, double mouseY) {
            super.tick(mouseX, mouseY);
            double newAnim = trashState / (nodeID == 0 ? 3D : 2D);
            if (trashAnim > 0.9 && newAnim == 0) trashAnim = 0;
            trashAnim = MathHelper.approachExp(trashAnim, newAnim, 0.5);
            if (trashState > 0 && System.currentTimeMillis() - trashTime > 5000) {
                trashState = 0;
            }

            if (parentGroup != null && (filterGui.entityFilter.getNode(nodeID) == null || !parentGroup.getNode().getSubNodeMap().containsKey(nodeID))) {
                parentGroup.reloadGroup();
            }
        }

        public @NotNull FilterBase getNode() {
            FilterBase base = filterGui.entityFilter.getNode(nodeID);
            if (base == null) return filterCache;
            return (filterCache = base);
        }
    }

    public static class GroupNode extends BaseNode<GroupNode> {
        private final Int2ObjectMap<BaseNode<?>> nodes = new Int2ObjectOpenHashMap<>();
        private GuiText title;
        private int depth = 1;

        public GroupNode(@NotNull GuiParent<?> parent, FilterGroup group, GuiEntityFilter filterGui) {
            super(parent, group, filterGui);
            title = new GuiText(this, Component.translatable(group.getTranslationKey()))
                    .setTextColour(filterGui.titleTextColour)
                    .setShadow(false)
                    .setAlignment(Align.LEFT)
                    .constrain(GeoParam.TOP, relative(get(GeoParam.TOP), 1))
                    .constrain(LEFT, relative(get(LEFT), 2))
                    .constrain(GeoParam.WIDTH, relative(get(GeoParam.WIDTH), -4))
                    .autoHeight();

            GuiButton add = new GuiButton(this);
            Constraints.size(add, 10, 10);
            Constraints.placeInside(add, this, Constraints.LayoutPos.TOP_RIGHT, -12, 1);
            Constraints.bind(new GuiTexture(add, BCGuiTextures.get("add")), add);
            add.onPress(() -> {
                GuiContextMenu menu = GuiContextMenu.tooltipStyleMenu(parent);
                for (FilterType type : FilterType.values()) {
                    if (!filterGui.entityFilter.isFilterAllowed(type) || (type == FilterType.FILTER_GROUP && depth >= 4)) continue;
                    Component name = Component.translatable("mod_gui.brandonscore.entity_filter." + type.name().toLowerCase(Locale.ROOT));
                    menu.addOption(() -> name, () -> filterGui.entityFilter.clientAddNode(type, group));
                }
                menu.setNormalizedPos(getModularGui().computeMouseX(), getModularGui().computeMouseY());
            });

            Supplier<Component> label = () -> Component.translatable("mod_gui.brandonscore.entity_filter.and_group.button." + getNode().isAndGroup()).withStyle(ChatFormatting.UNDERLINE);
            GuiButton mode = filterGui.textButton(this, label)
                    .onPress(() -> getNode().setAndGroup(!getNode().isAndGroup()))
                    .setTooltipSingle(() -> Component.translatable("mod_gui.brandonscore.entity_filter.and_group." + getNode().isAndGroup()))
                    .constrain(WIDTH, dynamic(() -> font().width(label.get()) + 4D))
                    .constrain(HEIGHT, literal(9));
            Constraints.placeOutside(mode, add, Constraints.LayoutPos.MIDDLE_LEFT, 0, 0);
        }

        public GroupNode reloadGroup() {
            nodes.forEach((integer, baseNode) -> removeChild(baseNode));
            nodes.clear();
            GuiElement<?> last = title;
            FilterGroup groupNode = getNode();
            if (filterGui.entityFilter.getNode(nodeID) == null) {
                return this;
            }

            for (int nodeID : groupNode.getSubNodeMap().keySet()) {
                FilterBase node = groupNode.getSubNodeMap().get(nodeID);
                BaseNode<?> newNode = node instanceof FilterGroup gn ? new GroupNode(this, gn, filterGui) : new FilterNode<>(this, node, filterGui);
                newNode.constrain(LEFT, relative(get(LEFT), 3));
                newNode.constrain(RIGHT, relative(get(RIGHT), -2));
                newNode.constrain(TOP, relative(last.get(BOTTOM), last instanceof GuiText ? 4 : 1));
                newNode.parentGroup = this;
                last = newNode;
                if (newNode instanceof GroupNode gn) {
                    gn.depth = depth + 1;
                    gn.reloadGroup();
                }
                nodes.put(nodeID, newNode);
            }
            if (nodes.isEmpty()) {
                constrain(HEIGHT, literal(12));
            } else {
                constrain(HEIGHT, dynamic(() -> nodes.values().stream().mapToDouble(ConstrainedGeometry::ySize).sum() + (nodes.size() - 1) + title.ySize() + 4 + 4));
            }
            return this;
        }

        @Override
        public void tick(double mouseX, double mouseY) {
            super.tick(mouseX, mouseY);
            if (filterGui.entityFilter.getNode(nodeID) == null || !getNode().getSubNodeMap().keySet().stream().allMatch(key -> nodes.containsKey(key.intValue()))) {
                filterGui.rootNode.reloadGroup();
            }
        }

        @Override
        public @NotNull FilterGroup getNode() {
            return (FilterGroup) super.getNode();
        }
    }

    public static class FilterNode<T extends FilterBase> extends BaseNode<FilterNode<T>> {
        public FilterNode(@NotNull GuiParent<?> parent, T filter, GuiEntityFilter filterGui) {
            super(parent, filter, filterGui);
            filterGui.layoutFilter(this, filter.getType());
        }

        @Override
        public @NotNull T getNode() {
            return (T) super.getNode();
        }
    }

    private static class PiCover extends GuiElement<PiCover> implements BackgroundRender {
        private final Supplier<Double> state;

        public PiCover(@NotNull GuiParent<?> parent, Supplier<Double> state) {
            super(parent);
            this.state = state;
        }

        @Override
        public void renderBackground(GuiRender render, double mouseX, double mouseY, float partialTicks) {
            double state = this.state.get();
            if (state > 0) {
                RenderUtils.drawPieProgress(render, xMin(), yMin(), Math.min(xSize(), ySize()), state, 0, 0xFFFF0000, 0xFF000000);
            }
        }
    }
}
