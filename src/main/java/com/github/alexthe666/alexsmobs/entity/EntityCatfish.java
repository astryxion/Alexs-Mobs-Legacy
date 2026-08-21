package com.github.alexthe666.alexsmobs.entity;
import com.github.alexthe666.alexsmobs.misc.AMLootTables;

import com.github.alexthe666.alexsmobs.config.AMConfig;
import com.github.alexthe666.alexsmobs.entity.ai.AnimalAISwimBottom;
import com.github.alexthe666.alexsmobs.entity.ai.AquaticMoveController;
import com.github.alexthe666.alexsmobs.entity.ai.FlyingEntityAITempt;
import com.github.alexthe666.alexsmobs.item.AMItemRegistry;
import com.github.alexthe666.alexsmobs.config.BiomeConfig;
import com.github.alexthe666.alexsmobs.misc.AMTagRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAIPanic;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathNavigateSwimmer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;

public class EntityCatfish extends EntityCreature {

    private static final DataParameter<Boolean> FROM_BUCKET = EntityDataManager.createKey(EntityCatfish.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> CATFISH_SIZE = EntityDataManager.createKey(EntityCatfish.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> SPIT_TIME = EntityDataManager.createKey(EntityCatfish.class, DataSerializers.VARINT);
    private static final DataParameter<Boolean> HAS_SWALLOWED_ENTITY = EntityDataManager.createKey(EntityCatfish.class, DataSerializers.BOOLEAN);
    private static final DataParameter<String> SWALLOWED_ENTITY_TYPE = EntityDataManager.createKey(EntityCatfish.class, DataSerializers.STRING);
    private static final DataParameter<NBTTagCompound> SWALLOWED_ENTITY_DATA = EntityDataManager.createKey(EntityCatfish.class, DataSerializers.COMPOUND_TAG);
    public InventoryBasic catfishInventory;
    private int eatCooldown;

    public EntityCatfish(World world) {
        super(world);
        initCatfishInventory();
        this.moveHelper = new AquaticMoveController(this, 1.0F, 15F);
        this.setSize(0.9F, 0.6F);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(10.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.25D);
    }

    @Override
    public int getMaxSpawnedInChunk() {
        return 2;
    }

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(1, new EntityAIWander(this, 1.0D, 50));
        this.tasks.addTask(2, new EntityAIPanic(this, 1.0D));
        this.tasks.addTask(3, new TargetFoodGoal(this));
        this.tasks.addTask(4, new FlyingEntityAITempt(this, 1.0D, Items.FISH, false) {
            @Override
            protected boolean isTempting(ItemStack stack) {
                return AMTagRegistry.itemInTag(AMTagRegistry.CATFISH_ITEM_FASCINATIONS, stack.getItem());
            }
        });
        this.tasks.addTask(5, new FascinateLanternGoal(this));
        this.tasks.addTask(6, new AnimalAISwimBottom(this, 1.0F, 7));
    }

    @Override
    protected boolean canDespawn() {
        return !this.isFromBucket() && !this.requiresCustomPersistence() && !this.hasCustomName() && super.canDespawn();
    }

    private void initCatfishInventory() {
        InventoryBasic old = this.catfishInventory;
        int size = this.getCatfishSize() > 2 ? 1 : this.getCatfishSize() == 1 ? 9 : 3;
        this.catfishInventory = new InventoryBasic("Catfish", false, size);
        if (old != null) {
            int i = Math.min(old.getSizeInventory(), this.catfishInventory.getSizeInventory());
            for (int j = 0; j < i; ++j) {
                ItemStack stack = old.getStackInSlot(j);
                if (!stack.isEmpty()) {
                    this.catfishInventory.setInventorySlotContents(j, stack.copy());
                }
            }
        }
    }

    @Override
    protected void dropEquipment(boolean wasRecentlyHit, int lootingModifier) {
        super.dropEquipment(wasRecentlyHit, lootingModifier);
        if (this.catfishInventory != null) {
            for (int i = 0; i < catfishInventory.getSizeInventory(); i++) {
                ItemStack stack = catfishInventory.getStackInSlot(i);
                if (!stack.isEmpty()) {
                    this.entityDropItem(stack, 0.0F);
                }
            }
            catfishInventory.clear();
        }
        if (this.getCatfishSize() == 2) {
            this.spit();
        }
    }

    public boolean requiresCustomPersistence() {
        return this.hasCustomName() || this.isFromBucket() || this.hasSwallowedEntity() || this.catfishInventory != null && !this.isInventoryEmpty();
    }

    private boolean isInventoryEmpty() {
        if (catfishInventory == null) {
            return true;
        }
        for (int i = 0; i < catfishInventory.getSizeInventory(); i++) {
            if (!catfishInventory.getStackInSlot(i).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public static boolean canCatfishSpawn(World world, BlockPos pos) {
        return world.getBlockState(pos).getMaterial() == Material.WATER;
    }

    @Override
    public boolean getCanSpawnHere() {
        return AMEntityRegistry.rollSpawn(AMConfig.catfishSpawnRolls, this.getRNG(), AMEntityRegistry.AMSpawnReason.OTHER);
    }

    @Override
    public boolean isNotColliding() {
        return AMEntityRegistry.aquaticNoEntityCollision(this);
    }

    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }

    @Override
    public boolean isPushedByWater() {
        return false;
    }

    @Override
    protected PathNavigate createNavigator(World worldIn) {
        return new PathNavigateSwimmer(this, worldIn);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(FROM_BUCKET, false);
        this.dataManager.register(CATFISH_SIZE, 0);
        this.dataManager.register(SPIT_TIME, 0);
        this.dataManager.register(SWALLOWED_ENTITY_TYPE, "minecraft:pig");
        this.dataManager.register(SWALLOWED_ENTITY_DATA, new NBTTagCompound());
        this.dataManager.register(HAS_SWALLOWED_ENTITY, false);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (!this.world.isRemote) {
            if (this.getSpitTime() > 0) {
                this.setSpitTime(this.getSpitTime() - 1);
            }
            if (eatCooldown > 0) {
                eatCooldown--;
            }
            this.applyCatfishSizeToBoundingBox();
            if (this.getHealth() > this.getMaxHealth()) {
                this.setHealth(this.getMaxHealth());
            }
        }
        if (!this.world.isRemote) {
            BlockPos vomitTo = null;
            int width = (int) Math.ceil(this.width / 2F);
            int height = (int) Math.ceil(this.height / 2F);
            for (int i = -width; i <= width; i++) {
                for (int j = -height; j <= height; j++) {
                    for (int k = -width; k <= width; k++) {
                        BlockPos pos = new BlockPos(this.posX + i, this.posY + j, this.posZ + k);
                        if (AMTagRegistry.blockInTag(AMTagRegistry.CATFISH_BLOCK_FASCINATIONS, this.world.getBlockState(pos).getBlock())) {
                            vomitTo = pos;
                            break;
                        }
                    }
                }
            }
            if (vomitTo != null && this.canSpit() && this.getSpitTime() == 0) {
                this.playSound(SoundEvents.ENTITY_PLAYER_BURP, this.getSoundVolume(), this.getSoundPitch());
                Vec3d face = new Vec3d(vomitTo.getX() + 0.5D, vomitTo.getY() + 0.5D, vomitTo.getZ() + 0.5D).subtract(this.posX, this.posY, this.posZ);
                double d0 = face.lengthVector();
                this.rotationPitch = (float) (-MathHelper.atan2(face.y, d0) * (180D / Math.PI));
                this.rotationYaw = (float) (MathHelper.atan2(face.z, face.x) * (180D / Math.PI) - 90F);
                this.renderYawOffset = this.rotationYaw;
                this.rotationYawHead = this.rotationYaw;
                this.spit();
            }
        }
    }

    private void applyCatfishSizeToBoundingBox() {
        float w = 0.9F;
        float h = 0.6F;
        if (this.getCatfishSize() == 1) {
            w = 1.25F;
            h = 0.9F;
        } else if (this.getCatfishSize() == 2) {
            w = 1.9F;
            h = 0.9F;
        }
        this.setSize(w, h);
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(10F * this.getCatfishSize() + 10F);
    }

    public boolean isFromBucket() {
        return this.dataManager.get(FROM_BUCKET);
    }

    public void setFromBucket(boolean bucketed) {
        this.dataManager.set(FROM_BUCKET, bucketed);
    }

    protected void setBucketData(ItemStack bucket) {
        if (this.hasCustomName()) {
            bucket.setStackDisplayName(this.getCustomNameTag());
        }
        NBTTagCompound tag = bucket.getTagCompound();
        if (tag == null) {
            tag = new NBTTagCompound();
            bucket.setTagCompound(tag);
        }
        this.writeEntityToNBT(tag);
    }

    protected ItemStack getFishBucket() {
        Item item;
        int size = this.getCatfishSize();
        if (size == 1) {
            item = AMItemRegistry.MEDIUM_CATFISH_BUCKET;
        } else if (size == 2) {
            item = AMItemRegistry.LARGE_CATFISH_BUCKET;
        } else {
            item = AMItemRegistry.SMALL_CATFISH_BUCKET;
        }
        return new ItemStack(item);
    }

    public int getCatfishSize() {
        return MathHelper.clamp(this.dataManager.get(CATFISH_SIZE), 0, 2);
    }

    public void setCatfishSize(int catfishSize) {
        this.dataManager.set(CATFISH_SIZE, catfishSize);
        if (!this.world.isRemote) {
            initCatfishInventory();
            applyCatfishSizeToBoundingBox();
        }
    }

    public int getSpitTime() {
        return this.dataManager.get(SPIT_TIME);
    }

    public void setSpitTime(int time) {
        this.dataManager.set(SPIT_TIME, time);
    }

    public boolean isSpitting() {
        return getSpitTime() > 0;
    }

    public String getSwallowedEntityType() {
        return this.dataManager.get(SWALLOWED_ENTITY_TYPE);
    }

    public void setSwallowedEntityType(String containedEntityType) {
        this.dataManager.set(SWALLOWED_ENTITY_TYPE, containedEntityType);
    }

    public NBTTagCompound getSwallowedData() {
        return this.dataManager.get(SWALLOWED_ENTITY_DATA);
    }

    public void setSwallowedData(NBTTagCompound containedData) {
        this.dataManager.set(SWALLOWED_ENTITY_DATA, containedData);
    }

    public boolean hasSwallowedEntity() {
        return this.dataManager.get(HAS_SWALLOWED_ENTITY);
    }

    public void setHasSwallowedEntity(boolean swallowedEntity) {
        this.dataManager.set(HAS_SWALLOWED_ENTITY, swallowedEntity);
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (super.attackEntityFrom(source, amount)) {
            this.spit();
            return true;
        }
        return false;
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (stack.getItem() == Items.WATER_BUCKET && this.isEntityAlive()) {
            this.playSound(SoundEvents.ITEM_BUCKET_FILL, 1.0F, 1.0F);
            if (!player.capabilities.isCreativeMode) {
                stack.shrink(1);
            }
            ItemStack itemstack1 = this.getFishBucket();
            this.setBucketData(itemstack1);
            if (stack.isEmpty()) {
                player.setHeldItem(hand, itemstack1);
            } else if (!player.inventory.addItemStackToInventory(itemstack1)) {
                player.dropItem(itemstack1, false);
            }
            this.setDead();
            return true;
        }
        return super.processInteract(player, hand);
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setBoolean("FromBucket", this.isFromBucket());
        compound.setInteger("CatfishSize", this.getCatfishSize());
        if (this.catfishInventory != null) {
            NBTTagList list = new NBTTagList();
            for (int i = 0; i < this.catfishInventory.getSizeInventory(); ++i) {
                ItemStack itemstack = this.catfishInventory.getStackInSlot(i);
                if (!itemstack.isEmpty()) {
                    NBTTagCompound slot = new NBTTagCompound();
                    slot.setByte("Slot", (byte) i);
                    itemstack.writeToNBT(slot);
                    list.appendTag(slot);
                }
            }
            compound.setTag("Items", list);
        }
        compound.setString("ContainedEntityType", this.getSwallowedEntityType());
        compound.setTag("ContainedData", this.getSwallowedData());
        compound.setBoolean("HasSwallowedEntity", this.hasSwallowedEntity());
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.setFromBucket(compound.getBoolean("FromBucket"));
        this.setCatfishSize(compound.getInteger("CatfishSize"));
        if (compound.hasKey("Items", 9)) {
            initCatfishInventory();
            NBTTagList list = compound.getTagList("Items", 10);
            for (int i = 0; i < list.tagCount(); ++i) {
                NBTTagCompound slot = list.getCompoundTagAt(i);
                int j = slot.getByte("Slot") & 255;
                this.catfishInventory.setInventorySlotContents(j, new ItemStack(slot));
            }
        }
        this.setSwallowedEntityType(compound.getString("ContainedEntityType"));
        if (compound.hasKey("ContainedData", 10)) {
            this.setSwallowedData(compound.getCompoundTag("ContainedData"));
        }
        this.setHasSwallowedEntity(compound.getBoolean("HasSwallowedEntity"));
    }

    @Nullable
    @Override
    public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData livingdata) {
        if (this.isFromBucket()) {
            return super.onInitialSpawn(difficulty, livingdata);
        }
        this.setCatfishSize(this.rand.nextFloat() < 0.35F ? 1 : 0);
        if (this.rand.nextFloat() < 0.1F && BiomeConfig.test(BiomeConfig.catfish, this.world.getBiome(this.getPosition()))
                && net.minecraftforge.common.BiomeDictionary.hasType(this.world.getBiome(this.getPosition()), net.minecraftforge.common.BiomeDictionary.Type.SWAMP)) {
            this.setCatfishSize(2);
        }
        return super.onInitialSpawn(difficulty, livingdata);
    }

    @Override
    public void travel(float strafe, float vertical, float forward) {
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

    protected void playStepSound(BlockPos pos, IBlockState blockIn) {
    }

    public void onCollideWithPlayer(EntityPlayer entityIn) {
        super.onCollideWithPlayer(entityIn);
    }

    @Override
    protected void updateAITasks() {
        super.updateAITasks();
        if (!this.world.isRemote && !this.isDead) {
            List<EntityItem> items = this.world.getEntitiesWithinAABB(EntityItem.class, this.getEntityBoundingBox().grow(1.0D, 0.5D, 1.0D));
            for (EntityItem itemEntity : items) {
                if (!this.isFull() && this.catfishInventory != null && this.getCatfishSize() < 2) {
                    ItemStack stack = itemEntity.getItem();
                    for (int i = 0; i < catfishInventory.getSizeInventory(); i++) {
                        if (catfishInventory.getStackInSlot(i).isEmpty()) {
                            catfishInventory.setInventorySlotContents(i, stack.copy());
                            itemEntity.setDead();
                            this.playSound(SoundEvents.ENTITY_GENERIC_EAT, this.getSoundVolume(), this.getSoundPitch());
                            break;
                        }
                    }
                }
            }
        }
    }

    public boolean isFull() {
        if (this.getCatfishSize() == 2 || this.catfishInventory == null) {
            return this.hasSwallowedEntity();
        }
        for (int i = 0; i < this.catfishInventory.getSizeInventory(); i++) {
            if (this.catfishInventory.getStackInSlot(i).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public float getSoundPitch() {
        float f = (3 - this.getCatfishSize()) * 0.33F;
        return super.getSoundPitch() * (float) Math.sqrt(f) * 1.2F;
    }

    public boolean swallowEntity(Entity entity) {
        if (this.getCatfishSize() == 2 && entity instanceof EntityMob) {
            this.setHasSwallowedEntity(true);
            ResourceLocation mobtype = EntityList.getKey(entity);
            if (mobtype != null) {
                this.setSwallowedEntityType(mobtype.toString());
            }
            NBTTagCompound tag = new NBTTagCompound();
            entity.writeToNBT(tag);
            this.setSwallowedData(tag);
            this.playSound(SoundEvents.ENTITY_GENERIC_EAT, this.getSoundVolume(), this.getSoundPitch());
            return true;
        }
        return false;
    }

    public boolean canSpit() {
        return this.getCatfishSize() == 2 ? this.hasSwallowedEntity() : this.catfishInventory != null && !this.isInventoryEmpty();
    }

    public void spit() {
        this.setSpitTime(10);
        this.eatCooldown = 60 + rand.nextInt(60);
        if (this.getCatfishSize() == 2) {
            if (this.hasSwallowedEntity()) {
                Entity entity = EntityList.createEntityByIDFromName(new ResourceLocation(this.getSwallowedEntityType()), this.world);
                if (entity instanceof EntityLivingBase) {
                    EntityLivingBase alive = (EntityLivingBase) entity;
                    alive.readFromNBT(this.getSwallowedData());
                    alive.setHealth(Math.max(2, alive.getMaxHealth() * 0.25F));
                    alive.rotationYaw = this.rand.nextFloat() * 360 - 180;
                    Vec3d mouth = this.getMouthVec();
                    alive.setPosition(mouth.x, mouth.y, mouth.z);
                    if (this.world.spawnEntity(alive)) {
                        this.setHasSwallowedEntity(false);
                        this.setSwallowedEntityType("minecraft:pig");
                        this.setSwallowedData(new NBTTagCompound());
                    }
                }
            }
        } else if (this.catfishInventory != null) {
            for (int i = 0; i < this.catfishInventory.getSizeInventory(); i++) {
                ItemStack itemStack = this.catfishInventory.getStackInSlot(i);
                if (!itemStack.isEmpty()) {
                    Vec3d vec3 = this.getMouthVec();
                    EntityItem item = new EntityItem(this.world, vec3.x, vec3.y, vec3.z, itemStack.copy());
                    item.motionX = item.motionY = item.motionZ = 0;
                    item.setPickupDelay(30);
                    if (this.world.spawnEntity(item)) {
                        this.catfishInventory.setInventorySlotContents(i, ItemStack.EMPTY);
                    }
                    break;
                }
            }
        }
    }

    private Vec3d getMouthVec() {
        float pitch = this.rotationPitch * 0.017453292F;
        float yaw = -this.rotationYaw * 0.017453292F;
        double forward = this.width * 0.8F;
        double x = MathHelper.sin(yaw) * MathHelper.cos(pitch) * forward;
        double y = MathHelper.sin(pitch) * forward + this.height * 0.25F;
        double z = MathHelper.cos(yaw) * MathHelper.cos(pitch) * forward;
        return new Vec3d(this.posX + x, this.posY + y, this.posZ + z);
    }

    private boolean isFood(Entity entity) {
        if (this.getCatfishSize() == 2) {
            ResourceLocation key = EntityList.getKey(entity);
            return key != null && !AMTagRegistry.entityMatchesEntityTypeTag(AMTagRegistry.CATFISH_IGNORE_EATING, entity)
                    && entity instanceof EntityMob && !(entity instanceof EntityCatfish) && entity.height <= 1.0F;
        }
        return entity instanceof EntityItem && ((EntityItem) entity).getAge() > 35;
    }

    private boolean canSeeBlock(BlockPos destinationBlock) {
        Vec3d vector3d = new Vec3d(this.posX, this.posY + (double) this.getEyeHeight(), this.posZ);
        Vec3d blockVec = new Vec3d(destinationBlock.getX() + 0.5D, destinationBlock.getY() + 0.5D, destinationBlock.getZ() + 0.5D);
        RayTraceResult result = this.world.rayTraceBlocks(vector3d, blockVec, false, true, false);
        return result == null || result.getBlockPos().equals(destinationBlock);
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_SQUID_DEATH;
    }
    @Override
    @Nullable
    protected ResourceLocation getLootTable() {
        return AMLootTables.CATFISH;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return SoundEvents.ENTITY_SQUID_HURT;
    }

    private static class TargetFoodGoal extends EntityAIBase {
        private final EntityCatfish catfish;
        private Entity food;
        private int executionCooldown = 50;

        public TargetFoodGoal(EntityCatfish catfish) {
            this.setMutexBits(1);
            this.catfish = catfish;
        }

        @Override
        public boolean shouldExecute() {
            if (!catfish.isInWater() || catfish.eatCooldown > 0) {
                return false;
            }
            if (executionCooldown > 0) {
                executionCooldown--;
                return false;
            }
            executionCooldown = 50 + catfish.getRNG().nextInt(50);
            if (!catfish.isFull()) {
                List<Entity> list = catfish.world.getEntitiesWithinAABB(Entity.class, catfish.getEntityBoundingBox().grow(8, 8, 8),
                        e -> e != catfish && catfish.isFood(e));
                list.sort(Comparator.comparingDouble(catfish::getDistanceSq));
                if (!list.isEmpty()) {
                    food = list.get(0);
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean shouldContinueExecuting() {
            return food != null && food.isEntityAlive() && !catfish.isFull();
        }

        @Override
        public void resetTask() {
            executionCooldown = 5;
            food = null;
        }

        @Override
        public void updateTask() {
            catfish.getNavigator().tryMoveToEntityLiving(food, 1.0D);
            float eatDist = catfish.width * 0.65F + food.width;
            if (catfish.getDistance(food) < eatDist + 3 && catfish.canEntityBeSeen(food)) {
                Vec3d delta = catfish.getMouthVec().subtract(food.posX, food.posY, food.posZ).normalize().scale(0.1D);
                food.motionX += delta.x;
                food.motionY += delta.y;
                food.motionZ += delta.z;
                if (catfish.getDistance(food) < eatDist) {
                    if (food instanceof EntityPlayer) {
                        food.attackEntityFrom(DamageSource.causeMobDamage(catfish), 12000);
                    } else if (catfish.swallowEntity(food)) {
                        catfish.playSound(SoundEvents.ENTITY_GENERIC_EAT, catfish.getSoundVolume(), catfish.getSoundPitch());
                        food.setDead();
                    }
                }
            }
        }
    }

    private static class FascinateLanternGoal extends EntityAIBase {
        protected BlockPos destinationBlock;
        private final EntityCatfish fish;
        private int runDelay = 70;
        private int chillTime;
        private int maxChillTime = 200;

        private FascinateLanternGoal(EntityCatfish fish) {
            this.setMutexBits(1);
            this.fish = fish;
        }

        @Override
        public boolean shouldContinueExecuting() {
            return destinationBlock != null && isSeaLantern(fish.world, destinationBlock) && isCloseToLantern(16) && !fish.isFull();
        }

        public boolean isCloseToLantern(double dist) {
            return destinationBlock == null || fish.getDistanceSq(destinationBlock) < dist * dist;
        }

        @Override
        public boolean shouldExecute() {
            if (!fish.isInWater()) {
                return false;
            }
            if (this.runDelay > 0) {
                --this.runDelay;
                return false;
            }
            this.runDelay = 70 + fish.getRNG().nextInt(70);
            return !fish.isFull() && searchForDestination();
        }

        @Override
        public void startExecuting() {
            chillTime = 0;
            maxChillTime = 10 + fish.getRNG().nextInt(20);
        }

        @Override
        public void updateTask() {
            Vec3d vec = new Vec3d(destinationBlock.getX() + 0.5D, destinationBlock.getY() + 0.5D, destinationBlock.getZ() + 0.5D);
            fish.getNavigator().tryMoveToXYZ(vec.x, vec.y, vec.z, 1.0D);
            if (fish.getDistanceSq(destinationBlock) < 1F + fish.width * 0.6F) {
                Vec3d face = vec.subtract(fish.posX, fish.posY, fish.posZ).normalize();
                fish.motionX += face.x * 0.1D;
                fish.motionY += face.y * 0.1D;
                fish.motionZ += face.z * 0.1D;
                if (chillTime++ > maxChillTime) {
                    destinationBlock = null;
                }
            }
        }

        @Override
        public void resetTask() {
            destinationBlock = null;
        }

        protected boolean searchForDestination() {
            BlockPos origin = fish.getPosition();
            for (int yOff = -8; yOff <= 2; yOff++) {
                for (int i = 0; i < 16; i++) {
                    for (int ring = 0; ring <= i; ring = ring > 0 ? -ring : 1 - ring) {
                        for (int side = ring < i && ring > -i ? i : 0; side <= i; side = side > 0 ? -side : 1 - side) {
                            BlockPos test = origin.add(ring, yOff - 1, side);
                            if (isSeaLantern(fish.world, test) && fish.canSeeBlock(test)) {
                                destinationBlock = test;
                                return true;
                            }
                        }
                    }
                }
            }
            return false;
        }

        private boolean isSeaLantern(World world, BlockPos pos) {
            return AMTagRegistry.blockInTag(AMTagRegistry.CATFISH_BLOCK_FASCINATIONS, world.getBlockState(pos).getBlock());
        }
    }
}
