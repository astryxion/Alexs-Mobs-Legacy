package com.github.alexthe666.citadel.client.model.container;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.model.TRSRTransformation;

public class BakedTabulaModel implements IBakedModel {
   private ImmutableList<BakedQuad> quads;
   private TextureAtlasSprite particle;
   private ImmutableMap<ItemCameraTransforms.TransformType, TRSRTransformation> transforms;

   public BakedTabulaModel(ImmutableList<BakedQuad> quads, TextureAtlasSprite particle, ImmutableMap<ItemCameraTransforms.TransformType, TRSRTransformation> transforms) {
      this.quads = quads;
      this.particle = particle;
      this.transforms = transforms;
   }

   public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
      return this.quads;
   }

   public boolean isAmbientOcclusion() {
      return true;
   }

   public boolean isGui3d() {
      return false;
   }

   public boolean isBuiltInRenderer() {
      return false;
   }

   public TextureAtlasSprite getParticleTexture() {
      return this.particle;
   }

   public ItemCameraTransforms getItemCameraTransforms() {
      return ItemCameraTransforms.DEFAULT;
   }

   public ItemOverrideList getOverrides() {
      return ItemOverrideList.NONE;
   }
}
