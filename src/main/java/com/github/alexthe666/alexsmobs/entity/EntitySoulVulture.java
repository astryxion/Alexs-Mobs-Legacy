package com.github.alexthe666.alexsmobs.entity;
import com.github.alexthe666.alexsmobs.misc.AMLootTables;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import com.github.alexthe666.alexsmobs.config.AMConfig;
import com.github.alexthe666.alexsmobs.entity.ai.DirectPathNavigator;
import com.github.alexthe666.alexsmobs.entity.ai.EntityAINearestTarget3D;
import com.github.alexthe666.alexsmobs.entity.ai.GroundPathNavigatorWide;
import com.github.alexthe666.alexsmobs.misc.AMSoundRegistry;
import com.github.alexthe666.alexsmobs.misc.AMTagRegistry;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.ai.EntityMoveHelper;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import com.google.common.base.Optional;
import java.util.Random;

public class EntitySoulVulture extends EntityMob {

    public static final ResourceLocation SOUL_LOOT = new ResourceLocation("alexsmobs", "entities/soul_vulture_heart");
    private static final DataParameter<Boolean> FLYING = EntityDataManager.createKey(EntitySoulVulture.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> TACKLING = EntityDataManager.createKey(EntitySoulVulture.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Optional<BlockPos>> PERCH_POS = EntityDataManager.createKey(EntitySoulVulture.class, DataSerializers.OPTIONAL_BLOCK_POS);
    private static final DataParameter<Integer> SOUL_LEVEL = EntityDataManager.createKey(EntitySoulVulture.class, DataSerializers.VARINT);
    public float prevFlyProgress;
    public float flyProgress;
    public float prevTackleProgress;
    public float tackleProgress;
    private boolean isLandNavigator;
    private int perchSearchCooldown = 0;
    private int landingCooldown = 0;
    private int tackleCooldown = 0;

    public EntitySoulVulture(World worldIn) {
        super(worldIn);
        switchNavigator(true);
    }

    @Override
    public EnumCreatureAttribute getCreatureAttribute() {
        return EnumCreatureAttribute.UNDEAD;
    }

    @Override
    @Nullable
    protected ResourceLocation getLootTable() {
        return this.getSoulLevel() > 2 ? AMLootTables.SOUL_VULTURE_HEART : AMLootTables.SOUL_VULTURE;
    }

    @Override
    public boolean getCanSpawnHere() {
        return AMEntityRegistry.rollSpawn(AMConfig.soulVultureSpawnRolls, this.getRNG(), AMEntityRegistry.AMSpawnReason.OTHER) && super.getCanSpawnHere();
    }

    public static boolean canVultureSpawn(World worldIn, BlockPos pos, Random random) {
        BlockPos blockpos = pos.down();
        boolean spawnBlock = AMTagRegistry.blockInTag(AMTagRegistry.SOUL_VULTURE_SPAWNS, worldIn.getBlockState(blockpos).getBlock());
        return spawnBlock && worldIn.getLight(pos) > 8;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return AMSoundRegistry.SOUL_VULTURE_IDLE;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return AMSoundRegistry.SOUL_VULTURE_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return AMSoundRegistry.SOUL_VULTURE_HURT;
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        AMEntityRegistry.registerAttributeIfAbsent(this, SharedMonsterAttributes.ATTACK_DAMAGE);
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(12.0D);
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(18.0D);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(4.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.25D);
    }

    public boolean isPerchBlock(BlockPos pos, IBlockState state) {
        return world.isAirBlock(pos.up()) && world.isAirBlock(pos.up(2)) && AMTagRegistry.blockInTag(AMTagRegistry.SOUL_VULTURE_PERCHES, state.getBlock());
    }

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(1, new AICirclePerch(this));
        this.tasks.addTask(2, new AIFlyRandom(this));
        this.tasks.addTask(3, new AITackleMelee(this));
        this.tasks.addTask(4, new EntityAIWatchClosest(this, EntityPlayer.class, 20.0F));
        this.tasks.addTask(5, new EntityAILookIdle(this));
        this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, true, EntitySoulVulture.class));
        this.targetTasks.addTask(2, new EntityAINearestTarget3D(this, EntityPlayer.class, true));
        this.targetTasks.addTask(2, new EntityAINearestTarget3D(this, EntityPigZombie.class, true));
        this.targetTasks.addTask(3, new EntityAINearestTarget3D(this, EntityVillager.class, true));
    }

    private void switchNavigator(boolean onLand) {
        if (onLand) {
            this.moveHelper = new EntityMoveHelper(this);
            this.navigator = new GroundPathNavigatorWide(this, this.world);
            this.isLandNavigator = true;
        } else {
            this.moveHelper = new MoveHelper(this);
            this.navigator = new DirectPathNavigator(this, this.world);
            this.isLandNavigator = false;
        }
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(FLYING, Boolean.FALSE);
        this.dataManager.register(TACKLING, Boolean.FALSE);
        this.dataManager.register(PERCH_POS, Optional.absent());
        this.dataManager.register(SOUL_LEVEL, 0);
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setBoolean("Flying", this.isFlying());
        if (this.getPerchPos() != null) {
            compound.setInteger("PerchX", this.getPerchPos().getX());
            compound.setInteger("PerchY", this.getPerchPos().getY());
            compound.setInteger("PerchZ", this.getPerchPos().getZ());
        }
        compound.setInteger("SoulLevel", this.getSoulLevel());
        compound.setInteger("LandingCooldown", landingCooldown);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.setFlying(compound.getBoolean("Flying"));
        this.setSoulLevel(compound.getInteger("SoulLevel"));
        this.landingCooldown = compound.getInteger("LandingCooldown");
        if (compound.hasKey("PerchX") && compound.hasKey("PerchY") && compound.hasKey("PerchZ")) {
            this.setPerchPos(new BlockPos(compound.getInteger("PerchX"), compound.getInteger("PerchY"), compound.getInteger("PerchZ")));
        }
    }

    public boolean isFlying() {
        return this.dataManager.get(FLYING);
    }

    public void setFlying(boolean flying) {
        this.dataManager.set(FLYING, flying);
    }

    public boolean isTackling() {
        return this.dataManager.get(TACKLING);
    }

    public void setTackling(boolean tackling) {
        this.dataManager.set(TACKLING, tackling);
    }

    public BlockPos getPerchPos() {
        return this.dataManager.get(PERCH_POS).orNull();
    }

    public void setPerchPos(BlockPos pos) {
        this.dataManager.set(PERCH_POS, Optional.fromNullable(pos));
    }

    public int getSoulLevel() {
        return this.dataManager.get(SOUL_LEVEL);
    }

    public void setSoulLevel(int level) {
        this.dataManager.set(SOUL_LEVEL, level);
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        this.prevTackleProgress = tackleProgress;
        this.prevFlyProgress = flyProgress;
        if (!world.isRemote) {
            if (perchSearchCooldown > 0) {
                perchSearchCooldown--;
            }
            if (this.getAttackTarget() != null && this.getAttackTarget().isEntityAlive()) {
                this.setPerchPos(this.getAttackTarget().getPosition().up(7));
            } else {
                if (this.getPerchPos() != null && !isPerchBlock(this.getPerchPos(), world.getBlockState(this.getPerchPos()))) {
                    this.setPerchPos(null);
                }
            }
            if (this.getPerchPos() == null && perchSearchCooldown == 0) {
                perchSearchCooldown = 20 + rand.nextInt(20);
                this.setPerchPos(this.findNewPerchPos());
            }
            if (!isFlying() && landingCooldown == 0 && (this.getPerchPos() == null || this.shouldLeavePerch(this.getPerchPos()))) {
                this.setFlying(true);
            }
            if (!isFlying() && this.getAttackTarget() != null) {
                this.setFlying(true);
            }

            if (landingCooldown > 0 && isFlying() && this.onGround && this.getAttackTarget() == null) {
                this.setFlying(false);
            }
        }
        if (isFlying() && this.isLandNavigator) {
            switchNavigator(false);
        }
        if (!isFlying() && !this.isLandNavigator) {
            switchNavigator(true);
        }
        if (this.isFlying() && flyProgress < 5F) {
            flyProgress++;
        }
        if (!this.isFlying() && flyProgress > 0F) {
            flyProgress--;
        }
        if (this.isTackling() && tackleProgress < 5F) {
            tackleProgress++;
        }
        if (!this.isTackling() && tackleProgress > 0F) {
            tackleProgress--;
        }
        if (landingCooldown > 0) {
            landingCooldown--;
        }
        if (tackleCooldown > 0) {
            tackleCooldown--;
        }
        if (isFlying()) {
            this.setNoGravity(true);
        } else {
            this.setNoGravity(false);
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void handleStatusUpdate(byte id) {
        if (id == 68) {
            for (int i = 0; i < 6 + rand.nextInt(3); i++) {
                double d2 = this.rand.nextGaussian() * 0.02D;
                double d0 = this.rand.nextGaussian() * 0.02D;
                double d1 = this.rand.nextGaussian() * 0.02D;
                this.world.spawnParticle(EnumParticleTypes.SPELL_WITCH, this.posX + (double) (this.rand.nextFloat() * this.width) - (double) this.width * 0.5F, this.posY + this.height * 0.5F + (double) (this.rand.nextFloat() * this.height * 0.5F), this.posZ + (double) (this.rand.nextFloat() * this.width) - (double) this.width * 0.5F, d0, d1, d2);
            }
        } else {
            super.handleStatusUpdate(id);
        }
    }

    public BlockPos findNewPerchPos() {
        BlockPos underneath = new BlockPos(this.posX, this.getEntityBoundingBox().minY - 1.0D, this.posZ);
        IBlockState beneathState = world.getBlockState(underneath);
        if (isPerchBlock(underneath, beneathState)) {
            return underneath;
        }
        BlockPos blockpos = null;
        Random random = new Random();
        int range = 14;
        for (int i = 0; i < 15; i++) {
            BlockPos blockpos1 = this.getPosition().add(random.nextInt(range) - range / 2, 3, random.nextInt(range) - range / 2);
            while (this.world.isAirBlock(blockpos1) && blockpos1.getY() > 1) {
                blockpos1 = blockpos1.down();
            }
            if (isPerchBlock(blockpos1, world.getBlockState(blockpos1))) {
                blockpos = blockpos1;
            }
        }
        return blockpos;
    }

    private boolean shouldLeavePerch(BlockPos perchPos) {
        return this.getDistanceSq(perchPos.getX() + 0.5D, perchPos.getY() + 0.5D, perchPos.getZ() + 0.5D) > 13 || landingCooldown == 0;
    }

    @Override
    public void fall(float distance, float damageMultiplier) {
    }

    @Override
    protected void updateFallState(double y, boolean onGroundIn, IBlockState state, BlockPos pos) {
    }

    public boolean shouldSwoop() {
        return this.getAttackTarget() != null && this.tackleCooldown == 0;
    }

    public boolean isTargetBlocked(Vec3d target) {
        Vec3d start = new Vec3d(this.posX, this.getPositionEyes(1.0F).y, this.posZ);
        RayTraceResult result = this.world.rayTraceBlocks(start, target, false, true, false);
        return result != null && result.typeOfHit != RayTraceResult.Type.MISS;
    }

    class AICirclePerch extends EntityAIBase {
        private final EntitySoulVulture vulture;
        float speed = 1;
        float circlingTime = 0;
        float circleDistance = 5;
        float maxCirclingTime = 80;
        boolean clockwise = false;
        private int yLevel = 1;

        AICirclePerch(EntitySoulVulture vulture) {
            this.vulture = vulture;
            this.setMutexBits(3);
        }

        @Override
        public boolean shouldExecute() {
            return !vulture.shouldSwoop() && this.vulture.isFlying() && this.vulture.getPerchPos() != null;
        }

        @Override
        public void startExecuting() {
            circlingTime = 0;
            speed = 0.8F + vulture.getRNG().nextFloat() * 0.4F;
            yLevel = vulture.rand.nextInt(3);
            maxCirclingTime = 360 + this.vulture.rand.nextInt(80);
            circleDistance = 5 + this.vulture.rand.nextFloat() * 5;
            clockwise = this.vulture.rand.nextBoolean();
        }

        @Override
        public void resetTask() {
            circlingTime = 0;
            speed = 0.8F + vulture.getRNG().nextFloat() * 0.4F;
            yLevel = vulture.rand.nextInt(3);
            maxCirclingTime = 360 + this.vulture.rand.nextInt(80);
            circleDistance = 5 + this.vulture.rand.nextFloat() * 5;
            clockwise = this.vulture.rand.nextBoolean();
            this.vulture.tackleCooldown = 0;
        }

        @Override
        public void updateTask() {
            BlockPos encircle = vulture.getPerchPos();
            double localSpeed = speed;
            if (this.vulture.getAttackTarget() != null) {
                localSpeed *= 1.55D;
            }
            if (encircle != null) {
                circlingTime++;
                if (circlingTime > 360) {
                    vulture.getMoveHelper().setMoveTo(encircle.getX() + 0.5D, encircle.getY() + 1.1D, encircle.getZ() + 0.5D, localSpeed);
                    if (vulture.collidedVertically || this.vulture.getDistanceSq(encircle.getX() + 0.5D, encircle.getY() + 1.1D, encircle.getZ() + 0.5D) < 1D) {
                        vulture.setFlying(false);
                        vulture.motionX = 0;
                        vulture.motionY = 0;
                        vulture.motionZ = 0;
                        vulture.landingCooldown = 400 + vulture.rand.nextInt(1200);
                        resetTask();
                    }
                } else {
                    BlockPos circlePos = getVultureCirclePos(encircle);
                    if (circlePos != null) {
                        vulture.getMoveHelper().setMoveTo(circlePos.getX() + 0.5D, circlePos.getY() + 0.5D, circlePos.getZ() + 0.5D, localSpeed);
                    }
                }
            }
        }

        @Override
        public boolean shouldContinueExecuting() {
            return shouldExecute();
        }

        public BlockPos getVultureCirclePos(BlockPos target) {
            float angle = (0.01745329251F * 3 * (clockwise ? -circlingTime : circlingTime));
            double extraX = circleDistance * MathHelper.sin(angle);
            double extraZ = circleDistance * MathHelper.cos(angle);
            BlockPos pos = new BlockPos(target.getX() + extraX, target.getY() + 1 + yLevel, target.getZ() + extraZ);
            if (vulture.world.isAirBlock(pos)) {
                return pos;
            }
            return null;
        }
    }

    class MoveHelper extends EntityMoveHelper {
        private final EntitySoulVulture parentEntity;

        MoveHelper(EntitySoulVulture bird) {
            super(bird);
            this.parentEntity = bird;
        }

        @Override
        public void onUpdateMoveHelper() {
            if (this.action == Action.MOVE_TO) {
                double d0 = this.posX - parentEntity.posX;
                double d1 = this.posY - parentEntity.posY;
                double d2 = this.posZ - parentEntity.posZ;
                double d5 = d0 * d0 + d1 * d1 + d2 * d2;
                if (d5 < 0.09D) {
                    this.action = Action.WAIT;
                    parentEntity.motionX *= 0.5D;
                    parentEntity.motionY *= 0.5D;
                    parentEntity.motionZ *= 0.5D;
                } else {
                    double scale = this.speed * 0.05D / Math.sqrt(d5);
                    parentEntity.motionX += d0 * scale;
                    parentEntity.motionY += d1 * scale;
                    parentEntity.motionZ += d2 * scale;
                    parentEntity.rotationYaw = -((float) MathHelper.atan2(parentEntity.motionX, parentEntity.motionZ)) * (180F / (float) Math.PI);
                    parentEntity.renderYawOffset = parentEntity.rotationYaw;
                }
            }
        }
    }

    private class AIFlyRandom extends EntityAIBase {

        private final EntitySoulVulture vulture;
        private BlockPos target = null;

        AIFlyRandom(EntitySoulVulture vulture) {
            this.vulture = vulture;
            this.setMutexBits(3);
        }

        @Override
        public boolean shouldExecute() {
            if (vulture.getPerchPos() != null || vulture.shouldSwoop()) {
                return false;
            }
            EntityMoveHelper movementcontroller = this.vulture.getMoveHelper();
            if (!movementcontroller.isUpdating() || target == null) {
                target = getBlockInViewVulture();
                if (target != null) {
                    this.vulture.getMoveHelper().setMoveTo(target.getX() + 0.5D, target.getY() + 0.5D, target.getZ() + 0.5D, 1.0D);
                }
                return true;
            }
            return false;
        }

        @Override
        public boolean shouldContinueExecuting() {
            if (vulture.getPerchPos() != null || vulture.shouldSwoop()) {
                return false;
            }
            return target != null && vulture.getDistanceSq(target.getX() + 0.5D, target.getY() + 0.5D, target.getZ() + 0.5D) > 2.4D
                    && vulture.getMoveHelper().isUpdating() && !vulture.collidedHorizontally;
        }

        @Override
        public void resetTask() {
            target = null;
        }

        @Override
        public void updateTask() {
            if (target == null) {
                target = getBlockInViewVulture();
            }
            if (target != null) {
                this.vulture.getMoveHelper().setMoveTo(target.getX() + 0.5D, target.getY() + 0.5D, target.getZ() + 0.5D, 1.0D);
                if (vulture.getDistanceSq(target.getX() + 0.5D, target.getY() + 0.5D, target.getZ() + 0.5D) < 2.5F) {
                    target = null;
                }
            }
        }

        public BlockPos getBlockInViewVulture() {
            float radius = 0.75F * (0.7F * 6) * -3 - vulture.getRNG().nextInt(10);
            float neg = vulture.getRNG().nextBoolean() ? 1 : -1;
            float renderYawOffset = vulture.renderYawOffset;
            float angle = (0.01745329251F * renderYawOffset) + 3.15F + (vulture.getRNG().nextFloat() * neg);
            double extraX = radius * MathHelper.sin((float) (Math.PI + angle));
            double extraZ = radius * MathHelper.cos(angle);
            BlockPos radialPos = new BlockPos(vulture.posX + extraX, vulture.posY, vulture.posZ + extraZ);
            while (vulture.world.isAirBlock(radialPos) && radialPos.getY() > 2) {
                radialPos = radialPos.down();
            }
            BlockPos newPos = radialPos.up(vulture.posY - radialPos.getY() > 16 ? 4 : vulture.getRNG().nextInt(5) + 5);
            if (!vulture.isTargetBlocked(new Vec3d(newPos.getX() + 0.5D, newPos.getY() + 0.5D, newPos.getZ() + 0.5D))
                    && vulture.getDistanceSq(newPos.getX() + 0.5D, newPos.getY() + 0.5D, newPos.getZ() + 0.5D) > 6) {
                return newPos;
            }
            return null;
        }
    }

    private class AITackleMelee extends EntityAIBase {

        private final EntitySoulVulture vulture;

        AITackleMelee(EntitySoulVulture vulture) {
            this.vulture = vulture;
            this.setMutexBits(3);
        }

        @Override
        public boolean shouldExecute() {
            if (vulture.getAttackTarget() != null && vulture.shouldSwoop()) {
                vulture.setFlying(true);
                return true;
            }
            return false;
        }

        @Override
        public void resetTask() {
            vulture.setTackling(false);
        }

        @Override
        public void updateTask() {
            if (vulture.isFlying()) {
                vulture.setTackling(true);
            } else {
                vulture.setTackling(false);
            }
            if (vulture.getAttackTarget() != null) {
                EntityLivingBase target = vulture.getAttackTarget();
                this.vulture.getMoveHelper().setMoveTo(target.posX, target.posY + target.getEyeHeight(), target.posZ, 2.0D);
                double d0 = this.vulture.posX - target.posX;
                double d2 = this.vulture.posZ - target.posZ;
                float f = (float) (MathHelper.atan2(d2, d0) * (double) (180F / (float) Math.PI)) - 90.0F;
                vulture.rotationYaw = f;
                vulture.renderYawOffset = vulture.rotationYaw;
                if (vulture.getEntityBoundingBox().grow(0.3D, 0.3D, 0.3D).intersects(target.getEntityBoundingBox()) && vulture.tackleCooldown == 0) {
                    vulture.tackleCooldown = 100 + vulture.rand.nextInt(200);
                    float dmg = (float) vulture.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
                    if (target.attackEntityFrom(DamageSource.causeMobDamage(vulture), dmg)) {
                        if (vulture.getHealth() < vulture.getMaxHealth() - dmg && vulture.getSoulLevel() < 5) {
                            this.vulture.setSoulLevel(vulture.getSoulLevel() + 1);
                            this.vulture.heal(dmg);
                            this.vulture.world.setEntityState(vulture, (byte) 68);
                        }
                    }
                    resetTask();
                }
            }
        }
    }
}
