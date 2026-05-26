package com.github.alexthe666.alexsmobs.entity;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

/**
 * 1.12.2 port of the cachalot echolocation projectile (1.16 used {@code EntityType}, {@link Vector3d}, etc.).
 */
public class EntityCachalotEcho extends Entity {
    private static final int MAX_AGE = 100;

    private static final DataParameter<Boolean> RETURNING = EntityDataManager.createKey(EntityCachalotEcho.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> FASTER_ANIM = EntityDataManager.createKey(EntityCachalotEcho.class, DataSerializers.BOOLEAN);

    private UUID ownerUniqueId;
    private int ownerEntityId;
    private boolean leftOwner;
    private boolean playerLaunched = false;

    public EntityCachalotEcho(World worldIn) {
        super(worldIn);
        this.setSize(0.35F, 0.35F);
    }

    public EntityCachalotEcho(World worldIn, EntityCachalotWhale shooter) {
        this(worldIn);
        this.setShooter(shooter);
    }

    public EntityCachalotEcho(World worldIn, EntityLivingBase shooter, boolean right) {
        this(worldIn);
        this.setShooter(shooter);
        float rot = shooter.rotationYawHead + (right ? 90 : -90);
        this.playerLaunched = true;
        this.setFasterAnimation(true);
        this.setPosition(
                shooter.posX - (double) shooter.width * 0.5D * (double) MathHelper.sin(rot * ((float) Math.PI / 180F)),
                shooter.posY + 1.0D,
                shooter.posZ + (double) shooter.width * 0.5D * (double) MathHelper.cos(rot * ((float) Math.PI / 180F)));
    }

    @SideOnly(Side.CLIENT)
    public EntityCachalotEcho(World worldIn, double x, double y, double z, double motX, double motY, double motZ) {
        this(worldIn);
        this.setPosition(x, y, z);
        this.motionX = motX;
        this.motionY = motY;
        this.motionZ = motZ;
    }

    protected static float lerpRotation(float from, float to) {
        while (to - from < -180.0F) {
            from -= 360.0F;
        }
        while (to - from >= 180.0F) {
            from += 360.0F;
        }
        return from + (to - from) * 0.2F;
    }

    public boolean isReturning() {
        return this.dataManager.get(RETURNING).booleanValue();
    }

    public void setReturning(boolean returning) {
        this.dataManager.set(RETURNING, returning);
    }

    public boolean isFasterAnimation() {
        return this.dataManager.get(FASTER_ANIM).booleanValue();
    }

    public void setFasterAnimation(boolean anim) {
        this.dataManager.set(FASTER_ANIM, anim);
    }

    @Override
    protected void entityInit() {
        this.dataManager.register(RETURNING, false);
        this.dataManager.register(FASTER_ANIM, false);
    }

    @Override
    public void onEntityUpdate() {
        double yMot = MathHelper.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
        this.rotationPitch = (float) (MathHelper.atan2(this.motionY, yMot) * (180D / Math.PI));
        if (!this.leftOwner) {
            this.leftOwner = this.updateLeftOwner();
        }
        super.onEntityUpdate();

        Vec3d motion = new Vec3d(this.motionX, this.motionY, this.motionZ);
        Vec3d pos = new Vec3d(this.posX, this.posY, this.posZ);
        RayTraceResult trace = this.traceMotion(pos, motion);
        if (trace != null && trace.typeOfHit != RayTraceResult.Type.MISS && !ForgeEventFactory.onProjectileImpact(this, trace)) {
            this.onImpact(trace);
        }

        Entity shooter = this.getShooter();
        if (this.isReturning() && shooter instanceof EntityCachalotWhale) {
            EntityCachalotWhale whale = (EntityCachalotWhale) shooter;
            double d = whale.headPart != null ? whale.headPart.getDistanceSq(this) : 9999D;
            if (d < (double) whale.headPart.width * (double) whale.headPart.width) {
                this.setDead();
                whale.recieveEcho();
            }
        }
        if (!this.playerLaunched && !this.world.isRemote && !this.isInWater()) {
            this.setDead();
        }
        if (this.ticksExisted > MAX_AGE) {
            this.setDead();
        }

        double nx = this.posX + motion.x;
        double ny = this.posY + motion.y;
        double nz = this.posZ + motion.z;

        this.updateRotationFromMotion(motion);
        if (this.playerLaunched) {
            this.noClip = true;
        }
        this.motionX *= 0.99D;
        this.motionY *= 0.99D;
        this.motionZ *= 0.99D;
        this.setNoGravity(true);
        this.setPosition(nx, ny, nz);
        this.rotationYaw = (float) (MathHelper.atan2(motion.x, motion.z) * (180D / Math.PI)) - 90.0F;
    }

    private void updateRotationFromMotion(Vec3d motion) {
        float len = MathHelper.sqrt(motion.x * motion.x + motion.z * motion.z);
        this.rotationPitch = lerpRotation(this.prevRotationPitch, (float) (MathHelper.atan2(motion.y, len) * (180D / Math.PI)));
        this.rotationYaw = lerpRotation(this.prevRotationYaw, (float) (MathHelper.atan2(motion.x, motion.z) * (180D / Math.PI)));
    }

    @Nullable
    private RayTraceResult traceMotion(Vec3d start, Vec3d motion) {
        Vec3d end = start.add(motion);
        RayTraceResult block = this.world.rayTraceBlocks(start, end, false, true, false);

        RayTraceResult entityHit = null;
        double bestDist = motion.lengthSquared();
        AxisAlignedBB search = this.getEntityBoundingBox().expand(motion.x, motion.y, motion.z).grow(1.0D);
        List<Entity> list = this.world.getEntitiesWithinAABBExcludingEntity(this, search);
        for (Entity e : list) {
            if (!this.canHitEntity(e)) {
                continue;
            }
            AxisAlignedBB bb = e.getEntityBoundingBox().grow(0.3D);
            RayTraceResult trace = bb.calculateIntercept(start, end);
            if (trace != null) {
                double dist = start.squareDistanceTo(trace.hitVec);
                if (dist < bestDist) {
                    entityHit = new RayTraceResult(e, trace.hitVec);
                    bestDist = dist;
                }
            }
        }

        if (entityHit != null) {
            if (block != null && start.squareDistanceTo(block.hitVec) < start.squareDistanceTo(entityHit.hitVec)) {
                return block.typeOfHit == RayTraceResult.Type.MISS ? null : block;
            }
            return entityHit;
        }
        return block != null && block.typeOfHit != RayTraceResult.Type.MISS ? block : null;
    }

    protected void onEntityHit(RayTraceResult result) {
        if (result.entityHit == null) {
            return;
        }
        Entity entity = this.getShooter();
        Entity hit = result.entityHit;
        if (this.isReturning()) {
            EntityCachalotWhale whale = null;
            if (entity instanceof EntityCachalotWhale) {
                whale = (EntityCachalotWhale) entity;
                if (hit instanceof EntityCachalotWhale || hit instanceof EntityCachalotPart) {
                    whale.recieveEcho();
                    this.setDead();
                }
            }
        } else if (hit != entity && !hit.isEntityEqual(entity)) {
            this.setReturning(true);
            if (entity instanceof EntityCachalotWhale) {
                Vec3d vec = ((EntityCachalotWhale) entity).getReturnEchoVector();
                double d0 = vec.x - this.posX;
                double d1 = vec.y - this.posY;
                double d2 = vec.z - this.posZ;
                this.motionX = 0;
                this.motionY = 0;
                this.motionZ = 0;
                EntityCachalotEcho echo = new EntityCachalotEcho(this.world, (EntityCachalotWhale) entity);
                echo.copyLocationAndAnglesFrom(this);
                this.setDead();
                echo.setReturning(true);
                echo.shoot(d0, d1, d2, 1.0F, 0.0F);
                if (!this.world.isRemote) {
                    this.world.spawnEntity(echo);
                }
            }
        }
    }

    private void onBlockHit(RayTraceResult result) {
        IBlockState state = this.world.getBlockState(result.getBlockPos());
        if (state != null && !this.world.isRemote && !this.playerLaunched) {
            this.setDead();
        }
    }

    public void setShooter(@Nullable Entity entityIn) {
        if (entityIn != null) {
            this.ownerUniqueId = entityIn.getUniqueID();
            this.ownerEntityId = entityIn.getEntityId();
        }
    }

    @Nullable
    public Entity getShooter() {
        if (this.ownerUniqueId != null && this.world instanceof WorldServer) {
            Entity e = ((WorldServer) this.world).getEntityFromUuid(this.ownerUniqueId);
            if (e != null) {
                return e;
            }
        }
        return this.ownerEntityId != 0 ? this.world.getEntityByID(this.ownerEntityId) : null;
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound compound) {
        if (this.ownerUniqueId != null) {
            compound.setUniqueId("Owner", this.ownerUniqueId);
        }
        if (this.leftOwner) {
            compound.setBoolean("LeftOwner", true);
        }
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound compound) {
        if (compound.hasUniqueId("Owner")) {
            this.ownerUniqueId = compound.getUniqueId("Owner");
        }
        this.leftOwner = compound.getBoolean("LeftOwner");
    }

    private boolean updateLeftOwner() {
        Entity entity = this.getShooter();
        if (entity != null) {
            for (Entity other : this.world.getEntitiesWithinAABBExcludingEntity(this, this.getEntityBoundingBox().expand(this.motionX, this.motionY, this.motionZ).grow(1.0D))) {
                if (other.isEntityAlive() && other.canBeCollidedWith()) {
                    if (other.getLowestRidingEntity() == entity.getLowestRidingEntity()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public void shoot(double x, double y, double z, float velocity, float inaccuracy) {
        Vec3d dir = new Vec3d(x, y, z).normalize();
        dir = dir.addVector(
                this.rand.nextGaussian() * 0.0075D * inaccuracy,
                this.rand.nextGaussian() * 0.0075D * inaccuracy,
                this.rand.nextGaussian() * 0.0075D * inaccuracy
        ).scale(velocity);
        this.motionX = dir.x;
        this.motionY = dir.y;
        this.motionZ = dir.z;
        float horiz = MathHelper.sqrt(dir.x * dir.x + dir.z * dir.z);
        this.rotationYaw = (float) (MathHelper.atan2(dir.x, dir.z) * (180D / Math.PI));
        this.rotationPitch = (float) (MathHelper.atan2(dir.y, horiz) * (180D / Math.PI));
        this.prevRotationYaw = this.rotationYaw;
        this.prevRotationPitch = this.rotationPitch;
    }

    protected void onImpact(RayTraceResult result) {
        if (this.playerLaunched) {
            return;
        }
        if (result.typeOfHit == RayTraceResult.Type.ENTITY) {
            this.onEntityHit(result);
        } else if (result.typeOfHit == RayTraceResult.Type.BLOCK) {
            this.onBlockHit(result);
        }
    }

    @SideOnly(Side.CLIENT)
    public void setVelocityClient(double x, double y, double z) {
        this.motionX = x;
        this.motionY = y;
        this.motionZ = z;
        if (this.prevRotationPitch == 0.0F && this.prevRotationYaw == 0.0F) {
            float f = MathHelper.sqrt(x * x + z * z);
            this.rotationPitch = (float) (MathHelper.atan2(y, f) * (180D / Math.PI));
            this.rotationYaw = (float) (MathHelper.atan2(x, z) * (180D / Math.PI));
            this.prevRotationPitch = this.rotationPitch;
            this.prevRotationYaw = this.rotationYaw;
            this.setLocationAndAngles(this.posX, this.posY, this.posZ, this.rotationYaw, this.rotationPitch);
        }
    }

    private boolean canHitEntity(Entity p) {
        if (this.playerLaunched) {
            return false;
        }
        if (this.isReturning()) {
            return p instanceof EntityCachalotPart || p instanceof EntityCachalotWhale;
        }
        if (p instanceof EntityCachalotPart) {
            return false;
        }
        if (p.isEntityAlive() && p.canBeCollidedWith()) {
            Entity entity = this.getShooter();
            return entity == null || this.leftOwner || !entity.isRidingSameEntity(p);
        }
        return false;
    }
}
