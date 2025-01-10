package icbm.datagen;

import icbm.classic.content.reg.BlockReg;
import net.minecraft.block.Block;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ExistingFileHelper;
import net.minecraftforge.client.model.generators.ModelFile;

public class BlockStateGenerator extends BlockStateProvider {
    public BlockStateGenerator(DataGenerator gen, String modid, ExistingFileHelper exFileHelper) {
        super(gen, modid, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        simpleModel(BlockReg.RADIOACTIVE_DIRT.get());
        simpleModel(BlockReg.RADIOACTIVE_STONE.get());
    }

    private void simpleModel(Block block) {
        simpleBlock(block, new ModelFile.UncheckedModelFile("block/" + block.getRegistryName().getPath()));
    }
}
