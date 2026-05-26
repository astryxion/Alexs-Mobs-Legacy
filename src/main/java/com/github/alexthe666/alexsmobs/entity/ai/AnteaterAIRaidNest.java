package com.github.alexthe666.alexsmobs.entity.ai;

import com.github.alexthe666.alexsmobs.block.AMBlockRegistry;
import com.github.alexthe666.alexsmobs.block.BlockLeafcutterAntChamber;
import com.github.alexthe666.alexsmobs.block.BlockLeafcutterAnthill;
import com.github.alexthe666.alexsmobs.entity.EntityAnteater;
import com.github.alexthe666.alexsmobs.entity.EntityLeafcutterAnt;
import com.github.alexthe666.alexsmobs.item.AMItemRegistry;
import com.github.alexthe666.alexsmobs.tileentity.TileEntityLeafcutterAnthill;
import com.github.alexthe666.citadel.animation.IAnimatedEntity;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.ai.EntityAIMoveToBlock;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraftforge.event.ForgeEventFactory;

import java.util.List;

/**
 * Forge 1.12.2 port of 1.16 {@code AnteaterAIRaidNest} / {@code MoveToBlockGoal}.
 */
public class AnteaterAIRaidNest extends EntityAIMoveToBlock {

    public static final ResourceLocation ANTEATER_REWARD = new ResourceLocation("alexsmobs", "gameplay/anteater_reward");
    private final EntityAnteater anteater;
    private int idleAtHiveTime = 0;
    private boolean isAboveDestinationAnteater;
    private boolean shootTongue;
    private int maxEatingTime = 0;

    public AnteaterAIRaidNest(EntityAnteater anteater) {
        super(anteater, 1.0D, 32);
        this.anteater = anteater;
    }

    private static List<ItemStack> getItemStacks(EntityAnteater anteater) {
        if (!(anteater.world instanceof WorldServer)) {
            return java.util.Collections.emptyList();
        }
        WorldServer sw = (WorldServer) anteater.world;
        LootTable loottable = sw.getLootTableManager().getLootTableFromLocation(ANTEATER_REWARD);
        return loottable.generateLootForPools(anteater.getRNG(), new LootContext.Builder(sw).withLootedEntity(anteater).build());
    }

    private void dropDigItems() {
        List<ItemStack> lootList = getItemStacks(anteater);
        if (lootList.size() > 0) {
            for (ItemStack stack : lootList) {
                EntityItem e = this.anteater.entityDropItem(stack.copy(), 0.0F);
                e.isAirBorne = true;
                e.motionX *= 0.2D;
                e.motionY *= 0.2D;
                e.motionZ *= 0.2D;
            }
        }
    }

    @Override
    public boolean shouldExecute() {
        return !anteater.isChild() && anteater.eatAntCooldown <= 0 && super.shouldExecute();
    }

    @Override
    public boolean shouldContinueExecuting() {
        return anteater.eatAntCooldown <= 0 && super.shouldContinueExecuting();
    }

    @Override
    public void startExecuting() {
        super.startExecuting();
        maxEatingTime = 150 + anteater.getRNG().nextInt(200);
    }

    @Override
    public void resetTask() {
        super.resetTask();
        idleAtHiveTime = 0;
        maxEatingTime = 150 + anteater.getRNG().nextInt(200);
        anteater.setLeaning(false);
        anteater.resetAntCooldown();
    }

    public double acceptedDistance() {
        return 1.2D;
    }

    @Override
    public void updateTask() {
        super.updateTask();
        BlockPos blockpos = this.destinationBlock;
        if (blockpos == null) {
            return;
        }
        if (!isWithinXZDist(blockpos, anteater.getPositionVector(), this.acceptedDistance())) {
            this.isAboveDestinationAnteater = false;
        } else {
            this.isAboveDestinationAnteater = true;
        }

        if (this.getIsAboveDestination()) {
            anteater.getLookHelper().setLookPosition(
                    blockpos.getX() + 0.5D,
                    blockpos.getY() - 1,
                    blockpos.getZ() + 0.5D,
                    30.0F,
                    30.0F);
            if (this.idleAtHiveTime >= 20 && this.idleAtHiveTime % 20 == 0) {
                shootTongue = anteater.getRNG().nextInt(2) == 0;
                if (shootTongue) {
                    this.eatHive();
                } else {
                    this.breakHiveEffect();
                }
            }
            ++this.idleAtHiveTime;
            if (shootTongue && anteater.getAnimation() == IAnimatedEntity.NO_ANIMATION) {
                anteater.setLeaning(false);
                anteater.setAnimation(EntityAnteater.ANIMATION_TOUNGE_IDLE);
            } else if (anteater.getAnimation() == IAnimatedEntity.NO_ANIMATION) {
                anteater.setLeaning(true);
                anteater.setAnimation(anteater.getRNG().nextBoolean() ? EntityAnteater.ANIMATION_SLASH_L : EntityAnteater.ANIMATION_SLASH_R);
            }
            if (this.idleAtHiveTime > maxEatingTime) {
                resetTask();
            }
        }
    }

    private boolean isWithinXZDist(BlockPos blockpos, Vec3d positionVec, double distance) {
        return blockpos.distanceSq(positionVec.x, blockpos.getY(), positionVec.z) < distance * distance;
    }

  @Override
    protected boolean getIsAboveDestination() {
        return this.isAboveDestinationAnteater;
    }

