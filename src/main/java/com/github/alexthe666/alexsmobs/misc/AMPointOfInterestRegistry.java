package com.github.alexthe666.alexsmobs.misc;

import com.github.alexthe666.alexsmobs.block.AMBlockRegistry;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * 1.12 replacement for 1.16 {@code PointOfInterestType} / {@code PointOfInterestManager#findAll}.
 * Only scans loaded chunks (unlike a naive cube scan over unloaded columns).
 */
public class AMPointOfInterestRegistry {

    @FunctionalInterface
    public interface BlockMatcher {
        boolean test(World world, BlockPos pos, IBlockState state);
    }

    public static List<BlockPos> findAll(World world, BlockPos origin, int range, BlockMatcher matcher) {
        List<BlockPos> out = new ArrayList<>();
        forEachInRange(world, origin, range, matcher, (pos, distSq) -> out.add(pos));
        return out;
    }

    /**
     * Nearest matching block within {@code range}, or null. Prefer this over {@link #findAll} for mob AI.
     */
    @Nullable
    public static BlockPos findClosest(World world, BlockPos origin, int range, BlockMatcher matcher) {
        final BlockPos[] closest = {null};
        final double[] closestDistSq = {Double.MAX_VALUE};
        forEachInRange(world, origin, range, matcher, (pos, distSq) -> {
            if (distSq < closestDistSq[0]) {
                closestDistSq[0] = distSq;
                closest[0] = pos;
            }
        });
        return closest[0];
    }

    @FunctionalInterface
    private interface MatchConsumer {
        void accept(BlockPos pos, double distSq);
    }

    private static void forEachInRange(World world, BlockPos origin, int range, BlockMatcher matcher, MatchConsumer consumer) {
        int r = range;
        long rSq = (long) r * (long) r;
        int ox = origin.getX();
        int oy = origin.getY();
        int oz = origin.getZ();
        int minY = Math.max(0, oy - r);
        int maxY = Math.min(255, oy + r);
        int chunkMinX = (ox - r) >> 4;
        int chunkMaxX = (ox + r) >> 4;
        int chunkMinZ = (oz - r) >> 4;
        int chunkMaxZ = (oz + r) >> 4;

        for (int chunkX = chunkMinX; chunkX <= chunkMaxX; chunkX++) {
            for (int chunkZ = chunkMinZ; chunkZ <= chunkMaxZ; chunkZ++) {
                if (!world.isBlockLoaded(new BlockPos((chunkX << 4) + 8, 64, (chunkZ << 4) + 8))) {
                    continue;
                }
                int baseX = chunkX << 4;
                int baseZ = chunkZ << 4;
                for (int x = baseX; x < baseX + 16; x++) {
                    for (int z = baseZ; z < baseZ + 16; z++) {
                        for (int y = minY; y <= maxY; y++) {
                            long dx = (long) x - ox;
                            long dy = (long) y - oy;
                            long dz = (long) z - oz;
                            long distSq = dx * dx + dy * dy + dz * dz;
                            if (distSq > rSq) {
                                continue;
                            }
                            BlockPos pos = new BlockPos(x, y, z);
                            IBlockState state = world.getBlockState(pos);
                            if (matcher.test(world, pos, state)) {
                                consumer.accept(pos, distSq);
                            }
                        }
                    }
                }
            }
        }
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
