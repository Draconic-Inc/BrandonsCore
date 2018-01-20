package com.brandon3055.brandonscore.client.gui.config;

import com.brandon3055.brandonscore.client.gui.ButtonColourRect;
import com.brandon3055.brandonscore.client.gui.modulargui_old.ModularGuiScreen;
import com.brandon3055.brandonscore.client.gui.modulargui_old.lib.EnumAlignment;
import com.brandon3055.brandonscore.client.gui.modulargui_old.modularelements.MGuiLabel;
import com.brandon3055.brandonscore.client.gui.modulargui_old.modularelements.MGuiList;
import com.brandon3055.brandonscore.client.gui.modulargui_old.modularelements.MGuiListEntryWrapper;
import com.brandon3055.brandonscore.client.utils.GuiHelper;
import com.brandon3055.brandonscore.registry.ModConfigParser;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
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
    public void initGui() {
        super.initGui();

        int cx = width / 2;
        int cy = height / 2;

        manager.clear();
        MGuiList list;
        manager.add(list = new MGuiList(this, cx - 230, cy - 90, 460, 100));
        list.addEntry(new MGuiListEntryWrapper(this, new MGuiLabel(this, 0, 0, 460, 15, "[modid:configName] - [Your Config] -> [Server Config]").setTextColour(0x00FF00).setAlignment(EnumAlignment.LEFT)));
        incompatibleProps.forEach((prop, s) -> {
            String client = prop.isArray ? "{Array Value}" : prop.property.getString();
            String server = prop.isArray ? "{Array Value}" : s + "";
            list.addEntry(new MGuiListEntryWrapper(this, new MGuiLabel(this, 0, 0, 460, 15, TextFormatting.GOLD + prop.modid + ":" + prop.name + ":  " + TextFormatting.RED + client + TextFormatting.RESET + " -> " + TextFormatting.GREEN + server).setAlignment(EnumAlignment.LEFT)));

        });

        manager.initElements();

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
        GuiHelper.drawCenteredSplitString(fontRenderer, "Incompatible client config detected!", 0, 0, 300, 0xFF0000, false);
        GlStateManager.popMatrix();

        GuiHelper.drawCenteredSplitString(fontRenderer,
                "Brandon's Core has detected that some critical configs in your client do not match the server" + //
                " and could not be synced automatically.\n Bellow is a list of the incompatible configs." //
                , cx, cy - 120, 450, 0xFF0000, false);
        GuiHelper.drawCenteredSplitString(fontRenderer, //
                "You have the following options. You can attempt to play with your current config but this is not recommended because things will probably break." + //
                        " You can disconnect from the server, or you can accept the server config which requires a client restart.\n If you accept the server config properties that do not match your client will be saved to your client config." + //
                        "You then simply need to restart your client to apply the changes and connect to this server again.", cx, cy + 22, 460, 0xFFFFFF, false);
        GuiHelper.drawCenteredSplitString(fontRenderer, error, cx, cy + 100, 460, 0xFF0000, false);

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
                this.mc.displayGuiScreen(null);
                if (this.mc.currentScreen == null) {
                    this.mc.setIngameFocus();
                }
                mc.player.sendMessage(new TextComponentString("Config saved! The changes will be applied when you restart your client. You may continue playing but mods may break or not function correctly.").setStyle(new Style().setColor(TextFormatting.GREEN)));
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
