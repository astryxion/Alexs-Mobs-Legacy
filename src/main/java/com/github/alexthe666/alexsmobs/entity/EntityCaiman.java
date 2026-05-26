package com.github.alexthe666.alexsmobs.entity;

import com.github.alexthe666.alexsmobs.config.AMConfig;
import com.github.alexthe666.alexsmobs.entity.ai.*;
import com.github.alexthe666.alexsmobs.misc.AMSoundRegistry;
import com.github.alexthe666.alexsmobs.misc.AMTagRegistry;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.passive.EntityWaterMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

public class EntityCaiman extends EntityTameable implements ISemiAquatic, IFollower {

    private static final DataParameter<Integer> COMMAND = EntityDataManager.createKey(EntityCaiman.class, DataSerializers.VARINT);
    private static final DataParameter<Boolean> SITTING = EntityDataManager.createKey(EntityCaiman.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> BELLOWING = EntityDataManager.createKey(EntityCaiman.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> HELD_MOB_ID = EntityDataManager.createKey(EntityCaiman.class, DataSerializers.VARINT);

    public float prevSitProgress;
    public float sitProgress;
    public float prevHoldProgress;
    public float holdProgress;
    public float prevSwimProgress;
    public float swimProgress;
    public float prevVibrateProgress;
    public float vibrateProgress;
    public int bellowCooldown = 100;
    public boolean tameAttackFlag = false;

    private int swimTimer = -1000;
    private boolean isLandNavigator;
    private boolean forcedSit;

    public EntityCaiman(World world) {
        super(world);
        this.setSize(1.4F, 0.55F);
        this.setPathPriority(PathNodeType.WATER, 0.0F);
        this.bellowCooldown = 100 + this.rand.nextInt(1000);
        switchNavigator(false);
    }

    public static boolean canCaimanSpawn(World world, BlockPos pos) {
        IBlockState blockstate = world.getBlockState(pos.down());
        return AMTagRegistry.blockInTag(AMTagRegistry.CAIMAN_SPAWNS, blockstate.getBlock());
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(COMMAND, 0);
        this.dataManager.register(BELLOWING, false);
        this.dataManager.register(SITTING, false);
        this.dataManager.register(HELD_MOB_ID, -1);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        AMEntityRegistry.registerAttributeIfAbsent(this, SharedMonsterAttributes.ARMOR);
        AMEntityRegistry.registerAttributeIfAbsent(this, SharedMonsterAttributes.ATTACK_DAMAGE);
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(20.0D);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(3.0D);
        this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(8.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.2D);
    }

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(0, new EntityAISit(this));
        this.tasks.addTask(1, new AnimalAIMate(this, 1.0D));
        this.tasks.addTask(2, new CaimanAIMelee(this));
        this.tasks.addTask(3, new BreatheAirGoal(this));
        this.tasks.addTask(4, new TameableAIFollowOwner(this, 1.1D, 4.0F, 2.0F, false));
        this.tasks.addTask(5, new EntityAIAttackMelee(this, 1.2F, false));
        this.tasks.addTask(6, new EntityAITempt(this, 1.1D, Items.FISH, false) {
            @Override
            protected boolean isTempting(ItemStack stack) {
                return AMTagRegistry.itemInTag(AMTagRegistry.CAIMAN_BREEDABLES, stack.getItem());
            }
        });
        this.tasks.addTask(7, new AnimalAIFindWater(this));
        this.tasks.addTask(7, new AnimalAILeaveWater(this));
        this.tasks.addTask(8, new CaimanAIBellow(this));
        this.tasks.addTask(9, new SemiAquaticAIRandomSwimming(this, 1.0D, 30));
        this.tasks.addTask(10, new EntityAIWander(this, 1.0D, 60));
        this.tasks.addTask(11, new EntityAILookIdle(this));
        this.tasks.addTask(11, new EntityAIWatchClosest(this, EntityPlayer.class, 6.0F));
        this.targetTasks.addTask(1, new AnimalAIHurtByTargetNotBaby(this, true));
        this.targetTasks.addTask(2, new EntityAIOwnerHurtByTarget(this) {
            @Override
            public void startExecuting() {
                super.startExecuting();
                EntityCaiman.this.tameAttackFlag = true;
            }

            @Override
            public void resetTask() {
                super.resetTask();
                EntityCaiman.this.tameAttackFlag = false;
            }
        });
        this.targetTasks.addTask(3, new EntityAIOwnerHurtTarget(this) {
            @Override
            public void startExecuting() {
                super.startExecuting();
                EntityCaiman.this.tameAttackFlag = true;
            }

            @Override
            public void resetTask() {
                super.resetTask();
                EntityCaiman.this.tameAttackFlag = false;
            }
        });
        this.targetTasks.addTask(5, new EntityAINearestTarget3D(this, EntityLivingBase.class, 180, false, true, AMEntityRegistry.buildPredicateFromTag(AMTagRegistry.CAIMAN_TARGETS)) {
            @Override
            public boolean shouldExecute() {
                return !EntityCaiman.this.isChild() && !EntityCaiman.this.isTamed() && super.shouldExecute();
            }
        });
    }

    @Override
    public boolean getCanSpawnHere() {
        return AMEntityRegistry.rollSpawn(AMConfig.caimanSpawnRolls, this.getRNG(), AMEntityRegistry.AMSpawnReason.OTHER) && super.getCanSpawnHere();
    }

    @Override
    public int getMaxSpawnedInChunk() {
        return 2;
    }

    public boolean isMaxGroupSizeReached(int sizeIn) {
        return false;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return this.isChild() ? AMSoundRegistry.CROCODILE_BABY : AMSoundRegistry.CAIMAN_IDLE;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return AMSoundRegistry.CAIMAN_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return AMSoundRegistry.CAIMAN_HURT;
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        this.prevHoldProgress = this.holdProgress;
        this.prevSwimProgress = this.swimProgress;
        this.prevSitProgress = this.sitProgress;
        this.prevVibrateProgress = this.vibrateProgress;

        final boolean ground = !this.isInWater();
        final boolean bellowing = this.isBellowing();
        final boolean grabbing = this.getHeldMobId() != -1;
        final boolean sitting = this.isSitting() && ground;

        if (!ground && this.isLandNavigator) {
            switchNavigator(false);
        }
        if (ground && !this.isLandNavigator) {
            switchNavigator(true);
        }
        if (ground && this.swimProgress > 0) {
            this.swimProgress--;
        }
        if (!ground && this.swimProgress < 5F) {
            this.swimProgress++;
        }
        if (bellowing && this.vibrateProgress < 5) {
            this.vibrateProgress++;
        }
        if (!bellowing && this.vibrateProgress > 0F) {
            this.vibrateProgress--;
        }
        if (sitting && this.sitProgress < 5) {
            this.sitProgress++;
        }
        if (!sitting && this.sitProgress > 0F) {
            this.sitProgress--;
        }
        if (grabbing && this.holdProgress < 5) {
            this.holdProgress += 2.5F;
        }
        if (!grabbing && this.holdProgress > 0F) {
            this.holdProgress -= 2.5F;
        }
        if (!this.world.isRemote) {
            if (this.isInWater()) {
                this.swimTimer++;
            } else {
                if (this.isBellowing()) {
                    this.setBellowing(false);
                }
                this.swimTimer--;
            }
            EntityLivingBase target = this.getAttackTarget();
            if (target instanceof EntityWaterMob && !this.isTamed() && target instanceof net.minecraft.entity.EntityLiving) {
                ((net.minecraft.entity.EntityLiving) target).enablePersistence();
            }
        } else if (this.isInWater() && this.isBellowing()) {
            int particles = 4 + this.rand.nextInt(3);
            for (int i = 0; i <= particles; i++) {
                float angle = (i / (float) particles) * (float) Math.PI * 2F;
                double px = this.posX + MathHelper.cos(angle);
                double pz = this.posZ + MathHelper.sin(angle);
                double py = this.getEntityBoundingBox().minY + this.getSubmergedHeight(Material.WATER) * 0.5D;
                this.world.spawnParticle(EnumParticleTypes.WATER_SPLASH, px, py, pz, 0, 0.3D, 0);
            }
        }
        if (this.bellowCooldown > 0) {
            this.bellowCooldown--;
        }
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        ItemStack itemstack = player.getHeldItem(hand);
        boolean type = super.processInteract(player, hand);
        if (this.isTamed() && AMTagRegistry.itemInTag(AMTagRegistry.CAIMAN_FOODSTUFFS, itemstack.getItem()) && this.getHealth() < this.getMaxHealth()) {
            if (!player.capabilities.isCreativeMode) {
                itemstack.shrink(1);
            }
            this.playSound(SoundEvents.ENTITY_GENERIC_EAT, this.getSoundVolume(), this.getSoundPitch());
            this.heal(5);
            return true;
        }
        if (this.isTamed() && this.isOwner(player) && !this.isBreedingItem(itemstack)) {
            this.setCommand(this.getCommand() + 1);
            if (this.getCommand() == 3) {
                this.setCommand(0);
            }
            player.sendStatusMessage(new TextComponentTranslation("entity.alexsmobs.all.command_" + this.getCommand(), this.getName()), true);
            boolean sit = this.getCommand() == 2;
            this.forcedSit = sit;
            this.setSitting(sit);
            return true;
        }
        return type;
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        return AMTagRegistry.itemInTag(AMTagRegistry.CAIMAN_BREEDABLES, stack.getItem());
    }

    public boolean isPushedByWater() {
        return false;
    }

    public int getCommand() {
        return this.dataManager.get(COMMAND);
    }

    public void setCommand(int command) {
        this.dataManager.set(COMMAND, command);
    }

    public void setHeldMobId(int i) {
        this.dataManager.set(HELD_MOB_ID, i);
    }

    public int getHeldMobId() {
        return this.dataManager.get(HELD_MOB_ID);
    }

    @Nullable
    public Entity getHeldMob() {
        final int id = getHeldMobId();
        return id == -1 ? null : this.world.getEntityByID(id);
    }

    public boolean isSitting() {
        return this.dataManager.get(SITTING);
    }

    public void setSitting(boolean sit) {
        this.dataManager.set(SITTING, sit);
    }

    public boolean isBellowing() {
        return this.dataManager.get(BELLOWING);
    }

    public void setBellowing(boolean bellowing) {
        this.dataManager.set(BELLOWING, bellowing);
    }

    public double getSubmergedHeight(Material material) {
        if (!this.isInsideOfMaterial(material)) {
            return 0;
        }
        return this.isInsideOfMaterial(Material.WATER) ? this.height : this.height * 0.35F;
    }

    @Override
    public void travel(float strafe, float vertical, float forward) {
        if (this.isSitting()) {
            super.travel(0, 0, 0);
        } else if (!this.world.isRemote && this.isInWater()) {
            this.moveRelative(strafe, vertical, forward, this.getAIMoveSpeed());
            this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
            this.motionX *= 0.9D;
            this.motionY *= 0.9D;
            this.motionZ *= 0.9D;
            if (this.getAttackTarget() == null) {
                this.motionY -= 0.005D;
            }
        } else {
            super.travel(strafe, vertical, forward);
        }
    }

    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }

