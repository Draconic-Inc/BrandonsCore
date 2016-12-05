package com.brandon3055.brandonscore.client.particle;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.entity.Entity;

/**
 * Created by brandon3055 on 30/11/2016.
 */
public interface IGLFXHandler {

    /**
     * Run any pre render gl code here.
     * You can also start drawing quads.
     */
    void preDraw(int layer, VertexBuffer vertexbuffer, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ);

    /**
     * Run any post render gl code here.
     * This is where you would draw if you started drawing in preDraw
     */
    void postDraw(int layer, VertexBuffer vertexbuffer, Tessellator tessellator);
}
