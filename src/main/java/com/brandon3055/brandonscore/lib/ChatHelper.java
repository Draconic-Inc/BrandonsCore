package com.brandon3055.brandonscore.lib;

import com.brandon3055.brandonscore.BrandonsCore;
import com.brandon3055.brandonscore.network.BCoreNetwork;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

/**
 * Created by brandon3055 on 28/11/2016.
 * This is just a simple helper class for chat messages.
 */
@Deprecated //This is just too much crap that never gets used. Not sure what i am going to do about it yet.
public class ChatHelper {

    //region Translation

    public static void tranServer(PlayerEntity player, String unlocalizedMessage, Object... args) {
        if (!player.getEntityWorld().isRemote) {
            translate(player, unlocalizedMessage, args);
        }
    }

    public static void tranServer(PlayerEntity player, String unlocalizedMessage, TextFormatting colour, Object... args) {
        if (!player.getEntityWorld().isRemote) {
            translate(player, unlocalizedMessage, colour, args);
        }
    }

    public static void tranServer(PlayerEntity player, String unlocalizedMessage, Style style, Object... args) {
        if (!player.getEntityWorld().isRemote) {
            translate(player, unlocalizedMessage, style, args);
        }
    }

    public static void tranClient(PlayerEntity player, String unlocalizedMessage, Object... args) {
        if (player.getEntityWorld().isRemote) {
            translate(player, unlocalizedMessage, args);
        }
    }

    public static void tranClient(PlayerEntity player, String unlocalizedMessage, TextFormatting colour, Object... args) {
        if (player.getEntityWorld().isRemote) {
            translate(player, unlocalizedMessage, colour, args);
        }
    }

    public static void tranClient(PlayerEntity player, String unlocalizedMessage, Style style, Object... args) {
        if (player.getEntityWorld().isRemote) {
            translate(player, unlocalizedMessage, style, args);
        }
    }

    public static void translate(PlayerEntity player, String unlocalizedMessage, Object... args) {
        player.sendMessage(new TranslationTextComponent(unlocalizedMessage, args));
    }

    public static void translate(PlayerEntity player, String unlocalizedMessage, TextFormatting colour, Object... args) {
        player.sendMessage(new TranslationTextComponent(unlocalizedMessage, args).setStyle(new Style().setColor(colour)));
    }

    public static void translate(PlayerEntity player, String unlocalizedMessage, Style style, Object... args) {
        player.sendMessage(new TranslationTextComponent(unlocalizedMessage, args).setStyle(style));
    }

    public static void indexedTrans(PlayerEntity player, String unlocalizedMessage, int index, Object... args) {
        if (player.getEntityWorld().isRemote) {
            BrandonsCore.proxy.setChatAtIndex(new TranslationTextComponent(unlocalizedMessage, args), index);
        }
        else if (player instanceof ServerPlayerEntity){
            BCoreNetwork.sendIndexedLocalizedChat((ServerPlayerEntity) player, unlocalizedMessage, index);
        }
    }

    /**
     * Client side use only!
     */
    public static void indexedTrans(PlayerEntity player, String unlocalizedMessage, TextFormatting colour, Object... args) {
        if (player.getEntityWorld().isRemote) {
            BrandonsCore.proxy.setChatAtIndex(new TranslationTextComponent(unlocalizedMessage, args).setStyle(new Style().setColor(colour)), -330553055);
        }
    }

    /**
     * Client side use only!
     */
    public static void indexedTrans(PlayerEntity player, String unlocalizedMessage, Style style, Object... args) {
        if (player.getEntityWorld().isRemote) {
            BrandonsCore.proxy.setChatAtIndex(new TranslationTextComponent(unlocalizedMessage, args).setStyle(style), -330553055);
        }
    }

    //endregion

    //region nonTranslation

    public static void msgServer(PlayerEntity player, String message) {
        if (!player.getEntityWorld().isRemote) {
            message(player, message);
        }
    }

    public static void msgServer(PlayerEntity player, String message, TextFormatting colour) {
        if (!player.getEntityWorld().isRemote) {
            message(player, message, colour);
        }
    }

    public static void msgServer(PlayerEntity player, String message, Style style) {
        if (!player.getEntityWorld().isRemote) {
            message(player, message, style);
        }
    }

    public static void msgClient(PlayerEntity player, String message) {
        if (player.getEntityWorld().isRemote) {
            message(player, message);
        }
    }

    public static void msgClient(PlayerEntity player, String message, TextFormatting colour) {
        if (player.getEntityWorld().isRemote) {
            message(player, message, colour);
        }
    }

    public static void msgClient(PlayerEntity player, String message, Style style) {
        if (player.getEntityWorld().isRemote) {
            message(player, message, style);
        }
    }

    public static void message(PlayerEntity player, String message) {
        player.sendMessage(new TranslationTextComponent(message));
    }

    public static void message(PlayerEntity player, String message, TextFormatting colour) {
        player.sendMessage(new TranslationTextComponent(message).setStyle(new Style().setColor(colour)));
    }

    public static void message(PlayerEntity player, String message, Style style) {
        player.sendMessage(new TranslationTextComponent(message).setStyle(style));
    }

    public static void indexedMsg(PlayerEntity player, String message, int index) {
        if (player.getEntityWorld().isRemote) {
            BrandonsCore.proxy.setChatAtIndex(new TranslationTextComponent(message), index);
        }
    }

    /**
     * Client side use only!
     */
    public static void indexedMsg(PlayerEntity player, String message) {
        if (player.getEntityWorld().isRemote) {
            BrandonsCore.proxy.setChatAtIndex(new TranslationTextComponent(message), -330553055);
        }
    }

    /**
     * Client side use only!
     */
    public static void indexedMsg(PlayerEntity player, String message, TextFormatting colour) {
        if (player.getEntityWorld().isRemote) {
            BrandonsCore.proxy.setChatAtIndex(new TranslationTextComponent(message).setStyle(new Style().setColor(colour)), -330553055);
        }
    }

    /**
     * Client side use only!
     */
    public static void indexedMsg(PlayerEntity player, String message, Style style) {
        if (player.getEntityWorld().isRemote) {
            BrandonsCore.proxy.setChatAtIndex(new TranslationTextComponent(message).setStyle(style), -330553055);
        }
    }

    //endregion
}

