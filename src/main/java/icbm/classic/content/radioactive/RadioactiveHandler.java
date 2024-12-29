package icbm.classic.content.radioactive;

import icbm.classic.config.blast.ConfigBlast;
import icbm.classic.config.util.BlockStateConfigList;
import icbm.classic.content.reg.BlockReg;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.Blocks;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.tuple.Pair;

public class RadioactiveHandler {
    public static final BlockStateConfigList.BlockChanceOut radioactiveBlockSwaps = new BlockStateConfigList.BlockChanceOut("[RadioactiveReplacements]", (configList) -> {
        configList.setDefault(Blocks.STONE.getRegistryName(), Pair.of(BlockReg.RADIOACTIVE_STONE.get().getDefaultState(), 0.2f), 0);

        configList.setDefault(net.minecraft.block.Blocks.DIRT.getRegistryName(), Pair.of(BlockReg.RADIOACTIVE_DIRT.get().getDefaultState(), 0.2f), 0);
        configList.setDefault(net.minecraft.block.Blocks.FARMLAND.getRegistryName(), Pair.of(BlockReg.RADIOACTIVE_DIRT.get().getDefaultState(), 0.2f), 0);
        configList.setDefault(Blocks.GRASS_PATH.getRegistryName(), Pair.of(BlockReg.RADIOACTIVE_DIRT.get().getDefaultState(), 0.2f), 0);
        configList.setDefault(net.minecraft.block.Blocks.GRASS.getRegistryName(), Pair.of(BlockReg.RADIOACTIVE_DIRT.get().getDefaultState(), 0.2f), 0);
        configList.setDefault(Blocks.MYCELIUM.getRegistryName(), Pair.of(BlockReg.RADIOACTIVE_DIRT.get().getDefaultState(), 0.2f), 0);

        for(Block block: ForgeRegistries.BLOCKS) {
            final Material material = block.getMaterial(block.getDefaultState());
            if(material == Material.PLANTS || material == Material.LEAVES || material == Material.TALL_PLANTS) {
                configList.setDefault(block.getRegistryName(), Pair.of(net.minecraft.block.Blocks.AIR.getDefaultState(), null), 0);
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
