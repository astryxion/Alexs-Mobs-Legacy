package com.github.alexthe666.alexsmobs.entity;

import com.github.alexthe666.alexsmobs.config.AMConfig;
import com.github.alexthe666.alexsmobs.entity.ai.AnimalAIFleeAdult;
import com.github.alexthe666.alexsmobs.entity.ai.AnimalAIWanderRanged;
import com.github.alexthe666.alexsmobs.entity.ai.CreatureAITargetItems;
import com.github.alexthe666.alexsmobs.entity.ai.EntityAINearestTarget3D;
import com.github.alexthe666.alexsmobs.entity.ai.KomodoDragonAIBreed;
import com.github.alexthe666.alexsmobs.entity.ai.TameableAIRide;
import com.github.alexthe666.alexsmobs.entity.ai.TameableAITempt;
import com.github.alexthe666.alexsmobs.item.AMItemRegistry;
import com.github.alexthe666.alexsmobs.misc.AMSoundRegistry;
import com.github.alexthe666.alexsmobs.misc.AMTagRegistry;
import com.google.common.base.Predicate;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.UUID;

public class EntityKomodoDragon extends EntityTameable implements ITargetsDroppedItems {

    public int slaughterCooldown = 0;
    public int timeUntilSpit = this.rand.nextInt(12000) + 24000;
    private int riderAttackCooldown = 0;

    public static final Predicate<EntityKomodoDragon> HURT_OR_BABY = new Predicate<EntityKomodoDragon>() {
        @Override
        public boolean apply(@Nullable EntityKomodoDragon komodo) {
            return komodo != null && (komodo.isChild() || komodo.getHealth() <= 0.7F * komodo.getMaxHealth());
        }
    };

    public EntityKomodoDragon(World worldIn) {
        super(worldIn);
        this.setSize(1.3F, 0.6F);
    }

    public static boolean canKomodoDragonSpawn(World world, BlockPos pos) {
        return AMTagRegistry.blockInTag(AMTagRegistry.KOMODO_DRAGON_SPAWNS, world.getBlockState(pos.down()).getBlock())
                && world.getLight(pos) > 8;
    }

    @Override
    public boolean getCanSpawnHere() {
        return AMEntityRegistry.rollSpawn(AMConfig.komodoDragonSpawnRolls, this.getRNG(), AMEntityRegistry.AMSpawnReason.OTHER) && super.getCanSpawnHere();
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        AMEntityRegistry.registerAttributeIfAbsent(this, SharedMonsterAttributes.ARMOR);
        AMEntityRegistry.registerAttributeIfAbsent(this, SharedMonsterAttributes.ATTACK_DAMAGE);
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(30.0D);
        this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(0.0D);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(4.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.23D);
    }

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(2, new EntityAIAttackMelee(this, 2.0D, false));
        this.tasks.addTask(2, new TameableAIRide(this, 2.0D));
        this.tasks.addTask(4, new TameableAITempt(this, 1.1D, Items.ROTTEN_FLESH, false));
        this.tasks.addTask(4, new AnimalAIFleeAdult(this, 1.25D, 32));
        this.tasks.addTask(5, new KomodoDragonAIBreed(this, 1.0D));
        this.tasks.addTask(6, new AnimalAIWanderRanged(this, 120, 1.0D, 50, 7));
        this.tasks.addTask(7, new EntityAIWatchClosest(this, EntityPlayer.class, 6.0F));
        this.tasks.addTask(8, new EntityAILookIdle(this));
        this.targetTasks.addTask(1, new EntityAIOwnerHurtByTarget(this));
        this.targetTasks.addTask(2, new EntityAIOwnerHurtTarget(this));
        this.targetTasks.addTask(3, new EntityAIHurtByTarget(this, true));
        this.targetTasks.addTask(4, new CreatureAITargetItems(this, false));
        this.targetTasks.addTask(6, new EntityAINearestAttackableTarget<EntityKomodoDragon>(this, EntityKomodoDragon.class, 50, true, false, HURT_OR_BABY));
        this.targetTasks.addTask(7, new EntityAINearestAttackableTarget<EntityPlayer>(this, EntityPlayer.class, 150, true, true, null));
        this.targetTasks.addTask(8, new EntityAINearestTarget3D(this, EntityLivingBase.class, 180, false, true, AMEntityRegistry.buildPredicateFromTag(AMTagRegistry.KOMODO_DRAGON_TARGETS)));
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (this.isEntityInvulnerable(source)) {
            return false;
        } else {
            Entity entity = source.getTrueSource();
            if (entity != null && this.isTamed() && !(entity instanceof EntityPlayer) && !(entity instanceof EntityArrow)) {
                amount = (amount + 1.0F) / 3.0F;
            }
            return super.attackEntityFrom(source, amount);
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return AMSoundRegistry.KOMODO_DRAGON_IDLE;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return AMSoundRegistry.KOMODO_DRAGON_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return AMSoundRegistry.KOMODO_DRAGON_HURT;
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        if (compound.hasKey("SpitTime")) {
            this.timeUntilSpit = compound.getInteger("SpitTime");
        }
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setInteger("SpitTime", this.timeUntilSpit);
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        return isTamed() && stack.getItem() == Items.ROTTEN_FLESH;
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        if (slaughterCooldown > 0) {
            slaughterCooldown--;
        }
        if (!this.world.isRemote && this.isEntityAlive() && !this.isChild() && --this.timeUntilSpit <= 0) {
            this.entityDropItem(new ItemStack(AMItemRegistry.KOMODO_SPIT), 0.0F);
            this.timeUntilSpit = this.rand.nextInt(12000) + 24000;
        }
        if (riderAttackCooldown > 0) {
            riderAttackCooldown--;
        }
        if (this.getControllingPassenger() instanceof EntityPlayer) {
            EntityPlayer rider = (EntityPlayer) this.getControllingPassenger();
            if (rider.getLastAttackedEntity() != null && this.getDistance(rider.getLastAttackedEntity()) < this.width + 3F && !this.isOnSameTeam(rider.getLastAttackedEntity())) {
                UUID preyUUID = rider.getLastAttackedEntity().getUniqueID();
                if (!this.getUniqueID().equals(preyUUID) && riderAttackCooldown == 0) {
                    attackEntityAsMob(rider.getLastAttackedEntity());
                    riderAttackCooldown = 20;
                }
            }
        }
    }

