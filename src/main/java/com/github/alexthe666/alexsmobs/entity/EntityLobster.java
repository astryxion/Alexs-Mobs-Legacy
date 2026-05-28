package com.github.alexthe666.alexsmobs.entity;

import com.github.alexthe666.alexsmobs.config.AMConfig;
import com.github.alexthe666.alexsmobs.entity.ai.AnimalAIFindWater;
import com.github.alexthe666.alexsmobs.entity.ai.AnimalAILeaveWater;
import com.github.alexthe666.alexsmobs.entity.ai.BottomFeederAIWander;
import com.github.alexthe666.alexsmobs.entity.ai.SemiAquaticPathNavigator;
import com.github.alexthe666.alexsmobs.item.AMItemRegistry;
import com.github.alexthe666.alexsmobs.misc.AMSoundRegistry;
import com.github.alexthe666.alexsmobs.misc.AMTagRegistry;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class EntityLobster extends EntityCreature implements ISemiAquatic {

    private static final DataParameter<Boolean> FROM_BUCKET = EntityDataManager.createKey(EntityLobster.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> ATTACK_TICK = EntityDataManager.createKey(EntityLobster.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> VARIANT = EntityDataManager.createKey(EntityLobster.class, DataSerializers.VARINT);
    public float attackProgress;
    public float prevAttackProgress;
    private int attackCooldown = 0;

    public EntityLobster(World world) {
        super(world);
        this.setPathPriority(PathNodeType.WATER, 0.0F);
        this.setPathPriority(PathNodeType.WATER, 0.0F);
    }

    @Override
    public int getMaxSpawnedInChunk() {
        return 3;
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        AMEntityRegistry.registerAttributeIfAbsent(this, SharedMonsterAttributes.ATTACK_DAMAGE);
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(5.0D);
        this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(2.0D);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(2.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.15D);
    }

    @Override
    public boolean getCanSpawnHere() {
        return AMEntityRegistry.rollSpawn(AMConfig.lobsterSpawnRolls, this.getRNG(), AMEntityRegistry.AMSpawnReason.OTHER);
    }

    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }

    @Override
    public boolean isPushedByWater() {
        return false;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return AMSoundRegistry.LOBSTER_HURT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return AMSoundRegistry.LOBSTER_HURT;
    }

    @Override
    public boolean isNotColliding() {
        return this.world.checkNoEntityCollision(this.getEntityBoundingBox(), this);
    }

    public static String getVariantName(int variant) {
        switch (variant) {
            case 1:
                return "blue";
            case 2:
                return "yellow";
            case 3:
                return "redblue";
            case 4:
                return "black";
            case 5:
                return "white";
            default:
                return "red";
        }
    }

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(1, new AnimalAIFindWater(this));
        this.tasks.addTask(1, new AnimalAILeaveWater(this));
        this.tasks.addTask(3, new BottomFeederAIWander(this, 1.0D, 10, 50));
        this.tasks.addTask(4, new EntityAILookIdle(this));
        this.tasks.addTask(5, new EntityAIWatchClosest(this, EntityPlayer.class, 6.0F));
        this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, true));
    }

    @Override
    public EnumCreatureAttribute getCreatureAttribute() {
        return EnumCreatureAttribute.ARTHROPOD;
    }

    @Override
    public void travel(float strafe, float vertical, float forward) {
        if (!this.world.isRemote && this.isInWater()) {
            this.moveRelative(strafe, vertical, forward, this.getAIMoveSpeed());
            this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
            if (this.isJumping) {
                this.motionX *= 1.4D;
                this.motionZ *= 1.4D;
                this.motionY += 0.72D;
            } else {
                this.motionX *= 0.4D;
                this.motionY *= 0.4D;
                this.motionZ *= 0.4D;
                this.motionY += -0.08D;
            }
        } else {
            super.travel(strafe, vertical, forward);
        }
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(VARIANT, 0);
        this.dataManager.register(ATTACK_TICK, 0);
        this.dataManager.register(FROM_BUCKET, false);
    }

    protected ItemStack getFishBucket() {
        ItemStack stack = new ItemStack(AMItemRegistry.LOBSTER_BUCKET);
        if (this.hasCustomName()) {
            stack.setStackDisplayName(this.getCustomNameTag());
        }
        return stack;
    }

    protected void setBucketData(ItemStack bucket) {
        if (this.hasCustomName()) {
            bucket.setStackDisplayName(this.getCustomNameTag());
        }
        NBTTagCompound tag = bucket.getTagCompound();
        if (tag == null) {
            tag = new NBTTagCompound();
            bucket.setTagCompound(tag);
        }
        tag.setInteger("BucketVariantTag", this.getVariant());
    }

    @Override
    protected boolean canDespawn() {
        return !this.isFromBucket() && super.canDespawn();
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        ItemStack itemstack = player.getHeldItem(hand);
        if (itemstack.getItem() == Items.WATER_BUCKET && this.isEntityAlive()) {
            this.playSound(SoundEvents.ITEM_BUCKET_FILL, 1.0F, 1.0F);
            if (!player.capabilities.isCreativeMode) {
                itemstack.shrink(1);
            }
            ItemStack itemstack1 = this.getFishBucket();
            this.setBucketData(itemstack1);
            if (!this.world.isRemote) {
                            }

            if (itemstack.isEmpty()) {
                player.setHeldItem(hand, itemstack1);
            } else if (!player.inventory.addItemStackToInventory(itemstack1)) {
                player.dropItem(itemstack1, false);
            }

            this.setDead();
            return true;
        } else {
            return super.processInteract(player, hand);
        }
    }

    public float getBlockPathWeight(BlockPos pos) {
        IBlockState below = this.world.getBlockState(pos.down());
        IBlockState at = this.world.getBlockState(pos);
        return below.getMaterial() != Material.WATER && at.getMaterial() == Material.WATER ? 10.0F : super.getBlockPathWeight(pos);
    }

    public boolean attackEntityAsMob(EntityLivingBase entityIn) {
        this.dataManager.set(ATTACK_TICK, 5);
        return super.attackEntityAsMob(entityIn);
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        prevAttackProgress = attackProgress;
        if (this.dataManager.get(ATTACK_TICK) > 0) {
            if (attackProgress == 3) {
                this.playSound(AMSoundRegistry.LOBSTER_ATTACK, this.getSoundVolume(), this.getSoundPitch());
            }
            if (this.dataManager.get(ATTACK_TICK) == 2 && this.getAttackTarget() != null && this.getDistance(this.getAttackTarget()) < 1.3D) {
                this.getAttackTarget().attackEntityFrom(DamageSource.causeMobDamage(this), 2);
            }
            this.dataManager.set(ATTACK_TICK, this.dataManager.get(ATTACK_TICK) - 1);
            if (attackProgress < 5F) {
                attackProgress++;
            }
        } else {
            if (attackProgress > 0F) {
                attackProgress--;
            }
        }
        if (attackCooldown > 0) {
            attackCooldown--;
        }
        if (this.getAttackTarget() != null && this.getDistance(this.getAttackTarget()) <= 1F && attackCooldown == 0) {
            this.getLookHelper().setLookPositionWithEntity(this.getAttackTarget(), 180F, 20F);
            attackEntityAsMob(this.getAttackTarget());
            attackCooldown = 20;
        }
    }

    public int getVariant() {
        return this.dataManager.get(VARIANT);
    }

    public void setVariant(int variant) {
        this.dataManager.set(VARIANT, variant);
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setInteger("Variant", this.getVariant());
        compound.setBoolean("FromBucket", this.isFromBucket());
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.setVariant(compound.getInteger("Variant"));
        this.setFromBucket(compound.getBoolean("FromBucket"));
    }

    private boolean isFromBucket() {
        return this.dataManager.get(FROM_BUCKET);
    }

    public void setFromBucket(boolean fromBucket) {
        this.dataManager.set(FROM_BUCKET, fromBucket);
    }

    @Nullable
    @Override
    public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData livingdata) {
        float variantChange = this.getRNG().nextFloat();
        if (variantChange <= 0.00001F) {
            this.setVariant(5);
        } else if (variantChange <= 0.00002F) {
            this.setVariant(4);
        } else if (variantChange <= 0.05F) {
            this.setVariant(3);
        } else if (variantChange <= 0.1F) {
            this.setVariant(2);
        } else if (variantChange <= 0.25F) {
            this.setVariant(1);
        } else {
            this.setVariant(0);
        }
        return super.onInitialSpawn(difficulty, livingdata);
    }

    @Override
    protected PathNavigate createNavigator(World worldIn) {
        return new SemiAquaticPathNavigator(this, worldIn) {
            public boolean canStandPosition(BlockPos pos) {
                return this.world.getBlockState(pos).getMaterial().isSolid();
            }
        };
    }

    @Override
    public boolean shouldEnterWater() {
        return true;
    }

    @Override
    public boolean shouldLeaveWater() {
        return false;
    }

    @Override
    public boolean shouldStopMoving() {
        return false;
    }

    @Override
    public int getWaterSearchRange() {
        return 5;
    }

    public static boolean canLobsterSpawn(World world, BlockPos pos) {
        boolean spawnBlock = AMTagRegistry.blockInTag(AMTagRegistry.LOBSTER_SPAWNS, world.getBlockState(pos.down()).getBlock());
        return spawnBlock || world.getBlockState(pos).getMaterial() == Material.WATER;
    }

}
