package com.github.alexthe666.alexsmobs.block;

import com.github.alexthe666.alexsmobs.AlexsMobs;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.util.BlockRenderLayer;

/**
 * 1.12 replacement for 1.20 bison fur block (full wool-like block).
 */
public class BlockBisonFurBlock extends Block {

    public BlockBisonFurBlock() {
        super(Material.CLOTH);
        this.setSoundType(SoundType.CLOTH);
        this.setHardness(0.6F);
        this.setResistance(1.0F);
        this.setCreativeTab(AlexsMobs.TAB);
        this.setRegistryName("alexsmobs:bison_fur_block");
    }

    @Override
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.CUTOUT_MIPPED;
    }
}
