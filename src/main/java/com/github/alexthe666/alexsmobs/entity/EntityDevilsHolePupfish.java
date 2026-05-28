package com.github.alexthe666.alexsmobs.entity;

import com.github.alexthe666.alexsmobs.config.AMConfig;
import com.github.alexthe666.alexsmobs.entity.ai.AnimalAISwimBottom;
import com.github.alexthe666.alexsmobs.entity.ai.AquaticMoveController;
import com.github.alexthe666.alexsmobs.item.AMItemRegistry;
import com.github.alexthe666.alexsmobs.misc.AMSoundRegistry;
import com.github.alexthe666.alexsmobs.misc.AMTagRegistry;
import com.github.alexthe666.alexsmobs.world.AMWorldData;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAIPanic;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathNavigateSwimmer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootTable;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import com.google.common.base.Optional;
import net.minecraft.block.Block;

public class EntityDevilsHolePupfish extends EntityCreature {

    public static final ResourceLocation PUPFISH_REWARD = new ResourceLocation("alexsmobs", "gameplay/pupfish_reward");
    private static final DataParameter<Boolean> FROM_BUCKET = EntityDataManager.createKey(EntityDevilsHolePupfish.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Float> PUPFISH_SCALE = EntityDataManager.createKey(EntityDevilsHolePupfish.class, DataSerializers.FLOAT);
    private static final DataParameter<Integer> FEEDING_TIME = EntityDataManager.createKey(EntityDevilsHolePupfish.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> BABY_AGE = EntityDataManager.createKey(EntityDevilsHolePupfish.class, DataSerializers.VARINT);
    private static final DataParameter<Optional<BlockPos>> FEEDING_POS = EntityDataManager.createKey(EntityDevilsHolePupfish.class, DataSerializers.OPTIONAL_BLOCK_POS);
    public float prevOnLandProgress;
    public float onLandProgress;
    public float prevFeedProgress;
    public float feedProgress;
    private EntityDevilsHolePupfish chasePartner;
    private int chaseTime;
    private boolean chaseDriver;
    private boolean breedNextChase;
    private int chaseCooldown;
    private int maxChaseTime = 300;

    public EntityDevilsHolePupfish(World world) {
        super(world);
        this.moveHelper = new AquaticMoveController(this, 1.0F, 15F);
        this.setSize(0.6F, 0.35F);
    }

    @Override
    protected PathNavigate createNavigator(World worldIn) {
        return new PathNavigateSwimmer(this, worldIn);
    }

    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return AMSoundRegistry.DEVILS_HOLE_PUPFISH_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return AMSoundRegistry.DEVILS_HOLE_PUPFISH_HURT;
    }

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(1, new EntityAIWander(this, 1.0D, 40));
        this.tasks.addTask(2, new EatMossGoal(this));
        this.tasks.addTask(3, new ChaseGoal(this));
        this.tasks.addTask(4, new EntityAIPanic(this, 1.0D));
        this.tasks.addTask(5, new AnimalAISwimBottom(this, 1.0F, 7));
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(2.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.34D);
    }

    public static boolean canPupfishSpawn(World world, BlockPos pos) {
        if (!isPupfishChunk(world, pos)) {
            return false;
        }
        if (world.getBlockState(pos).getMaterial() != Material.WATER) {
            return false;
        }
        return isInCave(world, pos);
    }

    private static boolean isPupfishChunk(World world, BlockPos pos) {
        if (!AMConfig.restrictPupfishSpawns) {
            return true;
        }
        AMWorldData data = AMWorldData.get(world);
        return data != null && data.isInPupfishChunk(pos);
    }

    private static boolean isInCave(World world, BlockPos pos) {
        BlockPos check = pos;
        while (world.getBlockState(check).getMaterial() == Material.WATER) {
            check = check.up();
        }
        return !world.canSeeSky(check) && check.getY() < world.getSeaLevel();
    }

    @Override
    public boolean getCanSpawnHere() {
        return AMEntityRegistry.rollSpawn(AMConfig.pupfishSpawnRolls, this.getRNG(), AMEntityRegistry.AMSpawnReason.OTHER);
    }

