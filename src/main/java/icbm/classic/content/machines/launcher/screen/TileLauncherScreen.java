package icbm.classic.content.machines.launcher.screen;

import icbm.classic.api.energy.IEnergyBufferProvider;
import icbm.classic.api.tile.IRadioWaveSender;
import icbm.classic.config.ConfigLauncher;
import icbm.classic.content.machines.launcher.TileLauncherPrefab;
import icbm.classic.content.machines.launcher.base.TileLauncherBase;
import icbm.classic.lib.LanguageUtility;
import icbm.classic.lib.network.IPacket;
import icbm.classic.lib.network.IPacketIDReceiver;
import icbm.classic.lib.network.packet.PacketTile;
import icbm.classic.lib.transform.vector.Pos;
import icbm.classic.prefab.FakeRadioSender;
import icbm.classic.prefab.inventory.ExternalInventory;
import icbm.classic.prefab.inventory.IInventoryProvider;
import icbm.classic.prefab.tile.EnumTier;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import resonant.api.explosion.ILauncherController;
import resonant.api.explosion.LauncherType;

/**
 * This tile entity is for the screen of the missile launcher
 *
 * @author Calclavia
 */
public class TileLauncherScreen extends TileLauncherPrefab implements IPacketIDReceiver, ILauncherController, IEnergyBufferProvider, IInventoryProvider<ExternalInventory>
{
    // The missile launcher base in which this
    // screen is connected with
    public TileLauncherBase launcherBase = null;

    /** Height to wait before missile curves */
    public short lockHeight = 3;

    public ExternalInventory inventory;

    @Override
    public ExternalInventory getInventory()
    {
        if (inventory == null)
        {
            inventory = new ExternalInventory(this, 2); //TODO figure out what these 2 slots did
        }
        return inventory;
    }

    @Override
    public void update()
    {
        super.update();
        if (this.launcherBase == null || this.launcherBase.isInvalid())
        {
            this.launcherBase = null;
            for (EnumFacing rotation : EnumFacing.HORIZONTALS)
            {
                final Pos position = toPos().add(rotation);
                final TileEntity tileEntity = position.getTileEntity(world);
                if (tileEntity != null)
                {
                    if (tileEntity instanceof TileLauncherBase)
                    {
                        this.launcherBase = (TileLauncherBase) tileEntity;
                        if (isServer())
                        {
                            setRotation(rotation.getOpposite());
                            updateClient = true;
                        }
                    }
                }
            }
        }
        if (isServer())
        {
            if (this.ticks % 100 == 0 && world.isBlockIndirectlyGettingPowered(getPos()) > 0)
            {
                this.launch();
            }
            if (ticks % 3 == 0)
            {
                sendDescPacket();
            }
        }
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newSate)
    {
        return false;
    }

    @Override
    public PacketTile getDescPacket()
    {
        return new PacketTile("desc", 0, this).addData(getEnergy(), this.getFrequency(), this.lockHeight, this.getTarget().xi(), this.getTarget().yi(), this.getTarget().zi());
    }

    @Override
    public PacketTile getGUIPacket()
    {
        return getDescPacket();
    }

    @Override
    public void placeMissile(ItemStack itemStack)
    {
        if (this.launcherBase != null)
        {
            if (!this.launcherBase.isInvalid())
            {
                this.launcherBase.getInventory().setInventorySlotContents(0, itemStack);
            }
        }
    }

    @Override
    public boolean read(ByteBuf data, int id, EntityPlayer player, IPacket packet)
    {
        if (!super.read(data, id, player, packet))
        {
            switch (id)
            {
                case 0:
                {
                    if (isClient())
                    {
                        //this.tier = data.readInt();
                        setEnergy(data.readInt());
                        this.setFrequency(data.readInt());
                        this.lockHeight = data.readShort();
                        this.setTarget(new Pos(data.readInt(), data.readInt(), data.readInt()));
                        return true;
                    }
                }
                case 1:
                {
                    this.setFrequency(data.readInt());
                    return true;
                }
                case 2:
                {
                    this.setTarget(new Pos(data.readInt(), data.readInt(), data.readInt()));
                    return true;
                }
                case 3:
                {
                    this.lockHeight = (short) Math.max(Math.min(data.readShort(), Short.MAX_VALUE), 3);
                    return true;
                }
            }
            return false;
        }
        return true;
    }

    // Checks if the missile is launchable
    @Override
    public boolean canLaunch()
    {
        if (this.launcherBase != null && this.launcherBase.getMissileStack() != null)
        {
            if (this.checkExtract())
            {
                return this.launcherBase.isInRange(this.getTarget());
            }
        }
        return false;
    }

