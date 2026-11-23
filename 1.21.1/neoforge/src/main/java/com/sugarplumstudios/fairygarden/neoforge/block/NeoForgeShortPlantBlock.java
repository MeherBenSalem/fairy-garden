package com.sugarplumstudios.fairygarden.neoforge.block;

import com.sugarplumstudios.fairygarden.block.ShortPlantBlock;
import net.neoforged.neoforge.common.IShearable;

/** Blush grass and moonlit grass on NeoForge: shears collect them through NeoForge's shearing hook. */
public class NeoForgeShortPlantBlock extends ShortPlantBlock implements IShearable {
    public NeoForgeShortPlantBlock(Properties properties) {
        super(properties);
    }
}
