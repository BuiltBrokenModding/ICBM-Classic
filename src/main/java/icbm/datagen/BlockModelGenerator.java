package icbm.datagen;

import icbm.classic.ICBMConstants;
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

         this.cubeBottomTop(name(BlockReg.EXPLOSIVE_CONDENSED),
             texture("explosive/condensed_side"),
             texture("explosive/bottom_1"),
             texture("explosive/condensed_top")
         );
         this.cubeBottomTop(name(BlockReg.EXPLOSIVE_SHRAPNEL),
             texture("explosive/shrapnel_side"),
             texture("explosive/bottom_1"),
             texture("explosive/shrapnel_top")
         );
         this.cubeBottomTop(name(BlockReg.EXPLOSIVE_INCENDIARY),
             texture("explosive/incendiary"),
             texture("explosive/bottom_1"),
             texture("explosive/bottom_1")
         );
         this.cubeBottomTop(name(BlockReg.EXPLOSIVE_DEBILITATION),
             texture("explosive/debilitation"),
             texture("explosive/bottom_1"),
             texture("explosive/bottom_1")
         );
         this.cubeBottomTop(name(BlockReg.EXPLOSIVE_CHEMICAL),
             texture("explosive/chemical"),
             texture("explosive/bottom_1"),
             texture("explosive/bottom_1")
         );
         this.cubeBottomTop(name(BlockReg.EXPLOSIVE_ANVIL),
             texture("explosive/anvil"),
             texture("explosive/bottom_1"),
             texture("explosive/bottom_1")
         );
         this.cubeBottomTop(name(BlockReg.EXPLOSIVE_REPULSIVE),
             texture("explosive/repulsive"),
             texture("explosive/bottom_1"),
             texture("explosive/bottom_1")
         );
         this.cubeBottomTop(name(BlockReg.EXPLOSIVE_ATTRACTIVE),
             texture("explosive/attractive"),
             texture("explosive/bottom_1"),
             texture("explosive/bottom_1")
         );
         this.cubeBottomTop(name(BlockReg.EXPLOSIVE_COLOR),
             texture("explosive/colorful"),
             texture("explosive/bottom_1"),
             texture("explosive/bottom_1")
         );
         this.cubeBottomTop(name(BlockReg.EXPLOSIVE_SMOKE),
             texture("explosive/smoke"),
             texture("explosive/bottom_1"),
             texture("explosive/bottom_1")
         );
         this.cubeBottomTop(name(BlockReg.EXPLOSIVE_FRAGMENTATION),
             texture("explosive/fragmentation"),
             texture("explosive/bottom_2"),
             texture("explosive/bottom_2")
         );
         this.cubeBottomTop(name(BlockReg.EXPLOSIVE_CONTAGIOUS),
             texture("explosive/contagious"),
             texture("explosive/bottom_2"),
             texture("explosive/bottom_2")
         );
         this.cubeBottomTop(name(BlockReg.EXPLOSIVE_SONIC),
             texture("explosive/sonic"),
             texture("explosive/bottom_2"),
             texture("explosive/bottom_2")
         );
         this.cubeBottomTop(name(BlockReg.EXPLOSIVE_THERMOBARIC),
             texture("explosive/thermobaric"),
             texture("explosive/bottom_2"),
             texture("explosive/bottom_2")
         );
         this.cubeBottomTop(name(BlockReg.EXPLOSIVE_NUCLEAR),
             texture("explosive/nuclear"),
             texture("explosive/bottom_3"),
             texture("explosive/bottom_3")
         );
         this.cubeBottomTop(name(BlockReg.EXPLOSIVE_EMP),
             texture("explosive/emp"),
             texture("explosive/bottom_3"),
             texture("explosive/bottom_3")
         );
         this.cubeBottomTop(name(BlockReg.EXPLOSIVE_EXOTHERMIC),
             texture("explosive/exothermic"),
             texture("explosive/bottom_3"),
             texture("explosive/bottom_3")
         );
         this.cubeBottomTop(name(BlockReg.EXPLOSIVE_ENDOTHERMIC),
             texture("explosive/endothermic"),
             texture("explosive/bottom_3"),
             texture("explosive/bottom_3")
         );
         this.cubeBottomTop(name(BlockReg.EXPLOSIVE_GRAVITY),
             texture("explosive/gravity"),
             texture("explosive/bottom_3"),
             texture("explosive/bottom_3")
         );
         this.cubeBottomTop(name(BlockReg.EXPLOSIVE_ENDER),
             texture("explosive/ender"),
             texture("explosive/bottom_3"),
             texture("explosive/bottom_3")
         );
         this.cubeBottomTop(name(BlockReg.EXPLOSIVE_ANTIMATTER),
             texture("explosive/antimatter"),
             texture("explosive/redmatter_bottom"), //TODO why redmatter bottom
             texture("explosive/antimatter")
         );
         this.cubeBottomTop(name(BlockReg.EXPLOSIVE_REDMATTER),
             texture("explosive/redmatter_side"),
             texture("explosive/redmatter_bottom"),
             texture("explosive/redmatter_top")
         );

         this.cubeBottomTop(name(BlockReg.EXPLOSIVE_BREACHING),
             texture("explosive/breaching_side"),
             texture("explosive/bottom_2"),
             texture("explosive/breaching_top")
         );
    }

    private String name(RegistryObject<? extends Block> block) {
        return block.getId().getPath();
    }

    private ResourceLocation texture(String path) {
        return new ResourceLocation(ICBMConstants.DOMAIN, "blocks/" + path);
    }

    private ResourceLocation texture(RegistryObject<? extends Block> block) {
        return new ResourceLocation(block.getId().getNamespace(), "blocks/" + block.getId().getPath());
    }

    @Nonnull
    @Override
    public String getName() {
        return "ICBM Block Models";
    }
}
