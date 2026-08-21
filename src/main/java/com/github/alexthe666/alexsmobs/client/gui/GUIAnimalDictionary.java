package com.github.alexthe666.alexsmobs.client.gui;

import com.github.alexthe666.alexsmobs.client.render.RenderLaviathan;
import com.github.alexthe666.citadel.client.gui.GuiBasicBook;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GUIAnimalDictionary extends GuiBasicBook {

    private static final ResourceLocation ROOT = new ResourceLocation("alexsmobs:book/animal_dictionary/root.json");

    public GUIAnimalDictionary(ItemStack bookStack) {
        super(bookStack, new TextComponentTranslation("animal_dictionary.title"));
    }

    public GUIAnimalDictionary(ItemStack bookStack, String page) {
        super(bookStack, new TextComponentTranslation("animal_dictionary.title"));
        this.currentPageJSON = new ResourceLocation(this.getTextFileDirectory() + page + ".json");
    }

    @Override
    protected double getBookEntityYawOffset() {
        return 180.0D;
    }

    @Override
    protected int getBindingColor() {
        return 0X606B26;
    }

    /**
     * Book JSON uses 1.16-style keys ({@code entity.modid.id}); 1.12.2 lang uses {@code entity.modid.id.name}.
     */
    @Override
    protected void readInPageWidgets(com.github.alexthe666.citadel.client.gui.BookPage page) {
        if (page.translatableTitle != null && page.translatableTitle.startsWith("entity.") && !page.translatableTitle.endsWith(".name")) {
            page.translatableTitle = page.translatableTitle + ".name";
        }
        super.readInPageWidgets(page);
    }

    @Override
    public void setEntityTooltip(String hoverText) {
        if (hoverText != null && hoverText.startsWith("entity.") && !hoverText.endsWith(".name")) {
            hoverText = hoverText + ".name";
        }
        super.setEntityTooltip(hoverText);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        RenderLaviathan.renderWithoutShaking = true;
        super.drawScreen(mouseX, mouseY, partialTicks);
        RenderLaviathan.renderWithoutShaking = false;
    }

    @Override
    public ResourceLocation getRootPage() {
        return ROOT;
    }

    @Override
    public String getTextFileDirectory() {
        return "alexsmobs:book/animal_dictionary/";
    }
}
