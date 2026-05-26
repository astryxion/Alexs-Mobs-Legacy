package com.github.alexthe666.alexsmobs.message;

import com.github.alexthe666.alexsmobs.AlexsMobs;
import com.github.alexthe666.alexsmobs.effect.AMEffectRegistry;
import com.github.alexthe666.alexsmobs.entity.EntityTarantulaHawk;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.potion.PotionEffect;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class MessageTarantulaHawkSting implements IMessage {

    public int hawk;
    public int spider;

    public MessageTarantulaHawkSting(int rider, int mount) {
        this.hawk = rider;
        this.spider = mount;
    }

    public MessageTarantulaHawkSting() {
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.hawk = buf.readInt();
        this.spider = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(hawk);
        buf.writeInt(spider);
    }

    public static class Handler implements IMessageHandler<MessageTarantulaHawkSting, IMessage> {

        @Override
        public IMessage onMessage(MessageTarantulaHawkSting message, MessageContext ctx) {
            if (ctx.side == Side.CLIENT) {
                Minecraft.getMinecraft().addScheduledTask(() -> doWork(message, AlexsMobs.PROXY.getClientSidePlayer()));
            } else {
                EntityPlayerMP sender = ctx.getServerHandler().player;
                sender.mcServer.addScheduledTask(() -> doWork(message, sender));
            }
            return null;
        }

        private static void doWork(MessageTarantulaHawkSting message, EntityPlayer player) {
            if (player == null || player.world == null) {
                return;
            }
            Entity entity = player.world.getEntityByID(message.hawk);
            Entity spider = player.world.getEntityByID(message.spider);
            if (entity instanceof EntityTarantulaHawk && spider instanceof EntityLivingBase && ((EntityLivingBase) spider).getCreatureAttribute() == EnumCreatureAttribute.ARTHROPOD) {
                ((EntityLivingBase) spider).addPotionEffect(new PotionEffect(AMEffectRegistry.DEBILITATING_STING, EntityTarantulaHawk.STING_DURATION));
            }
        }
    }
}
