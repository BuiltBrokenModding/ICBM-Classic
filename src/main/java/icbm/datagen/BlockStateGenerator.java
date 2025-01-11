package icbm.datagen;

import icbm.classic.ICBMConstants;
import icbm.classic.content.blocks.launcher.connector.BlockLaunchConnector;
import icbm.classic.content.blocks.radarstation.BlockRadarStation;
import icbm.classic.content.blocks.radarstation.RadarState;
import icbm.classic.content.reg.BlockReg;
import net.minecraft.block.Block;
import net.minecraft.data.DataGenerator;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ExistingFileHelper;
import net.minecraftforge.client.model.generators.ModelFile;

public class BlockStateGenerator extends BlockStateProvider {
    public BlockStateGenerator(DataGenerator gen, String modid, ExistingFileHelper exFileHelper) {
        super(gen, modid, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        noVariantModel(BlockReg.RADIOACTIVE_DIRT.get());
        noVariantModel(BlockReg.RADIOACTIVE_STONE.get());

        noVariantModel(BlockReg.CONCRETE_NORMAL.get());
        noVariantModel(BlockReg.CONCRETE_COMPACT.get());
        noVariantModel(BlockReg.CONCRETE_REINFORCED.get());
        noVariantModel(BlockReg.GLASS_REINFORCED.get());

        noVariantModel(BlockReg.SPIKE_NORMAL.get());
        noVariantModel(BlockReg.SPIKE_FIRE.get());
        noVariantModel(BlockReg.SPIKE_POISON.get());

        facingAlignedModel(BlockReg.EXPLOSIVE_CONDENSED.get());
        facingAlignedModel(BlockReg.EXPLOSIVE_SHRAPNEL.get());
        facingAlignedModel(BlockReg.EXPLOSIVE_INCENDIARY.get());
        facingAlignedModel(BlockReg.EXPLOSIVE_DEBILITATION.get());
        facingAlignedModel(BlockReg.EXPLOSIVE_CHEMICAL.get());
        facingAlignedModel(BlockReg.EXPLOSIVE_ANVIL.get());
        facingAlignedModel(BlockReg.EXPLOSIVE_REPULSIVE.get());
        facingAlignedModel(BlockReg.EXPLOSIVE_ATTRACTIVE.get());
        facingAlignedModel(BlockReg.EXPLOSIVE_COLOR.get());
        facingAlignedModel(BlockReg.EXPLOSIVE_SMOKE.get());
        facingAlignedModel(BlockReg.EXPLOSIVE_FRAGMENTATION.get());
        facingAlignedModel(BlockReg.EXPLOSIVE_CONTAGIOUS.get());
        facingAlignedModel(BlockReg.EXPLOSIVE_SONIC.get());
        facingAlignedModel(BlockReg.EXPLOSIVE_THERMOBARIC.get());
        facingAlignedModel(BlockReg.EXPLOSIVE_NUCLEAR.get());
        facingAlignedModel(BlockReg.EXPLOSIVE_EMP.get());
        facingAlignedModel(BlockReg.EXPLOSIVE_EXOTHERMIC.get());
        facingAlignedModel(BlockReg.EXPLOSIVE_ENDOTHERMIC.get());
        facingAlignedModel(BlockReg.EXPLOSIVE_GRAVITY.get());
        facingAlignedModel(BlockReg.EXPLOSIVE_ENDER.get());
        facingAlignedModel(BlockReg.EXPLOSIVE_ANTIMATTER.get());
        facingAlignedModel(BlockReg.EXPLOSIVE_REDMATTER.get());

        facingAlignedModel(BlockReg.EXPLOSIVE_BREACHING.get());

        radarModel();
        noVariantModel(BlockReg.EMP_TOWER_BASE.get());
        noVariantModel(BlockReg.EMP_TOWER_COIL.get());

        horizontalFacingModel(BlockReg.LAUNCHER_FRAME_BASE.get());
        horizontalFacingModel(BlockReg.LAUNCHER_FRAME_TOP.get());
        horizontalFacingModel(BlockReg.LAUNCHER_FRAME.get());

        directionalBlock(BlockReg.LAUNCHER_BASE.get(), new ModelFile.UncheckedModelFile(modelPath(BlockReg.LAUNCHER_BASE.get())));
        directionalBlock(BlockReg.LAUNCHER_SCREEN.get(), new ModelFile.UncheckedModelFile(modelPath(BlockReg.LAUNCHER_SCREEN.get())));
        launcherConnector();
        horizontalFacingModel(BlockReg.LAUNCHER_CRUISE.get());
    }

    private void launcherConnector() {
        getVariantBuilder(BlockReg.LAUNCHER_CONNECTOR.get())
            .forAllStates(state -> {

                final ResourceLocation model =  new ResourceLocation(ICBMConstants.DOMAIN, "block/" + BlockReg.LAUNCHER_CONNECTOR.getId().getPath() + "/"
                    + (state.get(BlockLaunchConnector.DOWN) ? "a" : "h")
                    + (state.get(BlockLaunchConnector.UP) ? "a" : "h")
                    + (state.get(BlockLaunchConnector.NORTH) ? "a" : "h")
                    + (state.get(BlockLaunchConnector.SOUTH) ? "a" : "h")
                    + (state.get(BlockLaunchConnector.EAST) ? "a" : "h")
                    + (state.get(BlockLaunchConnector.WEST) ? "a" : "h")
                );
                return ConfiguredModel.builder().modelFile(new ModelFile.UncheckedModelFile(model)).build();
            });
    }

    private void radarModel() {
        getVariantBuilder(BlockReg.RADAR_SCREEN.get())
            .forAllStates(state -> {
                final Direction dir = state.get(BlockStateProperties.FACING);
                final RadarState radarState = state.get(BlockRadarStation.RADAR_STATE);
                final ResourceLocation model = new ResourceLocation(ICBMConstants.DOMAIN,
                    "block/" + BlockReg.RADAR_SCREEN.getId().getPath() + "/" + radarState.getName().toLowerCase()
                );
                return ConfiguredModel.builder()
                    .modelFile(new ModelFile.UncheckedModelFile(model))
                    .rotationX(dir == Direction.DOWN ? 180 : dir.getAxis().isHorizontal() ? 90 : 0)
                    .rotationY(dir.getAxis().isVertical() ? 0 : (((int) dir.getHorizontalAngle()) + 180) % 360)
                    .build();
            });
    }

    private void noVariantModel(Block block) {
        simpleBlock(block, new ModelFile.UncheckedModelFile(modelPath(block)));
    }

    // Top aligns with placement face - If on NORTH then top faces NORTH
    private void facingAlignedModel(Block block) {
        getVariantBuilder(block).forAllStates(state -> {
            final Direction facing = state.get(BlockStateProperties.FACING);
            return ConfiguredModel.builder().modelFile(new ModelFile.UncheckedModelFile(modelPath(block)))
                .rotationX(facing.getYOffset() * 180)
                .rotationY(((int) facing.getHorizontalAngle() + 180) % 360)
                .build();
        });
    }

    private void horizontalFacingModel(Block block) {
        getVariantBuilder(block).forAllStates(state -> {
            final Direction facing = state.get(BlockStateProperties.HORIZONTAL_FACING);
            return ConfiguredModel.builder().modelFile(new ModelFile.UncheckedModelFile(modelPath(block)))
                .rotationX(facing.getYOffset() * 180)
                .rotationY(((int) facing.getHorizontalAngle() + 180) % 360)
                .build();
        });
    }

    private ResourceLocation modelPath(Block block) {
        return new ResourceLocation(block.getRegistryName().getNamespace(), "block/" + block.getRegistryName().getPath());
    }
}
