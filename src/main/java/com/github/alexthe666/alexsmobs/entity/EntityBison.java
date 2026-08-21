package com.github.alexthe666.alexsmobs.entity;
import com.github.alexthe666.alexsmobs.misc.AMLootTables;

import com.github.alexthe666.alexsmobs.config.AMConfig;
import com.github.alexthe666.alexsmobs.entity.ai.AnimalAIHurtByTargetNotBaby;
import com.github.alexthe666.alexsmobs.entity.ai.AnimalAIPanicBaby;
import com.github.alexthe666.alexsmobs.entity.ai.AnimalAIWanderRanged;
import com.github.alexthe666.alexsmobs.entity.ai.EntityAINearestTarget3D;
import com.github.alexthe666.alexsmobs.entity.ai.GroundPathNavigatorWide;
import com.github.alexthe666.alexsmobs.item.AMItemRegistry;
import com.github.alexthe666.alexsmobs.misc.AMSoundRegistry;
import com.github.alexthe666.alexsmobs.misc.AMTagRegistry;
import com.github.alexthe666.citadel.animation.Animation;
import com.github.alexthe666.citadel.animation.IAnimatedEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSnow;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import net.minecraftforge.common.IShearable;
import net.minecraftforge.event.ForgeEventFactory;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class EntityBison extends EntityAnimal implements IAnimatedEntity, IShearable {

    private static final ResourceLocation BISON_BREEDABLES = new ResourceLocation("alexsmobs", "bison_breedables");

    public static final Animation ANIMATION_PREPARE_CHARGE = Animation.create(40);
    public static final Animation ANIMATION_EAT = Animation.create(35);
    public static final Animation ANIMATION_ATTACK = Animation.create(15);

    private static final DataParameter<Boolean> SHEARED = EntityDataManager.createKey(EntityBison.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> SNOWY = EntityDataManager.createKey(EntityBison.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> CHARGING = EntityDataManager.createKey(EntityBison.class, DataSerializers.BOOLEAN);

    public float prevChargeProgress;
    public float chargeProgress;
    private int animationTick;
    private Animation currentAnimation;
    private int snowTimer = 0;
    private boolean permSnow = false;
    private int blockBreakCounter;
    private int chargeCooldown = rand.nextInt(2000);
    private EntityBison chargePartner;
    private boolean hasChargedSpeed = false;
    private int feedingsSinceLastShear = 0;

    public EntityBison(World worldIn) {
        super(worldIn);
        this.setSize(2.4F, 2.1F);
        this.stepHeight = 1.1F;
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        AMEntityRegistry.registerAttributeIfAbsent(this, SharedMonsterAttributes.ATTACK_DAMAGE);
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(40.0D);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(8.0D);
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(32.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.25D);
    }

    @Override
    public boolean getCanSpawnHere() {
        return AMEntityRegistry.rollSpawn(AMConfig.bisonSpawnRolls, this.getRNG(), AMEntityRegistry.AMSpawnReason.OTHER) && super.getCanSpawnHere();
    }

    @Override
    public int getMaxSpawnedInChunk() {
        return 10;
    }

    @Nullable
    @Override
    public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData livingdata) {
        if (livingdata == null && this.rand.nextFloat() < 0.25F) {
            this.setGrowingAge(-24000);
        }
        return super.onInitialSpawn(difficulty, livingdata);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return AMSoundRegistry.BISON_IDLE;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return AMSoundRegistry.BISON_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return AMSoundRegistry.BISON_HURT;
    }
    @Override
    @Nullable
    protected ResourceLocation getLootTable() {
        return AMLootTables.BISON;
    }

    @Override
    protected void playStepSound(BlockPos pos, Block blockIn) {
        this.playSound(SoundEvents.ENTITY_COW_STEP, 0.1F, 1.0F);
    }

    public boolean isSnowy() {
        return this.dataManager.get(SNOWY);
    }

    public void setSnowy(boolean snowy) {
        this.dataManager.set(SNOWY, snowy);
    }

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(1, new EntityAIAttackMelee(this, 1.0D, true));
        this.tasks.addTask(3, new AnimalAIPanicBaby(this, 1.25D));
        this.tasks.addTask(4, new EntityAIMate(this, 1.0D));
        this.tasks.addTask(4, new EntityAITempt(this, 1.0D, Items.WHEAT, false) {
            @Override
            protected boolean isTempting(ItemStack stack) {
                return AMTagRegistry.itemInTag(BISON_BREEDABLES, stack.getItem());
            }
        });
        this.tasks.addTask(5, new EntityAIFollowParent(this, 1.1D));
        this.tasks.addTask(6, new AIChargeFurthest());
        this.tasks.addTask(7, new AnimalAIWanderRanged(this, 70, 1.0D, 18, 7));
        this.tasks.addTask(7, new EntityAIWatchClosest(this, EntityPlayer.class, 15.0F));
        this.tasks.addTask(7, new EntityAILookIdle(this));
        this.targetTasks.addTask(1, new AIAttackNearPlayers());
        this.targetTasks.addTask(2, new AnimalAIHurtByTargetNotBaby(this));
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        return AMTagRegistry.itemInTag(BISON_BREEDABLES, stack.getItem());
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(SHEARED, Boolean.FALSE);
        this.dataManager.register(SNOWY, Boolean.FALSE);
        this.dataManager.register(CHARGING, Boolean.FALSE);
    }

    @Nullable
    @Override
    public EntityAgeable createChild(EntityAgeable ageable) {
        return new EntityBison(this.world);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.setSnowy(compound.getBoolean("Snowy"));
        this.setSheared(compound.getBoolean("Sheared"));
        this.permSnow = compound.getBoolean("SnowPerm");
        this.chargeCooldown = compound.getInteger("ChargeCooldown");
        this.feedingsSinceLastShear = compound.getInteger("Feedings");
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setBoolean("Snowy", this.isSnowy());
        compound.setBoolean("Sheared", this.isSheared());
        compound.setBoolean("SnowPerm", this.permSnow);
        compound.setInteger("ChargeCooldown", this.chargeCooldown);
        compound.setInteger("Feedings", this.feedingsSinceLastShear);
    }

    @Override
    protected PathNavigate createNavigator(World worldIn) {
        return new GroundPathNavigatorWide(this, worldIn);
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        this.prevChargeProgress = this.chargeProgress;
        if (this.isCharging() && chargeProgress < 5F) {
            chargeProgress++;
        }
        if (!this.isCharging() && chargeProgress > 0F) {
            chargeProgress--;
        }
        if (!this.world.isRemote) {
            if (snowTimer == 0) {
                snowTimer = 200 + rand.nextInt(400);
                if (this.isSnowy()) {
                    if (!permSnow) {
                        if (this.isBurning() || this.isInWater() || !EntityGrizzlyBear.isSnowingAt(world, this.getPosition().up())) {
                            this.setSnowy(false);
                        }
                    }
                } else {
                    if (EntityGrizzlyBear.isSnowingAt(world, this.getPosition())) {
                        this.setSnowy(true);
                    }
                }
            }

            EntityLivingBase attackTarget = this.getAttackTarget();
            if (this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ * this.motionZ < 0.05D
                    && this.getAnimation() == NO_ANIMATION && (attackTarget == null || !attackTarget.isEntityAlive())) {
                if (getRNG().nextInt(600) == 0 && world.getBlockState(this.getPosition().down()).getBlock() == Blocks.GRASS) {
                    this.setAnimation(ANIMATION_EAT);
                }
            }
            if (this.getAnimation() == ANIMATION_EAT && this.getAnimationTick() == 30
                    && world.getBlockState(this.getPosition().down()).getBlock() == Blocks.GRASS) {
                this.feedingsSinceLastShear++;
                BlockPos down = this.getPosition().down();
                this.world.playEvent(2001, down, Block.getStateId(Blocks.GRASS.getDefaultState()));
                this.world.setBlockState(down, Blocks.DIRT.getDefaultState(), 2);
            }

            if (isCharging()) {
                if (!hasChargedSpeed) {
                    this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.65D);
                    hasChargedSpeed = true;
                }
            } else {
                if (hasChargedSpeed) {
                    this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.25D);
                    hasChargedSpeed = false;
                }
            }

            if (attackTarget != null && attackTarget.isEntityAlive() && this.isEntityAlive()) {
                final double dist = this.getDistance(attackTarget);
                if (this.canEntityBeSeen(attackTarget)) {
                    this.getLookHelper().setLookPositionWithEntity(attackTarget, 30.0F, 30.0F);
                    this.renderYawOffset = this.rotationYaw;
                }
                if (dist < this.width + 3.0F) {
                    if (this.getAnimation() == ANIMATION_ATTACK && this.getAnimationTick() > 8
                            && dist < this.width + 1.0F && this.canEntityBeSeen(attackTarget)) {
                        float dmg = (float) this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getBaseValue();
                        if (attackTarget instanceof EntityWolf) {
                            dmg = 2;
                        }
                        launch(attackTarget, isCharging());
                        if (isCharging()) {
                            dmg += 3;
                            this.setCharging(false);
                        }
                        attackTarget.attackEntityFrom(DamageSource.causeMobDamage(this), dmg);
                    }
                } else if (!this.isCharging()) {
                    final Animation animation = this.getAnimation();
                    if (animation == NO_ANIMATION) {
                        this.setAnimation(ANIMATION_PREPARE_CHARGE);
                    } else if (animation == ANIMATION_PREPARE_CHARGE) {
                        this.getNavigator().clearPath();
                        if (this.getAnimationTick() > 30) {
                            this.setCharging(true);
                        }
                    }
                }
            }
            this.breakBlock();
        }
        if (chargeCooldown > 0) {
            chargeCooldown--;
        }
        if (feedingsSinceLastShear >= 5 && this.isSheared()) {
            feedingsSinceLastShear = 0;
            this.setSheared(false);
        }
        if (!this.world.isRemote && this.isCharging()
                && (this.getAttackTarget() == null && this.chargePartner == null || this.isInWater())) {
            this.setCharging(false);
        }
        AMEntityRegistry.updateAnimations(this);
    }

    @Override
    public boolean attackEntityAsMob(Entity entityIn) {
        if (this.getAnimation() == NO_ANIMATION) {
            this.setAnimation(ANIMATION_ATTACK);
        }
        return true;
    }

    public boolean isSheared() {
        return this.dataManager.get(SHEARED);
    }

    public void setSheared(boolean sheared) {
        this.dataManager.set(SHEARED, sheared);
    }

    private void launch(Entity launch, boolean huge) {
        if (!(launch instanceof EntityLivingBase)) {
            return;
        }
        final float rot = 180F + this.rotationYaw;
        final float hugeScale = huge ? 4F : 0.6F;
        double resist = 0.0D;
        if (((EntityLivingBase) launch).getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE) != null) {
            resist = ((EntityLivingBase) launch).getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).getAttributeValue();
        }
        final float strength = (float) (hugeScale * (1.0D - resist));
        final float rotRad = rot * 0.017453292F;
        final float x = MathHelper.sin(rotRad);
        final float z = -MathHelper.cos(rotRad);
        launch.isAirBorne = true;
        final Vec3d vec3 = new Vec3d(this.motionX, this.motionY, this.motionZ);
        final Vec3d impulse = new Vec3d(x, 0.0D, z).normalize().scale(strength);
        final Vec3d vec31 = vec3.add(impulse);
        launch.motionX = vec31.x;
        launch.motionY = huge ? 1.0D : 0.5D;
        launch.motionZ = vec31.z;
        launch.onGround = false;
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        ItemStack itemstack = player.getHeldItem(hand);
        Item item = itemstack.getItem();
        if (!this.world.isRemote) {
            if (item == Item.getItemFromBlock(Blocks.SNOW) && !this.isSnowy()) {
                if (!player.capabilities.isCreativeMode) {
                    itemstack.shrink(1);
                }
                this.permSnow = true;
                this.setSnowy(true);
                this.playSound(SoundEvents.BLOCK_SNOW_PLACE, this.getSoundVolume(), this.getSoundPitch());
                return true;
            }

            if (item instanceof ItemTool && ((ItemTool) item).getToolClasses(itemstack).contains("shovel") && this.isSnowy()) {
                this.permSnow = false;
                if (!player.capabilities.isCreativeMode) {
                    itemstack.damageItem(1, player);
                }
                this.setSnowy(false);
                this.playSound(SoundEvents.BLOCK_SNOW_BREAK, this.getSoundVolume(), this.getSoundPitch());
                return true;
            }
        }
        return super.processInteract(player, hand);
    }

    public void breakBlock() {
        if (this.blockBreakCounter > 0) {
            --this.blockBreakCounter;
            return;
        }
        boolean flag = false;
        if (!this.world.isRemote && this.blockBreakCounter == 0 && ForgeEventFactory.getMobGriefingEvent(world, this)) {
            for (int a = (int) Math.round(this.getEntityBoundingBox().minX); a <= (int) Math.round(this.getEntityBoundingBox().maxX); a++) {
                for (int b = (int) Math.round(this.getEntityBoundingBox().minY) - 1;
                     (b <= (int) Math.round(this.getEntityBoundingBox().maxY) + 1) && (b <= 127); b++) {
                    for (int c = (int) Math.round(this.getEntityBoundingBox().minZ); c <= (int) Math.round(this.getEntityBoundingBox().maxZ); c++) {
                        final BlockPos pos = new BlockPos(a, b, c);
                        final IBlockState state = world.getBlockState(pos);
                        final Block block = state.getBlock();
                        if (block == Blocks.SNOW_LAYER && state.getValue(BlockSnow.LAYERS) <= 1) {
                            this.motionX *= 0.6F;
                            this.motionZ *= 0.6F;
                            flag = true;
                            world.destroyBlock(pos, true);
                        }
                    }
                }
            }
        }
        if (flag) {
            blockBreakCounter = this.isCharging() && this.getAttackTarget() != null ? 2 : 20;
        }
    }

    @Override
    public int getAnimationTick() {
        return animationTick;
    }

    @Override
    public void setAnimationTick(int tick) {
        animationTick = tick;
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
        return new Animation[]{ANIMATION_PREPARE_CHARGE, ANIMATION_ATTACK, ANIMATION_EAT};
    }

    @Override
    public boolean isShearable(ItemStack item, net.minecraft.world.IBlockAccess world, BlockPos pos) {
        return this.readyForShearing();
    }

    public boolean isCharging() {
        return this.dataManager.get(CHARGING);
    }

    public void setCharging(boolean charging) {
        this.dataManager.set(CHARGING, charging);
    }

    public boolean readyForShearing() {
        return !isSheared() && !isChild();
    }

    @Override
    public List<ItemStack> onSheared(ItemStack item, net.minecraft.world.IBlockAccess world, BlockPos pos, int fortune) {
        this.world.playSound(null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_SHEEP_SHEAR, SoundCategory.PLAYERS, 1.0F, 1.0F);
        List<ItemStack> list = new ArrayList<>(6);
        if (!this.world.isRemote) {
            for (int i = 0; i < 2 + rand.nextInt(2); i++) {
                list.add(new ItemStack(AMItemRegistry.BISON_FUR));
            }
            this.feedingsSinceLastShear = 0;
            this.setSheared(true);
        }
        return list;
    }

    public boolean isValidCharging() {
        return !this.isChild() && this.isEntityAlive() && chargeCooldown == 0 && !this.isInWater();
    }

    public void pushBackJostling(EntityBison bison, float strength) {
        applyKnockbackFromBuffalo(strength, bison.posX - this.posX, bison.posZ - this.posZ);
    }

    private void applyKnockbackFromBuffalo(float strength, double ratioX, double ratioZ) {
        net.minecraftforge.event.entity.living.LivingKnockBackEvent event = net.minecraftforge.common.ForgeHooks.onLivingKnockBack(this, null, strength, ratioX, ratioZ);
        if (event.isCanceled()) {
            return;
        }
        strength = event.getStrength();
        ratioX = event.getRatioX();
        ratioZ = event.getRatioZ();
        if (!(strength <= 0.0F)) {
            this.isAirBorne = true;
            Vec3d vector3d = new Vec3d(this.motionX, this.motionY, this.motionZ);
            Vec3d vector3d1 = (new Vec3d(ratioX, 0.0D, ratioZ)).normalize().scale(strength);
            this.motionX = vector3d.x / 2.0D - vector3d1.x;
            this.motionY = 0.3D;
            this.motionZ = vector3d.z / 2.0D - vector3d1.z;
        }
    }

    private void resetChargeCooldown() {
        this.setCharging(false);
        this.chargePartner = null;
        this.chargeCooldown = 1000 + rand.nextInt(2000);
    }

    private class AIChargeFurthest extends EntityAIBase {

        public AIChargeFurthest() {
            this.setMutexBits(3);
        }

        @Override
        public boolean shouldExecute() {
            if (EntityBison.this.isValidCharging()) {
                if (EntityBison.this.chargePartner != null && EntityBison.this.chargePartner.isValidCharging()
                        && EntityBison.this.chargePartner != EntityBison.this) {
                    EntityBison.this.chargePartner.chargePartner = EntityBison.this;
                    return true;
                } else if (rand.nextInt(100) == 0) {
                    EntityBison furthest = null;
                    for (final EntityBison bison : EntityBison.this.world.getEntitiesWithinAABB(EntityBison.class,
                            EntityBison.this.getEntityBoundingBox().grow(15F))) {
                        if (bison.chargeCooldown == 0 && !bison.isChild() && bison != EntityBison.this) {
                            if (furthest == null || EntityBison.this.getDistance(furthest) < EntityBison.this.getDistance(bison)) {
                                furthest = bison;
                            }
                        }
                    }
                    if (furthest != null && furthest != EntityBison.this) {
                        EntityBison.this.chargePartner = furthest;
                        furthest.chargePartner = EntityBison.this;
                        return true;
                    }
                }
            }
            return false;
        }

        @Override
        public boolean shouldContinueExecuting() {
            return EntityBison.this.isValidCharging() && EntityBison.this.chargePartner != null
                    && EntityBison.this.chargePartner.isValidCharging() && EntityBison.this.chargePartner != EntityBison.this;
        }

        @Override
        public void updateTask() {
            EntityBison.this.getLookHelper().setLookPositionWithEntity(EntityBison.this.chargePartner, 30.0F, 30.0F);
            EntityBison.this.renderYawOffset = EntityBison.this.rotationYaw;
            if (!EntityBison.this.isCharging()) {
                final Animation bisonAnimation = EntityBison.this.getAnimation();
                if (bisonAnimation == NO_ANIMATION
                        || bisonAnimation == ANIMATION_PREPARE_CHARGE && EntityBison.this.getAnimationTick() > 35) {
                    EntityBison.this.setCharging(true);
                }
            } else {
                final float dist = EntityBison.this.getDistance(EntityBison.this.chargePartner);
                EntityBison.this.getNavigator().tryMoveToEntityLiving(EntityBison.this.chargePartner, 1.0D);
                if (EntityBison.this.canEntityBeSeen(EntityBison.this.chargePartner)) {
                    final float flingAnimAt = EntityBison.this.width + 1.0F;
                    if (dist < flingAnimAt && EntityBison.this.getAnimation() == ANIMATION_ATTACK) {
                        if (EntityBison.this.getAnimationTick() > 8) {
                            boolean flag = false;
                            if (EntityBison.this.onGround) {
                                EntityBison.this.pushBackJostling(EntityBison.this.chargePartner, 0.2F);
                                flag = true;
                            }
                            if (EntityBison.this.chargePartner.onGround) {
                                EntityBison.this.chargePartner.pushBackJostling(EntityBison.this, 0.9F);
                                flag = true;
                            }
                            if (flag) {
                                EntityBison.this.resetChargeCooldown();
                            }
                        }
                    } else {
                        final float startFlingAnimAt = EntityBison.this.width + 3.0F;
                        if (dist < startFlingAnimAt && EntityBison.this.getAnimation() != ANIMATION_ATTACK) {
                            EntityBison.this.setAnimation(ANIMATION_ATTACK);
                        }
                    }
                }
            }
        }
    }

    class AIAttackNearPlayers extends EntityAINearestTarget3D {

        public AIAttackNearPlayers() {
            super(EntityBison.this, EntityPlayer.class, 80, true, true, null);
        }

        @Override
        public boolean shouldExecute() {
            if (EntityBison.this.isChild() || EntityBison.this.isInLove()) {
                return false;
            } else {
                return super.shouldExecute();
            }
        }

        @Override
        protected double getTargetDistance() {
            return 3.0D;
        }
    }
}
