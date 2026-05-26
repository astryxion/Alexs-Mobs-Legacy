package com.github.alexthe666.alexsmobs.entity;

import com.github.alexthe666.alexsmobs.AlexsMobs;
import com.github.alexthe666.alexsmobs.message.MessageHurtMultipart;
import com.google.common.collect.ImmutableList;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.monster.EntityMob;
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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import javax.annotation.Nullable;
import java.util.List;
import com.google.common.base.Optional;
import java.util.UUID;

public class EntityCentipedeBody extends EntityMob implements IHurtableMultipart {

    private static final DataParameter<Integer> BODYINDEX = EntityDataManager.createKey(EntityCentipedeBody.class, DataSerializers.VARINT);
    private static final DataParameter<Optional<UUID>> PARENT_UUID = EntityDataManager.createKey(EntityCentipedeBody.class, DataSerializers.OPTIONAL_UNIQUE_ID);
    protected float radius;
    protected float angleYaw;
    protected float offsetY;
    protected float damageMultiplier = 1;
    private float parentYaw = 0;

    public EntityCentipedeBody(World worldIn) {
        super(worldIn);
        this.setSize(0.84F, 0.84F);
    }

    public EntityCentipedeBody(World worldIn, EntityLivingBase parent, float radius, float angleYaw, float offsetY) {
        this(worldIn);
        this.setParent(parent);
        this.radius = radius;
        this.angleYaw = (angleYaw + 90.0F) * ((float) Math.PI / 180.0F);
        this.offsetY = offsetY;
    }

    @Override
    protected boolean canDespawn() {
        return this.getParent() == null && super.canDespawn();
    }

    @Override
    protected void initEntityAI() {
    }

    @Override
    public boolean isEntityInvulnerable(DamageSource source) {
        return source == DamageSource.IN_WALL || source == DamageSource.FALLING_BLOCK || super.isEntityInvulnerable(source);
    }

