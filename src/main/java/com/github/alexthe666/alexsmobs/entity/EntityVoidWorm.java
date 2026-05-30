package com.github.alexthe666.alexsmobs.entity;
import com.github.alexthe666.alexsmobs.misc.AMLootTables;

import com.github.alexthe666.alexsmobs.AlexsMobs;
import com.github.alexthe666.alexsmobs.config.AMConfig;
import com.github.alexthe666.alexsmobs.entity.ai.DirectPathNavigator;
import com.github.alexthe666.alexsmobs.entity.ai.EntityAINearestTarget3D;
import com.github.alexthe666.alexsmobs.entity.ai.FlightMoveController;
import com.github.alexthe666.alexsmobs.misc.AMAdvancementTriggerRegistry;
import com.github.alexthe666.alexsmobs.misc.AMSoundRegistry;
import com.github.alexthe666.alexsmobs.misc.AMTagRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BossInfo;
import net.minecraft.world.BossInfoServer;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;
import com.google.common.base.Optional;
import java.util.Random;
import java.util.UUID;

public class EntityVoidWorm extends EntityMob {

    public static final ResourceLocation SPLITTER_LOOT = new ResourceLocation("alexsmobs", "entities/void_worm_splitter");
    private static final DataParameter<Optional<UUID>> CHILD_UUID = EntityDataManager.createKey(EntityVoidWorm.class, DataSerializers.OPTIONAL_UNIQUE_ID);
    private static final DataParameter<Optional<UUID>> SPLIT_FROM_UUID = EntityDataManager.createKey(EntityVoidWorm.class, DataSerializers.OPTIONAL_UNIQUE_ID);
    private static final DataParameter<Integer> SEGMENT_COUNT = EntityDataManager.createKey(EntityVoidWorm.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> JAW_TICKS = EntityDataManager.createKey(EntityVoidWorm.class, DataSerializers.VARINT);
    private static final DataParameter<Float> WORM_ANGLE = EntityDataManager.createKey(EntityVoidWorm.class, DataSerializers.FLOAT);
    private static final DataParameter<Float> SPEEDMOD = EntityDataManager.createKey(EntityVoidWorm.class, DataSerializers.FLOAT);
    private static final DataParameter<Boolean> SPLITTER = EntityDataManager.createKey(EntityVoidWorm.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> PORTAL_TICKS = EntityDataManager.createKey(EntityVoidWorm.class, DataSerializers.VARINT);
    private final BossInfoServer bossInfo = (BossInfoServer) (new BossInfoServer(this.getDisplayName(), BossInfo.Color.BLUE, BossInfo.Overlay.PROGRESS)).setDarkenSky(true);
    public float prevWormAngle;
    public float prevJawProgress;
    public float jawProgress;
    public Vec3d teleportPos = null;
    public EntityVoidPortal portalTarget = null;
    public boolean fullyThrough = true;
    public boolean updatePostSummon = false;
    private int makePortalCooldown = 0;
    private int stillTicks = 0;
    private int blockBreakCounter;
    private int makeIdlePortalCooldown = 200 + this.rand.nextInt(800);

    public EntityVoidWorm(World worldIn) {
        super(worldIn);
        this.setSize(1.2F, 1.95F);
        this.experienceValue = 10;
        this.moveHelper = new FlightMoveController(this, 1F, false, true);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(AMConfig.voidWormMaxHealth);
        this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(4.0D);
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(256.0D);
        AMEntityRegistry.registerAttributeIfAbsent(this, SharedMonsterAttributes.ATTACK_DAMAGE);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(5.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.3D);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return AMSoundRegistry.VOID_WORM_IDLE;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return AMSoundRegistry.VOID_WORM_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return AMSoundRegistry.VOID_WORM_HURT;
    }

    @Override
    protected float getSoundVolume() {
        return this.isSilent() ? 0 : 5;
    }

    @Override
    public boolean getCanSpawnHere() {
        return AMEntityRegistry.rollSpawn(AMConfig.voidWormSpawnRolls, this.getRNG(), AMEntityRegistry.AMSpawnReason.OTHER);
    }

    public static boolean canVoidWormSpawn(net.minecraft.world.biome.Biome.SpawnListEntry spawn, World worldIn, int x, int y, int z, Random random) {
        return true;
    }

    @Nullable
    @Override
    protected ResourceLocation getLootTable() {
        return this.isSplitter() ? AMLootTables.VOID_WORM_SPLITTER : AMLootTables.VOID_WORM;
    }

    @Override
    public void onKillCommand() {
        this.setDead();
    }

    @Override
    public void onDeath(DamageSource cause) {
        super.onDeath(cause);
        if (!this.world.isRemote && !this.isSplitter()) {
            if (cause != null && cause.getTrueSource() instanceof EntityPlayerMP) {
                AMAdvancementTriggerRegistry.VOID_WORM_SLAY_HEAD.trigger((EntityPlayerMP) cause.getTrueSource());
            }
        }
    }

    @Override
    public EntityItem entityDropItem(ItemStack stack, float offsetY) {
        EntityItem itementity = super.entityDropItem(stack, offsetY);
        if (itementity != null) {
            itementity.setNoGravity(true);
            itementity.setGlowing(true);
            itementity.setNoDespawn();
        }
        return itementity;
    }

    @Override
    public boolean isEntityInvulnerable(DamageSource source) {
        return source == DamageSource.FALL || source == DamageSource.DROWN || source == DamageSource.IN_WALL || source == DamageSource.FALLING_BLOCK || source == DamageSource.LAVA || source == DamageSource.OUT_OF_WORLD || source.isFireDamage() || super.isEntityInvulnerable(source);
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        boolean hurt = super.attackEntityFrom(source, amount);
        if (hurt) {
            this.alertToAttacker(source);
        }
        return hurt;
    }

    /**
     * Body segments have their own health; bow hits usually land on parts, not the head.
     * Forward aggro to the head so combat AI engages the shooter.
     */
    public void alertToAttacker(DamageSource source) {
        if (this.world.isRemote || source == null) {
            return;
        }
        Entity attacker = source.getTrueSource();
        if (attacker instanceof EntityLivingBase && attacker != this && attacker.isEntityAlive()) {
            EntityLivingBase living = (EntityLivingBase) attacker;
            this.setRevengeTarget(living);
            this.setAttackTarget(living);
            this.portalTarget = null;
            this.makeIdlePortalCooldown = Math.max(this.makeIdlePortalCooldown, 120);
        }
    }

    @Override
    protected boolean canDespawn() {
        return false;
    }

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(1, new AIEnterPortal());
        this.tasks.addTask(2, new AIAttack());
        this.tasks.addTask(3, new AIFlyIdle());
        this.targetTasks.addTask(0, new EntityAIHurtByTarget(this, true));
        this.targetTasks.addTask(1, new EntityAINearestTarget3D(this, EntityPlayer.class, 10, false, false, null));
        this.targetTasks.addTask(2, new EntityAINearestTarget3D(this, EntityDragon.class, 10, false, true, null));
    }

    @Override
    protected PathNavigate createNavigator(World worldIn) {
        return new DirectPathNavigator(this, worldIn);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        if (compound.hasUniqueId("ChildUUID")) {
            this.setChildId(compound.getUniqueId("ChildUUID"));
        }
        this.setWormSpeed(compound.getFloat("WormSpeed"));
        this.setSplitter(compound.getBoolean("Splitter"));
        this.setPortalTicks(compound.getInteger("PortalTicks"));
        this.makeIdlePortalCooldown = compound.getInteger("MakePortalTime");
        this.makePortalCooldown = compound.getInteger("MakePortalCooldown");
        if (this.hasCustomName()) {
            this.bossInfo.setName(this.getDisplayName());
        }
    }

    @Override
    public void setCustomNameTag(String name) {
        super.setCustomNameTag(name);
        this.bossInfo.setName(this.getDisplayName());
    }

    @Override
    public boolean hasNoGravity() {
        return true;
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        if (this.getChildId() != null) {
            compound.setUniqueId("ChildUUID", this.getChildId());
        }
        compound.setInteger("PortalTicks", this.getPortalTicks());
        compound.setInteger("MakePortalTime", this.makeIdlePortalCooldown);
        compound.setInteger("MakePortalCooldown", this.makePortalCooldown);
        compound.setFloat("WormSpeed", this.getWormSpeed());
        compound.setBoolean("Splitter", this.isSplitter());
    }

    @Nullable
    public Entity getChild() {
        UUID id = this.getChildId();
        if (id != null && !this.world.isRemote && this.world instanceof WorldServer) {
            return ((WorldServer) this.world).getEntityFromUuid(id);
        }
        return null;
    }

    @Override
    public boolean canBeLeashedTo(EntityPlayer player) {
        return true;
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        this.prevWormAngle = this.getWormAngle();
        this.prevJawProgress = this.jawProgress;
        float threshold = 0.05F;
        if (this.isSplitter()) {
            this.experienceValue = 10;
        } else {
            this.experienceValue = 70;
        }
        if (this.prevRotationYaw - this.rotationYaw > threshold) {
            this.setWormAngle(this.getWormAngle() + 15);
        } else if (this.prevRotationYaw - this.rotationYaw < -threshold) {
            this.setWormAngle(this.getWormAngle() - 15);
        } else if (this.getWormAngle() > 0) {
            this.setWormAngle(Math.max(this.getWormAngle() - 20, 0));
        } else if (this.getWormAngle() < 0) {
            this.setWormAngle(Math.min(this.getWormAngle() + 20, 0));
        }
        if (!this.world.isRemote) {
            if (!this.fullyThrough) {
                this.motionX *= 0.9F;
                this.motionY *= 0.9F;
                this.motionZ *= 0.9F;
                this.motionY -= 0.01;
            } else {
                this.motionY += 0.01;
            }
        }
        if (Math.abs(this.prevPosX - this.posX) < 0.01F && Math.abs(this.prevPosY - this.posY) < 0.01F && Math.abs(this.prevPosZ - this.posZ) < 0.01F) {
            this.stillTicks++;
        } else {
            this.stillTicks = 0;
        }
        if (this.stillTicks > 40 && this.makePortalCooldown == 0) {
            this.createStuckPortal();
        }
        if (this.makePortalCooldown > 0) {
            this.makePortalCooldown--;
        }
        if (this.makeIdlePortalCooldown > 0) {
            this.makeIdlePortalCooldown--;
        }
        if (this.makeIdlePortalCooldown == 0 && this.rand.nextInt(100) == 0) {
            this.createPortalRandomDestination();
            this.makeIdlePortalCooldown = 200 + this.rand.nextInt(1000);
        }
        if (this.dataManager.get(JAW_TICKS) > 0) {
            if (this.jawProgress < 5) {
                this.jawProgress++;
            }
            this.dataManager.set(JAW_TICKS, this.dataManager.get(JAW_TICKS) - 1);
        } else {
            if (this.jawProgress > 0) {
                this.jawProgress--;
            }
        }
        if (this.isEntityAlive()) {
            for (Entity entity : this.world.getEntitiesWithinAABB(EntityLivingBase.class, this.getEntityBoundingBox().grow(2.0D))) {
                if (!entity.isEntityEqual(this) && !(entity instanceof EntityVoidWormPart) && !this.isOnSameTeam(entity) && entity != this) {
                    this.launch(entity, false);
                }
            }
            this.stepHeight = 2.0F;
        }
        this.renderYawOffset = this.rotationYaw;
        float f2 = (float) -((float) this.motionY * (double) (180F / (float) Math.PI));
        this.rotationPitch = f2;
        this.stepHeight = 2.0F;
        if (!this.world.isRemote) {
            Entity child = this.getChild();
            if (child == null) {
                EntityLivingBase partParent = this;
                int tailstart = Math.min(3 + this.rand.nextInt(2), this.getSegmentCount());
                int segments = this.getSegmentCount();
                for (int i = 0; i < segments; i++) {
                    float scale = 1F + (i / (float) segments) * 0.5F;
                    boolean tail = false;
                    if (i >= segments - tailstart) {
                        tail = true;
                        scale = scale * 0.85F;
                    }
                    EntityVoidWormPart part = new EntityVoidWormPart(this.world, partParent, 1.0F + (scale * (tail ? 0.65F : 0.3F)) + (i == 0 ? 0.8F : 0), 180, i == 0 ? -0.0F : i == segments - tailstart ? -0.3F : 0);
                    part.setParent(partParent);
                    if (this.updatePostSummon) {
                        part.setPortalTicks(i * 2);
                    }
                    part.setBodyIndex(i);
                    part.setTail(tail);
                    part.setWormScale(scale);
                    if (partParent == this) {
                        this.setChildId(part.getUniqueID());
                    } else if (partParent instanceof EntityVoidWormPart) {
                        ((EntityVoidWormPart) partParent).setChildId(part.getUniqueID());
                    }
                    part.setInitialPartPos(this);
                    partParent = part;
                    this.world.spawnEntity(part);
                }
            }
        }
        if (this.getPortalTicks() > 0) {
            this.setPortalTicks(this.getPortalTicks() - 1);
            if (this.getPortalTicks() == 2 && this.teleportPos != null) {
                this.setPosition(this.teleportPos.x, this.teleportPos.y, this.teleportPos.z);
                this.teleportPos = null;
            }
        }
        if (this.portalTarget != null && this.portalTarget.getLifespan() < 5) {
            this.portalTarget = null;
        }
        this.bossInfo.setPercent(this.getHealth() / this.getMaxHealth());
        this.breakBlock();
        if (this.updatePostSummon) {
            this.updatePostSummon = false;
        }
        if (!this.isSilent() && !this.world.isRemote) {
            this.world.setEntityState(this, (byte) 67);
        }
    }

    public void setMaxHealth(double maxHealth, boolean heal) {
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(maxHealth);
        if (heal) {
            this.heal((float) maxHealth);
        }
    }

    @Override
    public void addTrackingPlayer(EntityPlayerMP player) {
        super.addTrackingPlayer(player);
        this.bossInfo.addPlayer(player);
    }

    @Override
    public void removeTrackingPlayer(EntityPlayerMP player) {
        super.removeTrackingPlayer(player);
        this.bossInfo.removePlayer(player);
    }

    public void teleportTo(Vec3d vec) {
        this.setPortalTicks(10);
        this.teleportPos = vec;
        this.fullyThrough = false;
        if (this.getChild() instanceof EntityVoidWormPart) {
            ((EntityVoidWormPart) this.getChild()).teleportTo(this.getPositionVector(), this.teleportPos);
        }
    }

    private void launch(Entity e, boolean huge) {
        if (e.onGround) {
            double d0 = e.posX - this.posX;
            double d1 = e.posZ - this.posZ;
            double d2 = Math.max(d0 * d0 + d1 * d1, 0.001D);
            float f = huge ? 2F : 0.5F;
            e.addVelocity(d0 / d2 * f, huge ? 0.5D : 0.2F, d1 / d2 * f);
        }
    }

    public void resetWormScales() {
        if (!this.world.isRemote) {
            Entity child = this.getChild();
            if (child == null) {
                EntityLivingBase nextPart = this;
                int tailstart = Math.min(3 + this.rand.nextInt(2), this.getSegmentCount());
                int segments = this.getSegmentCount();
                int i = 0;
                while (nextPart instanceof EntityVoidWormPart) {
                    EntityVoidWormPart part = (EntityVoidWormPart) ((EntityVoidWormPart) nextPart).getChild();
                    i++;
                    float scale = 1F + (i / (float) segments) * 0.5F;
                    boolean tail = i >= segments - tailstart;
                    part.setTail(tail);
                    part.setWormScale(scale);
                    part.radius = 1.0F + (scale * (tail ? 0.65F : 0.3F)) + (i == 0 ? 0.8F : 0F);
                    part.offsetY = i == 0 ? -0.0F : i == segments - tailstart ? -0.3F : 0;
                    nextPart = part;
                }
            }
        }
    }

    @Override
    @Nullable
    public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData spawnDataIn) {
        this.setSegmentCount(25 + this.rand.nextInt(15));
        this.rotationPitch = 0.0F;
        this.setMaxHealth(AMConfig.voidWormMaxHealth, true);
        return super.onInitialSpawn(difficulty, spawnDataIn);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(SPLIT_FROM_UUID, Optional.absent());
        this.dataManager.register(CHILD_UUID, Optional.absent());
        this.dataManager.register(SEGMENT_COUNT, 10);
        this.dataManager.register(JAW_TICKS, 0);
        this.dataManager.register(WORM_ANGLE, 0F);
        this.dataManager.register(SPEEDMOD, 1F);
        this.dataManager.register(SPLITTER, Boolean.FALSE);
        this.dataManager.register(PORTAL_TICKS, 0);
    }

