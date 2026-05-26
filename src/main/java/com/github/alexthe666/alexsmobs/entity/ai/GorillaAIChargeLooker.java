package com.github.alexthe666.alexsmobs.entity.ai;

import com.github.alexthe666.alexsmobs.entity.EntityGorilla;
import com.github.alexthe666.citadel.animation.IAnimatedEntity;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.Vec3d;

import java.util.List;

/**
 * 1.12 port of 1.20 {@link GorillaAIChargeLooker} — silverbacks charge players who stare at them.
 */
public class GorillaAIChargeLooker extends EntityAIBase {

    private final EntityGorilla gorilla;
    private final double range = 20D;
    private final double speed;
    private EntityPlayer starer;
    private int runDelay = 0;

    public GorillaAIChargeLooker(EntityGorilla gorilla, double speed) {
        this.gorilla = gorilla;
        this.speed = speed;
        this.setMutexBits(3);
    }

    @Override
    public boolean shouldExecute() {
        if (this.gorilla.isSilverback() && !this.gorilla.isTamed() && runDelay-- == 0) {
            runDelay = 100 + gorilla.getRNG().nextInt(200);
            List<EntityPlayer> playerList = this.gorilla.world.getEntitiesWithinAABB(EntityPlayer.class, this.gorilla.getEntityBoundingBox().grow(range, range, range));
            EntityPlayer closestPlayer = null;
            for (EntityPlayer player : playerList) {
                if (!player.isSpectator() && isLookingAtMe(player)) {
                    if (closestPlayer == null || player.getDistance(gorilla) < closestPlayer.getDistance(gorilla)) {
                        closestPlayer = player;
                    }
                }
            }
            starer = closestPlayer;
            return starer != null;
        }
        return false;
    }

    @Override
    public boolean shouldContinueExecuting() {
        return starer != null && starer.isEntityAlive() && this.gorilla.isEntityAlive();
    }

    @Override
    public void resetTask() {
        this.starer = null;
        this.gorilla.setSprinting(false);
        runDelay = 300 + gorilla.getRNG().nextInt(200);
    }

    @Override
    public void updateTask() {
        this.gorilla.setSitting(false);
        this.gorilla.poundChestCooldown = 50;
        if (this.gorilla.getDistance(starer) > 1 + starer.width + this.gorilla.width) {
            this.gorilla.getNavigator().tryMoveToEntityLiving(starer, speed);
            this.gorilla.setSprinting(!this.gorilla.isSitting() && !this.gorilla.isStanding());
        } else {
            this.gorilla.getNavigator().clearPath();
            this.gorilla.getLookHelper().setLookPositionWithEntity(starer, 180.0F, 30.0F);
            this.gorilla.setSprinting(false);
            if (this.gorilla.getAnimation() == IAnimatedEntity.NO_ANIMATION) {
                this.gorilla.setStanding(true);
                this.gorilla.maxStandTime = 45;
                this.gorilla.setAnimation(EntityGorilla.ANIMATION_POUNDCHEST);
            }
            if (this.gorilla.getAnimation() == EntityGorilla.ANIMATION_POUNDCHEST && this.gorilla.getAnimationTick() >= 10) {
                this.resetTask();
            }
        }
    }

    private boolean isLookingAtMe(EntityPlayer player) {
        Vec3d vec3 = player.getLook(1.0F).normalize();
        Vec3d vec31 = new Vec3d(gorilla.posX - player.posX, gorilla.getEntityBoundingBox().minY + (double) gorilla.getEyeHeight() - (player.posY + (double) player.getEyeHeight()), gorilla.posZ - player.posZ);
        double d0 = vec31.lengthVector();
        if (d0 < 1.0E-4D) {
            return false;
        }
        vec31 = vec31.normalize();
        double d1 = vec3.dotProduct(vec31);
        return d1 > 1.0D - 0.025D / d0 && player.canEntityBeSeen(gorilla);
    }
}
