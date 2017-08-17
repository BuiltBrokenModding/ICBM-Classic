package icbm.classic.content.machines.launcher.screen;

import com.builtbroken.mc.api.IWorldPosition;
import com.builtbroken.mc.api.items.tools.IWorldPosItem;
import com.builtbroken.mc.api.map.radio.IRadioWaveSender;
import com.builtbroken.mc.api.tile.access.IGuiTile;
import com.builtbroken.mc.core.network.IPacketIDReceiver;
import com.builtbroken.mc.core.network.packet.PacketTile;
import com.builtbroken.mc.core.network.packet.PacketType;
import com.builtbroken.mc.core.registry.implement.IRecipeContainer;
import com.builtbroken.mc.imp.transform.vector.Pos;
import com.builtbroken.mc.lib.helper.LanguageUtility;
import com.builtbroken.mc.lib.helper.recipe.UniversalRecipe;
import com.builtbroken.mc.prefab.gui.ContainerDummy;
import com.builtbroken.mc.prefab.hz.FakeRadioSender;
import com.builtbroken.mc.prefab.items.ItemBlockSubTypes;
import com.builtbroken.mc.prefab.tile.Tile;
import com.builtbroken.mc.prefab.tile.module.TileModuleInventory;
import icbm.classic.ICBMClassic;
import icbm.classic.content.items.ItemRemoteDetonator;
import icbm.classic.content.machines.launcher.TileLauncherPrefab;
import icbm.classic.content.machines.launcher.base.TileLauncherBase;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.oredict.ShapedOreRecipe;
import resonant.api.ITier;
import resonant.api.explosion.ILauncherController;
import resonant.api.explosion.LauncherType;

import java.util.List;

/**
 * This tile entity is for the screen of the missile launcher
 *
 * @author Calclavia
 */
public class TileLauncherScreen extends TileLauncherPrefab implements ITier, IPacketIDReceiver, ILauncherController, IRecipeContainer, IGuiTile
{
    // The tier of this screen
    private int tier = 0;

    // The missile launcher base in which this
    // screen is connected with
    public TileLauncherBase laucherBase = null;

    /** Detonation height of the missile. */
    public short targetHeight = 3;

    public TileLauncherScreen()
    {
        super("launcherScreen", Material.iron);
        this.itemBlock = ItemBlockSubTypes.class;
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
        return new TileLauncherScreen();
    }

