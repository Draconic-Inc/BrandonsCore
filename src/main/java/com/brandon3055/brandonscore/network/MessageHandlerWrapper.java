package com.brandon3055.brandonscore.network;

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

/**
 * Created by brandon3055 on 23/4/2016.
 */
public abstract class MessageHandlerWrapper<REQ extends IMessage, REPLY extends IMessage> implements IMessageHandler<REQ, REPLY> {

    @Override
    public REPLY onMessage(REQ message, MessageContext ctx) {

        PacketSyncObject<REQ, REPLY> syncObject = new PacketSyncObject<REQ, REPLY>(message, ctx) {
            @Override
            public void run() {
                reply = handleMessage(message, ctx);
            }
        };

        if (ctx.side == Side.CLIENT) {
            syncObject.addPacketClient();
        } else {
            syncObject.addPacketServer();
        }

        //TODO Find a way to handle replies (when needed) because this dose not actually work
        return syncObject.reply;
    }

    public abstract REPLY handleMessage(REQ message, MessageContext ctx);

}
