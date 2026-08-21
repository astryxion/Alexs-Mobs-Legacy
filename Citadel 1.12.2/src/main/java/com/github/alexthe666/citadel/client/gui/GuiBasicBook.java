package com.github.alexthe666.citadel.client.gui;

import com.github.alexthe666.citadel.Citadel;
import com.github.alexthe666.citadel.client.gui.data.EntityLinkData;
import com.github.alexthe666.citadel.client.gui.data.EntityRenderData;
import com.github.alexthe666.citadel.client.gui.data.ImageData;
import com.github.alexthe666.citadel.client.gui.data.ItemRenderData;
import com.github.alexthe666.citadel.client.gui.data.LineData;
import com.github.alexthe666.citadel.client.gui.data.LinkData;
import com.github.alexthe666.citadel.client.gui.data.RecipeData;
import com.github.alexthe666.citadel.client.gui.data.TabulaRenderData;
import com.github.alexthe666.citadel.client.gui.data.Whitespace;
import com.github.alexthe666.citadel.client.model.TabulaModel;
import com.github.alexthe666.citadel.client.model.TabulaModelHandler;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import org.lwjgl.opengl.GL11;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EntityList;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.io.IOUtils;

@SideOnly(Side.CLIENT)
public abstract class GuiBasicBook extends GuiScreen {
   private static final ResourceLocation BOOK_PAGE_TEXTURE = new ResourceLocation("citadel:textures/gui/book/book_pages.png");
   private static final ResourceLocation BOOK_BINDING_TEXTURE = new ResourceLocation("citadel:textures/gui/book/book_binding.png");
   private static final ResourceLocation BOOK_WIDGET_TEXTURE = new ResourceLocation("citadel:textures/gui/book/widgets.png");
   protected ItemStack bookStack;
   protected int xSize = 390;
   protected int ySize = 320;
   protected int currentPageCounter = 0;
   protected int maxPagesFromPrinting = 0;
   protected int linesFromJSON = 0;
   protected int linesFromPrinting = 0;
   protected ResourceLocation prevPageJSON;
   protected ResourceLocation currentPageJSON;
   protected ResourceLocation currentPageText = null;
   private BookPageButton buttonNextPage;
   private BookPageButton buttonPreviousPage;
   private BookPage internalPage = null;
   private List<LineData> lines = new ArrayList();
   private List<LinkData> links = new ArrayList();
   private List<ItemRenderData> itemRenders = new ArrayList();
   private List<RecipeData> recipes = new ArrayList();
   private List<TabulaRenderData> tabulaRenders = new ArrayList();
   private List<EntityRenderData> entityRenders = new ArrayList();
   private List<EntityLinkData> entityLinks = new ArrayList();
   private List<ImageData> images = new ArrayList();
   private List<Whitespace> yIndexesToSkip = new ArrayList();
   private Map<String, TabulaModel> renderedTabulaModels = new HashMap();
   private Map<String, Entity> renderedEntites = new HashMap();
   private Map<String, ResourceLocation> textureMap = new HashMap();
   private Map<Integer, LinkData> linkButtonMap = new HashMap();
   private Map<Integer, EntityLinkData> entityLinkButtonMap = new HashMap();
   private static final Map<ResourceLocation, BookPage> PAGE_CACHE = new HashMap();
   private ResourceLocation loadedPageJson = null;
   private ResourceLocation laidOutTextFile = null;
   private String writtenTitle = "";
   private int preservedPageIndex = 0;
   private String entityTooltip;
   private final ITextComponent guiTitle;

   public GuiBasicBook(ItemStack bookStack, ITextComponent title) {
      super();
      this.guiTitle = title;
      this.bookStack = bookStack;
      this.currentPageJSON = this.getRootPage();
   }

