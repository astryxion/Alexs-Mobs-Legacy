package com.github.alexthe666.alexsmobs.entity.ai;

import com.github.alexthe666.alexsmobs.entity.EntityOrca;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

public class OrcaAIJump extends JumpGoal {
    private static final int[] JUMP_DISTANCES = new int[]{0, 1, 4, 5, 6, 7, 10};
    private final EntityOrca dolphin;
    private final int field_220712_c;
    private boolean inWater;

    public OrcaAIJump(EntityOrca dolphin, int p_i50329_2_) {
        this.dolphin = dolphin;
        this.field_220712_c = p_i50329_2_;
    }

    @Override
    public boolean shouldExecute() {
        if (this.dolphin.getRNG().nextInt(this.field_220712_c) != 0 || dolphin.getAttackTarget() != null || dolphin.jumpCooldown != 0) {
            return false;
        } else {
            EnumFacing direction = this.dolphin.getAdjustedHorizontalFacing();
            int i = direction.getFrontOffsetX();
            int j = direction.getFrontOffsetZ();
            BlockPos blockpos = this.dolphin.getPosition();
            for (int k : JUMP_DISTANCES) {
                if (!this.canJumpTo(blockpos, i, j, k) || !this.isAirAbove(blockpos, i, j, k)) {
                    return false;
                }
            }
            return true;
        }
    }

    private boolean canJumpTo(BlockPos pos, int dx, int dz, int scale) {
        BlockPos blockpos = pos.add(dx * scale, 0, dz * scale);
        return this.dolphin.world.getBlockState(blockpos).getMaterial() == Material.WATER && !this.dolphin.world.getBlockState(blockpos).getMaterial().blocksMovement();
    }

    private boolean isAirAbove(BlockPos pos, int dx, int dz, int scale) {
        return this.dolphin.world.isAirBlock(pos.add(dx * scale, 1, dz * scale)) && this.dolphin.world.isAirBlock(pos.add(dx * scale, 2, dz * scale));
    }

    @Override
    public boolean shouldContinueExecuting() {
        double d0 = this.dolphin.motionY;
        return dolphin.jumpCooldown > 0 && (!(d0 * d0 < (double) 0.03F) || this.dolphin.rotationPitch == 0.0F || !(Math.abs(this.dolphin.rotationPitch) < 10.0F) || !this.dolphin.isInWater()) && !this.dolphin.onGround;
    }

    @Override
    public boolean isPreemptible() {
        return false;
    }

    @Override
    public void startExecuting() {
        EnumFacing direction = this.dolphin.getAdjustedHorizontalFacing();
        float up = 0.7F + dolphin.getRNG().nextFloat() * 0.8F;
        this.dolphin.motionX += (double) direction.getFrontOffsetX() * 0.6D;
        this.dolphin.motionY += up;
        this.dolphin.motionZ += (double) direction.getFrontOffsetZ() * 0.6D;
        this.dolphin.getNavigator().clearPath();
        this.dolphin.jumpCooldown = dolphin.getRNG().nextInt(256) + 256;
    }

    @Override
    public void resetTask() {
        this.dolphin.rotationPitch = 0.0F;
    }

    @Override
    public void updateTask() {
        boolean flag = this.inWater;
        if (!flag) {
            IBlockState state = this.dolphin.world.getBlockState(this.dolphin.getPosition());
            this.inWater = state.getMaterial() == Material.WATER;
        }

        if (this.inWater && !flag) {
            this.dolphin.playSound(SoundEvents.ENTITY_GENERIC_SPLASH, 1.0F, 1.0F);
        }

        double motY = this.dolphin.motionY;
        if (motY * motY < (double) 0.1F && this.dolphin.rotationPitch != 0.0F) {
            this.dolphin.rotationPitch = this.dolphin.rotationPitch + MathHelper.wrapDegrees(0.0F - this.dolphin.rotationPitch) * 0.2F;
        } else {
            double horiz = Math.sqrt(this.dolphin.motionX * this.dolphin.motionX + this.dolphin.motionZ * this.dolphin.motionZ);
            double len = Math.sqrt(this.dolphin.motionX * this.dolphin.motionX + motY * motY + this.dolphin.motionZ * this.dolphin.motionZ);
            if (len > 1.0E-4D) {
                double d1 = Math.signum(-motY) * Math.acos(horiz / len) * (double) (180F / (float) Math.PI);
                this.dolphin.rotationPitch = (float) d1;
            }
        }
    }
}
