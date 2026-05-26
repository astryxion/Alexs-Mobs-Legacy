package com.github.alexthe666.alexsmobs.entity.ai;

import com.github.alexthe666.alexsmobs.entity.EntityShoebill;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

/**
 * Forge 1.12.2 port of 1.16 {@code ShoebillAIFlightFlee}.
 */
public class ShoebillAIFlightFlee extends EntityAIBase {

    private final EntityShoebill bird;
    private BlockPos currentTarget = null;
    private int executionTime = 0;

    public ShoebillAIFlightFlee(EntityShoebill bird) {
        this.setMutexBits(1);
        this.bird = bird;
    }

    @Override
    public void resetTask() {
        currentTarget = null;
        executionTime = 0;
        bird.setFlying(false);
    }

    @Override
    public boolean shouldContinueExecuting() {
        return bird.isFlying() && (executionTime < 15 || !bird.onGround);
    }

    @Override
    public boolean shouldExecute() {
        return bird.revengeCooldown > 0 && bird.onGround;
    }

    @Override
    public void startExecuting() {
        if (bird.onGround) {
            bird.setFlying(true);
        }
    }

    @Override
    public void updateTask() {
        executionTime++;
        if (currentTarget == null) {
            if (bird.revengeCooldown == 0) {
                currentTarget = getBlockGrounding(new Vec3d(bird.posX, bird.posY, bird.posZ));
            } else {
                currentTarget = getBlockInViewAway(new Vec3d(bird.posX, bird.posY, bird.posZ));
            }
        }
        if (currentTarget != null) {
            bird.getNavigator().tryMoveToXYZ(currentTarget.getX() + 0.5F, currentTarget.getY() + 0.5F, currentTarget.getZ() + 0.5F, 1F);
            if (this.bird.getDistanceSq(currentTarget.getX() + 0.5D, currentTarget.getY() + 0.5D, currentTarget.getZ() + 0.5D) < 4) {
                currentTarget = null;
            }
        }
        if (bird.revengeCooldown == 0 && (bird.isInWater() || !bird.world.isAirBlock(bird.getPosition().down()))) {
            resetTask();
            bird.setFlying(false);
        }
    }

    public BlockPos getBlockInViewAway(Vec3d fleePos) {
        float radius = 0.75F * (0.7F * 6) * -3 - bird.getRNG().nextInt(24);
        float neg = bird.getRNG().nextBoolean() ? 1 : -1;
        float renderYawOffset = bird.renderYawOffset;
        float angle = (0.01745329251F * renderYawOffset) + 3.15F + (bird.getRNG().nextFloat() * neg);
        double extraX = radius * MathHelper.sin((float) (Math.PI + angle));
        double extraZ = radius * MathHelper.cos(angle);
        BlockPos radialPos = new BlockPos(fleePos.x + extraX, 0, fleePos.z + extraZ);
        BlockPos ground = bird.world.getTopSolidOrLiquidBlock(radialPos);
        int distFromGround = (int) bird.posY - ground.getY();
        int flightHeight = 4 + bird.getRNG().nextInt(10);
        BlockPos newPos = radialPos.up(distFromGround > 8 ? flightHeight : (int) bird.posY + bird.getRNG().nextInt(6) + 1);
        if (!bird.isTargetBlocked(new Vec3d(newPos.getX() + 0.5D, newPos.getY() + 0.5D, newPos.getZ() + 0.5D))
                && bird.getDistanceSq(newPos.getX() + 0.5D, newPos.getY() + 0.5D, newPos.getZ() + 0.5D) > 6) {
            return newPos;
        }
        return null;
    }

    public BlockPos getBlockGrounding(Vec3d fleePos) {
        float radius = 0.75F * (0.7F * 6) * -3 - bird.getRNG().nextInt(24);
        float neg = bird.getRNG().nextBoolean() ? 1 : -1;
        float renderYawOffset = bird.renderYawOffset;
        float angle = (0.01745329251F * renderYawOffset) + 3.15F + (bird.getRNG().nextFloat() * neg);
        double extraX = radius * MathHelper.sin((float) (Math.PI + angle));
        double extraZ = radius * MathHelper.cos(angle);
        BlockPos radialPos = new BlockPos(fleePos.x + extraX, 0, fleePos.z + extraZ);
        BlockPos ground = bird.world.getTopSolidOrLiquidBlock(radialPos);
        if (!bird.isTargetBlocked(new Vec3d(ground.getX() + 0.5D, ground.getY() + 1.0D, ground.getZ() + 0.5D))) {
            return ground;
        }
        return null;
    }
}
