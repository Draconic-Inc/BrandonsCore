package com.brandon3055.brandonscore.common.config;

import com.brandon3055.brandonscore.common.utills.LogHelper;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.registry.GameRegistry;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by brandon3055 on 18/3/2016.
 * Features Refer to Blocks and Items.
 * This class is responsible for storing a list of all blocks and items in the mod.
 * As well as handling the config for each block and item.
 */
public class ModFeatureParser {

	private String modid;
	private CreativeTabs[] modTabs;
	private static final String CATEGORY_BLOCKS = "Blocks";
	private static final String CATEGORY_ITEMS = "Items";

	private static Map<Object, Boolean> featureStates = new HashMap<Object, Boolean>();
	private static List<FeatureEntry> featureEntries = new ArrayList<FeatureEntry>();

	public ModFeatureParser(String modid, CreativeTabs[] modTabs){
		this.modid = modid;
		this.modTabs = modTabs;
	}

	public void loadFeatures(Class<? extends FeatureCollection> collection){
		for (Field field : collection.getFields()){
			if (field.isAnnotationPresent(Feature.class)){
				try {
					featureEntries.add(new FeatureEntry(field.get(null), field.getAnnotation(Feature.class)));
				}
				catch (IllegalAccessException e) {
					LogHelper.error("Error Loading Feature!!! ["+field.getAnnotation(Feature.class).name()+"]");
					e.printStackTrace();
				}
			}
		}
	}

	public void loadFeatureConfig(Configuration configuration){
		try {
			for (FeatureEntry entry : featureEntries){
				if (!entry.feature.isConfigurable()) {
					featureStates.put(entry.featureObj, true);
					continue;
				}

				String category = entry.featureObj instanceof Block ? CATEGORY_BLOCKS : CATEGORY_ITEMS;

				entry.enabled = configuration.get(category, entry.feature.name(), entry.feature.isActive()).getBoolean(entry.feature.isActive());
				featureStates.put(entry.featureObj, entry.enabled);
			}

		} catch (Exception var4) {
			LogHelper.error("Error Loading Block/Item Config");
			var4.printStackTrace();
		} finally {
			if(configuration.hasChanged()) configuration.save();
		}
	}

	public void registerFeatures(){
		for (FeatureEntry entry : featureEntries){
			if (!entry.enabled) continue;

			if (entry.featureObj instanceof ICustomRegestry){
				((ICustomRegestry)entry.featureObj).registerFeature(entry.feature);
				continue;
			}

			if (entry.featureObj instanceof Block){
				Block block = (Block) entry.featureObj;
				block.setRegistryName(modid, entry.feature.name());
				block.setUnlocalizedName(modid.toLowerCase() + ":" + entry.feature.name());

				if (entry.feature.cTab() >= 0 && entry.feature.cTab() < modTabs.length) {
					block.setCreativeTab(modTabs[entry.feature.cTab()]);
				}

				if (entry.feature.hasCustomItemBlock()) {
					GameRegistry.registerBlock(block, entry.feature.getItemBlock(), new FeatureWrapper(entry.feature));
				}

				else {
					GameRegistry.registerBlock(block);
				}
			}
			else if (entry.featureObj instanceof Item){
				Item item = (Item) entry.featureObj;
				item.setRegistryName(modid, entry.feature.name());
				item.setUnlocalizedName(modid.toLowerCase() + ":" + entry.feature.name());

				if (entry.feature.cTab() >= 0 && entry.feature.cTab() < modTabs.length) {
					item.setCreativeTab(modTabs[entry.feature.cTab()]);
				}

				GameRegistry.registerItem(item);
			}
		}
	}

	public void registerRendering(){
		for (FeatureEntry entry : featureEntries){
			if (!entry.enabled) continue;

			if (entry.featureObj instanceof ICustomRender){
				((ICustomRender)entry.featureObj).registerRenderer(entry.feature);
				continue;
			}

			if (entry.featureObj instanceof Block){
				Block block = (Block) entry.featureObj;
				if (entry.feature.variantMap().length > 0){
					registerVariants(Item.getItemFromBlock(block), entry.feature);
				}
				else ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(block), 0, new ModelResourceLocation(modid.toLowerCase() + ":" + entry.feature.name()));
			}
			else if (entry.featureObj instanceof Item){
				Item item = (Item) entry.featureObj;
				if (entry.feature.variantMap().length > 0){
					registerVariants(item, entry.feature);
				}
				else ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(modid.toLowerCase() + ":" + entry.feature.name()));
			}
		}
	}

	private void registerVariants(Item item, Feature feature){
		for (String s : feature.variantMap()){
			int meta = Integer.parseInt(s.substring(0, s.indexOf(":")));
			String fullName = modid.toLowerCase() + ":" + feature.name();
			String variant = s.substring(s.indexOf(":") + 1);
			ModelLoader.setCustomModelResourceLocation(item, meta, new ModelResourceLocation(fullName, variant));
		}
	}

	private static class FeatureEntry {

		private final Object featureObj;
		private final Feature feature;
		public boolean enabled;

		private FeatureEntry(Object featureObj, Feature feature){

			this.featureObj = featureObj;
			this.feature = feature;
			this.enabled = feature.isActive();
		}
	}

	private static class MeshDefinitionAllMeta implements ItemMeshDefinition {

		private String name;

		public MeshDefinitionAllMeta(String name) {
			this.name = name;
		}

		@Override
		public ModelResourceLocation getModelLocation(ItemStack stack) {
			return new ModelResourceLocation(name);
		}
	}
}
