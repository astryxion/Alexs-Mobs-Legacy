package com.github.alexthe666.alexsmobs.effect;

import com.github.alexthe666.alexsmobs.entity.AMEntityRegistry;
import com.github.alexthe666.alexsmobs.entity.EntityTarantulaHawk;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityMoveHelper;
import net.minecraft.entity.ai.attributes.AbstractAttributeMap;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;

public class EffectDebilitatingSting extends EffectAlexsMobs {

    private int lastDuration = -1;

    protected EffectDebilitatingSting() {
        super(false, 0XFFF385, "debilitating_sting");
        this.registerPotionAttributeModifier(SharedMonsterAttributes.MOVEMENT_SPEED, "7107DE5E-7CE8-4030-940E-514C1F160890", -1.0F, 1);
    }

    public void removeAttributesModifiersFromEntity(EntityLivingBase entityLivingBaseIn, AbstractAttributeMap attributeMapIn, int amplifier) {
        if (entityLivingBaseIn.getCreatureAttribute() == EnumCreatureAttribute.ARTHROPOD) {
            super.removeAttributesModifiersFromEntity(entityLivingBaseIn, attributeMapIn, amplifier);
        }
    }

    public void applyAttributesModifiersToEntity(EntityLivingBase entityLivingBaseIn, AbstractAttributeMap attributeMapIn, int amplifier) {
        if (entityLivingBaseIn.getCreatureAttribute() == EnumCreatureAttribute.ARTHROPOD) {
            super.applyAttributesModifiersToEntity(entityLivingBaseIn, attributeMapIn, amplifier);
        }
    }

    public void performEffect(EntityLivingBase entity, int amplifier) {
        if (entity.getCreatureAttribute() != EnumCreatureAttribute.ARTHROPOD) {
            if (entity.getHealth() > entity.getMaxHealth() * 0.5F) {
                entity.attackEntityFrom(DamageSource.MAGIC, 1.0F);
            }
        } else {
            boolean suf = entity.isEntityInsideOpaqueBlock();
            if (suf) {
                entity.motionX = 0;
                entity.motionY = 0;
                entity.motionZ = 0;
                entity.noClip = true;
            }
            entity.setNoGravity(suf);
            if (!entity.isBeingRidden() && entity instanceof EntityLiving && !(((EntityLiving) entity).getMoveHelper().getClass() == EntityMoveHelper.class)) {
                entity.motionX = 0;
                entity.motionY = -1;
                entity.motionZ = 0;
            }
            if (lastDuration == 1) {
                entity.attackEntityFrom(DamageSource.MAGIC, (amplifier + 1) * 30);
                if (amplifier > 0) {
                    BlockPos surface = entity.getPosition();
                    while (!entity.world.isAirBlock(surface) && surface.getY() < 256) {
                        surface = surface.up();
                    }
                    EntityTarantulaHawk baby = (EntityTarantulaHawk) AMEntityRegistry.TARANTULA_HAWK.newInstance(entity.world);
                    baby.setGrowingAge(-24000);
                    baby.setPosition(entity.posX, surface.getY() + 0.1F, entity.posZ);
                    if (!entity.world.isRemote) {
                        baby.onInitialSpawn(entity.world.getDifficultyForLocation(entity.getPosition()), null);
                        ((WorldServer) entity.world).spawnEntity(baby);
                    }
                }
                entity.setNoGravity(false);
                entity.noClip = false;
            }
        }
    }

    public boolean isReady(int duration, int amplifier) {
        lastDuration = duration;
        return duration > 0;
    }

    public String getName() {
        return "alexsmobs.potion.debilitating_sting";
    }
}
