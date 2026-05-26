package com.github.alexthe666.alexsmobs.entity.ai;

import com.github.alexthe666.alexsmobs.entity.EntityMantisShrimp;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MantisShrimpAIBreakBlocks extends EntityAIBase {

    private final EntityMantisShrimp mantisShrimp;
    private int idleAtFlowerTime = 0;
    private int timeoutCounter = 0;
    private int searchCooldown = 0;
    private boolean isAboveDestinationBear;
    private BlockPos destinationBlock;
    private final BlockSorter targetSorter;

    public MantisShrimpAIBreakBlocks(EntityMantisShrimp mantisShrimp) {
        this.mantisShrimp = mantisShrimp;
        this.targetSorter = new BlockSorter(mantisShrimp);
        this.setMutexBits(3);
    }

    @Override
    public boolean shouldExecute() {
        if (!mantisShrimp.isChild() && (mantisShrimp.getAttackTarget() == null || !mantisShrimp.getAttackTarget().isEntityAlive())
                && mantisShrimp.getCommand() == 3 && !mantisShrimp.getHeldItemMainhand().isEmpty()) {
            if (searchCooldown <= 0) {
                resetTarget();
                searchCooldown = 100 + mantisShrimp.getRNG().nextInt(200);
                return destinationBlock != null;
            } else {
                searchCooldown--;
            }
        }
        return false;
    }

    @Override
    public boolean shouldContinueExecuting() {
        return destinationBlock != null && timeoutCounter < 1200
                && (mantisShrimp.getAttackTarget() == null || !mantisShrimp.getAttackTarget().isEntityAlive())
                && mantisShrimp.getCommand() == 3 && !mantisShrimp.getHeldItemMainhand().isEmpty();
    }

    @Override
    public void resetTask() {
        searchCooldown = 50;
        timeoutCounter = 0;
        destinationBlock = null;
    }

    public double getTargetDistanceSq() {
        return 2.3D;
    }

    @Override
    public void updateTask() {
        BlockPos blockpos = destinationBlock;
        float yDist = (float) Math.abs(blockpos.getY() - mantisShrimp.posY - mantisShrimp.height / 2);
        this.mantisShrimp.getNavigator().tryMoveToXYZ(blockpos.getX() + 0.5D, blockpos.getY() + 0.5D, blockpos.getZ() + 0.5D, 1.0D);
        if (!isWithinXZDist(blockpos, mantisShrimp.getPositionVector(), this.getTargetDistanceSq()) || yDist > 2F) {
            this.isAboveDestinationBear = false;
            ++this.timeoutCounter;
        } else {
            this.isAboveDestinationBear = true;
            --this.timeoutCounter;
        }
        if (timeoutCounter > 2400) {
            resetTask();
        }
        if (this.getIsAboveDestination()) {
            mantisShrimp.getLookHelper().setLookPosition(destinationBlock.getX() + 0.5D, destinationBlock.getY(), destinationBlock.getZ() + 0.5D, 30.0F, mantisShrimp.getVerticalFaceSpeed());
            if (this.idleAtFlowerTime >= 2) {
                idleAtFlowerTime = 0;
                this.breakBlock();
                this.resetTask();
            } else {
                mantisShrimp.punch();
                ++this.idleAtFlowerTime;
            }
        }
    }

    private void resetTarget() {
        List<BlockPos> allBlocks = new ArrayList<>();
        int radius = 16;
        BlockPos origin = mantisShrimp.getPosition();
        for (BlockPos pos : BlockPos.getAllInBox(origin.add(-radius, -radius, -radius), origin.add(radius, radius, radius))) {
            if (!mantisShrimp.world.isAirBlock(pos) && shouldMoveTo(mantisShrimp.world, pos)) {
                if (!mantisShrimp.isInWater() || isBlockTouchingWater(pos)) {
                    allBlocks.add(pos.toImmutable());
                }
            }
        }
        if (!allBlocks.isEmpty()) {
            allBlocks.sort(this.targetSorter);
            for (BlockPos pos : allBlocks) {
                if (canSeeBlock(pos)) {
                    this.destinationBlock = pos;
                    return;
                }
            }
        }
        destinationBlock = null;
    }

    private boolean isBlockTouchingWater(BlockPos pos) {
        for (EnumFacing dir : EnumFacing.values()) {
            if (mantisShrimp.world.getBlockState(pos.offset(dir)).getMaterial() == Material.WATER) {
                return true;
            }
        }
        return false;
    }

    private boolean isWithinXZDist(BlockPos blockpos, Vec3d positionVec, double distance) {
        return blockpos.distanceSq(positionVec.x, positionVec.y, positionVec.z) < distance * distance;
    }

    protected boolean getIsAboveDestination() {
        return this.isAboveDestinationBear;
    }

    private void breakBlock() {
        if (shouldMoveTo(mantisShrimp.world, destinationBlock)) {
            IBlockState state = mantisShrimp.world.getBlockState(destinationBlock);
            if (!mantisShrimp.world.isAirBlock(destinationBlock)
                    && state.getBlock().canEntityDestroy(state, mantisShrimp.world, destinationBlock, mantisShrimp)
                    && net.minecraftforge.event.ForgeEventFactory.onEntityDestroyBlock(mantisShrimp, destinationBlock, state)
                    && state.getBlockHardness(mantisShrimp.world, destinationBlock) >= 0) {
                mantisShrimp.world.destroyBlock(destinationBlock, true);
            }
        }
    }

    private boolean canSeeBlock(BlockPos target) {
        Vec3d start = new Vec3d(mantisShrimp.posX, mantisShrimp.getPositionEyes(1.0F).y, mantisShrimp.posZ);
        Vec3d end = new Vec3d(target.getX() + 0.5D, target.getY() + 0.5D, target.getZ() + 0.5D);
        RayTraceResult result = mantisShrimp.world.rayTraceBlocks(start, end, false, true, false);
        return result != null && result.typeOfHit == RayTraceResult.Type.BLOCK && result.getBlockPos().equals(target);
    }

    protected boolean shouldMoveTo(World worldIn, BlockPos pos) {
        return mantisShrimp.getHeldItemMainhand().getItem() == net.minecraft.item.Item.getItemFromBlock(worldIn.getBlockState(pos).getBlock());
    }

    public class BlockSorter implements Comparator<BlockPos> {
        private final Entity entity;

        public BlockSorter(Entity entity) {
            this.entity = entity;
        }

        @Override
        public int compare(BlockPos pos1, BlockPos pos2) {
            return Double.compare(getDistance(pos1), getDistance(pos2));
        }

        private double getDistance(BlockPos pos) {
            double deltaX = this.entity.posX - (pos.getX() + 0.5);
            double deltaY = this.entity.posY + this.entity.getEyeHeight() - (pos.getY() + 0.5);
            double deltaZ = this.entity.posZ - (pos.getZ() + 0.5);
            return deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ;
        }
    }
}
