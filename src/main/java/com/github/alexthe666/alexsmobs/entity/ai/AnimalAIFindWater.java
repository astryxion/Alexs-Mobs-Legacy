package com.github.alexthe666.alexsmobs.entity.ai;

import com.github.alexthe666.alexsmobs.entity.ISemiAquatic;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.math.BlockPos;

import java.util.Random;

public class AnimalAIFindWater extends EntityAIBase {
    private final EntityCreature creature;
    private BlockPos targetPos;
    private int executionChance = 30;
    private int runTicks;

    public AnimalAIFindWater(EntityCreature creature) {
        this.creature = creature;
        this.setMutexBits(3);
    }

    @Override
    public boolean shouldExecute() {
        if (this.creature.onGround && !isWaterAt(this.creature.getPosition())) {
            if (this.creature instanceof ISemiAquatic && ((ISemiAquatic) this.creature).shouldEnterWater()
                    && (this.creature.getAttackTarget() != null || this.creature.getRNG().nextInt(executionChance) == 0)) {
                targetPos = generateTarget();
                return targetPos != null;
            }
        }
        return false;
    }

    @Override
    public void startExecuting() {
        runTicks = 0;
        moveTowardWater();
    }

    @Override
    public void updateTask() {
        runTicks++;
        moveTowardWater();
    }

    @Override
    public boolean shouldContinueExecuting() {
        if (this.creature instanceof ISemiAquatic && !((ISemiAquatic) this.creature).shouldEnterWater()) {
            this.creature.getNavigator().clearPath();
            return false;
        }
        if (runTicks > 200 || isWaterAt(this.creature.getPosition())) {
            return false;
        }
        return targetPos != null;
    }

    @Override
    public void resetTask() {
        targetPos = null;
        runTicks = 0;
        this.creature.getNavigator().clearPath();
    }

    private void moveTowardWater() {
        if (targetPos == null) {
            return;
        }
        if (this.creature.isInWater() || this.creature.isInLava()) {
            this.creature.getNavigator().tryMoveToXYZ(targetPos.getX(), targetPos.getY(), targetPos.getZ(), 1D);
            return;
        }
        this.creature.getNavigator().clearPath();
        if (!this.creature.getNavigator().tryMoveToXYZ(targetPos.getX(), targetPos.getY(), targetPos.getZ(), 1D)) {
            FindWaterSteering.steerToward(this.creature, targetPos);
        }
    }

    public BlockPos generateTarget() {
        BlockPos blockpos = null;
        Random random = new Random();
        int range = this.creature instanceof ISemiAquatic ? ((ISemiAquatic) this.creature).getWaterSearchRange() : 14;
        for (int i = 0; i < 15; i++) {
            BlockPos blockpos1 = this.creature.getPosition().add(random.nextInt(range) - range / 2, 3, random.nextInt(range) - range / 2);
            while (this.creature.world.isAirBlock(blockpos1) && blockpos1.getY() > 1) {
                blockpos1 = blockpos1.down();
            }
            if (isWaterAt(blockpos1)) {
                blockpos = blockpos1;
            }
        }
        return blockpos;
    }

    private boolean isWaterAt(BlockPos pos) {
        return this.creature.world.getBlockState(pos).getMaterial() == Material.WATER;
    }
}
