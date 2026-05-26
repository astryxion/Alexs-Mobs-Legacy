package com.github.alexthe666.alexsmobs.entity.ai;

import com.github.alexthe666.alexsmobs.entity.EntityHummingbird;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nullable;
import java.util.Random;

public class HummingbirdAIWander extends EntityAIBase {
    private final EntityHummingbird fly;
    private final int rangeXZ;
    private final int chance;
    private final float speed;
    private Vec3d moveToPoint = null;

    public HummingbirdAIWander(EntityHummingbird fly, int rangeXZ, int rangeY, int chance, float speed) {
        this.setMutexBits(1);
        this.fly = fly;
        this.rangeXZ = rangeXZ;
        this.chance = chance;
        this.speed = speed;
    }

    @Override
    public boolean shouldExecute() {
        return fly.hummingStill > 10 && fly.getRNG().nextInt(chance) == 0 && !fly.getMoveHelper().isUpdating();
    }

    @Override
    public void resetTask() {
        moveToPoint = null;
    }

    @Override
    public boolean shouldContinueExecuting() {
        return moveToPoint != null && fly.getDistanceSq(moveToPoint.x, moveToPoint.y, moveToPoint.z) > 0.85D;
    }

    @Override
    public void startExecuting() {
        moveToPoint = this.getRandomLocation();
        if (moveToPoint != null) {
            fly.getMoveHelper().setMoveTo(moveToPoint.x, moveToPoint.y, moveToPoint.z, speed);
        }
    }

    @Override
    public void updateTask() {
        if (moveToPoint != null) {
            fly.getMoveHelper().setMoveTo(moveToPoint.x, moveToPoint.y, moveToPoint.z, speed);
        }
    }

    @Nullable
    private Vec3d getRandomLocation() {
        Random random = fly.getRNG();
        BlockPos blockpos = null;
        BlockPos origin = fly.getFeederPos() == null ? fly.getPosition() : fly.getFeederPos();
        for (int i = 0; i < 15; i++) {
            BlockPos blockpos1 = origin.add(random.nextInt(rangeXZ) - rangeXZ / 2, 1, random.nextInt(rangeXZ) - rangeXZ / 2);
            while (fly.world.isAirBlock(blockpos1) && blockpos1.getY() > 0) {
                blockpos1 = blockpos1.down();
            }
            blockpos1 = blockpos1.up(1 + random.nextInt(3));
            if (fly.world.isAirBlock(blockpos1.down()) && fly.canBlockBeSeen(blockpos1) && fly.world.isAirBlock(blockpos1) && !fly.world.isAirBlock(blockpos1.down(2))) {
                blockpos = blockpos1;
            }
        }
        return blockpos == null ? null : new Vec3d(blockpos.getX() + 0.5D, blockpos.getY() + 0.5D, blockpos.getZ() + 0.5D);
    }
}
