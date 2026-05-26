package com.github.alexthe666.alexsmobs.entity;

import com.github.alexthe666.alexsmobs.AlexsMobs;
import com.github.alexthe666.alexsmobs.entity.util.AnacondaPartIndex;
import com.github.alexthe666.alexsmobs.message.MessageHurtMultipart;
import com.google.common.collect.ImmutableList;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
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
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import javax.annotation.Nullable;
import java.util.List;
import com.google.common.base.Optional;
import java.util.UUID;

public class EntityAnacondaPart extends EntityLivingBase implements IHurtableMultipart {

    private static final DataParameter<Integer> BODYINDEX = EntityDataManager.createKey(EntityAnacondaPart.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> BODY_TYPE = EntityDataManager.createKey(EntityAnacondaPart.class, DataSerializers.VARINT);
    private static final DataParameter<Optional<UUID>> CHILD_UUID = EntityDataManager.createKey(EntityAnacondaPart.class, DataSerializers.OPTIONAL_UNIQUE_ID);
    private static final DataParameter<Optional<UUID>> PARENT_UUID = EntityDataManager.createKey(EntityAnacondaPart.class, DataSerializers.OPTIONAL_UNIQUE_ID);
    private static final DataParameter<Float> SWELL = EntityDataManager.createKey(EntityAnacondaPart.class, DataSerializers.FLOAT);
    private static final DataParameter<Boolean> YELLOW = EntityDataManager.createKey(EntityAnacondaPart.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> SHEDDING = EntityDataManager.createKey(EntityAnacondaPart.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> BABY = EntityDataManager.createKey(EntityAnacondaPart.class, DataSerializers.BOOLEAN);
    private float strangleProgess;
    private float prevSwell;
    private float prevStrangleProgess;
    private int headEntityId = -1;
    private double prevHeight = 0;

    public EntityAnacondaPart(World world) {
        super(world);
        this.setSize(0.8F, 0.8F);
    }

    public EntityAnacondaPart(World world, EntityLivingBase parent) {
        this(world);
        this.setParent(parent);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        AMEntityRegistry.registerAttributeIfAbsent(this, SharedMonsterAttributes.MOVEMENT_SPEED);
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(10.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.15D);
    }

    @Override
    public EnumActionResult applyPlayerInteraction(EntityPlayer player, Vec3d vec, EnumHand hand) {
        Entity parent = this.getParent();
        if (parent != null) {
            return parent.applyPlayerInteraction(player, vec, hand);
        }
        return EnumActionResult.PASS;
    }

    @Override
    public boolean isEntityInvulnerable(DamageSource source) {
        return source == DamageSource.IN_WALL || super.isEntityInvulnerable(source);
    }

    @Override
    public boolean hasNoGravity() {
        return false;
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();

        prevStrangleProgess = strangleProgess;
        prevSwell = this.getSwell();
        this.inPortal = false;
        this.motionX = 0;
        this.motionY = 0;
        this.motionZ = 0;
        if (this.ticksExisted > 1) {
            Entity parent = getParent();
            refreshDimensions();
            if (!this.world.isRemote) {
                if (parent == null) {
                    this.setDead();
                }
                if (parent != null) {
                    if (parent instanceof EntityLivingBase) {
                        EntityLivingBase livingEntityParent = (EntityLivingBase) parent;
                        if (livingEntityParent.hurtTime > 0 || livingEntityParent.deathTime > 0) {
                            AlexsMobs.sendMSGToAll(new MessageHurtMultipart(this.getEntityId(), parent.getEntityId(), 0));
                            this.hurtTime = livingEntityParent.hurtTime;
                            this.deathTime = livingEntityParent.deathTime;
                        }
                    }
                    if (parent.isDead) {
                        this.setDead();
                    }
                } else if (ticksExisted > 20) {
                    setDead();
                }
                if (this.getSwell() > 0) {
                    final float swellInc = 0.25F;
                    if (parent instanceof EntityAnaconda || parent instanceof EntityAnacondaPart && ((EntityAnacondaPart) parent).getSwell() == 0) {
                        if (this.getChild() != null) {
                            EntityAnacondaPart child = (EntityAnacondaPart) this.getChild();
                            if (child.getPartType() == AnacondaPartIndex.TAIL) {
                                if (this.getSwell() == swellInc) {
                                    this.feedAnaconda();
                                }
                            } else {
                                child.setSwell(child.getSwell() + swellInc);
                            }
                        }
                        this.setSwell(this.getSwell() - swellInc);
                    }
                }
                this.pushEntities();
            }
        }
    }

    private void feedAnaconda() {
        Entity e = this.getParent();
        while (e instanceof EntityAnacondaPart) {
            e = ((EntityAnacondaPart) e).getParent();
        }

        if (e instanceof EntityAnaconda) {
            ((EntityAnaconda) e).feed();
        }
    }


    protected Vec3d calcOffsetVec(float offsetZ, float xRot, float yRot) {
        return new Vec3d(0, 0, offsetZ).rotatePitch(xRot * 0.017453292F).rotateYaw(-yRot * 0.017453292F);
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

    public Vec3d tickMultipartPosition(int headId, AnacondaPartIndex parentIndex, Vec3d parentPosition, float parentXRot, float parentYRot, float ourYRot, boolean doHeight) {
        Vec3d parentButt = parentPosition.add(calcOffsetVec(-parentIndex.getBackOffset() * this.getScale(), parentXRot, parentYRot));
        Vec3d ourButt = parentButt.add(calcOffsetVec((-this.getPartType().getBackOffset() - 0.5F * this.width) * this.getScale(), this.rotationPitch, ourYRot));
        Vec3d avg = new Vec3d((parentButt.x + ourButt.x) / 2F, (parentButt.y + ourButt.y) / 2F, (parentButt.z + ourButt.z) / 2F);
        double d0 = parentButt.x - ourButt.x;
        double d2 = parentButt.z - ourButt.z;
        double d3 = Math.sqrt(d0 * d0 + d2 * d2);
        double hgt = doHeight ? (getLowPartHeight(parentButt.x, parentButt.y, parentButt.z) + getHighPartHeight(ourButt.x, ourButt.y, ourButt.z)) : 0;
        if (Math.abs(hgt - prevHeight) > 0.2F) {
            prevHeight = hgt;
        }
        double partYDest = MathHelper.clamp(this.getScale() * prevHeight, -0.6F, 0.6F);
        float f = (float) (MathHelper.atan2(d2, d0) * 57.2957763671875D) - 90.0F;
        float rawAngle = MathHelper.wrapDegrees((float) (-(MathHelper.atan2(partYDest, d3) * (180F / (float) Math.PI))));
        float f2 = this.limitAngle(this.rotationPitch, rawAngle, 10F);
        this.rotationPitch = f2;
        this.rotationYaw = f;
        this.rotationYawHead = f;
        this.setPosition(avg.x, avg.y, avg.z);
        headEntityId = headId;
        return avg;
    }

    public double getLowPartHeight(double x, double yIn, double z) {
        if (isFluidAt(x, yIn, z)) {
            return 0.0D;
        }

        double checkAt = 0D;
        while (checkAt > -3D && !isOpaqueBlockAt(x, yIn + checkAt, z)) {
            checkAt -= 0.2D;
        }

        return checkAt;
    }

    public double getHighPartHeight(double x, double yIn, double z) {
        if (isFluidAt(x, yIn, z)) {
            return 0.0D;
        }

        double checkAt = 0D;
        while (checkAt <= 3D) {
            if (isOpaqueBlockAt(x, yIn + checkAt, z)) {
                checkAt += 0.2D;
            } else {
                break;
            }
        }

        return checkAt;
    }

    public boolean isOpaqueBlockAt(double x, double y, double z) {
        if (this.noClip) {
            return false;
        }
        BlockPos blockpos = new BlockPos(x, y, z);
        IBlockState state = this.world.getBlockState(blockpos);
        return !state.getMaterial().isReplaceable() && state.causesSuffocation();
    }

    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }

    @Override
    public boolean isPushedByWater() {
        return false;
    }

    public boolean isFluidAt(double x, double y, double z) {
        if (this.noClip) {
            return false;
        }
        return this.world.getBlockState(new BlockPos(x, y, z)).getMaterial() == Material.WATER;
    }

    public boolean hurtHeadId(DamageSource source, float f) {
        if (headEntityId != -1) {
            Entity e = world.getEntityByID(headEntityId);
            if (e instanceof EntityAnaconda) {
                return e.attackEntityFrom(source, f);
            }
        }
        return false;
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float damage) {
        return hurtHeadId(source, damage);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(CHILD_UUID, Optional.absent());
        this.dataManager.register(PARENT_UUID, Optional.absent());
        this.dataManager.register(BODYINDEX, 0);
        this.dataManager.register(BODY_TYPE, AnacondaPartIndex.NECK.ordinal());
        this.dataManager.register(SWELL, 0F);
        this.dataManager.register(YELLOW, false);
        this.dataManager.register(SHEDDING, false);
        this.dataManager.register(BABY, false);
    }

    public void pushEntities() {
        AxisAlignedBB box = this.getEntityBoundingBox().grow(0.2D, 0.0D, 0.2D);
        List<Entity> entities = this.world.getEntitiesWithinAABBExcludingEntity(this, box);
        Entity parent = this.getParent();
        if (parent != null) {
            for (Entity entity : entities) {
                if (!entity.isEntityEqual(parent) && !(entity instanceof EntityAnacondaPart) && !(entity instanceof EntityAnaconda) && entity.canBePushed()) {
                    entity.applyEntityCollision(parent);
                }
            }
        }
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
    public EnumHandSide getPrimaryHand() {
        return EnumHandSide.RIGHT;
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

    @Nullable
    public Entity getParent() {
        if (!this.world.isRemote) {
            UUID id = getParentId();
            if (id != null && this.world instanceof WorldServer) {
                return ((WorldServer) this.world).getEntityFromUuid(id);
            }
        }

        return null;
    }

    public void setParent(Entity entity) {
        this.setParentId(entity.getUniqueID());
    }

    @Nullable
    public UUID getParentId() {
        return this.dataManager.get(PARENT_UUID).orNull();
    }

    public void setParentId(@Nullable UUID uniqueId) {
        this.dataManager.set(PARENT_UUID, Optional.fromNullable(uniqueId));
    }

    @Nullable
    public Entity getChild() {
        if (!this.world.isRemote) {
            UUID id = getChildId();
            if (id != null && this.world instanceof WorldServer) {
                return ((WorldServer) this.world).getEntityFromUuid(id);
            }
        }

        return null;
    }

    @Nullable
    public UUID getChildId() {
        return this.dataManager.get(CHILD_UUID).orNull();
    }

    public void setChildId(@Nullable UUID uniqueId) {
        this.dataManager.set(CHILD_UUID, Optional.fromNullable(uniqueId));
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
        compound.setInteger("BodyModel", getPartType().ordinal());
        compound.setInteger("BodyIndex", getBodyIndex());
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
        this.setPartType(AnacondaPartIndex.fromOrdinal(compound.getInteger("BodyModel")));
        this.setBodyIndex(compound.getInteger("BodyIndex"));
    }

    @Override
    public boolean isEntityEqual(Entity entity) {
        return this == entity || this.getParent() == entity;
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Nullable
    @Override
    public ItemStack getPickedResult(RayTraceResult target) {
        Entity parent = this.getParent();
        return parent != null ? parent.getPickedResult(target) : ItemStack.EMPTY;
    }

    public int getBodyIndex() {
        return this.dataManager.get(BODYINDEX);
    }

    public void setBodyIndex(int index) {
        this.dataManager.set(BODYINDEX, index);
    }

    public AnacondaPartIndex getPartType() {
        return AnacondaPartIndex.fromOrdinal(this.dataManager.get(BODY_TYPE));
    }

    public void setPartType(AnacondaPartIndex index) {
        this.dataManager.set(BODY_TYPE, index.ordinal());
    }

    public void setSwell(float f) {
        this.dataManager.set(SWELL, f);
    }

    public float getSwell() {
        return Math.min(this.dataManager.get(SWELL), 5);
    }

    public float getSwellLerp(float partialTick) {
        return this.prevSwell + (Math.max(this.getSwell(), 0) - this.prevSwell) * partialTick;
    }

    public void setStrangleProgress(float f) {
        this.strangleProgess = f;
    }

    public float getStrangleProgress(float partialTick) {
        return this.prevStrangleProgess + (this.strangleProgess - this.prevStrangleProgess) * partialTick;
    }

    public void copyDataFrom(EntityAnaconda anaconda) {
        this.dataManager.set(YELLOW, anaconda.isYellow());
        this.dataManager.set(SHEDDING, anaconda.isShedding());
        this.dataManager.set(BABY, anaconda.isChild());
    }

    public boolean isYellow() {
        return this.dataManager.get(YELLOW);
    }

    public boolean isShedding() {
        return this.dataManager.get(SHEDDING);
    }

    @Override
    public boolean isChild() {
        return this.dataManager.get(BABY);
    }

    public float getScale() {
        return this.isChild() ? 0.75F : 1.0F;
    }

    public void refreshDimensions() {
        float scale = this.getScale();
        this.setSize(0.8F * scale, 0.8F * scale);
    }

    @Override
    public boolean startRiding(Entity entityIn) {
        if (!(entityIn instanceof EntityMinecart) && !(entityIn instanceof EntityBoat)) {
            return super.startRiding(entityIn);
        }
        return false;
    }

    @Override
    public void applyEntityCollision(Entity entityIn) {
    }
}
