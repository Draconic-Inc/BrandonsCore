package com.brandon3055.brandonscore.client.shader;

import codechicken.lib.render.shader.CCUniform;
import com.brandon3055.brandonscore.BrandonsCore;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;

/**
 * Created by brandon3055 on 20/11/2022
 */
public class ContribShader extends BCShader<ContribShader> {

    private CCUniform uv1OverrideUniform;
    private CCUniform uv2OverrideUniform;

    private CCUniform baseColorUniform;

    public ContribShader(String path, VertexFormat format) {
        super(new ResourceLocation(BrandonsCore.MODID, path), format);
    }

    public ContribShader(ResourceLocation location, VertexFormat format) {
        super(location, format);
    }

    // @formatter:off
    public final CCUniform getUv1OverrideUniform() { return Objects.requireNonNull(uv1OverrideUniform, missingUniformMessage("UV1Override")); }
    public final boolean hasUv1OverrideUniform() { return uv1OverrideUniform != null; }
    public final CCUniform getUv2OverrideUniform() { return Objects.requireNonNull(uv2OverrideUniform, missingUniformMessage("UV2Override")); }
    public final boolean hasUv2OverrideUniform() { return uv2OverrideUniform != null; }
    public final CCUniform getBaseColorUniform() { return Objects.requireNonNull(baseColorUniform, missingUniformMessage("BaseColor")); }
    public final boolean hasBaseColorUniform() { return baseColorUniform != null; }
    // @formatter:on

    @Override
    protected void onShaderLoaded() {
        super.onShaderLoaded();
        uv1OverrideUniform = shaderInstance.getUniform("UV1Override");
        uv2OverrideUniform = shaderInstance.getUniform("UV2Override");

        baseColorUniform = shaderInstance.getUniform("BaseColor");
    }
}