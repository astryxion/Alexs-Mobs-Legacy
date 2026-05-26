package com.github.alexthe666.alexsmobs.misc;

import net.minecraft.block.Block;
import net.minecraft.entity.IMerchant;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;

import java.util.Random;

public class ItemsForEmeraldsTrade implements EntityVillager.ITradeList {
    private final ItemStack sellingItem;
    private final int emeraldCount;
    private final int sellingItemCount;
    private final int maxUses;
    private final int xpValue;
    private final float priceMultiplier;

    public ItemsForEmeraldsTrade(Block sellingItem, int emeraldCount, int sellingItemCount, int maxUses, int xpValue) {
        this(new ItemStack(Item.getItemFromBlock(sellingItem)), emeraldCount, sellingItemCount, maxUses, xpValue);
    }

    public ItemsForEmeraldsTrade(Item sellingItem, int emeraldCount, int sellingItemCount, int xpValue) {
        this(new ItemStack(sellingItem), emeraldCount, sellingItemCount, 12, xpValue);
    }

    public ItemsForEmeraldsTrade(Item sellingItem, int emeraldCount, int sellingItemCount, int maxUses, int xpValue) {
        this(new ItemStack(sellingItem), emeraldCount, sellingItemCount, maxUses, xpValue);
    }

    public ItemsForEmeraldsTrade(ItemStack sellingItem, int emeraldCount, int sellingItemCount, int maxUses, int xpValue) {
        this(sellingItem, emeraldCount, sellingItemCount, maxUses, xpValue, 0.05F);
    }

    public ItemsForEmeraldsTrade(ItemStack sellingItem, int emeraldCount, int sellingItemCount, int maxUses, int xpValue, float priceMultiplier) {
        this.sellingItem = sellingItem;
        this.emeraldCount = emeraldCount;
        this.sellingItemCount = sellingItemCount;
        this.maxUses = maxUses;
        this.xpValue = xpValue;
        this.priceMultiplier = priceMultiplier;
    }

    @Override
    public void addMerchantRecipe(IMerchant merchant, MerchantRecipeList recipeList, Random random) {
        ItemStack buy = new ItemStack(Items.EMERALD, this.emeraldCount, 0);
        ItemStack sell = this.sellingItem.copy();
        sell.setCount(this.sellingItemCount);
        MerchantRecipe recipe = new MerchantRecipe(buy, ItemStack.EMPTY, sell, 0, this.maxUses);
        recipeList.add(recipe);
    }
}
