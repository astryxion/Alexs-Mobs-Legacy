package com.github.alexthe666.alexsmobs.client.render;

import com.github.alexthe666.alexsmobs.client.model.ModelMysteriousWorm;
import com.github.alexthe666.alexsmobs.client.model.ModelShieldOfTheDeep;
import com.github.alexthe666.alexsmobs.entity.*;
import com.github.alexthe666.alexsmobs.item.AMItemRegistry;
import com.github.alexthe666.alexsmobs.item.ItemTabIcon;
import com.github.alexthe666.citadel.client.gui.GuiBasicBook;
import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SideOnly(Side.CLIENT)
public class AMItemstackRenderer extends TileEntityItemStackRenderer {

    private static final class MobIconEntry {
        final EntityEntry entry;
        final float scale;

        MobIconEntry(EntityEntry entry, float scale) {
            this.entry = entry;
            this.scale = scale;
        }
    }

    private static final List<MobIconEntry> MOB_ICONS = Lists.newArrayList(
            new MobIconEntry(AMEntityRegistry.GRIZZLY_BEAR, 0.6F),
            new MobIconEntry(AMEntityRegistry.ROADRUNNER, 0.8F),
            new MobIconEntry(AMEntityRegistry.BONE_SERPENT, 0.55F),
            new MobIconEntry(AMEntityRegistry.GAZELLE, 0.6F),
            new MobIconEntry(AMEntityRegistry.CROCODILE, 0.3F),
            new MobIconEntry(AMEntityRegistry.FLY, 1.3F),
            new MobIconEntry(AMEntityRegistry.HUMMINGBIRD, 1.5F),
            new MobIconEntry(AMEntityRegistry.ORCA, 0.2F),
            new MobIconEntry(AMEntityRegistry.SUNBIRD, 0.3F),
            new MobIconEntry(AMEntityRegistry.GORILLA, 0.85F),
            new MobIconEntry(AMEntityRegistry.CRIMSON_MOSQUITO, 0.6F),
            new MobIconEntry(AMEntityRegistry.RATTLESNAKE, 0.6F),
            new MobIconEntry(AMEntityRegistry.ENDERGRADE, 0.8F),
            new MobIconEntry(AMEntityRegistry.HAMMERHEAD_SHARK, 0.5F),
            new MobIconEntry(AMEntityRegistry.LOBSTER, 0.85F),
            new MobIconEntry(AMEntityRegistry.KOMODO_DRAGON, 0.5F),
            new MobIconEntry(AMEntityRegistry.CAPUCHIN_MONKEY, 0.85F),
            new MobIconEntry(AMEntityRegistry.CENTIPEDE_HEAD, 0.65F),
            new MobIconEntry(AMEntityRegistry.WARPED_TOAD, 0.6F),
            new MobIconEntry(AMEntityRegistry.MOOSE, 0.5F),
            new MobIconEntry(AMEntityRegistry.MIMICUBE, 0.95F),
            new MobIconEntry(AMEntityRegistry.RACCOON, 0.8F),
            new MobIconEntry(AMEntityRegistry.BLOBFISH, 1F),
            new MobIconEntry(AMEntityRegistry.SEAL, 0.7F),
            new MobIconEntry(AMEntityRegistry.COCKROACH, 1F),
            new MobIconEntry(AMEntityRegistry.SHOEBILL, 0.8F),
            new MobIconEntry(AMEntityRegistry.ELEPHANT, 0.45F),
            new MobIconEntry(AMEntityRegistry.SOUL_VULTURE, 0.8F),
            new MobIconEntry(AMEntityRegistry.SNOW_LEOPARD, 0.7F),
            new MobIconEntry(AMEntityRegistry.SPECTRE, 0.3F),
            new MobIconEntry(AMEntityRegistry.CROW, 1.3F),
            new MobIconEntry(AMEntityRegistry.BLUE_JAY, 1.3F),
            new MobIconEntry(AMEntityRegistry.ALLIGATOR_SNAPPING_TURTLE, 0.65F),
            new MobIconEntry(AMEntityRegistry.MUNGUS, 0.7F),
            new MobIconEntry(AMEntityRegistry.MANTIS_SHRIMP, 0.7F),
            new MobIconEntry(AMEntityRegistry.GUSTER, 0.55F),
            new MobIconEntry(AMEntityRegistry.WARPED_MOSCO, 0.45F),
            new MobIconEntry(AMEntityRegistry.STRADDLER, 0.38F),
            new MobIconEntry(AMEntityRegistry.STRADPOLE, 0.9F),
            new MobIconEntry(AMEntityRegistry.EMU, 0.7F),
            new MobIconEntry(AMEntityRegistry.PLATYPUS, 1F),
            new MobIconEntry(AMEntityRegistry.DROPBEAR, 0.65F),
            new MobIconEntry(AMEntityRegistry.TASMANIAN_DEVIL, 1.2F),
            new MobIconEntry(AMEntityRegistry.KANGAROO, 0.7F),
            new MobIconEntry(AMEntityRegistry.CACHALOT_WHALE, 0.1F),
            new MobIconEntry(AMEntityRegistry.LEAFCUTTER_ANT, 1.2F),
            new MobIconEntry(AMEntityRegistry.ENDERIOPHAGE, 0.65F),
            new MobIconEntry(AMEntityRegistry.BALD_EAGLE, 0.85F),
            new MobIconEntry(AMEntityRegistry.TIGER, 0.65F),
            new MobIconEntry(AMEntityRegistry.TARANTULA_HAWK, 0.7F),
            new MobIconEntry(AMEntityRegistry.VOID_WORM, 0.3F),
            new MobIconEntry(AMEntityRegistry.FRILLED_SHARK, 0.65F),
            new MobIconEntry(AMEntityRegistry.MIMIC_OCTOPUS, 0.7F),
            new MobIconEntry(AMEntityRegistry.SEAGULL, 1.2F),
            new MobIconEntry(AMEntityRegistry.ANTEATER, 1.0F),
            new MobIconEntry(AMEntityRegistry.ANACONDA, 1.2F),
            new MobIconEntry(AMEntityRegistry.LAVIATHAN, 0.8F),
            new MobIconEntry(AMEntityRegistry.GIANT_SQUID, 0.9F)
    );

