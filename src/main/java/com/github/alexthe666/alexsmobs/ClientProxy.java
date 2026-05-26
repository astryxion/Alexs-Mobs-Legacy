package com.github.alexthe666.alexsmobs;

import com.github.alexthe666.alexsmobs.block.AMBlockRegistry;
import com.github.alexthe666.alexsmobs.client.event.ClientEvents;
import com.github.alexthe666.alexsmobs.client.model.*;
import com.github.alexthe666.alexsmobs.client.particle.*;
import com.github.alexthe666.alexsmobs.client.render.*;
import com.github.alexthe666.alexsmobs.client.render.layer.LayerTarantulaHawkElytra;
import com.github.alexthe666.alexsmobs.client.render.tile.RenderCapsid;
import com.github.alexthe666.alexsmobs.client.render.tile.RenderVoidWormBeak;
import com.github.alexthe666.alexsmobs.client.sound.SoundLaCucaracha;
import com.github.alexthe666.alexsmobs.client.sound.SoundWormBoss;
import com.github.alexthe666.alexsmobs.entity.*;
import com.github.alexthe666.alexsmobs.item.AMItemRegistry;
import com.github.alexthe666.alexsmobs.item.ItemAMSpawnEgg;
import com.github.alexthe666.alexsmobs.item.ItemStraddleboard;
import com.github.alexthe666.alexsmobs.item.ItemBloodSprayer;
import com.github.alexthe666.alexsmobs.item.ItemStinkRay;
import com.github.alexthe666.alexsmobs.item.ItemHemolymphBlaster;
import com.github.alexthe666.alexsmobs.tileentity.TileEntityCapsid;
import com.github.alexthe666.alexsmobs.tileentity.TileEntityVoidWormBeak;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.*;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.block.Block;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;

import java.util.*;

@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(modid = AlexsMobs.MODID, value = Side.CLIENT)
public class ClientProxy extends CommonProxy {

    private static final ModelRoadrunnerBoots ROADRUNNER_BOOTS_MODEL = new ModelRoadrunnerBoots(0.7F);
    private static final ModelMooseHeadgear MOOSE_HEADGEAR_MODEL = new ModelMooseHeadgear(0.3F);
    private static final ModelFrontierCap FRONTIER_CAP_MODEL = new ModelFrontierCap(0.3F);
    private static final ModelSombrero SOMBRERO_MODEL = new ModelSombrero(0.3F);
    private static final ModelSpikedTurtleShell SPIKED_TURTLE_SHELL_MODEL = new ModelSpikedTurtleShell(1.0F);
    private static final ModelFedora FEDORA_MODEL = new ModelFedora(0.3F);
    private static final ModelAMElytra ELYTRA_MODEL = new ModelAMElytra();
    public static final Map<Integer, SoundLaCucaracha> COCKROACH_SOUND_MAP = new HashMap<>();
    public static final Map<Integer, SoundWormBoss> WORMBOSS_SOUND_MAP = new HashMap<>();
    public static List<UUID> currentUnrenderedEntities = new ArrayList<UUID>();
    public int prevThirdPersonView = 0;
    public static int voidPortalCreationTime = 0;

    @Override
    public void preInitClient() {
        clientInit();
    }

    @Override
    public void initClient() {
        setupParticles();
        registerPlayerLayers();
    }

    private void registerPlayerLayers() {
        for (RenderPlayer renderPlayer : Minecraft.getMinecraft().getRenderManager().getSkinMap().values()) {
            renderPlayer.addLayer(new LayerTarantulaHawkElytra());
        }
    }

