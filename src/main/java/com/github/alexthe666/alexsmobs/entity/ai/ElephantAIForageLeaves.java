package com.github.alexthe666.alexsmobs.entity.ai;

import com.github.alexthe666.alexsmobs.entity.EntityElephant;
import com.github.alexthe666.alexsmobs.item.AMItemRegistry;
import com.github.alexthe666.alexsmobs.misc.AMTagRegistry;
import com.github.alexthe666.citadel.animation.IAnimatedEntity;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;

import javax.annotation.Nullable;
import java.util.Random;

/**
 * Forge 1.12.2 port of 1.16 {@code ElephantAIForageLeaves} / {@code MoveToBlockGoal},
 * with 1.20.1 path-recalc cooldown so A* is not invoked every tick.
 */
public class ElephantAIForageLeaves extends EntityAIBase {

    private static final int MAX_TIMEOUT = 1200;
    private static final int MAX_FAILED_PATHS = 3;

    private final EntityElephant elephant;
    private final double movementSpeed;
    @Nullable
    private BlockPos destinationBlock = null;
    private int idleAtLeavesTime = 0;
    private boolean isAboveDestinationBear;
    private int timeoutCounter;
    private int runDelay;
    private int moveCooldown;
    private int failedPathAttempts;

    public ElephantAIForageLeaves(EntityElephant elephant) {
        this.elephant = elephant;
        this.movementSpeed = 0.7D;
        this.setMutexBits(1);
    }

    @Override
    public boolean shouldExecute() {
        if (elephant.isChild() || elephant.getControllingPassenger() != null || elephant.getControllingVillager() != null || !elephant.getHeldItemMainhand().isEmpty() || elephant.aiItemFlag) {
            return false;
        }
        if (this.runDelay > 0) {
            --this.runDelay;
            return false;
        }
        this.runDelay = 100 + elephant.getRNG().nextInt(200);
        return findDestination();
    }

    private boolean findDestination() {
        BlockPos origin = new BlockPos(elephant);
        Random rand = elephant.getRNG();
        for (int tries = 0; tries < 32; ++tries) {
            BlockPos p = origin.add(
                    rand.nextInt(16) - 8 + rand.nextInt(3), rand.nextInt(5) - 2, rand.nextInt(16) - 8 + rand.nextInt(3));
            if (shouldMoveTo(elephant.world, p)) {
                destinationBlock = p;
                timeoutCounter = 0;
                failedPathAttempts = 0;
                moveCooldown = 0;
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean shouldContinueExecuting() {
        return destinationBlock != null
                && timeoutCounter <= MAX_TIMEOUT
                && shouldMoveTo(elephant.world, destinationBlock);
    }

    @Override
    public void resetTask() {
        idleAtLeavesTime = 0;
        destinationBlock = null;
        isAboveDestinationBear = false;
        failedPathAttempts = 0;
        elephant.getNavigator().clearPath();
    }

    public double getTargetDistanceSq() {
        return 4D;
    }

    private boolean isWithinXZDist(BlockPos blockpos, Vec3d positionVec, double distance) {
        return blockpos.distanceSq(positionVec.x, blockpos.getY(), positionVec.z) < distance * distance;
    }

    protected boolean getIsAboveDestination() {
        return this.isAboveDestinationBear;
    }

    @Override
    public void updateTask() {
        if (destinationBlock == null) {
            return;
        }
        if (moveCooldown > 0) {
            --moveCooldown;
        }
        if (!isWithinXZDist(destinationBlock, new Vec3d(elephant.posX, elephant.posY, elephant.posZ), this.getTargetDistanceSq())) {
            this.isAboveDestinationBear = false;
            ++this.timeoutCounter;
            if (this.moveCooldown == 0) {
                this.moveCooldown = 30 + elephant.getRNG().nextInt(50);
                boolean foundPath = elephant.getNavigator().tryMoveToXYZ(
                        (double) ((float) destinationBlock.getX()) + 0.5D,
                        destinationBlock.getY(),
                        (double) ((float) destinationBlock.getZ()) + 0.5D,
                        this.movementSpeed);
                if (!foundPath) {
                    ++this.failedPathAttempts;
                    if (this.failedPathAttempts >= MAX_FAILED_PATHS) {
                        this.giveUp();
                        return;
                    }
                } else {
                    this.failedPathAttempts = 0;
                }
            }
        } else {
            this.isAboveDestinationBear = true;
            --this.timeoutCounter;
        }

        if (this.getIsAboveDestination() && Math.abs(elephant.posY - destinationBlock.getY()) <= 3) {
            elephant.getLookHelper().setLookPosition(
                    destinationBlock.getX() + 0.5D,
                    destinationBlock.getY(),
                    destinationBlock.getZ() + 0.5D,
                    30F,
                    40F);
            if (elephant.posY + 2 < destinationBlock.getY()) {
                if (elephant.getAnimation() == IAnimatedEntity.NO_ANIMATION) {
                    elephant.setAnimation(EntityElephant.ANIMATION_BREAKLEAVES);
                }
                elephant.setStanding(true);
                elephant.maxStandTime = 15;
            } else {
                elephant.setAnimation(EntityElephant.ANIMATION_BREAKLEAVES);
                elephant.setStanding(false);
            }
            if (this.idleAtLeavesTime >= 10) {
                this.breakLeaves();
            } else {
                ++this.idleAtLeavesTime;
            }
        }
    }

    private void giveUp() {
        this.runDelay = 100 + elephant.getRNG().nextInt(200);
        this.destinationBlock = null;
    }

    private void breakLeaves() {
        if (ForgeEventFactory.getMobGriefingEvent(elephant.world, elephant)) {
            IBlockState blockstate = elephant.world.getBlockState(this.destinationBlock);
            if (AMTagRegistry.blockInTag(AMTagRegistry.ELEPHANT_FOODBLOCKS, blockstate.getBlock())) {
                elephant.world.destroyBlock(destinationBlock, false);
                Random rand = new Random();
                ItemStack stack = new ItemStack(blockstate.getBlock());
                EntityItem itementity = new EntityItem(elephant.world, destinationBlock.getX() + rand.nextFloat(), destinationBlock.getY() + rand.nextFloat(), destinationBlock.getZ() + rand.nextFloat(), stack);
                itementity.setDefaultPickupDelay();
                elephant.world.spawnEntity(itementity);
                if (AMTagRegistry.BLOCKS_DROPPING_ACACIA_BLOSSOMS.contains(blockstate.getBlock()) && rand.nextInt(30) == 0) {
                    ItemStack banana = new ItemStack(AMItemRegistry.ACACIA_BLOSSOM);
                    EntityItem itementity2 = new EntityItem(elephant.world, destinationBlock.getX() + rand.nextFloat(), destinationBlock.getY() + rand.nextFloat(), destinationBlock.getZ() + rand.nextFloat(), banana);
                    itementity2.setDefaultPickupDelay();
                    elephant.world.spawnEntity(itementity2);
                }
                resetTask();
            }
        }
    }

    protected boolean shouldMoveTo(World worldIn, BlockPos pos) {
        return !elephant.aiItemFlag && AMTagRegistry.blockInTag(AMTagRegistry.ELEPHANT_FOODBLOCKS, worldIn.getBlockState(pos).getBlock());
    }
}
