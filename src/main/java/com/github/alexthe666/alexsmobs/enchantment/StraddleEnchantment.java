package com.github.alexthe666.alexsmobs.enchantment;

import com.github.alexthe666.alexsmobs.item.ItemStraddleboard;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;

public class StraddleEnchantment extends Enchantment {

    protected StraddleEnchantment(Rarity rarity) {
        super(rarity, EnumEnchantmentType.BREAKABLE, new EntityEquipmentSlot[]{EntityEquipmentSlot.MAINHAND});
    }

    @Override
    public boolean canApply(ItemStack stack) {
        return stack.getItem() instanceof ItemStraddleboard;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return stack.getItem() instanceof ItemStraddleboard;
    }

    @Override
    public int getMinEnchantability(int level) {
        return 12 + (level + 1) * 9;
    }

    @Override
    public int getMaxEnchantability(int level) {
        return super.getMinEnchantability(level) + 30;
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }
}