    @Override
    public boolean shouldEnterWater() {
        return !this.shouldLeaveWater() && this.swimTimer <= -1000 || this.bellowCooldown == 0;
    }

    @Override
    public boolean shouldLeaveWater() {
        EntityLivingBase target = this.getAttackTarget();
        if (target != null && !target.isInWater()) {
            return true;
        }
        return this.swimTimer > 600 && !this.isBellowing();
    }

    @Override
    public boolean shouldStopMoving() {
        return this.isSitting();
    }

    @Override
    public int getWaterSearchRange() {
        return 12;
    }

    @Nullable
    @Override
    public EntityAgeable createChild(EntityAgeable ageable) {
        return (EntityCaiman) AMEntityRegistry.CAIMAN.newInstance(this.world);
    }

    public net.minecraft.util.math.Vec3d getShakePreyPos() {
        float yaw = -this.rotationYawHead * 0.017453292F;
        float pitch = -this.rotationPitch * 0.017453292F;
        double dx = MathHelper.sin(yaw) * MathHelper.cos(pitch);
        double dy = MathHelper.sin(pitch);
        double dz = MathHelper.cos(yaw) * MathHelper.cos(pitch);
        return new net.minecraft.util.math.Vec3d(this.posX + dx, this.posY + this.getEyeHeight() - 0.1, this.posZ + dz);
    }

    @Override
    public void applyEntityCollision(Entity entity) {
        if (this.getHeldMobId() == -1) {
            super.applyEntityCollision(entity);
        }
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setBoolean("Bellowing", this.isBellowing());
        compound.setInteger("CaimanCommand", this.getCommand());
        compound.setBoolean("CaimanSitting", this.isSitting());
        compound.setInteger("BellowCooldown", this.bellowCooldown);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.setBellowing(compound.getBoolean("Bellowing"));
        this.bellowCooldown = compound.getInteger("BellowCooldown");
        this.setCommand(compound.getInteger("CaimanCommand"));
        this.setSitting(compound.getBoolean("CaimanSitting"));
    }

    @Override
    public boolean shouldFollow() {
        return this.getCommand() == 1;
    }

    private void switchNavigator(boolean onLand) {
        if (onLand) {
            this.moveHelper = new EntityMoveHelper(this);
            this.navigator = new GroundPathNavigatorWide(this, this.world);
            this.isLandNavigator = true;
        } else {
            this.moveHelper = new AquaticMoveController(this, 1.1F);
            this.navigator = new SemiAquaticPathNavigator(this, this.world);
            this.isLandNavigator = false;
        }
    }
}
