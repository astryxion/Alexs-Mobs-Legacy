package com.github.alexthe666.alexsmobs.entity.ai;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

/**
 * Melee attack that never pursues creative/spectator players.
 */
public class RoadrunnerAIAttackMelee extends EntityAIAttackMelee {

    public RoadrunnerAIAttackMelee(EntityCreature creature, double speedIn, boolean useLongMemory) {
        super(creature, speedIn, useLongMemory);
    }

    private static boolean isNonCombatPlayer(EntityLivingBase target) {
        if (!(target instanceof EntityPlayer)) {
            return false;
        }
        EntityPlayer player = (EntityPlayer) target;
        return player.capabilities.isCreativeMode
                || (player instanceof EntityPlayerMP && ((EntityPlayerMP) player).isSpectator());
    }

    @Override
    public boolean shouldExecute() {
        return !isNonCombatPlayer(this.attacker.getAttackTarget()) && super.shouldExecute();
    }

    @Override
    public boolean shouldContinueExecuting() {
        return !isNonCombatPlayer(this.attacker.getAttackTarget()) && super.shouldContinueExecuting();
    }
}
