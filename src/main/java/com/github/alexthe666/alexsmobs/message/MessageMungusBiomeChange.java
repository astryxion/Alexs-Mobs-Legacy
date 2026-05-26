package com.github.alexthe666.alexsmobs.message;

import com.github.alexthe666.alexsmobs.AlexsMobs;
import com.github.alexthe666.alexsmobs.config.AMConfig;
import com.github.alexthe666.alexsmobs.entity.EntityMungus;
import com.github.alexthe666.citadel.server.message.PacketBufferUtils;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;

public class MessageMungusBiomeChange implements IMessage {

    public int mungusID;
    public int posX;
    public int posZ;
    public String biomeOption;

    public MessageMungusBiomeChange(int mungusID, int posX, int posY, String biomeOption) {
        this.mungusID = mungusID;
        this.posX = posX;
        this.posZ = posY;
        this.biomeOption = biomeOption;
    }

    public MessageMungusBiomeChange() {
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.mungusID = buf.readInt();
        this.posX = buf.readInt();
        this.posZ = buf.readInt();
        this.biomeOption = PacketBufferUtils.readUTF8String(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(mungusID);
        buf.writeInt(posX);
        buf.writeInt(posZ);
        PacketBufferUtils.writeUTF8String(buf, biomeOption);
    }

    public static class Handler implements IMessageHandler<MessageMungusBiomeChange, IMessage> {

        @Override
        public IMessage onMessage(MessageMungusBiomeChange message, MessageContext ctx) {
            if (ctx.side == Side.CLIENT) {
                Minecraft.getMinecraft().addScheduledTask(() -> doWork(message, AlexsMobs.PROXY.getClientSidePlayer()));
            } else {
                EntityPlayerMP sender = ctx.getServerHandler().player;
                sender.mcServer.addScheduledTask(() -> doWork(message, sender));
            }
            return null;
        }

        private static void doWork(MessageMungusBiomeChange message, EntityPlayer player) {
            if (player == null || player.world == null) {
                return;
            }
            Entity entity = player.world.getEntityByID(message.mungusID);
            Biome biome = ForgeRegistries.BIOMES.getValue(new ResourceLocation(message.biomeOption));
            if (AMConfig.mungusBiomeTransformationType == 2) {
                if (entity instanceof EntityMungus && entity.getDistanceSq(message.posX, entity.posY, message.posZ) < 1000 && biome != null) {
                    Chunk chunk = player.world.getChunkFromBlockCoords(new BlockPos(message.posX, 0, message.posZ));
                    byte[] arr = chunk.getBiomeArray();
                    int id = Biome.getIdForBiome(biome) & 255;
                    java.util.Arrays.fill(arr, (byte) id);
                    chunk.markDirty();
                    AlexsMobs.PROXY.updateBiomeVisuals(message.posX, message.posZ);
                }
            }
        }
    }
}
