package com.github.alexthe666.alexsmobs.entity;

import com.github.alexthe666.alexsmobs.config.AMConfig;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.stats.StatList;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
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

public class EntityVoidWormShot extends Entity {

    private static final DataParameter<Boolean> PORTALLING = EntityDataManager.createKey(EntityVoidWormShot.class, DataSerializers.BOOLEAN);

    private UUID ownerUniqueId;
    private int ownerEntityId;
    private boolean leftOwner;

    public EntityVoidWormShot(World worldIn) {
        super(worldIn);
        this.setSize(0.5F, 0.5F);
    }

    public EntityVoidWormShot(World worldIn, EntityVoidWorm shooter) {
        this(worldIn);
        this.setShooter(shooter);
        this.setPosition(
                shooter.posX - (double) (shooter.width + 1.0F) * 0.35D * (double) MathHelper.sin(shooter.renderYawOffset * ((float) Math.PI / 180F)),
                shooter.posY + 1.0D,
                shooter.posZ + (double) (shooter.width + 1.0F) * 0.35D * (double) MathHelper.cos(shooter.renderYawOffset * ((float) Math.PI / 180F)));
    }

    public EntityVoidWormShot(World worldIn, EntityLivingBase shooter, boolean right) {
        this(worldIn);
        this.setShooter(shooter);
        float rot = shooter.rotationYawHead + (right ? 60 : -60);
        this.setPosition(
                shooter.posX - (double) shooter.width * 0.9F * (double) MathHelper.sin(rot * ((float) Math.PI / 180F)),
                shooter.posY + 1.0D,
                shooter.posZ + (double) shooter.width * 0.9D * (double) MathHelper.cos(rot * ((float) Math.PI / 180F)));
    }

    @SideOnly(Side.CLIENT)
    public EntityVoidWormShot(World worldIn, double x, double y, double z, double motX, double motY, double motZ) {
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
    public void onUpdate() {
        if (!this.leftOwner) {
            this.leftOwner = this.checkLeftOwner();
        }
        if (this.ticksExisted > 40) {
            Entity entity = this.getShooter();
            if (isPortalType()) {
                this.motionX = 0;
                this.motionY = 0;
                this.motionZ = 0;
                if (this.ticksExisted > 60) {
                    this.setDead();
                }
            } else if (entity instanceof EntityMob) {
                EntityLivingBase target = ((EntityMob) entity).getAttackTarget();
                if (target == null) {
                    this.onKillCommand();
                } else {
                    double d0 = target.posX - this.posX;
                    double d1 = target.posY + (double) target.height * 0.5F - this.posY;
                    double d2 = target.posZ - this.posZ;
                    this.shoot(d0, d1, d2, 1.0F, 0.0F);
                    this.rotationYaw = -((float) MathHelper.atan2(d0, d2)) * (180F / (float) Math.PI);
                }
            }
        }
        super.onUpdate();

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
        this.noClip = true;

        if (this.world.collidesWithAnyBlock(this.getEntityBoundingBox())) {
            this.setDead();
        } else if (this.isInWater()) {
            this.setDead();
        } else {
            this.motionX = motion.x * 0.99D;
            this.motionY = motion.y * 0.99D;
            this.motionZ = motion.z * 0.99D;
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
        Entity entity = this.getShooter();
        if (result.entityHit == null) {
            return;
        }
        Entity hit = result.entityHit;
        if (entity instanceof EntityLivingBase && !(hit instanceof EntityVoidWorm) && !(hit instanceof EntityVoidWormPart)) {
            boolean b = wormAttack(hit, DamageSource.causeIndirectDamage(this, (EntityLivingBase) entity).setProjectile(), (float) (AMConfig.voidWormDamageModifier * 4F));
            if (b && hit instanceof EntityPlayer) {
                EntityPlayer player = (EntityPlayer) hit;
                if (player.isActiveItemStackBlocking()) {
                    damageShieldFor(player, 5.0F);
                }
            }
        }
        this.setDead();
    }

    private void damageShieldFor(EntityPlayer holder, float damage) {
        if (!holder.isActiveItemStackBlocking()) {
            return;
        }
        if (!this.world.isRemote) {
            holder.addStat(StatList.getObjectUseStats(holder.getActiveItemStack().getItem()));
        }
        if (damage >= 3.0F) {
            int i = 1 + MathHelper.floor(damage);
            EnumHand hand = holder.getActiveHand();
            holder.getActiveItemStack().damageItem(i, holder);
            if (holder.getActiveItemStack().isEmpty()) {
                if (hand == EnumHand.MAIN_HAND) {
                    holder.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, ItemStack.EMPTY);
                } else {
                    holder.setItemStackToSlot(EntityEquipmentSlot.OFFHAND, ItemStack.EMPTY);
                }
                holder.playSound(SoundEvents.ITEM_SHIELD_BREAK, 0.8F, 0.8F + this.world.rand.nextFloat() * 0.4F);
            }
        }
    }

    private boolean wormAttack(Entity entity, DamageSource source, float dmg) {
        return entity.attackEntityFrom(source, dmg);
    }

    protected void onBlockHit(RayTraceResult result) {
        if (!this.world.isRemote) {
            this.setDead();
        }
    }

    @Override
    protected void entityInit() {
        this.dataManager.register(PORTALLING, false);
    }

    public boolean isPortalType() {
        return this.dataManager.get(PORTALLING);
    }

    public void setPortalType(boolean portalType) {
        this.dataManager.set(PORTALLING, portalType);
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
        compound.setBoolean("PortalType", isPortalType());
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound compound) {
        if (compound.hasUniqueId("Owner")) {
            this.ownerUniqueId = compound.getUniqueId("Owner");
        }
        this.leftOwner = compound.getBoolean("LeftOwner");
        if (compound.hasKey("PortalType")) {
            this.setPortalType(compound.getBoolean("PortalType"));
        }
    }

    private boolean checkLeftOwner() {
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
        Vec3d vec = new Vec3d(x, y, z).normalize()
                .addVector(this.rand.nextGaussian() * 0.0075D * inaccuracy, this.rand.nextGaussian() * 0.0075D * inaccuracy, this.rand.nextGaussian() * 0.0075D * inaccuracy)
                .scale(velocity);
        this.motionX = vec.x;
        this.motionY = vec.y;
        this.motionZ = vec.z;
        float f = MathHelper.sqrt(vec.x * vec.x + vec.z * vec.z);
        this.rotationYaw = (float) (MathHelper.atan2(vec.x, vec.z) * (180D / Math.PI));
        this.rotationPitch = (float) (MathHelper.atan2(vec.y, f) * (180D / Math.PI));
        this.prevRotationYaw = this.rotationYaw;
        this.prevRotationPitch = this.rotationPitch;
    }

    protected void onImpact(RayTraceResult result) {
        if (result.typeOfHit == RayTraceResult.Type.ENTITY) {
            this.onEntityHit(result);
        } else if (result.typeOfHit == RayTraceResult.Type.BLOCK) {
            this.onBlockHit(result);
        }
        this.playSound(SoundEvents.BLOCK_GLASS_BREAK, 1.0F, 0.5F);
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

    protected boolean canHitEntity(Entity target) {
        if (!(target instanceof EntityPlayerMP && ((EntityPlayerMP) target).isSpectator()) && target.isEntityAlive() && target.canBeCollidedWith()) {
            Entity shooter = this.getShooter();
            return (shooter == null || this.leftOwner || !shooter.isRidingSameEntity(target))
                    && !(target instanceof EntityVoidWormShot) && !(target instanceof EntityVoidWormPart);
        }
        return false;
    }
}
