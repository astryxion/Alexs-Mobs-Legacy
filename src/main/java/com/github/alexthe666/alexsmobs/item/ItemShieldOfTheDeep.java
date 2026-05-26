package com.github.alexthe666.alexsmobs.item;

import com.github.alexthe666.alexsmobs.AlexsMobs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class ItemShieldOfTheDeep extends Item {
    public ItemShieldOfTheDeep() {
        setMaxDamage(400);
        setCreativeTab(AlexsMobs.TAB);
    }

    public boolean isShield(ItemStack stack, @Nullable EntityLivingBase entity)
    {
        return true;
    }

    public EnumAction getItemUseAction(ItemStack stack) {
        return EnumAction.BLOCK;
    }

    public int getMaxItemUseDuration(ItemStack stack) {
        return 72000;
    }

    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        ItemStack held = playerIn.getHeldItem(handIn);
        playerIn.setActiveHand(handIn);
        return new ActionResult<>(EnumActionResult.SUCCESS, held);
    }

    public boolean getIsRepairable(ItemStack p_82789_1_, ItemStack p_82789_2_) {
        return AMItemRegistry.SERRATED_SHARK_TOOTH == p_82789_2_.getItem() || super.getIsRepairable(p_82789_1_, p_82789_2_);
    }

}
