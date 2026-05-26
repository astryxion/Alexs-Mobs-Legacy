package com.github.alexthe666.alexsmobs.entity.ai;

import com.github.alexthe666.alexsmobs.entity.EntitySeagull;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class SeagullAIRevealTreasure extends EntityAIBase {

    private final EntitySeagull seagull;
    private BlockPos sitPos;

    public SeagullAIRevealTreasure(EntitySeagull entitySeagull) {
        this.seagull = entitySeagull;
        this.setMutexBits(3);
    }

    @Override
    public boolean shouldExecute() {
        return seagull.getTreasurePos() != null && seagull.treasureSitTime > 0;
    }

    @Override
    public void startExecuting() {
        seagull.aiItemFlag = true;
        sitPos = seagull.getSeagullGround(seagull.getTreasurePos());
    }

    @Override
    public void resetTask() {
        sitPos = null;
        seagull.setSitting(false);
        seagull.aiItemFlag = false;
    }

    @Override
    public void updateTask() {
        if (sitPos != null) {
            if (seagull.getDistanceSq(sitPos.getX() + 0.5F, seagull.posY, sitPos.getZ() + 0.5F) > 2.5F) {
                seagull.getMoveHelper().setMoveTo(sitPos.getX() + 0.5F, sitPos.getY() + 2, sitPos.getZ() + 0.5F, 1F);
                if (!seagull.onGround) {
                    seagull.setFlying(true);
                }
            } else {
                Vec3d vec = new Vec3d(sitPos.getX() + 0.5D, sitPos.getY() + 1.0D, sitPos.getZ() + 0.5D);
                Vec3d delta = vec.subtract(seagull.getPositionVector());
                if (delta.lengthVector() > 0.04F) {
                    seagull.motionX = delta.x * 0.2D;
                    seagull.motionY = delta.y * 0.2D;
                    seagull.motionZ = delta.z * 0.2D;
                }
                seagull.eatItem();
                seagull.treasureSitTime = Math.min(seagull.treasureSitTime, 100);
                seagull.setFlying(false);
                seagull.setSitting(true);
            }
        }
    }

}
