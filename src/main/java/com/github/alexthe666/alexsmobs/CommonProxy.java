package com.github.alexthe666.alexsmobs;

import com.github.alexthe666.alexsmobs.config.AMConfig;
import com.github.alexthe666.alexsmobs.misc.RecipeMimicreamRepair;
import com.github.alexthe666.alexsmobs.misc.BananaLootModifier;
import com.github.alexthe666.alexsmobs.misc.BlossomLootModifier;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static com.github.alexthe666.alexsmobs.AlexsMobs.MODID;

@Mod.EventBusSubscriber(modid = MODID)
public class CommonProxy {

    public void init() {
    }

    /** Client: particle registration after {@link net.minecraft.client.Minecraft} exists (see {@link ClientProxy#initClient()}). */
    public void initClient() {
    }

    /**
     * Client-only hook; must run in {@code preInit} so {@link net.minecraftforge.fml.client.registry.RenderingRegistry}
     * factories are applied before {@code RenderManager} is constructed.
     */
    public void preInitClient() {
    }

    public void clientInit() {
    }

    /**
     * Client-only hook for items using {@link net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer};
     * common side returns the item unchanged.
     */
    public Item setupISTER(Item item) {
        return item;
    }

    public EntityPlayer getClientSidePlayer() {
        return null;
    }

    public void openBookGUI(ItemStack itemStackIn) {
    }

    public void openBookGUI(ItemStack itemStackIn, String page) {
    }

    public Object getArmorModel(int armorId, net.minecraft.entity.EntityLivingBase entity) {
        return null;
    }

    public void onEntityStatus(net.minecraft.entity.Entity entity, byte updateKind) {
    }

    public int getSingingBlueJayId() {
        return -1;
    }

    public void updateBiomeVisuals(int x, int z) {
    }

    public void setupParticles() {
    }

    public void setRenderViewEntity(net.minecraft.entity.Entity entity) {

    }

    public void resetRenderViewEntity() {

    }

    public int getPreviousPOV() {
        return 0;
    }

    public boolean isFarFromCamera(double x, double y, double z) {
        return true;
    }

    public void resetVoidPortalCreation(EntityPlayer player) {
    }

    @SubscribeEvent
    public static void registerRecipes(RegistryEvent.Register<IRecipe> event) {
        // Crafting recipes load from assets/alexsmobs/recipes via Forge CraftingHelper (see _factories.json).
        // Smelting recipes register in AlexsMobs.init via AMSmeltingRecipes.
    }

    /**
     * 1.12 replacement for Forge global loot modifiers: same rolls as {@link BananaLootModifier} / {@link BlossomLootModifier}.
     */
    @SubscribeEvent
    public static void onHarvestDrops(BlockEvent.HarvestDropsEvent event) {
        World world = event.getWorld();
        if (world.isRemote) {
            return;
        }
        BlockPos pos = event.getPos();
        Block block = event.getState().getBlock();
        EntityPlayer harvester = event.getHarvester();
        ItemStack tool = harvester != null ? harvester.getHeldItem(net.minecraft.util.EnumHand.MAIN_HAND) : ItemStack.EMPTY;

        if (BananaLootModifier.shouldProcessForBlock(block)) {
            BananaLootModifier.applyToHarvestDrops(event.getDrops(), world, harvester, tool, event.isSilkTouching(), event.getFortuneLevel());
        }
        if (BlossomLootModifier.shouldProcessForBlock(block)) {
            BlossomLootModifier.applyToHarvestDrops(event.getDrops(), world, harvester, tool, event.isSilkTouching(), event.getFortuneLevel());
        }
    }
}
