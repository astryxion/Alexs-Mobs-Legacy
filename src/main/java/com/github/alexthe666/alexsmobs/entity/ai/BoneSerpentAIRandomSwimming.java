package com.github.alexthe666.alexsmobs.entity.ai;

import com.github.alexthe666.alexsmobs.entity.EntityBoneSerpent;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class BoneSerpentAIRandomSwimming extends EntityAIBase {
    private final EntityBoneSerpent creature;
    private final double speed;
    private final int executionChance;
    private double x;
    private double y;
    private double z;
    private boolean mustUpdate;

    public BoneSerpentAIRandomSwimming(EntityBoneSerpent creature, double speed, int chance) {
        this.creature = creature;
        this.speed = speed;
        this.executionChance = chance;
        this.setMutexBits(1);
    }

    @Override
    public boolean shouldExecute() {
        if (this.creature.isBeingRidden() || this.creature.getAttackTarget() != null) {
            return false;
        } else {
            if (!this.mustUpdate) {
                if (this.creature.getRNG().nextInt(this.executionChance) != 0) {
                    return false;
                }
            }
            Vec3d vector3d = this.getPosition();
            if (vector3d == null) {
                return false;
            } else {
                this.x = vector3d.x;
                this.y = vector3d.y;
                this.z = vector3d.z;
                this.mustUpdate = false;
                return true;
            }
        }
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
        if (this.creature.getRNG().nextFloat() < (this.creature.isInLava() ? 0.7F : 0.3F)) {
            Vec3d vector3d = findSurfaceTarget(this.creature, 32, 16);
            if (vector3d != null) {
                return vector3d;
            }
        }
        Vec3d vector3d = RandomPositionGenerator.findRandomTarget(this.creature, 32, 16);

        for (int i = 0; vector3d != null && !this.creature.world.getBlockState(new BlockPos(vector3d)).getBlock().isPassable(this.creature.world, new BlockPos(vector3d))
                && !canStandInFluid(new BlockPos(vector3d)) && i++ < 10; vector3d = RandomPositionGenerator.findRandomTarget(this.creature, 10, 7)) {
        }

        return vector3d;
    }

    private boolean canStandInFluid(BlockPos pos) {
        World w = this.creature.world;
        Material m = w.getBlockState(pos).getMaterial();
        return m == Material.WATER || m == Material.LAVA;
    }

    private Vec3d findSurfaceTarget(EntityCreature creature, int xz, int yRange) {
        World world = creature.world;
        BlockPos upPos = new BlockPos(creature.posX, creature.posY, creature.posZ);
        while (world.getBlockState(upPos).getMaterial() == Material.LAVA
                || world.getBlockState(upPos).getMaterial() == Material.WATER) {
            upPos = upPos.up();
        }
        if (isAirAbove(world, upPos.down(), 0, 0, 0) && canJumpTo(world, upPos.down(), 0, 0, 0)) {
            return new Vec3d(upPos.getX() + 0.5F, upPos.getY() + 3.5F, upPos.getZ() + 0.5F);
        }
        return null;
    }

    private boolean canJumpTo(World world, BlockPos pos, int dx, int dz, int scale) {
        BlockPos blockpos = pos.add(dx * scale, 0, dz * scale);
        Material m = world.getBlockState(blockpos).getMaterial();
        return (m == Material.WATER || m == Material.LAVA) && !world.getBlockState(blockpos).getMaterial().blocksMovement();
    }

    private boolean isAirAbove(World world, BlockPos pos, int dx, int dz, int scale) {
        return world.isAirBlock(pos.add(dx * scale, 1, dz * scale)) && world.isAirBlock(pos.add(dx * scale, 2, dz * scale));
    }
}