   public static void drawEntityOnScreen(int posX, int posY, float scale, boolean follow, double xRot, double yRot, double zRot, float mouseX, float mouseY, Entity entity) {
      float f = (float)Math.atan((double)(mouseX / 40.0F));
      float f1 = (float)Math.atan((double)(mouseY / 40.0F));
      float prevYaw = entity.rotationYaw;
      float prevPitch = entity.rotationPitch;
      float prevOffset = 0.0F;
      float prevPrevOffset = 0.0F;
      float prevHead = 0.0F;
      float prevPrevHead = 0.0F;
      EntityLivingBase living = entity instanceof EntityLivingBase ? (EntityLivingBase) entity : null;
      if (living != null) {
         prevOffset = living.renderYawOffset;
         prevPrevOffset = living.prevRenderYawOffset;
         prevHead = living.rotationYawHead;
         prevPrevHead = living.prevRotationYawHead;
      }

      GlStateManager.enableColorMaterial();
      GlStateManager.pushMatrix();
      GlStateManager.translate((float)posX, (float)posY, 50.0F);
      GlStateManager.scale(-scale, scale, scale);
      GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
      if (follow) {
         float yaw = f * 20.0F;
         entity.rotationYaw = yaw;
         entity.rotationPitch = -f1 * 20.0F;
         if (living != null) {
            living.renderYawOffset = yaw;
            living.prevRenderYawOffset = yaw;
            living.rotationYawHead = yaw;
            living.prevRotationYawHead = yaw;
         }
      }

      GlStateManager.rotate(135.0F, 0.0F, 1.0F, 0.0F);
      RenderHelper.enableStandardItemLighting();
      GlStateManager.rotate(-135.0F, 0.0F, 1.0F, 0.0F);
      GlStateManager.rotate((float)xRot, 1.0F, 0.0F, 0.0F);
      GlStateManager.rotate((float)yRot, 0.0F, 1.0F, 0.0F);
      GlStateManager.rotate((float)zRot, 0.0F, 0.0F, 1.0F);
      double prevX = entity.posX;
      double prevY = entity.posY;
      double prevZ = entity.posZ;
      double prevPrevX = entity.prevPosX;
      double prevPrevY = entity.prevPosY;
      double prevPrevZ = entity.prevPosZ;
      net.minecraft.entity.player.EntityPlayer player = Minecraft.getMinecraft().player;
      if (player != null) {
         entity.posX = entity.prevPosX = player.posX;
         entity.posY = entity.prevPosY = player.posY;
         entity.posZ = entity.prevPosZ = player.posZ;
      }

      net.minecraft.client.renderer.entity.RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();
      float prevViewY = renderManager.playerViewY;
      renderManager.playerViewY = 180.0F;
      renderManager.setRenderShadow(false);
      try {
         GuiShaderCompat.beginGuiEntity();
         renderManager.renderEntity(entity, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, false);
      } finally {
         renderManager.setRenderShadow(true);
         renderManager.playerViewY = prevViewY;
         entity.posX = prevX;
         entity.posY = prevY;
         entity.posZ = prevZ;
         entity.prevPosX = prevPrevX;
         entity.prevPosY = prevPrevY;
         entity.prevPosZ = prevPrevZ;
         entity.rotationYaw = prevYaw;
         entity.rotationPitch = prevPitch;
         if (living != null) {
            living.renderYawOffset = prevOffset;
            living.prevRenderYawOffset = prevPrevOffset;
            living.rotationYawHead = prevHead;
            living.prevRotationYawHead = prevPrevHead;
         }
         GuiShaderCompat.endGuiEntity();
         GlStateManager.popMatrix();
         restoreGuiLighting();
      }
   }

   /** Undo inventory-entity lighting so the rest of the GUI / world is not left tinted. */
   public static void restoreGuiLighting() {
      RenderHelper.disableStandardItemLighting();
      GlStateManager.disableRescaleNormal();
      GlStateManager.disableColorMaterial();
      GL11.glDisable(GL11.GL_SCISSOR_TEST);
      GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
      GlStateManager.enableTexture2D();
      GlStateManager.disableTexture2D();
      GL11.glDisable(GL11.GL_TEXTURE_2D);
      GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
      GlStateManager.enableTexture2D();
      GL11.glEnable(GL11.GL_TEXTURE_2D);
      GlStateManager.disableLighting();
      GlStateManager.disableFog();
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      GlStateManager.enableBlend();
      GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
      BookBlit.setRGB(255, 255, 255, 255);
   }

