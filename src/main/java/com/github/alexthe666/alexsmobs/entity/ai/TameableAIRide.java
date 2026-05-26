package com.github.alexthe666.alexsmobs.entity.ai;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.Vec3d;

public class TameableAIRide extends EntityAIBase {

    private final EntityCreature tameableEntity;
    private EntityLivingBase player;
    private final double speed;

    public TameableAIRide(EntityCreature dragon, double speed) {
        this.tameableEntity = dragon;
        this.speed = speed;
        this.setMutexBits(1);
    }

    @Override
    public boolean shouldExecute() {
        if (tameableEntity.getControllingPassenger() instanceof EntityPlayer) {
            player = (EntityPlayer) tameableEntity.getControllingPassenger();
            return true;
        }
        return false;
    }

    @Override
    public void startExecuting() {
        tameableEntity.getNavigator().clearPath();
    }

    @Override
    public void updateTask() {
        tameableEntity.getNavigator().clearPath();
        tameableEntity.setAttackTarget(null);
        double x = tameableEntity.posX;
        double y = tameableEntity.posY;
        double z = tameableEntity.posZ;
        if (player.moveForward != 0) {
            Vec3d lookVec = player.getLook(1.0F);
            if (player.moveForward < 0) {
                lookVec = new Vec3d(-lookVec.x, -lookVec.y, -lookVec.z);
            }
            x += lookVec.x * 10;
            z += lookVec.z * 10;
        }
        tameableEntity.moveStrafing = player.moveStrafing * 0.35F;
        tameableEntity.stepHeight = 1.0F;
        tameableEntity.getMoveHelper().setMoveTo(x, y, z, speed);
    }
}
