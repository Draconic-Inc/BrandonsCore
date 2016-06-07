package com.brandon3055.brandonscore.client.particle;

import com.brandon3055.brandonscore.client.ResourceHelperBC;
import com.brandon3055.brandonscore.lib.PairKV;
import net.minecraft.client.particle.Particle;
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
//        private final ArrayDeque<Particle>[][] renderQueue = new ArrayDeque[4][];
    private final Map<ResourceLocation, ArrayDeque<Particle>[][]> texturedRenderQueue = new HashMap<ResourceLocation, ArrayDeque<Particle>[][]>();

    //Particles that need to be injected into the render queues next tick;
//        private final Queue<Particle> queueParticle = new ArrayDeque<Particle>();
    private final Queue<PairKV<ResourceLocation, Particle>> queueTexturedParticle = new ArrayDeque<PairKV<ResourceLocation, Particle>>();

    @SuppressWarnings("unchecked")
    public BCEffectRenderer(World worldObj) {
        this.worldObj = worldObj;

//            for (int i = 0; i < 4; ++i) {
//                this.renderQueue[i] = new ArrayDeque[2];
//
//                for (int j = 0; j < 2; ++j) {
//                    this.renderQueue[i][j] = new ArrayDeque<Particle>();
//                }
//            }
    }

    //region Adders

//        public void addEffect(Particle Particle) {
//            if (Particle == null) return;
//            queueParticle.add(Particle);
//        }

    public void addEffect(ResourceLocation resourceLocation, Particle Particle) {
        if (resourceLocation == null || Particle == null) return;
        queueTexturedParticle.add(new PairKV<ResourceLocation, Particle>(resourceLocation, Particle));
    }

    //endregion

    //region Update

    @SuppressWarnings("unchecked")
    public void updateEffects() {

        for (int i = 0; i < 4; ++i) {
            updateEffectLayer(i);
        }

        //region Add Queued Effects

//            if (!queueParticle.isEmpty()) {
//                for (Particle Particle = this.queueParticle.poll(); Particle != null; Particle = this.queueParticle.poll()) {
//                    int layer = Particle.getFXLayer();
//                    int mask = Particle.func_187111_c() ? 0 : 1;
//
//                    if (this.renderQueue[layer][mask].size() >= 16384) {
//                        this.renderQueue[layer][mask].removeFirst();
//                    }
//
//                    this.renderQueue[layer][mask].add(Particle);
//                }
//            }

        if (!queueTexturedParticle.isEmpty()) {
            for (PairKV<ResourceLocation, Particle> entry = queueTexturedParticle.poll(); entry != null; entry = queueTexturedParticle.poll()) {
                if (!texturedRenderQueue.containsKey(entry.getKey())) {
                    ArrayDeque[][] array = new ArrayDeque[4][];
                    for (int i = 0; i < 4; i++) {
                        array[i] = new ArrayDeque[2];
                        for (int j = 0; j < 2; ++j) {
                            array[i][j] = new ArrayDeque<Particle>();
                        }
                    }
                    texturedRenderQueue.put(entry.getKey(), array);
                }

                ArrayDeque[][] array = texturedRenderQueue.get(entry.getKey());
                Particle Particle = entry.getValue();

                int layer = Particle.getFXLayer();
                int mask = Particle.isTransparent() ? 0 : 1;

                if (array[layer][mask].size() >= 6000) {
                    Object o = array[layer][mask].removeFirst();
                    if (o instanceof Particle) {
                        ((Particle) o).setExpired();
                    }
                }

                array[layer][mask].add(Particle);
            }
        }

        //endregion
    }

    private void updateEffectLayer(int layer) {
        //    this.worldObj.theProfiler.startSection(layer + "");

        for (int i = 0; i < 2; ++i) {
            //        this.worldObj.theProfiler.startSection(i + "");
            //this.tickAndRemoveDead(renderQueue[layer][i]);
            for (ArrayDeque<Particle>[][] queue : texturedRenderQueue.values()) {
                this.tickAndRemoveDead(queue[layer][i]);
            }

            //        this.worldObj.theProfiler.endSection();
        }

        //    this.worldObj.theProfiler.endSection();
    }

    private void tickAndRemoveDead(Queue<Particle> queue) {
        if (!queue.isEmpty()) {
            Iterator<Particle> iterator = queue.iterator();

            while (iterator.hasNext()) {
                Particle Particle = iterator.next();
                tickParticle(Particle);

                if (!Particle.isAlive()) {
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

        for (ArrayDeque<Particle>[][] array : texturedRenderQueue.values()) {
            for (int layer = 0; layer < 4; ++layer) {
                for (int mask = 0; mask < 2; ++mask) {
                    array[layer][mask].clear();
                }
            }
        }

    }

    private void tickParticle(final Particle particle) {
        try {
            particle.onUpdate();
        }
        catch (Throwable throwable) {
            CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Ticking Particle");
            CrashReportCategory crashreportcategory = crashreport.makeCategory("Particle being ticked");
            final int i = particle.getFXLayer();
            crashreportcategory.addCrashSection("Particle", new Callable<String>() {
                public String call() throws Exception {
                    return particle.toString();
                }
            });
            crashreportcategory.addCrashSection("Particle Type", new Callable<String>() {
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
        Particle.interpPosX = entityIn.lastTickPosX + (entityIn.posX - entityIn.lastTickPosX) * (double) partialTicks;
        Particle.interpPosY = entityIn.lastTickPosY + (entityIn.posY - entityIn.lastTickPosY) * (double) partialTicks;
        Particle.interpPosZ = entityIn.lastTickPosZ + (entityIn.posZ - entityIn.lastTickPosZ) * (double) partialTicks;
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
//                    for (final Particle Particle : renderQueue[layer][j]) {
//                        try {
//                            Particle.renderParticle(vertexbuffer, entityIn, partialTicks, f, f4, f1, f2, f3);
//                        }
//                        catch (Throwable throwable) {
//                            CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Rendering Particle");
//                            CrashReportCategory crashreportcategory = crashreport.makeCategory("Particle being rendered");
//                            crashreportcategory.addCrashSectionCallable("Particle", new Callable<String>() {
//                                public String call() throws Exception {
//                                    return Particle.toString();
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

            ArrayDeque<Particle>[][] texRenderQueue = texturedRenderQueue.get(resourceLocation);

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

                    for (final Particle Particle : texRenderQueue[layer][j]) {
                        try {
                            Particle.renderParticle(vertexbuffer, entityIn, partialTicks, f, f4, f1, f2, f3);
                        }
                        catch (Throwable throwable) {
                            CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Rendering Particle");
                            CrashReportCategory crashreportcategory = crashreport.makeCategory("Particle being rendered");
                            crashreportcategory.addCrashSection("Particle", new Callable<String>() {
                                public String call() throws Exception {
                                    return Particle.toString();
                                }
                            });
                            crashreportcategory.addCrashSection("Particle Type", new Callable<String>() {
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
                for (ArrayDeque<Particle>[][] list : texturedRenderQueue.values()) {
                    i += list[j][k].size();
                }
            }
        }

        return "" + i;
    }
}