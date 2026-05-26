package com.github.alexthe666.citadel.server.message;

import com.github.alexthe666.citadel.Citadel;
import com.github.alexthe666.citadel.server.entity.CitadelEntityData;
import com.github.alexthe666.citadel.server.entity.datatracker.EntityProperties;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class PropertiesMessage implements IMessage {
   private String propertyID;
   private NBTTagCompound compound;
   private int entityID;

   public PropertiesMessage() {
   }

   public PropertiesMessage(String propertyID, NBTTagCompound compound, int entityID) {
      this.propertyID = propertyID;
      this.compound = compound;
      this.entityID = entityID;
   }

   public PropertiesMessage(EntityProperties<?> properties, Entity entity) {
      this.propertyID = properties.getID();
      NBTTagCompound compound = new NBTTagCompound();
      properties.saveTrackingSensitiveData(compound);
      this.compound = compound;
      this.entityID = entity.getEntityId();
   }

   public void fromBytes(ByteBuf buf) {
      PacketBuffer packetBuffer = new PacketBuffer(buf);
      this.propertyID = PacketBufferUtils.readUTF8String(packetBuffer);
      this.compound = PacketBufferUtils.readTag(packetBuffer);
      this.entityID = packetBuffer.readInt();
   }

   public void toBytes(ByteBuf buf) {
      PacketBuffer packetBuffer = new PacketBuffer(buf);
      PacketBufferUtils.writeUTF8String(packetBuffer, this.propertyID);
      PacketBufferUtils.writeTag(packetBuffer, this.compound);
      packetBuffer.writeInt(this.entityID);
   }

   public static class Handler implements IMessageHandler<PropertiesMessage, IMessage> {
      public IMessage onMessage(PropertiesMessage message, MessageContext context) {
         if (context.side == Side.CLIENT) {
            Citadel.PROXY.handlePropertiesPacket(message.propertyID, message.compound, message.entityID);
         } else {
            Entity e = context.getServerHandler().player.world.getEntityByID(message.entityID);
            if (e instanceof EntityLivingBase && message.propertyID.equals("CitadelPatreonConfig")) {
               CitadelEntityData.setCitadelTag((EntityLivingBase)e, message.compound);
            }
         }

         return null;
      }
   }
}
