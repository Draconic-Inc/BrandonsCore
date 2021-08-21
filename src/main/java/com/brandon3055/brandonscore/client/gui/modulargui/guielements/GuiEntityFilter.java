package com.brandon3055.brandonscore.client.gui.modulargui.guielements;

import com.brandon3055.brandonscore.client.BCSprites;
import com.brandon3055.brandonscore.client.gui.GuiToolkit.Palette.Ctrl;
import com.brandon3055.brandonscore.client.gui.GuiToolkit.Palette.SubItem;
import com.brandon3055.brandonscore.client.gui.modulargui.GuiElement;
import com.brandon3055.brandonscore.client.gui.modulargui.baseelements.GuiButton;
import com.brandon3055.brandonscore.client.gui.modulargui.baseelements.GuiScrollElement;
import com.brandon3055.brandonscore.client.gui.modulargui.baseelements.GuiSlideControl;
import com.brandon3055.brandonscore.client.gui.modulargui.lib.GuiAlign;
import com.brandon3055.brandonscore.client.gui.modulargui.lib.GuiColourProvider;
import com.brandon3055.brandonscore.lib.StackReference;
import com.brandon3055.brandonscore.lib.entityfilter.*;
import com.brandon3055.brandonscore.utils.DataUtils;
import com.brandon3055.brandonscore.utils.Utils;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static net.minecraft.client.util.ITooltipFlag.TooltipFlags.ADVANCED;
import static net.minecraft.client.util.ITooltipFlag.TooltipFlags.NORMAL;

/**
 * Created by brandon3055 on 18/11/19.
 */
public class GuiEntityFilter extends GuiElement<GuiEntityFilter> {

    private final EntityFilter entityFilter;
    private GuiScrollElement scrollElement;
    private GroupElement rootNode;
    private Supplier<GuiElement> nodeBgBuilder = GuiBorderedRect::new;
    private GuiColourProvider<Integer> nodeTitleColour = () -> 0xFFFFFF;
    private Consumer<GuiSlideControl> scrollBarCustomizer;
    private boolean updateRequired = false;

    /**
     * @param entityFilter The client side entity filter instance.
     */
    public GuiEntityFilter(EntityFilter entityFilter) {
        this.entityFilter = entityFilter;
        this.elementTranslationExt = "entity_filter";
    }

    public void setNodeBackgroundBuilder(Supplier<GuiElement> nodeBackgroundBuilder) {
        this.nodeBgBuilder = nodeBackgroundBuilder;
    }

    public void setNodeTitleColour(GuiColourProvider<Integer> nodeTitleColour) {
        this.nodeTitleColour = nodeTitleColour;
    }

    public void setScrollBarCustomizer(Consumer<GuiSlideControl> scrollBarCustomizer) {
        this.scrollBarCustomizer = scrollBarCustomizer;
        if (scrollElement != null) {
            scrollBarCustomizer.accept(scrollElement.getVerticalScrollBar());
        }
    }

    @Override
    public void addChildElements() {
        super.addChildElements();
        modularGui.getJEIDropTargets().removeIf(o -> o instanceof GuiElement && ((GuiElement) o).isInGroup("[ENTITY_FILTER_STACK_DROP]"));

        scrollElement = new GuiScrollElement().setListMode(GuiScrollElement.ListMode.VERT_LOCK_POS_WIDTH);
        scrollElement.setScrollBarStateChangingListener(() -> updateRequired = true);
        scrollElement.setStandardScrollBehavior();
        scrollElement.setInsets(2, 2, 2, 2);
        if (scrollBarCustomizer != null) {
            scrollBarCustomizer.accept(scrollElement.getVerticalScrollBar());
        }
        addChild(scrollElement);
        scrollElement.setPos(this);
        scrollElement.setSize(this.getInsetRect()).bindSize(this, true);
        rootNode = new GroupElement(this, entityFilter);
        scrollElement.addElement(rootNode);
    }

    @Override
    public void reloadElement() {
        super.reloadElement();
        rootNode.updateChildNodes();
    }

    public GuiScrollElement getScrollElement() {
        return scrollElement;
    }