    public float getWormAngle() {
        return this.dataManager.get(WORM_ANGLE);
    }

    public void setWormAngle(float progress) {
        this.dataManager.set(WORM_ANGLE, progress);
    }

    public float getWormSpeed() {
        return this.dataManager.get(SPEEDMOD);
    }

    public void setWormSpeed(float progress) {
        if (this.getWormSpeed() != progress) {
            this.moveHelper = new FlightMoveController(this, progress, false, true);
        }
        this.dataManager.set(SPEEDMOD, progress);
    }

    public boolean isSplitter() {
        return this.dataManager.get(SPLITTER);
    }

    public void setSplitter(boolean splitter) {
        this.dataManager.set(SPLITTER, splitter);
    }

    public void openMouth(int time) {
        this.dataManager.set(JAW_TICKS, time);
    }

    public boolean isMouthOpen() {
        return this.dataManager.get(JAW_TICKS) >= 5;
    }

    @Nullable
    public UUID getChildId() {
        return this.dataManager.get(CHILD_UUID).orNull();
    }

    public void setChildId(@Nullable UUID uniqueId) {
        this.dataManager.set(CHILD_UUID, Optional.fromNullable(uniqueId));
    }

    @Nullable
    public UUID getSplitFromUUID() {
        return this.dataManager.get(SPLIT_FROM_UUID).orNull();
    }