    @Override
    public boolean isOnSameTeam(Entity entityIn) {
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
        if (super.attackEntityAsMob(entityIn)) {
            if (entityIn instanceof EntityLivingBase) {
                int i = 5;
                if (this.world.getDifficulty() == EnumDifficulty.NORMAL) {
                    i = 10;
                } else if (this.world.getDifficulty() == EnumDifficulty.HARD) {
                    i = 20;
                }
                ((EntityLivingBase) entityIn).addPotionEffect(new PotionEffect(MobEffects.POISON, i * 20, 0));
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean isPotionApplicable(PotionEffect potioneffectIn) {
        if (potioneffectIn.getPotion() == MobEffects.POISON) {
            return false;
        }
        return super.isPotionApplicable(potioneffectIn);
    }

    @Nullable
    @Override
    public Entity getControllingPassenger() {
        for (Entity passenger : this.getPassengers()) {
            if (passenger instanceof EntityPlayer) {
                return passenger;
            }
        }
        return null;
    }

    @Override
    public void updatePassenger(Entity passenger) {
        if (this.getPassengers().contains(passenger)) {
            float radius = 0;
            float angle = (0.01745329251F * this.renderYawOffset);
            double extraX = radius * MathHelper.sin((float) (Math.PI + angle));
            double extraZ = radius * MathHelper.cos(angle);
            passenger.setPosition(this.posX + extraX, this.posY + this.getMountedYOffset() + passenger.getYOffset(), this.posZ + extraZ);
        }
    }

    public double getMountedYOffset() {
        float f = Math.min(0.25F, this.limbSwingAmount);
        float f1 = this.limbSwing;
        return (double) this.height - 0.2D + (double) (0.12F * MathHelper.cos(f1 * 0.7F) * 0.7F * f);
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        ItemStack itemstack = player.getHeldItem(hand);
        Item item = itemstack.getItem();

        if (item == Items.ROTTEN_FLESH && !isTamed()) {
            int size = itemstack.getCount();
            int tameAmount = 58 + rand.nextInt(16);
            if (size > tameAmount) {
                this.setTamed(true);
                this.setOwnerId(player.getUniqueID());
            }
            itemstack.shrink(size);
            return true;
        }
        if (isTamed() && isOwner(player)) {
            if (isBreedingItem(itemstack)) {
                this.setInLove(player);
                if (!player.capabilities.isCreativeMode) {
                    itemstack.shrink(1);
                }
                return true;
            }
            if (!player.isSneaking() && !isBreedingItem(itemstack) && !this.isChild()) {
                player.startRiding(this);
                return true;
            }
        }
        return super.processInteract(player, hand);
    }

    @Override
    protected float getWaterSlowDown() {
        return 0.98F;
    }

    @Override
    public void setAttackTarget(@Nullable EntityLivingBase entitylivingbaseIn) {
        if (!this.isChild() || slaughterCooldown > 0) {
            super.setAttackTarget(entitylivingbaseIn);
        }
    }

    @Nullable
    @Override
    public EntityAgeable createChild(EntityAgeable ageable) {
        return (EntityKomodoDragon) AMEntityRegistry.KOMODO_DRAGON.newInstance(this.world);
    }

    @Override
    public boolean canTargetItem(ItemStack stack) {
        Item item = stack.getItem();
        if (item == Items.ROTTEN_FLESH) {
            return true;
        }
        return item instanceof ItemFood && ((ItemFood) item).isWolfsFavoriteMeat();
    }

    @Override
    public void onGetItem(EntityItem e) {
        this.heal(10);
    }
}
