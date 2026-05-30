package com.github.alexthe666.alexsmobs.entity;
import com.github.alexthe666.alexsmobs.misc.AMLootTables;
import javax.annotation.Nullable;
import net.minecraft.util.ResourceLocation;

import com.github.alexthe666.alexsmobs.config.AMConfig;
import com.github.alexthe666.alexsmobs.entity.ai.AnimalAIWanderRanged;
import com.github.alexthe666.alexsmobs.entity.ai.DirectPathNavigator;
import com.github.alexthe666.alexsmobs.entity.ai.MimiCubeAIRangedAttack;
import com.github.alexthe666.alexsmobs.item.AMItemRegistry;
import com.github.alexthe666.alexsmobs.misc.AMSoundRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.ai.EntityMoveHelper;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemShield;
import net.minecraft.item.ItemStack;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EntityMimicube extends EntityMob implements IRangedAttackMob {

    private static final DataParameter<Integer> ATTACK_TICK = EntityDataManager.createKey(EntityMimicube.class, DataSerializers.VARINT);
    private final MimiCubeAIRangedAttack aiArrowAttack = new MimiCubeAIRangedAttack(this, 1.0D, 10, 15.0F);
    private final EntityAIAttackMelee aiAttackOnCollide = new EntityAIAttackMelee(this, 1.2D, false);
    public float squishAmount;
    public float squishFactor;
    public float prevSquishFactor;
    public float leftSwapProgress = 0;
    public float prevLeftSwapProgress = 0;
    public float rightSwapProgress = 0;
    public float prevRightSwapProgress = 0;
    public float helmetSwapProgress = 0;
    public float prevHelmetSwapProgress = 0;
    public float prevAttackProgress;
    public float attackProgress;
    private boolean wasOnGround;
    private int eatingTicks;
    private boolean aggroed;

    public EntityMimicube(World world) {
        super(world);
        this.moveHelper = new MimicubeMoveHelper(this);
        this.navigator = new DirectPathNavigator(this, world);
        this.setCombatTask();
    }

    @Override
    public boolean getCanSpawnHere() {
        return AMEntityRegistry.rollSpawn(AMConfig.mimicubeSpawnRolls, this.getRNG(), AMEntityRegistry.AMSpawnReason.OTHER) && super.getCanSpawnHere();
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(ATTACK_TICK, 0);
    }

    @Override
    public boolean attackEntityAsMob(Entity entityIn) {
        this.dataManager.set(ATTACK_TICK, 5);
        return true;
    }

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(1, new AnimalAIWanderRanged(this, 60, 1.0D, 10, 7));
        this.tasks.addTask(2, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        this.tasks.addTask(2, new EntityAILookIdle(this));
        this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, true));
        this.targetTasks.addTask(2, new EntityAINearestAttackableTarget<>(this, EntityPlayer.class, true));
        this.targetTasks.addTask(3, new EntityAINearestAttackableTarget<>(this, EntityVillager.class, true));
    }

    public void setCombatTask() {
        if (this.world != null && !this.world.isRemote) {
            this.tasks.removeTask(this.aiAttackOnCollide);
            this.tasks.removeTask(this.aiArrowAttack);
            ItemStack itemstack = this.getHeldItemMainhand();
            if (itemstack.getItem() instanceof ItemBow) {
                int i = 10;
                if (this.world.getDifficulty() != EnumDifficulty.HARD) {
                    i = 30;
                }
                this.aiArrowAttack.setAttackCooldown(i);
                this.tasks.addTask(4, this.aiArrowAttack);
            } else {
                this.tasks.addTask(4, this.aiAttackOnCollide);
            }
        }
    }

    /**
     * 1.16 trident shot â€” 1.12 has no {@code TridentEntity}; uses ranged arrow with same velocity math.
     */
    public void attackEntityWithRangedAttackTrident(EntityLivingBase target, float distanceFactor) {
        this.attackEntityWithRangedAttack(target, distanceFactor);
    }

    @Override
    public void setSwingingArms(boolean swingingArms) {
    }

    @Override
    public void attackEntityWithRangedAttack(EntityLivingBase target, float distanceFactor) {
        ItemStack itemstack = this.findAmmo(this.getHeldItemMainhand());
        EntityTippedArrow arrow = new EntityTippedArrow(this.world, this);
        arrow.setEnchantmentEffectsFromEntity(this, distanceFactor);
        double d0 = target.posX - this.posX;
        double d1 = target.posY + (double) target.getEyeHeight() * 0.3333333333333333D - arrow.posY;
        double d2 = target.posZ - this.posZ;
        double d3 = MathHelper.sqrt(d0 * d0 + d2 * d2);
        arrow.shoot(d0, d1 + d3 * 0.2D, d2, 1.6F, (float) (14 - this.world.getDifficulty().getDifficultyId() * 4));
        if (itemstack.getItem() instanceof ItemBow) {
            arrow.setDamage(2.0D);
            int power = EnchantmentHelper.getEnchantmentLevel(net.minecraft.init.Enchantments.POWER, itemstack);
            if (power > 0) {
                arrow.setDamage(arrow.getDamage() + (double) power * 0.5D + 0.5D);
            }
        }
        this.playSound(SoundEvents.ENTITY_SKELETON_SHOOT, 1.0F, 1.0F / (this.getRNG().nextFloat() * 0.4F + 0.8F));
        this.world.spawnEntity(arrow);
    }

    protected ItemStack findAmmo(ItemStack weapon) {
        if (weapon.getItem() instanceof ItemBow) {
            for (EntityEquipmentSlot slot : EntityEquipmentSlot.values()) {
                ItemStack stack = this.getItemStackFromSlot(slot);
                if (stack.getItem() == Items.ARROW) {
                    return stack;
                }
            }
            return new ItemStack(Items.ARROW);
        }
        return ItemStack.EMPTY;
    }

    public void setItemStackToSlot(EntityEquipmentSlot slotIn, ItemStack stack) {
        if (slotIn == EntityEquipmentSlot.HEAD && !ItemStack.areItemStacksEqual(stack, this.getItemStackFromSlot(EntityEquipmentSlot.HEAD))) {
            helmetSwapProgress = 5;
            this.world.setEntityState(this, (byte) 45);
        }
        if (slotIn == EntityEquipmentSlot.MAINHAND && !ItemStack.areItemStacksEqual(stack, this.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND))) {
            rightSwapProgress = 5;
            this.world.setEntityState(this, (byte) 46);
        }
        if (slotIn == EntityEquipmentSlot.OFFHAND && !ItemStack.areItemStacksEqual(stack, this.getItemStackFromSlot(EntityEquipmentSlot.OFFHAND))) {
            leftSwapProgress = 5;
            this.world.setEntityState(this, (byte) 47);
        }
        super.setItemStackToSlot(slotIn, stack);
        if (!this.world.isRemote) {
            this.setCombatTask();
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void handleStatusUpdate(byte id) {
        super.handleStatusUpdate(id);
        if (id == 45) {
            helmetSwapProgress = 5;
        }
        if (id == 46) {
            rightSwapProgress = 5;
        }
        if (id == 47) {
            leftSwapProgress = 5;
        }
    }

    public boolean isActiveItemStackBlocking() {
        return this.getHeldItemMainhand().getItem() instanceof ItemShield || this.getHeldItemOffhand().getItem() instanceof ItemShield;
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        Entity trueSource = source.getTrueSource();
        if (trueSource instanceof EntityLivingBase) {
            EntityLivingBase attacker = (EntityLivingBase) trueSource;
            if (!attacker.getItemStackFromSlot(EntityEquipmentSlot.HEAD).isEmpty()) {
                this.setItemStackToSlot(EntityEquipmentSlot.HEAD, mimicStack(attacker.getItemStackFromSlot(EntityEquipmentSlot.HEAD)));
            }
            if (!attacker.getItemStackFromSlot(EntityEquipmentSlot.OFFHAND).isEmpty()) {
                this.setItemStackToSlot(EntityEquipmentSlot.OFFHAND, mimicStack(attacker.getItemStackFromSlot(EntityEquipmentSlot.OFFHAND)));
            }
            if (!attacker.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND).isEmpty()) {
                this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, mimicStack(attacker.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND)));
            }
        }
        return super.attackEntityFrom(source, amount);
    }

    private ItemStack mimicStack(ItemStack stack) {
        ItemStack copy = stack.copy();
        if (copy.isItemStackDamageable()) {
            copy.setItemDamage(copy.getMaxDamage());
        }
        return copy;
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        this.squishFactor += (this.squishAmount - this.squishFactor) * 0.5F;
        this.prevSquishFactor = this.squishFactor;
        this.prevHelmetSwapProgress = this.helmetSwapProgress;
        this.prevRightSwapProgress = this.rightSwapProgress;
        this.prevLeftSwapProgress = this.leftSwapProgress;
        this.prevAttackProgress = attackProgress;
        if (rightSwapProgress > 0F) {
            rightSwapProgress -= 0.5F;
        }
        if (leftSwapProgress > 0F) {
            leftSwapProgress -= 0.5F;
        }
        if (helmetSwapProgress > 0F) {
            helmetSwapProgress -= 0.5F;
        }
        if (this.onGround && !this.wasOnGround) {
            for (int j = 0; j < 8; ++j) {
                float f = this.rand.nextFloat() * ((float) Math.PI * 2F);
                float f1 = this.rand.nextFloat() * 0.5F + 0.5F;
                float f2 = MathHelper.sin(f) * 0.5F * f1;
                float f3 = MathHelper.cos(f) * 0.5F * f1;
                this.world.spawnParticle(EnumParticleTypes.ITEM_CRACK, this.posX + (double) f2, this.posY, this.posZ + (double) f3, 0.0D, 0.0D, 0.0D, Item.getIdFromItem(AMItemRegistry.MIMICREAM));
            }
            this.playSound(this.getSquishSound(), this.getSoundVolume(), ((this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 1.0F) / 0.8F);
            this.squishAmount = -0.35F;
        } else if (!this.onGround && this.wasOnGround) {
            this.squishAmount = 2F;
        }
        if (this.isInWater()) {
            this.motionY += 0.05D;
        }
        ItemStack offHand = this.getHeldItem(EnumHand.OFF_HAND);
        if (offHand.getItem() instanceof ItemFood && this.getHealth() < this.getMaxHealth()) {
            if (eatingTicks < 100) {
                for (int i = 0; i < 3; i++) {
                    double d2 = this.rand.nextGaussian() * 0.02D;
                    double d0 = this.rand.nextGaussian() * 0.02D;
                    double d1 = this.rand.nextGaussian() * 0.02D;
                    this.world.spawnParticle(EnumParticleTypes.ITEM_CRACK, this.posX + (double) (this.rand.nextFloat() * this.width) - (double) this.width * 0.5F, this.posY + this.height * 0.5F + (double) (this.rand.nextFloat() * this.height * 0.5F), this.posZ + (double) (this.rand.nextFloat() * this.width) - (double) this.width * 0.5F, d0, d1, d2, Item.getIdFromItem(offHand.getItem()));
                }
                if (eatingTicks % 6 == 0) {
                    this.playSound(SoundEvents.ENTITY_GENERIC_EAT, this.getSoundVolume(), this.getSoundPitch());
                }
                eatingTicks++;
            }
            if (eatingTicks == 100) {
                this.playSound(SoundEvents.ENTITY_PLAYER_BURP, this.getSoundVolume(), this.getSoundPitch());
                offHand.shrink(1);
                this.heal(5);
                eatingTicks = 0;
            }
        } else if (this.getHeldItemMainhand().getItem() instanceof ItemFood && this.getHealth() < this.getMaxHealth()) {
            if (eatingTicks < 100) {
                for (int i = 0; i < 3; i++) {
                    double d2 = this.rand.nextGaussian() * 0.02D;
                    double d0 = this.rand.nextGaussian() * 0.02D;
                    double d1 = this.rand.nextGaussian() * 0.02D;
                    ItemStack mainHand = this.getHeldItem(EnumHand.MAIN_HAND);
                    this.world.spawnParticle(EnumParticleTypes.ITEM_CRACK, this.posX + (double) (this.rand.nextFloat() * this.width) - (double) this.width * 0.5F, this.posY + this.height * 0.5F + (double) (this.rand.nextFloat() * this.height * 0.5F), this.posZ + (double) (this.rand.nextFloat() * this.width) - (double) this.width * 0.5F, d0, d1, d2, Item.getIdFromItem(mainHand.getItem()));
                }
                this.playSound(SoundEvents.ENTITY_GENERIC_EAT, this.getSoundVolume(), this.getSoundPitch());
                if (eatingTicks % 6 == 0) {
                    this.playSound(SoundEvents.ENTITY_GENERIC_EAT, this.getSoundVolume(), this.getSoundPitch());
                }
                eatingTicks++;
            }
            if (eatingTicks == 100) {
                this.playSound(SoundEvents.ENTITY_PLAYER_BURP, this.getSoundVolume(), this.getSoundPitch());
                this.getHeldItemMainhand().shrink(1);
                this.heal(5);
            }
        } else {
            eatingTicks = 0;
        }
        this.wasOnGround = this.onGround;
        this.alterSquishAmount();
        EntityLivingBase target = this.getAttackTarget();
        if (target != null && this.getDistanceSq(target) < 144D) {
            this.moveHelper.setMoveTo(target.posX, target.posY, target.posZ, 1.0D);
            this.wasOnGround = true;
        }
        if (this.dataManager.get(ATTACK_TICK) > 0) {
            if (this.dataManager.get(ATTACK_TICK) == 2 && this.getAttackTarget() != null && this.getDistance(this.getAttackTarget()) < 2.3D) {
                super.attackEntityAsMob(this.getAttackTarget());
            }
            this.dataManager.set(ATTACK_TICK, this.dataManager.get(ATTACK_TICK) - 1);
            if (attackProgress < 3F) {
                attackProgress++;
            }
        } else {
            if (attackProgress > 0F) {
                attackProgress--;
            }
        }
    }

    protected float getDropChance(EntityEquipmentSlot slotIn) {
        return 0;
    }

    private SoundEvent getSquishSound() {
        return AMSoundRegistry.MIMICUBE_JUMP;
    }

    private SoundEvent getJumpSound() {
        return AMSoundRegistry.MIMICUBE_JUMP;
    }

    @Override
    protected void jump() {
        this.motionY = this.getJumpUpwardsMotion();
        this.isAirBorne = true;
    }

    protected int getJumpDelay() {
        return this.rand.nextInt(20) + 10;
    }

    protected void alterSquishAmount() {
        this.squishAmount *= 0.6F;
    }

    public boolean shouldShoot() {
        return this.getHeldItemMainhand().getItem() instanceof ItemBow;
    }

    public void setAggroed(boolean aggroed) {
        this.aggroed = aggroed;
    }

    public boolean isAggroed() {
        return aggroed;
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(30.0D);
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(32.0D);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(2.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.45D);
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return AMSoundRegistry.MIMICUBE_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return AMSoundRegistry.MIMICUBE_HURT;
    }
    @Override
    @Nullable
    protected ResourceLocation getLootTable() {
        return AMLootTables.MIMICUBE;
    }

    private class MimicubeMoveHelper extends EntityMoveHelper {
        private final EntityMimicube slime;
        private int jumpDelay;

        MimicubeMoveHelper(EntityMimicube slimeIn) {
            super(slimeIn);
            this.slime = slimeIn;
        }

        @Override
        public void onUpdateMoveHelper() {
            if (this.slime.onGround) {
                this.slime.setAIMoveSpeed((float) (this.speed * this.slime.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue()));
                if (this.jumpDelay-- <= 0 && this.action != Action.WAIT) {
                    this.jumpDelay = this.slime.getJumpDelay();
                    if (this.slime.getAttackTarget() != null) {
                        this.jumpDelay /= 3;
                    }
                    this.slime.getJumpHelper().setJumping();
                    this.slime.playSound(this.slime.getJumpSound(), this.slime.getSoundVolume(), this.slime.getSoundPitch());
                } else {
                    this.slime.moveStrafing = 0.0F;
                    this.slime.moveForward = 0.0F;
                    this.slime.setAIMoveSpeed(0.0F);
                }
            }
            super.onUpdateMoveHelper();
        }
    }
}
