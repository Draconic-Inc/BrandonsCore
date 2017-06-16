package com.brandon3055.brandonscore.registry;

import com.brandon3055.brandonscore.utils.DataUtils;
import com.brandon3055.brandonscore.utils.LogHelperBC;
import com.google.common.base.Throwables;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.discovery.ASMDataTable;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Created by brandon3055 on 18/3/2016. <br><br>
 * <p>
 * Things have changed since 1.10.2 so listen up!
 * You no longer create an instance of this
 */
public class ModFeatureParser {

    public static final String CATEGORY_BLOCKS = "Blocks";
    public static final String CATEGORY_ITEMS = "Items";

    /**
     * Stores a boolean value for each feature thats indicates whether or not the feature is enabled.
     */
    private static final Map<Object, Boolean> featureStates = new HashMap<>();
    /**
     * Stores a list of Feature data holder to feature object. Feature object will be ether an item or a block
     */
    private static final Map<Feature, Object> allModFeatures = new HashMap<>();

    /**
     * This is a map of all mod id's to mod features.
     */
    private static final Map<String, List<Feature>> modFeatureMap = new HashMap<>();

    /**
     * Stores the {@link IModFeatures} instance for every mod that has one.
     */
    private static final Map<String, IModFeatures> iModFeaturesMap = new HashMap<>();

    /**
     * Stores the {@link IModFeatures} instance for every mod that has one.
     */
    private static final Map<String, Boolean> hardDisableModeMap = new HashMap<>();

    //region Pars & Config

    /**
     * Parses the ASMDataTable and finds all defined mod features.
     */
    public static void parseASMData(ASMDataTable table) {
        String modid = "unknown";
        for (ASMDataTable.ASMData data : table.getAll(ModFeatures.class.getName())) {
            try {
                Class clazz = Class.forName(data.getClassName());
                ModFeatures features = (ModFeatures) clazz.getAnnotation(ModFeatures.class);
                modid = features.modid();
                List<Feature> modFeatures = modFeatureMap.computeIfAbsent(modid, s -> new ArrayList<>());

                if (IModFeatures.class.isAssignableFrom(clazz)) {
                    if (iModFeaturesMap.containsKey(modid)) {
                        throw new RuntimeException("Mod: " + modid + " Attempted to register more than 1 IModFeatures class. THERE CAN ONLY BE ONE!!!");
                    }
                    iModFeaturesMap.put(modid, (IModFeatures) clazz.newInstance());
                }

                LogHelperBC.info("Found mod feature container for mod: " + modid);
                for (Field field : clazz.getFields()) {
                    try {
                        if (field.isAnnotationPresent(ModFeature.class)) {
                            Object featureObj = field.get(null);
                            Feature feature = Feature.extract(modid, field.getAnnotation(ModFeature.class));

                            if (featureObj instanceof IRegistryListener && !((IRegistryListener) featureObj).featureParsed(feature)) {
                                LogHelperBC.dev("Skipping Mod Feature: " + feature.getRegistryName() + " Disabled via IRegistryListener annotation on feature.");
                                continue;
                            }

                            allModFeatures.put(feature, featureObj);
                            featureStates.put(featureObj, feature.isActive());
                            modFeatures.add(feature);
                            LogHelperBC.dev("Loaded Mod Feature: " + feature.getRegistryName());
                        }
                    }
                    catch (Throwable e) {
                        LogHelperBC.error("An error occurred while attempting to parse feature " + field.getName() + " from mod " + modid);
                        e.printStackTrace();
                    }
                }
            }
            catch (Throwable e) {
                LogHelperBC.error("An error occurred while attempting to load mod features for mod " + modid);
                Throwables.propagate(e);
            }
        }
    }

