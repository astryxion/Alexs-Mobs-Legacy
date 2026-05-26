package com.github.alexthe666.alexsmobs.entity;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;

public interface ITargetsDroppedItems {

    boolean canTargetItem(ItemStack stack);

    void onGetItem(EntityItem e);

    default void onFindTarget(EntityItem e){}

    default double getMaxDistToItem(){return 2.0D; }
}
