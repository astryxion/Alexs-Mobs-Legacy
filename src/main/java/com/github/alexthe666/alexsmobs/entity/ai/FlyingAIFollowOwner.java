package com.github.alexthe666.alexsmobs.entity.ai;

import com.github.alexthe666.alexsmobs.entity.IFollower;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.init.Blocks;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.pathfinding.WalkNodeProcessor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * 1.12 counterpart to 1.16 {@code FlyingAIFollowOwner} / vanilla wolf follow owner flight behavior via {@link IFollower}.
 */
public class FlyingAIFollowOwner extends EntityAIBase {
    private final EntityTameable tameable;
    private final World world;
    private final double followSpeed;
    private final PathNavigate navigator;
    private int timeToRecalcPath;
    private final float maxDist;
    private final float minDist;
    private float oldWaterCost;
    private final boolean teleportToLeaves;
    private final IFollower follower;
    private EntityLivingBase owner;

    public FlyingAIFollowOwner(EntityTameable tameable, double speed, float minDist, float maxDist, boolean teleportToLeaves) {
        this.tameable = tameable;
        this.world = tameable.world;
        this.followSpeed = speed;
        this.navigator = tameable.getNavigator();
        this.minDist = minDist;
        this.maxDist = maxDist;
        this.teleportToLeaves = teleportToLeaves;
        this.follower = (IFollower) tameable;
        this.setMutexBits(3);
    }

    @Override
    public boolean shouldExecute() {
        EntityLivingBase o = this.tameable.getOwner();
        if (o == null) {
            return false;
        } else if (o instanceof net.minecraft.entity.player.EntityPlayer && ((net.minecraft.entity.player.EntityPlayer) o).isSpectator()) {
            return false;
        } else if (this.tameable.isSitting()) {
            return false;
        } else if (this.tameable.getDistanceSq(o) < (double) (this.minDist * this.minDist)) {
            return false;
        } else {
            this.owner = o;
            return follower.shouldFollow();
        }
    }

    @Override
    public boolean shouldContinueExecuting() {
        if (this.tameable.isSitting() || this.owner == null || !follower.shouldFollow()) {
            return false;
        }
        return this.tameable.getDistanceSq(this.owner) > (double) (this.maxDist * this.maxDist);
    }

    @Override
    public void startExecuting() {
        this.timeToRecalcPath = 0;
        this.oldWaterCost = this.tameable.getPathPriority(PathNodeType.WATER);
        this.tameable.setPathPriority(PathNodeType.WATER, 0.0F);
    }

    @Override
    public void resetTask() {
        this.owner = null;
        this.navigator.clearPath();
        this.tameable.setPathPriority(PathNodeType.WATER, this.oldWaterCost);
    }

    @Override
    public void updateTask() {
        this.tameable.getLookHelper().setLookPositionWithEntity(this.owner, 10.0F, (float) this.tameable.getVerticalFaceSpeed());
        if (--this.timeToRecalcPath <= 0) {
            this.timeToRecalcPath = 10;
            if (!this.tameable.getLeashed() && !this.tameable.isRiding()) {
                if (this.tameable.getDistanceSq(this.owner) >= 144.0D) {
                    this.tryToTeleportNearEntity();
                }
                follower.followEntity(tameable, owner, followSpeed);
            }
        }
    }

    private void tryToTeleportNearEntity() {
        BlockPos blockpos = this.owner.getPosition();

        for (int i = 0; i < 10; ++i) {
            int j = this.getRandomNumber(-3, 3);
            int k = this.getRandomNumber(-1, 1);
            int l = this.getRandomNumber(-3, 3);
            if (this.tryToTeleportToLocation(blockpos.getX() + j, blockpos.getY() + k, blockpos.getZ() + l)) {
                return;
            }
        }
    }

    private boolean tryToTeleportToLocation(int x, int y, int z) {
        if (Math.abs((double) x - this.owner.posX) < 2.0D && Math.abs((double) z - this.owner.posZ) < 2.0D) {
            return false;
        } else if (!this.isTeleportFriendlyBlock(new BlockPos(x, y, z))) {
            return false;
        } else {
            this.tameable.setLocationAndAngles((double) x + 0.5D, (double) y, (double) z + 0.5D, this.tameable.rotationYaw, this.tameable.rotationPitch);
            this.navigator.clearPath();
            return true;
        }
    }

    private boolean isTeleportFriendlyBlock(BlockPos pos) {
        PathNodeType pathnodetype = new WalkNodeProcessor().getPathNodeType(this.world, pos.getX(), pos.getY(), pos.getZ());
        if (pathnodetype != PathNodeType.WALKABLE) {
            return false;
        }
        IBlockState blockstate = this.world.getBlockState(pos.down());
        if (!this.teleportToLeaves && blockstate.getBlock() == Blocks.LEAVES) {
            return false;
        }
        BlockPos blockpos = pos.subtract(this.tameable.getPosition());
        return !this.world.collidesWithAnyBlock(this.tameable.getEntityBoundingBox().offset(blockpos));
    }

    private int getRandomNumber(int min, int max) {
        return this.tameable.getRNG().nextInt(max - min + 1) + min;
    }
}
