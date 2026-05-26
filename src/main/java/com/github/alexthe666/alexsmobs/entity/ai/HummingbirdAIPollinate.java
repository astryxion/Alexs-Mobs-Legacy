package com.github.alexthe666.alexsmobs.entity.ai;

import com.github.alexthe666.alexsmobs.entity.EntityHummingbird;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.BlockFlower;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Random;

/**
 * Forge 1.12.2 port of 1.16 {@code HummingbirdAIPollinate} / {@code MoveToBlockGoal}.
 */
public class HummingbirdAIPollinate extends EntityAIBase {

    private final EntityHummingbird bird;
    @Nullable
    private BlockPos destinationBlock;
    private int idleAtFlowerTime = 0;
    private boolean isAboveDestinationBear;
    private int timeoutCounter;
    private final double movementSpeed;

    public HummingbirdAIPollinate(EntityHummingbird bird) {
        this.bird = bird;
        this.movementSpeed = 1.0D;
        this.setMutexBits(1);
    }

    @Override
    public boolean shouldExecute() {
        return !bird.isChild() && bird.pollinateCooldown == 0 && searchForDestination();
    }

    @Override
    public void resetTask() {
        idleAtFlowerTime = 0;
        destinationBlock = null;
    }

    public double getTargetDistanceSq() {
        return 3.0D;
    }

    @Override
    public void updateTask() {
        if (destinationBlock == null) {
            return;
        }
        BlockPos blockpos = destinationBlock;
        if (!isWithinXZDist(blockpos, bird.getPositionVector(), this.getTargetDistanceSq())) {
            this.isAboveDestinationBear = false;
            ++this.timeoutCounter;
            double speedLoc = movementSpeed;
            if (bird.getDistanceSq(blockpos.getX() + 0.5D, blockpos.getY() + 0.5D, blockpos.getZ() + 0.5D) >= 3) {
                speedLoc = movementSpeed * 0.3D;
            }
            bird.getMoveHelper().setMoveTo(blockpos.getX() + 0.5D, blockpos.getY(), blockpos.getZ() + 0.5D, speedLoc);
        } else {
            this.isAboveDestinationBear = true;
            --this.timeoutCounter;
        }

        if (this.getIsAboveDestination() && Math.abs(bird.posY - destinationBlock.getY()) <= 2) {
            bird.getLookHelper().setLookPosition(destinationBlock.getX() + 0.5D, destinationBlock.getY(), destinationBlock.getZ() + 0.5D, 30.0F, bird.getVerticalFaceSpeed());
            if (this.idleAtFlowerTime >= 20) {
                this.pollinate();
                this.resetTask();
            } else {
                ++this.idleAtFlowerTime;
            }
        }
    }

    private boolean isGrowable(BlockPos pos, World world) {
        IBlockState blockstate = world.getBlockState(pos);
        Block block = blockstate.getBlock();
        return block instanceof BlockCrops && block.getMetaFromState(blockstate) < ((BlockCrops) block).getMaxAge();
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
        bird.world.playEvent(2005, destinationBlock, 0);
        bird.setCropsPollinated(bird.getCropsPollinated() + 1);
        bird.pollinateCooldown = 200;
        if (bird.getCropsPollinated() > 3) {
            if (isGrowable(destinationBlock, bird.world)) {
                ItemStack stack = new ItemStack(Items.DYE, 1, 15);
                net.minecraft.item.ItemDye.applyBonemeal(stack, bird.world, destinationBlock);
            }
            bird.setCropsPollinated(0);
        }
    }

    private boolean searchForDestination() {
        BlockPos origin = bird.getPosition();
        Random rand = bird.getRNG();
        for (int tries = 0; tries < 32; tries++) {
            BlockPos pos = origin.add(rand.nextInt(16) - 8, rand.nextInt(8) - 4, rand.nextInt(16) - 8);
            if (shouldMoveTo(bird.world, pos)) {
                destinationBlock = pos;
                timeoutCounter = 0;
                return true;
            }
        }
        return false;
    }

    protected boolean shouldMoveTo(World worldIn, BlockPos pos) {
        Block block = worldIn.getBlockState(pos).getBlock();
        if (block instanceof BlockCrops || block instanceof BlockFlower) {
            return bird.pollinateCooldown == 0 && bird.canBlockBeSeen(pos);
        }
        return false;
    }
}
