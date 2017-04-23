package icbm.classic.content.machines.launcher.frame;

import com.builtbroken.jlib.data.vector.IPos3D;
import com.builtbroken.mc.api.tile.multiblock.IMultiTile;
import com.builtbroken.mc.api.tile.multiblock.IMultiTileHost;
import com.builtbroken.mc.core.network.IPacketIDReceiver;
import com.builtbroken.mc.core.registry.implement.IRecipeContainer;
import com.builtbroken.mc.lib.helper.recipe.UniversalRecipe;
import com.builtbroken.mc.imp.transform.vector.Pos;
import com.builtbroken.mc.prefab.items.ItemBlockSubTypes;
import com.builtbroken.mc.prefab.tile.Tile;
import com.builtbroken.mc.prefab.tile.TileMachine;
import com.builtbroken.mc.framework.multiblock.EnumMultiblock;
import com.builtbroken.mc.framework.multiblock.MultiBlockHelper;
import cpw.mods.fml.common.registry.GameRegistry;
import icbm.classic.ICBMClassic;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.oredict.ShapedOreRecipe;

import java.util.HashMap;
import java.util.List;

/**
 * This tile entity is for the screen of the missile launcher
 *
 * @author Calclavia
 */
public class TileLauncherFrame extends TileMachine implements IPacketIDReceiver, IMultiTileHost, IRecipeContainer
{
    public static HashMap<IPos3D, String> tileMapCache = new HashMap();

    static
    {
        tileMapCache.put(new Pos(0, 1, 0), EnumMultiblock.TILE.getTileName());
        tileMapCache.put(new Pos(0, 2, 0), EnumMultiblock.TILE.getTileName());
    }

    // The tier of this screen
    private int tier = 0;

    private boolean _destroyingStructure = false;

    public TileLauncherFrame()
    {
        super("launcherFrame", Material.iron);
        this.itemBlock = ItemBlockSubTypes.class;
        this.hardness = 10f;
        this.resistance = 10f;
        this.isOpaque = false;
    }

    @Override
    public void writeDescPacket(ByteBuf buf)
    {
        super.writeDescPacket(buf);
        buf.writeInt(tier);
    }

    @Override
    public Tile newTile()
    {
        return new TileLauncherFrame();
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

    //==========================================
    //==== Multi-Block code
    //=========================================

    @Override
    public void firstTick()
    {
        super.firstTick();
        MultiBlockHelper.buildMultiBlock(world(), this, true, true);
    }

    @Override
    public void onMultiTileAdded(IMultiTile tileMulti)
    {
        if (tileMulti instanceof TileEntity)
        {
            if (tileMapCache.containsKey(new Pos((TileEntity) this).sub(new Pos((TileEntity) tileMulti))))
            {
                tileMulti.setHost(this);
            }
        }
    }

    @Override
    public boolean onMultiTileBroken(IMultiTile tileMulti, Object source, boolean harvest)
    {
        if (!_destroyingStructure && tileMulti instanceof TileEntity)
        {
            Pos pos = new Pos((TileEntity) tileMulti).sub(new Pos((TileEntity) this));

            if (tileMapCache.containsKey(pos))
            {
                MultiBlockHelper.destroyMultiBlockStructure(this, harvest, true, true);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean canPlaceBlockAt()
    {
        return super.canPlaceBlockAt() && world().getBlock(xi(), yi() + 1, zi()).isReplaceable(world(), xi(), yi() + 1, zi()) && world().getBlock(xi(), yi() + 2, zi()).isReplaceable(world(), xi(), yi() + 2, zi());
    }

    @Override
    public boolean canPlaceBlockOnSide(ForgeDirection side)
    {
        return side == ForgeDirection.UP && canPlaceBlockAt();
    }

    @Override
    public boolean removeByPlayer(EntityPlayer player, boolean willHarvest)
    {
        MultiBlockHelper.destroyMultiBlockStructure(this, false, true, false);
        return super.removeByPlayer(player, willHarvest);
    }

    @Override
    public void onTileInvalidate(IMultiTile tileMulti)
    {

    }

    @Override
    public boolean onMultiTileActivated(IMultiTile tile, EntityPlayer player, int side, float xHit, float yHit, float zHit)
    {
        return this.onPlayerRightClick(player, side, new Pos(xHit, yHit, zHit));
    }

    @Override
    public void onMultiTileClicked(IMultiTile tile, EntityPlayer player)
    {

    }

    @Override
    public HashMap<IPos3D, String> getLayoutOfMultiBlock()
    {
        return tileMapCache;
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
        this.tier = itemStack.getItemDamage();
    }

    @Override
    public int metadataDropped(int meta, int fortune)
    {
        return tier;
    }

    @Override
    protected boolean useMetaForFacing()
    {
        return true;
    }
}
