package com.github.alexthe666.alexsmobs.entity.ai;

import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nullable;

/**
 * 1.12.2 port of {@code RandomWalkingGoal}-style random swimming (1.16 extended {@code RandomWalkingGoal}).
 * Preserves home return, surface bias, water-path validation, and submerged depth adjustment.
 */
public class AnimalAIRandomSwimming extends EntityAIBase {

    protected final EntityCreature creature;
    private final double speed;
    private final int executionChance;
    private final int xzSpread;
    private final boolean submerged;
    private boolean mustUpdate;
    private double targetX;
    private double targetY;
    private double targetZ;

    public AnimalAIRandomSwimming(EntityAnimal creature, double speed, int chance, int xzSpread) {
        this(creature, speed, chance, xzSpread, false);
    }

    public AnimalAIRandomSwimming(EntityAnimal creature, double speed, int chance, int xzSpread, boolean submerged) {
        this.creature = creature;
        this.speed = speed;
        this.executionChance = chance;
        this.xzSpread = xzSpread;
        this.submerged = submerged;
        this.setMutexBits(1);
    }

    @Override
    public boolean shouldExecute() {
        if (this.creature.isBeingRidden()
                || this.creature.getAttackTarget() != null
                || (!this.creature.isInWater() && !this.creature.isInLava())) {
            return false;
        }
        if (!this.mustUpdate) {
            if (this.creature.getRNG().nextInt(this.executionChance) != 0) {
                return false;
            }
        }
        Vec3d dest = this.pickPosition();
        if (dest == null) {
            return false;
        }
        this.targetX = dest.x;
        this.targetY = dest.y;
        this.targetZ = dest.z;
        this.mustUpdate = false;
        return true;
    }

    @Nullable
    protected Vec3d pickPosition() {
        if (this.creature.getMaximumHomeDistance() >= 0.0F
                && this.creature.getDistanceSq(
                this.creature.getHomePosition().getX() + 0.5D,
                this.creature.getHomePosition().getY() + 0.5D,
                this.creature.getHomePosition().getZ() + 0.5D)
                > (double) (this.creature.getMaximumHomeDistance() * this.creature.getMaximumHomeDistance())) {
            Vec3d toward = RandomPositionGenerator.findRandomTargetBlockTowards(
                    this.creature,
                    this.xzSpread,
                    3,
                    new Vec3d(
                            (double) this.creature.getHomePosition().getX() + 0.5D,
                            (double) this.creature.getHomePosition().getY() + 0.5D,
                            (double) this.creature.getHomePosition().getZ() + 0.5D));
            if (toward != null) {
                return toward;
            }
        }
        if (this.creature.getRNG().nextFloat() < 0.3F) {
            Vec3d surface = this.findSurfaceTarget(this.creature, this.xzSpread, 7);
            if (surface != null) {
                return surface;
            }
        }
        Vec3d vector3d = RandomPositionGenerator.findRandomTarget(this.creature, this.xzSpread, 3);
        for (int i = 0;
             vector3d != null
                     && this.creature.world.getBlockState(new BlockPos(vector3d)).getMaterial() != Material.WATER
                     && i++ < 15;
             vector3d = RandomPositionGenerator.findRandomTarget(this.creature, 10, 7)) {
        }
        if (this.submerged && vector3d != null) {
            BlockPos base = new BlockPos(vector3d);
            if (this.creature.world.getBlockState(base.up()).getMaterial() != Material.WATER) {
                vector3d = vector3d.addVector(0.0D, -2.0D, 0.0D);
            } else if (this.creature.world.getBlockState(base.up(2)).getMaterial() != Material.WATER) {
                vector3d = vector3d.addVector(0.0D, -3.0D, 0.0D);
            }
        }
        return vector3d;
    }

    private boolean canJumpTo(BlockPos pos, int dx, int dz, int scale) {
        BlockPos blockpos = pos.add(dx * scale, 0, dz * scale);
        net.minecraft.block.state.IBlockState st = this.creature.world.getBlockState(blockpos);
        Material mat = st.getMaterial();
        return mat == Material.LAVA || (mat == Material.WATER && !mat.blocksMovement());
    }

    private boolean isAirAbove(BlockPos pos, int dx, int dz, int scale) {
        return this.creature.world.getBlockState(pos.add(dx * scale, 1, dz * scale)).getMaterial() == Material.AIR
                && this.creature.world.getBlockState(pos.add(dx * scale, 2, dz * scale)).getMaterial() == Material.AIR;
    }

    @Nullable
    private Vec3d findSurfaceTarget(EntityCreature creature, int xz, int unused) {
        BlockPos upPos = new BlockPos(creature);
        while (creature.world.getBlockState(upPos).getMaterial() == Material.WATER
                || creature.world.getBlockState(upPos).getMaterial() == Material.LAVA) {
            upPos = upPos.up();
        }
        if (this.isAirAbove(upPos.down(), 0, 0, 0) && this.canJumpTo(upPos.down(), 0, 0, 0)) {
            return new Vec3d((double) upPos.getX() + 0.5D, (double) upPos.getY() - 1.0D, (double) upPos.getZ() + 0.5D);
        }
        return null;
    }

    @Override
    public boolean shouldContinueExecuting() {
        return !this.creature.getNavigator().noPath();
    }

    @Override
    public void startExecuting() {
        this.creature.getNavigator().tryMoveToXYZ(this.targetX, this.targetY, this.targetZ, this.speed);
    }

    /**
     * Used by subclasses (e.g. cachalot whale) to add extra gates without copying pick logic.
     */
    public void setMustUpdate() {
        this.mustUpdate = true;
    }
}
