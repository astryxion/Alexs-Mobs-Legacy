package com.github.alexthe666.alexsmobs.item;

import com.github.alexthe666.alexsmobs.AlexsMobs;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemElytra;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

import java.util.HashSet;
import java.util.Set;

/**
 * Tarantula hawk elytra — vanilla elytra item ({@link ItemElytra}) with a custom texture/render layer.
 * <p>
 * In 1.16.5, Forge hooks {@code canElytraFly} / {@code elytraFlightTick} on the item. Forge never
 * backported that API to 1.12.2; vanilla still hardcodes {@link net.minecraft.init.Items#ELYTRA} in
 * client fall-flying packets, server packet handling, and {@code EntityLivingBase#updateElytra}.
 * The static helpers below re-apply vanilla elytra flight for this item only.
 */
public class ItemTarantulaHawkElytra extends ItemElytra {

    private static final Set<Integer> ACTIVE_GLIDERS = new HashSet<>();

    public ItemTarantulaHawkElytra() {
        setCreativeTab(AlexsMobs.TAB);
    }

    public static boolean isUsable(ItemStack stack) {
        return stack.getItem() instanceof ItemTarantulaHawkElytra && ItemElytra.isUsable(stack);
    }

    public static boolean isWearingUsable(EntityPlayer player) {
        return isUsable(player.getItemStackFromSlot(EntityEquipmentSlot.CHEST));
    }

    /** Mirrors {@link net.minecraft.client.entity.EntityPlayerSP} fall-flying start conditions. */
    public static boolean canStartGliding(EntityPlayer player, ItemStack chest) {
        return isUsable(chest)
                && !player.onGround
                && player.fallDistance > 0.0F
                && !player.isElytraFlying()
                && !player.capabilities.isFlying
                && !player.isInWater()
                && !player.isInLava()
                && !player.isRiding();
    }

    public static void tryStartGliding(EntityPlayer player) {
        if (!canStartGliding(player, player.getItemStackFromSlot(EntityEquipmentSlot.CHEST))) {
            return;
        }
        ACTIVE_GLIDERS.add(player.getEntityId());
        startElytraFlight(player);
    }

    /** Called after vanilla {@code updateElytra} — vanilla only keeps flight for {@link net.minecraft.init.Items#ELYTRA}. */
    public static void handleElytraFlightTick(EntityPlayer player, ItemStack chest) {
        if (!(chest.getItem() instanceof ItemTarantulaHawkElytra)) {
            clearGlidingState(player);
            return;
        }
        int id = player.getEntityId();
        if (player.capabilities.isFlying
                || player.onGround
                || !isUsable(chest)
                || player.isInWater()
                || player.isInLava()) {
            clearGlidingState(player);
            return;
        }
        if (ACTIVE_GLIDERS.contains(id)) {
            if (!player.isElytraFlying()) {
                startElytraFlight(player);
            }
        } else if (player.isElytraFlying()) {
            stopElytraFlight(player);
        }
    }

    public static void clearGlidingState(EntityPlayer player) {
        ACTIVE_GLIDERS.remove(player.getEntityId());
        if (player.isElytraFlying()) {
            stopElytraFlight(player);
        }
    }

    public static void startElytraFlight(EntityPlayer player) {
        if (player.capabilities.isFlying) {
            return;
        }
        ACTIVE_GLIDERS.add(player.getEntityId());
        if (player instanceof EntityPlayerMP) {
            ((EntityPlayerMP) player).setElytraFlying();
        }
    }

    public static void stopElytraFlight(EntityPlayer player) {
        ACTIVE_GLIDERS.remove(player.getEntityId());
        if (player instanceof EntityPlayerMP) {
            ((EntityPlayerMP) player).clearElytraFlying();
        }
    }

    @Override
    public EntityEquipmentSlot getEquipmentSlot(ItemStack stack) {
        return EntityEquipmentSlot.CHEST;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        ItemStack held = playerIn.getHeldItem(handIn);
        ItemStack chest = playerIn.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
        if (chest.isEmpty()) {
            playerIn.setItemStackToSlot(EntityEquipmentSlot.CHEST, held.copy());
            held.setCount(0);
            return new ActionResult<>(EnumActionResult.SUCCESS, held);
        }
        return new ActionResult<>(EnumActionResult.FAIL, held);
    }

    @Override
    public boolean getIsRepairable(ItemStack toRepair, ItemStack repair) {
        return repair.getItem() == AMItemRegistry.TARANTULA_HAWK_WING_FRAGMENT;
    }

    /** Client → server; vanilla {@code START_FALL_FLYING} only accepts {@link net.minecraft.init.Items#ELYTRA}. */
    public static class MessageStartGlide implements IMessage {

        public MessageStartGlide() {
        }

        @Override
        public void fromBytes(ByteBuf buf) {
        }

        @Override
        public void toBytes(ByteBuf buf) {
        }

        public static class Handler implements IMessageHandler<MessageStartGlide, IMessage> {

            @Override
            public IMessage onMessage(MessageStartGlide message, MessageContext ctx) {
                if (ctx.side == Side.SERVER) {
                    EntityPlayerMP player = ctx.getServerHandler().player;
                    player.mcServer.addScheduledTask(() -> {
                        if (player != null) {
                            ItemTarantulaHawkElytra.tryStartGliding(player);
                        }
                    });
                }
                return null;
            }
        }
    }
}
