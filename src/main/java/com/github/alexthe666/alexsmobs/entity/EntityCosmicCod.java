package com.github.alexthe666.alexsmobs.entity;

import com.github.alexthe666.alexsmobs.config.AMConfig;
import com.github.alexthe666.alexsmobs.entity.ai.CosmicCodAIFollowLeader;
import com.github.alexthe666.alexsmobs.entity.ai.FlightMoveController;
import com.github.alexthe666.alexsmobs.item.AMItemRegistry;
import com.github.alexthe666.alexsmobs.misc.AMSoundRegistry;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.EnderTeleportEvent;

import javax.annotation.Nullable;
import java.util.List;

public class EntityCosmicCod extends EntityCreature {

    private static final DataParameter<Float> FISH_PITCH = EntityDataManager.createKey(EntityCosmicCod.class, DataSerializers.FLOAT);
    private static final DataParameter<Boolean> FROM_BUCKET = EntityDataManager.createKey(EntityCosmicCod.class, DataSerializers.BOOLEAN);
    private static final float STARTING_ANGLE = 0.0174532925F;
    public float prevFishPitch;
    private int baitballCooldown = 100 + rand.nextInt(100);
    private int circleTime = 0;
    private int maxCircleTime = 300;
    private BlockPos circlePos;
    private int teleportIn;
    private EntityCosmicCod groupLeader;
    private int groupSize = 1;

    /** Max cod within this radius of a candidate spawn — prevents void-wide baitballs. */
    private static final double LOCAL_SPAWN_CAP_RADIUS = 32.0D;
    private static final int LOCAL_SPAWN_CAP = 14;

    public EntityCosmicCod(World world) {
        super(world);
        this.moveHelper = new FlightMoveController(this, 1F, false, true);
        this.setNoGravity(true);
    }

    /**
     * 1.20 spawns in End void air under islands only; 1.12 port must not treat the whole dimension as valid air.
     */
    public static boolean canSpawnAt(World world, BlockPos pos) {
        if (world == null || world.provider.getDimension() != 1) {
            return false;
        }
        if (!world.isAirBlock(pos)) {
            return false;
        }
        if (pos.getY() < 8 || pos.getY() > 220) {
            return false;
        }
        if (world.isBlockNormalCube(pos.down(), false)) {
            return false;
        }
        boolean islandAbove = false;
        for (int dy = 4; dy <= 72; dy++) {
            if (world.isBlockNormalCube(pos.up(dy), false)) {
                islandAbove = true;
                break;
            }
        }
        if (!islandAbove) {
            return false;
        }
        List<EntityCosmicCod> nearby = world.getEntitiesWithinAABB(
                EntityCosmicCod.class,
                new AxisAlignedBB(pos).grow(LOCAL_SPAWN_CAP_RADIUS));
        return nearby.size() < LOCAL_SPAWN_CAP;
    }