   public static void drawTabulaModelOnScreen(TabulaModel model, ResourceLocation tex, int posX, int posY, float scale, boolean follow, double xRot, double yRot, double zRot, float mouseX, float mouseY) {
      float f = (float)Math.atan((double)(mouseX / 40.0F));
      float f1 = (float)Math.atan((double)(mouseY / 40.0F));
      Minecraft.getMinecraft().getTextureManager().bindTexture(tex);
      GlStateManager.pushMatrix();
      GlStateManager.translate((float)posX, (float)posY, 50.0F);
      GlStateManager.scale(scale, -scale, scale);
      if (follow) {
         GlStateManager.rotate(180.0F + f * 40.0F, 0.0F, 1.0F, 0.0F);
         GlStateManager.rotate(f1 * 20.0F, 1.0F, 0.0F, 0.0F);
      }

      GlStateManager.rotate((float)xRot, 1.0F, 0.0F, 0.0F);
      GlStateManager.rotate((float)yRot, 0.0F, 1.0F, 0.0F);
      GlStateManager.rotate((float)zRot, 0.0F, 0.0F, 1.0F);
      model.resetToDefaultPose();
      model.render((Entity)null, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F);
      GlStateManager.popMatrix();
   }

   public void initGui() {
      super.initGui();
      this.playBookOpeningSound();
      this.loadBookEntry();
      if (this.buttonNextPage == null) {
         this.addNextPreviousButtons();
      }
   }

   private void addNextPreviousButtons() {
      int k = (this.width - this.xSize) / 2;
      int l = (this.height - this.ySize + 128) / 2;
      this.buttonPreviousPage = new BookPageButton(this, k + 10, l + 180, false, true);
      this.buttonNextPage = new BookPageButton(this, k + 365, l + 180, true, true);
      this.buttonList.add(this.buttonPreviousPage);
      this.buttonList.add(this.buttonNextPage);
   }

   private void addLinkButtons() {
      this.buttonList.clear();
      this.linkButtonMap.clear();
      this.entityLinkButtonMap.clear();
      this.addNextPreviousButtons();
      int k = (this.width - this.xSize) / 2;
      int l = (this.height - this.ySize + 128) / 2;

      for(LinkData linkData : this.links) {
         if (linkData.getPage() == this.currentPageCounter) {
            int maxLength = Math.max(100, Minecraft.getMinecraft().fontRenderer.getStringWidth(linkData.getTitleText()) + 20);
            this.yIndexesToSkip.add(new Whitespace(linkData.getPage(), linkData.getX() - maxLength / 2, linkData.getY(), 100, 20));
            int id = this.buttonList.size() + 10;
            this.buttonList.add(new GuiButton(id, k + linkData.getX() - maxLength / 2, l + linkData.getY(), maxLength, 20, linkData.getTitleText()));
            this.linkButtonMap.put(id, linkData);
         }

         if (linkData.getPage() > this.maxPagesFromPrinting) {
            this.maxPagesFromPrinting = linkData.getPage();
         }
      }

      for(EntityLinkData linkData : this.entityLinks) {
         if (linkData.getPage() == this.currentPageCounter) {
            this.yIndexesToSkip.add(new Whitespace(linkData.getPage(), linkData.getX() - 12, linkData.getY(), 100, 20));
            int id = this.buttonList.size() + 10;
            this.buttonList.add(new EntityLinkButton(this, linkData, k, l));
            this.buttonList.get(this.buttonList.size() - 1).id = id;
            this.entityLinkButtonMap.put(id, linkData);
         }

         if (linkData.getPage() > this.maxPagesFromPrinting) {
            this.maxPagesFromPrinting = linkData.getPage();
         }
      }

   }

   private void onSwitchPage(boolean next) {
      if (next) {
         if (this.currentPageCounter < this.maxPagesFromPrinting) {
            ++this.currentPageCounter;
         }
      } else if (this.currentPageCounter > 0) {
         --this.currentPageCounter;
      } else if (this.internalPage != null && !this.internalPage.getParent().isEmpty()) {
         this.prevPageJSON = this.currentPageJSON;
         this.currentPageJSON = new ResourceLocation(this.getTextFileDirectory() + this.internalPage.getParent());
         this.currentPageCounter = this.preservedPageIndex;
         this.preservedPageIndex = 0;
         this.loadBookEntry();
         return;
      }

      this.updatePageNavigation();
   }