    public void setSplitFromUuid(@Nullable UUID uniqueId) {
        this.dataManager.set(SPLIT_FROM_UUID, Optional.fromNullable(uniqueId));
    }

    public int getPortalTicks() {
        return this.dataManager.get(PORTAL_TICKS);
    }

    public void setPortalTicks(int ticks) {
        this.dataManager.set(PORTAL_TICKS, ticks);
    }

    public int getSegmentCount() {
        return this.dataManager.get(SEGMENT_COUNT);
    }

    public void setSegmentCount(int command) {
        this.dataManager.set(SEGMENT_COUNT, command);
    }

    public void collideWithNearbyEntities() {
        List<Entity> entities = this.world.getEntitiesWithinAABBExcludingEntity(this, this.getEntityBoundingBox().grow(0.20000000298023224D, 0.0D, 0.20000000298023224D));
        for (Entity entity : entities) {
            if (!(entity instanceof EntityVoidWormPart) && entity.canBePushed()) {
                entity.applyEntityCollision(this);
            }
        }
    }

    @Override
    public void applyEntityCollision(Entity entityIn) {
    }

    public void createStuckPortal() {
        if (this.getAttackTarget() != null) {
            this.createPortal(this.getAttackTarget().getPositionVector().addVector(this.rand.nextInt(8) - 4, 2 + this.rand.nextInt(3), this.rand.nextInt(8) - 4));
        } else {
            BlockPos elevated = this.getPosition().up(this.rand.nextInt(10) + 10);
            BlockPos height = this.world.getHeight(elevated);
            this.createPortal(vecCenter(height));
        }
    }

