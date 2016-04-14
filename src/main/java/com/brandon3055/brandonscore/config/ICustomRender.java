package com.brandon3055.brandonscore.config;

/**
 * Created  by brandon3055 on 21/3/2016.
 */
public interface ICustomRender {
    /**
     * Use this to register a custom renderer for the block.
     * */
	public void registerRenderer(Feature feature);

    /**
     * Return true if the normal json model should still be registered for the item
     * */
    public boolean registerNormal(Feature feature);
}
