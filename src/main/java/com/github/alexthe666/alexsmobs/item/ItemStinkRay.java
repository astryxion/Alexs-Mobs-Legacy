package com.github.alexthe666.alexsmobs.item;

import com.github.alexthe666.alexsmobs.AlexsMobs;
import com.github.alexthe666.alexsmobs.entity.EntityFart;
import com.github.alexthe666.alexsmobs.misc.AMSoundRegistry;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.function.Predicate;

public class ItemStinkRay extends Item {

    public static final Predicate<ItemStack> IS_FART_BOTTLE = (stack) -> stack.getItem() == AMItemRegistry.STINK_BOTTLE;

    public ItemStinkRay() {
        setCreativeTab(AlexsMobs.TAB);
        setMaxDamage(5);
    }

    public static boolean isUsable(ItemStack stack) {
        return stack.getItemDamage() < stack.getMaxDamage() - 1;
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return isUsable(stack) ? 72000 : 0;
    }

    @Override
    public EnumAction getItemUseAction(ItemStack stack) {
        return EnumAction.BOW;
    }

    public static float getPowerForTime(int i) {
        float f = (float) i / 20.0F;
        f = (f * f + f * 2.0F) / 3.0F;
        if (f > 1.0F) {
            f = 1.0F;
        }
        return f;
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
                ItemStack bottle = new ItemStack(Items.GLASS_BOTTLE);
                if (!playerIn.inventory.addItemStackToInventory(bottle)) {
                    playerIn.dropItem(bottle, false);
                }
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
            return new ItemStack(AMItemRegistry.STINK_BOTTLE);
        }
        for (int i = 0; i < entity.inventory.getSizeInventory(); ++i) {
            ItemStack stack = entity.inventory.getStackInSlot(i);
            if (IS_FART_BOTTLE.test(stack)) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void onPlayerStoppedUsing(ItemStack stack, World worldIn, EntityLivingBase livingEntityIn, int timeLeft) {
        if (!(livingEntityIn instanceof EntityPlayer) || !isUsable(stack)) {
            return;
        }
        int charge = this.getMaxItemUseDuration(stack) - timeLeft;
        if (charge < 10) {
            return;
        }
        boolean left = livingEntityIn.getActiveHand() == EnumHand.OFF_HAND && livingEntityIn.getPrimaryHand() == EnumHandSide.RIGHT
                || livingEntityIn.getActiveHand() == EnumHand.MAIN_HAND && livingEntityIn.getPrimaryHand() == EnumHandSide.LEFT;
        EntityFart fart = new EntityFart(worldIn, livingEntityIn, !left);
        Vec3d look = livingEntityIn.getLook(1.0F);
        worldIn.playSound(null, livingEntityIn.posX, livingEntityIn.posY, livingEntityIn.posZ, AMSoundRegistry.STINK_RAY, SoundCategory.PLAYERS, 1.0F, 0.9F + (livingEntityIn.getRNG().nextFloat() - livingEntityIn.getRNG().nextFloat()) * 0.2F);
        fart.shoot(look.x, look.y, look.z, 0.2F + getPowerForTime(charge) * 0.4F, 10.0F);
        if (!worldIn.isRemote) {
            worldIn.spawnEntity(fart);
        }
        stack.damageItem(1, livingEntityIn);
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return !ItemStack.areItemStacksEqual(oldStack, newStack);
    }
}
