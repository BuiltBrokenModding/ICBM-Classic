package icbm.classic.content.blocks.explosive;

import com.adelean.inject.resources.junit.jupiter.GivenJsonResource;
import com.adelean.inject.resources.junit.jupiter.TestWithResources;
import icbm.classic.ICBMClassic;
import icbm.classic.TestBase;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.caps.IExplosive;
import icbm.classic.api.reg.content.IExBlockRegistry;
import icbm.classic.content.reg.BlockReg;
import icbm.classic.lib.capability.ex.CapabilityExplosive;
import net.minecraft.block.Block;
import net.minecraft.init.Bootstrap;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;

import java.util.Objects;

@TestWithResources
public class TileEntityExplosiveTest extends TestBase {

    @GivenJsonResource("data/saves/4.0.0/incendiary-explosive-tile.json")
    NBTTagCompound version4save;

    private static Block block;
    private static Item item;
    private static final BlockPos pos = new BlockPos(5, 6, 7);

    public TileEntityExplosiveTest() {
        super("tile");
    }

    @BeforeAll
    public static void beforeAllTests()
    {
        // Start vanilla
        Bootstrap.register();

        // Setup explosive registry
        CapabilityExplosive.register();
        ICBMClassicAPI.EXPLOSIVE_CAPABILITY = getCap(IExplosive.class);
        ICBMClassic.INSTANCE = new ICBMClassic();
        ICBMClassic.INSTANCE.handleExRegistry(null);

        // Register block for placement
        ForgeRegistries.BLOCKS.register(block = new BlockExplosive());
        ForgeRegistries.ITEMS.register(item = new ItemBlockExplosive(block).setRegistryName(block.getRegistryName()));
    }

    @Test
    @DisplayName("Loads from old version 4.0.0 save file")
    void loadFromVersion4() {
        final World world = testManager.getWorld();

        // Place block in world, ensure it places or else error
        Assertions.assertTrue(world.setBlockState(pos, block.getDefaultState()));

        // Validate we have a test file
        Assertions.assertNotNull(version4save);

        // Check for our tile
        final TileEntity entity = world.getTileEntity(pos);
        Assertions.assertInstanceOf(TileEntityExplosive.class, entity);

        // Invoke loading
        entity.readFromNBT(version4save);

        // Validate we load the capability with the correct item stack
        final IExplosive capability = entity.getCapability(ICBMClassicAPI.EXPLOSIVE_CAPABILITY, null);
        Assertions.assertNotNull(capability);
        Assertions.assertTrue(ItemStack.areItemsEqual(new ItemStack(item, 1, 2), capability.toStack()));
    }
}
