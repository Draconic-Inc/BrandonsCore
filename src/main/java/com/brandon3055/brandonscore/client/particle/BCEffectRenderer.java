package com.brandon3055.brandonscore.client.particle;

import com.brandon3055.brandonscore.client.ResourceHelperBC;
import com.brandon3055.brandonscore.lib.PairKV;
import com.google.common.collect.Queues;
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

    //Textured Particle Queue
    @SuppressWarnings("unchecked")
    private final Map<ResourceLocation, ArrayDeque<Particle>[][]> renderQueue = new HashMap<>();
    private final Queue<PairKV<ResourceLocation, Particle>> newParticleQueue = new ArrayDeque<>();
    @SuppressWarnings("unchecked")
    private final Map<IGLFXHandler, ArrayDeque<Particle>[]> glRenderQueue = new HashMap<>();
    private final Queue<PairKV<IGLFXHandler, Particle>> newGlParticleQueue = Queues.newArrayDeque();

    @SuppressWarnings("unchecked")
    public BCEffectRenderer(World worldObj) {
        this.worldObj = worldObj;
    }

    //region Adders

    public void addRawGLEffect(IGLFXHandler handler, BCParticle particle) {
        if (particle == null) {
            return;
        }

        if (!particle.isRawGLParticle()) {
            throw new RuntimeException("Attempted to spawn a regular particle as a Raw GL particle! This is not allowed!");
        }
        newGlParticleQueue.add(new PairKV<IGLFXHandler, Particle>(handler, particle));
    }

    public void addEffect(ResourceLocation resourceLocation, Particle particle) {
        if (resourceLocation == null || particle == null) {
            return;
        }

        if (particle instanceof BCParticle && ((BCParticle) particle).isRawGLParticle()) {
            throw new RuntimeException("Attempted to spawn a Raw GL particle using the default spawn call! This is not allowed!");
        }

        newParticleQueue.add(new PairKV<>(resourceLocation, particle));
    }

    //endregion

    //region Update

    @SuppressWarnings("unchecked")
    public void updateEffects() {
        for (int i = 0; i < 4; ++i) {
            updateEffectLayer(i);
        }

        //region Add Queued Effects
//        clearEffects(worldObj);
//        glRenderQueue.clear();

        if (!newGlParticleQueue.isEmpty()) {
            for (PairKV<IGLFXHandler, Particle> handlerParticle = newGlParticleQueue.poll(); handlerParticle != null; handlerParticle = newGlParticleQueue.poll()) {
                if (!glRenderQueue.containsKey(handlerParticle.getKey())) {
                    glRenderQueue.put(handlerParticle.getKey(), new ArrayDeque[]{new ArrayDeque(), new ArrayDeque(), new ArrayDeque(), new ArrayDeque()});
                }
                int layer = handlerParticle.getValue().getFXLayer();

                if (glRenderQueue.get(handlerParticle.getKey())[layer].size() > 6000) {
                    glRenderQueue.get(handlerParticle.getKey())[layer].removeFirst().setExpired();
                }

                glRenderQueue.get(handlerParticle.getKey())[layer].add(handlerParticle.getValue());
            }
        }

        if (!newParticleQueue.isEmpty()) {
            for (PairKV<ResourceLocation, Particle> entry = newParticleQueue.poll(); entry != null; entry = newParticleQueue.poll()) {
                if (!renderQueue.containsKey(entry.getKey())) {
                    ArrayDeque[][] array = new ArrayDeque[4][];
                    for (int i = 0; i < 4; i++) {
                        array[i] = new ArrayDeque[2];
                        for (int j = 0; j < 2; ++j) {
                            array[i][j] = new ArrayDeque<>();
                        }
                    }
                    renderQueue.put(entry.getKey(), array);
                }

                ArrayDeque<Particle>[][] array = renderQueue.get(entry.getKey());
                Particle Particle = entry.getValue();

                int layer = Particle.getFXLayer();
                int mask = Particle.isTransparent() ? 0 : 1;

                if (array[layer][mask].size() >= 6000) {
                    array[layer][mask].removeFirst().setExpired();
                }

                array[layer][mask].add(Particle);
            }
        }

        //endregion
    }

    private void updateEffectLayer(int layer) {
        for (int i = 0; i < 2; ++i) {
            for (ArrayDeque<Particle>[][] queue : renderQueue.values()) {
                tickAndRemoveDead(queue[layer][i]);
            }
        }

        for (ArrayDeque<Particle>[] array : glRenderQueue.values()) {
            tickAndRemoveDead(array[layer]);
        }
    }

    private void tickAndRemoveDead(Queue<Particle> queue) {
        if (!queue.isEmpty()) {
            Iterator<Particle> iterator = queue.iterator();

            while (iterator.hasNext()) {
                Particle particle = iterator.next();
                tickParticle(particle);

                if (!particle.isAlive()) {
                    iterator.remove();
                }
            }
        }
    }

    /**
     * This should never be fired manually!
     */
    protected void clearEffects(World worldIn) {
        this.worldObj = worldIn;

        for (int j = 0; j < 4; ++j) {
            for (int k = 0; k < 2; ++k) {
                for (ArrayDeque<Particle>[][] list : renderQueue.values()) {
                    for (Particle particle : list[j][k]) {
                        particle.setExpired();
                    }
                }
            }
        }

        for (ArrayDeque<Particle>[] array : glRenderQueue.values()) {
            for (ArrayDeque<Particle> queue : array) {
                for (Particle particle : queue) {
                    particle.setExpired();
                }
            }
        }

//        for (ArrayDeque<Particle>[][] array : renderQueue.values()) {
//            for (int layer = 0; layer < 4; ++layer) {
//                for (int mask = 0; mask < 2; ++mask) {
//                    array[layer][mask].clear();
//                }
//            }
//        }
//
//
//        for (ArrayDeque<Particle>[] array : glRenderQueue.values()) {
//            for (ArrayDeque<Particle> queue : array) {
//                queue.clear();
//            }
//        }
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
        float rotationX = ActiveRenderInfo.getRotationX();
        float rotationZ = ActiveRenderInfo.getRotationZ();
        float rotationYZ = ActiveRenderInfo.getRotationYZ();
        float rotationXY = ActiveRenderInfo.getRotationXY();
        float rotationXZ = ActiveRenderInfo.getRotationXZ();
        Particle.interpPosX = entityIn.lastTickPosX + (entityIn.posX - entityIn.lastTickPosX) * (double) partialTicks;
        Particle.interpPosY = entityIn.lastTickPosY + (entityIn.posY - entityIn.lastTickPosY) * (double) partialTicks;
        Particle.interpPosZ = entityIn.lastTickPosZ + (entityIn.posZ - entityIn.lastTickPosZ) * (double) partialTicks;
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.alphaFunc(516, 0.003921569F);
        Tessellator tessellator = Tessellator.getInstance();

        for (int layer = 0; layer < 4; layer++) {
            renderGlParticlesInLayer(layer, tessellator, entityIn, partialTicks, rotationX, rotationZ, rotationYZ, rotationXY, rotationXZ);
            renderTexturedParticlesInLayer(layer, tessellator, entityIn, partialTicks, rotationX, rotationZ, rotationYZ, rotationXY, rotationXZ);
        }

        GlStateManager.depthMask(true);
        GlStateManager.disableBlend();
        GlStateManager.alphaFunc(516, 0.1F);
    }

    private void renderGlParticlesInLayer(int layer, Tessellator tessellator, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
        VertexBuffer vertexbuffer = tessellator.getBuffer();

        for (IGLFXHandler handler : glRenderQueue.keySet()) {

            handler.preDraw(layer, vertexbuffer, entityIn, partialTicks, rotationX, rotationZ, rotationYZ, rotationXY, rotationXZ);

            for (final Particle particle : glRenderQueue.get(handler)[layer]) {
                try {
                    particle.renderParticle(vertexbuffer, entityIn, partialTicks, rotationX, rotationXZ, rotationZ, rotationYZ, rotationXY);
                }
                catch (Throwable throwable) {
                    CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Rendering Particle");
                    CrashReportCategory crashreportcategory = crashreport.makeCategory("Particle being rendered");
                    crashreportcategory.addCrashSection("Particle", new Callable<String>() {
                        public String call() throws Exception {
                            return particle.toString();
                        }
                    });
                    throw new ReportedException(crashreport);
                }
            }

            handler.postDraw(layer, vertexbuffer, tessellator);
        }
    }

    private void renderTexturedParticlesInLayer(int layer, Tessellator tessellator, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
        for (ResourceLocation resourceLocation : renderQueue.keySet()) {
            ResourceHelperBC.bindTexture(resourceLocation);

            ArrayDeque<Particle>[][] texRenderQueue = renderQueue.get(resourceLocation);

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

                    for (final Particle particle : texRenderQueue[layer][j]) {
                        try {
                            particle.renderParticle(vertexbuffer, entityIn, partialTicks, rotationX, rotationXZ, rotationZ, rotationYZ, rotationXY);
                        }
                        catch (Throwable throwable) {
                            CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Rendering Particle");
                            CrashReportCategory crashreportcategory = crashreport.makeCategory("Particle being rendered");
                            crashreportcategory.addCrashSection("Particle", new Callable<String>() {
                                public String call() throws Exception {
                                    return particle.toString();
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
                for (ArrayDeque<Particle>[][] list : renderQueue.values()) {
                    i += list[j][k].size();
                }
            }
        }

        int g = 0;
        for (ArrayDeque<Particle>[] array : glRenderQueue.values()) {
            for (ArrayDeque<Particle> queue : array) {
                g += queue.size();
            }
        }

        return "" + i + " GLFX: " + g;
    }

    public static final IGLFXHandler DEFAULT_IGLFX_HANDLER = new IGLFXHandler() {
        @Override
        public void preDraw(int layer, VertexBuffer vertexbuffer, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.depthMask(false);
            GlStateManager.alphaFunc(GL11.GL_GREATER, 0F);
        }

        @Override
        public void postDraw(int layer, VertexBuffer vertexbuffer, Tessellator tessellator) {

        }
    };
}