package com.github.alexthe666.alexsmobs.block;

import com.github.alexthe666.alexsmobs.AlexsMobs;
import com.github.alexthe666.alexsmobs.entity.EntityLeafcutterAnt;
import com.github.alexthe666.alexsmobs.item.AMItemRegistry;
import com.github.alexthe666.alexsmobs.tileentity.TileEntityLeafcutterAnthill;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

/**
 * Fungus chamber for leafcutter ants. In 1.16 {@code Blocks.SHROOMLIGHT} suppressed anger; here {@link Blocks#GLOWSTONE}
 * is used (no shroomlight in 1.12). {@link Tags.Blocks#DIRT} is matched via {@link OreDictionary} {@code dirt} + common soil blocks.
 */
public class BlockLeafcutterAntChamber extends Block {

    public static final PropertyInteger FUNGUS = PropertyInteger.create("fungus", 0, 5);

    private static final int POI_SEARCH_RADIUS = 50;

    public BlockLeafcutterAntChamber() {
        super(Material.GROUND);
        this.setDefaultState(this.blockState.getBaseState().withProperty(FUNGUS, 0));
        this.setSoundType(SoundType.GROUND);
        this.setHardness(4.0F);
        this.setResistance(4.0F);
        this.setHarvestLevel("shovel", 0);
        this.setTickRandomly(true);
        this.setCreativeTab(AlexsMobs.TAB);
        this.setRegistryName("alexsmobs:leafcutter_ant_chamber");
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FUNGUS);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(FUNGUS);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(FUNGUS, MathHelper.clamp(meta, 0, 5));
    }

    /**
     * {@code Blocks.SHROOMLIGHT} (1.16): closest 1.12 bright nether-style block for the "fungus light calms ants" check.
     */
    private static boolean isShroomlightEquivalent(net.minecraft.block.Block b) {
        return b == Blocks.GLOWSTONE;
    }

    /**
     * Mirrors Forge {@code Tags.Blocks.DIRT} coverage for colony spread target blocks.
     */
    private static boolean isDirtTaggedLike(World world, BlockPos offset) {
        IBlockState st = world.getBlockState(offset);
        net.minecraft.block.Block b = st.getBlock();
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
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        int fungalLevel = state.getValue(FUNGUS);
        if (fungalLevel == 5) {
            boolean shroomlight = false;
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        BlockPos blockpos = pos.add(dx, dy, dz);
                        if (isShroomlightEquivalent(worldIn.getBlockState(blockpos).getBlock())) {
                            shroomlight = true;
                        }
                    }
                }
            }
            if (!shroomlight) {
                this.angerNearbyAnts(worldIn, pos);
            }
            worldIn.setBlockState(pos, state.withProperty(FUNGUS, 0), 2);
            if (!worldIn.isRemote) {
                if (worldIn.rand.nextInt(2) == 0) {
                    EnumFacing dir = EnumFacing.values()[worldIn.rand.nextInt(6)];
                    if (worldIn.getBlockState(pos.up()).getBlock() == AMBlockRegistry.LEAFCUTTER_ANTHILL) {
                        dir = EnumFacing.DOWN;
                    }
                    BlockPos offset = pos.offset(dir);
                    if (isDirtTaggedLike(worldIn, offset) && !worldIn.canSeeSky(offset)) {
                        worldIn.setBlockState(offset, this.getDefaultState());
                    }
                }
                Block.spawnAsEntity(worldIn, pos, new ItemStack(AMItemRegistry.GONGYLIDIA));
            }
            return true;
        }
        return false;
    }

    @Override
    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random random) {
        if (!this.isSurroundingChunksLoaded(worldIn, pos, 3)) {
            return;
        }
        if (worldIn.canSeeSky(pos.up())) {
            worldIn.setBlockState(pos, Blocks.DIRT.getDefaultState(), 2);
        }
    }

    /**
     * Forge parity with 1.16 {@code isAreaLoaded(pos, 3)} — avoids ticking unloaded neighbors.
     */
    private boolean isSurroundingChunksLoaded(World world, BlockPos pos, int radius) {
        int minCx = (pos.getX() - radius) >> 4;
        int maxCx = (pos.getX() + radius) >> 4;
        int minCz = (pos.getZ() - radius) >> 4;
        int maxCz = (pos.getZ() + radius) >> 4;
        for (int cx = minCx; cx <= maxCx; cx++) {
            for (int cz = minCz; cz <= maxCz; cz++) {
                if (!world.isBlockLoaded(new BlockPos(cx << 4, 0, cz << 4))) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void harvestBlock(World worldIn, EntityPlayer player, BlockPos pos, IBlockState state, @Nullable TileEntity te, ItemStack stack) {
        super.harvestBlock(worldIn, player, pos, state, te, stack);
        this.angerNearbyAnts(worldIn, pos);
    }

    private void angerNearbyAnts(World world, BlockPos pos) {
        AxisAlignedBB searchBox = new AxisAlignedBB(pos).grow(20.0D, 6.0D, 20.0D);
        List<EntityLeafcutterAnt> list = world.getEntitiesWithinAABB(EntityLeafcutterAnt.class, searchBox);
        List<EntityPlayer> list1 = world.getEntitiesWithinAABB(EntityPlayer.class, searchBox);
        if (list1.isEmpty()) {
            return;
        }
        EntityPlayer player = list1.get(world.rand.nextInt(list1.size()));
        for (EntityLeafcutterAnt beeentity : list) {
            if (beeentity.getAttackTarget() == null) {
                beeentity.setAttackTarget(player);
            }
        }
        if (!world.isRemote) {
            angerAnthillsInRange(world, pos, player);
        }
    }

    /**
     * 1.12 replacement for iterating {@code PointOfInterestManager} "leafcutter ant hill" entries within 50 blocks (1.16 POI API).
     * Scans {@link World#loadedTileEntityList} (no public per-chunk map in 1.12).
     */
    private static void angerAnthillsInRange(World world, BlockPos origin, EntityPlayer player) {
        final int r = POI_SEARCH_RADIUS;
        long rSq = (long) r * (long) r;
        for (TileEntity te : world.loadedTileEntityList) {
            if (te == null || te.isInvalid() || !(te instanceof TileEntityLeafcutterAnthill)) {
                continue;
            }
            BlockPos tp = te.getPos();
            long dx = (long) tp.getX() - origin.getX();
            long dy = (long) tp.getY() - origin.getY();
            long dz = (long) tp.getZ() - origin.getZ();
            if (dx * dx + dy * dy + dz * dz > rSq) {
                continue;
            }
            ((TileEntityLeafcutterAnthill) te).angerAnts(player, world.getBlockState(tp), TileEntityLeafcutterAnthill.AnthillReleaseState.EMERGENCY);
        }
    }
}
