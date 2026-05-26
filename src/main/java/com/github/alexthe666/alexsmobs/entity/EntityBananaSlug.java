package com.github.alexthe666.alexsmobs.entity;

import com.github.alexthe666.alexsmobs.config.AMConfig;
import com.github.alexthe666.alexsmobs.entity.ai.AnimalAIWanderRanged;
import com.github.alexthe666.alexsmobs.item.AMItemRegistry;
import com.github.alexthe666.alexsmobs.misc.AMSoundRegistry;
import com.github.alexthe666.alexsmobs.misc.AMTagRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;

import javax.annotation.Nullable;

/**
 * Forge 1.12.2 port of 1.20 {@code EntityBananaSlug}.
 * Wall-climbing movement follows {@link EntityLeafcutterAnt}.
 */
public class EntityBananaSlug extends EntityAnimal {

    private static final ResourceLocation BANANA_SLUG_BREEDABLES = new ResourceLocation("alexsmobs", "banana_slug_breedables");

    private static final DataParameter<Byte> CLIMBING = EntityDataManager.createKey(EntityBananaSlug.class, DataSerializers.BYTE);
    private static final DataParameter<Byte> ATTACHED_FACE = EntityDataManager.createKey(EntityBananaSlug.class, DataSerializers.BYTE);
    private static final DataParameter<Integer> VARIANT = EntityDataManager.createKey(EntityBananaSlug.class, DataSerializers.VARINT);

    private static final EnumFacing[] HORIZONTALS = new EnumFacing[]{EnumFacing.NORTH, EnumFacing.EAST, EnumFacing.SOUTH, EnumFacing.WEST};

    public float trailYaw;
    public float prevTrailYaw;
    public float trailVisability;
    public float prevTrailVisability;

    public float attachChangeProgress = 0F;
    public float prevAttachChangeProgress = 0F;
    public EnumFacing prevAttachDir = EnumFacing.DOWN;
    private int timeUntilSlime = this.rand.nextInt(12000) + 24000;

    public EntityBananaSlug(World world) {
        super(world);
        this.prevTrailYaw = this.renderYawOffset;
        this.trailYaw = this.renderYawOffset;
        this.setSize(0.8F, 0.4F);
    }

    public static boolean checkBananaSlugSpawnRules(World world, BlockPos pos) {
        return !world.isAirBlock(pos.down());
    }

    @Override
    public boolean getCanSpawnHere() {
        return AMEntityRegistry.rollSpawn(AMConfig.bananaSlugSpawnRolls, this.getRNG(), AMEntityRegistry.AMSpawnReason.OTHER) && super.getCanSpawnHere();
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(CLIMBING, (byte) 0);
        this.dataManager.register(ATTACHED_FACE, (byte) EnumFacing.DOWN.getIndex());
        this.dataManager.register(VARIANT, 0);
    }

