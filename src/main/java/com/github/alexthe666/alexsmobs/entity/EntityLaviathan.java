package com.github.alexthe666.alexsmobs.entity;

import com.github.alexthe666.alexsmobs.config.AMConfig;
import com.github.alexthe666.alexsmobs.entity.ai.*;
import com.github.alexthe666.alexsmobs.item.AMItemRegistry;
import com.github.alexthe666.alexsmobs.misc.AMAdvancementTriggerRegistry;
import com.github.alexthe666.alexsmobs.misc.AMSoundRegistry;
import com.github.alexthe666.alexsmobs.misc.AMTagRegistry;
import com.google.common.base.Predicate;
import com.google.common.collect.Sets;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityWaterMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class EntityLaviathan extends EntityAnimal implements ISemiAquatic, IHerdPanic {

    private static final DataParameter<Boolean> OBSIDIAN = EntityDataManager.createKey(EntityLaviathan.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Float> HEAD_HEIGHT = EntityDataManager.createKey(EntityLaviathan.class, DataSerializers.FLOAT);
    private static final DataParameter<Integer> CHILL_TIME = EntityDataManager.createKey(EntityLaviathan.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> ATTACK_TICK = EntityDataManager.createKey(EntityLaviathan.class, DataSerializers.VARINT);
    private static final DataParameter<Boolean> HAS_BODY_GEAR = EntityDataManager.createKey(EntityLaviathan.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> HAS_HEAD_GEAR = EntityDataManager.createKey(EntityLaviathan.class, DataSerializers.BOOLEAN);
    private static final Predicate<EntityCrimsonMosquito> HEALTHY_MOSQUITOES = new Predicate<EntityCrimsonMosquito>() {
        @Override
        public boolean apply(@Nullable EntityCrimsonMosquito mob) {
            return mob != null && mob.isEntityAlive() && mob.getHealth() > 0 && !mob.isSick();
        }
    };
    public static final ResourceLocation OBSIDIAN_LOOT = new ResourceLocation("alexsmobs", "entities/laviathan_obsidian");
    public final EntityLaviathanPart headPart;
    public final EntityLaviathanPart neckPart1;
    public final EntityLaviathanPart neckPart2;
    public final EntityLaviathanPart neckPart3;
    public final EntityLaviathanPart neckPart4;
    public final EntityLaviathanPart neckPart5;
    public final EntityLaviathanPart seat1;
    public final EntityLaviathanPart seat2;
    public final EntityLaviathanPart seat3;
    public final EntityLaviathanPart seat4;
    public final EntityLaviathanPart[] theEntireNeck;
    public final EntityLaviathanPart[] allParts;
    public final EntityLaviathanPart[] seatParts;
    private final UUID[] riderPositionMap = new UUID[4];
    public float prevHeadHeight = 0F;
    public float swimProgress = 0F;
    public float prevSwimProgress = 0F;
    public float biteProgress;
    public float prevBiteProgress;
    public int revengeCooldown = 0;
    private boolean isLandNavigator;
    private int conversionTime = 0;
    private int dismountCooldown = 0;
    private int headPeakCooldown = 0;
    private boolean hasObsidianArmor;
    private int blockBreakCounter;
    private double lastX = 0;
    private double lastZ = 0;
    private boolean laviathanPartsSpawned;

    public EntityLaviathan(World world) {
        super(world);
        this.stepHeight = 1.3F;
        this.setPathPriority(PathNodeType.WATER, 0.0F);
        this.setPathPriority(PathNodeType.LAVA, 0.0F);
        this.setPathPriority(PathNodeType.DANGER_FIRE, 0.0F);
        this.setPathPriority(PathNodeType.DAMAGE_FIRE, 0.0F);
        this.setSize(3.3F, 2.4F);
        this.headPart = new EntityLaviathanPart(this, 1.2F, 0.9F);
        this.neckPart1 = new EntityLaviathanPart(this, 0.9F, 0.9F);
        this.neckPart2 = new EntityLaviathanPart(this, 0.9F, 0.9F);
        this.neckPart3 = new EntityLaviathanPart(this, 0.9F, 0.9F);
        this.neckPart4 = new EntityLaviathanPart(this, 0.9F, 0.9F);
        this.neckPart5 = new EntityLaviathanPart(this, 0.9F, 0.9F);
        this.seat1 = new EntityLaviathanPart(this, 0.9F, 0.4F);
        this.seat2 = new EntityLaviathanPart(this, 0.9F, 0.4F);
        this.seat3 = new EntityLaviathanPart(this, 0.9F, 0.4F);
        this.seat4 = new EntityLaviathanPart(this, 0.9F, 0.4F);
        this.theEntireNeck = new EntityLaviathanPart[]{this.neckPart1, this.neckPart2, this.neckPart3, this.neckPart4, this.neckPart5, this.headPart};
        this.allParts = new EntityLaviathanPart[]{this.neckPart1, this.neckPart2, this.neckPart3, this.neckPart4, this.neckPart5, this.headPart, this.seat1, this.seat2, this.seat3, this.seat4};
        this.seatParts = new EntityLaviathanPart[]{this.seat1, this.seat2, this.seat3, this.seat4};
        this.switchNavigator(true);
    }

    public static boolean canLaviathanSpawn(World world, BlockPos pos) {
        BlockPos.MutableBlockPos blockpos$mutable = new BlockPos.MutableBlockPos(pos.getX(), pos.getY(), pos.getZ());
        do {
            blockpos$mutable.setPos(blockpos$mutable.getX(), blockpos$mutable.getY() + 1, blockpos$mutable.getZ());
        } while (world.getBlockState(blockpos$mutable).getMaterial() == Material.LAVA);
        return world.isAirBlock(blockpos$mutable);
    }

    @Override
    public boolean getCanSpawnHere() {
        return AMEntityRegistry.rollSpawn(AMConfig.laviathanSpawnRolls, this.getRNG(), AMEntityRegistry.AMSpawnReason.OTHER) && super.getCanSpawnHere();
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return AMSoundRegistry.LAVIATHAN_IDLE;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return AMSoundRegistry.LAVIATHAN_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return AMSoundRegistry.LAVIATHAN_HURT;
    }

    @Override
    @Nullable
    protected ResourceLocation getLootTable() {
        return this.isObsidian() ? OBSIDIAN_LOOT : super.getLootTable();
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(60.0D);
        AMEntityRegistry.registerAttributeIfAbsent(this, SharedMonsterAttributes.ATTACK_DAMAGE);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(1.0D);
        this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(10.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.3D);
        this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(1.0D);
    }

    @Override
    public boolean canBeCollidedWith() {
        return this.isEntityAlive();
    }

    @Override
    public boolean canBeRidden(Entity entityIn) {
        return this.getPassengers().size() < 4 && !this.isInLava() && !this.isInWater();
    }

    @Override
    public void applyEntityCollision(Entity entity) {
        if (!entity.isRidingSameEntity(this)) {
            entity.motionX += this.motionX;
            entity.motionY += this.motionY;
            entity.motionZ += this.motionZ;
        }
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        return AMTagRegistry.itemInTag(AMTagRegistry.LAVIATHAN_BREEDABLES, stack.getItem());
    }

    @Override
    public boolean canBePushed() {
        return false;
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        ItemStack itemstack = player.getHeldItem(hand);
        Item item = itemstack.getItem();
        if (AMTagRegistry.itemInTag(AMTagRegistry.LAVIATHAN_FOODSTUFFS, item) && this.getHealth() < this.getMaxHealth()) {
            if (!player.capabilities.isCreativeMode) {
                itemstack.shrink(1);
            }
            this.heal(10);
            return true;
        }
        if (item == AMItemRegistry.STRADDLE_HELMET && !this.hasHeadGear() && !this.isChild()) {
            if (!player.capabilities.isCreativeMode) {
                itemstack.shrink(1);
            }
            this.setHeadGear(true);
            return true;
        }
        if (item == AMItemRegistry.STRADDLE_SADDLE && !this.hasBodyGear() && !this.isChild()) {
            if (!player.capabilities.isCreativeMode) {
                itemstack.shrink(1);
            }
            this.setBodyGear(true);
            return true;
        }
        boolean type = super.processInteract(player, hand);
        if (!type && !isBreedingItem(itemstack) && this.hasBodyGear()) {
            if (!this.isChild()) {
                if (!player.isSneaking()) {
                    if (!this.world.isRemote) {
                        player.startRiding(this);
                    }
                } else {
                    this.removePassengers();
                }
                return true;
            }
        }
        return type;
    }

    public int getClosestOpenSeat(Vec3d entityPos) {
        int closest = -1;
        double closestDistance = Double.MAX_VALUE;
        for (int i = 0; i < seatParts.length; i++) {
            double dist = entityPos.distanceTo(new Vec3d(seatParts[i].posX, seatParts[i].posY, seatParts[i].posZ));
            if (closest == -1 || closestDistance > dist) {
                if (riderPositionMap[i] == null) {
                    closest = i;
                    closestDistance = dist;
                }
            }
        }
        return closest;
    }

    @Nullable
    @Override
    public Entity getControllingPassenger() {
        int playerPosition = -1;
        EntityPlayer player = null;
        if (this.hasHeadGear() && this.hasBodyGear()) {
            for (Entity passenger : this.getPassengers()) {
                if (passenger instanceof EntityPlayer) {
                    EntityPlayer player2 = (EntityPlayer) passenger;
                    int player2Position = getRiderPosition(passenger);
                    if (player == null || playerPosition > player2Position) {
                        player = player2;
                        playerPosition = player2Position;
                    }
                }
            }
        }
        return player;
    }

    public int getSeatRaytrace(Entity player) {
        double dist = this.getDistance(player);
        Vec3d eyePos = new Vec3d(player.posX, player.posY + player.getEyeHeight(), player.posZ);
        Vec3d lookVec = player.getLook(1.0F);
        Vec3d hitVec = eyePos.add(lookVec.scale(dist));
        return getClosestOpenSeat(hitVec);
    }

    @Override
    public void removePassenger(Entity entity) {
        super.removePassenger(entity);
        dismountCooldown = 40 + rand.nextInt(40);
        if (entity != null) {
            for (int i = 0; i < riderPositionMap.length; i++) {
                if (riderPositionMap[i] != null && riderPositionMap[i].equals(entity.getUniqueID())) {
                    riderPositionMap[i] = null;
                }
            }
        }
    }

    public int getRiderPosition(Entity passenger) {
        int posit = -1;
        for (int i = 0; i < this.riderPositionMap.length; i++) {
            if (this.riderPositionMap[i] != null && passenger != null && passenger.getUniqueID().equals(riderPositionMap[i])) {
                posit = i;
            }
        }
        return posit;
    }

    @Override
    public void addPassenger(Entity entity) {
        int rayTrace = getSeatRaytrace(entity);
        if (rayTrace >= 0 && rayTrace < 4) {
            if (riderPositionMap[rayTrace] != null) {
                if (!this.world.isRemote && this.world instanceof WorldServer) {
                    Entity kickOff = ((WorldServer) this.world).getEntityFromUuid(riderPositionMap[rayTrace]);
                    riderPositionMap[rayTrace] = null;
                    if (kickOff != null) {
                        kickOff.dismountRidingEntity();
                    }
                }
            }
            riderPositionMap[rayTrace] = entity.getUniqueID();
            super.addPassenger(entity);
        }
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setBoolean("Obsidian", this.isObsidian());
        compound.setBoolean("HeadGear", this.hasHeadGear());
        compound.setBoolean("BodyGear", this.hasBodyGear());
        compound.setInteger("ChillTime", this.getChillTime());
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.setObsidian(compound.getBoolean("Obsidian"));
        this.setHeadGear(compound.getBoolean("HeadGear"));
        this.setBodyGear(compound.getBoolean("BodyGear"));
        this.setChillTime(compound.getInteger("ChillTime"));
        this.laviathanPartsSpawned = false;
    }

    @Override
    public void updatePassenger(Entity passenger) {
        if (this.getPassengers().contains(passenger)) {
            int posit = getRiderPosition(passenger);
            if (posit < 0 || posit > 3) {
                passenger.dismountRidingEntity();
            } else {
                EntityLaviathanPart seat = seatParts[posit];
                passenger.setPosition(seat.posX, this.posY + this.getMountedYOffset() + passenger.getYOffset(), seat.posZ);
            }
        }
    }

    @Override
    public double getMountedYOffset() {
        return (double) this.height - 0.4F;
    }

    @Override
    protected void dropEquipment(boolean wasRecentlyHit, int lootingModifier) {
        super.dropEquipment(wasRecentlyHit, lootingModifier);
        if (this.hasBodyGear()) {
            if (!this.world.isRemote) {
                this.entityDropItem(new ItemStack(AMItemRegistry.STRADDLE_SADDLE), 0.0F);
            }
        }
        if (this.hasHeadGear()) {
            if (!this.world.isRemote) {
                this.entityDropItem(new ItemStack(AMItemRegistry.STRADDLE_HELMET), 0.0F);
            }
        }
        this.setBodyGear(false);
        this.setHeadGear(false);
    }

    private void switchNavigator(boolean onLand) {
        if (onLand) {
            this.moveHelper = new EntityMoveHelper(this);
            this.navigator = createNavigator(this.world);
            this.isLandNavigator = true;
        } else {
            this.moveHelper = new LaviathanMoveController(this);
            this.navigator = new BoneSerpentPathNavigator(this, this.world);
            this.isLandNavigator = false;
        }
    }

    @Override
    protected PathNavigate createNavigator(World worldIn) {
        return new GroundPathNavigatorWide(this, worldIn);
    }

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(0, new AnimalAIHerdPanic(this, 1.0D) {
            @Override
            public boolean shouldExecute() {
                return super.shouldExecute() && !EntityLaviathan.this.hasHeadGear();
            }
        });
        this.tasks.addTask(1, new EntityAIMate(this, 1.0D));
        this.tasks.addTask(3, new EntityAITempt(this, 1.1D, Items.AIR, false) {
            @Override
            protected boolean isTempting(ItemStack stack) {
                return AMTagRegistry.itemInTag(AMTagRegistry.LAVIATHAN_BREEDABLES, stack.getItem())
                        || AMTagRegistry.itemInTag(AMTagRegistry.LAVIATHAN_FOODSTUFFS, stack.getItem());
            }
        });
        this.tasks.addTask(4, new AnimalAIFindWaterLava(this));
        this.tasks.addTask(5, new LaviathanAIRandomSwimming(this, 1.0D, 22) {
            @Override
            public boolean shouldExecute() {
                return super.shouldExecute() && !EntityLaviathan.this.hasHeadGear() && !EntityLaviathan.this.hasBodyGear();
            }
        });
        this.tasks.addTask(6, new EntityAILookIdle(this));
        this.tasks.addTask(7, new EntityAIWatchClosest(this, EntityPlayer.class, 6.0F));
    }

    @Override
  public float getBlockPathWeight(BlockPos pos) {
        Material mat = this.world.getBlockState(pos).getMaterial();
        if (mat == Material.WATER || mat == Material.LAVA) {
            return 10.0F;
        } else {
            return this.isInLava() ? Float.NEGATIVE_INFINITY : 0.0F;
        }
    }

    @Override
    public int getMaxFallHeight() {
        return 256;
    }

    @Override
    public int getMaxSpawnedInChunk() {
        return 1;
    }

    public boolean isMaxGroupSizeReached(int sizeIn) {
        return false;
    }

    @Override
    public void travel(float strafe, float vertical, float forward) {
        boolean liquid = this.isInLava() || this.isInWater();
        if (this.getControllingPassenger() instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) this.getControllingPassenger();
            this.rotationYaw = player.rotationYaw;
            this.renderYawOffset = this.rotationYaw;
            this.rotationYawHead = player.rotationYawHead;
            this.rotationPitch = player.rotationPitch * 0.5F;
            this.stepHeight = 1.3F;
            this.getNavigator().clearPath();
            this.setAttackTarget(null);
            if (player.moveForward != 0) {
                float f = player.moveForward < 0 ? 0.5F : 1.0F;
                Vec3d lookVec = player.getLook(1.0F);
                float y = (float) lookVec.y * 0.1F;
                float waterAt = (float) getMaxFluidHeight();
                float half = this.height * 0.5F;
                if (waterAt > half) {
                    y = MathHelper.clamp(waterAt - half, 0F, 0.15F);
                } else if (waterAt < half) {
                    y = MathHelper.clamp(waterAt - half, -0.15F, 0F);
                }
                if (this.collidedHorizontally) {
                    y += 0.4F;
                }
                strafe = player.moveStrafing * 0.25F;
                vertical = y;
                forward = player.moveForward * (shouldSwim() ? 0.75F : 0.25F) * f;
                this.setSprinting(true);
            } else {
                this.setSprinting(false);
                strafe = 0;
                vertical = 0;
                forward = 0;
            }
        }
        if (!this.world.isRemote && liquid) {
            this.moveRelative(strafe, vertical, forward, this.getAIMoveSpeed());
            this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
            double scale = this.isBeingRidden() ? 0.5D : 0.9D;
            this.motionX *= scale;
            this.motionY *= scale;
            this.motionZ *= scale;
            if (!this.isBeingRidden() && !this.isChilling()) {
                this.motionY -= 0.01F;
            }
        } else {
            super.travel(strafe, vertical, forward);
        }
    }

    public void calculateEntityAnimation() {
        float f1 = (float) MathHelper.sqrt(Math.pow(this.posX - this.lastX, 2) + Math.pow(this.posZ - this.lastZ, 2));
        float walkSpeed = 4.0F;
        float f2 = Math.min(f1 * walkSpeed, 1.0F);
        this.limbSwingAmount += (f2 - this.limbSwingAmount) * 0.4F;
    }

    @Override
    public int getVerticalFaceSpeed() {
        return 50;
    }

    @Override
    public int getHorizontalFaceSpeed() {
        return 50;
    }

    public int getFaceRotSpeed() {
        return 4;
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
    public EnumCreatureAttribute getCreatureAttribute() {
        return EnumCreatureAttribute.UNDEFINED;
    }

    @Override
    public void onUpdate() {
        if (!this.world.isRemote && !this.laviathanPartsSpawned) {
            for (EntityLaviathanPart part : this.allParts) {
                if (!part.isEntityAlive()) {
                    part.setPosition(this.posX, this.posY, this.posZ);
                }
                this.world.spawnEntity(part);
            }
            this.laviathanPartsSpawned = true;
        }
        super.onUpdate();
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        this.renderYawOffset = approachDegrees(this.prevRenderYawOffset, this.renderYawOffset, this.getFaceRotSpeed());
        prevSwimProgress = swimProgress;
        prevBiteProgress = biteProgress;
        prevHeadHeight = this.getHeadHeight();
        if (shouldSwim()) {
            if (swimProgress < 5F) {
                swimProgress++;
            }
        } else {
            if (swimProgress > 0F) {
                swimProgress--;
            }
        }

        if (this.isObsidian()) {
            if (!hasObsidianArmor) {
                hasObsidianArmor = true;
                this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(30F);
            }
        } else {
            if (hasObsidianArmor) {
                hasObsidianArmor = false;
                this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(10F);
            }
        }

        if (!this.world.isRemote) {
            if (!this.isObsidian() && (this.isInWater() || this.isInsideOfMaterial(Material.WATER))) {
                if (conversionTime < 300) {
                    conversionTime++;
                } else {
                    this.setObsidian(true);
                }
            }
            if (shouldSwim()) {
                this.fallDistance = 0.0F;
            }
        }
        float neckBase = 0.8F;
        if (!this.isAIDisabled()) {
            Vec3d[] avector3d = new Vec3d[this.allParts.length];
            for (int j = 0; j < this.allParts.length; ++j) {
                this.allParts[j].collideWithNearbyEntities();
                avector3d[j] = new Vec3d(this.allParts[j].posX, this.allParts[j].posY, this.allParts[j].posZ);
            }
            float yaw = this.renderYawOffset * 0.017453292F;
            float neckContraction = 2.0F * Math.abs(getHeadHeight() / 3) + 0.5F * Math.abs(getHeadYaw(0) / 50F);

            for (int l = 0; l < this.theEntireNeck.length; ++l) {
                float f = l / ((float) this.theEntireNeck.length);
                float f1 = -(2.2F + l - f * neckContraction);
                float f2 = MathHelper.sin(yaw + (float) Math.toRadians(f * getHeadYaw(0))) * (1 - Math.abs((this.rotationPitch) / 90F));
                float f3 = MathHelper.cos(yaw + (float) Math.toRadians(f * getHeadYaw(0))) * (1 - Math.abs((this.rotationPitch) / 90F));
                this.setPartPosition(this.theEntireNeck[l], f2 * f1, neckBase + Math.sin(f * Math.PI * 0.5F) * (getHeadHeight() * 1.1F), -f3 * f1);
            }
            this.setPartPosition(this.seat1, getXForPart(yaw, 145) * 0.75F, 2F, getZForPart(yaw, 145) * 0.75F);
            this.setPartPosition(this.seat2, getXForPart(yaw, -145) * 0.75F, 2F, getZForPart(yaw, -145) * 0.75F);
            this.setPartPosition(this.seat3, getXForPart(yaw, 35) * 0.95F, 2F, getZForPart(yaw, 35) * 0.95F);
            this.setPartPosition(this.seat4, getXForPart(yaw, -35) * 0.95F, 2F, getZForPart(yaw, -35) * 0.95F);

            if (this.world.isRemote && this.isChilling()) {
                if (!this.isChild()) {
                    this.world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, this.posX + getXForPart(yaw, 158) * 1.75F, this.posY + 1.0D, this.posZ + getZForPart(yaw, 158) * 1.75F, 0.0D, this.rand.nextDouble() / 5.0D, 0.0D);
                    this.world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, this.posX + getXForPart(yaw, -166) * 1.48F, this.posY + 1.0D, this.posZ + getZForPart(yaw, -166) * 1.48F, 0.0D, this.rand.nextDouble() / 5.0D, 0.0D);
                    this.world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, this.posX + getXForPart(yaw, 14) * 1.78F, this.posY + 0.9D, this.posZ + getZForPart(yaw, 14) * 1.78F, 0.0D, this.rand.nextDouble() / 5.0D, 0.0D);
                    this.world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, this.posX + getXForPart(yaw, -14) * 1.6F, this.posY + 1.1D, this.posZ + getZForPart(yaw, -14) * 1.6F, 0.0D, this.rand.nextDouble() / 5.0D, 0.0D);
                }
                this.world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, this.headPart.posX + (this.rand.nextDouble() - 0.5D) * 0.6D, this.headPart.posY + this.headPart.height * 0.9D, this.headPart.posZ + (this.rand.nextDouble() - 0.5D) * 0.6D, 0.0D, this.rand.nextDouble() / 5.0D, 0.0D);
            }

            for (int l = 0; l < this.allParts.length; ++l) {
                this.allParts[l].prevPosX = avector3d[l].x;
                this.allParts[l].prevPosY = avector3d[l].y;
                this.allParts[l].prevPosZ = avector3d[l].z;
                this.allParts[l].lastTickPosX = avector3d[l].x;
                this.allParts[l].lastTickPosY = avector3d[l].y;
                this.allParts[l].lastTickPosZ = avector3d[l].z;
            }
        }
        if ((this.isInLava() || this.isInWater() || this.isInsideOfMaterial(Material.WATER)) && this.isLandNavigator) {
            switchNavigator(false);
        }
        if (!(this.isInLava() || this.isInWater() || this.isInsideOfMaterial(Material.WATER)) && !this.isLandNavigator) {
            switchNavigator(true);
        }
        if (!this.world.isRemote) {
            if (this.getChillTime() > 0) {
                this.setChillTime(this.getChillTime() - 1);
            } else if (this.shouldSwim()) {
                if (rand.nextInt(this.isBeingRidden() ? 200 : 2000) == 0 && revengeCooldown == 0) {
                    this.setChillTime(100 + rand.nextInt(500));
                }
            }
            if (revengeCooldown > 0) {
                revengeCooldown--;
            }
            if (headPeakCooldown > 0) {
                headPeakCooldown--;
            }
            if (revengeCooldown == 0 && this.getRevengeTarget() != null) {
                this.setRevengeTarget(null);
            }
        }
        if (!this.world.isRemote) {
            if (this.getControllingPassenger() == null && (this.getChillTime() > 0 || this.hasHeadGear() || dismountCooldown > 0)) {
                floatLaviathan();
            }

            if (!this.isChilling() && headPeakCooldown == 0) {
                float low = getLowHeadHeight();
                this.setHeadHeight(this.getHeadHeight() + (0.5F + ((getLowHeadHeight() + getHighHeadHeight(low)) / 2F) - this.getHeadHeight()) * 0.2F);
            } else {
                if (getMaxFluidHeight() <= this.height * 0.5F && getMaxFluidHeight() >= this.height * 0.25F) {
                    float mot = (float) (this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ * this.motionZ);
                    this.setHeadHeight(MathHelper.clamp(this.getHeadHeight() + 0.1F - 0.2F * mot, 0, 2));
                    headPeakCooldown = 5;
                }
            }
        }
        if (this.isChilling()) {
            boolean keepChillin = false;
            boolean startBiting = false;
            List<EntityCrimsonMosquito> nearbyMosquitoes = this.world.getEntitiesWithinAABB(EntityCrimsonMosquito.class, this.getEntityBoundingBox().grow(30.0D), HEALTHY_MOSQUITOES);
            for (EntityCrimsonMosquito entity : nearbyMosquitoes) {
                entity.setLuringLaviathan(this.getEntityId());
                keepChillin = true;
            }
            if (keepChillin) {
                this.setChillTime(Math.max(20, this.getChillTime()));
            }
            List<EntityCrimsonMosquito> headMosquitoes = this.world.getEntitiesWithinAABB(EntityCrimsonMosquito.class, this.headPart.getEntityBoundingBox().grow(1.0D), HEALTHY_MOSQUITOES);
            for (EntityCrimsonMosquito entity : headMosquitoes) {
                startBiting = true;
                if (this.biteProgress == 5.0F) {
                    entity.attackEntityFrom(DamageSource.causeMobDamage(this), 1000);
                    entity.setShrink(true);
                    this.setChillTime(0);
                }
            }
            if (startBiting) {
                if (this.dataManager.get(ATTACK_TICK) <= 0 && this.biteProgress == 0) {
                    this.dataManager.set(ATTACK_TICK, 7);
                }
            }
        }
        if (this.dataManager.get(ATTACK_TICK) > 0) {
            this.dataManager.set(ATTACK_TICK, this.dataManager.get(ATTACK_TICK) - 1);
        }
        if (this.dataManager.get(ATTACK_TICK) > 0 && this.biteProgress < 5.0F) {
            this.biteProgress++;
        }
        if (this.dataManager.get(ATTACK_TICK) <= 0 && this.biteProgress > 0.0F) {
            this.biteProgress--;
        }
        if (dismountCooldown > 0) {
            dismountCooldown--;
        }
        if (this.hasBodyGear()) {
            List<Entity> list = this.world.getEntitiesWithinAABBExcludingEntity(this, this.getEntityBoundingBox().expand(0.2F, -0.01F, 0.2F));
            if (!list.isEmpty()) {
                boolean flag2 = !this.world.isRemote;
                for (int j = 0; j < list.size(); ++j) {
                    Entity entity = list.get(j);
                    if (!entity.isRidingSameEntity(this)) {
                        if (flag2 && !(entity instanceof EntityPlayer) && !entity.isBeingRidden() && entity.width < this.width && !(entity instanceof EntityLaviathan) && !(entity instanceof EntityMob) && entity instanceof EntityCreature && this.canBeRidden(entity) && !(entity instanceof EntityWaterMob)) {
                            entity.startRiding(this);
                        } else {
                            this.applyEntityCollision(entity);
                        }
                    }
                }
            }
        }
        if (this.isBeingRidden() && !this.world.isRemote && this.ticksExisted % 40 == 0 && this.getPassengers().size() > 3) {
            for (Entity entity : this.getPassengers()) {
                if (entity instanceof EntityPlayerMP) {
                    AMAdvancementTriggerRegistry.LAVIATHAN_FOUR_PASSENGERS.trigger((EntityPlayerMP) entity);
                }
            }
        }
        lastX = this.posX;
        lastZ = this.posZ;
        this.calculateEntityAnimation();
        this.scaleParts();
    }

    @Override
    public void updateAITasks() {
        super.updateAITasks();
        breakBlock();
    }

    public void breakBlock() {
        if (this.blockBreakCounter > 0) {
            --this.blockBreakCounter;
            return;
        }
        boolean flag = false;
        if (!this.world.isRemote && this.isBeingRidden() && this.blockBreakCounter == 0 && net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(world, this)) {
            AxisAlignedBB box = this.getEntityBoundingBox();
            for (int a = (int) Math.round(box.minX); a <= (int) Math.round(box.maxX); a++) {
                for (int b = (int) Math.round(box.minY) - 1; (b <= (int) Math.round(box.maxY) + 1) && (b <= 127); b++) {
                    for (int c = (int) Math.round(box.minZ); c <= (int) Math.round(box.maxZ); c++) {
                        BlockPos pos = new BlockPos(a, b, c);
                        IBlockState state = world.getBlockState(pos);
                        Block block = state.getBlock();
                        if (!state.getMaterial().isLiquid() && state.getMaterial().blocksMovement() && AMTagRegistry.blockInTag(AMTagRegistry.LAVIATHAN_BREAKABLES, block)) {
                            if (block != Blocks.AIR) {
                                this.motionX *= 0.6F;
                                this.motionZ *= 0.6F;
                                flag = true;
                                world.destroyBlock(pos, true);
                            }
                        }
                    }
                }
            }
        }
        if (flag) {
            blockBreakCounter = 10;
        }
    }

    public float getLowHeadHeight() {
        float checkAt = 0F;
        while (checkAt > -3F && !isHeadInWall((float) this.posY + checkAt) && !isHeadInLava((float) this.posY + checkAt)) {
            checkAt -= 0.2F;
        }
        return checkAt;
    }

    public float getHighHeadHeight(float low) {
        float checkAt = 3F;
        while (checkAt > 0) {
            if (isHeadInWall((float) this.posY + checkAt) && !isHeadInLava((float) this.posY + checkAt)) {
                break;
            }
            checkAt -= 0.2F;
        }
        return checkAt;
    }

    public boolean isHeadInWall(float offset) {
        if (this.noClip) {
            return false;
        } else {
            float f = 0.8F;
            AxisAlignedBB axisalignedbb = new AxisAlignedBB(
                    headPart.posX - f * 0.5F, offset, headPart.posZ - f * 0.5F,
                    headPart.posX + f * 0.5F, offset + 1.0E-6D, headPart.posZ + f * 0.5F);
            return !this.world.getCollisionBoxes(this, axisalignedbb).isEmpty();
        }
    }

    public boolean isHeadInLava(float offset) {
        if (this.noClip) {
            return false;
        } else {
            BlockPos pos = new BlockPos(headPart.posX, offset, headPart.posZ);
            Material mat = level().getBlockState(pos).getMaterial();
            return mat == Material.LAVA || mat == Material.WATER;
        }
    }

    private World level() {
        return this.world;
    }

    private void floatLaviathan() {
        if (this.shouldSwim()) {
            if (getMaxFluidHeight() >= this.height) {
                this.motionY = 0.12F;
            } else if (getMaxFluidHeight() >= this.height * 0.5F) {
                this.motionY = 0.08F;
            } else {
                this.motionY = 0.0F;
            }
        }
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        boolean prev = super.attackEntityFrom(source, amount);
        if (prev && source.getTrueSource() != null) {
            int fleeTime = 100 + getRNG().nextInt(150);
            this.revengeCooldown = fleeTime;
            this.setChillTime(0);
        }
        return prev;
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(OBSIDIAN, Boolean.FALSE);
        this.dataManager.register(HAS_BODY_GEAR, Boolean.FALSE);
        this.dataManager.register(HAS_HEAD_GEAR, Boolean.FALSE);
        this.dataManager.register(HEAD_HEIGHT, 0F);
        this.dataManager.register(CHILL_TIME, 0);
        this.dataManager.register(ATTACK_TICK, 0);
    }

    public boolean shouldSwim() {
        return getMaxFluidHeight() >= 0.1F || this.isInLava() || this.isInWater() || this.isInsideOfMaterial(Material.WATER);
    }

    private float getXForPart(float yaw, float degree) {
        return MathHelper.sin((float) (yaw + Math.toRadians(degree)));
    }

    private float getZForPart(float yaw, float degree) {
        return -MathHelper.cos((float) (yaw + Math.toRadians(degree)));
    }

    public float getHeadHeight() {
        return MathHelper.clamp(this.dataManager.get(HEAD_HEIGHT), -3, 3);
    }

    public void setHeadHeight(float height) {
        this.dataManager.set(HEAD_HEIGHT, MathHelper.clamp(height, -3, 3));
    }

    public boolean isObsidian() {
        return this.dataManager.get(OBSIDIAN);
    }

    public void setObsidian(boolean obsidian) {
        this.dataManager.set(OBSIDIAN, obsidian);
    }

    public boolean hasHeadGear() {
        return this.dataManager.get(HAS_HEAD_GEAR);
    }

    public void setHeadGear(boolean headGear) {
        this.dataManager.set(HAS_HEAD_GEAR, headGear);
    }

    public boolean hasBodyGear() {
        return this.dataManager.get(HAS_BODY_GEAR);
    }

    public void setBodyGear(boolean bodyGear) {
        this.dataManager.set(HAS_BODY_GEAR, bodyGear);
    }

    public int getChillTime() {
        return this.dataManager.get(CHILL_TIME);
    }

    public void setChillTime(int chillTime) {
        this.dataManager.set(CHILL_TIME, chillTime);
    }

    public float getHeadYaw(float interp) {
        float f;
        if (interp == 0.0F) {
            f = this.rotationYawHead - this.renderYawOffset;
        } else {
            float yBodyRot1 = this.prevRenderYawOffset + (this.renderYawOffset - this.prevRenderYawOffset) * interp;
            float yHeadRot1 = this.prevRotationYawHead + (this.rotationYawHead - this.prevRotationYawHead) * interp;
            f = yHeadRot1 - yBodyRot1;
        }
        return MathHelper.clamp(MathHelper.wrapDegrees(f), -50, 50);
    }

    private void setPartPosition(EntityLaviathanPart part, double offsetX, double offsetY, double offsetZ) {
        part.setPosition(this.posX + offsetX * part.scale, this.posY + offsetY * part.scale, this.posZ + offsetZ * part.scale);
    }

    public boolean attackEntityPartFrom(EntityLaviathanPart part, DamageSource source, float amount) {
        return this.attackEntityFrom(source, amount);
    }

    @Override
    public boolean shouldEnterWater() {
        return !this.isBeingRidden();
    }

    @Override
    public boolean shouldLeaveWater() {
        return this.isBeingRidden();
    }

    @Override
    public boolean shouldStopMoving() {
        return this.isBeingRidden();
    }

    @Override
    public int getWaterSearchRange() {
        return 15;
    }

    private double getMaxFluidHeight() {
        return Math.max(getFluidHeight(Material.LAVA), getFluidHeight(Material.WATER));
    }

    private double getFluidHeight(Material material) {
        AxisAlignedBB axisalignedbb = this.getEntityBoundingBox();
        int i = MathHelper.floor(axisalignedbb.minX);
        int j = MathHelper.ceil(axisalignedbb.maxX);
        int k = MathHelper.floor(axisalignedbb.minY);
        int l = MathHelper.ceil(axisalignedbb.maxY);
        int i1 = MathHelper.floor(axisalignedbb.minZ);
        int j1 = MathHelper.ceil(axisalignedbb.maxZ);
        BlockPos.PooledMutableBlockPos blockpos$mutable = BlockPos.PooledMutableBlockPos.retain();
        try {
            label39:
            for (int k1 = k; k1 < l; ++k1) {
                float f = 0.0F;
                for (int l1 = i; l1 < j; ++l1) {
                    for (int i2 = i1; i2 < j1; ++i2) {
                        blockpos$mutable.setPos(l1, k1, i2);
                        if (this.world.getBlockState(blockpos$mutable).getMaterial() == material) {
                            f = Math.max(f, 1.0F);
                        }
                        if (f >= 1.0F) {
                            continue label39;
                        }
                    }
                }
                if (f > 0.0F) {
                    return (double) blockpos$mutable.getY() + (double) f - axisalignedbb.minY;
                }
            }
            return 0.0D;
        } finally {
            blockpos$mutable.release();
        }
    }

    public boolean isChilling() {
        return this.getChillTime() > 0 && this.getMaxFluidHeight() <= this.height * 0.5F;
    }

    public void scaleParts() {
        for (EntityLaviathanPart parts : allParts) {
            float prev = parts.scale;
            parts.scale = this.isChild() ? 0.5F : 1F;
            if (prev != parts.scale) {
                parts.recalculateSize();
            }
        }
    }

    public Vec3d getLureMosquitoPos() {
        return new Vec3d(this.headPart.posX, this.headPart.posY + this.headPart.height * 0.4F, this.headPart.posZ);
    }

    @Override
    public void onPanic() {
    }

    @Override
    public boolean canPanic() {
        return true;
    }

    @Override
    public EntityAgeable createChild(EntityAgeable ageable) {
        EntityLaviathan laviathan = (EntityLaviathan) AMEntityRegistry.LAVIATHAN.newInstance(this.world);
        return laviathan;
    }

    @Override
    protected boolean canDropLoot() {
        return true;
    }

    static class LaviathanMoveController extends EntityMoveHelper {
        private final EntityLaviathan laviathan;

        LaviathanMoveController(EntityLaviathan laviathanIn) {
            super(laviathanIn);
            this.laviathan = laviathanIn;
        }

        @Override
        public void onUpdateMoveHelper() {
            float speed = (float) (this.speed * 3 * laviathan.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue());
            if (this.action == Action.MOVE_TO && (!this.laviathan.getNavigator().noPath() || laviathan.getControllingPassenger() != null)) {
                double lvt_1_1_ = this.posX - laviathan.posX;
                double lvt_3_1_ = this.posY - laviathan.posY;
                double lvt_5_1_ = this.posZ - laviathan.posZ;
                double lvt_7_1_ = lvt_1_1_ * lvt_1_1_ + lvt_3_1_ * lvt_3_1_ + lvt_5_1_ * lvt_5_1_;
                if (lvt_7_1_ < 2.5F) {
                    this.laviathan.setMoveForward(0.0F);
                } else {
                    float lvt_9_1_ = (float) (MathHelper.atan2(lvt_5_1_, lvt_1_1_) * (double) (180F / (float) Math.PI)) - 90.0F;
                    this.laviathan.rotationYaw = this.limitAngle(this.laviathan.rotationYaw, lvt_9_1_, 5F);
                    this.laviathan.rotationYawHead = this.limitAngle(this.laviathan.rotationYawHead, lvt_9_1_, 90.0F);
                    if (laviathan.shouldSwim()) {
                        laviathan.setAIMoveSpeed(speed * 0.03F);
                        float lvt_11_1_ = -((float) (MathHelper.atan2(lvt_3_1_, MathHelper.sqrt(lvt_1_1_ * lvt_1_1_ + lvt_5_1_ * lvt_5_1_)) * (double) (180F / (float) Math.PI)));
                        lvt_11_1_ = MathHelper.clamp(MathHelper.wrapDegrees(lvt_11_1_), -85.0F, 85.0F);
                        laviathan.rotationPitch = this.limitAngle(laviathan.rotationPitch, lvt_11_1_, 25.0F);
                        float lvt_12_1_ = MathHelper.cos(laviathan.rotationPitch * 0.017453292F);
                        float lvt_13_1_ = MathHelper.sin(laviathan.rotationPitch * 0.017453292F);
                        laviathan.moveForward = lvt_12_1_ * speed;
                        laviathan.motionY = -lvt_13_1_ * speed * 0.03F;
                    } else {
                        laviathan.setAIMoveSpeed(speed * 0.1F);
                    }
                }
            } else if (!laviathan.world.getBlockState(laviathan.getPosition().up()).getMaterial().isLiquid() && laviathan.getChillTime() <= 0) {
                laviathan.motionY -= 0.05D;
            }
        }
    }
    private static float approachDegrees(float current, float target, float maxChange) {
        float delta = MathHelper.wrapDegrees(target - current);
        if (delta > maxChange) {
            delta = maxChange;
        }
        if (delta < -maxChange) {
            delta = -maxChange;
        }
        return current + delta;
    }
}