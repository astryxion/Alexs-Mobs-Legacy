package com.github.alexthe666.alexsmobs.entity.ai;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.Vec3d;

/**
 * Tusklin always moves forward when ridden (1.16 {@code TameableAIRide} with {@code shouldMoveForward} true).
 */
public class TusklinAIRide extends EntityAIBase {

    private final EntityCreature mount;
    private final double speed;
    private EntityPlayer rider;

    public TusklinAIRide(EntityCreature mount, double speed) {
        this.mount = mount;
        this.speed = speed;
        this.setMutexBits(1);
    }

    @Override
    public boolean shouldExecute() {
        if (mount.getControllingPassenger() instanceof EntityPlayer && mount.isBeingRidden()) {
            rider = (EntityPlayer) mount.getControllingPassenger();
            return true;
        }
        mount.setSprinting(false);
        return false;
    }

    @Override
    public void startExecuting() {
        mount.getNavigator().clearPath();
    }

    @Override
    public void updateTask() {
        mount.stepHeight = 1.0F;
        mount.getNavigator().clearPath();
        mount.setAttackTarget(null);
        double x = mount.posX;
        double y = mount.posY;
        double z = mount.posZ;
        if (mount.isBeingRidden()) {
            mount.setSprinting(true);
            Vec3d lookVec = rider.getLook(1.0F);
            x += lookVec.x * 10;
            z += lookVec.z * 10;
            mount.getMoveHelper().setMoveTo(x, y, z, speed);
        } else {
            mount.setSprinting(false);
        }
    }
}