    @Override
    public void renderElement(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
//        drawBorderedRect(xPos(), yPos(), xSize(), ySize(), 0.5, 0, Color.getHSBColor(((TimeKeeper.getClientTick() + hashCode()) / 500F) % 1F, 1F, 1F).getRGB());
        super.renderElement(minecraft, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean onUpdate() {
        if (updateRequired) {
            rootNode.updateChildNodes();
            updateRequired = false;
        }
        return super.onUpdate();
    }

    private abstract static class NodeElement<T extends FilterBase> extends GuiElement {
        protected GuiEntityFilter gui;
        protected GroupElement parentGroup;
        protected int nodeID;
        protected String filterNameKey;
        public GuiLabel nodeLabel;
        public GuiElement background;
        protected int depth = 1;
        private int trashTimer = 0;
        private int trashClicks = 0;
        protected GuiButton trash;

        public NodeElement(GuiEntityFilter gui, T filterNode) {
            this.reportYSizeChange = true;
            this.gui = gui;
            this.nodeID = filterNode.getNodeId();
            this.filterNameKey = filterNode.getTranslationKey();
            this.elementTranslationExt = "entity_filter";
        }

        @Override
        public void addChildElements() {
            background = gui.nodeBgBuilder.get();
            background.setPos(this).setXSizeMod(this::xSize).setYSize(this.ySize());
            addChild(background);

            nodeLabel = new GuiLabel(I18n.get(filterNameKey));
            nodeLabel.setHoverableTextCol(hovering -> gui.nodeTitleColour.getColour());
            nodeLabel.setWidthFromText(10);
            nodeLabel.setShadow(false);
            nodeLabel.setRelPos(this, 2, 1);
            addChild(nodeLabel);

            trash = new GuiButton().setSize(10, 10);
            trash.setHoverText(i18ni("delete." + (nodeID == 0 ? "all" : "node")));
            GuiTexture icon = new GuiTexture(10, 10, () -> BCSprites.get(nodeID == 0 ? "delete_all" : "delete"));
            trash.onPressed(() -> {
                trashClicks++;
                if (trashClicks == 3 && nodeID == 0) gui.entityFilter.clientClearFilter();
                else if (trashClicks == 2 && nodeID != 0) gui.entityFilter.clientRemoveNode(nodeID);
                trashTimer = 20;
            });
            trash.addChild(icon);
            trash.setYPos(yPos() + 1).setXPosMod(() -> maxXPos() - 11);
            icon.setXPosMod(trash::xPos);
            addChild(trash);

            super.addChildElements();
        }

        @Override
        public boolean onUpdate() {
            if (parentGroup != null && !parentGroup.stillHasNode(nodeID)) {
                parentGroup.removeNode(this);
                return true;
            }

            if (trashTimer > 0) {
                trashTimer--;
                if (trashTimer == 0) {
                    trashClicks = 0;
                }
            }
            return super.onUpdate();
        }

        public T getNode() {
            FilterBase node = gui.entityFilter.getNode(nodeID);
            return (T) node;
        }

        @Override
        public void renderElement(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
            super.renderElement(minecraft, mouseX, mouseY, partialTicks);
//            drawBorderedRect(xPos(), yPos(), xSize(), ySize(), 0.5, 0, Color.getHSBColor(((TimeKeeper.getClientTick() + hashCode()) / 500F) % 1F, 1F, 1F).getRGB());
        }

        protected void onRemoved() {}
    }

    private static class GroupElement extends NodeElement<FilterGroup> {
        private static int maxDepth = 4;
        protected Int2ObjectMap<NodeElement> nodeElements = new Int2ObjectOpenHashMap<>();

        public GroupElement(GuiEntityFilter gui, FilterGroup filterNode) {
            super(gui, filterNode);
        }

        @Override
        public void addChildElements() {
            super.addChildElements();
            background.setYSizeMod(this::ySize);

            GuiButton addButton = new GuiButton().setSize(10, 10);
            addButton.setHoverText(i18ni("add_filter"));
            GuiTexture addIcon = new GuiTexture(10, 10, BCSprites.get("add"));
            addButton.onPressed(() -> {
                GuiSelectDialog<FilterType> dialog = new GuiSelectDialog<>(gui);
                dialog.setCloseOnSelection(true);
                dialog.setListSpacing(1);
                dialog.setInsets(2, 2, 2, 2);
                dialog.setRendererBuilder(type -> {
                    GuiButton button = new GuiButton(i18ni(type.name().toLowerCase(Locale.ENGLISH))).set3dText(true);
                    button.setInsets(5, 0, 5, 0);
                    button.setSize(dialog.getInsetRect().x, 14);
                    GuiBorderedRect buttonBG = new GuiBorderedRect().setDoubleBorder(1).setXPos(button.xPos()).setYSizeMod(button::ySize).bindSize(button, false);
                    buttonBG.setFillColourL(Ctrl::fill);
                    buttonBG.setBorderColourL(Ctrl::border3D);
                    buttonBG.set3dTopLeftColourL(hovering -> button.isPressed() ? Ctrl.accentDark(hovering) : Ctrl.accentLight(hovering));
                    buttonBG.set3dBottomRightColourL(hovering -> button.isPressed() ? Ctrl.accentLight(hovering) : Ctrl.accentDark(hovering));
                    button.addChild(buttonBG);
                    return button;
                });
                dialog.setSelectionListener(e -> {
                    GuiButton.playGenericClick(mc);
                    gui.entityFilter.clientAddNode(e, getNode());
                });
                DataUtils.forEachMatch(FilterType.values(), e -> (e != FilterType.FILTER_GROUP || depth < maxDepth) && gui.entityFilter.isFilterAllowed(e), dialog::addItem);
                dialog.setSize(100, (dialog.getItems().size() * 15) + 3);
                dialog.addBackGroundChild(new GuiBorderedRect().set3DGetters(SubItem::fill, SubItem::accentLight, SubItem::accentDark).setDoubleBorder(1).setBorderColourL(e -> SubItem.border3d()).setPosAndSize(dialog));
                dialog.setPos(addButton.maxXPos() - 100, addButton.maxYPos() + 2);
                dialog.show(500);
            });
            addButton.addChild(addIcon);
            addButton.setYPos(yPos() + 1).setXPosMod(() -> trash.xPos() - 11);
            addIcon.setXPosMod(addButton::xPos);
            addChild(addButton);
            addButton.setEnabledCallback(() -> gui.entityFilter.nodeMap.size() < gui.entityFilter.maxFilters);

            GuiButton matchButton = new GuiButton().setShadow(true).setYSize(10).setInsets(0, 0, 0, 0);
            matchButton.setDisplaySupplier(() -> TextFormatting.UNDERLINE + i18ni("and_group.button." + isAndNode()));
            matchButton.setHoverText((e) -> i18ni("and_group." + isAndNode()));
            matchButton.onPressed(() -> { if (getNode() != null) getNode().setAndGroup(!getNode().isAndGroup()); });
            matchButton.setYPos(yPos() + 1).setXPosMod(() -> addButton.xPos() - fontRenderer.width(matchButton.getDisplayString()) - 1);
            matchButton.setXSizeMod(() -> fontRenderer.width(matchButton.getDisplayString()));
            addChild(matchButton);

            updateChildNodes();
        }

        protected boolean stillHasNode(int nodeID) {
            return getNode() != null && getNode().getSubNodeMap().containsKey(nodeID);
        }

        protected void removeNode(NodeElement element) {
            removeChild(element);
            element.onRemoved();
            if (nodeElements.containsKey(element.nodeID)) {
                nodeElements.remove(element.nodeID);
                gui.rootNode.updateChildNodes();
                gui.rootNode.updateChildNodes(); //Called twice to get around a little edge case weirdness.
            }
        }

        protected void updateChildNodes() {
            reportYSizeChange = false;
            setYSize(12);
            reportYSizeChange = true;
            if (getNode() instanceof EntityFilter) {
                setXPos(gui.scrollElement.getInsetRect().x).setXSize(gui.scrollElement.getInsetRect().width);
            }

            FilterGroup node = getNode();
            if (node == null) {
                if (parentGroup != null) parentGroup.removeNode(this);
                return;
            }

            int yPos = yPos() + 12 + 1;
            for (int nodeID : node.getSubNodeMap().keySet()) {
                if (!nodeElements.containsKey(nodeID)) {
                    NodeElement element = gui.createElementFor(node.getSubNodeMap().get(nodeID));
                    element.parentGroup = this;
                    element.depth = depth + 1;
                    addChild(element);
                    nodeElements.put(nodeID, element);
                }

                NodeElement element = nodeElements.get(nodeID);
                element.setXSizeMod(() -> xSize() - 6).setPos(xPos() + 4, yPos);
                if (element instanceof GroupElement) {
                    ((GroupElement) element).updateChildNodes();
                }
                yPos += element.ySize() + 1;
            }
//            Works around an edge case where the scroll bar breaks due to repeat y size changing from a size where its required to a size where its not within update callback.. i think.
//            I really cant wait to re write the scroll element...
            setYSize(getEnclosingRect().height + (nodeElements.isEmpty() ? 0 : 2));
            setYSize(ySize());
        }

        @Override
        public boolean onUpdate() {
            if (getNode() != null && !getNode().getSubNodeMap().keySet().stream().allMatch(nodeElements::containsKey)) {
                gui.rootNode.updateChildNodes();
                return true;
            }

            return super.onUpdate();
        }

        private boolean isAndNode() {
            return getNode() != null && getNode().isAndGroup();
        }
    }

    public static class NodeHostile extends NodeElement<FilterHostile> {

        public NodeHostile(GuiEntityFilter gui, FilterHostile filterNode) {
            super(gui, filterNode);
            setYSize(12);
        }

        @Override
        public void addChildElements() {
            super.addChildElements();
            nodeLabel.setEnabled(false);

            GuiButton toggleHostile = new GuiButton();
            toggleHostile.setPos(this).setYSize(ySize()).setXSizeMod(() -> xSize() - 12);
            toggleHostile.setDisplaySupplier(() -> i18ni("hostile." + (getNode() != null && getNode().isWhitelistHostile())));
            toggleHostile.setAlignment(GuiAlign.LEFT);
            toggleHostile.getInsets().left = 2;
            toggleHostile.onPressed(() -> { if (getNode() != null) getNode().setWhitelistHostile(!getNode().isWhitelistHostile()); });
            addChild(toggleHostile);
        }
    }

    public static class NodeTamed extends NodeElement<FilterTamed> {

        public NodeTamed(GuiEntityFilter gui, FilterTamed filterNode) {
            super(gui, filterNode);
            setYSize(22);
        }

        @Override
        public void addChildElements() {
            super.addChildElements();
            nodeLabel.setEnabled(false);

            GuiButton toggleTamed = new GuiButton();
            toggleTamed.setPos(xPos(), yPos() + 2).setYSize(8).setXSizeMod(() -> xSize() - 12);
            toggleTamed.setDisplaySupplier(() -> i18ni("tamed." + (getNode() != null && getNode().isWhitelistTamed())));
            toggleTamed.setAlignment(GuiAlign.LEFT);
            toggleTamed.getInsets().left = 2;
            toggleTamed.onPressed(() -> { if (getNode() != null) getNode().setWhitelistTamed(!getNode().isWhitelistTamed()); });
            addChild(toggleTamed);

            GuiButton toggleTamable = new GuiButton();
            toggleTamable.setPos(xPos(), yPos() + 12).setYSize(8).setXSizeMod(() -> xSize() - 12);
            toggleTamable.setDisplaySupplier(() -> "[ " + i18ni("tamable." + (getNode() != null && getNode().isIncludeTamable())) + " ]");
            toggleTamable.setHoverText((e) -> i18ni("tamable.info"));
            toggleTamable.setAlignment(GuiAlign.LEFT);
            toggleTamable.getInsets().left = 6;
            toggleTamable.onPressed(() -> { if (getNode() != null) getNode().setIncludeTamable(!getNode().isIncludeTamable()); });
            addChild(toggleTamable);
        }
    }

    public static class NodeAdults extends NodeElement<FilterAdults> {

        public NodeAdults(GuiEntityFilter gui, FilterAdults filterNode) {
            super(gui, filterNode);
            setYSize(22);
        }

        @Override
        public void addChildElements() {
            super.addChildElements();
            nodeLabel.setEnabled(false);

            GuiButton toggleAdults = new GuiButton();
            toggleAdults.setPos(xPos(), yPos() + 2).setYSize(8).setXSizeMod(() -> xSize() - 12);
            toggleAdults.setDisplaySupplier(() -> i18ni("adults." + (getNode() != null && getNode().isWhitelistAdults())));
            toggleAdults.setAlignment(GuiAlign.LEFT);
            toggleAdults.getInsets().left = 2;
            toggleAdults.onPressed(() -> { if (getNode() != null) getNode().setWhitelistAdults(!getNode().isWhitelistAdults()); });
            addChild(toggleAdults);

            GuiButton toggleAgeable = new GuiButton();
            toggleAgeable.setPos(xPos(), yPos() + 12).setYSize(8).setXSizeMod(() -> xSize() - 12);
            toggleAgeable.setDisplaySupplier(() -> "[ " + i18ni("non_ageable." + (getNode() != null && getNode().isIncludeNonAgeable())) + " ]");
            toggleAgeable.setHoverText((e) -> i18ni("non_ageable.info"));
            toggleAgeable.setAlignment(GuiAlign.LEFT);
            toggleAgeable.getInsets().left = 6;
            toggleAgeable.onPressed(() -> { if (getNode() != null) getNode().setIncludeNonAgeable(!getNode().isIncludeNonAgeable()); });
            addChild(toggleAgeable);
        }
    }

    public static class NodePlayers extends NodeElement<FilterPlayer> {

        public NodePlayers(GuiEntityFilter gui, FilterPlayer filterNode) {
            super(gui, filterNode);
            setYSize(22);
        }

        @Override
        public void addChildElements() {
            super.addChildElements();
            nodeLabel.setEnabled(false);

            GuiButton toggleIncludePlayers = new GuiButton();
            toggleIncludePlayers.setPos(xPos(), yPos() + 2).setYSize(8).setXSizeMod(() -> xSize() - 12);
            toggleIncludePlayers.setDisplaySupplier(() -> i18ni("player." + (getNode() != null && getNode().isWhitelistPlayers())));
            toggleIncludePlayers.setAlignment(GuiAlign.LEFT);
            toggleIncludePlayers.getInsets().left = 2;
            toggleIncludePlayers.onPressed(() -> { if (getNode() != null) getNode().setWhitelistPlayers(!getNode().isWhitelistPlayers()); });
            toggleIncludePlayers.setHoverText(i18ni("player.info"));
            addChild(toggleIncludePlayers);

            GuiLabel nameLabel = new GuiLabel(i18ni("player.name")).setShadow(false).setTextColour(gui.nodeTitleColour.getColour());
            nameLabel.setPos(xPos() + 2, yPos() + 12).setWidthFromText(8);
            addChild(nameLabel);

            GuiTextField nameField = new GuiTextField();
//            nameField.setLinkedValue(() -> getNode() == null ? "" : getNode().getPlayerName());
            nameField.setLinkedValue(() -> getNode() == null ? "" : getNode().getPlayerName(), s -> { if (getNode() != null) getNode().setPlayerName(s); }); //TODO Test
//            nameField.setChangeListener(s -> { if (getNode() != null) getNode().setPlayerName(s); });
            nameField.setValidator(s -> FilterPlayer.namePattern.matcher(s).find());
            nameField.setMaxStringLength(16);
            nameField.setEnableBackgroundDrawing(false);
            nameField.setPos(nameLabel.maxXPos() + 1, yPos() + 12).setYSize(10).setXSizeMod(() -> xSize() - (nameLabel.xSize() + 15));
            addChild(nameField);
        }
    }

    public static class NodeEntityType extends NodeElement<FilterEntity> {

        public NodeEntityType(GuiEntityFilter gui, FilterEntity filterNode) {
            super(gui, filterNode);
            setYSize(22);
        }

        @Override
        public void addChildElements() {
            super.addChildElements();
            nodeLabel.setEnabled(false);

            GuiButton toggleIncludeEntity = new GuiButton();
            toggleIncludeEntity.setPos(xPos(), yPos() + 2).setYSize(8).setXSizeMod(() -> xSize() - 12);
            toggleIncludeEntity.setDisplaySupplier(() -> i18ni("entity_type." + (getNode() != null && getNode().isWhitelistEntity())));
            toggleIncludeEntity.setAlignment(GuiAlign.LEFT);
            toggleIncludeEntity.getInsets().left = 2;
            toggleIncludeEntity.onPressed(() -> { if (getNode() != null) getNode().setWhitelistEntity(!getNode().isWhitelistEntity()); });
            addChild(toggleIncludeEntity);

            GuiLabel nameLabel = new GuiLabel(i18ni("entity_type.name")).setShadow(false).setTextColour(gui.nodeTitleColour.getColour());
            nameLabel.setPos(xPos() + 2, yPos() + 12).setWidthFromText(8);
            addChild(nameLabel);

            GuiTextField nameField = new GuiTextField();
            nameField.setValidator(ResourceLocation::isValidResourceLocation);
//            nameField.setLinkedValue(() -> getNode() == null ? "" : getNode().getEntityName());
            nameField.setLinkedValue(() -> getNode() == null ? "" : getNode().getEntityName(), s -> {
                if (getNode() != null) {
                    getNode().setEntityName(s);
                    ResourceLocation rs = new ResourceLocation(s);
                    //TODO Test
                    EntityType<?> type = ForgeRegistries.ENTITIES.getValue(rs);
                    boolean exists = ForgeRegistries.ENTITIES.containsKey(rs);
                    String name = exists ? type.getDescriptionId() : "unknown";
                    nameField.setTextColor(exists ? 0x00FF00 : 0xFF0000);
                    Optional<? extends ModContainer> mod = ModList.get().getModContainerById(rs.getNamespace());
                    String modName = mod.isPresent() ? mod.get().getModInfo().getDisplayName() : "[unknown-mod]";
                    String entityName = I18n.get(name);
                    if (exists) {
                        nameField.setHoverText(entityName, TextFormatting.BLUE + "" + TextFormatting.ITALIC + modName);
                    } else {
                        nameField.setHoverText(TextFormatting.RED + "Unknown entity string");
                    }
                }
            }); //TODO Test

//            nameField.setChangeListener(s -> {
//
//            });
            nameField.setMaxStringLength(512);
            nameField.setEnableBackgroundDrawing(false);
            nameField.setPos(nameLabel.maxXPos() + 1, yPos() + 12).setYSize(10).setXSizeMod(() -> xSize() - (nameLabel.xSize() + 15));
            addChild(nameField);

            GuiButton select = new GuiButton("...").setFillColour(0xFF000000).setBorderColour(0xFF303030);
            select.setYPos(yPos() + 12).setXPosMod(() -> maxXPos() - 10).setYSize(8).setXSize(8);
            select.setHoverText(i18ni("entity_type.find"));
            select.setInsets(0, 1, 3, 0);
            select.onPressed(() -> {
                GuiSelectDialog<ResourceLocation> dialog = new GuiSelectDialog<>(gui);
                dialog.setCloseOnSelection(true);
                dialog.setListSpacing(1);
                dialog.setInsets(3, 3, 15, 2);
                dialog.setRendererBuilder(rs -> {
                    GuiElement container = new GuiElement();
                    container.setYSize(20);
                    GuiEntityRenderer renderer = new GuiEntityRenderer().setEntity(rs);
                    renderer.setSize(16, 16);
                    renderer.setPos(8, 2);
                    container.addChild(renderer);
                    EntityType type = ForgeRegistries.ENTITIES.getValue(rs);
                    String name = type == null ? "unknown" : type.getDescription().getString();
                    GuiLabel label = new GuiLabel(I18n.get(I18n.get(name)));
                    label.setPos(renderer.maxXPos() + 6, container.yPos() + 2).setWrap(true).setYSize(container.ySize() - 4).setXSizeMod(() -> container.xSize() - (16 + 6 + 2 + 2));
                    container.addChild(label);
                    label.zOffset+=5;
                    GuiBorderedRect buttonBG = new GuiBorderedRect().setDoubleBorder(0).setXPos(container.xPos()).setYSizeMod(container::ySize).bindSize(container, false);
                    buttonBG.setFillColourL((h) -> SubItem.fill());
                    buttonBG.setBorderColourL((h) -> SubItem.border3d());
                    buttonBG.set3dTopLeftColourL((h) -> SubItem.accentLight());
                    buttonBG.set3dBottomRightColourL((h) -> SubItem.accentDark());
                    container.addChild(buttonBG);
                    return container;
                });
                dialog.setSelectionListener(rs -> {
                    GuiButton.playGenericClick(mc);
                    if (getNode() != null) {
                        nameField.setText(rs.toString());
                    }
                });

                DataUtils.forEachMatch(ForgeRegistries.ENTITIES.getEntries(), e -> {
                    return e.getValue().create(mc.level) instanceof LivingEntity;
                }, e -> dialog.addItem(e.getKey().location()));
                dialog.setSize(150, 190);
                dialog.addBackGroundChild(new GuiBorderedRect().set3DGetters(SubItem::fill, SubItem::accentLight, SubItem::accentDark).setDoubleBorder(1).setBorderColourL(e -> SubItem.border3d()).setPosAndSize(dialog));
                GuiTextField filter = new GuiTextField();
                filter.textZOffset+=5;
                filter.setSize(dialog.xSize() - 6, 12).setPos(dialog.xPos() + 3, dialog.maxYPos() - 15);
                filter.setChangeListener((s) -> {
                    dialog.clearItems();
                    DataUtils.forEachMatch(ForgeRegistries.ENTITIES.getEntries(), e -> {
                        EntityType<?> type = e.getValue();
                        boolean pass = s.isEmpty() || type.toString().toLowerCase(Locale.ENGLISH).contains(s.toLowerCase(Locale.ENGLISH));
                        String name = type.getDescription().getString();
                        if (!pass && name.toLowerCase(Locale.ENGLISH).contains(s.toLowerCase(Locale.ENGLISH))) {
                            pass = true;
                        }
                        return pass && type.create(mc.level) instanceof LivingEntity;
                    }, e -> dialog.addItem(e.getKey().location()));
                });
                GuiLabel searchLabel = new GuiLabel(i18ni("search")).setTextColour(0xB0B0B0).setShadow(false);
                searchLabel.setPosAndSize(filter).translate(0, 1);
                searchLabel.setAlignment(GuiAlign.LEFT);
                searchLabel.getInsets().left = 3;
                searchLabel.setEnabledCallback(() -> filter.getText().isEmpty() && !filter.isFocused());
                filter.addChild(searchLabel);
                dialog.addChild(filter);
                filter.zOffset++;
                searchLabel.zOffset++;
                dialog.showCenter(500);
                gui.scrollBarCustomizer.accept(dialog.scrollElement.getVerticalScrollBar());
            });
            addChild(select);
        }
    }

    public static class NodeItem extends NodeElement<FilterItem> {
        private GuiTextField countField;
        private GuiTextField metaField;
        private GuiTextField nbtField;
        private GuiStackIcon stackIcon;

        public NodeItem(GuiEntityFilter gui, FilterItem filterNode) {
            super(gui, filterNode);
            setYSize(41);
        }

        @Override
        public void addChildElements() {
            super.addChildElements();
            nodeLabel.setEnabled(false);

            GuiButton toggleWhiteList = new GuiButton();
            toggleWhiteList.setPos(xPos(), yPos() + 2).setYSize(8).setXSizeMod(() -> xSize() - 12);
            toggleWhiteList.setDisplaySupplier(() -> i18ni("item_filter." + (getNode() != null && getNode().isWhitelistItem())));
            toggleWhiteList.setAlignment(GuiAlign.LEFT);
            toggleWhiteList.getInsets().left = 2;
            toggleWhiteList.onPressed(() -> { if (getNode() != null) getNode().setWhitelistItem(!getNode().isWhitelistItem()); });
            addChild(toggleWhiteList);


            stackIcon = new GuiStackIcon(null);
            updateStackIcon();
            stackIcon.setToolTip(false);
            stackIcon.addSlotBackground();
            stackIcon.setPos(xPos() + 2, yPos() + 11);
            stackIcon.setClickListener(() -> {
                if (getNode() != null) {
                    PlayerEntity player = Minecraft.getInstance().player;
                    if (!player.inventory.getCarried().isEmpty()) {
                        ItemStack stack = player.inventory.getCarried().copy();
                        stack.setCount(1);
                        getNode().setItemName(stack.getItem().getRegistryName().toString());
                        getNode().setDamage(stack.isDamageableItem() ? stack.getDamageValue() : -1);
                        getNode().setNbt(stack.getTag());
                    } else {
                        getNode().setItemName("");
                    }
                }
            });
            addChild(stackIcon);
            stackIcon.addToGroup("[ENTITY_FILTER_STACK_DROP]");
            modularGui.getJEIDropTargets().add(stackIcon);
            stackIcon.setIngredientDropListener(e -> {
                if (e instanceof ItemStack && getNode() != null) {
                    ItemStack stack = ((ItemStack) e).copy();
                    stack.setCount(1);
                    getNode().setItemName(stack.getItem().getRegistryName().toString());
                    getNode().setDamage(stack.isDamageableItem() ? stack.getDamageValue() : -1);
                    getNode().setNbt(stack.getTag());
                }
            });
            stackIcon.setEnabledCallback(() -> gui.scrollElement.getInsetRect().intersects(stackIcon.getRect()));

            GuiLabel countLabel = new GuiLabel(i18ni("item.count")).setShadow(false).setTextColour(gui.nodeTitleColour.getColour());
            countLabel.setPos(stackIcon.maxXPos() + 2, yPos() + 12).setWidthFromText(8);
            countLabel.setHoverText(i18ni("item.count.info"));
            addChild(countLabel);

            GuiLabel metaLabel = new GuiLabel(i18ni("item.damage")).setShadow(false).setTextColour(gui.nodeTitleColour.getColour());
            metaLabel.setPos(stackIcon.maxXPos() + 2, yPos() + 20).setWidthFromText(8);
            metaLabel.setHoverText(i18ni("item.damage.info"));
            addChild(metaLabel);

            countField = new GuiTextField();
            countField.setHoverText(i18ni("item.count.info"));
//            countField.setLinkedValue(() -> getNode() == null ? "" : getNode().getCount() == 0 ? "" : "" + getNode().getCount());
            countField.setLinkedValue(() -> getNode() == null ? "" : getNode().getCount() == 0 ? "" : "" + getNode().getCount(), s -> {
                if (getNode() != null) {
                    if (s.isEmpty()) getNode().setCount(0);
                    else getNode().setCount(Utils.parseInt(s));
                }
            }); //TODO Test

//            countField.setChangeListener(s -> {
//                if (getNode() != null) {
//                    if (s.isEmpty()) getNode().setCount(0);
//                    else getNode().setCount(Utils.parseInt(s));
//                }
//            });
            countField.setValidator(s -> s.isEmpty() || (Utils.validInteger(s) && Utils.parseInt(s) > 0 && Utils.parseInt(s) <= 64));
            countField.setMaxStringLength(2);
            countField.setEnableBackgroundDrawing(false);
            countField.setPos(Math.max(countLabel.maxXPos(), metaLabel.maxXPos()) + 1, yPos() + 12).setYSize(10).setXSize(20);
            addChild(countField);

            metaField = new GuiTextField();
            metaField.setHoverText(i18ni("item.damage.info"));
//            metaField.setLinkedValue(() -> getNode() == null ? "" : getNode().getDamage() == -1 ? "" : "" + getNode().getDamage());
            countField.setLinkedValue(() -> getNode() == null ? "" : getNode().getDamage() == 0 ? "" : "" + getNode().getDamage(), s -> {
                if (getNode() != null) {
                    if (s.isEmpty()) getNode().setDamage(-1);
                    else getNode().setDamage(Utils.parseInt(s));
                }
            }); //TODO Test

//            metaField.setChangeListener(s -> {
//                if (getNode() != null) {
//                    if (s.isEmpty()) getNode().setDamage(-1);
//                    else getNode().setDamage(Utils.parseInt(s));
//                }
//            });
            metaField.setValidator(s -> s.isEmpty() || (Utils.validInteger(s) && Utils.parseInt(s) >= 0 && Utils.parseInt(s) <= Short.MAX_VALUE));
            metaField.setMaxStringLength(5);
            metaField.setEnableBackgroundDrawing(false);
            metaField.setPos(Math.max(countLabel.maxXPos(), metaLabel.maxXPos()) + 1, yPos() + 20).setYSize(10).setXSize(30);
            addChild(metaField);

            GuiButton blocksItems = new GuiButton().setWrap(true);
            blocksItems.setYPos(yPos() + 16).setYSize(16).setXPosMod(() -> Math.max(metaLabel.maxXPos(), countLabel.maxXPos()) + 3);
            blocksItems.setDisplaySupplier(() -> {
                if (getNode() != null) {
                    return getNode().isFilterBlocks() ? i18ni("item.blocks_only") : //
                            getNode().isFilterItems() ? i18ni("item.items_only") : //
                                    i18ni("item.items_or_blocks");
                }
                return "error";
            });
            blocksItems.setXSizeMod(() -> maxXPos() - 3 - blocksItems.xPos());
            blocksItems.setEnabledCallback(() -> getNode() != null && getNode().getItemName().isEmpty());
            blocksItems.setAlignment(GuiAlign.RIGHT);
            blocksItems.getInsets().right = 0;
            blocksItems.getInsets().left = 0;
            blocksItems.onPressed(() -> {
                if (getNode() != null) {
                    if (getNode().isFilterItems()) {
                        getNode().setFilterItemsBlocks(false, true);
                    } else if (getNode().isFilterBlocks()) {
                        getNode().setFilterItemsBlocks(false, false);
                    } else {
                        getNode().setFilterItemsBlocks(true, false);
                    }
                }
            });
            addChild(blocksItems);

            GuiLabel nbtLabel = new GuiLabel(i18ni("item.nbt")).setShadow(false).setTextColour(gui.nodeTitleColour.getColour());
            nbtLabel.setPos(xPos() + 2, yPos() + 31).setWidthFromText(8);
            nbtLabel.setHoverText(i18ni("item.nbt.info"));
            addChild(nbtLabel);

            nbtField = new GuiTextField();
            nbtField.setHoverText(i18ni("item.nbt.info"));
            nbtField.setText(getNode() == null || getNode().getNbt() == null ? "" : getNode().getNbt().toString());
            nbtField.setChangeListener(s -> {
                if (getNode() != null) {
                    if (s.isEmpty()) {
                        getNode().setNbt(null);
                        nbtField.setTextColor(0xFFFFFF);
                        nbtField.setHoverText(i18ni("item.nbt.info"));
                    } else {
                        try {
                            CompoundNBT compound = JsonToNBT.parseTag(s);
                            getNode().setNbt(compound);
                            nbtField.setTextColor(0xFFFFFF);
                            nbtField.setHoverText(i18ni("item.nbt.info"));
                        }
                        catch (CommandSyntaxException e) {
                            getNode().setNbt(null);
                            nbtField.setTextColor(0xFF0000);
                            nbtField.setHoverText(i18ni("item.nbt.bad"));
                        }
                    }
                }
            });
            nbtField.setMaxStringLength(1024);
            nbtField.setEnableBackgroundDrawing(false);
            nbtField.setPos(nbtLabel.maxXPos() + 1, yPos() + 31).setYSize(10).setXSizeMod(() -> xSize() - (nbtLabel.xSize() + 3));
            addChild(nbtField);
//            Will have a chost item box (look into jei ghost item support?)
//            and a text box with item string
//            Also i guess maybe an option to set count... and damage... ore handling? also the block stiff...
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
            countField.setFocused(false);
            metaField.setFocused(false);
            nbtField.setFocused(false);
            return super.mouseClicked(mouseX, mouseY, mouseButton);
        }

        private void updateStackIcon() {
            if (getNode() != null) {
                if (getNode().getItemName().isEmpty()) {
                    stackIcon.setStack((StackReference) null);
                    stackIcon.setHoverText(i18ni("set_stack"));
                } else {
                    StackReference stack = new StackReference(getNode().getItemName(), getNode().getCount(), getNode().getDamage(), getNode().getNbt());
                    stackIcon.setStack(stack);
                    List<ITextComponent> tooltip = stack.createStack().getTooltipLines(mc.player, mc.options.advancedItemTooltips ? ADVANCED : NORMAL);
                    tooltip.add(new StringTextComponent(TextFormatting.GRAY + "----------------------------"));
                    tooltip.add(new TranslationTextComponent("set_stack"));
                    stackIcon.setComponentHoverText(tooltip);
                }
            }
        }

        @Override
        public boolean onUpdate() {
            if (getNode() != null && getNode().dataChanged) {
                nbtField.setText(getNode() == null || getNode().getNbt() == null ? "" : getNode().getNbt().toString());
                updateStackIcon();
                getNode().dataChanged = false;
            }

            return super.onUpdate();
        }

        @Override
        protected void onRemoved() {
            modularGui.getJEIDropTargets().remove(stackIcon);
        }
    }

    private NodeElement createElementFor(FilterBase node) {
        switch (node.getType()) {
            case HOSTILE:
                return new NodeHostile(this, (FilterHostile) node);
            case TAMED:
                return new NodeTamed(this, (FilterTamed) node);
            case ADULTS:
                return new NodeAdults(this, (FilterAdults) node);
            case PLAYER:
                return new NodePlayers(this, (FilterPlayer) node);
            case ENTITY_TYPE:
                return new NodeEntityType(this, (FilterEntity) node);
            case ITEM_FILTER:
                return new NodeItem(this, (FilterItem) node);
            case FILTER_GROUP:
                return new GroupElement(this, (FilterGroup) node);
        }
        throw new IllegalStateException("Unknown Filter Type: " + node.getType());
    }
}
