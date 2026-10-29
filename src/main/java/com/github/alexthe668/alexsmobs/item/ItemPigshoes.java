package com.github.alexthe668.alexsmobs.item;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.init.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ItemPigshoes extends Item {

    public ItemPigshoes() {
        this.setMaxStackSize(1);
    }

    @Override
    public int getItemEnchantability() {
        return 1;
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return true;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return enchantment.type == EnumEnchantmentType.ARMOR_FEET
                && !enchantment.isCurse()
                && enchantment != Enchantments.UNBREAKING
                && enchantment != Enchantments.MENDING;
    }
}
