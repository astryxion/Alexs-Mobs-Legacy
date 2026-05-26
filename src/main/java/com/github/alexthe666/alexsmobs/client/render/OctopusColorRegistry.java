package com.github.alexthe666.alexsmobs.client.render;



import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import com.github.alexthe666.alexsmobs.AlexsMobs;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

@SideOnly(Side.CLIENT)
public class OctopusColorRegistry {

    public static final IBlockState FALLBACK_BLOCK = Blocks.SAND.getDefaultState();
    public static Map<String, Integer> TEXTURES_TO_COLOR = new HashMap<>();

    public static int getBlockColor(IBlockState stack) {
        String blockName = stack.toString();
        if (TEXTURES_TO_COLOR.get(blockName) != null) {
            return TEXTURES_TO_COLOR.get(blockName).intValue();
        } else {
            int colorizer = -1;
            try{
                colorizer = Minecraft.getMinecraft().getBlockColors().getColor(stack, null, null);
            }catch (Exception e){
                AlexsMobs.LOGGER.warn("Another mod did not use block colorizers correctly.");
            }
            int color = 0XFFFFFF;
            if(colorizer == -1){
                BufferedImage texture = null;
                try {
                    Color texColour = getAverageColour(getTextureAtlas(stack));
                    color = texColour.getRGB();
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }else{
                color = colorizer;
            }
            TEXTURES_TO_COLOR.put(blockName, color);
            return color;
        }
    }

    private static Color getAverageColour(TextureAtlasSprite image) {
        float red = 0;
        float green = 0;
        float blue = 0;
        float count = 0;
        int uMax = image.getIconWidth();
        int vMax = image.getIconHeight();
        int[][] frame = image.getFrameTextureData(0);
        if (frame == null || frame.length == 0 || frame[0] == null) {
            return new Color(0XFFFFFF);
        }
        int[] pixels = frame[0];
        for (int i = 0; i < uMax; i++) {
            for (int j = 0; j < vMax; j++) {
                int rgba = pixels[j * uMax + i];
                int alpha = rgba >> 24 & 0xFF;
                if (alpha == 0) {
                    continue;
                }
                red += rgba >> 16 & 0xFF;
                green += rgba >> 8 & 0xFF;
                blue += rgba & 0xFF;
                count++;
            }
        }
        if (count <= 0) {
            return new Color(0XFFFFFF);
        }
        return new Color((int) (red / count), (int) (green / count), (int) (blue / count));
    }

    private static TextureAtlasSprite getTextureAtlas(IBlockState state) {
        return Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getTexture(state);
    }
}
