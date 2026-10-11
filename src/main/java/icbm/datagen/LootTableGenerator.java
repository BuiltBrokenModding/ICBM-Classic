package icbm.datagen;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import icbm.classic.ICBMConstants;
import icbm.classic.content.reg.BlockReg;
import net.minecraft.block.Block;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.LootTableProvider;
import net.minecraft.data.loot.BlockLootTables;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.*;
import net.minecraft.world.storage.loot.conditions.SurvivesExplosion;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class LootTableGenerator extends LootTableProvider {

	public LootTableGenerator(DataGenerator dataGeneratorIn) {
		super(dataGeneratorIn);
	}

	@Override
	protected List<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>>, LootParameterSet>> getTables() {
		return ImmutableList.of(
			Pair.of(BlockLootTableGenerator::new, LootParameterSets.BLOCK)
		);
	}

	@Override
	protected void validate(Map<ResourceLocation, LootTable> map, ValidationResults validationresults) {
		map.forEach((location, lootTable) -> LootTableManager.func_215302_a(validationresults, location, lootTable, map::get));
	}

	public static class BlockLootTableGenerator extends BlockLootTables {

		protected void registerDropSelf(Block block) {
			registerLootTable(block, droppingItem(block));
		}

		protected static LootTable.Builder droppingItem(IItemProvider item) {
			return LootTable.builder().addLootPool(
				LootPool.builder()
					.rolls(ConstantRange.of(1))
					.addEntry(ItemLootEntry.builder(item))
					.acceptCondition(SurvivesExplosion.builder())
			);
		}

		@Override
		protected void addTables() {
			registerDropSelf(BlockReg.CONCRETE_NORMAL.get());
			registerDropSelf(BlockReg.CONCRETE_COMPACT.get());
			registerDropSelf(BlockReg.CONCRETE_REINFORCED.get());
			registerDropSelf(BlockReg.GLASS_REINFORCED.get());

			registerDropSelf(BlockReg.SPIKE_NORMAL.get());
			registerDropSelf(BlockReg.SPIKE_FIRE.get());
			registerDropSelf(BlockReg.SPIKE_POISON.get());

			registerDropSelf(BlockReg.EMP_TOWER_BASE.get());
			registerDropSelf(BlockReg.EMP_TOWER_COIL.get());

			registerDropSelf(BlockReg.RADAR_SCREEN.get());

			registerDropSelf(BlockReg.LAUNCHER_FRAME.get());
			registerDropSelf(BlockReg.LAUNCHER_FRAME_TOP.get());
			registerDropSelf(BlockReg.LAUNCHER_FRAME_BASE.get());

			registerDropSelf(BlockReg.LAUNCHER_CONNECTOR.get());
			registerDropSelf(BlockReg.LAUNCHER_BASE.get());
			registerDropSelf(BlockReg.LAUNCHER_SCREEN.get());
			registerDropSelf(BlockReg.LAUNCHER_CRUISE.get());

			registerDropSelf(BlockReg.RADIOACTIVE_DIRT.get());
			registerDropSelf(BlockReg.RADIOACTIVE_STONE.get());

			registerDropSelf(BlockReg.EXPLOSIVE_CONDENSED.get());
			registerDropSelf(BlockReg.EXPLOSIVE_SHRAPNEL.get());
			registerDropSelf(BlockReg.EXPLOSIVE_INCENDIARY.get());
			registerDropSelf(BlockReg.EXPLOSIVE_DEBILITATION.get());
			registerDropSelf(BlockReg.EXPLOSIVE_CHEMICAL.get());
			registerDropSelf(BlockReg.EXPLOSIVE_ANVIL.get());
			registerDropSelf(BlockReg.EXPLOSIVE_REPULSIVE.get());
			registerDropSelf(BlockReg.EXPLOSIVE_ATTRACTIVE.get());
			registerDropSelf(BlockReg.EXPLOSIVE_COLOR.get());
			registerDropSelf(BlockReg.EXPLOSIVE_SMOKE.get());
			registerDropSelf(BlockReg.EXPLOSIVE_FRAGMENTATION.get());
			registerDropSelf(BlockReg.EXPLOSIVE_CONTAGIOUS.get());
			registerDropSelf(BlockReg.EXPLOSIVE_SONIC.get());
			registerDropSelf(BlockReg.EXPLOSIVE_THERMOBARIC.get());
			registerDropSelf(BlockReg.EXPLOSIVE_NUCLEAR.get());
			registerDropSelf(BlockReg.EXPLOSIVE_EMP.get());
			registerDropSelf(BlockReg.EXPLOSIVE_EXOTHERMIC.get());
			registerDropSelf(BlockReg.EXPLOSIVE_ENDOTHERMIC.get());
			registerDropSelf(BlockReg.EXPLOSIVE_GRAVITY.get());
			registerDropSelf(BlockReg.EXPLOSIVE_ENDER.get());
			registerDropSelf(BlockReg.EXPLOSIVE_ANTIMATTER.get());
			registerDropSelf(BlockReg.EXPLOSIVE_REDMATTER.get());
			registerDropSelf(BlockReg.EXPLOSIVE_BREACHING.get());
		}

		@Override
		protected Iterable<Block> getKnownBlocks() {
			return BlockReg.BLOCKS.getEntries().stream()
				.map(RegistryObject::get)
				.collect(Collectors.toList());
		}
	}
}
