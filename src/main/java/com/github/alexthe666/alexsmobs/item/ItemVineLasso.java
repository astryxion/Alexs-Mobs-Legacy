package com.github.alexthe666.alexsmobs.item;

import com.github.alexthe666.alexsmobs.entity.EntityVineLasso;
import com.github.alexthe666.alexsmobs.misc.AMSoundRegistry;
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
import net.minecraft.world.World;

public class ItemVineLasso extends Item {

    public ItemVineLasso() {
        setCreativeTab(com.github.alexthe666.alexsmobs.AlexsMobs.TAB);
        setMaxStackSize(1);
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 72000;
    }

    public static boolean isItemInUse(ItemStack stack) {
        return stack.getTagCompound() != null && stack.getTagCompound().hasKey("Swinging") && stack.getTagCompound().getBoolean("Swinging");
    }

    @Override
    public void onUpdate(ItemStack stack, World world, net.minecraft.entity.Entity entity, int slot, boolean isSelected) {
        if (entity instanceof EntityLivingBase) {
            EntityLivingBase living = (EntityLivingBase) entity;
            if (!stack.hasTagCompound()) {
                stack.setTagCompound(new net.minecraft.nbt.NBTTagCompound());
            }
            stack.getTagCompound().setBoolean("Swinging", living.getActiveItemStack() == stack && living.isHandActive());
        }
    }

    @Override
    public EnumAction getItemUseAction(ItemStack stack) {
        return EnumAction.BOW;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        ItemStack itemstack = playerIn.getHeldItem(handIn);
        playerIn.setActiveHand(handIn);
        return new ActionResult<>(EnumActionResult.SUCCESS, itemstack);
    }

    @Override
    public void onUsingTick(ItemStack stack, EntityLivingBase livingEntityIn, int count) {
        if (count % 7 == 0) {
            livingEntityIn.world.playSound(null, livingEntityIn.posX, livingEntityIn.posY, livingEntityIn.posZ, AMSoundRegistry.VINE_LASSO, SoundCategory.PLAYERS, 1.0F, 1.0F + (livingEntityIn.getRNG().nextFloat() - livingEntityIn.getRNG().nextFloat()) * 0.2F);
        }
    }

    @Override
    public void onPlayerStoppedUsing(ItemStack stack, World worldIn, EntityLivingBase livingEntityIn, int timeLeft) {
        if (!worldIn.isRemote) {
            int power = this.getMaxItemUseDuration(stack) - timeLeft;
            EntityVineLasso lasso = new EntityVineLasso(worldIn, livingEntityIn);
            float velocity = Math.max(getPowerForTime(power), 0.5F);
            lasso.shootFromRotation(livingEntityIn, livingEntityIn.rotationPitch, livingEntityIn.rotationYaw, 0.0F, velocity, 1.0F);
            worldIn.spawnEntity(lasso);
            stack.shrink(1);
        }
    }

    public static float getPowerForTime(int p) {
        float f = (float) p / 20.0F;
        f = (f * f + f * 2.0F) / 3.0F;
        if (f > 1.0F) {
            f = 1.0F;
        }

        return f;
    }
}
