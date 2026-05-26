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

public class MessageCrowMountPlayer implements IMessage {

    public int rider;
    public int mount;

    public MessageCrowMountPlayer(int rider, int mount) {
        this.rider = rider;
        this.mount = mount;
    }

    public MessageCrowMountPlayer() {
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

    public static class Handler implements IMessageHandler<MessageCrowMountPlayer, IMessage> {

        @Override
        public IMessage onMessage(MessageCrowMountPlayer message, MessageContext ctx) {
            if (ctx.side == Side.CLIENT) {
                Minecraft.getMinecraft().addScheduledTask(() -> doWork(message, AlexsMobs.PROXY.getClientSidePlayer()));
            } else {
                EntityPlayerMP sender = ctx.getServerHandler().player;
                sender.mcServer.addScheduledTask(() -> doWork(message, sender));
            }
            return null;
        }

        private static void doWork(MessageCrowMountPlayer message, EntityPlayer player) {
            if (player == null || player.world == null) {
                return;
            }
            Entity entity = player.world.getEntityByID(message.rider);
            Entity mountEntity = player.world.getEntityByID(message.mount);
            if (entity instanceof EntityCrow && mountEntity instanceof EntityPlayer && entity.getDistance(mountEntity) < 16D) {
                entity.startRiding(mountEntity, true);
            }
        }
    }
}
