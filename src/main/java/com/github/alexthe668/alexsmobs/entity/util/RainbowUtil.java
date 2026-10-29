package com.github.alexthe668.alexsmobs.entity.util;

import com.github.alexthe668.alexsmobs.AlexsMobs;
import com.github.alexthe668.alexsmobs.config.AMConfig;
import com.github.alexthe668.alexsmobs.item.ItemRainbowJelly;
import com.github.alexthe668.alexsmobs.message.MessageRainbowSync;
import com.github.alexthe668.alexsmobs.misc.AMSimplexNoise;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

import java.awt.Color;
import java.util.Locale;

public class RainbowUtil {

    public static void setRainbowType(EntityLivingBase entity, int type) {
        setRainbowTypeLocal(entity, type);
        if (!entity.world.isRemote) {
            AlexsMobs.NETWORK_WRAPPER.sendToAll(new MessageRainbowSync(entity.getEntityId(), type));
        }
    }

    public static void setRainbowTypeLocal(EntityLivingBase entity, int type) {
        RainbowCapability.RainbowData data = RainbowCapability.get(entity);
        if (data != null) {
            data.setType(type);
        }
    }

    public static int getRainbowType(EntityLivingBase entity) {
        RainbowCapability.RainbowData data = RainbowCapability.get(entity);
        return data == null ? 0 : data.getType();
    }

    public static int getRainbowTypeFromStack(ItemStack stack) {
        String name = stack.getDisplayName().toLowerCase(Locale.ROOT);
        return ItemRainbowJelly.RainbowType.getFromString(name).ordinal() + 1;
    }

    public static int calculateGlassColor(BlockPos pos) {
        float f = (float) AMConfig.rainbowGlassFidelity;
        float f1 = (float) ((AMSimplexNoise.noise((pos.getX() + f) / f, (pos.getY() + f) / f, (pos.getZ() + f) / f) + 1.0F) * 0.5F);
        return Color.HSBtoRGB(f1, 1.0F, 1.0F);
    }
}
