package com.github.alexthe666.alexsmobs.entity;
import com.github.alexthe666.alexsmobs.misc.AMLootTables;

import com.github.alexthe666.alexsmobs.client.particle.AMParticleRegistry;
import com.github.alexthe666.alexsmobs.config.AMConfig;
import com.github.alexthe666.alexsmobs.entity.ai.AnimalAIWanderRanged;
import com.github.alexthe666.alexsmobs.entity.ai.GroundPathNavigatorWide;
import com.github.alexthe666.alexsmobs.misc.AMSoundRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;

import javax.annotation.Nullable;
import java.util.List;

public class EntityGuster extends EntityMob {

    private static final DataParameter<Integer> LIFT_ENTITY = EntityDataManager.createKey(EntityGuster.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> VARIANT = EntityDataManager.createKey(EntityGuster.class, DataSerializers.VARINT);
    private int liftingTime = 0;
    private int maxLiftTime = 40;
    private int shootingTicks;
    public static final ResourceLocation RED_LOOT = new ResourceLocation("alexsmobs", "entities/guster_red");
    public static final ResourceLocation SOUL_LOOT = new ResourceLocation("alexsmobs", "entities/guster_soul");

    public EntityGuster(World worldIn) {
        super(worldIn);
        this.stepHeight = 1;
        this.setPathPriority(PathNodeType.WATER, -1.0F);
    }

    @Override
    public int getTalkInterval() {
        return 80;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return AMSoundRegistry.GUSTER_IDLE;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return AMSoundRegistry.GUSTER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return AMSoundRegistry.GUSTER_HURT;
    }

    @Nullable
    @Override
    protected ResourceLocation getLootTable() {
        return this.getVariant() == 2 ? AMLootTables.GUSTER_SOUL : this.getVariant() == 1 ? AMLootTables.GUSTER_RED : AMLootTables.GUSTER;
    }

    private static boolean isGusterSpawnBlock(IBlockState state) {
        Block block = state.getBlock();
        return block == Blocks.SAND || block == Blocks.SOUL_SAND || block == Blocks.MAGMA;
    }

    @Override
    public boolean getCanSpawnHere() {
        BlockPos down = new BlockPos(this.posX, this.posY - 1, this.posZ);
        boolean spawnBlock = isGusterSpawnBlock(this.world.getBlockState(down));
        boolean weatherOk = !AMConfig.limitGusterSpawnsToWeather || this.world.isThundering() || this.world.isRaining() || isBiomeNether(this.world, down);
        return spawnBlock && weatherOk
                && AMEntityRegistry.rollSpawn(AMConfig.gusterSpawnRolls, this.getRNG(), AMEntityRegistry.AMSpawnReason.OTHER)
                && super.getCanSpawnHere();
    }

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(0, new MeleeGoal());
        this.tasks.addTask(1, new AnimalAIWanderRanged(this, 60, 1.0D, 10, 7));
        this.tasks.addTask(2, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        this.tasks.addTask(2, new EntityAILookIdle(this));
        this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, true));
        this.targetTasks.addTask(2, new EntityAINearestAttackableTarget<>(this, EntityPlayer.class, true));
        this.targetTasks.addTask(3, new EntityAINearestAttackableTarget<>(this, EntityVillager.class, true));
    }

    @Override
    protected PathNavigate createNavigator(World worldIn) {
        return new GroundPathNavigatorWide(this, worldIn);
    }

    @Override
    public void fall(float distance, float damageMultiplier) {
    }

    @Override
    protected void playStepSound(BlockPos pos, Block blockIn) {
    }

    @Override
    public void applyEntityCollision(Entity entityIn) {
        if (this.getLiftedEntity() == null && liftingTime >= 0 && !(entityIn instanceof EntityGuster)) {
            this.setLiftedEntity(entityIn.getEntityId());
            maxLiftTime = 30 + rand.nextInt(30);
        }
    }

    public boolean hasLiftedEntity() {
        return this.dataManager.get(LIFT_ENTITY) != 0;
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(LIFT_ENTITY, 0);
        this.dataManager.register(VARIANT, 0);
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (this.isEntityInvulnerable(source)) {
            return false;
        } else {
            if (source.isProjectile()) {
                amount = (amount + 1.0F) / 3.0F;
            }
            return super.attackEntityFrom(source, amount);
        }
    }

    private void spit(EntityLivingBase target) {
        EntitySandShot sghot = new EntitySandShot(this.world, this);
        double d0 = target.posX - this.posX;
        double d1 = target.posY + target.height * 0.3333333333333333D - sghot.posY;
        double d2 = target.posZ - this.posZ;
        float f = MathHelper.sqrt(d0 * d0 + d2 * d2) * 0.35F;
        sghot.shoot(d0, d1 + (double) f, d2, 1F, 10.0F);
        sghot.setVariant(this.getVariant());
        if (!this.isSilent()) {
            this.world.playSound(null, this.posX, this.posY, this.posZ, SoundEvents.BLOCK_SAND_BREAK, this.getSoundCategory(), 1.0F, 1.0F + (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F);
        }
        this.world.spawnEntity(sghot);
    }

    @Override
    public float getEyeHeight() {
        return this.height + 1.0F;
    }

    @Nullable
    public Entity getLiftedEntity() {
        if (!this.hasLiftedEntity()) {
            return null;
        } else {
            return this.world.getEntityByID(this.dataManager.get(LIFT_ENTITY));
        }
    }

    @Nullable
    @Override
    public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData livingdata) {
        BlockPos pos = this.getPosition();
        if (isBiomeNether(this.world, pos)) {
            this.setVariant(2);
        } else if (isBiomeRed(this.world, pos)) {
            this.setVariant(1);
        } else {
            this.setVariant(0);
        }
        this.setAir(300);
        this.rotationPitch = 0.0F;
        return super.onInitialSpawn(difficulty, livingdata);
    }

    private void setLiftedEntity(int entityId) {
        this.dataManager.set(LIFT_ENTITY, entityId);
    }

    public int getVariant() {
        return this.dataManager.get(VARIANT);
    }

    public void setVariant(int variant) {
        this.dataManager.set(VARIANT, variant);
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        Entity lifted = this.getLiftedEntity();
        if (lifted == null && !world.isRemote && ticksExisted % 15 == 0) {
            List<EntityItem> list = this.world.getEntitiesWithinAABB(EntityItem.class, this.getEntityBoundingBox().grow(0.8D, 0.8D, 0.8D));
            EntityItem closestItem = null;
            for (EntityItem entity : list) {
                if (entity.onGround && (closestItem == null || this.getDistance(closestItem) > this.getDistance(entity))) {
                    closestItem = entity;
                }
            }
            if (closestItem != null) {
                this.setLiftedEntity(closestItem.getEntityId());
                maxLiftTime = 30 + rand.nextInt(30);
            }
        }
        if (this.isInWater()) {
            this.attackEntityFrom(DamageSource.DROWN, 0.5F);
        }
        float f = (float) this.posY;
        if (this.isEntityAlive()) {
            int type = this.getVariant() == 2 ? AMParticleRegistry.GUSTER_SAND_SPIN_SOUL : this.getVariant() == 1 ? AMParticleRegistry.GUSTER_SAND_SPIN_RED : AMParticleRegistry.GUSTER_SAND_SPIN;
            for (int j = 0; j < 4; ++j) {
                float f1 = (this.rand.nextFloat() * 2.0F - 1.0F) * this.width * 0.95F;
                float f2 = (this.rand.nextFloat() * 2.0F - 1.0F) * this.width * 0.95F;
                AMParticleRegistry.spawnParticle(this.world, type, this.posX + (double) f1, f, this.posZ + (double) f2, this.posX, this.posY + rand.nextFloat() * this.height + 0.2F, this.posZ);
            }
        }
        if (lifted != null && liftingTime >= 0) {
            liftingTime++;
            float resist = 1F;
            if (lifted instanceof EntityLivingBase) {
                resist = (float) MathHelper.clamp((1.0D - ((EntityLivingBase) lifted).getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).getAttributeValue()), 0, 1);
            }
            float radius = 1F + (liftingTime * 0.05F);
            if (lifted instanceof EntityItem) {
                radius = 0.2F + (liftingTime * 0.025F);
            }
            float angle = liftingTime * -0.25F;
            double extraX = this.posX + radius * MathHelper.sin((float) (Math.PI + angle));
            double extraZ = this.posZ + radius * MathHelper.cos(angle);
            double d0 = (extraX - lifted.posX) * resist;
            double d1 = (extraZ - lifted.posZ) * resist;
            lifted.motionX = d0;
            lifted.motionY = 0.1 * resist;
            lifted.motionZ = d1;
            lifted.isAirBorne = true;
            if (liftingTime > maxLiftTime) {
                this.setLiftedEntity(0);
                liftingTime = -20;
                maxLiftTime = 30 + rand.nextInt(30);
            }
        } else if (liftingTime < 0) {
            liftingTime++;
        } else if (this.getAttackTarget() != null && this.getDistance(this.getAttackTarget()) < this.width + 1F && !(this.getAttackTarget() instanceof EntityGuster)) {
            this.setLiftedEntity(this.getAttackTarget().getEntityId());
            maxLiftTime = 30 + rand.nextInt(30);
        }
        if (!world.isRemote && shootingTicks >= 0) {
            if (shootingTicks <= 0) {
                if (this.getAttackTarget() != null && (lifted == null || lifted.getEntityId() != this.getAttackTarget().getEntityId()) && this.isEntityAlive()) {
                    this.spit(this.getAttackTarget());
                }
                shootingTicks = 40 + rand.nextInt(40);
            } else {
                shootingTicks--;
            }
        }
        if (!this.onGround && this.motionY < 0.0D) {
            this.motionY *= 0.6D;
        }
    }

    public boolean isGooglyEyes() {
        String s = TextFormatting.getTextWithoutFormattingCodes(this.getName());
        return s != null && s.toLowerCase().contains("tweester");
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setInteger("Variant", this.getVariant());
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.setVariant(compound.getInteger("Variant"));
    }

    private static boolean isBiomeRed(World worldIn, BlockPos position) {
        Biome biome = worldIn.getBiome(position);
        return BiomeDictionary.hasType(biome, BiomeDictionary.Type.MESA);
    }

    private static boolean isBiomeNether(World worldIn, BlockPos position) {
        Biome biome = worldIn.getBiome(position);
        return BiomeDictionary.hasType(biome, BiomeDictionary.Type.NETHER);
    }

    public static int getColorForVariant(int variant) {
        if (variant == 2) {
            return 0X4E3D33;
        } else if (variant == 1) {
            return 0XC66127;
        } else {
            return 0XF3C389;
        }
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(16.0D);
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(32.0D);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(1.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.2D);
    }

    private class MeleeGoal extends EntityAIBase {

        MeleeGoal() {
            this.setMutexBits(1);
        }

        @Override
        public boolean shouldExecute() {
            return EntityGuster.this.getAttackTarget() != null;
        }

        @Override
        public void updateTask() {
            Entity thrownEntity = EntityGuster.this.getLiftedEntity();
            if (EntityGuster.this.getAttackTarget() != null) {
                if (thrownEntity != null && thrownEntity.getEntityId() == EntityGuster.this.getAttackTarget().getEntityId()) {
                    EntityGuster.this.getNavigator().clearPath();
                } else {
                    EntityGuster.this.getNavigator().tryMoveToEntityLiving(EntityGuster.this.getAttackTarget(), 1.25D);
                }
            }
        }
    }
}
