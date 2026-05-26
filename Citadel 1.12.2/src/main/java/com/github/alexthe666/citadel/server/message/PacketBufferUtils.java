package com.github.alexthe666.citadel.server.message;

import io.netty.buffer.ByteBuf;
import java.nio.charset.StandardCharsets;
import javax.annotation.Nullable;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import org.apache.commons.lang3.Validate;

public class PacketBufferUtils {
   public static int varIntByteCount(int toCount) {
      return (toCount & -128) == 0 ? 1 : ((toCount & -16384) == 0 ? 2 : ((toCount & -2097152) == 0 ? 3 : ((toCount & -268435456) == 0 ? 4 : 5)));
   }

   public static int readVarInt(ByteBuf buf, int maxSize) {
      Validate.isTrue(maxSize < 6 && maxSize > 0, "Varint length is between 1 and 5, not %d", (long)maxSize);
      int i = 0;
      int j = 0;

      byte b0;
      do {
         b0 = buf.readByte();
         i |= (b0 & 127) << j++ * 7;
         if (j > maxSize) {
            throw new RuntimeException("VarInt too big");
         }
      } while((b0 & 128) == 128);

      return i;
   }

   public static int readVarShort(ByteBuf buf) {
      int low = buf.readUnsignedShort();
      int high = 0;
      if ((low & 32768) != 0) {
         low &= 32767;
         high = buf.readUnsignedByte();
      }

      return (high & 255) << 15 | low;
   }

   public static void writeVarShort(ByteBuf buf, int toWrite) {
      int low = toWrite & 32767;
      int high = (toWrite & 8355840) >> 15;
      if (high != 0) {
         low |= 32768;
      }

      buf.writeShort(low);
      if (high != 0) {
         buf.writeByte(high);
      }

   }

   public static void writeVarInt(ByteBuf to, int toWrite, int maxSize) {
      Validate.isTrue(varIntByteCount(toWrite) <= maxSize, "Integer is too big for %d bytes", (long)maxSize);

      while((toWrite & -128) != 0) {
         to.writeByte(toWrite & 127 | 128);
         toWrite >>>= 7;
      }

      to.writeByte(toWrite);
   }

   public static String readUTF8String(ByteBuf from) {
      int len = readVarInt(from, 2);
      String str = from.toString(from.readerIndex(), len, StandardCharsets.UTF_8);
      from.readerIndex(from.readerIndex() + len);
      return str;
   }

   public static void writeUTF8String(ByteBuf to, String string) {
      byte[] utf8Bytes = string.getBytes(StandardCharsets.UTF_8);
      Validate.isTrue(varIntByteCount(utf8Bytes.length) < 3, "The string is too long for this encoding.", new Object[0]);
      writeVarInt(to, utf8Bytes.length, 2);
      to.writeBytes(utf8Bytes);
   }

   public static void writeItemStack(ByteBuf to, ItemStack stack) {
      PacketBuffer pb = new PacketBuffer(to);
      pb.writeItemStack(stack);
   }

   public static ItemStack readItemStack(ByteBuf from) {
      PacketBuffer pb = new PacketBuffer(from);

      try {
         return pb.readItemStack();
      } catch (Exception e) {
         throw new RuntimeException(e);
      }
   }

   public static void writeTag(ByteBuf to, NBTTagCompound tag) {
      PacketBuffer pb = new PacketBuffer(to);
      pb.writeCompoundTag(tag);
   }

   @Nullable
   public static NBTTagCompound readTag(ByteBuf from) {
      PacketBuffer pb = new PacketBuffer(from);

      try {
         return pb.readCompoundTag();
      } catch (Exception e) {
         throw new RuntimeException(e);
      }
   }
}
