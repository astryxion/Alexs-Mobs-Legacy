package com.github.alexthe666.alexsmobs.entity;
import com.github.alexthe666.alexsmobs.misc.AMLootTables;

import com.github.alexthe666.alexsmobs.config.AMConfig;
import com.github.alexthe666.alexsmobs.entity.ai.*;
import com.github.alexthe666.alexsmobs.misc.AMSoundRegistry;
import com.google.common.base.Predicate;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.ai.EntityMoveHelper;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntityWitherSkeleton;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import javax.annotation.Nullable;
import net.minecraft.util.ResourceLocation;
import java.util.List;
import com.google.common.base.Optional;
import java.util.Random;
import java.util.UUID;

public class EntityBoneSerpent extends EntityMob {

    private static final DataParameter<Optional<UUID>> CHILD_UUID = EntityDataManager.createKey(EntityBoneSerpent.class, DataSerializers.OPTIONAL_UNIQUE_ID);
    private static final Predicate<EntityLivingBase> NOT_RIDING_STRADDLEBOARD_FRIENDLY = new Predicate<EntityLivingBase>() {
        @Override
        public boolean apply(@Nullable EntityLivingBase entity) {
            return entity != null && entity.isEntityAlive() && (entity.getRidingEntity() == null
                    || !(entity.getRidingEntity() instanceof EntityStraddleboard)
                    || !((EntityStraddleboard) entity.getRidingEntity()).shouldSerpentFriend());
        }
    };
    private static final Predicate<EntityStraddleboard> STRADDLEBOARD_FRIENDLY = new Predicate<EntityStraddleboard>() {
        @Override
        public boolean apply(@Nullable EntityStraddleboard entity) {
            return entity != null && entity.isBeingRidden() && entity.shouldSerpentFriend();
        }
    };

    public int jumpCooldown = 0;
    private boolean isLandNavigator;
    private int boardCheckCooldown = 0;
    private EntityStraddleboard boardToBoast = null;

    public EntityBoneSerpent(World worldIn) {
        super(worldIn);
        this.setSize(1.2F, 1.15F);
        this.setPathPriority(PathNodeType.WATER, 0.0F);
        this.setPathPriority(PathNodeType.LAVA, 0.0F);
        this.switchNavigator(false);
    }

    public boolean getCanSpawnHere() {
        return AMEntityRegistry.rollSpawn(AMConfig.boneSeprentSpawnRolls, this.getRNG(), AMEntityRegistry.AMSpawnReason.OTHER)
                && super.getCanSpawnHere();
    }

