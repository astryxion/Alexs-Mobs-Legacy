package com.github.alexthe666.alexsmobs.tileentity;

import com.github.alexthe666.alexsmobs.block.AMBlockRegistry;
import com.github.alexthe666.alexsmobs.block.BlockLeafcutterAntChamber;
import com.github.alexthe666.alexsmobs.config.AMConfig;
import com.github.alexthe666.alexsmobs.entity.AMEntityRegistry;
import com.github.alexthe666.alexsmobs.entity.EntityLeafcutterAnt;
import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFire;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.*;

/**
 * Leafcutter anthill storage. Vanilla 1.12 has no {@code BeehiveTileEntity}; {@link AnthillReleaseState} mirrors bee-hive states.
 * Enter/exit sounds use grass block sounds (1.16 bee-hive sounds do not exist in 1.12).
 */
public class TileEntityLeafcutterAnthill extends TileEntity implements ITickable {

    /**
     * 1.12 replacement for {@code net.minecraft.tileentity.BeehiveTileEntity.State} (not present in 1.12).
     */
    public enum AnthillReleaseState {
        EMERGENCY,
        HONEY_DELIVERED,
        BEE_RELEASED
    }

    private final List<TileEntityLeafcutterAnthill.Ant> ants = Lists.newArrayList();
    private int leafFeedings = 0;

    public TileEntityLeafcutterAnthill() {
    }

    public boolean hasNoAnts() {
        return this.ants.isEmpty();
    }

    public boolean hasAtleastThisManyAnts(int antCount) {
        return this.ants.size() >= antCount;
    }

    public void angerAntsBecauseAnteater(@Nullable EntityLivingBase target, IBlockState blockState, AnthillReleaseState releaseState) {
        List<Entity> list = this.tryReleaseAntAnteater(blockState, releaseState);
        if (target != null) {
            for (Entity entity : list) {
                if (entity instanceof EntityLeafcutterAnt) {
                    EntityLeafcutterAnt entityLeafcutterAnt = (EntityLeafcutterAnt) entity;
                    if (target.getDistanceSq(entity.posX, entity.posY, entity.posZ) <= 16.0D) {
                        entityLeafcutterAnt.setAttackTarget(target);
                    }
                    entityLeafcutterAnt.setStayOutOfHiveCountdown(400);
                }
            }
        }
    }

    private List<Entity> tryReleaseAntAnteater(IBlockState blockState, AnthillReleaseState releaseState) {
        List<Entity> list = Lists.newArrayList();
        this.ants.removeIf((ant) -> !ant.queen && this.addAntToWorld(blockState, ant, list, releaseState));
        return list;
    }

    @Nullable
    public IBlockState shrinkFungus() {
        BlockPos bottomChamber = this.getPos().down();
        while (world.getBlockState(bottomChamber.down()).getBlock() == AMBlockRegistry.LEAFCUTTER_ANT_CHAMBER && bottomChamber.getY() > 0) {
            bottomChamber = bottomChamber.down();
        }
        BlockPos chamber = bottomChamber;
        if (!isUnfilledChamber(chamber)) {
            IBlockState prev = world.getBlockState(chamber);
            if (prev.getBlock() == AMBlockRegistry.LEAFCUTTER_ANT_CHAMBER) {
                int fungalLevel = prev.getValue(BlockLeafcutterAntChamber.FUNGUS);
                world.setBlockState(chamber, AMBlockRegistry.LEAFCUTTER_ANT_CHAMBER.getDefaultState().withProperty(BlockLeafcutterAntChamber.FUNGUS, Math.min(0, fungalLevel - 1)), 3);
                return prev;
            }
        } else {
            boolean flag = false;
            List<BlockPos> possibleChambers = new ArrayList<>();
            while (!flag) {
                for (BlockPos blockpos : BlockPos.getAllInBox(chamber.add(-4, 0, -4), chamber.add(4, 0, 4))) {
                    if (isUnfilledChamber(blockpos)) {
                        possibleChambers.add(blockpos);
                        flag = true;
                    }
                }
                if (!flag) {
                    chamber = chamber.up();
                    if (chamber.getY() > this.pos.getY()) {
                        return null;
                    }
                }
            }
            Collections.shuffle(possibleChambers);
            if (!possibleChambers.isEmpty()) {
                BlockPos newChamber = possibleChambers.get(0);
                if (newChamber != null && !isUnfilledChamber(newChamber)) {
                    IBlockState prev = world.getBlockState(newChamber);
                    if (prev.getBlock() == AMBlockRegistry.LEAFCUTTER_ANT_CHAMBER) {
                        int fungalLevel = prev.getValue(BlockLeafcutterAntChamber.FUNGUS);
                        world.setBlockState(newChamber, AMBlockRegistry.LEAFCUTTER_ANT_CHAMBER.getDefaultState().withProperty(BlockLeafcutterAntChamber.FUNGUS, Math.min(fungalLevel - 1, 0)), 3);
                        return prev;
                    }
                }
            }
        }
        return null;
    }

