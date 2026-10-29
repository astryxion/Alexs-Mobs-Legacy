package com.github.alexthe668.alexsmobs.item;

import com.github.alexthe668.alexsmobs.AlexsMobs;
import com.github.alexthe668.alexsmobs.entity.EntityCockroachEgg;
import com.github.alexthe668.alexsmobs.entity.EntityEmuEgg;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.init.SoundEvents;
import net.minecraft.world.World;

public class ItemAnimalEgg extends Item {

    public ItemAnimalEgg() {
        setCreativeTab(AlexsMobs.TAB);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        ItemStack itemstack = playerIn.getHeldItem(handIn);
        worldIn.playSound(null, playerIn.posX, playerIn.posY, playerIn.posZ, SoundEvents.ENTITY_EGG_THROW, SoundCategory.PLAYERS, 0.5F, 0.4F / (itemRand.nextFloat() * 0.4F + 0.8F));
        if (!worldIn.isRemote) {
            Entity eggentity;
            if (this == AMItemRegistry.EMU_EGG) {
                eggentity = new EntityEmuEgg(worldIn, playerIn);
            } else {
                eggentity = new EntityCockroachEgg(worldIn, playerIn);
            }
            net.minecraft.entity.projectile.EntityThrowable throwable = (net.minecraft.entity.projectile.EntityThrowable) eggentity;
            net.minecraft.util.math.Vec3d look = playerIn.getLook(1.0F);
            throwable.setPosition(playerIn.posX + look.x * 0.5D, playerIn.posY + playerIn.getEyeHeight() - 0.1D, playerIn.posZ + look.z * 0.5D);
            throwable.shoot(look.x, look.y, look.z, 1.5F, 1.0F);
            worldIn.spawnEntity(eggentity);
        }

        playerIn.addStat(StatList.getObjectUseStats(this));
        if (!playerIn.capabilities.isCreativeMode) {
            itemstack.shrink(1);
        }

        return new ActionResult<>(worldIn.isRemote ? EnumActionResult.PASS : EnumActionResult.SUCCESS, itemstack);
    }
}
