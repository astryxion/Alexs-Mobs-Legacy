package com.github.alexthe666.alexsmobs.entity;

import com.github.alexthe666.alexsmobs.client.particle.AMParticleRegistry;
import com.github.alexthe666.alexsmobs.event.ServerEvents;
import com.github.alexthe666.alexsmobs.misc.AMSoundRegistry;
import com.github.alexthe666.alexsmobs.misc.AMTagRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.ITeleporter;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class EntityVoidPortal extends Entity {

    protected static final DataParameter<Byte> ATTACHED_FACE = EntityDataManager.createKey(EntityVoidPortal.class, DataSerializers.BYTE);
    protected static final DataParameter<Integer> LIFESPAN = EntityDataManager.createKey(EntityVoidPortal.class, DataSerializers.VARINT);
    private static final DataParameter<Boolean> HAS_DESTINATION = EntityDataManager.createKey(EntityVoidPortal.class, DataSerializers.BOOLEAN);
    private static final DataParameter<BlockPos> DESTINATION = EntityDataManager.createKey(EntityVoidPortal.class, DataSerializers.BLOCK_POS);
    private static final DataParameter<String> SISTER_UUID = EntityDataManager.createKey(EntityVoidPortal.class, DataSerializers.STRING);

    /** Target dimension id ({@link net.minecraft.world.WorldProvider#getDimension()}), or null for same-dimension teleport. */
    @Nullable
    public Integer exitDimension;
    private boolean madeOpenNoise = false;
    private boolean madeCloseNoise = false;

    public EntityVoidPortal(World worldIn) {
        super(worldIn);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (this.ticksExisted == 1) {
            if (this.getLifespan() == 0) {
                this.setLifespan(100);
            }
        }
        if (!madeOpenNoise) {
            this.playSound(AMSoundRegistry.VOID_PORTAL_OPEN, 1.0F, 1 + rand.nextFloat() * 0.2F);
            madeOpenNoise = true;
        }
        EnumFacing direction2 = this.getAttachmentFacing().getOpposite();
        float minX = -0.15F;
        float minY = -0.15F;
        float minZ = -0.15F;
        float maxX = 0.15F;
        float maxY = 0.15F;
        float maxZ = 0.15F;
        switch (direction2) {
            case NORTH:
            case SOUTH:
                minX = -1.5F;
                maxX = 1.5F;
                minY = -1.5F;
                maxY = 1.5F;
                break;
            case EAST:
            case WEST:
                minZ = -1.5F;
                maxZ = 1.5F;
                minY = -1.5F;
                maxY = 1.5F;
                break;
            case UP:
            case DOWN:
                minX = -1.5F;
                maxX = 1.5F;
                minZ = -1.5F;
                maxZ = 1.5F;
                break;
            default:
                break;
        }
        AxisAlignedBB bb = new AxisAlignedBB(this.posX + minX, this.posY + minY, this.posZ + minZ, this.posX + maxX, this.posY + maxY, this.posZ + maxZ);
        this.setEntityBoundingBox(bb);
        if (rand.nextFloat() < 0.5F && world.isRemote && Math.min(ticksExisted, this.getLifespan()) >= 20) {
            double particleX = this.getEntityBoundingBox().minX + rand.nextFloat() * (this.getEntityBoundingBox().maxX - this.getEntityBoundingBox().minX);
            double particleY = this.getEntityBoundingBox().minY + rand.nextFloat() * (this.getEntityBoundingBox().maxY - this.getEntityBoundingBox().minY);
            double particleZ = this.getEntityBoundingBox().minZ + rand.nextFloat() * (this.getEntityBoundingBox().maxZ - this.getEntityBoundingBox().minZ);
            spawnWormPortalParticle(particleX, particleY, particleZ);
        }
        List<Entity> entities = this.world.getEntitiesWithinAABBExcludingEntity(this, bb.grow(-0.2D));
        if (!world.isRemote) {
            MinecraftServer server = world.getMinecraftServer();
            if (this.getDestination() != null && this.getLifespan() > 20 && ticksExisted > 20 && server != null) {
                BlockPos offsetPos = this.getDestination().offset(this.getAttachmentFacing().getOpposite(), 2);
                for (Entity e : entities) {
                    if (e.timeUntilPortal > 0 || e.isSneaking() || e instanceof EntityVoidPortal
                            || AMTagRegistry.entityMatchesEntityTypeTag(AMTagRegistry.VOID_PORTAL_IGNORES, e)) {
                        continue;
                    }
                    if (e instanceof EntityVoidWormPart) {
                        if (this.getLifespan() < 22) {
                            this.setLifespan(this.getLifespan() + 1);
                        }
                    } else if (e instanceof EntityVoidWorm) {
                        EntityVoidWorm worm = (EntityVoidWorm) e;
                        worm.teleportTo(new Vec3d(offsetPos.getX() + 0.5D, offsetPos.getY() + 0.5D, offsetPos.getZ() + 0.5D));
                        e.timeUntilPortal = 300;
                        worm.resetPortalLogic();
                    } else {
                        boolean flag = true;
                        if (exitDimension != null) {
                            WorldServer dimWorld = server.getWorld(exitDimension);
                            if (dimWorld != null && this.world.provider.getDimension() != exitDimension) {
                                teleportEntityFromDimension(e, dimWorld, offsetPos);
                                flag = false;
                            }
                        }
                        if (flag) {
                            e.setPositionAndUpdate(offsetPos.getX() + 0.5D, offsetPos.getY() + 0.5D, offsetPos.getZ() + 0.5D);
                            e.timeUntilPortal = 300;
                        }
                    }
                }
            }
        }
        this.setLifespan(this.getLifespan() - 1);
        if (this.getLifespan() <= 20) {
            if (!madeCloseNoise) {
                this.playSound(AMSoundRegistry.VOID_PORTAL_CLOSE, 1.0F, 1 + rand.nextFloat() * 0.2F);
                madeCloseNoise = true;
            }
        }
        if (this.getLifespan() <= 0) {
            this.setDead();
        }
    }

    @SideOnly(Side.CLIENT)
    private void spawnWormPortalParticle(double x, double y, double z) {
        AMParticleRegistry.spawnParticle(world, AMParticleRegistry.WORM_PORTAL, x, y, z, 0.1D * rand.nextGaussian(), 0.1D * rand.nextGaussian(), 0.1D * rand.nextGaussian());
    }

    private void teleportEntityFromDimension(Entity entity, WorldServer endpointWorld, BlockPos endpoint) {
        if (entity instanceof EntityPlayerMP) {
            ServerEvents.teleportPlayers.add(new ServerEvents.PendingDimensionTeleport((EntityPlayerMP) entity, endpointWorld.provider.getDimension(), endpoint));
            if (this.getSisterId() == null) {
                createAndSetSister(endpointWorld, EnumFacing.DOWN);
            }
        } else {
            NBTTagCompound nbt = new NBTTagCompound();
            entity.writeToNBT(nbt);
            EntityEntry entry = ForgeRegistries.ENTITIES.getValue(AMTagRegistry.registrationNameForEntity(entity));
            if (entry != null) {
                Entity teleportedEntity = entry.newInstance(endpointWorld);
                if (teleportedEntity != null) {
                    teleportedEntity.readFromNBT(nbt);
                    teleportedEntity.setLocationAndAngles(endpoint.getX() + 0.5D, endpoint.getY() + 0.5D, endpoint.getZ() + 0.5D, entity.rotationYaw, entity.rotationPitch);
                    teleportedEntity.setRotationYawHead(entity.rotationYaw);
                    teleportedEntity.timeUntilPortal = 300;
                    endpointWorld.spawnEntity(teleportedEntity);
                }
            }
            entity.setDead();
        }
    }

    public EnumFacing getAttachmentFacing() {
        return EnumFacing.values()[this.dataManager.get(ATTACHED_FACE) & 0xFF];
    }

    public void setAttachmentFacing(EnumFacing facing) {
        this.dataManager.set(ATTACHED_FACE, (byte) facing.getIndex());
    }

    public int getLifespan() {
        return this.dataManager.get(LIFESPAN);
    }

    public void setLifespan(int i) {
        this.dataManager.set(LIFESPAN, i);
    }

    @Nullable
    public BlockPos getDestination() {
        return this.dataManager.get(HAS_DESTINATION) ? this.dataManager.get(DESTINATION) : null;
    }

    public void setDestination(BlockPos destination) {
        this.dataManager.set(HAS_DESTINATION, destination != null);
        if (destination != null) {
            this.dataManager.set(DESTINATION, destination);
        }
        if (this.getSisterId() == null && (exitDimension == null || exitDimension == this.world.provider.getDimension())) {
            createAndSetSister(world, null);
        }
    }

    public void createAndSetSister(World world, @Nullable EnumFacing dir) {
        EntityVoidPortal portal = (EntityVoidPortal) AMEntityRegistry.VOID_PORTAL.newInstance(world);
        portal.setAttachmentFacing(dir != null ? dir : this.getAttachmentFacing().getOpposite());
        BlockPos dest = this.getDestination();
        if (dest != null) {
            portal.setPosition(dest.getX() + 0.5D, dest.getY() + 0.5D, dest.getZ() + 0.5D);
        }
        portal.link(this);
        portal.exitDimension = this.world.provider.getDimension();
        world.spawnEntity(portal);
    }

    public void setDestination(BlockPos destination, EnumFacing dir) {
        this.dataManager.set(HAS_DESTINATION, destination != null);
        if (destination != null) {
            this.dataManager.set(DESTINATION, destination);
        }
        if (this.getSisterId() == null && (exitDimension == null || exitDimension == this.world.provider.getDimension())) {
            createAndSetSister(world, dir);
        }
    }

    public void link(EntityVoidPortal portal) {
        this.setSisterId(portal.getUniqueID());
        portal.setSisterId(this.getUniqueID());
        portal.setLifespan(this.getLifespan());
        this.setDestination(portal.getPosition());
        portal.setDestination(this.getPosition());
    }

    @Override
    protected void entityInit() {
        this.dataManager.register(ATTACHED_FACE, (byte) EnumFacing.DOWN.getIndex());
        this.dataManager.register(LIFESPAN, 300);
        this.dataManager.register(SISTER_UUID, "");
        this.dataManager.register(HAS_DESTINATION, false);
        this.dataManager.register(DESTINATION, BlockPos.ORIGIN);
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound compound) {
        this.dataManager.set(ATTACHED_FACE, compound.getByte("AttachFace"));
        this.setLifespan(compound.getInteger("Lifespan"));
        if (compound.hasKey("DX")) {
            int i = compound.getInteger("DX");
            int j = compound.getInteger("DY");
            int k = compound.getInteger("DZ");
            this.dataManager.set(HAS_DESTINATION, true);
            this.dataManager.set(DESTINATION, new BlockPos(i, j, k));
        } else {
            this.dataManager.set(HAS_DESTINATION, false);
        }
        if (compound.hasUniqueId("SisterUUID")) {
            this.setSisterId(compound.getUniqueId("SisterUUID"));
        }
        if (compound.hasKey("ExitDimension")) {
            this.exitDimension = compound.getInteger("ExitDimension");
        }
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound compound) {
        compound.setByte("AttachFace", this.dataManager.get(ATTACHED_FACE));
        compound.setInteger("Lifespan", getLifespan());
        BlockPos blockpos = this.getDestination();
        if (blockpos != null) {
            compound.setInteger("DX", blockpos.getX());
            compound.setInteger("DY", blockpos.getY());
            compound.setInteger("DZ", blockpos.getZ());
        }
        if (this.getSisterId() != null) {
            compound.setUniqueId("SisterUUID", this.getSisterId());
        }
        if (this.exitDimension != null) {
            compound.setInteger("ExitDimension", this.exitDimension);
        }
    }

    @Nullable
    public Entity getSister() {
        UUID id = getSisterId();
        if (id != null && !world.isRemote && world instanceof WorldServer) {
            return ((WorldServer) world).getEntityFromUuid(id);
        }
        return null;
    }

    @Nullable
    public UUID getSisterId() {
        String s = this.dataManager.get(SISTER_UUID);
        return s.isEmpty() ? null : UUID.fromString(s);
    }

    public void setSisterId(@Nullable UUID uniqueId) {
        this.dataManager.set(SISTER_UUID, uniqueId == null ? "" : uniqueId.toString());
    }

    /**
     * Places entities at the void portal destination when changing dimensions (Forge 1.12.2).
     */
    public static class BlockPosTeleporter implements ITeleporter {
        private final BlockPos pos;

        public BlockPosTeleporter(BlockPos pos) {
            this.pos = pos;
        }

        @Override
        public void placeEntity(World world, Entity entity, float yaw) {
            entity.setLocationAndAngles(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, yaw, entity.rotationPitch);
            entity.motionX = 0;
            entity.motionY = 0;
            entity.motionZ = 0;
            entity.fallDistance = 0.0F;
        }
    }
}
