package com.github.alexthe666.alexsmobs.entity.ai;

import com.github.alexthe666.alexsmobs.entity.EntityCrow;
import com.github.alexthe666.alexsmobs.misc.AMTagRegistry;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Random;

/**
 * Forge 1.12.2 port of 1.16 {@code CrowAICircleCrops} / {@code MoveToBlockGoal}.
 */
public class CrowAICircleCrops extends EntityAIBase {

    private final EntityCrow crow;
    @Nullable
    private BlockPos destinationBlock;
    private int idleAtFlowerTime = 0;
    private boolean isAboveDestinationBear;
    private int timeoutCounter;
    float circlingTime = 0;
    float circleDistance = 2;
    float yLevel = 2;
    boolean clockwise = false;
    boolean circlePhase = false;

    public CrowAICircleCrops(EntityCrow bird) {
        this.crow = bird;
        this.setMutexBits(1);
    }

    @Override
    public void startExecuting() {
        circlePhase = true;
        clockwise = crow.getRNG().nextBoolean();
        yLevel = 1 + crow.getRNG().nextInt(3);
        circleDistance = 1 + crow.getRNG().nextInt(3);
        timeoutCounter = 0;
    }

    @Override
    public boolean shouldExecute() {
        if (crow.isChild() || (crow.getAttackTarget() != null && crow.getAttackTarget().isEntityAlive()) || crow.isTamed() || crow.fleePumpkinFlag != 0 || crow.aiItemFlag) {
            return false;
        }
        return searchForDestination();
    }

    @Override
    public boolean shouldContinueExecuting() {
        return destinationBlock != null && (crow.getAttackTarget() == null || !crow.getAttackTarget().isEntityAlive()) && !crow.isTamed() && !crow.aiItemFlag && crow.fleePumpkinFlag == 0;
    }

    @Override
    public void resetTask() {
        idleAtFlowerTime = 0;
        circlingTime = 0;
        timeoutCounter = 0;
        destinationBlock = null;
    }

    public double getTargetDistanceSq() {
        return 1D;
    }

    @Override
    public void updateTask() {
        if (destinationBlock == null) {
            return;
        }
        BlockPos blockpos = destinationBlock;
        if (circlePhase) {
            this.timeoutCounter = 0;
            BlockPos circlePos = getVultureCirclePos(blockpos);
            if (circlePos != null) {
                crow.setFlying(true);
                crow.getMoveHelper().setMoveTo(circlePos.getX() + 0.5D, circlePos.getY() + 0.5D, circlePos.getZ() + 0.5D, 0.7F);
            }
            circlingTime++;
            if (circlingTime > 200) {
                circlingTime = 0;
                circlePhase = false;
            }
        } else {
            if (crow.onGround) {
                crow.setFlying(false);
            }
            if (!isWithinXZDist(blockpos, new Vec3d(crow.posX, crow.posY, crow.posZ), this.getTargetDistanceSq())) {
                this.isAboveDestinationBear = false;
                ++this.timeoutCounter;
                crow.getNavigator().tryMoveToXYZ((double) blockpos.getX() + 0.5D, blockpos.getY() - 0.5D, (double) blockpos.getZ() + 0.5D, 1);
            } else {
                this.isAboveDestinationBear = true;
                --this.timeoutCounter;
            }

            if (this.getIsAboveDestination()) {
                crow.getLookHelper().setLookPosition(destinationBlock.getX() + 0.5D, destinationBlock.getY(), destinationBlock.getZ() + 0.5D, 30.0F, crow.getVerticalFaceSpeed());
                if (this.idleAtFlowerTime >= 5) {
                    this.pollinate();
                    this.resetTask();
                } else {
                    crow.peck();
                    ++this.idleAtFlowerTime;
                }
            }
        }
    }

    public BlockPos getVultureCirclePos(BlockPos target) {
        float angle = (0.01745329251F * 8 * (clockwise ? -circlingTime : circlingTime));
        double extraX = circleDistance * MathHelper.sin(angle);
        double extraZ = circleDistance * MathHelper.cos(angle);
        BlockPos pos = new BlockPos(target.getX() + 0.5F + extraX, target.getY() + 1 + yLevel, target.getZ() + 0.5F + extraZ);
        if (crow.world.isAirBlock(pos)) {
            return pos;
        }
        return null;
    }

    private boolean isWithinXZDist(BlockPos blockpos, Vec3d positionVec, double distance) {
        return blockpos.distanceSq(positionVec.x, positionVec.y, positionVec.z) < distance * distance;
    }

    protected boolean getIsAboveDestination() {
        return this.isAboveDestinationBear;
    }

    private void pollinate() {
        if (destinationBlock == null) {
            return;
        }
        if (!crow.world.getGameRules().getBoolean("mobGriefing")) {
            timeoutCounter = 1200;
            return;
        }
        IBlockState state = crow.world.getBlockState(destinationBlock);
        if (state.getBlock() instanceof BlockCrops) {
            BlockCrops block = (BlockCrops) state.getBlock();
            int cropAge = block.getMetaFromState(state);
            if (cropAge > 0) {
                crow.world.setBlockState(destinationBlock, block.getStateFromMeta(cropAge - 1));
            } else {
                crow.world.setBlockState(destinationBlock, Blocks.AIR.getDefaultState(), 2);
            }
        } else {
            crow.world.setBlockState(destinationBlock, Blocks.AIR.getDefaultState(), 2);
        }
        timeoutCounter = 1200;
    }

    private boolean searchForDestination() {
        BlockPos origin = crow.getPosition();
        Random rand = crow.getRNG();
        for (int tries = 0; tries < 64; tries++) {
            BlockPos pos = origin.add(
                    rand.nextInt(64) - 32, rand.nextInt(16) - 8, rand.nextInt(64) - 32);
            if (shouldMoveTo(crow.world, pos)) {
                destinationBlock = pos;
                timeoutCounter = 0;
                return true;
            }
        }
        return false;
    }

    protected boolean shouldMoveTo(World worldIn, BlockPos pos) {
        return AMTagRegistry.blockInTag(AMTagRegistry.CROW_FOODBLOCKS, worldIn.getBlockState(pos).getBlock());
    }
}
