package icbm.classic.content.reg;

import icbm.classic.ICBMConstants;
import icbm.classic.content.blocks.emptower.TileEMPTower;
import icbm.classic.content.blocks.emptower.gui.ContainerEMPTower;
import icbm.classic.content.blocks.launcher.base.TileLauncherBase;
import icbm.classic.content.blocks.launcher.base.gui.ContainerLaunchBase;
import icbm.classic.content.blocks.launcher.cruise.TileCruiseLauncher;
import icbm.classic.content.blocks.launcher.cruise.gui.ContainerCruiseLauncher;
import icbm.classic.content.blocks.launcher.screen.TileLauncherScreen;
import icbm.classic.content.blocks.launcher.screen.gui.ContainerLaunchScreen;
import icbm.classic.content.blocks.radarstation.TileRadarStation;
import icbm.classic.content.blocks.radarstation.gui.ContainerRadarStation;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Menu (container) types. Containers are constructed with {@code (int windowId, Player player, TileX tile)};
 * the client side factory reads the tile position written by {@code player.openMenu(tile, buf -> buf.writeBlockPos(pos))}.
 */
public final class MenuReg
{
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, ICBMConstants.DOMAIN);

    public static final DeferredHolder<MenuType<?>, MenuType<ContainerEMPTower>> EMP_TOWER = MENUS.register("emptower",
        () -> IMenuTypeExtension.create((id, inv, buf) -> new ContainerEMPTower(id, inv.player, tile(inv, buf, TileEMPTower.class))));
    public static final DeferredHolder<MenuType<?>, MenuType<ContainerLaunchBase>> LAUNCHER_BASE = MENUS.register("launcherbase",
        () -> IMenuTypeExtension.create((id, inv, buf) -> new ContainerLaunchBase(id, inv.player, tile(inv, buf, TileLauncherBase.class))));
    public static final DeferredHolder<MenuType<?>, MenuType<ContainerCruiseLauncher>> CRUISE_LAUNCHER = MENUS.register("cruiselauncher",
        () -> IMenuTypeExtension.create((id, inv, buf) -> new ContainerCruiseLauncher(id, inv.player, tile(inv, buf, TileCruiseLauncher.class))));
    public static final DeferredHolder<MenuType<?>, MenuType<ContainerLaunchScreen>> LAUNCHER_SCREEN = MENUS.register("launcherscreen",
        () -> IMenuTypeExtension.create((id, inv, buf) -> new ContainerLaunchScreen(id, inv.player, tile(inv, buf, TileLauncherScreen.class))));
    public static final DeferredHolder<MenuType<?>, MenuType<ContainerRadarStation>> RADAR_STATION = MENUS.register("radarstation",
        () -> IMenuTypeExtension.create((id, inv, buf) -> new ContainerRadarStation(id, inv.player, tile(inv, buf, TileRadarStation.class))));

    private MenuReg()
    {
    }

    private static <T extends BlockEntity> T tile(Inventory inventory, RegistryFriendlyByteBuf buf, Class<T> clazz)
    {
        final BlockEntity tile = inventory.player.level().getBlockEntity(buf.readBlockPos());
        if (clazz.isInstance(tile))
        {
            return clazz.cast(tile);
        }
        throw new IllegalStateException("Expected " + clazz.getSimpleName() + " for menu but found " + tile);
    }

    /** Helper for menus to validate access, mirrors 1.12 Container#canInteractWith */
    public static boolean stillValid(AbstractContainerMenu menu, BlockEntity tile, net.minecraft.world.entity.player.Player player)
    {
        return tile != null && !tile.isRemoved() && player.distanceToSqr(tile.getBlockPos().getCenter()) <= 64;
    }

    public static void register(IEventBus modBus)
    {
        MENUS.register(modBus);
    }
}
