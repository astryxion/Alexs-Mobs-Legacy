package com.github.alexthe666.citadel;

import com.github.alexthe666.citadel.animation.IAnimatedEntity;
import com.github.alexthe666.citadel.client.CitadelItemstackRenderer;
import com.github.alexthe666.citadel.client.CitadelPatreonRenderer;
import com.github.alexthe666.citadel.client.event.EventGetOutlineColor;
import com.github.alexthe666.citadel.client.gui.GuiCitadelBook;
import com.github.alexthe666.citadel.client.gui.GuiCitadelPatreonConfig;
import com.github.alexthe666.citadel.client.model.TabulaModel;
import com.github.alexthe666.citadel.client.model.TabulaModelHandler;
import com.github.alexthe666.citadel.client.patreon.SpaceStationPatreonRenderer;
import com.github.alexthe666.citadel.server.entity.CitadelEntityData;
import com.github.alexthe666.citadel.server.entity.datatracker.EntityDataHandler;
import com.github.alexthe666.citadel.server.entity.datatracker.EntityProperties;
import com.github.alexthe666.citadel.server.entity.datatracker.IEntityData;
import java.io.IOException;
import java.util.concurrent.Callable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiCustomizeSkin;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@EventBusSubscriber(
   modid = "citadel",
   value = Side.CLIENT
)
public class ClientProxy extends ServerProxy {
   public static TabulaModel CITADEL_MODEL;
   private static final ResourceLocation CITADEL_TEXTURE = new ResourceLocation("citadel", "textures/patreon/citadel_model.png");
   private static final ResourceLocation CITADEL_TEXTURE_RED = new ResourceLocation("citadel", "textures/patreon/citadel_model_red.png");
   private static final ResourceLocation CITADEL_TEXTURE_GRAY = new ResourceLocation("citadel", "textures/patreon/citadel_model_gray.png");

   public void onPreInit() {
      try {
         CITADEL_MODEL = new TabulaModel(TabulaModelHandler.INSTANCE.loadTabulaModel("/assets/citadel/models/citadel_model"));
      } catch (IOException e) {
         e.printStackTrace();
      }

      CitadelPatreonRenderer.register("citadel", new SpaceStationPatreonRenderer(CITADEL_TEXTURE));
      CitadelPatreonRenderer.register("citadel_red", new SpaceStationPatreonRenderer(CITADEL_TEXTURE_RED));
      CitadelPatreonRenderer.register("citadel_gray", new SpaceStationPatreonRenderer(CITADEL_TEXTURE_GRAY));
   }

   @SubscribeEvent
   public void openCustomizeSkinScreen(GuiScreenEvent.InitGuiEvent event) {
      if (event.getGui() instanceof GuiCustomizeSkin && Minecraft.getMinecraft().player != null) {
         try {
            String username = Minecraft.getMinecraft().player.getName();
            if (Citadel.PATREONS.contains(username)) {
               event.getButtonList().add(new GuiButton(200194, event.getGui().width / 2 - 100, event.getGui().height / 6 + 150, 200, 20, TextFormatting.GREEN + (new TextComponentTranslation("citadel.gui.patreon_rewards_option")).getFormattedText()));
            }
         } catch (Exception e) {
            e.printStackTrace();
         }
      }

   }

   @SubscribeEvent
   public void playerRender(RenderPlayerEvent.Post event) {
      String username = event.getEntityPlayer().getName();
      if (event.getEntityPlayer().isWearing(EnumPlayerModelParts.CAPE)) {
         if (Citadel.PATREONS.contains(username)) {
            NBTTagCompound tag = CitadelEntityData.getOrCreateCitadelTag(Minecraft.getMinecraft().player);
            String rendererName = tag.hasKey("CitadelFollowerType") ? tag.getString("CitadelFollowerType") : "citadel";
            if (!rendererName.equals("none")) {
               CitadelPatreonRenderer renderer = CitadelPatreonRenderer.get(rendererName);
               if (renderer != null) {
                  float distance = tag.hasKey("CitadelRotateDistance") ? tag.getFloat("CitadelRotateDistance") : 2.0F;
                  float speed = tag.hasKey("CitadelRotateSpeed") ? tag.getFloat("CitadelRotateSpeed") : 1.0F;
                  float height = tag.hasKey("CitadelRotateHeight") ? tag.getFloat("CitadelRotateHeight") : 1.0F;
                  renderer.render(event.getPartialRenderTick(), event.getEntityPlayer(), distance, speed, height);
               }
            }
         }

      }
   }

   public void handleAnimationPacket(int entityId, int index) {
      EntityPlayer player = Minecraft.getMinecraft().player;
      if (player != null) {
         IAnimatedEntity entity = (IAnimatedEntity)player.world.getEntityByID(entityId);
         if (entity != null) {
            if (index == -1) {
               entity.setAnimation(IAnimatedEntity.NO_ANIMATION);
            } else {
               entity.setAnimation(entity.getAnimations()[index]);
            }

            entity.setAnimationTick(0);
         }
      }

   }

   public void handlePropertiesPacket(String propertyID, NBTTagCompound compound, int entityID) {
      if (compound != null) {
         EntityPlayer player = Minecraft.getMinecraft().player;
         Entity entity = player.world.getEntityByID(entityID);
         if (propertyID.equals("CitadelPatreonConfig") && entity instanceof EntityLivingBase) {
            CitadelEntityData.setCitadelTag((EntityLivingBase)entity, compound);
         } else if (entity != null) {
            IEntityData<?> extendedProperties = EntityDataHandler.INSTANCE.getEntityData(entity, propertyID);
            if (extendedProperties instanceof EntityProperties) {
               EntityProperties<?> properties = (EntityProperties)extendedProperties;
               properties.loadTrackingSensitiveData(compound);
               properties.onSync();
            }
         }

      }
   }

   public Item setupISTER(Item item) {
      return item;
   }

   @SideOnly(Side.CLIENT)
   public static Callable<TileEntityItemStackRenderer> getTEISR() {
      return CitadelItemstackRenderer::new;
   }

   public void openBookGUI(ItemStack book) {
      Minecraft.getMinecraft().displayGuiScreen(new GuiCitadelBook(book));
   }

   @SubscribeEvent
   public void outlineColorTest(EventGetOutlineColor event) {
   }
}
