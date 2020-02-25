//package com.brandon3055.brandonscore.client.particle;
//
//import com.brandon3055.brandonscore.BrandonsCore;
//import com.brandon3055.brandonscore.lib.PairKV;
//import com.brandon3055.brandonscore.lib.Vec3D;
//import com.brandon3055.brandonscore.network.PacketSpawnParticle;
//import com.brandon3055.brandonscore.utils.BCProfiler;
//import com.brandon3055.brandonscore.utils.LogHelperBC;
//import net.minecraft.client.Minecraft;
//import net.minecraft.client.particle.Particle;
//import net.minecraft.client.renderer.BufferBuilder;
//import net.minecraft.client.renderer.Tessellator;
//import net.minecraft.entity.Entity;
//import net.minecraft.util.ResourceLocation;
//import net.minecraft.util.text.TextFormatting;
//import net.minecraft.world.World;
//import net.minecraftforge.api.distmarker.Dist;
//import net.minecraftforge.api.distmarker.OnlyIn;
//import net.minecraftforge.client.event.RenderGameOverlayEvent;
//import net.minecraftforge.client.event.RenderWorldLastEvent;
//import net.minecraftforge.common.MinecraftForge;
//import net.minecraftforge.event.world.WorldEvent;
//import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
//import net.minecraftforge.fml.common.gameevent.TickEvent;
//import net.minecraftforge.fml.common.network.NetworkRegistry;
//import net.minecraftforge.fml.relauncher.Side;
//import net.minecraftforge.fml.relauncher.SideOnly;
//
//import java.util.LinkedHashMap;
//import java.util.Map;
//
///**
// * Created by brandon3055 on 19/4/2016.
// * This class is going to be responsible for rendering all of my particles from now on.
// */
//public class BCEffectHandler {
//    private static BCEffectHandler instance = new BCEffectHandler();
////    @OnlyIn(Dist.CLIENT)
////    public static BCEffectRenderer effectRenderer;
//    public static Map<Integer, PairKV<IBCParticleFactory, ResourceLocation>> particleRegistry = new LinkedHashMap<Integer, PairKV<IBCParticleFactory, ResourceLocation>>();
//    private static int lastIndex = -1;
//    private static World currentWorld = null;
//
////    @OnlyIn(Dist.CLIENT)
////    public static void iniEffectRenderer() {
////        effectRenderer = new BCEffectRenderer(null);
////        MinecraftForge.EVENT_BUS.register(instance);
////    }
//
//    //region Registry
//
//    /**
//     * Registers a particle and its texture sheet with the CE Effect Handler. Its best to use the same sheet for as many
//     * particles as possible for best performance.
//     */
////    @OnlyIn(Dist.CLIENT)
//    public static int registerFX(ResourceLocation particleSheet, IBCParticleFactory factory) {
//        lastIndex++;
//        particleRegistry.put(lastIndex, new PairKV<IBCParticleFactory, ResourceLocation>(factory, particleSheet));
//        return lastIndex;
//    }
//
//    public static int registerFXServer() {
//        lastIndex++;
//        return lastIndex;
//    }
//
//    //endregion
//
//    //region Spawning
//
//    /**
//     * Spawns a particle in the world respecting the current particle settings.
//     * Can be called server side (Will automatically send a packet to all clients in range to spawn client side)
//     */
//    public static void spawnFX(int particleID, World world, double xCoord, double yCoord, double zCoord, double xSpeed, double ySpeed, double zSpeed, double viewRange, int... args) {
//        spawnFX(particleID, world, new Vec3D(xCoord, yCoord, zCoord), new Vec3D(xSpeed, ySpeed, zSpeed), viewRange, args);
////        if (!world.isRemote) {
////            BrandonsCore.network.sendToAllAround(new PacketSpawnParticle(particleID, xCoord, yCoord, zCoord, xSpeed, ySpeed, zSpeed, viewRange, args), new NetworkRegistry.TargetPoint(world.provider.getDimension(), xCoord, yCoord, zCoord, viewRange));
////        } else {
////            if (isInRange(xCoord, yCoord, zCoord, viewRange) && effectRenderer != null) {
////
////                if (!particleRegistry.containsKey(particleID)) {
////                    LogHelper.error("Attempted to spawn an unregistered particle ID (%s)", particleID);
////                    return;
////                }
////
////                Minecraft mc = Minecraft.getInstance();
////                int particleSetting = mc.gameSettings.particleSetting;
////
////                if (particleSetting == 2 || (particleSetting == 1 && world.rand.nextInt(3) != 0)) {
////                    return;
////                }
////
////                PairKV<IBCParticleFactory, ResourceLocation> pair = particleRegistry.get(particleID);
////                effectRenderer.addEffect(pair.getValue(), pair.getKey().getEntityFX(particleID, world, new Vec3D(xCoord, yCoord, zCoord), new Vec3D(xSpeed, ySpeed, zSpeed), args));
////            }
////        }
//    }
//
//    /**
//     * Spawns with a range of 64.
//     */
//    public static void spawnFX(int particleID, World world, double xCoord, double yCoord, double zCoord, double xSpeed, double ySpeed, double zSpeed, int... args) {
//        spawnFX(particleID, world, new Vec3D(xCoord, yCoord, zCoord), new Vec3D(xSpeed, ySpeed, zSpeed), 32, args);
//        //spawnFX(particleID, world, xCoord, yCoord, zCoord, xSpeed, ySpeed, zSpeed, 64, args);
//    }
//
//    /**
//     * Spawns with a range of 64.
//     */
//    public static void spawnFX(int particleID, World world, Vec3D pos, Vec3D speed, int... args) {
//        spawnFX(particleID, world, pos, speed, 32, args);
//    }
//
//    public static void spawnFX(int particleID, World world, Vec3D pos, Vec3D speed, double viewRange, int... args) {
//        if (!world.isRemote) {
//            BrandonsCore.network.sendToAllAround(new PacketSpawnParticle(particleID, pos.x, pos.y, pos.z, speed.x, speed.y, speed.z, viewRange, args), new NetworkRegistry.TargetPoint(world.provider.getDimension(), pos.x, pos.y, pos.z, viewRange));
//        } else {
//            if (isInRange(pos.x, pos.y, pos.z, viewRange) && effectRenderer != null) {
//
//                if (!particleRegistry.containsKey(particleID)) {
//                    LogHelperBC.error("Attempted to spawn an unregistered particle ID (%s)", particleID);
//                    return;
//                }
//
//                Minecraft mc = Minecraft.getInstance();
//                int particleSetting = mc.gameSettings.particleSetting;
//
//                if (particleSetting == 2 || (particleSetting == 1 && world.rand.nextInt(3) != 0)) {
//                    return;
//                }
//
//                PairKV<IBCParticleFactory, ResourceLocation> pair = particleRegistry.get(particleID);
//                Particle particle = pair.getKey().getEntityFX(particleID, world, pos, speed, args);
//
//                if (particle instanceof BCParticle && ((BCParticle) particle).isRawGLParticle()) {
//                    effectRenderer.addRawGLEffect(RAW_GL_DUMMY_HANDLER, (BCParticle) particle);
//                }
//                else {
//                    effectRenderer.addEffect(pair.getValue(), particle);
//                }
//            }
//        }
//    }
//
//    /**
//     * A client side only method that allows you to add effects directly to the effect renderer.
//     */
//    @OnlyIn(Dist.CLIENT)
//    public static void spawnFXDirect(ResourceLocation texture, BCParticle particle) {
//        spawnFXDirect(texture, particle, 32, true);
//    }
//
//    /**
//     * A client side only method that allows you to add effects directly to the effect renderer.
//     */
//    @OnlyIn(Dist.CLIENT)
//    public static void spawnFXDirect(ResourceLocation texture, BCParticle particle, double viewRange, boolean respectParticleSetting) {
//        Vec3D pos = particle.getPos();
//        if (isInRange(pos.x, pos.y, pos.z, viewRange) && effectRenderer != null) {
//
//            Minecraft mc = Minecraft.getInstance();
//            int particleSetting = mc.gameSettings.particleSetting;
//
//            if (respectParticleSetting && (particleSetting == 2 || (particleSetting == 1 && particle.getWorld().rand.nextInt(3) != 0))) {
//                return;
//            }
//
//            effectRenderer.addEffect(texture, particle);
//        }
//    }
//
//    /**
//     * Spawns a particle with direct GL access.
//     * WARNING! Only use this with compatible particles!
//     * Attempting to spawn any old particle with this will break things.
//     *
//     * */
//    @OnlyIn(Dist.CLIENT)
//    public static void spawnGLParticle(IGLFXHandler handler, BCParticle particle, double viewRange, boolean respectParticleSetting) {
//        Vec3D pos = particle.getPos();
//        if (isInRange(pos.x, pos.y, pos.z, viewRange) && effectRenderer != null) {
//
//            Minecraft mc = Minecraft.getInstance();
//            int particleSetting = mc.gameSettings.particleSetting;
//
//            if (respectParticleSetting && (particleSetting == 2 || (particleSetting == 1 && particle.getWorld().rand.nextInt(3) != 0))) {
//                return;
//            }
//
//            effectRenderer.addRawGLEffect(handler, particle);
//        }
//    }
//
//    /**
//     * Strait pass-through method that does thats adds the affect directly to the renderer without doing any checks.
//     */
//    @OnlyIn(Dist.CLIENT)
//    public static void spawnGLParticle(IGLFXHandler handler, BCParticle particle) {
//        effectRenderer.addRawGLEffect(handler, particle);
//    }
//
//    //endregion
//
//    //region Events
//
////    @OnlyIn(Dist.CLIENT)
////    @SubscribeEvent
////    public void clientTick(TickEvent.ClientTickEvent event) {
////        Minecraft mc = Minecraft.getInstance();
////        if (event.phase != TickEvent.Phase.END || mc.isGamePaused()) {
////            return;
////        }
////
////        if (currentWorld != mc.world && mc.world != null) {
////            currentWorld = mc.world;
////            BrandonsCore.proxy.resetEffectRenderer(currentWorld);
////        }
////
////        if (effectRenderer.world != null) {
////            BCProfiler.TICK.start("update_bc_effect_renderer");
////            mc.mcProfiler.startSection("BCParticlesUpdate");
////            effectRenderer.updateEffects();
////            mc.mcProfiler.endSection();
////            BCProfiler.TICK.stop();
////        }
////    }
////
////    @OnlyIn(Dist.CLIENT)
////    @SubscribeEvent
////    public void worldLoad(WorldEvent.Load event) {
////        BrandonsCore.proxy.resetEffectRenderer(event.getWorld());
////        currentWorld = event.getWorld();
////    }
////
////    @OnlyIn(Dist.CLIENT)
////    @SubscribeEvent
////    public void renderWorld(RenderWorldLastEvent event) {
////        BCProfiler.RENDER.start("bc_effect_renderer_draw");
////        Minecraft.getInstance().mcProfiler.startSection("BCParticles");
////        effectRenderer.renderParticles(Minecraft.getInstance().player, event.getPartialTicks());
////        Minecraft.getInstance().mcProfiler.endSection();
////        BCProfiler.RENDER.stop();
////    }
////
////    //TODO Move this to a separate client event handler if i ever need this event elsewhere
////    @OnlyIn(Dist.CLIENT)
////    @SubscribeEvent
////    public void debugOverlay(RenderGameOverlayEvent.Text event) {
////        if (event.getLeft().size() >= 5 && effectRenderer != null) {
////            String particleTxt = event.getLeft().get(4);
////            particleTxt += "." + TextFormatting.GOLD + " BC-P: " + effectRenderer.getStatistics();
////            event.getLeft().set(4, particleTxt);
////        }
////    }
//
//    //endregion Events
//
//    //region helpers
//
//    @OnlyIn(Dist.CLIENT)
//    public static boolean isInRange(double x, double y, double z, double vewRange) {
//        Minecraft mc = Minecraft.getInstance();
//
//        if (mc == null || mc.getRenderViewEntity() == null || mc.particles == null) return false;
//
//        double var15 = mc.getRenderViewEntity().posX - x;
//        double var17 = mc.getRenderViewEntity().posY - y;
//        double var19 = mc.getRenderViewEntity().posZ - z;
//        if (var15 * var15 + var17 * var17 + var19 * var19 > vewRange * vewRange) {
//            return false;
//        }
//        return true;
//    }
//
//    //endregion
//
//    private static final IGLFXHandler RAW_GL_DUMMY_HANDLER = new IGLFXHandler() {
//        @Override
//        public void preDraw(int layer, BufferBuilder buffer, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
//        }
//
//        @Override
//        public void postDraw(int layer, BufferBuilder buffer, Tessellator tessellator) {
//        }
//    };
//}
