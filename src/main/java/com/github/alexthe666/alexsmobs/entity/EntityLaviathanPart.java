package com.github.alexthe666.alexsmobs.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class EntityLaviathanPart extends Entity {

    @Nullable
    private EntityLaviathan laviathan;
    public float scale = 1F;
    private final float baseWidth;
    private final float baseHeight;

    public EntityLaviathanPart(World world) {
        super(world);
        this.baseWidth = 1.0F;
        this.baseHeight = 1.0F;
        this.setSize(1.0F, 1.0F);
        this.setInvisible(true);
    }

    public EntityLaviathanPart(EntityLaviathan parent, float sizeX, float sizeY) {
        super(parent.world);
        this.laviathan = parent;
        this.baseWidth = sizeX;
        this.baseHeight = sizeY;
        this.setSize(sizeX, sizeY);
        this.setInvisible(true);
    }

    @Nullable
    public EntityLaviathan getLaviathan() {
        return this.laviathan;
    }

    @Override
    protected void entityInit() {
    }

    @Override
    public boolean isInvisible() {
        return true;
    }

    

    protected void collideWithNearbyEntities() {
        List<Entity> entities = this.world.getEntitiesWithinAABBExcludingEntity(this, this.getEntityBoundingBox().expand(0.20000000298023224D, 0.0D, 0.20000000298023224D));
        Entity parent = this.laviathan;
        if (parent != null) {
            for (Entity entity : entities) {
                if (entity != parent && !(entity instanceof EntityLaviathanPart && ((EntityLaviathanPart) entity).laviathan == parent) && entity.canBePushed()) {
                    entity.applyEntityCollision(parent);
                }
            }
        }
    }

    @Override
    public boolean processInitialInteract(EntityPlayer player, EnumHand hand) {
        return this.laviathan != null && this.laviathan.processInteract(player, hand);
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (this.laviathan == null) {
            return false;
        }
        return !this.isEntityInvulnerable(source) && this.laviathan.attackEntityPartFrom(this, source, amount);
    }

    @Override
    public boolean isEntityEqual(Entity entityIn) {
        return this == entityIn || this.laviathan == entityIn;
    }

    public void recalculateSize() {
        this.setSize(this.baseWidth * this.scale, this.baseHeight * this.scale);
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound compound) {
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound compound) {
    }
}
