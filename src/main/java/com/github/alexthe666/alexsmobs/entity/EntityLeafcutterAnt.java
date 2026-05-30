package com.github.alexthe666.alexsmobs.entity;
import com.github.alexthe666.alexsmobs.misc.AMLootTables;

import com.github.alexthe666.alexsmobs.entity.ai.*;
import com.github.alexthe666.alexsmobs.item.AMItemRegistry;
import com.github.alexthe666.alexsmobs.misc.AMSoundRegistry;
import com.github.alexthe666.alexsmobs.tileentity.TileEntityLeafcutterAnthill;
import com.github.alexthe666.citadel.animation.Animation;
import com.github.alexthe666.citadel.animation.IAnimatedEntity;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityMoveHelper;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Forge 1.12.2 port of 1.16 {@code EntityLeafcutterAnt}.
 */
public class EntityLeafcutterAnt extends EntityAnimal implements IAngerable, IAnimatedEntity {

    public static final Animation ANIMATION_BITE = Animation.create(13);
    public static final ResourceLocation QUEEN_LOOT = new ResourceLocation("alexsmobs", "entities/leafcutter_ant_queen");

    private static final DataParameter<Byte> CLIMBING = EntityDataManager.createKey(EntityLeafcutterAnt.class, DataSerializers.BYTE);
    private static final DataParameter<Boolean> HAS_LEAF = EntityDataManager.createKey(EntityLeafcutterAnt.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Float> ANT_SCALE = EntityDataManager.createKey(EntityLeafcutterAnt.class, DataSerializers.FLOAT);
    private static final DataParameter<Byte> ATTACHED_FACE = EntityDataManager.createKey(EntityLeafcutterAnt.class, DataSerializers.BYTE);
    private static final DataParameter<Boolean> QUEEN = EntityDataManager.createKey(EntityLeafcutterAnt.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> ANGER_TIME = EntityDataManager.createKey(EntityLeafcutterAnt.class, DataSerializers.VARINT);
    private static final DataParameter<Boolean> HAS_LEAF_POS = EntityDataManager.createKey(EntityLeafcutterAnt.class, DataSerializers.BOOLEAN);
    private static final DataParameter<BlockPos> LEAF_HARVESTED_POS = EntityDataManager.createKey(EntityLeafcutterAnt.class, DataSerializers.BLOCK_POS);
    private static final DataParameter<Integer> LEAF_STATE_ID = EntityDataManager.createKey(EntityLeafcutterAnt.class, DataSerializers.VARINT);

    private static final EnumFacing[] HORIZONTALS = new EnumFacing[]{EnumFacing.NORTH, EnumFacing.EAST, EnumFacing.SOUTH, EnumFacing.WEST};

    public float attachChangeProgress = 0F;
    public float prevAttachChangeProgress = 0F;
    private EnumFacing prevAttachDir = EnumFacing.DOWN;
    @Nullable
    private EntityLeafcutterAnt caravanHead;
    @Nullable
    private EntityLeafcutterAnt caravanTail;
    private UUID lastHurtBy;
    @Nullable
    private BlockPos hivePos = null;
    private int stayOutOfHiveCountdown;
    private int animationTick;
    private Animation currentAnimation;
    private boolean isUpsideDownNavigator;
    private int haveBabyCooldown = 0;
    private long lastPlayerAttackGameTime = -1000000L;

    public EntityLeafcutterAnt(World world) {
        super(world);
        setPathPriority(net.minecraft.pathfinding.PathNodeType.WATER, -1.0F);
        setSize(0.5F, 0.35F);
        switchNavigator(true);
    }

    @Override
    public void setAttackTarget(@Nullable net.minecraft.entity.EntityLivingBase entitylivingbaseIn) {
        if (entitylivingbaseIn instanceof EntityPlayer && ((EntityPlayer) entitylivingbaseIn).capabilities.isCreativeMode) {
            return;
        }
        super.setAttackTarget(entitylivingbaseIn);
    }

    @Nullable
    protected ResourceLocation getLootTable() {
        return this.isQueen() ? AMLootTables.LEAFCUTTER_ANT_QUEEN : AMLootTables.LEAFCUTTER_ANT;
    }

    @Override
    public EnumCreatureAttribute getCreatureAttribute() {
        return EnumCreatureAttribute.ARTHROPOD;
    }

    private void switchNavigator(boolean rightsideUp) {
        if (rightsideUp) {
            this.moveHelper = new EntityMoveHelper(this);
            this.navigator = new PathNavigateGround(this, world);
            this.isUpsideDownNavigator = false;
        } else {
            this.moveHelper = new FlightMoveController(this, 0.6F, false);
            this.navigator = new DirectPathNavigator(this, world);
            this.isUpsideDownNavigator = true;
        }
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        AMEntityRegistry.registerAttributeIfAbsent(this, SharedMonsterAttributes.ATTACK_DAMAGE);
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(6.0D);
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(32.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.25D);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(2.0D);
    }

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(1, new ReturnToHiveGoal());
        this.tasks.addTask(2, new EntityAIAttackMelee(this, 1.0D, false));
        this.tasks.addTask(3, new TameableAITempt(this, 1.1D, AMItemRegistry.GONGYLIDIA, false));
        this.tasks.addTask(4, new LeafcutterAntAIFollowCaravan(this, 1D));
        this.tasks.addTask(5, new LeafcutterAntAIForageLeaves(this));
        this.tasks.addTask(6, new AnimalAIWanderRanged(this, 30, 1.0D, 25, 7));
        this.tasks.addTask(7, new EntityAIWatchClosest(this, EntityPlayer.class, 6.0F));
        this.tasks.addTask(8, new EntityAILookIdle(this));
        this.targetTasks.addTask(1, new AngerGoal(this));
        this.targetTasks.addTask(2, new ResetAngerGoal(this, true));
    }

    @Override
    public void fall(float distance, float damageMultiplier) {
    }

    public EnumFacing getAttachmentFacing() {
        return facingFromIndex(this.dataManager.get(ATTACHED_FACE));
    }

    @Override
    protected PathNavigate createNavigator(World worldIn) {
        return new PathNavigateGround(this, worldIn);
    }

    @Override
    protected net.minecraft.util.SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return isQueen() ? AMSoundRegistry.LEAFCUTTER_ANT_QUEEN_HURT : AMSoundRegistry.LEAFCUTTER_ANT_HURT;
    }

