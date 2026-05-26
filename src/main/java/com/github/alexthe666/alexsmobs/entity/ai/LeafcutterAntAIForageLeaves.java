package com.github.alexthe666.alexsmobs.entity.ai;

import com.github.alexthe666.alexsmobs.config.AMConfig;
import com.github.alexthe666.alexsmobs.entity.EntityLeafcutterAnt;
import com.github.alexthe666.alexsmobs.misc.AMTagRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLog;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;

import javax.annotation.Nullable;
import java.util.Random;

/**
 * Forge 1.12.2 port of 1.16 {@code LeafcutterAntAIForageLeaves} / {@code MoveToBlockGoal}.
 */
public class LeafcutterAntAIForageLeaves extends EntityAIBase {

    private final EntityLeafcutterAnt ant;
    @Nullable
    private BlockPos destinationBlock = null;
    private int idleAtLeavesTime = 0;
    private int randomLeafCheckCooldown = 40;
    private BlockPos logStartPos = null;
    /** Matches 1.16 {@code MoveToBlockGoal} run delay. */
    private int timeoutCounter;

    public LeafcutterAntAIForageLeaves(EntityLeafcutterAnt ant) {
        this.ant = ant;
    }

    @Override
    public boolean shouldExecute() {
        if (ant.isChild() || ant.hasLeaf() || ant.isQueen()) {
            return false;
        }
        return findDestination();
    }

