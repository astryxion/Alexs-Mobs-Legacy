package com.github.alexthe666.alexsmobs.client.render.layer;

import com.github.alexthe666.alexsmobs.ClientProxy;
import com.github.alexthe666.alexsmobs.client.model.ModelKangaroo;
import com.github.alexthe666.alexsmobs.client.render.RenderKangaroo;
import com.github.alexthe666.alexsmobs.entity.EntityKangaroo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.util.ReportedException;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class LayerKangarooBaby implements LayerRenderer<EntityKangaroo> {

    private final RenderKangaroo renderer;

    public LayerKangarooBaby(RenderKangaroo render) {
        this.renderer = render;
    }

    @Override
    public void doRenderLayer(EntityKangaroo roo, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        if (roo.isBeingRidden() && !roo.isChild()) {
            for (Entity passenger : roo.getPassengers()) {
                float riderRot = passenger.prevRotationYaw + (passenger.rotationYaw - passenger.prevRotationYaw) * partialTicks;
                Render<?> entityRender = Minecraft.getMinecraft().getRenderManager().getEntityRenderObject(passenger);
                ModelBase modelBase = null;
                if (entityRender instanceof RenderLiving) {
                    modelBase = ((RenderLiving) entityRender).getMainModel();
                }
                if (modelBase != null) {
                    ClientProxy.currentUnrenderedEntities.remove(passenger.getUniqueID());
                    GlStateManager.pushMatrix();
                    translateToPouch(scale);
                    GlStateManager.translate(0.0F, 1.12F, -0.3F);
                    ModelKangaroo.renderOnlyHead = true;
                    GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
                    GlStateManager.rotate(riderRot + 180.0F, 0.0F, 1.0F, 0.0F);
                    renderEntity(passenger, 0.0D, 0.0D, 0.0D, 0.0F, partialTicks);
                    ModelKangaroo.renderOnlyHead = false;
                    GlStateManager.popMatrix();
                    ClientProxy.currentUnrenderedEntities.add(passenger.getUniqueID());
                }
            }
        }
    }

    public <E extends Entity> void renderEntity(E entityIn, double x, double y, double z, float yaw, float partialTicks) {
        Render<? super E> render = null;
        RenderManager manager = Minecraft.getMinecraft().getRenderManager();
        try {
            render = manager.getEntityRenderObject(entityIn);
            if (render != null) {
                try {
                    render.doRender(entityIn, x, y, z, yaw, partialTicks);
                } catch (Throwable throwable1) {
                    throw new ReportedException(CrashReport.makeCrashReport(throwable1, "Rendering entity in world"));
                }
            }
        } catch (Throwable throwable3) {
            final Render<? super E> assignedRenderer = render;
            CrashReport crashreport = CrashReport.makeCrashReport(throwable3, "Rendering entity in world");
            CrashReportCategory crashreportcategory = crashreport.makeCategory("Entity being rendered");
            entityIn.addEntityCrashInfo(crashreportcategory);
            CrashReportCategory crashreportcategory1 = crashreport.makeCategory("Renderer details");
            crashreportcategory1.addDetail("Assigned renderer", () -> assignedRenderer == null ? "null" : assignedRenderer.toString());
            crashreportcategory1.addDetail("Location", () -> CrashReportCategory.getCoordinateInfo(x, y, z));
            crashreportcategory1.addDetail("Rotation", () -> String.valueOf(yaw));
            crashreportcategory1.addDetail("Delta", () -> String.valueOf(partialTicks));
            throw new ReportedException(crashreport);
        }
    }

    protected void translateToPouch(float scale) {
        ModelKangaroo model = (ModelKangaroo) this.renderer.getMainModel();
        model.root.postRender(scale);
        model.body.postRender(scale);
    }

    @Override
    public boolean shouldCombineTextures() {
        return false;
    }
}
