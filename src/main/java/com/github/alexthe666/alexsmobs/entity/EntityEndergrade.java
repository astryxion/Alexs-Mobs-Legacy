package com.github.alexthe666.alexsmobs.entity;

import com.github.alexthe666.alexsmobs.config.AMConfig;
import com.github.alexthe666.alexsmobs.effect.AMEffectRegistry;
import com.github.alexthe666.alexsmobs.entity.ai.DirectPathNavigator;
import com.github.alexthe666.alexsmobs.entity.ai.EndergradeAIBreakFlowers;
import com.github.alexthe666.alexsmobs.entity.ai.EndergradeAITargetItems;
import com.github.alexthe666.alexsmobs.entity.ai.TameableAIRide;
import com.github.alexthe666.alexsmobs.item.AMItemRegistry;
import com.github.alexthe666.alexsmobs.misc.AMSoundRegistry;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIMate;
import net.minecraft.entity.ai.EntityAITempt;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.ai.EntityMoveHelper;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Random;

public class EntityEndergrade extends EntityAnimal {

    private static final DataParameter<Integer> BITE_TICK = EntityDataManager.createKey(EntityEndergrade.class, DataSerializers.VARINT);
    private static final DataParameter<Boolean> SADDLED = EntityDataManager.createKey(EntityEndergrade.class, DataSerializers.BOOLEAN);
    public float tartigradePitch = 0;
    public float prevTartigradePitch = 0;
    public float biteProgress = 0;
    public float prevBiteProgress = 0;
    public boolean stopWandering = false;
    public boolean hasItemTarget = false;

    public EntityEndergrade(World worldIn) {
        super(worldIn);
        this.moveHelper = new MoveHelperController(this);
        this.setSize(0.9F, 0.5F);
    }

