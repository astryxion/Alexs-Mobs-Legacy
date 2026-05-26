package com.github.alexthe666.alexsmobs.entity.util;

import com.github.alexthe666.alexsmobs.AlexsMobs;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Vine lasso leash state. Citadel's {@code LivingEntityMixin} is not injected on 1.12.2, so lasso data
 * is tracked in mod-local maps and synced with {@link MessageVineLassoSync}.
 */
public class VineLassoUtil {

    private static final Map<UUID, LassoRecord> SERVER_RECORDS = new HashMap<>();
    private static final Set<UUID> LASSO_NAV_CONFIGURED = new HashSet<>();
    @SideOnly(Side.CLIENT)
    private static final Map<UUID, LassoRecord> CLIENT_RECORDS = new HashMap<>();

    private static final class LassoRecord {
        final UUID ownerUuid;
        final int ownerEntityId;
        final boolean removed;

        LassoRecord(UUID ownerUuid, int ownerEntityId, boolean removed) {
            this.ownerUuid = ownerUuid;
            this.ownerEntityId = ownerEntityId;
            this.removed = removed;
        }

        static LassoRecord active(UUID ownerUuid, int ownerEntityId) {
            return new LassoRecord(ownerUuid, ownerEntityId, false);
        }

        static LassoRecord removed() {
            return new LassoRecord(null, -1, true);
        }
    }

    private static Map<UUID, LassoRecord> recordsFor(World world) {
        if (world.isRemote) {
            return CLIENT_RECORDS;
        }
        return SERVER_RECORDS;
    }

    public static void lassoTo(@Nullable EntityLivingBase lassoer, EntityLivingBase lassoed) {
        UUID lassoedUuid = lassoed.getUniqueID();
        LassoRecord record;
        if (lassoer == null) {
            record = LassoRecord.removed();
            recordsFor(lassoed.world).remove(lassoedUuid);
            clearLassoNavigation(lassoed);
        } else {
            record = LassoRecord.active(lassoer.getUniqueID(), lassoer.getEntityId());
            recordsFor(lassoed.world).put(lassoedUuid, record);
        }
        if (!lassoed.world.isRemote) {
            syncLassoToClients(new MessageVineLassoSync(lassoedUuid, lassoed.getEntityId(), record));
        }
    }

    private static void syncLassoToClients(MessageVineLassoSync message) {
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        if (server == null) {
            return;
        }
        for (EntityPlayerMP player : server.getPlayerList().getPlayers()) {
            AlexsMobs.NETWORK_WRAPPER.sendTo(message, player);
        }
    }

    public static boolean hasLassoData(EntityLivingBase lasso) {
        LassoRecord record = recordsFor(lasso.world).get(lasso.getUniqueID());
        return record != null && !record.removed && record.ownerEntityId != -1;
    }

    public static Entity getLassoedTo(EntityLivingBase lassoed) {
        LassoRecord record = recordsFor(lassoed.world).get(lassoed.getUniqueID());
        if (record == null || record.removed) {
            return null;
        }
        if (lassoed.world.isRemote && record.ownerEntityId != -1) {
            Entity found = lassoed.world.getEntityByID(record.ownerEntityId);
            if (found != null) {
                return found;
            }
            if (record.ownerUuid != null) {
                return lassoed.world.getPlayerEntityByUUID(record.ownerUuid);
            }
        } else if (lassoed.world instanceof WorldServer && record.ownerUuid != null) {
            Entity found = ((WorldServer) lassoed.world).getEntityFromUuid(record.ownerUuid);
            if (found != null) {
                return found;
            }
            if (record.ownerEntityId != -1) {
                return lassoed.world.getEntityByID(record.ownerEntityId);
            }
        }
        return null;
    }

