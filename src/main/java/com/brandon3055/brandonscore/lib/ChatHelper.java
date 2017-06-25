package com.brandon3055.brandonscore.lib;

import com.brandon3055.brandonscore.BrandonsCore;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

/**
 * Created by brandon3055 on 28/11/2016.
 * This is just a simple helper class for chat messages.
 */
public class ChatHelper {

    //region Translation

    public static void tranServer(ICommandSender player, String unlocalizedMessage, Object... args) {
        if (!player.getEntityWorld().isRemote) {
            translate(player, unlocalizedMessage, args);
        }
    }

    public static void tranServer(ICommandSender player, String unlocalizedMessage, TextFormatting colour, Object... args) {
        if (!player.getEntityWorld().isRemote) {
            translate(player, unlocalizedMessage, colour, args);
        }
    }

    public static void tranServer(ICommandSender player, String unlocalizedMessage, Style style, Object... args) {
        if (!player.getEntityWorld().isRemote) {
            translate(player, unlocalizedMessage, style, args);
        }
    }

    public static void tranClient(ICommandSender player, String unlocalizedMessage, Object... args) {
        if (player.getEntityWorld().isRemote) {
            translate(player, unlocalizedMessage, args);
        }
    }

    public static void tranClient(ICommandSender player, String unlocalizedMessage, TextFormatting colour, Object... args) {
        if (player.getEntityWorld().isRemote) {
            translate(player, unlocalizedMessage, colour, args);
        }
    }

    public static void tranClient(ICommandSender player, String unlocalizedMessage, Style style, Object... args) {
        if (player.getEntityWorld().isRemote) {
            translate(player, unlocalizedMessage, style, args);
        }
    }

    public static void translate(ICommandSender player, String unlocalizedMessage, Object... args) {
        player.sendMessage(new TextComponentTranslation(unlocalizedMessage, args));
    }

    public static void translate(ICommandSender player, String unlocalizedMessage, TextFormatting colour, Object... args) {
        player.sendMessage(new TextComponentTranslation(unlocalizedMessage, args).setStyle(new Style().setColor(colour)));
    }

    public static void translate(ICommandSender player, String unlocalizedMessage, Style style, Object... args) {
        player.sendMessage(new TextComponentTranslation(unlocalizedMessage, args).setStyle(style));
    }

    /**
     * Client side use only!
     */
    public static void indexedTrans(ICommandSender player, String unlocalizedMessage, int index, Object... args) {
        if (player.getEntityWorld().isRemote) {
            BrandonsCore.proxy.setChatAtIndex(new TextComponentTranslation(unlocalizedMessage, args), index);
        }
    }

    /**
     * Client side use only!
     */
    public static void indexedTrans(ICommandSender player, String unlocalizedMessage, TextFormatting colour, int index, Object... args) {
        if (player.getEntityWorld().isRemote) {
            BrandonsCore.proxy.setChatAtIndex(new TextComponentTranslation(unlocalizedMessage, args).setStyle(new Style().setColor(colour)), index);
        }
    }

    /**
     * Client side use only!
     */
    public static void indexedTrans(ICommandSender player, String unlocalizedMessage, Style style, int index, Object... args) {
        if (player.getEntityWorld().isRemote) {
            BrandonsCore.proxy.setChatAtIndex(new TextComponentTranslation(unlocalizedMessage, args).setStyle(style), index);
        }
    }

    //endregion

    //region nonTranslation

    public static void msgServer(ICommandSender player, String message) {
        if (!player.getEntityWorld().isRemote) {
            message(player, message);
        }
    }

    public static void msgServer(ICommandSender player, String message, TextFormatting colour) {
        if (!player.getEntityWorld().isRemote) {
            message(player, message, colour);
        }
    }

    public static void msgServer(ICommandSender player, String message, Style style) {
        if (!player.getEntityWorld().isRemote) {
            message(player, message, style);
        }
    }

    public static void msgClient(ICommandSender player, String message) {
        if (player.getEntityWorld().isRemote) {
            message(player, message);
        }
    }

    public static void msgClient(ICommandSender player, String message, TextFormatting colour) {
        if (player.getEntityWorld().isRemote) {
            message(player, message, colour);
        }
    }

    public static void msgClient(ICommandSender player, String message, Style style) {
        if (player.getEntityWorld().isRemote) {
            message(player, message, style);
        }
    }

    public static void message(ICommandSender player, String message) {
        player.sendMessage(new TextComponentString(message));
    }

    public static void message(ICommandSender player, String message, TextFormatting colour) {
        player.sendMessage(new TextComponentString(message).setStyle(new Style().setColor(colour)));
    }

    public static void message(ICommandSender player, String message, Style style) {
        player.sendMessage(new TextComponentString(message).setStyle(style));
    }

    /**
     * Client side use only!
     */
    public static void indexedMsg(ICommandSender player, String message, int index) {
        if (player.getEntityWorld().isRemote) {
            BrandonsCore.proxy.setChatAtIndex(new TextComponentString(message), index);
        }
    }

    /**
     * Client side use only!
     */
    public static void indexedMsg(ICommandSender player, String message, TextFormatting colour, int index) {
        if (player.getEntityWorld().isRemote) {
            BrandonsCore.proxy.setChatAtIndex(new TextComponentString(message).setStyle(new Style().setColor(colour)), index);
        }
    }

    /**
     * Client side use only!
     */
    public static void indexedMsg(ICommandSender player, String message, Style style, int index) {
        if (player.getEntityWorld().isRemote) {
            BrandonsCore.proxy.setChatAtIndex(new TextComponentString(message).setStyle(style), index);
        }
    }

    //endregion
}

