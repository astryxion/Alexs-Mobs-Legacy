package com.github.alexthe666.alexsmobs.entity;

import com.github.alexthe666.alexsmobs.config.AMConfig;
import com.github.alexthe666.alexsmobs.entity.ai.CreatureAITargetItems;
import com.github.alexthe666.alexsmobs.entity.ai.DirectPathNavigator;
import com.github.alexthe666.alexsmobs.entity.ai.SeagullAIRevealTreasure;
import com.github.alexthe666.alexsmobs.entity.ai.SeagullAIStealFromPlayers;
import com.github.alexthe666.alexsmobs.item.AMItemRegistry;
import com.github.alexthe666.alexsmobs.misc.AMSoundRegistry;
import com.github.alexthe666.alexsmobs.misc.AMTagRegistry;
import com.google.common.base.Predicate;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIMate;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAITempt;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.ai.EntityMoveHelper;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import com.google.common.base.Optional;
import java.util.UUID;

public class EntitySeagull extends EntityAnimal implements ITargetsDroppedItems {

    /** 1.16 {@code MapDecoration.Type.RED_X} icon byte. */
    private static final byte MAP_ICON_RED_X = 4;
    /** 1.16 {@code MapDecoration.Type.TARGET_X} icon byte. */
    private static final byte MAP_ICON_TARGET_X = 5;

    private static final DataParameter<Boolean> FLYING = EntityDataManager.createKey(EntitySeagull.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Float> FLIGHT_LOOK_YAW = EntityDataManager.createKey(EntitySeagull.class, DataSerializers.FLOAT);
    private static final DataParameter<Integer> ATTACK_TICK = EntityDataManager.createKey(EntitySeagull.class, DataSerializers.VARINT);
    private static final DataParameter<Boolean> SITTING = EntityDataManager.createKey(EntitySeagull.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Optional<BlockPos>> TREASURE_POS = EntityDataManager.createKey(EntitySeagull.class, DataSerializers.OPTIONAL_BLOCK_POS);
    public float prevFlyProgress;
    public float flyProgress;
    public float prevFlapAmount;
    public float flapAmount;
    public boolean aiItemFlag = false;
    public float attackProgress;
    public float prevAttackProgress;
    public float sitProgress;
    public float prevSitProgress;
    public int stealCooldown = rand.nextInt(2500);
    private boolean isLandNavigator;
    private int timeFlying;
    private BlockPos orbitPos = null;
    private double orbitDist = 5D;
    private boolean orbitClockwise = false;
    private boolean fallFlag = false;
    private int flightLookCooldown = 0;
    private float targetFlightLookYaw;
    private int heldItemTime = 0;
    public int treasureSitTime;
    public UUID feederUUID = null;

    public EntitySeagull(World worldIn) {
        super(worldIn);
        this.setPathPriority(PathNodeType.DANGER_FIRE, -1.0F);
        this.setPathPriority(PathNodeType.WATER, -1.0F);
        this.setPathPriority(PathNodeType.WATER, 16.0F);
        this.setPathPriority(PathNodeType.OPEN, -1.0F);
        this.setPathPriority(PathNodeType.FENCE, -1.0F);
        switchNavigator(false);
    }

    protected SoundEvent getAmbientSound() {
        return AMSoundRegistry.SEAGULL_IDLE;
    }

    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return AMSoundRegistry.SEAGULL_HURT;
    }

    protected SoundEvent getDeathSound() {
        return AMSoundRegistry.SEAGULL_HURT;
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setBoolean("Flying", this.isFlying());
        compound.setBoolean("Sitting", this.isSitting());
        compound.setInteger("StealCooldown", this.stealCooldown);
        compound.setInteger("TreasureSitTime", this.treasureSitTime);
        if (feederUUID != null) {
            compound.setUniqueId("FeederUUID", feederUUID);
        }
        if (this.getTreasurePos() != null) {
            compound.setInteger("TresX", this.getTreasurePos().getX());
            compound.setInteger("TresY", this.getTreasurePos().getY());
            compound.setInteger("TresZ", this.getTreasurePos().getZ());
        }
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.setFlying(compound.getBoolean("Flying"));
        this.setSitting(compound.getBoolean("Sitting"));
        this.stealCooldown = compound.getInteger("StealCooldown");
        this.treasureSitTime = compound.getInteger("TreasureSitTime");
        if (compound.hasUniqueId("FeederUUID")) {
            this.feederUUID = compound.getUniqueId("FeederUUID");
        }
        if (compound.hasKey("TresX") && compound.hasKey("TresY") && compound.hasKey("TresZ")) {
            this.setTreasurePos(new BlockPos(compound.getInteger("TresX"), compound.getInteger("TresY"), compound.getInteger("TresZ")));
        }
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        AMEntityRegistry.registerAttributeIfAbsent(this, SharedMonsterAttributes.ATTACK_DAMAGE);
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(8.0D);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(1.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.2D);
    }

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(1, new SeagullAIRevealTreasure(this));
        this.tasks.addTask(2, new SeagullAIStealFromPlayers(this));
        this.tasks.addTask(3, new EntityAIMate(this, 1.0D));
        this.tasks.addTask(4, new EntityAITempt(this, 1.0D, Items.FISH, false) {
            @Override
            protected boolean isTempting(ItemStack stack) {
                Item item = stack.getItem();
                return item == Items.FISH || item == AMItemRegistry.LOBSTER_TAIL || item == AMItemRegistry.COOKED_LOBSTER_TAIL;
            }

            @Override
            public boolean shouldExecute() {
                return !EntitySeagull.this.aiItemFlag && super.shouldExecute();
            }
        });
        this.tasks.addTask(5, new AIWanderIdle());
        this.tasks.addTask(6, new EntityAIWatchClosest(this, EntityPlayer.class, 6.0F));
        this.tasks.addTask(7, new EntityAIWatchClosest(this, EntityCreature.class, 6.0F));
        this.tasks.addTask(8, new EntityAILookIdle(this));
        this.tasks.addTask(9, new AIScatter());
        this.targetTasks.addTask(1, new AITargetItems(false, false, 15, 16));
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        return stack.getItem() == Items.FISH;
    }

