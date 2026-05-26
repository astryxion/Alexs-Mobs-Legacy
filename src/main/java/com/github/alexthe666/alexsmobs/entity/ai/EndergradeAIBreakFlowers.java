package com.github.alexthe666.alexsmobs.entity.ai;

import com.github.alexthe666.alexsmobs.entity.EntityEndergrade;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Random;

/**
 * Forge 1.12.2 port of 1.16 {@code EndergradeAIBreakFlowers} / {@code MoveToBlockGoal}.
 */
public class EndergradeAIBreakFlowers extends EntityAIBase {

    private final EntityEndergrade endergrade;
    private final double movementSpeed;
    @Nullable
    private BlockPos destinationBlock = null;
    private int idleAtFlowerTime = 0;
    private boolean isAboveDestinationBear;
    private int timeoutCounter;
    private int runDelay;

    public EndergradeAIBreakFlowers(EntityEndergrade endergrade) {
        this.endergrade = endergrade;
        this.movementSpeed = 1.0D;
        this.setMutexBits(1);
    }

    @Override
    public boolean shouldExecute() {
        if (endergrade.isChild() || endergrade.hasItemTarget) {
            return false;
        }
        if (this.runDelay > 0) {
            --this.runDelay;
            return false;
        }
        this.runDelay = 20 + endergrade.getRNG().nextInt(40);
        return findDestination();
    }

    private boolean findDestination() {
        BlockPos origin = new BlockPos(endergrade);
        Random rand = endergrade.getRNG();
        for (int tries = 0; tries < 32; ++tries) {
            BlockPos p = origin.add(
                    rand.nextInt(32) - 16, rand.nextInt(8) - 4, rand.nextInt(32) - 16);
            if (shouldMoveTo(endergrade.world, p)) {
                destinationBlock = p;
                timeoutCounter = 0;
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean shouldContinueExecuting() {
        return !endergrade.hasItemTarget && destinationBlock != null;
    }

    @Override
    public void resetTask() {
        idleAtFlowerTime = 0;
        destinationBlock = null;
        isAboveDestinationBear = false;
        this.endergrade.stopWandering = false;
    }

    public double getTargetDistanceSq() {
        return 2.0D;
    }

    private boolean isWithinXZDist(BlockPos blockpos, Vec3d positionVec, double distance) {
        return blockpos.distanceSq(positionVec.x, positionVec.y, positionVec.z) < distance * distance;
    }

    protected boolean getIsAboveDestination() {
        return this.isAboveDestinationBear;
    }

    @Override
    public void updateTask() {
        if (destinationBlock == null) {
            return;
        }
        this.endergrade.stopWandering = true;
        if (!isWithinXZDist(destinationBlock, new Vec3d(endergrade.posX, endergrade.posY, endergrade.posZ), this.getTargetDistanceSq())) {
            this.isAboveDestinationBear = false;
            ++this.timeoutCounter;
            endergrade.getMoveHelper().setMoveTo(
                    destinationBlock.getX() + 0.5D,
                    destinationBlock.getY() - 0.5D,
                    destinationBlock.getZ() + 0.5D,
                    movementSpeed);
        } else {
            this.isAboveDestinationBear = true;
            --this.timeoutCounter;
        }

        if (this.getIsAboveDestination() && Math.abs(endergrade.posY - destinationBlock.getY()) <= 2) {
            endergrade.getLookHelper().setLookPosition(
                    destinationBlock.getX() + 0.5D,
                    destinationBlock.getY(),
                    destinationBlock.getZ() + 0.5D,
                    30.0F,
                    endergrade.getVerticalFaceSpeed());
            if (this.idleAtFlowerTime >= 20) {
                endergrade.bite();
                endergrade.world.destroyBlock(destinationBlock, true);
                this.resetTask();
            } else {
                ++this.idleAtFlowerTime;
            }
        }
    }

    protected boolean shouldMoveTo(World worldIn, BlockPos pos) {
        return worldIn.getBlockState(pos).getBlock() == Blocks.CHORUS_FLOWER;
    }
}
