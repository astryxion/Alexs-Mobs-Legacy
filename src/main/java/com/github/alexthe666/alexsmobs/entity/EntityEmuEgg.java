package com.github.alexthe666.alexsmobs.entity;

import com.github.alexthe666.alexsmobs.item.AMItemRegistry;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.item.Item;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EntityEmuEgg extends EntityThrowable {

    public EntityEmuEgg(World worldIn) {
        super(worldIn);
    }

    public EntityEmuEgg(World worldIn, EntityLivingBase throwerIn) {
        super(worldIn, throwerIn);
    }

    public EntityEmuEgg(World worldIn, double x, double y, double z) {
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
            if (this.rand.nextInt(8) == 0) {
                int lvt_2_1_ = 1;
                if (this.rand.nextInt(32) == 0) {
                    lvt_2_1_ = 4;
                }
                for (int lvt_3_1_ = 0; lvt_3_1_ < lvt_2_1_; ++lvt_3_1_) {
                    EntityEmu lvt_4_1_ = (EntityEmu) AMEntityRegistry.EMU.newInstance(this.world);
                    if (this.rand.nextInt(50) == 0) {
                        lvt_4_1_.setVariant(2);
                    } else if (rand.nextInt(3) == 0) {
                        lvt_4_1_.setVariant(1);
                    }
                    lvt_4_1_.setGrowingAge(-24000);
                    lvt_4_1_.setPositionAndRotation(this.posX, this.posY, this.posZ, this.rotationYaw, 0.0F);
                    this.world.spawnEntity(lvt_4_1_);
                }
            }
            this.world.setEntityState(this, (byte) 3);
            this.setDead();
        }
    }

    protected Item getDefaultItem() {
        return AMItemRegistry.EMU_EGG;
    }
}