    public static int ticksExisted = 0;
    private static final ModelShieldOfTheDeep SHIELD_OF_THE_DEEP_MODEL = new ModelShieldOfTheDeep();
    private static final ResourceLocation SHIELD_OF_THE_DEEP_TEXTURE = new ResourceLocation("alexsmobs:textures/armor/shield_of_the_deep.png");
    private static final ModelMysteriousWorm MYTERIOUS_WORM_MODEL = new ModelMysteriousWorm();
    private static final ResourceLocation MYTERIOUS_WORM_TEXTURE = new ResourceLocation("alexsmobs:textures/item/mysterious_worm_model.png");
    private final Map<String, Entity> renderedEntites = new HashMap<>();

    public static void incrementTick() {
        ticksExisted++;
    }

    private static float getScaleFor(EntityEntry entry) {
        for (MobIconEntry pair : MOB_ICONS) {
            if (pair.entry == entry) {
                return pair.scale;
            }
        }
        return 1.0F;
    }

    private static Entity createEntity(EntityEntry entry) {
        World world = Minecraft.getMinecraft().world;
        if (world == null || entry == null) {
            return null;
        }
        return entry.newInstance(world);
    }

    private static EntityEntry getEntryForClass(Class<? extends Entity> clazz) {
        if (clazz == null) {
            return null;
        }
        for (EntityEntry entry : ForgeRegistries.ENTITIES.getValues()) {
            if (entry != null && entry.getEntityClass() == clazz) {
                return entry;
            }
        }
        return null;
    }

    private static String cacheKey(EntityEntry entry) {
        return entry.getRegistryName() != null ? entry.getRegistryName().toString() : entry.getName();
    }

