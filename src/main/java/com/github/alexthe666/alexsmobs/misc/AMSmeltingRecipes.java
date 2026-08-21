package com.github.alexthe666.alexsmobs.misc;

import com.github.alexthe666.alexsmobs.item.AMItemRegistry;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;

/**
 * 1.12 has no {@code minecraft:smelting} JSON recipe type. Furnace recipes must be
 * registered in code so Forge's crafting JSON loader does not try to parse them.
 */
public final class AMSmeltingRecipes {

    private AMSmeltingRecipes() {
    }

    public static void register() {
        FurnaceRecipes furnace = FurnaceRecipes.instance();
        furnace.addSmeltingRecipe(new ItemStack(AMItemRegistry.MOOSE_RIBS), new ItemStack(AMItemRegistry.COOKED_MOOSE_RIBS), 0.15F);
        furnace.addSmeltingRecipe(new ItemStack(AMItemRegistry.LOBSTER_TAIL), new ItemStack(AMItemRegistry.COOKED_LOBSTER_TAIL), 0.15F);
        furnace.addSmeltingRecipe(new ItemStack(AMItemRegistry.KANGAROO_MEAT), new ItemStack(AMItemRegistry.COOKED_KANGAROO_MEAT), 0.15F);
        furnace.addSmeltingRecipe(new ItemStack(AMItemRegistry.RAW_CATFISH), new ItemStack(AMItemRegistry.COOKED_CATFISH), 0.15F);
        furnace.addSmeltingRecipe(new ItemStack(AMItemRegistry.EMU_EGG), new ItemStack(AMItemRegistry.BOILED_EMU_EGG), 0.15F);
    }
}
