package com.github.alexthe666.alexsmobs.entity;
import com.github.alexthe666.alexsmobs.misc.AMLootTables;

import com.github.alexthe666.alexsmobs.config.AMConfig;
import com.github.alexthe666.alexsmobs.entity.ai.AnimalAIHurtByTargetNotBaby;
import com.github.alexthe666.alexsmobs.entity.ai.AnimalAIPanicBaby;
import com.github.alexthe666.alexsmobs.entity.ai.AnimalAIWanderRanged;
import com.github.alexthe666.alexsmobs.entity.ai.MooseAIJostle;
import com.github.alexthe666.alexsmobs.item.AMItemRegistry;
import com.github.alexthe666.alexsmobs.misc.AMTagRegistry;
import com.github.alexthe666.alexsmobs.misc.AMSoundRegistry;
import com.github.alexthe666.citadel.animation.Animation;
import com.github.alexthe666.citadel.animation.IAnimatedEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
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
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import net.minecraft.util.ResourceLocation;
import com.google.common.base.Optional;
import java.util.UUID;

public class EntityMoose extends EntityAnimal implements IAnimatedEntity {

    public static final Animation ANIMATION_EAT_GRASS = Animation.create(30);
    public static final Animation ANIMATION_ATTACK = Animation.create(15);
    private static final int DAY = 24000;
    private static final DataParameter<Boolean> ANTLERED = EntityDataManager.createKey(EntityMoose.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> JOSTLING = EntityDataManager.createKey(EntityMoose.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Float> JOSTLE_ANGLE = EntityDataManager.createKey(EntityMoose.class, DataSerializers.FLOAT);
    private static final DataParameter<Optional<UUID>> JOSTLER_UUID = EntityDataManager.createKey(EntityMoose.class, DataSerializers.OPTIONAL_UNIQUE_ID);
    private static final DataParameter<Boolean> SNOWY = EntityDataManager.createKey(EntityMoose.class, DataSerializers.BOOLEAN);
    public float prevJostleAngle;
    public float prevJostleProgress;
    public float jostleProgress;
    public boolean jostleDirection;
    public int jostleTimer = 0;
    public boolean instantlyTriggerJostleAI = false;
    public int jostleCooldown = 100 + rand.nextInt(40);
    public int timeUntilAntlerDrop = 7 * DAY + this.rand.nextInt(3) * DAY;
    private int animationTick;
    private Animation currentAnimation;
    private int snowTimer = 0;
    private boolean permSnow = false;

    public EntityMoose(World worldIn) {
        super(worldIn);
        this.setSize(1.4F, 2.0F);
        this.stepHeight = 1.1F;
    }

    public static boolean canMooseSpawn(World world, BlockPos pos) {
        net.minecraft.block.state.IBlockState blockstate = world.getBlockState(pos.down());
        return (blockstate.getBlock() == Blocks.GRASS || blockstate.getBlock() == Blocks.SNOW || blockstate.getBlock() == Blocks.SNOW_LAYER)
                && world.getLight(pos) > 8;
    }

    @Override
    public boolean getCanSpawnHere() {
        return AMEntityRegistry.rollSpawn(AMConfig.mooseSpawnRolls, this.getRNG(), AMEntityRegistry.AMSpawnReason.OTHER) && super.getCanSpawnHere();
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        AMEntityRegistry.registerAttributeIfAbsent(this, SharedMonsterAttributes.ATTACK_DAMAGE);
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(55.0D);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(7.5D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.25D);
        this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(0.5D);
    }

    @Override
    protected float getWaterSlowDown() {
        return 0.98F;
    }

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(1, new EntityAISwimming(this));
        this.tasks.addTask(2, new MooseAIJostle(this));
        this.tasks.addTask(3, new AnimalAIPanicBaby(this, 1.25D));
        this.tasks.addTask(4, new EntityAIAttackMelee(this, 1.1D, true));
        this.tasks.addTask(5, new EntityAIMate(this, 1.0D));
        this.tasks.addTask(6, new EntityAIFollowParent(this, 1.1D));
        this.tasks.addTask(7, new EntityAITempt(this, 1.1D, Items.AIR, false) {
            @Override
            protected boolean isTempting(ItemStack stack) {
                return AMTagRegistry.itemInTag(AMTagRegistry.MOOSE_BREEDABLES, stack.getItem());
            }
        });
        this.tasks.addTask(7, new AnimalAIWanderRanged(this, 120, 1.0D, 14, 7));
        this.tasks.addTask(8, new EntityAIWatchClosest(this, EntityPlayer.class, 15.0F));
        this.tasks.addTask(8, new EntityAILookIdle(this));
        this.targetTasks.addTask(1, new AnimalAIHurtByTargetNotBaby(this));
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void handleStatusUpdate(byte id) {
        if (id == 6) {
            for (int i = 0; i < 7; ++i) {
                double d0 = this.rand.nextGaussian() * 0.02D;
                double d1 = this.rand.nextGaussian() * 0.02D;
                double d2 = this.rand.nextGaussian() * 0.02D;
                this.world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, this.posX + (this.rand.nextDouble() - 0.5D) * this.width * 2.0D, this.posY + this.rand.nextDouble() * this.height * 0.5D + 0.5D, this.posZ + (this.rand.nextDouble() - 0.5D) * this.width * 2.0D, d0, d1, d2);
            }
        } else {
            super.handleStatusUpdate(id);
        }
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        if (AMTagRegistry.itemInTag(AMTagRegistry.MOOSE_BREEDABLES, stack.getItem()) && !this.isInLove() && this.getGrowingAge() == 0) {
            if (this.getRNG().nextInt(5) == 0) {
                return true;
            } else {
                this.world.setEntityState(this, (byte) 6);
                return false;
            }
        }
        return false;
    }

    @Override
    public void setAttackTarget(@Nullable EntityLivingBase entitylivingbaseIn) {
        if (!this.isChild()) {
            super.setAttackTarget(entitylivingbaseIn);
        }
    }

    @Override
    public boolean attackEntityAsMob(Entity entityIn) {
        if (this.getAnimation() == NO_ANIMATION) {
            this.setAnimation(ANIMATION_ATTACK);
        }
        return true;
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(ANTLERED, Boolean.TRUE);
        this.dataManager.register(JOSTLING, Boolean.FALSE);
        this.dataManager.register(SNOWY, Boolean.FALSE);
        this.dataManager.register(JOSTLE_ANGLE, Float.valueOf(0F));
        this.dataManager.register(JOSTLER_UUID, Optional.absent());
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.setSnowy(compound.getBoolean("Snowy"));
        if (compound.hasKey("AntlerTime")) {
            this.timeUntilAntlerDrop = compound.getInteger("AntlerTime");
        }
        this.setAntlered(compound.getBoolean("Antlered"));
        this.jostleCooldown = compound.getInteger("JostlingCooldown");
        this.permSnow = compound.getBoolean("SnowPerm");
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setBoolean("Snowy", this.isSnowy());
        compound.setBoolean("SnowPerm", this.permSnow);
        compound.setInteger("AntlerTime", this.timeUntilAntlerDrop);
        compound.setBoolean("Antlered", this.isAntlered());
        compound.setInteger("JostlingCooldown", this.jostleCooldown);
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        prevJostleProgress = jostleProgress;
        prevJostleAngle = this.getJostleAngle();
        if (this.isJostling() && jostleProgress < 5F) {
            jostleProgress++;
        }
        if (!this.isJostling() && jostleProgress > 0F) {
            jostleProgress--;
        }
        if (jostleCooldown > 0) {
            jostleCooldown--;
        }
        if (!world.isRemote && this.getAnimation() == NO_ANIMATION && getRNG().nextInt(120) == 0 && (this.getAttackTarget() == null || !this.getAttackTarget().isEntityAlive()) && !this.isJostling() && this.getJostlingPartnerUUID() == null) {
            if (world.getBlockState(this.getPosition().down()).getBlock() == Blocks.GRASS && getRNG().nextInt(3) == 0) {
                this.setAnimation(ANIMATION_EAT_GRASS);
            }
        }
        if (timeUntilAntlerDrop > 0) {
            timeUntilAntlerDrop--;
        }
        if (timeUntilAntlerDrop == 0) {
            if (this.isAntlered()) {
                this.setAntlered(false);
                this.entityDropItem(new ItemStack(AMItemRegistry.MOOSE_ANTLER), 0.0F);
                timeUntilAntlerDrop = 2 * DAY + this.rand.nextInt(3) * DAY;
            } else {
                this.setAntlered(true);
                timeUntilAntlerDrop = 7 * DAY + this.rand.nextInt(3) * DAY;
            }
        }
        if (this.getAttackTarget() != null && this.getAttackTarget().isEntityAlive()) {
            if (this.isJostling()) {
                this.setJostling(false);
            }
            if (!world.isRemote && this.getAnimation() == ANIMATION_ATTACK && this.getAnimationTick() == 8) {
                float dmg = (float) this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getBaseValue();
                if (!isAntlered()) {
                    dmg = 3;
                }
                if (this.getAttackTarget() instanceof EntityWolf || this.getAttackTarget() instanceof EntityOrca) {
                    dmg = 2;
                }
                this.getAttackTarget().knockBack(this, 1F, this.getAttackTarget().posX - this.posX, this.getAttackTarget().posZ - this.posZ);
                this.getAttackTarget().attackEntityFrom(DamageSource.causeMobDamage(this), dmg);
            }
        }
        if (snowTimer > 0) {
            snowTimer--;
        }
        if (snowTimer == 0 && !world.isRemote) {
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
        AMEntityRegistry.updateAnimations(this);
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (this.isEntityInvulnerable(source)) {
            return false;
        } else {
            Entity entity = source.getTrueSource();
            if (entity instanceof EntityOrca || entity instanceof EntityWolf) {
                amount = (amount + 1.0F) * 3.0F;
            }
            return super.attackEntityFrom(source, amount);
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return AMSoundRegistry.MOOSE_IDLE;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return AMSoundRegistry.MOOSE_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return AMSoundRegistry.MOOSE_HURT;
    }
    @Override
    @Nullable
    protected ResourceLocation getLootTable() {
        return AMLootTables.MOOSE;
    }

    public boolean isAntlered() {
        return this.dataManager.get(ANTLERED).booleanValue();
    }

    public void setAntlered(boolean anters) {
        this.dataManager.set(ANTLERED, Boolean.valueOf(anters));
    }

    public boolean isJostling() {
        return this.dataManager.get(JOSTLING).booleanValue();
    }

    public void setJostling(boolean jostle) {
        this.dataManager.set(JOSTLING, Boolean.valueOf(jostle));
    }

    public float getJostleAngle() {
        return this.dataManager.get(JOSTLE_ANGLE).floatValue();
    }

    public void setJostleAngle(float scale) {
        this.dataManager.set(JOSTLE_ANGLE, Float.valueOf(scale));
    }

    @Nullable
    public UUID getJostlingPartnerUUID() {
        return this.dataManager.get(JOSTLER_UUID).orNull();
    }

    public void setJostlingPartnerUUID(@Nullable UUID uniqueId) {
        this.dataManager.set(JOSTLER_UUID, Optional.fromNullable(uniqueId));
    }

    public boolean isSnowy() {
        return this.dataManager.get(SNOWY).booleanValue();
    }

    public void setSnowy(boolean snowy) {
        this.dataManager.set(SNOWY, Boolean.valueOf(snowy));
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        ItemStack itemstack = player.getHeldItem(hand);
        Item item = itemstack.getItem();
        if (item == Item.getItemFromBlock(Blocks.SNOW) && !this.isSnowy() && !world.isRemote) {
            if (!player.capabilities.isCreativeMode) {
                itemstack.shrink(1);
            }
            this.permSnow = true;
            this.setSnowy(true);
            this.playSound(SoundEvents.BLOCK_SNOW_PLACE, this.getSoundVolume(), this.getSoundPitch());
            return true;
        }
        if (item instanceof ItemTool && ((ItemTool) item).getToolClasses(itemstack).contains("shovel") && this.isSnowy() && !world.isRemote) {
            this.permSnow = false;
            if (!player.capabilities.isCreativeMode) {
                itemstack.damageItem(1, player);
            }
            this.setSnowy(false);
            this.playSound(SoundEvents.BLOCK_SNOW_BREAK, this.getSoundVolume(), this.getSoundPitch());
            return true;
        }
        return super.processInteract(player, hand);
    }

    @Nullable
    public Entity getJostlingPartner() {
        UUID id = getJostlingPartnerUUID();
        if (id != null && !world.isRemote && world instanceof WorldServer) {
            return ((WorldServer) world).getEntityFromUuid(id);
        }
        return null;
    }

    public void setJostlingPartner(@Nullable Entity jostlingPartner) {
        if (jostlingPartner == null) {
            this.setJostlingPartnerUUID(null);
        } else {
            this.setJostlingPartnerUUID(jostlingPartner.getUniqueID());
        }
    }

    public void pushBackJostling(EntityMoose entityMoose, float strength) {
        applyKnockbackFromMoose(strength, entityMoose.posX - this.posX, entityMoose.posZ - this.posZ);
    }

    private void applyKnockbackFromMoose(float strength, double ratioX, double ratioZ) {
        net.minecraftforge.event.entity.living.LivingKnockBackEvent event = net.minecraftforge.common.ForgeHooks.onLivingKnockBack(this, null, strength, ratioX, ratioZ);
        if (event.isCanceled()) {
            return;
        }
        strength = event.getStrength();
        ratioX = event.getRatioX();
        ratioZ = event.getRatioZ();
        if (!(strength <= 0.0F)) {
            this.isAirBorne = true;
            double d0 = ratioX;
            double d1 = ratioZ;
            double len = Math.sqrt(d0 * d0 + d1 * d1);
            if (len > 0.0D) {
                d0 /= len;
                d1 /= len;
            }
            this.motionX = this.motionX / 2.0D - d0 * strength;
            this.motionY = 0.3D;
            this.motionZ = this.motionZ / 2.0D - d1 * strength;
        }
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
    }

    @Override
    public Animation[] getAnimations() {
        return new Animation[]{ANIMATION_ATTACK, ANIMATION_EAT_GRASS};
    }

    @Nullable
    @Override
    public EntityAgeable createChild(EntityAgeable ageable) {
        return (EntityMoose) AMEntityRegistry.MOOSE.newInstance(this.world);
    }

    public boolean canJostleWith(EntityMoose moose) {
        return !moose.isJostling() && moose.isAntlered() && moose.getAnimation() == NO_ANIMATION && !moose.isChild() && moose.getJostlingPartnerUUID() == null && moose.jostleCooldown == 0;
    }

    public void playJostleSound() {
        this.playSound(AMSoundRegistry.MOOSE_JOSTLE, this.getSoundPitch(), this.getSoundVolume());
    }
}
