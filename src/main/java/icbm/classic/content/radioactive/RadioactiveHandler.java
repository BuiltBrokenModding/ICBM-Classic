package icbm.classic.content.radioactive;

import icbm.classic.config.blast.ConfigBlast;
import icbm.classic.config.util.BlockReplacementData;
import icbm.classic.config.util.BlockStateConfigList;
import icbm.classic.content.reg.BlockReg;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class RadioactiveHandler {
    public static final BlockStateConfigList.BlockChanceOut radioactiveBlockSwaps = new BlockStateConfigList.BlockChanceOut("[RadioactiveReplacements]", (configList) -> {
        configList.setDefault(Blocks.STONE.getRegistryName(), new BlockReplacementData().setChance(0.2f).setBlockState(BlockReg.blockRadioactive.getDefaultState().withProperty(BlockRadioactive.TYPE_PROP, BlockRadioactive.EnumType.STONE)), 0);

        final IBlockState radDirt = BlockReg.blockRadioactive.getDefaultState().withProperty(BlockRadioactive.TYPE_PROP, BlockRadioactive.EnumType.DIRT);
        configList.setDefault(Blocks.DIRT.getRegistryName(), new BlockReplacementData().setChance(0.2f).setBlockState(radDirt), 0);
        configList.setDefault(Blocks.FARMLAND.getRegistryName(), new BlockReplacementData().setChance(0.2f).setBlockState(radDirt), 0);
        configList.setDefault(Blocks.GRASS_PATH.getRegistryName(), new BlockReplacementData().setChance(0.2f).setBlockState(radDirt), 0);
        configList.setDefault(Blocks.GRASS.getRegistryName(), new BlockReplacementData().setChance(0.2f).setBlockState(radDirt), 0);
        configList.setDefault(Blocks.MYCELIUM.getRegistryName(), new BlockReplacementData().setChance(0.2f).setBlockState(radDirt), 0);

        for(Block block: ForgeRegistries.BLOCKS) {
            final Material material = block.getMaterial(block.getDefaultState());
            if(material == Material.PLANTS || material == Material.LEAVES || material == Material.VINE) {
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
