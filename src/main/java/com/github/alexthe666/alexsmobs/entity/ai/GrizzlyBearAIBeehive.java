package com.github.alexthe666.alexsmobs.entity.ai;

import com.github.alexthe666.alexsmobs.entity.EntityGrizzlyBear;
import com.github.alexthe666.alexsmobs.entity.EntityLeafcutterAnt;
import com.github.alexthe666.alexsmobs.misc.AMTagRegistry;
import com.github.alexthe666.alexsmobs.tileentity.TileEntityLeafcutterAnthill;
import com.github.alexthe666.citadel.animation.IAnimatedEntity;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;

import javax.annotation.Nullable;
import java.util.Random;

/**
 * Forge 1.12.2 port of 1.16 {@code GrizzlyBearAIBeehive} / {@code MoveToBlockGoal}.
 * Targets leafcutter anthills (1.12 has no vanilla beehives).
 */
public class GrizzlyBearAIBeehive extends EntityAIBase {

    private final EntityGrizzlyBear bear;
    private final double movementSpeed;
    @Nullable
    private BlockPos destinationBlock = null;
    private int idleAtHiveTime = 0;
    private boolean isAboveDestinationBear;
    private int timeoutCounter;

    public GrizzlyBearAIBeehive(EntityGrizzlyBear bear) {
        this.bear = bear;
        this.movementSpeed = 1.0D;
    }

    @Override
    public boolean shouldExecute() {
        if (bear.isChild()) {
            return false;
        }
        return findDestination();
    }

    private boolean findDestination() {
        BlockPos origin = new BlockPos(bear);
        Random rand = bear.getRNG();
        for (int tries = 0; tries < 32; tries++) {
            BlockPos p = origin.add(
                    8 - rand.nextInt(16), rand.nextInt(8) - 4, 8 - rand.nextInt(16));
            if (shouldMoveTo(bear.world, p)) {
                destinationBlock = p;
                timeoutCounter = 0;
                idleAtHiveTime = 0;
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean shouldContinueExecuting() {
        return destinationBlock != null;
    }

    @Override
    public void resetTask() {
        idleAtHiveTime = 0;
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
        if (!isWithinXZDist(destinationBlock, bear.getPositionVector(), getTargetDistanceSq())) {
            this.isAboveDestinationBear = false;
            ++this.timeoutCounter;
            if (bear.shouldMove()) {
                bear.getNavigator().tryMoveToXYZ(
                        (double) ((float) destinationBlock.getX()) + 0.5D,
                        destinationBlock.getY(),
                        (double) ((float) destinationBlock.getZ()) + 0.5D,
                        this.movementSpeed);
            }
        } else {
            this.isAboveDestinationBear = true;
            --this.timeoutCounter;
        }

        if (this.getIsAboveDestination() && Math.abs(bear.posY - destinationBlock.getY()) <= 3) {
            bear.getLookHelper().setLookPosition(
                    destinationBlock.getX() + 0.5D,
                    destinationBlock.getY(),
                    destinationBlock.getZ() + 0.5D,
                    30.0F,
                    30.0F);
            if (bear.posY + 2 < destinationBlock.getY()) {
                bear.setAnimation(EntityGrizzlyBear.ANIMATION_MAUL);
                bear.maxStandTime = 60;
                bear.setStanding(true);
            } else {
                if (bear.getAnimation() == IAnimatedEntity.NO_ANIMATION) {
                    bear.setAnimation(bear.getRNG().nextBoolean() ? EntityGrizzlyBear.ANIMATION_SWIPE_L : EntityGrizzlyBear.ANIMATION_SWIPE_R);
                }
            }
            if (this.idleAtHiveTime >= 20) {
                this.eatHive();
            } else {
                ++this.idleAtHiveTime;
            }
        }
    }

    private boolean isWithinXZDist(BlockPos blockpos, Vec3d positionVec, double distance) {
        return blockpos.distanceSq(positionVec.x, positionVec.y, positionVec.z) < distance * distance;
    }

    private void eatHive() {
        if (destinationBlock == null) {
            return;
        }
        if (ForgeEventFactory.getMobGriefingEvent(bear.world, bear)) {
            IBlockState blockstate = bear.world.getBlockState(this.destinationBlock);
            if (AMTagRegistry.blockInTag(AMTagRegistry.GRIZZLY_BEEHIVE, blockstate.getBlock())) {
                TileEntity te = bear.world.getTileEntity(this.destinationBlock);
                if (te instanceof TileEntityLeafcutterAnthill) {
                    TileEntityLeafcutterAnthill anthill = (TileEntityLeafcutterAnthill) te;
                    int level = anthill.getAntCount();
                    Random rand = bear.getRNG();
                    anthill.angerAnts(bear, blockstate, TileEntityLeafcutterAnthill.AnthillReleaseState.EMERGENCY);
                    ItemStack stack = new ItemStack(Items.SUGAR);
                    for (int i = 0; i < level; i++) {
                        EntityItem entityitem = new EntityItem(
                                bear.world,
                                destinationBlock.getX() + rand.nextFloat(),
                                destinationBlock.getY() + rand.nextFloat(),
                                destinationBlock.getZ() + rand.nextFloat(),
                                stack.copy());
                        entityitem.setDefaultPickupDelay();
                        bear.world.spawnEntity(entityitem);
                    }
                    double d0 = 15;
                    for (EntityLeafcutterAnt ant : bear.world.getEntitiesWithinAABB(
                            EntityLeafcutterAnt.class,
                            bear.getEntityBoundingBox().grow(d0, d0, d0))) {
                        if (ant.getDistanceSq(destinationBlock) < d0 * d0) {
                            ant.setAngerTime(100);
                            ant.setAttackTarget(bear);
                            ant.setStayOutOfHiveCountdown(400);
                        }
                    }
                    resetTask();
                }
            }
        }
    }

    private boolean shouldMoveTo(World worldIn, BlockPos pos) {
        IBlockState state = worldIn.getBlockState(pos);
        if (AMTagRegistry.blockInTag(AMTagRegistry.GRIZZLY_BEEHIVE, state.getBlock())) {
            TileEntity te = worldIn.getTileEntity(pos);
            if (te instanceof TileEntityLeafcutterAnthill) {
                return ((TileEntityLeafcutterAnthill) te).getAntCount() > 0;
            }
        }
        return false;
    }
}
