package com.github.alexthe666.alexsmobs.entity;

import com.github.alexthe666.alexsmobs.config.AMConfig;
import com.github.alexthe666.alexsmobs.entity.ai.AquaticMoveController;
import com.github.alexthe666.alexsmobs.entity.ai.BoneSerpentPathNavigator;
import com.github.alexthe666.alexsmobs.item.AMItemRegistry;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.ai.RandomPositionGenerator;
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
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.ForgeEventFactory;

import javax.annotation.Nullable;
import java.util.List;
import com.google.common.base.Optional;
import java.util.UUID;

public class EntityStradpole extends EntityCreature implements ISemiAquatic {

    private static final DataParameter<Boolean> FROM_BUCKET = EntityDataManager.createKey(EntityStradpole.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> DESPAWN_SOON = EntityDataManager.createKey(EntityStradpole.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> LAUNCHED = EntityDataManager.createKey(EntityStradpole.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Optional<UUID>> PARENT_UUID = EntityDataManager.createKey(EntityStradpole.class, DataSerializers.OPTIONAL_UNIQUE_ID);

    public float swimPitch;
    public float prevSwimPitch;
    private int despawnTimer;
    private int ricochetCount;

    public EntityStradpole(World world) {
        super(world);
        this.setSize(0.5F, 0.5F);
        this.setPathPriority(PathNodeType.WATER, 0.0F);
        this.setPathPriority(PathNodeType.LAVA, 0.0F);
        this.moveHelper = new AquaticMoveController(this, 1.4F);
        this.isImmuneToFire = true;
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(4.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.3D);
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
    public int getMaxSpawnedInChunk() {
        return 2;
    }

    protected ItemStack getFishBucket() {
        ItemStack stack = new ItemStack(AMItemRegistry.STRADPOLE_BUCKET);
        if (this.hasCustomName()) {
            stack.setStackDisplayName(this.getCustomNameTag());
        }
        return stack;
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        ItemStack itemstack = player.getHeldItem(hand);
        if (itemstack.getItem() == AMItemRegistry.MOSQUITO_LARVA) {
            if (!player.capabilities.isCreativeMode) {
                itemstack.shrink(1);
            }
            if (this.rand.nextFloat() < 0.45F) {
                EntityStraddler straddler = (EntityStraddler) AMEntityRegistry.STRADDLER.newInstance(this.world);
                straddler.setLocationAndAngles(this.posX, this.posY, this.posZ, this.rotationYaw, this.rotationPitch);
                if (!this.world.isRemote) {
                    this.world.spawnEntity(straddler);
                }
                this.setDead();
            }
            return true;
        }
        if (itemstack.getItem() == Items.LAVA_BUCKET && this.isEntityAlive()) {
            this.playSound(SoundEvents.ITEM_BUCKET_FILL, 1.0F, 1.0F);
            itemstack.shrink(1);
            ItemStack itemstack1 = this.getFishBucket();
            if (!this.world.isRemote) {
                            }

            if (itemstack.isEmpty()) {
                player.setHeldItem(hand, itemstack1);
            } else if (!player.inventory.addItemStackToInventory(itemstack1)) {
                player.dropItem(itemstack1, false);
            }

            this.setDead();
            return true;
        }
        return super.processInteract(player, hand);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(PARENT_UUID, Optional.absent());
        this.dataManager.register(DESPAWN_SOON, Boolean.FALSE);
        this.dataManager.register(LAUNCHED, Boolean.FALSE);
        this.dataManager.register(FROM_BUCKET, Boolean.FALSE);
    }

    private boolean isFromBucket() {
        return this.dataManager.get(FROM_BUCKET);
    }

    public void setFromBucket(boolean fromBucket) {
        this.dataManager.set(FROM_BUCKET, fromBucket);
    }

    @Nullable
    public UUID getParentId() {
        return this.dataManager.get(PARENT_UUID).orNull();
    }

    public void setParentId(@Nullable UUID uniqueId) {
        this.dataManager.set(PARENT_UUID, Optional.fromNullable(uniqueId));
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        if (this.getParentId() != null) {
            compound.setUniqueId("ParentUUID", this.getParentId());
        }
        compound.setBoolean("FromBucket", this.isFromBucket());
        compound.setBoolean("DespawnSoon", this.isDespawnSoon());
    }

    @Override
    protected boolean canDespawn() {
        if (this.isFromBucket() || this.hasCustomName()) {
            return false;
        }
        return super.canDespawn();
    }

    @Override
    public boolean getCanSpawnHere() {
        return AMEntityRegistry.rollSpawn(AMConfig.stradpoleSpawnRolls, this.getRNG(), AMEntityRegistry.AMSpawnReason.OTHER);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        if (compound.hasUniqueId("ParentUUID")) {
            this.setParentId(compound.getUniqueId("ParentUUID"));
        }
        this.setFromBucket(compound.getBoolean("FromBucket"));
        this.setDespawnSoon(compound.getBoolean("DespawnSoon"));
    }

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(1, new StradpoleAISwim(this, 1.0D, 10));
        this.tasks.addTask(4, new EntityAILookIdle(this));
        this.tasks.addTask(5, new EntityAIWatchClosest(this, EntityPlayer.class, 6.0F));
    }

    @Override
    public float getBlockPathWeight(BlockPos pos) {
        Material mat = this.world.getBlockState(pos).getMaterial();
        if (mat == Material.WATER || mat == Material.LAVA) {
            return 15.0F;
        }
        return Float.NEGATIVE_INFINITY;
    }

    public boolean isDespawnSoon() {
        return this.dataManager.get(DESPAWN_SOON);
    }

    public void setDespawnSoon(boolean despawnSoon) {
        this.dataManager.set(DESPAWN_SOON, despawnSoon);
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
        return new BoneSerpentPathNavigator(this, worldIn);
    }

    @Override
    public void onLivingUpdate() {
        float f = 1.0F;
        if (this.dataManager.get(LAUNCHED)) {
            this.renderYawOffset = this.rotationYaw;
            RayTraceResult raytraceresult = this.stradpoleProjectileRayTrace();
            if (raytraceresult != null && raytraceresult.typeOfHit != RayTraceResult.Type.MISS
                    && !ForgeEventFactory.onProjectileImpact(this, raytraceresult)) {
                this.onImpact(raytraceresult);
            }
            f = 0.1F;
        }
        super.onLivingUpdate();
        boolean liquid = this.isInWater() || this.isInLava();
        this.prevSwimPitch = this.swimPitch;

        float f2 = (float) -((float) this.motionY * (liquid ? 2.5F : f) * (double) (180F / (float) Math.PI));
        this.swimPitch = f2;
        if (this.onGround && !this.isInWater() && !this.isInLava()) {
            this.motionX += (this.rand.nextFloat() * 2.0F - 1.0F) * 0.2F;
            this.motionY += 0.5D;
            this.motionZ += (this.rand.nextFloat() * 2.0F - 1.0F) * 0.2F;
            this.rotationYaw = this.rand.nextFloat() * 360.0F;
            this.onGround = false;
            this.isAirBorne = true;
        }
        this.setNoGravity(false);
        if (liquid) {
            this.setNoGravity(true);
        }
        if (this.isDespawnSoon()) {
            this.despawnTimer++;
            if (this.despawnTimer > 100) {
                this.despawnTimer = 0;
                this.spawnDispelPoof();
                this.setDead();
            }
        }
    }

    private void spawnDispelPoof() {
        for (int i = 0; i < 8; ++i) {
            double ox = (this.rand.nextDouble() - 0.5D) * 0.4D;
            double oy = (this.rand.nextDouble() - 0.5D) * 0.4D;
            double oz = (this.rand.nextDouble() - 0.5D) * 0.4D;
            this.world.spawnParticle(EnumParticleTypes.EXPLOSION_NORMAL, this.posX + ox, this.posY + this.height * 0.5D + oy, this.posZ + oz, 0.0D, 0.0D, 0.0D);
        }
    }

    /** 1.12 equivalent of {@code ProjectileHelper.func_234618_a_} for launched stradpole ricochet / hit. */
    private RayTraceResult stradpoleProjectileRayTrace() {
        Vec3d start = new Vec3d(this.posX, this.posY, this.posZ);
        Vec3d end = start.addVector(this.motionX, this.motionY, this.motionZ);
        RayTraceResult blockResult = this.world.rayTraceBlocks(start, end, false, true, false);
        double blockDist = blockResult != null && blockResult.typeOfHit != RayTraceResult.Type.MISS ? start.distanceTo(blockResult.hitVec) : Double.MAX_VALUE;

        Entity closestEntity = null;
        Vec3d closestHitVec = null;
        double entityDist = Double.MAX_VALUE;
        AxisAlignedBB search = this.getEntityBoundingBox().expand(this.motionX, this.motionY, this.motionZ).grow(1.0D);
        List<Entity> list = this.world.getEntitiesWithinAABBExcludingEntity(this, search);
        for (Entity entity : list) {
            if (!this.canStradpoleHit(entity)) {
                continue;
            }
            AxisAlignedBB axisalignedbb = entity.getEntityBoundingBox().grow(0.30000001192092896D);
            RayTraceResult intercept = axisalignedbb.calculateIntercept(start, end);
            if (intercept != null) {
                double d = start.distanceTo(intercept.hitVec);
                if (d < entityDist) {
                    entityDist = d;
                    closestEntity = entity;
                    closestHitVec = intercept.hitVec;
                }
            }
        }

        if (closestEntity != null && entityDist <= blockDist) {
            return new RayTraceResult(closestEntity, closestHitVec);
        }
        return blockResult;
    }

    private void onImpact(RayTraceResult raytraceresult) {
        if (raytraceresult.typeOfHit == RayTraceResult.Type.ENTITY) {
            this.onEntityHit(raytraceresult);
        } else if (raytraceresult.typeOfHit == RayTraceResult.Type.BLOCK) {
            BlockPos pos = raytraceresult.getBlockPos();
            IBlockState blockstate = this.world.getBlockState(pos);
            AxisAlignedBB col = blockstate.getCollisionBoundingBox(this.world, pos);
            if (col != null && col != Block.NULL_AABB) {
                Vec3d prevMotion = new Vec3d(this.motionX, this.motionY, this.motionZ);
                double motionX = prevMotion.x;
                double motionY = prevMotion.y;
                double motionZ = prevMotion.z;
                switch (raytraceresult.sideHit) {
                    case EAST:
                    case WEST:
                        motionX = -motionX;
                        break;
                    case SOUTH:
                    case NORTH:
                        motionZ = -motionZ;
                        break;
                    default:
                        motionY = -motionY;
                        break;
                }
                this.motionX = motionX;
                this.motionY = motionY;
                this.motionZ = motionZ;
                if (this.ticksExisted > 200 || this.ricochetCount > 20) {
                    this.dataManager.set(LAUNCHED, Boolean.FALSE);
                } else {
                    this.ricochetCount++;
                }
            }
        }
    }

    @Nullable
    public Entity getParent() {
        UUID id = this.getParentId();
        if (id != null && !this.world.isRemote && this.world instanceof WorldServer) {
            return ((WorldServer) this.world).getEntityFromUuid(id);
        }
        return null;
    }

    private void onEntityHit(RayTraceResult raytraceresult) {
        Entity entity = this.getParent();
        Entity hit = raytraceresult.entityHit;
        if (entity instanceof EntityLivingBase && !this.world.isRemote && hit instanceof EntityLivingBase) {
            EntityLivingBase target = (EntityLivingBase) hit;
            target.attackEntityFrom(DamageSource.causeIndirectDamage(this, (EntityLivingBase) entity), 3.0F);
            target.knockBack(this, 0.7F, entity.posX - this.posX, entity.posZ - this.posZ);
            this.dataManager.set(LAUNCHED, Boolean.FALSE);
        }
    }

    protected boolean canStradpoleHit(Entity entity) {
        if ((entity instanceof EntityPlayer) && ((EntityPlayer) entity).isSpectator()) {
            return false;
        }
        return !(entity instanceof EntityStraddler) && !(entity instanceof EntityStradpole);
    }

    @Override
    public boolean isBurning() {
        return false;
    }

    @Override
    public void travel(float strafe, float vertical, float forward) {
        if (!this.world.isRemote && (this.isInWater() || this.isInLava())) {
            this.moveRelative(strafe, vertical, forward, 0.02F);
            this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
            this.motionX *= 0.9D;
            this.motionY *= 0.9D;
            this.motionZ *= 0.9D;
            if (this.getAttackTarget() == null) {
                this.motionY -= 0.05D;
            }
        } else {
            super.travel(strafe, vertical, forward);
        }
    }

    protected void updateAir(int air) {
    }

    public void shoot(double vx, double vy, double vz, float velocity, float inaccuracy) {
        Vec3d motion = new Vec3d(vx, vy, vz).normalize()
                .addVector(
                        this.rand.nextGaussian() * 0.007499999832361937D * (double) inaccuracy,
                        this.rand.nextGaussian() * 0.007499999832361937D * (double) inaccuracy,
                        this.rand.nextGaussian() * 0.007499999832361937D * (double) inaccuracy)
                .scale(velocity);
        this.motionX = motion.x;
        this.motionY = motion.y;
        this.motionZ = motion.z;
        float horiz = MathHelper.sqrt(motion.x * motion.x + motion.z * motion.z);
        this.rotationYaw = (float) (MathHelper.atan2(motion.x, motion.z) * 57.2957763671875D);
        this.rotationPitch = (float) (MathHelper.atan2(motion.y, horiz) * 57.2957763671875D);
        this.prevRotationPitch = this.rotationPitch;
        this.renderYawOffset = this.rotationYaw;
        this.rotationYawHead = this.rotationYaw;
        this.prevRotationYawHead = this.rotationYaw;
        this.prevRotationYaw = this.rotationYaw;
        this.setDespawnSoon(true);
        this.dataManager.set(LAUNCHED, Boolean.TRUE);
    }

    @Override
    public boolean shouldEnterWater() {
        return !this.isInWater() && !this.isInLava();
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
        return 10;
    }

    private static boolean isLiquidAt(World world, BlockPos pos) {
        Material m = world.getBlockState(pos).getMaterial();
        return m == Material.WATER || m == Material.LAVA;
    }

    private static boolean allowsWaterPath(World world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        return state.getMaterial() == Material.WATER && !state.getMaterial().blocksMovement();
    }

    /**
     * 1.12 port of {@code EntityStradpole.StradpoleAISwim} ({@code RandomWalkingGoal} in 1.16).
     */
    private static class StradpoleAISwim extends EntityAIBase {
        private final EntityStradpole stradpole;
        private final double speed;
        private final int executionChance;
        private double x;
        private double y;
        private double z;

        StradpoleAISwim(EntityStradpole creature, double speedIn, int chance) {
            this.stradpole = creature;
            this.speed = speedIn;
            this.executionChance = chance;
            this.setMutexBits(1);
        }

        @Override
        public boolean shouldExecute() {
            if ((!this.stradpole.isInLava() && !this.stradpole.isInWater())
                    || this.stradpole.isRiding()
                    || this.stradpole.getAttackTarget() != null
                    || (!this.stradpole.isInWater() && !this.stradpole.isInLava() && !this.stradpole.shouldEnterWater())) {
                return false;
            }
            if (this.stradpole.getRNG().nextInt(this.executionChance) != 0) {
                return false;
            }
            Vec3d vector3d = this.pickPosition();
            if (vector3d == null) {
                return false;
            }
            this.x = vector3d.x;
            this.y = vector3d.y;
            this.z = vector3d.z;
            return true;
        }

        @Override
        public boolean shouldContinueExecuting() {
            return !this.stradpole.getNavigator().noPath();
        }

        @Override
        public void startExecuting() {
            this.stradpole.getNavigator().tryMoveToXYZ(this.x, this.y, this.z, this.speed);
        }

        @Nullable
        protected Vec3d pickPosition() {
            if (this.stradpole.getRNG().nextFloat() < 0.3F) {
                Vec3d vector3d = findSurfaceTarget(this.stradpole);
                if (vector3d != null) {
                    return vector3d;
                }
            }
            Vec3d vector3d = RandomPositionGenerator.findRandomTarget(this.stradpole, 7, 3);

            for (int i = 0;
                 vector3d != null && !isLiquidLava(this.stradpole.world, new BlockPos(vector3d))
                         && !allowsWaterPath(this.stradpole.world, new BlockPos(vector3d))
                         && i++ < 15;
                 vector3d = RandomPositionGenerator.findRandomTarget(this.stradpole, 10, 7)) {
            }

            return vector3d;
        }

        private static boolean isLiquidLava(World world, BlockPos pos) {
            return world.getBlockState(pos).getMaterial() == Material.LAVA;
        }

        private boolean canJumpTo(BlockPos pos, int dx, int dz, int scale) {
            BlockPos blockpos = pos.add(dx * scale, 0, dz * scale);
            Material mat = this.stradpole.world.getBlockState(blockpos).getMaterial();
            return mat == Material.LAVA || (mat == Material.WATER && !this.stradpole.world.getBlockState(blockpos).getMaterial().blocksMovement());
        }

        private boolean isAirAbove(BlockPos pos, int dx, int dz, int scale) {
            return this.stradpole.world.isAirBlock(pos.add(dx * scale, 1, dz * scale))
                    && this.stradpole.world.isAirBlock(pos.add(dx * scale, 2, dz * scale));
        }

        private Vec3d findSurfaceTarget(EntityStradpole creature) {
            BlockPos upPos = creature.getPosition();
            while (isLiquidAt(creature.world, upPos)) {
                upPos = upPos.up();
            }
            if (this.isAirAbove(upPos.down(), 0, 0, 0) && this.canJumpTo(upPos.down(), 0, 0, 0)) {
                return new Vec3d((double) upPos.getX() + 0.5D, (double) upPos.getY() - 1.0D, (double) upPos.getZ() + 0.5D);
            }
            return null;
        }
    }
}
