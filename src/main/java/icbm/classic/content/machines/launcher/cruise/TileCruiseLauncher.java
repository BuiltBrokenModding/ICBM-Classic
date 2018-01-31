package icbm.classic.content.machines.launcher.cruise;

import com.builtbroken.mc.api.data.IPacket;
import com.builtbroken.mc.api.tile.access.IGuiTile;
import com.builtbroken.mc.api.tile.provider.IInventoryProvider;
import com.builtbroken.mc.core.Engine;
import com.builtbroken.mc.core.network.IPacketIDReceiver;
import com.builtbroken.mc.core.network.packet.PacketSpawnParticleStream;
import com.builtbroken.mc.core.network.packet.PacketTile;
import com.builtbroken.mc.data.Direction;
import com.builtbroken.mc.imp.transform.region.Cube;
import com.builtbroken.mc.imp.transform.rotation.EulerAngle;
import com.builtbroken.mc.imp.transform.vector.Pos;
import com.builtbroken.mc.lib.helper.LanguageUtility;
import com.builtbroken.mc.prefab.inventory.ExternalInventory;
import icbm.classic.ICBMClassic;
import icbm.classic.content.entity.EntityMissile;
import icbm.classic.content.explosive.Explosives;
import icbm.classic.content.explosive.ex.Explosion;
import icbm.classic.content.items.ItemMissile;
import icbm.classic.content.machines.launcher.TileLauncherPrefab;
import icbm.classic.prefab.EnumTier;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import resonant.api.explosion.ILauncherContainer;
import resonant.api.explosion.ILauncherController;
import resonant.api.explosion.LauncherType;

public class TileCruiseLauncher extends TileLauncherPrefab implements IPacketIDReceiver, ILauncherController, ILauncherContainer, IGuiTile, IInventoryProvider<ExternalInventory>
{
    /** Desired aim angle, updated every tick if target != null */
    protected final EulerAngle aim = new EulerAngle(0, 0, 0);
    /** Current aim angle, updated each tick */
    protected final EulerAngle currentAim = new EulerAngle(0, 0, 0);

    protected static double ROTATION_SPEED = 10.0;

    /** Last time rotation was updated, used in {@link EulerAngle#lerp(EulerAngle, double)} function for smooth rotation */
    protected long lastRotationUpdate = System.nanoTime();
    /** Percent of time that passed since last tick, should be 1.0 on a stable server */
    protected double deltaTime;

    ExternalInventory inventory;


    protected ItemStack cachedMissileStack;

    @Override
    public ExternalInventory getInventory()
    {
        if (inventory == null)
        {
            inventory = new ExternalInventory(this, 2);
        }
        return inventory;
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
        String status;

        if (!this.checkExtract())
        {
            status = LanguageUtility.getLocal("gui.launcherCruise.statusNoPower");
        }
        else if (this.getInventory().getStackInSlot(0) == null)
        {
            status = LanguageUtility.getLocal("gui.launcherCruise.statusEmpty");
        }
        else if (this.getInventory().getStackInSlot(0).getItem() != ICBMClassic.itemMissile)
        {
            status = LanguageUtility.getLocal("gui.launcherCruise.invalidMissile");
        }
        else
        {
            final Explosion missile = (Explosion) Explosives.get(this.getInventory().getStackInSlot(0).getItemDamage()).handler;
            if (missile == null)
            {
                status = LanguageUtility.getLocal("gui.launcherCruise.invalidMissile");
            }
            else if (!missile.isCruise())
            {
                status = LanguageUtility.getLocal("gui.launcherCruise.notCruiseMissile");
            }
            else if (missile.getTier().ordinal() > 3)
            {
                status = LanguageUtility.getLocal("gui.launcherCruise.invalidMissileTier");
            }
            else if (this.getTarget() == null || getTarget().isZero())
            {
                status = LanguageUtility.getLocal("gui.launcherCruise.statusInvalid");
            }
            else if (this.isTooClose(getTarget()))
            {
                status = LanguageUtility.getLocal("gui.launcherCruise.targetToClose");
            }
            else if (!canSpawnMissileWithNoCollision())
            {
                status = LanguageUtility.getLocal("gui.launcherCruise.noRoom");
            }
            else
            {
                color = "\u00a72";
                status = LanguageUtility.getLocal("gui.launcherCruise.statusReady");
            }
        }
        return color + status;
    }

    @Override
    public void update()
    {
        super.update();

        deltaTime = (System.nanoTime() - lastRotationUpdate) / 100000000.0; // time / time_tick, client uses different value
        lastRotationUpdate = System.nanoTime();

        //this.discharge(this.containingItems[1]);

        if (getTarget() != null && !getTarget().isZero())
        {
            Pos aimPoint = getTarget();
            Pos center = toPos().add(0.5);
            if (Engine.runningAsDev)
            {
                Engine.packetHandler.sendToAllAround(new PacketSpawnParticleStream(world.provider.getDimension(), center, aimPoint), this);
            }
            aim.set(center.toEulerAngle(aimPoint).clampTo360());

            currentAim.moveTowards(aim, ROTATION_SPEED, deltaTime).clampTo360();

            if (isServer())
            {
                if (this.ticks % 40 == 0 && this.world.isBlockIndirectlyGettingPowered(getPos()) > 0)
                {
                    this.launch();
                }
            }
        }
    }

    @Override
    public void placeMissile(ItemStack itemStack)
    {
        getInventory().setInventorySlotContents(0, itemStack);
    }