    @Override
    public int getMaxSpawnedInChunk() {
        return 6;
    }

    @Override
    protected boolean canDespawn() {
        return !this.isFromBucket() && !this.hasCustomName() && !this.isBaby() && super.canDespawn();
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(FROM_BUCKET, false);
        this.dataManager.register(PUPFISH_SCALE, 1.0F);
        this.dataManager.register(FEEDING_TIME, 0);
        this.dataManager.register(BABY_AGE, 0);
        this.dataManager.register(FEEDING_POS, Optional.absent());
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        this.prevOnLandProgress = onLandProgress;
        this.prevFeedProgress = feedProgress;
        if (chaseCooldown > 0) {
            chaseCooldown--;
        }
        if (!this.isInWater() && onLandProgress < 5F) {
            onLandProgress++;
        }
        if (this.isInWater() && onLandProgress > 0F) {
            onLandProgress--;
        }
        if (this.getFeedingTime() > 0 && feedProgress < 5F) {
            feedProgress++;
        }
        if (this.getFeedingTime() <= 0 && feedProgress > 0F) {
            feedProgress--;
        }
        if (this.isBaby()) {
            this.setBabyAge(this.getBabyAge() + 1);
        }
        BlockPos feedingPos = this.dataManager.get(FEEDING_POS).orNull();
        if (feedingPos == null) {
            float f2 = (float) -((float) this.motionY * 2.2F * (double) ((float) Math.PI / 180F));
            this.rotationPitch = f2;
        } else if (this.getFeedingTime() > 0) {
            Vec3d face = new Vec3d(feedingPos.getX() + 0.5D, feedingPos.getY() + 0.5D, feedingPos.getZ() + 0.5D).subtract(this.posX, this.posY, this.posZ);
            double d0 = face.lengthVector();
            this.rotationPitch = (float) (-MathHelper.atan2(face.y, d0) * (180D / Math.PI));
            this.rotationYaw = (float) (MathHelper.atan2(face.z, face.x) * (180D / Math.PI) - 90F);
            this.renderYawOffset = this.rotationYaw;
            this.rotationYawHead = this.rotationYaw;
            IBlockState state = world.getBlockState(feedingPos);
            if (this.rand.nextInt(2) == 0 && !state.getMaterial().isLiquid()) {
                for (int i = 0; i < 4 + this.rand.nextInt(2); i++) {
                    this.world.spawnParticle(EnumParticleTypes.BLOCK_CRACK, this.posX, this.posY + this.height * 0.5F, this.posZ,
                            this.rand.nextGaussian() * 0.02D, 0.1F + this.rand.nextFloat() * 0.2F, this.rand.nextGaussian() * 0.02D,
                            Block.getStateId(state));
                }
            }
        }
        if (!this.isInWater() && this.isEntityAlive() && this.onGround && this.rand.nextFloat() < 0.5F) {
            this.motionX += (this.rand.nextFloat() * 2.0F - 1.0F) * 0.2F;
            this.motionY = 0.5D;
            this.motionZ += (this.rand.nextFloat() * 2.0F - 1.0F) * 0.2F;
            this.rotationYaw = this.rand.nextFloat() * 360.0F;
            this.playSound(SoundEvents.ENTITY_GUARDIAN_FLOP, this.getSoundVolume(), this.getSoundPitch());
        }
        this.applyPupfishScaleToBoundingBox();
    }

    private void applyPupfishScaleToBoundingBox() {
        float sc = this.getPupfishScale();
        this.setSize(0.6F * sc, 0.35F * sc);
    }

    public boolean isFromBucket() {
        return this.dataManager.get(FROM_BUCKET);
    }

    public void setFromBucket(boolean bucketed) {
        this.dataManager.set(FROM_BUCKET, bucketed);
    }

    protected void setBucketData(ItemStack bucket) {
        if (this.hasCustomName()) {
            bucket.setStackDisplayName(this.getCustomNameTag());
        }
        NBTTagCompound tag = bucket.getTagCompound();
        if (tag == null) {
            tag = new NBTTagCompound();
            bucket.setTagCompound(tag);
        }
        tag.setFloat("BucketScale", this.getPupfishScale());
        tag.setInteger("BabyAge", this.getBabyAge());
    }

