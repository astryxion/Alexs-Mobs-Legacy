package com.github.alexthe668.alexsmobs.misc;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import javax.annotation.Nullable;

/**
 * Optional Future MC lookups. Missing entries stay null so the mod still loads without Future MC.
 */
public final class FutureMcCompat {

    private static boolean lookedUp;
    @Nullable
    private static Item honeyBottle;
    @Nullable
    private static Item honeycomb;
    @Nullable
    private static Item honeyBlockItem;
    @Nullable
    private static Block beehive;
    @Nullable
    private static Block beeNest;
    @Nullable
    private static Block honeyBlock;

    private FutureMcCompat() {
    }

    private static void lookup() {
        if (lookedUp) {
            return;
        }
        lookedUp = true;
        honeyBottle = item("honey_bottle");
        honeycomb = item("honeycomb");
        honeyBlockItem = item("honey_block");
        beehive = block("beehive");
        beeNest = block("bee_nest");
        honeyBlock = block("honey_block");
    }

    @Nullable
    private static Item item(String path) {
        return ForgeRegistries.ITEMS.getValue(new ResourceLocation("futuremc", path));
    }

    @Nullable
    private static Block block(String path) {
        return ForgeRegistries.BLOCKS.getValue(new ResourceLocation("futuremc", path));
    }

    public static boolean isHoneyItem(@Nullable Item item) {
        if (item == null) {
            return false;
        }
        lookup();
        return item == honeyBottle || item == honeycomb || item == honeyBlockItem;
    }

    @Nullable
    public static Item getHoneyBottle() {
        lookup();
        return honeyBottle;
    }

    public static boolean isBeeHive(@Nullable Block block) {
        if (block == null) {
            return false;
        }
        lookup();
        return block == beehive || block == beeNest || block == honeyBlock;
    }

    public static boolean isBee(@Nullable Entity entity) {
        if (entity == null) {
            return false;
        }
        ResourceLocation key = EntityList.getKey(entity);
        return key != null && "futuremc".equals(key.getResourceDomain()) && "bee".equals(key.getResourcePath());
    }
}
