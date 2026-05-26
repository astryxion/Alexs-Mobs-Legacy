package com.github.alexthe666.alexsmobs.entity.ai;

import com.github.alexthe666.alexsmobs.entity.EntityWarpedToad;
import com.github.alexthe666.alexsmobs.entity.ISemiAquatic;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nullable;

public class WarpedToadAIRandomSwimming extends EntityAIBase {
    private final EntityWarpedToad creature;
    private final double speed;
    private final int executionChance;
    private double x;
    private double y;
    private double z;
    private boolean mustUpdate;

    public WarpedToadAIRandomSwimming(EntityWarpedToad creature, double speed, int chance) {
        this.creature = creature;
        this.speed = speed;
        this.executionChance = chance;
        this.setMutexBits(1);
    }

    @Override
    public boolean shouldExecute() {
        if (this.creature.isBeingRidden() || this.creature.isSitting() || this.creature.getAttackTarget() != null
                || (!this.creature.isInWater() && !this.creature.isInLava()
                && this.creature instanceof ISemiAquatic && !((ISemiAquatic) this.creature).shouldEnterWater())) {
            return false;
        }
        if (!this.mustUpdate) {
            if (this.creature.getRNG().nextInt(this.executionChance) != 0) {
                return false;
            }
        }
        Vec3d vector3d = this.getPosition();
        if (vector3d == null) {
            return false;
        }
        this.x = vector3d.x;
        this.y = vector3d.y;
        this.z = vector3d.z;
        this.mustUpdate = false;
        return true;
    }

    @Override
    public boolean shouldContinueExecuting() {
        return !this.creature.getNavigator().noPath();
    }

    @Override
    public void startExecuting() {
        this.creature.getNavigator().tryMoveToXYZ(this.x, this.y, this.z, this.speed);
    }

    @Nullable
    protected Vec3d getPosition() {
        if (this.creature.hasHome() && this.creature.getDistanceSq(this.creature.getHomePosition()) > this.creature.getMaximumHomeDistance() * this.creature.getMaximumHomeDistance()) {
            return RandomPositionGenerator.findRandomTargetBlockTowards(this.creature, 7, 3, new Vec3d(this.creature.getHomePosition().getX() + 0.5D, this.creature.getHomePosition().getY(), this.creature.getHomePosition().getZ() + 0.5D));
        }
        if (this.creature.getRNG().nextFloat() < 0.3F) {
            Vec3d vector3d = findSurfaceTarget(this.creature, 15, 7);
            if (vector3d != null) {
                return vector3d;
            }
        }
        Vec3d vector3d = RandomPositionGenerator.findRandomTarget(this.creature, 7, 3);
        for (int i = 0; vector3d != null && !isValidSwimTarget(new BlockPos(vector3d)) && i++ < 15; vector3d = RandomPositionGenerator.findRandomTarget(this.creature, 10, 7)) {
        }
        return vector3d;
    }

    private boolean isValidSwimTarget(BlockPos pos) {
        Material mat = this.creature.world.getBlockState(pos).getMaterial();
        if (mat != Material.LAVA && mat != Material.WATER) {
            return false;
        }
        return this.creature.world.getBlockState(pos).getBlock().isPassable(this.creature.world, pos);
    }

    private boolean canJumpTo(BlockPos pos, int dx, int dz, int scale) {
        BlockPos blockpos = pos.add(dx * scale, 0, dz * scale);
        Material mat = this.creature.world.getBlockState(blockpos).getMaterial();
        return mat == Material.LAVA || mat == Material.WATER;
    }

    private boolean isAirAbove(BlockPos pos, int dx, int dz, int scale) {
        return this.creature.world.isAirBlock(pos.add(dx * scale, 1, dz * scale))
                && this.creature.world.isAirBlock(pos.add(dx * scale, 2, dz * scale));
    }

    @Nullable
    private Vec3d findSurfaceTarget(EntityCreature creature, int range, int yRange) {
        BlockPos upPos = creature.getPosition();
        while (isLiquidAt(upPos)) {
            upPos = upPos.up();
        }
        if (isAirAbove(upPos.down(), 0, 0, 0) && canJumpTo(upPos.down(), 0, 0, 0)) {
            return new Vec3d(upPos.getX() + 0.5F, upPos.getY() - 1F, upPos.getZ() + 0.5F);
        }
        return null;
    }

    private boolean isLiquidAt(BlockPos pos) {
        Material mat = creature.world.getBlockState(pos).getMaterial();
        return mat == Material.WATER || mat == Material.LAVA;
    }
}