    @Override
    public boolean getCanSpawnHere() {
        BlockPos pos = this.getPosition();
        return this.world.getLightFromNeighbors(pos) > 8 && !this.world.getBlockState(pos.down()).getMaterial().isLiquid()
                && AMEntityRegistry.rollSpawn(AMConfig.seagullSpawnRolls, this.getRNG(), AMEntityRegistry.AMSpawnReason.OTHER) && super.getCanSpawnHere();
    }

    @Override
    public void fall(float distance, float damageMultiplier) {
    }

    @Override
    protected void updateFallState(double y, boolean onGroundIn, IBlockState state, BlockPos pos) {
        this.fallDistance = 0.0F;
    }

    private void switchNavigator(boolean onLand) {
        if (onLand) {
            this.moveHelper = new EntityMoveHelper(this);
            this.navigator = new PathNavigateGround(this, world);
            this.isLandNavigator = true;
        } else {
            this.moveHelper = new SeagullMoveHelper(this);
            this.navigator = new DirectPathNavigator(this, world);
            this.isLandNavigator = false;
        }
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(FLYING, false);
        this.dataManager.register(SITTING, false);
        this.dataManager.register(ATTACK_TICK, 0);
        this.dataManager.register(TREASURE_POS, Optional.absent());
        this.dataManager.register(FLIGHT_LOOK_YAW, 0F);
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

    public boolean isSitting() {
        return this.dataManager.get(SITTING);
    }

    public void setSitting(boolean sitting) {
        this.dataManager.set(SITTING, sitting);
    }

    public float getFlightLookYaw() {
        return dataManager.get(FLIGHT_LOOK_YAW);
    }

    public void setFlightLookYaw(float yaw) {
        dataManager.set(FLIGHT_LOOK_YAW, yaw);
    }

    public BlockPos getTreasurePos() {
        return this.dataManager.get(TREASURE_POS).orNull();
    }

    public void setTreasurePos(BlockPos pos) {
        this.dataManager.set(TREASURE_POS, Optional.fromNullable(pos));
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (this.isEntityInvulnerable(source)) {
            return false;
        } else {
            Entity entity = source.getTrueSource();
            boolean prev = super.attackEntityFrom(source, amount);
            if (prev) {
                this.setSitting(false);
                if (!this.getHeldItemMainhand().isEmpty()) {
                    this.entityDropItem(this.getHeldItemMainhand(), 0.0F);
                    this.setHeldItem(EnumHand.MAIN_HAND, ItemStack.EMPTY);
                    stealCooldown = 1500 + rand.nextInt(1500);
                }
                this.feederUUID = null;
                this.treasureSitTime = 0;
            }
            return prev;
        }
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        this.prevFlyProgress = flyProgress;
        this.prevFlapAmount = flapAmount;
        this.prevAttackProgress = attackProgress;
        this.prevSitProgress = sitProgress;
        float yMot = (float) -((float) this.motionY * (double) (180F / (float) Math.PI));
        float absYaw = Math.abs(this.rotationYaw - this.prevRotationYaw);
        if (isFlying() && flyProgress < 5F) {
            flyProgress++;
        }
        if (!isFlying() && flyProgress > 0F) {
            flyProgress--;
        }
        if (isSitting() && sitProgress < 5F) {
            sitProgress++;
        }
        if (!isSitting() && sitProgress > 0F) {
            sitProgress--;
        }
        if (absYaw > 8) {
            flapAmount = Math.min(1F, flapAmount + 0.1F);
        } else if (yMot < 0.0F) {
            flapAmount = Math.min(-yMot * 0.2F, 1F);
        } else {
            if (flapAmount > 0.0F) {
                flapAmount -= Math.min(flapAmount, 0.05F);
            } else {
                flapAmount = 0;
            }
        }
        if (this.dataManager.get(ATTACK_TICK) > 0) {
            this.dataManager.set(ATTACK_TICK, this.dataManager.get(ATTACK_TICK) - 1);
            if (attackProgress < 5F) {
                attackProgress++;
            }
        } else {
            if (attackProgress > 0F) {
                attackProgress--;
            }
        }
        if (!world.isRemote) {
            if (isFlying()) {
                float lookYawDist = Math.abs(this.getFlightLookYaw() - targetFlightLookYaw);
                if (flightLookCooldown > 0) {
                    flightLookCooldown--;
                }
                if (flightLookCooldown == 0 && this.rand.nextInt(4) == 0 && lookYawDist < 0.5F) {
                    targetFlightLookYaw = MathHelper.clamp(rand.nextFloat() * 120F - 60, -60, 60);
                    flightLookCooldown = 3 + rand.nextInt(15);
                }
                if (this.getFlightLookYaw() < this.targetFlightLookYaw && lookYawDist > 0.5F) {
                    this.setFlightLookYaw(this.getFlightLookYaw() + Math.min(lookYawDist, 4F));
                }
                if (this.getFlightLookYaw() > this.targetFlightLookYaw && lookYawDist > 0.5F) {
                    this.setFlightLookYaw(this.getFlightLookYaw() - Math.min(lookYawDist, 4F));
                }
                if (this.onGround && !this.isInWater() && this.timeFlying > 30) {
                    this.setFlying(false);
                }
                timeFlying++;
                this.setNoGravity(true);
                if (this.isRiding() || this.isInLove()) {
                    this.setFlying(false);
                }
            } else {
                fallFlag = false;
                timeFlying = 0;
                this.setNoGravity(false);
            }
            if (isFlying() && this.isLandNavigator) {
                switchNavigator(false);
            }
            if (!isFlying() && !this.isLandNavigator) {
                switchNavigator(true);
            }
        }
        if (!this.getHeldItemMainhand().isEmpty()) {
            heldItemTime++;
            if (heldItemTime > 200 && canTargetItem(this.getHeldItemMainhand())) {
                heldItemTime = 0;
                this.heal(4);
                this.playSound(SoundEvents.ENTITY_GENERIC_EAT, this.getSoundVolume(), this.getSoundPitch());
                if (this.getHeldItemMainhand().getItem().hasContainerItem()) {
                    this.entityDropItem(new ItemStack(this.getHeldItemMainhand().getItem().getContainerItem()), 0.0F);
                }
                eatItemEffect(this.getHeldItemMainhand());
                this.getHeldItemMainhand().shrink(1);
            }
        } else {
            heldItemTime = 0;
        }
        if (stealCooldown > 0) {
            stealCooldown--;
        }
        if(treasureSitTime > 0){
            treasureSitTime--;
        }
        if (this.isSitting() && this.isInWater()) {
            this.motionY += 0.02D;
        }
    }

    public void eatItem(){
        heldItemTime = 200;
    }
    @Override
    public boolean canTargetItem(ItemStack stack) {
        return stack.getItem() instanceof ItemFood && !this.isSitting();
    }

    private void eatItemEffect(ItemStack heldItemMainhand) {
        for (int i = 0; i < 2 + rand.nextInt(2); i++) {
            double d2 = this.rand.nextGaussian() * 0.02D;
            double d0 = this.rand.nextGaussian() * 0.02D;
            double d1 = this.rand.nextGaussian() * 0.02D;
            float radius = this.width * 0.65F;
            float angle = (0.01745329251F * this.renderYawOffset);
            double extraX = radius * MathHelper.sin((float) (Math.PI + angle));
            double extraZ = radius * MathHelper.cos(angle);
            if (heldItemMainhand.getItem() instanceof ItemBlock) {
                this.world.spawnParticle(EnumParticleTypes.BLOCK_CRACK, this.posX + extraX, this.posY + this.height * 0.6F, this.posZ + extraZ, d0, d1, d2, Block.getStateId(((ItemBlock) heldItemMainhand.getItem()).getBlock().getDefaultState()));
            } else {
                this.world.spawnParticle(EnumParticleTypes.ITEM_CRACK, this.posX + extraX, this.posY + this.height * 0.6F, this.posZ + extraZ, d0, d1, d2, Item.getIdFromItem(heldItemMainhand.getItem()), heldItemMainhand.getMetadata());
            }
        }
    }

    public void setDataFromTreasureMap(EntityPlayer player) {
        boolean flag = false;
        for (int slot = 0; slot < player.inventory.mainInventory.size(); slot++) {
            ItemStack map = player.inventory.mainInventory.get(slot);
            if (map.getItem() == Items.FILLED_MAP || map.getItem() == Items.MAP) {
                if (map.hasTagCompound() && map.getTagCompound().hasKey("Decorations", 9)) {
                    NBTTagList listnbt = map.getTagCompound().getTagList("Decorations", 10);
                    for (int i = 0; i < listnbt.tagCount(); i++) {
                        NBTTagCompound nbt = listnbt.getCompoundTagAt(i);
                        byte type = nbt.getByte("type");
                        if (type == MAP_ICON_RED_X || type == MAP_ICON_TARGET_X) {
                            int x = nbt.getInteger("x");
                            int z = nbt.getInteger("z");
                            if (this.getDistanceSq(x, this.posY, z) <= 400) {
                                flag = true;
                                this.setTreasurePos(new BlockPos(x, 0, z));
                            }
                        }
                    }
                }
            }
        }
        ItemStack off = player.getHeldItemOffhand();
        if (!off.isEmpty() && (off.getItem() == Items.FILLED_MAP || off.getItem() == Items.MAP)) {
            ItemStack map = off;
            if (map.hasTagCompound() && map.getTagCompound().hasKey("Decorations", 9)) {
                NBTTagList listnbt = map.getTagCompound().getTagList("Decorations", 10);
                for (int i = 0; i < listnbt.tagCount(); i++) {
                    NBTTagCompound nbt = listnbt.getCompoundTagAt(i);
                    byte type = nbt.getByte("type");
                    if (type == MAP_ICON_RED_X || type == MAP_ICON_TARGET_X) {
                        int x = nbt.getInteger("x");
                        int z = nbt.getInteger("z");
                        if (this.getDistanceSq(x, this.posY, z) <= 400) {
                            flag = true;
                            this.setTreasurePos(new BlockPos(x, 0, z));
                        }
                    }
                }
            }
        }
        if (flag) {
            this.feederUUID = player.getUniqueID();
            this.treasureSitTime = 300;
            this.stealCooldown = 1500 + rand.nextInt(1500);
        }
    }

    @Override
    public void travel(float strafe, float vertical, float forward) {
        if (this.isSitting()) {
            if (this.getNavigator().getPath() != null) {
                this.getNavigator().clearPath();
            }
            strafe = 0.0F;
            vertical = 0.0F;
            forward = 0.0F;
        }
        super.travel(strafe, vertical, forward);
    }

    public boolean isWingull() {
        String s = TextFormatting.getTextWithoutFormattingCodes(this.getName());
        return s != null && s.toLowerCase().equals("wingull");
    }

    @Override
    public void onGetItem(EntityItem e) {
        ItemStack duplicate = e.getItem().copy();
        duplicate.setCount(1);
        if (!this.getHeldItem(EnumHand.MAIN_HAND).isEmpty() && !this.world.isRemote) {
            this.entityDropItem(this.getHeldItem(EnumHand.MAIN_HAND), 0.0F);
        }
        stealCooldown += 600 + rand.nextInt(1200);
        if (e.getThrower() != null && (e.getItem().getItem() == AMItemRegistry.LOBSTER_TAIL || e.getItem().getItem() == AMItemRegistry.COOKED_LOBSTER_TAIL)) {
            EntityPlayer player = world.getPlayerEntityByName(e.getThrower());
            if (player != null) {
                setDataFromTreasureMap(player);
                feederUUID = player.getUniqueID();
            }
        }
        this.setFlying(true);
        this.setHeldItem(EnumHand.MAIN_HAND, duplicate);
    }

    public Vec3d getBlockInViewAway(Vec3d fleePos, float radiusAdd) {
        float radius = 5 + radiusAdd + this.getRNG().nextInt(5);
        float neg = this.getRNG().nextBoolean() ? 1 : -1;
        float renderYawOffset = this.renderYawOffset;
        float angle = (0.01745329251F * renderYawOffset) + 3.15F + (this.getRNG().nextFloat() * neg);
        double extraX = radius * MathHelper.sin((float) (Math.PI + angle));
        double extraZ = radius * MathHelper.cos(angle);
        BlockPos radialPos = new BlockPos(fleePos.x + extraX, 0, fleePos.z + extraZ);
        BlockPos ground = getSeagullGround(radialPos);
        int distFromGround = (int) this.posY - ground.getY();
        int flightHeight = 8 + this.getRNG().nextInt(4);
        BlockPos newPos = ground.up(distFromGround > 3 ? flightHeight : this.getRNG().nextInt(4) + 8);
        Vec3d center = new Vec3d(newPos.getX() + 0.5D, newPos.getY() + 0.5D, newPos.getZ() + 0.5D);
        if (!this.isTargetBlocked(center) && this.getDistanceSq(center.x, center.y, center.z) > 1) {
            return center;
        }
        return null;
    }

    public BlockPos getSeagullGround(BlockPos in) {
        BlockPos position = new BlockPos(in.getX(), (int) this.posY, in.getZ());
        while (position.getY() < 256 && world.getBlockState(position).getMaterial().isLiquid()) {
            position = position.up();
        }
        while (position.getY() > 2 && world.isAirBlock(position)) {
            position = position.down();
        }
        return position;
    }

    public Vec3d getBlockGrounding(Vec3d fleePos) {
        float radius = 10 + this.getRNG().nextInt(15);
        float neg = this.getRNG().nextBoolean() ? 1 : -1;
        float renderYawOffset = this.renderYawOffset;
        float angle = (0.01745329251F * renderYawOffset) + 3.15F + (this.getRNG().nextFloat() * neg);
        double extraX = radius * MathHelper.sin((float) (Math.PI + angle));
        double extraZ = radius * MathHelper.cos(angle);
        BlockPos radialPos = new BlockPos(fleePos.x + extraX, (int) posY, fleePos.z + extraZ);
        BlockPos ground = this.getSeagullGround(radialPos);
        if (ground.getY() == 0) {
            return this.getPositionVector();
        } else {
            ground = this.getPosition();
            while (ground.getY() > 2 && world.isAirBlock(ground)) {
                ground = ground.down();
            }
        }
        Vec3d center = new Vec3d(ground.getX() + 0.5D, ground.getY() + 0.5D, ground.getZ() + 0.5D);
        if (!this.isTargetBlocked(center)) {
            return center;
        }
        return null;
    }

    public boolean isTargetBlocked(Vec3d target) {
        Vec3d start = new Vec3d(this.posX, this.getPositionEyes(1.0F).y, this.posZ);
        RayTraceResult result = this.world.rayTraceBlocks(start, target, false, true, false);
        return result != null && result.typeOfHit != RayTraceResult.Type.MISS;
    }

    private Vec3d getOrbitVec(Vec3d vector3d, float gatheringCircleDist) {
        float angle = (0.01745329251F * (float) this.orbitDist * (orbitClockwise ? -ticksExisted : ticksExisted));
        double extraX = gatheringCircleDist * MathHelper.sin((angle));
        double extraZ = gatheringCircleDist * MathHelper.cos(angle);
        if (this.orbitPos != null) {
            Vec3d pos = new Vec3d(orbitPos.getX() + extraX, orbitPos.getY() + rand.nextInt(2), orbitPos.getZ() + extraZ);
            if (this.world.isAirBlock(new BlockPos(pos))) {
                return pos;
            }
        }
        return null;
    }

    private boolean isOverWaterOrVoid() {
        BlockPos position = this.getPosition();
        while (position.getY() > 0 && world.isAirBlock(position)) {
            position = position.down();
        }
        return world.getBlockState(position).getMaterial().isLiquid() || position.getY() <= 0;
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        if (super.processInteract(player, hand)) {
            return true;
        }
        if (!this.getHeldItemMainhand().isEmpty()) {
            this.entityDropItem(this.getHeldItemMainhand().copy(), 0.0F);
            this.setHeldItem(EnumHand.MAIN_HAND, ItemStack.EMPTY);
            stealCooldown = 1500 + rand.nextInt(1500);
            return true;
        }
        return false;
    }

    @Nullable
    @Override
    public EntitySeagull createChild(EntityAgeable ageable) {
        return new EntitySeagull(this.world);
    }

    public void peck() {
        this.dataManager.set(ATTACK_TICK, 7);
    }

    private class AIScatter extends EntityAIBase {
        protected final EntitySeagull.AIScatter.Sorter theNearestAttackableTargetSorter;
        protected final Predicate<? super Entity> targetEntitySelector;
        protected int executionChance = 8;
        protected boolean mustUpdate;
        private Entity targetEntity;
        private Vec3d flightTarget = null;
        private int cooldown = 0;

        AIScatter() {
            this.setMutexBits(1);
            this.theNearestAttackableTargetSorter = new EntitySeagull.AIScatter.Sorter(EntitySeagull.this);
            this.targetEntitySelector = new Predicate<Entity>() {
                @Override
                public boolean apply(@Nullable Entity e) {
                    return e != null && e.isEntityAlive() && (AMTagRegistry.entityMatchesEntityTypeTag(AMTagRegistry.SCATTERS_CROWS, e)
                            || e instanceof EntityPlayer && !((EntityPlayer) e).capabilities.isCreativeMode);
                }
            };
        }

        @Override
        public boolean shouldExecute() {
            if (EntitySeagull.this.isRiding() || EntitySeagull.this.isSitting() || EntitySeagull.this.aiItemFlag || EntitySeagull.this.isBeingRidden()) {
                return false;
            }
            if (!this.mustUpdate) {
                long worldTime = EntitySeagull.this.world.getTotalWorldTime() % 10;
                if (EntitySeagull.this.getIdleTime() >= 100 && worldTime != 0) {
                    return false;
                }
                if (EntitySeagull.this.getRNG().nextInt(this.executionChance) != 0 && worldTime != 0) {
                    return false;
                }
            }
            List<Entity> list = EntitySeagull.this.world.getEntitiesWithinAABB(Entity.class, this.getTargetableArea(this.getTargetDistance()), this.targetEntitySelector);
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
                EntitySeagull.this.setFlying(true);
                EntitySeagull.this.getMoveHelper().setMoveTo(flightTarget.x, flightTarget.y, flightTarget.z, 1F);
                if (cooldown == 0 && EntitySeagull.this.isTargetBlocked(flightTarget)) {
                    cooldown = 30;
                    flightTarget = null;
                }
            }

            if (targetEntity != null) {
                if (EntitySeagull.this.onGround || flightTarget == null || EntitySeagull.this.getDistanceSq(flightTarget.x, flightTarget.y, flightTarget.z) < 3) {
                    Vec3d vec = EntitySeagull.this.getBlockInViewAway(targetEntity.getPositionVector(), 0);
                    if (vec != null && vec.y > EntitySeagull.this.posY) {
                        flightTarget = vec;
                    }
                }
                if (EntitySeagull.this.getDistance(targetEntity) > 20.0F) {
                    this.resetTask();
                }
            }
        }

        protected double getTargetDistance() {
            return 4D;
        }

        protected AxisAlignedBB getTargetableArea(double targetDistance) {
            Vec3d renderCenter = new Vec3d(EntitySeagull.this.posX, EntitySeagull.this.posY + 0.5, EntitySeagull.this.posZ);
            AxisAlignedBB aabb = new AxisAlignedBB(-2, -2, -2, 2, 2, 2);
            return aabb.offset(renderCenter.x, renderCenter.y, renderCenter.z);
        }


        public class Sorter implements Comparator<Entity> {
            private final Entity theEntity;

            public Sorter(Entity theEntityIn) {
                this.theEntity = theEntityIn;
            }

            public int compare(Entity p_compare_1_, Entity p_compare_2_) {
                double d0 = this.theEntity.getDistanceSq(p_compare_1_);
                double d1 = this.theEntity.getDistanceSq(p_compare_2_);
                return d0 < d1 ? -1 : (d0 > d1 ? 1 : 0);
            }
        }
    }

