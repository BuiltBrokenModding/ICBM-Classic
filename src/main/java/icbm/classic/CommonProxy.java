package icbm.classic;

import icbm.classic.api.missiles.parts.IMissileFlightLogic;
import icbm.classic.lib.network.packet.PacketSpawnAirParticle;
import icbm.classic.lib.network.packet.PacketSpawnBlockExplosion;
import icbm.classic.prefab.tile.IGuiTile;
import icbm.classic.lib.transform.vector.Pos;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

public class CommonProxy implements IGuiHandler
{
    public static final int GUI_ITEM = 10002;
    public static final int GUI_ENTITY = 10001;

    public void doLoadModels()
    {

    }

    public void preInit()
    {

    }

    public void init()
    {

    }

    public void postInit()
    {

    }

    public void loadComplete()
    {

    }

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
    {
        if (ID == GUI_ITEM)
        {
            return getServerGuiElement(y, player, x);
        }
        else if (ID == GUI_ENTITY)
        {
            return getServerGuiElement(y, player, world.getEntityByID(x));
        }
        return getServerGuiElement(ID, player, world.getTileEntity(new BlockPos(x, y, z)));
    }

    public Object getServerGuiElement(int ID, EntityPlayer player, int slot)
    {
        ItemStack stack = player.inventory.getStackInSlot(slot);
        if (stack != null && stack.getItem() instanceof IGuiTile)
        {
            return ((IGuiTile) stack.getItem()).getServerGuiElement(ID, player);
        }
        return null;
    }

    public Object getServerGuiElement(int ID, EntityPlayer player, TileEntity tile)
    {
        if (tile instanceof IGuiTile)
        {
            return ((IGuiTile) tile).getServerGuiElement(ID, player);
        }
        return null;
    }

    public Object getServerGuiElement(int ID, EntityPlayer player, Entity entity)
    {
        if (entity instanceof IGuiTile)
        {
            return ((IGuiTile) entity).getServerGuiElement(ID, player);
        }
        return null;
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
    {
        if (ID == GUI_ITEM)
        {
            return getServerGuiElement(y, player, world.getEntityByID(x));
        }
        else if (ID == GUI_ENTITY)
        {
            return getClientGuiElement(y, player, world.getEntityByID(x));
        }
        return getClientGuiElement(ID, player, world.getTileEntity(new BlockPos(x, y, z)));
    }

    public Object getClientGuiElement(int ID, EntityPlayer player, int slot)
    {
        ItemStack stack = player.inventory.getStackInSlot(slot);
        if (stack != null && stack.getItem() instanceof IGuiTile)
        {
            return ((IGuiTile) stack.getItem()).getClientGuiElement(ID, player);
        }
        return null;
    }

    public Object getClientGuiElement(int ID, EntityPlayer player, TileEntity tile)
    {
        if (tile instanceof IGuiTile)
        {
            return ((IGuiTile) tile).getClientGuiElement(ID, player);
        }
        return null;
    }

    public Object getClientGuiElement(int ID, EntityPlayer player, Entity entity)
    {
        if (entity instanceof IGuiTile)
        {
            return ((IGuiTile) entity).getClientGuiElement(ID, player);
        }
        return null;
    }

    @SideOnly(Side.CLIENT)
    public boolean isShiftHeld()
    {
        return Keyboard.isKeyDown(Keyboard.KEY_RSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_LSHIFT);
    }

    public void spawnSmoke(World world, Pos position, double v, double v1, double v2, float red, float green, float blue, float scale, int age)
    {

    }

    public void spawnAirParticle(World world, double x, double y, double z, double v, double v1, double v2, float red, float green, float blue, float scale, int ticksToLive)
    {
        //TODO allow client settings to sync to server to not received these packets
        PacketSpawnAirParticle.sendToAllClients(world, x, y, z, v, v1, v2, red, green, blue, scale, ticksToLive);
    }

    /**
     * Spawns a vanilla explosion particle when destroying blocks. Server side this will sync a packet per
     * block destroyed. Client side it will spawn 2 particles depending on data.
     *
     * @param world to spawn inside
     * @param sourceX of the blast
     * @param sourceY of the blast
     * @param sourceZ of the blast
     * @param blastScale of the blast
     * @param blockPos of the block being destroyed
     */
    public void spawnExplosionParticles(World world, double sourceX, double sourceY, double sourceZ, double blastScale, BlockPos blockPos)
    {
        //TODO allow client settings to sync to server to not received these packets
        PacketSpawnBlockExplosion.sendToAllClients(world, sourceX, sourceY, sourceZ, blastScale, blockPos);
    }

    public void spawnMissileSmoke(Entity entity, IMissileFlightLogic flightLogic, int ticksInAir) //TODO refactor to be packet based or wired to each flight logic type
    {

    }

    public void spawnPadSmoke(Entity entity, IMissileFlightLogic flightLogic, int ticksInAir) {

    }
}
