package com.github.alexthe666.alexsmobs.entity;

import com.github.alexthe666.alexsmobs.AlexsMobs;
import com.github.alexthe666.alexsmobs.message.MessageHurtMultipart;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Hitbox part for the giant squid; Forge 1.12 has no {@code PartEntity}, so this is a lightweight
 * {@link Entity} positioned each tick by {@link EntityGiantSquid}.
 */
public class EntityGiantSquidPart extends Entity implements IHurtableMultipart {

    @Nullable
    private EntityGiantSquid squid;
    public float scale = 1F;
    private final float baseWidth;
    private final float baseHeight;
    private boolean collisionOnly = false;

  /**
     * Required for {@link net.minecraftforge.fml.common.registry.EntityEntry} / factory registration.
     */
    public EntityGiantSquidPart(World world) {
        super(world);
        this.baseWidth = 1.0F;
        this.baseHeight = 1.0F;
        this.setSize(1.0F, 1.0F);
        this.setInvisible(true);
    }

    public EntityGiantSquidPart(EntityGiantSquid parent, float sizeX, float sizeY) {
        super(parent.world);
        this.squid = parent;
        this.baseWidth = sizeX;
        this.baseHeight = sizeY;
        this.setSize(sizeX, sizeY);
        this.setInvisible(true);
    }

    public EntityGiantSquidPart(EntityGiantSquid parent, float sizeX, float sizeY, boolean collisionOnly) {
        this(parent, sizeX, sizeY);
        this.collisionOnly = collisionOnly;
    }

    @Nullable
    public EntityGiantSquid getSquid() {
        return this.squid;
    }

    protected void entityInit() {
    }

    @Override
    public boolean isInvisible() {
        return true;
    }

    protected void collideWithNearbyEntities() {
        List<Entity> entities = this.world.getEntitiesWithinAABBExcludingEntity(this, this.getEntityBoundingBox().grow(0.20000000298023224D, 0.0D, 0.20000000298023224D));
        Entity parent = this.squid;
        if (parent != null) {
            for (Entity entity : entities) {
                if (entity != parent && !(entity instanceof EntityGiantSquidPart && ((EntityGiantSquidPart) entity).squid == parent) && entity.canBePushed()) {
                    entity.applyEntityCollision(parent);
                }
            }
        }
    }

    public boolean processInitialInteract(EntityPlayer player, EnumHand hand) {
        return this.squid != null && this.squid.applyInteractionFromPart(player, hand);
    }

    @Override
    public boolean canBeCollidedWith() {
        return !collisionOnly;
    }

    protected void collideWithEntity(Entity entityIn) {
        if (!collisionOnly) {
            entityIn.applyEntityCollision(this);
        }
    }

    public boolean isPickable() {
        return !collisionOnly;
    }

    @Nullable
    public ItemStack getPickedResult(net.minecraft.util.math.RayTraceResult target) {
        Entity parent = this.squid;
        return parent != null ? parent.getPickedResult(target) : ItemStack.EMPTY;
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (this.world.isRemote && this.squid != null && !this.squid.isEntityInvulnerable(source) && !collisionOnly) {
            AlexsMobs.sendMSGToServer(new MessageHurtMultipart(this.getEntityId(), this.squid.getEntityId(), amount));
        }
        return !collisionOnly && !this.isEntityInvulnerable(source) && this.squid != null && this.squid.attackEntityPartFrom(this, source, amount);
    }

    @Override
    public boolean isEntityEqual(Entity entityIn) {
        return this == entityIn || this.squid == entityIn;
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

    @Override
    public void onAttackedFromServer(EntityLivingBase parent, float damage) {
    }
}
