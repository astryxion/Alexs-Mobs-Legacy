package com.github.alexthe666.alexsmobs.entity;
import com.github.alexthe666.alexsmobs.misc.AMLootTables;

import com.github.alexthe666.alexsmobs.AlexsMobs;
import com.github.alexthe666.alexsmobs.config.AMConfig;
import com.github.alexthe666.alexsmobs.entity.ai.AnimalAIFleeLight;
import com.github.alexthe666.alexsmobs.entity.ai.CreatureAITargetItems;
import com.github.alexthe666.alexsmobs.item.AMItemRegistry;
import com.github.alexthe666.alexsmobs.misc.AMSoundRegistry;
import net.minecraft.init.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAvoidEntity;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIMate;
import net.minecraft.entity.ai.EntityAIPanic;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAITempt;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.IShearable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;
import com.google.common.base.Optional;
import java.util.Random;
import java.util.UUID;

public class EntityCockroach extends EntityAnimal implements IShearable, ITargetsDroppedItems {

    public static final ResourceLocation MARACA_LOOT = new ResourceLocation("alexsmobs", "entities/cockroach_maracas");
    public static final ResourceLocation MARACA_HEADLESS_LOOT = new ResourceLocation("alexsmobs", "entities/cockroach_maracas_headless");
    private static final float NORMAL_WIDTH = 0.5F;
    private static final float NORMAL_HEIGHT = 0.4F;
    private static final float STAND_WIDTH = 0.7F;
    private static final float STAND_HEIGHT = 0.9F;
    private static final DataParameter<Boolean> DANCING = EntityDataManager.createKey(EntityCockroach.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> HEADLESS = EntityDataManager.createKey(EntityCockroach.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> MARACAS = EntityDataManager.createKey(EntityCockroach.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Optional<UUID>> NEAREST_MUSICIAN = EntityDataManager.createKey(EntityCockroach.class, DataSerializers.OPTIONAL_UNIQUE_ID);
    private static final DataParameter<Boolean> RAINBOW = EntityDataManager.createKey(EntityCockroach.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> BREADED = EntityDataManager.createKey(EntityCockroach.class, DataSerializers.BOOLEAN);
    public int randomWingFlapTick = 0;
    public float prevDanceProgress;
    public float danceProgress;
    private boolean prevStand = false;
    private boolean isJukeboxing;
    private BlockPos jukeboxPosition;
    private int laCucarachaTimer = 0;
    public int timeUntilNextEgg = this.rand.nextInt(24000) + 24000;

    public EntityCockroach(World world) {
        super(world);
        this.setSize(NORMAL_WIDTH, NORMAL_HEIGHT);
    }

    public static boolean isValidLightLevel(World world, BlockPos pos, Random random) {
        if (world.getLightFor(EnumSkyBlock.SKY, pos) > random.nextInt(32)) {
            return false;
        } else {
            int light = world.getLight(pos, world.isThundering());
            return light <= random.nextInt(8);
        }
    }

    public static boolean canCockroachSpawn(World world, BlockPos pos, Random random) {
        return !world.canSeeSky(pos) && pos.getY() <= 64 && isValidLightLevel(world, pos, random)
                && net.minecraft.entity.EntityLiving.SpawnPlacementType.ON_GROUND.canSpawnAt(world, pos);
    }

    @Override
    public boolean getCanSpawnHere() {
        return AMEntityRegistry.rollSpawn(AMConfig.cockroachSpawnRolls, this.getRNG(), AMEntityRegistry.AMSpawnReason.OTHER)
                && canCockroachSpawn(this.world, this.getPosition(), this.getRNG());
    }

    @Override
    protected boolean canDespawn() {
        return !preventDespawn() && super.canDespawn();
    }

    public boolean preventDespawn() {
        return this.isBreaded() || this.isRainbow() || this.isDancing() || this.hasMaracas() || this.isHeadless();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return AMSoundRegistry.COCKROACH_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return AMSoundRegistry.COCKROACH_HURT;
    }

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(1, new EntityAIPanic(this, 1.1D));
        this.tasks.addTask(2, new EntityAIMate(this, 1.0D));
        this.tasks.addTask(3, new EntityAITempt(this, 1.0D, false, com.google.common.collect.Sets.newHashSet(AMItemRegistry.MARACA, Items.SUGAR)));
        this.tasks.addTask(4, new EntityAIAvoidEntity(this, EntityCentipedeHead.class, 16.0F, 1.3D, 1.0D));
        this.tasks.addTask(4, new EntityAIAvoidEntity(this, EntityPlayer.class, 8.0F, 1.3D, 1.0D) {
            @Override
            public boolean shouldExecute() {
                return !EntityCockroach.this.isBreaded() && super.shouldExecute();
            }
        });
        this.tasks.addTask(5, new AnimalAIFleeLight(this, 1.0D) {
            @Override
            public boolean shouldExecute() {
                return !EntityCockroach.this.isBreaded() && super.shouldExecute();
            }
        });
        this.tasks.addTask(6, new EntityAIWander(this, 1.0D, 80));
        this.tasks.addTask(7, new EntityAIWatchClosest(this, EntityPlayer.class, 6.0F));
        this.tasks.addTask(8, new EntityAILookIdle(this));
        this.targetTasks.addTask(1, new CreatureAITargetItems(this, false));
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        boolean prev = super.attackEntityFrom(source, amount);
        if (prev) {
            randomWingFlapTick = 5 + rand.nextInt(15);
            if (this.getHealth() <= 1.0F && amount > 0 && !this.isHeadless() && this.getRNG().nextInt(3) == 0) {
                this.setHeadless(true);
                if (!world.isRemote) {
                    for (int i = 0; i < 3; i++) {
                        ((WorldServer) this.world).spawnParticle(EnumParticleTypes.SMOKE_NORMAL, this.posX + (this.rand.nextDouble() - 0.5D) * 0.52F, this.posY + this.height, this.posZ + (this.rand.nextDouble() - 0.5D) * 0.52F, 1, 0.0D, 0.0D, 0.0D, 0.0D);
                    }
                }
            }
        }
        return prev;
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        return stack.getItem() == Items.SUGAR;
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setBoolean("Maracas", this.hasMaracas());
        compound.setBoolean("Rainbow", this.isRainbow());
        compound.setBoolean("Dancing", this.isDancing());
        compound.setBoolean("Breaded", this.isBreaded());
        compound.setInteger("EggTime", this.timeUntilNextEgg);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.setMaracas(compound.getBoolean("Maracas"));
        this.setRainbow(compound.getBoolean("Rainbow"));
        this.setDancing(compound.getBoolean("Dancing"));
        this.setBreaded(compound.getBoolean("Breaded"));
        if (compound.hasKey("EggTime")) {
            this.timeUntilNextEgg = compound.getInteger("EggTime");
        }
        this.updateCockroachSize();
    }

    @Nullable
    @Override
    protected ResourceLocation getLootTable() {
        return this.hasMaracas() ? this.isHeadless() ? AMLootTables.COCKROACH_MARACAS_HEADLESS : AMLootTables.COCKROACH_MARACAS : AMLootTables.COCKROACH;
    }

    public float getBlockPathWeight(BlockPos pos, net.minecraft.world.IBlockAccess worldIn) {
        return 0.5F - (float) worldIn.getCombinedLight(pos, 0) / 16.0F;
    }

    @Override
    public EnumCreatureAttribute getCreatureAttribute() {
        return EnumCreatureAttribute.ARTHROPOD;
    }

    public float getRenderScale() {
        return this.isChild() ? 0.5F : 1.0F;
    }

    private void updateCockroachSize() {
        float sc = this.getRenderScale();
        if (isDancing() || danceProgress > 0) {
            this.setSize(STAND_WIDTH * sc, STAND_HEIGHT * sc);
        } else {
            this.setSize(NORMAL_WIDTH * sc, NORMAL_HEIGHT * sc);
        }
    }

    @Override
    public boolean isEntityInvulnerable(DamageSource source) {
        return source == DamageSource.FALL || source == DamageSource.DROWN || source == DamageSource.IN_WALL || source == DamageSource.FALLING_BLOCK || super.isEntityInvulnerable(source);
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (stack.getItem() == net.minecraft.item.Item.getItemFromBlock(net.minecraft.init.Blocks.SPONGE) && this.isEntityAlive() && this.isRainbow()) {
            this.setRainbow(false);
            for (int i = 0; i < 6 + rand.nextInt(3); i++) {
                double d2 = this.rand.nextGaussian() * 0.02D;
                double d0 = this.rand.nextGaussian() * 0.02D;
                double d1 = this.rand.nextGaussian() * 0.02D;
                this.world.spawnParticle(EnumParticleTypes.ITEM_CRACK, this.posX + (double) (this.rand.nextFloat() * this.width) - (double) this.width * 0.5F, this.posY + this.height * 0.5F + (double) (this.rand.nextFloat() * this.height * 0.5F), this.posZ + (double) (this.rand.nextFloat() * this.width) - (double) this.width * 0.5F, d0, d1, d2, Item.getIdFromItem(AMItemRegistry.MIMICREAM));
            }
            return true;
        } else if (stack.getItem() == AMItemRegistry.MIMICREAM && this.isEntityAlive() && !this.isRainbow()) {
            this.setRainbow(true);
            for (int i = 0; i < 6 + rand.nextInt(3); i++) {
                double d2 = this.rand.nextGaussian() * 0.02D;
                double d0 = this.rand.nextGaussian() * 0.02D;
                double d1 = this.rand.nextGaussian() * 0.02D;
                this.world.spawnParticle(EnumParticleTypes.ITEM_CRACK, this.posX + (double) (this.rand.nextFloat() * this.width) - (double) this.width * 0.5F, this.posY + this.height * 0.5F + (double) (this.rand.nextFloat() * this.height * 0.5F), this.posZ + (double) (this.rand.nextFloat() * this.width) - (double) this.width * 0.5F, d0, d1, d2, Item.getIdFromItem(stack.getItem()), stack.getMetadata());
            }
            stack.shrink(1);
            return !this.world.isRemote;
        } else if (stack.getItem() == AMItemRegistry.MARACA && this.isEntityAlive() && !this.hasMaracas()) {
            this.setMaracas(true);
            if (!player.capabilities.isCreativeMode) {
                stack.shrink(1);
            }
            return true;
        } else if (stack.getItem() != AMItemRegistry.MARACA && this.isEntityAlive() && this.hasMaracas()) {
            this.setMaracas(false);
            this.setDancing(false);
            if (!this.world.isRemote) {
                this.entityDropItem(new ItemStack(AMItemRegistry.MARACA), 0.0F);
            }
            return true;
        } else {
            return super.processInteract(player, hand);
        }
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(DANCING, Boolean.FALSE);
        this.dataManager.register(HEADLESS, Boolean.FALSE);
        this.dataManager.register(MARACAS, Boolean.FALSE);
        this.dataManager.register(NEAREST_MUSICIAN, Optional.absent());
        this.dataManager.register(RAINBOW, Boolean.FALSE);
        this.dataManager.register(BREADED, Boolean.FALSE);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(6.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.35D);
    }

    public boolean isDancing() {
        return this.dataManager.get(DANCING);
    }

    public void setDancing(boolean dancing) {
        this.dataManager.set(DANCING, dancing);
    }

    public boolean isHeadless() {
        return this.dataManager.get(HEADLESS);
    }

    public void setHeadless(boolean head) {
        this.dataManager.set(HEADLESS, head);
    }

    public boolean hasMaracas() {
        return this.dataManager.get(MARACAS);
    }

    public void setMaracas(boolean head) {
        this.dataManager.set(MARACAS, head);
    }

    public boolean isBreaded() {
        return this.dataManager.get(BREADED);
    }

    public void setBreaded(boolean breaded) {
        this.dataManager.set(BREADED, breaded);
    }

    @Nullable
    public UUID getNearestMusicianId() {
        return this.dataManager.get(NEAREST_MUSICIAN).orNull();
    }

    public boolean isRainbow() {
        return this.dataManager.get(RAINBOW);
    }

    public void setRainbow(boolean head) {
        this.dataManager.set(RAINBOW, head);
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        prevDanceProgress = danceProgress;
        boolean dance = this.isJukeboxing || isDancing();
        if (this.jukeboxPosition == null || this.jukeboxPosition.distanceSq(this.posX, this.posY, this.posZ) > 3.46D * 3.46D || this.world.getBlockState(this.jukeboxPosition).getBlock() != Blocks.JUKEBOX) {
            this.isJukeboxing = false;
            this.jukeboxPosition = null;
        }
        if (this.getEyeHeight() > this.height) {
            this.updateCockroachSize();
        }
        if (dance && danceProgress < 5F) {
            danceProgress++;
        }
        if (!dance && danceProgress > 0F) {
            danceProgress--;
        }
        if (!this.onGround || rand.nextInt(200) == 0) {
            randomWingFlapTick = 5 + rand.nextInt(15);
        }
        if (randomWingFlapTick > 0) {
            randomWingFlapTick--;
        }
        if (prevStand != dance) {
            if (hasMaracas()) {
                tellOthersImPlayingLaCucaracha();
            }
            this.updateCockroachSize();
        }
        if (!hasMaracas()) {
            Entity musician = this.getNearestMusician();
            if (musician != null) {
                if (!musician.isEntityAlive() || this.getDistance(musician) > 10 || musician instanceof EntityCockroach && !((EntityCockroach) musician).hasMaracas()) {
                    this.setNearestMusician(null);
                    this.setDancing(false);
                } else {
                    this.setDancing(true);
                }
            }
        }
        if (hasMaracas()) {
            laCucarachaTimer++;
            if (laCucarachaTimer % 20 == 0 && rand.nextFloat() < 0.3F) {
                tellOthersImPlayingLaCucaracha();
            }
            this.setDancing(true);
            if (!this.isSilent()) {
                this.world.setEntityState(this, (byte) 67);
            }
        } else {
            laCucarachaTimer = 0;
        }
        if (!this.world.isRemote && this.isEntityAlive() && !this.isChild() && --this.timeUntilNextEgg <= 0) {
            EntityItem dropped = this.entityDropItem(new ItemStack(AMItemRegistry.COCKROACH_OOTHECA), 0.0F);
            dropped.setDefaultPickupDelay();
            this.timeUntilNextEgg = this.rand.nextInt(24000) + 24000;
        }
        prevStand = dance;
    }

    private void tellOthersImPlayingLaCucaracha() {
        List<EntityCockroach> list = this.world.getEntitiesWithinAABB(EntityCockroach.class, this.getMusicianDistance());
        for (EntityCockroach roach : list) {
            if (!roach.hasMaracas()) {
                roach.setNearestMusician(this.getUniqueID());
            }
        }
    }

    private AxisAlignedBB getMusicianDistance() {
        return this.getEntityBoundingBox().grow(10, 10, 10);
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

    public Entity getNearestMusician() {
        UUID id = getNearestMusicianId();
        if (id != null && !world.isRemote) {
            return ((WorldServer) world).getEntityFromUuid(id);
        }
        return null;
    }

    public void setNearestMusician(@Nullable UUID uniqueId) {
        this.dataManager.set(NEAREST_MUSICIAN, Optional.fromNullable(uniqueId));
    }

    @SideOnly(Side.CLIENT)
    public void setPartying(BlockPos pos, boolean isPartying) {
        this.jukeboxPosition = pos;
        this.isJukeboxing = isPartying;
    }

    @Nullable
    @Override
    public EntityCockroach createChild(EntityAgeable ageable) {
        EntityCockroach roach = new EntityCockroach(this.world);
        roach.setBreaded(true);
        return roach;
    }

    public boolean isShearable() {
        return this.isEntityAlive() && !this.isChild() && !isHeadless();
    }

    @Override
    public boolean isShearable(ItemStack item, net.minecraft.world.IBlockAccess world, BlockPos pos) {
        return isShearable();
    }

    @Override
    public java.util.List<ItemStack> onSheared(ItemStack item, net.minecraft.world.IBlockAccess world, BlockPos pos, int fortune) {
        this.world.playSound(null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_SHEEP_SHEAR, SoundCategory.PLAYERS, 1.0F, 1.0F);
        this.attackEntityFrom(DamageSource.GENERIC, 0F);
        if (!this.world.isRemote) {
            for (int i = 0; i < 3; i++) {
                ((WorldServer) this.world).spawnParticle(EnumParticleTypes.SMOKE_NORMAL, this.posX + (this.rand.nextDouble() - 0.5D) * 0.52F, this.posY + this.height, this.posZ + (this.rand.nextDouble() - 0.5D) * 0.52F, 1, 0.0D, 0.0D, 0.0D, 0.0D);
            }
        }
        this.setHeadless(true);
        java.util.List<ItemStack> drops = new java.util.ArrayList<ItemStack>();
        drops.add(new ItemStack(AMItemRegistry.COCKROACH_WING));
        if (this.rand.nextBoolean()) {
            drops.add(new ItemStack(AMItemRegistry.COCKROACH_WING));
        }
        return drops;
    }

    @Override
    public boolean canTargetItem(ItemStack stack) {
        return stack.getItem() instanceof ItemFood || stack.getItem() == Items.SUGAR;
    }

    @Override
    public void travel(float strafe, float vertical, float forward) {
        if (this.isDancing() || danceProgress > 0) {
            if (this.getNavigator().getPath() != null) {
                this.getNavigator().clearPath();
            }
            return;
        }
        super.travel(strafe, vertical, forward);
    }

    @Override
    public void onGetItem(EntityItem e) {
        if (e.getItem().getItem() == AMItemRegistry.MARACA) {
            this.setMaracas(true);
        } else {
            if (e.getItem().getItem().hasContainerItem()) {
                this.entityDropItem(new ItemStack(e.getItem().getItem().getContainerItem()), 0.0F);
            }
            this.heal(5);
            if (e.getItem().getItem() == Items.BREAD || e.getItem().getItem() == Items.SUGAR) {
                this.setBreaded(true);
            }
        }
    }
}