   @Override
   protected void actionPerformed(GuiButton button) throws IOException {
      if (button == this.buttonNextPage) {
         this.onSwitchPage(true);
         return;
      }

      if (button == this.buttonPreviousPage) {
         this.onSwitchPage(false);
         return;
      }

      LinkData linkData = (LinkData)this.linkButtonMap.get(button.id);
      if (linkData != null) {
         this.prevPageJSON = this.currentPageJSON;
         this.currentPageJSON = new ResourceLocation(this.getTextFileDirectory() + linkData.getLinkedPage());
         this.preservedPageIndex = this.currentPageCounter;
         this.currentPageCounter = 0;
         this.loadBookEntry();
         return;
      }

      EntityLinkData entityLinkData = (EntityLinkData)this.entityLinkButtonMap.get(button.id);
      if (entityLinkData != null) {
         this.prevPageJSON = this.currentPageJSON;
         this.currentPageJSON = new ResourceLocation(this.getTextFileDirectory() + entityLinkData.getLinkedPage());
         this.preservedPageIndex = this.currentPageCounter;
         this.currentPageCounter = 0;
         this.loadBookEntry();
      }
   }

   public void drawScreen(int x, int y, float partialTicks) {
      int color = this.getBindingColor();
      int r = (color & 16711680) >> 16;
      int g = (color & '\uff00') >> 8;
      int b = color & 255;
      this.drawDefaultBackground();
      int k = (this.width - this.xSize) / 2;
      int l = (this.height - this.ySize + 128) / 2;
      BookBlit.setRGB(255, 255, 255, 255);
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      this.mc.getTextureManager().bindTexture(this.getBookPageTexture());
      BookBlit.func_238463_a_(k, l, 0.0F, 0.0F, this.xSize, this.ySize, this.xSize, this.ySize);
      this.mc.getTextureManager().bindTexture(this.getBookBindingTexture());
      BookBlit.setRGB(r, g, b, 255);
      BookBlit.func_238463_a_(k, l, 0.0F, 0.0F, this.xSize, this.ySize, this.xSize, this.ySize);
      BookBlit.setRGB(255, 255, 255, 255);
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      if (this.internalPage == null || this.loadedPageJson == null || !this.currentPageJSON.equals(this.loadedPageJson)) {
         this.loadBookEntry();
      }

      if (this.internalPage != null) {
         this.renderOtherWidgets(x, y, this.internalPage);
         this.writePageText(x, y);
      }

      this.prevPageJSON = this.currentPageJSON;
      restoreGuiLighting();
      EntityLinkButton.beginFrame();
      super.drawScreen(x, y, partialTicks);
      restoreGuiLighting();
      if (this.entityTooltip != null) {
         this.drawHoveringText(this.fontRenderer.listFormattedStringToWidth((new TextComponentTranslation(this.entityTooltip)).getFormattedText(), Math.max(this.width / 2 - 43, 170)), x, y);
         this.entityTooltip = null;
      }

   }

   @Override
   public void onGuiClosed() {
      restoreGuiLighting();
      super.onGuiClosed();
   }

   private void loadBookEntry() {
      if (this.currentPageJSON == null) {
         return;
      }

      if (this.internalPage == null || this.loadedPageJson == null || !this.currentPageJSON.equals(this.loadedPageJson)) {
         this.internalPage = this.generatePage(this.currentPageJSON);
         this.loadedPageJson = this.currentPageJSON;
         this.laidOutTextFile = null;
      }

      if (this.internalPage == null) {
         return;
      }

      ResourceLocation textFile = this.resolvePageTextFile(this.internalPage);
      this.readInPageWidgets(this.internalPage);
      this.addWidgetSpacing();
      if (!textFile.equals(this.laidOutTextFile)) {
         this.currentPageText = textFile;
         this.readInPageText(textFile);
         this.laidOutTextFile = textFile;
      }

      this.updatePageNavigation();
   }

   private void updatePageNavigation() {
      if (this.internalPage != null) {
         this.addLinkButtons();
      }
   }

   private ResourceLocation resolvePageTextFile(BookPage page) {
      String lang = Minecraft.getMinecraft().getLanguageManager().getCurrentLanguage().getLanguageCode().toLowerCase();
      ResourceLocation localized = new ResourceLocation(this.getTextFileDirectory() + lang + "/" + page.getTextFileToReadFrom());

      try {
         Minecraft.getMinecraft().getResourceManager().getResource(localized);
         return localized;
      } catch (Exception var4) {
         Citadel.LOGGER.warn("Could not find language file for translation, defaulting to english");
         return new ResourceLocation(this.getTextFileDirectory() + "en_us/" + page.getTextFileToReadFrom());
      }
   }

