package com.github.alexthe666.alexsmobs.client.event;

import com.github.alexthe666.alexsmobs.AlexsMobs;
import com.github.alexthe666.alexsmobs.ClientProxy;
import com.github.alexthe666.alexsmobs.client.model.ModelWanderingVillagerRider;
import com.github.alexthe666.alexsmobs.client.render.AMItemstackRenderer;
import com.github.alexthe666.alexsmobs.client.render.LavaVisionFluidRenderer;
import com.github.alexthe666.alexsmobs.client.render.RenderVineLasso;
import com.github.alexthe666.alexsmobs.config.AMConfig;
import com.github.alexthe666.alexsmobs.effect.AMEffectRegistry;
import com.github.alexthe666.alexsmobs.entity.EntityBaldEagle;
import com.github.alexthe666.alexsmobs.entity.EntityBlueJay;
import com.github.alexthe666.alexsmobs.entity.EntityElephant;
import com.github.alexthe666.alexsmobs.entity.util.VineLassoUtil;
import net.minecraft.entity.Entity;
import com.github.alexthe666.alexsmobs.item.AMItemRegistry;
import com.github.alexthe666.alexsmobs.item.ItemTarantulaHawkElytra;
import com.github.alexthe666.alexsmobs.message.MessageUpdateEagleControls;
import com.github.alexthe666.alexsmobs.misc.AMTagRegistry;
import com.github.alexthe666.citadel.client.event.EventGetOutlineColor;
import com.github.alexthe666.citadel.client.gui.BookBlit;
import com.google.common.base.MoreObjects;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.BlockFluidRenderer;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.util.ReportedException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;
import net.minecraft.potion.Potion;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderSpecificHandEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.lang.reflect.Field;

@SideOnly(Side.CLIENT)
public class ClientEvents {

    private static final ResourceLocation RADIUS_TEXTURE = new ResourceLocation("alexsmobs:textures/falconry_radius.png");
    private static final ResourceLocation FLIP_SHADER = new ResourceLocation("minecraft:shaders/post/flip.json");
    private static final Field MAIN_MODEL_FIELD = ReflectionHelper.findField(RenderLivingBase.class, "mainModel", "field_77045_g");
    private static final Field SWINGING_HAND_FIELD = ReflectionHelper.findField(EntityLivingBase.class, "swingingHand", "field_184622_au");

