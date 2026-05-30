package com.github.alexthe666.alexsmobs.entity.util;

import com.github.alexthe666.alexsmobs.config.AMConfig;
import com.github.alexthe666.alexsmobs.item.ItemRainbowJelly;
import com.github.alexthe666.alexsmobs.misc.AMSimplexNoise;
import com.github.alexthe666.citadel.Citadel;
import com.github.alexthe666.citadel.server.entity.CitadelEntityData;
import com.github.alexthe666.citadel.server.message.PropertiesMessage;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

import java.awt.Color;
import java.util.Locale;

public class RainbowUtil {

    private static final String RAINBOW_TYPE = "RainbowTypeAlexsMobs";

    public static void setRainbowType(EntityLivingBase entity, int type) {
        NBTTagCompound tag = CitadelEntityData.getOrCreateCitadelTag(entity);
        tag.setInteger(RAINBOW_TYPE, type);
        CitadelEntityData.setCitadelTag(entity, tag);
        if (!entity.world.isRemote) {
            Citadel.sendMSGToAll(new PropertiesMessage("CitadelPatreonConfig", tag, entity.getEntityId()));
        } else {
            Citadel.sendMSGToServer(new PropertiesMessage("CitadelPatreonConfig", tag, entity.getEntityId()));
        }
    }

    public static int getRainbowType(EntityLivingBase entity) {
        NBTTagCompound tag = CitadelEntityData.getOrCreateCitadelTag(entity);
        if (tag.hasKey(RAINBOW_TYPE)) {
            return tag.getInteger(RAINBOW_TYPE);
        }
        return 0;
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
