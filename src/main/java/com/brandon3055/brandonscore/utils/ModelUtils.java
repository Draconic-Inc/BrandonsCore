package com.brandon3055.brandonscore.utils;

import com.google.common.base.Function;
import jline.internal.Nullable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3i;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.obj.OBJModel;
import net.minecraftforge.common.model.TRSRTransformation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by brandon3055 on 9/4/2016.
 * Used for general rendering stuff
 */
public class ModelUtils implements IResourceManagerReloadListener {
    public static Map<IBlockState, List<BakedQuad>> quadCache = new HashMap<IBlockState, List<BakedQuad>>();
    public static Map<ResourceLocation, IBakedModel> bakedModelCache = new HashMap<ResourceLocation, IBakedModel>();

    public static List<BakedQuad> getModelQuads(IBlockState state) {
        if (!quadCache.containsKey(state)) {
            IBakedModel blockModel = Minecraft.getMinecraft().getBlockRendererDispatcher().getModelForState(state);
            List<BakedQuad> listQuads = new ArrayList<BakedQuad>();

            if (blockModel instanceof OBJModel.OBJBakedModel) {
                listQuads.addAll(blockModel.getQuads(state, null, 0));
            } else {
                for (EnumFacing face : EnumFacing.VALUES) {
                    listQuads.addAll(blockModel.getQuads(state, face, 0));
                }
            }
            quadCache.put(state, listQuads);
        }

        if (quadCache.containsKey(state)) {
            return quadCache.get(state);
        }

        return new ArrayList<BakedQuad>();
    }

    public static void renderQuads(List<BakedQuad> listQuads) {
        Tessellator tessellator = Tessellator.getInstance();
        VertexBuffer vertexbuffer = tessellator.getBuffer();
        int i = 0;
        vertexbuffer.begin(7, DefaultVertexFormats.ITEM);
        for (int j = listQuads.size(); i < j; ++i) {
            BakedQuad bakedquad = listQuads.get(i);

            vertexbuffer.addVertexData(bakedquad.getVertexData());

            vertexbuffer.putColorRGB_F4(1, 1, 1);

            Vec3i vec3i = bakedquad.getFace().getDirectionVec();
            vertexbuffer.putNormal((float) vec3i.getX(), (float) vec3i.getY(), (float) vec3i.getZ());

        }
        tessellator.draw();
    }

    public static void renderQuadsRGB(List<BakedQuad> listQuads, float r, float g, float b) {
        Tessellator tessellator = Tessellator.getInstance();
        VertexBuffer vertexbuffer = tessellator.getBuffer();
        int i = 0;
        for (int j = listQuads.size(); i < j; ++i) {
            BakedQuad bakedquad = (BakedQuad) listQuads.get(i);
            vertexbuffer.begin(7, DefaultVertexFormats.ITEM);
            vertexbuffer.addVertexData(bakedquad.getVertexData());

            vertexbuffer.putColorRGB_F4(r, g, b);

            Vec3i vec3i = bakedquad.getFace().getDirectionVec();
            vertexbuffer.putNormal((float) vec3i.getX(), (float) vec3i.getY(), (float) vec3i.getZ());
            tessellator.draw();
        }
    }

    public static void renderQuadsARGB(List<BakedQuad> listQuads, int ARGB_Hex) {
        Tessellator tessellator = Tessellator.getInstance();
        VertexBuffer vertexbuffer = tessellator.getBuffer();
        int i = 0;
        for (int j = listQuads.size(); i < j; ++i) {
            BakedQuad bakedquad = (BakedQuad) listQuads.get(i);
            vertexbuffer.begin(7, DefaultVertexFormats.ITEM);
            vertexbuffer.addVertexData(bakedquad.getVertexData());

            vertexbuffer.putColor4(ARGB_Hex);

            Vec3i vec3i = bakedquad.getFace().getDirectionVec();
            vertexbuffer.putNormal((float) vec3i.getX(), (float) vec3i.getY(), (float) vec3i.getZ());
            tessellator.draw();
        }
    }

    public static IBakedModel loadBakedModel(ResourceLocation modelLocation) {
        if (!bakedModelCache.containsKey(modelLocation)) {
            try {
                IModel model = ModelLoaderRegistry.getModel(modelLocation);
                IBakedModel bakedModel = model.bake(TRSRTransformation.identity(), DefaultVertexFormats.ITEM, new Function<ResourceLocation, TextureAtlasSprite>() {
                    @Nullable
                    @Override
                    public TextureAtlasSprite apply(ResourceLocation input) {
                        return Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(input.toString());
                    }
                });

                bakedModelCache.put(modelLocation, bakedModel);
            }
            catch (Exception e) {
                LogHelperBC.fatalErrorMessage("Error at ModelUtils.loadBakedModel, Resource: " + modelLocation.toString());
                throw new RuntimeException(e);
            }
        }

        return bakedModelCache.get(modelLocation);
    }

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager) {
        quadCache.clear();
        bakedModelCache.clear();
    }
}


