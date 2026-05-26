package com.github.alexthe666.alexsmobs.entity.ai;

import com.github.alexthe666.alexsmobs.entity.EntityOrca;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAIAttackMelee;

public class OrcaAIMelee extends EntityAIAttackMelee {

    private final EntityOrca orca;

    public OrcaAIMelee(EntityOrca orca, double speed, boolean useLongMemory) {
        super(orca, speed, useLongMemory);
        this.orca = orca;
    }

    @Override
    public boolean shouldExecute() {
        if (this.orca.getAttackTarget() == null || this.orca.shouldUseJumpAttack(this.orca.getAttackTarget())) {
            return false;
        }
        return super.shouldExecute();
    }
}
