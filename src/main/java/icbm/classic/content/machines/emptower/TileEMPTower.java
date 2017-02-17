package icbm.classic.content.machines.emptower;

import com.builtbroken.jlib.data.vector.IPos3D;
import com.builtbroken.mc.api.tile.IGuiTile;
import com.builtbroken.mc.api.tile.multiblock.IMultiTile;
import com.builtbroken.mc.api.tile.multiblock.IMultiTileHost;
import com.builtbroken.mc.core.network.IPacketIDReceiver;
import com.builtbroken.mc.core.network.packet.PacketTile;
import com.builtbroken.mc.core.network.packet.PacketType;
import com.builtbroken.mc.core.registry.implement.IRecipeContainer;
import com.builtbroken.mc.lib.helper.recipe.UniversalRecipe;
import com.builtbroken.mc.lib.transform.vector.Pos;
import com.builtbroken.mc.prefab.gui.ContainerDummy;
import com.builtbroken.mc.prefab.items.ItemBlockBase;
import com.builtbroken.mc.prefab.tile.Tile;
import com.builtbroken.mc.prefab.tile.module.TileModuleInventory;
import com.builtbroken.mc.prefab.tile.multiblock.EnumMultiblock;
import com.builtbroken.mc.prefab.tile.multiblock.MultiBlockHelper;
import icbm.classic.ICBMClassic;
import icbm.classic.content.explosive.blast.BlastEMP;
import icbm.classic.prefab.TileICBMMachine;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.oredict.ShapedOreRecipe;

import java.util.HashMap;
import java.util.List;

public class TileEMPTower extends TileICBMMachine implements IMultiTileHost, IPacketIDReceiver, IRecipeContainer, IGuiTile
{
    // The maximum possible radius for the EMP to strike
    public static final int MAX_RADIUS = 150;

    public static HashMap<IPos3D, String> tileMapCache = new HashMap();

    static
    {
        tileMapCache.put(new Pos(0, 1, 0), EnumMultiblock.TILE.getTileName());
    }

    public float rotation = 0;
    private float rotationDelta;

    // The EMP mode. 0 = All, 1 = Missiles Only, 2 = Electricity Only
    public byte empMode = 0;

    private int cooldownTicks = 0;

    // The EMP explosion radius
    public int empRadius = 60;

    private boolean _destroyingStructure = false;

    public TileEMPTower()
    {
        super("empTower", Material.iron);
        this.itemBlock = ItemBlockBase.class;
        this.hardness = 10f;
        this.resistance = 10f;
        this.isOpaque = false;
    }

    @Override
    protected IInventory createInventory()
    {
        return new TileModuleInventory(this, 2);
    }

    @Override
    public Tile newTile()
    {
        return new TileEMPTower();
    }

    @Override
    public void update()
    {
        super.update();

        if (!isReady())
        {
            cooldownTicks--;
        }
        else if (isIndirectlyPowered())
        {
            fire();
        }

        if (ticks % 20 == 0 && getEnergyStored(ForgeDirection.UNKNOWN) > 0)
        {
            worldObj.playSoundEffect(xCoord, yCoord, zCoord, ICBMClassic.PREFIX + "machinehum", 0.5F, 0.85F * getEnergyStored(ForgeDirection.UNKNOWN) / getMaxEnergyStored(ForgeDirection.UNKNOWN));
            sendDescPacket();
        }

        rotationDelta = (float) (Math.pow(getEnergyStored(ForgeDirection.UNKNOWN) / getMaxEnergyStored(ForgeDirection.UNKNOWN), 2) * 0.5);
        rotation += rotationDelta;
        if (rotation > 360)
        {
            rotation = 0;
        }
    }

    @Override
    public boolean read(ByteBuf data, int id, EntityPlayer player, PacketType type)
    {
        if (!super.read(data, id, player, type))
        {
            switch (id)
            {
                case 0:
                {
                    energy = data.readInt();
                    empRadius = data.readInt();
                    empMode = data.readByte();
                    return true;
                }
                case 1:
                {
                    empRadius = data.readInt();
                    return true;
                }
                case 2:
                {
                    empMode = data.readByte();
                    return true;
                }
            }
            return false;
        }
        return true;
    }

