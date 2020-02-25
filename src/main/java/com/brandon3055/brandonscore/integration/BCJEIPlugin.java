package com.brandon3055.brandonscore.integration;

import com.brandon3055.brandonscore.BrandonsCore;
import com.brandon3055.brandonscore.client.gui.modulargui.IModularGui.JEITargetAdapter;
import com.brandon3055.brandonscore.client.gui.modulargui.ModularGuiContainer;
import com.brandon3055.brandonscore.client.gui.modulargui.ModularGuiScreen;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IModIngredientRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.util.ResourceLocation;

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
        registration.addGuiContainerHandler(ModularGuiContainer.class, new IGuiContainerHandler<ModularGuiContainer>() {
            @Override
            public List<Rectangle2d> getGuiExtraAreas(ModularGuiContainer containerScreen) {
                return containerScreen.getManager().getJeiExclusions();
            }
        });
//        registration.addGuiScreenHandler(ModularGuiScreen.class, new IScreenHandler<ModularGuiScreen>() {
//            @Nullable
//            @Override
//            public IGuiProperties apply(ModularGuiScreen guiScreen) {
//                return guiScreen.getManager().getJeiExclusions();
//            }
//        });

        registration.addGhostIngredientHandler(ModularGuiContainer.class, new IGhostIngredientHandler<ModularGuiContainer>() {
            @Override
            public <I> java.util.List<Target<I>> getTargets(ModularGuiContainer gui, I ingredient, boolean doStart) {
                //noinspection unchecked
                return ((Stream<JEITargetAdapter>) gui.getJEIDropTargets().stream()).filter(JEITargetAdapter::isEnabled).map(adaptor -> new Target<I>() {
                    @Override
                    public Rectangle2d getArea() {
                        return adaptor.getMCRect();
                    }

                    @Override
                    public void accept(I o) {
                        adaptor.accept(o);
                    }
                }).collect(Collectors.toList());
            }

            @Override
            public void onComplete() {

            }
        });

        registration.addGhostIngredientHandler(ModularGuiScreen.class, new IGhostIngredientHandler<ModularGuiScreen>() {
            @Override
            public <I> java.util.List<Target<I>> getTargets(ModularGuiScreen gui, I ingredient, boolean doStart) {
                return gui.getJEIDropTargets().stream().filter(JEITargetAdapter::isEnabled).map(adaptor -> new Target<I>() {
                    @Override
                    public Rectangle2d getArea() {
                        return adaptor.getMCRect();
                    }

                    @Override
                    public void accept(I o) {
                        adaptor.accept(o);
                    }
                }).collect(Collectors.toList());
            }

            @Override
            public void onComplete() {}
        });
    }


//    @Override
//    public void register(IModRegistry registry) {
////        ModFeatureParser.getFeaturesToHide(stack -> registry.getJeiHelpers().getIngredientBlacklist().addIngredientToBlacklist(stack));
//        ingredientRegistry = registry.getIngredientRegistry();
//
//
//
////        ModRegistry reg = (ModRegistry) registry;
////        reg.getGhostIngredientHandlers().put(IModularGui.class, new IGhostIngredientHandler() {
////            @SuppressWarnings("unchecked")
////            @Override
////            public List<Target> getTargets(Screen gui, Object o, boolean b) {
////                return (List<Target>)((IModularGui)gui).getJEIDropTargets().stream().map(adaptor -> new Target<Object>() {
////                    @Override
////                    public Rectangle getArea() {
////                        return ((IModularGui.JEITargetAdapter)adaptor).getArea();
////                    }
////
////                    @Override
////                    public void accept(Object o) {
////                        ((IModularGui.JEITargetAdapter)adaptor).accept(o);
////                    }
////                }).collect(Collectors.toList());
////            }
////
////            @Override
////            public void onComplete() {}
////        });
//
//    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime iJeiRuntime) {
        jeiRuntime = iJeiRuntime;
    }
}
