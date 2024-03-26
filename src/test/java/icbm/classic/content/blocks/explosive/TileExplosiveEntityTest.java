package icbm.classic.content.blocks.explosive;

import com.adelean.inject.resources.junit.jupiter.GivenJsonResource;
import com.adelean.inject.resources.junit.jupiter.TestWithResources;
import icbm.classic.TestBase;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.caps.IExplosive;
import icbm.classic.world.block.explosive.ExplosiveBlock;
import icbm.classic.world.block.explosive.ItemBlockExplosive;
import icbm.classic.world.block.explosive.BlockEntityExplosive;
import net.minecraft.world.level.block.Block;
import net.minecraft.init.Bootstrap;
import net.minecraft.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.junit.jupiter.api.*;

@TestWithResources
class TileExplosiveEntityTest extends TestBase {

    @GivenJsonResource("data/saves/4.0.0/tileEntity_IncendiaryExplosive.json")
    CompoundTag version4save;

    private static Block block;
    private static Item item;
    private static final BlockPos pos = new BlockPos(5, 6, 7);

    public TileExplosiveEntityTest() {
        super("tile");
    }

    @BeforeAll
    public static void beforeAllTests()
    {
        // Register block for placement
        ForgeRegistries.BLOCKS.register(block = new ExplosiveBlock());
        ForgeRegistries.ITEMS.register(item = new ItemBlockExplosive(block).setRegistryName(block.getRegistryName()));
    }

    @Test
    @DisplayName("Loads from old version 4.0.0 save file")
    void loadFromVersion4() {
        final Level level = testManager.getLevel();

        // Place block in world, ensure it places or else error
        Assertions.assertTrue(world.setBlockState(pos, block.getDefaultState()));

        // Validate we have a test file
        Assertions.assertNotNull(version4save);

        // Check for our tile
        final BlockEntity entity = world.getBlockEntity(pos);
        Assertions.assertInstanceOf(BlockEntityExplosive.class, entity);

        // Invoke loading
        entity.readFromNBT(version4save);

        // Validate we load the capability with the correct item stack
        final IExplosive capability = entity.getCapability(ICBMClassicAPI.EXPLOSIVE_CAPABILITY, null);
        Assertions.assertNotNull(capability);
        Assertions.assertTrue(ItemStack.areItemsEqual(new ItemStack(item, 1, 2), capability.toStack()));
    }
}
