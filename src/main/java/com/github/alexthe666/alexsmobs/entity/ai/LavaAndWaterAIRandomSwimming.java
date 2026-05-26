package com.github.alexthe666.alexsmobs.entity.ai;

import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class LavaAndWaterAIRandomSwimming extends EntityAIBase {
    protected final EntityCreature mob;
    protected final double speed;
    protected final int interval;
    protected double wantedX;
    protected double wantedY;
    protected double wantedZ;
    protected boolean forceTrigger;

    public LavaAndWaterAIRandomSwimming(EntityCreature creature, double speed, int chance) {
        this.mob = creature;
        this.speed = speed;
        this.interval = chance;
        this.setMutexBits(1);
    }

    @Override
    public boolean shouldExecute() {
        if (this.mob.isBeingRidden() || this.mob.getAttackTarget() != null) {
            return false;
        } else {
            if (!this.forceTrigger) {
                int i = this.mob.isInLava() || this.mob.isInWater() ? this.interval : this.interval * 2;
                if (this.mob.getRNG().nextInt(i) != 0) {
                    return false;
                }
            }
            Vec3d vector3d = this.getPosition();
            if (vector3d == null) {
                return false;
            } else {
                this.wantedX = vector3d.x;
                this.wantedY = vector3d.y;
                this.wantedZ = vector3d.z;
                this.forceTrigger = false;
                return true;
            }
        }
    }

    @Override
    public boolean shouldContinueExecuting() {
        return !this.mob.getNavigator().noPath();
    }

    @Override
    public void startExecuting() {
        this.mob.getNavigator().tryMoveToXYZ(this.wantedX, this.wantedY, this.wantedZ, this.speed);
    }

    @Nullable
    protected Vec3d getPosition() {
        if (this.mob.getRNG().nextFloat() < (this.mob.isInLava() ? 0.7F : 0.3F)) {
            Vec3d vector3d = findSurfaceTarget(this.mob, 32, 16);
            if (vector3d != null) {
                return vector3d;
            }
        }
        Vec3d vector3d = RandomPositionGenerator.findRandomTarget(this.mob, 32, 16);

        for (int i = 0; vector3d != null && !canStandInFluid(new BlockPos(vector3d)) && i++ < 10; vector3d = RandomPositionGenerator.findRandomTarget(this.mob, 10, 7)) {
        }

        return vector3d;
    }

    private boolean canJumpTo(BlockPos pos, int dx, int dz, int scale) {
        BlockPos blockpos = pos.add(dx * scale, 0, dz * scale);
        Material mat = this.mob.world.getBlockState(blockpos).getMaterial();
        return (mat == Material.WATER || mat == Material.LAVA) && !this.mob.world.getBlockState(blockpos).getMaterial().blocksMovement();
    }

    private boolean isAirAbove(BlockPos pos, int dx, int dz, int scale) {
        return this.mob.world.isAirBlock(pos.add(dx * scale, 1, dz * scale)) && this.mob.world.isAirBlock(pos.add(dx * scale, 2, dz * scale));
    }

    protected Vec3d findSurfaceTarget(EntityCreature creature, int i, int i1) {
        BlockPos upPos = creature.getPosition();
        while (creature.world.getBlockState(upPos).getMaterial() == Material.LAVA || creature.world.getBlockState(upPos).getMaterial() == Material.WATER) {
            upPos = upPos.up();
        }
        if (isAirAbove(upPos.down(), 0, 0, 0) && canJumpTo(upPos.down(), 0, 0, 0)) {
            return new Vec3d(upPos.getX() + 0.5F, upPos.getY() - 0.5F, upPos.getZ() + 0.5F);
        }
        return null;
    }

    private boolean canStandInFluid(BlockPos pos) {
        World w = this.mob.world;
        Material m = w.getBlockState(pos).getMaterial();
        return m == Material.WATER || m == Material.LAVA;
    }
}
