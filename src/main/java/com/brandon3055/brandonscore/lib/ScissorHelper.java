package com.brandon3055.brandonscore.lib;

import com.brandon3055.brandonscore.utils.LogHelperBC;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11;

import java.util.LinkedList;

public class ScissorHelper {

        private static LinkedList<ScissorState> prevStates = new LinkedList<>();
        private static ScissorState currentState = null;

        public static void pushScissor(int x, int y, int width, int height) {
            int xMax = x + width;
            int yMax = y + height;
            if (currentState == null) {
                Minecraft mc = Minecraft.getMinecraft();
                prevStates.add(currentState = new ScissorState(false, 0, 0, mc.displayWidth, mc.displayHeight));
            }
            else {
                prevStates.add(currentState);
            }
            currentState = new ScissorState(true, x, y, xMax, yMax, currentState).apply();
        }

        public static void popScissor() {
            ScissorState lastState = prevStates.size() > 0 ? prevStates.removeLast() : null;
            if (lastState != null) {
                currentState = lastState.apply();
                if (!currentState.enabled) {
                    currentState = null;
                }
            }
            else {
                LogHelperBC.bigError("ScissorHelper: Attempt to popScissor but Scissor state has not been set");
            }
        }

        private static class ScissorState {
            private boolean enabled;
            private int x;
            private int y;
            private int xMax;
            private int yMax;

            private ScissorState(boolean enabled, int x, int y, int xMax, int yMax, ScissorState prevState) {
                this.enabled = enabled;
                this.x = Math.max(prevState.x, x);
                this.y = Math.max(prevState.y, y);
                this.xMax = Math.min(prevState.xMax, xMax);
                this.yMax = Math.min(prevState.yMax, yMax);
                Minecraft mc = Minecraft.getMinecraft();
                if (this.x < 0) this.x = 0;
                if (this.y < 0) this.y = 0;
                if (this.xMax > mc.displayWidth) this.xMax = mc.displayWidth;
                if (this.yMax > mc.displayHeight) this.yMax = mc.displayWidth;
                if (this.xMax < this.x) this.xMax = this.x;
                if (this.yMax < this.y) this.yMax = this.y;
            }

            private ScissorState(boolean enabled, int x, int y, int xMax, int yMax) {
                this.enabled = enabled;
                this.x = x;
                this.y = y;
                this.xMax = xMax;
                this.yMax = yMax;
            }

            private ScissorState apply() {
                if (enabled) { GL11.glEnable(GL11.GL_SCISSOR_TEST); }
                else { GL11.glDisable(GL11.GL_SCISSOR_TEST); }
                GL11.glScissor(x, y, xMax - x, yMax - y);
                return this;
            }
        }
    }