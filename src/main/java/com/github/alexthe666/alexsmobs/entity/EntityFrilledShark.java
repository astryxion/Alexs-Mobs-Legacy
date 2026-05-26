package com.github.alexthe666.alexsmobs.entity;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import com.github.alexthe666.alexsmobs.client.particle.AMParticleRegistry;
import com.github.alexthe666.alexsmobs.config.AMConfig;
import com.github.alexthe666.alexsmobs.effect.AMEffectRegistry;
import com.github.alexthe666.alexsmobs.entity.ai.AnimalAISwimBottom;
import com.github.alexthe666.alexsmobs.entity.ai.AquaticMoveController;
import com.github.alexthe666.alexsmobs.entity.ai.EntityAINearestTarget3D;
import com.github.alexthe666.alexsmobs.entity.ai.SemiAquaticAIRandomSwimming;
import com.github.alexthe666.alexsmobs.item.AMItemRegistry;
import com.github.alexthe666.citadel.animation.Animation;
import com.github.alexthe666.citadel.animation.IAnimatedEntity;
import com.google.common.base.Predicate;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathNavigateSwimmer;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class EntityFrilledShark extends EntityCreature implements IAnimatedEntity {

    public static final Animation ANIMATION_ATTACK = Animation.create(17);
    private static final DataParameter<Boolean> DEPRESSURIZED = EntityDataManager.createKey(EntityFrilledShark.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> FROM_BUCKET = EntityDataManager.createKey(EntityFrilledShark.class, DataSerializers.BOOLEAN);
    public float prevOnLandProgress;
    public float onLandProgress;
    private int animationTick;
    private Animation currentAnimation;

    public EntityFrilledShark(World worldIn) {
        super(worldIn);
        this.setSize(1.1F, 0.6F);
        this.moveHelper = new AquaticMoveController(this, 1F);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        AMEntityRegistry.registerAttributeIfAbsent(this, SharedMonsterAttributes.ATTACK_DAMAGE);
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(20.0D);
        this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(0.0D);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(3.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.2D);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(DEPRESSURIZED, Boolean.FALSE);
        this.dataManager.register(FROM_BUCKET, Boolean.FALSE);
    }

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(1, new AIFindWater());
        this.tasks.addTask(2, new AIMelee());
        this.tasks.addTask(3, new AnimalAISwimBottom(this, 0.8D, 7));
        this.tasks.addTask(4, new SemiAquaticAIRandomSwimming(this, 0.8D, 3));
        this.tasks.addTask(5, new EntityAILookIdle(this));
        this.tasks.addTask(6, new AIFollowBoat());
        this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, true));
        this.targetTasks.addTask(2, new EntityAINearestTarget3D(this, EntitySquid.class, 40, false, true, null));
        this.targetTasks.addTask(2, new EntityAINearestTarget3D(this, EntityMimicOctopus.class, 70, false, true, null));
        this.targetTasks.addTask(3, new EntityAINearestTarget3D(this, EntitySquid.class, 100, false, true, null));
        this.targetTasks.addTask(4, new EntityAINearestTarget3D(this, EntityZombie.class, 4, false, true, IN_WATER_ZOMBIE));
    }

    private static final Predicate<EntityLivingBase> IN_WATER_ZOMBIE = entity ->
            entity instanceof EntityZombie && entity.isInWater();

    @Override
    public boolean getCanSpawnHere() {
        return AMEntityRegistry.rollSpawn(AMConfig.frilledSharkSpawnRolls, this.getRNG(), AMEntityRegistry.AMSpawnReason.OTHER);
    }

    @Override
    public boolean isNotColliding() {
        return AMEntityRegistry.aquaticNoEntityCollision(this);
    }

    private boolean isFromBucket() {
        return this.dataManager.get(FROM_BUCKET);
    }

    public void setFromBucket(boolean fromBucket) {
        this.dataManager.set(FROM_BUCKET, fromBucket);
    }

    public void writeAdditional(NBTTagCompound compound) {
        compound.setBoolean("FromBucket", this.isFromBucket());
        compound.setBoolean("Depressurized", this.isDepressurized());
    }

    public void readAdditional(NBTTagCompound compound) {
        this.setFromBucket(compound.getBoolean("FromBucket"));
        this.setDepressurized(compound.getBoolean("Depressurized"));
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        this.writeAdditional(compound);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.readAdditional(compound);
    }

    @Override
    protected boolean canDespawn() {
        return !this.isFromBucket() && !this.hasCustomName() && super.canDespawn();
    }

    private void doInitialPosing(World world) {
        BlockPos down = this.getPosition();
        while (world.getBlockState(down).getMaterial() == Material.WATER && down.getY() > 1) {
            down = down.down();
        }
        this.setPosition(down.getX() + 0.5D, down.getY() + 1, down.getZ() + 0.5D);
    }

    @Nullable
    @Override
    public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData livingdata) {
        if (!this.world.isRemote) {
            doInitialPosing(this.world);
        }
        return super.onInitialSpawn(difficulty, livingdata);
    }

    public boolean isDepressurized() {
        return this.dataManager.get(DEPRESSURIZED);
    }

    public void setDepressurized(boolean depressurized) {
        this.dataManager.set(DEPRESSURIZED, depressurized);
    }

    @Override
    protected PathNavigate createNavigator(World worldIn) {
        return new PathNavigateSwimmer(this, worldIn);
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_GENERIC_SPLASH;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return SoundEvents.ENTITY_GENERIC_SPLASH;
    }

    protected ItemStack getFishBucket() {
        ItemStack stack = new ItemStack(AMItemRegistry.FRILLED_SHARK_BUCKET);
        NBTTagCompound platTag = new NBTTagCompound();
        this.writeAdditional(platTag);
        if (!stack.hasTagCompound()) {
            stack.setTagCompound(new NBTTagCompound());
        }
        stack.getTagCompound().setTag("FrilledSharkData", platTag);
        if (this.hasCustomName()) {
            stack.setStackDisplayName(this.getCustomNameTag());
        }
        return stack;
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        ItemStack itemstack = player.getHeldItem(hand);
        if (itemstack.getItem() == Items.WATER_BUCKET && this.isEntityAlive()) {
            this.playSound(SoundEvents.ITEM_BUCKET_FILL, 1.0F, 1.0F);
            if (!player.capabilities.isCreativeMode) {
                itemstack.shrink(1);
            }
            ItemStack itemstack1 = this.getFishBucket();
            if (!this.world.isRemote) {
                            }

            if (itemstack.isEmpty()) {
                player.setHeldItem(hand, itemstack1);
            } else if (!player.inventory.addItemStackToInventory(itemstack1)) {
                player.dropItem(itemstack1, false);
            }

            this.setDead();
            return true;
        }
        return super.processInteract(player, hand);
    }

    @Override
    public void travel(float strafe, float vertical, float forward) {
        if (!this.world.isRemote && this.isInWater()) {
            this.moveRelative(strafe, vertical, forward, 0.02F);
            this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
            this.motionX *= 0.9D;
            this.motionY *= 0.6D;
            this.motionZ *= 0.9D;
            if (this.getAttackTarget() == null) {
                this.motionY += -0.005D;
            }
        } else {
            super.travel(strafe, vertical, forward);
        }
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        this.prevOnLandProgress = onLandProgress;
        if (!this.isInWater() && onLandProgress < 5F) {
            onLandProgress++;
        }
        if (this.isInWater() && onLandProgress > 0F) {
            onLandProgress--;
        }
        if (this.isInWater()) {
            this.motionY *= 0.8D;
        }
        boolean clear = hasClearance();
        if (this.isDepressurized() && clear) {
            this.setDepressurized(false);
        }
        if (!isDepressurized() && !clear) {
            this.setDepressurized(true);
        }
        if (!world.isRemote && this.getAttackTarget() != null && this.getAnimation() == ANIMATION_ATTACK && this.getAnimationTick() == 12) {
            float f1 = this.rotationYaw * ((float) Math.PI / 180F);
            this.motionX += -MathHelper.sin(f1) * 0.06F;
            this.motionZ += MathHelper.cos(f1) * 0.06F;
            if (this.getAttackTarget().attackEntityFrom(DamageSource.causeMobDamage(this), (float) this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue())) {
                this.getAttackTarget().addPotionEffect(new PotionEffect(AMEffectRegistry.EXSANGUINATION, 60, 2));
                if (rand.nextInt(15) == 0 && this.getAttackTarget() instanceof EntitySquid) {
                    this.entityDropItem(new ItemStack(AMItemRegistry.SERRATED_SHARK_TOOTH), 0.0F);
                }
            }
        }
        AMEntityRegistry.updateAnimations(this);
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (source.getTrueSource() instanceof EntityZombie && ((EntityZombie) source.getTrueSource()).isInWater()) {
            amount *= 0.5F;
        }
        return super.attackEntityFrom(source, amount);
    }

    private boolean hasClearance() {
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        for (int l1 = 0; l1 < 10; ++l1) {
            mutable.setPos(MathHelper.floor(this.posX), MathHelper.floor(this.posY) + l1, MathHelper.floor(this.posZ));
            if (this.world.getBlockState(mutable).getMaterial() != Material.WATER) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Animation getAnimation() {
        return currentAnimation;
    }

    @Override
    public void setAnimation(Animation animation) {
        currentAnimation = animation;
    }

    public boolean isKaiju() {
        String s = TextFormatting.getTextWithoutFormattingCodes(this.getCustomNameTag());
        return s != null && (s.toLowerCase().contains("kamata kun") || s.toLowerCase().contains("kamata-kun"));
    }

    @Override
    public Animation[] getAnimations() {
        return new Animation[]{ANIMATION_ATTACK};
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
        if (this.getAnimation() == NO_ANIMATION) {
            this.setAnimation(ANIMATION_ATTACK);
        }
        return true;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void handleStatusUpdate(byte id) {
        if (id == 68) {
            double d2 = this.rand.nextGaussian() * 0.1D;
            double d0 = this.rand.nextGaussian() * 0.1D;
            double d1 = this.rand.nextGaussian() * 0.1D;
            float radius = this.width * 0.8F;
            float angle = (0.01745329251F * this.renderYawOffset);
            double extraX = radius * MathHelper.sin((float) (Math.PI + angle));
            double extraZ = radius * MathHelper.cos(angle);
            double x = this.posX + extraX + d0;
            double y = this.posY + this.height * 0.15F + d1;
            double z = this.posZ + extraZ + d2;
            AMParticleRegistry.spawnParticle(world, AMParticleRegistry.TEETH_GLINT, x, y, z, this.motionX, this.motionY, this.motionZ);
        } else {
            super.handleStatusUpdate(id);
        }
    }

    /**
     * 1.16 {@code FindWaterGoal}; 1.12 has no equivalent on {@link EntityMob}.
     */
    private class AIFindWater extends EntityAIBase {
        private BlockPos targetPos;
        private int executionChance = 30;

        AIFindWater() {
            this.setMutexBits(3);
        }

        @Override
        public boolean shouldExecute() {
            if (EntityFrilledShark.this.onGround && !isWaterAt(EntityFrilledShark.this.getPosition())) {
                if (EntityFrilledShark.this.getAttackTarget() != null || EntityFrilledShark.this.getRNG().nextInt(executionChance) == 0) {
                    targetPos = generateTarget();
                    return targetPos != null;
                }
            }
            return false;
        }

        @Override
        public void startExecuting() {
            if (targetPos != null) {
                EntityFrilledShark.this.getNavigator().tryMoveToXYZ(targetPos.getX(), targetPos.getY(), targetPos.getZ(), 1.2D);
            }
        }

        @Override
        public boolean shouldContinueExecuting() {
            return targetPos != null && !isWaterAt(EntityFrilledShark.this.getPosition()) && EntityFrilledShark.this.getDistanceSq(targetPos) > 2;
        }

        private boolean isWaterAt(BlockPos pos) {
            return EntityFrilledShark.this.world.getBlockState(pos).getMaterial() == Material.WATER;
        }

        @Nullable
        private BlockPos generateTarget() {
            BlockPos pos = EntityFrilledShark.this.getPosition();
            for (int i = 0; i < 15; i++) {
                BlockPos pos1 = pos.add(
                        EntityFrilledShark.this.getRNG().nextInt(16) - 8, EntityFrilledShark.this.getRNG().nextInt(8) - 4, EntityFrilledShark.this.getRNG().nextInt(16) - 8);
                if (isWaterAt(pos1)) {
                    return pos1;
                }
            }
            return null;
        }
    }

    /**
     * Vanilla 1.16 dolphin-style boat interest; 1.12 has no {@code FollowBoatGoal}.
     */
    private class AIFollowBoat extends EntityAIBase {
        private EntityBoat boat;
        private int delay;

        @Override
        public boolean shouldExecute() {
            if (!EntityFrilledShark.this.isInWater()) {
                return false;
            }
            List<EntityBoat> list = EntityFrilledShark.this.world.getEntitiesWithinAABB(EntityBoat.class, EntityFrilledShark.this.getEntityBoundingBox().grow(24.0D));
            EntityBoat closest = null;
            double best = Double.MAX_VALUE;
            for (EntityBoat b : list) {
                double d = EntityFrilledShark.this.getDistanceSq(b);
                if (d < best) {
                    best = d;
                    closest = b;
                }
            }
            this.boat = closest;
            return this.boat != null;
        }

        @Override
        public boolean shouldContinueExecuting() {
            return this.boat != null && this.boat.isEntityAlive() && EntityFrilledShark.this.getDistanceSq(this.boat) > 4.0D;
        }

        @Override
        public void resetTask() {
            this.boat = null;
        }

        @Override
        public void updateTask() {
            if (this.boat == null) {
                return;
            }
            if (--this.delay <= 0) {
                this.delay = 10;
                EntityFrilledShark.this.getNavigator().tryMoveToEntityLiving(this.boat, 1.0D);
            }
        }
    }

    private class AIMelee extends EntityAIBase {

        AIMelee() {
            this.setMutexBits(1);
        }

        @Override
        public boolean shouldExecute() {
            return EntityFrilledShark.this.getAttackTarget() != null && EntityFrilledShark.this.getAttackTarget().isEntityAlive();
        }

        @Override
        public boolean shouldContinueExecuting() {
            return shouldExecute();
        }

        @Override
        public void updateTask() {
            EntityLivingBase target = EntityFrilledShark.this.getAttackTarget();
            if (target != null) {
                double speed = 1.0D;
                if (EntityFrilledShark.this.getDistance(target) < 10) {
                    if (EntityFrilledShark.this.getDistance(target) < 1.9D) {
                        EntityFrilledShark.this.attackEntityAsMob(target);
                        speed = 0.8D;
                    } else {
                        speed = 0.6D;
                        EntityFrilledShark.this.getLookHelper().setLookPositionWithEntity(target, 70.0F, 70.0F);
                        if (target instanceof EntitySquid) {
                            Vec3d mouth = EntityFrilledShark.this.getPositionVector();
                            float squidSpeed = 0.07F;
                            target.motionX += (mouth.x - target.posX) * squidSpeed;
                            target.motionY += (mouth.y - target.getPositionEyes(1.0F).y) * squidSpeed;
                            target.motionZ += (mouth.z - target.posZ) * squidSpeed;
                            EntityFrilledShark.this.world.setEntityState(EntityFrilledShark.this, (byte) 68);
                        }
                    }
                }
                if (target instanceof EntityZombie || target instanceof EntityPlayer) {
                    speed = 1.0D;
                }
                EntityFrilledShark.this.getNavigator().tryMoveToEntityLiving(target, speed);
            }
        }
    }
}
