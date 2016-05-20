package com.brandon3055.brandonscore.client.particle;

import com.brandon3055.brandonscore.client.ResourceHelperBC;
import com.brandon3055.brandonscore.lib.PairKV;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.util.ReportedException;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

import java.util.*;
import java.util.concurrent.Callable;

/**
 * Created by brandon3055 on 23/4/2016.
 * Custom effect renderer used by all of my mods
 */
public class BCEffectRenderer {
    public World worldObj;
    private final Random rand = new Random();

    //Textured Particle Queue
    @SuppressWarnings("unchecked")
//        private final ArrayDeque<EntityFX>[][] renderQueue = new ArrayDeque[4][];
    private final Map<ResourceLocation, ArrayDeque<EntityFX>[][]> texturedRenderQueue = new HashMap<ResourceLocation, ArrayDeque<EntityFX>[][]>();

    //Particles that need to be injected into the render queues next tick;
//        private final Queue<EntityFX> queueEntityFX = new ArrayDeque<EntityFX>();
    private final Queue<PairKV<ResourceLocation, EntityFX>> queueTexturedEntityFX = new ArrayDeque<PairKV<ResourceLocation, EntityFX>>();

    @SuppressWarnings("unchecked")
    public BCEffectRenderer(World worldObj) {
        this.worldObj = worldObj;

//            for (int i = 0; i < 4; ++i) {
//                this.renderQueue[i] = new ArrayDeque[2];
//
//                for (int j = 0; j < 2; ++j) {
//                    this.renderQueue[i][j] = new ArrayDeque<EntityFX>();
//                }
//            }
    }

    //region Adders

//        public void addEffect(EntityFX entityFX) {
//            if (entityFX == null) return;
//            queueEntityFX.add(entityFX);
//        }

    public void addEffect(ResourceLocation resourceLocation, EntityFX entityFX) {
        if (resourceLocation == null || entityFX == null) return;
        queueTexturedEntityFX.add(new PairKV<ResourceLocation, EntityFX>(resourceLocation, entityFX));
    }

    //endregion

    //region Update

    @SuppressWarnings("unchecked")
    public void updateEffects() {

        for (int i = 0; i < 4; ++i) {
            updateEffectLayer(i);
        }

        //region Add Queued Effects

//            if (!queueEntityFX.isEmpty()) {
//                for (EntityFX entityfx = this.queueEntityFX.poll(); entityfx != null; entityfx = this.queueEntityFX.poll()) {
//                    int layer = entityfx.getFXLayer();
//                    int mask = entityfx.func_187111_c() ? 0 : 1;
//
//                    if (this.renderQueue[layer][mask].size() >= 16384) {
//                        this.renderQueue[layer][mask].removeFirst();
//                    }
//
//                    this.renderQueue[layer][mask].add(entityfx);
//                }
//            }

        if (!queueTexturedEntityFX.isEmpty()) {
            for (PairKV<ResourceLocation, EntityFX> entry = queueTexturedEntityFX.poll(); entry != null; entry = queueTexturedEntityFX.poll()) {
                if (!texturedRenderQueue.containsKey(entry.getKey())) {
                    ArrayDeque[][] array = new ArrayDeque[4][];
                    for (int i = 0; i < 4; i++) {
                        array[i] = new ArrayDeque[2];
                        for (int j = 0; j < 2; ++j) {
                            array[i][j] = new ArrayDeque<EntityFX>();
                        }
                    }
                    texturedRenderQueue.put(entry.getKey(), array);
                }

                ArrayDeque[][] array = texturedRenderQueue.get(entry.getKey());
                EntityFX entityFX = entry.getValue();

                int layer = entityFX.getFXLayer();
                int mask = entityFX.func_187111_c() ? 0 : 1;

                if (array[layer][mask].size() >= 6000) {
                    Object o = array[layer][mask].removeFirst();
                    if (o instanceof EntityFX) {
                        ((EntityFX) o).setExpired();
                    }
                }

                array[layer][mask].add(entityFX);
            }
        }

        //endregion
    }

