package com.github.alexthe666.alexsmobs.misc;

import com.github.alexthe666.alexsmobs.AlexsMobs;
import com.github.alexthe666.alexsmobs.effect.AMEffectRegistry;
import com.github.alexthe666.alexsmobs.enchantment.AMEnchantmentRegistry;
import com.github.alexthe666.alexsmobs.item.AMItemRegistry;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.item.ItemEnchantedBook;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionType;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.lang.reflect.Field;

public class AMItemGroup extends CreativeTabs {

    public AMItemGroup() {
        super(AlexsMobs.MODID);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public ItemStack getTabIconItem() {
        return new ItemStack(AMItemRegistry.ANIMAL_DICTIONARY);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void displayAllRelevantItems(NonNullList<ItemStack> items) {
        AMCreativeTabOrder.fillCreativeTab(items);
        try {
            for (Field f : AMEffectRegistry.class.getDeclaredFields()) {
                Object obj = f.get(null);
                if (obj instanceof PotionType) {
                    ItemStack potionStack = AMEffectRegistry.createPotion((PotionType) obj);
                    items.add(potionStack);
                }
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        try {
            for (Field f : AMEnchantmentRegistry.class.getDeclaredFields()) {
                Object obj = f.get(null);
                if (obj instanceof Enchantment) {
                    Enchantment enchant = (Enchantment) obj;
                    if (enchant.isAllowedOnBooks()) {
                        items.add(ItemEnchantedBook.getEnchantedItemStack(new EnchantmentData(enchant, enchant.getMaxLevel())));
                    }
                }
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
