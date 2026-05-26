package com.github.alexthe666.alexsmobs.entity;

import com.github.alexthe666.alexsmobs.AlexsMobs;
import com.github.alexthe666.alexsmobs.client.particle.AMParticleRegistry;
import com.github.alexthe666.alexsmobs.message.MessageHurtMultipart;
import com.github.alexthe666.alexsmobs.misc.AMAdvancementTriggerRegistry;
import com.google.common.collect.ImmutableList;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;
import com.google.common.base.Optional;
import java.util.UUID;

public class EntityVoidWormPart extends EntityLivingBase implements IHurtableMultipart {

    private static final DataParameter<Boolean> TAIL = EntityDataManager.createKey(EntityVoidWormPart.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> BODYINDEX = EntityDataManager.createKey(EntityVoidWormPart.class, DataSerializers.VARINT);
    private static final DataParameter<Float> WORM_SCALE = EntityDataManager.createKey(EntityVoidWormPart.class, DataSerializers.FLOAT);
    private static final DataParameter<Float> WORM_YAW = EntityDataManager.createKey(EntityVoidWormPart.class, DataSerializers.FLOAT);
    private static final DataParameter<Float> WORM_ANGLE = EntityDataManager.createKey(EntityVoidWormPart.class, DataSerializers.FLOAT);
    private static final DataParameter<Optional<UUID>> PARENT_UUID = EntityDataManager.createKey(EntityVoidWormPart.class, DataSerializers.OPTIONAL_UNIQUE_ID);
    private static final DataParameter<Optional<UUID>> CHILD_UUID = EntityDataManager.createKey(EntityVoidWormPart.class, DataSerializers.OPTIONAL_UNIQUE_ID);
    private static final DataParameter<Integer> PORTAL_TICKS = EntityDataManager.createKey(EntityVoidWormPart.class, DataSerializers.VARINT);
    public float prevWormAngle;
    protected float radius;
    protected float angleYaw;
    protected float offsetY;
    protected float damageMultiplier = 1;
    private float prevWormYaw = 0;
    private Vec3d teleportPos = null;
    private Vec3d enterPos = null;
    private boolean doesParentControlPos = false;

    public EntityVoidWormPart(World world) {
        super(world);
        this.updatePartSize();
    }

    public EntityVoidWormPart(World world, EntityLivingBase parent, float radius, float angleYaw, float offsetY) {
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
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(30.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.15D);
    }

    public void applyEntityCollision(Entity entityIn) {
    }

    public void onKillCommand() {
        this.setDead();
    }

    public void updatePartSize() {
        float rs = this.getRenderScale();
        if (this.isTail()) {
            this.setSize(1.6F * rs, 2.0F * rs);
        } else {
            this.setSize(1.2F * rs, 1.95F * rs);
        }
    }

    public float getWormScale() {
        return this.dataManager.get(WORM_SCALE);
    }

    public void setWormScale(float scale) {
        this.dataManager.set(WORM_SCALE, scale);
        this.updatePartSize();
    }

    public float getRenderScale() {
        return this.getWormScale() + 0.5F;
    }

    @Override
    public boolean startRiding(Entity entityIn) {
        if (!(entityIn instanceof EntityMinecart) && !(entityIn instanceof EntityBoat)) {
            return super.startRiding(entityIn);
        }
        return false;
    }

    @Override
    public boolean isEntityInvulnerable(DamageSource source) {
        return source == DamageSource.FALL || source == DamageSource.DROWN || source == DamageSource.OUT_OF_WORLD || source == DamageSource.IN_WALL || source == DamageSource.FALLING_BLOCK || source == DamageSource.LAVA || source.isFireDamage() || super.isEntityInvulnerable(source);
    }

    public Entity getEntity() {
        return this;
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        if (this.getParentId() != null) {
            compound.setUniqueId("ParentUUID", this.getParentId());
        }
        if (this.getChildId() != null) {
            compound.setUniqueId("ChildUUID", this.getChildId());
        }
        compound.setBoolean("TailPart", this.isTail());
        compound.setInteger("BodyIndex", this.getBodyIndex());
        compound.setInteger("PortalTicks", this.getPortalTicks());
        compound.setFloat("PartAngle", this.angleYaw);
        compound.setFloat("WormScale", this.getWormScale());
        compound.setFloat("PartRadius", this.radius);
        compound.setFloat("PartYOffset", this.offsetY);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        if (compound.hasUniqueId("ParentUUID")) {
            this.setParentId(compound.getUniqueId("ParentUUID"));
        }
        if (compound.hasUniqueId("ChildUUID")) {
            this.setChildId(compound.getUniqueId("ChildUUID"));
        }
        this.setTail(compound.getBoolean("TailPart"));
        this.setBodyIndex(compound.getInteger("BodyIndex"));
        this.setPortalTicks(compound.getInteger("PortalTicks"));
        this.angleYaw = compound.getFloat("PartAngle");
        this.setWormScale(compound.getFloat("WormScale"));
        this.radius = compound.getFloat("PartRadius");
        this.offsetY = compound.getFloat("PartYOffset");
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(PARENT_UUID, Optional.absent());
        this.dataManager.register(CHILD_UUID, Optional.absent());
        this.dataManager.register(TAIL, Boolean.FALSE);
        this.dataManager.register(BODYINDEX, 0);
        this.dataManager.register(WORM_SCALE, 1F);
        this.dataManager.register(WORM_YAW, 0F);
        this.dataManager.register(WORM_ANGLE, 0F);
        this.dataManager.register(PORTAL_TICKS, 0);
    }

    @Nullable
    public UUID getParentId() {
        return this.dataManager.get(PARENT_UUID).orNull();
    }

    public void setParentId(@Nullable UUID uniqueId) {
        this.dataManager.set(PARENT_UUID, Optional.fromNullable(uniqueId));
    }

    @Nullable
    public UUID getChildId() {
        return this.dataManager.get(CHILD_UUID).orNull();
    }

    public void setChildId(@Nullable UUID uniqueId) {
        this.dataManager.set(CHILD_UUID, Optional.fromNullable(uniqueId));
    }

    public void setInitialPartPos(Entity parent) {
        this.setPosition(
                parent.prevPosX + this.radius * Math.cos(parent.rotationYaw * (Math.PI / 180.0F) + this.angleYaw),
                parent.prevPosY + this.offsetY,
                parent.prevPosZ + this.radius * Math.sin(parent.rotationYaw * (Math.PI / 180.0F) + this.angleYaw));
    }

    public float getWormAngle() {
        return this.dataManager.get(WORM_ANGLE);
    }

    public void setWormAngle(float progress) {
        this.dataManager.set(WORM_ANGLE, progress);
    }

    public int getPortalTicks() {
        return this.dataManager.get(PORTAL_TICKS);
    }

    public void setPortalTicks(int ticks) {
        this.dataManager.set(PORTAL_TICKS, ticks);
    }

    @Override
    public void onUpdate() {
        this.inPortal = false;
        this.prevWormAngle = this.getWormAngle();
        this.prevWormYaw = this.dataManager.get(WORM_YAW);
        this.motionX = 0;
        this.motionY = 0;
        this.motionZ = 0;
        this.radius = 1.0F + (this.getWormScale() * (this.isTail() ? 0.65F : 0.3F)) + (this.getBodyIndex() == 0 ? 0.8F : 0);
        if (this.ticksExisted > 3) {
            Entity parent = this.getParent();
            this.updatePartSize();
            if (parent != null && !this.world.isRemote) {
                this.setNoGravity(true);
                Vec3d parentVec = new Vec3d(parent.posX - parent.prevPosX, parent.posY - parent.prevPosY, parent.posZ - parent.prevPosZ);
                double restrictRadius = MathHelper.clamp((float) (this.radius - parentVec.lengthVector() * parentVec.lengthVector() * 0.25F), this.radius * 0.5F, this.radius);
                if (parent instanceof EntityVoidWorm) {
                    restrictRadius *= (this.isTail() ? 0.8F : 0.4F);
                }
                double x = parent.posX + restrictRadius * Math.cos(parent.rotationYaw * (Math.PI / 180.0F) + this.angleYaw);
                double yStretch = Math.abs(parent.posY - parent.prevPosY) > this.width ? parent.posY : parent.prevPosY;
                double y = yStretch + this.offsetY * this.getWormScale();
                double z = parent.posZ + restrictRadius * Math.sin(parent.rotationYaw * (Math.PI / 180.0F) + this.angleYaw);

                double d0 = parent.prevPosX - this.posX;
                double d1 = parent.prevPosY - this.posY;
                double d2 = parent.prevPosZ - this.posZ;
                float yaw = (float) (MathHelper.atan2(d2, d0) * (double) (180F / (float) Math.PI)) - 90.0F;
                float pitch = parent.rotationPitch;
                if (this.getPortalTicks() <= 1 && !this.doesParentControlPos) {
                    float f2 = -((float) (MathHelper.atan2(d1, MathHelper.sqrt(d0 * d0 + d2 * d2)) * (double) (180F / (float) Math.PI)));
                    this.setPosition(x, y, z);
                    this.rotationPitch = this.limitAngle(this.rotationPitch, f2, 5.0F);
                    this.rotationYaw = yaw;
                    this.dataManager.set(WORM_YAW, this.rotationYaw);
                }
                this.velocityChanged = true;
                this.rotationYawHead = this.rotationYaw;
                this.renderYawOffset = pitch;
                if (parent instanceof EntityLivingBase) {
                    EntityLivingBase livingParent = (EntityLivingBase) parent;
                    if (!this.world.isRemote && (livingParent.hurtTime > 0 || livingParent.deathTime > 0)) {
                        AlexsMobs.sendMSGToAll(new MessageHurtMultipart(this.getEntityId(), parent.getEntityId(), 0));
                        this.hurtTime = livingParent.hurtTime;
                        this.deathTime = livingParent.deathTime;
                    }
                }
                this.collideWithNearbyEntities();
                if (parent.isDead && !this.world.isRemote) {
                    this.setDead();
                }
                if (parent instanceof EntityVoidWorm) {
                    this.setWormAngle(((EntityVoidWorm) parent).prevWormAngle);
                } else if (parent instanceof EntityVoidWormPart) {
                    this.setWormAngle(((EntityVoidWormPart) parent).prevWormAngle);
                }
            } else if (this.ticksExisted > 20 && !this.world.isRemote) {
                this.setDead();
            }
        }
        if (this.ticksExisted % 400 == 0) {
            this.heal(1.0F);
        }
        super.onUpdate();
        if (this.doesParentControlPos && this.enterPos != null) {
            this.setPositionAndUpdate(this.enterPos.x, this.enterPos.y, this.enterPos.z);
        }
        if (this.getPortalTicks() > 0) {
            this.setPortalTicks(this.getPortalTicks() - 1);
            if (this.getPortalTicks() <= 5 && this.teleportPos != null) {
                Vec3d vec = this.teleportPos;
                this.setPositionAndUpdate(vec.x, vec.y, vec.z);
                this.lastTickPosX = vec.x;
                this.lastTickPosY = vec.y;
                this.lastTickPosZ = vec.z;
                if (this.getPortalTicks() == 5 && this.getChild() instanceof EntityVoidWormPart) {
                    ((EntityVoidWormPart) this.getChild()).teleportTo(this.enterPos, this.teleportPos);
                }
                this.teleportPos = null;
            } else if (this.getPortalTicks() > 5 && this.enterPos != null) {
                this.setPositionAndUpdate(this.enterPos.x, this.enterPos.y, this.enterPos.z);
            }
            if (this.getPortalTicks() == 0) {
                this.doesParentControlPos = false;
            }
        }
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

    @Override
    protected void onDeathUpdate() {
        ++this.deathTime;
        if (this.deathTime == 20) {
            this.setDead();
            if (this.world.isRemote) {
                for (int i = 0; i < 30; ++i) {
                    double d0 = this.rand.nextGaussian() * 0.02D;
                    double d1 = this.rand.nextGaussian() * 0.02D;
                    double d2 = this.rand.nextGaussian() * 0.02D;
                    AMParticleRegistry.spawnParticle(this.world, AMParticleRegistry.WORM_PORTAL, this.posX + (this.rand.nextDouble() - 0.5D) * 2.0D, this.posY + this.rand.nextDouble() * this.height, this.posZ + (this.rand.nextDouble() - 0.5D) * 2.0D, d0, d1, d2);
                }
            }
        }
    }

    @Override
    public void onDeath(DamageSource cause) {
        EntityVoidWorm worm = this.getWorm();
        if (worm != null) {
            int segments = Math.max(worm.getSegmentCount() / 2 - 1, 1);
            worm.setSegmentCount(segments);
            if (this.getChild() instanceof EntityVoidWormPart) {
                EntityVoidWormPart segment = (EntityVoidWormPart) this.getChild();
                EntityVoidWorm worm2 = new EntityVoidWorm(this.world);
                worm2.copyLocationAndAnglesFrom(this);
                segment.copyLocationAndAnglesFrom(this);
                worm2.setChildId(segment.getUniqueID());
                worm2.setSegmentCount(segments);
                segment.setParent(worm2);
                if (!this.world.isRemote) {
                    this.world.spawnEntity(worm2);
                }
                worm2.setSplitter(true);
                worm2.setMaxHealth(worm.getMaxHealth() / 2F, true);
                worm2.setSplitFromUuid(worm.getUniqueID());
                worm2.setWormSpeed((float) MathHelper.clamp(worm.getWormSpeed() * 0.8, 0.4F, 1F));
                worm2.resetWormScales();
                if (!this.world.isRemote) {
                    if (cause != null && cause.getTrueSource() instanceof EntityPlayerMP) {
                        AMAdvancementTriggerRegistry.VOID_WORM_SPLIT.trigger((EntityPlayerMP) cause.getTrueSource());
                    }
                }
            }
            worm.resetWormScales();
        }
    }

    public boolean isOnSameTeam(Entity entityIn) {
        EntityVoidWorm worm = this.getWorm();
        return super.isOnSameTeam(entityIn) || worm != null && worm.isOnSameTeam(entityIn);
    }

    public EntityVoidWorm getWorm() {
        Entity parent = this.getParent();
        while (parent instanceof EntityVoidWormPart) {
            parent = ((EntityVoidWormPart) parent).getParent();
        }
        if (parent instanceof EntityVoidWorm) {
            return (EntityVoidWorm) parent;
        }
        return null;
    }

    @Nullable
    public Entity getChild() {
        UUID id = this.getChildId();
        if (id != null && !this.world.isRemote && this.world instanceof WorldServer) {
            return ((WorldServer) this.world).getEntityFromUuid(id);
        }
        return null;
    }

    @Nullable
    public Entity getParent() {
        UUID id = this.getParentId();
        if (id != null && !this.world.isRemote && this.world instanceof WorldServer) {
            return ((WorldServer) this.world).getEntityFromUuid(id);
        }
        return null;
    }

    public void setParent(Entity entity) {
        this.setParentId(entity.getUniqueID());
    }

    @Override
    public boolean isEntityEqual(Entity entity) {
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
        List<Entity> entities = this.world.getEntitiesWithinAABBExcludingEntity(this, this.getEntityBoundingBox().grow(0.20000000298023224D, 0.0D, 0.20000000298023224D));
        Entity parent = this.getParent();
        if (parent != null) {
            for (Entity entity : entities) {
                if (!entity.isEntityEqual(parent) && !(entity instanceof EntityVoidWormPart) && entity.canBePushed()) {
                    entity.applyEntityCollision(parent);
                }
            }
        }
    }

    public boolean processInitialInteract(EntityPlayer player, EnumHand hand) {
        Entity parent = this.getParent();
        return parent != null && parent.processInitialInteract(player, hand);
    }

    public boolean isHurt() {
        return this.getHealth() <= this.getHealthThreshold();
    }

    public double getHealthThreshold() {
        return 5D;
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float damage) {
        if (super.attackEntityFrom(source, damage)) {
            EntityVoidWorm worm = this.getWorm();
            if (worm != null) {
                worm.playHurtSoundWorm(source);
            }
            return true;
        }
        return false;
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
        this.updatePartSize();
    }

    public int getBodyIndex() {
        return this.dataManager.get(BODYINDEX);
    }

    public void setBodyIndex(int index) {
        this.dataManager.set(BODYINDEX, index);
    }

    public boolean shouldNotExist() {
        Entity parent = this.getParent();
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

    public boolean shouldContinuePersisting() {
        return this.isAddedToWorld() || this.isDead;
    }

    public float getWormYaw(float partialTicks) {
        return partialTicks == 0 ? this.dataManager.get(WORM_YAW) : this.prevWormYaw + (this.dataManager.get(WORM_YAW) - this.prevWormYaw) * partialTicks;
    }

    public void teleportTo(Vec3d enterPos, Vec3d to) {
        this.setPortalTicks(10);
        this.teleportPos = to;
        this.enterPos = enterPos;
        EntityVoidWorm worm = this.getWorm();
        if (worm != null) {
            if (this.getChild() == null) {
                worm.fullyThrough = true;
            }
        }
    }
}
