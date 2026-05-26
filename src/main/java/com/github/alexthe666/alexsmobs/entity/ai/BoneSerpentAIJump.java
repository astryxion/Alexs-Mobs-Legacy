package com.github.alexthe666.alexsmobs.entity.ai;

import com.github.alexthe666.alexsmobs.entity.EntityBoneSerpent;
import net.minecraft.block.material.Material;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class BoneSerpentAIJump extends JumpGoal {
    private static final int[] JUMP_DISTANCES = new int[] { 0, 1, 4, 5, 6, 7 };
    private final EntityBoneSerpent dolphin;
    private final int field_220712_c;
    private boolean inWater;

    public BoneSerpentAIJump(EntityBoneSerpent dolphin, int p_i50329_2_) {
        this.dolphin = dolphin;
        this.field_220712_c = p_i50329_2_;
    }

    @Override
    public boolean shouldExecute() {
        if (this.dolphin.getRNG().nextInt(this.field_220712_c) != 0 || this.dolphin.getAttackTarget() != null) {
            return false;
        } else {
            EnumFacing direction = EnumFacing.getHorizontal(MathHelper.floor((double) (this.dolphin.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3);
            int i = direction.getFrontOffsetX();
            int j = direction.getFrontOffsetZ();
            BlockPos blockpos = new BlockPos(this.dolphin.posX, this.dolphin.posY, this.dolphin.posZ);
            for (int k : JUMP_DISTANCES) {
                if (!this.canJumpTo(this.dolphin.world, blockpos, i, j, k) || !this.isAirAbove(this.dolphin.world, blockpos, i, j, k)) {
                    return false;
                }
            }
            return true;
        }
    }

    private boolean canJumpTo(World world, BlockPos pos, int dx, int dz, int scale) {
        BlockPos blockpos = pos.add(dx * scale, 0, dz * scale);
        Material m = world.getBlockState(blockpos).getMaterial();
        return (m == Material.WATER || m == Material.LAVA) && !world.getBlockState(blockpos).getMaterial().blocksMovement();
    }

    private boolean isAirAbove(World world, BlockPos pos, int dx, int dz, int scale) {
        return world.isAirBlock(pos.add(dx * scale, 1, dz * scale)) && world.isAirBlock(pos.add(dx * scale, 2, dz * scale));
    }

    @Override
    public boolean shouldContinueExecuting() {
        double d0 = this.dolphin.motionY;
        return this.dolphin.jumpCooldown > 0
                && (!(d0 * d0 < (double) 0.03F) || this.dolphin.rotationPitch == 0.0F || !(Math.abs(this.dolphin.rotationPitch) < 10.0F) || !this.dolphin.isInWater())
                && !this.dolphin.onGround;
    }

    @Override
    public boolean isPreemptible() {
        return false;
    }

    @Override
    public void startExecuting() {
        EnumFacing direction = EnumFacing.getHorizontal(MathHelper.floor((double) (this.dolphin.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3);
        float up = 0.7F + this.dolphin.getRNG().nextFloat() * 0.8F;
        this.dolphin.motionX += (double) direction.getFrontOffsetX() * 0.6D;
        this.dolphin.motionY += up;
        this.dolphin.motionZ += (double) direction.getFrontOffsetZ() * 0.6D;
        this.dolphin.getNavigator().clearPath();
        this.dolphin.jumpCooldown = this.dolphin.getRNG().nextInt(32) + 32;
    }

    @Override
    public void resetTask() {
        this.dolphin.rotationPitch = 0.0F;
    }

    @Override
    public void updateTask() {
        boolean flag = this.inWater;
        if (!flag) {
            Material mat = this.dolphin.world.getBlockState(this.dolphin.getPosition()).getMaterial();
            this.inWater = mat == Material.LAVA || mat == Material.WATER;
        }

        if (this.inWater && !flag) {
            this.dolphin.playSound(SoundEvents.ENTITY_GENERIC_SPLASH, 1.0F, 1.0F);
        }

        double vHoriz = this.dolphin.motionX * this.dolphin.motionX + this.dolphin.motionZ * this.dolphin.motionZ;
        if (this.dolphin.motionY * this.dolphin.motionY < (double) 0.1F && this.dolphin.rotationPitch != 0.0F) {
            this.dolphin.rotationPitch = this.dolphin.rotationPitch + (0.0F - this.dolphin.rotationPitch) * 0.2F;
        } else {
            Vec3d vel = new Vec3d(this.dolphin.motionX, this.dolphin.motionY, this.dolphin.motionZ);
            double d0 = Math.sqrt(vHoriz);
            double d1 = Math.signum(-this.dolphin.motionY) * Math.acos(d0 / vel.lengthVector()) * (double) (180F / (float) Math.PI);
            this.dolphin.rotationPitch = (float) d1;
        }
    }
}