    /**
     * This is called from ModConfigParser.
     * Generates or reads the config setting for every loaded feature.
     *
     * @param configuration the mods configuration
     */
    public static void loadModFeatureConfig(String modid, Configuration configuration) {
        if (modFeatureMap.containsKey(modid)) {
            hardDisableModeMap.put(modid, configuration.get("Block/Item Config", "hardDisableMode", false, "If set to true blocks and items will be completely removed from the game when disabled.\n" +
                    "When set to softDisableMode they will just have their recipes removed and will not show up in NEI/JEI or the Creative Inventory.\n" +
                    "Soft mode is recommended. Only use hard mode if you know what you are doing.").setRequiresMcRestart(true).getBoolean(false));

            modFeatureMap.get(modid).forEach(feature -> {
                Object featureObj = allModFeatures.get(feature);
                try {
                    if (!feature.canBeDisabled()) {
                        featureStates.put(featureObj, true);
                    }
                    else {
                        String category = featureObj instanceof Block ? CATEGORY_BLOCKS : CATEGORY_ITEMS;
                        Property prop = configuration.get(category, feature.getName(), feature.isActive()).setRequiresMcRestart(true);
                        feature.setActive(prop.getBoolean(feature.isActive()));
                        featureStates.put(featureObj, feature.isActive());
                        ModConfigParser.addFeatureProperty(modid, prop, category);
                    }
                }
                catch (Exception e) {
                    LogHelperBC.error("Error Loading Block/Item Config - " + featureObj + " for " + modid);
                    Throwables.propagate(e);
                }
            });
        }
        else {
            LogHelperBC.warn("No features were detected for mod: " + modid + ". This maybe an issue or maybe this mod just does not have any items or blocks.");
        }

        if (configuration.hasChanged()) {
            configuration.save();
        }
    }

    //endregion

    //region Registration

    /**
     * Registers all features that are not disabled via the config.
     * Must be called AFTER loadFeatureConfig
     */
    public static void registerFeatures() {
        for (String modid : modFeatureMap.keySet()) {
            LogHelperBC.info("Registering features for mod: " + modid);

            for (Feature feature : modFeatureMap.get(modid)) {
                if (!feature.isActive() && hardDisableModeMap.get(modid)) {
                    LogHelperBC.dev("Skipping registration of disabled feature: " + feature.getRegistryName());
                    continue;
                }

                LogHelperBC.dev("Registering feature: " + feature.getRegistryName());
                Object featureObj = allModFeatures.get(feature);

                //Handler Overridden feature registration
                if (featureObj instanceof IRegistryOverride) {
                    ((IRegistryOverride) featureObj).handleCustomRegistration(feature);
                    if (!((IRegistryOverride) featureObj).enableDefaultRegistration(feature)) {
                        continue;
                    }
                }

                if (featureObj instanceof Block) {
                    registerFeatureBlock(feature, (Block) featureObj);
                }
                else if (featureObj instanceof Item) {
                    registerFeatureItem(feature, (Item) featureObj);
                }
                else {
                    LogHelperBC.error("Skipping registration of invalid/unsupported feature type: " + featureObj);
                }
            }
        }
    }

    /**
     * Handles the registration of a block feature.
     */
    private static void registerFeatureBlock(Feature feature, Block block) {
        block.setRegistryName(feature.getRegistryName());
        block.setUnlocalizedName(feature.getRegistryName().toString());

        if (iModFeaturesMap.containsKey(feature.getModid()) && feature.isActive()) {
            CreativeTabs tab = iModFeaturesMap.get(feature.getModid()).getCreativeTab(feature);
            if (tab != null) {
                block.setCreativeTab(tab);
            }
        }

        GameRegistry.register(block);

        if (feature.hasItemBlock()) {
            try {
                Constructor<? extends ItemBlock> constructor = feature.itemBlock().getConstructor(Block.class);
                ItemBlock itemBlock = constructor.newInstance(block);
                itemBlock.setRegistryName(feature.getRegistryName());
                if (!(itemBlock instanceof IRegistryListener) || ((IRegistryListener) itemBlock).featureParsed(feature)) {
                    GameRegistry.register(itemBlock);
                }
            }
            catch (Throwable e) {
                LogHelperBC.error("An error occurred while trying to create the ItemBlock for feature: " + feature.getRegistryName());
                Throwables.propagate(e);
            }
        }

        if (feature.tileEntity() != TileEntity.class) {
            GameRegistry.registerTileEntity(feature.tileEntity(), feature.getRegistryName().toString());
        }
    }

