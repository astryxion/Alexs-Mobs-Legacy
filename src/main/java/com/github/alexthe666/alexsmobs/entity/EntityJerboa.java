package com.github.alexthe666.alexsmobs.entity;

import com.github.alexthe666.alexsmobs.config.AMConfig;
import com.github.alexthe666.alexsmobs.effect.AMEffectRegistry;
import com.github.alexthe666.alexsmobs.entity.ai.AnimalAIWanderRanged;
import com.github.alexthe666.alexsmobs.entity.ai.JerboaAIBeg;
import com.github.alexthe666.alexsmobs.misc.AMSoundRegistry;
import com.github.alexthe666.alexsmobs.misc.AMTagRegistry;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAvoidEntity;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIMate;
import net.minecraft.entity.ai.EntityAIPanic;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.ai.EntityJumpHelper;
import net.minecraft.entity.ai.EntityMoveHelper;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityOcelot;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.Path;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.Random;

public class EntityJerboa extends EntityAnimal {

    private static final DataParameter<Boolean> JUMP_ACTIVE = EntityDataManager.createKey(EntityJerboa.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> BEGGING = EntityDataManager.createKey(EntityJerboa.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> SLEEPING = EntityDataManager.createKey(EntityJerboa.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> BEFRIENDED = EntityDataManager.createKey(EntityJerboa.class, DataSerializers.BOOLEAN);
    public float jumpProgress;
    public float prevJumpProgress;
    public float reboundProgress;
    public float prevReboundProgress;
    public float begProgress;
    public float prevBegProgress;
    public float sleepProgress;
    public float prevSleepProgress;
    private int jumpTicks;
    private int jumpDuration;
    private boolean wasOnGround;
    private int currentMoveTypeDuration;

    public EntityJerboa(World world) {
        super(world);
        this.setSize(0.5F, 0.5F);
        this.moveHelper = new MoveHelperController(this);
        this.jumpHelper = new JumpHelperController(this);
    }

    public static boolean isValidLightLevel(World world, BlockPos pos, Random random) {
        if (world.getLightFor(EnumSkyBlock.SKY, pos) > random.nextInt(32)) {
            return false;
        }
        int light = world.getLight(pos);
        return light <= random.nextInt(8);
    }

    public static boolean canJerboaSpawn(World world, BlockPos pos, Random random) {
        return world.canSeeSky(pos.up()) && isValidLightLevel(world, pos, random)
                && net.minecraft.entity.EntityLiving.SpawnPlacementType.ON_GROUND.canSpawnAt(world, pos);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(4.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.45D);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(JUMP_ACTIVE, false);
        this.dataManager.register(BEGGING, false);
        this.dataManager.register(SLEEPING, false);
        this.dataManager.register(BEFRIENDED, false);
    }

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(0, new JerboaAIBeg(this, 1.0D));
        this.tasks.addTask(1, new EntityAIMate(this, 1.0D));
        this.tasks.addTask(2, new EntityAIAvoidEntity<EntityPlayer>(this, EntityPlayer.class, 5.0F, 1.3D, 1.0D) {
            @Override
            public boolean shouldExecute() {
                return !EntityJerboa.this.isBefriended() && super.shouldExecute();
            }
        });
        this.tasks.addTask(3, new EntityAIAvoidEntity<>(this, EntityOcelot.class, 9.0F, 1.3D, 1.0D));
        this.tasks.addTask(4, new EntityAIAvoidEntity<>(this, EntityRattlesnake.class, 9.0F, 1.3D, 1.0D));
        this.tasks.addTask(5, new EntityAIPanic(this, 1.1D));
        this.tasks.addTask(6, new AnimalAIWanderRanged(this, 20, 1.0D, 10, 7));
        this.tasks.addTask(7, new EntityAIWatchClosest(this, EntityPlayer.class, 10.0F));
        this.tasks.addTask(8, new EntityAILookIdle(this));
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.setBefriended(compound.getBoolean("Befriended"));
        this.setSleeping(compound.getBoolean("Sleeping"));
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setBoolean("Befriended", this.isBefriended());
        compound.setBoolean("Sleeping", this.isSleeping());
    }

    @Override
    protected boolean canDespawn() {
        return !this.isBefriended() && super.canDespawn();
    }

    @Override
    public boolean getCanSpawnHere() {
        return AMEntityRegistry.rollSpawn(AMConfig.jerboaSpawnRolls, this.getRNG(), AMEntityRegistry.AMSpawnReason.OTHER)
                && canJerboaSpawn(this.world, this.getPosition(), this.getRNG());
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return AMSoundRegistry.JERBOA_IDLE;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return AMSoundRegistry.JERBOA_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return AMSoundRegistry.JERBOA_HURT;
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        this.prevJumpProgress = jumpProgress;
        this.prevReboundProgress = reboundProgress;
        this.prevSleepProgress = sleepProgress;
        this.prevBegProgress = begProgress;
        if (!this.world.isRemote) {
            this.dataManager.set(JUMP_ACTIVE, !this.onGround);
        }
        if (this.dataManager.get(JUMP_ACTIVE)) {
            if (jumpProgress < 5F) {
                jumpProgress += 1F;
                if (reboundProgress > 0) {
                    reboundProgress--;
                }
            }
            if (jumpProgress >= 5F) {
                if (reboundProgress < 5F) {
                    reboundProgress += 1;
                }
            }
        } else {
            if (reboundProgress > 0) {
                reboundProgress = Math.max(reboundProgress - 1F, 0);
            }
            if (jumpProgress > 0) {
                jumpProgress = Math.max(jumpProgress - 1F, 0);
            }
        }

        if (this.isBegging()) {
            if (begProgress < 5F) {
                begProgress++;
            }
        } else if (begProgress > 0F) {
            begProgress--;
        }

        if (this.isSleeping()) {
            if (sleepProgress < 5F) {
                sleepProgress++;
            }
        } else if (sleepProgress > 0F) {
            sleepProgress--;
        }

        if (!this.world.isRemote) {
            if (this.world.isDaytime() && this.getRevengeTarget() == null && !this.isBegging()) {
                if (this.ticksExisted % 10 == 0 && this.getRNG().nextInt(750) == 0) {
                    this.setSleeping(true);
                }
            } else if (this.isSleeping()) {
                this.setSleeping(false);
            }
        }
        if (this.jumpTicks != this.jumpDuration) {
            ++this.jumpTicks;
        } else if (this.jumpDuration != 0) {
            this.jumpTicks = 0;
            this.jumpDuration = 0;
            this.setJumping(false);
        }
    }

    public boolean isBegging() {
        return this.dataManager.get(BEGGING);
    }

    public void setBegging(boolean begging) {
        this.dataManager.set(BEGGING, begging);
    }

    public boolean isSleeping() {
        return this.dataManager.get(SLEEPING);
    }

    public void setSleeping(boolean sleeping) {
        this.dataManager.set(SLEEPING, sleeping);
    }

    public boolean isBefriended() {
        return this.dataManager.get(BEFRIENDED);
    }

    public void setBefriended(boolean befriended) {
        this.dataManager.set(BEFRIENDED, befriended);
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        ItemStack itemstack = player.getHeldItem(hand);
        if ((AMTagRegistry.itemInTag(AMTagRegistry.JERBOA_BEGS_FOR, itemstack.getItem()) || this.isBreedingItem(itemstack))
                && (this.getHealth() < this.getMaxHealth() || !this.isBefriended())) {
            if (!player.capabilities.isCreativeMode) {
                itemstack.shrink(1);
            }
            this.setBefriended(true);
            this.heal(4.0F);
            return true;
        }
        boolean type = super.processInteract(player, hand);
        if (!type && !this.isBreedingItem(itemstack) && AMTagRegistry.itemInTag(AMTagRegistry.JERBOA_BEGS_FOR, itemstack.getItem())) {
            this.setSleeping(false);
            this.playSound(SoundEvents.ENTITY_PARROT_EAT, this.getSoundPitch(), this.getSoundVolume());
            if (!player.capabilities.isCreativeMode) {
                itemstack.shrink(1);
            }
            for (int i = 0; i < 6 + this.rand.nextInt(3); i++) {
                double d2 = this.rand.nextGaussian() * 0.02D;
                double d0 = this.rand.nextGaussian() * 0.02D;
                double d1 = this.rand.nextGaussian() * 0.02D;
                this.world.spawnParticle(EnumParticleTypes.ITEM_CRACK,
                        this.posX + (double) (this.rand.nextFloat() * this.width) - (double) this.width * 0.5F,
                        this.posY + this.height * 0.5F + (double) (this.rand.nextFloat() * this.height * 0.5F),
                        this.posZ + (double) (this.rand.nextFloat() * this.width) - (double) this.width * 0.5F,
                        d0, d1, d2, net.minecraft.item.Item.getIdFromItem(itemstack.getItem()));
            }
            if (this.rand.nextFloat() <= 0.3F) {
                player.addPotionEffect(new PotionEffect(AMEffectRegistry.FLEET_FOOTED, 12000));
            }
            return true;
        }
        return type;
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        boolean prev = super.attackEntityFrom(source, amount);
        if (prev) {
            this.setSleeping(false);
            Entity entity = source.getTrueSource();
            if (entity instanceof EntityLivingBase) {
                EntityLivingBase hurter = (EntityLivingBase) entity;
                if (hurter.isPotionActive(AMEffectRegistry.FLEET_FOOTED)) {
                    hurter.removePotionEffect(AMEffectRegistry.FLEET_FOOTED);
                }
            }
        }
        return prev;
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        return AMTagRegistry.itemInTag(AMTagRegistry.JERBOA_BREEDABLES, stack.getItem());
    }

    public boolean shouldMove() {
        return !this.isSleeping();
    }

    public float getJumpCompletion(float partialTicks) {
        return this.jumpDuration == 0 ? 0.0F : ((float) this.jumpTicks + partialTicks) / (float) this.jumpDuration;
    }

    @Override
    protected float getJumpUpwardsMotion() {
        return this.collidedHorizontally ? super.getJumpUpwardsMotion() + 0.2F : 0.25F + this.rand.nextFloat() * 0.15F;
    }

    @Override
    public void fall(float distance, float damageMultiplier) {
    }

    @Override
    protected void updateFallState(double y, boolean onGroundIn, IBlockState state, BlockPos pos) {
    }

    @Override
    protected void jump() {
        super.jump();
        double d0 = this.moveHelper.getSpeed();
        if (d0 > 0.0D) {
            double d1 = this.motionX * this.motionX + this.motionZ * this.motionZ;
            if (d1 < 0.01D) {
                this.motionX *= 0.0D;
                this.motionZ *= 0.0D;
            }
        }
        if (!this.world.isRemote) {
            this.world.setEntityState(this, (byte) 1);
        }
    }

    public void setMovementSpeed(double newSpeed) {
        this.getNavigator().setSpeed(newSpeed);
        this.moveHelper.setMoveTo(this.moveHelper.getX(), this.moveHelper.getY(), this.moveHelper.getZ(), newSpeed);
    }

    public void startJumping() {
        this.setJumping(true);
        this.jumpDuration = 10;
        this.jumpTicks = 0;
    }

    private void checkLandingDelay() {
        this.updateMoveTypeDuration();
        this.disableJumpControl();
    }

    private void calculateRotationYaw(double x, double z) {
        this.rotationYaw = (float) (MathHelper.atan2(z - this.posZ, x - this.posX) * (180D / Math.PI)) - 90.0F;
    }

    private void enableJumpControl() {
        if (this.jumpHelper instanceof JumpHelperController) {
            ((JumpHelperController) this.jumpHelper).setCanJump(true);
        }
    }

    private void disableJumpControl() {
        if (this.jumpHelper instanceof JumpHelperController) {
            ((JumpHelperController) this.jumpHelper).setCanJump(false);
        }
    }

    private void updateMoveTypeDuration() {
        if (this.moveHelper.getSpeed() < 2.2D) {
            this.currentMoveTypeDuration = 2;
        } else {
            this.currentMoveTypeDuration = 1;
        }
    }

    @Override
    public void updateAITasks() {
        if (this.currentMoveTypeDuration > 0) {
            --this.currentMoveTypeDuration;
        }
        if (this.onGround && this.shouldMove()) {
            if (!this.wasOnGround) {
                this.setJumping(false);
                this.checkLandingDelay();
            }
            if (this.currentMoveTypeDuration == 0) {
                EntityLivingBase target = this.getAttackTarget();
                if (target != null && this.getDistanceSq(target) < 16.0D) {
                    this.calculateRotationYaw(target.posX, target.posZ);
                    this.moveHelper.setMoveTo(target.posX, target.posY, target.posZ, this.moveHelper.getSpeed());
                    this.startJumping();
                    this.wasOnGround = true;
                }
            }
            if (this.jumpHelper instanceof JumpHelperController) {
                JumpHelperController rabbitController = (JumpHelperController) this.jumpHelper;
                if (!rabbitController.getIsJumping()) {
                    if (this.moveHelper.isUpdating() && this.currentMoveTypeDuration == 0) {
                        Path path = this.getNavigator().getPath();
                        Vec3d vector3d = new Vec3d(this.moveHelper.getX(), this.moveHelper.getY(), this.moveHelper.getZ());
                        if (path != null && !path.isFinished()) {
                            vector3d = path.getPosition(this);
                        }
                        this.calculateRotationYaw(vector3d.x, vector3d.z);
                        this.startJumping();
                    }
                } else if (!rabbitController.canJump()) {
                    this.enableJumpControl();
                }
            }
        } else if (!this.shouldMove()) {
            this.setJumping(false);
            this.checkLandingDelay();
        }
        this.wasOnGround = this.onGround;
        super.updateAITasks();
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void handleStatusUpdate(byte id) {
        if (id == 1) {
            this.jumpDuration = 10;
            this.jumpTicks = 0;
        } else {
            super.handleStatusUpdate(id);
        }
    }

    @Nullable
    @Override
    public EntityJerboa createChild(EntityAgeable ageable) {
        EntityJerboa boa = new EntityJerboa(this.world);
        boa.setBefriended(true);
        return boa;
    }

    public boolean hasJumper() {
        return this.jumpHelper instanceof JumpHelperController;
    }

    static class MoveHelperController extends EntityMoveHelper {
        private final EntityJerboa jerboa;
        private double nextJumpSpeed;

        MoveHelperController(EntityJerboa jerboa) {
            super(jerboa);
            this.jerboa = jerboa;
        }

        @Override
        public void onUpdateMoveHelper() {
            if (this.jerboa.hasJumper() && this.jerboa.onGround && !this.jerboa.isJumping && !((JumpHelperController) this.jerboa.jumpHelper).getIsJumping()) {
                this.jerboa.setMovementSpeed(0.0D);
            } else if (this.isUpdating()) {
                this.jerboa.setMovementSpeed(this.nextJumpSpeed);
            }
            if (this.action == Action.MOVE_TO) {
                this.action = Action.WAIT;
                Vec3d vector3d = new Vec3d(this.posX - jerboa.posX, this.posY - jerboa.posY, this.posZ - jerboa.posZ);
                double d0 = vector3d.lengthVector();
                if (d0 < 1.0E-5F) {
                    return;
                }
                jerboa.motionX += vector3d.x / d0 * this.speed * 0.05D;
                jerboa.motionY += vector3d.y / d0 * this.speed * 0.05D;
                jerboa.motionZ += vector3d.z / d0 * this.speed * 0.05D;
            }
            super.onUpdateMoveHelper();
        }

        @Override
        public void setMoveTo(double x, double y, double z, double speedIn) {
            if (this.jerboa.isInWater()) {
                speedIn = 1.5D;
            }
            super.setMoveTo(x, y, z, speedIn);
            if (speedIn > 0.0D) {
                this.nextJumpSpeed = speedIn;
            }
        }
    }

    public static class JumpHelperController extends EntityJumpHelper {
        private final EntityJerboa jerboa;
        private boolean canJump;

        public JumpHelperController(EntityJerboa jerboa) {
            super(jerboa);
            this.jerboa = jerboa;
        }

        public boolean getIsJumping() {
            return this.isJumping;
        }

        public boolean canJump() {
            return this.canJump;
        }

        public void setCanJump(boolean canJumpIn) {
            this.canJump = canJumpIn;
        }

        @Override
        public void doJump() {
            if (this.isJumping) {
                this.jerboa.startJumping();
                this.isJumping = false;
            }
        }
    }
}
