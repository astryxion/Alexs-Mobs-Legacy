package com.github.alexthe666.alexsmobs.entity;
import com.github.alexthe666.alexsmobs.misc.AMLootTables;

import com.github.alexthe666.alexsmobs.config.AMConfig;
import com.github.alexthe666.alexsmobs.entity.ai.FlightMoveController;
import com.github.alexthe666.alexsmobs.entity.ai.FlyingEntityAITempt;
import com.github.alexthe666.alexsmobs.misc.AMSoundRegistry;
import com.github.alexthe666.alexsmobs.misc.AMTagRegistry;
import com.google.common.base.Predicate;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAIFollowParent;
import net.minecraft.entity.ai.EntityAIMate;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIAvoidEntity;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathNavigateFlying;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import net.minecraft.util.ResourceLocation;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class EntityFly extends EntityAnimal {

    private static final DataParameter<Boolean> NO_DESPAWN = EntityDataManager.createKey(EntityFly.class, DataSerializers.BOOLEAN);
    private int conversionTime = 0;

    public EntityFly(World worldIn) {
        super(worldIn);
        this.moveHelper = new FlightMoveController(this, 0.8F, true, true);
        this.setPathPriority(PathNodeType.DANGER_FIRE, -1.0F);
        this.setPathPriority(PathNodeType.WATER, -1.0F);
        this.setPathPriority(PathNodeType.WATER, 16.0F);
        this.setPathPriority(PathNodeType.FENCE, -1.0F);
        this.setSize(0.5F, 0.5F);
    }

    protected void playStepSound(BlockPos pos, net.minecraft.block.Block blockIn) {
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setBoolean("NoFlyDespawn", this.isNoDespawn());
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.setNoDespawn(compound.getBoolean("NoFlyDespawn"));
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(NO_DESPAWN, Boolean.FALSE);
    }

    public boolean isNoDespawn() {
        return this.dataManager.get(NO_DESPAWN);
    }

    public void setNoDespawn(boolean despawn) {
        this.dataManager.set(NO_DESPAWN, despawn);
    }

    @Override
    protected boolean canDespawn() {
        return !this.isNoDespawn() && super.canDespawn();
    }

    /**
     * 1.16 {@code EntityFly#canFlySpawn}: Y &gt; 63, {@code nextInt(4) == 0}, sky light &gt; 8,
     * no block light, sand or dirt/grass. The 1/4 roll lives here so ambient spawning still uses
     * it even if placement is skipped. Pass {@code applyRarityRoll = false} from placement.
     */
    public static boolean canFlySpawnAt(World world, BlockPos pos, java.util.Random random, boolean applyRarityRoll) {
        if (pos.getY() <= 63) {
            return false;
        }
        if (applyRarityRoll && random.nextInt(4) != 0) {
            return false;
        }
        if (world.getLightFromNeighbors(pos) <= 8) {
            return false;
        }
        if (world.getLightFor(EnumSkyBlock.BLOCK, pos) != 0) {
            return false;
        }
        Block down = world.getBlockState(pos.down()).getBlock();
        return down == Blocks.SAND || down == Blocks.DIRT || down == Blocks.GRASS
                || down == Blocks.GRASS_PATH || down == Blocks.FARMLAND || down == Blocks.MYCELIUM;
    }

    @Override
    public boolean getCanSpawnHere() {
        return AMEntityRegistry.rollSpawn(AMConfig.flySpawnRolls, this.getRNG(), AMEntityRegistry.AMSpawnReason.OTHER)
                && canFlySpawnAt(this.world, this.getPosition(), this.rand, true);
    }

    public boolean isInNether() {
        return this.world.provider.getDimension() == -1 && !this.isAIDisabled();
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return AMSoundRegistry.FLY_IDLE;
    }

    @Override
    public int getTalkInterval() {
        return 30;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return AMSoundRegistry.FLY_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return AMSoundRegistry.FLY_HURT;
    }
    @Override
    @Nullable
    protected ResourceLocation getLootTable() {
        return AMLootTables.FLY;
    }

    @Override
    public int getMaxSpawnedInChunk() {
        return 2;
    }

    @Override
    public float getBlockPathWeight(BlockPos pos) {
        return this.world.isAirBlock(pos) ? 10.0F : 0.0F;
    }

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(1, new EntityAIMate(this, 1.0D));
        this.tasks.addTask(2, new FlyingEntityAITempt(this, 1.25D, false, com.google.common.collect.Sets.newHashSet(Items.ROTTEN_FLESH, Items.SUGAR)));
        this.tasks.addTask(3, new EntityAIFollowParent(this, 1.25D));
        this.tasks.addTask(3, new EntityAIAvoidEntity(this, EntitySpider.class, 6.0F, 1.0D, 1.2D));
        this.tasks.addTask(4, new AnnoyZombieGoal());
        this.tasks.addTask(5, new WanderGoal());
        this.tasks.addTask(6, new EntityAISwimming(this));
    }

    @Override
    protected PathNavigate createNavigator(World worldIn) {
        PathNavigateFlying flyingpathnavigator = new PathNavigateFlying(this, worldIn) {
            @Override
            public boolean canEntityStandOnPos(BlockPos pos) {
                return !this.world.isAirBlock(pos.down());
            }
        };
        flyingpathnavigator.setCanOpenDoors(false);
        flyingpathnavigator.setCanFloat(false);
        flyingpathnavigator.setCanEnterDoors(true);
        return flyingpathnavigator;
    }

    @Override
    public float getEyeHeight() {
        return this.isChild() ? this.height * 0.5F : this.height * 0.5F;
    }

    @Override
    public void fall(float distance, float damageMultiplier) {
    }

    @Override
    protected void updateFallState(double y, boolean onGroundIn, IBlockState state, BlockPos pos) {
        this.fallDistance = 0.0F;
    }

    @Override
    public boolean hasNoGravity() {
        return true;
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        if (!this.onGround && this.motionY < 0.0D) {
            this.motionY *= 0.4D;
        }
        this.setNoGravity(true);
        if (this.isChild() && this.getEyeHeight() > this.height) {
            this.setSize(this.width, this.getEyeHeight());
        }
        if (this.isInLove() && !this.isNoDespawn()) {
            this.setNoDespawn(true);
        }
        if (this.isInWater()) {
            this.motionY += 0.01D;
        }
        if (isInNether()) {
            this.setNoDespawn(true);
            conversionTime++;
            if (conversionTime > 300) {
                EntityCrimsonMosquito mosquito = (EntityCrimsonMosquito) AMEntityRegistry.CRIMSON_MOSQUITO.newInstance(world);
                mosquito.setLocationAndAngles(this.posX, this.posY, this.posZ, this.rotationYaw, this.rotationPitch);
                if (!world.isRemote) {
                    mosquito.onInitialSpawn(this.world.getDifficultyForLocation(this.getPosition()), (net.minecraft.entity.IEntityLivingData) null);
                }
                world.spawnEntity(mosquito);
                mosquito.onSpawnFromFly();
                this.setDead();
            }
        }
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (stack.getItem() == Items.SUGAR) {
            if (!player.capabilities.isCreativeMode) {
                stack.shrink(1);
            }
            this.setNoDespawn(true);
            this.heal(2.0F);
            return true;
        }
        return super.processInteract(player, hand);
    }

    @Override
    public EnumCreatureAttribute getCreatureAttribute() {
        return EnumCreatureAttribute.ARTHROPOD;
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        return stack.getItem() == Items.ROTTEN_FLESH;
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        AMEntityRegistry.registerAttributeIfAbsent(this, SharedMonsterAttributes.ATTACK_DAMAGE);
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(2.0D);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(1.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.25D);
    }

    @Override
    @Nullable
    public EntityAgeable createChild(EntityAgeable ageable) {
        return (EntityFly) AMEntityRegistry.FLY.newInstance(this.world);
    }

    @SideOnly(Side.CLIENT)
    public double getMountedYOffset() {
        return 0.5F * this.getEyeHeight();
    }

    private class WanderGoal extends EntityAIBase {

        WanderGoal() {
            this.setMutexBits(1);
        }

        @Override
        public boolean shouldExecute() {
            return EntityFly.this.getNavigator().noPath() && EntityFly.this.rand.nextInt(3) == 0;
        }

        @Override
        public boolean shouldContinueExecuting() {
            return !EntityFly.this.getNavigator().noPath();
        }

        @Override
        public void startExecuting() {
            Vec3d vector3d = this.getRandomLocation();
            if (vector3d != null) {
                Path path = EntityFly.this.getNavigator().getPathToPos(new BlockPos(vector3d));
                if (path != null) {
                    EntityFly.this.getNavigator().setPath(path, 1.0D);
                }
            }
        }

        @Nullable
        private Vec3d getRandomLocation() {
            Vec3d vector3d = EntityFly.this.getLook(0.0F);
            Vec3d vector3d2 = this.findAirTarget(vector3d);
            return vector3d2 != null ? vector3d2 : RandomPositionGenerator.findRandomTargetBlockTowards(EntityFly.this, 3, 3, vector3d);
        }

        @Nullable
        private Vec3d findAirTarget(Vec3d direction) {
            PathNavigate nav = EntityFly.this.getNavigator();
            BlockPos origin = EntityFly.this.getPosition();
            Vec3d bestPos = null;
            float bestWeight = -99999.0F;
            for (int attempt = 0; attempt < 10; ++attempt) {
                BlockPos pos = origin.add(
                        EntityFly.this.rand.nextInt(7) - 3,
                        EntityFly.this.rand.nextInt(7) - 3,
                        EntityFly.this.rand.nextInt(7) - 3);
                if (nav.canEntityStandOnPos(pos)) {
                    float weight = EntityFly.this.getBlockPathWeight(pos);
                    if (direction != null) {
                        double dx = (pos.getX() + 0.5D) - EntityFly.this.posX;
                        double dy = pos.getY() - EntityFly.this.posY;
                        double dz = (pos.getZ() + 0.5D) - EntityFly.this.posZ;
                        double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
                        if (len > 0.0D) {
                            weight += (float) ((dx / len) * direction.x + (dy / len) * direction.y + (dz / len) * direction.z) * 2.0F;
                        }
                    }
                    if (weight > bestWeight) {
                        bestWeight = weight;
                        bestPos = new Vec3d(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D);
                    }
                }
            }
            return bestPos;
        }
    }

    private class AnnoyZombieGoal extends EntityAIBase {
        protected final Sorter theNearestAttackableTargetSorter;
        protected final Predicate<Entity> targetEntitySelector;
        protected int executionChance = 8;
        protected boolean mustUpdate;
        private Entity targetEntity;
        private int cooldown = 0;

        AnnoyZombieGoal() {
            this.setMutexBits(1);
            this.theNearestAttackableTargetSorter = new Sorter(EntityFly.this);
            this.targetEntitySelector = new Predicate<Entity>() {
                @Override
                public boolean apply(@Nullable Entity e) {
                    return e != null && e.isEntityAlive()
                            && AMTagRegistry.entityMatchesEntityTypeTag(AMTagRegistry.FLY_TARGETS, e)
                            && (!(e instanceof EntityLivingBase) || ((EntityLivingBase) e).getHealth() >= 2.0F);
                }
            };
        }

        @Override
        public boolean shouldExecute() {
            if (EntityFly.this.isRiding() || EntityFly.this.isBeingRidden()) {
                return false;
            }
            if (!this.mustUpdate) {
                long worldTime = EntityFly.this.world.getTotalWorldTime() % 10;
                if (EntityFly.this.getIdleTime() >= 100 && worldTime != 0) {
                    return false;
                }
                if (EntityFly.this.getRNG().nextInt(this.executionChance) != 0 && worldTime != 0) {
                    return false;
                }
            }
            List<Entity> list = EntityFly.this.world.getEntitiesWithinAABB(Entity.class, this.getTargetableArea(this.getTargetDistance()), this.targetEntitySelector);
            if (list.isEmpty()) {
                return false;
            } else {
                Collections.sort(list, this.theNearestAttackableTargetSorter);
                this.targetEntity = list.get(0);
                this.mustUpdate = false;
                return true;
            }
        }

        @Override
        public boolean shouldContinueExecuting() {
            return targetEntity != null;
        }

        @Override
        public void resetTask() {
            this.targetEntity = null;
        }

        @Override
        public void updateTask() {
            if (cooldown > 0) {
                cooldown--;
            }
            if (targetEntity != null) {
                if (EntityFly.this.getNavigator().noPath()) {
                    int i = EntityFly.this.getRNG().nextInt(3) - 1;
                    int k = EntityFly.this.getRNG().nextInt(3) - 1;
                    int l = (int) ((EntityFly.this.getRNG().nextInt(3) - 1) * Math.ceil(targetEntity.height));
                    EntityFly.this.getNavigator().tryMoveToXYZ(this.targetEntity.posX + i, this.targetEntity.posY + l, this.targetEntity.posZ + k, 1.0D);
                }
                if (EntityFly.this.getDistanceSq(targetEntity) < 3.0F) {
                    if (targetEntity instanceof EntityLivingBase && ((EntityLivingBase) targetEntity).getHealth() > 2.0F) {
                        if (cooldown == 0) {
                            targetEntity.attackEntityFrom(DamageSource.GENERIC, 1.0F);
                            cooldown = 100;
                        }
                    } else {
                        this.resetTask();
                    }
                }
            }
        }

        protected double getTargetDistance() {
            return 16D;
        }

        protected net.minecraft.util.math.AxisAlignedBB getTargetableArea(double targetDistance) {
            double renderRadius = 5;
            return new net.minecraft.util.math.AxisAlignedBB(-renderRadius, -renderRadius, -renderRadius, renderRadius, renderRadius, renderRadius)
                    .offset(EntityFly.this.posX + 0.5D, EntityFly.this.posY + 0.5D, EntityFly.this.posZ + 0.5D);
        }

        public class Sorter implements Comparator<Entity> {
            private final Entity theEntity;

            public Sorter(Entity theEntityIn) {
                this.theEntity = theEntityIn;
            }

            public int compare(Entity p_compare_1_, Entity p_compare_2_) {
                double d0 = this.theEntity.getDistanceSq(p_compare_1_);
                double d1 = this.theEntity.getDistanceSq(p_compare_2_);
                return Double.compare(d0, d1);
            }
        }
    }
}