    protected ItemStack getFishBucket() {
        ItemStack stack = new ItemStack(AMItemRegistry.PUPFISH_BUCKET);
        if (this.hasCustomName()) {
            stack.setStackDisplayName(this.getCustomNameTag());
        }
        return stack;
    }

    public float getPupfishScale() {
        return this.dataManager.get(PUPFISH_SCALE);
    }

    public void setPupfishScale(float scale) {
        this.dataManager.set(PUPFISH_SCALE, scale);
    }

    public int getFeedingTime() {
        return this.dataManager.get(FEEDING_TIME);
    }

    public void setFeedingTime(int feedingTime) {
        this.dataManager.set(FEEDING_TIME, feedingTime);
    }

    public int getBabyAge() {
        return this.dataManager.get(BABY_AGE);
    }

    public void setBabyAge(int babyAge) {
        this.dataManager.set(BABY_AGE, babyAge);
    }

    public boolean isBaby() {
        return getBabyAge() < 0;
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setBoolean("FromBucket", this.isFromBucket());
        compound.setBoolean("BreedNextChase", this.breedNextChase);
        compound.setFloat("PupfishScale", this.getPupfishScale());
        compound.setInteger("BabyAge", this.getBabyAge());
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.setFromBucket(compound.getBoolean("FromBucket"));
        this.breedNextChase = compound.getBoolean("BreedNextChase");
        this.setPupfishScale(compound.getFloat("PupfishScale"));
        this.setBabyAge(compound.getInteger("BabyAge"));
    }

