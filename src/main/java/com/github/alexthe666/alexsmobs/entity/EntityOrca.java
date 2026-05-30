package com.github.alexthe666.alexsmobs.entity;

import com.github.alexthe666.alexsmobs.config.AMConfig;
import com.github.alexthe666.alexsmobs.effect.AMEffectRegistry;
import com.github.alexthe666.alexsmobs.entity.ai.*;
import com.github.alexthe666.alexsmobs.misc.AMSoundRegistry;
import com.github.alexthe666.alexsmobs.misc.AMTagRegistry;
import com.github.alexthe666.citadel.animation.Animation;
import com.github.alexthe666.citadel.animation.IAnimatedEntity;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.monster.EntityGuardian;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import com.google.common.base.Predicate;

public class EntityOrca extends EntityTameable implements IAnimatedEntity {

    public static final Animation ANIMATION_BITE = Animation.create(8);
    public static final Animation ANIMATION_TAILSWING = Animation.create(20);
    private static final DataParameter<Integer> MOISTNESS = EntityDataManager.createKey(EntityOrca.class, DataSerializers.VARINT);
    public int jumpCooldown;
    private int animationTick;
    private Animation currentAnimation;
    private int blockBreakCounter;
    public static final Predicate<EntityLivingBase> TARGET_BABY = new Predicate<EntityLivingBase>() {
        @Override
        public boolean apply(@Nullable EntityLivingBase entity) {
            return entity != null && entity.isChild();
        }
    };

