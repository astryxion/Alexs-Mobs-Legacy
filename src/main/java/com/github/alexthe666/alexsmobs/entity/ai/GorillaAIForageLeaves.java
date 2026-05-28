package com.github.alexthe666.alexsmobs.entity.ai;

import com.github.alexthe666.alexsmobs.entity.EntityGorilla;
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
 * Forge 1.12.2 port of 1.16 {@code GorillaAIForageLeaves} / {@code MoveToBlockGoal}.
 */
public class GorillaAIForageLeaves extends EntityAIBase {

    private final EntityGorilla gorilla;
    private final double movementSpeed;
    @Nullable
    private BlockPos destinationBlock = null;
    private int idleAtLeavesTime = 0;
    private boolean isAboveDestinationBear;
    private int timeoutCounter;
    private static final int MAX_TIMEOUT = 600;

    public GorillaAIForageLeaves(EntityGorilla gorilla) {
        this.gorilla = gorilla;
        this.movementSpeed = 1.0D;
    }

    @Override
    public boolean shouldExecute() {
        if (gorilla.isChild() || !gorilla.getHeldItemMainhand().isEmpty()) {
            return false;
        }
        return findDestination();
    }

    private boolean findDestination() {
        BlockPos origin = new BlockPos(gorilla);
        Random rand = gorilla.getRNG();
        for (int tries = 0; tries < 32; tries++) {
            BlockPos p = origin.add(
                    8 - rand.nextInt(16), rand.nextInt(6) - 3, 8 - rand.nextInt(16));
            if (shouldMoveTo(gorilla.world, p)) {
                destinationBlock = p;
                timeoutCounter = 0;
                idleAtLeavesTime = 0;
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean shouldContinueExecuting() {
        return destinationBlock != null && timeoutCounter < MAX_TIMEOUT;
    }

    @Override
    public void resetTask() {
        idleAtLeavesTime = 0;
        destinationBlock = null;
        isAboveDestinationBear = false;
    }

    public double getTargetDistanceSq() {
        return 2.0D;
    }

    private boolean getIsAboveDestination() {
        return this.isAboveDestinationBear;
    }

    @Override
    public void updateTask() {
        if (destinationBlock == null) {
            return;
        }
        if (!isWithinXZDist(destinationBlock, gorilla.getPositionVector(), getTargetDistanceSq())) {
            this.isAboveDestinationBear = false;
            ++this.timeoutCounter;
            gorilla.getNavigator().tryMoveToXYZ(
                    (double) ((float) destinationBlock.getX()) + 0.5D,
                    destinationBlock.getY(),
                    (double) ((float) destinationBlock.getZ()) + 0.5D,
                    this.movementSpeed);
        } else {
            this.isAboveDestinationBear = true;
            --this.timeoutCounter;
        }

        if (this.getIsAboveDestination() && Math.abs(gorilla.posY - destinationBlock.getY()) <= 3) {
            gorilla.getLookHelper().setLookPosition(
                    destinationBlock.getX() + 0.5D,
                    destinationBlock.getY(),
                    destinationBlock.getZ() + 0.5D,
                    30.0F,
                    30.0F);
            if (gorilla.posY + 2 < destinationBlock.getY()) {
                gorilla.setAnimation(gorilla.getRNG().nextBoolean() ? EntityGorilla.ANIMATION_BREAKBLOCK_L : EntityGorilla.ANIMATION_BREAKBLOCK_R);
                gorilla.maxStandTime = 60;
                gorilla.setStanding(true);
            } else {
                if (gorilla.getAnimation() == IAnimatedEntity.NO_ANIMATION) {
                    gorilla.setAnimation(gorilla.getRNG().nextBoolean() ? EntityGorilla.ANIMATION_BREAKBLOCK_L : EntityGorilla.ANIMATION_BREAKBLOCK_R);
                }
            }
            if (this.idleAtLeavesTime >= 20) {
                if (!this.breakLeaves()) {
                    resetTask();
                }
            } else {
                ++this.idleAtLeavesTime;
            }
        }
    }

    private boolean isWithinXZDist(BlockPos blockpos, Vec3d positionVec, double distance) {
        return blockpos.distanceSq(positionVec.x, blockpos.getY(), positionVec.z) < distance * distance;
    }

    private boolean breakLeaves() {
        if (destinationBlock == null) {
            return false;
        }
        if (!ForgeEventFactory.getMobGriefingEvent(gorilla.world, gorilla)) {
            return false;
        }
        IBlockState blockstate = gorilla.world.getBlockState(this.destinationBlock);
        if (!AMTagRegistry.blockInTag(AMTagRegistry.GORILLA_BREAKABLES, blockstate.getBlock())) {
            return false;
        }
        gorilla.world.destroyBlock(destinationBlock, false);
        Random rand = gorilla.getRNG();
        ItemStack stack = new ItemStack(blockstate.getBlock());
        EntityItem itementity = new EntityItem(gorilla.world, destinationBlock.getX() + rand.nextFloat(), destinationBlock.getY() + rand.nextFloat(), destinationBlock.getZ() + rand.nextFloat(), stack);
        itementity.setDefaultPickupDelay();
        itementity.setPickupDelay(60);
        gorilla.world.spawnEntity(itementity);
        if (AMTagRegistry.BLOCKS_DROPPING_BANANAS.contains(blockstate.getBlock()) && rand.nextInt(30) == 0) {
            ItemStack banana = new ItemStack(AMItemRegistry.BANANA);
            EntityItem itementity2 = new EntityItem(gorilla.world, destinationBlock.getX() + rand.nextFloat(), destinationBlock.getY() + rand.nextFloat(), destinationBlock.getZ() + rand.nextFloat(), banana);
            itementity2.setDefaultPickupDelay();
            itementity2.setPickupDelay(60);
            gorilla.world.spawnEntity(itementity2);
        }
        resetTask();
        return true;
    }

    private boolean shouldMoveTo(World worldIn, BlockPos pos) {
        return AMTagRegistry.blockInTag(AMTagRegistry.GORILLA_BREAKABLES, worldIn.getBlockState(pos).getBlock());
    }
}
