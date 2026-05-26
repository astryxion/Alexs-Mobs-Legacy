package com.github.alexthe666.alexsmobs.misc;

import com.github.alexthe666.alexsmobs.AlexsMobs;
import com.github.alexthe666.alexsmobs.config.AMConfig;
import com.github.alexthe666.alexsmobs.item.AMItemRegistry;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Enchantments;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.common.crafting.IRecipeFactory;
import net.minecraftforge.common.crafting.JsonContext;
import net.minecraftforge.registries.IForgeRegistryEntry;

import java.util.Map;

public class RecipeMimicreamRepair extends IForgeRegistryEntry.Impl<IRecipe> implements IRecipe {

    public RecipeMimicreamRepair() {
        // Registry name is assigned by Forge from assets/alexsmobs/recipes/mimicream_repair_recipe.json
    }

    @Override
    public boolean matches(InventoryCrafting inv, World worldIn) {
        if (!AMConfig.mimicreamRepair) {
            return false;
        }
        ItemStack damageableStack = ItemStack.EMPTY;
        int mimicreamCount = 0;
        for (int j = 0; j < inv.getSizeInventory(); ++j) {
            ItemStack itemstack1 = inv.getStackInSlot(j);
            if (!itemstack1.isEmpty()) {
                if (itemstack1.isItemStackDamageable() && !isBlacklisted(itemstack1)) {
                    damageableStack = itemstack1;
                } else if (itemstack1.getItem() == AMItemRegistry.MIMICREAM) {
                    mimicreamCount++;
                }
            }
        }
        return !damageableStack.isEmpty() && mimicreamCount >= 8;
    }

    public boolean isBlacklisted(ItemStack stack) {
        String name = stack.getItem().getRegistryName().toString();
        return AMConfig.mimicreamBlacklist.contains(name);
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inv) {
        ItemStack damageableStack = ItemStack.EMPTY;
        int mimicreamCount = 0;
        for (int j = 0; j < inv.getSizeInventory(); ++j) {
            ItemStack itemstack1 = inv.getStackInSlot(j);
            if (!itemstack1.isEmpty()) {
                if (itemstack1.isItemStackDamageable() && !isBlacklisted(itemstack1)) {
                    damageableStack = itemstack1;
                } else if (itemstack1.getItem() == AMItemRegistry.MIMICREAM) {
                    mimicreamCount++;
                }
            }
        }
        if (damageableStack.isEmpty() || mimicreamCount < 8) {
            return ItemStack.EMPTY;
        }
        ItemStack itemstack2 = damageableStack.copy();
        Map<Enchantment, Integer> enchants = EnchantmentHelper.getEnchantments(itemstack2);
        enchants.remove(Enchantments.MENDING);
        EnchantmentHelper.setEnchantments(enchants, itemstack2);
        itemstack2.setItemDamage(itemstack2.getMaxDamage());
        return itemstack2;
    }

    @Override
    public ItemStack getRecipeOutput() {
        return ItemStack.EMPTY;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(InventoryCrafting inv) {
        NonNullList<ItemStack> list = NonNullList.withSize(inv.getSizeInventory(), ItemStack.EMPTY);
        for (int i = 0; i < list.size(); ++i) {
            ItemStack itemstack = inv.getStackInSlot(i);
            if (itemstack.getItem().hasContainerItem()) {
                list.set(i, new ItemStack(itemstack.getItem().getContainerItem()));
            } else if (itemstack.isItemStackDamageable()) {
                ItemStack copy = itemstack.copy();
                copy.setCount(1);
                list.set(i, copy);
                break;
            }
        }
        return list;
    }

    @Override
    public boolean canFit(int width, int height) {
        return width >= 3 && height >= 3;
    }

    @Override
    public boolean isDynamic() {
        return true;
    }

    public static class Factory implements IRecipeFactory {
        @Override
        public IRecipe parse(JsonContext context, com.google.gson.JsonObject json) {
            return new RecipeMimicreamRepair();
        }
    }
}
