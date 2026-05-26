package com.github.alexthe666.alexsmobs.entity.ai;

import com.github.alexthe666.alexsmobs.entity.EntityBoneSerpent;
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
        if ((this.creature.jumpCooldown == 0 || this.creature.onGround)
                && !isLiquidMaterial(w.getBlockState(this.creature.getPosition()).getMaterial())) {
            targetPos = generateTarget();
            return targetPos != null;
        }
        return false;
    }

    private static boolean isLiquidMaterial(Material m) {
        return m == Material.WATER || m == Material.LAVA;
    }

    @Override
    public void startExecuting() {
        if (targetPos != null) {
            this.creature.getNavigator().tryMoveToXYZ(targetPos.getX(), targetPos.getY(), targetPos.getZ(), 0.5D);
        }
    }

    @Override
    public boolean shouldContinueExecuting() {
        World w = this.creature.world;
        return !this.creature.getNavigator().noPath() && targetPos != null
                && !isLiquidMaterial(w.getBlockState(this.creature.getPosition()).getMaterial());
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
            if (mat == Material.WATER || mat == Material.LAVA) {
                blockpos = blockpos1;
            }
        }
        return blockpos;
    }
}
