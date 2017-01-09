package icbm.classic.content.machines.launcher;

import com.builtbroken.mc.core.network.IPacketIDReceiver;
import com.builtbroken.mc.core.network.packet.PacketTile;
import com.builtbroken.mc.core.network.packet.PacketType;
import com.builtbroken.mc.core.registry.implement.IRecipeContainer;
import com.builtbroken.mc.lib.helper.LanguageUtility;
import com.builtbroken.mc.lib.helper.recipe.UniversalRecipe;
import com.builtbroken.mc.lib.transform.vector.Pos;
import com.builtbroken.mc.prefab.tile.Tile;
import icbm.classic.ICBMClassic;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.oredict.ShapedOreRecipe;
import resonant.api.ITier;
import resonant.api.explosion.ILauncherController;
import resonant.api.explosion.IMissile;
import resonant.api.explosion.LauncherType;

import java.util.List;

/**
 * This tile entity is for the screen of the missile launcher
 *
 * @author Calclavia
 */
public class TileLauncherScreen extends TileLauncherPrefab implements ITier, IPacketIDReceiver, ILauncherController, IRecipeContainer
{

    // The tier of this screen
    private int tier = 0;

    // The missile launcher base in which this
    // screen is connected with
    public TileLauncherBase laucherBase = null;

    public short gaoDu = 3;

    public TileLauncherScreen()
    {
        super("launcherScreen", Material.iron);
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

        if (this.laucherBase == null)
        {
            for (byte i = 2; i < 6; i++)
            {
                Pos position = new Pos(this.xCoord, this.yCoord, this.zCoord).add(ForgeDirection.getOrientation(i));

                TileEntity tileEntity = this.worldObj.getTileEntity(position.xi(), position.yi(), position.zi());

                if (tileEntity != null)
                {
                    if (tileEntity instanceof TileLauncherBase)
                    {
                        this.laucherBase = (TileLauncherBase) tileEntity;
                        this.setFacing(ForgeDirection.getOrientation(i));
                    }
                }
            }
        }
        else
        {
            if (this.laucherBase.isInvalid())
            {
                this.laucherBase = null;
            }
        }

        if (this.ticks % 100 == 0 && this.worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord))
        {
            this.launch();
        }

        if (!this.worldObj.isRemote)
        {
            if (this.ticks % 3 == 0)
            {
                if (this.targetPos == null)
                {
                    this.targetPos = new Pos(this.xCoord, 0, this.zCoord);
                }
            }

            if (this.ticks % 600 == 0)
            {
                this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
            }
        }
    }

    @Override
    public PacketTile getDescPacket()
    {
        return new PacketTile(this, 0, this.getDirection().ordinal(), this.tier, this.getFrequency(), this.gaoDu);
    }


    public PacketTile getGUIPacket()
    {
        return new PacketTile(this, 4, this.getEnergyStored(ForgeDirection.UNKNOWN), this.targetPos.xi(), this.targetPos.yi(), this.targetPos.zi());
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
                    this.setFacing(ForgeDirection.getOrientation(data.readByte()));
                    this.tier = data.readInt();
                    this.setFrequency(data.readInt());
                    this.gaoDu = data.readShort();
                    return true;
                }
                case 1:
                {
                    this.setFrequency(data.readInt());
                    return true;
                }
                case 2:
                {
                    this.targetPos = new Pos(data.readInt(), data.readInt(), data.readInt());
                    return true;
                }
                case 3:
                {
                    this.gaoDu = (short) Math.max(Math.min(data.readShort(), Short.MAX_VALUE), 3);
                    return true;
                }
                case 4:
                {
                    this.energy = data.readInt();
                    this.targetPos = new Pos(data.readInt(), data.readInt(), data.readInt());
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
        if (this.laucherBase != null && this.laucherBase.missile != null)
        {
            if (this.checkExtract())
            {
                return this.laucherBase.isInRange(this.targetPos);
            }
        }
        return false;
    }

    /** Calls the missile launcher base to launch it's missile towards a targeted location */
    @Override
    public void launch()
    {
        if (this.canLaunch())
        {
            this.extractEnergy();
            this.laucherBase.launchMissile(this.targetPos.clone(), this.gaoDu);
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
        else if (this.laucherBase.missile == null)
        {
            status = LanguageUtility.getLocal("gui.launcherScreen.statusEmpty");
        }
        else if (this.targetPos == null)
        {
            status = LanguageUtility.getLocal("gui.launcherScreen.statusInvalid");
        }
        else if (this.laucherBase.shiTaiJin(this.targetPos))
        {
            status = LanguageUtility.getLocal("gui.launcherScreen.statusClose");
        }
        else if (this.laucherBase.shiTaiYuan(this.targetPos))
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
        this.gaoDu = par1NBTTagCompound.getShort("gaoDu");
    }

    /** Writes a tile entity to NBT. */
    @Override
    public void writeToNBT(NBTTagCompound par1NBTTagCompound)
    {
        super.writeToNBT(par1NBTTagCompound);

        par1NBTTagCompound.setInteger("tier", this.tier);
        par1NBTTagCompound.setShort("gaoDu", this.gaoDu);
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
    public int getMaxEnergyStored(ForgeDirection from)
    {
        return getEnergyConsumption() * 2;
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
    public LauncherType getLauncherType()
    {
        return LauncherType.TRADITIONAL;
    }

    @Override
    public IMissile getMissile()
    {
        if (this.laucherBase != null)
        {
            return this.laucherBase.getContainingMissile();
        }

        return null;
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
        this.tier = itemStack.stackSize;
    }
}