   private Item getItemByRegistryName(String registryName) {
      return (Item)ForgeRegistries.ITEMS.getValue(new ResourceLocation(registryName));
   }

   private IRecipe getRecipeByName(String registryName) {
      try {
         return (IRecipe)ForgeRegistries.RECIPES.getValue(new ResourceLocation(registryName));
      } catch (Exception e) {
         e.printStackTrace();
      }

      return null;
   }

   private void addWidgetSpacing() {
      this.yIndexesToSkip.clear();

      for(ItemRenderData itemRenderData : this.itemRenders) {
         Item item = this.getItemByRegistryName(itemRenderData.getItem());
         if (item != null) {
            this.yIndexesToSkip.add(new Whitespace(itemRenderData.getPage(), itemRenderData.getX(), itemRenderData.getY(), (int)(itemRenderData.getScale() * (double)17.0F), (int)(itemRenderData.getScale() * (double)15.0F)));
         }
      }

      for(RecipeData recipeData : this.recipes) {
         IRecipe recipe = this.getRecipeByName(recipeData.getRecipe());
         if (recipe != null) {
            this.yIndexesToSkip.add(new Whitespace(recipeData.getPage(), recipeData.getX(), recipeData.getY() - (int)(recipeData.getScale() * (double)15.0F), (int)(recipeData.getScale() * (double)35.0F), (int)(recipeData.getScale() * (double)60.0F), true));
         }
      }

      for(ImageData imageData : this.images) {
         if (imageData != null) {
            this.yIndexesToSkip.add(new Whitespace(imageData.getPage(), imageData.getX(), imageData.getY(), (int)(imageData.getScale() * (double)imageData.getWidth()), (int)(imageData.getScale() * (double)imageData.getHeight() * (double)0.8F)));
         }
      }

      if (!this.writtenTitle.isEmpty()) {
         this.yIndexesToSkip.add(new Whitespace(0, 20, 5, 70, 15));
      }

   }

