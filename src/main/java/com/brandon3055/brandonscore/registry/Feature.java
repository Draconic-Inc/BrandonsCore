//package com.brandon3055.brandonscore.registry;
//
//import com.brandon3055.brandonscore.BrandonsCore;
//import com.brandon3055.brandonscore.utils.DataUtils;
//import net.minecraft.item.BlockItem;
//import net.minecraft.tileentity.TileEntity;
//import net.minecraft.util.ResourceLocation;
//
//import java.util.HashMap;
//import java.util.Map;
//
///**
// * Created by brandon3055 on 21/3/2016.
// * Stores all registry information about a mod feature (item or block)
// * once it has been extracted from the {@link ModFeature} annotation.
// */
//public class Feature {
//
//    private final String modid;
//    private final String name;
//    private final ResourceLocation registryName;
//    private final boolean canBeDisabled;
//    private boolean isActive;
//    private final boolean hasItemBlock;
//    private final Class<? extends BlockItem> itemBlockClass;
//    private final Class<? extends TileEntity> tileClass;
//    private final int creativeTab;
//    private final String stateOverride;
//    public final String[] rawVariantMap;
//    private final Map<Integer, String> variantMap = new HashMap<Integer, String>();
//
//    private Feature(String modid, ModFeature modFeature) {
//        this.modid = modid;
//        this.name = modFeature.name();
//        this.registryName = new ResourceLocation(modid, name);
//        this.canBeDisabled = modFeature.canBeDisabled();
//        this.isActive = modFeature.isActive();
//        this.creativeTab = modFeature.cTab();
//        this.tileClass = modFeature.tileEntity();
//        this.itemBlockClass = modFeature.itemBlock();
//        this.stateOverride = modFeature.stateOverride();
//        this.rawVariantMap = modFeature.variantMap();
//        this.hasItemBlock = modFeature.hasItemBlock();
//
//        DataUtils.forEach(rawVariantMap, s -> variantMap.put(Integer.parseInt(s.substring(0, s.indexOf(":"))), s.substring(s.indexOf(":") + 1)));
//    }
//
//    /**
//     * @return The mod id of the mod this feature belongs to.
//     */
//    public String getModid() {
//        return modid;
//    }
//
//    /**
//     * @return The registry name for the feature.
//     */
//    public String getName() {
//        return name;
//    }
//
//    /**
//     * @return The fill registry resource location for this feature.
//     */
//    public ResourceLocation getRegistryName() {
//        return registryName;
//    }
//
//    /**
//     * If this feature has been disabled via the config or is disabled by
//     * default and has not been enabled via the config this will return false.
//     *
//     * @return whether or not this feature is active.
//     */
//    public boolean isActive() {
//        return isActive;
//    }
//
//    /**
//     * Sets the feature active state. This should ONLY be called during pre init and
//     * ONLY by BrandonsCore!
//     */
//    public void setActive(boolean active) {
//        isActive = active;
//
//        boolean isBC = Loader.instance().activeModContainer() != null && Loader.instance().activeModContainer().matches(BrandonsCore.instance);
//        if (!isBC || Loader.instance().getLoaderState() != LoaderState.PREINITIALIZATION) {
//            throw new RuntimeException("Attempted to call Feature.setActive by another mod! This method should only be called by BrandonsCore during ore init! No touchy!");
//        }
//    }
//
//    /**
//     * @return true to generate a config option to disable this feature.
//     */
//    public boolean canBeDisabled() {
//        return canBeDisabled;
//    }
//
//    /**
//     * Use this to map item metadata to BlockState variants.
//     * Used for both Items and ItemBlocks.<br>
//     * Example variant map:<br>
//     * {"0:type=normal", "1:type=nether", "2:type=end"}
//     */
//    public Map<Integer, String> variantMap() {
//        return variantMap;
//    }
//
//    /**
//     * "As is BlockState override"
//     * This can be used in 2 ways. The first is to supply a fully qualified model resource location<br>
//     * e.g. armor#type=wyvernHelm<br>
//     * The other option is to just specify the model location without the variant and use variantMap to map the variants.<br>
//     * e.g. armor <br>
//     * then variantMap = {"0:type=wyvernHelm", "1:type=wyvernChest", ...etc}<br><br>
//     * Used for both Items and ItemBlocks.
//     */
//    public String stateOverride() {
//        return stateOverride;
//    }
//
//    /**
//     * Allows you to prevent the registration of an BlockItem.
//     * Used For Blocks.
//     */
//    public boolean hasItemBlock() {
//        return hasItemBlock;
//    }
//
//    /**
//     * Allows you to override the BlockItem used if this feature is a block.<br><br>
//     * Used for Blocks.
//     */
//    public Class<? extends BlockItem> itemBlock() {
//        return itemBlockClass;
//    }
//
//    /**
//     * Used to register a tile entity for this block if it has one.
//     * If you need to register more than 1 tile then implement {@link IRegistryOverride}
//     * on your block and register them manually.<br><br>
//     * Used For Blocks
//     */
//    public Class<? extends TileEntity> tileEntity() {
//        return tileClass;
//    }
//
//    /**
//     * Adds this feature to one of the creative tabs defined in @ModFeatureContainer
//     * set to -1 to exclude from creative tabs.<br><br>
//     * Used for both Items and Blocks.
//     */
//    public int creativeTab() {
//        return creativeTab;
//    }
//
//
//    /**
//     * Extracts all data from an @ModFeature annotation into a new Feature object.
//     * @param modid The mod id of the mod this feature belongs to.
//     * @param modFeature The feature annotation to extract data from.
//     * @return a new Feature object containing all data extracted from feature annotation.
//     */
//    public static Feature extract(String modid, ModFeature modFeature) {
//        return new Feature(modid, modFeature);
//    }
//}
