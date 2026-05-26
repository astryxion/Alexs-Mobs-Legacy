package com.github.alexthe666.alexsmobs.message;

import com.github.alexthe666.alexsmobs.AlexsMobs;
import com.github.alexthe666.alexsmobs.entity.IHurtableMultipart;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class MessageHurtMultipart implements IMessage {

    public int part;
    public int parent;
    public float damage;

    public MessageHurtMultipart(int part, int parent, float damage) {
        this.part = part;
        this.parent = parent;
        this.damage = damage;
    }

    public MessageHurtMultipart() {
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.part = buf.readInt();
        this.parent = buf.readInt();
        this.damage = buf.readFloat();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(part);
        buf.writeInt(parent);
        buf.writeFloat(damage);
    }

    public static class Handler implements IMessageHandler<MessageHurtMultipart, IMessage> {

        @Override
        public IMessage onMessage(MessageHurtMultipart message, MessageContext ctx) {
            if (ctx.side == Side.CLIENT) {
                Minecraft.getMinecraft().addScheduledTask(() -> doWork(message, AlexsMobs.PROXY.getClientSidePlayer()));
            } else {
                EntityPlayerMP sender = ctx.getServerHandler().player;
                sender.mcServer.addScheduledTask(() -> doWork(message, sender));
            }
            return null;
        }

        private static void doWork(MessageHurtMultipart message, EntityPlayer player) {
            if (player == null || player.world == null) {
                return;
            }
            Entity part = player.world.getEntityByID(message.part);
            Entity parent = player.world.getEntityByID(message.parent);
            if (part instanceof IHurtableMultipart && parent instanceof EntityLivingBase) {
                ((IHurtableMultipart) part).onAttackedFromServer((EntityLivingBase) parent, message.damage);
            }
        }
    }
}