   private void renderOtherWidgets(int x, int y, BookPage page) {
      int color = this.getBindingColor();
      int r = (color & 16711680) >> 16;
      int g = (color & '\uff00') >> 8;
      int b = color & 255;
      int k = (this.width - this.xSize) / 2;
      int l = (this.height - this.ySize + 128) / 2;

      for(ItemRenderData itemRenderData : this.itemRenders) {
         if (itemRenderData.getPage() == this.currentPageCounter) {
            Item item = this.getItemByRegistryName(itemRenderData.getItem());
            if (item != null) {
               GlStateManager.pushMatrix();
               GlStateManager.translate((float)k, (float)l, 32.0F);
               float scale = (float)itemRenderData.getScale();
               GlStateManager.scale(scale, scale, scale);
               ItemStack stack = new ItemStack(item);
               if (itemRenderData.getItemTag() != null && !itemRenderData.getItemTag().isEmpty()) {
                  NBTTagCompound tag = null;

                  try {
                     tag = JsonToNBT.getTagFromJson(itemRenderData.getItemTag());
                  } catch (NBTException e) {
                     e.printStackTrace();
                  }

                  stack.setTagCompound(tag);
               }

               this.itemRender.renderItemAndEffectIntoGUI(stack, itemRenderData.getX(), itemRenderData.getY());
               GlStateManager.popMatrix();
            }
         }
      }

      for(RecipeData recipeData : this.recipes) {
         if (recipeData.getPage() == this.currentPageCounter) {
            IRecipe recipe = this.getRecipeByName(recipeData.getRecipe());
            int playerTicks = Minecraft.getMinecraft().player.ticksExisted;
            if (recipe != null) {
               float scale = (float)recipeData.getScale();
               this.mc.getTextureManager().bindTexture(this.getBookWidgetTexture());
               GlStateManager.pushMatrix();
               GlStateManager.translate((float)(k + recipeData.getX()), (float)(l + recipeData.getY()), 0.0F);
               GlStateManager.scale(scale, scale, scale);
               this.drawTexturedModalRect(0, 0, 0, 88, 116, 53);
               GlStateManager.popMatrix();

               for(int i = 0; i < recipe.getIngredients().size(); ++i) {
                  Ingredient ing = (Ingredient)recipe.getIngredients().get(i);
                  ItemStack stack = ItemStack.EMPTY;
                  if (ing.getMatchingStacks().length > 0) {
                     if (ing.getMatchingStacks().length > 1) {
                        int currentIndex = (int)((float)playerTicks / 20.0F % (float)ing.getMatchingStacks().length);
                        stack = ing.getMatchingStacks()[currentIndex];
                     } else {
                        stack = ing.getMatchingStacks()[0];
                     }
                  }

                  if (!stack.isEmpty()) {
                     GlStateManager.pushMatrix();
                     GlStateManager.translate((float)k, (float)l, 32.0F);
                     GlStateManager.translate((float)((int)((float)recipeData.getX() + (float)(i % 3 * 20) * scale)), (float)((int)((float)recipeData.getY() + (float)(i / 3 * 20) * scale)), 0.0F);
                     GlStateManager.scale(scale, scale, scale);
                     this.itemRender.renderItemAndEffectIntoGUI(stack, 0, 0);
                     GlStateManager.popMatrix();
                  }
               }

               GlStateManager.pushMatrix();
               GlStateManager.translate((float)k, (float)l, 32.0F);
               float finScale = scale * 1.5F;
               GlStateManager.translate((float)recipeData.getX() + 70.0F * finScale, (float)recipeData.getY() + 10.0F * finScale, 0.0F);
               GlStateManager.scale(finScale, finScale, finScale);
               this.itemRender.renderItemAndEffectIntoGUI(recipe.getRecipeOutput(), 0, 0);
               GlStateManager.popMatrix();
            }
         }
      }

      for(TabulaRenderData tabulaRenderData : this.tabulaRenders) {
         if (tabulaRenderData.getPage() == this.currentPageCounter) {
            TabulaModel model = null;
            ResourceLocation texture;
            if (this.textureMap.get(tabulaRenderData.getTexture()) != null) {
               texture = (ResourceLocation)this.textureMap.get(tabulaRenderData.getTexture());
            } else {
               texture = (ResourceLocation)this.textureMap.put(tabulaRenderData.getTexture(), new ResourceLocation(tabulaRenderData.getTexture()));
            }

            if (this.renderedTabulaModels.get(tabulaRenderData.getModel()) != null) {
               model = (TabulaModel)this.renderedTabulaModels.get(tabulaRenderData.getModel());
            } else {
               try {
                  model = new TabulaModel(TabulaModelHandler.INSTANCE.loadTabulaModel("/assets/" + tabulaRenderData.getModel().split(":")[0] + "/" + tabulaRenderData.getModel().split(":")[1]));
               } catch (Exception var20) {
                  Citadel.LOGGER.warn("Could not load in tabula model for book at " + tabulaRenderData.getModel());
               }

               this.renderedTabulaModels.put(tabulaRenderData.getModel(), model);
            }

            if (model != null && texture != null) {
               float scale = (float)tabulaRenderData.getScale();
               drawTabulaModelOnScreen(model, texture, k + tabulaRenderData.getX(), l + tabulaRenderData.getY(), 30.0F * scale, tabulaRenderData.isFollow_cursor(), tabulaRenderData.getRot_x(), tabulaRenderData.getRot_y() + this.getBookEntityYawOffset(), tabulaRenderData.getRot_z(), (float)(k + tabulaRenderData.getX() - x), (float)(l + tabulaRenderData.getY() - y));
            }
         }
      }

      for(EntityRenderData data : this.entityRenders) {
         if (data.getPage() == this.currentPageCounter) {
            Entity model = null;
            ResourceLocation entityId = new ResourceLocation(data.getEntity());
            if (EntityList.isRegistered(entityId)) {
               model = this.renderedEntites.get(data.getEntity());
               if (model == null) {
                  model = EntityList.createEntityByIDFromName(entityId, Minecraft.getMinecraft().world);
                  if (model != null) {
                     this.renderedEntites.put(data.getEntity(), model);
                  }
               }
            }

            if (model != null) {
               float scale = (float)data.getScale();
               model.ticksExisted = Minecraft.getMinecraft().player.ticksExisted;
               int mouseX = k + data.getX() - x;
               int mouseY = k + data.getY() / 2 - y;
               drawEntityOnScreen(k + data.getX(), l + data.getY(), 30.0F * scale, data.isFollow_cursor(), data.getRot_x(), data.getRot_y() + this.getBookEntityYawOffset(), data.getRot_z(), (float)mouseX, (float)mouseY, model);
            }
         }
      }

      for(ImageData imageData : this.images) {
         if (imageData.getPage() == this.currentPageCounter && imageData != null) {
            ResourceLocation tex = (ResourceLocation)this.textureMap.get(imageData.getTexture());
            if (tex == null) {
               tex = new ResourceLocation(imageData.getTexture());
               this.textureMap.put(imageData.getTexture(), tex);
            }

            float scale = (float)imageData.getScale();
            this.mc.getTextureManager().bindTexture(tex);
            GlStateManager.pushMatrix();
            GlStateManager.translate((float)(k + imageData.getX()), (float)(l + imageData.getY()), 0.0F);
            GlStateManager.scale(scale, scale, scale);
            this.drawTexturedModalRect(0, 0, imageData.getU(), imageData.getV(), imageData.getWidth(), imageData.getHeight());
            GlStateManager.popMatrix();
         }
      }

   }

