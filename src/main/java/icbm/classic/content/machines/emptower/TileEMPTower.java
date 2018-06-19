package icbm.classic.content.machines.emptower;

import com.builtbroken.jlib.data.vector.IPos3D;
import com.builtbroken.mc.api.computer.DataMethodType;
import com.builtbroken.mc.api.computer.DataSystemMethod;
import com.builtbroken.mc.api.energy.IEnergyBuffer;
import com.builtbroken.mc.api.tile.access.IGuiTile;
import com.builtbroken.mc.api.tile.multiblock.IMultiTile;
import com.builtbroken.mc.api.tile.multiblock.IMultiTileHost;
import com.builtbroken.mc.core.network.IPacketIDReceiver;
import com.builtbroken.mc.core.network.packet.PacketTile;
import com.builtbroken.mc.core.network.packet.PacketType;
import com.builtbroken.mc.core.registry.implement.IRecipeContainer;
import com.builtbroken.mc.framework.multiblock.EnumMultiblock;
import com.builtbroken.mc.framework.multiblock.MultiBlockHelper;
import com.builtbroken.mc.imp.transform.vector.Pos;
import com.builtbroken.mc.lib.helper.recipe.UniversalRecipe;
import com.builtbroken.mc.prefab.gui.ContainerDummy;
import com.builtbroken.mc.prefab.inventory.InventoryUtility;
import com.builtbroken.mc.prefab.items.ItemBlockBase;
import com.builtbroken.mc.prefab.tile.Tile;
import com.builtbroken.mc.prefab.tile.module.TileModuleInventory;
import icbm.classic.ICBMClassic;
import icbm.classic.content.explosive.Explosives;
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
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;

import java.util.HashMap;
import java.util.List;

public class TileEMPTower extends TileICBMMachine implements IMultiTileHost, IPacketIDReceiver, IRecipeContainer, IGuiTile
{
    // The maximum possible radius for the EMP to strike
    public static final int MAX_RADIUS = 150;
    public static final int ENERGY_SCALE_USAGE = 300000;
    public static final int ENERGY_MIN_USAGE = 50000;

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

        if (ticks % 20 == 0 && getEnergy() > 0)
        {
            worldObj.playSoundEffect(xCoord, yCoord, zCoord, ICBMClassic.PREFIX + "machinehum", 0.5F, 0.85F * getChargePercentage());
            sendDescPacket();
        }

        rotationDelta = (float) (Math.pow(getChargePercentage(), 2) * 0.5);
        rotation += rotationDelta;
        if (rotation > 360)
        {
            rotation = 0;
        }
    }

    public boolean isCharged()
    {
        return checkExtract();
    }

    public float getChargePercentage()
    {
        return Math.min(1, getEnergy() / (float)getEnergyConsumption());
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
                    setEnergy(data.readInt());
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
    public int getEnergyBufferSize()
    {
        return ENERGY_SCALE_USAGE * 2;
    }

    @Override
    public int getEnergyConsumption()
    {
        return Math.max(ENERGY_SCALE_USAGE * (this.empRadius / MAX_RADIUS), ENERGY_MIN_USAGE);
    }

    @Override
    public IEnergyBuffer getEnergyBuffer(ForgeDirection side)
    {
        if (energyBuffer == null)
        {
            energyBuffer = new EnergyBufferEMPTower(this);
        }
        return energyBuffer;
    }

    @Override
    public PacketTile getDescPacket()
    {
        return new PacketTile(this, 0, getEnergy(), this.empRadius, this.empMode);
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
    @DataSystemMethod(name = "fireEmp", type = DataMethodType.INVOKE)
    public boolean fire()
    {
        if (this.isReady())
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

    @DataSystemMethod(name = "isEmpReady", type = DataMethodType.GET)
    public boolean isReady()
    {
        return getCooldown() <= 0 && isCharged();
    }

    @DataSystemMethod(name = "empCooldown", type = DataMethodType.GET)
    public int getCooldown()
    {
        return cooldownTicks;
    }

    @DataSystemMethod(name = "empMaxCooldown", type = DataMethodType.GET)
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
        MultiBlockHelper.buildMultiBlock(oldWorld(), this, true, true);
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
        return super.canPlaceBlockAt() && oldWorld().getBlock(xi(), yi() + 1, zi()).isReplaceable(oldWorld(), xi(), yi() + 1, zi());
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
        Object[] items = {"batteryBox",
                InventoryUtility.getItemStack("IC2:blockElectric", 0),
                InventoryUtility.getItemStack("ThermalExpansion:Frame", 5),
                InventoryUtility.getItemStack("Mekanism:EnergyCube", 0)};

        boolean registered = false;
        for (Object object : items)
        {
            if (object != null && (!(object instanceof String) || OreDictionary.doesOreNameExist((String) object)))
            {
                recipes.add(new ShapedOreRecipe(new ItemStack(ICBMClassic.blockEmpTower, 1, 0),
                        "?W?", "@!@", "?#?",
                        '?', UniversalRecipe.PRIMARY_PLATE.get(),
                        '!', UniversalRecipe.CIRCUIT_T3.get(),
                        '@', object,
                        '#', UniversalRecipe.MOTOR.get(),
                        'W', UniversalRecipe.WIRE.get()));
                registered = true;
            }
        }
        if (!registered)
        {
            recipes.add(new ShapedOreRecipe(new ItemStack(ICBMClassic.blockEmpTower, 1, 0),
                    "?W?", "@!@", "?#?",
                    '?', UniversalRecipe.PRIMARY_PLATE.get(),
                    '!', UniversalRecipe.CIRCUIT_T3.get(),
                    '@', Explosives.EMP.getItemStack(),
                    '#', UniversalRecipe.MOTOR.get(),
                    'W', UniversalRecipe.WIRE.get()));
        }
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
