package com.brandon3055.brandonscore.network;

import codechicken.lib.packet.ICustomPacketHandler;
import codechicken.lib.packet.PacketCustom;
import com.brandon3055.brandonscore.BCConfig;
import com.brandon3055.brandonscore.blocks.TileBCore;
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
            case BCoreNetwork.S_CONTAINER_MESSAGE:
                handleContainerMessage(packet, sender, handler);
                break;
            case BCoreNetwork.S_PLAYER_ACCESS_BUTTON:
                handlePlayerAccess(packet, sender, handler);
                break;
            case BCoreNetwork.S_TILE_DATA_MANAGER:
                handleTileDataManager(packet, sender, handler);
                break;
        }
    }

    private void handleContainerMessage(PacketCustom packet, ServerPlayer sender, ServerGamePacketListenerImpl handler) {
        try {
            if (sender.containerMenu instanceof ContainerBCTile) {
                ((ContainerBCTile<?>) sender.containerMenu).handleContainerMessage(packet, sender);
            }
        }
        catch (Throwable e) {
            LogHelperBC.error("Something went wrong while attempting to read a packet sent from this client: " + sender);
            e.printStackTrace();
        }
    }

    private void handlePlayerAccess(PacketCustom packet, ServerPlayer sender, ServerGamePacketListener handler) {
//        int button = packet.readByte();
//        if (!sender.getCommandSource().hasPermissionLevel(3)) {
//            sender.sendMessage(new StringTextComponent("You do not have permission to use that command").setStyle(new Style().setColor(TextFormatting.RED)));
//            return;
//        }
//        ContainerPlayerAccess container = sender.openContainer instanceof ContainerPlayerAccess ? (ContainerPlayerAccess) sender.openContainer : null;
//        if (container == null) return;
//        PlayerEntity other = container.playerAccess;
//        switch (button) {
//            case 0: //tp to player
//                TeleportUtils.teleportEntity(sender, other.dimension.getId(), other.posX, other.posY, other.posZ, other.rotationYaw, other.rotationPitch);
//                break;
//            case 1: //tp player to you
//                if (other instanceof OfflinePlayer) {
//                    ((OfflinePlayer) other).tpTo(sender);
//                }
//                else {
//                    TeleportUtils.teleportEntity(other, sender.dimension.getId(), sender.posX, sender.posY, sender.posZ, sender.rotationYaw, sender.rotationPitch);
//                }
//                break;
//            case 2: //clear player inventory
//                other.inventory.clear();
//                break;
//        }
    }

    private void handleTileDataManager(PacketCustom packet, ServerPlayer sender, ServerGamePacketListener handler) {
        try {
            if (sender.containerMenu instanceof ContainerBCTile<?>) {
                ((ContainerBCTile<?>) sender.containerMenu).handleTileDataPacket(packet, sender);
            }
        }
        catch (Throwable e) {
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