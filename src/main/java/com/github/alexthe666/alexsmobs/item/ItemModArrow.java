package com.github.alexthe666.alexsmobs.item;

import com.github.alexthe666.alexsmobs.AlexsMobs;
import com.github.alexthe666.alexsmobs.entity.EntitySharkToothArrow;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.item.ItemArrow;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

/**
 * Forge 1.12.2: extends {@link ItemArrow}; tipped data for shark arrows comes from {@link EntitySharkToothArrow#setPotionEffect}.
 */
public class ItemModArrow extends ItemArrow {

    public ItemModArrow() {
        this.setCreativeTab(AlexsMobs.TAB);
        this.setMaxStackSize(64);
    }

    @Override
    public EntityArrow createArrow(World worldIn, ItemStack stack, EntityLivingBase shooter) {
        if (this == AMItemRegistry.SHARK_TOOTH_ARROW) {
            EntitySharkToothArrow entityarrow = new EntitySharkToothArrow(worldIn, shooter);
            entityarrow.setPotionEffect(stack);
            return entityarrow;
        }
        return super.createArrow(worldIn, stack, shooter);
    }
}
