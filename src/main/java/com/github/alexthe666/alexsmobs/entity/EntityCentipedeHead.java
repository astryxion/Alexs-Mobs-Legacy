package com.github.alexthe666.alexsmobs.entity;
import com.github.alexthe666.alexsmobs.misc.AMLootTables;

import com.github.alexthe666.alexsmobs.config.AMConfig;
import com.github.alexthe666.alexsmobs.misc.AMSoundRegistry;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import javax.annotation.Nullable;
import net.minecraft.util.ResourceLocation;
import java.util.List;
import com.google.common.base.Optional;
import java.util.UUID;

public class EntityCentipedeHead extends EntityMob {

    private static final DataParameter<Optional<UUID>> CHILD_UUID = EntityDataManager.createKey(EntityCentipedeHead.class, DataSerializers.OPTIONAL_UNIQUE_ID);

    public EntityCentipedeHead(World worldIn) {
        super(worldIn);
        this.stepHeight = 3.0F;
        this.setSize(1.1F, 0.9F);
    }

    @Override
    public boolean getCanSpawnHere() {
        return AMEntityRegistry.rollSpawn(AMConfig.caveCentipedeSpawnRolls, this.getRNG(), AMEntityRegistry.AMSpawnReason.OTHER) && super.getCanSpawnHere();
    }

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(1, new EntityAIAttackMelee(this, 1.0D, false));
        this.tasks.addTask(2, new EntityAIWander(this, 1.0D));
        this.tasks.addTask(3, new EntityAIWatchClosest(this, EntityPlayer.class, 6.0F));
        this.tasks.addTask(4, new EntityAILookIdle(this));
        this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, true));
        this.targetTasks.addTask(2, new EntityAINearestAttackableTarget<>(this, EntityPlayer.class, true));
        this.targetTasks.addTask(2, new EntityAINearestAttackableTarget<>(this, EntityVillager.class, true));
        this.targetTasks.addTask(2, new EntityAINearestAttackableTarget<>(this, EntityCockroach.class, true));
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return AMSoundRegistry.CENTIPEDE_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return AMSoundRegistry.CENTIPEDE_HURT;
    }
    @Override
    @Nullable
    protected ResourceLocation getLootTable() {
        return AMLootTables.CENTIPEDE_HEAD;
    }

    @Override
    public EnumCreatureAttribute getCreatureAttribute() {
        return EnumCreatureAttribute.ARTHROPOD;
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(35.0D);
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(32.0D);
        AMEntityRegistry.registerAttributeIfAbsent(this, SharedMonsterAttributes.ARMOR);
        this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(6.0D);
        AMEntityRegistry.registerAttributeIfAbsent(this, SharedMonsterAttributes.ATTACK_DAMAGE);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(8.0D);
        AMEntityRegistry.registerAttributeIfAbsent(this, SharedMonsterAttributes.KNOCKBACK_RESISTANCE);
        this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(0.5D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.22D);
    }

    protected void playStepSound(BlockPos pos, net.minecraft.block.Block blockIn) {
        this.playSound(AMSoundRegistry.CENTIPEDE_WALK, 1.0F, 1.0F);
    }

    public int getVerticalFaceSpeed() {
        return 1;
    }

    public int getHorizontalFaceSpeed() {
        return 1;
    }

    public int getFaceRotSpeed() {
        return 1;
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(CHILD_UUID, Optional.absent());
    }

    @Override
    public boolean attackEntityAsMob(Entity entityIn) {
        if (super.attackEntityAsMob(entityIn)) {
            if (entityIn instanceof EntityLivingBase) {
                int i = 3;
                if (this.world.getDifficulty() == EnumDifficulty.NORMAL) {
                    i = 10;
                } else if (this.world.getDifficulty() == EnumDifficulty.HARD) {
                    i = 20;
                }
                if (i > 0) {
                    ((EntityLivingBase) entityIn).addPotionEffect(new PotionEffect(MobEffects.POISON, i * 20, 1));
                }
            }
            this.playSound(AMSoundRegistry.CENTIPEDE_ATTACK, this.getSoundVolume(), this.getSoundPitch());
            return true;
        } else {
            return false;
        }
    }

    @Nullable
    public UUID getChildId() {
        return this.dataManager.get(CHILD_UUID).orNull();
    }

    public void setChildId(@Nullable UUID uniqueId) {
        this.dataManager.set(CHILD_UUID, Optional.fromNullable(uniqueId));
    }

    @Nullable
    public Entity getChild() {
        UUID id = getChildId();
        if (id != null && !world.isRemote && world instanceof WorldServer) {
            return ((WorldServer) world).getEntityFromUuid(id);
        }
        return null;
    }

    @Override
    public void collideWithNearbyEntities() {
        AxisAlignedBB box = this.getEntityBoundingBox().grow(0.20000000298023224D, 0.0D, 0.20000000298023224D);
        List<Entity> entities = this.world.getEntitiesWithinAABBExcludingEntity(this, box);
        for (Entity entity : entities) {
            if (!(entity instanceof EntityCentipedeBody) && entity.canBePushed()) {
                entity.applyEntityCollision(this);
            }
        }
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        if (this.getChildId() != null) {
            compound.setUniqueId("ChildUUID", this.getChildId());
        }
    }

    @Override
    public boolean isEntityInvulnerable(DamageSource source) {
        return source == DamageSource.IN_WALL || source == DamageSource.FALLING_BLOCK || super.isEntityInvulnerable(source);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        if (compound.hasUniqueId("ChildUUID")) {
            this.setChildId(compound.getUniqueId("ChildUUID"));
        }
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        this.inPortal = false;
        this.rotationYaw = this.renderYawOffset;
        if (!world.isRemote) {
            Entity child = getChild();
            if (child == null) {
                EntityLivingBase partParent = this;
                int segments = 5 + getRNG().nextInt(3);
                for (int i = 0; i < segments; i++) {
                    EntityCentipedeBody part = createBody(partParent, i == segments - 1);
                    part.setParent(partParent);
                    part.setBodyIndex(i);
                    if (partParent == this) {
                        this.setChildId(part.getUniqueID());
                    }
                    part.setInitialPartPos(this, i + 1);
                    world.spawnEntity(part);
                    partParent = part;
                }
            }
        }
    }

    public EntityCentipedeBody createBody(EntityLivingBase parent, boolean tail) {
        return tail ? new EntityCentipedeTail(parent.world, parent, 0.84F, 180, 0) : new EntityCentipedeBody(parent.world, parent, 0.84F, 180, 0);
    }

    @Override
    public boolean canBeLeashedTo(EntityPlayer player) {
        return true;
    }
}
