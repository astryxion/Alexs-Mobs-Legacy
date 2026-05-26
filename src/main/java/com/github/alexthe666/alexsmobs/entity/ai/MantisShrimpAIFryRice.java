package com.github.alexthe666.alexsmobs.entity.ai;

import com.github.alexthe666.alexsmobs.entity.EntityMantisShrimp;
import com.github.alexthe666.alexsmobs.item.AMItemRegistry;
import com.github.alexthe666.alexsmobs.misc.AMTagRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFurnace;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.ai.EntityAIMoveToBlock;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class MantisShrimpAIFryRice extends EntityAIMoveToBlock {

    private final EntityMantisShrimp mantisShrimp;
    private boolean wasLitPrior = false;
    private int cookingTicks = 0;

    public MantisShrimpAIFryRice(EntityMantisShrimp entityMantisShrimp) {
        super(entityMantisShrimp, 1.0D, 8);
        this.mantisShrimp = entityMantisShrimp;
    }

    private static boolean isFurnaceBlock(Block block) {
        return block == Blocks.FURNACE || block == Blocks.LIT_FURNACE;
    }

    private static IBlockState setFurnaceLit(IBlockState state, boolean lit) {
        if (!isFurnaceBlock(state.getBlock())) {
            return state;
        }
        Block target = lit ? Blocks.LIT_FURNACE : Blocks.FURNACE;
        EnumFacing facing = state.getValue(BlockHorizontal.FACING);
        return target.getDefaultState().withProperty(BlockHorizontal.FACING, facing);
    }

    @Override
    public void resetTask() {
        cookingTicks = 0;
        if (!wasLitPrior && this.destinationBlock != null) {
            BlockPos blockpos = this.destinationBlock;
            IBlockState state = mantisShrimp.world.getBlockState(blockpos);
            if (isFurnaceBlock(state.getBlock())) {
                mantisShrimp.world.setBlockState(blockpos, setFurnaceLit(state, false));
            }
        }
        super.resetTask();
    }

    @Override
    public void updateTask() {
        super.updateTask();
        if (this.destinationBlock == null) {
            return;
        }
        BlockPos blockpos = this.destinationBlock;
        if (this.getIsAboveDestination()) {
            IBlockState state = mantisShrimp.world.getBlockState(blockpos);
            if (mantisShrimp.punchProgress == 0) {
                mantisShrimp.punch();
            }
            if (isFurnaceBlock(state.getBlock()) && !wasLitPrior) {
                mantisShrimp.world.setBlockState(blockpos, setFurnaceLit(state, true));
            }
            cookingTicks++;
            if (cookingTicks > 200) {
                cookingTicks = 0;
                ItemStack rice = new ItemStack(AMItemRegistry.SHRIMP_FRIED_RICE);
                rice.setCount(mantisShrimp.getHeldItemMainhand().getCount());
                mantisShrimp.setHeldItem(EnumHand.MAIN_HAND, rice);
            }
        } else {
            cookingTicks = 0;
        }
    }

    @Override
    public boolean shouldExecute() {
        return AMTagRegistry.itemInTag(AMTagRegistry.SHRIMP_RICE_FRYABLES, this.mantisShrimp.getHeldItemMainhand().getItem())
                && !mantisShrimp.isSitting() && super.shouldExecute();
    }

    @Override
    public boolean shouldContinueExecuting() {
        return AMTagRegistry.itemInTag(AMTagRegistry.SHRIMP_RICE_FRYABLES, this.mantisShrimp.getHeldItemMainhand().getItem())
                && !mantisShrimp.isSitting() && super.shouldContinueExecuting();
    }

    @Override
    protected boolean shouldMoveTo(World worldIn, BlockPos pos) {
        if (!worldIn.isAirBlock(pos.up())) {
            return false;
        } else {
            IBlockState blockstate = worldIn.getBlockState(pos);
            Block block = blockstate.getBlock();
            if (isFurnaceBlock(block)) {
                wasLitPrior = block == Blocks.LIT_FURNACE;
                return true;
            }
            return block == Blocks.FIRE;
        }
    }
}
