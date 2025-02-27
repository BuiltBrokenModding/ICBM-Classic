package icbm.classic.lib;

import icbm.classic.ICBMClassic;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

public class BlockUtils {

    /**
     * Opens a graphical user interface (GUI) for a player interacting with a block at the specified position in the world.
     * The method checks if the world is client-side, ensures that the tile entity implements the correct interface,
     * and validates that the player is a server-side player before proceeding.
     *
     * @param world The world in which the operation is taking place.
     * @param pos The position of the block whose GUI is to be opened.
     * @param player The player attempting to open the GUI.
     */
    public static void openGUI(World world, BlockPos pos, PlayerEntity player) {
        if (world.isRemote) {
            return;
        }

        final TileEntity tileEntity = world.getTileEntity(pos);
        if (!(tileEntity instanceof INamedContainerProvider)) {
            if (ICBMClassic.runningAsDev) {
                throw new RuntimeException("Tile entity " + tileEntity + " does not implement INamedContainerProvider");
            }
            return;
        }

        if (!(player instanceof ServerPlayerEntity)) {
            if (ICBMClassic.runningAsDev) {
                throw new RuntimeException(player + " is not a server player");
            }
            return;
        }

        NetworkHooks.openGui((ServerPlayerEntity) player, (INamedContainerProvider) tileEntity, tileEntity.getPos());
    }
}