    /**
     * Handles the registration of an item feature.
     */
    private static void registerFeatureItem(Feature feature, Item item) {
        item.setRegistryName(feature.getRegistryName());
        item.setUnlocalizedName(feature.getRegistryName().toString());

        if (iModFeaturesMap.containsKey(feature.getModid()) && feature.isActive()) {
            CreativeTabs tab = iModFeaturesMap.get(feature.getModid()).getCreativeTab(feature);
            if (tab != null) {
                item.setCreativeTab(tab);
            }
        }

        GameRegistry.register(item);
    }

    //endregion

    //region Client Registration

    /**
     * Registers the rendering for all loaded and enabled features.
     * Must be called AFTER registerFeatures, during Pre Initialization and from your Client Proxy
     */
    @SideOnly(Side.CLIENT)
    public static void registerFeatureRendering() {
        for (String modid : modFeatureMap.keySet()) {
            LogHelperBC.info("Registering feature renderers for mod: " + modid);

            for (Feature feature : modFeatureMap.get(modid)) {
                if (!feature.isActive() && hardDisableModeMap.get(modid)) {
                    continue;
                }

                Object featureObj = allModFeatures.get(feature);

                //Handler Overridden renderer registration
                if (featureObj instanceof IRenderOverride) {
                    ((IRenderOverride) featureObj).registerRenderer(feature);
                    if (!((IRenderOverride) featureObj).registerNormal(feature)) {
                        continue;
                    }
                }

                if (featureObj instanceof Block) {
                    registerBlockRendering(feature, (Block) featureObj);
                }
                else if (featureObj instanceof Item) {
                    registerItemVariants(feature, (Item) featureObj);
                }
                else {
                    LogHelperBC.error("Skipping registration of invalid/unsupported feature type: " + featureObj);
                }
            }
        }
    }

    /**
     * Registers Item Block Rendering.
     */
    @SideOnly(Side.CLIENT)
    private static void registerBlockRendering(Feature feature, Block block) {
        //Register itemBlock rendering
        if (feature.variantMap().size() > 0) {
            registerItemVariants(feature, Item.getItemFromBlock(block));
        }
        else {
            ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(block), 0, new ModelResourceLocation(feature.getRegistryName().toString()));
        }
    }

    /**
     * Registers item variants.
     */
    @SideOnly(Side.CLIENT)
    private static void registerItemVariants(Feature feature, Item item) {
        //Register Default ModelResourceLocation
        if (feature.variantMap().isEmpty() && feature.stateOverride().isEmpty()) {
            ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(feature.getRegistryName().toString()));
        }
        //Register ModelResourceLocation with overridden BlockState.
        else if (feature.variantMap().isEmpty() && !feature.stateOverride().isEmpty()) {
            ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(feature.getModid() + ":" + feature.stateOverride()));
        }
        //Register variants with optional state override.
        else if (!feature.variantMap().isEmpty()) {
            String location = feature.stateOverride().isEmpty() ? feature.getName() : feature.stateOverride();
            feature.variantMap().keySet().forEach(meta -> ModelLoader.setCustomModelResourceLocation(item, meta, new ModelResourceLocation(feature.getModid() + ":" + location, feature.variantMap().get(meta))));
        }
    }

    //endregion

    //region Feature Helpers

    /**
     * Returns true if this object has been registered with ModFeatureParser
     */
    public static boolean isFeature(Object object) {
        return featureStates.containsKey(object);
    }

    /**
     * Returns true if feature is enabled. Applies to all mods using a ModFeatureParser instance
     */
    public static boolean isEnabled(Object feature) {
        return featureStates.getOrDefault(feature, true);
    }

    /**
     * Iterates over every loaded feature and applies the given consumer
     * to all disabled features that are not disabled via hardDisableMode.
     */
    public static void getFeaturesToHide(Consumer<ItemStack> consumer) {
        DataUtils.forEachMatch(allModFeatures.keySet(), fe -> (!fe.isActive() && !hardDisableModeMap.containsKey(fe.getModid())), fe -> {
            ItemStack stack;
            Object feature = allModFeatures.get(fe);

            if (feature instanceof Item) {
                stack = new ItemStack((Item) feature);
            }
            else if (feature instanceof Block) {
                stack = new ItemStack((Block) feature);
            }
            else {
                throw new RuntimeException("WTF is this? " + feature + " Only Items and Blocks are valid features m8");
            }

            consumer.accept(stack);
        });
    }

    //endregion
}