    @Override
    public void update()
    {
        super.update();
        if (this.laucherBase == null || this.laucherBase.isInvalid())
        {
            this.laucherBase = null;
            for (byte i = 2; i < 6; i++)
            {
                final Pos position = toPos().add(ForgeDirection.getOrientation(i));
                final TileEntity tileEntity = position.getTileEntity(oldWorld());
                if (tileEntity != null)
                {
                    if (tileEntity instanceof TileLauncherBase)
                    {
                        this.laucherBase = (TileLauncherBase) tileEntity;
                        if (isServer())
                        {
                            this.setFacing(ForgeDirection.getOrientation(i).getOpposite());
                            updateClient = true;
                        }
                    }
                }
            }
        }
        if (isServer())
        {
            if (this.ticks % 100 == 0 && this.isIndirectlyPowered())
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
    public boolean onPlayerActivated(EntityPlayer player, int side, Pos hit)
    {
        if (isServer())
        {
            boolean notNull = player.getHeldItem() != null;
            if (notNull && player.getHeldItem().getItem() == Items.redstone)
            {
                if (canLaunch())
                {
                    launch();
                }
                else
                {
                    player.addChatComponentMessage(new ChatComponentText(LanguageUtility.getLocal("chat.launcher.failedToFire")));
                    String translation = LanguageUtility.getLocal("chat.launcher.status");
                    translation = translation.replace("%1", getStatus());
                    player.addChatComponentMessage(new ChatComponentText(translation));
                }
            }
            else if (notNull && player.getHeldItem().getItem() instanceof ItemRemoteDetonator)
            {
                ((ItemRemoteDetonator) player.getHeldItem().getItem()).setBroadCastHz(player.getHeldItem(), getFrequency());
                player.addChatComponentMessage(new ChatComponentText(LanguageUtility.getLocal("chat.launcher.toolFrequencySet").replace("%1", "" + getFrequency())));
            }
            else if (notNull && player.getHeldItem().getItem() instanceof IWorldPosItem)
            {
                IWorldPosition location = ((IWorldPosItem) player.getHeldItem().getItem()).getLocation(player.getHeldItem());
                if (location != null)
                {
                    if (location.oldWorld() == oldWorld())
                    {
                        setTarget(new Pos(location.x(), location.y(), location.z()));
                        player.addChatComponentMessage(new ChatComponentText(LanguageUtility.getLocal("chat.launcher.toolTargetSet")));
                    }
                    else
                    {
                        player.addChatComponentMessage(new ChatComponentText(LanguageUtility.getLocal("chat.launcher.toolWorldNotMatch")));
                    }
                }
                else
                {
                    player.addChatComponentMessage(new ChatComponentText(LanguageUtility.getLocal("chat.launcher.noTargetInTool")));
                }
            }
            else
            {
                player.openGui(ICBMClassic.INSTANCE, 0, this.worldObj, this.xCoord, this.yCoord, this.zCoord);
            }
        }
        return true;
    }

    @Override
    public PacketTile getDescPacket()
    {
        return new PacketTile(this, 0, this.tier, getEnergy(), this.getFrequency(), this.targetHeight, this.getTarget().xi(), this.getTarget().yi(), this.getTarget().zi());
    }

    @Override
    public PacketTile getGUIPacket()
    {
        return new PacketTile(this, 4, getEnergy(), this.getTarget().xi(), this.getTarget().yi(), this.getTarget().zi());
    }

    @Override
    public void placeMissile(ItemStack itemStack)
    {
        if (this.laucherBase != null)
        {
            if (!this.laucherBase.isInvalid())
            {
                this.laucherBase.setInventorySlotContents(0, itemStack);
            }
        }
    }

    @Override
    public boolean read(ByteBuf data, int id, EntityPlayer player, PacketType packet)
    {
        if (!super.read(data, id, player, packet))
        {
            switch (id)
            {
                case 0:
                {
                    this.tier = data.readInt();
                    setEnergy(data.readInt());
                    this.setFrequency(data.readInt());
                    this.targetHeight = data.readShort();
                    this.setTarget(new Pos(data.readInt(), data.readInt(), data.readInt()));
                    return true;
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
                    this.targetHeight = (short) Math.max(Math.min(data.readShort(), Short.MAX_VALUE), 3);
                    return true;
                }
                case 4:
                {
                    setEnergy(data.readInt());
                    this.setTarget(new Pos(data.readInt(), data.readInt(), data.readInt()));
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
        if (this.laucherBase != null && this.laucherBase.getMissileStack() != null)
        {
            if (this.checkExtract())
            {
                return this.laucherBase.isInRange(this.getTarget());
            }
        }
        return false;
    }

    /** Calls the missile launcher base to launch it's missile towards a targeted location */
    @Override
    public void launch()
    {
        if (this.canLaunch() && this.laucherBase.launchMissile(this.getTarget(), this.targetHeight))
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

        if (this.laucherBase == null)
        {
            status = LanguageUtility.getLocal("gui.launcherScreen.statusMissing");
        }
        else if (!checkExtract())
        {
            status = LanguageUtility.getLocal("gui.launcherScreen.statusNoPower");
        }
        else if (this.laucherBase.getMissileStack() == null)
        {
            status = LanguageUtility.getLocal("gui.launcherScreen.statusEmpty");
        }
        else if (this.getTarget() == null)
        {
            status = LanguageUtility.getLocal("gui.launcherScreen.statusInvalid");
        }
        else if (this.laucherBase.isTargetTooClose(this.getTarget()))
        {
            status = LanguageUtility.getLocal("gui.launcherScreen.statusClose");
        }
        else if (this.laucherBase.isTargetTooFar(this.getTarget()))
        {
            status = LanguageUtility.getLocal("gui.launcherScreen.statusFar");
        }
        else
        {
            color = "\u00a72";
            status = LanguageUtility.getLocal("gui.launcherScreen.statusReady");
        }

        return color + status;
    }

    /** Reads a tile entity from NBT. */
    @Override
    public void readFromNBT(NBTTagCompound par1NBTTagCompound)
    {
        super.readFromNBT(par1NBTTagCompound);

        this.tier = par1NBTTagCompound.getInteger("tier");
        this.targetHeight = par1NBTTagCompound.getShort("targetHeight");
    }

    /** Writes a tile entity to NBT. */
    @Override
    public void writeToNBT(NBTTagCompound par1NBTTagCompound)
    {
        super.writeToNBT(par1NBTTagCompound);

        par1NBTTagCompound.setInteger("tier", this.tier);
        par1NBTTagCompound.setShort("targetHeight", this.targetHeight);
    }

    @Override
    public int getTier()
    {
        return this.tier;
    }

    @Override
    public void setTier(int tier)
    {
        this.tier = tier;
        updateClient = true;
    }

    @Override
    public int getEnergyConsumption()
    {
        switch (this.getTier())
        {
            case 0:
                return 50000;
            case 1:
                return 80000;
        }
        return 100000;
    }

    @Override
    public int getEnergyBufferSize()
    {
        return getEnergyConsumption() * 2;
    }

    @Override
    public LauncherType getLauncherType()
    {
        return LauncherType.TRADITIONAL;
    }

    @Override
    public void genRecipes(List<IRecipe> recipes)
    {
        // Missile Launcher Panel
        recipes.add(new ShapedOreRecipe(new ItemStack(ICBMClassic.blockLaunchScreen, 1, 0),
                "!!!", "!#!", "!?!",
                '#', UniversalRecipe.CIRCUIT_T1.get(),
                '!', Blocks.glass,
                '?', UniversalRecipe.WIRE.get()));

        recipes.add(new ShapedOreRecipe(new ItemStack(ICBMClassic.blockLaunchScreen, 1, 1),
                "!$!", "!#!", "!?!",
                '#', UniversalRecipe.CIRCUIT_T2.get(),
                '!', UniversalRecipe.PRIMARY_METAL.get(),
                '?', UniversalRecipe.WIRE.get(),
                '$', new ItemStack(ICBMClassic.blockLaunchScreen, 1, 0)));

        recipes.add(new ShapedOreRecipe(new ItemStack(ICBMClassic.blockLaunchScreen, 1, 2),
                "!$!", "!#!", "!?!",
                '#', UniversalRecipe.CIRCUIT_T3.get(),
                '!', Items.gold_ingot,
                '?', UniversalRecipe.WIRE.get(),
                '$', new ItemStack(ICBMClassic.blockLaunchScreen, 1, 1)));
    }

    @Override
    public void onPlaced(EntityLivingBase entityLiving, ItemStack itemStack)
    {
        super.onPlaced(entityLiving, itemStack);
        setTier(itemStack.getItemDamage());
    }

    @Override
    public void getSubBlocks(Item item, CreativeTabs creativeTabs, List list)
    {
        list.add(new ItemStack(item, 1, 0));
        list.add(new ItemStack(item, 1, 1));
        list.add(new ItemStack(item, 1, 2));
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

    @Override
    public void receiveRadioWave(float hz, IRadioWaveSender sender, String messageHeader, Object[] data)
    {
        //Floor frequency as we do not care about sub ranges
        int frequency = (int) Math.floor(hz);
        //Only tier 3 (2 for tier value) can be remotely fired
        if (getTier() == 2 && frequency == getFrequency() && laucherBase != null)
        {
            //Laser detonator signal
            if (messageHeader.equals("activateLauncherWithTarget"))
            {
                Pos pos = (Pos) data[0];
                if (toPos().distance(pos) < this.laucherBase.getRange())
                {
                    setTarget(pos);
                    launch();
                    ((FakeRadioSender) sender).player.addChatComponentMessage(new ChatComponentText("Firing missile at " + pos));
                }
            }
            //Remote detonator signal
            else if (messageHeader.equals("activateLauncher"))
            {
                ((FakeRadioSender) sender).player.addChatComponentMessage(new ChatComponentText("Firing missile at " + getTarget()));
                launch();
            }
        }
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
