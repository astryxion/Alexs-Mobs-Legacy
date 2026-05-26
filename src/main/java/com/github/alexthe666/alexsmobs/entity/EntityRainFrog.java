package com.github.alexthe666.alexsmobs.entity;

import com.github.alexthe666.alexsmobs.config.AMConfig;
import com.github.alexthe666.alexsmobs.entity.ai.AnimalAIWanderRanged;
import com.github.alexthe666.alexsmobs.entity.ai.CreatureAITargetItems;
import com.github.alexthe666.alexsmobs.misc.AMSoundRegistry;
import com.github.alexthe666.alexsmobs.misc.AMTagRegistry;
import net.minecraft.block.BlockSand;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAvoidEntity;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIMate;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAITempt;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldInfo;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.Random;

public class EntityRainFrog extends EntityAnimal implements ITargetsDroppedItems {

    private static final DataParameter<Integer> VARIANT = EntityDataManager.createKey(EntityRainFrog.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> STANCE_TIME = EntityDataManager.createKey(EntityRainFrog.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> ATTACK_TIME = EntityDataManager.createKey(EntityRainFrog.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> DANCE_TIME = EntityDataManager.createKey(EntityRainFrog.class, DataSerializers.VARINT);
    private static final DataParameter<Boolean> BURROWED = EntityDataManager.createKey(EntityRainFrog.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> DISTURBED = EntityDataManager.createKey(EntityRainFrog.class, DataSerializers.BOOLEAN);
    public float burrowProgress;
    public float prevBurrowProgress;
    public float danceProgress;
    public float prevDanceProgress;
    public float attackProgress;
    public float prevAttackProgress;
    public float stanceProgress;
    public float prevStanceProgress;
    private int burrowCooldown = 0;
    private int weatherCooldown = 0;
    private boolean isJukeboxing;
    private BlockPos jukeboxPosition;

    public EntityRainFrog(World world) {
        super(world);
        this.setSize(0.7F, 0.5F);
    }

    public static boolean canRainFrogSpawn(World world, BlockPos pos, Random random) {
        boolean spawnBlock = AMTagRegistry.blockInTag(AMTagRegistry.RAIN_FROG_SPAWNS, world.getBlockState(pos.down()).getBlock());
        WorldInfo info = world.getWorldInfo();
        return spawnBlock && (info.isRaining() || info.isThundering());
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(6.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.2D);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(VARIANT, 0);
        this.dataManager.register(STANCE_TIME, 0);
        this.dataManager.register(ATTACK_TIME, 0);
        this.dataManager.register(DANCE_TIME, 0);
        this.dataManager.register(BURROWED, false);
        this.dataManager.register(DISTURBED, false);
    }

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(1, new EntityAITempt(this, 1.0D, Items.SPIDER_EYE, false) {
            @Override
            protected boolean isTempting(ItemStack stack) {
                return AMTagRegistry.itemInTag(AMTagRegistry.RAIN_FROG_BREEDABLES, stack.getItem());
            }
        });
        this.tasks.addTask(2, new EntityAIMate(this, 1.0D));
        this.tasks.addTask(3, new EntityAIAvoidEntity<>(this, EntityRattlesnake.class, 9.0F, 1.3D, 1.0D));
        this.tasks.addTask(5, new AIBurrow());
        this.tasks.addTask(6, new AnimalAIWanderRanged(this, 20, 1.0D, 10, 7));
        this.tasks.addTask(7, new EntityAIWatchClosest(this, EntityPlayer.class, 10.0F));
        this.tasks.addTask(8, new EntityAILookIdle(this));
        this.targetTasks.addTask(1, new CreatureAITargetItems(this, false));
    }

    @Override
    public boolean getCanSpawnHere() {
        return AMEntityRegistry.rollSpawn(AMConfig.rainFrogSpawnRolls, this.getRNG(), AMEntityRegistry.AMSpawnReason.OTHER)
                && canRainFrogSpawn(this.world, this.getPosition(), this.getRNG());
    }

    @Override
    protected boolean canDespawn() {
        return !this.isDisturbed() && super.canDespawn();
    }

    public boolean isBurrowed() {
        return this.dataManager.get(BURROWED);
    }

    public void setBurrowed(boolean burrowed) {
        this.dataManager.set(BURROWED, burrowed);
    }

    public boolean isDisturbed() {
        return this.dataManager.get(DISTURBED);
    }

    public void setDisturbed(boolean disturbed) {
        this.dataManager.set(DISTURBED, disturbed);
    }

    public int getVariant() {
        return this.dataManager.get(VARIANT);
    }

    public void setVariant(int variant) {
        this.dataManager.set(VARIANT, variant);
    }

    public int getStanceTime() {
        return this.dataManager.get(STANCE_TIME);
    }

    public void setStanceTime(int stanceTime) {
        this.dataManager.set(STANCE_TIME, stanceTime);
    }

    public int getAttackTime() {
        return this.dataManager.get(ATTACK_TIME);
    }

    public void setAttackTime(int attackTime) {
        this.dataManager.set(ATTACK_TIME, attackTime);
    }

    public int getDanceTime() {
        return this.dataManager.get(DANCE_TIME);
    }

    public void setDanceTime(int danceTime) {
        this.dataManager.set(DANCE_TIME, danceTime);
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        return AMTagRegistry.itemInTag(AMTagRegistry.RAIN_FROG_BREEDABLES, stack.getItem());
    }

    @Nullable
    @Override
    public EntityRainFrog createChild(EntityAgeable ageable) {
        EntityRainFrog frog = new EntityRainFrog(this.world);
        frog.setVariant(this.getVariant());
        frog.setDisturbed(true);
        return frog;
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setBoolean("Disturbed", this.isDisturbed());
        compound.setInteger("Variant", this.getVariant());
        compound.setInteger("WeatherCooldown", this.weatherCooldown);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.setDisturbed(compound.getBoolean("Disturbed"));
        this.setVariant(compound.getInteger("Variant"));
        this.weatherCooldown = compound.getInteger("WeatherCooldown");
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        prevBurrowProgress = burrowProgress;
        prevDanceProgress = danceProgress;
        prevAttackProgress = attackProgress;
        prevStanceProgress = stanceProgress;

        if (this.isBurrowed()) {
            if (burrowProgress < 5F) {
                burrowProgress += 0.5F;
            }
        } else if (burrowProgress > 0F) {
            burrowProgress -= 0.5F;
        }

        if (this.burrowCooldown > 0) {
            this.burrowCooldown--;
        }

        if (this.getStanceTime() > 0) {
            this.setStanceTime(this.getStanceTime() - 1);
            if (this.stanceProgress < 5F) {
                this.stanceProgress++;
            }
        } else if (this.stanceProgress > 0F) {
            this.stanceProgress--;
        }

        if (this.getAttackTime() > 0) {
            this.setAttackTime(this.getAttackTime() - 1);
            if (this.attackProgress < 5F) {
                this.attackProgress += 2.5F;
            }
        } else if (this.attackProgress > 0F) {
            this.attackProgress -= 0.5F;
        }

        boolean dancing = this.getDanceTime() > 0 || this.isJukeboxing;
        if (dancing) {
            if (this.danceProgress < 5F) {
                this.danceProgress++;
            }
        } else if (this.danceProgress > 0F) {
            this.danceProgress--;
        }

        if (this.getDanceTime() > 0) {
            this.setBurrowed(false);
            this.setDanceTime(this.getDanceTime() - 1);
            if (this.getDanceTime() == 1 && weatherCooldown <= 0 && this.world.getGameRules().getBoolean("doWeatherCycle")) {
                changeWeather();
            }
        }
        if (weatherCooldown > 0) {
            weatherCooldown--;
        }
        if (this.jukeboxPosition == null || this.jukeboxPosition.distanceSq(this.posX, this.posY, this.posZ) > 15.0D * 15.0D
                || this.world.getBlockState(this.jukeboxPosition).getBlock() != Blocks.JUKEBOX) {
            this.isJukeboxing = false;
            this.setDanceTime(0);
            this.jukeboxPosition = null;
        }
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        boolean prev = super.attackEntityFrom(source, amount);
        if (prev && source.getImmediateSource() instanceof EntityLivingBase) {
            if (this.getStanceTime() <= 0) {
                this.setStanceTime(30 + this.rand.nextInt(20));
            }
            this.setBurrowed(false);
        }
        return prev;
    }

    @Override
    public boolean isEntityInvulnerable(DamageSource source) {
        return source == DamageSource.IN_WALL || super.isEntityInvulnerable(source);
    }

    @Override
    public net.minecraft.entity.IEntityLivingData onInitialSpawn(net.minecraft.world.DifficultyInstance difficulty, net.minecraft.entity.IEntityLivingData livingdata) {
        livingdata = super.onInitialSpawn(difficulty, livingdata);
        this.setVariant(this.rand.nextInt(3));
        return livingdata;
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        ItemStack itemstack = player.getHeldItem(hand);
        Item item = itemstack.getItem();
        boolean type = super.processInteract(player, hand);
        if (item == Items.IRON_SHOVEL || item == Items.DIAMOND_SHOVEL || item == Items.GOLDEN_SHOVEL || item == Items.STONE_SHOVEL || item == Items.WOODEN_SHOVEL) {
            if ((this.isBurrowed() || !this.isDisturbed()) && !this.world.isRemote) {
                this.livingSoundTime = 1000;
                if (!player.capabilities.isCreativeMode) {
                    itemstack.damageItem(1, player);
                }
                this.setStanceTime(20 + this.rand.nextInt(30));
                this.setBurrowed(false);
                this.setDisturbed(true);
                this.burrowCooldown += 150 + this.rand.nextInt(120);
                this.playSound(SoundEvents.BLOCK_SAND_BREAK, this.getSoundVolume(), this.getSoundPitch());
                return true;
            }
        }
        return type;
    }

    @Override
    public void travel(float strafe, float vertical, float forward) {
        if (this.isBurrowed() || this.getDanceTime() > 0) {
            if (this.getNavigator().getPath() != null) {
                this.getNavigator().clearPath();
            }
            super.travel(0.0F, 0.0F, 0.0F);
            return;
        }
        if (!this.world.isRemote && this.isInWater()) {
            this.moveRelative(strafe, vertical, forward, this.getAIMoveSpeed());
            this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
            this.motionX *= 0.9D;
            this.motionY *= 0.9D;
            this.motionZ *= 0.9D;
            if (this.getAttackTarget() == null) {
                this.motionY -= 0.005D;
            }
        } else {
            super.travel(strafe, vertical, forward);
        }
    }

    @Override
    public void onFindTarget(EntityItem e) {
        this.setBurrowed(false);
        this.burrowCooldown += 50;
    }

    @Override
    public boolean canTargetItem(ItemStack stack) {
        return AMTagRegistry.itemInTag(AMTagRegistry.INSECT_ITEMS, stack.getItem());
    }

    @Override
    public void onGetItem(EntityItem e) {
        this.setAttackTime(10);
        this.heal(2.0F);
    }

    private void changeWeather() {
        int time = 24000 + 1200 * this.rand.nextInt(10);
        int type = 0;
        if (!this.world.isRaining()) {
            type = this.rand.nextInt(1) + 1;
        }
        if (!this.world.isRemote) {
            if (type == 0) {
                this.world.getWorldInfo().setRaining(false);
                this.world.getWorldInfo().setThundering(false);
                this.world.getWorldInfo().setRainTime(time);
                this.world.getWorldInfo().setThunderTime(0);
            } else {
                this.world.getWorldInfo().setRainTime(time);
                this.world.getWorldInfo().setRaining(true);
                this.world.getWorldInfo().setThundering(type == 2);
                if (type == 2) {
                    this.world.getWorldInfo().setThunderTime(time);
                }
            }
        }
        weatherCooldown = time + 24000;
    }

    public void setPartying(BlockPos pos, boolean isPartying) {
        this.jukeboxPosition = pos;
        this.isJukeboxing = isPartying;
        if (isPartying && weatherCooldown == 0) {
            this.setDanceTime(240 + this.rand.nextInt(200));
        } else if (!isPartying) {
            this.setDanceTime(0);
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return this.getStanceTime() > 0 ? AMSoundRegistry.RAIN_FROG_HURT : AMSoundRegistry.RAIN_FROG_IDLE;
    }

    @Override
    public int getTalkInterval() {
        return this.getStanceTime() > 0 ? 10 : 80;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return AMSoundRegistry.RAIN_FROG_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return AMSoundRegistry.RAIN_FROG_HURT;
    }

    private class AIBurrow extends EntityAIBase {
        private BlockPos sand = null;
        private int burrowedTime = 0;

        AIBurrow() {
            this.setMutexBits(1);
        }

        @Override
        public boolean shouldExecute() {
            if (!EntityRainFrog.this.isBurrowed() && EntityRainFrog.this.burrowCooldown == 0 && EntityRainFrog.this.rand.nextInt(200) == 0) {
                this.burrowedTime = 0;
                sand = findSand();
                return sand != null;
            }
            return false;
        }

        @Override
        public boolean shouldContinueExecuting() {
            return burrowedTime < 300;
        }

        @Nullable
        private BlockPos findSand() {
            BlockPos blockpos = null;
            for (BlockPos blockpos1 : BlockPos.getAllInBox(
                    MathHelper.floor(EntityRainFrog.this.posX - 4.0D),
                    MathHelper.floor(EntityRainFrog.this.posY - 1.0D),
                    MathHelper.floor(EntityRainFrog.this.posZ - 4.0D),
                    MathHelper.floor(EntityRainFrog.this.posX + 4.0D),
                    (int) EntityRainFrog.this.getEntityBoundingBox().maxY,
                    MathHelper.floor(EntityRainFrog.this.posZ + 4.0D))) {
                if (EntityRainFrog.this.world.getBlockState(blockpos1).getBlock() instanceof BlockSand) {
                    blockpos = blockpos1;
                    break;
                }
            }
            return blockpos;
        }

        @Override
        public void updateTask() {
            if (EntityRainFrog.this.isBurrowed()) {
                burrowedTime++;
                if (!(EntityRainFrog.this.world.getBlockState(EntityRainFrog.this.getPosition().down()).getBlock() instanceof BlockSand)) {
                    EntityRainFrog.this.setBurrowed(false);
                }
            } else if (sand != null) {
                EntityRainFrog.this.getNavigator().tryMoveToXYZ(sand.getX() + 0.5F, sand.getY() + 1F, sand.getZ() + 0.5F, 1F);
                if (EntityRainFrog.this.world.getBlockState(EntityRainFrog.this.getPosition().down()).getBlock() instanceof BlockSand) {
                    EntityRainFrog.this.setBurrowed(true);
                    EntityRainFrog.this.getNavigator().clearPath();
                    sand = null;
                } else {
                    EntityRainFrog.this.setBurrowed(false);
                }
            }
        }

        @Override
        public void resetTask() {
            EntityRainFrog.this.setBurrowed(false);
            EntityRainFrog.this.burrowCooldown = 120 + EntityRainFrog.this.rand.nextInt(1200);
            this.sand = null;
        }
    }
}
