package com.github.alexthe666.alexsmobs.client.render;

import com.github.alexthe666.alexsmobs.client.model.ModelGeladaMonkey;
import com.github.alexthe666.alexsmobs.entity.EntityGeladaMonkey;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderGeladaMonkey extends RenderLiving<EntityGeladaMonkey> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("alexsmobs:textures/entity/gelada_monkey.png");
    private static final ResourceLocation TEXTURE_ANGRY = new ResourceLocation("alexsmobs:textures/entity/gelada_monkey_angry.png");
    private static final ResourceLocation TEXTURE_LEADER = new ResourceLocation("alexsmobs:textures/entity/gelada_monkey_leader.png");
    private static final ResourceLocation TEXTURE_LEADER_ANGRY = new ResourceLocation("alexsmobs:textures/entity/gelada_monkey_leader_angry.png");

    public RenderGeladaMonkey(RenderManager renderManagerIn) {
        super(renderManagerIn, new ModelGeladaMonkey(), 0.45F);
    }

    @Override
    protected void preRenderCallback(EntityGeladaMonkey entitylivingbaseIn, float partialTickTime) {
        float s = entitylivingbaseIn.getGeladaScale();
        GlStateManager.scale(s, s, s);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityGeladaMonkey entity) {
        if (entity.isLeader()) {
            return entity.isAggro() ? TEXTURE_LEADER_ANGRY : TEXTURE_LEADER;
        }
        return entity.isAggro() ? TEXTURE_ANGRY : TEXTURE;
    }
}
