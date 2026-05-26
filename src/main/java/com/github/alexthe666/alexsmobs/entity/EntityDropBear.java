package com.github.alexthe666.alexsmobs.entity;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import com.github.alexthe666.alexsmobs.config.AMConfig;
import com.github.alexthe666.alexsmobs.entity.ai.DirectPathNavigator;
import com.github.alexthe666.alexsmobs.entity.ai.EntityAINearestTarget3D;
import com.github.alexthe666.alexsmobs.entity.ai.FlightMoveController;
import com.github.alexthe666.alexsmobs.entity.ai.GroundPathNavigatorWide;
import com.github.alexthe666.alexsmobs.misc.AMSoundRegistry;
import com.github.alexthe666.citadel.animation.Animation;
import com.github.alexthe666.citadel.animation.IAnimatedEntity;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityMoveHelper;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Random;

public class EntityDropBear extends EntityMob implements IAnimatedEntity {

    public static final Animation ANIMATION_BITE = Animation.create(9);
    public static final Animation ANIMATION_SWIPE_R = Animation.create(15);
    public static final Animation ANIMATION_SWIPE_L = Animation.create(15);
    public static final Animation ANIMATION_JUMPUP = Animation.create(20);
    private static final DataParameter<Boolean> UPSIDE_DOWN = EntityDataManager.createKey(EntityDropBear.class, DataSerializers.BOOLEAN);
    public float prevUpsideDownProgress;
    public float upsideDownProgress;
    public boolean fallRotation = rand.nextBoolean();
    private int animationTick;
    private boolean jumpingUp = false;
    private Animation currentAnimation;
    private int upwardsFallingTicks = 0;
    private boolean isUpsideDownNavigator;
    private boolean prevOnGround = false;

    public EntityDropBear(World worldIn) {
        super(worldIn);
        this.setSize(1.1F, 1.5F);
        switchNavigator(true);
    }

    public static BlockPos getLowestPos(World world, BlockPos pos) {
        while (!world.getBlockState(pos).isSideSolid(world, pos, EnumFacing.DOWN) && pos.getY() < 255) {
            pos = pos.up();
        }
        return pos;
    }

    @Override
    public boolean getCanSpawnHere() {
        return AMEntityRegistry.rollSpawn(AMConfig.dropbearSpawnRolls, this.getRNG(), AMEntityRegistry.AMSpawnReason.OTHER) && super.getCanSpawnHere();
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(22.0D);
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(20.0D);
        this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(0.7D);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(2.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.25D);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return AMSoundRegistry.DROPBEAR_IDLE;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return AMSoundRegistry.DROPBEAR_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return AMSoundRegistry.DROPBEAR_HURT;
    }

