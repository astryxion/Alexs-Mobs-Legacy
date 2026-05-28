package com.github.alexthe666.alexsmobs.entity;

import com.github.alexthe666.alexsmobs.config.AMConfig;
import com.github.alexthe666.alexsmobs.entity.ai.*;
import com.github.alexthe666.alexsmobs.misc.AMSoundRegistry;
import com.github.alexthe666.alexsmobs.misc.AMTagRegistry;
import com.github.alexthe666.citadel.animation.Animation;
import com.github.alexthe666.citadel.animation.IAnimatedEntity;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.List;
import com.google.common.base.Optional;
import java.util.UUID;

public class EntityRaccoon extends EntityTameable implements IAnimatedEntity, IFollower, ITargetsDroppedItems, ILootsChests {

    private static final DataParameter<Boolean> STANDING = EntityDataManager.createKey(EntityRaccoon.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> SITTING = EntityDataManager.createKey(EntityRaccoon.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> BEGGING = EntityDataManager.createKey(EntityRaccoon.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> WASHING = EntityDataManager.createKey(EntityRaccoon.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Optional<BlockPos>> WASH_POS = EntityDataManager.createKey(EntityRaccoon.class, DataSerializers.OPTIONAL_BLOCK_POS);
    private static final DataParameter<Integer> COMMAND = EntityDataManager.createKey(EntityRaccoon.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> CARPET_COLOR = EntityDataManager.createKey(EntityRaccoon.class, DataSerializers.VARINT);

    public static final Animation ANIMATION_ATTACK = Animation.create(12);

    public float prevStandProgress;
    public float standProgress;
    public float prevBegProgress;
    public float begProgress;
    public float prevWashProgress;
    public float washProgress;
    public float prevSitProgress;
    public float sitProgress;
    public int maxStandTime = 75;
    public int lookForWaterBeforeEatingTimer = 0;
    public boolean forcedSit = false;

    private int standingTime = 0;
    private int stealCooldown = 0;
    private int animationTick;
    private Animation currentAnimation;
    private int pickupItemCooldown = 0;
    @Nullable
    private UUID eggThrowerUUID = null;

    public EntityRaccoon(World world) {
        super(world);
        this.setSize(0.6F, 0.8F);
        this.setPathPriority(PathNodeType.WATER, 0.0F);
    }

    @Override
    protected float getWaterSlowDown() {
        return 0.98F;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return AMSoundRegistry.RACCOON_IDLE;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return AMSoundRegistry.RACCOON_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return AMSoundRegistry.RACCOON_HURT;
    }

    @Override
    public boolean getCanSpawnHere() {
        return AMEntityRegistry.rollSpawn(AMConfig.raccoonSpawnRolls, this.getRNG(), AMEntityRegistry.AMSpawnReason.OTHER) && super.getCanSpawnHere();
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        AMEntityRegistry.registerAttributeIfAbsent(this, SharedMonsterAttributes.ATTACK_DAMAGE);
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(9.0D);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(2.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.25D);
    }

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(0, new EntityAISit(this));
        this.tasks.addTask(1, new EntityAIMate(this, 1.0D));
        this.tasks.addTask(2, new RaccoonAIWash(this));
        this.tasks.addTask(3, new TameableAIFollowOwner(this, 1.3D, 10.0F, 2.0F, false));
        this.tasks.addTask(4, new EntityAISwimming(this));
        this.tasks.addTask(5, new EntityAILeapAtTarget(this, 0.4F));
        this.tasks.addTask(6, new EntityAIAttackMelee(this, 1.1D, true));
        this.tasks.addTask(7, new AnimalAILootChests(this, 16));
        this.tasks.addTask(8, new EntityAIFollowParent(this, 1.1D));
        this.tasks.addTask(9, new RaccoonAIBeg(this, 0.65D));
        this.tasks.addTask(10, new AnimalAIPanicBaby(this, 1.25D));
        this.tasks.addTask(11, new AIStealFromVillagers(this));
        this.tasks.addTask(12, new RaccoonStrollGoal(this, 200));
        this.tasks.addTask(13, new AnimalAIWanderRanged(this, 120, 1.0D, 14, 7));
        this.tasks.addTask(14, new EntityAIWatchClosest(this, EntityPlayer.class, 15.0F));
        this.tasks.addTask(15, new EntityAILookIdle(this));
        this.targetTasks.addTask(1, new AnimalAIHurtByTargetNotBaby(this));
        this.targetTasks.addTask(1, new CreatureAITargetItems(this, false));
        this.targetTasks.addTask(3, new EntityAIOwnerHurtByTarget(this));
        this.targetTasks.addTask(4, new EntityAIOwnerHurtTarget(this));
    }

    @Override
    public boolean isOnSameTeam(Entity entityIn) {
        if (entityIn instanceof EntityBlueJay) {
            EntityBlueJay jay = (EntityBlueJay) entityIn;
            return jay.getRaccoonUUID() != null && jay.getRaccoonUUID().equals(this.getUniqueID());
        }
        if (this.isTamed()) {
            EntityLivingBase owner = this.getOwner();
            if (entityIn == owner) {
                return true;
            }
            if (entityIn instanceof EntityTameable) {
                return ((EntityTameable) entityIn).isOwner(owner);
            }
            if (owner != null) {
                return owner.isOnSameTeam(entityIn);
            }
        }
        return super.isOnSameTeam(entityIn);
    }

    @Override
    public boolean attackEntityAsMob(Entity entityIn) {
        if (this.getAnimation() == NO_ANIMATION) {
            this.setAnimation(ANIMATION_ATTACK);
        }
        return true;
    }

    @Override
    protected void dropEquipment(boolean wasRecentlyHit, int looting) {
        super.dropEquipment(wasRecentlyHit, looting);
        if (this.getColor() != null && !this.world.isRemote) {
            this.entityDropItem(this.getCarpetItemBeingWorn(), 0.0F);
            this.setColor(null);
        }
    }

    @Nullable
    public EnumDyeColor getColor() {
        int id = this.dataManager.get(CARPET_COLOR);
        return id == -1 ? null : EnumDyeColor.byMetadata(id);
    }

    public void setColor(@Nullable EnumDyeColor color) {
        this.dataManager.set(CARPET_COLOR, color == null ? -1 : color.getMetadata());
    }

    public ItemStack getCarpetItemBeingWorn() {
        if (this.getColor() != null) {
            Item item = EntityElephant.DYE_COLOR_ITEM_MAP.get(this.getColor());
            return item != null ? new ItemStack(item) : ItemStack.EMPTY;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        return stack.getItem() == Items.BREAD;
    }

    public static boolean isRaccoonFood(ItemStack stack) {
        Item item = stack.getItem();
        return item instanceof ItemFood || AMTagRegistry.itemInTag(AMTagRegistry.RACCOON_FOODSTUFFS, item);
    }

    public static boolean isFood(ItemStack stack) {
        return stack.getItem() == Items.BREAD;
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        ItemStack itemstack = player.getHeldItem(hand);
        Item item = itemstack.getItem();
        boolean owner = this.isTamed() && isOwner(player);
        if (itemstack.getItem() == Items.NAME_TAG) {
            return super.processInteract(player, hand);
        }
        if (AMTagRegistry.itemInTag(AMTagRegistry.RACCOON_TEAMING_FOODS, itemstack.getItem()) && bondWithBlueJays(player.getUniqueID())) {
            if (!player.capabilities.isCreativeMode) {
                itemstack.shrink(1);
            }
            this.world.setEntityState(this, (byte) 93);
            return true;
        }
        if (owner && EntityElephant.isElephantCarpet(item)) {
            EnumDyeColor color = EntityElephant.getCarpetColor(itemstack);
            if (color != this.getColor()) {
                if (this.getColor() != null) {
                    this.entityDropItem(this.getCarpetItemBeingWorn(), 0.0F);
                }
                this.playSound(SoundEvents.ENTITY_LLAMA_SWAG, 1.0F, (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 1.0F);
                if (!player.capabilities.isCreativeMode) {
                    itemstack.shrink(1);
                }
                this.setColor(color);
                return true;
            }
            return false;
        } else if (owner && this.getColor() != null && item == Items.SHEARS) {
            this.playSound(SoundEvents.ENTITY_SHEEP_SHEAR, 1.0F, (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 1.0F);
            this.entityDropItem(this.getCarpetItemBeingWorn(), 0.0F);
            this.setColor(null);
            return true;
        } else if (isTamed() && isRaccoonFood(itemstack) && !isFood(itemstack) && this.getHealth() < this.getMaxHealth()) {
            if (this.getHeldItemMainhand().isEmpty()) {
                ItemStack copy = itemstack.copy();
                copy.setCount(1);
                this.setHeldItem(EnumHand.MAIN_HAND, copy);
                this.onEatItem(copy);
                if (itemstack.getItem().hasContainerItem()) {
                    this.entityDropItem(new ItemStack(itemstack.getItem().getContainerItem()), 0.0F);
                }
                if (!player.capabilities.isCreativeMode) {
                    itemstack.shrink(1);
                }
                this.setHeldItem(EnumHand.MAIN_HAND, ItemStack.EMPTY);
            } else {
                this.playSound(SoundEvents.ENTITY_GENERIC_EAT, this.getSoundVolume(), this.getSoundPitch());
                this.heal(5);
            }
            return true;
        }
        if (owner && !this.getHeldItemMainhand().isEmpty()) {
            if (!this.world.isRemote) {
                this.entityDropItem(this.getHeldItemMainhand().copy(), 0.0F);
            }
            this.setHeldItem(EnumHand.MAIN_HAND, ItemStack.EMPTY);
            pickupItemCooldown = 60;
            return true;
        }
        boolean type = super.processInteract(player, hand);
        if (!type && isTamed() && isOwner(player) && !isBreedingItem(itemstack)) {
            if (!player.isSneaking()) {
                this.setCommand(this.getCommand() + 1);
                if (this.getCommand() == 3) {
                    this.setCommand(0);
                }
                player.sendStatusMessage(new TextComponentTranslation("entity.alexsmobs.all.command_" + this.getCommand(), this.getName()), true);
                boolean sit = this.getCommand() == 2;
                if (sit) {
                    this.forcedSit = true;
                    this.setSitting(true);
                    return true;
                } else {
                    this.forcedSit = false;
                    this.setSitting(false);
                    return true;
                }
            }
        }
        return type;
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setBoolean("RacSitting", this.isSitting());
        compound.setBoolean("ForcedToSit", this.forcedSit);
        compound.setInteger("RacCommand", this.getCommand());
        compound.setInteger("Carpet", this.dataManager.get(CARPET_COLOR));
        compound.setInteger("StealCooldown", stealCooldown);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.setSitting(compound.getBoolean("RacSitting"));
        this.forcedSit = compound.getBoolean("ForcedToSit");
        this.setCommand(compound.getInteger("RacCommand"));
        this.dataManager.set(CARPET_COLOR, compound.getInteger("Carpet"));
        this.stealCooldown = compound.getInteger("StealCooldown");
    }

    public void setCommand(int command) {
        this.dataManager.set(COMMAND, command);
    }

    public int getCommand() {
        return this.dataManager.get(COMMAND);
    }

    public void setSitting(boolean sit) {
        this.dataManager.set(SITTING, sit);
    }

    public boolean isSitting() {
        return this.dataManager.get(SITTING);
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (this.isEntityInvulnerable(source)) {
            return false;
        }
        Entity entity = source.getTrueSource();
        this.setSitting(false);
        if (entity != null && this.isTamed() && !(entity instanceof EntityPlayer) && !(entity instanceof EntityArrow)) {
            amount = (amount + 1.0F) / 4.0F;
        }
        return super.attackEntityFrom(source, amount);
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        this.prevStandProgress = this.standProgress;
        this.prevBegProgress = this.begProgress;
        this.prevWashProgress = this.washProgress;
        this.prevSitProgress = this.sitProgress;
        if (this.isStanding() && standProgress < 5) {
            standProgress++;
        }
        if (!this.isStanding() && standProgress > 0) {
            standProgress--;
        }
        if (this.isBegging() && begProgress < 5) {
            begProgress++;
        }
        if (!this.isBegging() && begProgress > 0) {
            begProgress--;
        }
        if (this.isWashing() && washProgress < 5) {
            washProgress++;
        }
        if (!this.isWashing() && washProgress > 0) {
            washProgress--;
        }
        if (this.isSitting() && sitProgress < 5) {
            sitProgress++;
        }
        if (!this.isSitting() && sitProgress > 0) {
            sitProgress--;
        }
        if (isStanding() && ++standingTime > maxStandTime) {
            this.setStanding(false);
            standingTime = 0;
            maxStandTime = 75 + rand.nextInt(50);
        }
        if (!world.isRemote) {
            if (lookForWaterBeforeEatingTimer > 0) {
                lookForWaterBeforeEatingTimer--;
            } else if (!isWashing() && shouldEatHeldFood()) {
                eatHeldFood();
            }
        }
        if (isWashing()) {
            BlockPos washingPos = getWashPos();
            if (washingPos != null) {
                if (this.getDistanceSq(washingPos.getX() + 0.5D, washingPos.getY() + 0.5D, washingPos.getZ() + 0.5D) < 3) {
                    for (int j = 0; (float) j < 4; ++j) {
                        double d2 = this.rand.nextDouble();
                        double d3 = this.rand.nextDouble();
                        this.world.spawnParticle(EnumParticleTypes.WATER_SPLASH, washingPos.getX() + d2, washingPos.getY() + 0.8F, washingPos.getZ() + d3, this.motionX, this.motionY, this.motionZ);
                    }
                } else {
                    setWashing(false);
                }
            }
        }
        if (!world.isRemote && this.getAttackTarget() != null && this.canEntityBeSeen(this.getAttackTarget())
                && this.getDistance(this.getAttackTarget()) < 4
                && this.getAnimation() == ANIMATION_ATTACK && this.getAnimationTick() == 5) {
            float f1 = this.rotationYaw * ((float) Math.PI / 180F);
            this.motionX += -MathHelper.sin(f1) * -0.06F;
            this.motionZ += MathHelper.cos(f1) * -0.06F;
            this.getAttackTarget().knockBack(this, 0.35F, this.getAttackTarget().posX - this.posX, this.getAttackTarget().posZ - this.posZ);
            this.getAttackTarget().attackEntityFrom(DamageSource.causeMobDamage(this), (float) this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue());
        }
        if (stealCooldown > 0) {
            stealCooldown--;
        }
        if (pickupItemCooldown > 0) {
            pickupItemCooldown--;
        }
        AMEntityRegistry.updateAnimations(this);
    }

    private boolean shouldEatHeldFood() {
        ItemStack held = this.getHeldItemMainhand();
        return canTargetItem(held);
    }

    private void eatHeldFood() {
        ItemStack held = this.getHeldItemMainhand();
        if (held.isEmpty() || !isRaccoonFood(held)) {
            return;
        }
        if (held.getItem().hasContainerItem()) {
            this.entityDropItem(new ItemStack(held.getItem().getContainerItem()), 0.0F);
        }
        onEatItem(held);
        held.shrink(1);
        this.lookForWaterBeforeEatingTimer = 100;
    }

    public void onEatItem() {
        onEatItem(this.getHeldItemMainhand());
    }

    public void onEatItem(ItemStack eatenStack) {
        this.heal(10);
        this.spawnEatParticles(eatenStack);
        this.playSound(SoundEvents.ENTITY_GENERIC_EAT, this.getSoundVolume(), this.getSoundPitch());
    }

    private void spawnEatParticles(ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        for (int i = 0; i < 6 + rand.nextInt(3); i++) {
            double d2 = this.rand.nextGaussian() * 0.02D;
            double d0 = this.rand.nextGaussian() * 0.02D;
            double d1 = this.rand.nextGaussian() * 0.02D;
            this.world.spawnParticle(EnumParticleTypes.ITEM_CRACK, this.posX + (double) (this.rand.nextFloat() * this.width) - (double) this.width * 0.5F, this.posY + this.height * 0.5F + (double) (this.rand.nextFloat() * this.height * 0.5F), this.posZ + (double) (this.rand.nextFloat() * this.width) - (double) this.width * 0.5F, d0, d1, d2, Item.getIdFromItem(stack.getItem()), stack.getMetadata());
        }
    }

    public void postWashItem(ItemStack stack) {
        if (stack.getItem() == Items.EGG && eggThrowerUUID != null && !this.isTamed()) {
            if (getRNG().nextFloat() < 0.3F) {
                this.setTamed(true);
                this.setOwnerId(eggThrowerUUID);
                EntityPlayer player = world.getPlayerEntityByUUID(eggThrowerUUID);
                if (player instanceof EntityPlayerMP) {
                    CriteriaTriggers.TAME_ANIMAL.trigger((EntityPlayerMP) player, this);
                }
                this.world.setEntityState(this, (byte) 7);
            } else {
                this.world.setEntityState(this, (byte) 6);
            }
        }
    }


    public boolean isStanding() {
        return this.dataManager.get(STANDING);
    }

    public void setStanding(boolean standing) {
        this.dataManager.set(STANDING, standing);
    }

    public boolean isBegging() {
        return this.dataManager.get(BEGGING);
    }

    public void setBegging(boolean begging) {
        this.dataManager.set(BEGGING, begging);
    }

    public boolean isWashing() {
        return this.dataManager.get(WASHING);
    }

    public void setWashing(boolean washing) {
        this.dataManager.set(WASHING, washing);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(STANDING, Boolean.FALSE);
        this.dataManager.register(SITTING, Boolean.FALSE);
        this.dataManager.register(BEGGING, Boolean.FALSE);
        this.dataManager.register(WASHING, Boolean.FALSE);
        this.dataManager.register(CARPET_COLOR, -1);
        this.dataManager.register(COMMAND, 0);
        this.dataManager.register(WASH_POS, Optional.absent());
    }

    public BlockPos getWashPos() {
        return this.dataManager.get(WASH_POS).orNull();
    }

    public void setWashPos(BlockPos washingPos) {
        this.dataManager.set(WASH_POS, Optional.fromNullable(washingPos));
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
        if (animation == ANIMATION_ATTACK) {
            maxStandTime = 15;
            this.setStanding(true);
        }
    }

    @Override
    public Animation[] getAnimations() {
        return new Animation[]{ANIMATION_ATTACK};
    }

    @Nullable
    @Override
    public EntityAgeable createChild(EntityAgeable ageable) {
        return (EntityRaccoon) AMEntityRegistry.RACCOON.newInstance(this.world);
    }

    @Override
    public void travel(float strafe, float vertical, float forward) {
        if (this.isSitting() || this.isWashing()) {
            if (this.getNavigator().getPath() != null) {
                this.getNavigator().clearPath();
            }
            strafe = 0.0F;
            vertical = 0.0F;
            forward = 0.0F;
        }
        super.travel(strafe, vertical, forward);
    }

    @Override
    public boolean shouldFollow() {
        return getCommand() == 1;
    }

    @Override
    public boolean canTargetItem(ItemStack stack) {
        return isRaccoonFood(stack) && pickupItemCooldown == 0;
    }

    @Override
    public void onGetItem(EntityItem e) {
        lookForWaterBeforeEatingTimer = 100;
        ItemStack duplicate = e.getItem().copy();
        duplicate.setCount(1);
        if (!this.getHeldItem(EnumHand.MAIN_HAND).isEmpty() && !this.world.isRemote) {
            this.entityDropItem(this.getHeldItem(EnumHand.MAIN_HAND), 0.0F);
        }
        String throwerName = e.getThrower();
        EntityPlayer thrower = throwerName != null ? world.getPlayerEntityByName(throwerName) : null;
        if (AMTagRegistry.itemInTag(AMTagRegistry.RACCOON_TEAMING_FOODS, e.getItem().getItem()) && thrower != null && bondWithBlueJays(thrower.getUniqueID())) {
            this.world.setEntityState(this, (byte) 93);
        } else {
            this.setHeldItem(EnumHand.MAIN_HAND, duplicate);
            if (e.getItem().getItem() == Items.EGG) {
                EntityPlayer eggThrower = world.getPlayerEntityByName(e.getThrower());
                eggThrowerUUID = eggThrower != null ? eggThrower.getUniqueID() : null;
            } else {
                eggThrowerUUID = null;
            }
        }
    }

    private boolean bondWithBlueJays(UUID uuid) {
        AxisAlignedBB allyBox = this.getEntityBoundingBox().grow(48);
        boolean any = false;
        List<EntityBlueJay> jays = this.world.getEntitiesWithinAABB(EntityBlueJay.class, allyBox);
        for (EntityBlueJay entity : jays) {
            if (entity.getFeedTime() > 0 && entity.getLastFeederUUID() != null && entity.getLastFeederUUID().equals(uuid)) {
                entity.setRaccoon(this);
                entity.setFeedTime(0);
                any = true;
            }
        }
        return any;
    }

    @Override
    public boolean isLootable(IInventory inventory) {
        for (int i = 0; i < inventory.getSizeInventory(); i++) {
            if (shouldLootItem(inventory.getStackInSlot(i))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean shouldLootItem(ItemStack stack) {
        return isRaccoonFood(stack);
    }

    public BlockPos getLightPosition() {
        BlockPos pos = new BlockPos(this.posX, this.posY, this.posZ);
        if (!world.getBlockState(pos).isNormalCube()) {
            return pos.up();
        }
        return pos;
    }

    /**
     * 1.12.2 port of 1.16 {@code MoveThroughVillageAtNightGoal} ({@code StrollGoal}).
     */
    private static class RaccoonStrollGoal extends EntityAIWander {

        private final EntityRaccoon raccoon;

        RaccoonStrollGoal(EntityRaccoon raccoon, int chance) {
            super(raccoon, 1.0D, chance);
            this.raccoon = raccoon;
        }

        @Override
        public boolean shouldExecute() {
            return !raccoon.world.isDaytime() && super.shouldExecute() && canStroll();
        }

        @Override
        public boolean shouldContinueExecuting() {
            return super.shouldContinueExecuting() && canStroll();
        }

        private boolean canStroll() {
            return !raccoon.isWashing() && !raccoon.isSitting() && raccoon.getAttackTarget() == null;
        }
    }

    /**
     * Forge 1.12.2 port of 1.16 {@code AIStealFromVillagers}.
     */
    private static class AIStealFromVillagers extends EntityAIBase {

        private final EntityRaccoon raccoon;
        private EntityVillager target;
        private int golemCheckTime;
        private int cooldown;
        private int fleeTime;

        private AIStealFromVillagers(EntityRaccoon raccoon) {
            this.raccoon = raccoon;
            this.setMutexBits(3);
        }

        @Override
        public boolean shouldExecute() {
            if (cooldown > 0) {
                cooldown--;
                return false;
            } else if (raccoon != null && raccoon.stealCooldown == 0 && raccoon.getHeldItemMainhand() != null && raccoon.getHeldItemMainhand().isEmpty()) {
                EntityVillager villager = getNearbyVillagers();
                if (!isGolemNearby() && villager != null) {
                    target = villager;
                }
                cooldown = 150;
                return target != null;
            }
            return false;
        }

        @Override
        public boolean shouldContinueExecuting() {
            return target != null && raccoon != null;
        }

        @Override
        public void resetTask() {
            target = null;
            cooldown = 200 + raccoon.getRNG().nextInt(200);
            golemCheckTime = 0;
            fleeTime = 0;
        }

        @Override
        public void updateTask() {
            if (target != null) {
                golemCheckTime++;
                if (fleeTime > 0) {
                    fleeTime--;
                    if (raccoon.getNavigator().noPath()) {
                        Vec3d fleevec = RandomPositionGenerator.findRandomTargetBlockAwayFrom(raccoon, 16, 7, new Vec3d(raccoon.posX, raccoon.posY, raccoon.posZ));
                        if (fleevec != null) {
                            raccoon.getNavigator().tryMoveToXYZ(fleevec.x, fleevec.y, fleevec.z, 1.3D);
                        }
                    }
                    if (fleeTime == 0) {
                        resetTask();
                    }
                } else {
                    raccoon.getNavigator().tryMoveToEntityLiving(target, 1.0D);
                    if (raccoon.getDistance(target) < 1.7F) {
                        raccoon.setStanding(true);
                        raccoon.maxStandTime = 15;
                        MerchantRecipeList offers = target.getRecipes((EntityPlayer) null);
                        if (offers.isEmpty()) {
                            resetTask();
                        } else {
                            MerchantRecipe offer = offers.get(offers.size() <= 1 ? 0 : raccoon.getRNG().nextInt(offers.size() - 1));
                            if (offer != null) {
                                ItemStack stealStack = offer.getItemToSell().getItem() == Items.EMERALD ? offer.getItemToBuy() : offer.getItemToSell();
                                if (stealStack.isEmpty()) {
                                    resetTask();
                                } else {
                                    offer.incrementToolUses();
                                    ItemStack copy = stealStack.copy();
                                    copy.setCount(1);
                                    raccoon.setHeldItem(EnumHand.MAIN_HAND, copy);
                                    fleeTime = 60 + raccoon.getRNG().nextInt(60);
                                    raccoon.getNavigator().clearPath();
                                    raccoon.lookForWaterBeforeEatingTimer = 120 + raccoon.getRNG().nextInt(60);
                                    target.attackEntityFrom(DamageSource.causeMobDamage(raccoon), target.getHealth() <= 2 ? 0 : 1);
                                    raccoon.stealCooldown = 24000 + raccoon.getRNG().nextInt(48000);
                                }
                            }
                        }
                    }
                    if (golemCheckTime % 30 == 0 && raccoon.getRNG().nextBoolean() && isGolemNearby()) {
                        resetTask();
                    }
                }
            }
        }

        private boolean isGolemNearby() {
            List<EntityIronGolem> list = raccoon.world.getEntitiesWithinAABB(EntityIronGolem.class, raccoon.getEntityBoundingBox().grow(25.0D));
            return !list.isEmpty();
        }

        @Nullable
        private EntityVillager getNearbyVillagers() {
            List<EntityVillager> list = raccoon.world.getEntitiesWithinAABB(EntityVillager.class, raccoon.getEntityBoundingBox().grow(20.0D));
            double best = 10000;
            EntityVillager closest = null;
            for (EntityVillager villager : list) {
                if (villager.getHealth() > 2.0F) {
                    MerchantRecipeList recipes = villager.getRecipes((EntityPlayer) null);
                    if (!recipes.isEmpty() && raccoon.getDistanceSq(villager) < best) {
                        closest = villager;
                        best = raccoon.getDistanceSq(villager);
                    }
                }
            }
            return closest;
        }
    }
}
