package com.brandon3055.brandonscore.network;

import com.brandon3055.brandonscore.BrandonsCore;
import com.brandon3055.brandonscore.client.BCClientEventHandler;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

/**
 * Created by Brandon on 1/12/2014.
 */
public class PacketUpdateMount implements IMessage {

    public int entityID;

    public PacketUpdateMount() {
    }

    public PacketUpdateMount(int id) {
        this.entityID = id;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(entityID);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        entityID = buf.readInt();
    }

    public static class Handler extends MessageHandlerWrapper<PacketUpdateMount, IMessage> {

        @Override
        public IMessage handleMessage(PacketUpdateMount message, MessageContext ctx) {
            if (ctx.side.equals(Side.SERVER)) {
                if (message.entityID == -1) {
                    ctx.getServerHandler().playerEntity.dismountRidingEntity();
                    return null;
                } else if (ctx.getServerHandler().playerEntity.getRidingEntity() != null) {
                    BrandonsCore.network.sendTo(new PacketUpdateMount(ctx.getServerHandler().playerEntity.getRidingEntity().getEntityId()), ctx.getServerHandler().playerEntity);
                    return null;
                }
                return null;
            }

            BCClientEventHandler.tryRepositionPlayerOnMount(message.entityID);
            return null;
        }
    }
}