    public void clientInit() {
        RenderingRegistry.registerEntityRenderingHandler(EntityGrizzlyBear.class, manager -> new RenderGrizzlyBear(manager));
        RenderingRegistry.registerEntityRenderingHandler(EntityRoadrunner.class, manager -> new RenderRoadrunner(manager));
        RenderingRegistry.registerEntityRenderingHandler(EntityBoneSerpent.class, manager -> new RenderBoneSerpent(manager));
        RenderingRegistry.registerEntityRenderingHandler(EntityBoneSerpentPart.class, manager -> new RenderBoneSerpentPart(manager));
        RenderingRegistry.registerEntityRenderingHandler(EntityGazelle.class, manager -> new RenderGazelle(manager));
        RenderingRegistry.registerEntityRenderingHandler(EntityCrocodile.class, manager -> new RenderCrocodile(manager));
        RenderingRegistry.registerEntityRenderingHandler(EntityFly.class, manager -> new RenderFly(manager));
        RenderingRegistry.registerEntityRenderingHandler(EntityHummingbird.class, manager -> new RenderHummingbird(manager));
        RenderingRegistry.registerEntityRenderingHandler(EntityOrca.class, manager -> new RenderOrca(manager));
        RenderingRegistry.registerEntityRenderingHandler(EntitySunbird.class, manager -> new RenderSunbird(manager));
        RenderingRegistry.registerEntityRenderingHandler(EntityGorilla.class, manager -> new RenderGorilla(manager));
        RenderingRegistry.registerEntityRenderingHandler(EntityCrimsonMosquito.class, manager -> new RenderCrimsonMosquito(manager));
        RenderingRegistry.registerEntityRenderingHandler(EntityMosquitoSpit.class, manager -> new RenderMosquitoSpit(manager));
        RenderingRegistry.registerEntityRenderingHandler(EntityRattlesnake.class, manager -> new RenderRattlesnake(manager));
        RenderingRegistry.registerEntityRenderingHandler(EntityEndergrade.class, manager -> new RenderEndergrade(manager));
        RenderingRegistry.registerEntityRenderingHandler(EntityHammerheadShark.class, manager -> new RenderHammerheadShark(manager));
        RenderingRegistry.registerEntityRenderingHandler(EntitySharkToothArrow.class, manager -> new RenderSharkToothArrow(manager));
        RenderingRegistry.registerEntityRenderingHandler(EntityLobster.class, manager -> new RenderLobster(manager));
        RenderingRegistry.registerEntityRenderingHandler(EntityKomodoDragon.class, manager -> new RenderKomodoDragon(manager));
        RenderingRegistry.registerEntityRenderingHandler(EntityCapuchinMonkey.class, manager -> new RenderCapuchinMonkey(manager));
        RenderingRegistry.registerEntityRenderingHandler(EntityTossedItem.class, manager -> new RenderTossedItem(manager));
        RenderingRegistry.registerEntityRenderingHandler(EntityCentipedeHead.class, manager -> new RenderCentipedeHead(manager));
        RenderingRegistry.registerEntityRenderingHandler(EntityCentipedeBody.class, manager -> new RenderCentipedeBody(manager));
        RenderingRegistry.registerEntityRenderingHandler(EntityCentipedeTail.class, manager -> new RenderCentipedeTail(manager));
        RenderingRegistry.registerEntityRenderingHandler(EntityWarpedToad.class, manager -> new RenderWarpedToad(manager));
        RenderingRegistry.registerEntityRenderingHandler(EntityMoose.class, manager -> new RenderMoose(manager));
        RenderingRegistry.registerEntityRenderingHandler(EntityMimicube.class, manager -> new RenderMimicube(manager));
        RenderingRegistry.registerEntityRenderingHandler(EntityRaccoon.class, manager -> new RenderRaccoon(manager));
        RenderingRegistry.registerEntityRenderingHandler(EntityBlobfish.class, manager -> new RenderBlobfish(manager));
        RenderingRegistry.registerEntityRenderingHandler(EntitySeal.class, manager -> new RenderSeal(manager));
        RenderingRegistry.registerEntityRenderingHandler(EntityCockroach.class, manager -> new RenderCockroach(manager));
        RenderingRegistry.registerEntityRenderingHandler(EntityCockroachEgg.class, manager -> new RenderSnowball(manager, AMItemRegistry.COCKROACH_OOTHECA, Minecraft.getMinecraft().getRenderItem()));
        RenderingRegistry.registerEntityRenderingHandler(EntityShoebill.class, manager -> new RenderShoebill(manager));
        RenderingRegistry.registerEntityRenderingHandler(EntityElephant.class, manager -> new RenderElephant(manager));
        RenderingRegistry.registerEntityRenderingHandler(EntitySoulVulture.class, manager -> new RenderSoulVulture(manager));
        RenderingRegistry.registerEntityRenderingHandler(EntitySnowLeopard.class, manager -> new RenderSnowLeopard(manager));
        RenderingRegistry.registerEntityRenderingHandler(EntitySpectre.class, manager -> new RenderSpectre(manager));
        RenderingRegistry.registerEntityRenderingHandler(EntityCrow.class, manager -> new RenderCrow(manager));
        RenderingRegistry.registerEntityRenderingHandler(EntityAlligatorSnappingTurtle.class, manager -> new RenderAlligatorSnappingTurtle(manager));
        RenderingRegistry.registerEntityRenderingHandler(EntityMungus.class, manager -> new RenderMungus(manager));
        RenderingRegistry.registerEntityRenderingHandler(EntityMantisShrimp.class, manager -> new RenderMantisShrimp(manager));
        RenderingRegistry.registerEntityRenderingHandler(EntityGuster.class, manager -> new RenderGuster(manager));
        RenderingRegistry.registerEntityRenderingHandler(EntitySandShot.class, manager -> new RenderSandShot(manager));
        RenderingRegistry.registerEntityRenderingHandler(EntityGust.class, manager -> new RenderGust(manager));
        RenderingRegistry.registerEntityRenderingHandler(EntityWarpedMosco.class, manager -> new RenderWarpedMosco(manager));
        RenderingRegistry.registerEntityRenderingHandler(EntityHemolymph.class, manager -> new RenderHemolymph(manager));
        RenderingRegistry.registerEntityRenderingHandler(EntityStraddler.class, manager -> new RenderStraddler(manager));
        RenderingRegistry.registerEntityRenderingHandler(EntityStradpole.class, manager -> new RenderStradpole(manager));
        RenderingRegistry.registerEntityRenderingHandler(EntityStraddleboard.class, manager -> new RenderStraddleboard(manager));
        RenderingRegistry.registerEntityRenderingHandler(EntityEmu.class, manager -> new RenderEmu(manager));
        RenderingRegistry.registerEntityRenderingHandler(EntityEmuEgg.class, manager -> new RenderSnowball(manager, AMItemRegistry.EMU_EGG, Minecraft.getMinecraft().getRenderItem()));
        RenderingRegistry.registerEntityRenderingHandler(EntityPlatypus.class, manager -> new RenderPlatypus(manager));
        RenderingRegistry.registerEntityRenderingHandler(EntityDropBear.class, manager -> new RenderDropBear(manager));
        RenderingRegistry.registerEntityRenderingHandler(EntityTasmanianDevil.class, manager -> new RenderTasmanianDevil(manager));
        RenderingRegistry.registerEntityRenderingHandler(EntityKangaroo.class, manager -> new RenderKangaroo(manager));
        RenderingRegistry.registerEntityRenderingHandler(EntityCachalotWhale.class, manager -> new RenderCachalotWhale(manager));
        RenderingRegistry.registerEntityRenderingHandler(EntityCachalotEcho.class, manager -> new RenderCachalotEcho(manager));
        RenderingRegistry.registerEntityRenderingHandler(EntityLeafcutterAnt.class, manager -> new RenderLeafcutterAnt(manager));
        RenderingRegistry.registerEntityRenderingHandler(EntityEnderiophage.class, manager -> new RenderEnderiophage(manager));
        RenderingRegistry.registerEntityRenderingHandler(EntityEnderiophageRocket.class, manager -> new RenderSnowball(manager, AMItemRegistry.ENDERIOPHAGE_ROCKET, Minecraft.getMinecraft().getRenderItem()));
        RenderingRegistry.registerEntityRenderingHandler(EntityBaldEagle.class, manager -> new RenderBaldEagle(manager));
        RenderingRegistry.registerEntityRenderingHandler(EntityTiger.class, manager -> new RenderTiger(manager));
        RenderingRegistry.registerEntityRenderingHandler(EntityTarantulaHawk.class, manager -> new RenderTarantulaHawk(manager));
        RenderingRegistry.registerEntityRenderingHandler(EntityVoidWorm.class, manager -> new RenderVoidWormHead(manager));
        RenderingRegistry.registerEntityRenderingHandler(EntityVoidWormPart.class, manager -> new RenderVoidWormBody(manager));
        RenderingRegistry.registerEntityRenderingHandler(EntityVoidWormShot.class, manager -> new RenderVoidWormShot(manager));
        RenderingRegistry.registerEntityRenderingHandler(EntityVoidPortal.class, manager -> new RenderVoidPortal(manager));
        RenderingRegistry.registerEntityRenderingHandler(EntityFrilledShark.class, manager -> new RenderFrilledShark(manager));
        RenderingRegistry.registerEntityRenderingHandler(EntityMimicOctopus.class, manager -> new RenderMimicOctopus(manager));
        RenderingRegistry.registerEntityRenderingHandler(EntitySeagull.class, manager -> new RenderSeagull(manager));
        RenderingRegistry.registerEntityRenderingHandler(EntityBison.class, manager -> new RenderBison(manager));
        RenderingRegistry.registerEntityRenderingHandler(EntityRhinoceros.class, manager -> new RenderRhinoceros(manager));
        RenderingRegistry.registerEntityRenderingHandler(EntitySkunk.class, manager -> new RenderSkunk(manager));
        RenderingRegistry.registerEntityRenderingHandler(EntityTusklin.class, manager -> new RenderTusklin(manager));
        RenderingRegistry.registerEntityRenderingHandler(EntityBlueJay.class, manager -> new RenderBlueJay(manager));
        RenderingRegistry.registerEntityRenderingHandler(EntityAnteater.class, manager -> new RenderAnteater(manager));
        RenderingRegistry.registerEntityRenderingHandler(EntityBananaSlug.class, manager -> new RenderBananaSlug(manager));
        RenderingRegistry.registerEntityRenderingHandler(EntityAnaconda.class, manager -> new RenderAnaconda(manager));
        RenderingRegistry.registerEntityRenderingHandler(EntityAnacondaPart.class, manager -> new RenderAnacondaPart(manager));
        RenderingRegistry.registerEntityRenderingHandler(EntityVineLasso.class, manager -> new RenderVineLasso(manager));
        RenderingRegistry.registerEntityRenderingHandler(EntityLaviathan.class, manager -> new RenderLaviathan(manager));
        RenderingRegistry.registerEntityRenderingHandler(EntityGiantSquid.class, manager -> new RenderGiantSquid(manager));
        RenderingRegistry.registerEntityRenderingHandler(EntityGiantSquidPart.class, manager -> new RenderMultipartHitbox(manager));
        RenderingRegistry.registerEntityRenderingHandler(EntityLaviathanPart.class, manager -> new RenderMultipartHitbox(manager));
        RenderingRegistry.registerEntityRenderingHandler(EntityCachalotPart.class, manager -> new RenderMultipartHitbox(manager));
        RenderingRegistry.registerEntityRenderingHandler(EntitySquidGrapple.class, manager -> new RenderSquidGrapple(manager));
        RenderingRegistry.registerEntityRenderingHandler(EntityFart.class, manager -> new RenderFart(manager));
        RenderingRegistry.registerEntityRenderingHandler(EntityJerboa.class, manager -> new RenderJerboa(manager));
        RenderingRegistry.registerEntityRenderingHandler(EntityRainFrog.class, manager -> new RenderRainFrog(manager));
        RenderingRegistry.registerEntityRenderingHandler(EntityTriops.class, manager -> new RenderTriops(manager));
        RenderingRegistry.registerEntityRenderingHandler(EntityFlyingFish.class, manager -> new RenderFlyingFish(manager));
        RenderingRegistry.registerEntityRenderingHandler(EntityCombJelly.class, manager -> new RenderCombJelly(manager));
        RenderingRegistry.registerEntityRenderingHandler(EntityCosmicCod.class, manager -> new RenderCosmicCod(manager));
        RenderingRegistry.registerEntityRenderingHandler(EntityDevilsHolePupfish.class, manager -> new RenderDevilsHolePupfish(manager));
        RenderingRegistry.registerEntityRenderingHandler(EntityCatfish.class, manager -> new RenderCatfish(manager));
        RenderingRegistry.registerEntityRenderingHandler(EntityTerrapin.class, manager -> new RenderTerrapin(manager));
        RenderingRegistry.registerEntityRenderingHandler(EntityGeladaMonkey.class, manager -> new RenderGeladaMonkey(manager));
        RenderingRegistry.registerEntityRenderingHandler(EntityManedWolf.class, manager -> new RenderManedWolf(manager));
        RenderingRegistry.registerEntityRenderingHandler(EntityCaiman.class, manager -> new RenderCaiman(manager));
        RenderingRegistry.registerEntityRenderingHandler(EntityToucan.class, manager -> new RenderToucan(manager));
        RenderingRegistry.registerEntityRenderingHandler(EntityPotoo.class, manager -> new RenderPotoo(manager));
        RenderingRegistry.registerEntityRenderingHandler(EntitySugarGlider.class, manager -> new RenderSugarGlider(manager));
        MinecraftForge.EVENT_BUS.register(new ClientEvents());
        try {
            AMItemRegistry.BLOOD_SPRAYER.addPropertyOverride(new ResourceLocation("empty"), (stack, world, entity) ->
                    !ItemBloodSprayer.isUsable(stack) || entity instanceof EntityPlayer && ((EntityPlayer) entity).getCooldownTracker().hasCooldown(AMItemRegistry.BLOOD_SPRAYER) ? 1.0F : 0.0F);
            AMItemRegistry.HEMOLYMPH_BLASTER.addPropertyOverride(new ResourceLocation("empty"), (stack, world, entity) ->
                    !ItemHemolymphBlaster.isUsable(stack) || entity instanceof EntityPlayer && ((EntityPlayer) entity).getCooldownTracker().hasCooldown(AMItemRegistry.HEMOLYMPH_BLASTER) ? 1.0F : 0.0F);
            AMItemRegistry.SHIELD_OF_THE_DEEP.addPropertyOverride(new ResourceLocation("blocking"), (stack, world, entity) ->
                    entity != null && entity.isHandActive() && entity.getActiveItemStack() == stack ? 1.0F : 0.0F);
            AMItemRegistry.FALCONRY_GLOVE.addPropertyOverride(new ResourceLocation("in_hand"), (stack, world, entity) -> {
                if (!isRenderingHandHeldItem()) {
                    return 0.0F;
                }
                if (entity instanceof EntityLivingBase && stack.getItem() == AMItemRegistry.FALCONRY_GLOVE) {
                    EntityLivingBase living = (EntityLivingBase) entity;
                    if (living.getHeldItemMainhand() == stack || living.getHeldItemOffhand() == stack) {
                        return 1.0F;
                    }
                }
                return 0.0F;
            });
            AMItemRegistry.VINE_LASSO.addPropertyOverride(new ResourceLocation("in_hand"), (stack, world, entity) -> {
                if (!isRenderingHandHeldItem()) {
                    return 0.0F;
                }
                if (entity instanceof EntityLivingBase && stack.getItem() == AMItemRegistry.VINE_LASSO) {
                    EntityLivingBase living = (EntityLivingBase) entity;
                    if (living.getHeldItemMainhand() == stack || living.getHeldItemOffhand() == stack) {
                        return 1.0F;
                    }
                }
                return 0.0F;
            });
            AMItemRegistry.STINK_RAY.addPropertyOverride(new ResourceLocation("empty"), (stack, world, entity) ->
                    !ItemStinkRay.isUsable(stack) ? 1.0F : 0.0F);
            AMItemRegistry.STINK_RAY.addPropertyOverride(new ResourceLocation("in_hand"), (stack, world, entity) -> {
                if (!isRenderingHandHeldItem()) {
                    return 0.0F;
                }
                if (entity instanceof EntityLivingBase && stack.getItem() == AMItemRegistry.STINK_RAY) {
                    EntityLivingBase living = (EntityLivingBase) entity;
                    if (living.getHeldItemMainhand() == stack || living.getHeldItemOffhand() == stack) {
                        return 1.0F;
                    }
                }
                return 0.0F;
            });
        } catch (Exception e) {
            AlexsMobs.LOGGER.warn("Could not load item models for weapons");
        }
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityCapsid.class, new RenderCapsid());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityVoidWormBeak.class, new RenderVoidWormBeak());

    }

    /**
     * 1.12 binds item models on {@link ModelRegistryEvent}; registry names match JSON under {@code models/item/}.
     */
    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void onModelRegistry(ModelRegistryEvent event) {
        for (Item item : ForgeRegistries.ITEMS.getValuesCollection()) {
            ResourceLocation rl = item.getRegistryName();
            if (rl != null && AlexsMobs.MODID.equals(rl.getResourceDomain())) {
                ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(rl, "inventory"));
            }
        }
        for (Block block : ForgeRegistries.BLOCKS.getValuesCollection()) {
            ResourceLocation rl = block.getRegistryName();
            if (rl != null && AlexsMobs.MODID.equals(rl.getResourceDomain())) {
                Item item = Item.getItemFromBlock(block);
                if (item != null && item != net.minecraft.init.Items.AIR) {
                    ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(rl, "inventory"));
                }
            }
        }
        ModelLoader.setCustomModelResourceLocation(AMItemRegistry.FALCONRY_GLOVE_HAND, 0, new ModelResourceLocation("alexsmobs:falconry_glove_hand", "inventory"));
        ModelLoader.setCustomModelResourceLocation(AMItemRegistry.FALCONRY_GLOVE_INVENTORY, 0, new ModelResourceLocation("alexsmobs:falconry_glove_inventory", "inventory"));
        ModelLoader.setCustomModelResourceLocation(AMItemRegistry.VINE_LASSO_HAND, 0, new ModelResourceLocation("alexsmobs:vine_lasso_hand", "inventory"));
        ModelLoader.setCustomModelResourceLocation(AMItemRegistry.VINE_LASSO_INVENTORY, 0, new ModelResourceLocation("alexsmobs:vine_lasso_inventory", "inventory"));
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void onItemColors(ColorHandlerEvent.Item event) {
        event.getItemColors().registerItemColorHandler((stack, colorIn) -> colorIn < 1 ? -1 : ((ItemStraddleboard) stack.getItem()).getColor(stack), AMItemRegistry.STRADDLEBOARD);
        for (Item item : ForgeRegistries.ITEMS.getValuesCollection()) {
            if (item instanceof ItemAMSpawnEgg) {
                event.getItemColors().registerItemColorHandler((stack, tintIndex) -> ((ItemAMSpawnEgg) item).getColorFromItemStack(stack, tintIndex), item);
            }
        }
    }

    private static final String ANIMAL_DICTIONARY_GUI = "com.github.alexthe666.alexsmobs.client.gui.GUIAnimalDictionary";

    public void openBookGUI(ItemStack itemStackIn) {
        Minecraft.getMinecraft().displayGuiScreen(createAnimalDictionaryGui(itemStackIn, null));
    }

    public void openBookGUI(ItemStack itemStackIn, String page) {
        Minecraft.getMinecraft().displayGuiScreen(createAnimalDictionaryGui(itemStackIn, page));
    }

    /**
     * Loaded by name so {@link ClientProxy} can be verified during mod construction before Citadel's jar
     * is guaranteed on the classpath (see {@code required-after:citadel} on {@link AlexsMobs}).
     */
    private static GuiScreen createAnimalDictionaryGui(ItemStack itemStackIn, @javax.annotation.Nullable String page) {
        try {
            Class<?> guiClass = Class.forName(ANIMAL_DICTIONARY_GUI, true, ClientProxy.class.getClassLoader());
            if (page == null) {
                return (GuiScreen) guiClass.getConstructor(ItemStack.class).newInstance(itemStackIn);
            }
            return (GuiScreen) guiClass.getConstructor(ItemStack.class, String.class).newInstance(itemStackIn, page);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to open animal dictionary GUI", e);
        }
    }

  /** True only while the item renderer is drawing a held stack (first/third person), not GUI slots. */
    private static boolean isRenderingHandHeldItem() {
        for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
            String method = element.getMethodName();
            if ("renderItemIntoGUI".equals(method) || "renderItemAndEffectIntoGUI".equals(method)) {
                return false;
            }
            if ("renderItemInFirstPerson".equals(method) || "renderItemSide".equals(method) || "renderHeldItem".equals(method)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Item setupISTER(Item item) {
        item.setTileEntityItemStackRenderer(new AMItemstackRenderer());
        return item;
    }

    public EntityPlayer getClientSidePlayer() {
        return Minecraft.getMinecraft().player;
    }

    @SideOnly(Side.CLIENT)
    public Object getArmorModel(int armorId, EntityLivingBase entity) {
        switch (armorId) {
            case 0:
                return ROADRUNNER_BOOTS_MODEL;
            case 1:
                return MOOSE_HEADGEAR_MODEL;
            case 2:
                return FRONTIER_CAP_MODEL.withAnimations(entity);
            case 3:
                return SOMBRERO_MODEL;
            case 4:
                return SPIKED_TURTLE_SHELL_MODEL;
            case 5:
                return FEDORA_MODEL;
            case 6:
                return ELYTRA_MODEL.withAnimations(entity);
            default:
                return null;
        }
    }

    private int singingBlueJayId = -1;

    @SideOnly(Side.CLIENT)
    public void onEntityStatus(Entity entity, byte updateKind) {
        if (entity instanceof EntityBlueJay && entity.isEntityAlive() && updateKind == 67) {
            singingBlueJayId = entity.getEntityId();
        }
        if (entity instanceof EntityBlueJay && entity.isEntityAlive() && updateKind == 68) {
            singingBlueJayId = -1;
        }
        if(entity instanceof EntityCockroach && entity.isEntityAlive() && updateKind == 67){
            SoundLaCucaracha sound;
            if(COCKROACH_SOUND_MAP.get(entity.getEntityId()) == null){
                sound = new SoundLaCucaracha((EntityCockroach)entity);
                COCKROACH_SOUND_MAP.put(entity.getEntityId(), sound);
            }else{
                sound = COCKROACH_SOUND_MAP.get(entity.getEntityId());
            }
            if(!Minecraft.getMinecraft().getSoundHandler().isSoundPlaying(sound) && sound.shouldPlaySound() && sound.isOnlyCockroach()){
                Minecraft.getMinecraft().getSoundHandler().playSound(sound);
            }
        }
        if(entity instanceof EntityVoidWorm && entity.isEntityAlive() && updateKind == 67){
            float f2 = Minecraft.getMinecraft().gameSettings.getSoundLevel(SoundCategory.MUSIC);
            if(f2 <= 0){
                WORMBOSS_SOUND_MAP.clear();
            }else{
                SoundWormBoss sound;
                if(WORMBOSS_SOUND_MAP.get(entity.getEntityId()) == null){
                    sound = new SoundWormBoss((EntityVoidWorm)entity);
                    WORMBOSS_SOUND_MAP.put(entity.getEntityId(), sound);
                }else{
                    sound = WORMBOSS_SOUND_MAP.get(entity.getEntityId());
                }
                if(!Minecraft.getMinecraft().getSoundHandler().isSoundPlaying(sound) && sound.isNearest()){
                    Minecraft.getMinecraft().getSoundHandler().playSound(sound);
                }
            }

        }
    }

    public void updateBiomeVisuals(int x, int z) {
        Minecraft.getMinecraft().renderGlobal.markBlockRangeForRenderUpdate(x - 32, 0, z - 32, x + 32, 255, z + 32);
    }

    public void setupParticles() {
        AMParticleRegistry.registerFactories();
    }


    public void setRenderViewEntity(Entity entity){
        prevThirdPersonView = Minecraft.getMinecraft().gameSettings.thirdPersonView;
        Minecraft.getMinecraft().setRenderViewEntity(entity);
        Minecraft.getMinecraft().gameSettings.thirdPersonView = 1;
    }

    public void resetRenderViewEntity(){
        Minecraft.getMinecraft().setRenderViewEntity(Minecraft.getMinecraft().player);
    }

    public int getPreviousPOV(){
        return prevThirdPersonView;
    }

    public boolean isFarFromCamera(double x, double y, double z) {
        Minecraft lvt_1_1_ = Minecraft.getMinecraft();
        Entity view = lvt_1_1_.getRenderViewEntity() != null ? lvt_1_1_.getRenderViewEntity() : lvt_1_1_.player;
        return view.getDistanceSq(x, y, z) >= 256.0D;
    }

    public void resetVoidPortalCreation(EntityPlayer player){

    }

    @Override
    public int getSingingBlueJayId() {
        return singingBlueJayId;
    }

}
