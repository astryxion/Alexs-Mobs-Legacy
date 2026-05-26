package com.github.alexthe666.alexsmobs.item;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Items used only for rendering, NBT, or mechanics — not shown in creative or recipe viewers.
 */
public class ItemAMInternal extends Item {

    public ItemAMInternal() {
        setCreativeTab(null);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
    }
}
