package com.brandon3055.brandonscore.network;

import codechicken.lib.packet.ICustomPacketHandler;
import codechicken.lib.packet.PacketCustom;
import com.brandon3055.brandonscore.BCConfig;
import com.brandon3055.brandonscore.blocks.TileBCore;
import com.brandon3055.brandonscore.handlers.contributor.ContributorHandler;
import com.brandon3055.brandonscore.inventory.ContainerBCTile;
import com.brandon3055.brandonscore.utils.LogHelperBC;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;

public class ServerPacketHandler implements ICustomPacketHandler.IServerPacketHandler {

    @Override
    public void handlePacket(PacketCustom packet, ServerPlayer sender, ServerGamePacketListenerImpl handler) {
        switch (packet.getType()) {
            case BCoreNetwork.S_CONTAINER_MESSAGE -> handleContainerMessage(packet, sender, handler);
            case BCoreNetwork.S_TILE_DATA_MANAGER -> handleTileDataManager(packet, sender, handler);
            case BCoreNetwork.S_CONTRIBUTOR_CONFIG -> ContributorHandler.handleSettingsFromClient(sender, packet);
            case BCoreNetwork.S_CONTRIBUTOR_LINK -> ContributorHandler.handleClientLink(sender);
        }
    }

    private void handleContainerMessage(PacketCustom packet, ServerPlayer sender, ServerGamePacketListenerImpl handler) {
        try {
            if (sender.containerMenu instanceof ContainerBCTile) {
                ((ContainerBCTile<?>) sender.containerMenu).handleContainerMessage(packet, sender);
            }
        } catch (Throwable e) {
            LogHelperBC.error("Something went wrong while attempting to read a packet sent from this client: " + sender);
            e.printStackTrace();
        }
    }

    private void handleTileDataManager(PacketCustom packet, ServerPlayer sender, ServerGamePacketListener handler) {
        try {
            if (sender.containerMenu instanceof ContainerBCTile<?>) {
                ((ContainerBCTile<?>) sender.containerMenu).handleTileDataPacket(packet, sender);
            }
        } catch (Throwable e) {
            LogHelperBC.error("Something went wrong while attempting to read data manager a packet sent from this client: " + sender);
            e.printStackTrace();
        }
    }

    //This is to assist things like grief prevention. If a player is not allowed to right click a block then they are not allowed to send packets to it.
    @Deprecated //Anything requiring player permission should go through the open container
    public static boolean verifyPlayerPermission(Player player, BlockPos pos) {
        if (!BCConfig.clientPermissionVerification) return true;
        BlockHitResult traceResult = new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false);
        PlayerInteractEvent.RightClickBlock event = new PlayerInteractEvent.RightClickBlock(player, InteractionHand.MAIN_HAND, pos, traceResult);
        MinecraftForge.EVENT_BUS.post(event);
        return event.getResult() != Event.Result.DENY;
    }
}