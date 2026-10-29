package com.github.alexthe668.alexsmobs.item;

import com.github.alexthe668.alexsmobs.AlexsMobs;
import net.minecraft.item.ItemShield;
import net.minecraft.item.ItemStack;

public class ItemShieldOfTheDeep extends ItemShield {
    public ItemShieldOfTheDeep() {
        setMaxDamage(400);
        setCreativeTab(AlexsMobs.TAB);
    }

    public boolean getIsRepairable(ItemStack p_82789_1_, ItemStack p_82789_2_) {
        return AMItemRegistry.SERRATED_SHARK_TOOTH == p_82789_2_.getItem() || super.getIsRepairable(p_82789_1_, p_82789_2_);
    }

}