    @Override
    public int getMaxSpawnedInChunk() {
        return 1;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return AMSoundRegistry.BONE_SERPENT_IDLE;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return AMSoundRegistry.BONE_SERPENT_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return AMSoundRegistry.BONE_SERPENT_HURT;
    }
    @Override
    @Nullable
    protected ResourceLocation getLootTable() {
        return AMLootTables.BONE_SERPENT;
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(25.0D);
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(32.0D);
        AMEntityRegistry.registerAttributeIfAbsent(this, SharedMonsterAttributes.ATTACK_DAMAGE);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(5.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(1.45D);
    }

    @Override
    public int getMaxFallHeight() {
        return 256;
    }

    @Override
    public boolean isPotionApplicable(PotionEffect potioneffectIn) {
        if (potioneffectIn.getPotion() == MobEffects.WITHER) {
            return false;
        }
        return super.isPotionApplicable(potioneffectIn);
    }

    @Override
    public EnumCreatureAttribute getCreatureAttribute() {
        return EnumCreatureAttribute.UNDEAD;
    }

    @Override
    public float getBlockPathWeight(BlockPos pos) {
        Material mat = this.world.getBlockState(pos).getMaterial();
        if (mat == Material.WATER || mat == Material.LAVA) {
            return 10.0F;
        }
        return this.isInLava() ? Float.NEGATIVE_INFINITY : 0.0F;
    }

    @Override
    public boolean isPushedByWater() {
        return false;
    }

    @Override
    public boolean canBeLeashedTo(EntityPlayer player) {
        return true;
    }

    public static boolean canBoneSerpentSpawn(World world, BlockPos pos) {
        BlockPos.MutableBlockPos blockpos$mutable = new BlockPos.MutableBlockPos(pos.getX(), pos.getY(), pos.getZ());
        do {
            blockpos$mutable.setPos(blockpos$mutable.getX(), blockpos$mutable.getY() + 1, blockpos$mutable.getZ());
        } while (world.getBlockState(blockpos$mutable).getMaterial() == Material.LAVA);
        return world.getBlockState(blockpos$mutable).getMaterial() == Material.AIR;
    }

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(0, new BreatheAirGoal(this));
        this.tasks.addTask(0, new BoneSerpentAIFindLava(this));
        this.tasks.addTask(1, new BoneSerpentAIMeleeJump(this));
        this.tasks.addTask(2, new BoneSerpentAIJump(this, 10));
        this.tasks.addTask(3, new BoneSerpentAIRandomSwimming(this, 1.0D, 8));
        this.tasks.addTask(4, new EntityAILookIdle(this));
        this.tasks.addTask(5, new EntityAIWatchClosest(this, EntityPlayer.class, 6.0F));
        this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, true));
        if (!AMConfig.neutralBoneSerpents) {
            this.targetTasks.addTask(2, new EntityAINearestTarget3D(this, EntityPlayer.class, 10, true, false, NOT_RIDING_STRADDLEBOARD_FRIENDLY));
            this.targetTasks.addTask(3, new EntityAINearestTarget3D(this, EntityVillager.class, 10, true, false, NOT_RIDING_STRADDLEBOARD_FRIENDLY));
        }
        this.targetTasks.addTask(4, new EntityAINearestTarget3D(this, EntityWitherSkeleton.class, 10, true, false, NOT_RIDING_STRADDLEBOARD_FRIENDLY));
        this.targetTasks.addTask(5, new EntityAINearestTarget3D(this, EntitySoulVulture.class, 10, true, false, NOT_RIDING_STRADDLEBOARD_FRIENDLY));
    }

    @Override
    public void travel(float strafe, float vertical, float forward) {
        boolean liquid = this.isInLava() || this.isInWater();
        if (!this.world.isRemote && liquid) {
            this.moveRelative(strafe, vertical, forward, this.getAIMoveSpeed());
            this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
            this.motionX *= 0.9D;
            this.motionY *= 0.9D;
            this.motionZ *= 0.9D;
            if (this.getAttackTarget() == null) {
                this.motionY -= 0.005D;
            }
        } else {
            super.travel(strafe, vertical, forward);
        }
    }

    private void switchNavigator(boolean onLand) {
        if (onLand) {
            this.moveHelper = new EntityMoveHelper(this);
            this.navigator = this.createNavigator(this.world);
            this.isLandNavigator = true;
        } else {
            this.moveHelper = new BoneSerpentMoveController(this);
            this.navigator = new BoneSerpentPathNavigator((net.minecraft.entity.EntityLiving) this, this.world);
            this.isLandNavigator = false;
        }
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        if (this.getChildId() != null) {
            compound.setUniqueId("ChildUUID", this.getChildId());
        }
    }

    @Override
    public void collideWithNearbyEntities() {
        AxisAlignedBB box = this.getEntityBoundingBox().grow(0.20000000298023224D, 0.0D, 0.20000000298023224D);
        List<Entity> entities = this.world.getEntitiesWithinAABBExcludingEntity(this, box);
        for (Entity entity : entities) {
            if (!(entity instanceof EntityBoneSerpentPart) && entity.canBePushed()) {
                entity.applyEntityCollision(this);
            }
        }
    }

    @Override
    public boolean isEntityInvulnerable(DamageSource source) {
        return source == DamageSource.FALL || source == DamageSource.DROWN || source == DamageSource.IN_WALL
                || source == DamageSource.FALLING_BLOCK || source == DamageSource.LAVA || source.isFireDamage()
                || super.isEntityInvulnerable(source);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        if (compound.hasUniqueId("ChildUUID")) {
            this.setChildId(compound.getUniqueId("ChildUUID"));
        }
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(CHILD_UUID, Optional.absent());
    }

    @Nullable
    public UUID getChildId() {
        return this.dataManager.get(CHILD_UUID).orNull();
    }

    public void setChildId(@Nullable UUID uniqueId) {
        this.dataManager.set(CHILD_UUID, Optional.fromNullable(uniqueId));
    }

    @Nullable
    public Entity getChild() {
        UUID id = getChildId();
        if (id != null && !world.isRemote && world instanceof WorldServer) {
            return ((WorldServer) world).getEntityFromUuid(id);
        }
        return null;
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        this.inPortal = false;
        boolean ground = !this.isInLava() && !this.isInWater() && this.onGround;
        if (jumpCooldown > 0) {
            jumpCooldown--;
            float f2 = (float) -((float) this.motionY * (double) (180F / (float) Math.PI));
            this.rotationPitch = f2;
        }
        if (!ground && this.isLandNavigator) {
            this.switchNavigator(false);
        }
        if (ground && !this.isLandNavigator) {
            this.switchNavigator(true);
        }
        if (!world.isRemote) {
            Entity child = getChild();
            if (child == null) {
                EntityLivingBase partParent = this;
                int segments = 7 + getRNG().nextInt(8);
                for (int i = 0; i < segments; i++) {
                    EntityBoneSerpentPart part = new EntityBoneSerpentPart(world, partParent, 0.9F, 180, 0);
                    part.setParent(partParent);
                    part.setBodyIndex(i);
                    if (partParent == this) {
                        this.setChildId(part.getUniqueID());
                    }
                    part.setInitialPartPos(this);
                    if (i == segments - 1) {
                        part.setTail(true);
                    }
                    world.spawnEntity(part);
                    partParent = part;
                }
            }
        }
        if (!world.isRemote) {
            if (boardCheckCooldown <= 0) {
                boardCheckCooldown = 100 + rand.nextInt(150);
                AxisAlignedBB search = this.getEntityBoundingBox().grow(100, 15, 100);
                List<EntityStraddleboard> list = this.world.getEntitiesWithinAABB(EntityStraddleboard.class, search);
                EntityStraddleboard closestBoard = null;
                for (EntityStraddleboard board : list) {
                    if (!STRADDLEBOARD_FRIENDLY.apply(board)) {
                        continue;
                    }
                    if (closestBoard == null || this.getDistanceSq(closestBoard) > this.getDistanceSq(board)) {
                        closestBoard = board;
                    }
                }
                boardToBoast = closestBoard;
            } else {
                boardCheckCooldown--;
            }
            if (boardToBoast != null) {
                if (this.getDistanceSq(boardToBoast) > 200 * 200) {
                    boardToBoast = null;
                } else {
                    if ((this.isInLava() || this.isInWater()) && this.getDistanceSq(boardToBoast) < 15 * 15 && jumpCooldown == 0) {
                        float up = 0.7F + this.getRNG().nextFloat() * 0.8F;
                        Vec3d vector3d1 = this.getLookVec();
                        this.motionX += (double) vector3d1.x * 0.6D;
                        this.motionY += up;
                        this.motionZ += (double) vector3d1.y * 0.6D;
                        this.getNavigator().clearPath();
                        this.jumpCooldown = this.getRNG().nextInt(300) + 100;
                    }
                    if (this.getDistanceSq(boardToBoast) > 5 * 5) {
                        this.getNavigator().tryMoveToEntityLiving(boardToBoast, 1.5D);
                    } else {
                        this.getNavigator().clearPath();
                    }
                }
            }
        }
    }

    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }

    static class BoneSerpentMoveController extends EntityMoveHelper {
        private final EntityBoneSerpent dolphin;

        BoneSerpentMoveController(EntityBoneSerpent dolphinIn) {
            super(dolphinIn);
            this.dolphin = dolphinIn;
        }

        @Override
        public void onUpdateMoveHelper() {
            if (this.dolphin.isInWater() || this.dolphin.isInLava()) {
                this.dolphin.motionY += 0.005D;
            }

            if (this.action == Action.MOVE_TO && !this.dolphin.getNavigator().noPath()) {
                double d0 = this.posX - this.dolphin.posX;
                double d1 = this.posY - this.dolphin.posY;
                double d2 = this.posZ - this.dolphin.posZ;
                double d3 = d0 * d0 + d1 * d1 + d2 * d2;
                if (d3 < (double) 2.5000003E-7F) {
                    this.dolphin.setMoveForward(0.0F);
                } else {
                    float f = (float) (MathHelper.atan2(d2, d0) * (double) (180F / (float) Math.PI)) - 90.0F;
                    this.dolphin.rotationYaw = this.limitAngle(this.dolphin.rotationYaw, f, 10.0F);
                    this.dolphin.renderYawOffset = this.dolphin.rotationYaw;
                    this.dolphin.rotationYawHead = this.dolphin.rotationYaw;
                    float speedAttr = (float) this.dolphin.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue();
                    float f1 = (float) (this.speed * speedAttr);
                    if (this.dolphin.isInWater() || this.dolphin.isInLava()) {
                        this.dolphin.setAIMoveSpeed(f1 * 0.02F);
                        float f2 = -((float) (MathHelper.atan2(d1, MathHelper.sqrt(d0 * d0 + d2 * d2)) * (double) (180F / (float) Math.PI)));
                        f2 = MathHelper.clamp(MathHelper.wrapDegrees(f2), -85.0F, 85.0F);
                        this.dolphin.motionY += (double) this.dolphin.getAIMoveSpeed() * d1 * 0.6D;
                        this.dolphin.rotationPitch = this.limitAngle(this.dolphin.rotationPitch, f2, 1.0F);
                        float f3 = MathHelper.cos(this.dolphin.rotationPitch * ((float) Math.PI / 180F));
                        float f4 = MathHelper.sin(this.dolphin.rotationPitch * ((float) Math.PI / 180F));
                        this.dolphin.setMoveForward(f3 * f1);
                    } else {
                        this.dolphin.setAIMoveSpeed(f1 * 0.1F);
                    }
                }
            } else {
                this.dolphin.setAIMoveSpeed(0.0F);
                this.dolphin.moveStrafing = 0.0F;
                this.dolphin.setMoveForward(0.0F);
            }
        }
    }
}
