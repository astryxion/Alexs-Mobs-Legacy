package com.github.alexthe666.alexsmobs.client.render;

import com.github.alexthe666.alexsmobs.client.model.ModelKangaroo;
import com.github.alexthe666.alexsmobs.client.render.layer.LayerKangarooArmor;
import com.github.alexthe666.alexsmobs.client.render.layer.LayerKangarooBaby;
import com.github.alexthe666.alexsmobs.client.render.layer.LayerKangarooItem;
import com.github.alexthe666.alexsmobs.entity.EntityKangaroo;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderKangaroo extends RenderLiving<EntityKangaroo> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("alexsmobs:textures/entity/kangaroo.png");

    public RenderKangaroo(RenderManager renderManagerIn) {
        super(renderManagerIn, new ModelKangaroo(), 0.5F);
        this.addLayer(new LayerKangarooItem(this));
        this.addLayer(new LayerKangarooArmor(this));
        this.addLayer(new LayerKangarooBaby(this));
    }

    @Override
    public boolean shouldRender(EntityKangaroo kangaroo, ICamera camera, double camX, double camY, double camZ) {
        if (kangaroo.isChild() && kangaroo.isRiding() && kangaroo.getRidingEntity() instanceof EntityKangaroo) {
            return false;
        }
        return super.shouldRender(kangaroo, camera, camX, camY, camZ);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityKangaroo entity) {
        return TEXTURE;
    }
}