    public boolean isFullOfAnts() {
        return this.ants.size() == AMConfig.leafcutterAntColonySize;
    }

    public void angerAnts(@Nullable EntityLivingBase target, IBlockState blockState, AnthillReleaseState releaseState) {
        List<Entity> list = this.tryReleaseAnt(blockState, releaseState);
        if (target != null) {
            for (Entity entity : list) {
                if (entity instanceof EntityLeafcutterAnt) {
                    EntityLeafcutterAnt entityLeafcutterAnt = (EntityLeafcutterAnt) entity;
                    double distSq = target.getDistanceSq(entity.posX, entity.posY, entity.posZ);
                    if (distSq <= 256.0D) {
                        entityLeafcutterAnt.setAttackTarget(target);
                    }
                    entityLeafcutterAnt.setStayOutOfHiveCountdown(400);
                }
            }
        }

    }

    private List<Entity> tryReleaseAnt(IBlockState blockState, AnthillReleaseState releaseState) {
        List<Entity> list = Lists.newArrayList();
        this.ants.removeIf((ant) -> this.addAntToWorld(blockState, ant, list, releaseState));
        return list;
    }

    private boolean addAntToWorld(IBlockState anthillState, Ant antData, @Nullable List<Entity> spawnList, AnthillReleaseState releaseState) {
        BlockPos anthillPos = this.getPos();
        NBTTagCompound entityData = antData.entityData;
        entityData.removeTag("Passengers");
        entityData.removeTag("Leash");
        entityData.removeTag("UUID");
        BlockPos abovePos = anthillPos.up();
        IBlockState aboveState = this.world.getBlockState(abovePos);
        AxisAlignedBB collisionAbove = aboveState.getBlock().getCollisionBoundingBox(aboveState, this.world, abovePos);
        boolean blockedAbove = collisionAbove != null && collisionAbove != Block.NULL_AABB;
        if (blockedAbove && releaseState != AnthillReleaseState.EMERGENCY) {
            return false;
        } else {
            Entity entity = loadEntityAndExecute(entityData, this.world, (e) -> e);
            if (entity != null) {

                if (entity instanceof EntityLeafcutterAnt) {
                    EntityLeafcutterAnt entityLeafcutterAnt = (EntityLeafcutterAnt) entity;
                    entityLeafcutterAnt.setLeaf(false);
                    if (releaseState == AnthillReleaseState.HONEY_DELIVERED) {

                    }
                    if (spawnList != null) {
                        spawnList.add(entityLeafcutterAnt);
                    }

                    double d0 = (double) anthillPos.getX() + 0.5D;
                    double d1 = (double) anthillPos.getY() + 1.0D;
                    double d2 = (double) anthillPos.getZ() + 0.5D;
                    entity.setLocationAndAngles(d0, d1, d2, entity.rotationYaw, entity.rotationPitch);
                    if (((EntityLeafcutterAnt) entity).isQueen()) {
                        entityLeafcutterAnt.setStayOutOfHiveCountdown(400);
                    }
                }

                this.world.playSound(null, anthillPos.getX() + 0.5D, anthillPos.getY() + 0.5D, anthillPos.getZ() + 0.5D,
                        SoundEvents.BLOCK_GRASS_BREAK, SoundCategory.BLOCKS, 1.0F, 1.0F);
                return this.world.spawnEntity(entity);

            } else {
                return false;
            }
        }

    }

