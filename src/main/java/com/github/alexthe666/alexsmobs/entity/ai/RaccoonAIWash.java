package com.github.alexthe666.alexsmobs.entity.ai;

import com.github.alexthe666.alexsmobs.entity.EntityRaccoon;
import net.minecraft.block.material.Material;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

import java.util.Random;

/**
 * Forge 1.12.2 port of 1.16 {@code RaccoonAIWash}.
 */
public class RaccoonAIWash extends EntityAIBase {

    private final EntityRaccoon raccoon;
    private BlockPos waterPos;
    private BlockPos targetPos;
    private int washTime = 0;
    private final EnumFacing[] HORIZONTALS = new EnumFacing[]{EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.WEST, EnumFacing.EAST};

    public RaccoonAIWash(EntityRaccoon creature) {
        this.setMutexBits(3);
        this.raccoon = creature;
    }

    @Override
    public boolean shouldExecute() {
        if (raccoon.getHeldItemMainhand().isEmpty()) {
            return false;
        }
        if (raccoon.lookForWaterBeforeEatingTimer > 0) {
            waterPos = generateTarget();
            if (waterPos != null) {
                targetPos = getLandPos(waterPos);
                return targetPos != null;
            }
        }
        return false;
    }

    @Override
    public void startExecuting() {
        this.raccoon.lookForWaterBeforeEatingTimer = 1800;
    }

    @Override
    public void resetTask() {
        targetPos = null;
        waterPos = null;
        washTime = 0;
        this.raccoon.setWashPos(null);
        this.raccoon.setWashing(false);
        this.raccoon.lookForWaterBeforeEatingTimer = 100;
        this.raccoon.getNavigator().clearPath();
    }

    @Override
    public void updateTask() {
        if (targetPos != null && waterPos != null) {
            double dist = this.raccoon.getDistanceSq(waterPos.getX() + 0.5D, waterPos.getY(), waterPos.getZ() + 0.5D);
            if (dist > 2 && this.raccoon.isWashing()) {
                this.raccoon.setWashing(false);
            }
            if (dist <= 1F) {
                double d0 = waterPos.getX() + 0.5D - this.raccoon.posX;
                double d2 = waterPos.getZ() + 0.5D - this.raccoon.posZ;
                float yaw = (float) (MathHelper.atan2(d2, d0) * (double) (180F / (float) Math.PI)) - 90.0F;
                this.raccoon.rotationYaw = yaw;
                this.raccoon.rotationYawHead = yaw;
                this.raccoon.renderYawOffset = yaw;
                this.raccoon.getNavigator().clearPath();
                this.raccoon.setWashing(true);
                this.raccoon.setWashPos(waterPos);
                this.raccoon.lookForWaterBeforeEatingTimer = 0;
                if (washTime % 10 == 0) {
                    this.raccoon.playSound(SoundEvents.ENTITY_GENERIC_SWIM, 0.7F, 0.5F + raccoon.getRNG().nextFloat());
                }
                washTime++;
                if (washTime > 100 || raccoon.getHeldItemMainhand().getItem() == Items.SUGAR && washTime > 20) {
                    ItemStack washedStack = raccoon.getHeldItemMainhand().copy();
                    this.resetTask();
                    if (washedStack.getItem() != Items.SUGAR) {
                        if (washedStack.getItem().hasContainerItem()) {
                            this.raccoon.entityDropItem(new ItemStack(washedStack.getItem().getContainerItem()), 0.0F);
                        }
                        raccoon.onEatItem(washedStack);
                        raccoon.getHeldItemMainhand().shrink(1);
                    } else {
                        raccoon.getHeldItemMainhand().shrink(1);
                    }
                    this.raccoon.postWashItem(washedStack);
                }
            } else {
                this.raccoon.getNavigator().tryMoveToXYZ(waterPos.getX(), waterPos.getY(), waterPos.getZ(), 1.2D);
            }
        }
    }

    @Override
    public boolean shouldContinueExecuting() {
        if (raccoon.getHeldItemMainhand().isEmpty()) {
            return false;
        }
        return targetPos != null && !this.raccoon.isInWater() && EntityRaccoon.isRaccoonFood(this.raccoon.getHeldItemMainhand());
    }

    public BlockPos generateTarget() {
        BlockPos blockpos = null;
        Random random = new Random();
        int range = 32;
        for (int i = 0; i < 15; i++) {
            BlockPos blockpos1 = this.raccoon.getPosition().add(random.nextInt(range) - range / 2, 3, random.nextInt(range) - range / 2);
            while (this.raccoon.world.isAirBlock(blockpos1) && blockpos1.getY() > 1) {
                blockpos1 = blockpos1.down();
            }
            if (isConnectedToLand(blockpos1)) {
                blockpos = blockpos1;
            }
        }
        return blockpos;
    }

    public boolean isConnectedToLand(BlockPos pos) {
        if (this.raccoon.world.getBlockState(pos).getMaterial() == Material.WATER) {
            for (EnumFacing dir : HORIZONTALS) {
                BlockPos offsetPos = pos.offset(dir);
                if (this.raccoon.world.getBlockState(offsetPos).getMaterial() != Material.WATER
                        && this.raccoon.world.getBlockState(offsetPos.up()).getMaterial() != Material.WATER) {
                    return true;
                }
            }
        }
        return false;
    }

    public BlockPos getLandPos(BlockPos pos) {
        if (this.raccoon.world.getBlockState(pos).getMaterial() == Material.WATER) {
            for (EnumFacing dir : HORIZONTALS) {
                BlockPos offsetPos = pos.offset(dir);
                if (this.raccoon.world.getBlockState(offsetPos).getMaterial() != Material.WATER
                        && this.raccoon.world.getBlockState(offsetPos.up()).getMaterial() != Material.WATER) {
                    return offsetPos;
                }
            }
        }
        return null;
    }
}