   private void writePageText(int x, int y) {
      FontRenderer font = this.fontRenderer;
      int k = (this.width - this.xSize) / 2;
      int l = (this.height - this.ySize + 128) / 2;
      if (this.currentPageCounter == 0 && !this.writtenTitle.isEmpty()) {
         String actualTitle = I18n.format(this.writtenTitle, new Object[0]);
         GlStateManager.pushMatrix();
         float scale = 2.0F;
         if (font.getStringWidth(actualTitle) > 80) {
            scale = 2.0F - MathHelper.clamp((float)(font.getStringWidth(actualTitle) - 80) * 0.011F, 0.0F, 1.95F);
         }

         GlStateManager.translate((float)(k + 10), (float)(l + 10), 0.0F);
         GlStateManager.scale(scale, scale, 1.0F);
         font.drawString(actualTitle, 0, 0, this.getTitleColor());
         GlStateManager.popMatrix();
      }

      this.buttonNextPage.visible = this.currentPageCounter < this.maxPagesFromPrinting;
      boolean rootPage = this.currentPageJSON.equals(this.getRootPage());
      this.buttonPreviousPage.visible = this.currentPageCounter > 0 || !rootPage;

      for(LineData line : this.lines) {
         if (line.getPage() == this.currentPageCounter) {
            String text = line.getText();
            if (text != null) {
               String trimmed = text.trim();
               if (!trimmed.isEmpty() && !".".equals(trimmed)) {
                  font.drawString(text, k + 10 + line.getxIndex(), l + 10 + line.getyIndex() * 12, this.getTextColor());
               }
            }
         }
      }

   }

   public boolean doesGuiPauseGame() {
      return false;
   }

   protected void playBookOpeningSound() {
      Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
   }

   protected void playBookClosingSound() {
   }

   protected abstract int getBindingColor();

   public int getWidgetColor() {
      return this.getBindingColor();
   }

   protected int getTextColor() {
      return 3158064;
   }

   protected int getTitleColor() {
      return 12233880;
   }

   /** 1.12 entity renderer is mirrored vs 1.16 book JSON yaw. */
   protected double getBookEntityYawOffset() {
      return 0.0D;
   }

   public abstract ResourceLocation getRootPage();

   public abstract String getTextFileDirectory();

   protected ResourceLocation getBookPageTexture() {
      return BOOK_PAGE_TEXTURE;
   }

   protected ResourceLocation getBookBindingTexture() {
      return BOOK_BINDING_TEXTURE;
   }

   public ResourceLocation getBookWidgetTexture() {
      return BOOK_WIDGET_TEXTURE;
   }

   protected void playPageFlipSound() {
   }

   @Nullable
   protected BookPage generatePage(ResourceLocation res) {
      BookPage cached = PAGE_CACHE.get(res);
      if (cached != null) {
         return cached;
      }

      try {
         IResource resource = Minecraft.getMinecraft().getResourceManager().getResource(res);
         try (InputStream inputstream = resource.getInputStream();
              Reader reader = new BufferedReader(new InputStreamReader(inputstream, StandardCharsets.UTF_8))) {
            BookPage page = BookPage.deserialize(reader);
            if (page != null) {
               PAGE_CACHE.put(res, page);
            }
            return page;
         } catch (IOException e1) {
            e1.printStackTrace();
         }
      } catch (IOException var7) {
         return null;
      }

      return null;
   }

