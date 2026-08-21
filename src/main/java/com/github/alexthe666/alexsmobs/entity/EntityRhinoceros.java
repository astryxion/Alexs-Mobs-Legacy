package com.github.alexthe666.alexsmobs.entity;

import com.github.alexthe666.alexsmobs.config.AMConfig;
import com.github.alexthe666.alexsmobs.entity.ai.AnimalAIHurtByTargetNotBaby;
import com.github.alexthe666.alexsmobs.entity.ai.AnimalAIPanicBaby;
import com.github.alexthe666.alexsmobs.entity.ai.AnimalAIWanderRanged;
import com.github.alexthe666.alexsmobs.entity.ai.EntityAINearestTarget3D;
import com.github.alexthe666.alexsmobs.entity.ai.GroundPathNavigatorWide;
import com.github.alexthe666.alexsmobs.misc.AMSoundRegistry;
import com.github.alexthe666.alexsmobs.misc.AMTagRegistry;
import com.github.alexthe666.citadel.animation.Animation;
import com.github.alexthe666.citadel.animation.IAnimatedEntity;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.monster.EntityVindicator;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionType;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class EntityRhinoceros extends EntityAnimal implements IAnimatedEntity {

    private static final ResourceLocation RHINOCEROS_BREEDABLES = new ResourceLocation("alexsmobs", "rhinoceros_breedables");
    private static final ResourceLocation RHINOCEROS_FOODSTUFFS = new ResourceLocation("alexsmobs", "rhinoceros_foodstuffs");

    public static final Animation ANIMATION_FLICK_EARS = Animation.create(20);
    public static final Animation ANIMATION_EAT_GRASS = Animation.create(35);
    public static final Animation ANIMATION_FLING = Animation.create(15);
    public static final Animation ANIMATION_SLASH = Animation.create(30);

    private static final DataParameter<String> APPLIED_POTION = EntityDataManager.createKey(EntityRhinoceros.class, DataSerializers.STRING);
    private static final DataParameter<Integer> POTION_LEVEL = EntityDataManager.createKey(EntityRhinoceros.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> INFLICTED_COUNT = EntityDataManager.createKey(EntityRhinoceros.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> POTION_DURATION = EntityDataManager.createKey(EntityRhinoceros.class, DataSerializers.VARINT);
    private static final DataParameter<Optional<UUID>> DATA_TRUSTED_ID_0 = EntityDataManager.createKey(EntityRhinoceros.class, DataSerializers.OPTIONAL_UNIQUE_ID);
    private static final DataParameter<Optional<UUID>> DATA_TRUSTED_ID_1 = EntityDataManager.createKey(EntityRhinoceros.class, DataSerializers.OPTIONAL_UNIQUE_ID);
    private static final DataParameter<Boolean> ANGRY = EntityDataManager.createKey(EntityRhinoceros.class, DataSerializers.BOOLEAN);

    private static final Map<String, Integer> potionToColor = new HashMap<>();

    private int animationTick;
    private Animation currentAnimation;

    public EntityRhinoceros(World worldIn) {
        super(worldIn);
        this.setSize(2.3F, 2.4F);
        this.stepHeight = 1.1F;
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        AMEntityRegistry.registerAttributeIfAbsent(this, SharedMonsterAttributes.ATTACK_DAMAGE);
        AMEntityRegistry.registerAttributeIfAbsent(this, SharedMonsterAttributes.ARMOR);
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(60.0D);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(8.0D);
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(32.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.25D);
        this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(12.0D);
        this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(0.9D);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(DATA_TRUSTED_ID_0, Optional.absent());
        this.dataManager.register(DATA_TRUSTED_ID_1, Optional.absent());
        this.dataManager.register(APPLIED_POTION, "");
        this.dataManager.register(POTION_LEVEL, 0);
        this.dataManager.register(INFLICTED_COUNT, 0);
        this.dataManager.register(POTION_DURATION, 0);
        this.dataManager.register(ANGRY, Boolean.FALSE);
    }

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(1, new EntityAIAttackMelee(this, 1.4D, true));
        this.tasks.addTask(2, new AnimalAIPanicBaby(this, 1.25D));
        this.tasks.addTask(3, new EntityAIMate(this, 1.0D));
        this.tasks.addTask(4, new EntityAITempt(this, 1.0D, Items.WHEAT, false) {
            @Override
            protected boolean isTempting(ItemStack stack) {
                return AMTagRegistry.itemInTag(RHINOCEROS_FOODSTUFFS, stack.getItem())
                        || AMTagRegistry.itemInTag(RHINOCEROS_BREEDABLES, stack.getItem());
            }
        });
        this.tasks.addTask(5, new EntityAIFollowParent(this, 1.1D));
        this.tasks.addTask(6, new AnimalAIWanderRanged(this, 90, 1.0D, 18, 7));
        this.tasks.addTask(7, new StrollGoal(200));
        this.tasks.addTask(8, new EntityAIWatchClosest(this, EntityPlayer.class, 15.0F));
        this.tasks.addTask(8, new EntityAILookIdle(this));
        this.targetTasks.addTask(1, new DefendTrustedTargetGoal(EntityLivingBase.class, false, false, entity -> !trusts(entity.getUniqueID())));
        this.targetTasks.addTask(2, new EntityAINearestAttackableTarget<EntityVindicator>(this, EntityVindicator.class, 50, true, true, null) {
            @Override
            public boolean shouldExecute() {
                return super.shouldExecute() && !EntityRhinoceros.this.isChild();
            }
        });
        this.targetTasks.addTask(3, new AIAttackNearPlayers());
        this.targetTasks.addTask(4, new AnimalAIHurtByTargetNotBaby(this));
    }

    @Override
    protected PathNavigate createNavigator(World worldIn) {
        return new GroundPathNavigatorWide(this, worldIn);
    }

    @Override
    public boolean getCanSpawnHere() {
        return AMEntityRegistry.rollSpawn(AMConfig.rhinocerosSpawnRolls, this.getRNG(), AMEntityRegistry.AMSpawnReason.OTHER) && super.getCanSpawnHere();
    }

    @Override
    public int getMaxSpawnedInChunk() {
        return 3;
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        AMEntityRegistry.updateAnimations(this);
        if (!this.world.isRemote) {
            if (this.getAnimation() == NO_ANIMATION && (this.getAttackTarget() == null || !this.getAttackTarget().isEntityAlive())) {
                double motionSq = this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ * this.motionZ;
                if (motionSq < 0.03D && getRNG().nextInt(500) == 0
                        && world.getBlockState(this.getPosition().down()).getBlock() == Blocks.GRASS) {
                    this.setAnimation(ANIMATION_EAT_GRASS);
                } else if (getRNG().nextInt(200) == 0) {
                    this.setAnimation(ANIMATION_FLICK_EARS);
                }
            }
            if (this.getAnimation() == ANIMATION_EAT_GRASS && this.getAnimationTick() == 30
                    && world.getBlockState(this.getPosition().down()).getBlock() == Blocks.GRASS) {
                BlockPos down = this.getPosition().down();
                this.world.playEvent(2001, down, Block.getStateId(Blocks.GRASS.getDefaultState()));
                this.world.setBlockState(down, Blocks.DIRT.getDefaultState(), 2);
                this.heal(10.0F);
            }
            EntityLivingBase target = this.getAttackTarget();
            if (target != null && target.isEntityAlive()) {
                this.setAngry(this.getDistance(target) < 20);
                double dist = this.getDistance(target);
                if (canEntityBeSeen(target)) {
                    this.getLookHelper().setLookPositionWithEntity(target, 30.0F, 30.0F);
                    this.renderYawOffset = this.rotationYaw;
                }
                if (dist < this.width + 3.0F) {
                    if (this.getAnimation() == NO_ANIMATION) {
                        this.setAnimation(rand.nextBoolean() ? ANIMATION_SLASH : ANIMATION_FLING);
                    }
                    if (dist < this.width + 1.5F && this.canEntityBeSeen(target)) {
                        if (this.getAnimation() == ANIMATION_FLING && this.getAnimationTick() >= 5 && this.getAnimationTick() <= 8) {
                            float dmg = (float) this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getBaseValue();
                            if (target instanceof EntityVindicator) {
                                dmg = 10;
                            }
                            attackWithPotion(target, dmg);
                            launch(target, 0, 1F);
                            for (EntityLivingBase entity : this.world.getEntitiesWithinAABB(EntityLivingBase.class, this.getEntityBoundingBox().grow(1.0D))) {
                                if (!(entity instanceof EntityAnimal) && !trusts(entity.getUniqueID()) && entity != target) {
                                    attackWithPotion(entity, Math.max(dmg - 5, 1));
                                    launch(entity, 0, 0.5F);
                                }
                            }
                        }
                        if (this.getAnimation() == ANIMATION_SLASH
                                && (this.getAnimationTick() >= 9 && this.getAnimationTick() <= 11
                                || this.getAnimationTick() >= 19 && this.getAnimationTick() <= 21)) {
                            float dmg = (float) this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getBaseValue();
                            if (target instanceof EntityVindicator) {
                                dmg = 10;
                            }
                            attackWithPotion(target, dmg);
                            launch(target, this.getAnimationTick() <= 15 ? -90 : 90, 1F);
                            for (EntityLivingBase entity : this.world.getEntitiesWithinAABB(EntityLivingBase.class, this.getEntityBoundingBox().grow(1.0D))) {
                                if (!(entity instanceof EntityAnimal) && !trusts(entity.getUniqueID()) && entity != target) {
                                    attackWithPotion(entity, Math.max(dmg - 5, 1));
                                    launch(entity, this.getAnimationTick() <= 15 ? -90 : 90, 0.5F);
                                }
                            }
                        }
                    }
                }
            } else {
                this.setAngry(false);
            }
        }
    }

    @Override
    protected void playStepSound(BlockPos pos, Block blockIn) {
        if (!isChild()) {
            this.playSound(AMSoundRegistry.ELEPHANT_WALK, 0.2F, 1.2F);
        } else {
            super.playStepSound(pos, blockIn);
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return AMSoundRegistry.RHINOCEROS_IDLE;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return AMSoundRegistry.RHINOCEROS_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return AMSoundRegistry.RHINOCEROS_HURT;
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        return AMTagRegistry.isDeadBush(stack)
                || AMTagRegistry.isTallGrassPlant(stack)
                || AMTagRegistry.itemInTag(RHINOCEROS_BREEDABLES, stack.getItem());
    }

    public String getAppliedPotionId() {
        return this.dataManager.get(APPLIED_POTION);
    }

    public void setAppliedPotionId(String potionId) {
        this.dataManager.set(APPLIED_POTION, potionId);
    }

    public int getPotionColor() {
        String s = this.getAppliedPotionId();
        if (s.isEmpty()) {
            return -1;
        } else {
            if (!potionToColor.containsKey(s)) {
                Potion effect = getPotionEffect();
                if (effect != null) {
                    int color = effect.getLiquidColor();
                    potionToColor.put(s, color);
                    return color;
                }
                return -1;
            } else {
                return potionToColor.get(s);
            }
        }
    }

    @Nullable
    public Potion getPotionEffect() {
        if (this.getAppliedPotionId().isEmpty()) {
            return null;
        }
        return ForgeRegistries.POTIONS.getValue(new ResourceLocation(this.getAppliedPotionId()));
    }

    public int getPotionDuration() {
        return this.dataManager.get(POTION_DURATION);
    }

    public void setPotionDuration(int time) {
        this.dataManager.set(POTION_DURATION, time);
    }

    public int getPotionLevel() {
        return this.dataManager.get(POTION_LEVEL);
    }

    public void setPotionLevel(int level) {
        this.dataManager.set(POTION_LEVEL, level);
    }

    public int getInflictedCount() {
        return this.dataManager.get(INFLICTED_COUNT);
    }

    public void setInflictedCount(int count) {
        this.dataManager.set(INFLICTED_COUNT, count);
    }

    public void resetPotion() {
        this.setAppliedPotionId("");
        this.setPotionDuration(0);
        this.setPotionLevel(0);
        this.setInflictedCount(0);
    }

    private List<UUID> getTrustedUUIDs() {
        List<UUID> list = Lists.newArrayList();
        list.add(this.dataManager.get(DATA_TRUSTED_ID_0).orNull());
        list.add(this.dataManager.get(DATA_TRUSTED_ID_1).orNull());
        return list;
    }

    private void addTrustedUUID(@Nullable UUID uuid) {
        if (this.dataManager.get(DATA_TRUSTED_ID_0).isPresent()) {
            this.dataManager.set(DATA_TRUSTED_ID_1, Optional.fromNullable(uuid));
        } else {
            this.dataManager.set(DATA_TRUSTED_ID_0, Optional.fromNullable(uuid));
        }
    }

    private void launch(Entity launch, float angle, float scale) {
        if (!(launch instanceof EntityLivingBase)) {
            return;
        }
        final float rot = 180F + angle + this.rotationYaw;
        final float hugeScale = 1.0F + rand.nextFloat() * 0.5F * scale;
        double resist = 0.0D;
        if (((EntityLivingBase) launch).getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE) != null) {
            resist = ((EntityLivingBase) launch).getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).getAttributeValue();
        }
        final float strength = (float) (hugeScale * (1.0D - resist));
        final float rotRad = rot * 0.017453292F;
        final float x = MathHelper.sin(rotRad);
        final float z = -MathHelper.cos(rotRad);
        launch.isAirBorne = true;
        Vec3d vec3 = new Vec3d(this.motionX, this.motionY, this.motionZ);
        Vec3d vec31 = vec3.add(new Vec3d(x, 0.0D, z).normalize().scale(strength));
        launch.motionX = vec31.x;
        launch.motionY = hugeScale * 0.3D;
        launch.motionZ = vec31.z;
        launch.onGround = false;
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

    private boolean trusts(UUID uuid) {
        return uuid != null && this.getTrustedUUIDs().contains(uuid);
    }

    @Override
    public Animation[] getAnimations() {
        return new Animation[]{ANIMATION_FLICK_EARS, ANIMATION_EAT_GRASS, ANIMATION_FLING, ANIMATION_SLASH};
    }

    @Nullable
    @Override
    public EntityAgeable createChild(EntityAgeable ageable) {
        return new EntityRhinoceros(this.world);
    }

    public boolean isAngry() {
        return this.dataManager.get(ANGRY);
    }

    public void setAngry(boolean angry) {
        this.dataManager.set(ANGRY, angry);
    }

    private void attackWithPotion(EntityLivingBase target, float dmg) {
        Potion potion = this.getPotionEffect();
        target.attackEntityFrom(DamageSource.causeMobDamage(this), dmg);
        if (potion != null) {
            PotionEffect instance = new PotionEffect(potion, this.getPotionDuration(), this.getPotionLevel());
            if (!target.isPotionActive(potion)) {
                target.addPotionEffect(instance);
                this.setInflictedCount(this.getInflictedCount() + 1);
            }
        }
        if (this.getInflictedCount() > 15 && rand.nextInt(3) == 0 || this.getInflictedCount() > 20) {
            this.resetPotion();
        }
    }

    @Override
    public boolean attackEntityAsMob(Entity entityIn) {
        if (this.getAnimation() == NO_ANIMATION) {
            this.setAnimation(rand.nextBoolean() ? ANIMATION_SLASH : ANIMATION_FLING);
            return true;
        }
        return false;
    }

    @Override
    public boolean isOnSameTeam(Entity entityIn) {
        if (entityIn instanceof EntityTameable) {
            EntityTameable tamable = (EntityTameable) entityIn;
            if (tamable.getOwnerId() != null && trusts(tamable.getOwnerId())) {
                return true;
            }
        }
        return super.isOnSameTeam(entityIn) || trusts(entityIn.getUniqueID());
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound tag) {
        super.writeEntityToNBT(tag);
        List<UUID> list = this.getTrustedUUIDs();
        NBTTagList listtag = new NBTTagList();
        for (UUID uuid : list) {
            if (uuid != null) {
                NBTTagCompound uuidTag = new NBTTagCompound();
                uuidTag.setUniqueId("M", uuid);
                listtag.appendTag(uuidTag);
            }
        }
        tag.setTag("Trusted", listtag);
        tag.setString("PotionName", this.getAppliedPotionId());
        tag.setInteger("PotionLevel", this.getPotionLevel());
        tag.setInteger("PotionDuration", this.getPotionDuration());
        tag.setInteger("InflictedCount", this.getInflictedCount());
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound tag) {
        super.readEntityFromNBT(tag);
        NBTTagList listtag = tag.getTagList("Trusted", 10);
        for (int i = 0; i < listtag.tagCount(); ++i) {
            this.addTrustedUUID(listtag.getCompoundTagAt(i).getUniqueId("M"));
        }
        this.setAppliedPotionId(tag.getString("PotionName"));
        this.setPotionLevel(tag.getInteger("PotionLevel"));
        this.setPotionDuration(tag.getInteger("PotionDuration"));
        this.setInflictedCount(tag.getInteger("InflictedCount"));
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        ItemStack itemstack = player.getHeldItem(hand);
        if (isBreedingItem(itemstack)) {
            return super.processInteract(player, hand);
        }
        if (!isChild() && (itemstack.getItem() == Items.POTIONITEM
                || itemstack.getItem() == Items.SPLASH_POTION
                || itemstack.getItem() == Items.LINGERING_POTION)) {
            PotionType contained = PotionUtils.getPotionFromItem(itemstack);
            if (applyPotion(contained, itemstack)) {
                this.playSound(SoundEvents.ITEM_BOTTLE_FILL, 1.0F, 1.0F);
                if (!player.capabilities.isCreativeMode) {
                    itemstack.shrink(1);
                }
                ItemStack bottle = new ItemStack(Items.GLASS_BOTTLE);
                if (!player.inventory.addItemStackToInventory(bottle)) {
                    player.dropItem(bottle, false);
                }
                return true;
            }
        } else if (AMTagRegistry.itemInTag(RHINOCEROS_FOODSTUFFS, itemstack.getItem()) && !trusts(player.getUniqueID())) {
            addTrustedUUID(player.getUniqueID());
            if (!player.capabilities.isCreativeMode) {
                itemstack.shrink(1);
            }
            this.playSound(SoundEvents.ENTITY_HORSE_EAT, 1.0F, 1.0F);
            return true;
        }
        return super.processInteract(player, hand);
    }

    public boolean applyPotion(PotionType potionType, ItemStack stack) {
        if (potionType == null || PotionUtils.getEffectsFromStack(stack).isEmpty()) {
            resetPotion();
            return true;
        } else {
            List<PotionEffect> effects = PotionUtils.getEffectsFromStack(stack);
            if (!effects.isEmpty()) {
                PotionEffect first = effects.get(0);
                Potion effect = first.getPotion();
                ResourceLocation loc = effect.getRegistryName();
                if (loc != null) {
                    this.setAppliedPotionId(loc.toString());
                    this.setPotionLevel(first.getAmplifier());
                    this.setPotionDuration(first.getDuration());
                    this.setInflictedCount(0);
                    return true;
                }
            }
        }
        return false;
    }

    class AIAttackNearPlayers extends EntityAINearestTarget3D {

        public AIAttackNearPlayers() {
            super(EntityRhinoceros.this, EntityPlayer.class, 80, true, true, null);
        }

        @Override
        public boolean shouldExecute() {
            if (EntityRhinoceros.this.isChild() || EntityRhinoceros.this.isInLove() || EntityRhinoceros.this.trustsAny()) {
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

    private boolean trustsAny() {
        return this.dataManager.get(DATA_TRUSTED_ID_0).isPresent() || this.dataManager.get(DATA_TRUSTED_ID_1).isPresent();
    }

    class DefendTrustedTargetGoal extends EntityAINearestAttackableTarget<EntityLivingBase> {
        private EntityLivingBase trustedLastHurtBy;
        private EntityLivingBase trustedLastHurt;
        private EntityLivingBase trusted;
        private int timestamp;

        public DefendTrustedTargetGoal(Class<EntityLivingBase> entityClass, boolean checkSight, boolean onlyNearby, Predicate<EntityLivingBase> targetPredicate) {
            super(EntityRhinoceros.this, entityClass, 10, checkSight, onlyNearby, targetPredicate);
        }

        @Override
        public boolean shouldExecute() {
            if (this.taskOwner.getRNG().nextInt(10) != 0 || this.taskOwner.isChild()) {
                return false;
            } else {
                Iterator<UUID> var1 = EntityRhinoceros.this.getTrustedUUIDs().iterator();
                while (var1.hasNext()) {
                    UUID uuid = var1.next();
                    if (uuid != null && EntityRhinoceros.this.world instanceof WorldServer) {
                        Entity entity = ((WorldServer) EntityRhinoceros.this.world).getEntityFromUuid(uuid);
                        if (entity instanceof EntityLivingBase) {
                            EntityLivingBase livingentity = (EntityLivingBase) entity;
                            this.trusted = livingentity;
                            this.trustedLastHurtBy = livingentity.getRevengeTarget();
                            this.trustedLastHurt = livingentity.getLastAttackedEntity();
                            int i = livingentity.getRevengeTimer();
                            if (i != this.timestamp && this.isSuitableTarget(this.trustedLastHurtBy, false)) {
                                return true;
                            }
                            if (i != this.timestamp && this.isSuitableTarget(this.trustedLastHurt, false)) {
                                return true;
                            }
                        }
                    }
                }
                return false;
            }
        }

        @Override
        public void startExecuting() {
            if (this.trustedLastHurtBy != null) {
                this.target = this.trustedLastHurtBy;
                if (this.trusted != null) {
                    this.timestamp = this.trusted.getRevengeTimer();
                }
            } else {
                this.target = this.trustedLastHurt;
                if (this.trusted != null) {
                    this.timestamp = this.trusted.getRevengeTimer();
                }
            }
            super.startExecuting();
        }
    }

    class StrollGoal extends EntityAIWander {

        public StrollGoal(int chance) {
            super(EntityRhinoceros.this, 1.0D, chance);
        }

        @Override
        public boolean shouldExecute() {
            return super.shouldExecute() && canRhinoWander();
        }

        @Override
        public boolean shouldContinueExecuting() {
            return super.shouldContinueExecuting() && canRhinoWander();
        }

        private boolean canRhinoWander() {
            for (UUID uuid : EntityRhinoceros.this.getTrustedUUIDs()) {
                if (uuid != null) {
                    return true;
                }
            }
            return false;
        }
    }
}