    private boolean findDestination() {
        BlockPos origin = new BlockPos(ant);
        Random rand = ant.getRNG();
        for (int tries = 0; tries < 24; tries++) {
            BlockPos p = origin.add(
                    6 - rand.nextInt(12) + rand.nextInt(3), -rand.nextInt(7), 6 - rand.nextInt(12) + rand.nextInt(3));
            if (shouldMoveTo(ant.world, p)) {
                destinationBlock = p;
                timeoutCounter = 0;
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean shouldContinueExecuting() {
        return !ant.hasLeaf() && destinationBlock != null;
    }

    @Override
    public void resetTask() {
        idleAtLeavesTime = 0;
        logStartPos = null;
        destinationBlock = null;
    }

    public double getTargetDistanceSq() {
        return 2.0D;
    }

    private boolean getIsAboveDestination() {
        if (destinationBlock == null) {
            return false;
        }
        return ant.getDistanceSq(
                destinationBlock.getX() + 0.5D,
                destinationBlock.getY(),
                destinationBlock.getZ() + 0.5D) < getTargetDistanceSq();
    }

    @Override
    public void updateTask() {
        if (destinationBlock == null) {
            return;
        }
        if (randomLeafCheckCooldown > 0) {
            randomLeafCheckCooldown--;
        } else {
            randomLeafCheckCooldown = 30 + ant.getRNG().nextInt(50);
            for (EnumFacing dir : EnumFacing.values()) {
                BlockPos offset = new BlockPos(ant).offset(dir);
                if (shouldMoveTo(ant.world, offset) && ant.getRNG().nextInt(1) == 0) {
                    destinationBlock = offset;
                    logStartPos = null;
                }
            }
        }

        if (ant.getAttachmentFacing() == EnumFacing.UP) {
            this.ant.getMoveHelper().setMoveTo(
                    destinationBlock.getX() + 0.5D,
                    destinationBlock.getY() - 1D,
                    destinationBlock.getZ() + 0.5D,
                    1.0D);
            this.ant.motionY += 0.5D;
            if (ant.getRNG().nextInt(2) == 0 && shouldMoveTo(ant.world, new BlockPos(ant).up())) {
                destinationBlock = new BlockPos(ant).up();
            }
        } else if (destinationBlock.getY() > ant.posY + 2F || logStartPos != null) {
            ant.getNavigator().clearPath();
            if (ant.getRNG().nextInt(5) == 0 && shouldMoveTo(ant.world, new BlockPos(ant).down())) {
                destinationBlock = new BlockPos(ant).down();
            }
            if (logStartPos != null) {
                double xDif = logStartPos.getX() + 0.5 - ant.posX;
                double zDif = logStartPos.getZ() + 0.5 - ant.posZ;
                float f = (float) (MathHelper.atan2(zDif, xDif) * (180D / Math.PI)) - 90.0F;
                ant.rotationYaw = f;
                ant.renderYawOffset = ant.rotationYaw;
                Vec3d vec = new Vec3d(logStartPos.getX() + 0.5, ant.posY, logStartPos.getZ() + 0.5);
                Vec3d ap = new Vec3d(ant.posX, ant.posY, ant.posZ);
                vec = vec.subtract(ap);
                Vec3d horiz = vec.normalize();
                if (ant.onGround || ant.isOnLadder()) {
                    ant.motionX += horiz.x * 0.1D;
                    ant.motionZ += horiz.z * 0.1D;
                }
                this.ant.getNavigator().tryMoveToXYZ(logStartPos.getX(), ant.posY, logStartPos.getZ(), 1.0D);
                if (Math.abs(xDif) < 0.6 && Math.abs(zDif) < 0.6) {
                    ant.motionX = 0;
                    ant.motionZ = 0;
                    this.ant.getMoveHelper().setMoveTo(logStartPos.getX() + 0.5D, ant.posY + 2, logStartPos.getZ() + 0.5D, 1.0D);
                    BlockPos test = new BlockPos(logStartPos.getX(), ant.posY, logStartPos.getZ());
                    IBlockState st = ant.world.getBlockState(test);
                    if (!(st.getBlock() instanceof BlockLog) && ant.getAttachmentFacing() == EnumFacing.DOWN) {
                        this.resetTask();
                        return;
                    }
                }
            } else {
                for (int i = 0; i < 15; i++) {
                    BlockPos test = destinationBlock.add(
                            6 - ant.getRNG().nextInt(12), -ant.getRNG().nextInt(7), 6 - ant.getRNG().nextInt(12));
                    if (ant.world.getBlockState(test).getBlock() instanceof BlockLog) {
                        logStartPos = test;
                        break;
                    }
                }
            }
            timeoutCounter++;
        } else {
            moveTowardsDestination();
            logStartPos = null;
        }
        if (this.getIsAboveDestination() || new BlockPos(ant).up().equals(destinationBlock)) {
            ant.getLookHelper().setLookPosition(
                    destinationBlock.getX() + 0.5D,
                    destinationBlock.getY(),
                    destinationBlock.getZ() + 0.5D,
                    30F,
                    40F);
            ant.setAnimation(EntityLeafcutterAnt.ANIMATION_BITE);
            if (this.idleAtLeavesTime >= 6) {
                ant.setLeafHarvestedPos(destinationBlock);
                ant.setLeafHarvestedState(ant.world.getBlockState(destinationBlock));
                if (!ant.hasLeaf()) {
                    this.breakLeaves();
                }
                ant.setLeaf(true);
                resetTask();
                this.idleAtLeavesTime = 0;
            } else {
                ++this.idleAtLeavesTime;
            }
        }
    }

    private void moveTowardsDestination() {
        this.ant.getNavigator().tryMoveToXYZ(
                destinationBlock.getX() + 0.5D,
                destinationBlock.getY(),
                destinationBlock.getZ() + 0.5D,
                1.0D);
        timeoutCounter++;
    }

    private void breakLeaves() {
        IBlockState blockstate = ant.world.getBlockState(this.destinationBlock);
        if (AMTagRegistry.blockInTag(AMTagRegistry.LEAFCUTTER_ANT_BREAKABLES, blockstate.getBlock())) {
            if (ForgeEventFactory.getMobGriefingEvent(ant.world, ant)) {
                ant.world.destroyBlock(destinationBlock, false);
                if (ant.getRNG().nextFloat() > AMConfig.leafcutterAntBreakLeavesChance) {
                    ant.world.setBlockState(destinationBlock, blockstate);
                }
            }
        }
    }

    protected boolean shouldMoveTo(World worldIn, BlockPos pos) {
        return AMTagRegistry.blockInTag(AMTagRegistry.LEAFCUTTER_ANT_BREAKABLES, worldIn.getBlockState(pos).getBlock());
    }
}
