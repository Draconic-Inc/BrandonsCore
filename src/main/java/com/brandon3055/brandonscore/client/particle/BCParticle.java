package com.brandon3055.brandonscore.client.particle;

import com.brandon3055.brandonscore.lib.Vec3D;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.particle.IParticleRenderType;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;

/**
 * Created by brandon3055 on 2/5/2016.
 * I wanted this for a thing... I dont actually remember what it is now but...
 * Im just going to add it so when i remember the thing it is here!
 */
@Deprecated //I dont need this anymore
public class BCParticle extends Particle {
    protected float texturesPerRow = 16F;
    protected float airResistance = 0;
    protected float baseScale = 1;

    public BCParticle(ClientWorld worldIn, Vec3D pos) {
        super(worldIn, pos.x, pos.y, pos.z);
    }

    public BCParticle(ClientWorld worldIn, Vec3D pos, Vec3D speed) {
        super(worldIn, pos.x, pos.y, pos.z, speed.x, speed.y, speed.z);
    }

//    /**
//     * Valid Range range 0-3
//     */
//    @Override
//    public int getFXLayer() {
//        return super.getFXLayer();
//    }

    public IGLFXHandler getFXHandler(){
        return DEFAULT_FX_HANDLER;
    }

    /**
     * This is to ensure particles are spawned using the correct methods because raw gl particles are handled very differently<br>
     * and attempting to render them with the default pipeline will break things.<br><br>
     *
     * Raw gl particles are pretty much what they sound like. The renderer wont bind a texture or start a draw call before rendering them.<br>
     * So you can do whatever you like!<br><br>
     *
     * Raw gl particles are rendered with blend enabled. depthMask disabled and GL_GREATER set to 0.<br>
     * Be sure to leave the render call in this state when you are done!
     *
     * @return true if this particle needs raw gl access.
     */
    public boolean isRawGLParticle() {
        return false;
    }


    public BCParticle setColour(float red, float green, float blue) {
        super.setColor(red, green, blue);
        return this;
    }

    public BCParticle setMaxAge(int age, int randAdditive) {
        super.setMaxAge(age + rand.nextInt(randAdditive));
        return this;
    }

    public BCParticle setGravity(double gravity) {
        this.particleGravity = (float) gravity;
        return this;
    }

    public BCParticle setAirResistance(float airResistance) {
        this.airResistance = airResistance;
        return this;
    }

    public BCParticle setSizeAndRandMotion(double scale, double xMotion, double yMotion, double zMotion) {
        baseScale = (float) scale;

        this.motionX = (-0.5 + rand.nextDouble()) * xMotion;
        this.motionY = (-0.5 + rand.nextDouble()) * yMotion;
        this.motionZ = (-0.5 + rand.nextDouble()) * zMotion;

        return this;
    }

    public Vec3D getPos() {
        return new Vec3D(posX, posY, posZ);
    }

    public World getWorld() {
        return world;
    }

    public BCParticle setPosition(Vec3D pos) {
        setPosition(pos.x, pos.y, pos.z);
        return this;
    }

    public void moveEntityNoClip(double x, double y, double z) {
        this.setBoundingBox(this.getBoundingBox().offset(0.0D, y, 0.0D));
        this.setBoundingBox(this.getBoundingBox().offset(x, 0.0D, 0.0D));
        this.setBoundingBox(this.getBoundingBox().offset(0.0D, 0.0D, z));
        this.resetPositionToBB();
    }



    @Override
    public void renderParticle(IVertexBuilder buffer, ActiveRenderInfo renderInfo, float partialTicks) {

    }

    @Override
    public IParticleRenderType getRenderType() {
        return null;
    }

    private static IGLFXHandler DEFAULT_FX_HANDLER = new IGLFXHandler() {
        @Override
        public void preDraw(int layer, BufferBuilder vertexbuffer, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {

        }

        @Override
        public void postDraw(int layer, BufferBuilder vertexbuffer, Tessellator tessellator) {

        }
    };
}