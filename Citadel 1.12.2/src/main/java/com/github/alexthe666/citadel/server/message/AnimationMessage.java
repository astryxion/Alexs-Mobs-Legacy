package com.github.alexthe666.citadel.server.message;

import com.github.alexthe666.citadel.Citadel;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class AnimationMessage implements IMessage {
   private int entityID;
   private int index;

   public AnimationMessage() {
   }

   public AnimationMessage(int entityID, int index) {
      this.entityID = entityID;
      this.index = index;
   }

   public void fromBytes(ByteBuf buf) {
      PacketBuffer packetBuffer = new PacketBuffer(buf);
      this.entityID = packetBuffer.readInt();
      this.index = packetBuffer.readInt();
   }

   public void toBytes(ByteBuf buf) {
      PacketBuffer packetBuffer = new PacketBuffer(buf);
      packetBuffer.writeInt(this.entityID);
      packetBuffer.writeInt(this.index);
   }

   public static class Handler implements IMessageHandler<AnimationMessage, IMessage> {
      public IMessage onMessage(AnimationMessage message, MessageContext context) {
         Citadel.PROXY.handleAnimationPacket(message.entityID, message.index);
         return null;
      }
   }
}
