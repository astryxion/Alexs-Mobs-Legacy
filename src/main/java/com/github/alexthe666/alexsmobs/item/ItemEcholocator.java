package com.github.alexthe666.alexsmobs.item;

import com.github.alexthe666.alexsmobs.AlexsMobs;
import com.github.alexthe666.alexsmobs.entity.EntityCachalotEcho;
import com.github.alexthe666.alexsmobs.misc.AMPointOfInterestRegistry;
import com.github.alexthe666.alexsmobs.misc.AMSoundRegistry;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Collections;
import java.util.List;
import java.util.Random;

public class ItemEcholocator extends Item {

    public final boolean ender;

    public ItemEcholocator(boolean ender) {
        this.setCreativeTab(AlexsMobs.TAB);
        this.setMaxDamage(ender ? 25 : 100);
        this.ender = ender;
    }

    private List<BlockPos> getNearbyPortals(BlockPos blockpos, World world, int range) {
        if (ender) {
            return AMPointOfInterestRegistry.findAll(world, blockpos, range, AMPointOfInterestRegistry::matchesEndPortalFrame);
        } else {
            Random random = new Random();
            for (int i = 0; i < 256; i++) {
                BlockPos checkPos = blockpos.add(random.nextInt(range) - range / 2, random.nextInt(range) / 2 - range / 2, random.nextInt(range) - range / 2);
                if (isDarkCaveAir(world, checkPos)) {
                    return Collections.singletonList(checkPos);
                }
            }
            return Collections.emptyList();
        }
    }

    /** 1.12 has no {@code Blocks.CAVE_AIR}; match dark non-sky air pockets like 1.16 cave-air echolocation. */
    private static boolean isDarkCaveAir(World world, BlockPos checkPos) {
        if (world.getBlockState(checkPos).getMaterial() != Material.AIR) {
            return false;
        }
        return world.getLight(checkPos) < 4 && !world.canSeeSky(checkPos);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        boolean left = player.getActiveHand() == EnumHand.OFF_HAND && player.getPrimaryHand() == EnumHandSide.RIGHT
                || player.getActiveHand() == EnumHand.MAIN_HAND && player.getPrimaryHand() == EnumHandSide.LEFT;
        EntityCachalotEcho whaleEcho = new EntityCachalotEcho(worldIn, player, !left);
        if (!worldIn.isRemote) {
            BlockPos playerPos = player.getPosition();
            List<BlockPos> portals = getNearbyPortals(playerPos, worldIn, 128);
            BlockPos pos = null;
            if (ender) {
                for (BlockPos portalPos : portals) {
                    if (pos == null || pos.distanceSq(playerPos) > portalPos.distanceSq(playerPos)) {
                        pos = portalPos;
                    }
                }
            } else {
                NBTTagCompound nbt = stack.getTagCompound();
                if (nbt == null) {
                    nbt = new NBTTagCompound();
                }
                if (nbt.hasKey("CavePos") && nbt.getBoolean("ValidCavePos")) {
                    pos = BlockPos.fromLong(nbt.getLong("CavePos"));
                    if (!isDarkCaveAir(worldIn, pos) || 1000000 < pos.distanceSq(playerPos)) {
                        nbt.setBoolean("ValidCavePos", false);
                    }
                } else {
                    for (BlockPos portalPos : portals) {
                        if (pos == null || pos.distanceSq(playerPos) < portalPos.distanceSq(playerPos)) {
                            pos = portalPos;
                        }
                    }
                    if (pos != null) {
                        nbt.setLong("CavePos", pos.toLong());
                        nbt.setBoolean("ValidCavePos", true);
                        stack.setTagCompound(nbt);
                    }
                }
            }
            if (pos != null) {
                double d0 = pos.getX() + 0.5F - whaleEcho.posX;
                double d1 = pos.getY() + 0.5F - whaleEcho.posY;
                double d2 = pos.getZ() + 0.5F - whaleEcho.posZ;
                whaleEcho.ticksExisted = 15;
                whaleEcho.shoot(d0, d1, d2, 0.4F, 0.3F);
                worldIn.spawnEntity(whaleEcho);
                worldIn.playSound(null, whaleEcho.posX, whaleEcho.posY, whaleEcho.posZ, AMSoundRegistry.CACHALOT_WHALE_CLICK, SoundCategory.PLAYERS, 1.0F, 1.0F);
                stack.damageItem(1, player);
            }
        }
        player.getCooldownTracker().setCooldown(this, 5);
        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }
}
