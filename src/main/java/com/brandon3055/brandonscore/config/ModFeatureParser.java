package com.brandon3055.brandonscore.config;

import com.brandon3055.brandonscore.blocks.ItemBlockBCore;
import com.brandon3055.brandonscore.blocks.NoItemBlock;
import com.brandon3055.brandonscore.utils.LogHelperBC;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by brandon3055 on 18/3/2016.
 * Features Refer to Blocks and Items.
 * This class is responsible for the registration of all features in a mod including registering rendering.
 * It also generates and reads configs for all features as it registers (Or dose not register) them.
 * And it stores a list of all features registered by every mod using this class for feature registration.
 */
public class ModFeatureParser {

    private String modid;
    private CreativeTabs[] modTabs;
    private static final String CATEGORY_BLOCKS = "Blocks";
    private static final String CATEGORY_ITEMS = "Items";

    private static final Map<Object, Boolean> featureStates = new HashMap<>();
    private static final List<FeatureEntry> allFeatureEntries = new ArrayList<>();
    private final List<FeatureEntry> modFeatureEntries = new ArrayList<>();

    /**
     * @param modid   modid of the mod implementing this instance of ModFeatureParser
     * @param modTabs list of creative tabs that belong to the mod
     */
    public ModFeatureParser(String modid, CreativeTabs[] modTabs) {
        this.modid = modid;
        this.modTabs = modTabs;
    }

    public ModFeatureParser(String modid) {
        this.modid = modid;
        this.modTabs = new CreativeTabs[0];
    }

