package com.github.alexthe666.alexsmobs.item;

import com.github.alexthe666.alexsmobs.AlexsMobs;
import com.github.alexthe666.alexsmobs.block.AMBlockRegistry;
import com.github.alexthe666.alexsmobs.block.BlockSkunkSpray;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * 1.12 port of 1.20 {@link com.github.alexthe666.alexsmobs.item.ItemStinkBottle}.
 */
public class ItemStinkBottle extends ItemBlock {

    public ItemStinkBottle(Block block) {
        super(block);
        this.setCreativeTab(AlexsMobs.TAB);
        this.setMaxStackSize(16);
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        IBlockState here = world.getBlockState(pos);
        BlockPos placePos = here.getBlock().isReplaceable(world, pos) ? pos : pos.offset(facing);
        IBlockState existing = world.getBlockState(placePos);
        IBlockState sprayState = BlockSkunkSpray.getStateForPlacement(existing, world, placePos, facing.getOpposite());
        if (sprayState == null) {
            return EnumActionResult.FAIL;
        }
        if (!player.canPlayerEdit(placePos, facing, player.getHeldItem(hand))) {
            return EnumActionResult.FAIL;
        }
        if (!world.isRemote) {
            BlockSkunkSpray.applyPlacementState(world, placePos, sprayState);
            if (!player.capabilities.isCreativeMode) {
                player.getHeldItem(hand).shrink(1);
            }
            ItemStack bottle = new ItemStack(Items.GLASS_BOTTLE);
            if (!player.addItemStackToInventory(bottle)) {
                player.dropItem(bottle, false);
            }
        }
        return EnumActionResult.SUCCESS;
    }
}