    @Override
    public int getMaxEnergyStored(ForgeDirection from)
    {
        return Math.max(3000000 * (this.empRadius / MAX_RADIUS), 1000000);
    }

    @Override
    public PacketTile getDescPacket()
    {
        return new PacketTile(this, 0, getEnergyStored(ForgeDirection.UNKNOWN), this.empRadius, this.empMode);
    }

    /** Reads a tile entity from NBT. */
    @Override
    public void readFromNBT(NBTTagCompound par1NBTTagCompound)
    {
        super.readFromNBT(par1NBTTagCompound);

        this.empRadius = par1NBTTagCompound.getInteger("empRadius");
        this.empMode = par1NBTTagCompound.getByte("empMode");
    }

    /** Writes a tile entity to NBT. */
    @Override
    public void writeToNBT(NBTTagCompound par1NBTTagCompound)
    {
        super.writeToNBT(par1NBTTagCompound);

        par1NBTTagCompound.setInteger("empRadius", this.empRadius);
        par1NBTTagCompound.setByte("empMode", this.empMode);
    }

    //@Callback(limit = 1)
    public boolean fire()
    {
        if (this.checkExtract())
        {
            if (isReady())
            {
                switch (this.empMode)
                {
                    default:
                        new BlastEMP(this.worldObj, null, this.xCoord + 0.5, this.yCoord + 1.2, this.zCoord + 0.5, this.empRadius).setEffectBlocks().setEffectEntities().explode();
                        break;
                    case 1:
                        new BlastEMP(this.worldObj, null, this.xCoord + 0.5, this.yCoord + 1.2, this.zCoord + 0.5, this.empRadius).setEffectEntities().explode();
                        break;
                    case 2:
                        new BlastEMP(this.worldObj, null, this.xCoord + 0.5, this.yCoord + 1.2, this.zCoord + 0.5, this.empRadius).setEffectBlocks().explode();
                        break;
                }
                this.extractEnergy();
                this.cooldownTicks = getMaxCooldown();
                return true;
            }
        }
        return false;
    }

    @Override
    protected boolean onPlayerRightClick(EntityPlayer player, int side, Pos hit)
    {
        if (isServer())
        {
            openGui(player, ICBMClassic.INSTANCE);
        }
        return true;
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox()
    {
        return INFINITE_EXTENT_AABB;
    }

    //@Callback
    public boolean isReady()
    {
        return getCooldown() <= 0;
    }

    //@Callback
    public int getCooldown()
    {
        return cooldownTicks;
    }

    //@Callback
    public int getMaxCooldown()
    {
        return 120;
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
            if (tileMapCache.containsKey(new Pos((TileEntity)this).sub(new Pos((TileEntity) tileMulti))))
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
            Pos pos = new Pos((TileEntity) tileMulti).sub(new Pos((TileEntity)this));

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
        return super.canPlaceBlockAt() && world().getBlock(xi(), yi() + 1, zi()).isReplaceable(world(), xi(), yi() + 1, zi());
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
    public boolean onMultiTileActivated(IMultiTile tile, EntityPlayer player, int side, IPos3D hit)
    {
        return this.onPlayerRightClick(player, side, new Pos(hit));
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
        recipes.add(new ShapedOreRecipe(new ItemStack(ICBMClassic.blockEmpTower, 1, 0),
                "?W?", "@!@", "?#?",
                '?', UniversalRecipe.PRIMARY_PLATE.get(),
                '!', UniversalRecipe.CIRCUIT_T3.get(),
                '@', UniversalRecipe.BATTERY_BOX.get(),
                '#', UniversalRecipe.MOTOR.get(),
                'W', UniversalRecipe.WIRE.get()));
    }

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player)
    {
        return new ContainerDummy();
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player)
    {
        return null;
    }
}
