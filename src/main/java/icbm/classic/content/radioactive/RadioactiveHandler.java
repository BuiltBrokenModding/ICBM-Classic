package icbm.classic.content.radioactive;

import icbm.classic.config.blast.ConfigBlast;
import icbm.classic.config.util.BlockReplacementData;
import icbm.classic.config.util.BlockStateConfigList;
import icbm.classic.content.reg.BlockReg;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.tuple.Pair;

public class RadioactiveHandler {
    public static final BlockStateConfigList.BlockChanceOut radioactiveBlockSwaps = new BlockStateConfigList.BlockChanceOut("[RadioactiveReplacements]", (configList) -> {
        configList.setDefault(Blocks.STONE.getRegistryName(), new BlockReplacementData().setBlockState(BlockReg.RADIOACTIVE_STONE.get().getDefaultState()).setChance(0.2f), 0);

        configList.setDefault(net.minecraft.block.Blocks.DIRT.getRegistryName(), new BlockReplacementData().setBlockState(BlockReg.RADIOACTIVE_DIRT.get().getDefaultState()).setChance(0.2f), 0);
        configList.setDefault(net.minecraft.block.Blocks.FARMLAND.getRegistryName(), new BlockReplacementData().setBlockState(BlockReg.RADIOACTIVE_DIRT.get().getDefaultState()).setChance(0.2f), 0);
        configList.setDefault(Blocks.GRASS_PATH.getRegistryName(), new BlockReplacementData().setBlockState(BlockReg.RADIOACTIVE_DIRT.get().getDefaultState()).setChance(0.2f), 0);
        configList.setDefault(net.minecraft.block.Blocks.GRASS.getRegistryName(), new BlockReplacementData().setBlockState(BlockReg.RADIOACTIVE_DIRT.get().getDefaultState()).setChance(0.2f), 0);
        configList.setDefault(Blocks.MYCELIUM.getRegistryName(), new BlockReplacementData().setBlockState(BlockReg.RADIOACTIVE_DIRT.get().getDefaultState()).setChance(0.2f), 0);

        for(Block block: ForgeRegistries.BLOCKS) {
            final Material material = block.getMaterial(block.getDefaultState());
            if(material == Material.PLANTS || material == Material.LEAVES || material == Material.TALL_PLANTS) {
                configList.setDefault(block.getRegistryName(), new BlockReplacementData().setBlockState(Blocks.AIR.getDefaultState()), 0);
            }
        }

        configList.load("icbmclassic/blast/nuclear/radioactive_replacements/list", ConfigBlast.nuclear.radiationReplacements.blockStates);
    });

    public static void loadFromConfig() {
        radioactiveBlockSwaps.reload();
    }

    public static void setup() {
        loadFromConfig();
    }
}
