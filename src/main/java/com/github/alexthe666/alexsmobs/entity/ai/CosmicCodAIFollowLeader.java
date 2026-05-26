package com.github.alexthe666.alexsmobs.entity.ai;

import com.github.alexthe666.alexsmobs.entity.EntityCosmicCod;
import net.minecraft.entity.ai.EntityAIBase;

import java.util.List;

public class CosmicCodAIFollowLeader extends EntityAIBase {
    private static final int INTERVAL_TICKS = 200;
    private final EntityCosmicCod mob;
    private int timeToRecalcPath;
    private int nextStartTick;

    public CosmicCodAIFollowLeader(EntityCosmicCod cod) {
        this.mob = cod;
        this.nextStartTick = this.nextStartTick(cod);
    }

    protected int nextStartTick(EntityCosmicCod cod) {
        return 100 + cod.getRNG().nextInt(100) % 20;
    }

    @Override
    public boolean shouldExecute() {
        if (this.mob.isGroupLeader() || this.mob.isCircling()) {
            return false;
        } else if (this.mob.hasGroupLeader()) {
            return true;
        } else if (this.nextStartTick > 0) {
            --this.nextStartTick;
            return false;
        } else {
            this.nextStartTick = this.nextStartTick(this.mob);
            List<EntityCosmicCod> list = this.mob.world.getEntitiesWithinAABB(EntityCosmicCod.class, this.mob.getEntityBoundingBox().grow(8.0D, 8.0D, 8.0D),
                    cod -> cod.canGroupGrow() || !cod.hasGroupLeader());
            EntityCosmicCod leader = null;
            for (EntityCosmicCod candidate : list) {
                if (candidate.canGroupGrow()) {
                    leader = candidate;
                    break;
                }
            }
            if (leader == null) {
                leader = this.mob;
            }
            for (EntityCosmicCod follower : list) {
                if (!follower.hasGroupLeader() && follower != leader) {
                    follower.createAndSetLeader(leader);
                }
            }
            return this.mob.hasGroupLeader();
        }
    }

    @Override
    public boolean shouldContinueExecuting() {
        return this.mob.hasGroupLeader() && this.mob.inRangeOfGroupLeader() && !this.mob.isCircling();
    }

    @Override
    public void startExecuting() {
        this.timeToRecalcPath = 0;
    }

    @Override
    public void resetTask() {
        this.mob.leaveGroup();
    }

    @Override
    public void updateTask() {
        if (--this.timeToRecalcPath <= 0) {
            this.timeToRecalcPath = 10;
            this.mob.moveToGroupLeader();
        }
    }
}
