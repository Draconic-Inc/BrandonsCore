package com.brandon3055.brandonscore.integration;

import com.brandon3055.brandonscore.api.IJEIClearance;
import com.brandon3055.brandonscore.client.gui.modulargui.IModularGui.JEITargetAdapter;
import com.brandon3055.brandonscore.client.gui.modulargui.ModularGuiContainer;
import com.brandon3055.brandonscore.client.gui.modulargui.ModularGuiScreen;
import com.brandon3055.brandonscore.registry.ModFeatureParser;
import mezz.jei.api.IJeiRuntime;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.gui.IAdvancedGuiHandler;
import mezz.jei.api.gui.IGhostIngredientHandler;
import mezz.jei.api.ingredients.IIngredientRegistry;
import net.minecraft.client.gui.inventory.GuiContainer;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by brandon3055 on 14/06/2017.
 */
@JEIPlugin
public class BCJEIPlugin implements IModPlugin {

    public static IJeiRuntime jeiRuntime = null;
    public static IIngredientRegistry ingredientRegistry = null;

    @Override
    public void register(IModRegistry registry) {
        ModFeatureParser.getFeaturesToHide(stack -> registry.getJeiHelpers().getIngredientBlacklist().addIngredientToBlacklist(stack));
        ingredientRegistry = registry.getIngredientRegistry();
        registry.addAdvancedGuiHandlers(new IAdvancedGuiHandler() {
            @Override
            public Class getGuiContainerClass() {
                return IJEIClearance.class;
            }

            @Nullable
            @Override
            public java.util.List<Rectangle> getGuiExtraAreas(GuiContainer guiContainer) {
                if (guiContainer instanceof IJEIClearance) {
                    return ((IJEIClearance) guiContainer).getGuiExtraAreas();
                }
                return new ArrayList<>();
            }
        });

        registry.addGhostIngredientHandler(ModularGuiContainer.class, new IGhostIngredientHandler<ModularGuiContainer>() {
            @Override
            public <I> java.util.List<Target<I>> getTargets(ModularGuiContainer gui, I ingredient, boolean doStart) {
                //noinspection unchecked
                return ((Stream<JEITargetAdapter>)gui.getJEIDropTargets().stream()).filter(JEITargetAdapter::isEnabled).map(adaptor -> new Target<I>() {
                    @Override
                    public Rectangle getArea() {
                        return adaptor.getArea();
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

        registry.addGhostIngredientHandler(ModularGuiScreen.class, new IGhostIngredientHandler<ModularGuiScreen>() {
            @Override
            public <I> java.util.List<Target<I>> getTargets(ModularGuiScreen gui, I ingredient, boolean doStart) {
                return gui.getJEIDropTargets().stream().filter(JEITargetAdapter::isEnabled).map(adaptor -> new Target<I>() {
                    @Override
                    public Rectangle getArea() {
                        return adaptor.getArea();
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

//        ModRegistry reg = (ModRegistry) registry;
//        reg.getGhostIngredientHandlers().put(IModularGui.class, new IGhostIngredientHandler() {
//            @SuppressWarnings("unchecked")
//            @Override
//            public List<Target> getTargets(Screen gui, Object o, boolean b) {
//                return (List<Target>)((IModularGui)gui).getJEIDropTargets().stream().map(adaptor -> new Target<Object>() {
//                    @Override
//                    public Rectangle getArea() {
//                        return ((IModularGui.JEITargetAdapter)adaptor).getArea();
//                    }
//
//                    @Override
//                    public void accept(Object o) {
//                        ((IModularGui.JEITargetAdapter)adaptor).accept(o);
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
