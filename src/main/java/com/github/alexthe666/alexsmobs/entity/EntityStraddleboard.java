package com.github.alexthe666.alexsmobs.entity;

import com.github.alexthe666.alexsmobs.enchantment.AMEnchantmentRegistry;
import com.github.alexthe666.alexsmobs.item.AMItemRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.IJumpingMount;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

public class EntityStraddleboard extends Entity implements IJumpingMount {

    private static final DataParameter<ItemStack> ITEMSTACK = EntityDataManager.createKey(EntityStraddleboard.class, DataSerializers.ITEM_STACK);
    private static final DataParameter<Integer> TIME_SINCE_HIT = EntityDataManager.createKey(EntityStraddleboard.class, DataSerializers.VARINT);
    private static final DataParameter<Float> DAMAGE_TAKEN = EntityDataManager.createKey(EntityStraddleboard.class, DataSerializers.FLOAT);
    private static final DataParameter<Integer> ROCKING_TICKS = EntityDataManager.createKey(EntityStraddleboard.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> COLOR = EntityDataManager.createKey(EntityStraddleboard.class, DataSerializers.VARINT);
    private static final DataParameter<Boolean> DEFAULT_COLOR = EntityDataManager.createKey(EntityStraddleboard.class, DataSerializers.BOOLEAN);
    public float boardRot = 0;
    public float prevBoardRot = 0;
    private double lastYd;
    private boolean rocking;
    private boolean downwards;
    private float rockingIntensity;
    private float rockingAngle;
    private float prevRockingAngle;
    private boolean jumpOutOfLava = false;
    private float outOfControlTicks;
    private Status status;
    private Status previousStatus;
    private float momentum;
    private double waterLevel;
    private float boatGlide;
    private int extinguishTimer = 0;

    public EntityStraddleboard(World worldIn) {
        super(worldIn);
        this.isImmuneToFire = true;
        this.preventEntitySpawning = true;
        this.setSize(1.375F, 0.5625F);
    }

    public EntityStraddleboard(World worldIn, double x, double y, double z) {
        this(worldIn);
        this.setPosition(x, y, z);
        this.motionX = 0;
        this.motionY = 0;
        this.motionZ = 0;
        this.prevPosX = x;
        this.prevPosY = y;
        this.prevPosZ = z;
    }

    public static boolean canCollideWithEntity(Entity p_242378_0_, Entity entity) {
        return entity.canBePushed() && !p_242378_0_.isRidingSameEntity(entity);
    }

    @Override
    public float getEyeHeight() {
        return this.height;
    }

    @Override
    protected boolean canTriggerWalking() {
        return false;
    }

    @Override
    protected void entityInit() {
        this.dataManager.register(TIME_SINCE_HIT, 0);
        this.dataManager.register(ITEMSTACK, new ItemStack(AMItemRegistry.STRADDLEBOARD));
        this.dataManager.register(ROCKING_TICKS, 0);
        this.dataManager.register(DEFAULT_COLOR, Boolean.TRUE);
        this.dataManager.register(COLOR, 0);
        this.dataManager.register(DAMAGE_TAKEN, 0.0F);
    }

    @Override
    public boolean shouldRiderSit() {
        return false;
    }

    public boolean canCollide(Entity entity) {
        return canCollideWithEntity(this, entity);
    }

    @Override
    public boolean canBePushed() {
        return true;
    }

    @Override
    public double getMountedYOffset() {
        return 0.9D;
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (this.isEntityInvulnerable(source)) {
            return false;
        } else if (!this.world.isRemote && !this.isDead) {
            this.setTimeSinceHit(10);
            this.setDamageTaken(this.getDamageTaken() + amount * 10.0F);
            this.velocityChanged = true;
            this.setRockingTicks(25);
            boolean flag = source.getTrueSource() instanceof EntityPlayer && ((EntityPlayer) source.getTrueSource()).capabilities.isCreativeMode;
            if (flag || this.getDamageTaken() > 40.0F) {
                if (!flag) {
                    EntityPlayer p = null;
                    if (source.getTrueSource() instanceof EntityPlayer) {
                        p = (EntityPlayer) source.getTrueSource();
                    }
                    if (this.getControllingPassenger() != null && this.getControllingPassenger() instanceof EntityPlayer) {
                        p = (EntityPlayer) this.getControllingPassenger();
                    }
                    boolean dropItem = true;
                    if (p != null && this.getEnchant(AMEnchantmentRegistry.STRADDLE_BOARDRETURN) > 0) {
                        if (p.inventory.addItemStackToInventory(this.getItemBoard())) {
                            dropItem = false;
                        }
                    }
                    if (dropItem) {
                        if (this.world.getGameRules().getBoolean("doEntityDrops")) {
                            this.entityDropItem(this.getItemBoard(), 0.0F);
                        }
                    }
                }
                this.setDead();
            }
            return true;
        } else {
            return true;
        }
    }

    private ItemStack getItemBoard() {
        return this.getItemStack();
    }

    @Override
    public void applyEntityCollision(Entity entityIn) {
        if (entityIn instanceof EntityStraddleboard) {
            if (entityIn.getEntityBoundingBox().minY < this.getEntityBoundingBox().maxY) {
                super.applyEntityCollision(entityIn);
            }
        } else if (entityIn.getEntityBoundingBox().minY <= this.getEntityBoundingBox().minY) {
            super.applyEntityCollision(entityIn);
        }
    }

    @SideOnly(Side.CLIENT)
    public void performHurtAnimation() {
        this.setTimeSinceHit(10);
        this.setDamageTaken(this.getDamageTaken() * 11.0F);
    }

    @Override
    public boolean canBeCollidedWith() {
        return !this.isDead;
    }

    private Status getBoatStatus() {
        Status under = this.getUnderwaterStatus();
        if (under != null) {
            this.waterLevel = this.getEntityBoundingBox().maxY;
            return under;
        } else if (this.checkInWater()) {
            return Status.IN_WATER;
        } else {
            float f = this.getBoatGlide();
            if (f > 0.0F) {
                this.boatGlide = f;
                return Status.ON_LAND;
            } else {
                return Status.IN_AIR;
            }
        }
    }

    public float getBoatGlide() {
        AxisAlignedBB axisalignedbb = this.getEntityBoundingBox();
        AxisAlignedBB axisalignedbb1 = new AxisAlignedBB(axisalignedbb.minX, axisalignedbb.minY - 0.001D, axisalignedbb.minZ, axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.maxZ);
        int i = MathHelper.floor(axisalignedbb1.minX) - 1;
        int j = MathHelper.ceil(axisalignedbb1.maxX) + 1;
        int k = MathHelper.floor(axisalignedbb1.minY) - 1;
        int l = MathHelper.ceil(axisalignedbb1.maxY) + 1;
        int i1 = MathHelper.floor(axisalignedbb1.minZ) - 1;
        int j1 = MathHelper.ceil(axisalignedbb1.maxZ) + 1;
        float f = 0.0F;
        int k1 = 0;
        BlockPos.MutableBlockPos blockpos$mutable = new BlockPos.MutableBlockPos();

        for (int l1 = i; l1 < j; ++l1) {
            for (int i2 = i1; i2 < j1; ++i2) {
                int j2 = (l1 != i && l1 != j - 1 ? 0 : 1) + (i2 != i1 && i2 != j1 - 1 ? 0 : 1);
                if (j2 != 2) {
                    for (int k2 = k; k2 < l; ++k2) {
                        if (j2 <= 0 || k2 != k && k2 != l - 1) {
                            blockpos$mutable.setPos(l1, k2, i2);
                            IBlockState blockstate = this.world.getBlockState(blockpos$mutable);
                            if (blockstate.getBlock() == Blocks.WATERLILY) {
                                continue;
                            }
                            AxisAlignedBB blockbb = blockstate.getBoundingBox(this.world, blockpos$mutable).offset(blockpos$mutable);
                            if (blockbb != null && blockbb != Block.NULL_AABB && axisalignedbb1.intersects(blockbb)) {
                                f += blockstate.getBlock().getSlipperiness(blockstate, this.world, blockpos$mutable, this);
                                ++k1;
                            }
                        }
                    }
                }
            }
        }

        return k1 > 0 ? f / (float) k1 : 0.0F;
    }

    private static float getLiquidColumnTop(World world, BlockPos pos, Material mat) {
        IBlockState st = world.getBlockState(pos);
        if (st.getMaterial() != mat) {
            return 0;
        }
        if (st.getBlock() instanceof BlockLiquid) {
            float frac = BlockLiquid.getLiquidHeightPercent(st.getValue(BlockLiquid.LEVEL));
            return pos.getY() + frac;
        }
        return pos.getY() + 1.0F;
    }

    private boolean checkInWater() {
        AxisAlignedBB axisalignedbb = this.getEntityBoundingBox();
        int i = MathHelper.floor(axisalignedbb.minX);
        int j = MathHelper.ceil(axisalignedbb.maxX);
        int k = MathHelper.floor(axisalignedbb.minY);
        int l = MathHelper.ceil(axisalignedbb.minY - 0.001D);
        int i1 = MathHelper.floor(axisalignedbb.minZ);
        int j1 = MathHelper.ceil(axisalignedbb.maxZ);
        boolean flag = false;
        this.waterLevel = Double.MIN_VALUE;
        BlockPos.MutableBlockPos blockpos$mutable = new BlockPos.MutableBlockPos();

        for (int k1 = i; k1 < j; ++k1) {
            for (int l1 = k; l1 < l; ++l1) {
                for (int i2 = i1; i2 < j1; ++i2) {
                    blockpos$mutable.setPos(k1, l1, i2);
                    IBlockState bs = this.world.getBlockState(blockpos$mutable);
                    Material mat = bs.getMaterial();
                    if (mat == Material.WATER || mat == Material.LAVA) {
                        float surface = getLiquidColumnTop(this.world, blockpos$mutable, mat);
                        this.waterLevel = Math.max(surface, this.waterLevel);
                        flag |= axisalignedbb.minY < (double) surface;
                    }
                }
            }
        }

        return flag;
    }

    private void updateMotion() {
        double d1 = this.hasNoGravity() ? 0.0D : -0.04D;
        double d2 = 0.0D;
        this.momentum = 0.05F;
        if (this.previousStatus == Status.IN_AIR && this.status != Status.IN_AIR && this.status != Status.ON_LAND) {
            this.waterLevel = this.posY + 1.0D;
            this.setPosition(this.posX, (double) (this.getWaterLevelAbove() - this.height) + 0.25, this.posZ);
            this.motionX *= 1.0D;
            this.motionY *= 1.0D;
            this.motionZ *= 1.0D;
            this.lastYd = 0.0D;
            this.status = Status.IN_WATER;
        } else {
            if (this.status == Status.IN_WATER) {
                d2 = (this.waterLevel - this.posY) / (double) this.height;
                this.momentum = 0.9F;
            } else if (this.status == Status.UNDER_FLOWING_WATER) {
                d1 = -7.0E-4D;
                this.momentum = 0.9F;
            } else if (this.status == Status.UNDER_WATER) {
                d2 = 0.01F;
                this.momentum = 0.45F;
            } else if (this.status == Status.IN_AIR) {
                this.momentum = 0.9F;
            } else if (this.status == Status.ON_LAND) {
                this.momentum = this.boatGlide;
                if (this.getControllingPassenger() instanceof EntityPlayer) {
                    this.boatGlide /= 2.0F;
                }
            }

            this.motionX *= (double) this.momentum;
            this.motionY += d1;
            this.motionZ *= (double) this.momentum;
            if (d2 > 0.0D) {
                this.motionY = (this.motionY + d2 * 0.06153846016296973D) * 0.75D;
            }
        }
    }

    public boolean isDefaultColor() {
        return this.dataManager.get(DEFAULT_COLOR);
    }

    public void setDefaultColor(boolean bar) {
        this.dataManager.set(DEFAULT_COLOR, bar);
    }

    public int getColor() {
        if (isDefaultColor()) {
            return 0XADC3D7;
        }
        return this.dataManager.get(COLOR);
    }

    public void setColor(int index) {
        this.dataManager.set(COLOR, index);
    }

    @Nullable
    private Status getUnderwaterStatus() {
        AxisAlignedBB axisalignedbb = this.getEntityBoundingBox();
        double d0 = axisalignedbb.maxY + 0.001D;
        int i = MathHelper.floor(axisalignedbb.minX);
        int j = MathHelper.ceil(axisalignedbb.maxX);
        int k = MathHelper.floor(axisalignedbb.maxY);
        int l = MathHelper.ceil(d0);
        int i1 = MathHelper.floor(axisalignedbb.minZ);
        int j1 = MathHelper.ceil(axisalignedbb.maxZ);
        boolean flag = false;
        BlockPos.MutableBlockPos blockpos$mutable = new BlockPos.MutableBlockPos();

        for (int k1 = i; k1 < j; ++k1) {
            for (int l1 = k; l1 < l; ++l1) {
                for (int i2 = i1; i2 < j1; ++i2) {
                    blockpos$mutable.setPos(k1, l1, i2);
                    IBlockState st = this.world.getBlockState(blockpos$mutable);
                    Material mat = st.getMaterial();
                    if (mat != Material.WATER && mat != Material.LAVA) {
                        continue;
                    }
                    float top = getLiquidColumnTop(this.world, blockpos$mutable, mat);
                    if (d0 < (double) top) {
                        if (!isLiquidSourceBlock(this.world, blockpos$mutable, st)) {
                            return Status.UNDER_FLOWING_WATER;
                        }
                        flag = true;
                    }
                }
            }
        }

        return flag ? Status.UNDER_WATER : null;
    }

    private static boolean isLiquidSourceBlock(World world, BlockPos pos, IBlockState st) {
        if (!(st.getBlock() instanceof BlockLiquid)) {
            return true;
        }
        if (st.getMaterial() == Material.WATER) {
            return st.getBlock() == Blocks.WATER || st.getValue(BlockLiquid.LEVEL) == 0;
        }
        if (st.getMaterial() == Material.LAVA) {
            return st.getBlock() == Blocks.LAVA || st.getValue(BlockLiquid.LEVEL) == 0;
        }
        return true;
    }

    private static double horizontalMag(Vec3d v) {
        return v.x * v.x + v.z * v.z;
    }

    private boolean hasPlayerPassenger() {
        for (Entity e : this.getPassengers()) {
            if (e instanceof EntityPlayer) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onUpdate() {
        prevBoardRot = this.boardRot;
        super.onUpdate();
        this.previousStatus = this.status;
        this.status = this.getBoatStatus();
        this.doBlockCollisions();
        if (this.isEntityInsideOpaqueBlock()) {
            this.pushOutOfBlocks(this.posX, (this.getEntityBoundingBox().minY + this.getEntityBoundingBox().maxY) / 2.0D, this.posZ);
        }
        if (this.getTimeSinceHit() > 0) {
            this.setTimeSinceHit(this.getTimeSinceHit() - 1);
        }
        if (this.getDamageTaken() > 0.0F) {
            this.setDamageTaken(this.getDamageTaken() - 1.0F);
        }
        if (this.isInLava()) {
            this.setNoGravity(true);
            double eyesY = this.posY + (double) this.getEyeHeight();
            BlockPos eyePos = new BlockPos(this.posX, eyesY, this.posZ);
            if (this.world.getBlockState(eyePos).getMaterial() == Material.LAVA) {
                this.motionX = 0;
                this.motionY = 0.1D;
                this.motionZ = 0;
            }
        } else {
            this.setNoGravity(false);
        }
        float f2 = (float) -((float) this.motionY * 0.5F * (double) (180F / (float) Math.PI));
        this.rotationPitch = f2;

        if (extinguishTimer > 0) {
            extinguishTimer--;
        }
        this.updateRocking();
        Entity controller = getControllingPassenger();
        if (controller instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) controller;
            if (this.ticksExisted % 50 == 0) {
                if (getEnchant(AMEnchantmentRegistry.STRADDLE_LAVAWAX) > 0) {
                    player.addPotionEffect(new PotionEffect(MobEffects.FIRE_RESISTANCE, 100, 0, true, false));
                }
            }
            if (player.isBurning() && extinguishTimer == 0) {
                player.extinguish();
            }
            this.rotationYaw = player.prevRotationYaw;
            Vec3d vector3d = new Vec3d(this.motionX, this.motionY, this.motionZ);
            if (vector3d.y > -0.5D) {
                this.fallDistance = 1.0F;
            }

            Vec3d vector3d1 = player.getLookVec();
            float f = player.rotationPitch * ((float) Math.PI / 180F);
            double d1 = Math.sqrt(vector3d1.x * vector3d1.x + vector3d1.z * vector3d1.z);
            double d3 = Math.sqrt(horizontalMag(vector3d));
            double d4 = Math.sqrt(vector3d1.x * vector3d1.x + vector3d1.y * vector3d1.y + vector3d1.z * vector3d1.z);
            float f1 = MathHelper.cos(f);
            f1 = (float) ((double) f1 * (double) f1 * Math.min(1.0D, d4 / 0.4D));
            float slow = player.moveForward < 0 ? 0 : player.moveForward * 0.115F;
            float threshold = 0.05F;
            if (this.prevRotationYaw - this.rotationYaw > threshold) {
                boardRot = boardRot + 2;
                slow *= 0;
            } else if (this.prevRotationYaw - this.rotationYaw < -threshold) {
                boardRot = boardRot - 2;
                slow *= 0;
            } else if (boardRot > 0) {
                boardRot = (Math.max(boardRot - 10, 0));
            } else if (boardRot < 0) {
                boardRot = (Math.min(boardRot + 10, 0));
            }
            boardRot = (MathHelper.clamp(boardRot, -25, 25));

            if (d1 > 1.0E-6D) {
                vector3d = new Vec3d(
                        vector3d.x + vector3d1.x * slow / d1,
                        vector3d.y,
                        vector3d.z + vector3d1.z * slow / d1);
            }

            if (d1 > 0.0D) {
                vector3d = new Vec3d(
                        vector3d.x + (vector3d1.x / d1 * d3 - vector3d.x) * 0.1D,
                        vector3d.y,
                        vector3d.z + (vector3d1.z / d1 * d3 - vector3d.z) * 0.1D);
            }

            this.motionX = vector3d.x * 0.99D;
            this.motionY = vector3d.y * 1.0D;
            this.motionZ = vector3d.z * 0.99D;

            if (player.isEntityInsideOpaqueBlock()) {
                player.dismountRidingEntity();
                this.attackEntityFrom(DamageSource.GENERIC, 100);
            }
        }
        this.updateMotion();
        this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
    }

    public double getPosYEye() {
        return this.posY + 0.3F;
    }

    @Override
    protected void removePassenger(Entity passenger) {
        super.removePassenger(passenger);
        if (!world.isRemote) {
            EntityStraddleboard copy = new EntityStraddleboard(world);
            NBTTagCompound tag = new NBTTagCompound();
            this.writeEntityToNBT(tag);
            copy.readEntityFromNBT(tag);
            copy.copyLocationAndAnglesFrom(passenger);
            world.spawnEntity(copy);
        }
        this.setDead();
    }

    @Nullable
    public Entity getControllingPassenger() {
        for (Entity passenger : this.getPassengers()) {
            if (passenger instanceof EntityPlayer) {
                return passenger;
            }
        }
        return null;
    }

    private void updateRocking() {
        if (this.world.isRemote) {
            int i = this.getRockingTicks();
            if (i > 0) {
                this.rockingIntensity += 1F;
            } else {
                this.rockingIntensity -= 0.1F;
            }

            this.rockingIntensity = MathHelper.clamp(this.rockingIntensity, 0.0F, 1.0F);
            this.prevRockingAngle = this.rockingAngle;
            this.rockingAngle = 10.0F * (float) Math.sin(0.5F * (float) this.world.getTotalWorldTime()) * this.rockingIntensity;
        } else {
            if (!this.rocking) {
                this.setRockingTicks(0);
            }

            int k = this.getRockingTicks();
            if (k > 0) {
                --k;
                this.setRockingTicks(k);
                int j = 60 - k - 1;
                if (j > 0 && k == 0) {
                    this.setRockingTicks(0);
                    if (this.downwards) {
                        this.motionY -= 0.7D;
                        this.removePassengers();
                    } else {
                        this.motionY = this.hasPlayerPassenger() ? 2.7D : 0.6D;
                    }
                }

                this.rocking = false;
            }
        }
    }

    public float getWaterLevelAbove() {
        AxisAlignedBB axisalignedbb = this.getEntityBoundingBox();
        int i = MathHelper.floor(axisalignedbb.minX);
        int j = MathHelper.ceil(axisalignedbb.maxX);
        int k = MathHelper.floor(axisalignedbb.maxY);
        int l = MathHelper.ceil(axisalignedbb.maxY - this.lastYd);
        int i1 = MathHelper.floor(axisalignedbb.minZ);
        int j1 = MathHelper.ceil(axisalignedbb.maxZ);
        BlockPos.MutableBlockPos blockpos$mutable = new BlockPos.MutableBlockPos();

        label39:
        for (int k1 = k; k1 < l; ++k1) {
            float f = 0.0F;

            for (int l1 = i; l1 < j; ++l1) {
                for (int i2 = i1; i2 < j1; ++i2) {
                    blockpos$mutable.setPos(l1, k1, i2);
                    IBlockState st = this.world.getBlockState(blockpos$mutable);
                    Material mat = st.getMaterial();
                    if (mat == Material.WATER || mat == Material.LAVA) {
                        f = Math.max(f, getFluidHeightPercent(st));
                    }

                    if (f >= 1.0F) {
                        continue label39;
                    }
                }
            }

            if (f < 1.0F) {
                return (float) blockpos$mutable.getY() + f;
            }
        }

        return (float) (l + 1);
    }

    private static float getFluidHeightPercent(IBlockState st) {
        if (st.getBlock() instanceof BlockLiquid) {
            return BlockLiquid.getLiquidHeightPercent(st.getValue(BlockLiquid.LEVEL));
        }
        return 1.0F;
    }

    @Override
    public boolean processInitialInteract(EntityPlayer player, EnumHand hand) {
        if (player.isSneaking()) {
            return false;
        }
        if (!this.world.isRemote) {
            return player.startRiding(this);
        }
        return true;
    }

    public float getDamageTaken() {
        return this.dataManager.get(DAMAGE_TAKEN);
    }

    public void setDamageTaken(float damageTaken) {
        this.dataManager.set(DAMAGE_TAKEN, damageTaken);
    }

    public int getTimeSinceHit() {
        return this.dataManager.get(TIME_SINCE_HIT);
    }

    public void setTimeSinceHit(int timeSinceHit) {
        this.dataManager.set(TIME_SINCE_HIT, timeSinceHit);
    }

    private int getRockingTicks() {
        return this.dataManager.get(ROCKING_TICKS);
    }

    private void setRockingTicks(int ticks) {
        this.dataManager.set(ROCKING_TICKS, ticks);
    }

    @SideOnly(Side.CLIENT)
    public float getRockingAngle(float partialTicks) {
        return this.prevRockingAngle + (this.rockingAngle - this.prevRockingAngle) * partialTicks;
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        this.setDefaultColor(compound.getBoolean("IsDefColor"));
        if (compound.hasKey("BoardStack", 10)) {
            ItemStack stack = new ItemStack(compound.getCompoundTag("BoardStack"));
            if (!stack.isEmpty()) {
                this.setItemStack(stack);
            }
        }
        this.setColor(compound.getInteger("Color"));
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        compound.setBoolean("IsDefColor", this.isDefaultColor());
        compound.setInteger("Color", this.getColor());
        if (!this.getItemStack().isEmpty()) {
            NBTTagCompound stackTag = new NBTTagCompound();
            this.getItemStack().writeToNBT(stackTag);
            compound.setTag("BoardStack", stackTag);
        }
    }

    @Override
    public void setJumpPower(int power) {
    }

    @Override
    public boolean canJump() {
        return this.world.getBlockState(this.getPosition().down()).getMaterial() == Material.LAVA;
    }

    @Override
    public void handleStartJump(int power) {
        jumpOutOfLava = true;
        float scaled = power * 0.01F + 0.1F * getEnchant(AMEnchantmentRegistry.STRADDLE_JUMP);
        this.motionY += scaled * 1.5F;
    }

    private int getEnchant(Enchantment enchantment) {
        return EnchantmentHelper.getEnchantmentLevel(enchantment, this.getItemBoard());
    }

    public boolean shouldSerpentFriend() {
        return getEnchant(AMEnchantmentRegistry.STRADDLE_SERPENTFRIEND) > 0;
    }

    @Override
    public void handleStopJump() {
    }

    public void setItemStack(ItemStack item) {
        this.dataManager.set(ITEMSTACK, item);
    }

    public ItemStack getItemStack() {
        return this.dataManager.get(ITEMSTACK);
    }

    public enum Status {
        IN_WATER,
        UNDER_WATER,
        UNDER_FLOWING_WATER,
        ON_LAND,
        IN_AIR
    }
}