    private boolean previousLavaVision = false;
    private BlockFluidRenderer previousFluidRenderer;
    private boolean flipPostShaderActive;
    private Boolean savedPlayerNameTagVisibility;
    private boolean sentGlideRequestThisFall;
    private int glideRequestCooldown;

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onOutlineEntityColor(EventGetOutlineColor event) {
        if (event.getEntityIn() instanceof net.minecraft.entity.monster.IMob && AlexsMobs.PROXY.getSingingBlueJayId() != -1) {
            Entity singer = event.getEntityIn().world.getEntityByID(AlexsMobs.PROXY.getSingingBlueJayId());
            if (singer instanceof EntityBlueJay && singer.isEntityAlive() && ((EntityBlueJay) singer).isMakingMonstersBlue()) {
                event.setColor(0X4B95FE);
                event.setResult(Event.Result.ALLOW);
            }
        }
        if (event.getEntityIn() instanceof EntityItem && AMTagRegistry.itemInTag(AMTagRegistry.VOID_WORM_DROPS, ((EntityItem) event.getEntityIn()).getItem().getItem())) {
            int fromColor = 0;
            int toColor = 0X21E5FF;
            float startR = (float) (fromColor >> 16 & 255) / 255.0F;
            float startG = (float) (fromColor >> 8 & 255) / 255.0F;
            float startB = (float) (fromColor & 255) / 255.0F;
            float endR = (float) (toColor >> 16 & 255) / 255.0F;
            float endG = (float) (toColor >> 8 & 255) / 255.0F;
            float endB = (float) (toColor & 255) / 255.0F;
            float f = (float) (Math.cos(0.4F * (event.getEntityIn().ticksExisted + Minecraft.getMinecraft().getRenderPartialTicks())) + 1.0F) * 0.5F;
            float r = (endR - startR) * f + startR;
            float g = (endG - startG) * f + startG;
            float b = (endB - startB) * f + startB;
            int j = ((((int) (r * 255)) & 0xFF) << 16)
                    | ((((int) (g * 255)) & 0xFF) << 8)
                    | ((((int) (b * 255)) & 0xFF) << 0);
            event.setColor(j);
            event.setResult(Event.Result.ALLOW);
        }
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onFogDensity(EntityViewRenderEvent.FogDensity event) {
        IBlockState state = event.getState();
        Block b = state.getBlock();
        if (Minecraft.getMinecraft().player != null && Minecraft.getMinecraft().player.isPotionActive(AMEffectRegistry.LAVA_VISION)) {
            if (b == Blocks.LAVA || b == Blocks.FLOWING_LAVA) {
                event.setDensity(0.05F);
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onFOVUpdate(EntityViewRenderEvent.FOVModifier event) {
        if (event.getEntity() instanceof EntityLivingBase && ((EntityLivingBase) event.getEntity()).isPotionActive(AMEffectRegistry.FEAR)) {
            event.setFOV(1.0F);
        }
    }

    /**
     * 1.16 had {@code WanderingTraderEntity}; 1.12.2 has no such class — match registry name / class name for parity with modded backports.
     */
    private static boolean isWanderingMerchantLike(Entity entity) {
        if (entity == null) {
            return false;
        }
        String cls = entity.getClass().getName();
        return cls.contains("WanderingTrader");
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onPreRenderEntity(RenderLivingEvent.Pre<?> event) {
        EntityLivingBase entity = event.getEntity();
        RenderLivingBase<?> renderer = event.getRenderer();
        if (isWanderingMerchantLike(entity)) {
            if (entity.getRidingEntity() instanceof EntityElephant) {
                try {
                    if (!(MAIN_MODEL_FIELD.get(renderer) instanceof ModelWanderingVillagerRider)) {
                        MAIN_MODEL_FIELD.set(renderer, new ModelWanderingVillagerRider());
                    }
                } catch (IllegalAccessException e) {
                    AlexsMobs.LOGGER.warn("Could not swap wandering merchant model on elephant", e);
                }
            }
        }
        if (entity.isPotionActive(AMEffectRegistry.CLINGING) && entity.getEyeHeight() < entity.height * 0.45F || entity.isPotionActive(AMEffectRegistry.DEBILITATING_STING) && entity.getCreatureAttribute() == EnumCreatureAttribute.ARTHROPOD && entity.width > entity.height) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(0.0D, entity.height + 0.1F, 0.0D);
            GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
            entity.prevRenderYawOffset = -entity.prevRenderYawOffset;
            entity.renderYawOffset = -entity.renderYawOffset;
            entity.prevRotationYawHead = -entity.prevRotationYawHead;
            entity.rotationYawHead = -entity.rotationYawHead;
        }
        if (entity.isPotionActive(AMEffectRegistry.ENDER_FLU)) {
            GlStateManager.pushMatrix();
            GlStateManager.rotate((float) (Math.cos((double) entity.ticksExisted * 7F) * Math.PI * (double) 1.2F), 0.0F, 1.0F, 0.0F);
            float vibrate = 0.05F;
            GlStateManager.translate((entity.getRNG().nextFloat() - 0.5F) * vibrate, (entity.getRNG().nextFloat() - 0.5F) * vibrate, (entity.getRNG().nextFloat() - 0.5F) * vibrate);
        }
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onPostRenderEntity(RenderLivingEvent.Post<?> event) {
        EntityLivingBase entity = event.getEntity();
        if (entity.isPotionActive(AMEffectRegistry.ENDER_FLU)) {
            GlStateManager.popMatrix();
        }
        if (entity.isPotionActive(AMEffectRegistry.CLINGING) && entity.getEyeHeight() < entity.height * 0.45F || entity.isPotionActive(AMEffectRegistry.DEBILITATING_STING) && entity.getCreatureAttribute() == EnumCreatureAttribute.ARTHROPOD && entity.width > entity.height) {
            GlStateManager.popMatrix();
            entity.prevRenderYawOffset = -entity.prevRenderYawOffset;
            entity.renderYawOffset = -entity.renderYawOffset;
            entity.prevRotationYawHead = -entity.prevRotationYawHead;
            entity.rotationYawHead = -entity.rotationYawHead;
        }
        if (VineLassoUtil.hasLassoData(entity) && !(entity instanceof EntityPlayer)) {
            Entity lassoedOwner = VineLassoUtil.getLassoedTo(entity);
            if (lassoedOwner instanceof EntityLivingBase && lassoedOwner != entity) {
                EntityLivingBase owner = (EntityLivingBase) lassoedOwner;
                RenderVineLasso.renderVine(entity, event.getPartialRenderTick(), owner, event.getX(), event.getY(), event.getZ(), owner.getPrimaryHand() != EnumHandSide.LEFT, 0.1F);
            }
        }
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onRenderHand(RenderHandEvent event) {
        if (Minecraft.getMinecraft().getRenderViewEntity() instanceof EntityBaldEagle) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onRenderSpecificHand(RenderSpecificHandEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayer player = mc.player;
        if (player == null) {
            return;
        }
        ItemStack handStack = event.getItemStack();
        if (!player.getPassengers().isEmpty() && event.getHand() == EnumHand.MAIN_HAND) {
            boolean leftHand = false;
            if (player.getHeldItem(EnumHand.MAIN_HAND).getItem() == AMItemRegistry.FALCONRY_GLOVE) {
                leftHand = player.getPrimaryHand() == EnumHandSide.LEFT;
            } else if (player.getHeldItem(EnumHand.OFF_HAND).getItem() == AMItemRegistry.FALCONRY_GLOVE) {
                leftHand = player.getPrimaryHand() != EnumHandSide.LEFT;
            }
            for (Entity entity : player.getPassengers()) {
                if (entity instanceof EntityBaldEagle) {
                    float yaw = player.prevRenderYawOffset + (player.renderYawOffset - player.prevRenderYawOffset) * event.getPartialTicks();
                    ClientProxy.currentUnrenderedEntities.remove(entity.getUniqueID());
                    GlStateManager.pushMatrix();
                    GlStateManager.scale(0.5F, 0.5F, 0.5F);
                    GlStateManager.translate(leftHand ? -0.8F : 0.8F, -0.6F, -1F);
                    GlStateManager.rotate(yaw, 0.0F, 1.0F, 0.0F);
                    if (leftHand) {
                        GlStateManager.rotate(90.0F, 0.0F, 1.0F, 0.0F);
                    } else {
                        GlStateManager.rotate(-90.0F, 0.0F, 1.0F, 0.0F);
                    }
                    renderEntitySimple(entity, 0.0D, 0.0D, 0.0D, 0.0F, event.getPartialTicks());
                    GlStateManager.popMatrix();
                    ClientProxy.currentUnrenderedEntities.add(entity.getUniqueID());
                }
            }
        }
        if (player.getActiveItemStack().getItem() == AMItemRegistry.DIMENSIONAL_CARVER && event.getItemStack().getItem() == AMItemRegistry.DIMENSIONAL_CARVER) {
            GlStateManager.pushMatrix();
            EnumHand hand = MoreObjects.firstNonNull(getSwingingHand(player), EnumHand.MAIN_HAND);
            float f = player.getSwingProgress(event.getPartialTicks());
            float f5 = -0.4F * MathHelper.sin(MathHelper.sqrt(f) * (float) Math.PI);
            float f6 = 0.2F * MathHelper.sin(MathHelper.sqrt(f) * ((float) Math.PI * 2F));
            float f10 = -0.2F * MathHelper.sin(f * (float) Math.PI);
            EnumHandSide handside = hand == EnumHand.MAIN_HAND ? player.getPrimaryHand() : player.getPrimaryHand().opposite();
            boolean flag3 = handside == EnumHandSide.RIGHT;
            int l = flag3 ? 1 : -1;
            GlStateManager.translate((float) l * f5, f6, f10);
            GlStateManager.popMatrix();
        }
    }

    private static EnumHand getSwingingHand(EntityLivingBase living) {
        try {
            return (EnumHand) SWINGING_HAND_FIELD.get(living);
        } catch (IllegalAccessException e) {
            return EnumHand.MAIN_HAND;
        }
    }

    private static <E extends Entity> void renderEntitySimple(E entityIn, double x, double y, double z, float yaw, float partialTicks) {
        Render<E> render = null;
        RenderManager manager = Minecraft.getMinecraft().getRenderManager();
        try {
            @SuppressWarnings("unchecked")
            Render<E> r = (Render<E>) manager.getEntityRenderObject(entityIn);
            render = r;

            if (render != null) {
                try {
                    render.doRender(entityIn, x, y, z, yaw, partialTicks);
                } catch (Throwable throwable1) {
                    throw new ReportedException(CrashReport.makeCrashReport(throwable1, "Rendering entity in world"));
                }
            }
        } catch (Throwable throwable3) {
            CrashReport crashreport = CrashReport.makeCrashReport(throwable3, "Rendering entity in world");
            CrashReportCategory crashreportcategory = crashreport.makeCategory("Entity being rendered");
            entityIn.addEntityCrashInfo(crashreportcategory);
            CrashReportCategory crashreportcategory1 = crashreport.makeCategory("Renderer details");
            crashreportcategory1.addCrashSection("Assigned renderer", render);
            crashreportcategory1.addCrashSection("Location", CrashReportCategory.getCoordinateInfo(x, y, z));
            crashreportcategory1.addCrashSection("Rotation", Float.valueOf(yaw));
            crashreportcategory1.addCrashSection("Delta", Float.valueOf(partialTicks));
            throw new ReportedException(crashreport);
        }
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onClientPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !event.player.world.isRemote) {
            return;
        }
        if (!(event.player instanceof EntityPlayerSP)) {
            return;
        }
        EntityPlayerSP player = (EntityPlayerSP) event.player;
        if (!ItemTarantulaHawkElytra.isWearingUsable(player)) {
            sentGlideRequestThisFall = false;
            glideRequestCooldown = 0;
            return;
        }
        if (player.capabilities.isFlying || player.isRiding()) {
            sentGlideRequestThisFall = false;
            glideRequestCooldown = 0;
            return;
        }
        if (player.onGround) {
            sentGlideRequestThisFall = false;
            glideRequestCooldown = 0;
            return;
        }
        if (player.isElytraFlying()) {
            sentGlideRequestThisFall = false;
            return;
        }
        ItemStack chest = player.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
        if (player.movementInput.jump && ItemTarantulaHawkElytra.canStartGliding(player, chest)) {
            if (glideRequestCooldown <= 0) {
                AlexsMobs.sendMSGToServer(new ItemTarantulaHawkElytra.MessageStartGlide());
                sentGlideRequestThisFall = true;
                glideRequestCooldown = 5;
            } else {
                glideRequestCooldown--;
            }
        }
    }

    /**
     * 1.12.2 has no {@code RenderNameplateEvent}; approximate deny by toggling name-tag visibility on the local player while riding eagle view in singleplayer.
     */
    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onClientTickNameplateWorkaround(TickEvent.ClientTickEvent event) {
        if (event.side != Side.CLIENT || event.phase != TickEvent.Phase.END) {
            return;
        }
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        if (player == null) {
            return;
        }
        boolean hide = Minecraft.getMinecraft().getRenderViewEntity() instanceof EntityBaldEagle && Minecraft.getMinecraft().isSingleplayer();
        if (hide) {
            if (savedPlayerNameTagVisibility == null) {
                savedPlayerNameTagVisibility = player.getAlwaysRenderNameTag();
            }
            player.setAlwaysRenderNameTag(false);
        } else {
            if (savedPlayerNameTagVisibility != null) {
                player.setAlwaysRenderNameTag(savedPlayerNameTagVisibility);
                savedPlayerNameTagVisibility = null;
            }
        }
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onRenderWorldLastEvent(RenderWorldLastEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        AMItemstackRenderer.incrementTick();
        if (!AMConfig.shadersCompat) {
            if (mc.player != null && mc.player.isPotionActive(AMEffectRegistry.LAVA_VISION)) {
                if (!previousLavaVision) {
                    BlockRendererDispatcher dispatcher = mc.getBlockRendererDispatcher();
                    previousFluidRenderer = ReflectionHelper.getPrivateValue(BlockRendererDispatcher.class, dispatcher, "fluidRenderer", "field_175025_e");
                    ReflectionHelper.setPrivateValue(BlockRendererDispatcher.class, dispatcher, new LavaVisionFluidRenderer(mc.getBlockColors()), "fluidRenderer", "field_175025_e");
                    updateAllChunks();
                }
            } else {
                if (previousLavaVision) {
                    if (previousFluidRenderer != null) {
                        ReflectionHelper.setPrivateValue(BlockRendererDispatcher.class, mc.getBlockRendererDispatcher(), previousFluidRenderer, "fluidRenderer", "field_175025_e");
                        previousFluidRenderer = null;
                    }
                    updateAllChunks();
                }
            }
            previousLavaVision = mc.player != null && mc.player.isPotionActive(AMEffectRegistry.LAVA_VISION);
            if (AMConfig.clingingFlipEffect) {
                if (mc.player != null && mc.player.isPotionActive(AMEffectRegistry.CLINGING) && mc.player.getEyeHeight() < mc.player.height * 0.45F) {
                    mc.entityRenderer.loadShader(FLIP_SHADER);
                    flipPostShaderActive = true;
                } else if (flipPostShaderActive && mc.entityRenderer.getShaderGroup() != null) {
                    mc.entityRenderer.stopUseShader();
                    flipPostShaderActive = false;
                }
            }
        }
        if (mc.getRenderViewEntity() instanceof EntityBaldEagle) {
            EntityBaldEagle eagle = (EntityBaldEagle) mc.getRenderViewEntity();
            EntityPlayerSP player = mc.player;
            if (player == null) {
                return;
            }
            if (eagle.shouldHoodedReturn() || eagle.isDead) {
                mc.setRenderViewEntity(player);
                mc.gameSettings.thirdPersonView = AlexsMobs.PROXY.getPreviousPOV();
            } else {
                float rotX = MathHelper.wrapDegrees(player.rotationYaw + player.rotationYawHead);
                float rotY = player.rotationPitch;
                Entity over = null;
                RayTraceResult mop = mc.objectMouseOver;
                if (mop != null && mop.typeOfHit == RayTraceResult.Type.ENTITY) {
                    over = mop.entityHit;
                } else {
                    mc.objectMouseOver = null;
                }
                boolean loadChunks = player.world.getWorldTime() % 10L == 0L;
                eagle.directFromPlayer(rotX, rotY, false, over);
                AlexsMobs.NETWORK_WRAPPER.sendToServer(new MessageUpdateEagleControls(mc.getRenderViewEntity().getEntityId(), rotX, rotY, loadChunks, over == null ? -1 : over.getEntityId()));
            }
        }
    }

    private static void updateAllChunks() {
        FMLCommonHandler.instance().reloadRenderers();
    }

    /**
     * Entity layers and custom potion icon drawing can leave {@link GlStateManager#color} or {@link BookBlit} tint active,
     * which tints vanilla/Citadel GUIs (creative inventory, animal dictionary, etc.).
     */
    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void onDrawScreenPre(GuiScreenEvent.DrawScreenEvent.Pre event) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        BookBlit.setRGB(255, 255, 255, 255);
    }

    @SubscribeEvent
  public static void onTextureStitch(TextureStitchEvent.Pre event) {
        try {
            for (Field f : AMEffectRegistry.class.getDeclaredFields()) {
                Object obj = f.get(null);
                if (obj instanceof Potion && obj.getClass().getPackage().equals(AMEffectRegistry.class.getPackage())) {
                    Potion potion = (Potion) obj;
                    if (potion.getRegistryName() != null) {
                        event.getMap().registerSprite(new ResourceLocation(AlexsMobs.MODID, "mob_effect/" + potion.getRegistryName().getResourcePath()));
                    }
                }
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
