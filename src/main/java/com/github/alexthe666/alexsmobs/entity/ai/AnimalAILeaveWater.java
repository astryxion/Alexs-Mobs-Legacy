package com.github.alexthe666.alexsmobs.entity.ai;

import com.github.alexthe666.alexsmobs.entity.ISemiAquatic;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nullable;

public class AnimalAILeaveWater extends EntityAIBase {
    private final EntityCreature creature;
    private BlockPos targetPos;
    private int executionChance = 30;

    public AnimalAILeaveWater(EntityCreature creature) {
        this.creature = creature;
        this.setMutexBits(3);
    }

    @Override
    public boolean shouldExecute() {
        if (isWaterAt(this.creature.getPosition()) && (this.creature.getAttackTarget() != null || this.creature.getRNG().nextInt(executionChance) == 0)) {
            if (this.creature instanceof ISemiAquatic && ((ISemiAquatic) this.creature).shouldLeaveWater()) {
                targetPos = generateTarget();
                return targetPos != null;
            }
        }
        return false;
    }

    @Override
    public void startExecuting() {
        if (targetPos != null) {
            this.creature.getNavigator().tryMoveToXYZ(targetPos.getX(), targetPos.getY(), targetPos.getZ(), 1D);
        }
    }

    @Override
    public void updateTask() {
        if (targetPos != null) {
            this.creature.getNavigator().tryMoveToXYZ(targetPos.getX(), targetPos.getY(), targetPos.getZ(), 1D);
        }
        if (this.creature.collidedHorizontally && this.creature.isInWater()) {
            float f1 = creature.rotationYaw * ((float) Math.PI / 180F);
            creature.motionX += (double) (-MathHelper.sin(f1) * 0.2F);
            creature.motionY += 0.1D;
            creature.motionZ += (double) (MathHelper.cos(f1) * 0.2F);
        }
    }

    @Override
    public boolean shouldContinueExecuting() {
        if (this.creature instanceof ISemiAquatic && !((ISemiAquatic) this.creature).shouldLeaveWater()) {
            this.creature.getNavigator().clearPath();
            return false;
        }
        return !this.creature.getNavigator().noPath() && targetPos != null && !isWaterAt(targetPos);
    }

    @Nullable
    public BlockPos generateTarget() {
        Vec3d vector3d = RandomPositionGenerator.findRandomTarget(this.creature, 23, 7);
        int tries = 0;
        while (vector3d != null && tries < 8) {
            boolean waterDetected = false;
            for (BlockPos blockpos1 : BlockPos.getAllInBox(
                    new BlockPos(MathHelper.floor(vector3d.x - 2.0D), MathHelper.floor(vector3d.y - 1.0D), MathHelper.floor(vector3d.z - 2.0D)),
                    new BlockPos(MathHelper.floor(vector3d.x + 2.0D), MathHelper.floor(vector3d.y), MathHelper.floor(vector3d.z + 2.0D)))) {
                if (isWaterAt(blockpos1)) {
                    waterDetected = true;
                    break;
                }
            }
            if (waterDetected) {
                vector3d = RandomPositionGenerator.findRandomTarget(this.creature, 23, 7);
            } else {
                return new BlockPos(vector3d.x, vector3d.y, vector3d.z);
            }
            tries++;
        }
        return null;
    }

    private boolean isWaterAt(BlockPos pos) {
        return this.creature.world.getBlockState(pos).getMaterial() == Material.WATER;
    }
}
