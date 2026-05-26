package com.github.alexthe666.alexsmobs.entity;

import com.github.alexthe666.alexsmobs.client.particle.AMParticleRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.init.MobEffects;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
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

public class EntityFart extends Entity {

    private UUID ownerUniqueId;
    private int ownerEntityId;
    private boolean leftOwner;

    public EntityFart(World worldIn) {
        super(worldIn);
        this.setSize(0.7F, 0.3F);
    }

    public EntityFart(World worldIn, EntityLivingBase shooter, boolean right) {
        this(worldIn);
        this.setShooter(shooter);
        float rot = shooter.rotationYawHead + (right ? 60 : -60);
        this.setPosition(
                shooter.posX - (double) shooter.width * 0.5D * (double) MathHelper.sin(rot * ((float) Math.PI / 180F)),
                shooter.getEyeHeight() - (double) 0.2F + shooter.posY,
                shooter.posZ + (double) shooter.width * 0.5D * (double) MathHelper.cos(rot * ((float) Math.PI / 180F)));
    }

    @Override
    protected void entityInit() {
    }

    @Override
    public void onEntityUpdate() {
        if (!this.leftOwner) {
            this.leftOwner = this.updateLeftOwner();
        }
        super.onEntityUpdate();

        Vec3d motion = new Vec3d(this.motionX, this.motionY, this.motionZ);
        RayTraceResult trace = this.traceMotion(new Vec3d(this.posX, this.posY, this.posZ), motion);
        if (trace != null && trace.typeOfHit != RayTraceResult.Type.MISS && !ForgeEventFactory.onProjectileImpact(this, trace)) {
            this.onImpact(trace);
        }

        this.updateRotationFromMotion(motion);

        double nx = this.posX + motion.x;
        double ny = this.posY + motion.y;
        double nz = this.posZ + motion.z;

        this.motionX = motion.x * 0.95D;
        this.motionY = motion.y * 0.95D;
        this.motionZ = motion.z * 0.95D;
        this.setPosition(nx, ny, nz);

        if (this.ticksExisted > 30) {
            this.setDead();
        }
    }

    protected void onImpact(RayTraceResult result) {
        if (result.typeOfHit == RayTraceResult.Type.ENTITY) {
            this.onEntityHit(result);
        } else if (result.typeOfHit == RayTraceResult.Type.BLOCK) {
            this.onBlockHit(result);
        }
    }

    protected void onEntityHit(RayTraceResult result) {
        if (result.entityHit instanceof EntityLivingBase) {
            EntityLivingBase living = (EntityLivingBase) result.entityHit;
            living.addPotionEffect(new PotionEffect(MobEffects.NAUSEA, 300));
            if (this.world.isRemote) {
                for (int i = 0; i < 10 + this.rand.nextInt(6); i++) {
                    AMParticleRegistry.spawnParticle(this.world, AMParticleRegistry.SMELLY,
                            living.posX + (living.getRNG().nextDouble() - 0.5D) * living.width,
                            living.posY + living.getRNG().nextDouble() * living.height,
                            living.posZ + (living.getRNG().nextDouble() - 0.5D) * living.width,
                            0, 0, 0);
                }
            } else {
                for (EntityMob nearby : this.world.getEntitiesWithinAABB(EntityMob.class, living.getEntityBoundingBox().grow(15))) {
                    if (nearby == living || nearby.getEntityId() == living.getEntityId()
                            || nearby.isOnSameTeam(living) || living.isOnSameTeam(nearby)
                            || nearby instanceof IHurtableMultipart) {
                        continue;
                    }
                    nearby.setRevengeTarget(living);
                    if (nearby instanceof EntityCreature) {
                        ((EntityCreature) nearby).setAttackTarget(living);
                    }
                }
            }
        }
    }

    protected void onBlockHit(RayTraceResult result) {
        if (!this.world.isRemote) {
            this.setDead();
        }
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

    protected static float lerpRotation(float from, float to) {
        while (to - from < -180.0F) {
            from -= 360.0F;
        }
        while (to - from >= 180.0F) {
            from += 360.0F;
        }
        return from + (to - from) * 0.2F;
    }

    protected void updateRotationFromMotion(Vec3d motion) {
        float len = MathHelper.sqrt(motion.x * motion.x + motion.z * motion.z);
        this.rotationPitch = lerpRotation(this.prevRotationPitch, (float) (MathHelper.atan2(motion.y, len) * (180D / Math.PI)));
        this.rotationYaw = lerpRotation(this.prevRotationYaw, (float) (MathHelper.atan2(motion.x, motion.z) * (180D / Math.PI)));
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
                if (other.canBeCollidedWith()) {
                    if (other.getLowestRidingEntity() == entity.getLowestRidingEntity()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private boolean canHitEntity(Entity hit) {
        if (!hit.isEntityAlive() || !hit.canBeCollidedWith()) {
            return false;
        }
        Entity entity = this.getShooter();
        return entity == null || this.leftOwner || !entity.isRidingSameEntity(hit);
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
}
