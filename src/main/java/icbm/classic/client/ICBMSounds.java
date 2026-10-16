package icbm.classic.client;

import com.builtbroken.jlib.data.vector.IPos3D;
import icbm.classic.ICBMConstants;
import net.minecraft.world.entity.Entity;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.Level;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;


/**
 * Enum of sounds used by ICBM
 *
 *
 * Created by Dark(DarkGuardsman, Robin) on 1/6/2018.
 * <p>
 * Credit to https://github.com/kitsushadow for sharing info on how to do sounds in MC 1.12
 */
public enum ICBMSounds
{
    ANTIMATTER("antimatter"),
    BEAM_CHARGING("beamcharging"),
    COLLAPSE("collapse"),
    DEBILITATION("debilitation"),
    EMP("emp"),
    EXPLOSION("explosion"),
    EXPLOSION_FIRE("explosionfire"),
    GAS_LEAK("gasleak"),
    MACHINE_HUM("machinehum"),
    POWER_DOWN("powerdown"),
    REDMATTER("redmatter"),
    SONICWAVE("sonicwave"),
    MISSILE_LAUNCH("missilelaunch"),
    MISSILE_ENGINE("missileinair"),
    MEEP("meep", SoundSource.NEUTRAL),
    RADIOACTIVE_TICK("radioactive_tick", SoundSource.BLOCKS);

    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, ICBMConstants.DOMAIN);

    private final ResourceLocation location;
    private final SoundSource category;
    private DeferredHolder<SoundEvent, SoundEvent> holder;

    ICBMSounds(String path)
    {
       this(path, SoundSource.BLOCKS);
    }

    ICBMSounds(String path, SoundSource category)
    {
        this.category = category;
        location = ResourceLocation.fromNamespaceAndPath(ICBMConstants.DOMAIN, path);
    }

    /**
     * Gets the sound event for use with MC code
     *
     * @return sound event
     */
    public SoundEvent getSound()
    {
        return holder.get();
    }

    /**
     * Players the given sound at the entity's location with the arguments
     *
     * @param entity        - source
     * @param volume        - sound level
     * @param pitch         - sound pitch
     * @param distanceDelay - should the sound be delayed by distance
     */
    public void play(Entity entity, float volume, float pitch, boolean distanceDelay)
    {
        //TODO move audio settings to constants attached to configs
        play(entity.level(), entity.getX(), entity.getY(), entity.getZ(), volume, pitch, distanceDelay);
    }

    /**
     * Players the given sound at the entity's location with the arguments
     *
     * @param world         - location
     * @param x-            location
     * @param y-            location
     * @param z-            location
     * @param volume        - sound level
     * @param pitch         - sound pitch
     * @param distanceDelay - should the sound be delayed by distance
     */
    public void play(Level world, double x, double y, double z, float volume, float pitch, boolean distanceDelay)
    {
        if(world.isClientSide()) {
            world.playLocalSound(x, y, z, getSound(), category, volume, pitch, distanceDelay);
        }
        else {
            world.playSound(null, x, y, z, getSound(), category, volume, pitch);
        }
    }

    public void play(Level world, IPos3D pos, float volume, float pitch, boolean distanceDelay)
    {
        world.playSound(null, pos.x(), pos.y(), pos.z(), getSound(), category, volume, pitch);
    }

    public static void register(IEventBus modBus)
    {
        for (ICBMSounds icbmSounds : values())
        {
            icbmSounds.holder = SOUNDS.register(icbmSounds.location.getPath(), () -> SoundEvent.createVariableRangeEvent(icbmSounds.location));
        }
        SOUNDS.register(modBus);
    }
}
// PORTED
