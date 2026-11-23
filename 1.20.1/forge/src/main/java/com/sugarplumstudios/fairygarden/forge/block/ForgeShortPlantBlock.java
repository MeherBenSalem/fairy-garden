package com.sugarplumstudios.fairygarden.forge.block;

import com.sugarplumstudios.fairygarden.block.ShortPlantBlock;
import net.minecraftforge.common.IForgeShearable;

/** Blush grass and moonlit grass on Forge: shears collect them through Forge's shearing hook. */
public class ForgeShortPlantBlock extends ShortPlantBlock implements IForgeShearable {
    public ForgeShortPlantBlock(Properties properties) {
        super(properties);
    }
}