    /** Calls the missile launcher base to launch it's missile towards a targeted location */
    @Override
    public void launch()
    {
        if (this.canLaunch() && this.launcherBase.launchMissile(this.getTarget(), this.lockHeight))
        {
            this.extractEnergy();
            updateClient = true;
        }
    }

    /**
     * Gets the display status of the missile launcher
     *
     * @return The string to be displayed
     */
    @Override
    public String getStatus()
    {
        String color = "\u00a74";
        String status = LanguageUtility.getLocal("gui.misc.idle");

        if (this.launcherBase == null)
        {
            status = LanguageUtility.getLocal("gui.launcherscreen.statusMissing");
        }
        else if (!checkExtract())
        {
            status = LanguageUtility.getLocal("gui.launcherscreen.statusNoPower");
        }
        else if (this.launcherBase.getMissileStack().isEmpty())
        {
            status = LanguageUtility.getLocal("gui.launcherscreen.statusEmpty");
        }
        else if (this.getTarget() == null)
        {
            status = LanguageUtility.getLocal("gui.launcherscreen.statusInvalid");
        }
        else if (this.launcherBase.isTargetTooClose(this.getTarget()))
        {
            status = LanguageUtility.getLocal("gui.launcherscreen.statusClose");
        }
        else if (this.launcherBase.isTargetTooFar(this.getTarget()))
        {
            status = LanguageUtility.getLocal("gui.launcherscreen.statusFar");
        }
        else
        {
            color = "\u00a72";
            status = LanguageUtility.getLocal("gui.launcherscreen.statusReady");
        }

        return color + status;
    }

    /** Reads a tile entity from NBT. */
    @Override
    public void readFromNBT(NBTTagCompound par1NBTTagCompound)
    {
        super.readFromNBT(par1NBTTagCompound);
        //this.tier = par1NBTTagCompound.getInteger("tier");
        this.lockHeight = par1NBTTagCompound.getShort("targetHeight");
    }

    /** Writes a tile entity to NBT. */
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound par1NBTTagCompound)
    {
        //par1NBTTagCompound.setInteger("tier", this.tier);
        par1NBTTagCompound.setShort("targetHeight", this.lockHeight);
        return super.writeToNBT(par1NBTTagCompound);
    }

    @Override
    public int getEnergyConsumption()
    {
        switch (this.getTier())
        {
            case ONE:
                return ConfigLauncher.LAUNCHER_POWER_USAGE_TIER1;
            case TWO:
                return ConfigLauncher.LAUNCHER_POWER_USAGE_TIER2;
        }
        return ConfigLauncher.LAUNCHER_POWER_USAGE_TIER3;
    }

    @Override
    public int getEnergyBufferSize()
    {
        switch (this.getTier())
        {
            case ONE:
                return ConfigLauncher.LAUNCHER_POWER_CAP_TIER1;
            case TWO:
                return ConfigLauncher.LAUNCHER_POWER_CAP_TIER2;
        }
        return ConfigLauncher.LAUNCHER_POWER_CAP_TIER3;
    }

    @Override
    public LauncherType getLauncherType()
    {
        return LauncherType.TRADITIONAL;
    }

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player)
    {
        return new ContainerLaunchScreen(player, this);
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player)
    {
        return new GuiLauncherScreen(player, this);
    }

    @Override
    public void receiveRadioWave(float hz, IRadioWaveSender sender, String messageHeader, Object[] data)
    {
        if (isServer())
        {
            //Floor frequency as we do not care about sub ranges
            int frequency = (int) Math.floor(hz);
            //Only tier 3 (2 for tier value) can be remotely fired
            if (getTier() == EnumTier.THREE && frequency == getFrequency() && launcherBase != null)
            {
                //Laser detonator signal
                if (messageHeader.equals("activateLauncherWithTarget"))
                {
                    Pos pos = (Pos) data[0];
                    if (toPos().distance(pos) < this.launcherBase.getRange())
                    {
                        setTarget(pos);
                        launch();
                        ((FakeRadioSender) sender).player.sendMessage(new TextComponentString("Firing missile at " + pos));
                    }
                }
                //Remote detonator signal
                else if (messageHeader.equals("activateLauncher"))
                {
                    ((FakeRadioSender) sender).player.sendMessage(new TextComponentString("Firing missile at " + getTarget()));
                    launch();
                }
            }
        }
    }
}
