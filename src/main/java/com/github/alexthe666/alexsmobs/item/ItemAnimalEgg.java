package com.github.alexthe666.alexsmobs.item;

import com.github.alexthe666.alexsmobs.AlexsMobs;
import com.github.alexthe666.alexsmobs.entity.EntityCockroachEgg;
import com.github.alexthe666.alexsmobs.entity.EntityEmuEgg;
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
            worldIn.spawnEntity(eggentity);
        }

        playerIn.addStat(StatList.getObjectUseStats(this));
        if (!playerIn.capabilities.isCreativeMode) {
            itemstack.shrink(1);
        }

        return new ActionResult<>(worldIn.isRemote ? EnumActionResult.PASS : EnumActionResult.SUCCESS, itemstack);
    }
}
