package icbm.datagen;

import icbm.classic.content.reg.BlockReg;
import net.minecraft.block.Block;
import net.minecraft.data.DataGenerator;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.generators.BlockModelProvider;
import net.minecraftforge.client.model.generators.ExistingFileHelper;
import net.minecraftforge.fml.RegistryObject;

import javax.annotation.Nonnull;

public class BlockModelGenerator extends BlockModelProvider {

    public BlockModelGenerator(DataGenerator generator, String modid, ExistingFileHelper existingFileHelper) {
        super(generator, modid, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        this.cubeAll(name(BlockReg.RADIOACTIVE_DIRT), texture(BlockReg.RADIOACTIVE_DIRT));
        this.cubeAll(name(BlockReg.RADIOACTIVE_STONE), texture(BlockReg.RADIOACTIVE_STONE));

        this.cubeAll(name(BlockReg.CONCRETE_NORMAL), texture(BlockReg.CONCRETE_NORMAL));
        this.cubeAll(name(BlockReg.CONCRETE_COMPACT), texture(BlockReg.CONCRETE_COMPACT));
        this.cubeAll(name(BlockReg.CONCRETE_REINFORCED), texture(BlockReg.CONCRETE_REINFORCED));
        this.cubeAll(name(BlockReg.GLASS_REINFORCED), texture(BlockReg.GLASS_REINFORCED));
    }

    private String name(RegistryObject<Block> block) {
        return block.getId().getPath();
    }

    private ResourceLocation texture(RegistryObject<Block> block, String path) {
        return new ResourceLocation(block.getId().getNamespace(), "blocks/" + path);
    }

    private ResourceLocation texture(RegistryObject<Block> block) {
        return new ResourceLocation(block.getId().getNamespace(), "blocks/" + block.getId().getPath());
    }

    @Nonnull
    @Override
    public String getName() {
        return "ICBM Block Models";
    }
}
