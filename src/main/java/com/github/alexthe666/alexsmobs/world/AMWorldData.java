package com.github.alexthe666.alexsmobs.world;

import com.github.alexthe666.alexsmobs.AlexsMobs;
import com.github.alexthe666.alexsmobs.config.AMConfig;
import net.minecraft.block.material.Material;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;

import javax.annotation.Nullable;
import java.util.Random;
import java.util.UUID;

public class AMWorldData extends WorldSavedData {

    private static final String IDENTIFIER = "alexsmobs_world_data";
    private static final int NO_PUPFISH_CHUNK = Integer.MIN_VALUE;
    private World world;
    private int tickCounter;
    private int beachedCachalotSpawnDelay;
    private int beachedCachalotSpawnChance;
    private UUID beachedCachalotID;
    private int pupfishChunkX = NO_PUPFISH_CHUNK;
    private int pupfishChunkZ = NO_PUPFISH_CHUNK;
    private int pupfishChunkTime;
    private int pupfishSeedAddition;
    private long startPupfishSearchTimestamp = -1L;
    private boolean noPupfishChunk;

    public AMWorldData() {
        super(IDENTIFIER);
    }

    public static AMWorldData get(World world) {
        if (world.isRemote) {
            return null;
        }
        MinecraftServer server = world.getMinecraftServer();
        if (server == null) {
            return null;
        }
        WorldServer overworld = server.getWorld(0);
        if (overworld == null) {
            return null;
        }
        MapStorage storage = overworld.getPerWorldStorage();
        AMWorldData data = (AMWorldData) storage.getOrLoadData(AMWorldData.class, IDENTIFIER);
        if (data == null) {
            data = new AMWorldData();
            storage.setData(IDENTIFIER, data);
            data.markDirty();
        }
        data.world = world;
        return data;
    }

    public int getBeachedCachalotSpawnDelay() {
        return this.beachedCachalotSpawnDelay;
    }

    public void setBeachedCachalotSpawnDelay(int delay) {
        this.beachedCachalotSpawnDelay = delay;
    }

    public int getBeachedCachalotSpawnChance() {
        return this.beachedCachalotSpawnChance;
    }

    public void setBeachedCachalotSpawnChance(int chance) {
        this.beachedCachalotSpawnChance = chance;
    }

    public void setBeachedCachalotID(UUID id) {
        this.beachedCachalotID = id;
    }

    @Nullable
    public ChunkPos getPupfishChunk() {
        if (this.pupfishChunkX == NO_PUPFISH_CHUNK) {
            return null;
        }
        return new ChunkPos(this.pupfishChunkX, this.pupfishChunkZ);
    }

    public boolean isInPupfishChunk(BlockPos pos) {
        if (this.pupfishChunkX == NO_PUPFISH_CHUNK) {
            return false;
        }
        int minX = this.pupfishChunkX << 4;
        int maxX = minX + 15;
        int minZ = this.pupfishChunkZ << 4;
        int maxZ = minZ + 15;
        return pos.getX() >= minX && pos.getX() <= maxX && pos.getZ() >= minZ && pos.getZ() <= maxZ;
    }

    public void debug() {
    }

    public void tick() {
        ++this.tickCounter;
        if (this.world != null && this.world.provider.getDimension() == 0) {
            this.tickPupfish();
        }
    }

    public void tickPupfish() {
        if (!AMConfig.restrictPupfishSpawns || this.noPupfishChunk) {
            return;
        }
        if (this.pupfishChunkX == NO_PUPFISH_CHUNK && this.startPupfishSearchTimestamp == -1L) {
            this.startPupfishSearchTimestamp = System.currentTimeMillis();
        }
        if (this.pupfishChunkX == NO_PUPFISH_CHUNK && this.pupfishChunkTime % 10 == 0) {
            long seconds = (System.currentTimeMillis() - this.startPupfishSearchTimestamp) / 1000L;
            if (seconds / 60 > 5) {
                AlexsMobs.LOGGER.info("Giving up search for pupfish chunk after {} minutes. no pupfish will spawn in this world :( ", seconds / 60);
                this.noPupfishChunk = true;
            } else {
                this.searchForPupfishChunk();
            }
        }
        this.pupfishChunkTime++;
    }

    private void searchForPupfishChunk() {
        if (!(this.world instanceof WorldServer)) {
            return;
        }
        WorldServer level = (WorldServer) this.world;
        Random random = new Random(level.getSeed() + this.pupfishSeedAddition);
        int randomXCoord = random.nextInt(AMConfig.pupfishChunkSpawnDistance * 2) - AMConfig.pupfishChunkSpawnDistance;
        int randomZCoord = random.nextInt(AMConfig.pupfishChunkSpawnDistance * 2) - AMConfig.pupfishChunkSpawnDistance;
        int chunkX = randomXCoord >> 4;
        int chunkZ = randomZCoord >> 4;
        int centerX = (chunkX << 4) + 8;
        int centerZ = (chunkZ << 4) + 8;
        int maxWater = getWaterHeight(level, centerX, centerZ);
        if (maxWater > 31 && maxWater < 63) {
            this.pupfishChunkX = chunkX;
            this.pupfishChunkZ = chunkZ;
            AlexsMobs.LOGGER.info("Found Pupfish chunk at {} ~ {} after {} tries", this.pupfishChunkX << 4, this.pupfishChunkZ << 4, this.pupfishSeedAddition);
        }
        this.pupfishSeedAddition++;
    }

    private static int getWaterHeight(World world, int x, int z) {
        int maxY = 0;
        for (int y = 1; y < 64; y++) {
            if (world.getBlockState(new BlockPos(x, y, z)).getMaterial() == Material.WATER) {
                maxY = y;
            }
        }
        return maxY;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        if (nbt.hasKey("BeachedCachalotSpawnDelay", 3)) {
            this.beachedCachalotSpawnDelay = nbt.getInteger("BeachedCachalotSpawnDelay");
        }
        if (nbt.hasKey("BeachedCachalotSpawnChance", 3)) {
            this.beachedCachalotSpawnChance = nbt.getInteger("BeachedCachalotSpawnChance");
        }
        if (nbt.hasKey("BeachedCachalotId", 8)) {
            this.beachedCachalotID = UUID.fromString(nbt.getString("BeachedCachalotId"));
        }
        if (nbt.hasKey("PupfishChunkX", 3) && nbt.hasKey("PupfishChunkZ", 3)) {
            this.pupfishChunkX = nbt.getInteger("PupfishChunkX");
            this.pupfishChunkZ = nbt.getInteger("PupfishChunkZ");
        }
        if (nbt.hasKey("NoPupfishChunk", 1)) {
            this.noPupfishChunk = nbt.getBoolean("NoPupfishChunk");
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setInteger("beachedCachalotSpawnDelay", this.beachedCachalotSpawnDelay);
        compound.setInteger("beachedCachalotSpawnChance", this.beachedCachalotSpawnChance);
        if (this.beachedCachalotID != null) {
            compound.setString("beachedCachalotId", this.beachedCachalotID.toString());
        }
        if (this.pupfishChunkX != NO_PUPFISH_CHUNK) {
            compound.setInteger("PupfishChunkX", this.pupfishChunkX);
            compound.setInteger("PupfishChunkZ", this.pupfishChunkZ);
        }
        if (this.noPupfishChunk) {
            compound.setBoolean("NoPupfishChunk", true);
        }
        return compound;
    }
}
