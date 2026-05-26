package com.github.alexthe666.alexsmobs.entity.ai;

import com.github.alexthe666.alexsmobs.AlexsMobs;
import com.github.alexthe666.alexsmobs.config.AMConfig;
import com.github.alexthe666.alexsmobs.entity.EntityRaccoon;
import net.minecraft.block.Block;
import net.minecraft.block.BlockChest;
import net.minecraft.entity.ai.EntityAIMoveToBlock;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Forge 1.12.2 port of 1.16 {@code AnimalAILootChests} / {@code MoveToBlockGoal}.
 */
public class AnimalAILootChests extends EntityAIMoveToBlock {

    private final EntityAnimal entity;
    private final ILootsChests chestLooter;
    private boolean hasOpenedChest = false;

    public AnimalAILootChests(EntityAnimal entity, int range) {
        super(entity, 1.0D, range);
        this.entity = entity;
        this.chestLooter = (ILootsChests) entity;
    }

    public boolean isChestRaidable(World world, BlockPos pos) {
        if (world.getBlockState(pos).getBlock() instanceof BlockChest) {
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof IInventory) {
                IInventory inventory = (IInventory) te;
                try {
                    if (!inventory.isEmpty() && chestLooter.isLootable(inventory)) {
                        return true;
                    }
                } catch (Exception e) {
                    AlexsMobs.LOGGER.warn("Alex's Mobs stopped a " + te.getClass().getSimpleName() + " from causing a crash during access");
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    @Override
    public boolean shouldExecute() {
        if (this.entity instanceof EntityTameable && ((EntityTameable) this.entity).isTamed()) {
            return false;
        }
        if (!AMConfig.raccoonsStealFromChests) {
            return false;
        }
        if (!this.entity.getHeldItem(EnumHand.MAIN_HAND).isEmpty()) {
            return false;
        }
        if (this.runDelay <= 0) {
            if (!net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(this.entity.world, this.entity)) {
                return false;
            }
        }
        return super.shouldExecute();
    }

    @Override
    public boolean shouldContinueExecuting() {
        return super.shouldContinueExecuting() && this.entity.getHeldItem(EnumHand.MAIN_HAND).isEmpty();
    }

    public boolean canSeeChest() {
        if (this.destinationBlock == null) {
            return false;
        }
        Vec3d start = new Vec3d(this.entity.posX, this.entity.getPositionEyes(1.0F).y, this.entity.posZ);
        Vec3d end = new Vec3d(this.destinationBlock.getX() + 0.5D, this.destinationBlock.getY() + 0.5D, this.destinationBlock.getZ() + 0.5D);
        net.minecraft.util.math.RayTraceResult raytraceresult = entity.world.rayTraceBlocks(start, end, false, true, false);
        if (raytraceresult != null && raytraceresult.typeOfHit == net.minecraft.util.math.RayTraceResult.Type.BLOCK) {
            BlockPos pos = raytraceresult.getBlockPos();
            return pos.equals(this.destinationBlock) || entity.world.isAirBlock(pos)
                    || entity.world.getTileEntity(pos) == entity.world.getTileEntity(this.destinationBlock);
        }
        return true;
    }

    public ItemStack getFoodFromInventory(IInventory inventory, Random random) {
        List<ItemStack> items = new ArrayList<>();
        for (int i = 0; i < inventory.getSizeInventory(); i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (chestLooter.shouldLootItem(stack)) {
                items.add(stack);
            }
        }
        if (items.isEmpty()) {
            return ItemStack.EMPTY;
        } else if (items.size() == 1) {
            return items.get(0);
        } else {
            return items.get(random.nextInt(items.size()));
        }
    }

    @Override
    public void updateTask() {
        super.updateTask();
        if (this.destinationBlock != null) {
            TileEntity te = this.entity.world.getTileEntity(this.destinationBlock);
            if (te instanceof IInventory) {
                IInventory feeder = (IInventory) te;
                double distance = this.entity.getDistanceSq(this.destinationBlock.getX() + 0.5F, this.destinationBlock.getY() + 0.5F, this.destinationBlock.getZ() + 0.5F);
                if (canSeeChest()) {
                    if (this.getIsAboveDestination() && distance <= 3) {
                        toggleChest(feeder, false);
                        ItemStack stack = getFoodFromInventory(feeder, this.entity.world.rand);
                        if (stack.isEmpty()) {
                            this.resetTask();
                        } else {
                            ItemStack duplicate = stack.copy();
                            duplicate.setCount(1);
                            if (!this.entity.getHeldItem(EnumHand.MAIN_HAND).isEmpty() && !this.entity.world.isRemote) {
                                this.entity.entityDropItem(this.entity.getHeldItem(EnumHand.MAIN_HAND), 0.0F);
                            }
                            this.entity.setHeldItem(EnumHand.MAIN_HAND, duplicate);
                            if (entity instanceof EntityRaccoon) {
                                ((EntityRaccoon) entity).lookForWaterBeforeEatingTimer = 10;
                            }
                            stack.shrink(1);
                            this.resetTask();
                        }
                    } else {
                        if (distance < 5 && !hasOpenedChest) {
                            hasOpenedChest = true;
                            toggleChest(feeder, true);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void resetTask() {
        super.resetTask();
        if (this.destinationBlock != null) {
            TileEntity te = this.entity.world.getTileEntity(this.destinationBlock);
            if (te instanceof IInventory) {
                toggleChest((IInventory) te, false);
            }
        }
        this.destinationBlock = null;
        this.hasOpenedChest = false;
    }

    @Override
    protected boolean shouldMoveTo(World worldIn, BlockPos pos) {
        return pos != null && isChestRaidable(worldIn, pos);
    }

    public void toggleChest(IInventory te, boolean open) {
        if (te instanceof TileEntityChest && this.destinationBlock != null) {
            TileEntityChest chest = (TileEntityChest) te;
            Block block = chest.getBlockType();
            if (open) {
                this.entity.world.addBlockEvent(this.destinationBlock, block, 1, 1);
            } else {
                this.entity.world.addBlockEvent(this.destinationBlock, block, 1, 0);
            }
            chest.numPlayersUsing = open ? 1 : 0;
            this.entity.world.notifyNeighborsOfStateChange(this.destinationBlock, block, true);
            this.entity.world.notifyNeighborsOfStateChange(this.destinationBlock.down(), block, true);
        }
    }
}
