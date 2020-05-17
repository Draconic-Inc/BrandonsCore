package com.brandon3055.brandonscore.client;

import com.brandon3055.brandonscore.BrandonsCore;
import com.brandon3055.brandonscore.CommonProxy;
import com.brandon3055.brandonscore.api.TimeKeeper;
import com.brandon3055.brandonscore.handlers.IProcess;
import com.brandon3055.brandonscore.lib.DLRSCache;
import com.brandon3055.brandonscore.utils.BCProfiler;
import com.brandon3055.brandonscore.utils.ModelUtils;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.particle.IParticleRenderType;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoader;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.commons.lang3.Validate;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWImage;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Base64;

/**
 * Created by Brandon on 14/5/2015.
 */
public class ClientProxy extends CommonProxy {

    public static final IParticleRenderType PARTICLE_SHEET_NO_DEPTH = new IParticleRenderType() {
        public void beginRender(BufferBuilder p_217600_1_, TextureManager p_217600_2_) {
            RenderSystem.depthMask(false);
            p_217600_2_.bindTexture(AtlasTexture.LOCATION_PARTICLES_TEXTURE);
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            RenderSystem.alphaFunc(516, 0.003921569F);
            p_217600_1_.begin(7, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
        }

        public void finishRender(Tessellator p_217599_1_) {
            p_217599_1_.draw();
        }

        public String toString() {
            return "PARTICLE_SHEET_NO_DEPTH";
        }
    };

    @Override
    public void construct() {
        super.construct();
        FMLJavaModLoadingContext.get().getModEventBus().addListener(BCSprites::initialize);
    }

    @Override
    public void commonSetup(FMLCommonSetupEvent event) {
        super.commonSetup(event);
        ((IReloadableResourceManager) Minecraft.getInstance().getResourceManager()).addReloadListener(new ModelUtils());
        MinecraftForge.EVENT_BUS.register(new BCClientEventHandler());
        DLRSCache.initialize();
        ProcessHandlerClient.init();
        BCProfiler.init();
    }

    @Override
    public void clientSetup(FMLClientSetupEvent event) {
        MinecraftForge.EVENT_BUS.addListener(CursorHelper::closeGui);
    }


    @Override
    public MinecraftServer getMCServer() {
        return super.getMCServer();
    }

    @Override
    public World getClientWorld() {
        return Minecraft.getInstance().world;
    }

    @Override
    public boolean isJumpKeyDown() {
        return Minecraft.getInstance().gameSettings.keyBindJump.isKeyDown();
    }

    @Override
    public boolean isSneakKeyDown() {
        return Minecraft.getInstance().gameSettings.keyBindSneak.isKeyDown();
    }

    @Override
    public boolean isSprintKeyDown() {
        return Minecraft.getInstance().gameSettings.keyBindSprint.isKeyDown();
    }

    @Override
    public boolean isCTRLKeyDown() {
        return Screen.hasControlDown();
    }

    @Override
    public PlayerEntity getClientPlayer() {
        return Minecraft.getInstance().player;
    }

    @Override
    public void setChatAtIndex(ITextComponent chat, int index) {
        if (chat == null) {
            Minecraft.getInstance().ingameGUI.getChatGUI().deleteChatLine(index);
        } else {
            Minecraft.getInstance().ingameGUI.getChatGUI().printChatMessageWithOptionalDeletion(chat, index);
        }
    }

    @Override
    public void addProcess(IProcess iProcess) {
        ProcessHandlerClient.addProcess(iProcess);
    }

//    @Override
//    public void registerModFeatures(String modid) {
//        super.registerModFeatures(modid);
//        ModFeatureParser.registerModRendering(modid);
//    }

    @Override
    public void runSidedProcess(IProcess process) {
        ProcessHandlerClient.addProcess(process);
    }

    @Override
    public void resetEffectRenderer(World world) {
//        BCEffectHandler.effectRenderer.clearEffects(world);
    }

//    @Override
//    public IAnimationStateMachine loadASM(ResourceLocation location, ImmutableMap<String, ITimeValue> customParameters) {
//        return ModelLoaderRegistry.loadASM(location, customParameters);
//    }

    @Override
    public int tickTimer() {
        return TimeKeeper.getClientTick();
    }
}
