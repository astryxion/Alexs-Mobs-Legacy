package com.github.alexthe666.alexsmobs.client.gui;

import com.github.alexthe666.alexsmobs.client.render.RenderLaviathan;
import com.github.alexthe666.citadel.client.gui.BookBlit;
import com.github.alexthe666.citadel.client.gui.BookPage;
import com.github.alexthe666.citadel.client.gui.GuiBasicBook;
import com.github.alexthe666.citadel.client.gui.data.EntityRenderData;
import com.github.alexthe666.citadel.client.gui.data.TabulaRenderData;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.lang.reflect.Field;
import java.util.List;


@SideOnly(Side.CLIENT)
public class GUIAnimalDictionary extends GuiBasicBook {

    /** Citadel 1.12 {@link GuiBasicBook#drawEntityOnScreen} is mirrored vs 1.16+/1.20; add 180° Y on page renders. */
    private static final double DICTIONARY_Y_ROT_OFFSET = 180.0D;

    private static final Field ENTITY_RENDERS_FIELD;
    private static final Field TABULA_RENDERS_FIELD;

    static {
        try {
            ENTITY_RENDERS_FIELD = GuiBasicBook.class.getDeclaredField("entityRenders");
            ENTITY_RENDERS_FIELD.setAccessible(true);
            TABULA_RENDERS_FIELD = GuiBasicBook.class.getDeclaredField("tabulaRenders");
            TABULA_RENDERS_FIELD.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    public GUIAnimalDictionary(ItemStack bookStack) {
        super(bookStack, new TextComponentTranslation("animal_dictionary.title"));
    }

    public GUIAnimalDictionary(ItemStack bookStack, String page) {
        super(bookStack, new TextComponentTranslation("animal_dictionary.title"));
        this.currentPageJSON = new ResourceLocation(this.getTextFileDirectory() + page + ".json");
    }

    protected int getBindingColor() {
        return 0X606B26;
    }

    /**
     * Book JSON uses 1.16-style keys ({@code entity.modid.id}); 1.12.2 lang uses {@code entity.modid.id.name}.
     */
    @Override
    protected void readInPageWidgets(BookPage page) {
        if (page.translatableTitle != null && page.translatableTitle.startsWith("entity.") && !page.translatableTitle.endsWith(".name")) {
            page.translatableTitle = page.translatableTitle + ".name";
        }
        super.readInPageWidgets(page);
        applyDictionaryFacingOffset();
    }

    private void applyDictionaryFacingOffset() {
        try {
            offsetRotY((List<?>) ENTITY_RENDERS_FIELD.get(this));
            offsetRotY((List<?>) TABULA_RENDERS_FIELD.get(this));
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static void offsetRotY(List<?> renders) {
        for (Object render : renders) {
            if (render instanceof EntityRenderData) {
                EntityRenderData data = (EntityRenderData) render;
                data.setRot_y(data.getRot_y() + DICTIONARY_Y_ROT_OFFSET);
            } else if (render instanceof TabulaRenderData) {
                TabulaRenderData data = (TabulaRenderData) render;
                data.setRot_y(data.getRot_y() + DICTIONARY_Y_ROT_OFFSET);
            }
        }
    }

    @Override
    public void setEntityTooltip(String hoverText) {
        if (hoverText != null && hoverText.startsWith("entity.") && !hoverText.endsWith(".name")) {
            hoverText = hoverText + ".name";
        }
        super.setEntityTooltip(hoverText);
    }

  /**
   * Citadel {@link GuiBasicBook#drawScreen} draws the page texture before {@link BookBlit#setRGB}, so the page
   * inherits stale vertex tint from the binding color (olive green). Reset to white before/after the book draws.
   */
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        BookBlit.setRGB(255, 255, 255, 255);
        RenderLaviathan.renderWithoutShaking = true;
        super.drawScreen(mouseX, mouseY, partialTicks);
        RenderLaviathan.renderWithoutShaking = false;
        BookBlit.setRGB(255, 255, 255, 255);
    }

    public ResourceLocation getRootPage() {
        return new ResourceLocation("alexsmobs:book/animal_dictionary/root.json");
    }

    public String getTextFileDirectory() {
        return "alexsmobs:book/animal_dictionary/";
    }
}
