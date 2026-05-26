package com.github.alexthe666.alexsmobs.entity.ai;

import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nullable;

/**
 * 1.16 {@code RandomWalkingGoal} swim-bottom variant: prefers walking on the seafloor under water.
 */
public class AnimalAISwimBottom extends EntityAIBase {
    private final EntityCreature creature;
    private final double speed;
    private double targetX;
    private double targetY;
    private double targetZ;

    public AnimalAISwimBottom(EntityCreature creature, double speed, int unused1) {
        this.creature = creature;
        this.speed = speed;
        this.setMutexBits(1);
    }

    @Override
    public boolean shouldExecute() {
        Vec3d dest = this.pickPosition();
        if (dest == null) {
            return false;
        }
        this.targetX = dest.x;
        this.targetY = dest.y;
        this.targetZ = dest.z;
        return true;
    }

    @Override
    public void startExecuting() {
        this.creature.getNavigator().tryMoveToXYZ(this.targetX, this.targetY, this.targetZ, this.speed);
    }

    @Override
    public boolean shouldContinueExecuting() {
        return !this.creature.getNavigator().noPath();
    }

    @Nullable
    private Vec3d pickPosition() {
        Vec3d vec = RandomPositionGenerator.findRandomTarget(this.creature, 10, 7);
        for (int var2 = 0;
             vec != null
                     && this.creature.world.getBlockState(new BlockPos(vec.x, vec.y, vec.z)).getMaterial() != Material.WATER
                     && var2++ < 10;
             vec = RandomPositionGenerator.findRandomTarget(this.creature, 10, 7)) {
        }
        int yDrop = 1 + this.creature.getRNG().nextInt(3);
        if (vec != null) {
            BlockPos pos = new BlockPos(vec.x, vec.y, vec.z);
            while (this.creature.world.getBlockState(pos).getMaterial() == Material.WATER && pos.getY() > 1) {
                pos = pos.down();
            }
            pos = pos.up();
            int yUp = 0;
            while (this.creature.world.getBlockState(pos).getMaterial() == Material.WATER && yUp < yDrop) {
                pos = pos.up();
                yUp++;
            }
            return new Vec3d((double) pos.getX() + 0.5D, (double) pos.getY() + 0.5D, (double) pos.getZ() + 0.5D);
        }
        return null;
    }
}
