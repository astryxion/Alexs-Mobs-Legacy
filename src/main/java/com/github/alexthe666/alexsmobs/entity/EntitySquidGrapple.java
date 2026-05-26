package com.github.alexthe666.alexsmobs.entity;

import com.google.common.base.Optional;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import javax.annotation.Nullable;
import java.util.UUID;

public class EntitySquidGrapple extends Entity {

    private static final DataParameter<Optional<UUID>> OWNER_UUID = EntityDataManager.createKey(EntitySquidGrapple.class, DataSerializers.OPTIONAL_UNIQUE_ID);
    private static final DataParameter<Byte> ATTACHED_FACE = EntityDataManager.createKey(EntitySquidGrapple.class, DataSerializers.BYTE);
    private static final DataParameter<Boolean> WITHDRAWING = EntityDataManager.createKey(EntitySquidGrapple.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> ATTACHED_X = EntityDataManager.createKey(EntitySquidGrapple.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> ATTACHED_Y = EntityDataManager.createKey(EntitySquidGrapple.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> ATTACHED_Z = EntityDataManager.createKey(EntitySquidGrapple.class, DataSerializers.VARINT);
    private int ticksWithdrawing = 0;

    public EntitySquidGrapple(World worldIn) {
        super(worldIn);
        this.setSize(0.5F, 0.5F);
    }

    public EntitySquidGrapple(World worldIn, EntityLivingBase player, boolean rightHand) {
        this(worldIn);
        this.setOwnerId(player.getUniqueID());
        float rot = player.rotationYawHead + (rightHand ? 60 : -60);
        this.setPosition(
                player.posX - (double) player.width * 0.5D * (double) MathHelper.sin(rot * ((float) Math.PI / 180F)),
                player.getEyeHeight() - (double) 0.2F + player.posY,
                player.posZ + (double) player.width * 0.5D * (double) MathHelper.cos(rot * ((float) Math.PI / 180F)));
    }

    protected static float lerpRotation(float f2, float f3) {
        while (f3 - f2 < -180.0F) {
            f2 -= 360.0F;
        }

        while (f3 - f2 >= 180.0F) {
            f2 += 360.0F;
        }

        return f2 + (f3 - f2) * 0.2F;
    }

    public void shoot(double x, double y, double z, float velocity, float inaccuracy) {
        Vec3d vector3d = (new Vec3d(x, y, z)).normalize().add(new Vec3d(this.rand.nextGaussian() * (double) 0.0075F * (double) inaccuracy, this.rand.nextGaussian() * (double) 0.0075F * (double) inaccuracy, this.rand.nextGaussian() * (double) 0.0075F * (double) inaccuracy)).scale(velocity);
        this.motionX = vector3d.x;
        this.motionY = vector3d.y;
        this.motionZ = vector3d.z;
        float f = MathHelper.sqrt(vector3d.x * vector3d.x + vector3d.z * vector3d.z);
        this.rotationYaw = MathHelper.wrapDegrees((float) (MathHelper.atan2(vector3d.x, vector3d.z) * (double) (180F / (float) Math.PI)) + 180);
        this.rotationPitch = (float) (MathHelper.atan2(vector3d.y, f) * (double) (180F / (float) Math.PI));
        this.prevRotationYaw = this.rotationYaw;
        this.prevRotationPitch = this.rotationPitch;
    }

    public EnumFacing getAttachmentFacing() {
        return EnumFacing.values()[this.dataManager.get(ATTACHED_FACE) & 7];
    }

    public void setAttachmentFacing(EnumFacing direction) {
        this.dataManager.set(ATTACHED_FACE, (byte) direction.getIndex());
    }

    @Nullable
    public UUID getOwnerId() {
        return this.dataManager.get(OWNER_UUID).orNull();
    }

    public void setOwnerId(@Nullable UUID uniqueId) {
        this.dataManager.set(OWNER_UUID, Optional.fromNullable(uniqueId));
    }

    @Nullable
    public BlockPos getStuckToPos() {
        int x = this.dataManager.get(ATTACHED_X);
        if (x == Integer.MIN_VALUE) {
            return null;
        }
        return new BlockPos(x, this.dataManager.get(ATTACHED_Y), this.dataManager.get(ATTACHED_Z));
    }

    public void setStuckToPos(@Nullable BlockPos harvestedPos) {
        if (harvestedPos == null) {
            this.dataManager.set(ATTACHED_X, Integer.MIN_VALUE);
        } else {
            this.dataManager.set(ATTACHED_X, harvestedPos.getX());
            this.dataManager.set(ATTACHED_Y, harvestedPos.getY());
            this.dataManager.set(ATTACHED_Z, harvestedPos.getZ());
        }
    }
    @Override
    protected void entityInit() {
        this.dataManager.register(OWNER_UUID, Optional.absent());
        this.dataManager.register(ATTACHED_FACE, (byte) EnumFacing.DOWN.getIndex());
        this.dataManager.register(ATTACHED_X, Integer.MIN_VALUE);
        this.dataManager.register(ATTACHED_Y, 0);
        this.dataManager.register(ATTACHED_Z, 0);
        this.dataManager.register(WITHDRAWING, false);
    }

    @Nullable
    public Entity getOwner() {
        UUID id = getOwnerId();
        if (id == null) {
            return null;
        }
        if (!this.world.isRemote && this.world instanceof WorldServer) {
            return ((WorldServer) this.world).getEntityFromUuid(id);
        }
        EntityPlayer player = this.world.getPlayerEntityByUUID(id);
        if (player != null) {
            return player;
        }
        for (Entity entity : this.world.loadedEntityList) {
            if (id.equals(entity.getUniqueID())) {
                return entity;
            }
        }
        return null;
    }

    public boolean isWithdrawing() {
        return this.dataManager.get(WITHDRAWING);
    }

    public void setWithdrawing(boolean withdrawing) {
        this.dataManager.set(WITHDRAWING, withdrawing);
    }

    @Override
    public void onUpdate() {
        this.prevRotationPitch = this.rotationPitch;
        this.prevRotationYaw = this.rotationYaw;
        Entity entity = this.getOwner();
        if (!this.world.isRemote) {
            if (entity == null || !entity.isEntityAlive()) {
                this.setDead();
            } else if (entity.isSneaking()) {
                this.setWithdrawing(true);
            }
        }
        if (this.isWithdrawing() && entity != null) {
            super.onUpdate();
            ticksWithdrawing++;
            this.setStuckToPos(null);
            Vec3d withDrawTo = entity.getPositionEyes(1.0F).addVector(0, -0.2F, 0);
            if (withDrawTo.distanceTo(this.getPositionVector()) > 1.2D && ticksWithdrawing < 200) {
                Vec3d move = new Vec3d(withDrawTo.x - this.posX, withDrawTo.y - this.posY, withDrawTo.z - this.posZ);
                Vec3d vector3d = move.normalize().scale(1.2D);
                this.motionX = vector3d.x * 0.99D;
                this.motionY = vector3d.y * 0.99D;
                this.motionZ = vector3d.z * 0.99D;
                double d0 = this.posX + vector3d.x;
                double d1 = this.posY + vector3d.y;
                double d2 = this.posZ + vector3d.z;
                float f = MathHelper.sqrt(move.x * move.x + move.z * move.z);
                if (!this.world.isRemote) {
                    this.rotationYaw = MathHelper.wrapDegrees((float) (-MathHelper.atan2(move.x, move.z) * (double) (180F / (float) Math.PI)) - 180);
                    this.rotationPitch = (float) (MathHelper.atan2(move.y, f) * (double) (180F / (float) Math.PI));
                    this.prevRotationYaw = this.rotationYaw;
                    this.prevRotationPitch = this.rotationPitch;
                }
                this.setPosition(d0, d1, d2);
            } else {
                this.setDead();
            }
        } else if (this.world.isRemote || this.world.isBlockLoaded(this.getPosition(), false)) {
            if (this.getStuckToPos() == null) {
                super.onUpdate();
                Vec3d vector3d = new Vec3d(this.motionX, this.motionY, this.motionZ);
                RayTraceResult raytraceresult = this.rayTraceMotion(vector3d);
                if (raytraceresult != null && raytraceresult.typeOfHit != RayTraceResult.Type.MISS) {
                    this.onImpact(raytraceresult);
                }
                this.doBlockCollisions();
                double d0 = this.posX + this.motionX;
                double d1 = this.posY + this.motionY;
                double d2 = this.posZ + this.motionZ;
                this.updateRotation();
                this.motionX *= 0.99D;
                this.motionY *= 0.99D;
                this.motionZ *= 0.99D;
                if (this.isInsideOfBlock() && !this.isInWater()) {
                    this.motionX = 0;
                    this.motionY = 0;
                    this.motionZ = 0;
                } else {
                    this.setPosition(d0, d1, d2);
                }
                this.motionY -= 0.1F;
            } else {
                IBlockState state = this.world.getBlockState(this.getStuckToPos());
                Vec3d vec3 = new Vec3d(this.getStuckToPos().getX() + 0.5F, this.getStuckToPos().getY() + 0.5F, this.getStuckToPos().getZ() + 0.5F);
                Vec3d offset = new Vec3d(
                        this.getAttachmentFacing().getFrontOffsetX() * 0.55F,
                        this.getAttachmentFacing().getFrontOffsetY() * 0.55F,
                        this.getAttachmentFacing().getFrontOffsetZ() * 0.55F);
                float targetX = this.rotationPitch;
                float targetY = this.rotationYaw;
                switch (this.getAttachmentFacing()) {
                    case UP:
                        targetX = 0;
                        break;
                    case DOWN:
                        targetX = 180;
                        break;
                    case NORTH:
                        targetX = -90;
                        targetY = 0;
                        break;
                    case EAST:
                        targetX = -90;
                        targetY = 90;
                        break;
                    case SOUTH:
                        targetX = -90;
                        targetY = 180;
                        break;
                    case WEST:
                        targetX = -90;
                        targetY = -90;
                        break;
                    default:
                        break;
                }
                this.rotationPitch = targetX;
                this.rotationYaw = targetY;
                this.setPosition(vec3.x + offset.x, vec3.y + offset.y, vec3.z + offset.z);
                if (entity != null && entity.getDistance(this) > 2) {
                    float entitySwing = 1.0F;
                    if (entity instanceof EntityLivingBase) {
                        EntityLivingBase living = (EntityLivingBase) entity;
                        float detract = living.moveStrafing * living.moveStrafing + living.moveVertical * living.moveVertical + living.moveForward * living.moveForward;
                        entitySwing -= Math.min(1.0F, MathHelper.sqrt(detract) * 0.333F);
                    }
                    Vec3d move = new Vec3d(this.posX - entity.posX, this.posY - (double) entity.getEyeHeight() / 2.0D - entity.posY, this.posZ - entity.posZ);
                    move = move.normalize().scale(0.2D * entitySwing);
                    entity.motionX += move.x;
                    entity.motionY += move.y;
                    entity.motionZ += move.z;
                    if (!entity.onGround) {
                        entity.fallDistance = 0.0F;
                    }
                }
                if (!canStickToBlock(state)) {
                    this.setWithdrawing(true);
                }
            }
        } else {
            this.setDead();
        }
    }

    private static boolean canStickToBlock(IBlockState state) {
        return state.getMaterial().blocksMovement()
                && !state.getMaterial().isReplaceable()
                && state.isFullCube();
    }

    private RayTraceResult rayTraceMotion(Vec3d motion) {
        Vec3d start = new Vec3d(this.posX, this.posY, this.posZ);
        Vec3d end = start.add(motion);
        Vec3d current = start;
        for (int attempt = 0; attempt < 16; attempt++) {
            RayTraceResult result = this.world.rayTraceBlocks(current, end, false, true, false);
            if (result == null || result.typeOfHit == RayTraceResult.Type.MISS) {
                return null;
            }
            if (canStickToBlock(this.world.getBlockState(result.getBlockPos()))) {
                return result;
            }
            Vec3d hitVec = result.hitVec;
            Vec3d step = end.subtract(hitVec);
            double len = step.lengthVector();
            if (len < 1.0E-4D) {
                return null;
            }
            current = hitVec.add(step.normalize().scale(0.05D));
            if (current.squareDistanceTo(end) < 1.0E-4D) {
                return null;
            }
        }
        return null;
    }

    private boolean isInsideOfBlock() {
        if (this.world.isRemote) {
            return false;
        }
        AxisAlignedBB bb = this.getEntityBoundingBox();
        BlockPos from = new BlockPos(bb.minX + 0.001D, bb.minY + 0.001D, bb.minZ + 0.001D);
        BlockPos to = new BlockPos(bb.maxX - 0.001D, bb.maxY - 0.001D, bb.maxZ - 0.001D);
        for (BlockPos pos : BlockPos.getAllInBox(from, to)) {
            IBlockState state = this.world.getBlockState(pos);
            if (!state.getMaterial().isReplaceable()) {
                return true;
            }
        }
        return false;
    }

    protected float rotlerp(float in, float target, float maxShift) {
        float f = MathHelper.wrapDegrees(target - in);
        if (f > maxShift) {
            f = maxShift;
        }

        if (f < -maxShift) {
            f = -maxShift;
        }

        float f1 = in + f;
        if (f1 < 0.0F) {
            f1 += 360.0F;
        } else if (f1 > 360.0F) {
            f1 -= 360.0F;
        }

        return f1;
    }

    private void updateRotation() {
    }

    protected void onImpact(RayTraceResult result) {
        if (!this.world.isRemote && result.typeOfHit == RayTraceResult.Type.BLOCK && this.getStuckToPos() == null) {
            IBlockState state = this.world.getBlockState(result.getBlockPos());
            if (!canStickToBlock(state)) {
                return;
            }
            this.motionX = 0;
            this.motionY = 0;
            this.motionZ = 0;
            this.setStuckToPos(result.getBlockPos());
            this.setAttachmentFacing(result.sideHit);
            Vec3d anchor = new Vec3d(result.getBlockPos().getX() + 0.5D, result.getBlockPos().getY() + 0.5D, result.getBlockPos().getZ() + 0.5D);
            Vec3d offset = new Vec3d(
                    result.sideHit.getFrontOffsetX() * 0.55D,
                    result.sideHit.getFrontOffsetY() * 0.55D,
                    result.sideHit.getFrontOffsetZ() * 0.55D);
            this.setPosition(anchor.x + offset.x, anchor.y + offset.y, anchor.z + offset.z);
        }
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound compound) {
        if (compound.hasUniqueId("OwnerUUID")) {
            this.setOwnerId(compound.getUniqueId("OwnerUUID"));
        }
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound compound) {
        if (this.getOwnerId() != null) {
            compound.setUniqueId("OwnerUUID", this.getOwnerId());
        }
    }
}
