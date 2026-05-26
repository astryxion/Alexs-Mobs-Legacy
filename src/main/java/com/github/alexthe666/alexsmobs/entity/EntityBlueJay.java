package com.github.alexthe666.alexsmobs.entity;

import com.github.alexthe666.alexsmobs.AlexsMobs;
import com.github.alexthe666.alexsmobs.client.particle.AMParticleRegistry;
import com.github.alexthe666.alexsmobs.config.AMConfig;
import com.github.alexthe666.alexsmobs.entity.ai.BlueJayAIMelee;
import com.github.alexthe666.alexsmobs.entity.ai.CreatureAITargetItems;
import com.github.alexthe666.alexsmobs.entity.ai.DirectPathNavigator;
import com.github.alexthe666.alexsmobs.entity.ai.FlightMoveController;
import com.github.alexthe666.alexsmobs.entity.ai.FlyingEntityAITempt;
import com.github.alexthe666.alexsmobs.misc.AMSoundRegistry;
import com.github.alexthe666.alexsmobs.misc.AMTagRegistry;
import com.google.common.base.Predicate;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockLog;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAIFollowParent;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIMate;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class EntityBlueJay extends EntityAnimal implements ITargetsDroppedItems {

    private static final DataParameter<Boolean> FLYING = EntityDataManager.createKey(EntityBlueJay.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> ATTACK_TICK = EntityDataManager.createKey(EntityBlueJay.class, DataSerializers.VARINT);
    private static final DataParameter<Float> CREST_TARGET = EntityDataManager.createKey(EntityBlueJay.class, DataSerializers.FLOAT);
    private static final DataParameter<Integer> FEED_TIME = EntityDataManager.createKey(EntityBlueJay.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> SING_TIME = EntityDataManager.createKey(EntityBlueJay.class, DataSerializers.VARINT);
    private static final DataParameter<Boolean> BLUE_VISUAL_FLAG = EntityDataManager.createKey(EntityBlueJay.class, DataSerializers.BOOLEAN);

    private static final Predicate<EntityLivingBase> HIGHLIGHTS_WITH_SONG = entity -> entity instanceof EntityMob;

    public float prevFlyProgress;
    public float flyProgress;
    public float prevFlapAmount;
    public float flapAmount;
    public float attackProgress;
    public float prevAttackProgress;
    public float prevCrestAmount;
    public float crestAmount;
    private boolean isLandNavigator;
    private int timeFlying;
    public float birdPitch = 0;
    public float prevBirdPitch = 0;
    public boolean aiItemFlag = false;
    private int prevSingTime = 0;
    private int blueTime = 0;
    private int raiseCrestOverrideTicks;

    @Nullable
    private UUID lastFeederUUID;
    @Nullable
    private UUID raccoonUUID;

    public EntityBlueJay(World worldIn) {
        super(worldIn);
        this.setSize(0.5F, 0.6F);
        this.setPathPriority(PathNodeType.DANGER_FIRE, -1.0F);
        this.setPathPriority(PathNodeType.WATER, -1.0F);
        this.setPathPriority(PathNodeType.FENCE, -1.0F);
        switchNavigator(false);
    }

    public static boolean canBlueJaySpawnAt(World world, BlockPos pos) {
        if (world.getLightFromNeighbors(pos) <= 8) {
            return false;
        }
        IBlockState below = world.getBlockState(pos.down());
        Block block = below.getBlock();
        return block instanceof BlockLeaves || block instanceof BlockLog || block == Blocks.GRASS;
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        AMEntityRegistry.registerAttributeIfAbsent(this, SharedMonsterAttributes.ATTACK_DAMAGE);
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(10.0D);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(1.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.25D);
    }

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(1, new EntityAIMate(this, 1.0D));
        this.tasks.addTask(2, new BlueJayAIMelee(this));
        this.tasks.addTask(3, new EntityAIFollowParent(this, 1.0D));
        this.tasks.addTask(4, new FlyingEntityAITempt(this, 1.0D, false, java.util.Collections.<net.minecraft.item.Item>emptySet()) {
            @Override
            protected boolean isTempting(ItemStack stack) {
                return AMTagRegistry.itemInTag(AMTagRegistry.BLUE_JAY_FOODSTUFFS, stack.getItem());
            }

            @Override
            public void updateTask() {
                super.updateTask();
                if (EntityBlueJay.this.onGround) {
                    EntityBlueJay.this.setFlying(false);
                }
            }
        });
        this.tasks.addTask(5, new AIFollowFeederOrRaccoon());
        this.tasks.addTask(6, new AIFlyIdle());
        this.tasks.addTask(7, new AIScatter());
        this.tasks.addTask(8, new EntityAIWatchClosest(this, EntityPlayer.class, 6.0F));
        this.tasks.addTask(9, new EntityAIWatchClosest(this, EntityCreature.class, 6.0F));
        this.tasks.addTask(10, new EntityAILookIdle(this));
        this.targetTasks.addTask(1, new AITargetItems(false, false, 40, 16));
        this.targetTasks.addTask(4, new EntityAIHurtByTarget(this, true, EntityPlayer.class));
    }

    @Override
    public boolean getCanSpawnHere() {
        return canBlueJaySpawnAt(this.world, this.getPosition())
                && AMEntityRegistry.rollSpawn(AMConfig.blueJaySpawnRolls, this.getRNG(), AMEntityRegistry.AMSpawnReason.OTHER)
                && super.getCanSpawnHere();
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        return AMTagRegistry.itemInTag(AMTagRegistry.BLUE_JAY_BREEDABLES, stack.getItem());
    }

    @Override
    public void fall(float distance, float damageMultiplier) {
    }

    @Override
    protected void updateFallState(double y, boolean onGroundIn, IBlockState state, BlockPos pos) {
        this.fallDistance = 0.0F;
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(FLYING, false);
        this.dataManager.register(ATTACK_TICK, 0);
        this.dataManager.register(FEED_TIME, 0);
        this.dataManager.register(SING_TIME, 0);
        this.dataManager.register(CREST_TARGET, 0F);
        this.dataManager.register(BLUE_VISUAL_FLAG, false);
    }

    private void switchNavigator(boolean onLand) {
        if (onLand) {
            this.moveHelper = new net.minecraft.entity.ai.EntityMoveHelper(this);
            this.navigator = new PathNavigateGround(this, world);
            this.isLandNavigator = true;
        } else {
            this.moveHelper = new FlightMoveController(this, 1.0F, false);
            this.navigator = new DirectPathNavigator(this, world);
            this.isLandNavigator = false;
        }
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        this.prevCrestAmount = crestAmount;
        this.prevAttackProgress = attackProgress;
        this.prevFlapAmount = flapAmount;
        this.prevFlyProgress = flyProgress;
        this.prevBirdPitch = birdPitch;

        if (isFlying()) {
            if (flyProgress < 5F) {
                flyProgress++;
            }
        } else if (flyProgress > 0F) {
            flyProgress--;
        }

        if (this.dataManager.get(ATTACK_TICK) > 0) {
            this.dataManager.set(ATTACK_TICK, this.dataManager.get(ATTACK_TICK) - 1);
            if (attackProgress < 5F) {
                attackProgress++;
            }
        } else if (attackProgress > 0F) {
            attackProgress--;
        }

        float yMov = (float) this.motionY;
        this.birdPitch = yMov * 2 * -(180F / (float) Math.PI);
        if (yMov >= 0) {
            if (flapAmount < 1F) {
                flapAmount += 0.25F;
            }
        } else if (yMov < -0.07F) {
            if (flapAmount > 0) {
                flapAmount -= 0.25F;
            }
        }

        if (raiseCrestOverrideTicks > 0) {
            raiseCrestOverrideTicks--;
            this.crestAmount = 0.75F;
        } else {
            float target = this.getTargetCrest();
            if (crestAmount < target) {
                crestAmount = Math.min(target, crestAmount + 0.3F);
            } else if (crestAmount > target) {
                crestAmount = Math.max(target, crestAmount - 0.3F);
            }
        }

        if (!this.world.isRemote) {
            if (isFlying()) {
                if (this.isLandNavigator) {
                    switchNavigator(false);
                }
            } else if (!this.isLandNavigator) {
                switchNavigator(true);
            }
            if (isFlying()) {
                timeFlying++;
                this.setNoGravity(true);
                if (this.isRiding() || this.isInLove()) {
                    this.setFlying(false);
                }
            } else {
                timeFlying = 0;
                this.setNoGravity(false);
            }
            if (this.getAttackTarget() != null) {
                this.setCrestTarget(1F);
            } else if (this.getRaccoonUUID() != null) {
                this.setCrestTarget(0.5F);
            } else {
                this.setCrestTarget(0.0F);
            }
        }

        if (this.getFeedTime() > 0) {
            this.setFeedTime(this.getFeedTime() - 1);
            if (this.getFeedTime() == 0) {
                this.setLastFeederUUID(null);
            }
        }

        if (this.getRidingEntity() instanceof EntityRaccoon) {
            this.renderYawOffset = ((EntityRaccoon) this.getRidingEntity()).renderYawOffset;
        }

        Entity owner = this.getRaccoon();
        if (owner instanceof EntityRaccoon) {
            EntityRaccoon raccoon = (EntityRaccoon) owner;
            EntityLivingBase jayTarget = this.getAttackTarget();
            EntityLivingBase raccoonTarget = raccoon.getAttackTarget();
            if (jayTarget != null && jayTarget.isEntityAlive()) {
                if (this.isRiding()) {
                    this.dismountRidingEntity();
                }
            } else if (raccoonTarget != null && raccoonTarget.isEntityAlive()) {
                if (this.canAttackClass(raccoonTarget.getClass())) {
                    this.setAttackTarget(raccoonTarget);
                }
            }
        }

        if (this.getSingTime() > 0) {
            this.setSingTime(this.getSingTime() - 1);
            if (this.prevSingTime % 15 == 0) {
                this.playSound(AMSoundRegistry.BLUE_JAY_SONG, this.getSoundVolume(), this.getSoundPitch());
            }
            if (this.world.isRemote && this.getSingTime() % 5 == 0) {
                float yaw = this.rotationYaw * ((float) Math.PI / 180F);
                float pitch = this.rotationPitch * ((float) Math.PI / 180F);
                double mx = -MathHelper.sin(yaw) * MathHelper.cos(pitch);
                double my = -MathHelper.sin(pitch);
                double mz = MathHelper.cos(yaw) * MathHelper.cos(pitch);
                double px = this.posX + mx * 0.3D;
                double py = this.posY + 0.2D + my * 0.3D;
                double pz = this.posZ + mz * 0.3D;
                AMParticleRegistry.spawnParticle(this.world, AMParticleRegistry.BIRD_SONG, px, py, pz, mx, my, mz);
            }
        }

        if (prevSingTime < getSingTime() && !this.world.isRemote) {
            blueTime = 1200;
            this.dataManager.set(BLUE_VISUAL_FLAG, true);
            highlightMonsters();
        }
        if (blueTime > 0) {
            blueTime--;
            if (blueTime == 0) {
                this.dataManager.set(BLUE_VISUAL_FLAG, false);
                this.world.setEntityState(this, (byte) 68);
            } else {
                this.world.setEntityState(this, (byte) 67);
            }
        }
        prevSingTime = getSingTime();
    }

    @Override
    public void playSound(SoundEvent soundIn, float volume, float pitch) {
        if (soundIn == this.getAmbientSound()) {
            raiseCrestOverrideTicks = 15;
        }
        super.playSound(soundIn, volume, pitch);
    }

    private void highlightMonsters() {
        AxisAlignedBB allyBox = this.getEntityBoundingBox().grow(64);
        allyBox = new AxisAlignedBB(allyBox.minX, -64, allyBox.minZ, allyBox.maxX, 320, allyBox.maxZ);
        List<EntityLivingBase> list = this.world.getEntitiesWithinAABB(EntityLivingBase.class, allyBox);
        for (EntityLivingBase entity : list) {
            if (!HIGHLIGHTS_WITH_SONG.apply(entity)) {
                continue;
            }
            entity.addPotionEffect(new PotionEffect(MobEffects.GLOWING, blueTime, 0, false, false));
        }
    }

    public boolean isMakingMonstersBlue() {
        return this.dataManager.get(BLUE_VISUAL_FLAG);
    }

    @Override
    public void setDead() {
        if (this.getSingTime() > 0 && !this.world.isRemote) {
            this.dataManager.set(BLUE_VISUAL_FLAG, false);
            this.world.setEntityState(this, (byte) 68);
        }
        super.setDead();
    }

    @Override
    public boolean isEntityInvulnerable(DamageSource source) {
        return source == DamageSource.IN_WALL || super.isEntityInvulnerable(source);
    }

    @Override
    public void travel(float strafe, float vertical, float forward) {
        if (this.isInWater() && this.motionY > 0F) {
            this.motionY *= 0.5D;
        }
        super.travel(strafe, vertical, forward);
    }

    public BlockPos getBlueJayGround(BlockPos in) {
        BlockPos position = new BlockPos(in.getX(), (int) this.posY, in.getZ());
        while (position.getY() < 256 && !this.world.getBlockState(position).getMaterial().isLiquid()) {
            position = position.up();
        }
        while (position.getY() > 0 && !this.world.getBlockState(position).isFullBlock() && !this.world.getBlockState(position).getMaterial().isLiquid()) {
            position = position.down();
        }
        return position;
    }

    @Override
    public boolean isOnSameTeam(Entity entityIn) {
        if (this.getRaccoonUUID() != null) {
            if (entityIn instanceof EntityRaccoon && this.getRaccoonUUID().equals(entityIn.getUniqueID())) {
                return true;
            }
            Entity raccoon = getRaccoon();
            if (raccoon != null && (raccoon.isOnSameTeam(entityIn) || entityIn.isOnSameTeam(raccoon))) {
                return true;
            }
        }
        return super.isOnSameTeam(entityIn);
    }

    public Vec3d getBlockGrounding(Vec3d fleePos) {
        float radius = 10 + this.getRNG().nextInt(15);
        float neg = this.getRNG().nextBoolean() ? 1 : -1;
        float renderYawOffset = this.renderYawOffset;
        float angle = (0.01745329251F * renderYawOffset) + 3.15F + (this.getRNG().nextFloat() * neg);
        double extraX = radius * MathHelper.sin((float) (Math.PI + angle));
        double extraZ = radius * MathHelper.cos(angle);
        BlockPos radialPos = new BlockPos((int) (fleePos.x + extraX), (int) this.posY, (int) (fleePos.z + extraZ));
        BlockPos ground = this.getBlueJayGround(radialPos);
        if (ground.getY() < 0) {
            return null;
        }
        ground = this.getPosition();
        while (ground.getY() > 0 && !this.world.getBlockState(ground).isFullBlock()) {
            ground = ground.down();
        }
        Vec3d center = new Vec3d(ground.getX() + 0.5D, ground.getY() + 0.5D, ground.getZ() + 0.5D);
        if (!this.isTargetBlocked(center)) {
            return new Vec3d(center.x, ground.getY(), center.z);
        }
        return null;
    }

    public Vec3d getBlockInViewAway(Vec3d fleePos, float radiusAdd) {
        float radius = 5 + radiusAdd + this.getRNG().nextInt(5);
        float neg = this.getRNG().nextBoolean() ? 1 : -1;
        float renderYawOffset = this.renderYawOffset;
        float angle = (0.01745329251F * renderYawOffset) + 3.15F + (this.getRNG().nextFloat() * neg);
        double extraX = radius * MathHelper.sin((float) (Math.PI + angle));
        double extraZ = radius * MathHelper.cos(angle);
        BlockPos radialPos = new BlockPos((int) (fleePos.x + extraX), 0, (int) (fleePos.z + extraZ));
        BlockPos ground = getBlueJayGround(radialPos);
        int distFromGround = (int) this.posY - ground.getY();
        int flightHeight = 5 + this.getRNG().nextInt(5);
        int j = this.getRNG().nextInt(5) + 5;
        BlockPos newPos = ground.up(distFromGround > 5 ? flightHeight : j);
        if (this.world.getBlockState(ground).getBlock() instanceof BlockLeaves) {
            newPos = ground.up(1 + this.getRNG().nextInt(3));
        }
        Vec3d center = new Vec3d(newPos.getX() + 0.5D, newPos.getY() + 0.5D, newPos.getZ() + 0.5D);
        if (!this.isTargetBlocked(center) && this.getDistanceSq(center.x, center.y, center.z) > 1) {
            return center;
        }
        return null;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return AMSoundRegistry.BLUE_JAY_IDLE;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return AMSoundRegistry.BLUE_JAY_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return AMSoundRegistry.BLUE_JAY_HURT;
    }

    public boolean isTargetBlocked(Vec3d target) {
        Vec3d start = new Vec3d(this.posX, this.getPositionEyes(1.0F).y, this.posZ);
        RayTraceResult result = this.world.rayTraceBlocks(start, target, false, true, false);
        return result != null && result.typeOfHit != RayTraceResult.Type.MISS;
    }

    public boolean isFlying() {
        return this.dataManager.get(FLYING);
    }

    public void setFlying(boolean flying) {
        if (flying && this.isChild()) {
            flying = false;
        }
        this.dataManager.set(FLYING, flying);
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setBoolean("Flying", this.isFlying());
        compound.setInteger("BlueTime", this.blueTime);
        if (this.getLastFeederUUID() != null) {
            compound.setUniqueId("FeederUUID", this.getLastFeederUUID());
        }
        if (this.getRaccoonUUID() != null) {
            compound.setUniqueId("RaccoonUUID", this.getRaccoonUUID());
        }
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.setFlying(compound.getBoolean("Flying"));
        this.blueTime = compound.getInteger("BlueTime");
        if (compound.hasUniqueId("FeederUUID")) {
            this.setLastFeederUUID(compound.getUniqueId("FeederUUID"));
        }
        if (compound.hasUniqueId("RaccoonUUID")) {
            this.setRaccoonUUID(compound.getUniqueId("RaccoonUUID"));
        }
    }

    public int getFeedTime() {
        return this.dataManager.get(FEED_TIME);
    }

    public void setFeedTime(int feedTime) {
        this.dataManager.set(FEED_TIME, feedTime);
    }

    public int getSingTime() {
        return this.dataManager.get(SING_TIME);
    }

    public void setSingTime(int singTime) {
        this.dataManager.set(SING_TIME, singTime);
    }

    public float getTargetCrest() {
        return this.dataManager.get(CREST_TARGET);
    }

    public void setCrestTarget(float crestTarget) {
        this.dataManager.set(CREST_TARGET, crestTarget);
    }

    @Nullable
    public UUID getLastFeederUUID() {
        return lastFeederUUID;
    }

    public void setLastFeederUUID(@Nullable UUID uniqueId) {
        this.lastFeederUUID = uniqueId;
    }

    @Nullable
    public Entity getLastFeeder() {
        UUID id = getLastFeederUUID();
        if (id != null && !this.world.isRemote) {
            return this.world.getPlayerEntityByUUID(id);
        }
        return null;
    }

    public void setLastFeeder(@Nullable Entity feeder) {
        if (feeder == null) {
            this.setLastFeederUUID(null);
        } else {
            this.setLastFeederUUID(feeder.getUniqueID());
        }
    }

    @Nullable
    public UUID getRaccoonUUID() {
        return raccoonUUID;
    }

    public void setRaccoonUUID(@Nullable UUID uniqueId) {
        this.raccoonUUID = uniqueId;
    }

    @Nullable
    public Entity getRaccoon() {
        UUID id = getRaccoonUUID();
        if (id != null && !this.world.isRemote) {
            List<EntityRaccoon> raccoons = this.world.getEntitiesWithinAABB(EntityRaccoon.class, this.getEntityBoundingBox().grow(128));
            for (EntityRaccoon raccoon : raccoons) {
                if (raccoon.getUniqueID().equals(id)) {
                    return raccoon;
                }
            }
        }
        return null;
    }

    public void setRaccoon(@Nullable Entity feeder) {
        if (feeder == null) {
            this.setRaccoonUUID(null);
        } else {
            this.setRaccoonUUID(feeder.getUniqueID());
        }
    }

    private boolean isOverWaterOrVoid() {
        BlockPos position = this.getPosition();
        while (position.getY() > 0 && this.world.isAirBlock(position)) {
            position = position.down();
        }
        return this.world.getBlockState(position).getMaterial().isLiquid() || this.world.getBlockState(position).getBlock() == Blocks.VINE || position.getY() <= 0;
    }

    @Nullable
    @Override
    public EntityBlueJay createChild(EntityAgeable ageable) {
        return new EntityBlueJay(this.world);
    }

    @Override
    public boolean canTargetItem(ItemStack stack) {
        return stack.getItem() instanceof ItemFood || AMTagRegistry.itemInTag(AMTagRegistry.BLUE_JAY_FOODSTUFFS, stack.getItem());
    }

    @Override
    public double getMaxDistToItem() {
        return 1.0D;
    }

    @Override
    public void onGetItem(EntityItem e) {
        if (!this.getHeldItemMainhand().isEmpty() && !this.world.isRemote) {
            this.entityDropItem(this.getHeldItemMainhand(), 0.0F);
        }
        this.heal(3);
        String throwerName = e.getThrower();
        EntityPlayer itemThrower = throwerName != null ? this.world.getPlayerEntityByName(throwerName) : null;
        if (itemThrower != null && AMTagRegistry.itemInTag(AMTagRegistry.BLUE_JAY_TEAMING_FOODS, e.getItem().getItem())) {
            this.setLastFeederUUID(itemThrower.getUniqueID());
            this.setFeedTime(1200);
            this.dismountRidingEntity();
        }
        if (itemThrower != null && AMTagRegistry.itemInTag(AMTagRegistry.BLUE_JAY_ALERT_FOODS, e.getItem().getItem())) {
            this.setSingTime(40);
        }
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        ItemStack itemstack = player.getHeldItem(hand);
        if (super.processInteract(player, hand)) {
            return true;
        }
        if (AMTagRegistry.itemInTag(AMTagRegistry.BLUE_JAY_TEAMING_FOODS, itemstack.getItem()) && this.getFeedTime() <= 0) {
            this.heal(3);
            if (!player.capabilities.isCreativeMode) {
                itemstack.shrink(1);
            }
            this.setRaccoonUUID(null);
            this.dismountRidingEntity();
            this.setLastFeeder(player);
            this.setFeedTime(1200);
            return true;
        } else if (AMTagRegistry.itemInTag(AMTagRegistry.BLUE_JAY_ALERT_FOODS, itemstack.getItem()) && this.getSingTime() <= 0) {
            this.heal(3);
            this.setSingTime(40);
            if (!player.capabilities.isCreativeMode) {
                itemstack.shrink(1);
            }
            return true;
        }
        return false;
    }

    public void peck() {
        this.dataManager.set(ATTACK_TICK, 7);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void handleStatusUpdate(byte id) {
        if (id == 67 || id == 68) {
            AlexsMobs.PROXY.onEntityStatus(this, id);
        } else {
            super.handleStatusUpdate(id);
        }
    }

    private boolean isTrusting() {
        return this.getFeedTime() > 0 || this.getSingTime() > 0 || this.getRaccoonUUID() != null || aiItemFlag;
    }

    private class AIFlyIdle extends EntityAIBase {
        protected double x;
        protected double y;
        protected double z;
        private boolean flightTarget;

        public AIFlyIdle() {
            this.setMutexBits(1);
        }

        @Override
        public boolean shouldExecute() {
            if (EntityBlueJay.this.isBeingRidden() || (EntityBlueJay.this.getAttackTarget() != null && EntityBlueJay.this.getAttackTarget().isEntityAlive())
                    || EntityBlueJay.this.isRiding() || EntityBlueJay.this.aiItemFlag || EntityBlueJay.this.getSingTime() > 0) {
                return false;
            }
            if (EntityBlueJay.this.getRNG().nextInt(45) != 0 && !EntityBlueJay.this.isFlying()) {
                return false;
            }
            flightTarget = EntityBlueJay.this.onGround ? EntityBlueJay.this.getRNG().nextBoolean() : EntityBlueJay.this.getRNG().nextInt(5) > 0 && EntityBlueJay.this.timeFlying < 200;
            Vec3d lvt = getPosition();
            if (lvt == null) {
                return false;
            }
            this.x = lvt.x;
            this.y = lvt.y;
            this.z = lvt.z;
            return true;
        }

        public void updateTask() {
            if (flightTarget) {
                EntityBlueJay.this.getMoveHelper().setMoveTo(this.x, this.y, this.z, 1F);
            } else {
                EntityBlueJay.this.getNavigator().tryMoveToXYZ(this.x, this.y, this.z, 1F);
            }
            if (!flightTarget && isFlying() && EntityBlueJay.this.onGround) {
                EntityBlueJay.this.setFlying(false);
            }
            if (isFlying() && EntityBlueJay.this.onGround && EntityBlueJay.this.timeFlying > 10) {
                EntityBlueJay.this.setFlying(false);
            }
        }

        @Nullable
        protected Vec3d getPosition() {
            Vec3d vector3d = EntityBlueJay.this.getPositionVector();
            if (EntityBlueJay.this.isOverWaterOrVoid()) {
                flightTarget = true;
            }
            if (flightTarget) {
                if (EntityBlueJay.this.timeFlying < 200 || EntityBlueJay.this.isOverWaterOrVoid()) {
                    return EntityBlueJay.this.getBlockInViewAway(vector3d, 0);
                }
                return EntityBlueJay.this.getBlockGrounding(vector3d);
            }
            return RandomPositionGenerator.findRandomTarget(EntityBlueJay.this, 10, 7);
        }

        @Override
        public boolean shouldContinueExecuting() {
            if (flightTarget) {
                return EntityBlueJay.this.isFlying() && EntityBlueJay.this.getDistanceSq(x, y, z) > 5F;
            }
            return !EntityBlueJay.this.getNavigator().noPath() && !EntityBlueJay.this.isBeingRidden();
        }

        public void startExecuting() {
            if (flightTarget) {
                EntityBlueJay.this.setFlying(true);
                EntityBlueJay.this.getMoveHelper().setMoveTo(x, y, z, 1F);
            } else {
                EntityBlueJay.this.getNavigator().tryMoveToXYZ(this.x, this.y, this.z, 1F);
            }
        }

        public void resetTask() {
            EntityBlueJay.this.getNavigator().clearPath();
            x = y = z = 0;
        }
    }

    private class AIScatter extends EntityAIBase {
        protected final AIScatter.Sorter theNearestAttackableTargetSorter;
        protected final Predicate<? super Entity> targetEntitySelector;
        protected int executionChance = 8;
        protected boolean mustUpdate;
        private Entity targetEntity;
        private Vec3d flightTarget = null;
        private int cooldown = 0;

        AIScatter() {
            this.setMutexBits(1);
            this.theNearestAttackableTargetSorter = new AIScatter.Sorter(EntityBlueJay.this);
            this.targetEntitySelector = e -> e != null && e.isEntityAlive()
                    && (AMTagRegistry.entityMatchesEntityTypeTag(AMTagRegistry.SCATTERS_CROWS, e)
                    || e instanceof EntityPlayer && !((EntityPlayer) e).capabilities.isCreativeMode);
        }

        @Override
        public boolean shouldExecute() {
            if (EntityBlueJay.this.isRiding() || EntityBlueJay.this.isBeingRidden()
                    || (EntityBlueJay.this.getAttackTarget() != null && EntityBlueJay.this.getAttackTarget().isEntityAlive())
                    || EntityBlueJay.this.isTrusting()) {
                return false;
            }
            if (!this.mustUpdate) {
                long worldTime = EntityBlueJay.this.world.getTotalWorldTime() % 10;
                if (EntityBlueJay.this.getIdleTime() >= 100 && worldTime != 0) {
                    return false;
                }
                if (EntityBlueJay.this.getRNG().nextInt(this.executionChance) != 0 && worldTime != 0) {
                    return false;
                }
            }
            List<Entity> list = EntityBlueJay.this.world.getEntitiesWithinAABB(Entity.class, getTargetableArea(getTargetDistance()), targetEntitySelector);
            if (list.isEmpty()) {
                return false;
            }
            Collections.sort(list, this.theNearestAttackableTargetSorter);
            this.targetEntity = list.get(0);
            this.mustUpdate = false;
            return true;
        }

        @Override
        public boolean shouldContinueExecuting() {
            return targetEntity != null;
        }

        public void resetTask() {
            flightTarget = null;
            this.targetEntity = null;
        }

        @Override
        public void updateTask() {
            if (cooldown > 0) {
                cooldown--;
            }
            if (flightTarget != null) {
                EntityBlueJay.this.setFlying(true);
                EntityBlueJay.this.getMoveHelper().setMoveTo(flightTarget.x, flightTarget.y, flightTarget.z, 1F);
                if (cooldown == 0 && EntityBlueJay.this.isTargetBlocked(flightTarget)) {
                    cooldown = 30;
                    flightTarget = null;
                }
            }
            if (targetEntity != null) {
                if (EntityBlueJay.this.onGround || flightTarget == null || EntityBlueJay.this.getDistanceSq(flightTarget.x, flightTarget.y, flightTarget.z) < 3) {
                    Vec3d vec = EntityBlueJay.this.getBlockInViewAway(targetEntity.getPositionVector(), 0);
                    if (vec != null && vec.y > EntityBlueJay.this.posY) {
                        flightTarget = vec;
                    }
                }
                if (EntityBlueJay.this.getDistance(targetEntity) > 20.0F) {
                    this.resetTask();
                }
            }
        }

        protected double getTargetDistance() {
            return 4D;
        }

        protected AxisAlignedBB getTargetableArea(double targetDistance) {
            Vec3d renderCenter = new Vec3d(EntityBlueJay.this.posX, EntityBlueJay.this.posY + 0.5, EntityBlueJay.this.posZ);
            return new AxisAlignedBB(-targetDistance, -targetDistance, -targetDistance, targetDistance, targetDistance, targetDistance).offset(renderCenter);
        }

        public class Sorter implements Comparator<Entity> {
            private final Entity theEntity;

            public Sorter(Entity theEntityIn) {
                this.theEntity = theEntityIn;
            }

            public int compare(Entity p1, Entity p2) {
                double d0 = this.theEntity.getDistanceSq(p1);
                double d1 = this.theEntity.getDistanceSq(p2);
                return Double.compare(d0, d1);
            }
        }
    }

    private class AITargetItems extends CreatureAITargetItems {
        public AITargetItems(boolean checkSight, boolean onlyNearby, int tickThreshold, int radius) {
            super(EntityBlueJay.this, checkSight, onlyNearby, tickThreshold, radius);
            this.executionChance = 1;
        }

        @Override
        public void resetTask() {
            super.resetTask();
            EntityBlueJay.this.aiItemFlag = false;
        }

        @Override
        public boolean shouldExecute() {
            return super.shouldExecute() && (EntityBlueJay.this.getAttackTarget() == null || !EntityBlueJay.this.getAttackTarget().isEntityAlive());
        }

        @Override
        public boolean shouldContinueExecuting() {
            return super.shouldContinueExecuting() && (EntityBlueJay.this.getAttackTarget() == null || !EntityBlueJay.this.getAttackTarget().isEntityAlive());
        }

        @Override
        public void updateTask() {
            if (this.targetEntity == null || !this.targetEntity.isEntityAlive()) {
                this.resetTask();
                EntityBlueJay.this.getNavigator().clearPath();
            } else {
                moveTo();
                if (EntityBlueJay.this.getDistanceSq(this.targetEntity) < EntityBlueJay.this.getMaxDistToItem()
                        && EntityBlueJay.this.getHeldItemMainhand().isEmpty()) {
                    EntityBlueJay.this.onGetItem(this.targetEntity);
                    this.targetEntity.getItem().shrink(1);
                    resetTask();
                }
            }
        }

        protected void moveTo() {
            EntityBlueJay jay = EntityBlueJay.this;
            if (this.targetEntity != null) {
                jay.aiItemFlag = true;
                if (jay.getDistance(this.targetEntity) < 2) {
                    jay.getMoveHelper().setMoveTo(this.targetEntity.posX, this.targetEntity.posY, this.targetEntity.posZ, 1);
                    jay.peck();
                }
                if (jay.getDistance(this.targetEntity) > 8 || jay.isFlying()) {
                    jay.setFlying(true);
                    float f = (float) (jay.posX - this.targetEntity.posX);
                    float f1 = 1.8F;
                    float f2 = (float) (jay.posZ - this.targetEntity.posZ);
                    float xzDist = MathHelper.sqrt(f * f + f2 * f2);
                    if (!jay.canEntityBeSeen(this.targetEntity)) {
                        jay.getMoveHelper().setMoveTo(this.targetEntity.posX, 1 + jay.posY, this.targetEntity.posZ, 1);
                    } else {
                        if (xzDist < 5) {
                            f1 = 0;
                        }
                        jay.getMoveHelper().setMoveTo(this.targetEntity.posX, f1 + this.targetEntity.posY, this.targetEntity.posZ, 1);
                    }
                } else {
                    jay.getNavigator().tryMoveToXYZ(this.targetEntity.posX, this.targetEntity.posY, this.targetEntity.posZ, 1);
                }
            }
        }
    }

    private class AIFollowFeederOrRaccoon extends EntityAIBase {
        private Entity following;

        AIFollowFeederOrRaccoon() {
            this.setMutexBits(3);
        }

        @Override
        public boolean shouldExecute() {
            if (EntityBlueJay.this.isRiding() || (EntityBlueJay.this.getAttackTarget() != null && EntityBlueJay.this.getAttackTarget().isEntityAlive())) {
                return false;
            }
            if (EntityBlueJay.this.getRaccoonUUID() != null) {
                Entity raccoon = EntityBlueJay.this.getRaccoon();
                if (raccoon != null) {
                    following = raccoon;
                    return true;
                }
            }
            if (EntityBlueJay.this.getFeedTime() > 0) {
                Entity feeder = EntityBlueJay.this.getLastFeeder();
                if (feeder != null) {
                    following = feeder;
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean shouldContinueExecuting() {
            EntityLivingBase target = EntityBlueJay.this.getAttackTarget();
            return following != null && following.isEntityAlive() && (target == null || !target.isEntityAlive())
                    && (following instanceof EntityRaccoon || EntityBlueJay.this.getFeedTime() > 0) && !EntityBlueJay.this.isRiding();
        }

        @Override
        public void updateTask() {
            double dist = EntityBlueJay.this.getDistance(following);
            if (dist > 6 || EntityBlueJay.this.isFlying()) {
                EntityBlueJay.this.setFlying(true);
                EntityBlueJay.this.getMoveHelper().setMoveTo(following.posX, following.posY, following.posZ, 1);
            } else {
                EntityBlueJay.this.getNavigator().tryMoveToXYZ(following.posX, following.posY, following.posZ, 1);
            }
            if (EntityBlueJay.this.isFlying() && EntityBlueJay.this.onGround && dist < 3) {
                EntityBlueJay.this.setFlying(false);
            }
            if (following instanceof EntityRaccoon) {
                EntityRaccoon raccoon = (EntityRaccoon) following;
                if (dist > 40) {
                    EntityBlueJay.this.setPosition(following.posX, following.posY, following.posZ);
                }
                if (dist < 2.5F) {
                    EntityBlueJay.this.getMoveHelper().setMoveTo(following.posX, following.posY, following.posZ, 1);
                }
                if (dist < 1F && raccoon.getPassengers().isEmpty()) {
                    EntityBlueJay.this.startRiding(raccoon, true);
                }
            }
        }
    }
}