   protected void readInPageWidgets(BookPage page) {
      this.links.clear();
      this.itemRenders.clear();
      this.recipes.clear();
      this.tabulaRenders.clear();
      this.entityRenders.clear();
      this.images.clear();
      this.entityLinks.clear();
      this.links.addAll(page.getLinkedButtons());
      this.entityLinks.addAll(page.getLinkedEntities());
      this.itemRenders.addAll(page.getItemRenders());
      this.recipes.addAll(page.getRecipes());
      this.tabulaRenders.addAll(page.getTabulaRenders());
      this.entityRenders.addAll(page.getEntityRenders());
      this.images.addAll(page.getImages());
      this.writtenTitle = page.generateTitle();
   }

   protected void readInPageText(ResourceLocation res) {
      IResource resource = null;
      int xIndex = 0;
      int actualTextX = 0;
      int yIndex = 0;

      try {
         resource = Minecraft.getMinecraft().getResourceManager().getResource(res);

         try {
            List<String> readStrings = IOUtils.readLines(resource.getInputStream(), StandardCharsets.UTF_8);
            this.linesFromJSON = readStrings.size();
            this.lines.clear();
            List<String> splitBySpaces = new ArrayList();

            for(String line : readStrings) {
               splitBySpaces.addAll(Arrays.asList(line.split(" ")));
            }

            String lineToPrint = "";
            this.linesFromPrinting = 0;
            int page = 0;

            for(int i = 0; i < splitBySpaces.size(); ++i) {
               String word = (String)splitBySpaces.get(i);
               int cutoffPoint = xIndex > 100 ? 30 : 35;
               boolean newline = word.equals("<NEWLINE>");

               for(Whitespace indexes : this.yIndexesToSkip) {
                  int indexPage = indexes.getPage();
                  if (indexPage == page) {
                     int buttonX = indexes.getX();
                     int buttonY = indexes.getY();
                     int width = indexes.getWidth();
                     int height = indexes.getHeight();
                     if (indexes.isDown()) {
                        if ((float)yIndex >= (float)buttonY / 12.0F && (float)yIndex <= (float)(buttonY + height) / 12.0F && (buttonX < 90 && xIndex < 90 || buttonX >= 90 && xIndex >= 90)) {
                           yIndex += 2;
                        }
                     } else if ((float)yIndex >= (float)(buttonY - height) / 12.0F && (float)yIndex <= (float)(buttonY + height) / 12.0F && (buttonX < 90 && xIndex < 90 || buttonX >= 90 && xIndex >= 90)) {
                        ++yIndex;
                     }
                  }
               }

               boolean last = i == splitBySpaces.size() - 1;
               actualTextX += word.length() + 1;
               if (lineToPrint.length() + word.length() + 1 < cutoffPoint && !newline) {
                  lineToPrint = lineToPrint + " " + word;
                  if (last) {
                     ++this.linesFromPrinting;
                     this.lines.add(new LineData(xIndex, yIndex, lineToPrint, page));
                     ++yIndex;
                     actualTextX = 0;
                     if (newline) {
                        ++yIndex;
                     }
                  }
               } else {
                  ++this.linesFromPrinting;
                  if (yIndex > 13) {
                     if (xIndex > 0) {
                        ++page;
                        xIndex = 0;
                        yIndex = 0;
                     } else {
                        xIndex = 200;
                        yIndex = 0;
                     }
                  }

                  if (last) {
                     lineToPrint = lineToPrint + " " + word;
                  }

                  this.lines.add(new LineData(xIndex, yIndex, lineToPrint, page));
                  ++yIndex;
                  actualTextX = 0;
                  if (newline) {
                     ++yIndex;
                  }

                  lineToPrint = "" + (word.equals("<NEWLINE>") ? "" : word);
               }
            }

            this.maxPagesFromPrinting = page;
         } catch (Exception e1) {
            e1.printStackTrace();
         }
      } catch (IOException var22) {
         Citadel.LOGGER.warn("Could not load in page .txt from json from page, page: " + res.toString());
      }

   }

   public void setEntityTooltip(String hoverText) {
      this.entityTooltip = hoverText;
   }
}
