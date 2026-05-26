package com.github.alexthe666.alexsmobs.message;

import com.github.alexthe666.alexsmobs.AlexsMobs;
import com.github.alexthe666.alexsmobs.entity.EntityCrow;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class MessageCrowDismount implements IMessage {

    public int rider;
    public int mount;

    public MessageCrowDismount(int rider, int mount) {
        this.rider = rider;
        this.mount = mount;
    }

    public MessageCrowDismount() {
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.rider = buf.readInt();
        this.mount = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(rider);
        buf.writeInt(mount);
    }

    public static class Handler implements IMessageHandler<MessageCrowDismount, IMessage> {

        @Override
        public IMessage onMessage(MessageCrowDismount message, MessageContext ctx) {
            if (ctx.side == Side.CLIENT) {
                Minecraft.getMinecraft().addScheduledTask(() -> doWork(message, AlexsMobs.PROXY.getClientSidePlayer()));
            } else {
                EntityPlayerMP sender = ctx.getServerHandler().player;
                sender.mcServer.addScheduledTask(() -> doWork(message, sender));
            }
            return null;
        }

        private static void doWork(MessageCrowDismount message, EntityPlayer player) {
            if (player == null || player.world == null) {
                return;
            }
            Entity entity = player.world.getEntityByID(message.rider);
            Entity mountEntity = player.world.getEntityByID(message.mount);
            if (entity instanceof EntityCrow && mountEntity != null) {
                entity.dismountRidingEntity();
            }
        }
    }
}
