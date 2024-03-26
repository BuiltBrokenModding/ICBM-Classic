package icbm.classic.world.missile.entity;

import icbm.classic.ICBMClassic;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.missiles.IMissile;
import icbm.classic.api.missiles.cause.IMissileSource;
import icbm.classic.api.missiles.parts.IMissileFlightLogic;
import icbm.classic.api.missiles.parts.IMissileTarget;
import icbm.classic.client.ICBMSounds;
import icbm.classic.config.ConfigDebug;
import icbm.classic.lib.CalculationHelpers;
import icbm.classic.lib.radar.RadarRegistry;
import icbm.classic.lib.saving.NbtSaveHandler;
import icbm.classic.lib.saving.NbtSaveNode;
import icbm.classic.world.missile.logic.source.MissileSource;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NBTBase;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.neoforged.common.capabilities.Capability;
import net.neoforged.common.capabilities.CapabilityManager;
import net.neoforged.neoforge.common.util.INBTSerializable;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Optional;

/**
 * Created by Dark(DarkGuardsman, Robert) on 1/7/19.
 */
public class CapabilityMissile implements IMissile, INBTSerializable<CompoundTag> {
    private final EntityMissile missile;
    private IMissileTarget targetData;

    private IMissileSource firingSource;

    private IMissileFlightLogic flightLogic;
    private boolean doFlight = false;

    public CapabilityMissile(EntityMissile missile) {
        this.missile = missile;
    }

    @Override
    public int getTicksInAir() {
        return missile.ticksInAir;
    }

    @Override
    public Entity getMissileEntity() {
        return missile;
    }

    @Override
    public void setTargetData(IMissileTarget data) {
        this.targetData = data;
    }

    @Override
    public IMissileTarget getTargetData() {
        return this.targetData;
    }

    @Override
    public void setFlightLogic(IMissileFlightLogic logic) {
        this.missile.syncClient = true;
        this.flightLogic = logic;
    }

    @Override
    public void switchFlightLogic(IMissileFlightLogic logic) {
        if (ConfigDebug.DEBUG_MISSILE_LOGIC) {
            // TODO move to event system
            ICBMClassic.logger().info(this + ": Switching flight logic from '" + getFlightLogic() + "' to '" + logic + "'");
        }
        setFlightLogic(logic);
        triggerFlightLogic();
    }

    protected void triggerFlightLogic() {
        if (flightLogic != null) {
            flightLogic.calculateFlightPath(level(), x(), y(), z(), getTargetData()); //TODO show in launcher screen with predicted path and time
            flightLogic.start(missile, this);
        }
    }

    @Override
    public IMissileFlightLogic getFlightLogic() {
        return this.flightLogic;
    }

    @Override
    public void setMissileSource(IMissileSource source) {
        this.firingSource = source;
    }

    @Override
    public IMissileSource getMissileSource() {
        return firingSource;
    }

