package com.github.alexthe666.alexsmobs.item;

import com.github.alexthe666.alexsmobs.AlexsMobs;
import com.github.alexthe666.alexsmobs.entity.EntitySandShot;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSand;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.SoundCategory;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.function.Predicate;

public class ItemPocketSand extends Item {

    public static final Predicate<ItemStack> IS_SAND = (stack) -> {
        Block block = Block.getBlockFromItem(stack.getItem());
        return block == Blocks.SAND || block instanceof BlockSand;
    };

    public ItemPocketSand() {
        setCreativeTab(AlexsMobs.TAB);
        setMaxDamage(200);
    }

    public ItemStack findAmmo(EntityPlayer entity) {
        if (entity.capabilities.isCreativeMode) {
            return ItemStack.EMPTY;
        }
        for (int i = 0; i < entity.inventory.getSizeInventory(); ++i) {
            ItemStack itemstack1 = entity.inventory.getStackInSlot(i);
            if (IS_SAND.test(itemstack1)) {
                return itemstack1;
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer livingEntityIn, EnumHand handIn) {
        ItemStack itemstack = livingEntityIn.getHeldItem(handIn);
        ItemStack ammo = findAmmo(livingEntityIn);
        if (livingEntityIn.capabilities.isCreativeMode) {
            ammo = new ItemStack(Blocks.SAND);
        }
        if (!worldIn.isRemote && !ammo.isEmpty()) {
            worldIn.playSound(null, livingEntityIn.posX, livingEntityIn.posY, livingEntityIn.posZ, SoundEvents.BLOCK_SAND_BREAK, SoundCategory.PLAYERS, 0.5F, 0.4F + (itemRand.nextFloat() * 0.4F + 0.8F));
            boolean left = livingEntityIn.getActiveHand() == EnumHand.OFF_HAND && livingEntityIn.getPrimaryHand() == EnumHandSide.RIGHT
                    || livingEntityIn.getActiveHand() == EnumHand.MAIN_HAND && livingEntityIn.getPrimaryHand() == EnumHandSide.LEFT;
            EntitySandShot blood = new EntitySandShot(worldIn, livingEntityIn, !left);
            Vec3d look = livingEntityIn.getLook(1.0F);
            blood.shoot(look.x, look.y, look.z, 1.2F, 11);
            if (!worldIn.isRemote) {
                worldIn.spawnEntity(blood);
            }
            livingEntityIn.getCooldownTracker().setCooldown(this, 2);
            ammo.shrink(1);
            itemstack.damageItem(1, livingEntityIn);
        }
        livingEntityIn.addStat(StatList.getObjectUseStats(this));
        return new ActionResult<>(worldIn.isRemote ? EnumActionResult.PASS : EnumActionResult.SUCCESS, itemstack);
    }
}
