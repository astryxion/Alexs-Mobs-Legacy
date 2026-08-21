package com.github.alexthe666.alexsmobs.entity;

import com.github.alexthe666.alexsmobs.AlexsMobs;
import com.github.alexthe666.alexsmobs.client.particle.AMParticleRegistry;
import com.github.alexthe666.alexsmobs.item.AMItemRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityFireworkRocket;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

public class EntityEnderiophageRocket extends EntityFireworkRocket {

    private static final String LIFETIME_SRG = "field_92055_b";

    private int phageAge = 0;

    public EntityEnderiophageRocket(World worldIn) {
        super(worldIn);
    }

    public EntityEnderiophageRocket(World worldIn, double x, double y, double z, ItemStack givenItem) {
        super(worldIn, x, y, z, givenItem);
        this.motionX = this.rand.nextGaussian() * 0.001D;
        this.motionY = 0.05D;
        this.motionZ = this.rand.nextGaussian() * 0.001D;
        ReflectionHelper.setPrivateValue(EntityFireworkRocket.class, this, 18 + this.rand.nextInt(14), "lifetime", LIFETIME_SRG);
    }

    public EntityEnderiophageRocket(World worldIn, @Nullable Entity shooter, double x, double y, double z, ItemStack givenItem) {
        this(worldIn, x, y, z, givenItem);
    }

    public EntityEnderiophageRocket(World worldIn, ItemStack stack, EntityLivingBase shooter) {
        super(worldIn, stack, shooter);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        ++this.phageAge;
        if (this.world.isRemote) {
            this.world.spawnParticle(EnumParticleTypes.END_ROD, this.posX, this.posY - 0.3D, this.posZ, this.rand.nextGaussian() * 0.05D, -this.motionY * 0.5D, this.rand.nextGaussian() * 0.05D);
        }
    }

    @SideOnly(Side.CLIENT)
    public void handleStatusUpdate(byte id) {
        if (id == 17) {
            this.world.spawnParticle(EnumParticleTypes.EXPLOSION_NORMAL, this.posX, this.posY, this.posZ, this.rand.nextGaussian() * 0.05D, 0.005D, this.rand.nextGaussian() * 0.05D);
            for (int i = 0; i < this.rand.nextInt(15) + 30; ++i) {
                AMParticleRegistry.spawnParticle(this.world, AMParticleRegistry.DNA, this.posX, this.posY, this.posZ, this.rand.nextGaussian() * 0.25D, this.rand.nextGaussian() * 0.25D, this.rand.nextGaussian() * 0.25D);
            }
            for (int i = 0; i < this.rand.nextInt(15) + 15; ++i) {
                this.world.spawnParticle(EnumParticleTypes.END_ROD, this.posX, this.posY, this.posZ, this.rand.nextGaussian() * 0.15D, this.rand.nextGaussian() * 0.15D, this.rand.nextGaussian() * 0.15D);
            }
            SoundEvent soundEvent = AlexsMobs.PROXY.isFarFromCamera(this.posX, this.posY, this.posZ) ? SoundEvents.ENTITY_FIREWORK_BLAST : SoundEvents.ENTITY_FIREWORK_BLAST_FAR;
            this.world.playSound(this.posX, this.posY, this.posZ, soundEvent, SoundCategory.AMBIENT, 20.0F, 0.95F + this.rand.nextFloat() * 0.1F, true);
        } else {
            super.handleStatusUpdate(id);
        }
    }

    @SideOnly(Side.CLIENT)
    public ItemStack getItem() {
        return new ItemStack(AMItemRegistry.ENDERIOPHAGE_ROCKET);
    }

}
