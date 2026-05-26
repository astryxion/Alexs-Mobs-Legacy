package com.github.alexthe666.alexsmobs.item;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.init.Enchantments;
import net.minecraft.inventory.EntityEquipmentSlot;
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
        if (!super.canApplyAtEnchantingTable(stack, enchantment)) {
            return false;
        }
        return enchantment.type == EnumEnchantmentType.ARMOR
                && !enchantment.isCurse()
                && enchantment != Enchantments.UNBREAKING
                && enchantment != Enchantments.MENDING;
    }

    @Override
    public EntityEquipmentSlot getEquipmentSlot(ItemStack stack) {
        return EntityEquipmentSlot.FEET;
    }
}
