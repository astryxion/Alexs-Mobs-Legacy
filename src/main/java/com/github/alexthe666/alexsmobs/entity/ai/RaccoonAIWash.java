package com.github.alexthe666.alexsmobs.entity.ai;

import com.github.alexthe666.alexsmobs.entity.EntityRaccoon;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.init.SoundEvents;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

/**
 * Forge 1.12.2 port of 1.16 {@code RaccoonAIWash}.
 * 1.16 stops at the shore via {@code WATER_BORDER}; 1.12 has no equivalent, so this
 * paths to land beside water and washes there. Touching water is not treated as a
 * failure — that cancelled the goal every tick and spammed A*.
 */
public class RaccoonAIWash extends EntityAIBase {

    private static final EnumFacing[] HORIZONTALS = new EnumFacing[]{EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.WEST, EnumFacing.EAST};
    private static final int MAX_SEEK_TICKS = 200;
    private static final double LAND_ARRIVE_DIST_SQ = 1.0D;

    private final EntityRaccoon raccoon;
    private BlockPos waterPos;
    private BlockPos targetPos;
    private int washTime = 0;
    private int pathRecalcTime = 0;
    private int seekTicks = 0;
    private int searchCooldown = 0;

    public RaccoonAIWash(EntityRaccoon creature) {
        this.setMutexBits(3);
        this.raccoon = creature;
    }

    @Override
    public boolean shouldExecute() {
        if (raccoon.getHeldItemMainhand().isEmpty() || raccoon.lookForWaterBeforeEatingTimer <= 0) {
            return false;
        }
        if (tryFindWashSite(2)) {
            return true;
        }
        if (searchCooldown > 0) {
            searchCooldown--;
            return false;
        }
        searchCooldown = 20;
        return tryFindWashSite(10);
    }

    @Override
    public void startExecuting() {
        this.raccoon.lookForWaterBeforeEatingTimer = 1800;
        this.pathRecalcTime = 0;
        this.seekTicks = 0;
        this.washTime = 0;
        this.updateWaterPathCost();
        if (!this.isAtWashSpot()) {
            this.tryPathToShore();
        }
    }

    @Override
    public void resetTask() {
        this.restoreWaterPathCost();
        targetPos = null;
        waterPos = null;
        washTime = 0;
        pathRecalcTime = 0;
        seekTicks = 0;
        this.raccoon.setWashPos(null);
        this.raccoon.setWashing(false);
        if (this.raccoon.lookForWaterBeforeEatingTimer > 0) {
            this.raccoon.lookForWaterBeforeEatingTimer = 100;
        }
        this.raccoon.getNavigator().clearPath();
    }

    @Override
    public void updateTask() {
        if (targetPos == null || waterPos == null) {
            return;
        }
        this.updateWaterPathCost();
        if (this.raccoon.isInWater() && !this.isAtWashSpot()) {
            this.raccoon.getJumpHelper().setJumping();
        }

        if (this.isAtWashSpot()) {
            double d0 = waterPos.getX() + 0.5D - this.raccoon.posX;
            double d2 = waterPos.getZ() + 0.5D - this.raccoon.posZ;
            float yaw = (float) (MathHelper.atan2(d2, d0) * (double) (180F / (float) Math.PI)) - 90.0F;
            this.raccoon.rotationYaw = yaw;
            this.raccoon.rotationYawHead = yaw;
            this.raccoon.renderYawOffset = yaw;
            this.raccoon.getNavigator().clearPath();
            this.raccoon.setWashing(true);
            this.raccoon.setWashPos(waterPos);
            this.raccoon.lookForWaterBeforeEatingTimer = 0;
            if (washTime % 10 == 0) {
                this.raccoon.playSound(SoundEvents.ENTITY_GENERIC_SWIM, 0.7F, 0.5F + raccoon.getRNG().nextFloat());
            }
            washTime++;
            if (washTime > 100 || raccoon.getHeldItemMainhand().getItem() == Items.SUGAR && washTime > 20) {
                ItemStack washedStack = raccoon.getHeldItemMainhand().copy();
                this.resetTask();
                if (washedStack.getItem() != Items.SUGAR) {
                    if (washedStack.getItem().hasContainerItem()) {
                        this.raccoon.entityDropItem(new ItemStack(washedStack.getItem().getContainerItem()), 0.0F);
                    }
                    raccoon.onEatItem(washedStack);
                    raccoon.getHeldItemMainhand().shrink(1);
                } else {
                    raccoon.getHeldItemMainhand().shrink(1);
                }
                this.raccoon.postWashItem(washedStack);
            }
        } else {
            if (this.raccoon.isWashing()) {
                this.raccoon.setWashing(false);
            }
            seekTicks++;
            if (pathRecalcTime <= 0) {
                this.tryPathToShore();
                pathRecalcTime = 15 + this.raccoon.getRNG().nextInt(10);
            } else {
                pathRecalcTime--;
            }
        }
    }

