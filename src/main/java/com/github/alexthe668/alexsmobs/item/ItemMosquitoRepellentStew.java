package com.github.alexthe668.alexsmobs.item;

import com.github.alexthe668.alexsmobs.AlexsMobs;
import com.github.alexthe668.alexsmobs.effect.AMEffectRegistry;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;

/**
 * Bowl food: 4 hunger, always edible, returns the bowl, and applies mosquito repellent for 20 minutes.
 */
public class ItemMosquitoRepellentStew extends ItemFood {

    public ItemMosquitoRepellentStew() {
        super(4, 0.3F, false);
        this.setAlwaysEdible();
        this.setPotionEffect(new PotionEffect(AMEffectRegistry.MOSQUITO_REPELLENT, 24000), 1.0F);
        this.setMaxStackSize(1);
        this.setCreativeTab(AlexsMobs.TAB);
        this.setRegistryName("alexsmobs:mosquito_repellent_stew");
    }

    @Override
    public ItemStack onItemUseFinish(ItemStack stack, World worldIn, EntityLivingBase entityLiving) {
        super.onItemUseFinish(stack, worldIn, entityLiving);
        return new ItemStack(Items.BOWL);
    }
}
