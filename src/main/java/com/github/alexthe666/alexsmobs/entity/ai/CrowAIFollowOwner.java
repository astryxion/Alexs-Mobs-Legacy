package com.github.alexthe666.alexsmobs.entity.ai;

import com.github.alexthe666.alexsmobs.AlexsMobs;
import com.github.alexthe666.alexsmobs.entity.EntityCrow;
import com.github.alexthe666.alexsmobs.message.MessageCrowMountPlayer;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityMoveHelper;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.pathfinding.WalkNodeProcessor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;

public class CrowAIFollowOwner extends EntityAIBase {
    private final EntityCrow crow;
    private final IBlockAccess world;
    private final double followSpeed;
    private final PathNavigate navigator;
    private final float maxDist;
    private final float minDist;
    private final boolean teleportToLeaves;
    float circlingTime = 0;
    float circleDistance = 1;
    float yLevel = 2;
    boolean clockwise = false;
    private EntityLivingBase owner;
    private int timeToRecalcPath;
    private float oldWaterCost;
    private int maxCircleTime;

    public CrowAIFollowOwner(EntityCrow crow, double followSpeed, float minDist, float maxDist, boolean teleportToLeaves) {
        this.crow = crow;
        this.world = crow.world;
        this.followSpeed = followSpeed;
        this.navigator = crow.getNavigator();
        this.minDist = minDist;
        this.maxDist = maxDist;
        this.teleportToLeaves = teleportToLeaves;
        this.setMutexBits(3);
    }

    @Override
    public boolean shouldExecute() {
        EntityLivingBase owner = this.crow.getOwner();
        if (owner == null) {
            return false;
        } else if (owner instanceof EntityPlayer && ((EntityPlayer) owner).isSpectator()) {
            return false;
        } else if (this.crow.isSitting() || crow.isBeingRidden()) {
            return false;
        } else if (crow.getCommand() != 1) {
            return false;
        } else if (this.crow.getDistanceSq(owner) < (double) (this.minDist * this.minDist)) {
            return false;
        } else {
            this.owner = owner;
            return crow.getAttackTarget() == null || !crow.getAttackTarget().isEntityAlive();
        }
    }

    @Override
    public boolean shouldContinueExecuting() {
        if (this.crow.isSitting()) {
            return false;
        }
        if (crow.getCommand() != 1 || crow.isBeingRidden()) {
            return false;
        }
        if (crow.getAttackTarget() != null && crow.getAttackTarget().isEntityAlive()) {
            return false;
        }
        EntityLivingBase owner = this.owner;
        if (owner == null || !owner.isEntityAlive()) {
            return false;
        }
        return crow.getDistanceSq(owner) >= (double) (this.minDist * this.minDist);
    }

    @Override
    public void startExecuting() {
        this.timeToRecalcPath = 0;
        this.oldWaterCost = this.crow.getPathPriority(PathNodeType.WATER);
        this.crow.setPathPriority(PathNodeType.WATER, 0.0F);
        clockwise = crow.getRNG().nextBoolean();
        yLevel = crow.getRNG().nextInt(1);
        circlingTime = 0;
        maxCircleTime = 20 + crow.getRNG().nextInt(100);
        circleDistance = 1F + crow.getRNG().nextFloat() * 2F;
    }

    @Override
    public void resetTask() {
        this.owner = null;
        this.navigator.clearPath();
        circlingTime = 0;
        this.crow.setFlying(false);
        this.crow.getMoveHelper().action = EntityMoveHelper.Action.WAIT;
        this.crow.setPathPriority(PathNodeType.WATER, this.oldWaterCost);
    }

