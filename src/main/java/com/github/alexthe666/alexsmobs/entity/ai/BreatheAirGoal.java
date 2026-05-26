package com.github.alexthe666.alexsmobs.entity.ai;

import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

/**
 * Seeks breathable blocks when air is low (1.16 {@code BreatheAirGoal} parity).
 */
public class BreatheAirGoal extends EntityAIBase {
    private final EntityLiving entityLiving;

    public BreatheAirGoal(EntityLiving entityLiving) {
        this.entityLiving = entityLiving;
        this.setMutexBits(3);
    }

    @Override
    public boolean shouldExecute() {
        return this.entityLiving.getAir() < 140 && this.submergedInFluid();
    }

    private boolean submergedInFluid() {
        return this.entityLiving.isInsideOfMaterial(Material.WATER)
                || this.entityLiving.isInsideOfMaterial(Material.LAVA);
    }

    @Override
    public boolean shouldContinueExecuting() {
        return this.shouldExecute();
    }

    @Override
    public void startExecuting() {
        this.tryNavigate();
    }

    @Override
    public void updateTask() {
        this.tryNavigate();
    }

    private void tryNavigate() {
        World world = this.entityLiving.world;
        BlockPos best = null;
        BlockPos.MutableBlockPos mpos = new BlockPos.MutableBlockPos();
        int bx = MathHelper.floor(this.entityLiving.posX);
        int by = MathHelper.floor(this.entityLiving.posY);
        int bz = MathHelper.floor(this.entityLiving.posZ);
        for (int x = bx - 1; x <= bx + 1; x++) {
            for (int y = by; y <= by + 8; y++) {
                for (int z = bz - 1; z <= bz + 1; z++) {
                    mpos.setPos(x, y, z);
                    if (canBreatheHere(world, mpos)) {
                        int down = MathHelper.floor((double) (this.entityLiving.height * 0.25F));
                        best = new BlockPos(mpos.getX(), mpos.getY() - down, mpos.getZ());
                        break;
                    }
                }
                if (best != null) {
                    break;
                }
            }
            if (best != null) {
                break;
            }
        }
        if (best == null) {
            best = new BlockPos(this.entityLiving.posX, this.entityLiving.posY + 4.0D, this.entityLiving.posZ);
        }
        if (this.entityLiving.isInsideOfMaterial(Material.WATER)) {
            this.entityLiving.motionY += 0.05D;
        }
        this.entityLiving.getNavigator().tryMoveToXYZ(best.getX(), best.getY(), best.getZ(), 0.7D);
    }

    private static boolean canBreatheHere(World world, BlockPos pos) {
        return world.isAirBlock(pos)
                && (world.getBlockState(pos.down()).getMaterial() == Material.WATER
                        || world.getBlockState(pos.down()).getMaterial() == Material.LAVA);
    }
}
