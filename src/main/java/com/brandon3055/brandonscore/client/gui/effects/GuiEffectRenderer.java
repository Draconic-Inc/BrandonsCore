//package com.brandon3055.brandonscore.client.gui.effects;
//
//import com.mojang.blaze3d.systems.RenderSystem;
//import org.lwjgl.opengl.GL11;
//
//import java.util.ArrayList;
//import java.util.Iterator;
//import java.util.List;
//
///**
// * Created by brandon3055 on 16/06/2016.
// */
//public class GuiEffectRenderer {
//    private List<GuiEffect> effects = new ArrayList<GuiEffect>();
//
//    public void updateEffects() {
//
//        Iterator<GuiEffect> i = effects.iterator();
//
//        while (i.hasNext()){
//            GuiEffect effect = i.next();
//
//            if (effect.isAlive()){
//                effect.onUpdate();
//            }
//            else {
//                i.remove();
//            }
//
//        }
//    }
//
//    public void renderEffects(float partialTick){
//        for (GuiEffect effect : effects){
//
//            if (effect.isTransparent()){
//                RenderSystem.enableBlend();
//                RenderSystem.alphaFunc(GL11.GL_GREATER, 0F);
//            }
//
//            RenderSystem.disableLighting();
//
//            effect.renderParticle(partialTick);
//
//            if (effect.isTransparent()){
//                RenderSystem.alphaFunc(GL11.GL_GREATER, 0.1F);
//            }
//        }
//    }
//
//    public void addEffect(GuiEffect effect){
//        effects.add(effect);
//    }
//
//    public void clearEffects(){
//        effects.clear();
//    }
//
//    public List<GuiEffect> getActiveEffects() {
//        return effects;
//    }
//}
//
//
