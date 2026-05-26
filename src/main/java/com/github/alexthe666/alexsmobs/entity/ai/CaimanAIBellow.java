package com.github.alexthe666.alexsmobs.entity.ai;

import com.github.alexthe666.alexsmobs.entity.EntityCaiman;
import com.github.alexthe666.alexsmobs.misc.AMSoundRegistry;
import net.minecraft.block.material.Material;
import net.minecraft.entity.ai.EntityAIBase;

public class CaimanAIBellow extends EntityAIBase {

    private final EntityCaiman caiman;
    private int bellowTime = 0;

    public CaimanAIBellow(EntityCaiman caiman) {
        this.caiman = caiman;
        this.setMutexBits(3);
    }

    @Override
    public boolean shouldExecute() {
        return this.caiman.getAttackTarget() == null && this.caiman.bellowCooldown <= 0 && this.caiman.isInWater() && !this.caiman.shouldFollow();
    }

    @Override
    public boolean shouldContinueExecuting() {
        return this.shouldExecute() && this.bellowTime < 60;
    }

    @Override
    public void resetTask() {
        this.bellowTime = 0;
        this.caiman.bellowCooldown = 1000 + this.caiman.getRNG().nextInt(1000);
        this.caiman.setBellowing(false);
    }

    @Override
    public void updateTask() {
        if (this.caiman.isInWater()) {
            double submerged = this.caiman.getSubmergedHeight(Material.WATER);
            this.caiman.getNavigator().clearPath();
            if (submerged > 0.3D) {
                double d2 = Math.pow(submerged - 0.3D, 2);
                this.caiman.motionY = Math.min(d2 * 0.08D, 0.04D);
            } else {
                this.caiman.motionY = -0.02D;
            }
            if (submerged > 0.19D && submerged < 0.5D) {
                this.bellowTime++;
                this.caiman.playSound(AMSoundRegistry.CAIMAN_SPLASH, 1.0F, 1.0F);
                this.caiman.setBellowing(true);
            }
        }
    }
}
