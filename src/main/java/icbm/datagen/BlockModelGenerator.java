package icbm.datagen;

import icbm.classic.ICBMConstants;
import icbm.classic.content.blocks.launcher.connector.BlockLaunchConnector;
import icbm.classic.content.reg.BlockReg;
import icbm.classic.content.reg.ItemReg;
import net.minecraft.block.Block;
import net.minecraft.data.DataGenerator;
import net.minecraft.state.BooleanProperty;
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

        this.cross(name(BlockReg.SPIKE_NORMAL), texture(BlockReg.SPIKE_NORMAL));
        this.cross(name(BlockReg.SPIKE_FIRE), texture(BlockReg.SPIKE_FIRE));
        this.cross(name(BlockReg.SPIKE_POISON), texture(BlockReg.SPIKE_POISON));

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

        orientable(name(BlockReg.RADAR_SCREEN),
            texture("machine_green"),
            texture("screen/radar"),
            texture("machine_green")
        );
        cubeBottomTop(name(BlockReg.LAUNCHER_BASE),
            texture("silo_pad/side"),
            texture("silo_pad/bottom"),
            texture("silo_pad/top")
        );
        cubeBottomTop(name(BlockReg.LAUNCHER_SCREEN),
            texture("machine_green"),
            texture("machine_green"),
            texture("screen/silo")
        );

        // Insanity
        for (Boolean DOWN : BlockLaunchConnector.DOWN.getAllowedValues()) {
            for (Boolean UP : BlockLaunchConnector.UP.getAllowedValues()) {
                for (Boolean NORTH : BlockLaunchConnector.NORTH.getAllowedValues()) {
                    for (Boolean SOUTH : BlockLaunchConnector.SOUTH.getAllowedValues()) {
                        for (Boolean EAST : BlockLaunchConnector.EAST.getAllowedValues()) {
                            for (Boolean WEST : BlockLaunchConnector.WEST.getAllowedValues()) {
                                cube(

                                    "block/" + name(BlockReg.LAUNCHER_CONNECTOR) + "/"
                                        + (DOWN ? "a" : "h")
                                        + (UP ? "a" : "h")
                                        + (NORTH ? "a" : "h")
                                        + (SOUTH ? "a" : "h")
                                        + (EAST ? "a" : "h")
                                        + (WEST ? "a" : "h"),

                                    texture("silo_connector/" + (DOWN ? "disconnected" : "connected")),
                                    texture("silo_connector/" + (UP ? "disconnected" : "connected")),
                                    texture("silo_connector/" + (NORTH ? "disconnected" : "connected")),
                                    texture("silo_connector/" + (SOUTH ? "disconnected" : "connected")),
                                    texture("silo_connector/" + (EAST ? "disconnected" : "connected")),
                                    texture("silo_connector/" + (WEST ? "disconnected" : "connected"))
                                ).texture("particle", texture("silo_connector/connected"));
                            }
                        }
                    }
                }
            }
        }

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
