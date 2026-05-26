package com.github.alexthe666.alexsmobs.item;

import com.github.alexthe666.alexsmobs.AlexsMobs;
import com.github.alexthe666.alexsmobs.block.AMBlockRegistry;
import com.github.alexthe666.alexsmobs.config.AMConfig;
import com.github.alexthe666.alexsmobs.entity.EntityLeafcutterAnt;
import com.github.alexthe666.alexsmobs.tileentity.TileEntityLeafcutterAnthill;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;

public class ItemLeafcutterPupa extends Item {

    public ItemLeafcutterPupa() {
        setCreativeTab(AlexsMobs.TAB);
    }

    private static boolean isDirtTaggedLike(World world, BlockPos offset) {
        IBlockState st = world.getBlockState(offset);
        Block b = st.getBlock();
        if (b == Blocks.GRASS || b == Blocks.MYCELIUM || b == Blocks.FARMLAND) {
            return true;
        }
        if (b == Blocks.DIRT) {
            return true;
        }
        ItemStack probe = new ItemStack(Item.getItemFromBlock(b), 1, OreDictionary.WILDCARD_VALUE);
        int[] ids = OreDictionary.getOreIDs(probe);
        for (int id : ids) {
            if ("dirt".equals(OreDictionary.getOreName(id))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack held = player.getHeldItem(hand);
        IBlockState blockstate = world.getBlockState(pos);
        if (isDirtTaggedLike(world, pos) && isDirtTaggedLike(world, pos.down())) {
            world.playSound(player, pos, SoundEvents.BLOCK_WOOD_BREAK, SoundCategory.BLOCKS, 1.0F, 1.0F);
            if (!world.isRemote) {
                world.setBlockState(pos, AMBlockRegistry.LEAFCUTTER_ANTHILL.getDefaultState(), 11);
                world.setBlockState(pos.down(), AMBlockRegistry.LEAFCUTTER_ANT_CHAMBER.getDefaultState(), 11);
                TileEntity tileentity = world.getTileEntity(pos);
                if (tileentity instanceof TileEntityLeafcutterAnthill) {
                    TileEntityLeafcutterAnthill beehivetileentity = (TileEntityLeafcutterAnthill) tileentity;
                    int j = Math.min(3, AMConfig.leafcutterAntColonySize);
                    for (int k = 0; k < j; ++k) {
                        EntityLeafcutterAnt beeentity = new EntityLeafcutterAnt(world);
                        beeentity.setQueen(k == 0);
                        beehivetileentity.tryEnterHive(beeentity, false, 100);
                    }
                }
                if (player != null && !player.capabilities.isCreativeMode) {
                    held.shrink(1);
                }
            }
            return world.isRemote ? EnumActionResult.PASS : EnumActionResult.SUCCESS;
        }
        return EnumActionResult.PASS;
    }
}
