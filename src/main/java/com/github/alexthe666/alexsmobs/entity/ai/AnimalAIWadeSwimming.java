package com.github.alexthe666.alexsmobs.entity.ai;

import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.math.BlockPos;

/**
 * Forge 1.12.2 port of 1.16 {@code AnimalAIWadeSwimming}.
 */
public class AnimalAIWadeSwimming extends EntityAIBase {

    private final EntityLiving entity;

    public AnimalAIWadeSwimming(EntityLiving entity) {
        this.entity = entity;
        this.setMutexBits(4);
    }

    @Override
    public boolean shouldExecute() {
        if (this.entity.isInLava()) {
            return true;
        }
        if (this.entity.isInWater()) {
            BlockPos pos = new BlockPos(this.entity.posX, this.entity.getEntityBoundingBox().maxY, this.entity.posZ);
            return this.entity.world.getBlockState(pos).getMaterial() == Material.WATER;
        }
        return false;
    }

    @Override
    public void updateTask() {
        if (this.entity.getRNG().nextFloat() < 0.8F) {
            this.entity.getJumpHelper().setJumping();
        }
    }
}
