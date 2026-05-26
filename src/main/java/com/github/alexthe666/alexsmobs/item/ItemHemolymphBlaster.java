package com.github.alexthe666.alexsmobs.item;

import com.github.alexthe666.alexsmobs.AlexsMobs;
import com.github.alexthe666.alexsmobs.entity.EntityHemolymph;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.SoundCategory;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Random;
import java.util.function.Predicate;

public class ItemHemolymphBlaster extends Item {

    public static final Predicate<ItemStack> HEMOLYMPH = (stack) -> stack.getItem() == AMItemRegistry.HEMOLYMPH_SAC;

    public ItemHemolymphBlaster() {
        setCreativeTab(AlexsMobs.TAB);
        setMaxDamage(150);
    }

    public static boolean isUsable(ItemStack stack) {
        return stack.getItemDamage() < stack.getMaxDamage() - 1;
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return isUsable(stack) ? Integer.MAX_VALUE : 0;
    }

    @Override
    public EnumAction getItemUseAction(ItemStack stack) {
        return EnumAction.BOW;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        ItemStack itemstack = playerIn.getHeldItem(handIn);
        playerIn.setActiveHand(handIn);
        if (!isUsable(itemstack)) {
            ItemStack ammo = findAmmo(playerIn);
            boolean flag = playerIn.capabilities.isCreativeMode;
            if (!ammo.isEmpty()) {
                ammo.shrink(1);
                flag = true;
            }
            if (flag) {
                itemstack.setItemDamage(0);
            }
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, itemstack);
    }

    public ItemStack findAmmo(EntityPlayer entity) {
        if (entity.capabilities.isCreativeMode) {
            return ItemStack.EMPTY;
        }
        for (int i = 0; i < entity.inventory.getSizeInventory(); ++i) {
            ItemStack itemstack1 = entity.inventory.getStackInSlot(i);
            if (HEMOLYMPH.test(itemstack1)) {
                return itemstack1;
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return !ItemStack.areItemStacksEqual(oldStack, newStack);
    }

    @Override
    public void onUsingTick(ItemStack stack, EntityLivingBase livingEntityIn, int count) {
        if (isUsable(stack)) {
            if (count % 2 == 0) {
                boolean left = livingEntityIn.getActiveHand() == EnumHand.OFF_HAND && livingEntityIn.getPrimaryHand() == EnumHandSide.RIGHT
                        || livingEntityIn.getActiveHand() == EnumHand.MAIN_HAND && livingEntityIn.getPrimaryHand() == EnumHandSide.LEFT;
                EntityHemolymph blood = new EntityHemolymph(livingEntityIn.world, livingEntityIn, !left);
                Vec3d look = livingEntityIn.getLook(1.0F);
                Random rand = livingEntityIn.world.rand;
                livingEntityIn.world.playSound(null, livingEntityIn.posX, livingEntityIn.posY, livingEntityIn.posZ, SoundEvents.BLOCK_LAVA_POP, SoundCategory.NEUTRAL, 1.0F, 0.5F + (rand.nextFloat() - rand.nextFloat()) * 0.2F);
                blood.shoot(look.x, look.y, look.z, 1F, 3);
                if (!livingEntityIn.world.isRemote) {
                    livingEntityIn.world.spawnEntity(blood);
                }
                stack.damageItem(1, livingEntityIn);
            }
        } else if (livingEntityIn instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) livingEntityIn;
            ItemStack ammo = findAmmo(player);
            boolean flag = player.capabilities.isCreativeMode;
            if (!ammo.isEmpty()) {
                ammo.shrink(1);
                flag = true;
            }
            if (flag) {
                player.getCooldownTracker().setCooldown(this, 20);
                stack.setItemDamage(0);
            }
            livingEntityIn.resetActiveHand();
        }
    }
}
