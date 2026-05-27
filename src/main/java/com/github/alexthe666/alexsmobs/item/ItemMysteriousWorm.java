package com.github.alexthe666.alexsmobs.item;

import com.github.alexthe666.alexsmobs.AlexsMobs;
import com.github.alexthe666.alexsmobs.config.AMConfig;
import com.github.alexthe666.alexsmobs.entity.AMEntityRegistry;
import com.github.alexthe666.alexsmobs.entity.EntityVoidWorm;
import com.github.alexthe666.alexsmobs.misc.AMAdvancementTriggerRegistry;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import java.util.Random;

/**
 * Per-item void worm summon (1.20 {@code ItemMysteriousWorm#onEntityItemUpdate}); avoids scanning all
 * {@link EntityItem}s every world tick.
 */
public class ItemMysteriousWorm extends Item {

    public ItemMysteriousWorm() {
        this.setCreativeTab(AlexsMobs.TAB);
    }

    @Override
    public EnumRarity getRarity(ItemStack stack) {
        return EnumRarity.RARE;
    }

    @Override
    public boolean onEntityItemUpdate(EntityItem entity) {
        if (entity.world.isRemote || !AMConfig.voidWormSummonable) {
            return false;
        }
        if (!AMConfig.isVoidWormDimension(entity.world)) {
            return false;
        }
        if (entity.posY >= -10) {
            return false;
        }
        ItemStack stack = entity.getItem();
        if (stack.isEmpty() || stack.getItem() != this) {
            return false;
        }
        NBTTagCompound tag = stack.getTagCompound();
        if (tag != null && tag.getBoolean("AMVoidWormSummoned")) {
            return false;
        }
        if (tag == null) {
            tag = new NBTTagCompound();
            stack.setTagCompound(tag);
        }
        tag.setBoolean("AMVoidWormSummoned", true);

        World world = entity.world;
        EntityVoidWorm worm = (EntityVoidWorm) AMEntityRegistry.VOID_WORM.newInstance(world);
        worm.setPosition(entity.posX, 0, entity.posZ);
        worm.setSegmentCount(25 + new Random().nextInt(15));
        worm.rotationPitch = -90.0F;
        worm.updatePostSummon = true;

        if (entity.getThrower() != null) {
            EntityPlayerMP thrower = (EntityPlayerMP) world.getPlayerEntityByName(entity.getThrower());
            if (thrower != null) {
                AMAdvancementTriggerRegistry.VOID_WORM_SUMMON.trigger(thrower);
            }
        }
        world.spawnEntity(worm);
        entity.setDead();
        return false;
    }
}
