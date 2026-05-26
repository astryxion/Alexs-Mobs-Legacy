package com.github.alexthe666.alexsmobs.message;

import com.github.alexthe666.alexsmobs.AlexsMobs;
import com.github.alexthe666.alexsmobs.entity.EntityKangaroo;
import com.github.alexthe666.citadel.server.message.PacketBufferUtils;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumParticleTypes;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class MessageKangarooEat implements IMessage {

    public int kangaroo;
    public ItemStack stack;

    public MessageKangarooEat(int kangaroo, ItemStack stack) {
        this.kangaroo = kangaroo;
        this.stack = stack;
    }

    public MessageKangarooEat() {
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.kangaroo = buf.readInt();
        this.stack = PacketBufferUtils.readItemStack(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(kangaroo);
        PacketBufferUtils.writeItemStack(buf, stack);
    }

    public static class Handler implements IMessageHandler<MessageKangarooEat, IMessage> {

        @Override
        public IMessage onMessage(MessageKangarooEat message, MessageContext ctx) {
            if (ctx.side == Side.CLIENT) {
                Minecraft.getMinecraft().addScheduledTask(() -> doWork(message, AlexsMobs.PROXY.getClientSidePlayer()));
            } else {
                EntityPlayerMP sender = ctx.getServerHandler().player;
                sender.mcServer.addScheduledTask(() -> doWork(message, sender));
            }
            return null;
        }

        private static void doWork(MessageKangarooEat message, EntityPlayer player) {
            if (player == null || player.world == null) {
                return;
            }
            Entity entity = player.world.getEntityByID(message.kangaroo);
            if (entity instanceof EntityKangaroo && ((EntityKangaroo) entity).kangarooInventory != null) {
                EntityKangaroo kangaroo = (EntityKangaroo) entity;
                for (int i = 0; i < 7; i++) {
                    double d2 = kangaroo.getRNG().nextGaussian() * 0.02D;
                    double d0 = kangaroo.getRNG().nextGaussian() * 0.02D;
                    double d1 = kangaroo.getRNG().nextGaussian() * 0.02D;
                    double px = entity.posX + (double) (kangaroo.getRNG().nextFloat() * entity.width) - (double) entity.width * 0.5F;
                    double py = entity.posY + entity.height * 0.5F + (double) (kangaroo.getRNG().nextFloat() * entity.height * 0.5F);
                    double pz = entity.posZ + (double) (kangaroo.getRNG().nextFloat() * entity.width) - (double) entity.width * 0.5F;
                    entity.world.spawnParticle(EnumParticleTypes.ITEM_CRACK, px, py, pz, d0, d1, d2, Item.getIdFromItem(message.stack.getItem()), message.stack.getMetadata());
                }
            }
        }
    }
}