    public void createPortal(Vec3d to) {
        this.createPortal(this.getPositionVector().add(this.getLookVec().scale(20.0D)), to, null);
    }

    public void createPortalRandomDestination() {
        Vec3d vec = null;
        for (int i = 0; i < 15; i++) {
            BlockPos pos = new BlockPos(this.posX + this.rand.nextInt(60) - 30, 0, this.posZ + this.rand.nextInt(60) - 30);
            BlockPos height = this.world.getHeight(pos);
            if (height.getY() < 10) {
                height = height.up(50 + this.rand.nextInt(50));
            } else {
                height = height.up(this.rand.nextInt(30));
            }
            if (this.world.isAirBlock(height)) {
                vec = centerHorizontal(height);
            }
        }
        if (vec != null) {
            this.createPortal(this.getPositionVector().add(this.getLookVec().scale(20.0D)), vec, null);
        }
    }

    public void createPortal(Vec3d from, Vec3d to, @Nullable EnumFacing outDir) {
        if (!this.world.isRemote && this.portalTarget == null) {
            Vec3d start = new Vec3d(this.posX, this.posY + (double) this.getEyeHeight(), this.posZ);
            RayTraceResult result = this.world.rayTraceBlocks(start, from, false, true, false);
            Vec3d vec = result != null && result.hitVec != null ? result.hitVec : this.getPositionVector();
            if (result != null && result.typeOfHit == RayTraceResult.Type.BLOCK) {
                EnumFacing face = result.sideHit;
                vec = vec.addVector(face.getFrontOffsetX(), face.getFrontOffsetY(), face.getFrontOffsetZ());
            }
            EntityVoidPortal portal = new EntityVoidPortal(this.world);
            portal.setPosition(vec.x, vec.y, vec.z);
            Vec3d dirVec = vec.subtract(this.getPositionVector());
            portal.setAttachmentFacing(facingFromVec(dirVec));
            portal.setLifespan(10000);
            this.world.spawnEntity(portal);
            this.portalTarget = portal;
            portal.setDestination(new BlockPos(to.x, to.y, to.z), outDir);
            this.makePortalCooldown = 300;
        }
    }

