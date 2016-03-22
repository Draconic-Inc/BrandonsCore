package com.brandon3055.brandonscore.common.config;

import net.minecraft.item.ItemBlock;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by brandon3055 on 21/3/2016.
 */
public class FeatureWrapper {
	private final String name;
	private final boolean isActive;
	private final boolean isConfigurable;
	private final String[] variantMap;
	private final boolean hasCustomItemBlock;
	private final Class<? extends ItemBlock> itemBlockClass;

	public final Map<Integer, String> nameMap = new HashMap<Integer, String>();

	public FeatureWrapper(Feature feature){
		this.name = feature.name();
		this.isActive = feature.isActive();
		this.isConfigurable = feature.isConfigurable();
		this.variantMap = feature.variantMap();
		this.hasCustomItemBlock = feature.hasCustomItemBlock();
		this.itemBlockClass = feature.getItemBlock();

		if (feature.variantMap().length > 0) {
			for (String s : feature.variantMap()) {
				int meta = Integer.parseInt(s.substring(0, s.indexOf(":")));
				nameMap.put(meta, s.substring(s.indexOf(":") + 1));
			}
		}
	}

	public String name() {return name;}

	public boolean isActive() {return isActive;}

	public boolean isConfigurable() {return isConfigurable;}

	public String[] variantMap() {return variantMap;}

	public boolean hasCustomItemBlock() {return hasCustomItemBlock;}

	public Class<? extends ItemBlock> getItemBlock() {return itemBlockClass;}

}
