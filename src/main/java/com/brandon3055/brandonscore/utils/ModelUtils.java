package com.brandon3055.brandonscore.utils;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ModelBakery;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.resources.IResourceManager;
import net.minecraft.resources.IResourceManagerReloadListener;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3i;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.obj.OBJModel;

import java.util.*;

/**
 * Created by brandon3055 on 9/4/2016.
 * Used for general rendering stuff
 */
public class ModelUtils implements IResourceManagerReloadListener {
    public static Map<BlockState, List<BakedQuad>> quadCache = new HashMap<BlockState, List<BakedQuad>>();
    public static Map<ResourceLocation, IBakedModel> bakedModelCache = new HashMap<ResourceLocation, IBakedModel>();
    public static Random rand = new Random();

//    public static List<BakedQuad> getModelQuads(BlockState state) {
//        if (!quadCache.containsKey(state)) {
//            IBakedModel blockModel = Minecraft.getInstance().getBlockRendererDispatcher().getModelForState(state);
//            List<BakedQuad> listQuads = new ArrayList<>();
//
//            if (blockModel instanceof OBJModel.ModelObject) {
//                listQuads.addAll(blockModel.getQuads(state, null, rand));
//            } else {
//                for (Direction face : Direction.values()) {
//                    listQuads.addAll(blockModel.getQuads(state, face, rand));
//                }
//            }
//            quadCache.put(state, listQuads);
//        }
//
//        if (quadCache.containsKey(state)) {
//            return quadCache.get(state);
//        }
//
//        return new ArrayList<BakedQuad>();
//    }
//
//    public static void renderQuads(List<BakedQuad> listQuads) {
//        Tessellator tessellator = Tessellator.getInstance();
//        BufferBuilder vertexbuffer = tessellator.getBuffer();
//        int i = 0;
//        vertexbuffer.begin(7, DefaultVertexFormats.BLOCK);
//        for (int j = listQuads.size(); i < j; ++i) {
//            BakedQuad bakedquad = listQuads.get(i);
//
//            vertexbuffer.addVertexData(bakedquad.getVertexData());
//
//            vertexbuffer.color(1F, 1F, 1F, 1F);
//
//            Vec3i vec3i = bakedquad.getFace().getDirectionVec();
//            vertexbuffer.putNormal((float) vec3i.getX(), (float) vec3i.getY(), (float) vec3i.getZ());
//
//        }
//        tessellator.draw();
//    }
//
//    public static void renderQuadsRGB(List<BakedQuad> listQuads, float r, float g, float b) {
//        Tessellator tessellator = Tessellator.getInstance();
//        BufferBuilder vertexbuffer = tessellator.getBuffer();
//        int i = 0;
//        for (int j = listQuads.size(); i < j; ++i) {
//            BakedQuad bakedquad = (BakedQuad) listQuads.get(i);
//            vertexbuffer.begin(7, DefaultVertexFormats.ITEM);
//            vertexbuffer.addVertexData(bakedquad.getVertexData());
//
//            vertexbuffer.putColorRGB_F4(r, g, b);
//
//            Vec3i vec3i = bakedquad.getFace().getDirectionVec();
//            vertexbuffer.putNormal((float) vec3i.getX(), (float) vec3i.getY(), (float) vec3i.getZ());
//            tessellator.draw();
//        }
//    }
//
//    public static void renderQuadsARGB(List<BakedQuad> listQuads, int ARGB_Hex) {
//        Tessellator tessellator = Tessellator.getInstance();
//        BufferBuilder vertexbuffer = tessellator.getBuffer();
//        int i = 0;
//        for (int j = listQuads.size(); i < j; ++i) {
//            BakedQuad bakedquad = (BakedQuad) listQuads.get(i);
//            vertexbuffer.begin(7, DefaultVertexFormats.ITEM);
//            vertexbuffer.addVertexData(bakedquad.getVertexData());
//
//            vertexbuffer.putColor4(ARGB_Hex);
//
//            Vec3i vec3i = bakedquad.getFace().getDirectionVec();
//            vertexbuffer.putNormal((float) vec3i.getX(), (float) vec3i.getY(), (float) vec3i.getZ());
//            tessellator.draw();
//        }
//    }
//
//    public static IBakedModel loadBakedModel(ResourceLocation modelLocation) {
//        if (!bakedModelCache.containsKey(modelLocation)) {
//            try {
//                IModel model = ModelLoaderRegistry.getModel(modelLocation);
////                bake(ModelBakery bakery, Function<ResourceLocation, TextureAtlasSprite > spriteGetter, ISprite sprite, VertexFormat format)
////                IBakedModel bakedModel = model.bake(TRSRTransformation.identity(), DefaultVertexFormats.ITEM, input -> Minecraft.getInstance().getTextureMap().getAtlasSprite(input.toString()));
//                IBakedModel bakedModel = null;//model.bake();TODO Stuff
//                bakedModelCache.put(modelLocation, bakedModel);
//            }
//            catch (Exception e) {
//                LogHelperBC.fatalErrorMessage("Error at ModelUtils.loadBakedModel, Resource: " + modelLocation.toString());
//                throw new RuntimeException(e);
//            }
//        }
//
//        return bakedModelCache.get(modelLocation);
//    }

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager) {
        quadCache.clear();
        bakedModelCache.clear();
    }
}