    public void tryEnterHive(EntityLeafcutterAnt ant, boolean withLeaf, int minOccupationTicks) {
        if (this.ants.size() < AMConfig.leafcutterAntColonySize) {
            ant.dismountRidingEntity();
            ant.removePassengers();
            NBTTagCompound tag = new NBTTagCompound();
            ant.writeToNBT(tag);
            if (withLeaf) {
                if (!world.isRemote && ant.getRNG().nextFloat() < AMConfig.leafcutterAntFungusGrowChance) {
                    growFungus();
                }
                leafFeedings++;
                if (leafFeedings >= AMConfig.leafcutterAntRepopulateFeedings && this.ants.size() < MathHelper.ceil(AMConfig.leafcutterAntColonySize * 0.5F) && hasQueen()) {
                    leafFeedings = 0;
                    this.ants.add(new Ant(new NBTTagCompound(), 0, 100, false));
                }
            }
            this.ants.add(new Ant(tag, minOccupationTicks, withLeaf ? 100 : 200, ant.isQueen()));
            if (this.world != null) {

                BlockPos soundPos = this.getPos();
                this.world.playSound(null, soundPos.getX() + 0.5D, soundPos.getY() + 0.5D, soundPos.getZ() + 0.5D,
                        SoundEvents.BLOCK_GRASS_PLACE, SoundCategory.BLOCKS, 1.0F, 1.0F);
            }

            ant.setDead();
        }
    }

    public boolean hasQueen() {
        for (Ant ant : ants) {
            if (ant.queen) {
                return true;
            }
        }
        return false;
    }

    public void releaseQueens() {
        this.ants.removeIf((ant) -> ant.queen && this.addAntToWorld(world.getBlockState(this.getPos()), ant, null, AnthillReleaseState.BEE_RELEASED));
    }

    public void tryEnterHive(EntityLeafcutterAnt ant, boolean withLeaf) {
        this.tryEnterHive(ant, withLeaf, 0);
    }

    public int getAntCount() {
        return this.ants.size();
    }

    @Override
    public void markDirty() {
        if (this.isNearFire()) {
            this.angerAnts(null, this.world.getBlockState(this.getPos()), AnthillReleaseState.EMERGENCY);
        }

        super.markDirty();
    }

    public boolean isNearFire() {
        if (this.world == null) {
            return false;
        } else {
            for (BlockPos blockpos : BlockPos.getAllInBox(this.pos.add(-1, -1, -1), this.pos.add(1, 1, 1))) {
                if (this.world.getBlockState(blockpos).getBlock() instanceof BlockFire) {
                    return true;
                }
            }

            return false;
        }
    }

    @Override
    public void update() {
        if (!world.isRemote) {
            tickAnts();
        }
    }

    public void growFungus() {
        BlockPos bottomChamber = this.getPos().down();
        while (world.getBlockState(bottomChamber.down()).getBlock() == AMBlockRegistry.LEAFCUTTER_ANT_CHAMBER && bottomChamber.getY() > 0) {
            bottomChamber = bottomChamber.down();
        }
        BlockPos chamber = bottomChamber;
        if (isUnfilledChamber(chamber)) {
            int fungalLevel = world.getBlockState(chamber).getValue(BlockLeafcutterAntChamber.FUNGUS);
            int fungalLevel2 = MathHelper.clamp(fungalLevel + 1 + world.rand.nextInt(1), 0, 5);
            world.setBlockState(chamber, AMBlockRegistry.LEAFCUTTER_ANT_CHAMBER.getDefaultState().withProperty(BlockLeafcutterAntChamber.FUNGUS, fungalLevel2));
        } else {
            boolean flag = false;
            List<BlockPos> possibleChambers = new ArrayList<>();
            while (!flag) {
                for (BlockPos blockpos : BlockPos.getAllInBox(chamber.add(-4, 0, -4), chamber.add(4, 0, 4))) {
                    if (isUnfilledChamber(blockpos)) {
                        possibleChambers.add(new BlockPos(blockpos));
                        flag = true;
                    }
                }
                if (!flag) {
                    chamber = chamber.up();
                    if (chamber.getY() > this.pos.getY()) {
                        return;
                    }
                }
            }
            Collections.shuffle(possibleChambers);
            if (!possibleChambers.isEmpty()) {
                BlockPos newChamber = possibleChambers.get(0);
                if (newChamber != null && isUnfilledChamber(newChamber)) {
                    int fungalLevel = world.getBlockState(newChamber).getValue(BlockLeafcutterAntChamber.FUNGUS);
                    int fungalLevel2 = MathHelper.clamp(fungalLevel + 1 + world.rand.nextInt(1), 0, 5);
                    world.setBlockState(newChamber, AMBlockRegistry.LEAFCUTTER_ANT_CHAMBER.getDefaultState().withProperty(BlockLeafcutterAntChamber.FUNGUS, fungalLevel2));
                }
            }
        }
    }