    @Nullable
    @Override
    public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData livingdata) {
        this.setPupfishScale(0.65F + this.rand.nextFloat() * 0.35F);
        return super.onInitialSpawn(difficulty, livingdata);
    }

    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }

    @Override
    public boolean isPushedByWater() {
        return false;
    }

    @Override
    public void onLivingUpdate() {
        if (this.isEntityAlive() && !this.isInWater()) {
            this.setAir(this.getAir() - 1);
            if (this.getAir() == -20) {
                this.setAir(0);
                this.attackEntityFrom(DamageSource.DROWN, 2.0F);
            }
        } else {
            this.setAir(600);
        }
        super.onLivingUpdate();
    }

    public int getMaxAir() {
        return 600;
    }

    @Override
    public void travel(float strafe, float vertical, float forward) {
        if (!this.world.isRemote && this.isInWater()) {
            this.moveRelative(strafe, vertical, forward, this.getAIMoveSpeed());
            this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
            this.motionX *= 0.9D;
            this.motionY *= 0.6D;
            this.motionZ *= 0.9D;
            if (this.getAttackTarget() == null) {
                this.motionY -= 0.005D;
            }
        } else {
            super.travel(strafe, vertical, forward);
        }
    }

    protected void playStepSound(BlockPos pos, IBlockState blockIn) {
    }

    private boolean canSeeBlock(BlockPos destinationBlock) {
        Vec3d vector3d = new Vec3d(this.posX, this.posY + (double) this.getEyeHeight(), this.posZ);
        Vec3d blockVec = new Vec3d(destinationBlock.getX() + 0.5D, destinationBlock.getY() + 0.5D, destinationBlock.getZ() + 0.5D);
        RayTraceResult result = this.world.rayTraceBlocks(vector3d, blockVec, false, true, false);
        return result == null || result.getBlockPos().equals(destinationBlock);
    }

    private static List<ItemStack> getFoodLoot(EntityDevilsHolePupfish pupfish) {
        if (!(pupfish.world instanceof WorldServer)) {
            return java.util.Collections.emptyList();
        }
        WorldServer sw = (WorldServer) pupfish.world;
        LootTable loottable = sw.getLootTableManager().getLootTableFromLocation(PUPFISH_REWARD);
        return loottable.generateLootForPools(pupfish.getRNG(), new LootContext.Builder(sw).withLootedEntity(pupfish).build());
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
            this.setBucketData(itemstack1);
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

    private void spawnBabiesWith(EntityDevilsHolePupfish chasePartner) {
        EntityDevilsHolePupfish baby = new EntityDevilsHolePupfish(this.world);
        baby.setPosition(this.posX, this.posY, this.posZ);
        baby.setPupfishScale(0.65F + this.rand.nextFloat() * 0.35F);
        baby.setBabyAge(-24000);
        this.world.spawnEntity(baby);
    }

    private class ChaseGoal extends EntityAIBase {
        private final EntityDevilsHolePupfish pupfish;
        private int executionCooldown = 50;

        public ChaseGoal(EntityDevilsHolePupfish pupfish) {
            this.setMutexBits(1);
            this.pupfish = pupfish;
        }

        @Override
        public boolean shouldExecute() {
            if (!pupfish.isInWater() || pupfish.chaseTime > pupfish.maxChaseTime || pupfish.chaseCooldown > 0) {
                return false;
            }
            if (pupfish.chasePartner != null && pupfish.chasePartner.isEntityAlive()) {
                return true;
            }
            if (executionCooldown > 0) {
                executionCooldown--;
                return false;
            }
            executionCooldown = 50 + pupfish.getRNG().nextInt(50);
            List<EntityDevilsHolePupfish> list = pupfish.world.getEntitiesWithinAABB(EntityDevilsHolePupfish.class, pupfish.getEntityBoundingBox().grow(10, 8, 10),
                    e -> e instanceof EntityDevilsHolePupfish && e.getEntityId() != pupfish.getEntityId() && ((EntityDevilsHolePupfish) e).chasePartner == null && ((EntityDevilsHolePupfish) e).chaseCooldown <= 0);
            list.sort(Comparator.comparingDouble(pupfish::getDistanceSq));
            if (!list.isEmpty()) {
                EntityDevilsHolePupfish closest = list.get(0);
                pupfish.chasePartner = closest;
                closest.chasePartner = pupfish;
                pupfish.chaseDriver = true;
                return true;
            }
            return false;
        }

        @Override
        public boolean shouldContinueExecuting() {
            return pupfish.chasePartner != null && pupfish.chasePartner.isEntityAlive() && pupfish.chaseTime < pupfish.maxChaseTime;
        }

        @Override
        public void startExecuting() {
            pupfish.chaseDriver = !pupfish.chasePartner.chaseDriver;
            pupfish.chaseTime = 0;
            pupfish.maxChaseTime = 600;
        }

        @Override
        public void resetTask() {
            pupfish.chaseTime = 0;
            pupfish.chaseCooldown = 100 + pupfish.getRNG().nextInt(100);
            executionCooldown = 50 + pupfish.getRNG().nextInt(20);
            if (pupfish.breedNextChase && pupfish.chasePartner != null) {
                pupfish.spawnBabiesWith(pupfish.chasePartner);
                pupfish.chasePartner.breedNextChase = false;
                pupfish.breedNextChase = false;
            }
            pupfish.chasePartner = null;
        }

        @Override
        public void updateTask() {
            pupfish.chaseTime++;
            if (pupfish.chasePartner == null || !pupfish.chaseDriver) {
                return;
            }
            float chaserSpeed = 1.2F + pupfish.getRNG().nextFloat() * 0.45F;
            float chasedSpeed = 0.2F + chaserSpeed * 0.7F;
            EntityDevilsHolePupfish flee = pupfish.chaseDriver ? pupfish.chasePartner : pupfish;
            EntityDevilsHolePupfish driver = pupfish.chaseDriver ? pupfish : pupfish.chasePartner;
            driver.getNavigator().tryMoveToXYZ(flee.posX, flee.posY + 0.5F, flee.posZ, chaserSpeed);
            Vec3d from = new Vec3d(flee.posX + pupfish.getRNG().nextFloat() - 0.5F, flee.posY + pupfish.getRNG().nextFloat() - 0.5F, flee.posZ + pupfish.getRNG().nextFloat() - 0.5F)
                    .subtract(driver.posX, driver.posY, driver.posZ).normalize().scale(2F + pupfish.getRNG().nextFloat() * 2F);
            flee.getNavigator().tryMoveToXYZ(flee.posX + from.x, flee.posY + from.y, flee.posZ + from.z, chasedSpeed);
            if (pupfish.getRNG().nextInt(50) == 0) {
                pupfish.chaseDriver = !pupfish.chaseDriver;
                pupfish.chasePartner.chaseDriver = !pupfish.chasePartner.chaseDriver;
            }
        }
    }

    private class EatMossGoal extends EntityAIBase {
        private final int searchLength;
        protected BlockPos destinationBlock;
        private final EntityDevilsHolePupfish pupfish;
        private int runDelay = 70;
        private int maxFeedTime = 200;

        private EatMossGoal(EntityDevilsHolePupfish pupfish) {
            this.setMutexBits(1);
            this.pupfish = pupfish;
            searchLength = 16;
        }

        @Override
        public boolean shouldContinueExecuting() {
            return destinationBlock != null && isMossBlock(pupfish.world, destinationBlock) && isCloseToMoss(16);
        }

        public boolean isCloseToMoss(double dist) {
            return destinationBlock == null || pupfish.getDistanceSq(destinationBlock) < dist * dist;
        }

        @Override
        public boolean shouldExecute() {
            if (!pupfish.isInWater()) {
                return false;
            }
            if (this.runDelay > 0) {
                --this.runDelay;
                return false;
            }
            this.runDelay = 200 + pupfish.getRNG().nextInt(150);
            return this.searchForDestination();
        }

        @Override
        public void startExecuting() {
            maxFeedTime = 60 + pupfish.getRNG().nextInt(60);
        }

        @Override
        public void updateTask() {
            Vec3d vec = new Vec3d(destinationBlock.getX() + 0.5D, destinationBlock.getY() + 0.5D, destinationBlock.getZ() + 0.5D);
            pupfish.getNavigator().tryMoveToXYZ(vec.x, vec.y, vec.z, 1.0D);
            if (pupfish.getDistanceSq(destinationBlock) < 1.15F) {
                pupfish.dataManager.set(FEEDING_POS, Optional.of(destinationBlock));
                Vec3d face = vec.subtract(pupfish.posX, pupfish.posY, pupfish.posZ).normalize();
                pupfish.motionX += face.x * 0.1D;
                pupfish.motionY += face.y * 0.1D;
                pupfish.motionZ += face.z * 0.1D;
                pupfish.setFeedingTime(pupfish.getFeedingTime() + 1);
                if (pupfish.getFeedingTime() > maxFeedTime) {
                    destinationBlock = null;
                    if (pupfish.getRNG().nextInt(3) == 0) {
                        for (ItemStack stack : getFoodLoot(pupfish)) {
                            if (!stack.isEmpty()) {
                                EntityItem e = pupfish.entityDropItem(stack.copy(), 0.0F);
                                if (e != null) {
                                    e.motionX *= 0.2D;
                                    e.motionY *= 0.2D;
                                    e.motionZ *= 0.2D;
                                }
                            }
                        }
                    }
                    if (pupfish.getRNG().nextInt(3) == 0 && !pupfish.isBaby()) {
                        pupfish.breedNextChase = true;
                    }
                }
            } else {
                pupfish.dataManager.set(FEEDING_POS, Optional.absent());
            }
        }

        @Override
        public void resetTask() {
            pupfish.dataManager.set(FEEDING_POS, Optional.absent());
            destinationBlock = null;
            pupfish.setFeedingTime(0);
        }

        protected boolean searchForDestination() {
            BlockPos origin = pupfish.getPosition();
            for (int yOff = -8; yOff <= 2; yOff++) {
                for (int i = 0; i < searchLength; i++) {
                    for (int ring = 0; ring <= i; ring = ring > 0 ? -ring : 1 - ring) {
                        for (int side = ring < i && ring > -i ? i : 0; side <= i; side = side > 0 ? -side : 1 - side) {
                            BlockPos test = origin.add(ring, yOff - 1, side);
                            if (isMossBlock(pupfish.world, test) && pupfish.canSeeBlock(test)) {
                                destinationBlock = test;
                                return true;
                            }
                        }
                    }
                }
            }
            return false;
        }

        private boolean isMossBlock(World world, BlockPos pos) {
            return AMTagRegistry.blockInTag(AMTagRegistry.PUPFISH_EATABLES, world.getBlockState(pos).getBlock());
        }
    }
}