    @Override
    protected net.minecraft.util.SoundEvent getDeathSound() {
        return isQueen() ? AMSoundRegistry.LEAFCUTTER_ANT_QUEEN_HURT : AMSoundRegistry.LEAFCUTTER_ANT_HURT;
    }

    @Override
    protected void playStepSound(BlockPos pos, Block blockIn) {
    }

    private void pacifyAllNearby() {
        resetTargets();
        List<EntityLeafcutterAnt> list = world.getEntitiesWithinAABB(EntityLeafcutterAnt.class, this.getEntityBoundingBox().grow(20D, 6.0D, 20D));
        for (EntityLeafcutterAnt ant : list) {
            ant.resetTargets();
        }
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        ItemStack itemstack = player.getHeldItem(hand);
        Item item = itemstack.getItem();
        if (item == AMItemRegistry.GONGYLIDIA) {
            if (isQueen() && haveBabyCooldown == 0) {
                int babies = 1 + rand.nextInt(1);
                pacifyAllNearby();
                for (int i = 0; i < babies; i++) {
                    EntityLeafcutterAnt leafcutterAnt = (EntityLeafcutterAnt) com.github.alexthe666.alexsmobs.entity.AMEntityRegistry.LEAFCUTTER_ANT.newInstance(world);
                    leafcutterAnt.copyLocationAndAnglesFrom(this);
                    leafcutterAnt.setGrowingAge(-24000);
                    if (!world.isRemote) {
                        world.setEntityState(this, (byte) 18);
                        world.spawnEntity(leafcutterAnt);
                    }
                }
                if (!player.capabilities.isCreativeMode) {
                    itemstack.shrink(1);
                }
                haveBabyCooldown = 24000;
                this.setGrowingAge(0);
            } else {
                pacifyAllNearby();
                if (!player.capabilities.isCreativeMode) {
                    itemstack.shrink(1);
                }
                world.setEntityState(this, (byte) 48);
                this.heal(3);
            }
            return true;
        }
        return super.processInteract(player, hand);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void handleStatusUpdate(byte id) {
        if (id == 48) {
            for (int i = 0; i < 3; ++i) {
                double d0 = this.rand.nextGaussian() * 0.02D;
                double d1 = this.rand.nextGaussian() * 0.02D;
                double d2 = this.rand.nextGaussian() * 0.02D;
                this.world.spawnParticle(EnumParticleTypes.VILLAGER_HAPPY,
                        this.posX + (this.rand.nextFloat() - this.rand.nextFloat()) * 0.4F,
                        this.posY + this.rand.nextFloat() * this.height,
                        this.posZ + (this.rand.nextFloat() - this.rand.nextFloat()) * 0.4F,
                        d0, d1, d2);
            }
        } else {
            super.handleStatusUpdate(id);
        }
    }

    @Override
    public void onLivingUpdate() {
        this.prevAttachChangeProgress = this.attachChangeProgress;
        super.onLivingUpdate();
        if (this.isQueen() && this.width < 1.25F) {
            setSize(1.25F, 0.98F);
        }
        if (!this.isQueen() && (this.width != 0.5F * getAntScale() || this.height != 0.35F * getAntScale())) {
            setSize(0.5F * getAntScale(), 0.35F * getAntScale());
        }
        if (attachChangeProgress > 0F) {
            attachChangeProgress -= 0.25F;
        }
        this.stepHeight = isQueen() ? 1F : 0.5F;
        double my = this.motionY;
        if (!this.world.isRemote && !this.isQueen()) {
            this.setBesideClimbableBlock(this.collidedHorizontally || (this.collidedVertically && !this.onGround));
            if (this.onGround || this.isInWater() || this.isInLava()) {
                this.dataManager.set(ATTACHED_FACE, (byte) EnumFacing.DOWN.getIndex());
            } else if (this.collidedVertically) {
                this.dataManager.set(ATTACHED_FACE, (byte) EnumFacing.UP.getIndex());
            } else {
                EnumFacing closestDirection = EnumFacing.DOWN;
                double closestDistance = 100;
                BlockPos antPos = new BlockPos(this);
                for (EnumFacing dir : HORIZONTALS) {
                    BlockPos offsetPos = antPos.offset(dir);
                    Vec3d offset = new Vec3d(offsetPos.getX() + 0.5D, offsetPos.getY() + 0.5D, offsetPos.getZ() + 0.5D);
                    Vec3d self = new Vec3d(this.posX, this.posY, this.posZ);
                    if (closestDistance > self.distanceTo(offset) && world.isSideSolid(offsetPos, dir.getOpposite(), false)) {
                        closestDistance = self.distanceTo(offset);
                        closestDirection = dir;
                    }
                }
                this.dataManager.set(ATTACHED_FACE, (byte) closestDirection.getIndex());
            }
        }
        boolean flag = false;
        if (this.getAttachmentFacing() != EnumFacing.DOWN) {
            if (this.getAttachmentFacing() == EnumFacing.UP) {
                this.motionY += 1.0D;
            } else {
                if (!this.collidedHorizontally && this.getAttachmentFacing() != EnumFacing.UP) {
                    Vec3d vec = new Vec3d(this.getAttachmentFacing().getDirectionVec());
                    this.motionX += vec.x * 0.1D;
                    this.motionY += vec.y * 0.1D;
                    this.motionZ += vec.z * 0.1D;
                }
                if (!this.onGround && my < 0.0D) {
                    this.motionY *= 0.5D;
                    flag = true;
                }
            }
        }
        if (this.getAttachmentFacing() == EnumFacing.UP) {
            this.motionX *= 0.7D;
            this.motionZ *= 0.7D;
        }
        if (!flag && this.isOnLadder()) {
            this.motionX *= 1.0D;
            this.motionY *= 0.4D;
            this.motionZ *= 1.0D;
        }
        if (prevAttachDir != this.getAttachmentFacing()) {
            attachChangeProgress = 1F;
        }
        this.prevAttachDir = this.getAttachmentFacing();
        if (!this.world.isRemote) {
            if (this.getAttachmentFacing() == EnumFacing.UP && !this.isUpsideDownNavigator) {
                switchNavigator(false);
            }
            if (this.getAttachmentFacing() != EnumFacing.UP && this.isUpsideDownNavigator) {
                switchNavigator(true);
            }
            if (this.stayOutOfHiveCountdown > 0) {
                --this.stayOutOfHiveCountdown;
            }
            if (this.ticksExisted % 20 == 0 && !this.isHiveValid()) {
                this.hivePos = null;
            }
            net.minecraft.entity.EntityLivingBase attackTarget = this.getAttackTarget();
            if (attackTarget != null && getDistance(attackTarget) < attackTarget.width + this.width + 1 && this.canEntityBeSeen(attackTarget)) {
                if (this.getAnimation() == ANIMATION_BITE && this.getAnimationTick() == 6) {
                    float damage = (float) this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
                    attackTarget.attackEntityFrom(DamageSource.causeMobDamage(this), damage);
                }
            }
        }
        AMEntityRegistry.updateAnimations(this);
    }

    private boolean isHiveValid() {
        if (!this.hasHive()) {
            return false;
        } else {
            TileEntity tileentity = this.world.getTileEntity(this.hivePos);
            return tileentity instanceof TileEntityLeafcutterAnthill;
        }
    }

    @Override
    public boolean isOnLadder() {
        return this.isBesideClimbableBlock();
    }

    public boolean isBesideClimbableBlock() {
        return (this.dataManager.get(CLIMBING) & 1) != 0;
    }

    public void setBesideClimbableBlock(boolean climbing) {
        byte b0 = this.dataManager.get(CLIMBING);
        if (climbing) {
            b0 = (byte) (b0 | 1);
        } else {
            b0 = (byte) (b0 & -2);
        }
        this.dataManager.set(CLIMBING, b0);
    }

    @Override
    public int getAngerTime() {
        return this.dataManager.get(ANGER_TIME);
    }

    @Override
    public void setAngerTime(int time) {
        this.dataManager.set(ANGER_TIME, time);
    }

    @Override
    public UUID getAngerTarget() {
        return this.lastHurtBy;
    }

    @Override
    public void setAngerTarget(@Nullable UUID target) {
        this.lastHurtBy = target;
    }

    @Override
    public void func_230258_H__() {
        this.setAngerTime(10 + rand.nextInt(11));
    }

    @Override
    public void resetTargets() {
        this.setAngerTime(0);
        this.setAngerTarget(null);
        this.setAttackTarget(null);
        this.setRevengeTarget(null);
    }

    @Override
    public boolean wasHurtByPlayerRecently() {
        return world.getTotalWorldTime() - this.lastPlayerAttackGameTime <= 100L;
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        boolean ok = super.attackEntityFrom(source, amount);
        if (ok && source.getTrueSource() instanceof EntityPlayer) {
            this.lastPlayerAttackGameTime = world.getTotalWorldTime();
        }
        return ok;
    }

    @Override
    protected void updateAITasks() {
        super.updateAITasks();
        if (!this.world.isRemote) {
            tickIAngerable();
        }
    }

    /**
     * Mirrors 1.16 {@code IAngerable} server tick inside {@link #updateAITasks()}.
     */
    private void tickIAngerable() {
        UUID id = this.getAngerTarget();
        if (this.getAngerTime() > 0) {
            this.setAngerTime(this.getAngerTime() - 1);
        }
        if (id != null) {
            net.minecraft.entity.Entity e = null;
            if (this.world instanceof WorldServer) {
                e = ((WorldServer) this.world).getEntityFromUuid(id);
            }
            if (e instanceof net.minecraft.entity.EntityLivingBase) {
                if (this.getAngerTime() > 0 && this.getAttackTarget() != e) {
                    this.setAttackTarget((net.minecraft.entity.EntityLivingBase) e);
                }
            } else if (this.getAngerTime() <= 0) {
                this.setAngerTarget(null);
                this.setAttackTarget(null);
            }
        } else if (this.getAngerTime() <= 0) {
            this.setAttackTarget(null);
        }
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(CLIMBING, (byte) 0);
        this.dataManager.register(HAS_LEAF_POS, Boolean.FALSE);
        this.dataManager.register(LEAF_HARVESTED_POS, BlockPos.ORIGIN);
        this.dataManager.register(LEAF_STATE_ID, 0);
        this.dataManager.register(HAS_LEAF, Boolean.FALSE);
        this.dataManager.register(QUEEN, Boolean.FALSE);
        this.dataManager.register(ATTACHED_FACE, (byte) EnumFacing.DOWN.getIndex());
        this.dataManager.register(ANT_SCALE, 1.0F);
        this.dataManager.register(ANGER_TIME, 0);
    }

    public float getAntScale() {
        return this.dataManager.get(ANT_SCALE);
    }

    public void setAntScale(float scale) {
        this.dataManager.set(ANT_SCALE, scale);
    }

    @Nullable
    public BlockPos getHarvestedPos() {
        return this.dataManager.get(HAS_LEAF_POS) ? this.dataManager.get(LEAF_HARVESTED_POS) : null;
    }

    public void setLeafHarvestedPos(@Nullable BlockPos harvestedPos) {
        if (harvestedPos == null) {
            this.dataManager.set(HAS_LEAF_POS, Boolean.FALSE);
        } else {
            this.dataManager.set(HAS_LEAF_POS, Boolean.TRUE);
            this.dataManager.set(LEAF_HARVESTED_POS, harvestedPos);
        }
    }

    @Nullable
    public IBlockState getHarvestedState() {
        int id = this.dataManager.get(LEAF_STATE_ID);
        return id == 0 ? null : Block.getStateById(id);
    }

    public void setLeafHarvestedState(@Nullable IBlockState state) {
        if (state == null) {
            this.dataManager.set(LEAF_STATE_ID, 0);
        } else {
            this.dataManager.set(LEAF_STATE_ID, Block.getStateId(state));
        }
    }

    public boolean hasLeaf() {
        return this.dataManager.get(HAS_LEAF);
    }

    public void setLeaf(boolean leaf) {
        this.dataManager.set(HAS_LEAF, leaf);
    }

    public boolean isQueen() {
        return this.dataManager.get(QUEEN);
    }

    public void setQueen(boolean queen) {
        boolean prev = isQueen();
        if (!prev && queen) {
            this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(36.0D);
            this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(6.0D);
            setHealth(36F);
            setSize(1.25F, 0.98F);
        } else if (prev && !queen) {
            this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(6.0D);
            this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(2.0D);
            setSize(0.5F * getAntScale(), 0.35F * getAntScale());
        }
        this.dataManager.set(QUEEN, queen);
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setByte("AttachFace", (byte) this.getAttachmentFacing().getIndex());
        compound.setBoolean("Leaf", this.hasLeaf());
        compound.setBoolean("Queen", this.isQueen());
        compound.setFloat("AntScale", this.getAntScale());
        IBlockState blockstate = this.getHarvestedState();
        if (blockstate != null) {
            NBTTagCompound stateTag = new NBTTagCompound();
            stateTag.setString("Name", blockstate.getBlock().getRegistryName().toString());
            stateTag.setInteger("Meta", blockstate.getBlock().getMetaFromState(blockstate));
            compound.setTag("HarvestedLeafState", stateTag);
        }
        if (this.hasHive()) {
            BlockPos hp = this.getHivePos();
            NBTTagCompound hpTag = new NBTTagCompound();
            hpTag.setInteger("X", hp.getX());
            hpTag.setInteger("Y", hp.getY());
            hpTag.setInteger("Z", hp.getZ());
            compound.setTag("HivePos", hpTag);
        }
        compound.setInteger("CannotEnterHiveTicks", this.stayOutOfHiveCountdown);
        compound.setInteger("BabyCooldown", this.haveBabyCooldown);
        BlockPos blockpos = this.getHarvestedPos();
        if (blockpos != null) {
            compound.setInteger("HLPX", blockpos.getX());
            compound.setInteger("HLPY", blockpos.getY());
            compound.setInteger("HLPZ", blockpos.getZ());
        }
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.dataManager.set(ATTACHED_FACE, compound.getByte("AttachFace"));
        this.setLeaf(compound.getBoolean("Leaf"));
        this.setQueen(compound.getBoolean("Queen"));
        this.setAntScale(compound.getFloat("AntScale"));
        IBlockState blockstate = null;
        if (compound.hasKey("HarvestedLeafState", 10)) {
            NBTTagCompound st = compound.getCompoundTag("HarvestedLeafState");
            Block b = Block.getBlockFromName(st.getString("Name"));
            if (b != null) {
                blockstate = b.getStateFromMeta(st.getInteger("Meta"));
            }
        }
        this.stayOutOfHiveCountdown = compound.getInteger("CannotEnterHiveTicks");
        this.haveBabyCooldown = compound.getInteger("BabyCooldown");
        this.hivePos = null;
        if (compound.hasKey("HivePos", 10)) {
            NBTTagCompound hp = compound.getCompoundTag("HivePos");
            this.hivePos = new BlockPos(hp.getInteger("X"), hp.getInteger("Y"), hp.getInteger("Z"));
        }
        this.setLeafHarvestedState(blockstate);
        if (compound.hasKey("HLPX")) {
            int i = compound.getInteger("HLPX");
            int j = compound.getInteger("HLPY");
            int k = compound.getInteger("HLPZ");
            this.setLeafHarvestedPos(new BlockPos(i, j, k));
        } else {
            this.setLeafHarvestedPos(null);
        }
    }

    public void setStayOutOfHiveCountdown(int ticks) {
        this.stayOutOfHiveCountdown = ticks;
    }

    public boolean hasHive() {
        return this.hivePos != null;
    }

    @Nullable
    public BlockPos getHivePos() {
        return this.hivePos;
    }

    public void leaveCaravan() {
        if (this.caravanHead != null) {
            this.caravanHead.caravanTail = null;
        }
        this.caravanHead = null;
    }

    public void joinCaravan(EntityLeafcutterAnt caravanHeadIn) {
        this.caravanHead = caravanHeadIn;
        this.caravanHead.caravanTail = this;
    }

    public boolean hasCaravanTrail() {
        return this.caravanTail != null;
    }

    public boolean inCaravan() {
        return this.caravanHead != null;
    }

    @Nullable
    public EntityLeafcutterAnt getCaravanHead() {
        return this.caravanHead;
    }

    @Override
    @Nullable
    public EntityAgeable createChild(EntityAgeable ageable) {
        return null;
    }

    public boolean shouldLeadCaravan() {
        return !this.hasLeaf();
    }

    public void func_233629_a_(net.minecraft.entity.EntityLivingBase entityLivingBase, boolean unused) {
        entityLivingBase.prevLimbSwingAmount = entityLivingBase.limbSwingAmount;
        double d0 = entityLivingBase.posX - entityLivingBase.prevPosX;
        double d1 = (entityLivingBase.posY - entityLivingBase.prevPosY) * 2.0D;
        double d2 = entityLivingBase.posZ - entityLivingBase.prevPosZ;
        float f = MathHelper.sqrt(d0 * d0 + d1 * d1 + d2 * d2) * 4.0F;
        if (f > 1.0F) {
            f = 1.0F;
        }
        entityLivingBase.limbSwingAmount += (f - entityLivingBase.limbSwingAmount) * 0.4F;
        entityLivingBase.limbSwing += entityLivingBase.limbSwingAmount;
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
        return new Animation[]{ANIMATION_BITE};
    }

    @Override
    public int getAnimationTick() {
        return animationTick;
    }

    @Override
    public void setAnimationTick(int tick) {
        animationTick = tick;
    }

    @Override
    public boolean attackEntityAsMob(net.minecraft.entity.Entity entityIn) {
        this.setAnimation(ANIMATION_BITE);
        return true;
    }

    @Nullable
    private BlockPos findNearestAnthill(int radiusBlocks) {
        BlockPos origin = new BlockPos(this);
        BlockPos closest = null;
        double best = Double.MAX_VALUE;
        double r2 = (double) radiusBlocks * (double) radiusBlocks;
        for (TileEntity te : world.loadedTileEntityList) {
            if (te instanceof TileEntityLeafcutterAnthill && !te.isInvalid()) {
                double d = te.getPos().distanceSq(origin);
                if (d <= r2 && (closest == null || d < best)) {
                    best = d;
                    closest = te.getPos();
                }
            }
        }
        return closest;
    }

    private class ReturnToHiveGoal extends EntityAIBase {

        private int searchCooldown = 1;
        private BlockPos hiveTargetPos;
        private int approachTime = 0;

        ReturnToHiveGoal() {
        }

        @Override
        public boolean shouldExecute() {
            if (EntityLeafcutterAnt.this.stayOutOfHiveCountdown > 0) {
                return false;
            }
            if (EntityLeafcutterAnt.this.hasLeaf() || EntityLeafcutterAnt.this.isQueen()) {
                searchCooldown--;
                BlockPos hive = EntityLeafcutterAnt.this.hivePos;
                if (hive != null && EntityLeafcutterAnt.this.world.getTileEntity(hive) instanceof TileEntityLeafcutterAnthill) {
                    hiveTargetPos = hive;
                    return true;
                }
                if (searchCooldown <= 0) {
                    searchCooldown = 400;
                    BlockPos ret = EntityLeafcutterAnt.this.findNearestAnthill(100);
                    hiveTargetPos = ret;
                    EntityLeafcutterAnt.this.hivePos = ret;
                    return hiveTargetPos != null;
                }
            }
            return false;
        }

        @Override
        public boolean shouldContinueExecuting() {
            return hiveTargetPos != null
                    && EntityLeafcutterAnt.this.getDistanceSq(
                            hiveTargetPos.getX() + 0.5D,
                            hiveTargetPos.getY() + 1.0D,
                            hiveTargetPos.getZ() + 0.5D) > 1.0D;
        }

        @Override
        public void resetTask() {
            this.hiveTargetPos = null;
            this.searchCooldown = 20;
            this.approachTime = 0;
        }

        @Override
        public void updateTask() {
            if (hiveTargetPos == null) {
                return;
            }
            double dist = EntityLeafcutterAnt.this.getDistanceSq(
                    hiveTargetPos.getX() + 0.5D,
                    hiveTargetPos.getY() + 1.0D,
                    hiveTargetPos.getZ() + 0.5D);
            BlockPos under = new BlockPos(EntityLeafcutterAnt.this.posX, EntityLeafcutterAnt.this.posY - 0.2D, EntityLeafcutterAnt.this.posZ).down();
            if (dist < 1.2D && under.equals(hiveTargetPos)) {
                TileEntity tileentity = EntityLeafcutterAnt.this.world.getTileEntity(hiveTargetPos);
                if (tileentity instanceof TileEntityLeafcutterAnthill) {
                    ((TileEntityLeafcutterAnthill) tileentity).tryEnterHive(EntityLeafcutterAnt.this, EntityLeafcutterAnt.this.hasLeaf());
                }
            }
            if (dist < 256.0D) {
                approachTime++;
                if (dist < (approachTime < 200 ? 2.0D : 10.0D) && EntityLeafcutterAnt.this.posY >= hiveTargetPos.getY()) {
                    if (EntityLeafcutterAnt.this.getAttachmentFacing() != EnumFacing.DOWN) {
                        EntityLeafcutterAnt.this.motionY += 0.1D;
                    }
                    EntityMoveHelper mh = EntityLeafcutterAnt.this.getMoveHelper();
                    mh.setMoveTo(
                            (double) hiveTargetPos.getX() + 0.5D,
                            (double) hiveTargetPos.getY() + 1.5D,
                            (double) hiveTargetPos.getZ() + 0.5D,
                            1.0D);
                }
                EntityLeafcutterAnt.this.getNavigator().tryMoveToXYZ(
                        (double) hiveTargetPos.getX() + 0.5F,
                        (double) hiveTargetPos.getY() + 1.6F,
                        (double) hiveTargetPos.getZ() + 0.5F, 1.0D);
            } else {
                EntityLeafcutterAnt.this.getNavigator().tryMoveToXYZ(hiveTargetPos.getX(), hiveTargetPos.getY(), hiveTargetPos.getZ(), 1.0D);
            }
        }
    }

    class AngerGoal extends net.minecraft.entity.ai.EntityAIHurtByTarget {

        AngerGoal(EntityLeafcutterAnt ant) {
            super(ant, false);
        }

        @Override
        public boolean shouldContinueExecuting() {
            return EntityLeafcutterAnt.this.getAngerTime() > 0 && super.shouldContinueExecuting();
        }

        protected void alertOther(EntityCreature creatureIn, net.minecraft.entity.EntityLivingBase target) {
            if (creatureIn instanceof EntityLeafcutterAnt && this.taskOwner.canEntityBeSeen(target)) {
                creatureIn.setAttackTarget(target);
            }
        }
    }

    private static EnumFacing facingFromIndex(byte b) {
        EnumFacing[] v = EnumFacing.values();
        int i = b & 7;
        return i >= 0 && i < v.length ? v[i] : EnumFacing.DOWN;
    }
}
