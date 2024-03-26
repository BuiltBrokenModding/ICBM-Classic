package icbm.classic;

import icbm.classic.api.missiles.parts.IMissileFlightLogic;
import icbm.classic.lib.network.packet.PacketSpawnBlockExplosion;
import icbm.classic.mods.ModInteraction;
import icbm.classic.prefab.tile.IGuiTile;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class CommonProxy {
    public static final int GUI_ITEM = 10002;
    public static final int GUI_ENTITY = 10001;

    public void doLoadModels() {
    }

    public void preInit() {
        ModInteraction.preInit();
    }

    public void init() {
        ModInteraction.init();
    }

    public void postInit() {
        ModInteraction.postInit();
    }

    public void loadComplete() {
    }

    @Override
    public Object getServerGuiElement(int id, ServerPlayer player, Level level, int x, int y, int z) {
        if (id == GUI_ITEM) {
            return getServerGuiElement(y, player, x);
        } else if (id == GUI_ENTITY) {
            return getServerGuiElement(y, player, level.getEntity(x));
        }
        return getServerGuiElement(id, player, level.getBlockEntity(new BlockPos(x, y, z)));
    }

    public Object getServerGuiElement(int id, Player player, int slot) {
        ItemStack stack = player.getInventory().getItem(slot);
        if (stack.getItem() instanceof IGuiTile guiTile) {
            return guiTile.getServerGuiElement(id, player);
        }
        return null;
    }

    public Object getServerGuiElement(int id, Player player, BlockEntity blockEntity) {
        if (blockEntity instanceof IGuiTile guiBlockEntity) {
            return guiBlockEntity.getServerGuiElement(id, player);
        }
        return null;
    }

    public Object getServerGuiElement(int id, Player player, Entity entity) {
        if (entity instanceof IGuiTile guiEntity) {
            return guiEntity.getServerGuiElement(id, player);
        }
        return null;
    }

    @Override
    public Object getClientGuiElement(int id, Player player, Level level, int x, int y, int z) {
        if (id == GUI_ITEM) {
            return getServerGuiElement(y, player, level.getEntity(x));
        } else if (id == GUI_ENTITY) {
            return getClientGuiElement(y, player, level.getEntity(x));
        }
        return getClientGuiElement(id, player, level.getBlockEntity(new BlockPos(x, y, z)));
    }

    public Object getClientGuiElement(int id, Player player, int slot) {
        ItemStack stack = player.getInventory().getItem(slot);
        if (stack.getItem() instanceof IGuiTile guiTile) {
            return guiTile.getClientGuiElement(id, player);
        }
        return null;
    }

    public Object getClientGuiElement(int id, Player player, BlockEntity blockEntity) {
        if (blockEntity instanceof IGuiTile guiTile) {
            return guiTile.getClientGuiElement(id, player);
        }
        return null;
    }

    public Object getClientGuiElement(int ID, Player player, Entity entity) {
        if (entity instanceof IGuiTile guiTile) {
            return guiTile.getClientGuiElement(ID, player);
        }
        return null;
    }

    @OnlyIn(Dist.CLIENT)
    public boolean isShiftHeld() {
        return Keyboard.isKeyDown(Keyboard.KEY_RSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_LSHIFT);
    }

    /**
     * Spawns a vanilla explosion particle when destroying blocks. Server side this will sync a packet per
     * block destroyed. Client side it will spawn 2 particles depending on data.
     *
     * @param level      to spawn inside
     * @param sourceX    of the blast
     * @param sourceY    of the blast
     * @param sourceZ    of the blast
     * @param blastScale of the blast
     * @param blockPos   of the block being destroyed
     */
    public void spawnExplosionParticles(Level level, double sourceX, double sourceY, double sourceZ, double blastScale, BlockPos blockPos) {
        // TODO: allow client settings to sync to server to not received these packets
        PacketSpawnBlockExplosion.sendToAllClients(level, sourceX, sourceY, sourceZ, blastScale, blockPos);
    }

    public void spawnMissileSmoke(Entity entity, IMissileFlightLogic flightLogic, int ticksInAir) {
        // TODO: refactor to be packet based or wired to each flight logic type
    }

    public void spawnPadSmoke(Entity entity, IMissileFlightLogic flightLogic, int ticksInAir) {
    }
}
