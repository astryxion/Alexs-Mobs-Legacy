package com.github.alexthe666.alexsmobs.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Hitbox part for the cachalot whale; Forge 1.12 has no {@code PartEntity}, so this is a lightweight
 * {@link Entity} positioned each tick by {@link EntityCachalotWhale}.
 */
public class EntityCachalotPart extends Entity {

    @Nullable
    private EntityCachalotWhale whale;
    public float scale = 1F;
    private final float baseWidth;
    private final float baseHeight;

    /**
     * Required for {@link net.minecraftforge.fml.common.registry.EntityEntry} / factory registration.
     */
    public EntityCachalotPart(World world) {
        super(world);
        this.baseWidth = 1.0F;
        this.baseHeight = 1.0F;
        this.setSize(1.0F, 1.0F);
        this.setInvisible(true);
    }

    public EntityCachalotPart(EntityCachalotWhale parent, float sizeX, float sizeY) {
        super(parent.world);
        this.whale = parent;
        this.baseWidth = sizeX;
        this.baseHeight = sizeY;
        this.setSize(sizeX, sizeY);
        this.setInvisible(true);
    }

    @Nullable
    public EntityCachalotWhale getWhale() {
        return this.resolveWhale();
    }

    @Nullable
    private EntityCachalotWhale resolveWhale() {
        if (this.whale != null && this.whale.isEntityAlive()) {
            return this.whale;
        }
        List<EntityCachalotWhale> nearby = this.world.getEntitiesWithinAABB(EntityCachalotWhale.class, this.getEntityBoundingBox().grow(8.0D));
        EntityCachalotWhale nearest = null;
        double best = Double.MAX_VALUE;
        for (EntityCachalotWhale candidate : nearby) {
            double dist = this.getDistanceSq(candidate);
            if (dist < best) {
                best = dist;
                nearest = candidate;
            }
        }
        this.whale = nearest;
        return nearest;
    }

    @Override
    protected void entityInit() {
    }

    @Override
    public boolean isInvisible() {
        return true;
    }

    @Override
    public void onEntityUpdate() {
        super.onEntityUpdate();
    }

    protected void collideWithNearbyEntities() {
        List<Entity> entities = this.world.getEntitiesWithinAABBExcludingEntity(this, this.getEntityBoundingBox().expand(0.20000000298023224D, 0.0D, 0.20000000298023224D));
        Entity parent = this.whale;
        if (parent != null) {
            for (Entity entity : entities) {
                if (entity != parent && !(entity instanceof EntityCachalotPart && ((EntityCachalotPart) entity).whale == parent) && entity.canBePushed()) {
                    entity.applyEntityCollision(parent);
                }
            }
        }
    }

    @Override
    public boolean processInitialInteract(EntityPlayer player, EnumHand hand) {
        EntityCachalotWhale parent = this.resolveWhale();
        return parent != null && parent.applyInteractionFromPart(player, hand);
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        EntityCachalotWhale parent = this.resolveWhale();
        if (parent == null) {
            return false;
        }
        return !this.isEntityInvulnerable(source) && parent.attackEntityPartFrom(this, source, amount);
    }

    @Override
    public boolean isEntityEqual(Entity entityIn) {
        return this == entityIn || this.whale == entityIn;
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
