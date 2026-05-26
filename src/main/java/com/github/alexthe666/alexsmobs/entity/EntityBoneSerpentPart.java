package com.github.alexthe666.alexsmobs.entity;

import com.github.alexthe666.alexsmobs.AlexsMobs;
import com.github.alexthe666.alexsmobs.message.MessageHurtMultipart;
import com.google.common.collect.ImmutableList;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import javax.annotation.Nullable;
import java.util.List;
import com.google.common.base.Optional;
import java.util.UUID;

public class EntityBoneSerpentPart extends EntityLivingBase implements IHurtableMultipart {

    private static final DataParameter<Boolean> TAIL = EntityDataManager.createKey(EntityBoneSerpentPart.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> BODYINDEX = EntityDataManager.createKey(EntityBoneSerpentPart.class, DataSerializers.VARINT);
    private static final DataParameter<Optional<UUID>> PARENT_UUID = EntityDataManager.createKey(EntityBoneSerpentPart.class, DataSerializers.OPTIONAL_UNIQUE_ID);
    protected float radius;
    protected float angleYaw;
    protected float offsetY;
    protected float damageMultiplier = 1;

    public EntityBoneSerpentPart(World world) {
        super(world);
        this.setSize(1.0F, 1.0F);
    }

    public EntityBoneSerpentPart(World world, EntityLivingBase parent, float radius, float angleYaw, float offsetY) {
        this(world);
        this.setParent(parent);
        this.radius = radius;
        this.angleYaw = (angleYaw + 90.0F) * ((float) Math.PI / 180.0F);
        this.offsetY = offsetY;
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        AMEntityRegistry.registerAttributeIfAbsent(this, SharedMonsterAttributes.MOVEMENT_SPEED);
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(10.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.15D);
    }

    @Override
    public EnumCreatureAttribute getCreatureAttribute() {
        return EnumCreatureAttribute.UNDEAD;
    }

    @Override
    public boolean startRiding(Entity entityIn) {
        if (!(entityIn instanceof EntityMinecart) && !(entityIn instanceof EntityBoat)) {
            return super.startRiding(entityIn);
        }
        return false;
    }

    public net.minecraft.entity.Entity getEntity() {
        return this;
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        if (this.getParentId() != null) {
            compound.setUniqueId("ParentUUID", this.getParentId());
        }
        compound.setBoolean("TailPart", isTail());
        compound.setInteger("BodyIndex", getBodyIndex());
        compound.setFloat("PartAngle", angleYaw);
        compound.setFloat("PartRadius", radius);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        if (compound.hasUniqueId("ParentUUID")) {
            this.setParentId(compound.getUniqueId("ParentUUID"));
        }
        this.setTail(compound.getBoolean("TailPart"));
        this.setBodyIndex(compound.getInteger("BodyIndex"));
        this.angleYaw = compound.getFloat("PartAngle");
        this.radius = compound.getFloat("PartRadius");
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(PARENT_UUID, Optional.absent());
        this.dataManager.register(TAIL, Boolean.FALSE);
        this.dataManager.register(BODYINDEX, 0);
    }

    @Nullable
    public UUID getParentId() {
        return this.dataManager.get(PARENT_UUID).orNull();
    }

    public void setParentId(@Nullable UUID uniqueId) {
        this.dataManager.set(PARENT_UUID, Optional.fromNullable(uniqueId));
    }

    public void setInitialPartPos(Entity parent) {
        this.setPosition(
                parent.prevPosX + this.radius * Math.cos(parent.rotationYaw * (Math.PI / 180.0F) + this.angleYaw),
                parent.prevPosY + this.offsetY,
                parent.prevPosZ + this.radius * Math.sin(parent.rotationYaw * (Math.PI / 180.0F) + this.angleYaw));
    }

    @Override
    public void onUpdate() {
        this.inPortal = false;
        if (this.ticksExisted > 10) {
            Entity parent = getParent();
            if (parent != null && !world.isRemote) {
                this.setNoGravity(true);
                this.setPosition(
                        parent.prevPosX + this.radius * Math.cos(parent.prevRotationYaw * (Math.PI / 180.0F) + this.angleYaw),
                        parent.prevPosY + this.offsetY,
                        parent.prevPosZ + this.radius * Math.sin(parent.prevRotationYaw * (Math.PI / 180.0F) + this.angleYaw));
                double d0 = parent.posX - this.posX;
                double d1 = parent.posY - this.posY;
                double d2 = parent.posZ - this.posZ;
                float f2 = -((float) (MathHelper.atan2(d1, MathHelper.sqrt(d0 * d0 + d2 * d2)) * (double) (180F / (float) Math.PI)));
                this.rotationPitch = this.limitAngle(this.rotationPitch, f2, 5.0F);
                this.velocityChanged = true;
                this.rotationYaw = parent.prevRotationYaw;
                this.rotationYawHead = this.rotationYaw;
                this.renderYawOffset = this.prevRotationYaw;
                if (parent instanceof EntityLivingBase) {
                    EntityLivingBase plb = (EntityLivingBase) parent;
                    if (!world.isRemote && (plb.hurtTime > 0 || plb.deathTime > 0)) {
                        AlexsMobs.sendMSGToAll(new MessageHurtMultipart(this.getEntityId(), parent.getEntityId(), 0));
                        this.hurtTime = plb.hurtTime;
                        this.deathTime = plb.deathTime;
                    }
                }
                this.collideWithNearbyEntities();
                if (!parent.isEntityAlive() && !world.isRemote) {
                    this.setDead();
                }
            } else if (ticksExisted > 20 && !world.isRemote) {
                this.setDead();
            }
        }
        super.onUpdate();
    }

    protected float limitAngle(float sourceAngle, float targetAngle, float maximumChange) {
        float f = MathHelper.wrapDegrees(targetAngle - sourceAngle);
        if (f > maximumChange) {
            f = maximumChange;
        }

        if (f < -maximumChange) {
            f = -maximumChange;
        }

        float f1 = sourceAngle + f;
        if (f1 < 0.0F) {
            f1 += 360.0F;
        } else if (f1 > 360.0F) {
            f1 -= 360.0F;
        }

        return f1;
    }

    @Nullable
    public Entity getParent() {
        UUID id = getParentId();
        if (id != null && !world.isRemote && world instanceof WorldServer) {
            return ((WorldServer) world).getEntityFromUuid(id);
        }
        return null;
    }

    public void setParent(Entity entity) {
        this.setParentId(entity.getUniqueID());
    }

    @Override
    public boolean isEntityEqual(net.minecraft.entity.Entity entity) {
        return this == entity || this.getParent() == entity;
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    public EnumHandSide getPrimaryHand() {
        return EnumHandSide.RIGHT;
    }

    public void collideWithNearbyEntities() {
        AxisAlignedBB box = this.getEntityBoundingBox().grow(0.20000000298023224D, 0.0D, 0.20000000298023224D);
        List<Entity> entities = this.world.getEntitiesWithinAABBExcludingEntity(this, box);
        Entity parent = this.getParent();
        if (parent != null) {
            for (Entity entity : entities) {
                if (entity != parent && !(entity instanceof EntityBoneSerpentPart) && entity.canBePushed()) {
                    entity.applyEntityCollision(parent);
                }
            }
        }
    }

    @Override
    public boolean processInitialInteract(EntityPlayer player, EnumHand hand) {
        Entity parent = getParent();
        return parent != null && parent.processInitialInteract(player, hand);
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float damage) {
        Entity parent = getParent();
        boolean prev = parent != null && parent.attackEntityFrom(source, damage * this.damageMultiplier);
        if (prev && !world.isRemote) {
            AlexsMobs.sendMSGToAll(new MessageHurtMultipart(this.getEntityId(), parent.getEntityId(), damage * this.damageMultiplier));
        }
        return prev;
    }

    @Override
    public Iterable<ItemStack> getArmorInventoryList() {
        return ImmutableList.of();
    }

    @Override
    public ItemStack getItemStackFromSlot(EntityEquipmentSlot slotIn) {
        return ItemStack.EMPTY;
    }

    @Override
    public void setItemStackToSlot(EntityEquipmentSlot slotIn, ItemStack stack) {
    }

    public boolean isTail() {
        return this.dataManager.get(TAIL);
    }

    public void setTail(boolean tail) {
        this.dataManager.set(TAIL, tail);
    }

    public int getBodyIndex() {
        return this.dataManager.get(BODYINDEX);
    }

    public void setBodyIndex(int index) {
        this.dataManager.set(BODYINDEX, index);
    }

    public boolean shouldNotExist() {
        Entity parent = getParent();
        return parent == null || !parent.isEntityAlive();
    }

    @Override
    public void onAttackedFromServer(EntityLivingBase parent, float damage) {
        if (parent.deathTime > 0) {
            this.deathTime = parent.deathTime;
        }
        if (parent.hurtTime > 0) {
            this.hurtTime = parent.hurtTime;
        }
    }

}