    public void resetPortalLogic() {
        this.portalTarget = null;
    }

    @Override
    public boolean canBePushed() {
        return false;
    }

    public void breakBlock() {
        if (this.blockBreakCounter > 0) {
            --this.blockBreakCounter;
            return;
        }
        boolean flag = false;
        AxisAlignedBB box = this.getEntityBoundingBox();
        if (!this.world.isRemote && this.blockBreakCounter == 0 && net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(this.world, this)) {
            for (int a = (int) Math.round(box.minX); a <= (int) Math.round(box.maxX); a++) {
                for (int b = (int) Math.round(box.minY) - 1; (b <= (int) Math.round(box.maxY) + 1) && (b <= 127); b++) {
                    for (int c = (int) Math.round(box.minZ); c <= (int) Math.round(box.maxZ); c++) {
                        BlockPos pos = new BlockPos(a, b, c);
                        IBlockState state = this.world.getBlockState(pos);
                        Block block = state.getBlock();
                        if (this.world.isAirBlock(pos)) {
                            continue;
                        }
                        AxisAlignedBB col = state.getCollisionBoundingBox(this.world, pos);
                        if (col == null || col == Block.NULL_AABB) {
                            continue;
                        }
                        if (!AMTagRegistry.blockInTag(AMTagRegistry.VOID_WORM_BREAKABLES, block)) {
                            continue;
                        }
                        if (state.getMaterial() == Material.WATER) {
                            continue;
                        }
                        if (block != Blocks.AIR) {
                            this.motionX *= 0.6F;
                            this.motionZ *= 0.6F;
                            flag = true;
                            this.world.destroyBlock(pos, true);
                            if (block == Blocks.ICE || block == Blocks.PACKED_ICE || block == Blocks.FROSTED_ICE) {
                                this.world.setBlockState(pos, Blocks.WATER.getDefaultState());
                            }
                        }
                    }
                }
            }
        }
        if (flag) {
            this.blockBreakCounter = 10;
        }
    }

    public boolean isTargetBlocked(Vec3d target) {
        Vec3d start = new Vec3d(this.posX, this.posY + (double) this.getEyeHeight(), this.posZ);
        RayTraceResult res = this.world.rayTraceBlocks(start, target, false, true, false);
        return res != null && res.typeOfHit == RayTraceResult.Type.BLOCK;
    }