    @Override
    public PacketTile getGUIPacket()
    {
        return new PacketTile("gui", 0, this).addData(getEnergy(), this.getFrequency(), this.getTarget().xi(), this.getTarget().yi(), this.getTarget().zi());
    }

    @Override
    public boolean read(ByteBuf data, int id, EntityPlayer player, IPacket type)
    {
        if (!super.read(data, id, player, type))
        {
            if (isServer())
            {
                switch (id)
                {
                    //set frequency packet from GUI
                    case 1:
                    {
                        this.setFrequency(data.readInt());
                        return true;
                    }
                    //Set target packet from GUI
                    case 2:
                    {
                        this.setTarget(new Pos(data.readInt(), data.readInt(), data.readInt()));
                        return true;
                    }
                }
            }
            else
            {
                switch (id)
                {
                    //GUI description packet
                    case 0:
                    {
                        setEnergy(data.readInt());
                        this.setFrequency(data.readInt());
                        this.setTarget(new Pos(data.readInt(), data.readInt(), data.readInt()));
                        return true;
                    }
                }
            }
            return false;
        }
        return true;
    }

    @Override
    public void writeDescPacket(ByteBuf buf)
    {
        super.writeDescPacket(buf);
        buf.writeBoolean(getInventory().getStackInSlot(0) != null);
        if (getInventory().getStackInSlot(0) != null)
        {
            ByteBufUtils.writeItemStack(buf, getInventory().getStackInSlot(0));
        }
        buf.writeInt(getTarget().xi());
        buf.writeInt(getTarget().yi());
        buf.writeInt(getTarget().zi());
    }

    @Override
    public void readDescPacket(ByteBuf buf)
    {
        super.readDescPacket(buf);
        if (buf.readBoolean())
        {
            cachedMissileStack = ByteBufUtils.readItemStack(buf);
        }
        else
        {
            cachedMissileStack = null;
        }
        setTarget(new Pos(buf.readInt(), buf.readInt(), buf.readInt()));
    }

    @Override
    public boolean canLaunch()
    {
        if (getTarget() != null && !getTarget().isZero())
        {
            //Validate if we have an item and a target
            if (this.getInventory().getStackInSlot(0) != null && this.getInventory().getStackInSlot(0).getItem() == ICBMClassic.itemMissile)
            {
                //Validate that the item in the slot is a missile we can fire
                final Explosion missile = (Explosion) Explosives.get(this.getInventory().getStackInSlot(0).getItemDamage()).handler;
                if (missile != null && missile.isCruise() && missile.getTier().ordinal() <= 3)
                {
                    //Make sure we have enough energy
                    if (this.checkExtract())
                    {
                        //Make sure we are not going to blow ourselves up
                        //TODO check range by missile type
                        if (!this.isTooClose(this.getTarget()))
                        {
                            //Make sure we can safely spawn the missile
                            return canSpawnMissileWithNoCollision();
                        }
                    }
                }
            }
        }
        return false;
    }

    protected boolean canSpawnMissileWithNoCollision()
    {
        //Make sure there is noting above us to hit when spawning the missile
        for (int x = -1; x < 2; x++)
        {
            for (int z = -1; z < 2; z++)
            {
                BlockPos pos = getPos().add(x, 1, z);
                IBlockState state = world.getBlockState(pos);
                Block block = state.getBlock();
                if (!block.isAir(state, world, pos))
                {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public LauncherType getLauncherType()
    {
        return LauncherType.CRUISE;
    }

    /**
     * Launches the missile
     */
    @Override
    public void launch()
    {
        if (this.canLaunch())
        {
            this.extractEnergy();

            EntityMissile entityMissile = new EntityMissile(world, xi() + 0.5, yi() + 1.5, zi() + 0.5, -(float) currentAim.yaw() - 180, -(float) currentAim.pitch(), 2);
            entityMissile.missileType = EntityMissile.MissileType.CruiseMissile;
            entityMissile.explosiveID = Explosives.get(this.getInventory().getStackInSlot(0).getItemDamage());
            entityMissile.acceleration = 1;
            entityMissile.launch(null);
            world.spawnEntity(entityMissile);
            //Clear slot last so we can still access data as needed or roll back changes if a crash happens
            this.getInventory().decrStackSize(0, 1);
        }
    }

    // Is the target too close?
    public boolean isTooClose(Pos target)
    {
        return new Pos(getPos()).add(0.5).distance(new Pos(target.x() + .5, target.z() + .5, target.z() + .5)) < 20;
    }

    @Override
    public void onInventoryChanged(int slot, ItemStack prev, ItemStack item)
    {
        if (slot == 0)
        {
            updateClient = true;
        }
    }

    @Override
    public boolean targetWithYValue()
    {
        return true;
    }

    @Override
    public boolean canStore(ItemStack itemStack, Direction side)
    {
        if (itemStack != null && itemStack.getItem() instanceof ItemMissile && this.getInventory().getStackInSlot(0) == null)
        {
            if (Explosives.get(itemStack.getItemDamage()).handler instanceof Explosion)
            {
                Explosion missile = (Explosion) Explosives.get(itemStack.getItemDamage()).handler;

                if (missile.isCruise() && missile.getTier().ordinal() <= EnumTier.THREE.ordinal())
                {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public ILauncherController getController()
    {
        return this;
    }


    @Override
    public AxisAlignedBB getRenderBoundingBox()
    {
        return new Cube(-2, 0, -2, 2, 3, 2).add(toPos()).toAABB();
    }

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player)
    {
        return new ContainerCruiseLauncher(player, this);
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player)
    {
        return new GuiCruiseLauncher(player, this);
    }
}
