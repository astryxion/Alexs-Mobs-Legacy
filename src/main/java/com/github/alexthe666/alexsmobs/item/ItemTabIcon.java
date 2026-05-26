package com.github.alexthe666.alexsmobs.item;

import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import javax.annotation.Nullable;

public class ItemTabIcon extends ItemAMInternal {

    public ItemTabIcon() {
        setMaxStackSize(1);
    }

    public static boolean hasCustomEntityDisplay(ItemStack stack) {
        return stack.getTagCompound() != null && stack.getTagCompound().hasKey("DisplayEntityType");
    }

    public static String getCustomDisplayEntityString(ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();
        return tag != null ? tag.getString("DisplayEntityType") : "";
    }

    /**
     * Stored registry id (e.g. {@code alexsmobs:grizzly_bear}) for icon TEISR; 1.16 used {@code EntityType}.
     */
    @Nullable
    public static Class<? extends Entity> getEntityType(@Nullable NBTTagCompound tag) {
        if (tag == null || !tag.hasKey("DisplayEntityType")) {
            return null;
        }
        String raw = tag.getString("DisplayEntityType");
        try {
            ResourceLocation rl = new ResourceLocation(raw);
            EntityEntry entry = ForgeRegistries.ENTITIES.getValue(rl);
            if (entry != null) {
                return entry.getEntityClass();
            }
        } catch (RuntimeException ignored) {
        }
        return null;
    }
}
