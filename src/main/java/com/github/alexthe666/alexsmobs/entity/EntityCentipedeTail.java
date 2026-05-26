package com.github.alexthe666.alexsmobs.entity;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.world.World;

public class EntityCentipedeTail extends EntityCentipedeBody {

    public EntityCentipedeTail(World worldIn) {
        super(worldIn);
    }

    public EntityCentipedeTail(World worldIn, EntityLivingBase parent, float radius, float angleYaw, float offsetY) {
        super(worldIn, parent, radius, angleYaw, offsetY);
    }

    @Override
    public EnumCreatureAttribute getCreatureAttribute() {
        return EnumCreatureAttribute.ARTHROPOD;
    }
}