    @Override
    public void fall(float distance, float damageMultiplier) {
    }

    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }

    @Override
    public boolean isEntityInvulnerable(DamageSource source) {
        return source == DamageSource.IN_WALL || super.isEntityInvulnerable(source);
    }

    @Override
    public boolean isOnLadder() {
        return this.isBesideClimbableBlock();
    }

    public boolean isBesideClimbableBlock() {
        return (this.dataManager.get(CLIMBING) & 1) != 0 && this.getAttachmentFacing() != EnumFacing.DOWN;
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

    public EnumFacing getAttachmentFacing() {
        return facingFromIndex(this.dataManager.get(ATTACHED_FACE));
    }

  @Override
    protected void initEntityAI() {
        this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(2, new EntityAITempt(this, 1.0D, Item.getItemFromBlock(Blocks.BROWN_MUSHROOM), false) {
            @Override
            protected boolean isTempting(ItemStack stack) {
                return AMTagRegistry.itemInTag(BANANA_SLUG_BREEDABLES, stack.getItem());
            }
        });
        this.tasks.addTask(3, new EntityAIMate(this, 1.0D));
        this.tasks.addTask(4, new AnimalAIWanderRanged(this, 40, 1.0D, 10, 7));
        this.tasks.addTask(5, new EntityAIWatchClosest(this, EntityPlayer.class, 5.0F));
        this.tasks.addTask(6, new EntityAILookIdle(this));
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(4.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.1D);
    }

    @Override
    protected PathNavigate createNavigator(World worldIn) {
        return new PathNavigateGround(this, worldIn);
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return AMSoundRegistry.BANANA_SLUG_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return AMSoundRegistry.BANANA_SLUG_HURT;
    }

    @Override
    protected void playStepSound(BlockPos pos, Block blockIn) {
    }

    @Nullable
    @Override
    public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData livingdata) {
        this.setVariant(this.rand.nextInt(4));
        return super.onInitialSpawn(difficulty, livingdata);
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        return AMTagRegistry.itemInTag(BANANA_SLUG_BREEDABLES, stack.getItem());
    }

    @Override
    public void onLivingUpdate() {
        this.prevTrailYaw = this.trailYaw;
        this.prevTrailVisability = this.trailVisability;
        this.prevAttachChangeProgress = this.attachChangeProgress;
        super.onLivingUpdate();

        float maxHeadTurn = 4.0F;
        this.renderYawOffset = approachDegrees(this.renderYawOffset, this.rotationYaw, maxHeadTurn);
        this.trailYaw = approachDegrees(this.trailYaw, this.renderYawOffset, 2.0F);

        double motionLen = this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ * this.motionZ;
        boolean showTrail = isTrailVisible() && motionLen > 0.03D * 0.03D;

        if (this.prevAttachDir != this.getAttachmentFacing()) {
            if (attachChangeProgress < 5.0F) {
                attachChangeProgress += 1F;
                this.trailYaw = this.renderYawOffset;
            } else if (attachChangeProgress >= 5.0F) {
                this.prevAttachDir = this.getAttachmentFacing();
            }
        } else {
            this.attachChangeProgress = 5.0F;
        }

        if (trailVisability < 1.0F && showTrail) {
            trailVisability = Math.min(1.0F, trailVisability + 0.1F);
        }
        if (trailVisability > 0.0F && !showTrail) {
            float dec = motionLen > 0.03D * 0.03D ? 1.0F : 0.1F;
            trailVisability = Math.max(0.0F, trailVisability - dec);
        }

        double my = this.motionY;
        if (!this.world.isRemote) {
            this.setBesideClimbableBlock(this.collidedHorizontally || (this.collidedVertically && !this.onGround));
            if (this.onGround || this.isInWater() || this.isInLava()) {
                this.dataManager.set(ATTACHED_FACE, (byte) EnumFacing.DOWN.getIndex());
            } else if (this.collidedVertically) {
                this.dataManager.set(ATTACHED_FACE, (byte) EnumFacing.UP.getIndex());
            } else {
                EnumFacing closestDirection = EnumFacing.DOWN;
                double closestDistance = 100;
                BlockPos slugPos = new BlockPos(this);
                for (EnumFacing dir : HORIZONTALS) {
                    BlockPos offsetPos = slugPos.offset(dir);
                    Vec3d offset = new Vec3d(offsetPos.getX() + 0.5D, offsetPos.getY() + 0.5D, offsetPos.getZ() + 0.5D);
                    Vec3d self = new Vec3d(this.posX, this.posY, this.posZ);
                    if (closestDistance > self.distanceTo(offset) && this.world.isSideSolid(offsetPos, dir.getOpposite(), false)) {
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
            this.motionY = Math.max(this.motionY, 0.0D);
        }
        if (!flag && this.isOnLadder()) {
            this.motionY *= 0.4D;
        }
        if (!this.world.isRemote && this.isEntityAlive() && !this.isChild() && --this.timeUntilSlime <= 0) {
            this.entityDropItem(new ItemStack(AMItemRegistry.BANANA_SLUG_SLIME), 0.0F);
            this.timeUntilSlime = this.rand.nextInt(12000) + 24000;
        }
    }

    @Override
    public void notifyDataManagerChange(DataParameter<?> key) {
        super.notifyDataManagerChange(key);
        if (ATTACHED_FACE.equals(key)) {
            this.prevAttachChangeProgress = 0.0F;
            this.attachChangeProgress = 0.0F;
        }
    }

    private boolean isTrailVisible() {
        if (this.isInWater()) {
            return false;
        }
        if (this.onGround) {
            Vec3d modelBack = rotateLocalOffset(0, -0.1F, this.isChild() ? -0.35F : -0.7F, -this.trailYaw);
            Vec3d slugBack = new Vec3d(this.posX, this.posY, this.posZ).add(modelBack);
            BlockPos backPos = new BlockPos(slugBack);
            IBlockState state = world.getBlockState(backPos);
            AxisAlignedBB shape = state.getCollisionBoundingBox(world, backPos);
            if (shape == null || shape == Block.NULL_AABB) {
                return false;
            }
            return shape.maxY >= 0.8D;
        } else if (this.getAttachmentFacing().getAxis() != EnumFacing.Axis.Y) {
            BlockPos pos = this.getPosition().offset(this.getAttachmentFacing()).up(this.motionY <= -0.001D ? 1 : -1);
            IBlockState state = world.getBlockState(pos);
            AxisAlignedBB shape = state.getCollisionBoundingBox(world, pos);
            return shape != null && shape != Block.NULL_AABB;
        }
        return this.getAttachmentFacing() != EnumFacing.DOWN;
    }

    @Nullable
    @Override
    public EntityAgeable createChild(EntityAgeable ageable) {
        EntityBananaSlug slug = new EntityBananaSlug(this.world);
        slug.setVariant(this.getVariant());
        return slug;
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setInteger("Variant", this.getVariant());
        compound.setInteger("SlimeTime", this.timeUntilSlime);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        if (compound.hasKey("SlimeTime")) {
            this.timeUntilSlime = compound.getInteger("SlimeTime");
        }
        this.setVariant(compound.getInteger("Variant"));
    }

    public int getVariant() {
        return this.dataManager.get(VARIANT);
    }

    public void setVariant(int i) {
        this.dataManager.set(VARIANT, i);
    }

    private static float approachDegrees(float current, float target, float maxChange) {
        float delta = MathHelper.wrapDegrees(target - current);
        if (delta > maxChange) {
            delta = maxChange;
        }
        if (delta < -maxChange) {
            delta = -maxChange;
        }
        return current + delta;
    }

    private Vec3d rotateLocalOffset(double x, double y, double z, float yawDegrees) {
        float yaw = -yawDegrees * ((float) Math.PI / 180F);
        float cosY = MathHelper.cos(yaw);
        float sinY = MathHelper.sin(yaw);
        double x2 = x * cosY + z * sinY;
        double z2 = z * cosY - x * sinY;
        return new Vec3d(x2, y, z2);
    }

    private static EnumFacing facingFromIndex(byte b) {
        EnumFacing[] v = EnumFacing.values();
        int i = b & 7;
        return i >= 0 && i < v.length ? v[i] : EnumFacing.DOWN;
    }
}
