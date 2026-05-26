package com.github.alexthe666.alexsmobs.effect;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraftforge.common.brewing.BrewingRecipe;

import javax.annotation.Nonnull;

public class ProperBrewingRecipe extends BrewingRecipe {

    private final Ingredient input;
    private final Ingredient ingredient;

    public ProperBrewingRecipe(Ingredient input, Ingredient ingredient, ItemStack output) {
        super(stackFromIngredient(input), stackFromIngredient(ingredient), output);
        this.input = input;
        this.ingredient = ingredient;
    }

    private static ItemStack stackFromIngredient(Ingredient ingredient) {
        ItemStack[] stacks = ingredient.getMatchingStacks();
        return stacks.length > 0 ? stacks[0].copy() : ItemStack.EMPTY;
    }

    @Override
    public boolean isInput(@Nonnull ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        ItemStack[] matchingStacks = this.input.getMatchingStacks();
        if (matchingStacks.length == 0) {
            return stack.isEmpty();
        }
        for (ItemStack itemstack : matchingStacks) {
            if (itemstack.isItemEqual(stack) && ItemStack.areItemStackTagsEqual(itemstack, stack)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isIngredient(@Nonnull ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        ItemStack[] matchingStacks = this.ingredient.getMatchingStacks();
        for (ItemStack itemstack : matchingStacks) {
            if (itemstack.isItemEqual(stack)) {
                return true;
            }
        }
        return false;
    }
}