    @Override
    public void launch() {
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
        ICBMSounds.MISSILE_LAUNCH.play(level(), x(), y(), z(),
            1F, (1.0F + CalculationHelpers.randFloatRange(level().rand, 0.2F)) * 0.7F, true);


        if (ConfigDebug.DEBUG_MISSILE_LAUNCHES) {
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
    public Level level() {
        return missile != null ? missile.world : null;
    }

    @Override
    public double z() {
        return missile != null ? missile.getZ() : 0;
    }

    @Override
    public double x() {
        return missile != null ? missile.getX() : 0;
    }

    @Override
    public double y() {
        return missile != null ? missile.getY() : 0;
    }

    public static void register() {
        CapabilityManager.INSTANCE.register(IMissile.class, new Capability.IStorage<IMissile>() {
                @Nullable
                @Override
                public NBTBase writeNBT(Capability<IMissile> capability, IMissile instance, Direction side) {
                    return null;
                }

                @Override
                public void readNBT(Capability<IMissile> capability, IMissile instance, Direction side, NBTBase nbt) {

                }
            },
            () -> new CapabilityMissile(null));
    }

    @Override
    public CompoundTag serializeNBT() {
        final CompoundTag saveData = new CompoundTag();
        SAVE_LOGIC.save(this, saveData);
        return saveData;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        SAVE_LOGIC.load(this, nbt);
    }

    private static final NbtSaveHandler<CapabilityMissile> SAVE_LOGIC = new NbtSaveHandler<CapabilityMissile>()
        .addRoot("flags")
        /* */.nodeBoolean("doFlight", CapabilityMissile::canRunFlightLogic, (cap, i) -> cap.doFlight = i)
        .base()
        .mainRoot()
        /* */.node(new NbtSaveNode<CapabilityMissile, CompoundTag>("target",
            (cap) -> { //TODO convert to class so we can have targeting items and launcher reuse
                if (cap.getTargetData() != null) {
                    //Not all target components are restored via save, some are always present on the entity
                    if (cap.getTargetData().getRegistryName() != null) {
                        final CompoundTag tagCompound = new CompoundTag();
                        final CompoundTag logicSave = cap.getTargetData().serializeNBT();
                        if (logicSave != null && !logicSave.isEmpty()) {
                            tagCompound.put("data", logicSave);
                        }
                        tagCompound.putString("id", cap.getTargetData().getRegistryName().toString());
                        return tagCompound;
                    } else {
                        return cap.getTargetData().serializeNBT();
                    }
                }
                return null;
            },
            (cap, data) -> {
                //Attempt to restore target object from save
                if (cap.getTargetData() == null) {
                    final ResourceLocation saveId = new ResourceLocation(data.getString("id"));
                    final IMissileTarget target = ICBMClassicAPI.MISSILE_TARGET_DATA_REGISTRY.build(saveId);
                    if (target != null) {
                        if (data.contains("data")) {
                            target.deserializeNBT(data.getCompound("data"));
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
        /* */.node(new NbtSaveNode<CapabilityMissile, CompoundTag>("flight",
            (missile) -> { //TODO convert to class to make cleaner and provide better testing surface
                final CompoundTag save = new CompoundTag();
                final IMissileFlightLogic logic = missile.getFlightLogic();
                if (logic != null) {
                    final CompoundTag logicSave = logic.serializeNBT();
                    if (logicSave != null && !logicSave.isEmpty()) {
                        save.put("data", logicSave);
                    }
                    save.putString("id", logic.getRegistryName().toString());
                }
                return save;
            },
            (missile, data) -> {
                final ResourceLocation saveId = new ResourceLocation(data.getString("id"));
                final IMissileFlightLogic logic = ICBMClassicAPI.MISSILE_FLIGHT_LOGIC_REGISTRY.build(saveId);
                if (logic != null) {
                    if (data.contains("data")) {
                        logic.deserializeNBT(data.getCompound("data"));
                    }
                    missile.setFlightLogic(logic);
                }
            }
        ))
        /* */.node(new NbtSaveNode<CapabilityMissile, CompoundTag>("source",
            (missile) -> { //TODO convert to class to make cleaner and provide better testing surface
                final CompoundTag save = new CompoundTag();
                final IMissileSource source = missile.getMissileSource();
                if (source != null) {
                    return source.serializeNBT();
                }
                return save;
            },
            (missile, data) -> {
                final IMissileSource source = new MissileSource();
                if (data.contains("data")) {
                    source.deserializeNBT(data.getCompound("data"));
                } else {
                    source.deserializeNBT(data);
                }
            }
        ))
        .base();

    public boolean canRunFlightLogic() {
        return doFlight;
    }

    @Override
    public int hashCode() {
        return missile != null ? missile.hashCode() : super.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof CapabilityMissile) {
            return Objects.equals(((CapabilityMissile) other).flightLogic, flightLogic)
                && Objects.equals(((CapabilityMissile) other).targetData, targetData)
                && ((CapabilityMissile) other).doFlight == doFlight;
        }
        return false;
    }
}