    private class AIWanderIdle extends EntityAIBase {
        protected final EntitySeagull eagle;
        protected double x;
        protected double y;
        protected double z;
        private boolean flightTarget = false;
        private int orbitResetCooldown = 0;
        private int maxOrbitTime = 360;
        private int orbitTime = 0;

        public AIWanderIdle() {
            this.setMutexBits(1);
            this.eagle = EntitySeagull.this;
        }

        @Override
        public boolean shouldExecute() {
            if (orbitResetCooldown < 0) {
                orbitResetCooldown++;
            }
            if ((eagle.getAttackTarget() != null && eagle.getAttackTarget().isEntityAlive() && !this.eagle.isBeingRidden()) || eagle.isSitting() || this.eagle.isRiding()) {
                return false;
            } else {
                if (this.eagle.getRNG().nextInt(20) != 0 && !eagle.isFlying() || eagle.aiItemFlag) {
                    return false;
                }
                if (this.eagle.isChild()) {
                    this.flightTarget = false;
                } else if (this.eagle.isInWater()) {
                    this.flightTarget = true;
                } else if (this.eagle.onGround) {
                    this.flightTarget = eagle.getRNG().nextInt(10) == 0;
                } else {
                    if (orbitResetCooldown == 0 && eagle.getRNG().nextInt(6) == 0) {
                        orbitResetCooldown = 100 + eagle.getRNG().nextInt(300);
                        eagle.orbitPos = eagle.getPosition();
                        eagle.orbitDist = 4 + eagle.getRNG().nextInt(5);
                        eagle.orbitClockwise = eagle.getRNG().nextBoolean();
                        orbitTime = 0;
                        maxOrbitTime = (int) (180 + 360 * eagle.getRNG().nextFloat());
                    }
                    this.flightTarget = eagle.getRNG().nextInt(5) != 0 && eagle.timeFlying < 400;
                }
                Vec3d lvt_1_1_ = this.getPosition();
                if (lvt_1_1_ == null) {
                    return false;
                } else {
                    this.x = lvt_1_1_.x;
                    this.y = lvt_1_1_.y;
                    this.z = lvt_1_1_.z;
                    return true;
                }
            }
        }

