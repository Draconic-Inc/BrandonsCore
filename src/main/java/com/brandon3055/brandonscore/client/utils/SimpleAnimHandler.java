package com.brandon3055.brandonscore.client.utils;

import codechicken.lib.math.MathHelper;
import com.brandon3055.brandonscore.BrandonsCore;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableMap;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.animation.Animation;
import net.minecraftforge.common.animation.ITimeValue;
import net.minecraftforge.common.model.animation.IAnimationStateMachine;

/**
 * Created by brandon3055 on 3/11/19.
 */
public class SimpleAnimHandler {

    public final IAnimationStateMachine asm;
    private final VariableTickValue animPos = new VariableTickValue(0F);
    private float position = 0;
    private float speed = 0;
    private float targetSpeed;
    private float transitionRate;

    public SimpleAnimHandler(ResourceLocation asm) {
        this.asm = BrandonsCore.proxy.loadASM(asm, ImmutableMap.of("anim_pos", animPos));
    }

    public void setSpeed(float targetSpeed, float transitionRate) {
        this.targetSpeed = targetSpeed * 0.1F;
        this.transitionRate = transitionRate * 0.005F;
    }

    public void updateAnimation() {
        if (speed != targetSpeed) {
            speed = MathHelper.approachLinear(speed, targetSpeed, transitionRate);
        }
        position += speed;
        animPos.setValue(position, speed);
    }

    public static final class VariableTickValue implements ITimeValue {
        private float output;
        private float modifier;

        public VariableTickValue(float initialValue) {
            this.output = initialValue;
        }

        public void setValue(float newValue, float modifier) {
            this.output = newValue;
            this.modifier = modifier;
        }

        @Override
        public float apply(float input) {
            return output + (Animation.getPartialTickTime() * modifier);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(output, modifier);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null) return false;
            if (getClass() != obj.getClass()) return false;
            VariableTickValue other = (VariableTickValue) obj;
            return output == other.output && modifier == other.modifier;
        }
    }
}