    public EntityOrca(World worldIn) {
        super(worldIn);
        this.setPathPriority(PathNodeType.WATER, 0.0F);
        this.moveHelper = new MoveHelperController(this);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        AMEntityRegistry.registerAttributeIfAbsent(this, SharedMonsterAttributes.ARMOR);
        AMEntityRegistry.registerAttributeIfAbsent(this, SharedMonsterAttributes.ATTACK_DAMAGE);
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(60.0D);
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(64.0D);
        this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(0.0D);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(10.0D);
        this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(0.7D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(1.35D);
    }

    @Override
    protected PathNavigate createNavigator(World worldIn) {
        return new SwimmerJumpPathNavigator(this, worldIn);
    }

    public int getMoistness() {
        return this.dataManager.get(MOISTNESS);
    }

    public void setMoistness(int moistness) {
        this.dataManager.set(MOISTNESS, moistness);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(MOISTNESS, 2400);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return AMSoundRegistry.ORCA_IDLE;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return AMSoundRegistry.ORCA_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return AMSoundRegistry.ORCA_DIE;
    }

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(0, new BreatheAirGoal(this));
        this.tasks.addTask(1, new AIFindWater());
        this.tasks.addTask(2, new SwimWithPlayerGoal(this, 4.0D));
        this.tasks.addTask(4, new AnimalAIRandomSwimming(this, 1.0D, 10, 10, true));
        this.tasks.addTask(4, new EntityAILookIdle(this));
        this.tasks.addTask(5, new EntityAIWatchClosest(this, EntityPlayer.class, 6.0F));
        this.tasks.addTask(5, new OrcaAIJump(this, 10));
        this.tasks.addTask(6, new OrcaAIMeleeJump(this));
        this.tasks.addTask(6, new OrcaAIMelee(this, 1.2D, true));
        this.tasks.addTask(8, new AIFollowBoat());
        this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, true));
        this.targetTasks.addTask(2, new EntityAINearestTarget3D(this, EntityCachalotWhale.class, 5, false, false, TARGET_BABY));
        this.targetTasks.addTask(3, new EntityAINearestTarget3D(this, EntityLivingBase.class, 200, false, true, AMEntityRegistry.buildPredicateFromTag(AMTagRegistry.ORCA_TARGETS)));
    }

    @Override
    public Animation getAnimation() {
        return currentAnimation;
    }

    @Override
    public void setAnimation(Animation animation) {
        currentAnimation = animation;
    }

    @Override
    public Animation[] getAnimations() {
        return new Animation[]{ANIMATION_BITE, ANIMATION_TAILSWING};
    }

    @Override
    public void travel(float strafe, float vertical, float forward) {
        if (!this.world.isRemote && this.isInWater()) {
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
    public void updateAITasks() {
        super.updateAITasks();
        if (!this.world.isRemote && this.isInWater()) {
            breakBlock();
        }
    }

    public void breakBlock() {
        if (this.blockBreakCounter > 0) {
            --this.blockBreakCounter;
            return;
        }
        boolean flag = false;
        if (this.blockBreakCounter == 0) {
            for (int a = (int) Math.round(this.getEntityBoundingBox().minX); a <= (int) Math.round(this.getEntityBoundingBox().maxX); a++) {
                for (int b = (int) Math.round(this.getEntityBoundingBox().minY) - 1; (b <= (int) Math.round(this.getEntityBoundingBox().maxY) + 1) && (b <= 127); b++) {
                    for (int c = (int) Math.round(this.getEntityBoundingBox().minZ); c <= (int) Math.round(this.getEntityBoundingBox().maxZ); c++) {
                        BlockPos pos = new BlockPos(a, b, c);
                        IBlockState state = world.getBlockState(pos);
                        Block block = state.getBlock();
                        if (block != Blocks.AIR && !state.getMaterial().isLiquid() && AMTagRegistry.blockInTag(AMTagRegistry.ORCA_BREAKABLES, block) && world.isAirBlock(pos)) {
                            this.motionX *= 0.6F;
                            this.motionZ *= 0.6F;
                            flag = true;
                            if (block == Blocks.ICE || block == Blocks.FROSTED_ICE || block == Blocks.PACKED_ICE) {
                                world.destroyBlock(pos, false);
                                world.setBlockState(pos, Blocks.WATER.getDefaultState());
                            } else {
                                world.destroyBlock(pos, true);
                            }
                        }
                    }
                }
            }
        }
        if (flag) {
            blockBreakCounter = 20;
        }
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        if (jumpCooldown > 0) {
            jumpCooldown--;
            float f2 = (float) -((float) this.motionY * (double) (180F / (float) Math.PI));
            this.rotationPitch = f2;
        }
        if (this.isAIDisabled()) {
            this.setAir(this.getMaxAir());
        } else {
            if (!this.world.isRemote && !this.isInWater() && !this.isInLava()) {
                this.getNavigator().clearPath();
            }
            if (this.isInWater()) {
                this.setMoistness(2400);
            } else {
                this.setMoistness(this.getMoistness() - 1);
                if (this.getMoistness() <= 0) {
                    this.attackEntityFrom(DamageSource.STARVE, 1.0F);
                }

                if (this.onGround) {
                    this.motionX += (this.rand.nextFloat() * 2.0F - 1.0F) * 0.2F;
                    this.motionY = 0.5D;
                    this.motionZ += (this.rand.nextFloat() * 2.0F - 1.0F) * 0.2F;
                    this.rotationYaw = this.rand.nextFloat() * 360.0F;
                    this.onGround = false;
                    this.isAirBorne = true;
                }
            }

            if (this.world.isRemote && this.isInWater() && (this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ * this.motionZ) > 0.03D) {
                Vec3d vector3d = this.getLook(0.0F);
                float f = MathHelper.cos(this.rotationYaw * ((float) Math.PI / 180F)) * 0.9F;
                float f1 = MathHelper.sin(this.rotationYaw * ((float) Math.PI / 180F)) * 0.9F;
                float f2 = 1.2F - this.rand.nextFloat() * 0.7F;

                for (int i = 0; i < 2; ++i) {
                    this.world.spawnParticle(EnumParticleTypes.WATER_BUBBLE, this.posX - vector3d.x * (double) f2 + (double) f, this.posY - vector3d.y, this.posZ - vector3d.z * (double) f2 + (double) f1, 0.0D, 0.0D, 0.0D);
                    this.world.spawnParticle(EnumParticleTypes.WATER_BUBBLE, this.posX - vector3d.x * (double) f2 - (double) f, this.posY - vector3d.y, this.posZ - vector3d.z * (double) f2 - (double) f1, 0.0D, 0.0D, 0.0D);
                }
            }
        }
        EntityLivingBase attackTarget = this.getAttackTarget();
        if (attackTarget != null && getDistance(attackTarget) < attackTarget.width + this.width + 2) {
            if (this.getAnimation() == ANIMATION_BITE && this.getAnimationTick() == 4) {
                float damage = (float) ((int) this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue());
                if (attackTarget instanceof EntityZombie || attackTarget instanceof EntityGuardian) {
                    damage *= 2F;
                }
                boolean flag = attackTarget.attackEntityFrom(DamageSource.causeMobDamage(this), damage);
                if (flag) {
                    this.applyEnchantments(this, attackTarget);
                    this.playSound(SoundEvents.ENTITY_PLAYER_SPLASH, 1.0F, 1.0F);
                }
            }
            if (this.getAnimation() == ANIMATION_TAILSWING && this.getAnimationTick() == 6) {
                float damage = (float) ((int) this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue());
                if (attackTarget instanceof EntityZombie || attackTarget instanceof EntityGuardian) {
                    damage *= 2F;
                }
                boolean flag = attackTarget.attackEntityFrom(DamageSource.causeMobDamage(this), damage);
                if (flag) {
                    this.applyEnchantments(this, attackTarget);
                    this.playSound(SoundEvents.ENTITY_PLAYER_SPLASH, 1.0F, 1.0F);
                }
                attackTarget.knockBack(this, 1F, MathHelper.sin(rotationYaw * ((float) Math.PI / 180F)), -MathHelper.cos(rotationYaw * ((float) Math.PI / 180F)));
                float knockbackResist = (float) MathHelper.clamp((1.0D - this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).getAttributeValue()), 0, 1);
                this.getAttackTarget().motionY += knockbackResist * 0.4F;
            }
        }
        if (attackTarget != null && attackTarget instanceof EntityPlayer && attackTarget.isPotionActive(AMEffectRegistry.ORCAS_MIGHT)) {
            attackTarget.removePotionEffect(AMEffectRegistry.ORCAS_MIGHT);
        }
        AMEntityRegistry.updateAnimations(this);
    }

    @Override
    public int getAnimationTick() {
        return animationTick;
    }

    @Override
    public void setAnimationTick(int tick) {
        animationTick = tick;
    }

    @Override
    public boolean attackEntityAsMob(Entity entityIn) {
        if (this.isInWater() && rand.nextBoolean()) {
            this.setAnimation(ANIMATION_TAILSWING);
        } else {
            this.setAnimation(ANIMATION_BITE);
        }
        return true;
    }

    public int getMaxAir() {
        return 4800;
    }

    protected int determineNextAir(int currentAir) {
        return this.getMaxAir();
    }

    @Override
    public float getEyeHeight() {
        return 1.0F;
    }

    public int getVerticalFaceSpeed() {
        return 1;
    }

    public int getHorizontalFaceSpeed() {
        return 1;
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        return stack.getItem() == Items.FISH;
    }

    @Nullable
    @Override
    public EntityOrca createChild(EntityAgeable ageable) {
        return new EntityOrca(this.world);
    }

    public boolean shouldUseJumpAttack(EntityLivingBase attackTarget) {
        if (attackTarget.isInWater()) {
            BlockPos up = attackTarget.getPosition().up();
            return world.getBlockState(up.up()).getMaterial() != Material.WATER && world.getBlockState(up.up(2)).getMaterial() != Material.WATER && this.jumpCooldown == 0;
        } else {
            return this.jumpCooldown == 0;
        }
    }

    @Override
    @Nullable
    public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData livingdata) {
        this.setAir(this.getMaxAir());
        this.rotationPitch = 0.0F;
        this.setMoistness(2400);
        return super.onInitialSpawn(difficulty, livingdata);
    }

    @Override
    public boolean canBreatheUnderwater() {
        return false;
    }

    public void onUpdate() {
        int i = this.getAir();
        super.onUpdate();
        this.updateAir(i);
    }

    @Override
    public boolean isPushedByWater() {
        return false;
    }

    @Override
    public EnumCreatureAttribute getCreatureAttribute() {
        return EnumCreatureAttribute.UNDEFINED;
    }

    protected void updateAir(int air) {
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setInteger("Moistness", this.getMoistness());
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.setMoistness(compound.getInteger("Moistness"));
    }

    public void onJumpHit(EntityLivingBase entityIn) {
        boolean flag = entityIn.attackEntityFrom(DamageSource.causeMobDamage(this), (float) ((int) this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue()));
        if (flag) {
            this.applyEnchantments(this, entityIn);
            this.playSound(SoundEvents.ENTITY_PLAYER_SPLASH, 1.0F, 1.0F);
        }
    }

    @Override
    protected boolean canDespawn() {
        return !this.isTamed() && super.canDespawn();
    }

    @Override
    public boolean getCanSpawnHere() {
        return AMEntityRegistry.rollSpawn(AMConfig.orcaSpawnRolls, this.getRNG(), AMEntityRegistry.AMSpawnReason.OTHER)
                && this.getEntityBoundingBox().minY > 45.0D
                && this.getEntityBoundingBox().maxY < (double) this.world.getSeaLevel()
                && this.world.getBlockState(this.getPosition()).getMaterial() == Material.WATER;
    }

    @Override
    public int getMaxSpawnedInChunk() {
        return 1;
    }

    @Override
    public boolean isNotColliding() {
        return AMEntityRegistry.aquaticNoEntityCollision(this);
    }

    /**
     * 1.16 {@code FindWaterGoal} parity. On land, swim pathfinding is skipped (it can freeze the game).
     */
    private class AIFindWater extends EntityAIBase {
        private BlockPos targetPos;
        private int executionChance = 30;
        private int runTicks;

        AIFindWater() {
            this.setMutexBits(3);
        }

        @Override
        public boolean shouldExecute() {
            if (!EntityOrca.this.isInWater() && !EntityOrca.this.isInLava()) {
                if (EntityOrca.this.getAttackTarget() != null || EntityOrca.this.getRNG().nextInt(executionChance) == 0) {
                    targetPos = generateTarget();
                    return targetPos != null;
                }
            }
            return false;
        }

        @Override
        public void startExecuting() {
            runTicks = 0;
            if (targetPos != null && EntityOrca.this.isInWater()) {
                EntityOrca.this.getNavigator().tryMoveToXYZ(targetPos.getX(), targetPos.getY(), targetPos.getZ(), 1.2D);
            }
        }

        @Override
        public void updateTask() {
            runTicks++;
            if (targetPos == null) {
                return;
            }
            if (EntityOrca.this.isInWater()) {
                if (runTicks % 20 == 0 && EntityOrca.this.getNavigator().noPath()) {
                    EntityOrca.this.getNavigator().tryMoveToXYZ(targetPos.getX(), targetPos.getY(), targetPos.getZ(), 1.2D);
                }
            } else {
                EntityOrca.this.getNavigator().clearPath();
                steerTowardWater();
            }
        }

        @Override
        public boolean shouldContinueExecuting() {
            if (EntityOrca.this.isInWater() || EntityOrca.this.isInLava() || runTicks > 200) {
                return false;
            }
            return targetPos != null
                    && EntityOrca.this.world.getBlockState(EntityOrca.this.getPosition()).getMaterial() != Material.WATER;
        }

        @Override
        public void resetTask() {
            targetPos = null;
            runTicks = 0;
            EntityOrca.this.getNavigator().clearPath();
        }

        private void steerTowardWater() {
            FindWaterSteering.steerToward(EntityOrca.this, targetPos);
        }

        @Nullable
        private BlockPos generateTarget() {
            BlockPos origin = EntityOrca.this.getPosition();
            for (int i = 0; i < 15; i++) {
                BlockPos sample = origin.add(
                        EntityOrca.this.getRNG().nextInt(16) - 8,
                        EntityOrca.this.getRNG().nextInt(8) - 4,
                        EntityOrca.this.getRNG().nextInt(16) - 8);
                while (EntityOrca.this.world.isAirBlock(sample) && sample.getY() > 1) {
                    sample = sample.down();
                }
                if (EntityOrca.this.world.getBlockState(sample).getMaterial() == Material.WATER) {
                    return sample;
                }
            }
            return null;
        }
    }

    /**
     * 1.16 {@code FollowBoatGoal} parity.
     */
    private class AIFollowBoat extends EntityAIBase {
        private Entity entity;
        private int delay;

        @Override
        public boolean shouldExecute() {
            if (!EntityOrca.this.isInWater()) {
                return false;
            }
            for (Entity e : EntityOrca.this.world.getEntitiesWithinAABB(Entity.class, EntityOrca.this.getEntityBoundingBox().grow(24.0D))) {
                if (e instanceof net.minecraft.entity.item.EntityBoat) {
                    this.entity = e;
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean shouldContinueExecuting() {
            return this.entity != null && this.entity.isEntityAlive() && EntityOrca.this.getDistanceSq(this.entity) > 4.0D;
        }

        @Override
        public void resetTask() {
            this.entity = null;
        }

        @Override
        public void updateTask() {
            if (this.entity != null) {
                if (--this.delay <= 0) {
                    this.delay = 10;
                    EntityOrca.this.getNavigator().tryMoveToEntityLiving(this.entity, 1.2D);
                }
            }
        }
    }

    static class SwimWithPlayerGoal extends EntityAIBase {
        private final EntityOrca dolphin;
        private final double speed;
        private EntityPlayer targetPlayer;

        SwimWithPlayerGoal(EntityOrca dolphinIn, double speedIn) {
            this.dolphin = dolphinIn;
            this.speed = speedIn;
            this.setMutexBits(5);
        }

        @Override
        public boolean shouldExecute() {
            this.targetPlayer = this.dolphin.world.getClosestPlayerToEntity(this.dolphin, 24.0D);
            if (this.targetPlayer == null || this.targetPlayer.capabilities.isCreativeMode) {
                return false;
            }
            return this.targetPlayer.isInWater() && this.dolphin.getAttackTarget() != this.targetPlayer;
        }

        @Override
        public boolean shouldContinueExecuting() {
            return this.targetPlayer != null && this.dolphin.getAttackTarget() != this.targetPlayer && this.targetPlayer.isInWater() && this.dolphin.getDistanceSq(this.targetPlayer) < 256.0D;
        }

        @Override
        public void resetTask() {
            this.targetPlayer = null;
            this.dolphin.getNavigator().clearPath();
        }

        @Override
        public void updateTask() {
            this.dolphin.getLookHelper().setLookPositionWithEntity(this.targetPlayer, (float) (this.dolphin.getHorizontalFaceSpeed() + 20), (float) this.dolphin.getVerticalFaceSpeed());
            if (this.dolphin.getDistanceSq(this.targetPlayer) < 10D) {
                this.dolphin.getNavigator().clearPath();
            } else {
                this.dolphin.getNavigator().tryMoveToEntityLiving(this.targetPlayer, this.speed);
            }

            if (this.targetPlayer.isInWater() && this.targetPlayer.world.rand.nextInt(6) == 0) {
                this.targetPlayer.addPotionEffect(new PotionEffect(AMEffectRegistry.ORCAS_MIGHT, 1000));
            }
        }
    }

    static class MoveHelperController extends EntityMoveHelper {
        private final EntityOrca dolphin;

        public MoveHelperController(EntityOrca dolphinIn) {
            super(dolphinIn);
            this.dolphin = dolphinIn;
        }

        @Override
        public void onUpdateMoveHelper() {
            if (this.dolphin.isInWater()) {
                this.dolphin.motionY += 0.005D;
            }

            if (this.action == Action.MOVE_TO && !this.dolphin.getNavigator().noPath()) {
                double d0 = this.posX - this.dolphin.posX;
                double d1 = this.posY - this.dolphin.posY;
                double d2 = this.posZ - this.dolphin.posZ;
                double d3 = d0 * d0 + d1 * d1 + d2 * d2;
                if (d3 < (double) 2.5000003E-7F) {
                    this.dolphin.setAIMoveSpeed(0.0F);
                } else {
                    float f = (float) (MathHelper.atan2(d2, d0) * (double) (180F / (float) Math.PI)) - 90.0F;
                    this.dolphin.rotationYaw = this.limitAngle(this.dolphin.rotationYaw, f, 10.0F);
                    this.dolphin.renderYawOffset = this.dolphin.rotationYaw;
                    this.dolphin.rotationYawHead = this.dolphin.rotationYaw;
                    float f1 = (float) (this.speed * this.dolphin.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue());
                    if (this.dolphin.isInWater()) {
                        this.dolphin.setAIMoveSpeed(f1 * 0.02F);
                        float f2 = -((float) (MathHelper.atan2(d1, MathHelper.sqrt(d0 * d0 + d2 * d2)) * (double) (180F / (float) Math.PI)));
                        f2 = MathHelper.clamp(MathHelper.wrapDegrees(f2), -85.0F, 85.0F);
                        this.dolphin.rotationPitch = this.limitAngle(this.dolphin.rotationPitch, f2, 5.0F);
                        float f3 = MathHelper.cos(this.dolphin.rotationPitch * ((float) Math.PI / 180F));
                        float f4 = MathHelper.sin(this.dolphin.rotationPitch * ((float) Math.PI / 180F));
                        this.dolphin.moveForward = f3 * f1;
                        this.dolphin.moveVertical = -f4 * f1;
                    } else {
                        this.dolphin.setAIMoveSpeed(f1 * 0.1F);
                    }
                }
            } else {
                this.dolphin.setAIMoveSpeed(0.0F);
                this.dolphin.moveStrafing = 0.0F;
                this.dolphin.moveVertical = 0.0F;
                this.dolphin.moveForward = 0.0F;
            }
        }
    }
}
