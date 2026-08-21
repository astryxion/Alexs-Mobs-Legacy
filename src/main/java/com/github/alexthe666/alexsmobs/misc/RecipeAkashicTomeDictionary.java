package com.github.alexthe666.alexsmobs.misc;

import com.github.alexthe666.alexsmobs.item.AMItemRegistry;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistryEntry;

/**
 * Akashic Tome 1.12 only attaches items whose registry path contains book/tome/guide/etc.
 * {@code alexsmobs:animal_dictionary} does not, so AT's own attachment recipe rejects it.
 * This copies Vazkii's {@code AttachementRecipe} NBT so the dictionary can be merged.
 */
public class RecipeAkashicTomeDictionary extends IForgeRegistryEntry.Impl<IRecipe> implements IRecipe {

    private static final ResourceLocation TOME_ID = new ResourceLocation("akashictome", "tome");
    private static final String TAG_TOME_DATA = "akashictome:data";
    private static final String TAG_ITEM_DEFINED_MOD = "akashictome:definedMod";

    @Override
    public boolean matches(InventoryCrafting inv, World worldIn) {
        Item tome = tomeItem();
        if (tome == null) {
            return false;
        }
        boolean foundTome = false;
        boolean foundDictionary = false;
        for (int i = 0; i < inv.getSizeInventory(); i++) {
            ItemStack stack = inv.getStackInSlot(i);
            if (stack.isEmpty()) {
                continue;
            }
            if (stack.getItem() == AMItemRegistry.ANIMAL_DICTIONARY) {
                if (foundDictionary) {
                    return false;
                }
                foundDictionary = true;
            } else if (stack.getItem() == tome) {
                if (foundTome) {
                    return false;
                }
                foundTome = true;
            } else {
                return false;
            }
        }
        return foundTome && foundDictionary;
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inv) {
        Item tome = tomeItem();
        if (tome == null) {
            return ItemStack.EMPTY;
        }
        ItemStack tool = ItemStack.EMPTY;
        ItemStack target = ItemStack.EMPTY;
        for (int i = 0; i < inv.getSizeInventory(); i++) {
            ItemStack stack = inv.getStackInSlot(i);
            if (stack.isEmpty()) {
                continue;
            }
            if (stack.getItem() == tome) {
                tool = stack;
            } else if (stack.getItem() == AMItemRegistry.ANIMAL_DICTIONARY) {
                target = stack.copy();
            }
        }
        if (tool.isEmpty() || target.isEmpty()) {
            return ItemStack.EMPTY;
        }

        ItemStack copy = tool.copy();
        copy.setCount(1);
        if (!copy.hasTagCompound()) {
            copy.setTagCompound(new NBTTagCompound());
        }
        NBTTagCompound cmp = copy.getTagCompound();
        if (!cmp.hasKey(TAG_TOME_DATA)) {
            cmp.setTag(TAG_TOME_DATA, new NBTTagCompound());
        }
        NBTTagCompound morphData = cmp.getCompoundTag(TAG_TOME_DATA);
        String mod = "alexsmobs";
        String modClean = mod;
        int iter = 1;
        while (morphData.hasKey(mod)) {
            mod = modClean + iter;
            iter++;
        }
        if (!target.hasTagCompound()) {
            target.setTagCompound(new NBTTagCompound());
        }
        target.getTagCompound().setString(TAG_ITEM_DEFINED_MOD, mod);
        NBTTagCompound modCmp = new NBTTagCompound();
        target.writeToNBT(modCmp);
        morphData.setTag(mod, modCmp);
        return copy;
    }

    @Override
    public ItemStack getRecipeOutput() {
        return ItemStack.EMPTY;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(InventoryCrafting inv) {
        return NonNullList.withSize(inv.getSizeInventory(), ItemStack.EMPTY);
    }

    @Override
    public boolean canFit(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public boolean isDynamic() {
        return true;
    }

    private static Item tomeItem() {
        return ForgeRegistries.ITEMS.getValue(TOME_ID);
    }
}
