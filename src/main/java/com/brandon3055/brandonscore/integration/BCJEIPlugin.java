package com.brandon3055.brandonscore.integration;

import com.brandon3055.brandonscore.BrandonsCore;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IModIngredientRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.ResourceLocation;


import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by brandon3055 on 14/06/2017.
 */
@JeiPlugin
public class BCJEIPlugin implements IModPlugin {

    private static final ResourceLocation ID = new ResourceLocation(BrandonsCore.MODID, "jei_plugin");
    public static IJeiRuntime jeiRuntime = null;
//    public static IIngredientRegistry ingredientRegistry = null;

    @Override
    public ResourceLocation getPluginUid() {
        return ID;
    }

    @Override
    public void registerIngredients(IModIngredientRegistration registration) {
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        //TODO JEI integration
//        registration.addGuiContainerHandler(ModularGuiContainer.class, new IGuiContainerHandler<>() {
//            @Override
//            public List<Rect2i> getGuiExtraAreas(ModularGuiContainer containerScreen) {
//                return containerScreen.getManager().getJeiExclusions();
//            }
//        });
//
//        registration.addGuiScreenHandler(ModularGuiScreen.class, ModularGuiProperties::create);
//        registration.addGuiScreenHandler(ModularGuiContainer.class, ModularGuiProperties::create);
//
//
//
//        registration.addGhostIngredientHandler(ModularGuiContainer.class, new IGhostIngredientHandler<>() {
//            @Override
//            public <I> java.util.List<Target<I>> getTargets(ModularGuiContainer gui, I ingredient, boolean doStart) {
//                //noinspection unchecked
//                return ((Stream<JEITargetAdapter>) gui.getJEIDropTargets().stream()).filter(JEITargetAdapter::isEnabled).map(adaptor -> new Target<I>() {
//                    @Override
//                    public Rect2i getArea() {
//                        return adaptor.getMCRect();
//                    }
//
//                    @Override
//                    public void accept(I o) {
//                        adaptor.accept(o);
//                    }
//                }).collect(Collectors.toList());
//            }
//
//            @Override
//            public void onComplete() {
//
//            }
//        });
//
//        registration.addGhostIngredientHandler(ModularGuiScreen.class, new IGhostIngredientHandler<>() {
//            @Override
//            public <I> java.util.List<Target<I>> getTargets(ModularGuiScreen gui, I ingredient, boolean doStart) {
//                return gui.getJEIDropTargets().stream().filter(JEITargetAdapter::isEnabled).map(adaptor -> new Target<I>() {
//                    @Override
//                    public Rect2i getArea() {
//                        return adaptor.getMCRect();
//                    }
//
//                    @Override
//                    public void accept(I o) {
//                        adaptor.accept(o);
//                    }
//                }).collect(Collectors.toList());
//            }
//
//            @Override
//            public void onComplete() {}
//        });
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime iJeiRuntime) {
        jeiRuntime = iJeiRuntime;
    }
}