    private void breakHiveEffect() {
        if (destinationBlock == null) {
            return;
        }
        if (ForgeEventFactory.getMobGriefingEvent(anteater.world, anteater)) {
            IBlockState blockstate = anteater.world.getBlockState(this.destinationBlock);
            if (blockstate.getBlock() == AMBlockRegistry.LEAFCUTTER_ANTHILL) {
                TileEntity te = anteater.world.getTileEntity(this.destinationBlock);
                if (te instanceof TileEntityLeafcutterAnthill) {
                    TileEntityLeafcutterAnthill anthill = (TileEntityLeafcutterAnthill) te;
                    anthill.angerAntsBecauseAnteater(anteater, blockstate, TileEntityLeafcutterAnthill.AnthillReleaseState.EMERGENCY);
                    anteater.world.setBlockState(this.destinationBlock, Blocks.AIR.getDefaultState(), 2);
                    if (blockstate.getBlock() instanceof BlockLeafcutterAnthill) {
                        anteater.world.setBlockState(this.destinationBlock, blockstate, 3);
                    }
                    dropDigItems();
                }
            } else if (blockstate.getBlock() == AMBlockRegistry.LEAFCUTTER_ANT_CHAMBER) {
                anteater.world.setBlockState(this.destinationBlock, Blocks.AIR.getDefaultState(), 2);
                anteater.world.setBlockState(this.destinationBlock, blockstate, 3);
            }
        }
    }

    private void eatHive() {
        if (destinationBlock == null) {
            return;
        }
        if (ForgeEventFactory.getMobGriefingEvent(anteater.world, anteater)) {
            IBlockState blockstate = anteater.world.getBlockState(this.destinationBlock);
            if (blockstate.getBlock() == AMBlockRegistry.LEAFCUTTER_ANTHILL) {
                TileEntity te = anteater.world.getTileEntity(this.destinationBlock);
                if (te instanceof TileEntityLeafcutterAnthill) {
                    TileEntityLeafcutterAnthill anthill = (TileEntityLeafcutterAnthill) te;
                    anthill.angerAntsBecauseAnteater(anteater, blockstate, TileEntityLeafcutterAnthill.AnthillReleaseState.EMERGENCY);
                    anteater.world.notifyNeighborsOfStateChange(this.destinationBlock, blockstate.getBlock(), true);
                    if (!anthill.hasNoAnts()) {
                        IBlockState state = anthill.shrinkFungus();
                        if (state != null && state.getBlock() == AMBlockRegistry.LEAFCUTTER_ANT_CHAMBER && state.getValue(BlockLeafcutterAntChamber.FUNGUS) >= 5) {
                            ItemStack stack = new ItemStack(AMItemRegistry.GONGYLIDIA);
                            EntityItem itementity = new EntityItem(anteater.world, destinationBlock.getX() + anteater.getRNG().nextFloat(), destinationBlock.getY() + anteater.getRNG().nextFloat(), destinationBlock.getZ() + anteater.getRNG().nextFloat(), stack);
                            itementity.setDefaultPickupDelay();
                            anteater.world.spawnEntity(itementity);
                        }
                        anteater.setAntOnTongue(true);
                    }
                }
            } else if (blockstate.getBlock() == AMBlockRegistry.LEAFCUTTER_ANT_CHAMBER) {
                anteater.world.setBlockState(this.destinationBlock, Blocks.AIR.getDefaultState(), 2);
                if (blockstate.getValue(BlockLeafcutterAntChamber.FUNGUS) >= 5) {
                    ItemStack stack = new ItemStack(AMItemRegistry.GONGYLIDIA);
                    EntityItem itementity = new EntityItem(anteater.world, destinationBlock.getX() + anteater.getRNG().nextFloat(), destinationBlock.getY() + anteater.getRNG().nextFloat(), destinationBlock.getZ() + anteater.getRNG().nextFloat(), stack);
                    itementity.setDefaultPickupDelay();
                    anteater.world.spawnEntity(itementity);
                }
                anteater.world.setBlockState(this.destinationBlock, Blocks.DIRT.getDefaultState(), 3);
                anteater.setAntOnTongue(true);
            }
            double d0 = 15;
            for (EntityLeafcutterAnt leafcutter : anteater.world.getEntitiesWithinAABB(
                    EntityLeafcutterAnt.class,
                    new net.minecraft.util.math.AxisAlignedBB(
                            (double) destinationBlock.getX() - d0, (double) destinationBlock.getY() - d0, (double) destinationBlock.getZ() - d0,
                            (double) destinationBlock.getX() + d0, (double) destinationBlock.getY() + d0, (double) destinationBlock.getZ() + d0))) {
                leafcutter.setAngerTime(100);
                leafcutter.setAttackTarget(anteater);
                leafcutter.setStayOutOfHiveCountdown(400);
            }
        }
    }

    @Override
    protected boolean shouldMoveTo(World worldIn, BlockPos pos) {
        IBlockState state = worldIn.getBlockState(pos);
        if (state.getBlock() == AMBlockRegistry.LEAFCUTTER_ANT_CHAMBER) {
            return true;
        }
        if (state.getBlock() == AMBlockRegistry.LEAFCUTTER_ANTHILL) {
            TileEntity te = worldIn.getTileEntity(pos);
            return te instanceof TileEntityLeafcutterAnthill && this.isValidAnthill(pos, (TileEntityLeafcutterAnthill) te);
        }
        return false;
    }

    private boolean isValidAnthill(BlockPos pos, TileEntityLeafcutterAnthill blockEntity) {
        return blockEntity.hasAtleastThisManyAnts(2);
    }
}