        @Override
        public void updateTask() {
            if (orbitResetCooldown > 0) {
                orbitResetCooldown--;
            }
            if (orbitResetCooldown < 0) {
                orbitResetCooldown++;
            }
            if (orbitResetCooldown > 0 && eagle.orbitPos != null) {
                if (orbitTime < maxOrbitTime && !eagle.isInWater()) {
                    orbitTime++;
                } else {
                    orbitTime = 0;
                    eagle.orbitPos = null;
                    orbitResetCooldown = -400 - eagle.getRNG().nextInt(400);
                }
            }
            if (eagle.collidedHorizontally && !eagle.onGround) {
                resetTask();
            }
            if (flightTarget) {
                eagle.getMoveHelper().setMoveTo(x, y, z, 1F);
            } else {
                if (!eagle.isFlying() || eagle.onGround) {
                    this.eagle.getNavigator().tryMoveToXYZ(this.x, this.y, this.z, 1F);
                }
            }
            if (!flightTarget && isFlying()) {
                eagle.fallFlag = true;
                if (eagle.onGround) {
                    eagle.setFlying(false);
                    orbitTime = 0;
                    eagle.orbitPos = null;
                    orbitResetCooldown = -400 - eagle.getRNG().nextInt(400);
                }
            }
            if (isFlying() && (!eagle.world.isAirBlock(eagle.getPosition().down()) || eagle.onGround) && !eagle.isInWater() && eagle.timeFlying > 30) {
                eagle.setFlying(false);
                orbitTime = 0;
                eagle.orbitPos = null;
                orbitResetCooldown = -400 - eagle.getRNG().nextInt(400);
            }
        }

