package com.github.alexthe666.alexsmobs.entity;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import com.github.alexthe666.alexsmobs.block.BlockHummingbirdFeeder;
import com.github.alexthe666.alexsmobs.config.AMConfig;
import com.github.alexthe666.alexsmobs.entity.ai.FlightMoveController;
import com.github.alexthe666.alexsmobs.entity.ai.FlyingEntityAITempt;
import com.github.alexthe666.alexsmobs.entity.ai.HummingbirdAIPollinate;
import com.github.alexthe666.alexsmobs.entity.ai.HummingbirdAIWander;
import com.github.alexthe666.alexsmobs.misc.AMPointOfInterestRegistry;
import com.github.alexthe666.alexsmobs.misc.AMSoundRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFlower;
import net.minecraft.block.BlockLog;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathNavigateFlying;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;
import com.google.common.base.Optional;

public class EntityHummingbird extends EntityAnimal {

    private static final DataParameter<Boolean> FLYING = EntityDataManager.createKey(EntityHummingbird.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> VARIANT = EntityDataManager.createKey(EntityHummingbird.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> CROPS_POLLINATED = EntityDataManager.createKey(EntityHummingbird.class, DataSerializers.VARINT);
    private static final DataParameter<Optional<BlockPos>> FEEDER_POS = EntityDataManager.createKey(EntityHummingbird.class, DataSerializers.OPTIONAL_BLOCK_POS);
    public float flyProgress;
    public float prevFlyProgress;
    public float movingProgress;
    public float prevMovingProgress;
    public int hummingStill = 0;
    public int pollinateCooldown = 0;
    public int sipCooldown = 0;
    private int loopSoundTick = 0;
    private boolean sippy;
    public float sipProgress;
    public float prevSipProgress;

    public EntityHummingbird(World worldIn) {
        super(worldIn);
        this.moveHelper = new FlightMoveController(this, 1.5F);
        this.setPathPriority(PathNodeType.DANGER_FIRE, -1.0F);
        this.setPathPriority(PathNodeType.WATER, -1.0F);
        this.setPathPriority(PathNodeType.WATER, 16.0F);
        this.setPathPriority(PathNodeType.FENCE, -1.0F);
        this.setPathPriority(PathNodeType.OPEN, 0.0F);
        this.setSize(0.35F, 0.35F);
    }

    @Override
    public boolean getCanSpawnHere() {
        IBlockState blockstate = this.world.getBlockState(this.getPosition().down());
        Block block = blockstate.getBlock();
        boolean validGround = block == Blocks.GRASS || block == Blocks.TALLGRASS || block instanceof BlockLog
                || blockstate.getMaterial() == Material.LEAVES || this.world.isAirBlock(this.getPosition().down());
        return validGround && this.world.getLightFromNeighbors(this.getPosition()) > 8
                && AMEntityRegistry.rollSpawn(AMConfig.hummingbirdSpawnRolls, this.getRNG(), AMEntityRegistry.AMSpawnReason.OTHER) && super.getCanSpawnHere();
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return AMSoundRegistry.HUMMINGBIRD_IDLE;
    }

    @Override
    public int getTalkInterval() {
        return 60;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return AMSoundRegistry.HUMMINGBIRD_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return AMSoundRegistry.HUMMINGBIRD_HURT;
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        AMEntityRegistry.registerAttributeIfAbsent(this, SharedMonsterAttributes.ATTACK_DAMAGE);
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(4.0D);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(0.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.45D);
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        return stack.getItem() instanceof ItemBlock && ((ItemBlock) stack.getItem()).getBlock() instanceof BlockFlower;
    }

    @Override
    public int getMaxSpawnedInChunk() {
        return 7;
    }

    protected void initEntityAI() {
        this.tasks.addTask(1, new EntityAIMate(this, 1.0D));
        this.tasks.addTask(2, new FlyingEntityAITempt(this, 1.0D, false, java.util.Collections.<net.minecraft.item.Item>emptySet()) {
            @Override
            protected boolean isTempting(ItemStack stack) {
                return EntityHummingbird.this.isBreedingItem(stack);
            }
        });
        this.tasks.addTask(3, new EntityAIFollowParent(this, 1.0D));
        this.tasks.addTask(4, new AIUseFeeder());
        this.tasks.addTask(4, new HummingbirdAIPollinate(this));
        this.tasks.addTask(5, new HummingbirdAIWander(this, 16, 6, 15, 1.0F));
        this.tasks.addTask(6, new EntityAISwimming(this));
    }

    protected void playStepSound(BlockPos pos, net.minecraft.block.Block blockIn) {
    }

    @Override
    protected PathNavigate createNavigator(World worldIn) {
        PathNavigateFlying flyingpathnavigator = new PathNavigateFlying(this, worldIn) {
            @Override
            public boolean canEntityStandOnPos(BlockPos pos) {
                return !this.world.isAirBlock(pos.down(2));
            }
        };
        flyingpathnavigator.setCanOpenDoors(false);
        flyingpathnavigator.setCanFloat(false);
        flyingpathnavigator.setCanEnterDoors(true);
        return flyingpathnavigator;
    }

    @Override
    public void fall(float distance, float damageMultiplier) {
    }

    protected void updateFallState(double y, boolean onGroundIn, IBlockState state, BlockPos pos) {
        this.fallDistance = 0.0F;
    }

    @Override
    public float getEyeHeight() {
        return this.isChild() ? this.height * 0.5F : this.height * 0.5F;
    }

    public float getBlockPathWeight(BlockPos pos) {
        return this.world.isAirBlock(pos) ? 10.0F : 0.0F;
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setInteger("Variant", this.getVariant());
        compound.setInteger("CropsPollinated", this.getCropsPollinated());
        compound.setInteger("PollinateCooldown", this.pollinateCooldown);
        BlockPos blockpos = this.getFeederPos();
        if (blockpos != null) {
            compound.setInteger("HLPX", blockpos.getX());
            compound.setInteger("HLPY", blockpos.getY());
            compound.setInteger("HLPZ", blockpos.getZ());
        }
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.setVariant(compound.getInteger("Variant"));
        this.setCropsPollinated(compound.getInteger("CropsPollinated"));
        this.pollinateCooldown = compound.getInteger("PollinateCooldown");
        if (compound.hasKey("HLPX")) {
            int i = compound.getInteger("HLPX");
            int j = compound.getInteger("HLPY");
            int k = compound.getInteger("HLPZ");
            this.dataManager.set(FEEDER_POS, Optional.fromNullable(new BlockPos(i, j, k)));
        } else {
            this.dataManager.set(FEEDER_POS, Optional.absent());
        }
    }

    public BlockPos getFeederPos() {
        return this.dataManager.get(FEEDER_POS).orNull();
    }

    public void setFeederPos(BlockPos pos) {
        this.dataManager.set(FEEDER_POS, Optional.fromNullable(pos));
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(FLYING, Boolean.FALSE);
        this.dataManager.register(VARIANT, 0);
        this.dataManager.register(CROPS_POLLINATED, 0);
        this.dataManager.register(FEEDER_POS, Optional.absent());
    }

    @Override
    @Nullable
    public net.minecraft.entity.IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable net.minecraft.entity.IEntityLivingData livingdata) {
        this.setVariant(this.getRNG().nextInt(3));
        return super.onInitialSpawn(difficulty, livingdata);
    }

    private List<BlockPos> getNearbyFeeders(BlockPos blockpos, World world, int range) {
        return AMPointOfInterestRegistry.findAll(world, blockpos, range, AMPointOfInterestRegistry::matchesHummingbirdFeeder);
    }

    public boolean isFlying() {
        return this.dataManager.get(FLYING);
    }

    public void setFlying(boolean flying) {
        this.dataManager.set(FLYING, flying);
    }

    public int getVariant() {
        return this.dataManager.get(VARIANT);
    }

    public void setVariant(int variant) {
        this.dataManager.set(VARIANT, variant);
    }

    public int getCropsPollinated() {
        return this.dataManager.get(CROPS_POLLINATED);
    }

    public void setCropsPollinated(int crops) {
        this.dataManager.set(CROPS_POLLINATED, crops);
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        boolean flag = this.motionX * this.motionX + this.motionZ * this.motionZ >= 1.0E-3D;
        if (!this.onGround && this.motionY < 0.0D) {
            this.motionY *= 0.4D;
        }
        this.setFlying(true);
        this.setNoGravity(true);
        if (this.isFlying() && flyProgress < 5F) {
            flyProgress++;
        }
        if (!this.isFlying() && flyProgress > 0F) {
            flyProgress--;
        }
        if (sippy && sipProgress < 5F) {
            sipProgress++;
        }
        if (!sippy && sipProgress > 0F) {
            sipProgress--;
        }
        if (sippy && sipProgress == 5F) {
            sippy = false;
        }
        if (flag && movingProgress < 5F) {
            movingProgress++;
        }
        if (!flag && movingProgress > 0F) {
            movingProgress--;
        }
        if (this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ * this.motionZ < 1.0E-7D) {
            hummingStill++;
        } else {
            hummingStill = 0;
        }
        if (pollinateCooldown > 0) {
            pollinateCooldown--;
        }
        if (sipCooldown > 0) {
            sipCooldown--;
        }
        if (loopSoundTick == 0) {
            this.playSound(AMSoundRegistry.HUMMINGBIRD_LOOP, this.getSoundVolume() * 0.33F, this.getSoundPitch());
        }
        loopSoundTick++;
        if (loopSoundTick > 27) {
            loopSoundTick = 0;
        }
        prevFlyProgress = flyProgress;
        prevMovingProgress = movingProgress;
        prevSipProgress = sipProgress;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void handleStatusUpdate(byte id) {
        if (id == 68) {
            if (this.getFeederPos() != null) {
                if (rand.nextFloat() < 0.2F) {
                    double d2 = this.rand.nextGaussian() * 0.02D;
                    double d0 = this.rand.nextGaussian() * 0.02D;
                    double d1 = this.rand.nextGaussian() * 0.02D;
                    this.world.spawnParticle(EnumParticleTypes.SUSPENDED, this.getFeederPos().getX() + 0.2F + (double) (this.rand.nextFloat() * 0.6F), this.getFeederPos().getY() + 0.1F, this.getFeederPos().getZ() + 0.2F + (double) (this.rand.nextFloat() * 0.6F), d0, d1, d2);
                }
                this.sippy = true;
            }
        } else {
            super.handleStatusUpdate(id);
        }
    }

    @Nullable
    @Override
    public EntityHummingbird createChild(EntityAgeable ageable) {
        return new EntityHummingbird(this.world);
    }

    public boolean canBlockBeSeen(BlockPos pos) {
        double x = pos.getX() + 0.5F;
        double y = pos.getY() + 0.5F;
        double z = pos.getZ() + 0.5F;
        Vec3d start = new Vec3d(this.posX, this.getPositionEyes(1.0F).y, this.posZ);
        Vec3d end = new Vec3d(x, y, z);
        RayTraceResult result = this.world.rayTraceBlocks(start, end, false, true, false);
        if (result == null || result.typeOfHit == RayTraceResult.Type.MISS) {
            return true;
        }
        return result.hitVec.squareDistanceTo(x, y, z) <= 1.0D;
    }

    private class AIUseFeeder extends EntityAIBase {
        int runCooldown = 0;
        private int idleAtFlowerTime = 0;
        private BlockPos localFeeder;

        AIUseFeeder() {
            this.setMutexBits(3);
        }

        @Override
        public void resetTask() {
            localFeeder = null;
            idleAtFlowerTime = 0;
        }

        @Override
        public boolean shouldExecute() {
            if (EntityHummingbird.this.sipCooldown > 0) {
                return false;
            }
            if (runCooldown > 0) {
                runCooldown--;
            } else {
                BlockPos feedPos = getFeederPos();
                if (feedPos != null && isValidFeeder(world.getBlockState(feedPos))) {
                    localFeeder = feedPos;
                    return true;
                } else {
                    List<BlockPos> beacons = getNearbyFeeders(EntityHummingbird.this.getPosition(), world, 64);
                    BlockPos closest = null;
                    for (BlockPos pos : beacons) {
                        if (closest == null || EntityHummingbird.this.getDistanceSq(closest) > EntityHummingbird.this.getDistanceSq(pos)) {
                            if (isValidFeeder(world.getBlockState(pos))) {
                                closest = pos;
                            }
                        }
                    }
                    if (closest != null && isValidFeeder(world.getBlockState(closest))) {
                        localFeeder = closest;
                        return true;
                    }
                }
            }
            runCooldown = 400 + rand.nextInt(600);
            return false;
        }

        @Override
        public boolean shouldContinueExecuting() {
            return localFeeder != null && isValidFeeder(world.getBlockState(localFeeder)) && EntityHummingbird.this.sipCooldown == 0;
        }

        @Override
        public void updateTask() {
            if (localFeeder != null && isValidFeeder(world.getBlockState(localFeeder))) {
                if (EntityHummingbird.this.posY > localFeeder.getY() && !EntityHummingbird.this.onGround) {
                    EntityHummingbird.this.getMoveHelper().setMoveTo(localFeeder.getX() + 0.5F, localFeeder.getY() + 0.1F, localFeeder.getZ() + 0.5F, 1F);
                } else {
                    EntityHummingbird.this.getMoveHelper().setMoveTo(localFeeder.getX() + rand.nextInt(4) - 2, EntityHummingbird.this.posY + 1F, localFeeder.getZ() + rand.nextInt(4) - 2, 1F);
                }
                Vec3d vec = new Vec3d(localFeeder.getX() + 0.5D, localFeeder.getY() + 0.1D, localFeeder.getZ() + 0.5D);
                double dist = MathHelper.sqrt(EntityHummingbird.this.getDistanceSq(vec.x, vec.y, vec.z));
                if (dist < 2.5F && EntityHummingbird.this.posY > localFeeder.getY()) {
                    EntityHummingbird.this.getLookHelper().setLookPosition(vec.x, vec.y, vec.z, 30.0F, EntityHummingbird.this.getVerticalFaceSpeed());
                    idleAtFlowerTime++;
                    EntityHummingbird.this.setFeederPos(localFeeder);
                    EntityHummingbird.this.world.setEntityState(EntityHummingbird.this, (byte) 68);
                    if (idleAtFlowerTime > 55) {
                        if (EntityHummingbird.this.getCropsPollinated() > 2 && rand.nextInt(25) == 0 && isValidFeeder(world.getBlockState(localFeeder))) {
                            world.setBlockState(localFeeder, world.getBlockState(localFeeder).withProperty(BlockHummingbirdFeeder.CONTENTS, 0));
                        }
                        EntityHummingbird.this.setCropsPollinated(EntityHummingbird.this.getCropsPollinated() + 1);
                        EntityHummingbird.this.sipCooldown = 120 + rand.nextInt(1200);
                        EntityHummingbird.this.pollinateCooldown = Math.max(0, EntityHummingbird.this.pollinateCooldown / 3);
                        runCooldown = 400 + rand.nextInt(600);
                        resetTask();
                    }
                }
            }
        }

        public boolean isValidFeeder(IBlockState state) {
            return state.getBlock() instanceof BlockHummingbirdFeeder && state.getValue(BlockHummingbirdFeeder.CONTENTS) == 3;
        }
    }
}
