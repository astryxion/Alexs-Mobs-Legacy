package com.github.alexthe666.alexsmobs.client.render.layer;

import com.github.alexthe666.alexsmobs.ClientProxy;
import com.github.alexthe666.alexsmobs.client.model.ModelCachalotWhale;
import com.github.alexthe666.alexsmobs.client.render.RenderCachalotWhale;
import com.github.alexthe666.alexsmobs.entity.EntityCachalotWhale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.util.ReportedException;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class LayerCachalotWhaleCapturedSquid implements LayerRenderer<EntityCachalotWhale> {

    private final RenderCachalotWhale renderer;

    public LayerCachalotWhaleCapturedSquid(RenderCachalotWhale render) {
        this.renderer = render;
    }

    @Override
    public void doRenderLayer(EntityCachalotWhale whale, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        if (whale.hasCaughtSquid() && whale.isEntityAlive()) {
            Entity squid = whale.getCaughtSquid();
            if (squid != null && squid.isEntityAlive()) {
                boolean rightSquid = !whale.isHoldingSquidLeft();
                float riderRot = squid.prevRotationYaw + (squid.rotationYaw - squid.prevRotationYaw) * partialTicks;
                Render<?> entityRender = Minecraft.getMinecraft().getRenderManager().getEntityRenderObject(squid);
                ModelBase modelBase = null;
                if (entityRender instanceof RenderLiving) {
                    modelBase = ((RenderLiving) entityRender).getMainModel();
                }
                if (modelBase != null) {
                    ClientProxy.currentUnrenderedEntities.remove(squid.getUniqueID());
                    GlStateManager.pushMatrix();
                    translateToPouch(scale);
                    GlStateManager.translate(rightSquid ? -1.2F : 1.2F, 0.0F, -3.4F);
                    GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
                    GlStateManager.rotate(riderRot + (rightSquid ? -90.0F : 90.0F), 0.0F, 1.0F, 0.0F);
                    renderEntity(squid, 0.0D, 0.0D, 0.0D, 0.0F, partialTicks);
                    GlStateManager.popMatrix();
                    ClientProxy.currentUnrenderedEntities.add(squid.getUniqueID());
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
        ModelCachalotWhale model = (ModelCachalotWhale) this.renderer.getMainModel();
        model.body.postRender(scale);
        model.head.postRender(scale);
        model.jaw.postRender(scale);
    }

    @Override
    public boolean shouldCombineTextures() {
        return false;
    }
}
