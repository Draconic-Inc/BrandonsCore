package com.brandon3055.brandonscore.integration;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.config.KeyBindings;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by brandon3055 on 21/09/2016.
 */
public class JeiHelper {

    public static boolean jeiAvailable() {
        if (!ModHelperBC.isJEIInstalled) {
            return false;
        }

        return checkJEIRuntime();
    }

    public static boolean checkJEIRuntime() {
        return BCJEIPlugin.jeiRuntime != null;
    }

    public static List<IRecipeRenderer> getRecipeRenderers(ItemStack result) {
        if (!jeiAvailable()) {
            return null;
        }

        return getRenderers(result);
    }

    private static List<IRecipeRenderer> getRenderers(ItemStack result) {
        List<IRecipeRenderer> renderers = new ArrayList<>();

        IRecipeManager registry = BCJEIPlugin.jeiRuntime.getRecipeManager();
        List<IRecipeCategory<?>> categories = new LinkedList<IRecipeCategory<?>>(registry.getRecipeCategories(registry.createFocus(IFocus.Mode.OUTPUT, result), true));

        for (IRecipeCategory category : categories) {
            List wrappers = registry.getRecipes(category, registry.createFocus(IFocus.Mode.OUTPUT, result), true);
            for (Object wrapper : wrappers) {
                try {
                    renderers.add(new RecipeRenderer(category, wrapper, result));
                }
                catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        }

        return renderers;
    }

    public static void openJEIRecipe(ItemStack stack, boolean usage) {
        if (jeiAvailable()) {
            openJEIRecipeInternal(stack, usage);
        }
    }

    private static void openJEIRecipeInternal(ItemStack stack, boolean usage) {
        if (checkJEIRuntime()) {
            IFocus f = BCJEIPlugin.jeiRuntime.getRecipeManager().createFocus(usage ? IFocus.Mode.INPUT : IFocus.Mode.OUTPUT, stack);
            BCJEIPlugin.jeiRuntime.getRecipesGui().show(f);
        }
    }

    public static int getRecipeKey(boolean usage) {
        if (jeiAvailable()) {
            return getRecipeKeyInternal(usage);
        }
        return -1;
    }

    private static int getRecipeKeyInternal(boolean usage) {
        try {
            return usage ? KeyBindings.showUses.get(0).getKey().getValue() : KeyBindings.showRecipe.get(0).getKey().getValue();
        }
        catch (Throwable e) {
            e.printStackTrace();
            return 01;
        }
    }

    @Nullable //Because reasons!
    public static ItemStack getPanelItemUnderMouse() {
        if (!jeiAvailable()) {
            return null;
        }

        Object ingredient = BCJEIPlugin.jeiRuntime.getIngredientListOverlay().getIngredientUnderMouse();

        if (ingredient == null) {
            return null;
        }

        IIngredientHelper helper = BCJEIPlugin.jeiRuntime.getIngredientManager().getIngredientHelper(ingredient);

        return helper.getCheatItemStack(ingredient);
    }

    //region IRecipeRenderer

    private static class RecipeRenderer implements IRecipeRenderer {

        private IRecipeLayoutDrawable recipeLayout;
        private int width;
        private int height;
        private int xPos = 0;
        private int yPos = 0;
        private Component title;

        public RecipeRenderer(IRecipeCategory category, Object wrapper, ItemStack result) {
            IFocus<?> f = BCJEIPlugin.jeiRuntime.getRecipeManager().createFocus(IFocus.Mode.OUTPUT, result);//new Focus<Object>(result);
            this.recipeLayout = BCJEIPlugin.jeiRuntime.getRecipeManager().createRecipeLayoutDrawable(category, wrapper, f);
            this.width = category.getBackground().getWidth();
            this.height = category.getBackground().getHeight();
            this.title = category.getTitle();
        }

        @Override
        public int getWidth() {
            return width;
        }

        @Override
        public int getHeight() {
            return height;
        }

        @Override
        public void render(Minecraft mc, int xPos, int yPos, int mouseX, int mouseY) {
            if (this.xPos != xPos || this.yPos != yPos) {
                this.xPos = xPos;
                this.yPos = yPos;
                recipeLayout.setPosition(xPos, yPos);
            }

            recipeLayout.drawRecipe(new PoseStack(), mouseX, mouseY);
        }

        @Override
        public void renderOverlay(Minecraft mc, int mouseX, int mouseY) {
            recipeLayout.drawOverlays(new PoseStack(), mouseX, mouseY);
        }

        @Override
        public boolean handleRecipeClick(Minecraft minecraft, double mouseX, double mouseY, boolean usage) {
            Object clicked = recipeLayout.getIngredientUnderMouse((int) mouseX, (int) mouseY, () -> Object.class);

            if (clicked != null) {
                IFocus f = BCJEIPlugin.jeiRuntime.getRecipeManager().createFocus(usage ? IFocus.Mode.INPUT : IFocus.Mode.OUTPUT, clicked);
                BCJEIPlugin.jeiRuntime.getRecipesGui().show(f);
            }

            return false;//layout.handleRecipeClick(minecraft, mouseX, mouseY, mouseButton);
        }

        @Nullable
        @Override
        public Object getIngredientUnderMouse(double mouseX, double mouseY) {
            return recipeLayout.getIngredientUnderMouse((int) mouseX, (int) mouseY, () -> Object.class);
        }

        @Override
        public Component getTitle() {
            return title;
        }
    }

    //endregion

}
