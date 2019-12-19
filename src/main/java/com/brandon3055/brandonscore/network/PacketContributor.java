package com.brandon3055.brandonscore.network;

import com.brandon3055.brandonscore.lib.ModContributorHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

/**
 * Created by brandon3055 on 23/4/2016.
 */
public class PacketContributor implements IMessage {

    private String modid;
    private String contributor;
    private CompoundNBT data;

    public PacketContributor() {
    }

    public PacketContributor(String modid, String contributor, CompoundNBT data) {
        this.modid = modid;
        this.contributor = contributor;
        this.data = data;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, modid);
        ByteBufUtils.writeUTF8String(buf, contributor);
        ByteBufUtils.writeTag(buf, data);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        modid = ByteBufUtils.readUTF8String(buf);
        contributor = ByteBufUtils.readUTF8String(buf);
        data = ByteBufUtils.readTag(buf);
    }

    public static class Handler extends MessageHandlerWrapper<PacketContributor, IMessage> {

        @Override
        public IMessage handleMessage(PacketContributor message, MessageContext ctx) {
            if (!ModContributorHandler.MOD_CONTRIBUTOR_HANDLERS.containsKey(message.modid)) {
                return null;
            }

            ModContributorHandler handler = ModContributorHandler.MOD_CONTRIBUTOR_HANDLERS.get(message.modid);

            if (ctx.side == Side.CLIENT) {
                handler.configReceivedClient(message.data, message.contributor);
            }
            else {
                handler.configReceivedServer(message.data, message.contributor, ctx.getServerHandler().player);
            }

            return null;
        }
    }
}