    private void updateEffectLayer(int layer) {
        //    this.worldObj.theProfiler.startSection(layer + "");

        for (int i = 0; i < 2; ++i) {
            //        this.worldObj.theProfiler.startSection(i + "");
            //this.tickAndRemoveDead(renderQueue[layer][i]);
            for (ArrayDeque<EntityFX>[][] queue : texturedRenderQueue.values()) {
                this.tickAndRemoveDead(queue[layer][i]);
            }

            //        this.worldObj.theProfiler.endSection();
        }

        //    this.worldObj.theProfiler.endSection();
    }

    private void tickAndRemoveDead(Queue<EntityFX> queue) {
        if (!queue.isEmpty()) {
            Iterator<EntityFX> iterator = queue.iterator();

            while (iterator.hasNext()) {
                EntityFX entityfx = iterator.next();
                tickParticle(entityfx);

                if (!entityfx.isAlive()) {
                    iterator.remove();
                }
            }
        }
    }

    public void clearEffects(World worldIn) {
        this.worldObj = worldIn;

//            for (int layer = 0; layer < 4; ++layer) {
//                for (int mask = 0; mask < 2; ++mask) {
//                    renderQueue[layer][mask].clear();
//                }
//            }

        for (ArrayDeque<EntityFX>[][] array : texturedRenderQueue.values()) {
            for (int layer = 0; layer < 4; ++layer) {
                for (int mask = 0; mask < 2; ++mask) {
                    array[layer][mask].clear();
                }
            }
        }

    }

    private void tickParticle(final EntityFX particle) {
        try {
            particle.onUpdate();
        }
        catch (Throwable throwable) {
            CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Ticking Particle");
            CrashReportCategory crashreportcategory = crashreport.makeCategory("Particle being ticked");
            final int i = particle.getFXLayer();
            crashreportcategory.addCrashSectionCallable("Particle", new Callable<String>() {
                public String call() throws Exception {
                    return particle.toString();
                }
            });
            crashreportcategory.addCrashSectionCallable("Particle Type", new Callable<String>() {
                public String call() throws Exception {
                    return i == 0 ? "MISC_TEXTURE" : (i == 1 ? "TERRAIN_TEXTURE" : (i == 3 ? "ENTITY_PARTICLE_TEXTURE" : "Unknown - " + i));
                }
            });
            throw new ReportedException(crashreport);
        }
    }

    //endregion

    //region Render

    public void renderParticles(Entity entityIn, float partialTicks) {
        float f = ActiveRenderInfo.getRotationX();
        float f1 = ActiveRenderInfo.getRotationZ();
        float f2 = ActiveRenderInfo.getRotationYZ();
        float f3 = ActiveRenderInfo.getRotationXY();
        float f4 = ActiveRenderInfo.getRotationXZ();
        EntityFX.interpPosX = entityIn.lastTickPosX + (entityIn.posX - entityIn.lastTickPosX) * (double) partialTicks;
        EntityFX.interpPosY = entityIn.lastTickPosY + (entityIn.posY - entityIn.lastTickPosY) * (double) partialTicks;
        EntityFX.interpPosZ = entityIn.lastTickPosZ + (entityIn.posZ - entityIn.lastTickPosZ) * (double) partialTicks;
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.alphaFunc(516, 0.003921569F);
        Tessellator tessellator = Tessellator.getInstance();

        for (int layer = 0; layer < 4; layer++) {
            //    renderParticlesInLayer(layer, tessellator, entityIn, partialTicks, f, f1, f2, f3, f4);
            renderTexturedParticlesInLayer(layer, tessellator, entityIn, partialTicks, f, f1, f2, f3, f4);
        }

        GlStateManager.depthMask(true);
        GlStateManager.disableBlend();
        GlStateManager.alphaFunc(516, 0.1F);
    }

