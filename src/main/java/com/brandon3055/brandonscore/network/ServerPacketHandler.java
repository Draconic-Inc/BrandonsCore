package com.brandon3055.brandonscore.network;

import codechicken.lib.packet.ICustomPacketHandler;
import codechicken.lib.packet.PacketCustom;
import com.brandon3055.brandonscore.blocks.TileBCore;
import com.brandon3055.brandonscore.inventory.ContainerPlayerAccess;
import com.brandon3055.brandonscore.lib.TeleportUtils;
import com.brandon3055.brandonscore.utils.LogHelperBC;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.play.IServerPlayNetHandler;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

public class ServerPacketHandler implements ICustomPacketHandler.IServerPacketHandler {
    @Override
    public void handlePacket(PacketCustom packet, ServerPlayerEntity sender, IServerPlayNetHandler handler) {
        switch (packet.getType()) {
            case BCoreNetwork.S_TILE_MESSAGE:
                handleTileMessage(packet, sender, handler);
                break;
            case BCoreNetwork.S_PLAYER_ACCESS_BUTTON:
                handlePlayerAccess(packet, sender, handler);
                break;
            case BCoreNetwork.S_TILE_DATA_MANAGER:
                handleTileDataManager(packet, sender, handler);
                break;
        }
    }

    private void handleTileMessage(PacketCustom packet, ServerPlayerEntity sender, IServerPlayNetHandler handler) {
        try {
            BlockPos pos = packet.readPos();
            TileEntity tile = sender.level.getBlockEntity(pos);
            if (tile instanceof TileBCore && verifyPlayerPermission(sender, pos)) {
                int id = packet.readByte() & 0xFF;
                ((TileBCore) tile).receivePacketFromClient(packet, sender, id);
            }
        }
        catch (Throwable e) {
            LogHelperBC.error("Something went wrong while attempting to read a packet sent from this client: " + sender);
            e.printStackTrace();
        }
    }

    private void handlePlayerAccess(PacketCustom packet, ServerPlayerEntity sender, IServerPlayNetHandler handler) {
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

    private void handleTileDataManager(PacketCustom packet, ServerPlayerEntity sender, IServerPlayNetHandler handler) {
        try {
            BlockPos pos = packet.readPos();
            TileEntity tile = sender.level.getBlockEntity(pos);
            if (tile instanceof TileBCore && verifyPlayerPermission(sender, pos)) {
                ((TileBCore) tile).getDataManager().receiveDataFromClient(packet);
            }
        }
        catch (Throwable e) {
            LogHelperBC.error("Something went wrong while attempting to read data manager a packet sent from this client: " + sender);
            e.printStackTrace();
        }
    }

    //This is to assist things like grief prevention. If a player is not allowed to right click a block then they probably shouldn't be allowed to sent packets to is.
    private boolean verifyPlayerPermission(PlayerEntity player, BlockPos pos) {
        PlayerInteractEvent.RightClickBlock event = new PlayerInteractEvent.RightClickBlock(player, Hand.MAIN_HAND, pos, Direction.UP);
        MinecraftForge.EVENT_BUS.post(event);
        return !event.isCanceled();
    }
}