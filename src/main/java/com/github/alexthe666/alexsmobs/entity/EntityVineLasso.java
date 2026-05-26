package com.github.alexthe666.alexsmobs.entity;

import com.github.alexthe666.alexsmobs.entity.util.VineLassoUtil;
import com.github.alexthe666.alexsmobs.item.AMItemRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
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

public class EntityVineLasso extends Entity {

    private UUID ownerUUID;
    private int ownerNetworkId;
    private boolean leftOwner;

    public EntityVineLasso(World worldIn) {
        super(worldIn);
        this.setSize(0.85F, 0.2F);
    }

    public EntityVineLasso(World worldIn, EntityLivingBase entity) {
        this(worldIn);
        this.setShooter(entity);
        this.setPosition(entity.posX, entity.posY + (double) entity.getEyeHeight() + 0.15D, entity.posZ);
    }

    @SideOnly(Side.CLIENT)
    public EntityVineLasso(World worldIn, double x, double y, double z, double motX, double motY, double motZ) {
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

    @Override
    public void onEntityUpdate() {
        if (!this.leftOwner) {
            this.leftOwner = this.updateLeftOwner();
        }
        super.onEntityUpdate();
        if (this.isDead) {
            return;
        }
        Vec3d vector3d = new Vec3d(this.motionX, this.motionY, this.motionZ);
        RayTraceResult raytraceresult = this.traceMotion(new Vec3d(this.posX, this.posY, this.posZ), vector3d);
        if (raytraceresult != null && raytraceresult.typeOfHit != RayTraceResult.Type.MISS && !ForgeEventFactory.onProjectileImpact(this, raytraceresult)) {
            this.onImpact(raytraceresult);
        }
        if (this.isDead) {
            return;
        }
        if (!this.world.isRemote) {
            this.tryEntityOverlapHit();
        }
        if (this.isDead) {
            return;
        }

        this.updateRotation();
        if (!this.world.isRemote) {
            if (this.getShooter() != null && this.getDistance(this.getShooter()) > 15) {
                this.removeAndAddToInventory();
                return;
            }
            if (this.leftOwner && this.world.collidesWithAnyBlock(this.getEntityBoundingBox()) && !this.isInWater() && !this.isInLava()) {
                this.removeAndAddToInventory();
                return;
            }
        }

        double d0 = this.posX + vector3d.x;
        double d1 = this.posY + vector3d.y;
        double d2 = this.posZ + vector3d.z;
        this.motionX = vector3d.x * 0.99D;
        this.motionY = vector3d.y * 0.99D;
        this.motionZ = vector3d.z * 0.99D;
        if (!this.hasNoGravity()) {
            this.motionY -= 0.02D;
        }

        this.setPosition(d0, d1, d2);
    }

    private void tryEntityOverlapHit() {
        for (Entity entity : this.world.getEntitiesWithinAABBExcludingEntity(this, this.getEntityBoundingBox().grow(0.35D))) {
            if (this.canHitEntity(entity) && entity instanceof EntityLivingBase && entity != this.getShooter() && !VineLassoUtil.hasLassoData((EntityLivingBase) entity)) {
                this.onEntityHit(new RayTraceResult(entity, entity.getPositionVector().addVector(0.0D, entity.height * 0.5D, 0.0D)));
                return;
            }
        }
    }

    @Nullable
    private RayTraceResult traceMotion(Vec3d start, Vec3d motion) {
        Vec3d end = start.add(motion);
        RayTraceResult block = this.world.rayTraceBlocks(start, end, false, true, false);

        RayTraceResult entityHit = null;
        double bestDist = motion.lengthSquared();
        for (Entity e : this.world.getEntitiesWithinAABBExcludingEntity(this, this.getEntityBoundingBox().expand(motion.x, motion.y, motion.z).grow(1.0D))) {
            if (!this.canHitEntity(e)) {
                continue;
            }
            RayTraceResult trace = e.getEntityBoundingBox().grow(0.5D).calculateIntercept(start, end);
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
        if (this.world.isRemote || this.isDead || result.entityHit == null) {
            return;
        }
        Entity shooter = this.getShooter();
        Entity hit = result.entityHit;
        if (shooter instanceof EntityLivingBase && hit instanceof EntityLivingBase && hit != shooter && !VineLassoUtil.hasLassoData((EntityLivingBase) hit)) {
            VineLassoUtil.lassoTo((EntityLivingBase) shooter, (EntityLivingBase) hit);
            this.setDead();
        }
    }

    private void removeAndAddToInventory() {
        Entity entity = this.getShooter();
        ItemStack item = new ItemStack(AMItemRegistry.VINE_LASSO);
        if (!this.isDead) {
            if (!(entity instanceof EntityPlayer) || !((EntityPlayer) entity).inventory.addItemStackToInventory(item)) {
                this.entityDropItem(item, 0.0F);
            }
        }
        this.setDead();
    }

    protected void onHitBlock(RayTraceResult result) {
        if (!this.world.isRemote) {
            this.removeAndAddToInventory();
        }
    }

    @Override
    protected void entityInit() {
    }

    public void setShooter(@Nullable Entity entityIn) {
        if (entityIn != null) {
            this.ownerUUID = entityIn.getUniqueID();
            this.ownerNetworkId = entityIn.getEntityId();
        }
    }

    @Nullable
    public Entity getShooter() {
        if (this.ownerUUID != null && this.world instanceof WorldServer) {
            Entity e = ((WorldServer) this.world).getEntityFromUuid(this.ownerUUID);
            if (e != null) {
                return e;
            }
        }
        return this.ownerNetworkId != 0 ? this.world.getEntityByID(this.ownerNetworkId) : null;
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound compound) {
        if (this.ownerUUID != null) {
            compound.setUniqueId("Owner", this.ownerUUID);
        }

        if (this.leftOwner) {
            compound.setBoolean("LeftOwner", true);
        }
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound compound) {
        if (compound.hasUniqueId("Owner")) {
            this.ownerUUID = compound.getUniqueId("Owner");
        }

        this.leftOwner = compound.getBoolean("LeftOwner");
    }

    private boolean updateLeftOwner() {
        Entity entity = this.getShooter();
        if (entity != null) {
            for (Entity entity1 : this.world.getEntitiesWithinAABBExcludingEntity(this, this.getEntityBoundingBox().expand(this.motionX, this.motionY, this.motionZ).grow(1.0D))) {
                if (!(entity1 instanceof EntityPlayerMP && ((EntityPlayerMP) entity1).isSpectator()) && entity1.canBeCollidedWith()) {
                    if (entity1.getLowestRidingEntity() == entity.getLowestRidingEntity()) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    public void shoot(double x, double y, double z, float velocity, float inaccuracy) {
        Vec3d vector3d = new Vec3d(x, y, z).normalize().add(new Vec3d(this.rand.nextGaussian() * 0.0075D * inaccuracy, this.rand.nextGaussian() * 0.0075D * inaccuracy, this.rand.nextGaussian() * 0.0075D * inaccuracy)).scale(velocity);
        this.motionX = vector3d.x;
        this.motionY = vector3d.y;
        this.motionZ = vector3d.z;
        float f = MathHelper.sqrt(vector3d.x * vector3d.x + vector3d.z * vector3d.z);
        this.rotationYaw = (float) (MathHelper.atan2(vector3d.x, vector3d.z) * (180D / Math.PI));
        this.rotationPitch = (float) (MathHelper.atan2(vector3d.y, f) * (180D / Math.PI));
        this.prevRotationYaw = this.rotationYaw;
        this.prevRotationPitch = this.rotationPitch;
    }

    public void shootFromRotation(Entity shooter, float pitch, float yaw, float pitchOffset, float velocity, float inaccuracy) {
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
            this.onHitBlock(result);
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

    private boolean canHitEntity(Entity target) {
        if (!target.isEntityAlive() || !target.canBeCollidedWith()) {
            return false;
        }
        Entity entity = this.getShooter();
        return entity == null || this.leftOwner || !entity.isRidingSameEntity(target);
    }

    protected void updateRotation() {
        Vec3d vector3d = new Vec3d(this.motionX, this.motionY, this.motionZ);
        float f = MathHelper.sqrt(vector3d.x * vector3d.x + vector3d.z * vector3d.z);
        this.rotationPitch = lerpRotation(this.prevRotationPitch, (float) (MathHelper.atan2(vector3d.y, f) * (180D / Math.PI)));
        this.rotationYaw = this.rotationYaw + 20;
    }
}
