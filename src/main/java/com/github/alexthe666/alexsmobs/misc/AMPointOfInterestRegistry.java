package com.github.alexthe666.alexsmobs.misc;

import com.github.alexthe666.alexsmobs.block.AMBlockRegistry;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

/**
 * 1.12 replacement for 1.16 {@code PointOfInterestType} / {@code PointOfInterestManager#findAll}.
 * Scans blocks in a cube around {@code origin} (same practical range as POI search radius).
 */
public class AMPointOfInterestRegistry {

    @FunctionalInterface
    public interface BlockMatcher {
        boolean test(World world, BlockPos pos, IBlockState state);
    }

    public static List<BlockPos> findAll(World world, BlockPos origin, int range, BlockMatcher matcher) {
        List<BlockPos> out = new ArrayList<>();
        int r = range;
        int rSq = r * r;
        for (int dx = -r; dx <= r; dx++) {
            for (int dy = -r; dy <= r; dy++) {
                for (int dz = -r; dz <= r; dz++) {
                    if (dx * dx + dy * dy + dz * dz > rSq) {
                        continue;
                    }
                    BlockPos pos = origin.add(dx, dy, dz);
                    IBlockState state = world.getBlockState(pos);
                    if (matcher.test(world, pos, state)) {
                        out.add(pos);
                    }
                }
            }
        }
        return out;
    }

    public static boolean matchesEndPortalFrame(World world, BlockPos pos, IBlockState state) {
        return state.getBlock() == Blocks.END_PORTAL_FRAME;
    }

    public static boolean matchesBeacon(World world, BlockPos pos, IBlockState state) {
        return state.getBlock() == Blocks.BEACON;
    }

    public static boolean matchesLeafcutterAnthill(World world, BlockPos pos, IBlockState state) {
        return state.getBlock() == AMBlockRegistry.LEAFCUTTER_ANTHILL;
    }

    public static boolean matchesHummingbirdFeeder(World world, BlockPos pos, IBlockState state) {
        return state.getBlock() == AMBlockRegistry.HUMMINGBIRD_FEEDER;
    }
}
