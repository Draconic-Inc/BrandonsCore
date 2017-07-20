package com.brandon3055.brandonscore.client.gui.config;

import com.brandon3055.brandonscore.client.gui.ButtonColourRect;
import com.brandon3055.brandonscore.client.gui.modulargui.ModularGuiScreen;
import com.brandon3055.brandonscore.client.gui.modulargui.GuiElementManager;
import com.brandon3055.brandonscore.client.gui.modulargui.lib.GuiAlign;
import com.brandon3055.brandonscore.client.gui.modulargui.guielements.GuiLabel;
import com.brandon3055.brandonscore.client.gui.modulargui.needupdate.MGuiList;
import com.brandon3055.brandonscore.client.gui.modulargui.needupdate.MGuiListEntryWrapper;
import com.brandon3055.brandonscore.client.utils.GuiHelper;
import com.brandon3055.brandonscore.registry.ModConfigParser;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.text.TextFormatting;

import java.io.IOException;
import java.util.Map;

/**
 * Created by brandon3055 on 16/06/2017.
 */
public class GuiIncompatibleConfig extends ModularGuiScreen {

    private Map<ModConfigParser.PropertyHelper, Object> incompatibleProps;
    private String error = "";

    public GuiIncompatibleConfig(Map<ModConfigParser.PropertyHelper, Object> incompatibleProps) {
        this.incompatibleProps = incompatibleProps;
    }

    @Override
    public void addElements(GuiElementManager manager) {
        int cx = width / 2;
        int cy = height / 2;

        manager.clear();
        MGuiList list;
        manager.add(list = new MGuiList(cx - 230, cy - 90, 460, 100));
        list.addEntry(new MGuiListEntryWrapper(new GuiLabel(0, 0, 460, 15, "[modid:configName] - [Your Config] -> [Server Config]").setTextColour(0x00FF00).setAlignment(GuiAlign.LEFT)));
        incompatibleProps.forEach((prop, s) -> {
            String client = prop.isArray ? "{Array Value}" : prop.property.getString();
            String server = prop.isArray ? "{Array Value}" : s + "";
            list.addEntry(new MGuiListEntryWrapper(new GuiLabel(0, 0, 460, 15, TextFormatting.GOLD + prop.modid + ":" + prop.name + ":  " + TextFormatting.RED + client + TextFormatting.RESET + " -> " + TextFormatting.GREEN + server).setAlignment(GuiAlign.LEFT)));

        });

        for (int i = 0; i < 20; i++) {
            list.addEntry(new MGuiListEntryWrapper(new GuiLabel(0, 0, 460, 15, "Test")));
        }

        addButton(new ButtonColourRect(0, TextFormatting.RED + "Ignore conflict", cx - 230, cy + 80, 100, 15, 0xFF000000, 0xFF909090, 0xFFFFFFFF));
        addButton(new ButtonColourRect(1, "Disconnect from server", cx - 120, cy + 80, 150, 15, 0xFF000000, 0xFF909090, 0xFFFFFFFF));
        addButton(new ButtonColourRect(2, TextFormatting.GREEN + "Accept Server Config", cx + 40, cy + 80, 190, 15, 0xFF000000, 0xFF909090, 0xFFFFFFFF));

    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();

        int cx = width / 2;
        int cy = height / 2;

        GlStateManager.pushMatrix();
        GlStateManager.translate(cx, cy - 140, 0);
        GlStateManager.scale(2, 2, 2);
        GuiHelper.drawCenteredSplitString(fontRendererObj, "Incompatible client config detected!", 0, 0, 300, 0xFF0000, false);
        GlStateManager.popMatrix();

        GuiHelper.drawCenteredSplitString(fontRendererObj, "Brandon's Core has detected that some critical configs in your client do not match the server" + " and could not be synced automatically.\n Bellow is a list of the incompatible configs.", cx, cy - 120, 450, 0xFF0000, false);
        GuiHelper.drawCenteredSplitString(fontRendererObj, "You have the following options. You can attempt to play with your current config but this is not recommended because things will probably break." + " You can disconnect from the server, or you can accept the server config which requires a client restart.\n If you accept the server config will be automatically saved to your client and your client will shutdown." + "You then simply need to restart your client and connect to this server again.", cx, cy + 20, 460, 0xFFFFFF, false);
        GuiHelper.drawCenteredSplitString(fontRendererObj, error, cx, cy + 100, 460, 0xFF0000, false);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == 0) {
            this.mc.displayGuiScreen(null);
            if (this.mc.currentScreen == null) {
                this.mc.setIngameFocus();
            }
        }
        else if (button.id == 1) {
            this.mc.world.sendQuittingDisconnectingPacket();
            this.mc.loadWorld(null);
            this.mc.displayGuiScreen(new GuiMultiplayer(new GuiMainMenu()));
        }
        else if (button.id == 2) {
            try {
                ModConfigParser.acceptServerConfig(incompatibleProps);
                mc.shutdown();
            }
            catch (Throwable e) {
                e.printStackTrace();
                error = "Something went wrong while attempting to set config value! See log for details";
            }
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
    }
}
