package icbm.datagen;

import icbm.classic.content.reg.BlockReg;
import net.minecraft.block.Block;
import net.minecraft.data.DataGenerator;
import net.minecraft.util.ResourceLocation;
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

        simpleModel(BlockReg.CONCRETE_NORMAL.get());
        simpleModel(BlockReg.CONCRETE_COMPACT.get());
        simpleModel(BlockReg.CONCRETE_REINFORCED.get());
    }

    private void simpleModel(Block block) {
        simpleBlock(block, new ModelFile.UncheckedModelFile(new ResourceLocation(block.getRegistryName().getNamespace(), "block/" + block.getRegistryName().getPath())));
    }
}
