package com.github.alexthe668.alexsmobs.message;

import com.github.alexthe668.alexsmobs.entity.util.RainbowUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageRainbowSync implements IMessage {

    public int entityId;
    public int type;

    public MessageRainbowSync() {
    }

    public MessageRainbowSync(int entityId, int type) {
        this.entityId = entityId;
        this.type = type;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.entityId = buf.readInt();
        this.type = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.entityId);
        buf.writeInt(this.type);
    }

    public static class Handler implements IMessageHandler<MessageRainbowSync, IMessage> {
        @Override
        public IMessage onMessage(MessageRainbowSync message, MessageContext ctx) {
            Minecraft.getMinecraft().addScheduledTask(() -> {
                Entity entity = Minecraft.getMinecraft().world == null ? null : Minecraft.getMinecraft().world.getEntityByID(message.entityId);
                if (entity instanceof EntityLivingBase) {
                    RainbowUtil.setRainbowTypeLocal((EntityLivingBase) entity, message.type);
                }
            });
            return null;
        }
    }
}
