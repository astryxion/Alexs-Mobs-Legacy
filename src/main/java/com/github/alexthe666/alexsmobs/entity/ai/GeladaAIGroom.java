package com.github.alexthe666.alexsmobs.entity.ai;

import com.github.alexthe666.alexsmobs.entity.EntityGeladaMonkey;
import com.github.alexthe666.citadel.animation.IAnimatedEntity;
import net.minecraft.entity.ai.EntityAIBase;

import java.util.EnumSet;

public class GeladaAIGroom extends EntityAIBase {

    private final EntityGeladaMonkey monkey;
    private int groomTime = 0;
    private int groomCooldown = 220;
    private EntityGeladaMonkey beingGroomed;

    public GeladaAIGroom(EntityGeladaMonkey monkey) {
        this.setMutexBits(3);
        this.monkey = monkey;
    }

    @Override
    public boolean shouldExecute() {
        if (groomCooldown > 0) {
            groomCooldown--;
            return false;
        } else {
            groomCooldown = 200 + monkey.getRNG().nextInt(1000);
            EntityGeladaMonkey nearestMonkey = null;
            for (EntityGeladaMonkey entity : monkey.world.getEntitiesWithinAABB(EntityGeladaMonkey.class, monkey.getEntityBoundingBox().grow(15F))) {
                if (entity.getEntityId() != monkey.getEntityId() && monkey.canBeGroomed() && (nearestMonkey == null || monkey.getDistance(nearestMonkey) > monkey.getDistance(entity))) {
                    nearestMonkey = entity;
                }
            }
            beingGroomed = nearestMonkey;
            return beingGroomed != null;
        }
    }

    @Override
    public boolean shouldContinueExecuting() {
        return beingGroomed != null && beingGroomed.isEntityAlive() && !beingGroomed.shouldStopBeingGroomed() && groomTime < 200 && (beingGroomed.groomerID == -1 || beingGroomed.groomerID == monkey.getEntityId());
    }

    @Override
    public void resetTask() {
        groomTime = 0;
        monkey.isGrooming = false;
        if (beingGroomed != null) {
            beingGroomed.groomerID = -1;
        }
        beingGroomed = null;
    }

    @Override
    public void updateTask() {
        double dist = monkey.getDistance(beingGroomed);
        if (dist < monkey.width + 0.5F) {
            monkey.isGrooming = true;
            beingGroomed.groomerID = monkey.getEntityId();
            monkey.setSitting(true);
            groomTime++;
            if (groomTime % 50 == 0) {
                monkey.heal(1);
            }
            if (monkey.getAnimation() == IAnimatedEntity.NO_ANIMATION) {
                monkey.setAnimation(EntityGeladaMonkey.ANIMATION_GROOM);
            }
            monkey.getNavigator().clearPath();
            monkey.getLookHelper().setLookPositionWithEntity(beingGroomed, 360, 360);
        } else {
            monkey.isGrooming = false;
            beingGroomed.groomerID = -1;
            monkey.setSitting(false);
            monkey.getNavigator().tryMoveToEntityLiving(beingGroomed, 1);
        }
    }
}
