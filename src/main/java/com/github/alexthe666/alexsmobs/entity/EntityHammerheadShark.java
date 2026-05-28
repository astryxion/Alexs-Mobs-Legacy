package com.github.alexthe666.alexsmobs.entity;

import com.github.alexthe666.alexsmobs.config.AMConfig;
import com.github.alexthe666.alexsmobs.entity.ai.AquaticMoveController;
import com.github.alexthe666.alexsmobs.entity.ai.EntityAINearestTarget3D;
import com.github.alexthe666.alexsmobs.entity.ai.SemiAquaticAIRandomSwimming;
import com.github.alexthe666.alexsmobs.entity.ai.SemiAquaticPathNavigator;
import com.github.alexthe666.alexsmobs.item.AMItemRegistry;
import com.google.common.base.Predicate;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAIAvoidEntity;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.monster.EntityGuardian;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.entity.passive.EntityWaterMob;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;

import javax.annotation.Nullable;

public class EntityHammerheadShark extends EntityCreature {

    private static final Predicate<EntityLivingBase> INJURED_PREDICATE = new Predicate<EntityLivingBase>() {
        @Override
        public boolean apply(@Nullable EntityLivingBase mob) {
            return mob != null && mob.getHealth() <= mob.getMaxHealth() / 2D;
        }
    };

    private static final Predicate<EntityLivingBase> SMALL_AQUATIC_PREDICATE = new Predicate<EntityLivingBase>() {
        @Override
        public boolean apply(@Nullable EntityLivingBase mob) {
            return mob != null && mob instanceof EntityWaterMob
                    && !(mob instanceof EntitySquid)
                    && !(mob instanceof EntityGuardian)
                    && !(mob instanceof EntityHammerheadShark)
                    && mob.width < 1.0F;
        }
    };

    public EntityHammerheadShark(World worldIn) {
        super(worldIn);
        this.moveHelper = new AquaticMoveController(this, 1F);
    }

    @Override
    public boolean getCanSpawnHere() {
        return AMEntityRegistry.rollSpawn(AMConfig.hammerheadSharkSpawnRolls, this.getRNG(), AMEntityRegistry.AMSpawnReason.OTHER);
    }

    @Override
    public int getMaxSpawnedInChunk() {
        return 2;
    }

