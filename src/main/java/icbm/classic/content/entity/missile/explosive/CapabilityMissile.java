package icbm.classic.content.entity.missile.explosive;

import icbm.classic.ICBMClassic;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.missiles.IMissile;
import icbm.classic.api.explosion.responses.BlastResponse;
import icbm.classic.api.missiles.IMissileFlightLogic;
import icbm.classic.api.missiles.IMissileTarget;
import icbm.classic.client.ICBMSounds;
import icbm.classic.config.ConfigDebug;
import icbm.classic.content.reg.ItemReg;
import icbm.classic.lib.CalculationHelpers;
import icbm.classic.lib.radar.RadarRegistry;
import icbm.classic.lib.saving.NbtSaveHandler;
import icbm.classic.lib.saving.NbtSaveNode;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Optional;

/**
 * Created by Dark(DarkGuardsman, Robert) on 1/7/19.
 */
public class CapabilityMissile implements IMissile, INBTSerializable<NBTTagCompound>
{
    public final EntityExplosiveMissile missile;
    public IMissileTarget targetData;

    private IMissileFlightLogic flightLogic;
    private boolean doFlight = false;

    public CapabilityMissile(EntityExplosiveMissile missile)
    {
        this.missile = missile;
    }

    @Override
    public void dropMissileAsItem()
    {
        final ItemStack stack = toStack();
        if (stack != null && !stack.isEmpty() && world() != null)
        {
            world().spawnEntity(new EntityItem(world(), x(), y(), z(), stack));
        }
        missile.setDead();
    }

    @Override
    public BlastResponse doExplosion()
    {
        return missile.doExplosion();
    }

    @Override
    public boolean hasExploded()
    {
        return missile.isExploding;
    }

    @Override
    public ItemStack toStack()
    {
        return missile.explosive.toStack();
    }

    @Override
    public int getTicksInAir()
    {
        return missile.ticksInAir;
    }

    @Override
    public Entity getMissileEntity()
    {
        return missile;
    }

    @Override
    public void setTargetData(IMissileTarget data) {
        this.targetData = data;
    }

    @Override
    public IMissileTarget getTargetData()
    {
        return this.targetData;
    }

    @Override
    public void setFlightLogic(IMissileFlightLogic logic)
    {
        this.flightLogic = logic;
    }

    @Override
    public IMissileFlightLogic getFlightLogic()
    {
        return this.flightLogic;
    }

    @Override
    public void launch()
    {
        //Tell missile to start moving
        this.doFlight = true;
        Optional.ofNullable(getFlightLogic()).ifPresent(logic -> {
            logic.calculateFlightPath(missile.world, missile.x(), missile.y(), missile.z(), targetData); //TODO show in launcher screen with predicted path and time
            logic.start(missile);
        });

        //Trigger events
        //TODO add generic event
        ICBMClassicAPI.EX_MISSILE_REGISTRY.triggerLaunch(this);

        //Trigger events
        // TODO add an event system here
        RadarRegistry.add(this.missile);

        //Play audio
        ICBMSounds.MISSILE_LAUNCH.play(this.missile.world, this.missile.posX, this.missile.posY, this.missile.posZ,
            1F, (1.0F + CalculationHelpers.randFloatRange(this.missile.world.rand, 0.2F)) * 0.7F, true);


        if (ConfigDebug.DEBUG_MISSILE_LAUNCHES)
        {
            ICBMClassic.logger().info(
                String.format("Missile(%s): Launch triggered of type[%s] starting from '%s' and aimed at `%s`",
                    this.missile.getEntityId(),
                    this.missile.getName(),
                    Optional.ofNullable(this.missile.sourceOfProjectile).map(Objects::toString).orElse("'null'"),
                    Optional.ofNullable(this.missile.missileCapability.targetData).map(Objects::toString).orElse("'null'")
                )
            );
        }
    }

    @Override
    public World world()
    {
        return missile != null ? missile.world : null;
    }

    @Override
    public double z()
    {
        return missile != null ? missile.posZ : 0;
    }

    @Override
    public double x()
    {
        return missile != null ? missile.posX : 0;
    }

    @Override
    public double y()
    {
        return missile != null ? missile.posY : 0;
    }

    public static void register()
    {
        CapabilityManager.INSTANCE.register(IMissile.class, new Capability.IStorage<IMissile>()
                {
                    @Nullable
                    @Override
                    public NBTBase writeNBT(Capability<IMissile> capability, IMissile instance, EnumFacing side)
                    {
                        return null;
                    }

                    @Override
                    public void readNBT(Capability<IMissile> capability, IMissile instance, EnumFacing side, NBTBase nbt)
                    {

                    }
                },
                () -> new CapabilityMissile(null));
    }

    @Override
    public NBTTagCompound serializeNBT() {
        final NBTTagCompound saveData = new NBTTagCompound();
        SAVE_LOGIC.save(this, saveData);
        return saveData;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
       SAVE_LOGIC.load(this, nbt);
    }

    private static final NbtSaveHandler<CapabilityMissile> SAVE_LOGIC = new NbtSaveHandler<CapabilityMissile>()
        .addRoot("flags")
        /* */.nodeBoolean("do_flight", (cap) -> cap.canRunFlightLogic(), (cap, i) -> cap.doFlight = i)
        .base()
        .mainRoot()
        /* */.node(new NbtSaveNode<CapabilityMissile, NBTTagCompound>("target",
            (cap) -> { //TODO convert to class so we can have targeting items and launcher reuse
                if(cap.targetData != null) {
                    final NBTTagCompound tagCompound = new NBTTagCompound();
                    final NBTTagCompound logicSave = cap.targetData.serializeNBT();
                    if (logicSave != null && !logicSave.hasNoTags())
                    {
                        tagCompound.setTag("data", logicSave);
                    }
                    tagCompound.setString("id", cap.targetData.getRegistryName().toString());
                    return tagCompound;
                }
                return null;
            },
            (cap, data) -> {
                final ResourceLocation saveId = new ResourceLocation(data.getString("id"));
                final IMissileTarget target = ICBMClassicAPI.MISSILE_TARGET_DATA_REGISTRY.build(saveId);
                if (target != null)
                {
                    if (data.hasKey("data"))
                    {
                        target.deserializeNBT(data.getCompoundTag("data"));
                    }
                    cap.setTargetData(target);
                }
            }
        ))
        /* */.node(new NbtSaveNode<CapabilityMissile, NBTTagCompound>("flight",
            (missile) -> { //TODO convert to class to make cleaner and provide better testing surface
                final NBTTagCompound save = new NBTTagCompound();
                final IMissileFlightLogic logic = missile.getFlightLogic();
                if(logic != null)
                {
                    final NBTTagCompound logicSave = logic.save();
                    if (logicSave != null && !logicSave.hasNoTags())
                    {
                        save.setTag("data", logicSave);
                    }
                    save.setString("id", logic.getRegistryName().toString());
                }
                return save;
            },
            (missile, data) -> {
                final ResourceLocation saveId = new ResourceLocation(data.getString("id"));
                final IMissileFlightLogic logic = ICBMClassicAPI.MISSILE_FLIGHT_LOGIC_REGISTRY.build(saveId);
                if (logic != null)
                {
                    if (data.hasKey("data"))
                    {
                        logic.load(data.getCompoundTag("data"));
                    }
                    missile.setFlightLogic(logic);
                }
            }
        ))
        .base();

    public boolean canRunFlightLogic()
    {
        return doFlight;
    }
}