        @Nullable
        protected Vec3d getPosition() {
            Vec3d vector3d = eagle.getPositionVector();
            if (orbitResetCooldown > 0 && eagle.orbitPos != null) {
                return eagle.getOrbitVec(vector3d, 4 + eagle.getRNG().nextInt(4));
            }
            if (eagle.isBeingRidden() || eagle.isOverWaterOrVoid()) {
                flightTarget = true;
            }
            if (flightTarget) {
                if (eagle.timeFlying < 340 || eagle.isBeingRidden() || eagle.isOverWaterOrVoid()) {
                    return eagle.getBlockInViewAway(vector3d, 0);
                } else {
                    return eagle.getBlockGrounding(vector3d);
                }
            } else {
                return RandomPositionGenerator.findRandomTarget(this.eagle, 10, 7);
            }
        }

        @Override
        public boolean shouldContinueExecuting() {
            if (flightTarget) {
                return eagle.isFlying() && eagle.getDistanceSq(x, y, z) > 4F;
            } else {
                return (!this.eagle.getNavigator().noPath()) && !this.eagle.isBeingRidden();
            }
        }

        @Override
        public void startExecuting() {
            if (flightTarget) {
                eagle.setFlying(true);
                eagle.getMoveHelper().setMoveTo(x, y, z, 1F);
            } else {
                this.eagle.getNavigator().tryMoveToXYZ(this.x, this.y, this.z, 1F);
            }
        }

