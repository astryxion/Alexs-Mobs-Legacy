package com.github.alexthe666.alexsmobs.misc;

import net.minecraft.entity.IMerchant;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;

import java.util.Random;

/**
 * 1.12.2 equivalent of the 1.16 trade: player sells {@link #tradeItem}, receives emeralds.
 */
public class EmeraldsForItemsTrade implements EntityVillager.ITradeList {
    private final Item tradeItem;
    private final int count;
    private final int maxUses;
    private final int xpValue;
    private final float priceMultiplier;

    public EmeraldsForItemsTrade(Item tradeItem, int itemCountBuyFromPlayer, int maxUses, int xpValue) {
        this.tradeItem = tradeItem;
        this.count = itemCountBuyFromPlayer;
        this.maxUses = maxUses;
        this.xpValue = xpValue;
        this.priceMultiplier = 0.05F;
    }

    @Override
    public void addMerchantRecipe(IMerchant merchant, MerchantRecipeList recipeList, Random random) {
        ItemStack buy = new ItemStack(this.tradeItem, 1, 0);
        ItemStack sell = new ItemStack(Items.EMERALD, this.count, 0);
        MerchantRecipe recipe = new MerchantRecipe(buy, ItemStack.EMPTY, sell, 0, this.maxUses);
        recipeList.add(recipe);
    }
}