    @Override
    public boolean getCanSpawnHere() {
        return AMEntityRegistry.rollSpawn(AMConfig.cosmicCodSpawnRolls, this.getRNG(), AMEntityRegistry.AMSpawnReason.OTHER)
                && canSpawnAt(this.world, this.getPosition());
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(4.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.35D);
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return AMSoundRegistry.COSMIC_COD_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return AMSoundRegistry.COSMIC_COD_HURT;
    }

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(0, new AISwimIdle(this));
        this.tasks.addTask(1, new CosmicCodAIFollowLeader(this));
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(FISH_PITCH, 0F);
        this.dataManager.register(FROM_BUCKET, false);
    }

    public boolean isFromBucket() {
        return this.dataManager.get(FROM_BUCKET);
    }

    public void setFromBucket(boolean fromBucket) {
        this.dataManager.set(FROM_BUCKET, fromBucket);
    }

    protected ItemStack getFishBucket() {
        ItemStack stack = new ItemStack(AMItemRegistry.COSMIC_COD_BUCKET);
        if (this.hasCustomName()) {
            stack.setStackDisplayName(this.getCustomNameTag());
        }
        return stack;
    }

    protected void setBucketData(ItemStack bucket) {
        if (this.hasCustomName()) {
            bucket.setStackDisplayName(this.getCustomNameTag());
        }
        NBTTagCompound platTag = new NBTTagCompound();
        this.writeEntityToNBT(platTag);
        NBTTagCompound compound = bucket.getTagCompound();
        if (compound == null) {
            compound = new NBTTagCompound();
            bucket.setTagCompound(compound);
        }
        compound.setTag("CosmicCodData", platTag);
    }

    public void loadFromBucketTag(NBTTagCompound compound) {
        if (compound.hasKey("CosmicCodData")) {
            this.readEntityFromNBT(compound.getCompoundTag("CosmicCodData"));
        }
    }

    @Override
    protected boolean canDespawn() {
        return !this.isFromBucket() && !this.hasCustomName() && super.canDespawn();
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setBoolean("FromBucket", this.isFromBucket());
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.setFromBucket(compound.getBoolean("FromBucket"));
    }

    private void doInitialPosing(@Nullable GroupData data) {
        BlockPos down = this.getPosition();
        while (this.world.isAirBlock(down) && down.getY() > 1) {
            down = down.down();
        }
        if (down.getY() <= 1) {
            if (data != null && data.groupLeader != null) {
                this.setPosition(down.getX() + 0.5F, data.groupLeader.posY - 1 + this.rand.nextInt(2), down.getZ() + 0.5F);
            } else {
                this.setPosition(down.getX() + 0.5F, down.getY() + 90 + this.rand.nextInt(60), down.getZ() + 0.5F);
            }
        } else {
            this.setPosition(down.getX() + 0.5F, down.getY() + 1, down.getZ() + 0.5F);
        }
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        this.prevFishPitch = this.getFishPitch();
        if (!this.world.isRemote) {
            final double ydist = (this.posY - this.lastTickPosY);
            final float fishDist = (float) ((Math.abs(this.motionX) + Math.abs(this.motionZ)) * 6F) / getPitchSensitivity();
            this.incrementFishPitch((float) (ydist) * 10 * getPitchSensitivity());
            this.setFishPitch(MathHelper.clamp(this.getFishPitch(), -60, 40));
            if (this.getFishPitch() > 2F) {
                this.decrementFishPitch(fishDist * Math.abs(this.getFishPitch()) / 90);
            }
            if (this.getFishPitch() < -2F) {
                this.incrementFishPitch(fishDist * Math.abs(this.getFishPitch()) / 90);
            }
            if (this.getFishPitch() > 2F) {
                this.decrementFishPitch(1);
            } else if (this.getFishPitch() < -2F) {
                this.incrementFishPitch(1);
            }
            if (baitballCooldown > 0) {
                baitballCooldown--;
            }
        }
        if (teleportIn > 0) {
            teleportIn--;
            if (teleportIn == 0 && !this.world.isRemote) {
                final double range = 8;
                final AxisAlignedBB bb = new AxisAlignedBB(this.posX - range, this.posY - range, this.posZ - range, this.posX + range, this.posY + range, this.posZ + range);
                final List<EntityCosmicCod> list = this.world.getEntitiesWithinAABB(EntityCosmicCod.class, bb);
                final Vec3d vec3 = this.teleport();
                if (vec3 != null) {
                    baitballCooldown = 5;
                    for (final EntityCosmicCod cod : list) {
                        if (cod != this) {
                            cod.baitballCooldown = 5;
                            cod.setPositionAndUpdate(vec3.x, vec3.y, vec3.z);
                            cod.world.setEntityState(cod, (byte) 46);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void handleStatusUpdate(byte id) {
        if (id == 46) {
            this.playSound(SoundEvents.ENTITY_ENDERMEN_TELEPORT, 1.0F, 1.0F);
        }
        super.handleStatusUpdate(id);
    }

    public void resetBaitballCooldown() {
        baitballCooldown = 120 + this.rand.nextInt(100);
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        boolean prev = super.attackEntityFrom(source, amount);
        if (prev) {
            teleportIn = 5;
        }
        return prev;
    }

    private float getPitchSensitivity() {
        return 3F;
    }

    @Override
    public boolean hasNoGravity() {
        return true;
    }

    public float getFishPitch() {
        return this.dataManager.get(FISH_PITCH);
    }

    public void setFishPitch(float pitch) {
        this.dataManager.set(FISH_PITCH, pitch);
    }

    public void incrementFishPitch(float pitch) {
        this.dataManager.set(FISH_PITCH, this.getFishPitch() + pitch);
    }

    public void decrementFishPitch(float pitch) {
        this.dataManager.set(FISH_PITCH, this.getFishPitch() - pitch);
    }

    @Override
    public void fall(float distance, float damageMultiplier) {
    }

    @Override
    protected void updateFallState(double y, boolean onGroundIn, IBlockState state, BlockPos pos) {
    }

    public boolean canBlockPosBeSeen(BlockPos pos) {
        final double x = pos.getX() + 0.5F;
        final double y = pos.getY() + 0.5F;
        final double z = pos.getZ() + 0.5F;
        final RayTraceResult result = this.world.rayTraceBlocks(this.getPositionEyes(1.0F), new Vec3d(x, y, z), false, true, false);
        if (result == null) {
            return true;
        }
        final double dist = result.hitVec.squareDistanceTo(x, y, z);
        return dist <= 1.0D || result.typeOfHit == RayTraceResult.Type.MISS;
    }

    @Nullable
    protected Vec3d teleport() {
        if (!this.world.isRemote && this.isEntityAlive()) {
            final double d0 = this.posX + (this.rand.nextDouble() - 0.5D) * 64.0D;
            final double d1 = this.posY + (double) (this.rand.nextInt(64) - 32);
            final double d2 = this.posZ + (this.rand.nextDouble() - 0.5D) * 64.0D;
            if (this.tryTeleport(d0, d1, d2)) {
                this.circlePos = null;
                return new Vec3d(d0, d1, d2);
            }
        }
        return null;
    }

    private boolean tryTeleport(double x, double y, double z) {
        BlockPos pos = new BlockPos(x, y, z);
        IBlockState blockstate = this.world.getBlockState(pos);
        if (this.world.isAirBlock(pos)) {
            this.playSound(SoundEvents.ENTITY_ENDERMEN_TELEPORT, 1.0F, 1.0F);
            EnderTeleportEvent event = new EnderTeleportEvent(this, x, y, z, 0.0F);
            if (net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(event)) {
                return false;
            }
            this.world.setEntityState(this, (byte) 46);
            this.setPositionAndUpdate(event.getTargetX(), event.getTargetY(), event.getTargetZ());
            return true;
        }
        return false;
    }

    public void leaveGroup() {
        if (this.groupLeader != null) {
            this.groupLeader.decreaseGroupSize();
        }
        this.groupLeader = null;
    }

    protected boolean hasNoLeader() {
        return !this.hasGroupLeader();
    }

    public boolean hasGroupLeader() {
        return this.groupLeader != null && this.groupLeader.isEntityAlive();
    }

    private void increaseGroupSize() {
        ++this.groupSize;
    }

    private void decreaseGroupSize() {
        --this.groupSize;
    }

    public boolean canGroupGrow() {
        return this.isGroupLeader() && this.groupSize < this.getMaxGroupSize();
    }

    private int getMaxGroupSize() {
        return 15;
    }

    @Override
    public int getMaxSpawnedInChunk() {
        return 7;
    }

    public boolean isGroupLeader() {
        return this.groupSize > 1;
    }

    public boolean inRangeOfGroupLeader() {
        return this.groupLeader != null && this.getDistanceSq(this.groupLeader) <= 121.0D;
    }

    public void moveToGroupLeader() {
        if (this.hasGroupLeader()) {
            this.moveHelper.setMoveTo(this.groupLeader.posX, this.groupLeader.posY, this.groupLeader.posZ, 1.0D);
        }
    }

    public void createAndSetLeader(EntityCosmicCod leader) {
        this.groupLeader = leader;
        leader.increaseGroupSize();
    }

    @Nullable
    @Override
    public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData livingdata) {
        if (livingdata == null) {
            livingdata = new GroupData(this);
        } else {
            this.createAndSetLeader(((GroupData) livingdata).groupLeader);
        }
        if (livingdata instanceof GroupData) {
            doInitialPosing((GroupData) livingdata);
        }
        return super.onInitialSpawn(difficulty, livingdata);
    }

    public boolean isCircling() {
        return circlePos != null && circleTime < maxCircleTime;
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

    public static class GroupData implements IEntityLivingData {
        public final EntityCosmicCod groupLeader;

        public GroupData(EntityCosmicCod groupLeaderIn) {
            this.groupLeader = groupLeaderIn;
        }
    }

    private static class AISwimIdle extends EntityAIBase {

        private final EntityCosmicCod cod;
        float circleDistance = 5;
        boolean clockwise = false;

        public AISwimIdle(EntityCosmicCod cod) {
            this.cod = cod;
        }

        @Override
        public boolean shouldExecute() {
            return this.cod.isGroupLeader() || cod.hasNoLeader() || cod.hasGroupLeader() && cod.groupLeader.circlePos != null;
        }

        @Override
        public void updateTask() {
            if (cod.circleTime > cod.maxCircleTime) {
                cod.circleTime = 0;
                cod.circlePos = null;
            }
            if (cod.circlePos != null && cod.circleTime <= cod.maxCircleTime) {
                cod.circleTime++;
                Vec3d movePos = getSharkCirclePos(cod.circlePos);
                cod.moveHelper.setMoveTo(movePos.x, movePos.y, movePos.z, 1.0F);
            } else if (this.cod.isGroupLeader()) {
                if (cod.baitballCooldown == 0) {
                    cod.resetBaitballCooldown();
                    if (cod.circlePos == null || cod.circleTime >= cod.maxCircleTime) {
                        cod.circleTime = 0;
                        cod.maxCircleTime = 360 + this.cod.getRNG().nextInt(80);
                        circleDistance = 1 + this.cod.getRNG().nextFloat();
                        clockwise = this.cod.getRNG().nextBoolean();
                        cod.circlePos = cod.getPosition().up();
                    }
                }
            } else if (cod.getRNG().nextInt(40) == 0 || cod.hasNoLeader()) {
                final Vec3d movepos = new Vec3d(cod.posX + cod.getRNG().nextInt(4) - 2,
                        cod.posY < 0 ? cod.posY + 1 : cod.posY + cod.getRNG().nextInt(4) - 2,
                        cod.posZ + cod.getRNG().nextInt(4) - 2);
                cod.moveHelper.setMoveTo(movepos.x, movepos.y, movepos.z, 1.0F);
            } else if (cod.hasGroupLeader() && cod.groupLeader.circlePos != null) {
                if (cod.circlePos == null) {
                    cod.circlePos = cod.groupLeader.circlePos;
                    cod.circleTime = cod.groupLeader.circleTime;
                    cod.maxCircleTime = cod.groupLeader.maxCircleTime;
                    circleDistance = 1 + this.cod.getRNG().nextFloat();
                    clockwise = this.cod.getRNG().nextBoolean();
                }
            }
        }

        public Vec3d getSharkCirclePos(BlockPos target) {
            final float prog = 1F - (cod.circleTime / (float) cod.maxCircleTime);
            final float angle = (STARTING_ANGLE * 10 * (clockwise ? -cod.circleTime : cod.circleTime));
            final float circleDistanceTimesProg = circleDistance * prog;
            final double extraX = (circleDistanceTimesProg + 0.75F) * MathHelper.sin(angle);
            final double extraZ = (circleDistanceTimesProg + 0.75F) * prog * MathHelper.cos(angle);
            return new Vec3d(target.getX() + 0.5F + extraX, Math.max(target.getY() + cod.getRNG().nextInt(4) - 2, -62), target.getZ() + 0.5F + extraZ);
        }
    }
}
