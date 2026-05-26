package com.github.alexthe666.alexsmobs.message;

import com.github.alexthe666.alexsmobs.AlexsMobs;
import com.github.alexthe666.alexsmobs.entity.EntityKangaroo;
import com.github.alexthe666.citadel.server.message.PacketBufferUtils;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class MessageKangarooInventorySync implements IMessage {

    public int kangaroo;
    public int slotId;
    public ItemStack stack;

    public MessageKangarooInventorySync(int kangaroo, int slotId, ItemStack stack) {
        this.kangaroo = kangaroo;
        this.slotId = slotId;
        this.stack = stack;
    }

    public MessageKangarooInventorySync() {
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.kangaroo = buf.readInt();
        this.slotId = buf.readInt();
        this.stack = PacketBufferUtils.readItemStack(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(kangaroo);
        buf.writeInt(slotId);
        PacketBufferUtils.writeItemStack(buf, stack);
    }

    public static class Handler implements IMessageHandler<MessageKangarooInventorySync, IMessage> {

        @Override
        public IMessage onMessage(MessageKangarooInventorySync message, MessageContext ctx) {
            if (ctx.side == Side.CLIENT) {
                Minecraft.getMinecraft().addScheduledTask(() -> doWork(message, AlexsMobs.PROXY.getClientSidePlayer()));
            } else {
                EntityPlayerMP sender = ctx.getServerHandler().player;
                sender.mcServer.addScheduledTask(() -> doWork(message, sender));
            }
            return null;
        }

        private static void doWork(MessageKangarooInventorySync message, EntityPlayer player) {
            if (player == null || player.world == null) {
                return;
            }
            Entity entity = player.world.getEntityByID(message.kangaroo);
            if (entity instanceof EntityKangaroo && ((EntityKangaroo) entity).kangarooInventory != null) {
                if (message.slotId >= 0) {
                    ((EntityKangaroo) entity).kangarooInventory.setInventorySlotContents(message.slotId, message.stack);
                }
            }
        }
    }
}
