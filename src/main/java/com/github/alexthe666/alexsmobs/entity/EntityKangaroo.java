package com.github.alexthe666.alexsmobs.entity;
import com.github.alexthe666.alexsmobs.misc.AMLootTables;

import com.github.alexthe666.alexsmobs.AlexsMobs;
import com.github.alexthe666.alexsmobs.config.AMConfig;
import com.github.alexthe666.alexsmobs.entity.ai.*;
import com.github.alexthe666.alexsmobs.message.MessageKangarooEat;
import com.github.alexthe666.alexsmobs.message.MessageKangarooInventorySync;
import com.github.alexthe666.alexsmobs.misc.AMSoundRegistry;
import com.github.alexthe666.alexsmobs.misc.AMTagRegistry;
import com.github.alexthe666.citadel.animation.Animation;
import com.github.alexthe666.citadel.animation.IAnimatedEntity;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.*;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import net.minecraft.util.ResourceLocation;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class EntityKangaroo extends EntityTameable implements IInventoryChangedListener, IAnimatedEntity, IFollower {

    public static final Animation ANIMATION_EAT_GRASS = Animation.create(30);
    public static final Animation ANIMATION_KICK = Animation.create(15);
    public static final Animation ANIMATION_PUNCH_R = Animation.create(13);
    public static final Animation ANIMATION_PUNCH_L = Animation.create(13);
    private static final DataParameter<Boolean> STANDING = EntityDataManager.createKey(EntityKangaroo.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> SITTING = EntityDataManager.createKey(EntityKangaroo.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> COMMAND = EntityDataManager.createKey(EntityKangaroo.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> VISUAL_FLAG = EntityDataManager.createKey(EntityKangaroo.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> POUCH_TICK = EntityDataManager.createKey(EntityKangaroo.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> HELMET_INDEX = EntityDataManager.createKey(EntityKangaroo.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> SWORD_INDEX = EntityDataManager.createKey(EntityKangaroo.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> CHEST_INDEX = EntityDataManager.createKey(EntityKangaroo.class, DataSerializers.VARINT);
    private static final DataParameter<Boolean> FORCED_SIT = EntityDataManager.createKey(EntityKangaroo.class, DataSerializers.BOOLEAN);
    public float prevPouchProgress;
    public float pouchProgress;
    public float sitProgress;
    public float prevSitProgress;
    public float standProgress;
    public float prevStandProgress;
    public float totalMovingProgress;
    public float prevTotalMovingProgress;
    public int maxStandTime = 75;
    public InventoryBasic kangarooInventory;
    private int animationTick;
    private Animation currentAnimation;
    private int jumpTicks;
    private int jumpDuration;
    private boolean wasOnGround;
    private int currentMoveTypeDuration;
    private int standingTime = 0;
    private int sittingTime = 0;
    private int maxSitTime = 75;
    private int eatCooldown = 0;
    private int carrotFeedings = 0;
    private int clientArmorCooldown = 0;

    public EntityKangaroo(World world) {
        super(world);
        this.setSize(1.65F, 1.5F);
        initKangarooInventory();
        this.jumpHelper = new EntityKangaroo.JumpHelperController(this);
        this.moveHelper = new EntityKangaroo.MoveHelperController(this);
    }

    public static boolean canKangarooSpawn(World world, BlockPos pos) {
        return AMTagRegistry.blockInTag(AMTagRegistry.KANGAROO_SPAWNS, world.getBlockState(pos.down()).getBlock()) && world.getLight(pos) > 8;
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        AMEntityRegistry.registerAttributeIfAbsent(this, SharedMonsterAttributes.ATTACK_DAMAGE);
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(22.0D);
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(32.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.5D);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(4.0D);
    }

    @Override
    protected void updateLeashedState() {
        super.updateLeashedState();
        Entity lvt_1_1_ = this.getLeashHolder();
        if (lvt_1_1_ != null && lvt_1_1_.world == this.world) {
            this.setHomePosAndDistance(lvt_1_1_.getPosition(), 5);
            float lvt_2_1_ = this.getDistance(lvt_1_1_);
            if (this.isSitting()) {
                if (lvt_2_1_ > 10.0F) {
                    this.clearLeashed(true, true);
                }

                return;
            }

            this.onLeashDistance(lvt_2_1_);
            if (lvt_2_1_ > 10.0F) {
                this.clearLeashed(true, true);
                this.getNavigator().clearPath();
            } else if (lvt_2_1_ > 6.0F) {
                double lvt_3_1_ = (lvt_1_1_.posX - this.posX) / (double) lvt_2_1_;
                double lvt_5_1_ = (lvt_1_1_.posY - this.posY) / (double) lvt_2_1_;
                double lvt_7_1_ = (lvt_1_1_.posZ - this.posZ) / (double) lvt_2_1_;
                this.motionX += Math.copySign(lvt_3_1_ * lvt_3_1_ * 0.4D, lvt_3_1_);
                this.motionY += Math.copySign(lvt_5_1_ * lvt_5_1_ * 0.4D, lvt_5_1_);
                this.motionZ += Math.copySign(lvt_7_1_ * lvt_7_1_ * 0.4D, lvt_7_1_);
                this.getNavigator().clearPath();
            } else {
                try {
                    Vec3d lvt_4_1_ = (new Vec3d(lvt_1_1_.posX - this.posX, lvt_1_1_.posY - this.posY, lvt_1_1_.posZ - this.posZ)).normalize().scale(Math.max(lvt_2_1_ - 2.0F, 0.0F));
                    this.getNavigator().tryMoveToXYZ(this.posX + lvt_4_1_.x, this.posY + lvt_4_1_.y, this.posZ + lvt_4_1_.z, this.followLeashSpeed());
                } catch (Exception e) {
                }
            }
        }
    }

    public boolean forcedSit() {
        return dataManager.get(FORCED_SIT);
    }

    public boolean isRoger() {
        String s = net.minecraft.util.text.TextFormatting.getTextWithoutFormattingCodes(this.getName());
        return s != null && s.toLowerCase().equals("roger");
    }

    @Override
    public boolean getCanSpawnHere() {
        return AMEntityRegistry.rollSpawn(AMConfig.kangarooSpawnRolls, this.getRNG(), AMEntityRegistry.AMSpawnReason.OTHER)
                && AMEntityRegistry.canLandSpawnWithoutGrass(this);
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return AMSoundRegistry.KANGAROO_IDLE;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return AMSoundRegistry.KANGAROO_IDLE;
    }
    @Override
    @Nullable
    protected ResourceLocation getLootTable() {
        return AMLootTables.KANGAROO;
    }

    private void initKangarooInventory() {
        IInventory animalchest = this.kangarooInventory;
        this.kangarooInventory = new InventoryBasic("KangarooPouch", false, 9) {
            @Override
            public void closeInventory(EntityPlayer player) {
                EntityKangaroo.this.dataManager.set(POUCH_TICK, 10);
                EntityKangaroo.this.resetKangarooSlots();
            }
        };
        this.kangarooInventory.addInventoryChangeListener(this);
        if (animalchest != null) {
            int i = Math.min(animalchest.getSizeInventory(), this.kangarooInventory.getSizeInventory());
            for (int j = 0; j < i; ++j) {
                ItemStack itemstack = animalchest.getStackInSlot(j);
                if (!itemstack.isEmpty()) {
                    this.kangarooInventory.setInventorySlotContents(j, itemstack.copy());
                }
            }
            resetKangarooSlots();
        }

    }


    @Override
    protected void dropEquipment(boolean wasRecentlyHit, int looting) {
        super.dropEquipment(wasRecentlyHit, looting);
        for (int i = 0; i < kangarooInventory.getSizeInventory(); i++) {
            this.entityDropItem(kangarooInventory.getStackInSlot(i), 0.0F);
        }
        kangarooInventory.clear();
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        ItemStack itemstack = player.getHeldItem(hand);
        Item item = itemstack.getItem();
        if (isBreedingItem(itemstack)) {
            super.processInteract(player, hand);
            return true;
        }
        boolean type = super.processInteract(player, hand);
        if (!isTamed() && item == Items.CARROT) {
            if (!player.capabilities.isCreativeMode) {
                itemstack.shrink(1);
            }
            this.playSound(SoundEvents.ENTITY_HORSE_EAT, this.getSoundVolume(), this.getSoundPitch());
            carrotFeedings++;
            if (carrotFeedings > 10 && getRNG().nextInt(2) == 0 || carrotFeedings > 15) {
                this.setTamedBy(player);
                this.world.setEntityState(this, (byte) 7);
            } else {
                this.world.setEntityState(this, (byte) 6);
            }
            return true;
        }
        if (isTamed() && this.getHealth() < this.getMaxHealth() && item instanceof ItemFood && !((ItemFood) item).isWolfsFavoriteMeat()) {
            if (!player.capabilities.isCreativeMode) {
                itemstack.shrink(1);
            }
            this.playSound(SoundEvents.ENTITY_HORSE_EAT, this.getSoundVolume(), this.getSoundPitch());
            this.heal(((ItemFood) item).getHealAmount(itemstack));
            return true;
        }
        if (!type && isTamed() && isOwner(player) && !isBreedingItem(itemstack)) {
            if (player.isSneaking()) {
                if (!this.isChild()) {
                    this.openGUI(player);
                    this.removePassengers();
                    this.dataManager.set(POUCH_TICK, -1);
                }
                return true;
            } else {
                this.setCommand(this.getCommand() + 1);
                if (this.getCommand() == 3) {
                    this.setCommand(0);
                }
                player.sendStatusMessage(new TextComponentTranslation("entity.alexsmobs.all.command_" + this.getCommand(), this.getName()), true);
                boolean sit = this.getCommand() == 2;
                if (sit) {
                    this.dataManager.set(FORCED_SIT, true);
                    this.setSitting(true);
                    return true;
                } else {
                    this.dataManager.set(FORCED_SIT, false);
                    maxSitTime = 0;
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
        compound.setBoolean("KangarooSitting", this.isSitting());
        compound.setBoolean("KangarooSittingForced", this.forcedSit());
        compound.setBoolean("Standing", this.isStanding());
        compound.setInteger("Command", this.getCommand());
        compound.setInteger("HelmetInvIndex", this.dataManager.get(HELMET_INDEX));
        compound.setInteger("SwordInvIndex", this.dataManager.get(SWORD_INDEX));
        compound.setInteger("ChestInvIndex", this.dataManager.get(CHEST_INDEX));
        if (kangarooInventory != null) {
            NBTTagList nbttaglist = new NBTTagList();
            for (int i = 0; i < this.kangarooInventory.getSizeInventory(); ++i) {
                ItemStack itemstack = this.kangarooInventory.getStackInSlot(i);
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
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.setSitting(compound.getBoolean("KangarooSitting"));
        this.dataManager.set(FORCED_SIT, compound.getBoolean("KangarooSittingForced"));
        this.setStanding(compound.getBoolean("Standing"));
        this.setCommand(compound.getInteger("Command"));
        this.dataManager.set(HELMET_INDEX, compound.getInteger("HelmetInvIndex"));
        this.dataManager.set(SWORD_INDEX, compound.getInteger("SwordInvIndex"));
        this.dataManager.set(CHEST_INDEX, compound.getInteger("ChestInvIndex"));
        if (kangarooInventory != null) {
            NBTTagList nbttaglist = compound.getTagList("Items", 10);
            this.initKangarooInventory();
            for (int i = 0; i < nbttaglist.tagCount(); ++i) {
                NBTTagCompound slotTag = nbttaglist.getCompoundTagAt(i);
                int j = slotTag.getByte("Slot") & 255;
                this.kangarooInventory.setInventorySlotContents(j, new ItemStack(slotTag));
            }
        } else {
            NBTTagList nbttaglist = compound.getTagList("Items", 10);
            this.initKangarooInventory();
            for (int i = 0; i < nbttaglist.tagCount(); ++i) {
                NBTTagCompound slotTag = nbttaglist.getCompoundTagAt(i);
                int j = slotTag.getByte("Slot") & 255;
                this.initKangarooInventory();
                this.kangarooInventory.setInventorySlotContents(j, new ItemStack(slotTag));
            }
        }
        resetKangarooSlots();
    }

    public void openGUI(EntityPlayer player) {
        if (!this.world.isRemote && !this.getPassengers().contains(player) && player instanceof EntityPlayerMP) {
            ((EntityPlayerMP) player).displayGUIChest(this.kangarooInventory);
        }
    }

    public boolean isSitting() {
        return this.dataManager.get(SITTING).booleanValue();
    }

    public void setSitting(boolean sit) {
        this.dataManager.set(SITTING, Boolean.valueOf(sit));
    }

    public boolean isStanding() {
        return this.dataManager.get(STANDING).booleanValue();
    }

    public void setStanding(boolean standing) {
        this.dataManager.set(STANDING, Boolean.valueOf(standing));
    }

    public int getCommand() {
        return this.dataManager.get(COMMAND).intValue();
    }

    public void setCommand(int command) {
        this.dataManager.set(COMMAND, Integer.valueOf(command));
    }

    public int getVisualFlag() {
        return this.dataManager.get(VISUAL_FLAG).intValue();
    }

    public void setVisualFlag(int visualFlag) {
        this.dataManager.set(VISUAL_FLAG, Integer.valueOf(visualFlag));
    }


    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(STANDING, Boolean.valueOf(false));
        this.dataManager.register(SITTING, Boolean.valueOf(false));
        this.dataManager.register(FORCED_SIT, Boolean.valueOf(false));
        this.dataManager.register(COMMAND, Integer.valueOf(0));
        this.dataManager.register(VISUAL_FLAG, Integer.valueOf(0));
        this.dataManager.register(POUCH_TICK, Integer.valueOf(0));
        this.dataManager.register(CHEST_INDEX, Integer.valueOf(-1));
        this.dataManager.register(HELMET_INDEX, Integer.valueOf(-1));
        this.dataManager.register(SWORD_INDEX, Integer.valueOf(-1));
    }

    @Override
    protected PathNavigate createNavigator(World worldIn) {
        return new GroundPathNavigatorWide(this, worldIn, 2F);
    }

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(0, new EntityAISit(this));
        this.tasks.addTask(1, new KangarooAIMelee(this, 1.2D, false));
        this.tasks.addTask(2, new EntityAISwimming(this));
        this.tasks.addTask(2, new TameableAIFollowOwner(this, 1.2D, 5.0F, 2.0F, false));
        this.tasks.addTask(3, new EntityAIMate(this, 1.0D));
        this.tasks.addTask(4, new AnimalAIRideParent(this, 1.25D));
        this.tasks.addTask(4, new EntityAITempt(this, 1.2D, Items.CARROT, false));
        this.tasks.addTask(5, new AnimalAIWanderRanged(this, 110, 1.2D, 10, 7));
        this.tasks.addTask(6, new EntityAIWatchClosest(this, EntityPlayer.class, 10.0F));
        this.tasks.addTask(7, new EntityAILookIdle(this));
        this.targetTasks.addTask(1, new EntityAIOwnerHurtByTarget(this));
        this.targetTasks.addTask(2, new EntityAIOwnerHurtTarget(this));
        this.targetTasks.addTask(3, new AnimalAIHurtByTargetNotBaby(this));
    }

    @Override
    public boolean canBeRidden(Entity passenger) {
        return super.canBeRidden(passenger) && this.dataManager.get(POUCH_TICK) == 0;
    }


    public double getMountedYOffset() {
        return (double) this.height * 0.35F;
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        boolean moving = this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ * this.motionZ > 0.03D;
        int pouchTick = this.dataManager.get(POUCH_TICK);
        this.prevTotalMovingProgress = totalMovingProgress;
        this.prevPouchProgress = pouchProgress;
        this.prevSitProgress = sitProgress;
        this.prevStandProgress = standProgress;
        if (this.isSitting() && sitProgress < 5) {
            sitProgress += 1;
        }
        if (eatCooldown > 0) {
            eatCooldown--;
        }
        if (!this.isSitting() && sitProgress > 0) {
            sitProgress -= 1;
        }
        if (this.isStanding() && standProgress < 5) {
            standProgress += 1;
        }
        if (!this.isStanding() && standProgress > 0) {
            standProgress -= 1;
        }
        if (moving && totalMovingProgress < 5) {
            totalMovingProgress += 1;
        }
        if (!moving && totalMovingProgress > 0) {
            totalMovingProgress -= 1;
        }
        if (pouchTick != 0 && pouchProgress < 5) {
            pouchProgress += 1;
        }
        if (pouchTick == 0 && pouchProgress > 0) {
            pouchProgress -= 1;
        }
        if (pouchTick > 0) {
            this.dataManager.set(POUCH_TICK, pouchTick - 1);
        }
        if (isStanding() && ++standingTime > maxStandTime) {
            this.setStanding(false);
            standingTime = 0;
            maxStandTime = 75 + rand.nextInt(50);
        }
        if (isSitting() && !forcedSit() && ++sittingTime > maxSitTime) {
            this.setSitting(false);
            sittingTime = 0;
            maxSitTime = 75 + rand.nextInt(50);
        }
        if (!world.isRemote && this.getAnimation() == NO_ANIMATION && this.getCommand() != 1 && !this.isStanding() && !this.isSitting() && rand.nextInt(1500) == 0) {
            maxSitTime = 500 + rand.nextInt(350);
            this.setSitting(true);
        }
        if (!forcedSit() && this.isSitting() && (this.getAttackTarget() != null || this.isStanding())) {
            this.setSitting(false);
        }
        if (this.getAnimation() == NO_ANIMATION && !this.isStanding() && !this.isSitting() && rand.nextInt(1500) == 0) {
            maxStandTime = 75 + rand.nextInt(50);
            this.setStanding(true);
        }
        if (this.forcedSit() && !this.isBeingRidden() && this.isTamed()) {
            this.setSitting(true);
        }
        if (!world.isRemote) {
            if (ticksExisted == 1) {
                updateClientInventory();
            }

            if (!moving && this.getAnimation() == NO_ANIMATION && !this.isSitting() && !this.isStanding()) {
                if ((getRNG().nextInt(180) == 0 || this.getHealth() < this.getMaxHealth() && getRNG().nextInt(40) == 0) && world.getBlockState(this.getPosition().down()).getBlock() == Blocks.GRASS) {
                    this.setAnimation(ANIMATION_EAT_GRASS);
                }
            }
            if (this.getAnimation() == ANIMATION_EAT_GRASS && this.getAnimationTick() == 20 && this.getHealth() < this.getMaxHealth() && world.getBlockState(this.getPosition().down()).getBlock() == Blocks.GRASS) {
                this.heal(6);
                this.world.playEvent(2001, getPosition().down(), Block.getStateId(Blocks.GRASS.getDefaultState()));
                this.world.setBlockState(getPosition().down(), Blocks.DIRT.getDefaultState(), 2);
            }
            if (this.getHealth() < this.getMaxHealth() && this.isTamed() && eatCooldown == 0) {
                eatCooldown = 20 + rand.nextInt(40);
                if (!this.kangarooInventory.isEmpty()) {
                    ItemStack foodStack = ItemStack.EMPTY;
                    for (int i = 0; i < this.kangarooInventory.getSizeInventory(); i++) {
                        ItemStack stack = this.kangarooInventory.getStackInSlot(i);
                        if (stack.getItem() instanceof ItemFood && !((ItemFood) stack.getItem()).isWolfsFavoriteMeat()) {
                            foodStack = stack;
                        }
                    }
                    if (!foodStack.isEmpty() && foodStack.getItem() instanceof ItemFood) {
                        AlexsMobs.sendMSGToAll(new MessageKangarooEat(this.getEntityId(), foodStack));
                        this.heal(((ItemFood) foodStack.getItem()).getHealAmount(foodStack) * 2);
                        foodStack.shrink(1);
                        this.playSound(SoundEvents.ENTITY_GENERIC_EAT, this.getSoundVolume(), this.getSoundPitch());
                    }
                }
            }
        }
        if (this.jumpTicks < this.jumpDuration) {
            ++this.jumpTicks;
        } else if (this.jumpDuration != 0) {
            this.jumpTicks = 0;
            this.jumpDuration = 0;
            this.setJumping(false);
        }
        EntityLivingBase attackTarget = this.getAttackTarget();
        if (!world.isRemote && attackTarget != null && this.canEntityBeSeen(attackTarget)) {
            if (getDistance(attackTarget) < attackTarget.width + this.width + 1) {
                if (this.getAnimation() == ANIMATION_KICK && this.getAnimationTick() == 8) {
                    attackTarget.knockBack(this, 1.3F, MathHelper.sin(this.rotationYaw * ((float) Math.PI / 180F)), -MathHelper.cos(this.rotationYaw * ((float) Math.PI / 180F)));
                    this.attackEntityAsMob(this.getAttackTarget());
                }
                if ((this.getAnimation() == ANIMATION_PUNCH_L) && this.getAnimationTick() == 6) {
                    float rot = rotationYaw + 90;
                    attackTarget.knockBack(this, 0.85F, MathHelper.sin(rot * ((float) Math.PI / 180F)), -MathHelper.cos(rot * ((float) Math.PI / 180F)));
                    this.attackEntityAsMob(this.getAttackTarget());
                }
                if ((this.getAnimation() == ANIMATION_PUNCH_R) && this.getAnimationTick() == 6) {
                    float rot = rotationYaw - 90;
                    attackTarget.knockBack(this, 0.85F, MathHelper.sin(rot * ((float) Math.PI / 180F)), -MathHelper.cos(rot * ((float) Math.PI / 180F)));
                    this.attackEntityAsMob(this.getAttackTarget());
                }
            }
        }
        if (attackTarget != null && this.canEntityBeSeen(attackTarget)) {
            this.getLookHelper().setLookPositionWithEntity(attackTarget, 360.0F, 360.0F);
        }
        if (this.isChild() && attackTarget != null) {
            this.setAttackTarget(null);
        }
        if (this.isBeingRidden()) {
            this.dataManager.set(POUCH_TICK, 10);
            this.setStanding(true);
            maxStandTime = 25;
        }
        if (this.isChild() && this.isRiding() && this.getRidingEntity() instanceof EntityKangaroo) {
            EntityKangaroo mount = (EntityKangaroo) this.getRidingEntity();
            this.rotationYaw = mount.renderYawOffset;
            this.rotationYawHead = mount.renderYawOffset;
            this.renderYawOffset = mount.renderYawOffset;
        }
        if (this.isRiding() && this.getRidingEntity() instanceof EntityKangaroo && !this.isChild()) {
            this.dismountRidingEntity();
        }
        if (clientArmorCooldown > 0) {
            clientArmorCooldown--;
        }
        if (ticksExisted > 5 && !world.isRemote && clientArmorCooldown == 0 && this.isTamed()) {
            this.updateClientInventory();
            clientArmorCooldown = 20;
        }
        AMEntityRegistry.updateAnimations(this);
    }

    @Override
    public boolean attackEntityAsMob(Entity entityIn) {
        float damage = (float) this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
        ItemStack weapon = this.getHeldItemMainhand();
        if (entityIn instanceof EntityLivingBase) {
            damage += EnchantmentHelper.getModifierForCreature(weapon, ((EntityLivingBase) entityIn).getCreatureAttribute());
        }
        damage += (float) getDamageForItem(weapon);
        boolean flag = entityIn.attackEntityFrom(DamageSource.causeMobDamage(this), damage);
        if (flag) {
            this.applyEnchantments(this, entityIn);
            if (!weapon.isEmpty()) {
                damageItem(weapon);
            }
        }
        return flag;
    }

    @Override
    public boolean attackEntityFrom(DamageSource src, float amount) {
        boolean prev = super.attackEntityFrom(src, amount);
        if (prev) {
            if (!this.getItemStackFromSlot(EntityEquipmentSlot.HEAD).isEmpty()) {
                damageItem(this.getItemStackFromSlot(EntityEquipmentSlot.HEAD));
            }
            if (!this.getItemStackFromSlot(EntityEquipmentSlot.CHEST).isEmpty()) {
                damageItem(this.getItemStackFromSlot(EntityEquipmentSlot.CHEST));
            }
        }
        return prev;
    }

    private void damageItem(ItemStack stack) {
        if (stack != null) {
            stack.damageItem(1, this);
            if (stack.isEmpty()) {
                stack.shrink(1);
            }
        }
    }

    @Override
    public boolean isEntityInvulnerable(DamageSource source) {
        return super.isEntityInvulnerable(source) || source == DamageSource.IN_WALL;
    }

    @Override
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

    public EntityMoveHelper getKangarooMoveHelper() {
        return this.moveHelper;
    }

    @Override
    public boolean canBeSteered() {
        return false;
    }

    @Override
    public void travel(float strafe, float vertical, float forward) {
        if (this.isSitting() || this.getAnimation() == ANIMATION_EAT_GRASS) {
            if (this.getNavigator().getPath() != null) {
                this.getNavigator().clearPath();
            }
            strafe = 0.0F;
            vertical = 0.0F;
            forward = 0.0F;
        }
        super.travel(strafe, vertical, forward);
    }


    private void checkLandingDelay() {
        this.updateMoveTypeDuration();
        this.disableJumpControl();
    }

    public PathNavigate getKangarooNavigator() {
        return this.navigator;
    }

    private void enableJumpControl() {
        if (jumpHelper instanceof EntityKangaroo.JumpHelperController) {
            ((EntityKangaroo.JumpHelperController) this.jumpHelper).setCanJump(true);
        }
    }

    private void disableJumpControl() {
        if (jumpHelper instanceof EntityKangaroo.JumpHelperController) {
            ((EntityKangaroo.JumpHelperController) this.jumpHelper).setCanJump(false);
        }
    }

    private void updateMoveTypeDuration() {
        if (this.moveHelper.getSpeed() < 2D) {
            this.currentMoveTypeDuration = 2;
        } else {
            this.currentMoveTypeDuration = 1;
        }

    }

    private void calculateRotationYaw(double x, double z) {
        this.rotationYaw = (float) (MathHelper.atan2(z - this.posZ, x - this.posX) * (double) (180F / (float) Math.PI)) - 90.0F;
    }

    public boolean canSpawnRunningEffectParticles() {
        return false;
    }

    @Override
    protected void updateAITasks() {
        super.updateAITasks();

        if (this.currentMoveTypeDuration > 0) {
            --this.currentMoveTypeDuration;
        }

        if (this.onGround) {
            if (!this.wasOnGround) {
                this.setJumping(false);
                this.checkLandingDelay();
            }

            if (this.currentMoveTypeDuration == 0) {
                EntityLivingBase livingentity = this.getAttackTarget();
                if (livingentity != null && this.getDistanceSq(livingentity) < 16.0D) {
                    this.calculateRotationYaw(livingentity.posX, livingentity.posZ);
                    this.moveHelper.setMoveTo(livingentity.posX, livingentity.posY, livingentity.posZ, this.moveHelper.getSpeed());
                    this.startJumping();
                    this.wasOnGround = true;
                }
            }
            if (this.jumpHelper instanceof EntityKangaroo.JumpHelperController) {
                EntityKangaroo.JumpHelperController rabbitController = (EntityKangaroo.JumpHelperController) this.jumpHelper;
                if (!rabbitController.getIsJumping()) {
                    if (this.moveHelper.isUpdating() && this.currentMoveTypeDuration == 0) {
                        Path path = this.navigator.getPath();
                        if (path != null && !path.isFinished()) {
                            this.startJumping();
                        }
                    }
                } else if (!rabbitController.canJump()) {
                    this.enableJumpControl();
                }
            }
        }

        this.wasOnGround = this.onGround;
    }

    public float getJumpCompletion(float partialTicks) {
        return this.jumpDuration == 0 ? 0.0F : ((float) this.jumpTicks + partialTicks) / (float) this.jumpDuration;
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
        if (animation == ANIMATION_KICK) {
            this.setStanding(true);
            maxStandTime = 30;
        } else if (animation == ANIMATION_PUNCH_R) {
            this.setStanding(true);
            maxStandTime = 15;
        } else if (animation == ANIMATION_PUNCH_L) {
            this.setStanding(true);
            maxStandTime = 15;
        }

    }

    @Override
    public Animation[] getAnimations() {
        return new Animation[]{ANIMATION_EAT_GRASS, ANIMATION_KICK, ANIMATION_PUNCH_L, ANIMATION_PUNCH_R};
    }

    @Nullable
    @Override
    public EntityAgeable createChild(EntityAgeable ageable) {
        EntityKangaroo child = new EntityKangaroo(this.world);
        UUID ownerId = this.getOwnerId();
        if (ownerId != null) {
            child.setOwnerId(ownerId);
            child.setTamed(true);
        }
        return child;
    }

    public void setMovementSpeed(double newSpeed) {
        this.setAIMoveSpeed((float) newSpeed);
        this.getNavigator().setSpeed(newSpeed);
    }

    @Override
    protected float getJumpUpwardsMotion() {
        return 0.5F;
    }

    @Override
    public void fall(float distance, float damageMultiplier) {
    }

    public boolean isBreedingItem(ItemStack stack) {
        return AMTagRegistry.isDeadBush(stack) || AMTagRegistry.isTallGrassPlant(stack);
    }

    /**
     * 1.12 {@code EntityTameable} blocks wild breeding. 1.16 kangaroos breed without being tamed.
     */
    @Override
    public boolean canMateWith(EntityAnimal otherAnimal) {
        return otherAnimal != this && otherAnimal.getClass() == this.getClass() && this.isInLove() && otherAnimal.isInLove();
    }

    public void resetKangarooSlots() {
        if (!world.isRemote) {
            int swordIndex = -1;
            double swordDamage = 0;
            int helmetIndex = -1;
            double helmetArmor = 0;
            int chestplateIndex = -1;
            double chestplateArmor = 0;
            for (int i = 0; i < this.kangarooInventory.getSizeInventory(); ++i) {
                ItemStack stack = this.kangarooInventory.getStackInSlot(i);
                if (!stack.isEmpty()) {
                    double dmg = getDamageForItem(stack);
                    if (dmg > 0 && dmg > swordDamage) {
                        swordDamage = dmg;
                        swordIndex = i;
                    }
                    if (stack.getItem().isValidArmor(stack, EntityEquipmentSlot.HEAD, this) && !this.isChild() && helmetIndex == -1) {
                        helmetIndex = i;
                    }
                    if (stack.getItem() instanceof ItemArmor && !this.isChild()) {
                        ItemArmor armorItem = (ItemArmor) stack.getItem();
                        if (armorItem.getEquipmentSlot() == EntityEquipmentSlot.HEAD) {
                            double prot = getProtectionForItem(stack, EntityEquipmentSlot.HEAD);
                            if (prot > 0 && prot > helmetArmor) {
                                helmetArmor = prot;
                                helmetIndex = i;
                            }
                        }
                        if (armorItem.getEquipmentSlot() == EntityEquipmentSlot.CHEST) {
                            double prot = getProtectionForItem(stack, EntityEquipmentSlot.CHEST);
                            if (prot > 0 && prot > chestplateArmor) {
                                chestplateArmor = prot;
                                chestplateIndex = i;
                            }
                        }
                    }
                }
            }
            this.dataManager.set(SWORD_INDEX, swordIndex);
            this.dataManager.set(CHEST_INDEX, chestplateIndex);
            this.dataManager.set(HELMET_INDEX, helmetIndex);
            updateClientInventory();
        }
    }

    private void updateClientInventory() {
        if (!world.isRemote) {
            for (int i = 0; i < 9; i++) {
                AlexsMobs.sendMSGToAll(new MessageKangarooInventorySync(this.getEntityId(), i, kangarooInventory.getStackInSlot(i)));
            }
        }
    }

    @Nullable
    private Map<EntityEquipmentSlot, ItemStack> func_241354_r_() {
        Map<EntityEquipmentSlot, ItemStack> map = null;

        for (EntityEquipmentSlot equipmentslottype : EntityEquipmentSlot.values()) {
            ItemStack itemstack;
            if (equipmentslottype.getSlotType() == EntityEquipmentSlot.Type.HAND) {
                itemstack = this.getItemInHand(equipmentslottype);
            } else if (equipmentslottype.getSlotType() == EntityEquipmentSlot.Type.ARMOR) {
                itemstack = this.getArmorInSlot(equipmentslottype);
            } else {
                continue;
            }

            ItemStack itemstack1 = this.getItemStackFromSlot(equipmentslottype);
            if (!ItemStack.areItemStacksEqual(itemstack1, itemstack)) {
                MinecraftForge.EVENT_BUS.post(new LivingEquipmentChangeEvent(this, equipmentslottype, itemstack, itemstack1));
                if (map == null) {
                    map = Maps.newEnumMap(EntityEquipmentSlot.class);
                }

                map.put(equipmentslottype, itemstack1);
                if (!itemstack.isEmpty()) {
                    this.getAttributeMap().removeAttributeModifiers(itemstack.getAttributeModifiers(equipmentslottype));
                }

                if (!itemstack1.isEmpty()) {
                    this.getAttributeMap().applyAttributeModifiers(itemstack1.getAttributeModifiers(equipmentslottype));
                }
            }
        }

        return map;
    }

    public ItemStack getItemStackFromSlot(EntityEquipmentSlot slotIn) {
        if (slotIn.getSlotType() == EntityEquipmentSlot.Type.HAND) {
            return getItemInHand(slotIn);
        } else if (slotIn.getSlotType() == EntityEquipmentSlot.Type.ARMOR) {
            return getArmorInSlot(slotIn);
        }
        return ItemStack.EMPTY;
    }

    private ItemStack getArmorInSlot(EntityEquipmentSlot slot) {
        int helmIndex = dataManager.get(HELMET_INDEX);
        int chestIndex = dataManager.get(CHEST_INDEX);
        return slot == EntityEquipmentSlot.HEAD && helmIndex >= 0 ? kangarooInventory.getStackInSlot(helmIndex) : slot == EntityEquipmentSlot.CHEST && chestIndex >= 0 ? kangarooInventory.getStackInSlot(chestIndex) : ItemStack.EMPTY;
    }

    private ItemStack getItemInHand(EntityEquipmentSlot slot) {
        int index = dataManager.get(SWORD_INDEX);
        return slot == EntityEquipmentSlot.MAINHAND && index >= 0 ? kangarooInventory.getStackInSlot(index) : ItemStack.EMPTY;
    }

    public double getDamageForItem(ItemStack itemStack) {
        Multimap<String, AttributeModifier> map = itemStack.getAttributeModifiers(EntityEquipmentSlot.MAINHAND);
        if (!map.isEmpty()) {
            double d = 0;
            for (AttributeModifier mod : map.get(SharedMonsterAttributes.ATTACK_DAMAGE.getName())) {
                d += mod.getAmount();
            }
            return d;
        }
        return 0;
    }

    public double getProtectionForItem(ItemStack itemStack, EntityEquipmentSlot type) {
        Multimap<String, AttributeModifier> map = itemStack.getAttributeModifiers(type);
        if (!map.isEmpty()) {
            double d = 0;
            for (AttributeModifier mod : map.get(SharedMonsterAttributes.ARMOR.getName())) {
                d += mod.getAmount();
            }
            return d;
        }
        return 0;
    }

    @Override
    protected void jump() {
        super.jump();
        double d0 = this.moveHelper.getSpeed();
        if (d0 > 0.0D) {
            double d1 = Math.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
            if (d1 < 0.01D) {
            }
        }

        if (!this.world.isRemote) {
            this.world.setEntityState(this, (byte) 1);
        }
    }

    public boolean hasJumper() {
        return jumpHelper instanceof JumpHelperController;
    }

    public void startJumping() {
        if (!this.isSitting() || this.isInWater()) {
            this.setJumping(true);
            this.jumpDuration = 16;
            this.jumpTicks = 0;
        }

    }

    @SideOnly(Side.CLIENT)
    @Override
    public void handleStatusUpdate(byte id) {
        if (id == 1) {
            this.spawnRunningParticles();
            this.jumpDuration = 16;
            this.jumpTicks = 0;
        } else {
            super.handleStatusUpdate(id);
        }
    }

    @Override
    public boolean shouldFollow() {
        return this.getCommand() == 1;
    }

    @Override
    public void onInventoryChanged(IInventory iInventory) {
        this.resetKangarooSlots();
    }

    static class MoveHelperController extends EntityMoveHelper {
        private final EntityKangaroo kangaroo;
        private double nextJumpSpeed;

        MoveHelperController(EntityKangaroo kangaroo) {
            super(kangaroo);
            this.kangaroo = kangaroo;
        }

        @Override
        public void onUpdateMoveHelper() {
            if (this.kangaroo.hasJumper() && this.kangaroo.onGround && !this.kangaroo.isJumping && !((EntityKangaroo.JumpHelperController) this.kangaroo.jumpHelper).getIsJumping()) {
                this.kangaroo.setMovementSpeed(0.0D);
            } else if (this.isUpdating()) {
                this.kangaroo.setMovementSpeed(this.nextJumpSpeed);
            }

            super.onUpdateMoveHelper();
        }

        @Override
        public void setMoveTo(double x, double y, double z, double speedIn) {
            if (this.kangaroo.isInWater()) {
                speedIn = 1.5D;
            }

            super.setMoveTo(x, y, z, speedIn);
            if (speedIn > 0.0D) {
                this.nextJumpSpeed = speedIn;
            }
        }
    }

    public class JumpHelperController extends EntityJumpHelper {
        private final EntityKangaroo kangaroo;
        private boolean canJump;

        public JumpHelperController(EntityKangaroo kangaroo) {
            super(kangaroo);
            this.kangaroo = kangaroo;
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
                this.kangaroo.startJumping();
                this.isJumping = false;
            }
        }
    }
}
