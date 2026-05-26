package com.github.alexthe666.alexsmobs.enchantment;

import com.github.alexthe666.alexsmobs.item.ItemStraddleboard;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;

public class StraddleJumpEnchantment extends Enchantment {

    protected StraddleJumpEnchantment(Rarity rarity) {
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
    public int getMinEnchantability(int enchantmentLevel) {
        return 15 + (enchantmentLevel - 1) * 9;
    }

    @Override
    public int getMaxEnchantability(int enchantmentLevel) {
        return super.getMinEnchantability(enchantmentLevel) + 50;
    }

    @Override
    public int getMaxLevel() {
        return 3;
    }
}
