package com.github.alexthe668.alexsmobs.entity.ai;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

public interface ILootsChests {

    boolean isLootable(IInventory inventory);

    boolean shouldLootItem(ItemStack stack);
}
