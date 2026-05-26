package com.github.alexthe666.alexsmobs.message;

import com.github.alexthe666.alexsmobs.AlexsMobs;
import com.github.alexthe666.alexsmobs.entity.EntityBaldEagle;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class MessageUpdateEagleControls implements IMessage {

    public int eagleId;
    public float rotationYaw;
    public float rotationPitch;
    public boolean chunkLoad;
    public int overEntityId;

    public MessageUpdateEagleControls(int eagleId, float rotationYaw, float rotationPitch, boolean chunkLoad, int overEntityId) {
        this.eagleId = eagleId;
        this.rotationYaw = rotationYaw;
        this.rotationPitch = rotationPitch;
        this.chunkLoad = chunkLoad;
        this.overEntityId = overEntityId;
    }

    public MessageUpdateEagleControls() {
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.eagleId = buf.readInt();
        this.rotationYaw = buf.readFloat();
        this.rotationPitch = buf.readFloat();
        this.chunkLoad = buf.readBoolean();
        this.overEntityId = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(eagleId);
        buf.writeFloat(rotationYaw);
        buf.writeFloat(rotationPitch);
        buf.writeBoolean(chunkLoad);
        buf.writeInt(overEntityId);
    }

    public static class Handler implements IMessageHandler<MessageUpdateEagleControls, IMessage> {

        @Override
        public IMessage onMessage(MessageUpdateEagleControls message, MessageContext ctx) {
            if (ctx.side == Side.SERVER) {
                EntityPlayerMP sender = ctx.getServerHandler().player;
                sender.mcServer.addScheduledTask(() -> {
                    if (sender.world == null) {
                        return;
                    }
                    Entity entity = sender.world.getEntityByID(message.eagleId);
                    if (entity instanceof EntityBaldEagle) {
                        Entity over = null;
                        if (message.overEntityId >= 0) {
                            over = sender.world.getEntityByID(message.overEntityId);
                        }
                        ((EntityBaldEagle) entity).directFromPlayer(message.rotationYaw, message.rotationPitch, message.chunkLoad, over);
                    }
                });
            }
            return null;
        }
    }
}
