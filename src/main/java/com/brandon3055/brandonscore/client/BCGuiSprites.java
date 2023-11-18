package com.brandon3055.brandonscore.client;

import codechicken.lib.gui.modular.sprite.Material;
import codechicken.lib.gui.modular.sprite.SpriteUploader;
import com.brandon3055.brandonscore.BCConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static com.brandon3055.brandonscore.BrandonsCore.MODID;

/**
 * Created by brandon3055 on 3/09/2016.
 */
public class BCGuiSprites {

    public static final ResourceLocation ATLAS_LOCATION = new ResourceLocation(MODID, "textures/atlas/gui.png");

    private static final Map<String, Material> MATERIAL_CACHE = new HashMap<>();
    private static final SpriteUploader SPRITE_UPLOADER = new SpriteUploader(new ResourceLocation(MODID, "textures/gui"), ATLAS_LOCATION, "gui");

    /**
     * The returned SpriteUploader needs to be registered as a resource reload listener using the appropriate NeoForge / Fabric event.
     */
    public static SpriteUploader getReloadListener() {
        return SPRITE_UPLOADER;
    }

    public static void onResourceReload(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(getReloadListener());
    }

    /**
     * @param texture Texture location starting inside 'modid:textures/gui/' You do not need to include .png
     *                The returned material is cached for efficiency, however this means it can only be used with a single RenderType
     *                This is because materials cache the render type they are used with.
     * @return A cached material for the specified texture.
     */
    public static Material get(String texture) {
        return MATERIAL_CACHE.computeIfAbsent(MODID + ":" + texture, s -> getUncached(texture));
    }

    public static Supplier<Material> getter(Supplier<String> texture) {
        return () -> get(texture.get());
    }

    public static Supplier<Material> getter(String texture) {
        return () -> get(texture);
    }

    public static Material getUncached(String texture) {
        return new Material(ATLAS_LOCATION, new ResourceLocation(MODID, texture), SPRITE_UPLOADER::getSprite);
    }

    public static Material getThemed(String location) {
        return get((BCConfig.darkMode ? "dark/" : "light/") + location);
    }

