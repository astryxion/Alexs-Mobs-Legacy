package com.github.alexthe666.alexsmobs.entity;

import com.github.alexthe666.alexsmobs.AlexsMobs;
import com.github.alexthe666.alexsmobs.config.AMConfig;
import com.github.alexthe666.alexsmobs.entity.ai.AnimalAIWanderRanged;
import com.github.alexthe666.alexsmobs.entity.ai.CreatureAITargetItems;
import com.github.alexthe666.alexsmobs.message.MessageMungusBiomeChange;
import com.github.alexthe666.alexsmobs.misc.AMSoundRegistry;
import com.github.alexthe666.alexsmobs.misc.AMTagRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.IGrowable;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAIFollowParent;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIMate;
import net.minecraft.entity.ai.EntityAIPanic;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAITempt;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.IShearable;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import com.google.common.base.Optional;
import java.util.Random;

public class EntityMungus extends EntityAnimal implements ITargetsDroppedItems, IShearable {

    protected static final DataParameter<Optional<BlockPos>> TARGETED_BLOCK_POS = EntityDataManager.createKey(EntityMungus.class, DataSerializers.OPTIONAL_BLOCK_POS);
    private static final DataParameter<Boolean> ALT_ORDER_MUSHROOMS = EntityDataManager.createKey(EntityMungus.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> REVERTING = EntityDataManager.createKey(EntityMungus.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> MUSHROOM_COUNT = EntityDataManager.createKey(EntityMungus.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> SACK_SWELL = EntityDataManager.createKey(EntityMungus.class, DataSerializers.VARINT);
    private static final DataParameter<Boolean> EXPLOSION_DISABLED = EntityDataManager.createKey(EntityMungus.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> MUSHROOM_STATE_ID = EntityDataManager.createKey(EntityMungus.class, DataSerializers.VARINT);
    private static final HashMap<String, String> MUSHROOM_TO_BIOME = new HashMap<>();
    private static final HashMap<String, String> MUSHROOM_TO_BLOCK = new HashMap<>();
    private static boolean initBiomeData = false;
    public float prevSwellProgress = 0;
    public float swellProgress = 0;
    private int beamCounter = 0;
    private int mosquitoAttackCooldown = 0;
    private boolean hasExploded;

    public EntityMungus(World worldIn) {
        super(worldIn);
        initBiomeData();
    }

    public static IBlockState getMushroomBlockstate(Item item) {
        if (item instanceof net.minecraft.item.ItemBlock) {
            if (MUSHROOM_TO_BIOME.containsKey(item.getRegistryName().toString())) {
                return ((net.minecraft.item.ItemBlock) item).getBlock().getDefaultState();
            }
        }
        return null;
    }

    private static void initBiomeData() {
        if (!initBiomeData || MUSHROOM_TO_BIOME.isEmpty()) {
            initBiomeData = true;
            for (String str : AMConfig.mungusBiomeMatches) {
                String[] split = str.split("\\|");
                if (split.length >= 2) {
                    MUSHROOM_TO_BIOME.put(split[0], split[1]);
                    MUSHROOM_TO_BLOCK.put(split[0], split[2]);
                }
            }
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return AMSoundRegistry.MUNGUS_IDLE;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return AMSoundRegistry.MUNGUS_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return AMSoundRegistry.MUNGUS_HURT;
    }

    @Override
    public boolean getCanSpawnHere() {
        return AMEntityRegistry.rollSpawn(AMConfig.mungusSpawnRolls, this.getRNG(), AMEntityRegistry.AMSpawnReason.OTHER) && super.getCanSpawnHere();
    }

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(0, new EntityAIMate(this, 1.0D));
        this.tasks.addTask(1, new EntityAISwimming(this));
        this.tasks.addTask(2, new EntityAIPanic(this, 1.25D));
        this.tasks.addTask(3, new EntityAITempt(this, 1.0D, Items.MUSHROOM_STEW, false) {
            @Override
            protected boolean isTempting(ItemStack stack) {
                return EntityMungus.this.shouldFollowMushroom(stack) || stack.getItem() == Items.MUSHROOM_STEW;
            }
        });
        this.tasks.addTask(5, new AITargetMushrooms());
        this.tasks.addTask(6, new EntityAIFollowParent(this, 1.1D));
        this.tasks.addTask(7, new AnimalAIWanderRanged(this, 60, 1.0D, 14, 7));
        this.tasks.addTask(8, new EntityAIWatchClosest(this, EntityLivingBase.class, 15.0F));
        this.tasks.addTask(8, new EntityAILookIdle(this));
        this.targetTasks.addTask(1, new CreatureAITargetItems(this, false, 10));
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        this.prevSwellProgress = swellProgress;
        if (this.isReverting() && AMConfig.mungusBiomeTransformationType == 2) {
            swellProgress += 0.5F;
            if (swellProgress >= 10) {
                explode();
                swellProgress = 0;
                this.dataManager.set(REVERTING, false);
            }
        } else if (isEntityAlive() && swellProgress > 0F) {
            swellProgress -= 1F;
        }
        if (dataManager.get(EXPLOSION_DISABLED)) {
            if (mosquitoAttackCooldown < 0) {
                mosquitoAttackCooldown++;
            }
            if (mosquitoAttackCooldown > 200) {
                mosquitoAttackCooldown = 0;
                dataManager.set(EXPLOSION_DISABLED, false);
            }
        }
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        if (!this.isEntityAlive()) {
            if (this.getMushroomCount() >= 5 && AMConfig.mungusBiomeTransformationType > 0 && !this.isChild() && !this.dataManager.get(EXPLOSION_DISABLED)) {
                this.swellProgress++;
                if (this.deathTime == 20 && !hasExploded) {
                    hasExploded = true;
                    explode();
                }
            }
        }
        if (this.getBeamTarget() != null) {
            BlockPos t = this.getBeamTarget();
            if (isMushroomTarget(t) && this.canSeeMushroom(t)) {
                this.getLookHelper().setLookPosition(t.getX() + 0.5D, t.getY() + 0.15D, t.getZ() + 0.5D, 90.0F, 90.0F);
                double d5 = 1.0F;
                double eyeHeight = this.posY + 1.0D;
                if (beamCounter % 20 == 0) {
                    this.playSound(AMSoundRegistry.MUNGUS_LASER_LOOP, this.getSoundPitch(), this.getSoundVolume());
                }
                beamCounter++;

                double d0 = t.getX() + 0.5D - this.posX;
                double d1 = t.getY() + 0.5D - eyeHeight;
                double d2 = t.getZ() + 0.5D - this.posZ;
                double d3 = Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
                d0 = d0 / d3;
                d1 = d1 / d3;
                d2 = d2 / d3;
                double d4 = this.rand.nextDouble();
                while (d4 < d3 - 0.5D) {
                    d4 += 1.0D - d5 + this.rand.nextDouble();
                    if (rand.nextFloat() < 0.1F) {
                        float r1 = 0.3F * (rand.nextFloat() - 0.5F);
                        float r2 = 0.3F * (rand.nextFloat() - 0.5F);
                        float r3 = 0.3F * (rand.nextFloat() - 0.5F);
                        this.world.spawnParticle(EnumParticleTypes.TOWN_AURA, this.posX + d0 * d4 + r1, this.posY + this.getEyeHeight() + d1 * d4 + r2, this.posZ + d2 * d4 + r3, r1 * 4, r2 * 4, r3 * 4);
                    }
                }
                if (beamCounter > 200) {
                    IBlockState state = world.getBlockState(t);
                    if (state.getBlock() instanceof IGrowable) {
                        IGrowable igrowable = (IGrowable) state.getBlock();
                        boolean flag = false;
                        if (igrowable.canGrow(this.world, t, state, this.world.isRemote)) {
                            for (int i = 0; i < 5; i++) {
                                float r1 = 3F * (rand.nextFloat() - 0.5F);
                                float r2 = 2F * (rand.nextFloat() - 0.5F);
                                float r3 = 3F * (rand.nextFloat() - 0.5F);
                                this.world.spawnParticle(EnumParticleTypes.EXPLOSION_NORMAL, t.getX() + 0.5D + r1, t.getY() + 0.5D + r2, t.getZ() + 0.5D + r3, r1 * 4, r2 * 4, r3 * 4);
                            }
                            if (!this.world.isRemote) {
                                this.world.playEvent(2005, t, 0);
                                igrowable.grow(this.world, this.rand, t, state);
                                flag = world.getBlockState(t).getBlock() != state.getBlock();
                            }
                        }
                        if (!flag) {
                            int grown = 0;
                            int maxGrow = 2 + rand.nextInt(3);
                            for (int i = 0; i < 15; i++) {
                                BlockPos pos = t.add(rand.nextInt(10) - 5, rand.nextInt(4) - 2, rand.nextInt(10) - 5);
                                if (grown < maxGrow) {
                                    if (world.isAirBlock(pos) && world.getBlockState(pos.down()).isSideSolid(world, pos.down(), EnumFacing.UP)) {
                                        world.setBlockState(pos, state);
                                        grown++;
                                    }
                                }
                            }
                        }
                        this.playSound(AMSoundRegistry.MUNGUS_LASER_END, this.getSoundPitch(), this.getSoundVolume());
                        if (flag) {
                            this.playSound(AMSoundRegistry.MUNGUS_LASER_GROW, this.getSoundPitch(), this.getSoundVolume());
                        }
                        this.setBeamTarget(null);
                        beamCounter = -1200;
                        if (this.getMushroomCount() > 0) {
                            this.setMushroomCount(this.getMushroomCount() - 1);
                        }
                    }
                }
            } else {
                this.setBeamTarget(null);
            }
        }
        if (beamCounter < 0) {
            beamCounter++;
        }
    }

    private void explode() {
        for (int i = 0; i < 5; i++) {
            float r1 = 6F * (rand.nextFloat() - 0.5F);
            float r2 = 2F * (rand.nextFloat() - 0.5F);
            float r3 = 6F * (rand.nextFloat() - 0.5F);
            this.world.spawnParticle(EnumParticleTypes.EXPLOSION_NORMAL, this.posX + r1, this.posY + 0.5F + r2, this.posZ + r3, r1 * 4, r2 * 4, r3 * 4);
        }
        final int radius = 3;
        final int j = radius + world.rand.nextInt(1);
        final int k = (radius + world.rand.nextInt(1));
        final int l = radius + world.rand.nextInt(1);
        final float f = (float) (j + k + l) * 0.333F + 0.5F;
        final float ff = f * f;
        final double ffDouble = ff;
        BlockPos center = this.getPosition();
        IBlockState transformState = Blocks.MYCELIUM.getDefaultState();
        Biome biome = ForgeRegistries.BIOMES.getValue(new ResourceLocation("mushroom_island"));
        ResourceLocation transformTag = AMTagRegistry.MUNGUS_REPLACE_MUSHROOM;
        if (this.getMushroomState() != null) {
            String mushroomKey = this.getMushroomState().getBlock().getRegistryName().toString();
            if (MUSHROOM_TO_BLOCK.containsKey(mushroomKey)) {
                Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(MUSHROOM_TO_BLOCK.get(mushroomKey)));
                if (block != null) {
                    transformState = block.getDefaultState();
                    if (block.getRegistryName() != null) {
                        String reg = block.getRegistryName().toString();
                        if ("minecraft:warped_nylium".equals(reg) || "minecraft:crimson_nylium".equals(reg)) {
                            transformTag = AMTagRegistry.MUNGUS_REPLACE_NETHER;
                        }
                    }
                }
            }
            Biome biomeFromShroom = getBiomeKeyFromShroom();
            if (biomeFromShroom != null) {
                biome = biomeFromShroom;
            }
        }
        IBlockState finalTransformState = transformState;
        ResourceLocation finalTransformReplace = transformTag;

        if (AMConfig.mungusBiomeTransformationType == 2 && !world.isRemote) {
            transformBiome(center, biome);
        }
        this.playSound(SoundEvents.ENTITY_GENERIC_EXPLODE, this.getSoundVolume(), this.getSoundPitch());
        if (!isReverting()) {
            for (BlockPos blockpos : BlockPos.getAllInBox(center.add(-j, -k, -l), center.add(j, k, l))) {
                if (blockpos.distanceSq(center) <= ffDouble) {
                    if (world.rand.nextFloat() > (float) blockpos.distanceSq(center) / ff) {
                        if (AMTagRegistry.blockInTag(finalTransformReplace, world.getBlockState(blockpos).getBlock())
                                && !world.getBlockState(blockpos.up()).isSideSolid(world, blockpos.up(), EnumFacing.DOWN)) {
                            world.setBlockState(blockpos, finalTransformState);
                        }
                        if (world.rand.nextInt(4) == 0 && world.getBlockState(blockpos).getMaterial().isSolid()
                                && !world.getBlockState(blockpos.up()).getMaterial().isLiquid()
                                && !world.getBlockState(blockpos.up()).isSideSolid(world, blockpos.up(), EnumFacing.DOWN)
                                && this.getMushroomState() != null) {
                            world.setBlockState(blockpos.up(), this.getMushroomState());
                        }
                    }
                }
            }
        }
    }

    public void disableExplosion() {
        this.dataManager.set(EXPLOSION_DISABLED, true);
    }

    @Nullable
    private Biome getBiomeKeyFromShroom() {
        IBlockState state = this.getMushroomState();
        if (state == null) {
            return null;
        }
        String blockRegName = state.getBlock().getRegistryName().toString();
        String str = MUSHROOM_TO_BIOME.get(blockRegName);
        if (str != null) {
            return ForgeRegistries.BIOMES.getValue(new ResourceLocation(str));
        }
        return null;
    }

    private void transformBiome(BlockPos pos, Biome biome) {
        Chunk chunk = world.getChunkFromBlockCoords(pos);
        if (this.dataManager.get(REVERTING)) {
            if (world instanceof WorldServer) {
                BiomeProvider biomeProvider = ((WorldServer) world).getBiomeProvider();
                byte[] arr = chunk.getBiomeArray();
                int baseX = chunk.x << 4;
                int baseZ = chunk.z << 4;
                Biome lastBiome = null;
                for (int i = 0; i < 16; i++) {
                    for (int j = 0; j < 16; j++) {
                        int idx = i * 16 + j;
                        BlockPos sample = new BlockPos(baseX + i, 64, baseZ + j);
                        lastBiome = biomeProvider.getBiome(sample);
                        arr[idx] = (byte) (Biome.getIdForBiome(lastBiome) & 255);
                    }
                }
                chunk.markDirty();
                if (lastBiome != null && !world.isRemote) {
                    ResourceLocation reg = lastBiome.getRegistryName();
                    if (reg != null) {
                        AlexsMobs.sendMSGToAll(new MessageMungusBiomeChange(this.getEntityId(), pos.getX(), pos.getZ(), reg.toString()));
                    }
                }
            }
        } else {
            if (biome == null) {
                return;
            }
            if (!world.isRemote) {
                byte[] arr = chunk.getBiomeArray();
                int id = Biome.getIdForBiome(biome) & 255;
                java.util.Arrays.fill(arr, (byte) id);
                chunk.markDirty();
                ResourceLocation reg = biome.getRegistryName();
                if (reg != null) {
                    AlexsMobs.sendMSGToAll(new MessageMungusBiomeChange(this.getEntityId(), pos.getX(), pos.getZ(), reg.toString()));
                }
            }
        }
    }

    private boolean shouldFollowMushroom(ItemStack stack) {
        IBlockState state = getMushroomBlockstate(stack.getItem());
        if (state != null) {
            if (this.getMushroomCount() == 0) {
                return true;
            } else {
                IBlockState current = this.getMushroomState();
                return current != null && current.getBlock() == state.getBlock();
            }
        }
        return false;
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        ItemStack itemstack = player.getHeldItem(hand);
        if (itemstack.getItem() == Items.POISONOUS_POTATO && !this.isChild()) {
            this.dataManager.set(REVERTING, true);
            if (!player.capabilities.isCreativeMode) {
                itemstack.shrink(1);
            }
            return true;
        }
        if (shouldFollowMushroom(itemstack) && this.getMushroomCount() < 5) {
            this.dataManager.set(REVERTING, false);
            IBlockState state = getMushroomBlockstate(itemstack.getItem());
            this.playSound(SoundEvents.BLOCK_SLIME_PLACE, this.getSoundVolume(), this.getSoundPitch());
            if (this.getMushroomState() != null && state != null && state.getBlock() != this.getMushroomState().getBlock()) {
                this.setMushroomCount(0);
            }
            this.setMushroomState(state);
            if (!player.capabilities.isCreativeMode) {
                itemstack.shrink(1);
            }
            this.setMushroomCount(this.getMushroomCount() + 1);
            return true;
        }
        return super.processInteract(player, hand);
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        boolean prev = super.attackEntityFrom(source, amount);
        if (prev) {
            this.setBeamTarget(null);
            beamCounter = Math.min(beamCounter, -1200);
        }
        return prev;
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(MUSHROOM_STATE_ID, 0);
        this.dataManager.register(TARGETED_BLOCK_POS, Optional.absent());
        this.dataManager.register(ALT_ORDER_MUSHROOMS, Boolean.FALSE);
        this.dataManager.register(REVERTING, Boolean.FALSE);
        this.dataManager.register(EXPLOSION_DISABLED, Boolean.FALSE);
        this.dataManager.register(MUSHROOM_COUNT, 0);
        this.dataManager.register(SACK_SWELL, 0);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(15.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.25D);
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        IBlockState blockstate = this.getMushroomState();
        if (blockstate != null) {
            NBTTagCompound mushroomTag = new NBTTagCompound();
            NBTUtil.writeBlockState(mushroomTag, blockstate);
            compound.setTag("MushroomState", mushroomTag);
        }
        compound.setInteger("MushroomCount", this.getMushroomCount());
        compound.setInteger("Sack", this.getSackSwell());
        compound.setInteger("BeamCounter", this.beamCounter);
        compound.setBoolean("AltMush", this.dataManager.get(ALT_ORDER_MUSHROOMS));
        if (this.getBeamTarget() != null) {
            compound.setTag("BeamTarget", NBTUtil.createPosTag(this.getBeamTarget()));
        }
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        IBlockState blockstate = null;
        if (compound.hasKey("MushroomState", 10)) {
            blockstate = NBTUtil.readBlockState(compound.getCompoundTag("MushroomState"));
            if (blockstate.getMaterial() == Material.AIR) {
                blockstate = null;
            }
        }
        if (compound.hasKey("BeamTarget", 10)) {
            this.setBeamTarget(NBTUtil.getPosFromTag(compound.getCompoundTag("BeamTarget")));
        }
        this.setMushroomState(blockstate);
        this.setMushroomCount(compound.getInteger("MushroomCount"));
        this.setSackSwell(compound.getInteger("Sack"));
        this.beamCounter = compound.getInteger("BeamCounter");
        this.dataManager.set(ALT_ORDER_MUSHROOMS, compound.getBoolean("AltMush"));
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        return stack.getItem() == Items.MUSHROOM_STEW;
    }

    @Override
    @Nullable
    public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData livingdata) {
        this.dataManager.set(ALT_ORDER_MUSHROOMS, rand.nextBoolean());
        this.setMushroomCount(rand.nextInt(2));
        setMushroomState(rand.nextBoolean() ? Blocks.BROWN_MUSHROOM.getDefaultState() : Blocks.RED_MUSHROOM.getDefaultState());
        return super.onInitialSpawn(difficulty, livingdata);
    }

    public int getMushroomCount() {
        return this.dataManager.get(MUSHROOM_COUNT);
    }

    public void setMushroomCount(int command) {
        this.dataManager.set(MUSHROOM_COUNT, command);
    }

    public int getSackSwell() {
        return this.dataManager.get(SACK_SWELL);
    }

    public void setSackSwell(int command) {
        this.dataManager.set(SACK_SWELL, command);
    }

    @Nullable
    public BlockPos getBeamTarget() {
        return this.dataManager.get(TARGETED_BLOCK_POS).orNull();
    }

    public void setBeamTarget(@Nullable BlockPos beamTarget) {
        this.dataManager.set(TARGETED_BLOCK_POS, Optional.fromNullable(beamTarget));
    }

    public boolean isAltOrderMushroom() {
        return this.dataManager.get(ALT_ORDER_MUSHROOMS);
    }

    @Nullable
    public IBlockState getMushroomState() {
        int id = this.dataManager.get(MUSHROOM_STATE_ID);
        return id == 0 ? null : Block.getStateById(id - 1);
    }

    public void setMushroomState(@Nullable IBlockState state) {
        this.dataManager.set(MUSHROOM_STATE_ID, state == null ? 0 : Block.getStateId(state) + 1);
    }

    @Override
    @Nullable
    public EntityAgeable createChild(EntityAgeable ageable) {
        return new EntityMungus(this.world);
    }

    public boolean isMushroomTarget(BlockPos pos) {
        if (this.getMushroomState() != null) {
            return world.getBlockState(pos).getBlock() == this.getMushroomState().getBlock();
        }
        return false;
    }

    @Override
    public boolean canTargetItem(ItemStack stack) {
        return shouldFollowMushroom(stack) && this.getMushroomCount() < 5;
    }

    @Override
    public void onGetItem(EntityItem e) {
        if (shouldFollowMushroom(e.getItem())) {
            IBlockState state = getMushroomBlockstate(e.getItem().getItem());
            if (this.getMushroomState() != null && state != null && state.getBlock() != this.getMushroomState().getBlock()) {
                this.setMushroomCount(0);
            }
            this.playSound(SoundEvents.BLOCK_SLIME_PLACE, this.getSoundVolume(), this.getSoundPitch());
            this.setMushroomState(state);
            this.setMushroomCount(this.getMushroomCount() + 1);
        }
    }

    private boolean canSeeMushroom(BlockPos destinationBlock) {
        Vec3d start = new Vec3d(this.posX, this.posY + this.getEyeHeight(), this.posZ);
        Vec3d end = new Vec3d(destinationBlock.getX() + 0.5D, destinationBlock.getY() + 0.5D, destinationBlock.getZ() + 0.5D);
        RayTraceResult result = this.world.rayTraceBlocks(start, end, false, true, false);
        return result != null && result.typeOfHit == RayTraceResult.Type.BLOCK && result.getBlockPos().equals(destinationBlock);
    }

    public boolean isShearable() {
        return this.isEntityAlive() && this.getMushroomState() != null && this.getMushroomCount() > 0;
    }

    @Override
    public boolean isShearable(@Nonnull ItemStack item, net.minecraft.world.IBlockAccess world, BlockPos pos) {
        return isShearable();
    }

    @Nonnull
    @Override
    public List<ItemStack> onSheared(@Nonnull ItemStack item, net.minecraft.world.IBlockAccess world, BlockPos pos, int fortune) {
        World w = this.world;
        w.playSound(null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_SHEEP_SHEAR, SoundCategory.PLAYERS, 1.0F, 1.0F);
        if (!w.isRemote && this.getMushroomState() != null && this.getMushroomCount() > 0) {
            this.setMushroomCount(this.getMushroomCount() - 1);
            if (this.getMushroomCount() <= 0) {
                this.setMushroomState(null);
                this.setBeamTarget(null);
                beamCounter = Math.min(-1200, beamCounter);
            }
        }
        return Collections.emptyList();
    }

    public boolean isReverting() {
        return dataManager.get(REVERTING);
    }

    public boolean isWarpedMoscoReady() {
        IBlockState state = this.getMushroomState();
        return state != null
                && "minecraft:warped_fungus".equals(state.getBlock().getRegistryName().toString())
                && this.getMushroomCount() >= 5;
    }

    class AITargetMushrooms extends EntityAIBase {
        private final int searchLength;
        protected BlockPos destinationBlock;
        protected int runDelay = 70;

        private AITargetMushrooms() {
            searchLength = 20;
            this.setMutexBits(1);
        }

        @Override
        public boolean shouldContinueExecuting() {
            return destinationBlock != null && EntityMungus.this.isMushroomTarget(destinationBlock) && isCloseToShroom(32);
        }

        public boolean isCloseToShroom(double dist) {
            if (destinationBlock == null) {
                return true;
            }
            return EntityMungus.this.getDistanceSq(destinationBlock.getX() + 0.5D, destinationBlock.getY() + 0.5D, destinationBlock.getZ() + 0.5D) < dist * dist;
        }

        @Override
        public boolean shouldExecute() {
            if (EntityMungus.this.getBeamTarget() != null || EntityMungus.this.beamCounter < 0 || EntityMungus.this.getMushroomCount() <= 0) {
                return false;
            }
            if (this.runDelay > 0) {
                --this.runDelay;
                return false;
            } else {
                this.runDelay = 70 + EntityMungus.this.rand.nextInt(150);
                return this.searchForDestination();
            }
        }

        @Override
        public void startExecuting() {
        }

        @Override
        public void updateTask() {
            if (this.destinationBlock == null || !EntityMungus.this.isMushroomTarget(this.destinationBlock) || EntityMungus.this.beamCounter < 0) {
                resetTask();
            } else {
                if (!EntityMungus.this.canSeeMushroom(this.destinationBlock)) {
                    EntityMungus.this.getNavigator().tryMoveToXYZ(this.destinationBlock.getX(), this.destinationBlock.getY(), this.destinationBlock.getZ(), 1D);
                } else {
                    EntityMungus.this.setBeamTarget(this.destinationBlock);
                    if (!EntityMungus.this.isInLove()) {
                        EntityMungus.this.getNavigator().clearPath();
                    }
                }
            }
        }

        @Override
        public void resetTask() {
            EntityMungus.this.setBeamTarget(null);
        }

        protected boolean searchForDestination() {
            int lvt_1_1_ = this.searchLength;
            BlockPos lvt_3_1_ = EntityMungus.this.getPosition();
            BlockPos.MutableBlockPos lvt_4_1_ = new BlockPos.MutableBlockPos();

            for (int lvt_5_1_ = -5; lvt_5_1_ <= 5; lvt_5_1_++) {
                for (int lvt_6_1_ = 0; lvt_6_1_ < lvt_1_1_; ++lvt_6_1_) {
                    for (int lvt_7_1_ = 0; lvt_7_1_ <= lvt_6_1_; lvt_7_1_ = lvt_7_1_ > 0 ? -lvt_7_1_ : 1 - lvt_7_1_) {
                        for (int lvt_8_1_ = lvt_7_1_ < lvt_6_1_ && lvt_7_1_ > -lvt_6_1_ ? lvt_6_1_ : 0; lvt_8_1_ <= lvt_6_1_; lvt_8_1_ = lvt_8_1_ > 0 ? -lvt_8_1_ : 1 - lvt_8_1_) {
                            lvt_4_1_.setPos(lvt_3_1_.getX() + lvt_7_1_, lvt_3_1_.getY() + lvt_5_1_ - 1, lvt_3_1_.getZ() + lvt_8_1_);
                            if (EntityMungus.this.isMushroomTarget(lvt_4_1_)) {
                                this.destinationBlock = lvt_4_1_.toImmutable();
                                return true;
                            }
                        }
                    }
                }
            }

            return false;
        }
    }
}
