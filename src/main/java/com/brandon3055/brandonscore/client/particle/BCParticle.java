package com.brandon3055.brandonscore.client.particle;

import com.brandon3055.brandonscore.lib.Vec3D;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;

/**
 * Created by brandon3055 on 2/5/2016.
 * I wanted this for a thing... I dont actually remember what it is now but...
 * Im just going to add it so when i remember the thing it is here!
 */
public class BCParticle extends Particle {
    protected float texturesPerRow = 16F;
    protected float airResistance = 0;
    protected float baseScale = 1;

    public BCParticle(World worldIn, Vec3D pos) {
        super(worldIn, pos.x, pos.y, pos.z);
    }

    public BCParticle(World worldIn, Vec3D pos, Vec3D speed) {
        super(worldIn, pos.x, pos.y, pos.z, speed.x, speed.y, speed.z);
    }

    /**
     * Valid Range range 0-3
     */
    @Override
    public int getFXLayer() {
        return super.getFXLayer();
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

    public BCParticle setScale(float scale) {
        this.particleScale = scale;
        baseScale = scale;
        return this;
    }

    public BCParticle setColour(float red, float green, float blue) {
        super.setRBGColorF(red, green, blue);
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
        this.particleScale = (float) scale;
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
        return worldObj;
    }

    public BCParticle setPosition(Vec3D pos) {
        setPosition(pos.x, pos.y, pos.z);
        return this;
    }

    public void moveEntityNoClip(double x, double y, double z) {
        this.setEntityBoundingBox(this.getEntityBoundingBox().offset(0.0D, y, 0.0D));
        this.setEntityBoundingBox(this.getEntityBoundingBox().offset(x, 0.0D, 0.0D));
        this.setEntityBoundingBox(this.getEntityBoundingBox().offset(0.0D, 0.0D, z));
        this.resetPositionToBB();
    }

    @Override
    public void renderParticle(VertexBuffer vertexbuffer, Entity entity, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
        float minU = (float) this.particleTextureIndexX / texturesPerRow;
        float maxU = minU + 1F / texturesPerRow;//0.0624375F;
        float minV = (float) this.particleTextureIndexY / texturesPerRow;
        float maxV = minV + 1F / texturesPerRow;//0.0624375F;
        float scale = 0.1F * this.particleScale;

        if (this.particleTexture != null) {
            minU = this.particleTexture.getMinU();
            maxU = this.particleTexture.getMaxU();
            minV = this.particleTexture.getMinV();
            maxV = this.particleTexture.getMaxV();
        }

        float renderX = (float) (this.prevPosX + (this.posX - this.prevPosX) * (double) partialTicks - interpPosX);
        float renderY = (float) (this.prevPosY + (this.posY - this.prevPosY) * (double) partialTicks - interpPosY);
        float renderZ = (float) (this.prevPosZ + (this.posZ - this.prevPosZ) * (double) partialTicks - interpPosZ);
        int brightnessForRender = this.getBrightnessForRender(partialTicks);
        int j = brightnessForRender >> 16 & 65535;
        int k = brightnessForRender & 65535;
        vertexbuffer.pos((double) (renderX - rotationX * scale - rotationXY * scale), (double) (renderY - rotationZ * scale), (double) (renderZ - rotationYZ * scale - rotationXZ * scale)).tex((double) maxU, (double) maxV).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j, k).endVertex();
        vertexbuffer.pos((double) (renderX - rotationX * scale + rotationXY * scale), (double) (renderY + rotationZ * scale), (double) (renderZ - rotationYZ * scale + rotationXZ * scale)).tex((double) maxU, (double) minV).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j, k).endVertex();
        vertexbuffer.pos((double) (renderX + rotationX * scale + rotationXY * scale), (double) (renderY + rotationZ * scale), (double) (renderZ + rotationYZ * scale + rotationXZ * scale)).tex((double) minU, (double) minV).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j, k).endVertex();
        vertexbuffer.pos((double) (renderX + rotationX * scale - rotationXY * scale), (double) (renderY - rotationZ * scale), (double) (renderZ + rotationYZ * scale - rotationXZ * scale)).tex((double) minU, (double) maxV).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j, k).endVertex();
    }
}