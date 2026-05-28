package com.github.alexthe666.alexsmobs.entity;

import com.github.alexthe666.alexsmobs.config.AMConfig;
import com.github.alexthe666.alexsmobs.entity.ai.*;
import com.github.alexthe666.alexsmobs.entity.util.TerrapinTypes;
import com.github.alexthe666.alexsmobs.item.AMItemRegistry;
import com.github.alexthe666.alexsmobs.misc.AMSoundRegistry;
import com.github.alexthe666.alexsmobs.misc.AMTagRegistry;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.stats.StatList;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public class EntityTerrapin extends EntityAnimal implements ISemiAquatic {

    private static final DataParameter<Integer> TURTLE_TYPE = EntityDataManager.createKey(EntityTerrapin.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> SHELL_TYPE = EntityDataManager.createKey(EntityTerrapin.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> SKIN_TYPE = EntityDataManager.createKey(EntityTerrapin.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> TURTLE_COLOR = EntityDataManager.createKey(EntityTerrapin.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> SHELL_COLOR = EntityDataManager.createKey(EntityTerrapin.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> SKIN_COLOR = EntityDataManager.createKey(EntityTerrapin.class, DataSerializers.VARINT);
    private static final DataParameter<Boolean> RETREATED = EntityDataManager.createKey(EntityTerrapin.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> SPINNING = EntityDataManager.createKey(EntityTerrapin.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> FROM_BUCKET = EntityDataManager.createKey(EntityTerrapin.class, DataSerializers.BOOLEAN);

    public float clientSpin;
    public int spinCounter;
    public float prevSwimProgress;
    public float swimProgress;
    public float prevRetreatProgress;
    public float retreatProgress;
    public float prevSpinProgress;
    public float spinProgress;

    private int maxRollTime = 50;
    private boolean isLandNavigator;
    private int swimTimer = -1000;
    private int hideInShellTimer;
    private Vec3d spinDelta;
    private float spinYRot;
    private int changeSpinAngleCooldown;
    private EntityLivingBase lastLauncher;

    public EntityTerrapin(World world) {
        super(world);
        this.setSize(0.65F, 0.45F);
        this.setPathPriority(PathNodeType.WATER, 0.0F);
        switchNavigator(true);
    }

    public static boolean canTerrapinSpawn(World world, BlockPos pos) {
        return world.getBlockState(pos).getMaterial() == Material.WATER;
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        AMEntityRegistry.registerAttributeIfAbsent(this, SharedMonsterAttributes.ARMOR);
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(10.0D);
        this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(10.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.1D);
    }

    @Override
    public boolean getCanSpawnHere() {
        return AMEntityRegistry.rollSpawn(AMConfig.terrapinSpawnRolls, this.getRNG(), AMEntityRegistry.AMSpawnReason.OTHER) && super.getCanSpawnHere();
    }

    @Override
    public int getMaxSpawnedInChunk() {
        return 2;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return AMSoundRegistry.TERRAPIN_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return AMSoundRegistry.TERRAPIN_HURT;
    }

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(0, new BreatheAirGoal(this));
        this.tasks.addTask(1, new MateGoal(this, 1.0D));
        this.tasks.addTask(2, new EntityAITempt(this, 1.1D, Items.FISH, false) {
            @Override
            protected boolean isTempting(ItemStack stack) {
                return AMTagRegistry.itemInTag(AMTagRegistry.TERRAPIN_BREEDABLES, stack.getItem());
            }
        });
        this.tasks.addTask(3, new AnimalAIFindWater(this));
        this.tasks.addTask(3, new AnimalAILeaveWater(this));
        this.tasks.addTask(4, new SemiAquaticAIRandomSwimming(this, 1.0D, 30));
        this.tasks.addTask(6, new EntityAIPanic(this, 1.1D));
        this.tasks.addTask(7, new EntityAIWander(this, 1.0D, 60));
        this.tasks.addTask(8, new EntityAILookIdle(this));
        this.tasks.addTask(8, new EntityAIWatchClosest(this, EntityPlayer.class, 6.0F));
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        this.prevSwimProgress = this.swimProgress;
        this.prevRetreatProgress = this.retreatProgress;
        this.prevSpinProgress = this.spinProgress;

        final boolean inWater = this.isInWater();
        final boolean spinning = this.isSpinning();
        final boolean retreated = this.hasRetreated();

        if (inWater) {
            if (this.swimProgress < 5F) {
                this.swimProgress++;
            }
        } else if (this.swimProgress > 0F) {
            this.swimProgress--;
        }
        if (spinning) {
            if (this.spinProgress < 5F) {
                this.spinProgress++;
            }
        } else if (this.spinProgress > 0F) {
            this.spinProgress--;
        }
        if (retreated) {
            if (this.retreatProgress < 5F) {
                this.retreatProgress++;
            }
        } else if (this.retreatProgress > 0F) {
            this.retreatProgress--;
        }

        if (spinning) {
            this.handleSpin();
            if (this.isEntityAlive() && this.spinCounter > 5 && !this.isChild()) {
                for (Entity entity : this.world.getEntitiesWithinAABB(EntityLivingBase.class, this.getEntityBoundingBox().grow(0.3F))) {
                    if (!this.isOnSameTeam(entity) && !(entity instanceof EntityTerrapin)) {
                        entity.attackEntityFrom(DamageSource.causeMobDamage(this.lastLauncher == null ? this : this.lastLauncher), 4.0F + this.rand.nextFloat() * 4.0F);
                    }
                }
            }
            if (!this.isEntityAlive()) {
                this.setSpinning(false);
            }
            if (this.collidedHorizontally) {
                if (this.changeSpinAngleCooldown == 0) {
                    this.changeSpinAngleCooldown = 10;
                    float f = this.spinYRot - 180;
                    f += this.rand.nextInt(40) - 20;
                    this.rotationYaw = f;
                    this.copySpinDelta(f, Vec3d.ZERO);
                } else {
                    this.maxRollTime -= 30;
                }
            }
            if (this.changeSpinAngleCooldown > 0) {
                this.changeSpinAngleCooldown--;
            }
        }

        if (!this.world.isRemote) {
            if (this.isInWater() && this.isLandNavigator) {
                switchNavigator(false);
            }
            if (!this.isInWater() && !this.isLandNavigator) {
                switchNavigator(true);
            }
            if (this.isInWater()) {
                this.swimTimer = Math.max(0, this.swimTimer + 1);
            } else {
                this.swimTimer = Math.min(0, this.swimTimer - 1);
                List<EntityPlayer> list = this.world.getEntitiesWithinAABB(EntityPlayer.class, this.getEntityBoundingBox().grow(0, 0.15F, 0));
                for (EntityPlayer player : list) {
                    if ((player.motionY > 0 || !player.onGround) && player.posY > this.posY + this.getEyeHeight()) {
                        if (!this.hasRetreated()) {
                            this.hideInShellTimer += 40 + this.rand.nextInt(40);
                        } else if (!this.isSpinning()) {
                            this.lastLauncher = player;
                            int spin = 100 + this.rand.nextInt(100);
                            this.hideInShellTimer = spin;
                            this.rotationYaw = player.rotationYawHead;
                            this.spinFor(spin);
                        }
                    }
                }
            }
            if (this.hideInShellTimer > 0) {
                this.hideInShellTimer--;
            }
            this.setRetreated(this.hideInShellTimer > 0 && !this.isSpinning());
        }
    }

    private void switchNavigator(boolean onLand) {
        if (onLand) {
            this.moveHelper = new EntityMoveHelper(this);
            this.navigator = new GroundPathNavigatorWide(this, this.world);
            this.isLandNavigator = true;
        } else {
            this.moveHelper = new AnimalSwimMoveControllerSink(this, 2.5F, 1.15F);
            this.navigator = new SemiAquaticPathNavigator(this, this.world);
            this.isLandNavigator = false;
        }
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(TURTLE_TYPE, 0);
        this.dataManager.register(SHELL_TYPE, 0);
        this.dataManager.register(SKIN_TYPE, 0);
        this.dataManager.register(SHELL_COLOR, 0);
        this.dataManager.register(SKIN_COLOR, 0);
        this.dataManager.register(TURTLE_COLOR, 0);
        this.dataManager.register(RETREATED, false);
        this.dataManager.register(SPINNING, false);
        this.dataManager.register(FROM_BUCKET, false);
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setInteger("TurtleType", this.getTurtleTypeOrdinal());
        compound.setInteger("ShellType", this.getShellType());
        compound.setInteger("SkinType", this.getSkinType());
        compound.setInteger("TurtleColor", this.getTurtleColor());
        compound.setInteger("ShellColor", this.getShellColor());
        compound.setInteger("SkinColor", this.getSkinColor());
        compound.setBoolean("Bucketed", this.fromBucket());
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.setTurtleTypeOrdinal(compound.getInteger("TurtleType"));
        this.setShellType(compound.getInteger("ShellType"));
        this.setSkinType(compound.getInteger("SkinType"));
        this.setTurtleColor(compound.getInteger("TurtleColor"));
        this.setShellColor(compound.getInteger("ShellColor"));
        this.setSkinColor(compound.getInteger("SkinColor"));
        this.setFromBucket(compound.getBoolean("Bucketed"));
    }

    @Override
    protected void playStepSound(BlockPos pos, net.minecraft.block.Block blockIn) {
        if (!this.isSpinning()) {
            super.playStepSound(pos, blockIn);
        }
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        return AMTagRegistry.itemInTag(AMTagRegistry.TERRAPIN_BREEDABLES, stack.getItem());
    }

    public boolean fromBucket() {
        return this.dataManager.get(FROM_BUCKET);
    }

    public void setFromBucket(boolean fromBucket) {
        this.dataManager.set(FROM_BUCKET, fromBucket);
    }

    @Override
    protected boolean canDespawn() {
        return !this.fromBucket() && !this.hasCustomName();
    }

    private int getTurtleTypeOrdinal() {
        return MathHelper.clamp(this.dataManager.get(TURTLE_TYPE), 0, TerrapinTypes.values().length - 1);
    }

    private void setTurtleTypeOrdinal(int i) {
        this.dataManager.set(TURTLE_TYPE, i);
    }

    public int getShellType() {
        return this.dataManager.get(SHELL_TYPE);
    }

    public void setShellType(int i) {
        this.dataManager.set(SHELL_TYPE, i);
    }

    public int getSkinType() {
        return this.dataManager.get(SKIN_TYPE);
    }

    public void setSkinType(int i) {
        this.dataManager.set(SKIN_TYPE, i);
    }

    public int getShellColor() {
        return this.dataManager.get(SHELL_COLOR);
    }

    public void setShellColor(int i) {
        this.dataManager.set(SHELL_COLOR, i);
    }

    public int getSkinColor() {
        return this.dataManager.get(SKIN_COLOR);
    }

    public void setSkinColor(int i) {
        this.dataManager.set(SKIN_COLOR, i);
    }

    public int getTurtleColor() {
        return this.dataManager.get(TURTLE_COLOR);
    }

    public void setTurtleColor(int i) {
        this.dataManager.set(TURTLE_COLOR, i);
    }

    public TerrapinTypes getTurtleType() {
        return TerrapinTypes.values()[this.getTurtleTypeOrdinal()];
    }

    public void setTurtleType(TerrapinTypes type) {
        this.setTurtleTypeOrdinal(type.ordinal());
    }

    public boolean isSpinning() {
        return this.dataManager.get(SPINNING);
    }

    public void setSpinning(boolean b) {
        this.dataManager.set(SPINNING, b);
    }

    public boolean hasRetreated() {
        return this.dataManager.get(RETREATED);
    }

    public void setRetreated(boolean b) {
        this.dataManager.set(RETREATED, b);
    }

    @Override
    public void applyEntityCollision(Entity entity) {
        if (this.isInWater() || entity instanceof EntityTerrapin) {
            super.applyEntityCollision(entity);
        } else {
            entity.addVelocity(this.motionX, this.motionY, this.motionZ);
        }
    }

    @Override
    public boolean canBeCollidedWith() {
        return this.isInWater() ? super.canBeCollidedWith() : this.isEntityAlive();
    }

    private void spinFor(int time) {
        this.maxRollTime = time;
        this.setSpinning(true);
    }

    private void copySpinDelta(float spinRot, Vec3d motionIn) {
        final float f = spinRot * 0.017453292F;
        final float f1 = this.isChild() ? 0.3F : 0.5F;
        this.spinYRot = spinRot;
        this.spinDelta = new Vec3d(motionIn.x + -MathHelper.sin(f) * f1, 0.0D, motionIn.z + MathHelper.cos(f) * f1);
        this.motionX = this.spinDelta.x;
        this.motionZ = this.spinDelta.z;
    }

    private void handleSpin() {
        this.setRetreated(true);
        ++this.spinCounter;
        if (!this.world.isRemote) {
            if (this.spinCounter > this.maxRollTime) {
                this.setSpinning(false);
                this.hideInShellTimer = 10 + this.rand.nextInt(30);
                this.spinCounter = 0;
            } else {
                if (this.spinCounter == 1) {
                    this.copySpinDelta(this.rotationYaw, new Vec3d(this.motionX, this.motionY, this.motionZ));
                } else {
                    this.rotationYaw = this.spinYRot;
                    this.rotationYawHead = this.spinYRot;
                    this.renderYawOffset = this.spinYRot;
                    this.motionX = this.spinDelta.x;
                    this.motionZ = this.spinDelta.z;
                }
            }
        }
    }

    @Override
    @Nullable
    public net.minecraft.entity.IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable net.minecraft.entity.IEntityLivingData livingdata) {
        this.setAir(300);
        this.setTurtleType(TerrapinTypes.getRandomType(this.rand));
        this.setShellType(this.rand.nextInt(7));
        this.setSkinType(this.rand.nextInt(4));
        this.setTurtleColor(TerrapinTypes.generateRandomColor(this.rand));
        this.setShellColor(TerrapinTypes.generateRandomColor(this.rand));
        this.setSkinColor(TerrapinTypes.generateRandomColor(this.rand));
        return super.onInitialSpawn(difficulty, livingdata);
    }

    @Override
    public EntityAgeable createChild(EntityAgeable ageable) {
        return (EntityTerrapin) AMEntityRegistry.TERRAPIN.newInstance(this.world);
    }

    @Override
    public boolean shouldStopMoving() {
        return this.isSpinning() || this.hasRetreated();
    }

    @Override
    public boolean shouldEnterWater() {
        return this.getAttackTarget() == null && !this.shouldLeaveWater() && this.swimTimer <= -1000;
    }

    @Override
    public boolean shouldLeaveWater() {
        return this.swimTimer > 600;
    }

    @Override
    public int getWaterSearchRange() {
        return 10;
    }

    public boolean isPushedByWater() {
        return false;
    }

    @Override
    public void travel(float strafe, float vertical, float forward) {
        if (this.shouldStopMoving()) {
            if (this.getNavigator().getPath() != null) {
                this.getNavigator().clearPath();
            }
            strafe = 0;
            vertical = 0;
            forward = 0;
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

    protected ItemStack getFishBucket() {
        ItemStack stack = new ItemStack(AMItemRegistry.TERRAPIN_BUCKET);
        if (this.hasCustomName()) {
            stack.setStackDisplayName(this.getCustomNameTag());
        }
        return stack;
    }

    protected void setBucketData(ItemStack bucket) {
        if (this.hasCustomName()) {
            bucket.setStackDisplayName(this.getCustomNameTag());
        }
        NBTTagCompound platTag = new NBTTagCompound();
        this.writeEntityToNBT(platTag);
        if (!bucket.hasTagCompound()) {
            bucket.setTagCompound(new NBTTagCompound());
        }
        bucket.getTagCompound().setTag("TerrapinData", platTag);
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        ItemStack itemstack = player.getHeldItem(hand);
        if (AMTagRegistry.itemInTag(AMTagRegistry.TERRAPIN_BREEDABLES, itemstack.getItem())) {
            this.enablePersistence();
        }
        ItemStack bucket = this.getFishBucket();
        if (itemstack.getItem() == Items.WATER_BUCKET && this.isEntityAlive()) {
            this.playSound(SoundEvents.ITEM_BUCKET_FILL, 1.0F, 1.0F);
            if (!player.capabilities.isCreativeMode) {
                itemstack.shrink(1);
            }
            this.setBucketData(bucket);
            if (!this.world.isRemote) {
                this.setDead();
            }
            if (itemstack.isEmpty()) {
                player.setHeldItem(hand, bucket);
            } else if (!player.inventory.addItemStackToInventory(bucket)) {
                player.dropItem(bucket, false);
            }
            return true;
        }
        return super.processInteract(player, hand);
    }

    public boolean isKoopa() {
        String s = TextFormatting.getTextWithoutFormattingCodes(this.getName());
        return s != null && s.toLowerCase().contains("koopa");
    }

    static class ParentData {
        public final TerrapinTypes type;
        public final int shellType;
        public final int skinType;
        public final int turtleColor;
        public final int shellColor;
        public final int skinColor;

        public ParentData(TerrapinTypes type, int shellType, int skinType, int turtleColor, int shellColor, int skinColor) {
            this.type = type;
            this.shellType = shellType;
            this.skinType = skinType;
            this.turtleColor = turtleColor;
            this.shellColor = shellColor;
            this.skinColor = skinColor;
        }

        public static ParentData from(EntityTerrapin terrapin) {
            return new ParentData(terrapin.getTurtleType(), terrapin.getShellType(), terrapin.getSkinType(), terrapin.getTurtleColor(), terrapin.getShellColor(), terrapin.getSkinColor());
        }

        public static void addAttributesToOffspring(EntityTerrapin baby, ParentData parent1, ParentData parent2, Random random) {
            baby.setTurtleType(random.nextBoolean() ? parent1.type : parent2.type);
            baby.setShellType(random.nextBoolean() ? parent1.shellType : parent2.shellType);
            baby.setSkinType(random.nextBoolean() ? parent1.skinType : parent2.skinType);
            baby.setTurtleColor((parent1.turtleColor + parent2.turtleColor) / 2);
            baby.setShellColor((parent1.shellColor + parent2.shellColor) / 2);
            baby.setSkinColor((parent1.skinColor + parent2.skinColor) / 2);
            if (random.nextFloat() < 0.15F) {
                baby.setTurtleType(TerrapinTypes.OVERLAY);
                switch (random.nextInt(3)) {
                    case 0:
                        baby.setTurtleColor((int) (0xFFFFFF * random.nextFloat()));
                        break;
                    case 1:
                        baby.setShellColor((int) (0xFFFFFF * random.nextFloat()));
                        break;
                    case 2:
                        baby.setSkinColor((int) (0xFFFFFF * random.nextFloat()));
                        break;
                    default:
                        break;
                }
            }
        }
    }

    static class MateGoal extends AnimalAIMate {
        private final EntityTerrapin turtle;

        MateGoal(EntityTerrapin turtle, double speedIn) {
            super(turtle, speedIn);
            this.turtle = turtle;
        }

        @Override
        protected void spawnBaby() {
            EntityPlayerMP breeder = this.getAnimal().getLoveCause();
            EntityAnimal partner = this.getTargetMate();
            if (breeder == null && partner != null && partner.getLoveCause() != null) {
                breeder = partner.getLoveCause();
            }
            EntityAgeable child = this.turtle.createChild(partner);
            if (breeder != null) {
                breeder.addStat(StatList.ANIMALS_BRED);
                CriteriaTriggers.BRED_ANIMALS.trigger(breeder, this.getAnimal(), partner, child);
            }
            if (child instanceof EntityTerrapin) {
                EntityTerrapin baby = (EntityTerrapin) child;
                ParentData parent1 = ParentData.from(this.turtle);
                ParentData parent2 = partner instanceof EntityTerrapin ? ParentData.from((EntityTerrapin) partner) : parent1;
                ParentData.addAttributesToOffspring(baby, parent1, parent2, this.turtle.getRNG());
            }
            if (child != null) {
                child.setGrowingAge(-24000);
                child.setLocationAndAngles(this.getAnimal().posX, this.getAnimal().posY, this.getAnimal().posZ, 0.0F, 0.0F);
                this.world.spawnEntity(child);
            }
            this.getAnimal().setGrowingAge(6000);
            if (partner != null) {
                partner.setGrowingAge(6000);
            }
            this.getAnimal().resetInLove();
            if (partner != null) {
                partner.resetInLove();
            }
            this.resetTask();
            Random random = this.getAnimal().getRNG();
            if (this.world.getGameRules().getBoolean("doMobLoot")) {
                this.world.spawnEntity(new EntityXPOrb(this.world, this.getAnimal().posX, this.getAnimal().posY, this.getAnimal().posZ, random.nextInt(7) + 1));
            }
        }
    }
}
