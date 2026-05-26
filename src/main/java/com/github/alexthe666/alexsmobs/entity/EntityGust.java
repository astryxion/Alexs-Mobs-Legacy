package com.github.alexthe666.alexsmobs.entity;

import com.github.alexthe666.alexsmobs.client.particle.AMParticleRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

public class EntityGust extends Entity {

    protected static final DataParameter<Boolean> VERTICAL = EntityDataManager.createKey(EntityGust.class, DataSerializers.BOOLEAN);
    protected static final DataParameter<Float> X_DIR = EntityDataManager.createKey(EntityGust.class, DataSerializers.FLOAT);
    protected static final DataParameter<Float> Y_DIR = EntityDataManager.createKey(EntityGust.class, DataSerializers.FLOAT);
    protected static final DataParameter<Float> Z_DIR = EntityDataManager.createKey(EntityGust.class, DataSerializers.FLOAT);
    private Entity pushedEntity = null;

    public EntityGust(World worldIn) {
        super(worldIn);
        this.setSize(1.0F, 1.0F);
    }

    @Override
    public void applyEntityCollision(Entity entityIn) {
    }

    protected static float func_234614_e_(float p_234614_0_, float p_234614_1_) {
        while (p_234614_1_ - p_234614_0_ < -180.0F) {
            p_234614_0_ -= 360.0F;
        }

        while (p_234614_1_ - p_234614_0_ >= 180.0F) {
            p_234614_0_ += 360.0F;
        }

        return p_234614_0_ + (p_234614_1_ - p_234614_0_) * 0.2F;
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (this.ticksExisted > 300) {
            this.setDead();
        }
        for (int i = 0; i < 1 + rand.nextInt(1); ++i) {
            AMParticleRegistry.spawnParticle(world, AMParticleRegistry.GUSTER_SAND_SPIN, this.posX + 0.5F * (rand.nextFloat() - 0.5F), this.posY + 0.5F * (rand.nextFloat() - 0.5F), this.posZ + 0.5F * (rand.nextFloat() - 0.5F), this.posX, this.posY + 0.5F, this.posZ);
        }
        Vec3d vector3d = new Vec3d(this.dataManager.get(X_DIR), this.dataManager.get(Y_DIR), this.dataManager.get(Z_DIR));
        RayTraceResult raytraceresult = this.traceMotion(vector3d);
        if (raytraceresult != null && raytraceresult.typeOfHit != RayTraceResult.Type.MISS && ticksExisted > 4) {
            this.onImpact(raytraceresult);
        }
        List<Entity> list = this.world.getEntitiesWithinAABB(Entity.class, this.getEntityBoundingBox().grow(0.1D));

        if (pushedEntity != null && this.getDistance(pushedEntity) > 2) {
            pushedEntity = null;
        }
        double d0 = this.posX + vector3d.x;
        double d1 = this.posY + vector3d.y;
        double d2 = this.posZ + vector3d.z;
        if (this.posY > this.world.getHeight()) {
            this.setDead();
        }
        this.func_234617_x_(vector3d);
        if (this.isInWater()) {
            this.setDead();
        } else {
            this.motionX = vector3d.x;
            this.motionY = vector3d.y;
            this.motionZ = vector3d.z;
            this.motionY -= 0.06D;
            this.setPosition(d0, d1, d2);
            if (pushedEntity != null) {
                pushedEntity.motionX = this.motionX;
                pushedEntity.motionY = this.motionY + 0.063D;
                pushedEntity.motionZ = this.motionZ;
            }
            for (Entity e : list) {
                e.motionX = this.motionX;
                e.motionY = this.motionY + 0.068D;
                e.motionZ = this.motionZ;
                if (e.motionY < 0) {
                    e.motionY = 0;
                }
                e.fallDistance = 0;
            }
        }
    }

    public void setGustDir(float x, float y, float z) {
        this.dataManager.set(X_DIR, x);
        this.dataManager.set(Y_DIR, y);
        this.dataManager.set(Z_DIR, z);
    }

    public float getGustDir(int xyz) {
        return this.dataManager.get(xyz == 2 ? Z_DIR : xyz == 1 ? Y_DIR : X_DIR);
    }