        @Override
        public void resetTask() {
            this.eagle.getNavigator().clearPath();
            super.resetTask();
        }
    }

    class SeagullMoveHelper extends EntityMoveHelper {
        private final EntitySeagull parentEntity;

        public SeagullMoveHelper(EntitySeagull bird) {
            super(bird);
            this.parentEntity = bird;
        }

        @Override
        public void onUpdateMoveHelper() {
            if (this.action == Action.MOVE_TO) {
                double tx = this.getX() - parentEntity.posX;
                double ty = this.getY() - parentEntity.posY;
                double tz = this.getZ() - parentEntity.posZ;
                double d5 = MathHelper.sqrt(tx * tx + ty * ty + tz * tz);
                if (d5 < 0.3) {
                    this.action = Action.WAIT;
                    parentEntity.motionX *= 0.5D;
                    parentEntity.motionY *= 0.5D;
                    parentEntity.motionZ *= 0.5D;
                } else {
                    double d1 = this.getY() - parentEntity.posY;
                    parentEntity.motionX += tx / d5 * this.speed * 0.03D;
                    parentEntity.motionY += ty / d5 * this.speed * 0.03D;
                    parentEntity.motionZ += tz / d5 * this.speed * 0.03D;
                    parentEntity.rotationYaw = -((float) MathHelper.atan2(parentEntity.motionX, parentEntity.motionZ)) * (180F / (float) Math.PI);
                    parentEntity.renderYawOffset = parentEntity.rotationYaw;
                }
            }
        }
    }

