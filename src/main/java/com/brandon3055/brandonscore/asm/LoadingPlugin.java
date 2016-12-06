package com.brandon3055.brandonscore.asm;

import com.brandon3055.brandonscore.BrandonsCore;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

import java.util.Map;

/**
 * Created by brandon3055 on 27/5/2015.
 */
@IFMLLoadingPlugin.Name(value = BrandonsCore.MODNAME)
@IFMLLoadingPlugin.MCVersion(value = "1.10.2")
@IFMLLoadingPlugin.TransformerExclusions(value = "com.brandon3055.brandonscore.asm.")
//@IFMLLoadingPlugin.SortingIndex(value = 1)
public class LoadingPlugin implements IFMLLoadingPlugin {

	@Override
	public String[] getASMTransformerClass() {
		return new String[] {ClassTransformer.class.getName()};
	}

	@Override
	public String getModContainerClass() {
		return null;
	}

	@Override
	public String getSetupClass() {
		return null;
	}

	@Override
	public void injectData(Map<String, Object> data) {}

	@Override
	public String getAccessTransformerClass() {
		return null;
	}
}
