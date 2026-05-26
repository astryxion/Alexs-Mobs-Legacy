package com.github.alexthe666.alexsmobs.item;

import com.github.alexthe666.alexsmobs.AlexsMobs;
import com.github.alexthe666.citadel.server.item.CustomArmorMaterial;
import com.google.common.collect.Multimap;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class ItemModArmor extends ItemArmor {

    private static final UUID[] ARMOR_MODIFIERS = new UUID[]{
            UUID.fromString("845DB27C-C624-495F-8C9F-6020A9A58B6B"),
            UUID.fromString("D8499B04-0E66-4726-AB29-64469D734E0D"),
            UUID.fromString("9F3D476D-C118-4544-8365-64846904B48E"),
            UUID.fromString("2AD3F246-FEE1-4E67-B886-69FD380BB150")
    };
    private Multimap<String, AttributeModifier> attributeMapCroc;
    private Multimap<String, AttributeModifier> attributeMapMoose;
    private final CustomArmorMaterial customMaterial;

    public ItemModArmor(CustomArmorMaterial armorMaterial, EntityEquipmentSlot slot) {
        super(ItemArmor.ArmorMaterial.LEATHER, slot.getIndex(), slot);
        this.customMaterial = armorMaterial;
        this.setCreativeTab(AlexsMobs.TAB);
    }

    public CustomArmorMaterial getCustomMaterial() {
        return customMaterial;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, net.minecraft.client.util.ITooltipFlag flagIn) {
        if (this.customMaterial == AMItemRegistry.CENTIPEDE_ARMOR_MATERIAL) {
            tooltip.add(TextFormatting.GRAY + I18n.translateToLocal("item.alexsmobs.centipede_leggings.desc"));
        }
        if (this.customMaterial == AMItemRegistry.EMU_ARMOR_MATERIAL) {
            tooltip.add(TextFormatting.GRAY + I18n.translateToLocal("item.alexsmobs.emu_leggings.desc"));
        }
        super.addInformation(stack, worldIn, tooltip, flagIn);
        if (this.customMaterial == AMItemRegistry.ROADRUNNER_ARMOR_MATERIAL) {
            tooltip.add(TextFormatting.BLUE + I18n.translateToLocal("item.alexsmobs.roadrunner_boots.desc"));
        }
        if (this.customMaterial == AMItemRegistry.RACCOON_ARMOR_MATERIAL) {
            tooltip.add(TextFormatting.BLUE + I18n.translateToLocal("item.alexsmobs.frontier_cap.desc"));
        }
    }

    private void buildCrocAttributes(CustomArmorMaterial materialIn) {
        com.google.common.collect.ImmutableMultimap.Builder<String, AttributeModifier> builder = com.google.common.collect.ImmutableMultimap.builder();
        UUID uuid = ARMOR_MODIFIERS[this.armorType.getIndex()];
        builder.put(SharedMonsterAttributes.ARMOR.getName(), new AttributeModifier(uuid, "Armor modifier", materialIn.getDamageReductionAmount(this.armorType), 0));
        builder.put(SharedMonsterAttributes.ARMOR_TOUGHNESS.getName(), new AttributeModifier(uuid, "Armor toughness", materialIn.getToughness(), 0));
        builder.put(EntityLivingBase.SWIM_SPEED.getName(), new AttributeModifier(uuid, "Swim speed", 1.0D, 0));
        if (materialIn.getKnockbackResistance() > 0) {
            builder.put(SharedMonsterAttributes.KNOCKBACK_RESISTANCE.getName(), new AttributeModifier(uuid, "Armor knockback resistance", materialIn.getKnockbackResistance(), 0));
        }
        attributeMapCroc = builder.build();
    }

    private void buildMooseAttributes(CustomArmorMaterial materialIn) {
        com.google.common.collect.ImmutableMultimap.Builder<String, AttributeModifier> builder = com.google.common.collect.ImmutableMultimap.builder();
        UUID uuid = ARMOR_MODIFIERS[this.armorType.getIndex()];
        builder.put(SharedMonsterAttributes.ARMOR.getName(), new AttributeModifier(uuid, "Armor modifier", materialIn.getDamageReductionAmount(this.armorType), 0));
        builder.put(SharedMonsterAttributes.ARMOR_TOUGHNESS.getName(), new AttributeModifier(uuid, "Armor toughness", materialIn.getToughness(), 0));
        if (materialIn.getKnockbackResistance() > 0) {
            builder.put(SharedMonsterAttributes.KNOCKBACK_RESISTANCE.getName(), new AttributeModifier(uuid, "Armor knockback resistance", materialIn.getKnockbackResistance(), 0));
        }
        attributeMapMoose = builder.build();
    }

    @Override
    public Multimap<String, AttributeModifier> getItemAttributeModifiers(EntityEquipmentSlot equipmentSlot) {
        if (equipmentSlot != this.armorType) {
            return super.getItemAttributeModifiers(equipmentSlot);
        }
        if (this.customMaterial == AMItemRegistry.CROCODILE_ARMOR_MATERIAL) {
            if (attributeMapCroc == null) {
                buildCrocAttributes(AMItemRegistry.CROCODILE_ARMOR_MATERIAL);
            }
            return attributeMapCroc;
        }
        if (this.customMaterial == AMItemRegistry.MOOSE_ARMOR_MATERIAL) {
            if (attributeMapMoose == null) {
                buildMooseAttributes(AMItemRegistry.MOOSE_ARMOR_MATERIAL);
            }
            return attributeMapMoose;
        }
        com.google.common.collect.ImmutableMultimap.Builder<String, AttributeModifier> builder = com.google.common.collect.ImmutableMultimap.builder();
        UUID uuid = ARMOR_MODIFIERS[this.armorType.getIndex()];
        builder.put(SharedMonsterAttributes.ARMOR.getName(), new AttributeModifier(uuid, "Armor modifier", this.customMaterial.getDamageReductionAmount(this.armorType), 0));
        builder.put(SharedMonsterAttributes.ARMOR_TOUGHNESS.getName(), new AttributeModifier(uuid, "Armor toughness", this.customMaterial.getToughness(), 0));
        if (this.customMaterial.getKnockbackResistance() > 0) {
            builder.put(SharedMonsterAttributes.KNOCKBACK_RESISTANCE.getName(), new AttributeModifier(uuid, "Armor knockback resistance", this.customMaterial.getKnockbackResistance(), 0));
        }
        return builder.build();
    }

    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EntityEquipmentSlot slot, String type) {
        if (this.customMaterial == AMItemRegistry.CROCODILE_ARMOR_MATERIAL) {
            return "alexsmobs:textures/armor/crocodile_chestplate.png";
        } else if (this.customMaterial == AMItemRegistry.ROADRUNNER_ARMOR_MATERIAL) {
            return "alexsmobs:textures/armor/roadrunner_boots.png";
        } else if (this.customMaterial == AMItemRegistry.CENTIPEDE_ARMOR_MATERIAL) {
            return "alexsmobs:textures/armor/centipede_leggings.png";
        } else if (this.customMaterial == AMItemRegistry.MOOSE_ARMOR_MATERIAL) {
            return "alexsmobs:textures/armor/moose_headgear.png";
        } else if (this.customMaterial == AMItemRegistry.RACCOON_ARMOR_MATERIAL) {
            return "alexsmobs:textures/armor/frontier_cap.png";
        } else if (this.customMaterial == AMItemRegistry.SOMBRERO_ARMOR_MATERIAL) {
            return "alexsmobs:textures/armor/sombrero.png";
        } else if (this.customMaterial == AMItemRegistry.SPIKED_TURTLE_SHELL_ARMOR_MATERIAL) {
            return "alexsmobs:textures/armor/spiked_turtle_shell.png";
        } else if (this.customMaterial == AMItemRegistry.FEDORA_ARMOR_MATERIAL) {
            return "alexsmobs:textures/armor/fedora.png";
        } else if (this.customMaterial == AMItemRegistry.EMU_ARMOR_MATERIAL) {
            return "alexsmobs:textures/armor/emu_leggings.png";
        }
        return null;
    }

    @SideOnly(Side.CLIENT)
    @Nullable
    public ModelBiped getArmorModel(EntityLivingBase entityLiving, ItemStack itemStack, EntityEquipmentSlot armorSlot, ModelBiped _default) {
        if (this.customMaterial == AMItemRegistry.ROADRUNNER_ARMOR_MATERIAL) {
            return (ModelBiped) AlexsMobs.PROXY.getArmorModel(0, entityLiving);
        } else if (this.customMaterial == AMItemRegistry.MOOSE_ARMOR_MATERIAL) {
            return (ModelBiped) AlexsMobs.PROXY.getArmorModel(1, entityLiving);
        } else if (this.customMaterial == AMItemRegistry.RACCOON_ARMOR_MATERIAL) {
            return (ModelBiped) AlexsMobs.PROXY.getArmorModel(2, entityLiving);
        } else if (this.customMaterial == AMItemRegistry.SOMBRERO_ARMOR_MATERIAL) {
            return (ModelBiped) AlexsMobs.PROXY.getArmorModel(3, entityLiving);
        } else if (this.customMaterial == AMItemRegistry.SPIKED_TURTLE_SHELL_ARMOR_MATERIAL) {
            return (ModelBiped) AlexsMobs.PROXY.getArmorModel(4, entityLiving);
        } else if (this.customMaterial == AMItemRegistry.FEDORA_ARMOR_MATERIAL) {
            return (ModelBiped) AlexsMobs.PROXY.getArmorModel(5, entityLiving);
        }
        return null;
    }
}
