package com.github.alexthe666.alexsmobs.item;

import com.github.alexthe666.alexsmobs.AlexsMobs;
import com.github.alexthe666.alexsmobs.entity.*;
import com.github.alexthe666.alexsmobs.misc.AMTagRegistry;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class ItemAnimalDictionary extends Item {
    public ItemAnimalDictionary() {
        setCreativeTab(AlexsMobs.TAB);
        setMaxStackSize(1);
    }

    private boolean usedOnEntity = false;

    @Override
    public boolean itemInteractionForEntity(ItemStack stack, EntityPlayer playerIn, EntityLivingBase target, EnumHand hand) {
        ItemStack itemStackIn = playerIn.getHeldItem(hand);
        if (playerIn instanceof EntityPlayerMP) {
            EntityPlayerMP serverPlayer = (EntityPlayerMP) playerIn;
            CriteriaTriggers.CONSUME_ITEM.trigger(serverPlayer, itemStackIn);
            serverPlayer.addStat(StatList.getObjectUseStats(this));
        }
        ResourceLocation entityId = AMTagRegistry.registrationNameForEntity(target);
        if (playerIn.world.isRemote && entityId != null && AlexsMobs.MODID.equals(entityId.getResourceDomain())) {
            usedOnEntity = true;
            String id = entityId.getResourcePath();
            if(target instanceof EntityBoneSerpent || target instanceof EntityBoneSerpentPart){
                id = "bone_serpent";
            }
            if(target instanceof EntityCentipedeHead || target instanceof EntityCentipedeBody || target instanceof EntityCentipedeTail){
                id = "cave_centipede";
            }
            if(target instanceof EntityVoidWorm || target instanceof EntityVoidWormPart){
                id = "void_worm";
            }
            if(target instanceof EntityAnaconda || target instanceof EntityAnacondaPart){
                id = "anaconda";
            }
            if(target instanceof EntityGiantSquid || "giant_squid_part".equals(id)){
                id = "giant_squid";
            }
            AlexsMobs.PROXY.openBookGUI(itemStackIn, id);
        }
        return true;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        ItemStack itemStackIn = playerIn.getHeldItem(handIn);
        if (!usedOnEntity) {
            if (playerIn instanceof EntityPlayerMP) {
                EntityPlayerMP serverPlayer = (EntityPlayerMP) playerIn;
                CriteriaTriggers.CONSUME_ITEM.trigger(serverPlayer, itemStackIn);
                serverPlayer.addStat(StatList.getObjectUseStats(this));
            }
            if (worldIn.isRemote) {
                AlexsMobs.PROXY.openBookGUI(itemStackIn);
            }
        }
        usedOnEntity = false;

        return new ActionResult(EnumActionResult.PASS, itemStackIn);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, net.minecraft.client.util.ITooltipFlag flagIn) {
        tooltip.add(TextFormatting.GRAY + net.minecraft.client.resources.I18n.format("item.alexsmobs.animal_dictionary.desc"));
    }
}