    public Vec3d getBlockInViewAway(Vec3d fleePos, float radiusAdd) {
        float radius = (0.75F * (0.7F * 6) * -3 - this.getRNG().nextInt(24)) * radiusAdd;
        float neg = this.getRNG().nextBoolean() ? 1 : -1;
        float renderYawOffset = this.renderYawOffset;
        float angle = (0.01745329251F * renderYawOffset) + 3.15F + (this.getRNG().nextFloat() * neg);
        double extraX = radius * MathHelper.sin((float) (Math.PI + angle));
        double extraZ = radius * MathHelper.cos(angle);
        BlockPos radialPos = new BlockPos(fleePos.x + extraX, 0, fleePos.z + extraZ);
        BlockPos ground = this.getGround(radialPos);
        int distFromGround = (int) this.posY - ground.getY();
        int flightHeight = 10 + this.getRNG().nextInt(20);
        BlockPos newPos = ground.up(distFromGround > 8 ? flightHeight : this.getRNG().nextInt(10) + 15);
        Vec3d centerNew = vecCenter(newPos);
        if (!this.isTargetBlocked(centerNew) && this.getDistanceSq(centerNew.x, centerNew.y, centerNew.z) > 1.0D) {
            return centerNew;
        }
        return null;
    }

    public Vec3d getBlockInViewAwaySlam(Vec3d fleePos, int slamHeight) {
        float radius = 3 + this.rand.nextInt(3);
        float neg = this.getRNG().nextBoolean() ? 1 : -1;
        float renderYawOffset = this.renderYawOffset;
        float angle = (0.01745329251F * renderYawOffset) + 3.15F + (this.getRNG().nextFloat() * neg);
        double extraX = radius * MathHelper.sin((float) (Math.PI + angle));
        double extraZ = radius * MathHelper.cos(angle);
        BlockPos radialPos = new BlockPos(fleePos.x + extraX, 0, fleePos.z + extraZ);
        BlockPos ground = this.getHeighestAirAbove(radialPos, slamHeight);
        Vec3d centerGround = vecCenter(ground);
        if (!this.isTargetBlocked(centerGround) && this.getDistanceSq(centerGround.x, centerGround.y, centerGround.z) > 1.0D) {
            return centerGround;
        }
        return null;
    }

    private BlockPos getHeighestAirAbove(BlockPos radialPos, int limit) {
        BlockPos position = new BlockPos(radialPos.getX(), (int) this.posY, radialPos.getZ());
        while (position.getY() < 256 && position.getY() < this.posY + limit && this.world.isAirBlock(position)) {
            position = position.up();
        }
        return position;
    }

    private BlockPos getGround(BlockPos in) {
        BlockPos position = new BlockPos(in.getX(), (int) this.posY, in.getZ());
        while (position.getY() > 1 && this.world.isAirBlock(position)) {
            position = position.down();
        }
        if (position.getY() < 2) {
            return position.up(60 + this.rand.nextInt(5));
        }

        return position;
    }

    @Override
    public boolean isOnSameTeam(Entity entityIn) {
        return super.isOnSameTeam(entityIn) || this.getSplitFromUUID() != null && this.getSplitFromUUID().equals(entityIn.getUniqueID()) ||
                entityIn instanceof EntityVoidWorm && ((EntityVoidWorm) entityIn).getSplitFromUUID() != null && ((EntityVoidWorm) entityIn).getSplitFromUUID().equals(entityIn.getUniqueID());
    }

