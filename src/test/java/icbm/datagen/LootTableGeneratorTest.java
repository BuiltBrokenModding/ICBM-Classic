package icbm.datagen;

import icbm.classic.content.reg.BlockReg;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Bootstrap;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Unit tests validating loot table data generation for registered blocks.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class LootTableGeneratorTest {

	private final Map<ResourceLocation, LootTable.Builder> generatedTables = new HashMap<>();

	@BeforeAll
	public void setUp() throws Exception {
		initializeVanillaRegistries();
		initializeBlockRegistryEntries();
		generateLootTables();
	}

	/**
	 * Bootstraps Vanilla Minecraft registries and loot table managers.
	 */
	private static void initializeVanillaRegistries() {
		Bootstrap.register();
	}

	/**
	 * Populates DeferredRegister RegistryObject values and associates BlockItems.
	 */
	@SuppressWarnings("unchecked")
	private static void initializeBlockRegistryEntries() throws Exception {
		Field entriesField = DeferredRegister.class.getDeclaredField("entries");
		entriesField.setAccessible(true);
		Map<RegistryObject<?>, java.util.function.Supplier<?>> entries =
			(Map<RegistryObject<?>, java.util.function.Supplier<?>>) entriesField.get(BlockReg.BLOCKS);

		Field valField = RegistryObject.class.getDeclaredField("value");
		valField.setAccessible(true);

		for (Map.Entry<RegistryObject<?>, java.util.function.Supplier<?>> entry : entries.entrySet()) {
			RegistryObject<?> ro = entry.getKey();
			Block block = (Block) entry.getValue().get();
			if (block.getRegistryName() == null) {
				block.setRegistryName(ro.getId());
			}
			valField.set(ro, block);

			BlockItem item = new BlockItem(block, new Item.Properties());
			if (item.getRegistryName() == null) {
				item.setRegistryName(ro.getId());
			}
			Item.BLOCK_TO_ITEM.put(block, item);
		}
	}

	/**
	 * Runs the block loot table generator to produce loot table builders.
	 */
	private void generateLootTables() {
		LootTableGenerator.BlockLootTableGenerator generator = new LootTableGenerator.BlockLootTableGenerator();
		generator.accept(generatedTables::put);
	}

	/**
	 * Retrieves all registered Block IDs from BlockReg for parameterized test execution.
	 */
	static Stream<ResourceLocation> provideRegisteredBlockIds() throws Exception {
		List<ResourceLocation> blockIds = new ArrayList<>();
		for (Field field : BlockReg.class.getDeclaredFields()) {
			if (Modifier.isStatic(field.getModifiers()) && RegistryObject.class.isAssignableFrom(field.getType())) {
				@SuppressWarnings("unchecked")
				RegistryObject<Block> regObject = (RegistryObject<Block>) field.get(null);
				blockIds.add(regObject.getId());
			}
		}
		Assertions.assertFalse(blockIds.isEmpty(), "BlockReg should contain registered blocks");
		return blockIds.stream();
	}

	/**
	 * Tests that every registered block has a generated loot table and that it builds successfully.
	 */
	@ParameterizedTest(name = "{index} => block: {0}")
	@MethodSource("provideRegisteredBlockIds")
	public void testBlockHasValidLootTable(ResourceLocation blockId) {
		ResourceLocation expectedLootTableId = getExpectedLootTableId(blockId);

		assertLootTableExists(expectedLootTableId, blockId);
		assertLootTableBuilds(expectedLootTableId);
	}

	private ResourceLocation getExpectedLootTableId(ResourceLocation blockId) {
		return new ResourceLocation(blockId.getNamespace(), "blocks/" + blockId.getPath());
	}

	private void assertLootTableExists(ResourceLocation lootTableId, ResourceLocation blockId) {
		Assertions.assertTrue(generatedTables.containsKey(lootTableId),
			"Missing loot table for block: " + blockId + " (expected table ID: " + lootTableId + ")");
	}

	private void assertLootTableBuilds(ResourceLocation lootTableId) {
		LootTable.Builder builder = generatedTables.get(lootTableId);
		Assertions.assertNotNull(builder, "LootTable.Builder should not be null for " + lootTableId);

		LootTable table = builder.build();
		Assertions.assertNotNull(table, "Built LootTable should not be null for " + lootTableId);
	}
}
