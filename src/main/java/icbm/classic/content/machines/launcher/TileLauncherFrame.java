package icbm.classic.content.machines.launcher;

import com.builtbroken.jlib.data.vector.IPos3D;
import com.builtbroken.mc.api.tile.IRotatable;
import com.builtbroken.mc.api.tile.multiblock.IMultiTile;
import com.builtbroken.mc.api.tile.multiblock.IMultiTileHost;
import com.builtbroken.mc.core.network.IPacketReceiver;
import com.builtbroken.mc.core.network.packet.PacketTile;
import com.builtbroken.mc.core.network.packet.PacketType;
import com.builtbroken.mc.core.registry.implement.IRecipeContainer;
import com.builtbroken.mc.lib.helper.recipe.UniversalRecipe;
import com.builtbroken.mc.prefab.tile.TileEnt;
import cpw.mods.fml.common.registry.GameRegistry;
import icbm.classic.ICBMClassic;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.oredict.ShapedOreRecipe;

import java.util.HashMap;
import java.util.List;

/**
 * This tile entity is for the screen of the missile launcher
 *
 * @author Calclavia
 */
public class TileLauncherFrame extends TileEnt implements IPacketReceiver, IMultiTileHost, IRotatable, IRecipeContainer
{
    // The tier of this screen
    private int tier = 0;
    private byte orientation = 3;

    public TileLauncherFrame()
    {
        super("launcherFrame", Material.iron);
    }

    @Override
    public PacketTile getDescPacket()
    {
        return new PacketTile(this, this.orientation, this.getTier());
    }

    /** Gets the inaccuracy of the missile based on the launcher support frame's tier */
    public int getInaccuracy()
    {
        switch (this.tier)
        {
            default:
                return 15;
            case 1:
                return 7;
            case 2:
                return 0;
        }
    }

    /**
     * Determines if this TileEntity requires update calls.
     *
     * @return True if you want updateEntity() to be called, false if not
     */
    @Override
    public boolean canUpdate()
    {
        return false;
    }

    /** Reads a tile entity from NBT. */
    @Override
    public void readFromNBT(NBTTagCompound par1NBTTagCompound)
    {
        super.readFromNBT(par1NBTTagCompound);
        this.tier = par1NBTTagCompound.getInteger("tier");
    }

    /** Writes a tile entity to NBT. */
    @Override
    public void writeToNBT(NBTTagCompound par1NBTTagCompound)
    {
        super.writeToNBT(par1NBTTagCompound);
        par1NBTTagCompound.setInteger("tier", this.tier);
    }


    public int getTier()
    {
        return this.tier;
    }


    public void setTier(int tier)
    {
        this.tier = tier;
    }

    @Override
    public ForgeDirection getDirection()
    {
        return ForgeDirection.getOrientation(this.orientation);
    }

    @Override
    public void setDirection(ForgeDirection facingDirection)
    {
        this.orientation = (byte) facingDirection.ordinal();
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox()
    {
        return INFINITE_EXTENT_AABB;
    }

    @Override
    public void onMultiTileAdded(IMultiTile tileMulti)
    {

    }

    @Override
    public boolean onMultiTileBroken(IMultiTile tileMulti, Object source, boolean harvest)
    {
        return false;
    }

    @Override
    public void onTileInvalidate(IMultiTile tileMulti)
    {

    }

    @Override
    public boolean onMultiTileActivated(IMultiTile tile, EntityPlayer player, int side, IPos3D hit)
    {
        return false;
    }

    @Override
    public void onMultiTileClicked(IMultiTile tile, EntityPlayer player)
    {

    }

    @Override
    public HashMap<IPos3D, String> getLayoutOfMultiBlock()
    {
        //return new Pos[]{new Pos(0, 1, 0), new Pos(0, 2, 0)};
        return null;
    }

    @Override
    public void read(ByteBuf data, EntityPlayer player, PacketType packet)
    {
        this.orientation = data.readByte();
        this.tier = data.readInt();
    }

    @Override
    public void genRecipes(List<IRecipe> recipes)
    {
        // Missile Launcher Support Frame
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ICBMClassic.blockLaunchSupport, 1, 0),
                "! !", "!!!", "! !",
                '!', UniversalRecipe.SECONDARY_METAL.get()));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ICBMClassic.blockLaunchSupport, 1, 1),
                "! !", "!@!", "! !",
                '!', UniversalRecipe.PRIMARY_METAL.get(),
                '@', new ItemStack(ICBMClassic.blockLaunchSupport, 1, 0)));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ICBMClassic.blockLaunchSupport, 1, 2),
                "! !", "!@!", "! !",
                '!', UniversalRecipe.PRIMARY_PLATE.get(),
                '@', new ItemStack(ICBMClassic.blockLaunchSupport, 1, 1)));
    }

    @Override
    public void onPlaced(EntityLivingBase entityLiving, ItemStack itemStack)
    {
        super.onPlaced(entityLiving, itemStack);
        this.tier = itemStack.stackSize;
    }
}
