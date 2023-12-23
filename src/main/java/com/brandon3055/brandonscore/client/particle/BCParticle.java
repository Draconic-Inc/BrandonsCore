package com.brandon3055.brandonscore.client.particle;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

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

    public BCParticle(ClientLevel worldIn, Vec3 pos) {
        super(worldIn, pos.x, pos.y, pos.z);
    }

    public BCParticle(ClientLevel worldIn, Vec3 pos, Vec3 speed) {
        super(worldIn, pos.x, pos.y, pos.z, speed.x, speed.y, speed.z);
    }

//    /**
//     * Valid Range range 0-3
//     */
//    @Override
//    public int getFXLayer() {
//        return super.getFXLayer();
//    }

    public IGLFXHandler getFXHandler() {
        return DEFAULT_FX_HANDLER;
    }

    /**
     * This is to ensure particles are spawned using the correct methods because raw gl particles are handled very differently<br>
     * and attempting to render them with the default pipeline will break things.<br><br>
     * <p>
     * Raw gl particles are pretty much what they sound like. The renderer wont bind a texture or start a draw call before rendering them.<br>
     * So you can do whatever you like!<br><br>
     * <p>
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
        super.setLifetime(age + random.nextInt(randAdditive));
        return this;
    }

    public BCParticle setGravity(double gravity) {
        this.gravity = (float) gravity;
        return this;
    }

    public BCParticle setAirResistance(float airResistance) {
        this.airResistance = airResistance;
        return this;
    }

    public BCParticle setSizeAndRandMotion(double scale, double xMotion, double yMotion, double zMotion) {
        baseScale = (float) scale;

        this.xd = (-0.5 + random.nextDouble()) * xMotion;
        this.yd = (-0.5 + random.nextDouble()) * yMotion;
        this.zd = (-0.5 + random.nextDouble()) * zMotion;

        return this;
    }

    @Override
    public Vec3 getPos() {
        return new Vec3(x, y, z);
    }

    public Level getWorld() {
        return level;
    }

    public BCParticle setPosition(Vec3 pos) {
        setPos(pos.x, pos.y, pos.z);
        return this;
    }

    public void moveEntityNoClip(double x, double y, double z) {
        this.setBoundingBox(this.getBoundingBox().move(0.0D, y, 0.0D));
        this.setBoundingBox(this.getBoundingBox().move(x, 0.0D, 0.0D));
        this.setBoundingBox(this.getBoundingBox().move(0.0D, 0.0D, z));
        this.setLocationFromBoundingbox();
    }

    @Override
    public void render(VertexConsumer buffer, Camera renderInfo, float partialTicks) {

    }

    @Override
    public ParticleRenderType getRenderType() {
        return null;
    }

    private static IGLFXHandler DEFAULT_FX_HANDLER = new IGLFXHandler() {
        @Override
        public void preDraw(int layer, BufferBuilder vertexbuffer, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {

        }

        @Override
        public void postDraw(int layer, BufferBuilder vertexbuffer, Tesselator tessellator) {

        }
    };
}