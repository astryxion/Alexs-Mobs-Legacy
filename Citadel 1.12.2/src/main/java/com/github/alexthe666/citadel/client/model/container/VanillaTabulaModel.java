package com.github.alexthe666.citadel.client.model.container;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.Collection;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class VanillaTabulaModel implements IModel {
   private TabulaModelContainer model;
   private ResourceLocation particle;
   private Collection<ResourceLocation> textures;
   private ImmutableMap<ItemCameraTransforms.TransformType, TRSRTransformation> transforms;

   public VanillaTabulaModel(TabulaModelContainer model, ResourceLocation particle, ImmutableList<ResourceLocation> textures, ImmutableMap<ItemCameraTransforms.TransformType, TRSRTransformation> transforms) {
      this.model = model;
      this.particle = particle;
      this.textures = textures;
      this.transforms = transforms;
   }

   public Collection<ResourceLocation> getDependencies() {
      return ImmutableList.of();
   }

   public Collection<ResourceLocation> getTextures() {
      return this.textures;
   }

   @Nullable
   public IBakedModel bake(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> function) {
      return new BakedTabulaModel(ImmutableList.of(), function.apply(this.particle), this.transforms);
   }

   public IModelState getDefaultState() {
      return TRSRTransformation.identity();
   }
}