    private void spit(Vec3d shotAt, boolean portal) {
        shotAt = shotAt.rotateYaw(-this.rotationYaw * ((float) Math.PI / 180F));
        EntityVoidWormShot shot = new EntityVoidWormShot(this.world, this);
        double d0 = shotAt.x;
        double d1 = shotAt.y;
        double d2 = shotAt.z;
        float f = MathHelper.sqrt(d0 * d0 + d2 * d2) * 0.35F;
        shot.shoot(d0, d1 + (double) f, d2, 0.5F, 3.0F);
        if (!this.isSilent()) {
            this.world.playSound(null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_GHAST_SHOOT, this.getSoundCategory(), 1.0F, 1.0F + (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F);
        }
        shot.setPortalType(portal);
        this.openMouth(5);
        this.world.spawnEntity(shot);
    }

    private boolean wormAttack(Entity entity, DamageSource source, float dmg) {
        dmg *= AMConfig.voidWormDamageModifier;
        if (entity instanceof EntityDragon) {
            try {
                return (Boolean) ReflectionHelper.findMethod(EntityDragon.class, "attackDragonFrom", "func_82195_e", DamageSource.class, float.class).invoke(entity, source, dmg * 0.5F);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return entity.attackEntityFrom(source, dmg);
    }

    public void playHurtSoundWorm(DamageSource source) {
        this.playHurtSound(source);
    }

    private static Vec3d vecCenter(BlockPos p) {
        return new Vec3d((double) p.getX() + 0.5D, (double) p.getY() + 0.5D, (double) p.getZ() + 0.5D);
    }

    private static Vec3d centerHorizontal(BlockPos pos) {
        return new Vec3d((double) pos.getX() + 0.5D, (double) pos.getY(), (double) pos.getZ() + 0.5D);
    }

    private static EnumFacing facingFromVec(Vec3d vec) {
        double ax = Math.abs(vec.x);
        double ay = Math.abs(vec.y);
        double az = Math.abs(vec.z);
        if (ay >= ax && ay >= az) {
            return vec.y > 0 ? EnumFacing.UP : EnumFacing.DOWN;
        } else if (ax >= az) {
            return vec.x > 0 ? EnumFacing.EAST : EnumFacing.WEST;
        } else {
            return vec.z > 0 ? EnumFacing.SOUTH : EnumFacing.NORTH;
        }
    }

    private enum AttackMode {
        CIRCLE,
        SLAM_RISE,
        SLAM_FALL,
        PORTAL
    }

    private class AIFlyIdle extends EntityAIBase {
        protected final EntityVoidWorm voidWorm;
        protected double x;
        protected double y;
        protected double z;

        public AIFlyIdle() {
            this.setMutexBits(1);
            this.voidWorm = EntityVoidWorm.this;
        }

        @Override
        public boolean shouldExecute() {
            if (this.voidWorm.isBeingRidden() || this.voidWorm.portalTarget != null || (this.voidWorm.getAttackTarget() != null && this.voidWorm.getAttackTarget().isEntityAlive()) || this.voidWorm.isRiding()) {
                return false;
            } else {
                Vec3d lvt_1_1_ = this.getPosition();
                if (lvt_1_1_ == null) {
                    return false;
                } else {
                    this.x = lvt_1_1_.x;
                    this.y = lvt_1_1_.y;
                    this.z = lvt_1_1_.z;
                    return true;
                }
            }
        }

        @Override
        public void updateTask() {
            this.voidWorm.getMoveHelper().setMoveTo(this.x, this.y, this.z, 1.0D);
        }

        @Nullable
        protected Vec3d getPosition() {
            Vec3d vector3d = this.voidWorm.getPositionVector();
            return this.voidWorm.getBlockInViewAway(vector3d, 1);
        }

        @Override
        public boolean shouldContinueExecuting() {
            return this.voidWorm.getDistanceSq(this.x, this.y, this.z) > 20.0D && this.voidWorm.portalTarget == null && !this.voidWorm.collidedHorizontally && (this.voidWorm.getAttackTarget() == null || !this.voidWorm.getAttackTarget().isEntityAlive());
        }

        @Override
        public void startExecuting() {
            this.voidWorm.getMoveHelper().setMoveTo(this.x, this.y, this.z, 1.0D);
        }

        @Override
        public void resetTask() {
            this.voidWorm.getNavigator().clearPath();
        }
    }

    public class AIAttack extends EntityAIBase {

        private AttackMode mode = AttackMode.CIRCLE;
        private int modeTicks = 0;
        private int maxCircleTime = 500;
        private Vec3d moveTo = null;

        public AIAttack() {
            this.setMutexBits(5);
        }

        @Override
        public boolean shouldExecute() {
            return EntityVoidWorm.this.getAttackTarget() != null && EntityVoidWorm.this.getAttackTarget().isEntityAlive();
        }

        @Override
        public void resetTask() {
            this.mode = AttackMode.CIRCLE;
            this.modeTicks = 0;
        }

        @Override
        public void startExecuting() {
            this.mode = AttackMode.CIRCLE;
            this.maxCircleTime = 60 + EntityVoidWorm.this.rand.nextInt(200);
        }

        @Override
        public void updateTask() {
            EntityLivingBase target = EntityVoidWorm.this.getAttackTarget();
            boolean flag = false;
            float speed = 1;
            for (Entity entity : EntityVoidWorm.this.world.getEntitiesWithinAABB(EntityLivingBase.class, EntityVoidWorm.this.getEntityBoundingBox().grow(2.0D))) {
                if (!entity.isEntityEqual(EntityVoidWorm.this) && !(entity instanceof EntityVoidWormPart) && !entity.isOnSameTeam(EntityVoidWorm.this) && entity != EntityVoidWorm.this) {
                    if (EntityVoidWorm.this.isMouthOpen()) {
                        EntityVoidWorm.this.launch(entity, true);
                        flag = true;
                        EntityVoidWorm.this.wormAttack(entity, DamageSource.causeMobDamage(EntityVoidWorm.this), 8.0F + EntityVoidWorm.this.rand.nextFloat() * 8.0F);
                    } else {
                        EntityVoidWorm.this.openMouth(15);
                    }
                }
            }
            if (target != null) {
                if (this.mode == AttackMode.CIRCLE) {
                    if (this.moveTo == null || EntityVoidWorm.this.getDistanceSq(this.moveTo.x, this.moveTo.y, this.moveTo.z) < 16.0D || EntityVoidWorm.this.collidedHorizontally) {
                        double distSq = EntityVoidWorm.this.getDistanceSq(target);
                        if (distSq > 256.0D && (EntityVoidWorm.this.getRevengeTarget() == target || EntityVoidWorm.this.rand.nextInt(3) == 0)) {
                            this.moveTo = target.getPositionVector().addVector(0.0D, 3.0D + EntityVoidWorm.this.rand.nextInt(6), 0.0D);
                        } else {
                            this.moveTo = EntityVoidWorm.this.getBlockInViewAway(target.getPositionVector(), 0.4F + EntityVoidWorm.this.rand.nextFloat() * 0.2F);
                        }
                    }
                    this.modeTicks++;
                    if (this.modeTicks % 50 == 0) {
                        EntityVoidWorm.this.spit(new Vec3d(3, 3, 0), false);
                        EntityVoidWorm.this.spit(new Vec3d(-3, 3, 0), false);
                        EntityVoidWorm.this.spit(new Vec3d(3, -3, 0), false);
                        EntityVoidWorm.this.spit(new Vec3d(-3, -3, 0), false);
                    }
                    if (this.modeTicks > this.maxCircleTime) {
                        this.maxCircleTime = 60 + EntityVoidWorm.this.rand.nextInt(200);
                        this.mode = AttackMode.SLAM_RISE;
                        this.modeTicks = 0;
                        this.moveTo = null;
                    }
                } else if (this.mode == AttackMode.SLAM_RISE) {
                    if (this.moveTo == null) {
                        this.moveTo = EntityVoidWorm.this.getBlockInViewAwaySlam(target.getPositionVector(), 20 + EntityVoidWorm.this.rand.nextInt(20));
                    }
                    if (this.moveTo != null) {
                        if (EntityVoidWorm.this.posY > target.posY + 15) {
                            this.moveTo = null;
                            this.modeTicks = 0;
                            this.mode = AttackMode.SLAM_FALL;
                        }
                    }
                } else if (this.mode == AttackMode.SLAM_FALL) {
                    speed = 2;
                    EntityVoidWorm.this.getLookHelper().setLookPositionWithEntity(target, 360, 360);
                    this.moveTo = target.getPositionVector();
                    if (EntityVoidWorm.this.collidedHorizontally) {
                        this.moveTo = new Vec3d(target.posX, EntityVoidWorm.this.posY + 3, target.posZ);
                    }
                    EntityVoidWorm.this.openMouth(20);
                    if (EntityVoidWorm.this.getDistanceSq(this.moveTo.x, this.moveTo.y, this.moveTo.z) < 4.0D || flag) {
                        this.mode = AttackMode.CIRCLE;
                        this.moveTo = null;
                        this.modeTicks = 0;
                    }
                }
            }
            if (target != null && !EntityVoidWorm.this.canEntityBeSeen(target) && EntityVoidWorm.this.rand.nextInt(100) == 0) {
                if (EntityVoidWorm.this.makePortalCooldown == 0) {
                    Vec3d to = new Vec3d(target.posX, target.getEntityBoundingBox().maxY + 0.1, target.posZ);
                    EntityVoidWorm.this.createPortal(EntityVoidWorm.this.getPositionVector().add(EntityVoidWorm.this.getLookVec().scale(20.0D)), to, EnumFacing.UP);
                    EntityVoidWorm.this.makePortalCooldown = 50;
                    this.mode = AttackMode.SLAM_FALL;
                }
            }
            if (this.moveTo != null && EntityVoidWorm.this.portalTarget == null) {
                EntityVoidWorm.this.getMoveHelper().setMoveTo(this.moveTo.x, this.moveTo.y, this.moveTo.z, speed);
            }
        }
    }

    public class AIEnterPortal extends EntityAIBase {

        public AIEnterPortal() {
            this.setMutexBits(5);
        }

        @Override
        public boolean shouldExecute() {
            return EntityVoidWorm.this.portalTarget != null;
        }

        @Override
        public void updateTask() {
            if (EntityVoidWorm.this.portalTarget != null) {
                EntityVoidWorm.this.noClip = true;
                AxisAlignedBB bb = EntityVoidWorm.this.portalTarget.getEntityBoundingBox();
                double centerX = bb.minX + ((bb.maxX - bb.minX) / 2F);
                double centerY = bb.minY + ((bb.maxY - bb.minY) / 2F);
                double centerZ = bb.minZ + ((bb.maxZ - bb.minZ) / 2F);
                float sped = 0.08F;
                EntityVoidWorm.this.motionX += Math.signum(centerX - EntityVoidWorm.this.posX) * sped;
                EntityVoidWorm.this.motionY += Math.signum(centerY - EntityVoidWorm.this.posY) * sped;
                EntityVoidWorm.this.motionZ += Math.signum(centerZ - EntityVoidWorm.this.posZ) * sped;
                EntityVoidWorm.this.getMoveHelper().setMoveTo(centerX, centerY, centerZ, 1.0D);
            }
        }

        @Override
        public void resetTask() {
            EntityVoidWorm.this.noClip = false;
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void handleStatusUpdate(byte id) {
        if (id == 67) {
            AlexsMobs.PROXY.onEntityStatus(this, id);
        } else {
            super.handleStatusUpdate(id);
        }
    }
}
