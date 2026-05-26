package com.github.alexthe666.alexsmobs.entity;

import com.github.alexthe666.alexsmobs.item.AMItemRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EntityTossedItem extends EntityThrowable {

    protected static final DataParameter<Boolean> DART = EntityDataManager.createKey(EntityTossedItem.class, DataSerializers.BOOLEAN);

    public EntityTossedItem(World worldIn) {
        super(worldIn);
    }

    public EntityTossedItem(World worldIn, EntityLivingBase throwerIn) {
        super(worldIn, throwerIn);
    }

    public EntityTossedItem(World worldIn, double x, double y, double z) {
        super(worldIn, x, y, z);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(DART, false);
    }

    public boolean isDart() {
        return this.dataManager.get(DART);
    }

    public void setDart(boolean dart) {
        this.dataManager.set(DART, dart);
    }

    @SideOnly(Side.CLIENT)
    public void handleStatusUpdate(byte id) {
        if (id == 3) {
            for (int i = 0; i < 8; ++i) {
                this.world.spawnParticle(net.minecraft.util.EnumParticleTypes.ITEM_CRACK,
                        this.posX, this.posY, this.posZ,
                        ((double) this.rand.nextFloat() - 0.5D) * 0.08D,
                        ((double) this.rand.nextFloat() - 0.5D) * 0.08D,
                        ((double) this.rand.nextFloat() - 0.5D) * 0.08D,
                        Item.getIdFromItem(this.getDefaultItem()));
            }
        }
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
    }

    @Override
    protected void onImpact(RayTraceResult result) {
        if (result.entityHit != null) {
            Entity hit = result.entityHit;
            if (this.getThrower() instanceof EntityCapuchinMonkey) {
                EntityCapuchinMonkey boss = (EntityCapuchinMonkey) this.getThrower();
                if (!boss.isOnSameTeam(hit) || !boss.isTamed() && !(hit instanceof EntityCapuchinMonkey)) {
                    hit.attackEntityFrom(DamageSource.causeThrownDamage(this, boss), isDart() ? 8.0F : 4.0F);
                }
            }
        }
        if (!this.world.isRemote && (!this.isDart() || result.typeOfHit == RayTraceResult.Type.BLOCK)) {
            this.world.setEntityState(this, (byte) 3);
            this.setDead();
        }
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setBoolean("Dart", this.isDart());
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.setDart(compound.getBoolean("Dart"));
    }

    protected Item getDefaultItem() {
        return isDart() ? AMItemRegistry.ANCIENT_DART : Item.getItemFromBlock(net.minecraft.init.Blocks.COBBLESTONE);
    }

    public ItemStack getItemStack() {
        return new ItemStack(this.getDefaultItem());
    }
}