    @Override
    public void renderByItem(ItemStack itemStackIn, float partialTicks) {
        if (itemStackIn.getItem() == AMItemRegistry.SHIELD_OF_THE_DEEP) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(0.4F, -0.75F, 0.5F);
            GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
            Minecraft.getMinecraft().getTextureManager().bindTexture(SHIELD_OF_THE_DEEP_TEXTURE);
            SHIELD_OF_THE_DEEP_MODEL.render(null, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F);
            GlStateManager.popMatrix();
        }
        if (itemStackIn.getItem() == AMItemRegistry.MYSTERIOUS_WORM) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(0.0F, -2F, 0.0F);
            GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
            MYTERIOUS_WORM_MODEL.animateStack(itemStackIn);
            Minecraft.getMinecraft().getTextureManager().bindTexture(MYTERIOUS_WORM_TEXTURE);
            MYTERIOUS_WORM_MODEL.render(null, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F);
            GlStateManager.popMatrix();
        }
        if (itemStackIn.getItem() == AMItemRegistry.TAB_ICON) {
            Entity fakeEntity = null;
            int entityIndex = (Minecraft.getMinecraft().player != null ? Minecraft.getMinecraft().player.ticksExisted : ticksExisted) / 40 % MOB_ICONS.size();
            float scale = 1.0F;
            int flags = 0;
            EntityEntry displayEntry = null;
            if (ItemTabIcon.hasCustomEntityDisplay(itemStackIn)) {
                NBTTagCompound tag = itemStackIn.getTagCompound();
                flags = tag.getInteger("DisplayMobFlags");
                Class<? extends Entity> clazz = ItemTabIcon.getEntityType(tag);
                displayEntry = getEntryForClass(clazz);
                scale = getScaleFor(displayEntry);
                if (tag.hasKey("DisplayMobScale")) {
                    float customScale = tag.getFloat("DisplayMobScale");
                    if (customScale > 0) {
                        scale = customScale;
                    }
                }
                String index = ItemTabIcon.getCustomDisplayEntityString(itemStackIn);
                if (this.renderedEntites.get(index) == null && displayEntry != null) {
                    Entity entity = createEntity(displayEntry);
                    if (entity instanceof EntityBlobfish) {
                        ((EntityBlobfish) entity).setDepressurized(true);
                    }
                    fakeEntity = this.renderedEntites.put(index, entity);
                } else {
                    fakeEntity = this.renderedEntites.get(index);
                }
            } else {
                MobIconEntry mobIcon = MOB_ICONS.get(entityIndex);
                displayEntry = mobIcon.entry;
                scale = mobIcon.scale;
                if (displayEntry != null) {
                    String key = cacheKey(displayEntry);
                    if (this.renderedEntites.get(key) == null) {
                        Entity entity = createEntity(displayEntry);
                        if (entity instanceof EntityBlobfish) {
                            ((EntityBlobfish) entity).setDepressurized(true);
                        }
                        fakeEntity = this.renderedEntites.put(key, entity);
                    } else {
                        fakeEntity = this.renderedEntites.get(key);
                    }
                }
            }
            if (fakeEntity instanceof EntityCockroach) {
                if (flags == 99) {
                    ((EntityCockroach) fakeEntity).setMaracas(true);
                } else {
                    ((EntityCockroach) fakeEntity).setMaracas(false);
                }
            }
            if (fakeEntity instanceof EntityElephant) {
                if (flags == 99) {
                    ((EntityElephant) fakeEntity).setTusked(true);
                    ((EntityElephant) fakeEntity).setColor(null);
                } else if (flags == 98) {
                    ((EntityElephant) fakeEntity).setTusked(false);
                    ((EntityElephant) fakeEntity).setColor(EnumDyeColor.BROWN);
                } else {
                    ((EntityElephant) fakeEntity).setTusked(false);
                    ((EntityElephant) fakeEntity).setColor(null);
                }
            }
            if (fakeEntity instanceof EntityBaldEagle) {
                if (flags == 98) {
                    ((EntityBaldEagle) fakeEntity).setCap(true);
                } else {
                    ((EntityBaldEagle) fakeEntity).setCap(false);
                }
            }
            if (fakeEntity instanceof EntityLaviathan) {
                RenderLaviathan.renderWithoutShaking = true;
            }
            if (fakeEntity != null) {
                GlStateManager.pushMatrix();
                if (fakeEntity instanceof EntityVoidWorm || fakeEntity instanceof EntityMimicOctopus) {
                    GlStateManager.translate(0.0F, 0.5F, 0.0F);
                }
                if (fakeEntity instanceof EntityLaviathan) {
                    GlStateManager.translate(0.0F, 0.3F, 0.0F);
                }
                GlStateManager.translate(0.5F, 0.0F, 0.0F);
                GlStateManager.rotate(180.0F, 1.0F, 0.0F, 0.0F);
                GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
                float mouseX = 0.0F;
                float mouseY = 0.0F;
                GuiBasicBook.drawEntityOnScreen(0, 0, scale, true, 0.0, -45.0, 0.0, mouseX, mouseY, fakeEntity);
                GlStateManager.popMatrix();
            }
            if (fakeEntity instanceof EntityLaviathan) {
                RenderLaviathan.renderWithoutShaking = false;
            }
        }
    }
}