    @Override
    public boolean attackEntityAsMob(Entity entityIn) {
        if (this.getAnimation() == NO_ANIMATION) {
            this.setAnimation(rand.nextBoolean() ? ANIMATION_BITE : rand.nextBoolean() ? ANIMATION_SWIPE_L : ANIMATION_SWIPE_R);
        }
        return true;
    }

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(1, new AIDropMelee());
        this.tasks.addTask(2, new AIUpsideDownWander());
        this.tasks.addTask(6, new EntityAILookIdle(this));
        this.tasks.addTask(7, new EntityAIWatchClosest(this, EntityLivingBase.class, 30.0F));
        this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, true, EntityDropBear.class));
        this.targetTasks.addTask(2, new EntityAINearestTarget3D(this, EntityPlayer.class, true) {
            @Override
            protected AxisAlignedBB getTargetableArea(double targetDistance) {
                AxisAlignedBB bb = this.taskOwner.getEntityBoundingBox().grow(targetDistance, targetDistance, targetDistance);
                return new AxisAlignedBB(bb.minX, 0, bb.minZ, bb.maxX, 256, bb.maxZ);
            }
        });
        this.targetTasks.addTask(2, new EntityAINearestTarget3D(this, EntityVillager.class, true) {
            @Override
            protected AxisAlignedBB getTargetableArea(double targetDistance) {
                AxisAlignedBB bb = this.taskOwner.getEntityBoundingBox().grow(targetDistance, targetDistance, targetDistance);
                return new AxisAlignedBB(bb.minX, 0, bb.minZ, bb.maxX, 256, bb.maxZ);
            }
        });
    }

    @Override
    public boolean isEntityInvulnerable(DamageSource source) {
        return super.isEntityInvulnerable(source) || source == DamageSource.FALL || source == DamageSource.IN_WALL;
    }

    @Override
    protected void updateFallState(double y, boolean onGroundIn, IBlockState state, BlockPos pos) {
        super.updateFallState(y, onGroundIn, state, pos);
    }

    @Override
    public void fall(float distance, float damageMultiplier) {
        this.onLand();
        super.fall(distance, damageMultiplier);
    }

    private void switchNavigator(boolean rightsideUp) {
        if (rightsideUp) {
            this.moveHelper = new EntityMoveHelper(this);
            this.navigator = new GroundPathNavigatorWide(this, world);
            this.isUpsideDownNavigator = false;
        } else {
            this.moveHelper = new FlightMoveController(this, 1.1F, false);
            this.navigator = new DirectPathNavigator(this, world);
            this.isUpsideDownNavigator = true;
        }
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        AMEntityRegistry.updateAnimations(this);
        prevUpsideDownProgress = upsideDownProgress;
        if (this.isUpsideDown() && upsideDownProgress < 5F) {
            upsideDownProgress++;
        }
        if (!this.isUpsideDown() && upsideDownProgress > 0F) {
            upsideDownProgress--;
        }
        if (!world.isRemote) {
            BlockPos abovePos = this.getPosition().up();
            IBlockState aboveState = world.getBlockState(abovePos);
            BlockPos underneathPos = this.getPosition().down();
            IBlockState belowState = world.getBlockState(underneathPos);
            BlockPos worldHeight = world.getHeight(this.getPosition());
            boolean validAboveState = aboveState.isSideSolid(world, abovePos, EnumFacing.DOWN);
            boolean validBelowState = belowState.isSideSolid(world, underneathPos, EnumFacing.UP);
            EntityLivingBase attackTarget = this.getAttackTarget();
            if (attackTarget != null && getDistance(attackTarget) < attackTarget.width + this.width + 1 && this.canEntityBeSeen(attackTarget)) {
                if (this.getAnimation() == ANIMATION_BITE && this.getAnimationTick() == 6) {
                    attackTarget.knockBack(this, 0.5F, this.posX - attackTarget.posX, this.posZ - attackTarget.posZ);
                    attackTarget.attackEntityFrom(DamageSource.causeMobDamage(this), (float) this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue());
                }
                if ((this.getAnimation() == ANIMATION_SWIPE_L) && this.getAnimationTick() == 9) {
                    float rot = rotationYaw + 90;
                    attackTarget.knockBack(this, 0.5F, MathHelper.sin(rot * ((float) Math.PI / 180F)), -MathHelper.cos(rot * ((float) Math.PI / 180F)));
                    attackTarget.attackEntityFrom(DamageSource.causeMobDamage(this), (float) this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue());
                }
                if ((this.getAnimation() == ANIMATION_SWIPE_R) && this.getAnimationTick() == 9) {
                    float rot = rotationYaw - 90;
                    attackTarget.knockBack(this, 0.5F, MathHelper.sin(rot * ((float) Math.PI / 180F)), -MathHelper.cos(rot * ((float) Math.PI / 180F)));
                    attackTarget.attackEntityFrom(DamageSource.causeMobDamage(this), (float) this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue());
                }
            }
            if ((attackTarget == null || !attackTarget.isEntityAlive()) && rand.nextInt(300) == 0 && this.onGround && !this.isUpsideDown() && this.posY + 2 < worldHeight.getY()) {
                if (this.getAnimation() == NO_ANIMATION) {
                    this.setAnimation(ANIMATION_JUMPUP);
                }
            }
            if (jumpingUp && this.posY > worldHeight.getY()) {
                jumpingUp = false;
            }
            if ((this.onGround && this.getAnimation() == ANIMATION_JUMPUP && this.getAnimationTick() > 10 || jumpingUp && this.getAnimation() == NO_ANIMATION)) {
                this.motionY += 2.0D;
                jumpingUp = true;
            }
            if (this.isUpsideDown()) {
                jumpingUp = false;
                this.setNoGravity(!this.onGround);
                float f = 0.91F;
                this.motionX *= f;
                this.motionZ *= f;
                if (!this.collidedVertically) {
                    if (this.onGround || validBelowState || upwardsFallingTicks > 5) {
                        this.setUpsideDown(false);
                        upwardsFallingTicks = 0;
                    } else {
                        if (!validAboveState) {
                            upwardsFallingTicks++;
                        }
                        this.motionY += 0.2D;
                    }
                } else {
                    upwardsFallingTicks = 0;
                }
                if (this.collidedHorizontally) {
                    upwardsFallingTicks = 0;
                    this.motionY += -0.3D;
                }
                if (this.isEntityInsideOpaqueBlock() && world.isAirBlock(this.getPositionUnderneath())) {
                    this.setPosition(this.posX, this.posY - 1, this.posZ);
                }
            } else {
                this.setNoGravity(false);
                if (validAboveState) {
                    this.setUpsideDown(true);
                }
            }
            if (this.isUpsideDown() && !this.isUpsideDownNavigator) {
                switchNavigator(false);
            }
            if (!this.isUpsideDown() && this.isUpsideDownNavigator) {
                switchNavigator(true);
            }
        }
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(UPSIDE_DOWN, Boolean.FALSE);
    }

    public boolean isUpsideDown() {
        return this.dataManager.get(UPSIDE_DOWN);
    }

    public void setUpsideDown(boolean upsideDown) {
        this.dataManager.set(UPSIDE_DOWN, upsideDown);
    }

    protected BlockPos getPositionAbove() {
        return new BlockPos(this.getPositionVector().x, this.getEntityBoundingBox().maxY + 0.5000001D, this.getPositionVector().z);
    }

    protected BlockPos getPositionUnderneath() {
        return new BlockPos(this.posX, this.posY - 1.0D, this.posZ);
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
    }

    @Override
    public Animation[] getAnimations() {
        return new Animation[]{ANIMATION_BITE, ANIMATION_SWIPE_L, ANIMATION_SWIPE_R, ANIMATION_JUMPUP};
    }

    private boolean canSeeBlock(BlockPos destinationBlock) {
        Vec3d start = new Vec3d(this.posX, this.getPositionEyes(1.0F).y, this.posZ);
        Vec3d blockVec = new Vec3d(destinationBlock.getX() + 0.5D, destinationBlock.getY() + 0.5D, destinationBlock.getZ() + 0.5D);
        RayTraceResult result = this.world.rayTraceBlocks(start, blockVec, false, true, false);
        return result != null && result.getBlockPos().equals(destinationBlock);
    }

    private void doInitialPosing(World world) {
        BlockPos upperPos = this.getPositionAbove().up();
        BlockPos highest = getLowestPos(world, upperPos);
        if (highest.getY() >= 250 || highest.getY() <= upperPos.getY()) {
            return;
        }
        this.setPosition(highest.getX() + 0.5D, highest.getY(), highest.getZ() + 0.5D);
    }

    private boolean hasCeilingToHangFrom() {
        BlockPos scan = this.getPosition().up(2);
        int limit = Math.min(255, scan.getY() + 32);
        for (int y = scan.getY(); y < limit; y++) {
            BlockPos check = new BlockPos(scan.getX(), y, scan.getZ());
            if (this.world.getBlockState(check).isSideSolid(this.world, check, EnumFacing.DOWN)) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    @Override
    public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData livingdata) {
        if (!this.world.isRemote && this.hasCeilingToHangFrom()) {
            doInitialPosing(this.world);
        }
        return super.onInitialSpawn(difficulty, livingdata);
    }

    private void onLand() {
        if (!world.isRemote) {
            world.setEntityState(this, (byte) 39);
            for (Entity entity : this.world.getEntitiesWithinAABB(EntityLivingBase.class, this.getEntityBoundingBox().grow(2.5D))) {
                if (!isOnSameTeam(entity) && !(entity instanceof EntityDropBear) && entity != this) {
                    entity.attackEntityFrom(DamageSource.causeMobDamage(this), 2.0F + rand.nextFloat() * 5F);
                    launch(entity, true);
                }
            }
        }
    }

    private void launch(Entity e, boolean huge) {
        if (e.onGround) {
            double d0 = e.posX - this.posX;
            double d1 = e.posZ - this.posZ;
            double d2 = Math.max(d0 * d0 + d1 * d1, 0.001D);
            float f = 0.5F;
            e.addVelocity(d0 / d2 * f, huge ? 0.5D : 0.2D, d1 / d2 * f);
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void handleStatusUpdate(byte id) {
        if (id == 39) {
            spawnGroundEffects();
        } else {
            super.handleStatusUpdate(id);
        }
    }

    public void spawnGroundEffects() {
        float radius = 2.3F;
        if (world.isRemote) {
            for (int i1 = 0; i1 < 20 + rand.nextInt(12); i1++) {
                double motionX = getRNG().nextGaussian() * 0.07D;
                double motionY = getRNG().nextGaussian() * 0.07D;
                double motionZ = getRNG().nextGaussian() * 0.07D;
                float angle = (0.01745329251F * this.renderYawOffset) + i1;
                double extraX = radius * MathHelper.sin((float) (Math.PI + angle));
                double extraY = 0.8F;
                double extraZ = radius * MathHelper.cos(angle);
                BlockPos ground = getGroundPosition(new BlockPos(MathHelper.floor(this.posX + extraX), this.posY, MathHelper.floor(this.posZ + extraZ)));
                IBlockState blockState = this.world.getBlockState(ground);
                if (blockState.getMaterial() != Material.AIR) {
                    world.spawnParticle(EnumParticleTypes.BLOCK_CRACK, true, this.posX + extraX, ground.getY() + extraY, this.posZ + extraZ, motionX, motionY, motionZ, Block.getStateId(blockState));
                }
            }
        }
    }

    private BlockPos getGroundPosition(BlockPos in) {
        BlockPos position = new BlockPos(in.getX(), this.posY, in.getZ());
        while (position.getY() > 2 && world.isAirBlock(position) && !world.getBlockState(position).getMaterial().isLiquid()) {
            position = position.down();
        }
        return position;
    }

    class AIUpsideDownWander extends EntityAIBase {
        private final double speed;
        private final int executionChance;
        protected double x;
        protected double y;
        protected double z;

        AIUpsideDownWander() {
            this.speed = 1.0D;
            this.executionChance = 50;
            this.setMutexBits(1);
        }

        @Nullable
        protected Vec3d getPosition() {
            if (EntityDropBear.this.isUpsideDown()) {
                for (int i = 0; i < 15; i++) {
                    Random random = EntityDropBear.this.getRNG();
                    BlockPos randPos = EntityDropBear.this.getPosition().add(random.nextInt(16) - 8, -2, random.nextInt(16) - 8);
                    BlockPos lowestPos = EntityDropBear.getLowestPos(world, randPos);
                    if (world.getBlockState(lowestPos).isSideSolid(world, lowestPos, EnumFacing.DOWN)) {
                        return new Vec3d(lowestPos.getX() + 0.5D, lowestPos.getY() + 0.5D, lowestPos.getZ() + 0.5D);
                    }
                }
                return null;
            } else {
                return RandomPositionGenerator.findRandomTarget(EntityDropBear.this, 10, 7);
            }
        }

        @Override
        public boolean shouldExecute() {
            if (EntityDropBear.this.isRiding() || EntityDropBear.this.isBeingRidden()) {
                return false;
            }
            if (EntityDropBear.this.getRNG().nextInt(this.executionChance) != 0) {
                return false;
            }
            Vec3d vec = this.getPosition();
            if (vec == null) {
                return false;
            }
            this.x = vec.x;
            this.y = vec.y;
            this.z = vec.z;
            return true;
        }

        @Override
        public boolean shouldContinueExecuting() {
            if (EntityDropBear.this.isUpsideDown()) {
                double d0 = EntityDropBear.this.posX - x;
                double d2 = EntityDropBear.this.posZ - z;
                double d4 = d0 * d0 + d2 * d2;
                return d4 > 4;
            } else {
                return !EntityDropBear.this.getNavigator().noPath();
            }
        }

        @Override
        public void resetTask() {
            this.x = 0;
            this.y = 0;
            this.z = 0;
        }

        @Override
        public void startExecuting() {
            if (EntityDropBear.this.isUpsideDown()) {
                EntityDropBear.this.getMoveHelper().setMoveTo(this.x, this.y, this.z, this.speed * 0.7F);
            } else {
                EntityDropBear.this.getNavigator().tryMoveToXYZ(this.x, this.y, this.z, this.speed);
            }
        }

        @Override
        public void updateTask() {
            if (EntityDropBear.this.isUpsideDown()) {
                EntityDropBear.this.getMoveHelper().setMoveTo(this.x, this.y, this.z, this.speed * 0.7F);
            } else {
                EntityDropBear.this.getNavigator().tryMoveToXYZ(this.x, this.y, this.z, this.speed);
            }
        }
    }

    private class AIDropMelee extends EntityAIBase {

        AIDropMelee() {
            this.setMutexBits(3);
        }

        @Override
        public boolean shouldExecute() {
            return EntityDropBear.this.getAttackTarget() != null;
        }

        @Override
        public boolean shouldContinueExecuting() {
            return shouldExecute();
        }

        @Override
        public void updateTask() {
            EntityLivingBase target = EntityDropBear.this.getAttackTarget();
            if (target != null) {
                double dist = EntityDropBear.this.getDistance(target);
                if (EntityDropBear.this.isUpsideDown()) {
                    double d0 = EntityDropBear.this.posX - target.posX;
                    double d2 = EntityDropBear.this.posZ - target.posZ;
                    double xzDistSqr = d0 * d0 + d2 * d2;
                    BlockPos ceilingPos = new BlockPos(target.posX, EntityDropBear.this.posY - 3 - rand.nextInt(3), target.posZ);
                    BlockPos lowestPos = EntityDropBear.getLowestPos(world, ceilingPos);
                    EntityDropBear.this.getMoveHelper().setMoveTo(lowestPos.getX() + 0.5D, ceilingPos.getY(), lowestPos.getZ() + 0.5D, 1.1D);
                    if (xzDistSqr < 2.5F) {
                        EntityDropBear.this.setUpsideDown(false);
                    }
                } else {
                    if (EntityDropBear.this.onGround) {
                        EntityDropBear.this.getNavigator().tryMoveToEntityLiving(target, 1.2D);
                    }
                }
                if (dist < 3D) {
                    EntityDropBear.this.attackEntityAsMob(target);
                }
            }
        }
    }
}