    public static void tickLasso(EntityLivingBase lassoed) {
        if (lassoed.world.isRemote) {
            return;
        }
        Entity lassoedOwner = VineLassoUtil.getLassoedTo(lassoed);
        if (lassoedOwner == null) {
            clearLassoNavigation(lassoed);
            return;
        }

        double distance = lassoed.getDistance(lassoedOwner);

        if (lassoed instanceof EntityCreature) {
            EntityCreature creature = (EntityCreature) lassoed;
            creature.setHomePosAndDistance(new BlockPos(lassoedOwner), 5);
            if (!LASSO_NAV_CONFIGURED.contains(lassoed.getUniqueID())) {
                if (creature.getNavigator() instanceof PathNavigateGround) {
                    ((PathNavigateGround) creature.getNavigator()).setCanSwim(true);
                }
                LASSO_NAV_CONFIGURED.add(lassoed.getUniqueID());
            }
            if (distance > 4.0F) {
                creature.getNavigator().tryMoveToEntityLiving(lassoedOwner, 1.0D);
            } else {
                creature.getNavigator().clearPath();
            }
        }

        if (distance > 6.0F) {
            double dx = (lassoedOwner.posX - lassoed.posX) / distance;
            double dy = (lassoedOwner.posY - lassoed.posY) / distance;
            double dz = (lassoedOwner.posZ - lassoed.posZ) / distance;
            if (!(lassoed instanceof EntityPlayer)) {
                lassoed.motionY += dy * Math.abs(dy) * 0.4D;
            }
            lassoed.motionX += dx * Math.abs(dx) * 0.4D;
            lassoed.motionZ += dz * Math.abs(dz) * 0.4D;
            lassoed.velocityChanged = true;
        }
    }

    private static void clearLassoNavigation(EntityLivingBase lassoed) {
        if (!LASSO_NAV_CONFIGURED.remove(lassoed.getUniqueID())) {
            return;
        }
        if (lassoed instanceof EntityCreature) {
            EntityCreature creature = (EntityCreature) lassoed;
            if (creature.getNavigator() instanceof PathNavigateGround) {
                ((PathNavigateGround) creature.getNavigator()).setCanSwim(false);
            }
            creature.detachHome();
            creature.getNavigator().clearPath();
        }
    }

    @SideOnly(Side.CLIENT)
    private static void applyClientRecord(UUID lassoedUuid, LassoRecord record) {
        if (record.removed) {
            CLIENT_RECORDS.remove(lassoedUuid);
        } else {
            CLIENT_RECORDS.put(lassoedUuid, record);
        }
    }

    private static void writeUuid(ByteBuf buf, @Nullable UUID uuid) {
        buf.writeBoolean(uuid != null);
        if (uuid != null) {
            buf.writeLong(uuid.getMostSignificantBits());
            buf.writeLong(uuid.getLeastSignificantBits());
        }
    }

    @Nullable
    private static UUID readUuid(ByteBuf buf) {
        if (!buf.readBoolean()) {
            return null;
        }
        return new UUID(buf.readLong(), buf.readLong());
    }

    public static class MessageVineLassoSync implements IMessage {

        private UUID lassoedUuid;
        private int lassoedEntityId;
        private boolean active;
        private UUID ownerUuid;
        private int ownerEntityId;

        public MessageVineLassoSync() {
        }

        public MessageVineLassoSync(UUID lassoedUuid, int lassoedEntityId, LassoRecord record) {
            this.lassoedUuid = lassoedUuid;
            this.lassoedEntityId = lassoedEntityId;
            this.active = record != null && !record.removed;
            this.ownerUuid = record != null ? record.ownerUuid : null;
            this.ownerEntityId = record != null ? record.ownerEntityId : -1;
        }

        @Override
        public void fromBytes(ByteBuf buf) {
            this.lassoedUuid = readUuid(buf);
            this.lassoedEntityId = buf.readInt();
            this.active = buf.readBoolean();
            this.ownerUuid = readUuid(buf);
            this.ownerEntityId = buf.readInt();
        }

        @Override
        public void toBytes(ByteBuf buf) {
            writeUuid(buf, this.lassoedUuid);
            buf.writeInt(this.lassoedEntityId);
            buf.writeBoolean(this.active);
            writeUuid(buf, this.ownerUuid);
            buf.writeInt(this.ownerEntityId);
        }

        public static class Handler implements IMessageHandler<MessageVineLassoSync, IMessage> {

            @Override
            public IMessage onMessage(MessageVineLassoSync message, MessageContext ctx) {
                if (ctx.side == Side.CLIENT) {
                    Minecraft.getMinecraft().addScheduledTask(() -> {
                        LassoRecord record = message.active
                                ? LassoRecord.active(message.ownerUuid, message.ownerEntityId)
                                : LassoRecord.removed();
                        applyClientRecord(message.lassoedUuid, record);
                    });
                }
                return null;
            }
        }
    }
}