    @Override
    public EnumCreatureAttribute getCreatureAttribute() {
        return EnumCreatureAttribute.ARTHROPOD;
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        AMEntityRegistry.registerAttributeIfAbsent(this, SharedMonsterAttributes.FOLLOW_RANGE);
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(10.0D);
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(32.0D);
        AMEntityRegistry.registerAttributeIfAbsent(this, SharedMonsterAttributes.ARMOR);
        this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(6.0D);
        AMEntityRegistry.registerAttributeIfAbsent(this, SharedMonsterAttributes.ATTACK_DAMAGE);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(8.0D);
        AMEntityRegistry.registerAttributeIfAbsent(this, SharedMonsterAttributes.KNOCKBACK_RESISTANCE);
        this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(0.5D);
        AMEntityRegistry.registerAttributeIfAbsent(this, SharedMonsterAttributes.MOVEMENT_SPEED);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.25D);
    }

    @Override
    public void onUpdate() {
        this.inPortal = false;
        Entity parent = getParent();
        if (parent != null && !world.isRemote) {
            float f = this.getDistance(parent);
            this.setNoGravity(true);
            this.getLookHelper().setLookPositionWithEntity(parent, 10.0F, 10.0F);
            this.parentYaw = this.limitAngle(this.parentYaw, parent.prevRotationYaw, 5.0F);
            double ySet = parent.prevPosY;
            if (!world.getBlockState(new BlockPos(this.posX, ySet - 0.1D, this.posZ)).isFullBlock()) {
                ySet = parent.prevPosY - 0.2F;
            }
            if (this.isEntityInsideOpaqueBlock() || world.getBlockState(new BlockPos(this.posX, ySet, this.posZ)).isFullBlock()) {
                ySet = parent.prevPosY + 0.2F;
            }
            double yaw = parentYaw;
            double x = parent.prevPosX + this.radius * Math.cos(yaw * (Math.PI / 180.0D) + this.angleYaw);
            double z = parent.prevPosZ + this.radius * Math.sin(yaw * (Math.PI / 180.0D) + this.angleYaw);
            this.setPosition(x, ySet, z);
            double d0 = parent.posX - this.posX;
            double d1 = parent.posY - this.posY;
            double d2 = parent.posZ - this.posZ;
            float f2 = -((float) (MathHelper.atan2(d1, MathHelper.sqrt(d0 * d0 + d2 * d2)) * (double) (180F / (float) Math.PI)));
            this.rotationPitch = this.limitAngle(this.rotationPitch, f2, 5.0F);
            this.velocityChanged = true;
            this.rotationYaw = (float) parentYaw;
            this.rotationYawHead = this.rotationYaw;
            this.renderYawOffset = parent.prevRotationYaw;
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
        }
        if (parent == null && !world.isRemote && this.ticksExisted > 20) {
            this.setDead();
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

    public void setInitialPartPos(EntityLivingBase parent, int index) {
        double radAdd = this.radius * index;
        this.rotationYaw = parent.rotationYaw;
        this.renderYawOffset = parent.renderYawOffset;
        this.parentYaw = parent.rotationYaw;
        this.setPosition(
                parent.prevPosX + radAdd * Math.cos(parent.rotationYaw * (Math.PI / 180.0D) + this.angleYaw),
                parent.prevPosY + this.offsetY,
                parent.prevPosZ + radAdd * Math.sin(parent.rotationYaw * (Math.PI / 180.0D) + this.angleYaw));
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        if (this.getParentId() != null) {
            compound.setUniqueId("ParentUUID", this.getParentId());
        }
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
        this.setBodyIndex(compound.getInteger("BodyIndex"));
        this.angleYaw = compound.getFloat("PartAngle");
        this.radius = compound.getFloat("PartRadius");
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(PARENT_UUID, Optional.absent());
        this.dataManager.register(BODYINDEX, 0);
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
    public boolean attackEntityFrom(DamageSource source, float damage) {
        Entity parent = getParent();
        boolean prev = parent != null && parent.attackEntityFrom(source, damage * this.damageMultiplier);
        if (prev && !world.isRemote) {
            AlexsMobs.sendMSGToAll(new MessageHurtMultipart(this.getEntityId(), parent.getEntityId(), damage * this.damageMultiplier));
        }
        return prev;
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    public void collideWithNearbyEntities() {
        AxisAlignedBB box = this.getEntityBoundingBox().grow(0.20000000298023224D, 0.0D, 0.20000000298023224D);
        List<Entity> entities = this.world.getEntitiesWithinAABBExcludingEntity(this, box);
        Entity parent = this.getParent();
        if (parent != null) {
            for (Entity entity : entities) {
                if (entity != parent && !(entity instanceof EntityCentipedeBody) && entity.canBePushed()) {
                    entity.applyEntityCollision(parent);
                }
            }
        }
    }

    @Override
    public boolean startRiding(Entity entityIn) {
        if (!(entityIn instanceof EntityMinecart) && !(entityIn instanceof EntityBoat)) {
            return super.startRiding(entityIn);
        }
        return false;
    }

    public int getBodyIndex() {
        return this.dataManager.get(BODYINDEX);
    }

    public void setBodyIndex(int index) {
        this.dataManager.set(BODYINDEX, index);
    }

    @Nullable
    public UUID getParentId() {
        return this.dataManager.get(PARENT_UUID).orNull();
    }

    public void setParentId(@Nullable UUID uniqueId) {
        this.dataManager.set(PARENT_UUID, Optional.fromNullable(uniqueId));
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

    @Override
    public EnumHandSide getPrimaryHand() {
        return EnumHandSide.RIGHT;
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

    @Override
    public net.minecraft.util.EnumActionResult applyPlayerInteraction(EntityPlayer player, net.minecraft.util.math.Vec3d vec, EnumHand hand) {
        Entity parent = getParent();
        if (parent != null) {
            return parent.applyPlayerInteraction(player, vec, hand);
        }
        return net.minecraft.util.EnumActionResult.PASS;
    }
}
