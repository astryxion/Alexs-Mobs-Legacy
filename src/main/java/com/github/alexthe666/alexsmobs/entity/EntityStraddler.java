package com.github.alexthe666.alexsmobs.entity;

import com.github.alexthe666.alexsmobs.config.AMConfig;
import com.github.alexthe666.alexsmobs.entity.ai.StraddlerAIShoot;
import com.github.alexthe666.alexsmobs.entity.ai.StraddlerLavaPathNavigator;
import com.github.alexthe666.alexsmobs.misc.AMSoundRegistry;
import com.github.alexthe666.citadel.animation.Animation;
import com.github.alexthe666.citadel.animation.IAnimatedEntity;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;


public class EntityStraddler extends EntityMob implements IAnimatedEntity {

    public static final Animation ANIMATION_LAUNCH = Animation.create(30);
    private static final DataParameter<Integer> STRADPOLE_COUNT = EntityDataManager.createKey(EntityStraddler.class, DataSerializers.VARINT);
    private int animationTick;
    private Animation currentAnimation;

    public EntityStraddler(World world) {
        super(world);
        this.setPathPriority(PathNodeType.LAVA, 0.0F);
        this.setPathPriority(PathNodeType.DANGER_FIRE, 0.0F);
        this.setPathPriority(PathNodeType.DAMAGE_FIRE, 0.0F);
        this.setSize(1.65F, 3.0F);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return AMSoundRegistry.STRADDLER_IDLE;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return AMSoundRegistry.STRADDLER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return AMSoundRegistry.STRADDLER_HURT;
    }

    public static boolean canStraddlerSpawn(World worldIn, BlockPos pos) {
        return worldIn.getBlockState(pos.down()).getBlock() == Blocks.NETHERRACK;
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        AMEntityRegistry.registerAttributeIfAbsent(this, SharedMonsterAttributes.ATTACK_DAMAGE);
        AMEntityRegistry.registerAttributeIfAbsent(this, SharedMonsterAttributes.KNOCKBACK_RESISTANCE);
        AMEntityRegistry.registerAttributeIfAbsent(this, SharedMonsterAttributes.ARMOR);
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(28.0D);
        this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(0.8D);
        this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(5.0D);
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(32.0D);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(2.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.3D);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(STRADPOLE_COUNT, 0);
    }

    public int getStradpoleCount() {
        return this.dataManager.get(STRADPOLE_COUNT);
    }

    public void setStradpoleCount(int index) {
        this.dataManager.set(STRADPOLE_COUNT, index);
    }

