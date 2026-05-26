package com.github.alexthe666.alexsmobs.entity;

import com.github.alexthe666.alexsmobs.entity.ai.AnimalAIWanderRanged;
import com.github.alexthe666.alexsmobs.entity.ai.CreatureAITargetItems;
import com.github.alexthe666.alexsmobs.misc.AMSoundRegistry;
import com.github.alexthe666.citadel.animation.Animation;
import com.github.alexthe666.citadel.animation.IAnimatedEntity;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIMate;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAITempt;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.entity.passive.EntityRabbit;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class EntityTasmanianDevil extends EntityAnimal implements IAnimatedEntity, ITargetsDroppedItems {

    private int animationTick;
    private Animation currentAnimation;
    public static final Animation ANIMATION_HOWL = Animation.create(40);
    public static final Animation ANIMATION_ATTACK = Animation.create(8);
    private static final DataParameter<Boolean> BASKING = EntityDataManager.createKey(EntityTasmanianDevil.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> SITTING = EntityDataManager.createKey(EntityTasmanianDevil.class, DataSerializers.BOOLEAN);
    public float prevBaskProgress;
    public float prevSitProgress;
    public float baskProgress;
    public float sitProgress;
    private int sittingTime;
    private int maxSitTime;
    private int scareMobsTime = 0;

    public EntityTasmanianDevil(World world) {
        super(world);
    }

    public boolean shouldMove() {
        return !isSitting() && !isBasking();
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return AMSoundRegistry.TASMANIAN_DEVIL_IDLE;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return AMSoundRegistry.TASMANIAN_DEVIL_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return AMSoundRegistry.TASMANIAN_DEVIL_HURT;
    }

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(1, new EntityAIAttackMelee(this, 1.5D, true));
        this.tasks.addTask(2, new EntityAITempt(this, 1.1D, Items.ROTTEN_FLESH, false) {
            @Override
            public void updateTask() {
                super.updateTask();
                if (EntityTasmanianDevil.this.getAnimation() == NO_ANIMATION) {
                    EntityTasmanianDevil.this.setBasking(false);
                    EntityTasmanianDevil.this.setSitting(false);
                }
            }
        });
        this.tasks.addTask(3, new AnimalAIWanderRanged(this, 60, 1.0D, 14, 7));
        this.tasks.addTask(4, new EntityAIMate(this, 1.0D));
        this.tasks.addTask(5, new EntityAIWatchClosest(this, EntityPlayer.class, 6.0F));
        this.tasks.addTask(6, new EntityAILookIdle(this));
        this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, true, EntityTasmanianDevil.class));
        this.targetTasks.addTask(2, new EntityAINearestAttackableTarget<>(this, EntityChicken.class, 120, true, true, null));
        this.targetTasks.addTask(2, new EntityAINearestAttackableTarget<>(this, EntityRabbit.class, 120, true, true, null));
        this.targetTasks.addTask(3, new CreatureAITargetItems(this, false, 30));
    }

    /**
     * 1.12.2 equivalent of 1.16 {@code func_241847_a} (looting-enchant kill bonus).
     */
    @Override
    public void onKillEntity(EntityLivingBase entity) {
        if (!this.world.isRemote && EnchantmentHelper.getLootingModifier(this) > 0 && this.getRNG().nextBoolean()) {
            if (entity instanceof EntityAnimal || entity.getCreatureAttribute() == EnumCreatureAttribute.UNDEAD) {
                entity.entityDropItem(new ItemStack(Items.BONE), 0.0F);
            }
        }
        super.onKillEntity(entity);
    }

    @Override
    public void travel(float strafe, float vertical, float forward) {
        if (!this.shouldMove()) {
            if (this.getNavigator().getPath() != null) {
                this.getNavigator().clearPath();
            }
            strafe = 0.0F;
            vertical = 0.0F;
            forward = 0.0F;
        }
        super.travel(strafe, vertical, forward);
    }

    public void setSitting(boolean sit) {
        this.dataManager.set(SITTING, sit);
    }

    public boolean isSitting() {
        return this.dataManager.get(SITTING);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(BASKING, Boolean.FALSE);
        this.dataManager.register(SITTING, Boolean.FALSE);
    }

    public boolean isBasking() {
        return this.dataManager.get(BASKING);
    }

    public void setBasking(boolean basking) {
        this.dataManager.set(BASKING, basking);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        AMEntityRegistry.registerAttributeIfAbsent(this, SharedMonsterAttributes.ATTACK_DAMAGE);
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(14.0D);
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(32.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.3D);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(2.0D);
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        Item item = stack.getItem();
        if (item instanceof ItemFood) {
            return ((ItemFood) item).isWolfsFavoriteMeat() && item != Items.ROTTEN_FLESH;
        }
        return false;
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        this.prevBaskProgress = this.baskProgress;
        this.prevSitProgress = this.sitProgress;
        if (this.isSitting() && sitProgress < 5) {
            sitProgress++;
        }
        if (!this.isSitting() && sitProgress > 0) {
            sitProgress--;
        }
        if (this.isBasking() && baskProgress < 5) {
            baskProgress++;
        }
        if (!this.isBasking() && baskProgress > 0) {
            baskProgress--;
        }
        if (!world.isRemote && this.getAttackTarget() != null && this.getAnimation() == ANIMATION_ATTACK && this.getAnimationTick() == 5 && this.canEntityBeSeen(this.getAttackTarget())) {
            float f1 = this.rotationYaw * ((float) Math.PI / 180F);
            this.motionX += -MathHelper.sin(f1) * 0.02F;
            this.motionZ += MathHelper.cos(f1) * 0.02F;
            this.getAttackTarget().knockBack(this, 1.0F, this.getAttackTarget().posX - this.posX, this.getAttackTarget().posZ - this.posZ);
            this.getAttackTarget().attackEntityFrom(DamageSource.causeMobDamage(this), (float) this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue());
        }
        if (!world.isRemote && (isSitting() || isBasking()) && ++sittingTime > maxSitTime) {
            this.setSitting(false);
            this.setBasking(false);
            sittingTime = 0;
            maxSitTime = 75 + rand.nextInt(50);
        }
        double motionSq = this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ * this.motionZ;
        if (!world.isRemote && motionSq < 0.03D && this.getAnimation() == NO_ANIMATION && !this.isBasking() && !this.isSitting() && rand.nextInt(100) == 0) {
            sittingTime = 0;
            maxSitTime = 100 + rand.nextInt(550);
            if (this.getRNG().nextBoolean()) {
                this.setSitting(true);
                this.setBasking(false);
            } else {
                this.setSitting(false);
                this.setBasking(true);
            }
        }
        if (this.getAnimation() == ANIMATION_HOWL && this.getAnimationTick() == 1) {
            this.playSound(AMSoundRegistry.TASMANIAN_DEVIL_ROAR, this.getSoundVolume() * 2F, this.getSoundPitch());
        }
        if (this.getAnimation() == ANIMATION_HOWL && this.getAnimationTick() > 3) {
            scareMobsTime = 40;
        }
        if (scareMobsTime > 0) {
            List<EntityMob> list = this.world.getEntitiesWithinAABB(EntityMob.class, this.getEntityBoundingBox().grow(16, 8, 16));
            for (EntityMob e : list) {
                e.setAttackTarget(null);
                e.setRevengeTarget(null);
                if (scareMobsTime % 5 == 0) {
                    Vec3d vec = RandomPositionGenerator.findRandomTargetBlockAwayFrom(e, 20, 7, new Vec3d(this.posX, this.posY, this.posZ));
                    if (vec != null) {
                        e.getNavigator().tryMoveToXYZ(vec.x, vec.y, vec.z, 1.5D);
                    }
                }
            }
            scareMobsTime--;
        }
        if (this.getAttackTarget() != null && this.getAttackTarget().isEntityAlive() && (this.getRevengeTarget() == null || !this.getRevengeTarget().isEntityAlive())) {
            this.setRevengeTarget(this.getAttackTarget());
        }
        if ((this.isSitting() || this.isBasking()) && (this.getAttackTarget() != null || this.isInLove())) {
            this.setSitting(false);
            this.setBasking(false);
        }
        AMEntityRegistry.updateAnimations(this);
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        ItemStack itemstack = player.getHeldItem(hand);
        Item item = itemstack.getItem();
        boolean type = super.processInteract(player, hand);
        if (item == Items.ROTTEN_FLESH && this.getAnimation() != ANIMATION_HOWL) {
            this.playSound(SoundEvents.ENTITY_GENERIC_EAT, this.getSoundVolume(), this.getSoundPitch());
            if (itemstack.getItem().hasContainerItem()) {
                this.entityDropItem(new ItemStack(itemstack.getItem().getContainerItem()), 0.0F);
            }
            if (!player.capabilities.isCreativeMode) {
                itemstack.shrink(1);
            }
            this.setAnimation(ANIMATION_HOWL);
            return true;
        }
        return type;
    }

    @Override
    public boolean attackEntityAsMob(Entity entityIn) {
        if (this.getAnimation() == NO_ANIMATION) {
            this.setAnimation(ANIMATION_ATTACK);
        }
        return true;
    }

    @Override
    public int getAnimationTick() {
        return animationTick;
    }

    @Override
    public void setAnimationTick(int i) {
        animationTick = i;
    }

    @Override
    public Animation getAnimation() {
        return currentAnimation;
    }

    @Override
    public void setAnimation(Animation animation) {
        currentAnimation = animation;
        if (animation == ANIMATION_HOWL) {
            this.setSitting(true);
            this.setBasking(false);
            maxSitTime = Math.max(25, maxSitTime);
        }
    }

    @Override
    public Animation[] getAnimations() {
        return new Animation[]{ANIMATION_ATTACK, ANIMATION_HOWL};
    }

    @Nullable
    @Override
    public EntityAgeable createChild(EntityAgeable ageable) {
        return (EntityTasmanianDevil) AMEntityRegistry.TASMANIAN_DEVIL.newInstance(this.world);
    }

    @Override
    public boolean canTargetItem(ItemStack stack) {
        Item item = stack.getItem();
        return (item instanceof ItemFood && ((ItemFood) item).isWolfsFavoriteMeat()) || item == Items.BONE;
    }

    @Override
    public void onGetItem(EntityItem e) {
        if (e.getItem().getItem() == Items.BONE) {
            dropBonemeal();
            this.playSound(SoundEvents.ENTITY_SKELETON_STEP, this.getSoundVolume(), this.getSoundPitch());
        } else {
            this.playSound(SoundEvents.ENTITY_GENERIC_EAT, this.getSoundVolume(), this.getSoundPitch());
            this.heal(5);
        }
    }

    public void dropBonemeal() {
        ItemStack stack = new ItemStack(Items.DYE, 1, 15);
        for (int i = 0; i < 3 + rand.nextInt(1); i++) {
            this.entityDropItem(stack, 0.0F);
        }
    }
}
