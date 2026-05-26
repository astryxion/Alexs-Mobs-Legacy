package com.github.alexthe666.alexsmobs.client.render.tile;

import com.github.alexthe666.alexsmobs.block.BlockVoidWormBeak;
import com.github.alexthe666.alexsmobs.client.model.ModelVoidWormBeak;
import com.github.alexthe666.alexsmobs.tileentity.TileEntityVoidWormBeak;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

/**
 * 1.12 {@link TileEntitySpecialRenderer} port of the 1.16 {@code TileEntityRenderer} / MatrixStack path.
 */
public class RenderVoidWormBeak extends TileEntitySpecialRenderer<TileEntityVoidWormBeak> {

    private static final ResourceLocation TEXTURE = new ResourceLocation("alexsmobs:textures/entity/void_worm_beak.png");
    private static final ModelVoidWormBeak HEAD_MODEL = new ModelVoidWormBeak();

    @Override
    public void render(TileEntityVoidWormBeak te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        if (te == null || te.getWorld() == null) {
            return;
        }
        EnumFacing dir = te.getWorld().getBlockState(te.getPos()).getValue(BlockVoidWormBeak.FACING);

        GlStateManager.pushMatrix();
        GlStateManager.translate((float) x, (float) y, (float) z);
        if (dir == EnumFacing.UP) {
            GlStateManager.translate(0.5F, 1.5F, 0.5F);
        } else if (dir == EnumFacing.DOWN) {
            GlStateManager.translate(0.5F, -0.5F, 0.5F);
        } else if (dir == EnumFacing.NORTH) {
            GlStateManager.translate(0.5F, 0.5F, -0.5F);
        } else if (dir == EnumFacing.EAST) {
            GlStateManager.translate(1.5F, 0.5F, 0.5F);
        } else if (dir == EnumFacing.SOUTH) {
            GlStateManager.translate(0.5F, 0.5F, 1.5F);
        } else if (dir == EnumFacing.WEST) {
            GlStateManager.translate(-0.5F, 0.5F, 0.5F);
        }
        applyDirectionRotation(dir.getOpposite());

        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0F, -0.01F, 0.0F);
        HEAD_MODEL.renderBeak(te, partialTicks);
        this.bindTexture(TEXTURE);
        GlStateManager.disableCull();
        HEAD_MODEL.render(null, 0.0F, 0.0F, te.ticksExisted + partialTicks, 0.0F, 0.0F, 0.0625F);
        GlStateManager.enableCull();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.popMatrix();
        GlStateManager.popMatrix();
    }

    /**
     * Equivalent to 1.16 {@code dir.getOpposite().getRotation()} (quaternion -> GlStateManager).
     * Enum order matches 1.16 {@code Direction}: DOWN=180X, UP=identity, NORTH=180Y, SOUTH=identity,
     * WEST=90Y, EAST=-90Y.
     */
    private static void applyDirectionRotation(EnumFacing dir) {
        switch (dir) {
            case DOWN:
                GlStateManager.rotate(180.0F, 1.0F, 0.0F, 0.0F);
                break;
            case UP:
                break;
            case NORTH:
                GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
                break;
            case SOUTH:
                break;
            case WEST:
                GlStateManager.rotate(90.0F, 0.0F, 1.0F, 0.0F);
                break;
            case EAST:
                GlStateManager.rotate(-90.0F, 0.0F, 1.0F, 0.0F);
                break;
        }
    }
}
