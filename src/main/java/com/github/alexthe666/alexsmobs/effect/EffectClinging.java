package com.github.alexthe666.alexsmobs.effect;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AbstractAttributeMap;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

public class EffectClinging extends EffectAlexsMobs {

    public EffectClinging() {
        super(false, 0XBD4B4B, "clinging");
    }

    private static BlockPos getPositionUnderneath(Entity e) {
        return new BlockPos(e.posX, e.getEntityBoundingBox().maxY + 1.51F, e.posZ);
    }

    public void performEffect(EntityLivingBase entity, int amplifier) {
        entity.setNoGravity(false);

        if (isUpsideDown(entity)) {
            entity.fallDistance = 0;
            if (!entity.isSneaking()) {
                if (!entity.collidedHorizontally) {
                    entity.motionY += 0.3F;
                }
                entity.motionX *= 0.998F;
                entity.motionZ *= 0.998F;
            }
        }
    }

    public static boolean isUpsideDown(EntityLivingBase entity){
        BlockPos pos = getPositionUnderneath(entity);
        IBlockState ground = entity.world.getBlockState(pos);
        return (entity.collidedVertically || ground.isSideSolid(entity.world, pos, EnumFacing.DOWN)) && !entity.onGround;
    }
    public void removeAttributesModifiersFromEntity(EntityLivingBase entityLivingBaseIn, AbstractAttributeMap attributeMapIn, int amplifier) {
        super.removeAttributesModifiersFromEntity(entityLivingBaseIn, attributeMapIn, amplifier);
    }

    public boolean isReady(int duration, int amplifier) {
        return duration > 0;
    }

    public String getName() {
        return "alexsmobs.potion.clinging";
    }

}
