package com.github.alexthe666.alexsmobs.message;

import com.github.alexthe666.alexsmobs.AlexsMobs;
import com.github.alexthe666.alexsmobs.tileentity.TileEntityCapsid;
import com.github.alexthe666.citadel.server.message.PacketBufferUtils;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class MessageUpdateCapsid implements IMessage {

    public long blockPos;
    public ItemStack heldStack;

    public MessageUpdateCapsid(long blockPos, ItemStack heldStack) {
        this.blockPos = blockPos;
        this.heldStack = heldStack;
    }

    public MessageUpdateCapsid() {
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.blockPos = buf.readLong();
        this.heldStack = PacketBufferUtils.readItemStack(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(blockPos);
        PacketBufferUtils.writeItemStack(buf, heldStack);
    }

    public static class Handler implements IMessageHandler<MessageUpdateCapsid, IMessage> {

        @Override
        public IMessage onMessage(MessageUpdateCapsid message, MessageContext ctx) {
            if (ctx.side == Side.CLIENT) {
                Minecraft.getMinecraft().addScheduledTask(() -> doWork(message, AlexsMobs.PROXY.getClientSidePlayer()));
            } else {
                EntityPlayerMP sender = ctx.getServerHandler().player;
                sender.mcServer.addScheduledTask(() -> doWork(message, sender));
            }
            return null;
        }

        private static void doWork(MessageUpdateCapsid message, EntityPlayer player) {
            if (player == null || player.world == null) {
                return;
            }
            BlockPos pos = BlockPos.fromLong(message.blockPos);
            if (player.world.getTileEntity(pos) instanceof TileEntityCapsid) {
                TileEntityCapsid podium = (TileEntityCapsid) player.world.getTileEntity(pos);
                podium.setInventorySlotContents(0, message.heldStack);
            }
        }
    }
}