    protected void onEntityHit(RayTraceResult result) {
        Entity entity = result.entityHit;
        if (entity instanceof EntityGust) {
            EntityGust other = (EntityGust) entity;
            double avgX = (other.posX + this.posX) / 2F;
            double avgY = (other.posY + this.posY) / 2F;
            double avgZ = (other.posZ + this.posZ) / 2F;
            other.setPosition(avgX, avgY, avgZ);
            other.setGustDir(other.getGustDir(0) + this.getGustDir(0), other.getGustDir(1) + this.getGustDir(1), other.getGustDir(2) + this.getGustDir(2));
            if (this.isEntityAlive() && other.isEntityAlive()) {
                this.setDead();
            }
        } else if (entity != null) {
            pushedEntity = entity;
        }
    }

    protected boolean func_230298_a_(Entity entity) {
        return !(entity instanceof EntityPlayerMP && ((EntityPlayerMP) entity).isSpectator());
    }

    protected void func_230299_a_(RayTraceResult blockResult) {
        BlockPos pos = blockResult.getBlockPos();
        if (pos != null && this.world.getBlockState(pos).getMaterial().isSolid()) {
            if (!this.world.isRemote) {
                this.setDead();
            }
        }
    }

    @Override
    protected void entityInit() {
        this.dataManager.register(VERTICAL, false);
        this.dataManager.register(X_DIR, 0f);
        this.dataManager.register(Y_DIR, 0F);
        this.dataManager.register(Z_DIR, 0F);
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound compound) {
        compound.setBoolean("VerticalTornado", getVertical());
        compound.setFloat("GustDirX", this.dataManager.get(X_DIR));
        compound.setFloat("GustDirY", this.dataManager.get(Y_DIR));
        compound.setFloat("GustDirZ", this.dataManager.get(Z_DIR));
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound compound) {
        this.dataManager.set(X_DIR, compound.getFloat("GustDirX"));
        this.dataManager.set(Y_DIR, compound.getFloat("GustDirX"));
        this.dataManager.set(Z_DIR, compound.getFloat("GustDirX"));
        this.setVertical(compound.getBoolean("VerticalTornado"));
    }

    public void setVertical(boolean vertical) {
        this.dataManager.set(VERTICAL, vertical);
    }

    public boolean getVertical() {
        return this.dataManager.get(VERTICAL);
    }

    protected void onImpact(RayTraceResult result) {
        if (result.typeOfHit == RayTraceResult.Type.ENTITY) {
            this.onEntityHit(result);
        } else if (result.typeOfHit == RayTraceResult.Type.BLOCK) {
            this.func_230299_a_(result);
        }
    }

    @SideOnly(Side.CLIENT)
    public void setVelocity(double x, double y, double z) {
        this.motionX = x;
        this.motionY = y;
        this.motionZ = z;
        if (this.prevRotationPitch == 0.0F && this.prevRotationYaw == 0.0F) {
            float f = MathHelper.sqrt(x * x + z * z);
            this.rotationPitch = (float) (MathHelper.atan2(y, f) * (double) (180F / (float) Math.PI));
            this.rotationYaw = (float) (MathHelper.atan2(x, z) * (double) (180F / (float) Math.PI));
            this.prevRotationPitch = this.rotationPitch;
            this.prevRotationYaw = this.rotationYaw;
            this.setLocationAndAngles(this.posX, this.posY, this.posZ, this.rotationYaw, this.rotationPitch);
        }
    }

    protected void func_234617_x_(Vec3d vector3d) {
        float f = MathHelper.sqrt(vector3d.x * vector3d.x + vector3d.z * vector3d.z);
        this.rotationPitch = func_234614_e_(this.prevRotationPitch, (float) (MathHelper.atan2(vector3d.y, f) * (double) (180F / (float) Math.PI)));
        this.rotationYaw = func_234614_e_(this.prevRotationYaw, (float) (MathHelper.atan2(vector3d.x, vector3d.z) * (double) (180F / (float) Math.PI)));
    }

    @Nullable
    private RayTraceResult traceMotion(Vec3d motion) {
        Vec3d start = new Vec3d(this.posX, this.posY, this.posZ);
        Vec3d end = start.add(motion);
        RayTraceResult block = this.world.rayTraceBlocks(start, end, false, true, false);

        RayTraceResult entityHit = null;
        double bestDist = motion.lengthSquared();
        AxisAlignedBB search = this.getEntityBoundingBox().expand(motion.x, motion.y, motion.z).grow(1.0D);
        List<Entity> list = this.world.getEntitiesWithinAABBExcludingEntity(this, search);
        for (Entity e : list) {
            if (!this.func_230298_a_(e)) {
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
}
