package icbm.classic.content.reg;

import icbm.classic.ICBMConstants;
import icbm.classic.content.blocks.emptower.gui.ContainerEMPTower;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

@NoArgsConstructor(access = AccessLevel.NONE)
public final class ContainerReg {
    public static final DeferredRegister<ContainerType<?>> CONTAINER_TYPES = new DeferredRegister<ContainerType<?>>(ForgeRegistries.CONTAINERS, ICBMConstants.DOMAIN);

    public static final RegistryObject<ContainerType<ContainerEMPTower>> EMP_TOWER_BASE = registerTileContainer(
        TileReg.EMP_TOWER_BASE,
        ContainerEMPTower::new
    );

    private static <CONTAINER extends Container, TILE extends TileEntity> RegistryObject<ContainerType<CONTAINER>>
    registerTileContainer(RegistryObject<TileEntityType<TILE>> tileType, CreateTileContainerFunction<CONTAINER, TILE> factory) {
        return CONTAINER_TYPES.register(tileType.getId().getPath(), () -> IForgeContainerType.create((windowId, inv, data) -> {
            final World world = inv.player.world;
            final BlockPos pos = data.readBlockPos();
            final TileEntity tileEntity = world.getTileEntity(pos);
            if (tileEntity != null && tileEntity.getType() == tileType.get()) {
                return factory.create(windowId, inv.player, (TILE) tileEntity);
            }
            return null;
        }));
    }

    private static <CONTAINER extends Container> RegistryObject<ContainerType<CONTAINER>> register(String name, CreateContainerFunction<CONTAINER> factory) {
        return CONTAINER_TYPES.register(name, () -> IForgeContainerType.create((windowId, inv, data) -> {
            final World world = inv.player.world;
            final BlockPos pos = data.readBlockPos();
            return factory.create(world, pos, windowId, inv.player, inv);
        }));
    }

    private interface CreateTileContainerFunction<CONTAINER extends Container, TILE extends TileEntity> {
        CONTAINER create(int windowId, PlayerEntity player, TILE tile);
    }

    private interface CreateContainerFunction<CONTAINER extends Container> {
        CONTAINER create(World world, BlockPos pos, int windowId, PlayerEntity player, PlayerInventory inventory);
    }
}