    private void renderParticlesInLayer(int layer, Tessellator tessellator, Entity entityIn, float partialTicks, float f, float f1, float f2, float f3, float f4) {
        //ResourceHelperBC.bindTexture(particleSheet);

//            for (int j = 0; j < 2; ++j) {
//                final int i_f = layer;
//
//                if (!renderQueue[layer][j].isEmpty()) {
//                    switch (j) {
//                        case 0:
//                            GlStateManager.depthMask(false);
//                            break;
//                        case 1:
//                            GlStateManager.depthMask(true);
//                    }
//
//                    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
//                    VertexBuffer vertexbuffer = tessellator.getBuffer();
//                    vertexbuffer.begin(7, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
//
//                    for (final EntityFX entityfx : renderQueue[layer][j]) {
//                        try {
//                            entityfx.renderParticle(vertexbuffer, entityIn, partialTicks, f, f4, f1, f2, f3);
//                        }
//                        catch (Throwable throwable) {
//                            CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Rendering Particle");
//                            CrashReportCategory crashreportcategory = crashreport.makeCategory("Particle being rendered");
//                            crashreportcategory.addCrashSectionCallable("Particle", new Callable<String>() {
//                                public String call() throws Exception {
//                                    return entityfx.toString();
//                                }
//                            });
//                            crashreportcategory.addCrashSectionCallable("Particle Type", new Callable<String>() {
//                                public String call() throws Exception {
//                                    return i_f == 0 ? "MISC_TEXTURE" : (i_f == 1 ? "TERRAIN_TEXTURE" : (i_f == 3 ? "ENTITY_PARTICLE_TEXTURE" : "Unknown - " + i_f));
//                                }
//                            });
//                            throw new ReportedException(crashreport);
//                        }
//                    }
//
//                    tessellator.draw();
//                }
//            }
    }

    private void renderTexturedParticlesInLayer(int layer, Tessellator tessellator, Entity entityIn, float partialTicks, float f, float f1, float f2, float f3, float f4) {
        for (ResourceLocation resourceLocation : texturedRenderQueue.keySet()) {
            ResourceHelperBC.bindTexture(resourceLocation);

            ArrayDeque<EntityFX>[][] texRenderQueue = texturedRenderQueue.get(resourceLocation);

            for (int j = 0; j < 2; ++j) {
                final int i_f = layer;

                if (!texRenderQueue[layer][j].isEmpty()) {
                    switch (j) {
                        case 0:
                            GlStateManager.depthMask(false);
                            GlStateManager.alphaFunc(GL11.GL_GREATER, 0F);
                            break;
                        case 1:
                            GlStateManager.depthMask(true);
                    }

                    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                    VertexBuffer vertexbuffer = tessellator.getBuffer();
                    vertexbuffer.begin(7, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);

                    for (final EntityFX entityfx : texRenderQueue[layer][j]) {
                        try {
                            entityfx.renderParticle(vertexbuffer, entityIn, partialTicks, f, f4, f1, f2, f3);
                        }
                        catch (Throwable throwable) {
                            CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Rendering Particle");
                            CrashReportCategory crashreportcategory = crashreport.makeCategory("Particle being rendered");
                            crashreportcategory.addCrashSectionCallable("Particle", new Callable<String>() {
                                public String call() throws Exception {
                                    return entityfx.toString();
                                }
                            });
                            crashreportcategory.addCrashSectionCallable("Particle Type", new Callable<String>() {
                                public String call() throws Exception {
                                    return i_f == 0 ? "MISC_TEXTURE" : (i_f == 1 ? "TERRAIN_TEXTURE" : (i_f == 3 ? "ENTITY_PARTICLE_TEXTURE" : "Unknown - " + i_f));
                                }
                            });
                            throw new ReportedException(crashreport);
                        }
                    }

                    tessellator.draw();
                }
            }
        }
    }

    //endregion

    public String getStatistics() {
        int i = 0;

        for (int j = 0; j < 4; ++j) {
            for (int k = 0; k < 2; ++k) {
                for (ArrayDeque<EntityFX>[][] list : texturedRenderQueue.values()) {
                    i += list[j][k].size();
                }
            }
        }

        return "" + i;
    }
}