package com.github.alexthe666.alexsmobs.entity.ai;

import com.github.alexthe666.alexsmobs.entity.EntityBlueJay;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class BlueJayAIMelee extends EntityAIBase {
    private static final float STARTING_ANGLE = 0.0174532925F;
    private final EntityBlueJay blueJay;
    float circlingTime = 0;
    float circleDistance = 1;
    float yLevel = 2;
    boolean clockwise = false;
    private int maxCircleTime;

    public BlueJayAIMelee(EntityBlueJay blueJay) {
        this.blueJay = blueJay;
    }

    @Override
    public boolean shouldExecute() {
        EntityLivingBase entity = blueJay.getAttackTarget();
        return entity != null && entity.isEntityAlive();
    }

    @Override
    public void startExecuting() {
        clockwise = blueJay.getRNG().nextBoolean();
        yLevel = blueJay.getRNG().nextInt(2);
        circlingTime = 0;
        maxCircleTime = 20 + blueJay.getRNG().nextInt(20);
        circleDistance = 0.5F + blueJay.getRNG().nextFloat() * 2F;
    }

    @Override
    public void resetTask() {
        clockwise = blueJay.getRNG().nextBoolean();
        yLevel = blueJay.getRNG().nextInt(2);
        circlingTime = 0;
        maxCircleTime = 20 + blueJay.getRNG().nextInt(20);
        circleDistance = 0.5F + blueJay.getRNG().nextFloat() * 2F;
        if (blueJay.onGround) {
            blueJay.setFlying(false);
        }
    }

    @Override
    public void updateTask() {
        if (this.blueJay.isFlying()) {
            circlingTime++;
        }
        EntityLivingBase target = blueJay.getAttackTarget();
        if (target != null) {
            if (blueJay.getDistance(target) < 3) {
                blueJay.peck();
                target.attackEntityFrom(DamageSource.GENERIC, 1);
                resetTask();
            }
            if (circlingTime > maxCircleTime) {
                blueJay.getMoveHelper().setMoveTo(target.posX, target.posY + target.getEyeHeight() / 2F, target.posZ, 1.6D);
            } else {
                Vec3d circlePos = getVultureCirclePos(new Vec3d(target.posX, target.posY, target.posZ));
                if (circlePos == null) {
                    circlePos = new Vec3d(target.posX, target.posY, target.posZ);
                }
                blueJay.setFlying(true);
                blueJay.getMoveHelper().setMoveTo(circlePos.x, circlePos.y + target.getEyeHeight() + 0.2F, circlePos.z, 1.6D);
            }
        }
    }

    public Vec3d getVultureCirclePos(Vec3d target) {
        float angle = (STARTING_ANGLE * 13 * (clockwise ? -circlingTime : circlingTime));
        double extraX = circleDistance * MathHelper.sin(angle);
        double extraZ = circleDistance * MathHelper.cos(angle);
        Vec3d pos = new Vec3d(target.x + extraX, target.y + yLevel, target.z + extraZ);
        if (blueJay.world.isAirBlock(new BlockPos(pos.x, pos.y, pos.z))) {
            return pos;
        }
        return null;
    }
}
