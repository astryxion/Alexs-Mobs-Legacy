package com.github.alexthe666.alexsmobs.message;

import com.github.alexthe666.alexsmobs.item.ItemFalconryGlove;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class MessageSwingArm implements IMessage {

    public MessageSwingArm() {
    }

    @Override
    public void fromBytes(ByteBuf buf) {
    }

    @Override
    public void toBytes(ByteBuf buf) {
    }

    public static class Handler implements IMessageHandler<MessageSwingArm, IMessage> {

        @Override
        public IMessage onMessage(MessageSwingArm message, MessageContext ctx) {
            if (ctx.side == Side.SERVER) {
                EntityPlayerMP player = ctx.getServerHandler().player;
                player.mcServer.addScheduledTask(() -> {
                    if (player != null) {
                        ItemFalconryGlove.onLeftClick(player, player.getHeldItem(EnumHand.OFF_HAND));
                        ItemFalconryGlove.onLeftClick(player, player.getHeldItem(EnumHand.MAIN_HAND));
                    }
                });
            }
            return null;
        }
    }
}
