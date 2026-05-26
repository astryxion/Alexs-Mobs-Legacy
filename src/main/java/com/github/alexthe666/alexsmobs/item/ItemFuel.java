package com.github.alexthe666.alexsmobs.item;

import com.github.alexthe666.alexsmobs.AlexsMobs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ItemFuel extends Item {

    private final int burnTime;

    public ItemFuel(int burnTime) {
        this.burnTime = burnTime;
    }

    public int getBurnTime(ItemStack itemStack) {
        return burnTime;
    }
}