    @Override
    public void updateTask() {
        this.crow.getLookHelper().setLookPositionWithEntity(this.owner, 10.0F, (float) this.crow.getVerticalFaceSpeed());
        if (!this.crow.getLeashed() && !this.crow.isBeingRidden()) {
            double dist = this.crow.getDistanceSq(this.owner);
            if (--this.timeToRecalcPath <= 0) {
                this.timeToRecalcPath = 10;
                if (dist >= 144.0D && !crow.aiItemFlag) {
                    crow.setFlying(true);
                    crow.getMoveHelper().setMoveTo(owner.posX, owner.posY + owner.getEyeHeight() + 0.2F, owner.posZ, 1F);
                    this.tryToTeleportNearEntity();
                    circlingTime = 0;
                }
            }

            if (!crow.aiItemFlag) {
                if (dist < (double) (this.minDist * this.minDist)) {
                    if (circlingTime > maxCircleTime && crow.getRidingCrows(owner) < 2 && crow.getDistance(owner) < 2.5D) {
                        if (!crow.world.isRemote) {
                            crow.startRiding(owner, true);
                            crow.rideCooldown = 40;
                            AlexsMobs.sendMSGToAll(new MessageCrowMountPlayer(crow.getEntityId(), owner.getEntityId()));
                        }
                    } else {
                        crow.setFlying(false);
                        crow.getMoveHelper().action = EntityMoveHelper.Action.WAIT;
                    }
                    return;
                }
                if (this.crow.isFlying()) {
                    circlingTime++;
                }
                if (circlingTime > maxCircleTime && crow.getRidingCrows(owner) < 2) {
                    crow.setFlying(true);
                    crow.getMoveHelper().setMoveTo(owner.posX, owner.posY + owner.getEyeHeight() + 0.2F, owner.posZ, 0.7F);
                    if (crow.getDistance(owner) < 2.5D && !crow.world.isRemote) {
                        crow.startRiding(owner, true);
                        crow.rideCooldown = 40;
                        AlexsMobs.sendMSGToAll(new MessageCrowMountPlayer(crow.getEntityId(), owner.getEntityId()));
                    }
                } else {
                    Vec3d circlePos = getVultureCirclePos(owner.getPositionVector());
                    if (circlePos == null) {
                        crow.setFlying(false);
                        crow.getNavigator().tryMoveToEntityLiving(owner, this.followSpeed);
                    } else {
                        crow.setFlying(true);
                        crow.getMoveHelper().setMoveTo(circlePos.x, circlePos.y + owner.getEyeHeight() + 0.2F, circlePos.z, 0.7F);
                    }
                }
            }
        }
    }

    public Vec3d getVultureCirclePos(Vec3d target) {
        float angle = (0.01745329251F * 8 * (clockwise ? -circlingTime : circlingTime));
        double extraX = circleDistance * MathHelper.sin(angle);
        double extraZ = circleDistance * MathHelper.cos(angle);
        Vec3d pos = new Vec3d(target.x + extraX, target.y + yLevel, target.z + extraZ);
        if (crow.world.isAirBlock(new BlockPos(pos))) {
            return pos;
        }
        return null;
    }

    private void tryToTeleportNearEntity() {
        BlockPos origin = this.owner.getPosition();

        for (int i = 0; i < 10; ++i) {
            int x = this.getRandomNumber(-3, 3);
            int y = this.getRandomNumber(-1, 1);
            int z = this.getRandomNumber(-3, 3);
            if (this.tryToTeleportToLocation(origin.getX() + x, origin.getY() + y, origin.getZ() + z)) {
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
            this.crow.setPosition(x + 0.5D, y, z + 0.5D);
            this.navigator.clearPath();
            return true;
        }
    }

    private boolean isTeleportFriendlyBlock(BlockPos pos) {
        PathNodeType nodeType = new WalkNodeProcessor().getPathNodeType(this.world, pos.getX(), pos.getY(), pos.getZ());
        if (nodeType != PathNodeType.WALKABLE) {
            return false;
        } else {
            IBlockState below = this.world.getBlockState(pos.down());
            if (!this.teleportToLeaves && below.getBlock() instanceof BlockLeaves) {
                return false;
            } else {
                BlockPos offset = pos.subtract(this.crow.getPosition());
                return this.crow.world.getCollisionBoxes(this.crow, this.crow.getEntityBoundingBox().offset(offset.getX(), offset.getY(), offset.getZ())).isEmpty();
            }
        }
    }

    private int getRandomNumber(int min, int max) {
        return this.crow.getRNG().nextInt(max - min + 1) + min;
    }
}