    /**
     * @param collection A class containing all of the mods "Features" Look at Draconic Evolution for an example implementation
     */
    public void loadFeatures(Class collection) {
        for (Field field : collection.getFields()) {
            if (field.isAnnotationPresent(Feature.class)) {
                try {
                    allFeatureEntries.add(new FeatureEntry(field.get(null), field.getAnnotation(Feature.class)));
                    modFeatureEntries.add(new FeatureEntry(field.get(null), field.getAnnotation(Feature.class)));
                }
                catch (IllegalAccessException e) {
                    LogHelperBC.error("Error Loading Feature!!! [" + field.getAnnotation(Feature.class).registryName() + "]");
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Generates or reads the config setting for every loaded feature.
     * Must be called AFTER loadFeatures
     *
     * @param configuration the mods configuration
     */
    public void loadFeatureConfig(Configuration configuration) {
        try {
            for (FeatureEntry entry : modFeatureEntries) {
                if (!entry.feature.isConfigurable()) {
                    featureStates.put(entry.featureObj, true);
                    continue;
                }

                String category = entry.featureObj instanceof Block ? CATEGORY_BLOCKS : CATEGORY_ITEMS;

                entry.enabled = configuration.get(category, entry.feature.registryName(), entry.feature.isActive()).setRequiresMcRestart(true).getBoolean(entry.feature.isActive());
                featureStates.put(entry.featureObj, entry.enabled);
            }
        }
        catch (Exception var4) {
            LogHelperBC.error("Error Loading Block/Item Config");
            var4.printStackTrace();
        }
        finally {
            if (configuration.hasChanged()) {
                configuration.save();
            }
        }
    }

    /**
     * Registers all features that are not disabled via the config.
     * Must be called AFTER loadFeatureConfig
     */
    public void registerFeatures() {
        for (FeatureEntry entry : modFeatureEntries) {
            if (!entry.enabled) continue;

            if (entry.featureObj instanceof ICustomRegistry) {
                ((ICustomRegistry) entry.featureObj).registerFeature(entry.feature);
                continue;
            }

            if (entry.featureObj instanceof Block) {
                Block block = (Block) entry.featureObj;
                block.setRegistryName(entry.feature.registryName());
                block.setUnlocalizedName(modid.toLowerCase() + ":" + entry.feature.registryName());

                if (entry.feature.cTab() >= 0 && entry.feature.cTab() < modTabs.length) {
                    block.setCreativeTab(modTabs[entry.feature.cTab()]);
                }

                //Note to self: this is the same as object instance of ItemBlockBCore (Its reversed)
                if (ItemBlockBCore.class.isAssignableFrom(entry.feature.itemBlock())) {
                    GameRegistry.register(block);

                    try {
                        Constructor<? extends ItemBlock> constructor = entry.feature.itemBlock().getConstructor(Block.class, FeatureWrapper.class);
                        ItemBlock itemBlock = constructor.newInstance(block, new FeatureWrapper(entry.feature));
                        itemBlock.setRegistryName(block.getRegistryName());
                        GameRegistry.register(itemBlock);
                    }
                    catch (Exception e) {
                        LogHelperBC.error("NOOOOOOOO!!!!!!!!!!!...... It broke... [%s]", entry.feature.registryName());
                        e.printStackTrace();
                    }

                }
                else if (NoItemBlock.class.isAssignableFrom(entry.feature.itemBlock())) {
                    //Do not register an item block
                }
                else {
                    GameRegistry.register(block);
                    GameRegistry.register(new ItemBlock(block).setRegistryName(block.getRegistryName()));
                }

                if (block.hasTileEntity(block.getDefaultState())) {
                    if (block instanceof ITileRegisterer) {
                        ((ITileRegisterer) block).registerTiles(modid.toLowerCase() + ":", entry.feature.registryName());
                    }
                    else {
                        GameRegistry.registerTileEntity(entry.feature.tileEntity(), modid.toLowerCase() + ":" + entry.feature.registryName());
                    }
                }
            }
            else if (entry.featureObj instanceof Item) {
                Item item = (Item) entry.featureObj;
                item.setRegistryName(entry.feature.registryName());
                item.setUnlocalizedName(modid.toLowerCase() + ":" + entry.feature.registryName());

                if (entry.feature.cTab() >= 0 && entry.feature.cTab() < modTabs.length) {
                    item.setCreativeTab(modTabs[entry.feature.cTab()]);
                }

                GameRegistry.register(item);
            }
        }
    }

    /**
     * Registers the rendering for all loaded and enabled features.
     * Must be called AFTER registerFeatures, during Pre Initialization and from your Client Proxy
     */
    @SideOnly(Side.CLIENT)
    public void registerRendering() {
        for (FeatureEntry entry : modFeatureEntries) {
            if (!entry.enabled) continue;

            if (entry.featureObj instanceof ICustomRender) {
                ICustomRender customRender = (ICustomRender) entry.featureObj;
                customRender.registerRenderer(entry.feature);

                if (!customRender.registerNormal(entry.feature)) {
                    continue;
                }
            }

            if (entry.featureObj instanceof Block) {
                Block block = (Block) entry.featureObj;

                if (entry.feature.variantMap().length > 0) {
                    registerVariants(Item.getItemFromBlock(block), entry.feature);
                }
                else {
                    ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(block), 0, new ModelResourceLocation(modid.toLowerCase() + ":" + entry.feature.registryName()));
                }
            }
            else if (entry.featureObj instanceof Item) {
                Item item = (Item) entry.featureObj;

                if (!entry.feature.stateOverride().isEmpty()) {
                    String s = entry.feature.stateOverride().substring(0, entry.feature.stateOverride().indexOf("#"));
                    if (entry.feature.variantMap().length > 0) {
                        registerOverrideVariants(item, entry.feature, s);
                    }
                    else {
                        s += entry.feature.stateOverride().substring(entry.feature.stateOverride().indexOf("#")).toLowerCase();
                        ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(modid.toLowerCase() + ":" + s));
                    }
                }
                else if (entry.feature.variantMap().length > 0) {
                    registerVariants(item, entry.feature);
                }
                else {
                    ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(modid.toLowerCase() + ":" + entry.feature.registryName()));
                }
            }
        }
    }

    private void registerOverrideVariants(Item item, Feature feature, String override) {
        for (String s : feature.variantMap()) {
            int meta = Integer.parseInt(s.substring(0, s.indexOf(":")));
            String fullName = modid.toLowerCase() + ":" + override;
            String variant = s.substring(s.indexOf(":") + 1);
            ModelLoader.setCustomModelResourceLocation(item, meta, new ModelResourceLocation(fullName, variant));
        }
    }

    private void registerVariants(Item item, Feature feature) {
        for (String s : feature.variantMap()) {
            int meta = Integer.parseInt(s.substring(0, s.indexOf(":")));
            String fullName = modid.toLowerCase() + ":" + feature.registryName();
            String variant = s.substring(s.indexOf(":") + 1);
            ModelLoader.setCustomModelResourceLocation(item, meta, new ModelResourceLocation(fullName, variant));
        }
    }

    /**
     * Returns true if this object has been registered with a ModFeatureParser
     */
    public static boolean isFeature(Object object) {
        return featureStates.containsKey(object);
    }

    /**
     * Returns true if feature is enabled. Applies to all mods using a ModFeatureParser instance
     */
    public static boolean isEnabled(Object feature) {
        if (!featureStates.containsKey(feature)) {
            return false;
        }
        else {
            return featureStates.get(feature);
        }
    }

    private static class FeatureEntry {

        private final Object featureObj;
        private final Feature feature;
        public boolean enabled;

        private FeatureEntry(Object featureObj, Feature feature) {

            this.featureObj = featureObj;
            this.feature = feature;
            this.enabled = feature.isActive();
        }
    }
}
