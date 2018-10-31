package icbm.classic.mods.nuclearcraft;

import icbm.classic.config.ConfigNuclearCraft;
import icbm.classic.mods.ModProxy;
import nc.capability.radiation.IRadiationSource;
import nc.init.NCBlocks;
import net.minecraft.block.Block;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Optional;

public class NCProxy extends ModProxy {

    public static final NCProxy INSTANCE = new NCProxy();

    @Override
    @Optional.Method(modid = "nuclearcraft")
    public void init() {

    }

    @Optional.Method(modid = "nuclearcraft")
    public Block getRadiationBlock() {
        return NCBlocks.dry_earth;
    }

    @Optional.Method(modid = "nuclearcraft")
    public void addRadiationToChunk(Chunk chunk) {
        if (chunk != null || chunk.hasCapability(IRadiationSource.CAPABILITY_RADIATION_SOURCE, null))
        {
            IRadiationSource chunkRadation = chunk.getCapability(IRadiationSource.CAPABILITY_RADIATION_SOURCE, null);
            if (chunkRadation != null)
            {
                if (chunkRadation.getRadiationBuffer() < ConfigNuclearCraft.RADS) {
                    chunkRadation.setRadiationBuffer(ConfigNuclearCraft.RADS);
                } else {
                    chunkRadation.setRadiationBuffer(chunkRadation.getRadiationLevel() + ConfigNuclearCraft.RADS);
                }
            }
        }
    }

    public boolean isNCActive() {
        if (ConfigNuclearCraft.DISABLED) {
            return false;
        } else {
            return true;
        }
    }

    public boolean isNCInstalled() {
        return Loader.isModLoaded("nuclearcraft");
    }
}
