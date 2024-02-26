package icbm.classic.content.missile.entity;

import icbm.classic.ICBMClassic;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.missiles.IMissile;
import icbm.classic.api.missiles.cause.IMissileSource;
import icbm.classic.api.missiles.parts.IMissileFlightLogic;
import icbm.classic.api.missiles.parts.IMissileTarget;
import icbm.classic.client.ICBMSounds;
import icbm.classic.config.ConfigDebug;
import icbm.classic.content.missile.logic.source.MissileSource;
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
    public void switchFlightLogic(IMissileFlightLogic logic) {
        if(ConfigDebug.DEBUG_MISSILE_LOGIC) {
            // TODO move to event system
            ICBMClassic.logger().info(this + ": Switching flight logic from '" + getFlightLogic() + "' to '" + logic + "'");
        }
        setFlightLogic(logic);
        triggerFlightLogic();
    }

    protected void triggerFlightLogic() {
        if(flightLogic != null) {
            flightLogic.calculateFlightPath(world(), x(), y(), z(), getTargetData()); //TODO show in launcher screen with predicted path and time
            flightLogic.start(missile, this);
        }
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
        triggerFlightLogic();

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
        /* */.nodeBoolean("doFlight", CapabilityMissile::canRunFlightLogic, (cap, i) -> cap.doFlight = i)
        .base()
        .mainRoot()
        /* */.nodeBuildableObject("target", () -> ICBMClassicAPI.MISSILE_TARGET_DATA_REGISTRY, CapabilityMissile::getTargetData, CapabilityMissile::setTargetData)
        /* */.nodeBuildableObject("flight", () -> ICBMClassicAPI.MISSILE_FLIGHT_LOGIC_REGISTRY, CapabilityMissile::getFlightLogic, CapabilityMissile::setFlightLogic)
        /* */.nodeINBTSerializable("source", CapabilityMissile::getMissileSource)
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
