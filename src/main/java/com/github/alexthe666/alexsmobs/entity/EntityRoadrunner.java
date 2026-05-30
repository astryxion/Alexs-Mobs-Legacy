package com.github.alexthe666.alexsmobs.entity;
import com.github.alexthe666.alexsmobs.misc.AMLootTables;

import com.github.alexthe666.alexsmobs.config.AMConfig;
import com.github.alexthe666.alexsmobs.entity.ai.AnimalAIWanderRanged;
import com.github.alexthe666.alexsmobs.entity.ai.RoadrunnerAIAttackMelee;
import com.github.alexthe666.alexsmobs.entity.ai.RoadrunnerAITempt;
import com.github.alexthe666.alexsmobs.item.AMItemRegistry;
import com.github.alexthe666.alexsmobs.misc.AMSoundRegistry;
import com.github.alexthe666.alexsmobs.misc.AMTagRegistry;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIFollowParent;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIMate;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAIPanic;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import net.minecraft.util.ResourceLocation;

public class EntityRoadrunner extends EntityAnimal {

    public float oFlapSpeed;
    public float oFlap;
    public float wingRotDelta = 1.0F;
    public float wingRotation;
    public float destPos;
    public float prevAttackProgress;
    public float attackProgress;
    private static final DataParameter<Integer> ATTACK_TICK = EntityDataManager.createKey(EntityRoadrunner.class, DataSerializers.VARINT);
    public int timeUntilNextFeather = this.rand.nextInt(24000) + 24000;