    @Override
    public boolean isNotColliding() {
        return AMEntityRegistry.aquaticNoEntityCollision(this);
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
    protected PathNavigate createNavigator(World worldIn) {
        return new SemiAquaticPathNavigator(this, worldIn);
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_GENERIC_SPLASH;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return SoundEvents.ENTITY_GENERIC_SPLASH;
    }

    @Override
    public void travel(float strafe, float vertical, float forward) {
        if (!this.world.isRemote && this.isInWater()) {
            this.moveRelative(strafe, vertical, forward, this.getAIMoveSpeed());
            this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
            this.motionX *= 0.9D;
            this.motionY *= 0.9D;
            this.motionZ *= 0.9D;
            if (this.getAttackTarget() == null) {
                this.motionY += -0.005D;
            }
        } else {
            super.travel(strafe, vertical, forward);
        }
    }

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(1, new AIFindWater(this));
        this.tasks.addTask(1, new CirclePreyGoal(this, 1F));
        this.tasks.addTask(4, new SemiAquaticAIRandomSwimming(this, 0.6D, 7));
        this.tasks.addTask(4, new EntityAILookIdle(this));
        this.tasks.addTask(8, new AIFollowBoat(this));
        this.tasks.addTask(9, new EntityAIAvoidEntity(this, EntityGuardian.class, 8.0F, 1.0D, 1.0D));
        this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, true));
        this.targetTasks.addTask(2, new EntityAINearestTarget3D(this, EntityLivingBase.class, 50, false, true, INJURED_PREDICATE));
        this.targetTasks.addTask(2, new EntityAINearestTarget3D(this, EntitySquid.class, 50, false, true, null));
        this.targetTasks.addTask(2, new EntityAINearestTarget3D(this, EntityMimicOctopus.class, 80, false, true, null));
        this.targetTasks.addTask(3, new EntityAINearestTarget3D(this, EntityWaterMob.class, 70, false, true, SMALL_AQUATIC_PREDICATE));
    }

    public boolean isTargetBlocked(Vec3d target) {
        Vec3d start = new Vec3d(this.posX, this.posY + (double) this.getEyeHeight(), this.posZ);
        RayTraceResult result = this.world.rayTraceBlocks(start, target, false, true, false);
        return result != null && result.typeOfHit == RayTraceResult.Type.BLOCK;
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        AMEntityRegistry.registerAttributeIfAbsent(this, SharedMonsterAttributes.ATTACK_DAMAGE);
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(30.0D);
        this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(0.0D);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(5.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.5D);
    }

    public static boolean canHammerheadSharkSpawn(World world, BlockPos pos) {
        if (pos.getY() > 45 && pos.getY() < world.getSeaLevel()) {
            Biome biome = world.getBiome(pos);
            boolean notOcean = !BiomeDictionary.hasType(biome, BiomeDictionary.Type.OCEAN) || !BiomeDictionary.hasType(biome, BiomeDictionary.Type.OCEAN);
            return notOcean && world.getBlockState(pos).getMaterial() == Material.WATER;
        } else {
            return false;
        }
    }

    /**
     * 1.16 {@code FindWaterGoal} parity.
     */
    private static class AIFindWater extends EntityAIBase {
        private final EntityHammerheadShark shark;
        private BlockPos targetPos;
        private int executionChance = 30;

        AIFindWater(EntityHammerheadShark shark) {
            this.shark = shark;
            this.setMutexBits(3);
        }

        @Override
        public boolean shouldExecute() {
            if (shark.onGround && shark.world.getBlockState(shark.getPosition()).getMaterial() != Material.WATER) {
                if (shark.getAttackTarget() != null || shark.getRNG().nextInt(executionChance) == 0) {
                    targetPos = generateTarget();
                    return targetPos != null;
                }
            }
            return false;
        }

        @Override
        public void startExecuting() {
            if (targetPos != null) {
                shark.getNavigator().tryMoveToXYZ(targetPos.getX(), targetPos.getY(), targetPos.getZ(), 1.2D);
            }
        }

        @Override
        public boolean shouldContinueExecuting() {
            return targetPos != null && shark.world.getBlockState(shark.getPosition()).getMaterial() != Material.WATER && shark.getDistanceSq(targetPos) > 2;
        }

        @Nullable
        private BlockPos generateTarget() {
            BlockPos pos = shark.getPosition();
            for (int i = 0; i < 15; i++) {
                BlockPos pos1 = pos.add(
                        shark.getRNG().nextInt(16) - 8, shark.getRNG().nextInt(8) - 4, shark.getRNG().nextInt(16) - 8);
                if (shark.world.getBlockState(pos1).getMaterial() == Material.WATER) {
                    return pos1;
                }
            }
            return null;
        }
    }

    /**
     * 1.16 {@code FollowBoatGoal} parity.
     */
    private static class AIFollowBoat extends EntityAIBase {
        private final EntityHammerheadShark shark;
        private EntityBoat boat;
        private int delay;

        AIFollowBoat(EntityHammerheadShark shark) {
            this.shark = shark;
            this.setMutexBits(1);
        }

        @Override
        public boolean shouldExecute() {
            if (!shark.isInWater()) {
                return false;
            }
            for (EntityBoat b : shark.world.getEntitiesWithinAABB(EntityBoat.class, shark.getEntityBoundingBox().grow(24.0D))) {
                this.boat = b;
                return true;
            }
            return false;
        }

        @Override
        public boolean shouldContinueExecuting() {
            return this.boat != null && this.boat.isEntityAlive() && shark.getDistanceSq(this.boat) > 4.0D;
        }

        @Override
        public void resetTask() {
            this.boat = null;
        }

        @Override
        public void updateTask() {
            if (this.boat != null) {
                if (--this.delay <= 0) {
                    this.delay = 10;
                    shark.getNavigator().tryMoveToEntityLiving(this.boat, 1.2D);
                }
            }
        }
    }

    private static class CirclePreyGoal extends EntityAIBase {
        private final EntityHammerheadShark shark;
        private final float speed;
        private float circlingTime = 0;
        private float circleDistance = 5;
        private float maxCirclingTime = 80;
        private boolean clockwise = false;

        CirclePreyGoal(EntityHammerheadShark shark, float speed) {
            this.setMutexBits(3);
            this.shark = shark;
            this.speed = speed;
        }

        @Override
        public boolean shouldExecute() {
            return this.shark.getAttackTarget() != null;
        }

        @Override
        public boolean shouldContinueExecuting() {
            return this.shark.getAttackTarget() != null;
        }

        @Override
        public void startExecuting() {
            circlingTime = 0;
            maxCirclingTime = 360 + this.shark.getRNG().nextInt(80);
            circleDistance = 5 + this.shark.rand.nextFloat() * 5;
            clockwise = this.shark.getRNG().nextBoolean();
        }

        @Override
        public void resetTask() {
            circlingTime = 0;
            maxCirclingTime = 360 + this.shark.getRNG().nextInt(80);
            circleDistance = 5 + this.shark.rand.nextFloat() * 5;
            clockwise = this.shark.getRNG().nextBoolean();
        }

        @Override
        public void updateTask() {
            EntityLivingBase prey = this.shark.getAttackTarget();
            if (prey != null) {
                double dist = this.shark.getDistance(prey);
                if (circlingTime >= maxCirclingTime) {
                    shark.getLookHelper().setLookPositionWithEntity(prey, 30.0F, 30.0F);
                    shark.getNavigator().tryMoveToEntityLiving(prey, 1.5D);
                    if (dist < 2D) {
                        shark.attackEntityAsMob(prey);
                        if (shark.rand.nextFloat() < 0.3F) {
                            shark.entityDropItem(new ItemStack(AMItemRegistry.SHARK_TOOTH), 0.0F);
                        }
                        resetTask();
                    }
                } else {
                    if (dist <= 25) {
                        circlingTime++;
                        BlockPos circlePos = getSharkCirclePos(prey);
                        if (circlePos != null) {
                            shark.getNavigator().tryMoveToXYZ(circlePos.getX() + 0.5D, circlePos.getY() + 0.5D, circlePos.getZ() + 0.5D, 0.6D);
                        }
                    } else {
                        shark.getLookHelper().setLookPositionWithEntity(prey, 30.0F, 30.0F);
                        shark.getNavigator().tryMoveToEntityLiving(prey, 0.8D);
                    }
                }
            }
        }

        public BlockPos getSharkCirclePos(EntityLivingBase target) {
            float angle = (0.01745329251F * (clockwise ? -circlingTime : circlingTime));
            double extraX = circleDistance * MathHelper.sin(angle);
            double extraZ = circleDistance * MathHelper.cos(angle);
            BlockPos ground = new BlockPos(target.posX + 0.5F + extraX, shark.posY, target.posZ + 0.5F + extraZ);
            if (shark.world.getBlockState(ground).getMaterial() == Material.WATER) {
                return ground;
            }
            return null;
        }
    }
}