    @Override
    public boolean getCanSpawnHere() {
        return AMEntityRegistry.rollSpawn(AMConfig.straddlerSpawnRolls, this.getRNG(), AMEntityRegistry.AMSpawnReason.OTHER);
    }

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(1, new StraddlerAIShoot(this, 0.5D, 30, 16.0F));
        this.tasks.addTask(7, new EntityAIWander(this, 1.0D, 60));
        this.tasks.addTask(8, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        this.tasks.addTask(8, new EntityAILookIdle(this));
        this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, true));
        this.targetTasks.addTask(2, new EntityAINearestAttackableTarget<EntityPlayer>(this, EntityPlayer.class, true));
        this.targetTasks.addTask(3, new EntityAINearestAttackableTarget<EntityVillager>(this, EntityVillager.class, true));
    }

    @Override
    protected void updateFallState(double y, boolean onGroundIn, IBlockState state, BlockPos pos) {
        this.doBlockCollisions();
        if (this.isInLava()) {
            this.fallDistance = 0.0F;
        } else {
            super.updateFallState(y, onGroundIn, state, pos);
        }
    }

    @Override
    public void travel(float strafe, float vertical, float forward) {
        if (this.world != null && !this.world.isRemote && (this.isInWater() || this.isInLava())) {
            float base = (float) this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue();
            float mult = (this.getAnimation() == ANIMATION_LAUNCH ? 0.5F : 1.0F) * (this.isInLava() ? 0.2F : 1.0F);
            this.setAIMoveSpeed(base * mult);
            this.moveRelative(strafe, vertical, forward, 0.02F);
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

    /**
     * Lava buoyancy / surface behavior from 1.16 strider-style logic, adapted to block materials.
     */
    private void updateLavaPhysics() {
        if (this.isInLava()) {
            BlockPos down = new BlockPos(this.posX, this.posY - 0.5D, this.posZ);
            BlockPos up = new BlockPos(this.posX, this.posY + (double) this.height, this.posZ);
            if (this.world.getBlockState(down).getMaterial() == Material.LAVA
                    && this.world.getBlockState(up).getMaterial() != Material.LAVA) {
                this.onGround = true;
            } else {
                this.motionX *= 0.5D;
                this.motionZ *= 0.5D;
                this.motionY += (double) this.rand.nextFloat() * 0.5D;
            }
        }
    }

    public boolean isNotColliding(World worldIn) {
        return worldIn.checkNoEntityCollision(this.getEntityBoundingBox(), this)
                && worldIn.getCollisionBoxes(this, this.getEntityBoundingBox()).isEmpty();
    }

    protected float determineNextStepDistance() {
        return this.distanceWalkedOnStepModified + 0.6F;
    }

    @Override
    public float getBlockPathWeight(BlockPos pos) {
        if (this.world.getBlockState(pos).getMaterial() == Material.LAVA) {
            return 10.0F;
        }
        return this.isInLava() ? Float.NEGATIVE_INFINITY : 0.0F;
    }

    @Override
    public boolean isBurning() {
        return false;
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setInteger("StradpoleCount", this.getStradpoleCount());
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.setStradpoleCount(compound.getInteger("StradpoleCount"));
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        this.updateLavaPhysics();
        if (this.getAnimation() == ANIMATION_LAUNCH && this.isEntityAlive()) {
            if (this.getAnimationTick() == 2) {
                this.playSound(SoundEvents.ENTITY_MAGMACUBE_JUMP, 2.0F, 1.0F / (this.getRNG().nextFloat() * 0.4F + 0.8F));
            }
        }
        if (this.getAnimation() == ANIMATION_LAUNCH && this.isEntityAlive() && this.getAnimationTick() == 20 && this.getAttackTarget() != null) {
            EntityStradpole pole = (EntityStradpole) AMEntityRegistry.STRADPOLE.newInstance(this.world);
            if (pole != null) {
                pole.setParentId(this.getUniqueID());
                pole.setPosition(this.posX, this.posY + (double) this.getEyeHeight(), this.posZ);
                EntityLivingBase target = this.getAttackTarget();
                double d0 = target.posY + (double) target.getEyeHeight() - 1.1D;
                double d1 = target.posX - this.posX;
                double d2 = d0 - pole.posY;
                double d3 = target.posZ - this.posZ;
                float f = MathHelper.sqrt(d1 * d1 + d3 * d3) * 0.4F;
                float f3 = MathHelper.sqrt(d1 * d1 + d2 * d2 + d3 * d3) * 0.2F;
                this.playSound(SoundEvents.ENTITY_ARROW_SHOOT, 2.0F, 1.0F / (this.getRNG().nextFloat() * 0.4F + 0.8F));
                pole.shoot(d1, d2 + (double) f3, d3, 2.0F, 0.0F);
                pole.rotationYaw = this.rotationYaw % 360.0F;
                pole.rotationPitch = MathHelper.clamp(this.rotationYaw, -90.0F, 90.0F) % 360.0F;
                if (!this.world.isRemote) {
                    this.world.spawnEntity(pole);
                }
            }
        }
        AMEntityRegistry.updateAnimations(this);
    }

    @Override
    public Animation getAnimation() {
        return this.currentAnimation;
    }

    @Override
    public void setAnimation(Animation animation) {
        this.currentAnimation = animation;
    }

    @Override
    public int getAnimationTick() {
        return this.animationTick;
    }

    @Override
    public void setAnimationTick(int i) {
        this.animationTick = i;
    }

    @Override
    public Animation[] getAnimations() {
        return new Animation[]{ANIMATION_LAUNCH};
    }

    @Override
    protected PathNavigate createNavigator(World worldIn) {
        return new StraddlerLavaPathNavigator(this, worldIn);
    }

    public boolean shouldShoot() {
        return true;
    }
}
