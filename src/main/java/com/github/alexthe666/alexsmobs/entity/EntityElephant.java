package com.github.alexthe666.alexsmobs.entity;

import com.github.alexthe666.alexsmobs.config.AMConfig;
import com.github.alexthe666.alexsmobs.entity.ai.*;
import com.github.alexthe666.alexsmobs.item.AMItemRegistry;
import com.github.alexthe666.alexsmobs.misc.AMSoundRegistry;
import com.github.alexthe666.alexsmobs.misc.AMTagRegistry;
import com.github.alexthe666.citadel.animation.Animation;
import com.github.alexthe666.citadel.animation.IAnimatedEntity;
import com.google.common.collect.Maps;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraft.init.Blocks;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class EntityElephant extends EntityTameable implements ITargetsDroppedItems, IAnimatedEntity {

    public static final Animation ANIMATION_TRUMPET_0 = Animation.create(20);
    public static final Animation ANIMATION_TRUMPET_1 = Animation.create(30);
    public static final Animation ANIMATION_CHARGE_PREPARE = Animation.create(25);
    public static final Animation ANIMATION_STOMP = Animation.create(20);
    public static final Animation ANIMATION_FLING = Animation.create(25);
    public static final Animation ANIMATION_EAT = Animation.create(30);
    public static final Animation ANIMATION_BREAKLEAVES = Animation.create(20);
    private static final float TUSKED_WIDTH = 2.3F;
    private static final float TUSKED_HEIGHT = 2.75F;
    private static final float NORMAL_WIDTH = 2.1F;
    private static final float NORMAL_HEIGHT = 2.5F;
    private static final DataParameter<Boolean> TUSKED = EntityDataManager.createKey(EntityElephant.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> SITTING = EntityDataManager.createKey(EntityElephant.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> STANDING = EntityDataManager.createKey(EntityElephant.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> CHESTED = EntityDataManager.createKey(EntityElephant.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> CARPET_COLOR = EntityDataManager.createKey(EntityElephant.class, DataSerializers.VARINT);
    private static final DataParameter<Boolean> TRADER = EntityDataManager.createKey(EntityElephant.class, DataSerializers.BOOLEAN);
    public static final Map<EnumDyeColor, Item> DYE_COLOR_ITEM_MAP = Maps.newHashMap();

    static {
        for (EnumDyeColor color : EnumDyeColor.values()) {
            DYE_COLOR_ITEM_MAP.put(color, Item.getItemFromBlock(Blocks.CARPET));
        }
    }
    private static final ResourceLocation TRADER_LOOT = new ResourceLocation("alexsmobs", "gameplay/trader_elephant_chest");
    public boolean forcedSit = false;
    public float prevSitProgress;
    public float sitProgress;
    public float prevStandProgress;
    public float standProgress;
    public int maxStandTime = 75;
    public boolean aiItemFlag = false;
    public IInventory elephantInventory;
    private int animationTick;
    private Animation currentAnimation;
    private boolean hasTuskedAttributes = false;
    private int standingTime = 0;
    @Nullable
    private EntityElephant caravanHead;
    @Nullable
    private EntityElephant caravanTail;
    private boolean hasChestVarChanged = false;
    private boolean hasChargedSpeed = false;
    private boolean charging;
    private int chargeCooldown = 0;
    private int chargingTicks = 0;
    @Nullable
    private UUID blossomThrowerUUID = null;
    private int despawnDelay = 47999;

    public EntityElephant(World worldIn) {
        super(worldIn);
        this.setSize(NORMAL_WIDTH, NORMAL_HEIGHT);
        initElephantInventory();
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        AMEntityRegistry.registerAttributeIfAbsent(this, SharedMonsterAttributes.ATTACK_DAMAGE);
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(65.0D);
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(32.0D);
        this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(0.9D);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(10.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.35D);
    }

    @Nullable
    public static EnumDyeColor getCarpetColor(ItemStack stack) {
        if (stack.getItem() == Item.getItemFromBlock(Blocks.CARPET)) {
            return EnumDyeColor.byMetadata(stack.getMetadata());
        }
        return null;
    }

    private static boolean isCarpetItem(Item item) {
        return DYE_COLOR_ITEM_MAP.containsValue(item);
    }

    public static boolean isElephantCarpet(Item item) {
        return isCarpetItem(item);
    }

    private static boolean isWoodenChestItem(Item item) {
        return AMTagRegistry.itemInTag(AMTagRegistry.FORGE_WOODEN_CHESTS, item) || item == Item.getItemFromBlock(Blocks.CHEST) || item == Item.getItemFromBlock(Blocks.TRAPPED_CHEST);
    }

    protected SoundEvent getAmbientSound() {
        return AMSoundRegistry.ELEPHANT_IDLE;
    }

    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return AMSoundRegistry.ELEPHANT_HURT;
    }

    protected SoundEvent getDeathSound() {
        return AMSoundRegistry.ELEPHANT_DIE;
    }

    @Override
    public boolean getCanSpawnHere() {
        return AMEntityRegistry.rollSpawn(AMConfig.elephantSpawnRolls, this.getRNG(), AMEntityRegistry.AMSpawnReason.OTHER) && super.getCanSpawnHere();
    }

    private void initElephantInventory() {
        IInventory animalchest = this.elephantInventory;
        this.elephantInventory = new InventoryBasic("ElephantChest", false, 54);
        if (animalchest != null) {
            int i = Math.min(animalchest.getSizeInventory(), this.elephantInventory.getSizeInventory());
            for (int j = 0; j < i; ++j) {
                ItemStack itemstack = animalchest.getStackInSlot(j);
                if (!itemstack.isEmpty()) {
                    this.elephantInventory.setInventorySlotContents(j, itemstack.copy());
                }
            }
        }
    }

    @Override
    protected PathNavigate createNavigator(World worldIn) {
        return new GroundPathNavigatorWide(this, worldIn);
    }

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(1, new TameableAIRide(this, 1D));
        this.tasks.addTask(1, new EntityAISit(this));
        this.tasks.addTask(2, new EntityAIAttackMelee(this, 1D, true));
        this.tasks.addTask(2, new EntityElephant.PanicGoal());
        this.tasks.addTask(2, new ElephantAIVillagerRide(this, 1D));
        this.tasks.addTask(3, new EntityAIMate(this, 1.0D));
        this.tasks.addTask(4, new EntityAITempt(this, 1.0D, AMItemRegistry.ACACIA_BLOSSOM, false));
        this.tasks.addTask(5, new ElephantAIForageLeaves(this));
        this.tasks.addTask(6, new EntityAIFollowParent(this, 1D));
        this.tasks.addTask(7, new ElephantAIFollowCaravan(this, 0.5D));
        this.tasks.addTask(9, new EntityElephant.AIWalkIdle(this, 0.5D));
        this.targetTasks.addTask(1, new EntityElephant.HurtByTargetGoal());
        this.targetTasks.addTask(2, new EntityAIOwnerHurtByTarget(this));
        this.targetTasks.addTask(3, new EntityAIOwnerHurtTarget(this));
        this.targetTasks.addTask(4, new CreatureAITargetItems(this, false));
    }

    public boolean isBreedingItem(ItemStack stack) {
        Item item = stack.getItem();
        return isTamed() && item == AMItemRegistry.ACACIA_BLOSSOM;
    }

    @Override
    protected void playStepSound(BlockPos pos, Block blockIn) {
        if (!isChild()) {
            this.playSound(AMSoundRegistry.ELEPHANT_WALK, 0.2F, 1.0F);
        } else {
            super.playStepSound(pos, blockIn);
        }
    }

    @Nullable
    public Entity getControllingPassenger() {
        for (Entity passenger : this.getPassengers()) {
            if (passenger instanceof EntityPlayer) {
                return passenger;
            }
        }
        return null;
    }

    @Nullable
    public EntityVillager getControllingVillager() {
        for (Entity passenger : this.getPassengers()) {
            if (passenger instanceof EntityVillager) {
                return (EntityVillager) passenger;
            }
        }
        return null;
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(TUSKED, Boolean.valueOf(false));
        this.dataManager.register(SITTING, Boolean.valueOf(false));
        this.dataManager.register(STANDING, Boolean.valueOf(false));
        this.dataManager.register(CHESTED, Boolean.valueOf(false));
        this.dataManager.register(TRADER, Boolean.valueOf(false));
        this.dataManager.register(CARPET_COLOR, -1);
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        if (this.isChild() && this.getEyeHeight() > this.height) {
            this.setSize(this.width, this.getEyeHeight());
        }
        prevSitProgress = sitProgress;
        prevStandProgress = standProgress;
        if (isSitting() && this.sitProgress < 5F) {
            this.sitProgress++;
        }
        if (!isSitting() && this.sitProgress > 0F) {
            this.sitProgress--;
        }
        if (this.isStanding() && standProgress < 5) {
            standProgress += 0.5F;
        }
        if (!this.isStanding() && standProgress > 0) {
            standProgress -= 0.5F;
        }
        if (isStanding() && ++standingTime > maxStandTime) {
            this.setStanding(false);
            standingTime = 0;
            maxStandTime = 75 + rand.nextInt(50);
        }
        if (isSitting() && isStanding()) {
            this.setStanding(false);
        }
        if (hasChestVarChanged && elephantInventory != null && !this.isChested()) {
            for (int i = 3; i < 18; i++) {
                if (!elephantInventory.getStackInSlot(i).isEmpty()) {
                    if (!world.isRemote) {
                        this.entityDropItem(elephantInventory.getStackInSlot(i), 0.0F);
                    }
                    elephantInventory.removeStackFromSlot(i);
                }
            }
            hasChestVarChanged = false;
        }
        if (isTusked() && !isChild() && !hasTuskedAttributes) {
            this.setSize(TUSKED_WIDTH, TUSKED_HEIGHT);
            hasTuskedAttributes = true;
        }
        if (!isTusked() && !isChild() && hasTuskedAttributes) {
            this.setSize(NORMAL_WIDTH, NORMAL_HEIGHT);
            hasTuskedAttributes = false;
        }
        if (charging) {
            chargingTicks++;
        }
        if (!this.getHeldItemMainhand().isEmpty() && this.canTargetItem(this.getHeldItemMainhand())) {
            if (this.getAnimation() == NO_ANIMATION) {
                this.setAnimation(ANIMATION_EAT);
            }
            if (this.getAnimation() == ANIMATION_EAT && this.getAnimationTick() == 17) {
                this.eatItemEffect(this.getHeldItemMainhand());
                if (!this.world.isRemote) {
                    if (this.getHeldItemMainhand().getItem() == AMItemRegistry.ACACIA_BLOSSOM && !this.isTamed() && (!isTusked() || isChild()) && blossomThrowerUUID != null) {
                        if (rand.nextInt(3) == 0) {
                            EntityPlayer player = this.world.getPlayerEntityByUUID(blossomThrowerUUID);
                            if (player != null) {
                                this.setTamedBy(player);
                            } else {
                                this.setTamed(true);
                                this.setOwnerId(blossomThrowerUUID);
                            }
                            for (Entity passenger : this.getPassengers()) {
                                passenger.dismountRidingEntity();
                            }
                            this.world.setEntityState(this, (byte) 7);
                        } else {
                            this.world.setEntityState(this, (byte) 6);
                        }
                    }
                    this.setHeldItem(EnumHand.MAIN_HAND, ItemStack.EMPTY);
                    this.heal(10);
                }
            }
        }
        if (chargeCooldown > 0) {
            chargeCooldown--;
        }
        if (charging) {
            chargingTicks++;
        } else {
            chargingTicks = 0;
        }
        if (this.getAnimation() == ANIMATION_CHARGE_PREPARE) {
            this.renderYawOffset = rotationYaw;
            if (this.getAnimationTick() == 20) {
                this.charging = true;
            }
        }
        if (this.getControllingPassenger() != null && charging && chargingTicks > 100) {
            this.charging = false;
            this.chargeCooldown = 200;
        }
        EntityLivingBase target = this.getAttackTarget();
        double maxAttackMod = 0.0F;
        if (this.getControllingPassenger() != null && this.getControllingPassenger() instanceof EntityPlayer) {
            EntityPlayer rider = (EntityPlayer) this.getControllingPassenger();
            if (rider.getLastAttackedEntity() != null && !this.isOnSameTeam(rider.getLastAttackedEntity())) {
                UUID preyUUID = rider.getLastAttackedEntity().getUniqueID();
                if (!this.getUniqueID().equals(preyUUID)) {
                    target = rider.getLastAttackedEntity();
                    maxAttackMod = 4F;
                }
            }
        }
        if (!world.isRemote && target != null) {
            if (this.getDistance(target) > 8 && this.getControllingPassenger() == null && this.isTusked() && this.canEntityBeSeen(target) && this.getAnimation() == NO_ANIMATION && !charging && chargeCooldown == 0) {
                this.setAnimation(ANIMATION_CHARGE_PREPARE);
            }
            if (this.getAnimation() == ANIMATION_CHARGE_PREPARE && this.getControllingPassenger() == null) {
                this.getLookHelper().setLookPositionWithEntity(target, 360, 30);
                this.renderYawOffset = rotationYaw;
                if (this.getAnimationTick() == 20) {
                    this.charging = true;
                }
            }
            if (this.getDistance(target) < 10D && charging) {
                this.setAnimation(ANIMATION_FLING);
            }
            if (this.getDistance(target) < 2.1D && charging) {
                target.knockBack(this, 1F, target.posX - this.posX, target.posZ - this.posZ);
                target.isAirBorne = true;
                target.motionY += 0.7D;
                target.attackEntityFrom(DamageSource.causeMobDamage(this), 2.4F * (float) this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue());
                launch(target, true);
                this.charging = false;
                this.chargeCooldown = 400;
            }
            double dist = this.getDistance(target);
            if (dist < 4.5D + maxAttackMod && this.getAnimation() == ANIMATION_FLING && this.getAnimationTick() == 15) {
                target.knockBack(this, 1F, target.posX - this.posX, target.posZ - this.posZ);
                target.motionY += 0.3D;
                launch(target, false);
                target.attackEntityFrom(DamageSource.causeMobDamage(this), (float) this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue());
            }
            if (dist < 4.5D + maxAttackMod && this.getAnimation() == ANIMATION_STOMP && this.getAnimationTick() == 17) {
                target.knockBack(this, 0.3F, target.posX - this.posX, target.posZ - this.posZ);
                target.attackEntityFrom(DamageSource.causeMobDamage(this), (float) this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue());
            }
        }
        if (!world.isRemote && this.getAttackTarget() == null && this.getControllingPassenger() == null) {
            charging = false;
        }
        if (charging && !hasChargedSpeed) {
            this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.65D);
            hasChargedSpeed = true;
        }
        if (!charging && hasChargedSpeed) {
            this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.35D);
            hasChargedSpeed = false;
        }
        if (!world.isRemote && this.getRNG().nextInt(400) == 0 && this.getAnimation() == NO_ANIMATION) {
            this.setAnimation(this.getRNG().nextBoolean() ? ANIMATION_TRUMPET_0 : ANIMATION_TRUMPET_1);
        }
        if (this.getAnimation() == ANIMATION_TRUMPET_0 && this.getAnimationTick() == 8 || this.getAnimation() == ANIMATION_TRUMPET_1 && this.getAnimationTick() == 4) {
            this.playSound(AMSoundRegistry.ELEPHANT_TRUMPET, this.getSoundVolume(), this.getSoundPitch());
        }
        if (this.isEntityAlive() && charging) {
            for (Entity entity : this.world.getEntitiesWithinAABB(EntityLivingBase.class, this.getEntityBoundingBox().grow(1.0D, 1.0D, 1.0D))) {
                if (!(this.isTamed() && isOnSameTeam(entity)) && !(!this.isTamed() && entity instanceof EntityElephant) && entity != this) {
                    entity.attackEntityFrom(DamageSource.causeMobDamage(this), 8.0F + rand.nextFloat() * 8.0F);
                    launch(entity, true);
                }
            }
            stepHeight = 2;
        }
        if (!isTamed() && isTrader()) {
            if (!this.world.isRemote) {
                this.tryDespawn();
            }
        }
        if (this.getAttackTarget() != null && !this.getAttackTarget().isEntityAlive()) {
            this.setAttackTarget(null);
        }
        AMEntityRegistry.updateAnimations(this);
    }

    private boolean canTraderDespawn() {
        return !this.isTamed() && this.isTrader();
    }

    private void tryDespawn() {
        if (this.canTraderDespawn()) {
            this.despawnDelay = this.despawnDelay - 1;
            if (this.despawnDelay <= 0) {
                this.clearLeashed(true, false);
                this.elephantInventory.clear();
                if (this.getControllingVillager() != null) {
                    this.getControllingVillager().setDead();
                }
                this.setDead();
            }
        }
    }

    private void launch(Entity e, boolean huge) {
        if (e.onGround) {
            double d0 = e.posX - this.posX;
            double d1 = e.posZ - this.posZ;
            double d2 = Math.max(d0 * d0 + d1 * d1, 0.001D);
            float f = huge ? 2F : 0.5F;
            e.addVelocity(d0 / d2 * f, huge ? 0.5D : 0.2D, d1 / d2 * f);
        }
    }

    private void eatItemEffect(ItemStack heldItemMainhand) {
        this.playSound(SoundEvents.ENTITY_GENERIC_EAT, this.getSoundPitch(), this.getSoundVolume());
        for (int i = 0; i < 8 + rand.nextInt(3); i++) {
            double d2 = this.rand.nextGaussian() * 0.02D;
            double d0 = this.rand.nextGaussian() * 0.02D;
            double d1 = this.rand.nextGaussian() * 0.02D;
            float radius = this.width * 0.65F;
            float angle = (0.01745329251F * this.renderYawOffset);
            double extraX = radius * MathHelper.sin((float) (Math.PI + angle));
            double extraZ = radius * MathHelper.cos(angle);
            double px = this.posX + extraX;
            double py = this.posY + this.height * 0.6F;
            double pz = this.posZ + extraZ;
            if (heldItemMainhand.getItem() instanceof ItemBlock) {
                IBlockState state = ((ItemBlock) heldItemMainhand.getItem()).getBlock().getDefaultState();
                this.world.spawnParticle(EnumParticleTypes.BLOCK_CRACK, px, py, pz, d0, d1, d2, Block.getStateId(state));
            } else {
                this.world.spawnParticle(EnumParticleTypes.ITEM_CRACK, px, py, pz, d0, d1, d2, Item.getIdFromItem(heldItemMainhand.getItem()));
            }
        }
    }

    private boolean isChargePlayer(Entity controllingPassenger) {
        return true;
    }

    public boolean attackEntityAsMob(Entity entityIn) {
        if (this.getAnimation() == NO_ANIMATION && !this.charging) {
            this.setAnimation(rand.nextBoolean() ? ANIMATION_FLING : ANIMATION_STOMP);
        }
        return true;
    }


    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        boolean owner = this.isTamed() && isOwner(player);
        if (super.processInteract(player, hand)) {
            return true;
        }
        if (isChested() && player.isSneaking()) {
            this.openGUI(player);
            return true;
        } else if (canTargetItem(stack) && this.getHeldItemMainhand().isEmpty()) {
            ItemStack rippedStack = stack.copy();
            rippedStack.setCount(1);
            stack.shrink(1);
            this.setHeldItem(EnumHand.MAIN_HAND, rippedStack);
            if (rippedStack.getItem() == AMItemRegistry.ACACIA_BLOSSOM) {
                blossomThrowerUUID = player.getUniqueID();
            }
            return true;
        } else if (owner && isCarpetItem(stack.getItem())) {
            EnumDyeColor color = getCarpetColor(stack);
            if (color != this.getColor()) {
                if (this.getColor() != null) {
                    this.entityDropItem(this.getCarpetItemBeingWorn(), 0.0F);
                }
                this.playSound(SoundEvents.ENTITY_LLAMA_SWAG, 1.0F, (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 1.0F);
                stack.shrink(1);
                this.setColor(color);
                return true;
            }
            return false;
        } else if (owner && this.getColor() != null && stack.getItem() == Items.SHEARS) {
            this.playSound(SoundEvents.ENTITY_SHEEP_SHEAR, 1.0F, (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 1.0F);
            if (this.getColor() != null) {
                this.entityDropItem(this.getCarpetItemBeingWorn(), 0.0F);
            }
            this.setColor(null);
            return true;
        } else if (owner && !this.isChested() && isWoodenChestItem(stack.getItem())) {
            this.setChested(true);
            this.playSound(SoundEvents.ENTITY_DONKEY_CHEST, 1.0F, (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 1.0F);
            if (!player.capabilities.isCreativeMode) {
                stack.shrink(1);
            }
            return true;
        } else if (owner && isChested() && stack.getItem() == Items.SHEARS) {
            this.playSound(SoundEvents.ENTITY_SHEEP_SHEAR, 1.0F, (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 1.0F);
            this.entityDropItem(new ItemStack(Blocks.CHEST), 0.0F);
            for (int i = 0; i < elephantInventory.getSizeInventory(); i++) {
                if (!elephantInventory.getStackInSlot(i).isEmpty()) {
                    this.entityDropItem(elephantInventory.getStackInSlot(i), 0.0F);
                }
            }
            elephantInventory.clear();
            this.setChested(false);
            return true;
        } else if (owner && !this.isChild()) {
            if (!world.isRemote) {
                player.startRiding(this);
            }
            return true;
        }
        return false;
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
        return new Animation[]{ANIMATION_TRUMPET_0, ANIMATION_TRUMPET_1, ANIMATION_CHARGE_PREPARE, ANIMATION_STOMP, ANIMATION_FLING, ANIMATION_EAT, ANIMATION_BREAKLEAVES};
    }

    @Override
    public int getAnimationTick() {
        return animationTick;
    }

    @Override
    public void setAnimationTick(int tick) {
        animationTick = tick;
    }

    public ItemStack getCarpetItemBeingWorn() {
        if (this.getColor() != null) {
            return new ItemStack(Blocks.CARPET, 1, this.getColor().getMetadata());
        }
        return ItemStack.EMPTY;
    }

    @Nullable
    @Override
    public EntityAgeable createChild(EntityAgeable ageable) {
        EntityElephant baby = new EntityElephant(this.world);
        baby.setTusked(this.getNearestTusked(this.world, 15) == null || rand.nextInt(2) == 0);
        return baby;
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setBoolean("Tusked", this.isTusked());
        compound.setBoolean("ElephantSitting", this.isSitting());
        compound.setBoolean("Standing", this.isStanding());
        compound.setBoolean("Chested", this.isChested());
        compound.setBoolean("Trader", this.isTrader());
        compound.setBoolean("ForcedToSit", this.forcedSit);
        compound.setBoolean("Tamed", this.isTamed());
        compound.setInteger("ChargeCooldown", this.chargeCooldown);
        compound.setInteger("Carpet", this.dataManager.get(CARPET_COLOR));
        compound.setInteger("DespawnDelay", this.despawnDelay);
        if (elephantInventory != null) {
            NBTTagList nbttaglist = new NBTTagList();
            for (int i = 0; i < this.elephantInventory.getSizeInventory(); ++i) {
                ItemStack itemstack = this.elephantInventory.getStackInSlot(i);
                if (!itemstack.isEmpty()) {
                    NBTTagCompound slotTag = new NBTTagCompound();
                    slotTag.setByte("Slot", (byte) i);
                    itemstack.writeToNBT(slotTag);
                    nbttaglist.appendTag(slotTag);
                }
            }
            compound.setTag("Items", nbttaglist);
        }
    }

    @Override
    public boolean isPotionApplicable(PotionEffect potioneffectIn) {
        if (potioneffectIn.getPotion() == MobEffects.WITHER) {
            return false;
        }
        return super.isPotionApplicable(potioneffectIn);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.setTamed(compound.getBoolean("Tamed"));
        this.setTusked(compound.getBoolean("Tusked"));
        this.setStanding(compound.getBoolean("Standing"));
        this.setSitting(compound.getBoolean("ElephantSitting"));
        this.setChested(compound.getBoolean("Chested"));
        this.setTrader(compound.getBoolean("Trader"));
        this.forcedSit = compound.getBoolean("ForcedToSit");
        this.chargeCooldown = compound.getInteger("ChargeCooldown");
        this.dataManager.set(CARPET_COLOR, compound.getInteger("Carpet"));
        if (elephantInventory != null) {
            NBTTagList nbttaglist = compound.getTagList("Items", 10);
            this.initElephantInventory();
            for (int i = 0; i < nbttaglist.tagCount(); ++i) {
                NBTTagCompound slotTag = nbttaglist.getCompoundTagAt(i);
                int j = slotTag.getByte("Slot") & 255;
                this.elephantInventory.setInventorySlotContents(j, new ItemStack(slotTag));
            }
        } else {
            NBTTagList nbttaglist = compound.getTagList("Items", 10);
            this.initElephantInventory();
            for (int i = 0; i < nbttaglist.tagCount(); ++i) {
                NBTTagCompound slotTag = nbttaglist.getCompoundTagAt(i);
                int j = slotTag.getByte("Slot") & 255;
                this.initElephantInventory();
                this.elephantInventory.setInventorySlotContents(j, new ItemStack(slotTag));
            }
        }
        if (compound.hasKey("DespawnDelay", 99)) {
            this.despawnDelay = compound.getInteger("DespawnDelay");
        }

    }

    public boolean isChested() {
        return Boolean.valueOf(this.dataManager.get(CHESTED).booleanValue());
    }

    public void setChested(boolean chested) {
        this.dataManager.set(CHESTED, Boolean.valueOf(chested));
        this.hasChestVarChanged = true;
    }

    public boolean replaceItemInInventory(int inventorySlot, @Nullable ItemStack itemStackIn) {
        int j = inventorySlot - 500 + 2;
        if (j >= 0 && j < this.elephantInventory.getSizeInventory()) {
            this.elephantInventory.setInventorySlotContents(j, itemStackIn);
            return true;
        } else {
            return false;
        }
    }

    public void onDeath(DamageSource cause) {
        super.onDeath(cause);
        if (elephantInventory != null && !this.world.isRemote) {
            for (int i = 0; i < elephantInventory.getSizeInventory(); ++i) {
                ItemStack itemstack = elephantInventory.getStackInSlot(i);
                if (!itemstack.isEmpty()) {
                    this.entityDropItem(itemstack, 0.0F);
                }
            }
        }
    }


    public boolean isStanding() {
        return this.dataManager.get(STANDING).booleanValue();
    }

    public void setStanding(boolean standing) {
        this.dataManager.set(STANDING, Boolean.valueOf(standing));
    }

    public boolean isSitting() {
        return this.dataManager.get(SITTING).booleanValue();
    }

    public void setSitting(boolean sit) {
        this.dataManager.set(SITTING, Boolean.valueOf(sit));
    }

    @Nullable
    public EnumDyeColor getColor() {
        int lvt_1_1_ = this.dataManager.get(CARPET_COLOR);
        return lvt_1_1_ == -1 ? null : EnumDyeColor.byMetadata(lvt_1_1_);
    }

    public void setColor(@Nullable EnumDyeColor color) {
        this.dataManager.set(CARPET_COLOR, color == null ? -1 : color.getMetadata());
    }

    @Nullable
    @Override
    public IEntityLivingData onInitialSpawn(DifficultyInstance difficultyIn, @Nullable IEntityLivingData spawnDataIn) {
        this.setTusked(this.getRNG().nextBoolean());
        return super.onInitialSpawn(difficultyIn, spawnDataIn);
    }

    @Nullable
    public EntityElephant getNearestTusked(World world, double dist) {
        List<EntityElephant> list = world.getEntitiesWithinAABB(this.getClass(), this.getEntityBoundingBox().grow(dist, dist / 2, dist));
        if (list.isEmpty()) {
            return null;
        }
        EntityElephant elephant1 = null;
        double d0 = Double.MAX_VALUE;
        for (EntityElephant elephant : list) {
            if (elephant.isTusked()) {
                double d1 = this.getDistanceSq(elephant);
                if (!(d1 > d0)) {
                    d0 = d1;
                    elephant1 = elephant;
                }
            }
        }
        return elephant1;
    }

    public boolean isTusked() {
        return this.dataManager.get(TUSKED).booleanValue();
    }

    public void setTusked(boolean tusked) {
        boolean prev = isTusked();
        if (!prev && tusked) {
            this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(80.0D);
            this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(15.0D);
            this.setHealth(150.0F);
        } else {
            this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(65.0D);
            this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(10.0D);
        }
        this.dataManager.set(TUSKED, tusked);
    }

    @Override
    public boolean canTargetItem(ItemStack stack) {
        return AMTagRegistry.itemInTag(AMTagRegistry.ELEPHANT_FOODSTUFFS, stack.getItem()) || stack.getItem() == AMItemRegistry.ACACIA_BLOSSOM;
    }

    @Override
    public void onGetItem(EntityItem e) {
        ItemStack duplicate = e.getItem().copy();
        duplicate.setCount(1);
        if (!this.getHeldItemMainhand().isEmpty() && !this.world.isRemote) {
            this.entityDropItem(this.getHeldItemMainhand(), 0.0F);
        }
        if (duplicate.getItem() == AMItemRegistry.ACACIA_BLOSSOM) {
            blossomThrowerUUID = resolveThrowerUuid(e);
        } else {
            blossomThrowerUUID = null;
        }
        this.setHeldItem(EnumHand.MAIN_HAND, duplicate);
        this.aiItemFlag = false;
    }

    @Override
    public void onFindTarget(EntityItem e) {
        this.aiItemFlag = true;
    }

    @Nullable
    private static UUID resolveThrowerUuid(EntityItem item) {
        String throwerName = item.getThrower();
        if (throwerName == null || throwerName.isEmpty()) {
            return null;
        }
        if (item.world instanceof net.minecraft.world.WorldServer) {
            EntityPlayer player = ((net.minecraft.world.WorldServer) item.world).getMinecraftServer().getPlayerList().getPlayerByUsername(throwerName);
            if (player != null) {
                return player.getUniqueID();
            }
        }
        return null;
    }

    public void addElephantLoot(@Nullable EntityPlayer player, int seed) {
        if (this.world instanceof net.minecraft.world.WorldServer) {
            net.minecraft.world.WorldServer sw = (net.minecraft.world.WorldServer) this.world;
            LootTable lootTable = sw.getLootTableManager().getLootTableFromLocation(TRADER_LOOT);
            java.util.List<ItemStack> loot = lootTable.generateLootForPools(sw.rand, new LootContext.Builder(sw).build());
            for (ItemStack stack : loot) {
                for (int i = 0; i < this.elephantInventory.getSizeInventory(); ++i) {
                    if (this.elephantInventory.getStackInSlot(i).isEmpty()) {
                        this.elephantInventory.setInventorySlotContents(i, stack);
                        break;
                    }
                }
            }
        }

    }

    public void leaveCaravan() {
        if (this.caravanHead != null) {
            this.caravanHead.caravanTail = null;
        }

        this.caravanHead = null;
    }

    public void joinCaravan(EntityElephant caravanHeadIn) {
        this.caravanHead = caravanHeadIn;
        this.caravanHead.caravanTail = this;
    }

    public boolean hasCaravanTrail() {
        return this.caravanTail != null;
    }

    public boolean inCaravan() {
        return this.caravanHead != null;
    }

    @Nullable
    public EntityElephant getCaravanHead() {
        return this.caravanHead;
    }

    public double getMaxDistToItem() {
        return 5.0D;
    }

    public void updatePassenger(Entity passenger) {
        if (this.getPassengers().contains(passenger)) {
            float standAdd = -0.3F * standProgress;
            float scale = this.isChild() ? 0.5F : this.isTusked() ? 1.1F : 1.0F;
            float sitAdd = -0.065F * sitProgress;
            float scaleY = scale * (2.4F * sitAdd - 0.4F * standAdd);
            if (passenger instanceof EntityVillager) {
                scaleY -= 0.3F;
            }
            float radius = scale * (0.5F + standAdd);
            float angle = (0.01745329251F * this.renderYawOffset);
            if (this.getAnimation() == ANIMATION_CHARGE_PREPARE) {
                float sinWave = MathHelper.sin((float) (Math.PI * (this.getAnimationTick() / 25F)));
                radius += sinWave * 0.2F * scale;
            }
            if (this.getAnimation() == ANIMATION_STOMP) {
                float sinWave = MathHelper.sin((float) (Math.PI * (this.getAnimationTick() / 20F)));
                radius -= sinWave * 1.0F * scale;
                scaleY += sinWave * 0.7F * scale;
            }
            double extraX = radius * MathHelper.sin((float) (Math.PI + angle));
            double extraZ = radius * MathHelper.cos(angle);

            passenger.setPosition(this.posX + extraX, this.posY + this.getMountedYOffset() + scaleY + passenger.getYOffset(), this.posZ + extraZ);
        }
    }

    public boolean canBeSteered() {
        return false;
    }


    public boolean canPassengerSteer() {
        return false;
    }

    public double getMountedYOffset() {
        float scale = this.isChild() ? 0.5F : this.isTusked() ? 1.1F : 1.0F;
        float f = Math.min(0.25F, this.limbSwingAmount);
        float f1 = this.limbSwing;
        float sitAdd = 0.01F * 0;
        float standAdd = 0.07F * 0;
        return (double) this.height - 0.05F - scale * ((double) (0.1F * MathHelper.cos(f1 * 1.4F) * 1.4F * f) + sitAdd + standAdd);
    }

    public boolean isOnSameTeam(Entity entityIn) {
        if (this.isTamed()) {
            EntityLivingBase livingentity = this.getOwner();
            if (entityIn == livingentity) {
                return true;
            }
            if (entityIn instanceof EntityTameable) {
                return ((EntityTameable) entityIn).isOwner(livingentity);
            }
            if (livingentity != null) {
                return livingentity.isOnSameTeam(entityIn);
            }
        }

        return super.isOnSameTeam(entityIn);
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

    public void openGUI(EntityPlayer playerEntity) {
        if (!this.world.isRemote && !this.getPassengers().contains(playerEntity)) {
            ((EntityPlayerMP) playerEntity).displayGUIChest(this.elephantInventory);
        }
    }

    public boolean isTrader() {
        return this.dataManager.get(TRADER).booleanValue();
    }

    public void setTrader(boolean trader) {
        this.dataManager.set(TRADER, trader);
    }

    public boolean triggerCharge(ItemStack stack) {
        if (this.getControllingPassenger() != null && chargeCooldown == 0 && !charging && this.getAnimation() == NO_ANIMATION && this.isTusked()) {
            this.setAnimation(ANIMATION_CHARGE_PREPARE);
            this.eatItemEffect(stack);
            this.heal(2);
            return true;
        }
        return false;
    }

    public boolean canSpawnWithTraderHere() {
        return this.getCanSpawnHere() && this.world.isAirBlock(this.getPosition().up(4));
    }

    private class AIWalkIdle extends EntityAIBase {
        private final EntityElephant elephant;
        private final double speed;
        private int executionChance = 120;
        @Nullable
        private Vec3d wanderTarget;

        public AIWalkIdle(EntityElephant e, double v) {
            this.elephant = e;
            this.speed = v;
            this.setMutexBits(1);
        }

        @Override
        public boolean shouldExecute() {
            this.executionChance = EntityElephant.this.isTusked() || !EntityElephant.this.inCaravan() ? 50 : 120;
            if (this.elephant.isRiding() || this.elephant.getAttackTarget() != null) {
                return false;
            }
            if (this.elephant.getRNG().nextInt(this.executionChance) != 0) {
                return false;
            }
            this.wanderTarget = RandomPositionGenerator.findRandomTarget(this.elephant, this.elephant.isTusked() || !this.elephant.inCaravan() ? 25 : 10, 7);
            return this.wanderTarget != null;
        }

        @Override
        public boolean shouldContinueExecuting() {
            return !this.elephant.getNavigator().noPath();
        }

        @Override
        public void startExecuting() {
            if (this.wanderTarget != null) {
                this.elephant.getNavigator().tryMoveToXYZ(this.wanderTarget.x, this.wanderTarget.y, this.wanderTarget.z, this.speed);
            }
        }
    }

    class HurtByTargetGoal extends EntityAIHurtByTarget {
        public HurtByTargetGoal() {
            super(EntityElephant.this, true);
        }

        @Override
        public void startExecuting() {
            if (EntityElephant.this.isChild() || !EntityElephant.this.isTusked()) {
                this.alertOthers();
                this.resetTask();
            } else {
                super.startExecuting();
            }
        }

        @Override
        protected void setEntityAttackTarget(EntityCreature creature, EntityLivingBase target) {
            if (creature instanceof EntityElephant && (!creature.isChild() || !((EntityElephant) creature).isTusked())) {
                super.setEntityAttackTarget(creature, target);
            }
        }
    }

    class PanicGoal extends EntityAIPanic {
        public PanicGoal() {
            super(EntityElephant.this, 1.0D);
        }

        @Override
        public boolean shouldExecute() {
            return (EntityElephant.this.isChild() || !EntityElephant.this.isTusked() || EntityElephant.this.isBurning()) && super.shouldExecute();
        }
    }
}