    public EntityRoadrunner(World worldIn) {
        super(worldIn);
        this.setSize(0.4F, 0.5F);
    }

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(1, new EntityAIPanic(this, 1.1D));
        this.tasks.addTask(1, new RoadrunnerAIAttackMelee(this, 1.0D, false));
        this.tasks.addTask(2, new EntityAIMate(this, 1.0D));
        this.tasks.addTask(4, new EntityAIFollowParent(this, 1.1D));
        this.tasks.addTask(4, new RoadrunnerAITempt(this, 1.1D, false));
        this.tasks.addTask(5, new AnimalAIWanderRanged(this, 50, 1.0D, 25, 7));
        this.tasks.addTask(6, new EntityAIWatchClosest(this, EntityPlayer.class, 6.0F));
        this.tasks.addTask(7, new EntityAILookIdle(this));
        this.targetTasks.addTask(1, new EntityAINearestAttackableTarget<>(this, EntityRattlesnake.class, 55, true, false, null));
        // 1.20 HurtByTargetGoal(..., Player.class) â€” do not retaliate against creative/spectator players
        this.targetTasks.addTask(2, new RoadrunnerAIHurtByTarget(this));
    }

    private static boolean isNonCombatPlayer(@Nullable EntityLivingBase target) {
        if (!(target instanceof EntityPlayer)) {
            return false;
        }
        EntityPlayer player = (EntityPlayer) target;
        return player.capabilities.isCreativeMode || (player instanceof EntityPlayerMP && ((EntityPlayerMP) player).isSpectator());
    }

    @Override
    public void setAttackTarget(@Nullable EntityLivingBase entitylivingbaseIn) {
        if (isNonCombatPlayer(entitylivingbaseIn)) {
            super.setAttackTarget(null);
            return;
        }
        super.setAttackTarget(entitylivingbaseIn);
    }

    @Override
    public void setRevengeTarget(@Nullable EntityLivingBase entitylivingbaseIn) {
        if (isNonCombatPlayer(entitylivingbaseIn)) {
            return;
        }
        super.setRevengeTarget(entitylivingbaseIn);
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        boolean result = super.attackEntityFrom(source, amount);
        Entity attacker = source.getTrueSource();
        if (attacker instanceof EntityPlayer && isNonCombatPlayer((EntityLivingBase) attacker)) {
            this.setRevengeTarget(null);
            this.setAttackTarget(null);
        }
        return result;
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        if (compound.hasKey("FeatherTime")) {
            this.timeUntilNextFeather = compound.getInteger("FeatherTime");
        }
    }

    @Override
    public boolean getCanSpawnHere() {
        return AMEntityRegistry.rollSpawn(AMConfig.roadrunnerSpawnRolls, this.getRNG(), AMEntityRegistry.AMSpawnReason.OTHER) && super.getCanSpawnHere();
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setInteger("FeatherTime", this.timeUntilNextFeather);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return AMSoundRegistry.ROADRUNNER_IDLE;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return AMSoundRegistry.ROADRUNNER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return AMSoundRegistry.ROADRUNNER_HURT;
    }
    @Override
    @Nullable
    protected ResourceLocation getLootTable() {
        return AMLootTables.ROADRUNNER;
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(ATTACK_TICK, 0);
    }

    @Override
    public boolean attackEntityAsMob(Entity entityIn) {
        this.dataManager.set(ATTACK_TICK, 5);
        return true;
    }

    @Override
    public boolean isEntityInvulnerable(DamageSource source) {
        return source == DamageSource.CACTUS || super.isEntityInvulnerable(source);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        AMEntityRegistry.registerAttributeIfAbsent(this, SharedMonsterAttributes.ATTACK_DAMAGE);
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(8.0D);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(1.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.45D);
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(10.0D);
    }

    @Override
    public void onLivingUpdate() {
        if (!this.world.isRemote) {
            if (isNonCombatPlayer(this.getRevengeTarget())) {
                this.setRevengeTarget(null);
            }
            if (isNonCombatPlayer(this.getAttackTarget())) {
                this.setAttackTarget(null);
            }
        }
        super.onLivingUpdate();
        this.oFlap = this.wingRotation;
        this.prevAttackProgress = attackProgress;
        this.oFlapSpeed = this.destPos;
        this.destPos = (float) ((double) this.destPos + (double) (this.onGround ? -1 : 4) * 0.3D);
        this.destPos = MathHelper.clamp(this.destPos, 0.0F, 1.0F);
        if (!this.onGround && this.wingRotDelta < 1.0F) {
            this.wingRotDelta = 1.0F;
        }
        if (!this.world.isRemote && this.isEntityAlive() && !this.isChild() && --this.timeUntilNextFeather <= 0) {
            this.entityDropItem(new ItemStack(AMItemRegistry.ROADRUNNER_FEATHER), 0.0F);
            this.timeUntilNextFeather = this.rand.nextInt(24000) + 24000;
        }
        this.wingRotDelta = (float) ((double) this.wingRotDelta * 0.9D);
        if (!this.onGround && this.motionY < 0.0D) {
            this.motionY *= 0.6D;
        }
        this.wingRotation += this.wingRotDelta * 2.0F;

        if (this.dataManager.get(ATTACK_TICK) > 0) {
            if (this.dataManager.get(ATTACK_TICK) == 2 && this.getAttackTarget() != null && this.getDistance(this.getAttackTarget()) < 1.3D) {
                this.getAttackTarget().attackEntityFrom(DamageSource.causeMobDamage(this), 2.0F);
            }
            this.dataManager.set(ATTACK_TICK, this.dataManager.get(ATTACK_TICK) - 1);
            if (attackProgress < 5F) {
                attackProgress++;
            }
        } else {
            if (attackProgress > 0F) {
                attackProgress--;
            }
        }
    }

    @Override
    public void fall(float distance, float damageMultiplier) {
    }

    protected void playStepSound(BlockPos pos, net.minecraft.block.Block blockIn) {
        this.playSound(SoundEvents.ENTITY_CHICKEN_STEP, 0.15F, 1.0F);
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        return AMTagRegistry.itemInTag(AMTagRegistry.INSECT_ITEMS, stack.getItem());
    }

    @Override
    @Nullable
    public EntityAgeable createChild(EntityAgeable ageable) {
        return (EntityRoadrunner) AMEntityRegistry.ROADRUNNER.newInstance(this.world);
    }

    private class RoadrunnerAIHurtByTarget extends EntityAIHurtByTarget {

        RoadrunnerAIHurtByTarget(EntityRoadrunner roadrunner) {
            super(roadrunner, true, EntityRattlesnake.class);
        }

        @Override
        public boolean shouldExecute() {
            return !isNonCombatPlayer(EntityRoadrunner.this.getRevengeTarget()) && super.shouldExecute();
        }

        @Override
        protected void setEntityAttackTarget(EntityCreature mobIn, EntityLivingBase targetIn) {
            if (!isNonCombatPlayer(targetIn)) {
                super.setEntityAttackTarget(mobIn, targetIn);
            }
        }
    }
}
