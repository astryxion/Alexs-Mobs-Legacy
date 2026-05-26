package com.github.alexthe666.alexsmobs.entity.ai;

import com.github.alexthe666.alexsmobs.entity.IFollower;
import net.minecraft.entity.ai.EntityAIFollowOwner;
import net.minecraft.entity.passive.EntityTameable;

public class TameableAIFollowOwner extends EntityAIFollowOwner {

    private final IFollower follower;

    public TameableAIFollowOwner(EntityTameable tameable, double speed, float minDist, float maxDist, boolean teleportToLeaves) {
        super(tameable, speed, minDist, maxDist);
        this.follower = (IFollower) tameable;
    }

    @Override
    public boolean shouldExecute() {
        return super.shouldExecute() && follower.shouldFollow();
    }
}
