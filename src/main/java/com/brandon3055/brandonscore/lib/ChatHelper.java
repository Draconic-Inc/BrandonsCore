package com.brandon3055.brandonscore.lib;

import com.brandon3055.brandonscore.BrandonsCore;
import com.brandon3055.brandonscore.utils.LogHelperBC;
import net.minecraft.Util;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/**
 * Created by brandon3055 on 15/12/2020.
 * This is just a simple helper class for chat messages.
 */
public class ChatHelper {

    /**
     * Dead simple method for sending a message to the player.
     *
     * @param player  The player.
     * @param message The message.
     */
    public static void sendMessage(Player player, Component message) {
        player.sendMessage(message, Util.NIL_UUID);
    }

    /**
     * This uses some trickery to send a message to the player using a specific index which means.
     * These messages will show up as normal however sending new messages with the same index will overwrite the
     * previous rather than adding a new message to the chat history. All messages sent to the same index will show up as if they
     * are new messages but the previous will no longer show in chat history.
     * <p>
     * This is use full when for example clicking a block adjusts some setting and you want to show the new value in chat without spamming the chat
     * as the user cycles though functions.
     * <p>
     * For the index just pick a number between -1000, and +1000. This will be converted to something outside the rage typically used by minecraft.
     * You can use the same index for everything or use different indexes for different application. Its up to you.
     *
     * @param player  The player.
     * @param message The message
     * @param index   message index.
     */
    public static void sendIndexed(Player player, Component message, int index) {
        //0xE3055000 is an arbitrary number. Combine that with the relatively limited range and chances if my indexes conflicting with
        //another mod doing something similar are pretty low. But even if there is a conflict its hardly a game breaking issue. 
        if (index < -1000 || index > 1000) LogHelperBC.bigWarn("Message index is out of bounds. Message: " + message.getString());
        BrandonsCore.proxy.sendIndexedMessage(player, message, index + 0xE3055000);
    }

    /**
     * For situations where this code will be called on both the client and the server. This ensures the message is not duplicated.
     * This only sends the server side message since the server is usually the one calling the shots.
     * 
     * If the client side message is what you want then use {@link #sendDeDupeMessageClient(PlayerEntity, ITextComponent)}
     * 
     * @param player  The player.
     * @param message The message.
     */
    public static void sendDeDupeMessage(Player player, Component message) {
        if (player instanceof ServerPlayer) {
            sendMessage(player, message);
        }
    }

    /**
     * For situations where this code will be called on both the client and the server. This ensures the message is not duplicated.
     * This only sends the server side message since the server is usually the one calling the shots.
     * 
     * If the client side message is what you want then use {@link #sendDeDupeIndexedClient(PlayerEntity, ITextComponent, int)}
     * 
     * @param player  The player.
     * @param message The message
     * @param index   message index.
     * @see #sendIndexed(PlayerEntity, ITextComponent, int) 
     */
    public static void sendDeDupeIndexed(Player player, Component message, int index) {
        if (player instanceof ServerPlayer) {
            sendIndexed(player, message, index);
        }
    }


    /**
     * For situations where this code will be called on both the client and the server. This ensures the message is not duplicated.
     * this only sends the client side message.
     *
     * @param player  The player.
     * @param message The message.
     */
    public static void sendDeDupeMessageClient(Player player, Component message) {
        if (player instanceof LocalPlayer) {
            sendMessage(player, message);
        }
    }

    /**
     * For situations where this code will be called on both the client and the server. This ensures the message is not duplicated.
     * this only sends the client side message.
     *
     * 
     * @param player  The player.
     * @param message The message
     * @param index   message index.
     * @see #sendIndexed(PlayerEntity, ITextComponent, int)
     */
    public static void sendDeDupeIndexedClient(Player player, Component message, int index) {
        if (player instanceof LocalPlayer) {
            sendIndexed(player, message, index);
        }
    }
}

