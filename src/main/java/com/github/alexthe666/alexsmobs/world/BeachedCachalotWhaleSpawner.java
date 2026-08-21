package com.github.alexthe666.alexsmobs.world;

import com.github.alexthe666.alexsmobs.config.AMConfig;
import com.github.alexthe666.alexsmobs.config.BiomeConfig;
import com.github.alexthe666.alexsmobs.entity.AMEntityRegistry;
import com.github.alexthe666.alexsmobs.entity.EntityCachalotWhale;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;

import javax.annotation.Nullable;
import java.util.Random;

public class BeachedCachalotWhaleSpawner {
    private final Random random = new Random();
    private final WorldServer world;
    private int timer;
    private int delay;
    private int chance;

    public BeachedCachalotWhaleSpawner(WorldServer world) {
        this.world = world;
        this.timer = 1200;
        AMWorldData worldinfo = AMWorldData.get(world);
        this.delay = worldinfo.getBeachedCachalotSpawnDelay();
        this.chance = worldinfo.getBeachedCachalotSpawnChance();
        if (this.delay == 0 && this.chance == 0) {
            this.delay = AMConfig.beachedCachalotWhaleSpawnDelay;
            worldinfo.setBeachedCachalotSpawnDelay(this.delay);
            this.chance = 25;
            worldinfo.setBeachedCachalotSpawnChance(this.chance);
        }
    }

    public void tick() {
        if (AMConfig.beachedCachalotWhales && --this.timer <= 0 && world.isThundering()) {
            this.timer = 1200;
            AMWorldData worldinfo = AMWorldData.get(world);
            this.delay -= 1200;
            if (delay < 0) {
                delay = 0;
            }
            worldinfo.setBeachedCachalotSpawnDelay(this.delay);
            if (this.delay <= 0) {
                this.delay = AMConfig.beachedCachalotWhaleSpawnDelay;
                if (this.world.getGameRules().getBoolean("doMobSpawning")) {
                    int i = this.chance;
                    this.chance = MathHelper.clamp(this.chance + AMConfig.beachedCachalotWhaleSpawnChance, 5, 100);
                    worldinfo.setBeachedCachalotSpawnChance(this.chance);
                    if (this.random.nextInt(100) <= i && this.attemptSpawnWhale()) {
                        this.chance = AMConfig.beachedCachalotWhaleSpawnChance;
                    }
                }
            }
        }
    }

    private boolean attemptSpawnWhale() {
        EntityPlayer player = null;
        if (!this.world.playerEntities.isEmpty()) {
            player = this.world.playerEntities.get(this.random.nextInt(this.world.playerEntities.size()));
        }
        if (player == null) {
            return true;
        } else if (this.random.nextInt(5) != 0) {
            return false;
        } else {
            BlockPos blockpos = new BlockPos(player.posX, player.posY, player.posZ);
            BlockPos blockpos2 = this.func_221244_a(blockpos, 84);
            if (blockpos2 != null && this.func_226559_a_(blockpos2) && blockpos2.distanceSq(blockpos) > 225) {
                BlockPos upPos = new BlockPos(blockpos2.getX(), blockpos2.getY() + 2, blockpos2.getZ());
                EntityCachalotWhale whale = (EntityCachalotWhale) AMEntityRegistry.CACHALOT_WHALE.newInstance(world);
                whale.setLocationAndAngles(upPos.getX() + 0.5D, upPos.getY() + 0.5D, upPos.getZ() + 0.5D, random.nextFloat() * 360 - 180F, 0);
                whale.onInitialSpawn(world.getDifficultyForLocation(upPos), null);
                whale.setBeached(true);
                AMWorldData worldinfo = AMWorldData.get(world);
                worldinfo.setBeachedCachalotID(whale.getUniqueID());
                whale.setHomePosAndDistance(upPos, 16);
                whale.setDespawnBeach(true);
                world.spawnEntity(whale);
                return true;
            }
            return false;
        }
    }

    @Nullable
    private BlockPos func_221244_a(BlockPos p_221244_1_, int p_221244_2_) {
        BlockPos blockpos = null;

        for (int i = 0; i < 10; ++i) {
            int j = p_221244_1_.getX() + this.random.nextInt(p_221244_2_ * 2) - p_221244_2_;
            int k = p_221244_1_.getZ() + this.random.nextInt(p_221244_2_ * 2) - p_221244_2_;
            int l = this.world.getHeight(new BlockPos(j, 0, k)).getY();
            BlockPos blockpos1 = new BlockPos(j, l, k);
            Biome biome = world.getBiome(blockpos1);
            IBlockState below = world.getBlockState(blockpos1.down());
            if (BiomeConfig.test(BiomeConfig.cachalot_whale_beached_spawns, biome)
                    && below.isSideSolid(world, blockpos1.down(), EnumFacing.UP)
                    && world.getLight(blockpos1) > 7) {
                blockpos = blockpos1;
                break;
            }
        }

        return blockpos;
    }

    private boolean func_226559_a_(BlockPos p_226559_1_) {
        for (BlockPos blockpos : BlockPos.getAllInBox(p_226559_1_, p_226559_1_.add(1, 2, 1))) {
            IBlockState state = this.world.getBlockState(blockpos);
            if (!state.getCollisionBoundingBox(world, blockpos).equals(net.minecraft.block.Block.NULL_AABB)
                    || state.getMaterial() == Material.WATER
                    || state.getMaterial() == Material.LAVA) {
                return false;
            }
        }
        return true;
    }
}
