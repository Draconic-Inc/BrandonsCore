package com.brandon3055.brandonscore.client.gui.effects;

import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by brandon3055 on 16/06/2016.
 */
public class GuiEffectRenderer {
    private List<GuiEffect> effects = new ArrayList<GuiEffect>();

    public void updateEffects() {

        Iterator<GuiEffect> i = effects.iterator();

        while (i.hasNext()){
            GuiEffect effect = i.next();

            if (effect.isAlive()){
                effect.onUpdate();
            }
            else {
                i.remove();
            }

        }
    }

    public void renderEffects(float partialTick){
        for (GuiEffect effect : effects){

            if (effect.isTransparent()){
                GlStateManager.enableBlend();
                GlStateManager.alphaFunc(GL11.GL_GREATER, 0F);
            }

            GlStateManager.disableLighting();

            effect.renderParticle(partialTick);

            if (effect.isTransparent()){
                GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
            }
        }
    }

    public void addEffect(GuiEffect effect){
        effects.add(effect);
    }

    public void clearEffects(){
        effects.clear();
    }
}


