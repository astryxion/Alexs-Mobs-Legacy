package com.github.alexthe666.alexsmobs.entity.ai;

import com.github.alexthe666.alexsmobs.entity.EntityCrow;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class CrowAIMelee extends EntityAIBase {
    private final EntityCrow crow;
    float circlingTime = 0;
    float circleDistance = 1;
    float yLevel = 2;
    boolean clockwise = false;
    private int maxCircleTime;

    public CrowAIMelee(EntityCrow crow) {
        this.crow = crow;
    }

    @Override
    public boolean shouldExecute() {
        return crow.getAttackTarget() != null && !crow.isSitting() && crow.getCommand() != 3;
    }

    @Override
    public void startExecuting() {
        clockwise = crow.getRNG().nextBoolean();
        yLevel = crow.getRNG().nextInt(2);
        circlingTime = 0;
        maxCircleTime = 20 + crow.getRNG().nextInt(100);
        circleDistance = 1.0F + crow.getRNG().nextFloat() * 3.0F;
    }

    @Override
    public void resetTask() {
        clockwise = crow.getRNG().nextBoolean();
        yLevel = crow.getRNG().nextInt(2);
        circlingTime = 0;
        maxCircleTime = 20 + crow.getRNG().nextInt(100);
        circleDistance = 1.0F + crow.getRNG().nextFloat() * 3.0F;
        if (crow.onGround) {
            crow.setFlying(false);
        }
    }

    @Override
    public void updateTask() {
        if (this.crow.isFlying()) {
            circlingTime++;
        }
        EntityLivingBase target = crow.getAttackTarget();
        if (circlingTime > maxCircleTime) {
            crow.getMoveHelper().setMoveTo(target.posX, target.posY + target.getEyeHeight() / 2.0F, target.posZ, 1.3D);
            if (crow.getDistance(target) < 2.0F) {
                crow.peck();
                if (target.getCreatureAttribute() == EnumCreatureAttribute.UNDEAD) {
                    target.attackEntityFrom(DamageSource.MAGIC, 4.0F);
                } else {
                    target.attackEntityFrom(DamageSource.GENERIC, 1.0F);
                }
                resetTask();
            }
        } else {
            Vec3d circlePos = getVultureCirclePos(new Vec3d(target.posX, target.posY, target.posZ));
            if (circlePos == null) {
                circlePos = new Vec3d(target.posX, target.posY, target.posZ);
            }
            crow.setFlying(true);
            crow.getMoveHelper().setMoveTo(circlePos.x, circlePos.y + target.getEyeHeight() + 0.2F, circlePos.z, 1.0D);
        }
    }

    public Vec3d getVultureCirclePos(Vec3d target) {
        float angle = (0.01745329251F * 8.0F * (clockwise ? -circlingTime : circlingTime));
        double extraX = circleDistance * MathHelper.sin(angle);
        double extraZ = circleDistance * MathHelper.cos(angle);
        Vec3d pos = new Vec3d(target.x + extraX, target.y + yLevel, target.z + extraZ);
        if (crow.world.isAirBlock(new BlockPos(pos.x, pos.y, pos.z))) {
            return pos;
        }
        return null;
    }
}
