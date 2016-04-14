package com.brandon3055.brandonscore.utills;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3i;
import net.minecraftforge.client.model.obj.OBJModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by brandon3055 on 9/4/2016.
 * Used for general rendering stuff
 */
public class ModelUtills {
    private static Map<IBlockState, List<BakedQuad>> quadCache = new HashMap<IBlockState, List<BakedQuad>>();

    public static List<BakedQuad> getModelQuads(IBlockState state){
        if (!quadCache.containsKey(state)){
            IBakedModel blockModel = Minecraft.getMinecraft().getBlockRendererDispatcher().getModelForState(state);
            List<BakedQuad> listQuads = new ArrayList<BakedQuad>();

            if (blockModel instanceof OBJModel.OBJBakedModel){
                listQuads.addAll(blockModel.getQuads(state, null, 0));
            }
            else {
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

    public static void renderQuads(Tessellator tessellator, List<BakedQuad> listQuads){
        VertexBuffer vertexbuffer = tessellator.getBuffer();
        int i = 0;
        for (int j = listQuads.size(); i < j; ++i) {
            BakedQuad bakedquad = (BakedQuad) listQuads.get(i);
            vertexbuffer.begin(7, DefaultVertexFormats.ITEM);
            vertexbuffer.addVertexData(bakedquad.getVertexData());

            //vertexbuffer.putColor4(0x55FFFFFF);
            vertexbuffer.putColorRGB_F4(1, 1, 1);

            Vec3i vec3i = bakedquad.getFace().getDirectionVec();
            vertexbuffer.putNormal((float) vec3i.getX(), (float) vec3i.getY(), (float) vec3i.getZ());
            tessellator.draw();
        }
    }

    public static void renderQuadsRGB(Tessellator tessellator, List<BakedQuad> listQuads, float r, float g, float b){
        VertexBuffer vertexbuffer = tessellator.getBuffer();
        int i = 0;
        for (int j = listQuads.size(); i < j; ++i) {
            BakedQuad bakedquad = (BakedQuad) listQuads.get(i);
            vertexbuffer.begin(7, DefaultVertexFormats.ITEM);
            vertexbuffer.addVertexData(bakedquad.getVertexData());

            //vertexbuffer.putColor4(0x55FFFFFF);
            vertexbuffer.putColorRGB_F4(r, g, b);

            Vec3i vec3i = bakedquad.getFace().getDirectionVec();
            vertexbuffer.putNormal((float) vec3i.getX(), (float) vec3i.getY(), (float) vec3i.getZ());
            tessellator.draw();
        }
    }

    public static void renderQuadsARGB(Tessellator tessellator, List<BakedQuad> listQuads, int ARGB_Hex){
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
}
