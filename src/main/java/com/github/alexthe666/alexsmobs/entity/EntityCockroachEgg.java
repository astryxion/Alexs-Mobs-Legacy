package com.github.alexthe666.alexsmobs.entity;

import com.github.alexthe666.alexsmobs.item.AMItemRegistry;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.item.Item;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EntityCockroachEgg extends EntityThrowable {

    public EntityCockroachEgg(World worldIn) {
        super(worldIn);
    }

    public EntityCockroachEgg(World worldIn, EntityLivingBase throwerIn) {
        super(worldIn, throwerIn);
    }

    public EntityCockroachEgg(World worldIn, double x, double y, double z) {
        super(worldIn, x, y, z);
    }

    @SideOnly(Side.CLIENT)
    public void handleStatusUpdate(byte id) {
        if (id == 3) {
            for (int i = 0; i < 8; ++i) {
                this.world.spawnParticle(EnumParticleTypes.ITEM_CRACK,
                        this.posX, this.posY, this.posZ,
                        ((double) this.rand.nextFloat() - 0.5D) * 0.08D,
                        ((double) this.rand.nextFloat() - 0.5D) * 0.08D,
                        ((double) this.rand.nextFloat() - 0.5D) * 0.08D,
                        Item.getIdFromItem(this.getDefaultItem()));
            }
        }
    }

    @Override
    protected void onImpact(RayTraceResult result) {
        if (!this.world.isRemote) {
            this.world.setEntityState(this, (byte) 3);
            int i = rand.nextInt(3);
            for (int j = 0; j < i; ++j) {
                EntityCockroach croc = (EntityCockroach) AMEntityRegistry.COCKROACH.newInstance(this.world);
                croc.setGrowingAge(-24000);
                croc.setPositionAndRotation(this.posX, this.posY, this.posZ, this.rotationYaw, 0.0F);
                croc.onInitialSpawn(this.world.getDifficultyForLocation(this.getPosition()), (IEntityLivingData) null);
                croc.setHomePosAndDistance(this.getPosition(), 20);
                this.world.spawnEntity(croc);
            }
            this.world.setEntityState(this, (byte) 3);
            this.setDead();
        }
    }

    protected Item getDefaultItem() {
        return AMItemRegistry.COCKROACH_OOTHECA;
    }
}