    private class AITargetItems extends CreatureAITargetItems {

        public AITargetItems(boolean checkSight, boolean onlyNearby, int tickThreshold, int radius) {
            super(EntitySeagull.this, checkSight, onlyNearby, tickThreshold, radius);
            this.executionChance = 1;
        }

        @Override
        public void resetTask() {
            super.resetTask();
            EntitySeagull.this.aiItemFlag = false;
        }

        @Override
        public boolean shouldExecute() {
            return super.shouldExecute() && !EntitySeagull.this.isSitting() && (EntitySeagull.this.getAttackTarget() == null || !EntitySeagull.this.getAttackTarget().isEntityAlive());
        }

        @Override
        public boolean shouldContinueExecuting() {
            return super.shouldContinueExecuting() && !EntitySeagull.this.isSitting() && (EntitySeagull.this.getAttackTarget() == null || !EntitySeagull.this.getAttackTarget().isEntityAlive());
        }

        @Override
        protected void moveTo() {
            EntitySeagull seagull = EntitySeagull.this;
            if (this.targetEntity != null) {
                seagull.aiItemFlag = true;
                if (seagull.getDistance(this.targetEntity) < 2) {
                    seagull.getMoveHelper().setMoveTo(this.targetEntity.posX, this.targetEntity.posY, this.targetEntity.posZ, 1.5F);
                    seagull.peck();
                }
                if (seagull.getDistance(this.targetEntity) > 8 || seagull.isFlying()) {
                    seagull.setFlying(true);
                    float f = (float) (seagull.posX - this.targetEntity.posX);
                    float f1 = 1.8F;
                    float f2 = (float) (seagull.posZ - this.targetEntity.posZ);
                    float xzDist = MathHelper.sqrt(f * f + f2 * f2);

                    if (!seagull.canEntityBeSeen(this.targetEntity)) {
                        seagull.getMoveHelper().setMoveTo(this.targetEntity.posX, 1 + seagull.posY, this.targetEntity.posZ, 1.5F);
                    } else {
                        if (xzDist < 5) {
                            f1 = 0;
                        }
                        seagull.getMoveHelper().setMoveTo(this.targetEntity.posX, f1 + this.targetEntity.posY, this.targetEntity.posZ, 1.5F);
                    }
                } else {
                    seagull.getNavigator().tryMoveToXYZ(this.targetEntity.posX, this.targetEntity.posY, this.targetEntity.posZ, 1.5F);
                }
            }
        }

        @Override
        public void updateTask() {
            if (this.targetEntity == null || !this.targetEntity.isEntityAlive()) {
                this.resetTask();
                EntitySeagull.this.getNavigator().clearPath();
            } else {
                moveTo();
            }
            if (this.targetEntity != null && EntitySeagull.this.canEntityBeSeen(this.targetEntity) && EntitySeagull.this.width > 2D && EntitySeagull.this.onGround) {
                EntitySeagull.this.getMoveHelper().setMoveTo(this.targetEntity.posX, this.targetEntity.posY, this.targetEntity.posZ, 1.0D);
            }
            if (this.targetEntity != null && this.targetEntity.isEntityAlive()
                    && EntitySeagull.this.getDistanceSq(this.targetEntity) < EntitySeagull.this.getMaxDistToItem()
                    && EntitySeagull.this.getHeldItemMainhand().isEmpty()) {
                EntitySeagull.this.onGetItem(this.targetEntity);
                this.targetEntity.getItem().shrink(1);
                resetTask();
            }
        }
    }
}