    @Override
    public boolean shouldContinueExecuting() {
        if (raccoon.getHeldItemMainhand().isEmpty()) {
            return false;
        }
        if (targetPos == null || waterPos == null || !EntityRaccoon.isRaccoonFood(this.raccoon.getHeldItemMainhand())) {
            return false;
        }
        if (this.raccoon.isWashing()) {
            return true;
        }
        if (seekTicks >= MAX_SEEK_TICKS) {
            this.raccoon.lookForWaterBeforeEatingTimer = 0;
            return false;
        }
        return true;
    }

    private boolean isAtWashSpot() {
        if (targetPos == null) {
            return false;
        }
        double dx = this.raccoon.posX - (targetPos.getX() + 0.5D);
        double dz = this.raccoon.posZ - (targetPos.getZ() + 0.5D);
        if (dx * dx + dz * dz > LAND_ARRIVE_DIST_SQ) {
            return false;
        }
        BlockPos below = new BlockPos(MathHelper.floor(this.raccoon.posX), MathHelper.floor(this.raccoon.posY - 0.2D), MathHelper.floor(this.raccoon.posZ));
        return isShoreStand(below) || isShoreStand(below.down());
    }

    private void tryPathToShore() {
        if (targetPos == null) {
            return;
        }
        double x = targetPos.getX() + 0.5D;
        double y = targetPos.getY() + 1.0D;
        double z = targetPos.getZ() + 0.5D;
        boolean pathed = this.raccoon.getNavigator().tryMoveToXYZ(x, y, z, 1.2D);
        if ((!pathed || this.raccoon.getNavigator().noPath()) && this.raccoon.isInWater()) {
            this.raccoon.getMoveHelper().setMoveTo(x, y, z, 1.2D);
        }
    }

    private void updateWaterPathCost() {
        // -1 = do not walk into lakes. While already swimming, allow a path back to land.
        this.raccoon.setPathPriority(PathNodeType.WATER, this.raccoon.isInWater() ? 8.0F : -1.0F);
    }

    private void restoreWaterPathCost() {
        this.raccoon.setPathPriority(PathNodeType.WATER, 8.0F);
    }

    private boolean tryFindWashSite(int range) {
        BlockPos origin = this.raccoon.getPosition();
        BlockPos bestWater = null;
        BlockPos bestLand = null;
        double bestDist = Double.MAX_VALUE;
        for (int y = -2; y <= 1; y++) {
            for (int x = -range; x <= range; x++) {
                for (int z = -range; z <= range; z++) {
                    BlockPos p = origin.add(x, y, z);
                    if (!isWater(p)) {
                        continue;
                    }
                    BlockPos land = getLandPos(p);
                    if (land == null) {
                        continue;
                    }
                    double d = this.raccoon.getDistanceSq(land.getX() + 0.5D, this.raccoon.posY, land.getZ() + 0.5D);
                    if (d < bestDist) {
                        bestDist = d;
                        bestWater = p;
                        bestLand = land;
                    }
                    if (d <= LAND_ARRIVE_DIST_SQ) {
                        waterPos = p;
                        targetPos = land;
                        return true;
                    }
                }
            }
        }
        if (bestLand != null) {
            waterPos = bestWater;
            targetPos = bestLand;
            return true;
        }
        return false;
    }

    public BlockPos getLandPos(BlockPos pos) {
        if (!isWater(pos)) {
            return null;
        }
        BlockPos best = null;
        double bestDist = Double.MAX_VALUE;
        for (EnumFacing dir : HORIZONTALS) {
            BlockPos stand = findStandPos(pos.offset(dir));
            if (stand == null) {
                continue;
            }
            double d = this.raccoon.getDistanceSq(stand.getX() + 0.5D, this.raccoon.posY, stand.getZ() + 0.5D);
            if (d < bestDist) {
                bestDist = d;
                best = stand;
            }
        }
        return best;
    }

    private BlockPos findStandPos(BlockPos pos) {
        for (int dy = 0; dy <= 2; dy++) {
            BlockPos p = pos.up(dy);
            if (isShoreStand(p)) {
                return p;
            }
        }
        if (isShoreStand(pos.down())) {
            return pos.down();
        }
        return null;
    }

    private boolean isWater(BlockPos pos) {
        return this.raccoon.world.getBlockState(pos).getMaterial() == Material.WATER;
    }

    private boolean isShoreStand(BlockPos pos) {
        IBlockState state = this.raccoon.world.getBlockState(pos);
        IBlockState above = this.raccoon.world.getBlockState(pos.up());
        if (state.getMaterial() == Material.WATER || above.getMaterial() == Material.WATER) {
            return false;
        }
        return state.getMaterial().blocksMovement() && !above.getMaterial().blocksMovement();
    }
}