    public static Supplier<Material> themedGetter(String location) {
        return () -> get((BCConfig.darkMode ? "dark/" : "light/") + location);
    }


//    public static final ResourceLocation ATLAS_LOCATION = new ResourceLocation(MODID, "textures/atlas/gui.png");
//
//    private static CustomSpriteUploader customSpriteUploader;
//    private static final Map<ResourceLocation, Consumer<TextureAtlasSprite>> registeredSprites = new HashMap<>();
//    private static final Map<String, Material> matCache = new HashMap<>();
//
//    public static final RenderType GUI_TYPE = RenderType.create("gui_tex", DefaultVertexFormat.POSITION_COLOR_TEX, VertexFormat.Mode.QUADS, 256, RenderType.CompositeState.builder()
//            .setShaderState(new RenderStateShard.ShaderStateShard(GameRenderer::getPositionColorTexShader))
//            .setTextureState(new RenderStateShard.TextureStateShard(ATLAS_LOCATION, false, false))
//            .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
//            .setCullState(RenderStateShard.NO_CULL)
//            .createCompositeState(false)
//    );


//    public static void initialize(FMLClientSetupEvent event) { //TODO [FoxMcloud5655]: Probably not correct.
//        customSpriteUploader = new CustomSpriteUploader(registeredSprites, BCGuiSprites.ATLAS_LOCATION, "gui");
//
//        //Gui Backgrounds
//        //Stream.of(GuiLayout.values()).filter(e -> e.xSize != -1).forEach(layout -> registerThemed(MODID, layout.textureName()));
//        registerThemed(MODID, "background_dynamic");
//        registerThemed(MODID, "bg_dynamic_small");
//        registerThemed(MODID, "borderless_bg_dynamic_small");
//
//        //Gui Sprites
//        registerThemed(MODID, "button");
//        registerThemed(MODID, "button_highlight");
//        registerThemed(MODID, "button_disabled");
//        registerThemed(MODID, "button_borderless");
//        registerThemed(MODID, "button_borderless_invert");
//        registerThemed(MODID, "slot");
//        registerThemed(MODID, "slot_large");
//        registerThemed(MODID, "resize");
//        registerThemed(MODID, "reposition");
//        registerThemed(MODID, "copy");
//        registerThemed(MODID, "theme");
//        registerThemed(MODID, "gear");
//        registerThemed(MODID, "advanced");
//        registerThemed(MODID, "arrow_left");
//        registerThemed(MODID, "arrow_right");
//        registerThemed(MODID, "expand_content");
//        registerThemed(MODID, "collapse_content");
//        registerThemed(MODID, "preset_icon");
//        registerThemed(MODID, "global_icon");
//        registerThemed(MODID, "global_icon_inactive");
//        registerThemed(MODID, "global_key_icon");
//        registerThemed(MODID, "grid_small");
//        registerThemed(MODID, "grid_large");
//        registerThemed(MODID, "item_config");
//        registerThemed(MODID, "hud_button");
//        registerThemed(MODID, "info_icon");
//        registerThemed(MODID, "prog_arrow_right");
//        registerThemed(MODID, "prog_arrow_right_over");
//        registerThemed(MODID, "prog_arrow_up");
//        registerThemed(MODID, "prog_arrow_up_over");
//        registerThemed(MODID, "prog_arrow_up_tall");
//        registerThemed(MODID, "prog_arrow_up_tall_over");
//        registerThemed(MODID, "pwr_btn");
//
//        register(MODID, "add");
//        register(MODID, "delete");
//        register(MODID, "delete_all");
//        register(MODID, "info_panel");
//        register(MODID, "reposition_gray");
//        register(MODID, "new_group");
//
//        register(MODID, "color_picker");
//        register(MODID, "legacy");
//        register(MODID, "rgb_checker");
//
//        register(MODID, "redstone/always_active");
//        register(MODID, "redstone/active_high");
//        register(MODID, "redstone/active_low");
//        register(MODID, "redstone/never_active");
//
//        //Slot Overlays
//        register(MODID, "slots/fuel");
//        register(MODID, "slots/energy");
//        register(MODID, "slots/armor_boots");
//        register(MODID, "slots/armor_chestplate");
//        register(MODID, "slots/armor_helmet");
//        register(MODID, "slots/armor_leggings");
//        register(MODID, "slots/armor_shield");
//        register(MODID, "slots/sword");
//        register(MODID, "slots/trash");
//        register(MODID, "slots/filter");
//
//        register(MODID, "item_charge/btn_right_charge");
//        register(MODID, "item_charge/btn_right_discharge");
//        register(MODID, "item_charge/btn_right_disabled");
//        register(MODID, "item_charge/btn_right_both");
//        register(MODID, "item_charge/btn_vertical_charge");
//        register(MODID, "item_charge/btn_vertical_discharge");
//        register(MODID, "item_charge/btn_vertical_disabled");
//        register(MODID, "item_charge/btn_vertical_both");
//        register(MODID, "item_charge/horizontal_charge");
//        register(MODID, "item_charge/horizontal_discharge");
//        register(MODID, "item_charge/right_charge");
//        register(MODID, "item_charge/right_discharge");
//        register(MODID, "item_charge/vertical_charge");
//        register(MODID, "item_charge/vertical_discharge");
//
//        register(MODID, "bars/food_empty");
//        register(MODID, "bars/food_half");
//        register(MODID, "bars/food_full");
//        register(MODID, "bars/energy_empty");
//        register(MODID, "bars/energy_full");
//
//        register(MODID, "downloading");
//        register(MODID, "download_failed");
//    }
//
//    //region register
//
//    public static void registerThemed(String modid, String location) {
//        register(modid, "light/" + location);
//        register(modid, "dark/" + location);
//    }
//    public static void register(String modid, String location) {
//        register(new ResourceLocation(modid, location), equipmentManager -> {});
//    }
//
//    public static void register(ResourceLocation location) {
//        register(location, null);
//    }
//
//    public static void register(String modid, String location, Consumer<TextureAtlasSprite> onLoad) {
//        register(new ResourceLocation(modid, location), onLoad);
//    }
//
//    public static void register(ResourceLocation location, Consumer<TextureAtlasSprite> onLoad) {
//        registeredSprites.put(location, onLoad);
//    }
//
//
//    //endregion
//    public static Material getThemed(String modid, String location) {
//        return get(modid, (BCConfig.darkMode ? "dark/" : "light/") + location);
//    }
//
//    public static Material getThemed(String location) {
//        return get(MODID, (BCConfig.darkMode ? "dark/" : "light/") + location);
//    }
//
//    public static Material get(String modid, String location) {
//        return matCache.computeIfAbsent(modid + ":" + location, s -> new CustomMat(ATLAS_LOCATION, new ResourceLocation(modid, location)));
//    }
//
//    public static Material get(String location) {
//        return get(MODID, location);
//    }
//
//    public static TextureAtlasSprite getSprite(String location) {
//        return get(location).sprite();
//    }
//
//    public static Supplier<Material> themedGetter(String modid, String location) {
//        return () -> get(modid, (BCConfig.darkMode ? "dark/" : "light/") + location);
//    }
//
//    public static Supplier<Material> themedGetter(String location) {
//        return () -> get(MODID, (BCConfig.darkMode ? "dark/" : "light/") + location);
//    }
//
//    public static Supplier<Material> getter(String modid, String location) {
//        return () -> matCache.computeIfAbsent(modid + ":" + location, s -> new CustomMat(ATLAS_LOCATION, new ResourceLocation(modid, location)));
//    }
//
//    public static Supplier<Material> getter(String location) {
//        return () -> get(MODID, location);
//    }
//
//    public static Material getButton(int state) {
//        return getThemed(state == 1 ? "button" : state == 2 ? "button_highlight" : "button_disabled");
//    }
//
//    private static String[] ARMOR_ORDER = new String[] {"slots/armor_boots", "slots/armor_leggings", "slots/armor_chestplate", "slots/armor_helmet"};
//    public static Material getArmorSlot(int slot) {
//        return get(ARMOR_ORDER[slot]);
//    }
//
//    public static RenderType makeType(ResourceLocation location) {
//        return RenderType.create("sprite_type", DefaultVertexFormat.POSITION_TEX, VertexFormat.Mode.QUADS, 256, RenderType.CompositeState.builder()
//                .setShaderState(new RenderStateShard.ShaderStateShard(GameRenderer::getPositionTexShader))
//                .setTextureState(new RenderStateShard.TextureStateShard(location, false, false))
//                .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
//                .setCullState(RenderStateShard.NO_CULL)
//                .createCompositeState(false));
//    }
//
//    public static VertexConsumer builder(MultiBufferSource getter, PoseStack mStack) {
//        return new TransformingVertexConsumer(getter.getBuffer(BCGuiSprites.GUI_TYPE), mStack);
//    }
//
//    public static VertexConsumer builder(MultiBufferSource getter) {
//        return getter.getBuffer(BCGuiSprites.GUI_TYPE);
//    }
//
//    private static class CustomMat extends Material {
//
//        public CustomMat(ResourceLocation atlasLocationIn, ResourceLocation textureLocationIn) {
//            super(atlasLocationIn, textureLocationIn);
//        }
//
//        @Override
//        public TextureAtlasSprite sprite() {
//            return customSpriteUploader.getSprite(texture());
//        }
//    }
}
