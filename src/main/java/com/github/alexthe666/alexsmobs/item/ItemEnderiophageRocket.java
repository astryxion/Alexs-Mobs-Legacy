package com.github.alexthe666.alexsmobs.item;

import com.github.alexthe666.alexsmobs.AlexsMobs;
import com.github.alexthe666.alexsmobs.entity.EntityEnderiophageRocket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.item.EntityFireworkRocket;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class ItemEnderiophageRocket extends Item {

    public ItemEnderiophageRocket() {
        setCreativeTab(AlexsMobs.TAB);
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack held = player.getHeldItem(hand);
        if (!world.isRemote) {
            Vec3d hitVec = new Vec3d(pos.getX() + hitX, pos.getY() + hitY, pos.getZ() + hitZ);
            EntityFireworkRocket fireworkrocketentity = new EntityEnderiophageRocket(world, hitVec.x + (double) facing.getFrontOffsetX() * 0.15D, hitVec.y + (double) facing.getFrontOffsetY() * 0.15D, hitVec.z + (double) facing.getFrontOffsetZ() * 0.15D, held.copy());
            world.spawnEntity(fireworkrocketentity);
            if (!player.capabilities.isCreativeMode) {
                held.shrink(1);
            }
        }
        return EnumActionResult.SUCCESS;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        if (playerIn.isElytraFlying()) {
            ItemStack itemstack = playerIn.getHeldItem(handIn);
            if (!worldIn.isRemote) {
                worldIn.spawnEntity(new EntityEnderiophageRocket(worldIn, itemstack.copy(), playerIn));
                if (!playerIn.capabilities.isCreativeMode) {
                    itemstack.shrink(1);
                }
            }
            return new ActionResult<>(EnumActionResult.SUCCESS, playerIn.getHeldItem(handIn));
        }
        return new ActionResult<>(EnumActionResult.PASS, playerIn.getHeldItem(handIn));
    }
}
