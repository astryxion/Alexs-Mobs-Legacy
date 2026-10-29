package com.github.alexthe668.alexsmobs.item;

import com.github.alexthe668.alexsmobs.block.BlockTriopsEggs;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

/**
 * Places triops eggs on the water surface. A normal item-block use would replace the water block.
 */
public class ItemTriopsEggs extends ItemBlock {

    public ItemTriopsEggs(net.minecraft.block.Block block) {
        super(block);
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        return EnumActionResult.PASS;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        ItemStack stack = playerIn.getHeldItem(handIn);
        RayTraceResult ray = this.rayTrace(worldIn, playerIn, true);
        if (ray == null || ray.typeOfHit != RayTraceResult.Type.BLOCK) {
            return new ActionResult<ItemStack>(EnumActionResult.PASS, stack);
        }
        BlockPos waterPos = ray.getBlockPos();
        if (worldIn.getBlockState(waterPos).getMaterial() != Material.WATER) {
            return new ActionResult<ItemStack>(EnumActionResult.PASS, stack);
        }
        BlockPos placePos = waterPos.up();
        if (!playerIn.canPlayerEdit(placePos, EnumFacing.UP, stack)) {
            return new ActionResult<ItemStack>(EnumActionResult.FAIL, stack);
        }
        IBlockState occupied = worldIn.getBlockState(placePos);
        if (occupied.getMaterial() == Material.WATER || !occupied.getBlock().isReplaceable(worldIn, placePos)) {
            return new ActionResult<ItemStack>(EnumActionResult.FAIL, stack);
        }
        if (!BlockTriopsEggs.canPlaceOn(worldIn, waterPos)) {
            return new ActionResult<ItemStack>(EnumActionResult.FAIL, stack);
        }
        if (!worldIn.isRemote) {
            worldIn.setBlockState(placePos, this.block.getDefaultState(), 3);
        }
        if (!playerIn.capabilities.isCreativeMode) {
            stack.shrink(1);
        }
        return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
    }
}
