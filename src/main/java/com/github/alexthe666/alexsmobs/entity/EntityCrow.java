package com.github.alexthe666.alexsmobs.entity;



import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import com.github.alexthe666.alexsmobs.AlexsMobs;
import com.github.alexthe666.alexsmobs.config.AMConfig;
import com.github.alexthe666.alexsmobs.entity.ai.CreatureAITargetItems;
import com.github.alexthe666.alexsmobs.entity.ai.CrowAICircleCrops;
import com.github.alexthe666.alexsmobs.entity.ai.CrowAIFollowOwner;
import com.github.alexthe666.alexsmobs.entity.ai.CrowAIMelee;
import com.github.alexthe666.alexsmobs.entity.ai.DirectPathNavigator;
import com.github.alexthe666.alexsmobs.entity.ai.FlightMoveController;
import com.github.alexthe666.alexsmobs.message.MessageCrowDismount;
import com.github.alexthe666.alexsmobs.misc.AMSoundRegistry;
import com.github.alexthe666.alexsmobs.misc.AMTagRegistry;
import com.google.common.base.Predicate;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIMate;
import net.minecraft.entity.ai.EntityAIOwnerHurtByTarget;
import net.minecraft.entity.ai.EntityAIOwnerHurtTarget;
import net.minecraft.entity.ai.EntityAISit;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.init.Items;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraft.item.ItemFood;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import com.google.common.base.Optional;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class EntityCrow extends EntityTameable implements ITargetsDroppedItems {

    private static final DataParameter<Boolean> FLYING = EntityDataManager.createKey(EntityCrow.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> ATTACK_TICK = EntityDataManager.createKey(EntityCrow.class, DataSerializers.VARINT);
    private static final DataParameter<Boolean> SITTING = EntityDataManager.createKey(EntityCrow.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> COMMAND = EntityDataManager.createKey(EntityCrow.class, DataSerializers.VARINT);
    private static final DataParameter<Optional<BlockPos>> PERCH_POS = EntityDataManager.createKey(EntityCrow.class, DataSerializers.OPTIONAL_BLOCK_POS);
    public float prevFlyProgress;
    public float flyProgress;
    public float prevAttackProgress;
    public float attackProgress;
    public int fleePumpkinFlag = 0;
    public boolean aiItemFlag = false;
    public boolean aiItemFrameFlag = false;
    public float prevSitProgress;
    public float sitProgress;
    private boolean isLandNavigator;
    private int timeFlying = 0;
    @Nullable
    private String seedThrowerName;
    private int heldItemTime = 0;
    private int checkPerchCooldown = 0;
    private boolean gatheringClockwise = false;

    public EntityCrow(World worldIn) {
        super(worldIn);
        this.setPathPriority(PathNodeType.DANGER_FIRE, -1.0F);
        this.setPathPriority(PathNodeType.WATER, -1.0F);
        this.setPathPriority(PathNodeType.WATER, 16.0F);
        this.setPathPriority(PathNodeType.FENCE, -1.0F);
        switchNavigator(false);
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
        this.tasks.addTask(1, new EntityAISit(this));
        this.tasks.addTask(2, new CrowAIMelee(this));
        this.tasks.addTask(3, new CrowAIFollowOwner(this, 1.0D, 4.0F, 2.0F, true));
        this.tasks.addTask(4, new AIDepositChests());
        this.tasks.addTask(4, new AIScatter());
        this.tasks.addTask(5, new AIAvoidPumpkins());
        this.tasks.addTask(5, new EntityAIMate(this, 1.0D));
        this.tasks.addTask(6, new CrowAICircleCrops(this));
        this.tasks.addTask(7, new AIWalkIdle());
        this.tasks.addTask(8, new EntityAIWatchClosest(this, EntityPlayer.class, 6.0F));
        this.tasks.addTask(9, new EntityAIWatchClosest(this, EntityCreature.class, 6.0F));
        this.tasks.addTask(10, new EntityAILookIdle(this));
        this.targetTasks.addTask(1, new AITargetItems(false, false, 40, 16));
        this.targetTasks.addTask(2, new EntityAIOwnerHurtByTarget(this));
        this.targetTasks.addTask(3, new EntityAIOwnerHurtTarget(this));
        this.targetTasks.addTask(4, new EntityAIHurtByTarget(this, true, EntityPlayer.class));

    }


    @Override
    public boolean getCanSpawnHere() {
        return AMEntityRegistry.rollSpawn(AMConfig.crowSpawnRolls, this.getRNG(), AMEntityRegistry.AMSpawnReason.OTHER);
    }

    public boolean isOnSameTeam(Entity entityIn) {
        if (this.isTamed()) {
            EntityLivingBase EntityLivingBase = this.getOwner();
            if (entityIn == EntityLivingBase) {
                return true;
            }
            if (entityIn instanceof EntityTameable) {
                return ((EntityTameable) entityIn).isOwner(EntityLivingBase);
            }
            if (EntityLivingBase != null) {
                return EntityLivingBase.isOnSameTeam(entityIn);
            }
        }

        return super.isOnSameTeam(entityIn);
    }

    private void switchNavigator(boolean onLand) {
        if (onLand) {
            this.moveHelper = new net.minecraft.entity.ai.EntityMoveHelper(this);
            this.navigator = new PathNavigateGround(this, world);
            this.isLandNavigator = true;
        } else {
            this.moveHelper = new FlightMoveController(this, 0.7F, false);
            this.navigator = new DirectPathNavigator(this, world);
            this.isLandNavigator = false;
        }
    }

    public boolean canTrample(World world, Block block, BlockPos pos, float fallDistance) {
        return false;
    }

    @Override
    public void fall(float distance, float damageMultiplier) {
    }

    @Override
    protected void updateFallState(double y, boolean onGroundIn, IBlockState state, BlockPos pos) {
        this.fallDistance = 0.0F;
    }

    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (this.isEntityInvulnerable(source)) {
            return false;
        } else {
            Entity entity = source.getTrueSource();
            this.setSitting(false);
            if (entity != null && this.isTamed() && !(entity instanceof EntityPlayer) && !(entity instanceof EntityArrow)) {
                amount = (amount + 1.0F) / 4.0F;
            }
            boolean prev = super.attackEntityFrom(source, amount);
            if (prev) {
                if (!this.getHeldItemMainhand().isEmpty()) {
                    this.entityDropItem(this.getHeldItemMainhand().copy(), 0.0F);
                    this.setHeldItem(EnumHand.MAIN_HAND, ItemStack.EMPTY);
                }
            }
            return prev;
        }
    }

    public void updateRidden() {
        Entity entity = this.getRidingEntity();
        if (this.isBeingRidden() && !entity.isEntityAlive()) {
            this.dismountRidingEntity();
        } else if (isTamed() && entity instanceof EntityLivingBase && isOwner((EntityLivingBase) entity)) {
            this.motionX = 0.0D;
            this.motionY = 0.0D;
            this.motionZ = 0.0D;
            this.onLivingUpdate();
            Entity riding = this.getRidingEntity();
            if (this.isBeingRidden()) {
                int i = riding.getPassengers().indexOf(this);
                float radius = 0.43F;
                float angle = (0.01745329251F * (((EntityPlayer) riding).renderYawOffset + (i == 0 ? -90 : 90)));
                double extraX = radius * MathHelper.sin((float) (Math.PI + angle));
                double extraZ = radius * MathHelper.cos(angle);
                double extraY = (riding.isSneaking() ? 1.25D : 1.45D);
                this.rotationYawHead = ((EntityPlayer) riding).rotationYawHead;
                this.prevRotationYaw = ((EntityPlayer) riding).rotationYawHead;
                this.setPosition(riding.posX + extraX, riding.posY + extraY, riding.posZ + extraZ);
                if (!riding.isEntityAlive() || rideCooldown == 0 && riding.isSneaking() || ((EntityPlayer) riding).isElytraFlying() || this.getAttackTarget() != null && this.getAttackTarget().isEntityAlive()) {
                    this.dismountRidingEntity();
                    if (!world.isRemote) {
                        AlexsMobs.sendMSGToAll(new MessageCrowDismount(this.getEntityId(), riding.getEntityId()));
                    }
                }
            }
        }else{
            super.updateRidden();
        }
    }


    public int getRidingCrows(EntityLivingBase player) {
        int crowCount = 0;
        for (Entity e : player.getPassengers()) {
            if (e instanceof EntityCrow) {
                crowCount++;
            }
        }
        return crowCount;
    }

    public boolean isBreedingItem(ItemStack stack) {
        return stack.getItem() == Items.PUMPKIN_SEEDS && this.isTamed();
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        ItemStack itemstack = player.getHeldItem(hand);
        if (super.processInteract(player, hand)) {
            return true;
        }
        if (!this.getHeldItemMainhand().isEmpty()) {
            this.entityDropItem(this.getHeldItemMainhand().copy(), 0.0F);
            this.setHeldItem(EnumHand.MAIN_HAND, ItemStack.EMPTY);
            return true;
        }
        if (isTamed() && isOwner(player) && !isBreedingItem(itemstack)) {
            if (isCrowEdible(itemstack) && this.getHeldItemMainhand().isEmpty()) {
                ItemStack cop = itemstack.copy();
                cop.setCount(1);
                this.setHeldItem(EnumHand.MAIN_HAND, cop);
                if (!player.capabilities.isCreativeMode) {
                    itemstack.shrink(1);
                }
            }
            this.setCommand(this.getCommand() + 1);
            if (this.getCommand() == 4) {
                this.setCommand(0);
            }
            if (this.getCommand() == 3) {
                player.sendStatusMessage(new TextComponentTranslation("entity.alexsmobs.crow.command_3", this.getName()), true);
            } else {
                player.sendStatusMessage(new TextComponentTranslation("entity.alexsmobs.all.command_" + this.getCommand(), this.getName()), true);
            }
            boolean sit = this.getCommand() == 2;
            if (sit) {
                this.setSitting(true);
            } else {
                this.setSitting(false);
            }
            return true;
        }
        return false;
    }


    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        this.prevAttackProgress = attackProgress;
        prevFlyProgress = flyProgress;
        this.prevSitProgress = this.sitProgress;
        if ((this.isSitting() || this.isBeingRidden()) && sitProgress < 5) {
            sitProgress += 1;
        }
        if (!(this.isSitting() || this.isBeingRidden()) && sitProgress > 0) {
            sitProgress -= 1;
        }
        if (isFlying() && flyProgress < 5F) {
            flyProgress++;
        }
        if (!isFlying() && flyProgress > 0F) {
            flyProgress--;
        }
        if (fleePumpkinFlag > 0) {
            fleePumpkinFlag--;
        }
        if (!world.isRemote) {
            if (isFlying() && this.isLandNavigator) {
                switchNavigator(false);
            }
            if (!isFlying() && !this.isLandNavigator) {
                switchNavigator(true);
            }
            if (isFlying()) {
                timeFlying++;
                this.setNoGravity(true);
                if (this.isSitting() || this.isBeingRidden() || this.isInLove()) {
                    this.setFlying(false);
                }
            } else {
                timeFlying = 0;
                this.setNoGravity(false);
            }
        }
        if (!this.getHeldItemMainhand().isEmpty()) {
            heldItemTime++;
            if (heldItemTime > 60 && isCrowEdible(this.getHeldItemMainhand()) && (!this.isTamed() || this.getHealth() < this.getMaxHealth())) {
                heldItemTime = 0;
                this.heal(4);
                this.playSound(SoundEvents.ENTITY_PARROT_EAT, this.getSoundVolume(), this.getSoundPitch());
                if (this.getHeldItemMainhand().getItem() == Items.PUMPKIN_SEEDS && seedThrowerName != null && !this.isTamed()) {
                    if (getRNG().nextFloat() < 0.3F) {
                        this.setTamed(true);
                        this.setCommand(1);
                        EntityPlayer player = this.world.getPlayerEntityByName(seedThrowerName);
                        if (player != null) {
                            this.setOwnerId(player.getUniqueID());
                            if (player instanceof EntityPlayerMP) {
                                CriteriaTriggers.TAME_ANIMAL.trigger((EntityPlayerMP) player, this);
                            }
                        }
                        this.world.setEntityState(this, (byte) 7);
                    } else {
                        this.world.setEntityState(this, (byte) 6);
                    }
                }
                if (this.getHeldItemMainhand().getItem().hasContainerItem()) {
                    this.entityDropItem(new ItemStack(this.getHeldItemMainhand().getItem().getContainerItem()), 0.0F);
                }
                this.getHeldItemMainhand().shrink(1);
            }
        } else {
            heldItemTime = 0;
        }
        if (rideCooldown > 0) {
            rideCooldown--;
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
        if(checkPerchCooldown > 0){
            checkPerchCooldown--;
        }

        if(this.isTamed() && checkPerchCooldown == 0){
            checkPerchCooldown = 50;
            IBlockState below = this.world.getBlockState(this.getPosition().down());
            if(below.getBlock() == Blocks.HAY_BLOCK){
                this.heal(1);
                this.world.setEntityState(this, (byte) 67);
                this.setPerchPos(this.getPosition().down());
            }
        }
        if(this.getCommand() == 3 && isTamed() && getPerchPos() != null && checkPerchCooldown == 0){
            checkPerchCooldown = 120;
            IBlockState below = this.world.getBlockState(getPerchPos());
            if(below.getBlock() != Blocks.HAY_BLOCK){
                this.world.setEntityState(this, (byte) 68);
                this.setPerchPos(null);
                this.setCommand(2);
                this.setSitting(true);
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public void handleStatusUpdate(byte id) {
        if (id == 67) {
            for(int i = 0; i < 7; ++i) {
                double d0 = this.rand.nextGaussian() * 0.02D;
                double d1 = this.rand.nextGaussian() * 0.02D;
                double d2 = this.rand.nextGaussian() * 0.02D;
                this.world.spawnParticle(EnumParticleTypes.VILLAGER_HAPPY, this.posX + (this.rand.nextDouble() - 0.5D) * 1.0D, this.posY + this.rand.nextDouble() * this.height + 0.5D, this.posZ + (this.rand.nextDouble() - 0.5D) * 1.0D, d0, d1, d2);
            }
        } else if (id == 68) {
            for(int i = 0; i < 7; ++i) {
                double d0 = this.rand.nextGaussian() * 0.02D;
                double d1 = this.rand.nextGaussian() * 0.02D;
                double d2 = this.rand.nextGaussian() * 0.02D;
                this.world.spawnParticle(EnumParticleTypes.VILLAGER_ANGRY, this.posX + (this.rand.nextDouble() - 0.5D) * 1.0D, this.posY + this.rand.nextDouble() * this.height + 0.5D, this.posZ + (this.rand.nextDouble() - 0.5D) * 1.0D, d0, d1, d2);
            }
        } else {
            super.handleStatusUpdate(id);
        }
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setBoolean("Flying", this.isFlying());
        compound.setBoolean("MonkeySitting", this.isSitting());
        compound.setInteger("Command", this.getCommand());
        if (this.getPerchPos() != null) {
            compound.setInteger("PerchX", this.getPerchPos().getX());
            compound.setInteger("PerchY", this.getPerchPos().getY());
            compound.setInteger("PerchZ", this.getPerchPos().getZ());
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

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.setFlying(compound.getBoolean("Flying"));
        this.setSitting(compound.getBoolean("MonkeySitting"));
        this.setCommand(compound.getInteger("Command"));
        if (compound.hasKey("PerchX") && compound.hasKey("PerchY") && compound.hasKey("PerchZ")) {
            this.setPerchPos(new BlockPos(compound.getInteger("PerchX"), compound.getInteger("PerchY"), compound.getInteger("PerchZ")));
        }
    }

    public boolean isFlying() {
        return this.dataManager.get(FLYING);
    }

    public void setFlying(boolean flying) {
        if(flying && isChild()){
            return;
        }
        this.dataManager.set(FLYING, flying);
    }

    public int getCommand() {
        return this.dataManager.get(COMMAND).intValue();
    }

    public void setCommand(int command) {
        this.dataManager.set(COMMAND, Integer.valueOf(command));
    }

    public boolean isSitting() {
        return this.dataManager.get(SITTING).booleanValue();
    }

    public void setSitting(boolean sit) {
        this.dataManager.set(SITTING, Boolean.valueOf(sit));
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(FLYING, false);
        this.dataManager.register(ATTACK_TICK, 0);
        this.dataManager.register(COMMAND, Integer.valueOf(0));
        this.dataManager.register(SITTING, Boolean.valueOf(false));
        this.dataManager.register(PERCH_POS, Optional.absent());
    }

    @Override
    public boolean isEntityInvulnerable(DamageSource source) {
        return source == DamageSource.IN_WALL || source == DamageSource.FALLING_BLOCK || source == DamageSource.CACTUS || super.isEntityInvulnerable(source);
    }

    @Nullable
    @Override
    public EntityCrow createChild(EntityAgeable ageable) {
        EntityCrow child = new EntityCrow(this.world);
        UUID ownerId = this.getOwnerId();
        if (ownerId != null) {
            child.setOwnerId(ownerId);
            child.setTamed(true);
        }
        return child;
    }

    public boolean isTargetBlocked(Vec3d target) {
        Vec3d start = new Vec3d(this.posX, this.getPositionEyes(1.0F).y, this.posZ);
        RayTraceResult result = this.world.rayTraceBlocks(start, target, false, true, false);
        return result != null && result.typeOfHit != RayTraceResult.Type.MISS;
    }

    public int getTalkInterval() {
        return 60;
    }

    protected SoundEvent getAmbientSound() {
        return AMSoundRegistry.CROW_IDLE;
    }

    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return AMSoundRegistry.CROW_HURT;
    }

    protected SoundEvent getDeathSound() {
        return AMSoundRegistry.CROW_HURT;
    }

    public Vec3d getBlockInViewAway(Vec3d fleePos, float radiusAdd) {
        float radius = 0.75F * (0.7F * 6) * -3 - this.getRNG().nextInt(24) - radiusAdd;
        float neg = this.getRNG().nextBoolean() ? 1 : -1;
        float renderYawOffset = this.renderYawOffset;
        float angle = (0.01745329251F * renderYawOffset) + 3.15F + (this.getRNG().nextFloat() * neg);
        double extraX = radius * MathHelper.sin((float) (Math.PI + angle));
        double extraZ = radius * MathHelper.cos(angle);
        BlockPos radialPos = new BlockPos(fleePos.x + extraX, 0, fleePos.z + extraZ);
        BlockPos ground = getCrowGround(radialPos);
        int distFromGround = (int) this.posY - ground.getY();
        int flightHeight = 4 + this.getRNG().nextInt(10);
        BlockPos newPos = ground.up(distFromGround > 8 ? flightHeight : (int)this.getRNG().nextInt(6) + 1);
        Vec3d centerPos = new Vec3d(newPos.getX() + 0.5D, newPos.getY() + 0.5D, newPos.getZ() + 0.5D);
        if (!this.isTargetBlocked(centerPos) && this.getDistanceSq(centerPos.x, centerPos.y, centerPos.z) > 1.0D) {
            return centerPos;
        }
        return null;
    }


    private BlockPos getCrowGround(BlockPos in){
        BlockPos position = new BlockPos(in.getX(), this.posY, in.getZ());
        while (position.getY() > 2 && world.isAirBlock(position)) {
            position = position.down();
        }
        return position;
    }
    public Vec3d getBlockGrounding(Vec3d fleePos) {
        float radius = 0.75F * (0.7F * 6) * -3 - this.getRNG().nextInt(24);
        float neg = this.getRNG().nextBoolean() ? 1 : -1;
        float renderYawOffset = this.renderYawOffset;
        float angle = (0.01745329251F * renderYawOffset) + 3.15F + (this.getRNG().nextFloat() * neg);
        double extraX = radius * MathHelper.sin((float) (Math.PI + angle));
        double extraZ = radius * MathHelper.cos(angle);
        BlockPos radialPos = new BlockPos(fleePos.x + extraX, posY, fleePos.z + extraZ);
        BlockPos ground = this.getCrowGround(radialPos);
        if (ground.getY() == 0) {
            return this.getPositionVector();
        } else {
            ground = this.getPosition();
            while (ground.getY() > 2 && world.isAirBlock(ground)) {
                ground = ground.down();
            }
        }
        BlockPos up = ground.up();
        Vec3d centerGround = new Vec3d(up.getX() + 0.5D, up.getY() + 0.5D, up.getZ() + 0.5D);
        if (!this.isTargetBlocked(centerGround)) {
            return centerGround;
        }
        return null;
    }

    private boolean isCrowOverWater() {
        BlockPos position = this.getPosition();
        while (position.getY() > 2 && world.isAirBlock(position)) {
            position = position.down();
        }
        return world.getBlockState(position).getMaterial().isLiquid();
    }

    public void peck() {
        this.dataManager.set(ATTACK_TICK, 7);
    }

    @Override
    public boolean canTargetItem(ItemStack stack) {
        return isCrowEdible(stack) || this.isTamed();
    }

    private boolean isCrowEdible(ItemStack stack) {
        return stack.getItem() instanceof ItemFood || AMTagRegistry.itemInTag(AMTagRegistry.CROW_FOODSTUFFS, stack.getItem());
    }

    public double getMaxDistToItem() {
        return 1.0D;
    }

    @Override
    public void onGetItem(EntityItem e) {
        ItemStack duplicate = e.getItem().copy();
        duplicate.setCount(1);
        if (!this.getHeldItem(EnumHand.MAIN_HAND).isEmpty() && !this.world.isRemote) {
            this.entityDropItem(this.getHeldItem(EnumHand.MAIN_HAND), 0.0F);
        }
        this.setHeldItem(EnumHand.MAIN_HAND, duplicate);
        if (e.getItem().getItem() == Items.PUMPKIN_SEEDS && !this.isTamed()) {
            seedThrowerName = e.getThrower();
        } else {
            seedThrowerName = null;
        }
    }

    public BlockPos getPerchPos() {
        return this.dataManager.get(PERCH_POS).orNull();
    }

    public void setPerchPos(BlockPos pos) {
        this.dataManager.set(PERCH_POS, Optional.fromNullable(pos));
    }


    private class AIWalkIdle extends EntityAIBase {
        protected final EntityCrow crow;
        protected double x;
        protected double y;
        protected double z;
        private boolean flightTarget = false;

        public AIWalkIdle() {
            super();
            this.setMutexBits(1);
            this.crow = EntityCrow.this;
        }

        @Override
        public boolean shouldExecute() {
            if (this.crow.isBeingRidden() || EntityCrow.this.getCommand() == 1 || EntityCrow.this.aiItemFlag || (crow.getAttackTarget() != null && crow.getAttackTarget().isEntityAlive()) || this.crow.isBeingRidden() || this.crow.isSitting()) {
                return false;
            } else {
                if (this.crow.getRNG().nextInt(30) != 0 && !crow.isFlying()) {
                    return false;
                }
                if (this.crow.onGround) {
                    this.flightTarget = rand.nextBoolean();
                } else {
                    this.flightTarget = rand.nextInt(5) > 0 && crow.timeFlying < 200;
                }
                if(crow.getCommand() == 3){
                    if(crow.aiItemFrameFlag){
                        return false;
                    }
                    this.flightTarget = true;
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
            if (flightTarget) {
                crow.getMoveHelper().setMoveTo(x, y, z, 1F);
            } else {
                this.crow.getNavigator().tryMoveToXYZ(this.x, this.y, this.z, 1F);
            }
            if (!flightTarget && isFlying() && crow.onGround) {
                crow.setFlying(false);
            }
            if (isFlying() && crow.onGround && crow.timeFlying > 10) {
                crow.setFlying(false);
            }
        }

        @Nullable
        protected Vec3d getPosition() {
            Vec3d vector3d = crow.getPositionVector();
            if (crow.getCommand() == 3 && crow.getPerchPos() != null) {
                return crow.getGatheringVec(vector3d, 4 + rand.nextInt(2));
            }
            if(crow.isCrowOverWater()){
                flightTarget = true;
            }
            if (flightTarget) {
                if (crow.timeFlying < 50 || crow.isCrowOverWater()) {
                    return crow.getBlockInViewAway(vector3d, 0);
                } else {
                    return crow.getBlockGrounding(vector3d);
                }
            } else {

                return RandomPositionGenerator.findRandomTarget(this.crow, 10, 7);
            }
        }

        public boolean shouldContinueExecuting() {
            if (crow.aiItemFlag || crow.isSitting() || EntityCrow.this.getCommand() == 1) {
                return false;
            }
            if (flightTarget) {
                return crow.isFlying() && crow.getDistanceSq(x, y, z) > 2F;
            } else {
                return (!this.crow.getNavigator().noPath()) && !this.crow.isBeingRidden();
            }
        }

        public void startExecuting() {
            if (flightTarget) {
                crow.setFlying(true);
                crow.getMoveHelper().setMoveTo(x, y, z, 1F);
            } else {
                this.crow.getNavigator().tryMoveToXYZ(this.x, this.y, this.z, 1F);
            }
        }

        public void resetTask() {
            this.crow.getNavigator().clearPath();
            super.resetTask();
        }
    }

    private Vec3d getGatheringVec(Vec3d vector3d, float gatheringCircleDist) {
        float angle = (0.01745329251F * 8 * (gatheringClockwise ? -ticksExisted : ticksExisted));
        double extraX = gatheringCircleDist * MathHelper.sin((angle));
        double extraZ = gatheringCircleDist * MathHelper.cos(angle);
        if(this.getPerchPos() != null){
            Vec3d pos = new Vec3d(getPerchPos().getX() + extraX, getPerchPos().getY() + 2, getPerchPos().getZ() + extraZ);
            if (this.world.isAirBlock(new BlockPos(pos))) {
                return pos;
            }
        }
        return null;
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
            this.theNearestAttackableTargetSorter = new AIScatter.Sorter(EntityCrow.this);
            this.targetEntitySelector = new Predicate<Entity>() {
                @Override
                public boolean apply(@Nullable Entity e) {
                    return (e.isEntityAlive() && AMTagRegistry.entityMatchesEntityTypeTag(AMTagRegistry.SCATTERS_CROWS, e))
                            || (e instanceof EntityPlayer && !((EntityPlayer) e).capabilities.isCreativeMode);
                }
            };
        }

        @Override
        public boolean shouldExecute() {
            if (EntityCrow.this.isBeingRidden() || EntityCrow.this.aiItemFlag || EntityCrow.this.isBeingRidden() || EntityCrow.this.isTamed()) {
                return false;
            }
            if (!this.mustUpdate) {
                long worldTime = EntityCrow.this.world.getTotalWorldTime() % 10;
                if (EntityCrow.this.getIdleTime() >= 100 && worldTime != 0) {
                    return false;
                }
                if (EntityCrow.this.getRNG().nextInt(this.executionChance) != 0 && worldTime != 0) {
                    return false;
                }
            }
            List<Entity> list = EntityCrow.this.world.getEntitiesWithinAABB(Entity.class, this.getTargetableArea(this.getTargetDistance()), this.targetEntitySelector);
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
            return targetEntity != null && !EntityCrow.this.isTamed();
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
                EntityCrow.this.setFlying(true);
                EntityCrow.this.getMoveHelper().setMoveTo(flightTarget.x, flightTarget.y, flightTarget.z, 1F);
                if(cooldown == 0 && EntityCrow.this.isTargetBlocked(flightTarget)){
                    cooldown = 30;
                    flightTarget = null;
                }
            }

            if (targetEntity != null) {
                if (EntityCrow.this.onGround || flightTarget == null || flightTarget != null && EntityCrow.this.getDistanceSq(flightTarget.x, flightTarget.y, flightTarget.z) < 3) {
                    Vec3d vec = EntityCrow.this.getBlockInViewAway(targetEntity.getPositionVector(), 0);
                    if (vec != null && vec.y > EntityCrow.this.posY) {
                        flightTarget = vec;
                    }
                }
                if (EntityCrow.this.getDistance(targetEntity) > 20.0F) {
                    this.resetTask();
                }
            }
        }

        protected double getTargetDistance() {
            return 4D;
        }

        protected AxisAlignedBB getTargetableArea(double targetDistance) {
            Vec3d renderCenter = new Vec3d(EntityCrow.this.posX, EntityCrow.this.posY + 0.5, EntityCrow.this.posZ);
            AxisAlignedBB aabb = new AxisAlignedBB(-2, -2, -2, 2, 2, 2);
            return aabb.offset(renderCenter);
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

    private class AIAvoidPumpkins extends EntityAIBase {
        private final int searchLength;
        private final int field_203113_j;
        protected BlockPos destinationBlock;
        protected int runDelay = 70;
        private Vec3d flightTarget;

        private AIAvoidPumpkins() {
            searchLength = 20;
            field_203113_j = 1;
        }

        public boolean shouldContinueExecuting() {
            return destinationBlock != null && isPumpkin(EntityCrow.this.world, destinationBlock) && isCloseToPumpkin(16);
        }

        public boolean isCloseToPumpkin(double dist) {
            return destinationBlock == null || EntityCrow.this.getDistanceSq(destinationBlock.getX() + 0.5D, destinationBlock.getY() + 0.5D, destinationBlock.getZ() + 0.5D) < dist * dist;
        }

        @Override
        public boolean shouldExecute() {
            if (EntityCrow.this.isTamed()) {
                return false;
            }
            if (this.runDelay > 0) {
                --this.runDelay;
                return false;
            } else {
                this.runDelay = 70 + EntityCrow.this.rand.nextInt(150);
                return this.searchForDestination();
            }
        }

        public void startExecuting() {
            EntityCrow.this.fleePumpkinFlag = 200;
            Vec3d vec = EntityCrow.this.getBlockInViewAway(new Vec3d(destinationBlock.getX() + 0.5D, destinationBlock.getY() + 0.5D, destinationBlock.getZ() + 0.5D), 10);
            if (vec != null) {
                flightTarget = vec;
                EntityCrow.this.setFlying(true);
                EntityCrow.this.getMoveHelper().setMoveTo(vec.x, vec.y, vec.z, 1F);
            }
        }

        @Override
        public void updateTask() {
            if (this.isCloseToPumpkin(16)) {
                EntityCrow.this.fleePumpkinFlag = 200;
                if (flightTarget == null || EntityCrow.this.getDistanceSq(flightTarget.x, flightTarget.y, flightTarget.z) < 2F) {
                    Vec3d vec = EntityCrow.this.getBlockInViewAway(new Vec3d(destinationBlock.getX() + 0.5D, destinationBlock.getY() + 0.5D, destinationBlock.getZ() + 0.5D), 10);
                    if (vec != null) {
                        flightTarget = vec;
                        EntityCrow.this.setFlying(true);
                    }
                }
                if (flightTarget != null) {
                    EntityCrow.this.getMoveHelper().setMoveTo(flightTarget.x, flightTarget.y, flightTarget.z, 1F);
                }
            }
        }

        public void resetTask() {
            flightTarget = null;
        }

        protected boolean searchForDestination() {
            int lvt_1_1_ = this.searchLength;
            int lvt_2_1_ = this.field_203113_j;
            BlockPos lvt_3_1_ = EntityCrow.this.getPosition();
            BlockPos.MutableBlockPos lvt_4_1_ = new BlockPos.MutableBlockPos();

            for (int lvt_5_1_ = -8; lvt_5_1_ <= 2; lvt_5_1_++) {
                for (int lvt_6_1_ = 0; lvt_6_1_ < lvt_1_1_; ++lvt_6_1_) {
                    for (int lvt_7_1_ = 0; lvt_7_1_ <= lvt_6_1_; lvt_7_1_ = lvt_7_1_ > 0 ? -lvt_7_1_ : 1 - lvt_7_1_) {
                        for (int lvt_8_1_ = lvt_7_1_ < lvt_6_1_ && lvt_7_1_ > -lvt_6_1_ ? lvt_6_1_ : 0; lvt_8_1_ <= lvt_6_1_; lvt_8_1_ = lvt_8_1_ > 0 ? -lvt_8_1_ : 1 - lvt_8_1_) {
                            lvt_4_1_.setPos(lvt_3_1_.getX() + lvt_7_1_, lvt_3_1_.getY() + lvt_5_1_ - 1, lvt_3_1_.getZ() + lvt_8_1_);
                            if (this.isPumpkin(EntityCrow.this.world, lvt_4_1_)) {
                                this.destinationBlock = lvt_4_1_.toImmutable();
                                return true;
                            }
                        }
                    }
                }
            }

            return false;
        }

        private boolean isPumpkin(World world, BlockPos pos) {
            return AMTagRegistry.blockInTag(AMTagRegistry.CROW_FEARS, world.getBlockState(pos).getBlock());
        }

    }

    private class AITargetItems extends CreatureAITargetItems {

        public AITargetItems(boolean checkSight, boolean onlyNearby, int tickThreshold, int radius) {
            super(EntityCrow.this, checkSight, onlyNearby, tickThreshold, radius);
            this.executionChance = 1;
        }

        @Override
        public void resetTask() {
            super.resetTask();
            EntityCrow.this.aiItemFlag = false;
        }

        @Override
        public boolean shouldExecute() {
            return super.shouldExecute() && !EntityCrow.this.isSitting() && (EntityCrow.this.getAttackTarget() == null || !EntityCrow.this.getAttackTarget().isEntityAlive());
        }

        @Override
        public boolean shouldContinueExecuting() {
            return super.shouldContinueExecuting() && !EntityCrow.this.isSitting() && (EntityCrow.this.getAttackTarget() == null || !EntityCrow.this.getAttackTarget().isEntityAlive());
        }

        @Override
        protected void moveTo() {
            EntityCrow crow = EntityCrow.this;
            if (this.targetEntity != null) {
                crow.aiItemFlag = true;
                if (crow.getDistance(this.targetEntity) < 2) {
                    crow.getMoveHelper().setMoveTo(this.targetEntity.posX, this.targetEntity.posY, this.targetEntity.posZ, 1);
                    crow.peck();
                }
                if (crow.getDistance(this.targetEntity) > 8 || crow.isFlying()) {
                    crow.setFlying(true);
                    float f = (float) (crow.posX - this.targetEntity.posX);
                    float f1 = 1.8F;
                    float f2 = (float) (crow.posZ - this.targetEntity.posZ);
                    float xzDist = MathHelper.sqrt(f * f + f2 * f2);

                    if (!crow.canEntityBeSeen(this.targetEntity)) {
                        crow.getMoveHelper().setMoveTo(this.targetEntity.posX, 1 + crow.posY, this.targetEntity.posZ, 1);
                    } else {
                        if (xzDist < 5) {
                            f1 = 0;
                        }
                        crow.getMoveHelper().setMoveTo(this.targetEntity.posX, f1 + this.targetEntity.posY, this.targetEntity.posZ, 1);
                    }
                } else {
                    crow.getNavigator().tryMoveToXYZ(this.targetEntity.posX, this.targetEntity.posY, this.targetEntity.posZ, 1);
                }
            }
        }

        @Override
        public void updateTask() {
            if (this.targetEntity == null || !this.targetEntity.isEntityAlive()) {
                this.resetTask();
                EntityCrow.this.getNavigator().clearPath();
            } else {
                moveTo();
            }
            if (this.targetEntity != null && EntityCrow.this.canEntityBeSeen(this.targetEntity) && EntityCrow.this.width > 2D && EntityCrow.this.onGround) {
                EntityCrow.this.getMoveHelper().setMoveTo(this.targetEntity.posX, this.targetEntity.posY, this.targetEntity.posZ, 1.0D);
            }
            if (this.targetEntity != null && this.targetEntity.isEntityAlive()
                    && EntityCrow.this.getDistanceSq(this.targetEntity) < EntityCrow.this.getMaxDistToItem()
                    && EntityCrow.this.getHeldItemMainhand().isEmpty()) {
                EntityCrow.this.onGetItem(this.targetEntity);
                this.targetEntity.getItem().shrink(1);
                resetTask();
            }
        }
    }


    private class AIDepositChests extends EntityAIBase {
        protected final AIDepositChests.Sorter theNearestAttackableTargetSorter;
        protected final Predicate<EntityItemFrame> targetEntitySelector;
        protected int executionChance = 8;
        protected boolean mustUpdate;
        private EntityItemFrame targetEntity;
        private Vec3d flightTarget = null;
        private int cooldown = 0;

        AIDepositChests() {
            this.setMutexBits(1);
            this.theNearestAttackableTargetSorter = new AIDepositChests.Sorter(EntityCrow.this);
            this.targetEntitySelector = new Predicate<EntityItemFrame>() {
                @Override
                public boolean apply(@Nullable EntityItemFrame e) {
                    BlockPos hangingPosition = e.getHangingPosition().offset(e.getHorizontalFacing().getOpposite());
                    TileEntity entity = e.world.getTileEntity(hangingPosition);
                    if (entity != null) {
                        IItemHandler handler = entity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, e.getHorizontalFacing().getOpposite());
                        if (handler != null) {
                            return ItemStack.areItemsEqual(e.getDisplayedItem(), EntityCrow.this.getHeldItemMainhand());
                        }
                    }
                    return false;
                }
            };
        }

        @Override
        public boolean shouldExecute() {
            if (EntityCrow.this.isBeingRidden() || EntityCrow.this.aiItemFlag || EntityCrow.this.isBeingRidden() || EntityCrow.this.isSitting() || EntityCrow.this.getCommand() != 3) {
                return false;
            }
            if(EntityCrow.this.getHeldItemMainhand().isEmpty()){
                return false;
            }
            if (!this.mustUpdate) {
                long worldTime = EntityCrow.this.world.getTotalWorldTime() % 10;
                if (EntityCrow.this.getIdleTime() >= 100 && worldTime != 0) {
                    return false;
                }
                if (EntityCrow.this.getRNG().nextInt(this.executionChance) != 0 && worldTime != 0) {
                    return false;
                }
            }
            List<EntityItemFrame> list = EntityCrow.this.world.getEntitiesWithinAABB(EntityItemFrame.class, this.getTargetableArea(this.getTargetDistance()), this.targetEntitySelector);
            if (list.isEmpty()) {
                return false;
            } else {
                Collections.sort(list, this.theNearestAttackableTargetSorter);
                this.targetEntity = list.get(0);
                this.mustUpdate = false;
                EntityCrow.this.aiItemFrameFlag = true;
                return true;
            }
        }

        @Override
        public boolean shouldContinueExecuting() {
            return targetEntity != null && EntityCrow.this.getCommand() == 3 && !EntityCrow.this.getHeldItemMainhand().isEmpty();
        }

        public void resetTask() {
            flightTarget = null;
            this.targetEntity = null;
            EntityCrow.this.aiItemFrameFlag = false;
        }

        @Override
        public void updateTask() {
            if (cooldown > 0) {
                cooldown--;
            }
            if (flightTarget != null) {
                EntityCrow.this.setFlying(true);
                if (EntityCrow.this.collidedHorizontally) {
                    EntityCrow.this.getMoveHelper().setMoveTo(flightTarget.x, EntityCrow.this.posY + 1F, flightTarget.z, 1F);

                } else {
                    EntityCrow.this.getMoveHelper().setMoveTo(flightTarget.x, flightTarget.y, flightTarget.z, 1F);
                }
            }
            if (targetEntity != null) {
                flightTarget = targetEntity.getPositionVector();
                if (EntityCrow.this.getDistance(targetEntity) < 2.0F) {
                    try {
                        BlockPos hangingPosition = targetEntity.getHangingPosition().offset(targetEntity.getHorizontalFacing().getOpposite());
                        TileEntity entity = targetEntity.world.getTileEntity(hangingPosition);
                        EnumFacing deposit = targetEntity.getHorizontalFacing();
                        IItemHandler handler = entity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, deposit);
                        if (handler != null && cooldown == 0) {
                            ItemStack duplicate = EntityCrow.this.getHeldItem(EnumHand.MAIN_HAND).copy();
                            ItemStack insertSimulate = ItemHandlerHelper.insertItem(handler, duplicate, true);
                            if (!ItemStack.areItemStacksEqual(insertSimulate, duplicate)) {
                                ItemStack shrunkenStack = ItemHandlerHelper.insertItem(handler, duplicate, false);
                                if (shrunkenStack.isEmpty()) {
                                    EntityCrow.this.setHeldItem(EnumHand.MAIN_HAND, ItemStack.EMPTY);
                                } else {
                                    EntityCrow.this.setHeldItem(EnumHand.MAIN_HAND, shrunkenStack);
                                }
                                EntityCrow.this.peck();
                            } else {
                                cooldown = 20;
                            }
                        }
                    } catch (Exception e) {
                    }
                    this.resetTask();
                }
            }
        }

        protected double getTargetDistance() {
            return 4D;
        }

        protected AxisAlignedBB getTargetableArea(double targetDistance) {
            Vec3d renderCenter = new Vec3d(EntityCrow.this.posX, EntityCrow.this.posY, EntityCrow.this.posZ);
            AxisAlignedBB aabb = new AxisAlignedBB(-16, -16, -16, 16, 16, 16);
            return aabb.offset(renderCenter);
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
}
