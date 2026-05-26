package com.github.alexthe666.alexsmobs.entity;

import com.github.alexthe666.alexsmobs.client.particle.AMParticleRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.MobEffects;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
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

public class EntitySandShot extends Entity {

    private static final DataParameter<Integer> VARIANT = EntityDataManager.createKey(EntitySandShot.class, DataSerializers.VARINT);

    private UUID ownerUniqueId;
    private int ownerEntityId;
    private boolean leftOwner;

    public EntitySandShot(World worldIn) {
        super(worldIn);
        this.setSize(0.5F, 0.5F);
    }

    public EntitySandShot(World worldIn, EntityGuster shooter) {
        this(worldIn);
        this.setShooter(shooter);
        this.setPosition(
                shooter.posX - (double) (shooter.width + 1.0F) * 0.35D * (double) MathHelper.sin(shooter.renderYawOffset * ((float) Math.PI / 180F)),
                shooter.posY + (double) shooter.getEyeHeight() + 0.2D,
                shooter.posZ + (double) (shooter.width + 1.0F) * 0.35D * (double) MathHelper.cos(shooter.renderYawOffset * ((float) Math.PI / 180F)));
    }

    public EntitySandShot(World worldIn, EntityLivingBase shooter, boolean right) {
        this(worldIn);
        this.setShooter(shooter);
        float rot = shooter.rotationYawHead + (right ? 60 : -60);
        this.setPosition(
                shooter.posX - (double) shooter.width * 0.5D * (double) MathHelper.sin(rot * ((float) Math.PI / 180F)),
                shooter.posY + (double) shooter.getEyeHeight() - 0.2D,
                shooter.posZ + (double) shooter.width * 0.5D * (double) MathHelper.cos(rot * ((float) Math.PI / 180F)));
    }

    @SideOnly(Side.CLIENT)
    public EntitySandShot(World worldIn, double x, double y, double z, double motX, double motY, double motZ) {
        this(worldIn);
        this.setPosition(x, y, z);
        this.motionX = motX;
        this.motionY = motY;
        this.motionZ = motZ;
    }

    @Override
    protected void entityInit() {
        this.dataManager.register(VARIANT, 0);
    }

    public int getVariant() {
        return this.dataManager.get(VARIANT);
    }

    public void setVariant(int variant) {
        this.dataManager.set(VARIANT, variant);
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

    @Override
    public void onEntityUpdate() {
        if (!this.leftOwner) {
            this.leftOwner = this.updateLeftOwner();
        }
        int variant = this.getVariant();
        int type = variant == 2 ? AMParticleRegistry.GUSTER_SAND_SHOT_SOUL : variant == 1 ? AMParticleRegistry.GUSTER_SAND_SHOT_RED : AMParticleRegistry.GUSTER_SAND_SHOT;
        for (int i = 0; i < 3 + this.rand.nextInt(6); ++i) {
            double d0 = 0.1D + 0.3D * (double) i;
            AMParticleRegistry.spawnParticle(this.world, type, this.posX + 0.25F * (this.rand.nextFloat() - 0.5F), this.posY + 0.25F * (this.rand.nextFloat() - 0.5F), this.posZ + 0.25F * (this.rand.nextFloat() - 0.5F), this.motionX * d0, this.motionY, this.motionZ * d0);
        }
        super.onEntityUpdate();

        Vec3d motion = new Vec3d(this.motionX, this.motionY, this.motionZ);
        Vec3d pos = new Vec3d(this.posX, this.posY, this.posZ);
        RayTraceResult trace = this.traceMotion(pos, motion);
        if (trace != null && trace.typeOfHit != RayTraceResult.Type.MISS && !ForgeEventFactory.onProjectileImpact(this, trace)) {
            this.onImpact(trace);
        }

        double nx = this.posX + motion.x;
        double ny = this.posY + motion.y;
        double nz = this.posZ + motion.z;

        this.updateRotationFromMotion(motion);

        if (this.world.collidesWithAnyBlock(this.getEntityBoundingBox())) {
            this.setDead();
        } else if (this.isInWater()) {
            this.setDead();
        } else {
            this.motionX = motion.x * 0.99D;
            this.motionY = motion.y * 0.99D - 0.03D;
            this.motionZ = motion.z * 0.99D;
            if (!this.hasNoGravity()) {
                this.motionY -= 0.03D;
            }
            this.setPosition(nx, ny, nz);
        }
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
        Entity shooter = this.getSandShooter();
        Entity hit = result.entityHit;
        if (shooter instanceof EntityLivingBase) {
            hit.attackEntityFrom(DamageSource.causeIndirectDamage(this, (EntityLivingBase) shooter), 2.5F);
        }
        if (shooter instanceof EntityPlayer && hit instanceof EntityLivingBase) {
            ((EntityLivingBase) hit).addPotionEffect(new PotionEffect(MobEffects.BLINDNESS, 100, 0, true, false));
        }
    }

    protected void onBlockHit(RayTraceResult result) {
        if (!this.world.isRemote) {
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
    public Entity getSandShooter() {
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
        Entity entity = this.getSandShooter();
        if (entity != null) {
            for (Entity other : this.world.getEntitiesWithinAABBExcludingEntity(this, this.getEntityBoundingBox().expand(this.motionX, this.motionY, this.motionZ).grow(1.0D))) {
                if (!(other instanceof EntityPlayerMP && ((EntityPlayerMP) other).isSpectator()) && other.canBeCollidedWith()) {
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

    public void shootFromEntity(Entity shooter, float pitch, float yaw, float pitchOffset, float velocity, float inaccuracy) {
        float f = -MathHelper.sin(yaw * ((float) Math.PI / 180F)) * MathHelper.cos(pitch * ((float) Math.PI / 180F));
        float f1 = -MathHelper.sin((pitch + pitchOffset) * ((float) Math.PI / 180F));
        float f2 = MathHelper.cos(yaw * ((float) Math.PI / 180F)) * MathHelper.cos(pitch * ((float) Math.PI / 180F));
        this.shoot(f, f1, f2, velocity, inaccuracy);
        this.motionX += shooter.motionX;
        this.motionY += shooter.onGround ? 0.0D : shooter.motionY;
        this.motionZ += shooter.motionZ;
    }

    protected void onImpact(RayTraceResult result) {
        if (result.typeOfHit == RayTraceResult.Type.ENTITY) {
            this.onEntityHit(result);
        } else if (result.typeOfHit == RayTraceResult.Type.BLOCK) {
            this.onBlockHit(result);
        }
    }

    @SideOnly(Side.CLIENT)
    public void setVelocity(double x, double y, double z) {
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
        if (!p.isEntityAlive()) {
            return false;
        }
        if (p.canBeCollidedWith()) {
            Entity entity = this.getSandShooter();
            return entity == null || this.leftOwner || !entity.isRidingSameEntity(p);
        }
        return false;
    }
}