    @Override
    protected PathNavigate createNavigator(World worldIn) {
        return new DirectPathNavigator(this, worldIn);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        AMEntityRegistry.registerAttributeIfAbsent(this, SharedMonsterAttributes.ARMOR);
        AMEntityRegistry.registerAttributeIfAbsent(this, SharedMonsterAttributes.ATTACK_DAMAGE);
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(20.0D);
        this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(0.0D);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(2.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.15D);
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setBoolean("Saddled", this.isSaddled());
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.setSaddled(compound.getBoolean("Saddled"));
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(BITE_TICK, 0);
        this.dataManager.register(SADDLED, Boolean.FALSE);
    }

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(0, new TameableAIRide(this, 1.2D));
        this.tasks.addTask(1, new EndergradeAIBreakFlowers(this));
        this.tasks.addTask(2, new EntityAIMate(this, 1.2D) {
            @Override
            public void startExecuting() {
                super.startExecuting();
                EntityEndergrade.this.stopWandering = true;
            }

            @Override
            public void resetTask() {
                super.resetTask();
                EntityEndergrade.this.stopWandering = false;
            }
        });
        this.tasks.addTask(3, new EntityAITempt(this, 1.1D, Items.CHORUS_FRUIT, false) {
            @Override
            public void startExecuting() {
                super.startExecuting();
                EntityEndergrade.this.stopWandering = true;
            }

            @Override
            public void resetTask() {
                super.resetTask();
                EntityEndergrade.this.stopWandering = false;
            }
        });
        this.tasks.addTask(4, new RandomFlyGoal(this));
        this.tasks.addTask(5, new EntityAIWatchClosest(this, EntityPlayer.class, 10.0F));
        this.tasks.addTask(5, new EntityAILookIdle(this));
        this.targetTasks.addTask(1, new EndergradeAITargetItems(this, true));
    }

    @Override
    @Nullable
    public Entity getControllingPassenger() {
        for (Entity passenger : this.getPassengers()) {
            if (passenger instanceof EntityPlayer) {
                EntityPlayer player = (EntityPlayer) passenger;
                if (player.getHeldItemMainhand().getItem() == AMItemRegistry.CHORUS_ON_A_STICK
                        || player.getHeldItemOffhand().getItem() == AMItemRegistry.CHORUS_ON_A_STICK) {
                    return player;
                }
            }
        }
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return AMSoundRegistry.ENDERGRADE_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return AMSoundRegistry.ENDERGRADE_HURT;
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        ItemStack itemstack = player.getHeldItem(hand);
        Item item = itemstack.getItem();
        if (item == Items.SADDLE && !this.isSaddled()) {
            if (!player.capabilities.isCreativeMode) {
                itemstack.shrink(1);
            }
            this.setSaddled(true);
            return true;
        }
        if (item == Items.CHORUS_FRUIT && this.isPotionActive(AMEffectRegistry.ENDER_FLU)) {
            if (!player.capabilities.isCreativeMode) {
                itemstack.shrink(1);
            }
            this.heal(8.0F);
            this.removePotionEffect(AMEffectRegistry.ENDER_FLU);
            return true;
        }
        if (super.processInteract(player, hand)) {
            return true;
        }
        if (!isBreedingItem(itemstack)) {
            if (!player.isSneaking() && this.isSaddled()) {
                player.startRiding(this);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        return stack.getItem() == Items.CHORUS_FRUIT;
    }

    @Override
    public void updatePassenger(Entity passenger) {
        if (this.getPassengers().contains(passenger)) {
            float radius = -0.25F;
            float angle = (0.01745329251F * this.renderYawOffset);
            double extraX = radius * MathHelper.sin((float) (Math.PI + angle));
            double extraZ = radius * MathHelper.cos(angle);
            passenger.setPosition(this.posX + extraX, this.posY + this.getMountedYOffset() + passenger.getYOffset(), this.posZ + extraZ);
        }
    }

    @Override
    public double getMountedYOffset() {
        float f = Math.min(0.25F, this.limbSwingAmount);
        float f1 = this.limbSwing;
        return (double) this.height - 0.1D + (double) (0.12F * MathHelper.cos(f1 * 0.7F) * 0.7F * f);
    }

    @Override
    public boolean hasNoGravity() {
        return true;
    }

    public boolean isSaddled() {
        return this.dataManager.get(SADDLED);
    }

    public void setSaddled(boolean saddled) {
        this.dataManager.set(SADDLED, saddled);
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        this.setNoGravity(true);
        this.prevTartigradePitch = this.tartigradePitch;
        this.prevBiteProgress = this.biteProgress;
        float f2 = (float) -((float) this.motionY * 3.0D * (180F / (float) Math.PI));
        this.tartigradePitch = f2;
        double motionSq = this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ * this.motionZ;
        if (motionSq > 0.005D) {
            float angleMotion = (0.01745329251F * this.renderYawOffset);
            double extraXMotion = -0.2D * MathHelper.sin((float) (Math.PI + angleMotion));
            double extraZMotion = -0.2D * MathHelper.cos(angleMotion);
            this.world.spawnParticle(EnumParticleTypes.END_ROD, this.posX + (this.rand.nextDouble() - 0.5D) * 0.5D, this.posY + 0.3D, this.posZ + (this.rand.nextDouble() - 0.5D) * 0.5D, extraXMotion, 0.0D, extraZMotion);
        }
        int tick = this.dataManager.get(BITE_TICK);
        if (tick > 0) {
            this.dataManager.set(BITE_TICK, tick - 1);
            this.biteProgress++;
        } else if (biteProgress > 0) {
            biteProgress--;
        }
    }

    @Override
    public void fall(float distance, float damageMultiplier) {
    }

    @Override
    public EnumCreatureAttribute getCreatureAttribute() {
        return EnumCreatureAttribute.ARTHROPOD;
    }

    @Override
    protected void updateFallState(double y, boolean onGroundIn, IBlockState state, BlockPos pos) {
    }

    private BlockPos getGroundPosition(BlockPos radialPos) {
        while (radialPos.getY() > 1 && world.isAirBlock(radialPos)) {
            radialPos = radialPos.down();
        }
        if (radialPos.getY() <= 1) {
            return new BlockPos(radialPos.getX(), world.getSeaLevel(), radialPos.getZ());
        }
        return radialPos;
    }

    public boolean isTargetBlocked(Vec3d target) {
        Vec3d start = new Vec3d(this.posX, this.getPositionEyes(1.0F).y, this.posZ);
        RayTraceResult result = this.world.rayTraceBlocks(start, target, false, true, false);
        return result != null && result.typeOfHit != RayTraceResult.Type.MISS;
    }

    public boolean canTargetItem(ItemStack stack) {
        return stack.getItem() == Items.CHORUS_FRUIT || stack.getItem() == Item.getItemFromBlock(Blocks.CHORUS_FLOWER);
    }

    public void onGetItem(EntityItem targetEntity) {
        this.playSound(SoundEvents.ENTITY_CAT_PURREOW, this.getSoundVolume(), this.getSoundPitch());
        this.heal(5.0F);
    }

    public void bite() {
        this.dataManager.set(BITE_TICK, 5);
    }

    @Override
    public boolean getCanSpawnHere() {
        return AMEntityRegistry.rollSpawn(AMConfig.endergradeSpawnRolls, this.getRNG(), AMEntityRegistry.AMSpawnReason.OTHER)
                && AMEntityRegistry.isOuterEnd(this.world, this.getPosition())
                && canEndergradeSpawn(this.world, this.getPosition(), this.rand);
    }

    public static boolean canEndergradeSpawn(World worldIn, BlockPos pos, Random random) {
        return !worldIn.isAirBlock(pos.down());
    }

    @Nullable
    @Override
    public EntityEndergrade createChild(EntityAgeable ageable) {
        return new EntityEndergrade(this.world);
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        boolean prev = super.attackEntityFrom(source, amount);
        if (prev && source.getTrueSource() instanceof EntityLivingBase) {
            EntityLivingBase hurter = (EntityLivingBase) source.getTrueSource();
            if (hurter.isPotionActive(AMEffectRegistry.SUNBIRD_BLESSING)) {
                hurter.removePotionEffect(AMEffectRegistry.SUNBIRD_BLESSING);
            }
            hurter.addPotionEffect(new net.minecraft.potion.PotionEffect(AMEffectRegistry.SUNBIRD_CURSE, 600, 0));
        }
        return prev;
    }

    @Override
    protected void dropEquipment(boolean wasRecentlyHit, int looting) {
        super.dropEquipment(wasRecentlyHit, looting);
        if (this.isSaddled()) {
            this.dropItem(Items.SADDLE, 1);
        }
    }

    static class RandomFlyGoal extends EntityAIBase {
        private final EntityEndergrade parentEntity;
        private BlockPos target = null;

        RandomFlyGoal(EntityEndergrade endergrade) {
            this.parentEntity = endergrade;
            this.setMutexBits(1);
        }

        @Override
        public boolean shouldExecute() {
            EntityMoveHelper movementcontroller = this.parentEntity.getMoveHelper();
            if (parentEntity.stopWandering || parentEntity.hasItemTarget) {
                return false;
            }
            if (!movementcontroller.isUpdating() || target == null) {
                target = getBlockInViewEndergrade();
                if (target != null) {
                    this.parentEntity.getMoveHelper().setMoveTo(target.getX() + 0.5D, target.getY() + 0.5D, target.getZ() + 0.5D, 1.0D);
                }
                return true;
            }
            return false;
        }

        @Override
        public boolean shouldContinueExecuting() {
            return target != null && !parentEntity.stopWandering && !parentEntity.hasItemTarget
                    && parentEntity.getDistanceSq(target.getX() + 0.5D, target.getY() + 0.5D, target.getZ() + 0.5D) > 2.4D
                    && parentEntity.getMoveHelper().isUpdating() && !parentEntity.collidedHorizontally;
        }

        @Override
        public void resetTask() {
            target = null;
        }

        @Override
        public void updateTask() {
            if (target == null) {
                target = getBlockInViewEndergrade();
            }
            if (target != null) {
                this.parentEntity.getMoveHelper().setMoveTo(target.getX() + 0.5D, target.getY() + 0.5D, target.getZ() + 0.5D, 1.0D);
                if (parentEntity.getDistanceSq(target.getX() + 0.5D, target.getY() + 0.5D, target.getZ() + 0.5D) < 2.5F) {
                    target = null;
                }
            }
        }

        public BlockPos getBlockInViewEndergrade() {
            float radius = 1 + parentEntity.getRNG().nextInt(5);
            float neg = parentEntity.getRNG().nextBoolean() ? 1 : -1;
            float renderYawOffset = parentEntity.renderYawOffset;
            float angle = (0.01745329251F * renderYawOffset) + 3.15F + (parentEntity.getRNG().nextFloat() * neg);
            double extraX = radius * MathHelper.sin((float) (Math.PI + angle));
            double extraZ = radius * MathHelper.cos(angle);
            BlockPos radialPos = new BlockPos(parentEntity.posX + extraX, parentEntity.posY + 2, parentEntity.posZ + extraZ);
            BlockPos ground = parentEntity.getGroundPosition(radialPos);
            BlockPos newPos = ground.up(1 + parentEntity.getRNG().nextInt(6));
            if (!parentEntity.isTargetBlocked(new Vec3d(newPos.getX() + 0.5D, newPos.getY() + 0.5D, newPos.getZ() + 0.5D))
                    && parentEntity.getDistanceSq(newPos.getX() + 0.5D, newPos.getY() + 0.5D, newPos.getZ() + 0.5D) > 6) {
                return newPos;
            }
            return null;
        }
    }

    static class MoveHelperController extends EntityMoveHelper {
        private final EntityEndergrade parentEntity;

        MoveHelperController(EntityEndergrade endergrade) {
            super(endergrade);
            this.parentEntity = endergrade;
        }

        @Override
        public void onUpdateMoveHelper() {
            if (this.moveForward != 0.0F || this.moveStrafe != 0.0F) {
                double d0 = this.posX - parentEntity.posX;
                double d1 = this.posY - parentEntity.posY;
                double d2 = this.posZ - parentEntity.posZ;
                double len = Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
                if (len > 1.0E-4D) {
                    parentEntity.motionY += (d1 / len) * this.speed * 0.05D;
                }
                float f = (float) parentEntity.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue();
                float f1 = (float) this.speed * f;
                float f2 = this.moveForward;
                float f3 = this.moveStrafe;
                float f4 = MathHelper.sqrt(f2 * f2 + f3 * f3);
                if (f4 < 1.0F) {
                    f4 = 1.0F;
                }
                f4 = f1 / f4;
                f2 = f2 * f4;
                f3 = f3 * f4;
                float f5 = MathHelper.sin(parentEntity.rotationYaw * ((float) Math.PI / 180F));
                float f6 = MathHelper.cos(parentEntity.rotationYaw * ((float) Math.PI / 180F));
                parentEntity.motionX += f2 * f6 - f3 * f5;
                parentEntity.motionZ += f3 * f6 + f2 * f5;
                this.moveForward = 1.0F;
                this.moveStrafe = 0.0F;
                parentEntity.setAIMoveSpeed(f1);
                return;
            }
            if (this.action == Action.MOVE_TO) {
                double d0 = this.posX - parentEntity.posX;
                double d1 = this.posY - parentEntity.posY;
                double d2 = this.posZ - parentEntity.posZ;
                double len = Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
                AxisAlignedBB bb = parentEntity.getEntityBoundingBox();
                double avgEdge = (bb.maxX - bb.minX + bb.maxY - bb.minY + bb.maxZ - bb.minZ) / 3.0D;
                if (len < avgEdge) {
                    this.action = Action.WAIT;
                    parentEntity.motionX *= 0.5D;
                    parentEntity.motionY *= 0.5D;
                    parentEntity.motionZ *= 0.5D;
                } else {
                    double localSpeed = this.speed;
                    if (parentEntity.isBeingRidden()) {
                        localSpeed *= 1.5D;
                    }
                    double scale = localSpeed * 0.005D / len;
                    parentEntity.motionX += d0 * scale;
                    parentEntity.motionY += d1 * scale;
                    parentEntity.motionZ += d2 * scale;
                    if (parentEntity.getAttackTarget() == null) {
                        parentEntity.rotationYaw = -((float) MathHelper.atan2(parentEntity.motionX, parentEntity.motionZ)) * (180F / (float) Math.PI);
                        parentEntity.renderYawOffset = parentEntity.rotationYaw;
                    } else {
                        EntityLivingBase target = parentEntity.getAttackTarget();
                        double tx = target.posX - parentEntity.posX;
                        double tz = target.posZ - parentEntity.posZ;
                        parentEntity.rotationYaw = -((float) MathHelper.atan2(tx, tz)) * (180F / (float) Math.PI);
                        parentEntity.renderYawOffset = parentEntity.rotationYaw;
                    }
                }
            }
        }
    }
}
