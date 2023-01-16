package icbm.classic.content.missile.entity;

import icbm.classic.ICBMClassic;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.missiles.IMissile;
import icbm.classic.api.missiles.IMissileFlightLogic;
import icbm.classic.api.missiles.IMissileSource;
import icbm.classic.api.missiles.IMissileTarget;
import icbm.classic.client.ICBMSounds;
import icbm.classic.config.ConfigDebug;
import icbm.classic.lib.CalculationHelpers;
import icbm.classic.lib.radar.RadarRegistry;
import icbm.classic.lib.saving.NbtSaveHandler;
import icbm.classic.lib.saving.NbtSaveNode;
import net.minecraft.entity.Entity;
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
    private final EntityMissile missile;
    private IMissileTarget targetData;

    private IMissileSource firingSource;

    private IMissileFlightLogic flightLogic;
    private boolean doFlight = false;

    public CapabilityMissile(EntityMissile missile)
    {
        this.missile = missile;
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
        this.missile.syncClient = true;
        this.flightLogic = logic;
    }

    @Override
    public IMissileFlightLogic getFlightLogic()
    {
        return this.flightLogic;
    }

    @Override
    public void setMissileSource(IMissileSource source)
    {
        this.firingSource = source;
    }

    @Override
    public IMissileSource getMissileSource()
    {
        return firingSource;
    }

    @Override
    public void launch()
    {
        //Tell missile to start moving
        this.doFlight = true;
        Optional.ofNullable(getFlightLogic()).ifPresent(logic -> {
            logic.calculateFlightPath(world(), x(), y(), z(), getTargetData() ); //TODO show in launcher screen with predicted path and time
            logic.start(missile);
        });

        //Trigger events
        //TODO add generic event
        ICBMClassicAPI.EX_MISSILE_REGISTRY.triggerLaunch(this);

        //Trigger events
        // TODO add an event system here
        RadarRegistry.add(this.missile); //TODO replace with capability and have radar system listen for entity spawn event

        //Play audio
        ICBMSounds.MISSILE_LAUNCH.play(world(), x(), y(), z(),
            1F, (1.0F + CalculationHelpers.randFloatRange(world().rand, 0.2F)) * 0.7F, true);


        if (ConfigDebug.DEBUG_MISSILE_LAUNCHES)
        {
            ICBMClassic.logger().info(
                String.format("Missile(%s): Launch triggered of type[%s] starting from '%s' and aimed at `%s`",
                    this.missile.getEntityId(),
                    this.missile.getName(),
                    Optional.ofNullable(this.getMissileSource()).map(Objects::toString).orElse("'null'"),
                    Optional.ofNullable(this.getTargetData()).map(Objects::toString).orElse("'null'")
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
                if(cap.getTargetData() != null) {
                    //Not all target components are restored via save, some are always present on the entity
                    if (cap.getTargetData().getRegistryName() != null) {
                        final NBTTagCompound tagCompound = new NBTTagCompound();
                        final NBTTagCompound logicSave = cap.getTargetData().serializeNBT();
                        if (logicSave != null && !logicSave.hasNoTags()) {
                            tagCompound.setTag("data", logicSave);
                        }
                        tagCompound.setString("id", cap.getTargetData().getRegistryName().toString());
                        return tagCompound;
                    }
                    else {
                        return cap.getTargetData().serializeNBT();
                    }
                }
                return null;
            },            (cap, data) -> {


                //Attempt to restore target object from save
                if(cap.getTargetData() == null) {
                    final ResourceLocation saveId = new ResourceLocation(data.getString("id"));
                    final IMissileTarget target = ICBMClassicAPI.MISSILE_TARGET_DATA_REGISTRY.build(saveId);
                    if (target != null) {
                        if (data.hasKey("data")) {
                            target.deserializeNBT(data.getCompoundTag("data"));
                        }
                        cap.setTargetData(target);
                    }
                }
                //Cap has hard locked target wrapper, restore any data directly to object
                else {
                    cap.getTargetData().deserializeNBT(data);
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
        /* */.node(new NbtSaveNode<CapabilityMissile, NBTTagCompound>("source",
            (missile) -> { //TODO convert to class to make cleaner and provide better testing surface
                final NBTTagCompound save = new NBTTagCompound();
                final IMissileSource source = missile.getMissileSource();
                if(source != null)
                {
                    final NBTTagCompound sourceSave = source.save();
                    if (sourceSave != null && !sourceSave.hasNoTags())
                    {
                        save.setTag("data", sourceSave);
                    }
                    save.setString("id", source.getRegistryName().toString());
                }
                return save;
            },
            (missile, data) -> {
                final ResourceLocation saveId = new ResourceLocation(data.getString("id"));
                final IMissileSource source = ICBMClassicAPI.MISSILE_SOURCE_REGISTRY.build(saveId);
                if (source != null)
                {
                    if (data.hasKey("data"))
                    {
                        source.load(data.getCompoundTag("data"));
                    }
                    missile.setMissileSource(source);
                }
            }
        ))
        .base();

    public boolean canRunFlightLogic()
    {
        return doFlight;
    }

    @Override
    public int hashCode() {
        return missile != null ? missile.hashCode() : super.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if(other instanceof CapabilityMissile) {
            return Objects.equals(((CapabilityMissile) other).flightLogic, flightLogic)
                && Objects.equals(((CapabilityMissile) other).targetData, targetData)
                && ((CapabilityMissile) other).doFlight == doFlight;
        }
        return false;
    }
}
