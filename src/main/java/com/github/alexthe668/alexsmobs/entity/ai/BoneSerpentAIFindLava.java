package com.github.alexthe668.alexsmobs.entity.ai;

import com.github.alexthe668.alexsmobs.entity.EntityBoneSerpent;
import net.minecraft.block.material.Material;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Random;

public class BoneSerpentAIFindLava extends EntityAIBase {
    private final EntityBoneSerpent creature;
    private BlockPos targetPos;

    public BoneSerpentAIFindLava(EntityBoneSerpent creature) {
        this.creature = creature;
        this.setMutexBits(7);
    }

    @Override
    public boolean shouldExecute() {
        World w = this.creature.world;
        if ((this.creature.jumpCooldown == 0 || this.creature.onGround) && !isInLava(w)) {
            targetPos = generateTarget();
            return targetPos != null;
        }
        return false;
    }

    private boolean isInLava(World w) {
        return w.getBlockState(this.creature.getPosition()).getMaterial() == Material.LAVA;
    }

    @Override
    public void startExecuting() {
        this.creature.getNavigator().clearPath();
    }

    @Override
    public boolean shouldContinueExecuting() {
        World w = this.creature.world;
        return targetPos != null && !isInLava(w);
    }

    @Override
    public void updateTask() {
        if (targetPos == null) {
            return;
        }
        this.creature.getNavigator().clearPath();
        FindWaterSteering.steerToward(this.creature, targetPos);
        this.creature.motionX *= 0.45D;
        this.creature.motionZ *= 0.45D;
        this.creature.getLookHelper().setLookPosition(targetPos.getX() + 0.5D, targetPos.getY() + 0.5D, targetPos.getZ() + 0.5D, 10.0F, 10.0F);
    }

    public BlockPos generateTarget() {
        BlockPos blockpos = null;
        int range = 16;
        for (int i = 0; i < 15; i++) {
            BlockPos blockpos1 = this.creature.getPosition().add(this.creature.getRNG().nextInt(range) - range / 2, 3, this.creature.getRNG().nextInt(range) - range / 2);
            while (this.creature.world.isAirBlock(blockpos1) && blockpos1.getY() > 1) {
                blockpos1 = blockpos1.down();
            }
            Material mat = this.creature.world.getBlockState(blockpos1).getMaterial();
            if (mat == Material.LAVA) {
                blockpos = blockpos1;
            }
        }
        return blockpos;
    }
}
