package com.brandon3055.brandonscore.client.gui.modulargui.modularelements;

import com.brandon3055.brandonscore.client.ResourceHelperBC;
import com.brandon3055.brandonscore.client.gui.effects.GuiEffect;
import com.brandon3055.brandonscore.client.gui.effects.GuiEffectRenderer;
import com.brandon3055.brandonscore.client.gui.modulargui.IModularGui;
import com.brandon3055.brandonscore.client.gui.modulargui.MGuiElementBase;
import net.minecraft.client.Minecraft;

/**
 * Created by brandon3055 on 18/10/2016.
 */
public class MGuiEffectRenderer extends MGuiElementBase {

    private final GuiEffectRenderer effectRenderer = new GuiEffectRenderer();
    private String particleTexture = null;

    public MGuiEffectRenderer(IModularGui modularGui) {
        super(modularGui);
    }

    public MGuiEffectRenderer setParticleTexture(String particleTexture) {
        this.particleTexture = particleTexture;
        return this;
    }

    @Override
    public void renderForegroundLayer(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
        if (particleTexture != null) {
            ResourceHelperBC.bindTexture(ResourceHelperBC.getResourceRAW(particleTexture));
        }
        effectRenderer.renderEffects(partialTicks);
        super.renderForegroundLayer(minecraft, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean onUpdate() {
        effectRenderer.updateEffects();
        return super.onUpdate();
    }

    public MGuiEffectRenderer addEffect(GuiEffect effect){
        effect.zLevel = getRenderZLevel();
        effectRenderer.addEffect(effect);
        return this;
    }

    public void clearEffects(){
        effectRenderer.clearEffects();
    }
}