    private boolean isUnfilledChamber(BlockPos pos) {
        return world.getBlockState(pos).getBlock() == AMBlockRegistry.LEAFCUTTER_ANT_CHAMBER && world.getBlockState(pos).getValue(BlockLeafcutterAntChamber.FUNGUS) < 5;
    }

    private void tickAnts() {
        Iterator<Ant> iterator = this.ants.iterator();

        Ant ant;
        for (IBlockState blockstate = this.world.getBlockState(this.getPos()); iterator.hasNext(); ant.ticksInHive++) {
            ant = iterator.next();
            if (ant.ticksInHive > ant.minOccupationTicks && !ant.queen) {
                AnthillReleaseState mode = ant.entityData.getBoolean("HasNectar") ? AnthillReleaseState.HONEY_DELIVERED : AnthillReleaseState.BEE_RELEASED;
                if (this.addAntToWorld(blockstate, ant, null, mode)) {
                    iterator.remove();
                }
            }
        }

    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        this.ants.clear();
        this.leafFeedings = compound.getInteger("LeafFeedings");
        NBTTagList listnbt = compound.getTagList("Ants", 10);

        for (int i = 0; i < listnbt.tagCount(); ++i) {
            NBTTagCompound antTag = listnbt.getCompoundTagAt(i);
            Ant entry = new Ant(antTag.getCompoundTag("EntityData"), antTag.getInteger("TicksInHive"), antTag.getInteger("MinOccupationTicks"), antTag.getBoolean("Queen"));
            this.ants.add(entry);
        }
    }

    public NBTTagList getAnts() {
        NBTTagList listnbt = new NBTTagList();

        for (Ant ant : this.ants) {
            ant.entityData.removeTag("UUID");
            NBTTagCompound tag = new NBTTagCompound();
            tag.setTag("EntityData", ant.entityData);
            tag.setInteger("TicksInHive", ant.ticksInHive);
            tag.setInteger("MinOccupationTicks", ant.minOccupationTicks);
            tag.setBoolean("Queen", ant.queen);
            listnbt.appendTag(tag);
        }

        return listnbt;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setTag("Ants", this.getAnts());
        compound.setInteger("LeafFeedings", leafFeedings);
        return compound;
    }

    static class Ant {
        private final NBTTagCompound entityData;
        private final int minOccupationTicks;
        private int ticksInHive;
        private boolean queen;

        private Ant(NBTTagCompound data, int ticksInHive, int minOccupationTicks, boolean queen) {
            data.removeTag("UUID");
            this.entityData = data;
            this.ticksInHive = ticksInHive;
            this.minOccupationTicks = minOccupationTicks;
            this.queen = queen;
        }
    }

    /* To avoid ants not being mapped to vanilla, we have to handle this seperately than the default entitytype implementation.*/
    @Nullable
    public static Entity loadEntityAndExecute(NBTTagCompound compound, World worldIn, com.google.common.base.Function<Entity, Entity> processor) {
        com.google.common.base.Optional<Entity> loaded = loadEntity(compound, worldIn).transform(processor::apply);
        if (!loaded.isPresent()) {
            return null;
        }
        Entity entity = loaded.get();
        if (compound.hasKey("Passengers", 9)) {
            NBTTagList listnbt = compound.getTagList("Passengers", 10);
            for (int i = 0; i < listnbt.tagCount(); ++i) {
                Entity passenger = loadEntityAndExecute(listnbt.getCompoundTagAt(i), worldIn, processor);
                if (passenger != null) {
                    passenger.startRiding(entity, true);
                }
            }
        }
        return entity;
    }

    private static com.google.common.base.Optional<Entity> loadEntity(NBTTagCompound compound, World worldIn) {
        try {
            return loadEntityUnchecked(compound, worldIn);
        } catch (RuntimeException runtimeexception) {
            return com.google.common.base.Optional.absent();
        }
    }

    public static com.google.common.base.Optional<Entity> loadEntityUnchecked(NBTTagCompound compound, World worldIn) {
        EntityLeafcutterAnt leafcutterAnt = (EntityLeafcutterAnt) AMEntityRegistry.LEAFCUTTER_ANT.newInstance(worldIn);
        leafcutterAnt.readFromNBT(compound);
        return com.google.common.base.Optional.of(leafcutterAnt);
    }
}
