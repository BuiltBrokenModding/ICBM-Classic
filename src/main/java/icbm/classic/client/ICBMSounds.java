package icbm.classic.client;

import icbm.classic.ICBMClassic;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.ForgeRegistries;


/**
 * Enum of sounds used by ICBM
 *
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 1/6/2018.
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
    HYPERSONIC("hypersonic"),
    MACHINE_HUM("machinehum"),
    POWER_DOWN("powerdown"),
    TARGET_LOCKED("targetlocked"),
    REDMATTER("redmatter"),
    SONICWAVE("sonicwave");

    private final ResourceLocation location;
    private SoundEvent sound;


    ICBMSounds(String path)
    {
        location = new ResourceLocation(ICBMClassic.DOMAIN, path);
    }

    /**
     * Gets the sound event for use with MC code
     *
     * @return sound event
     */
    public SoundEvent getSound()
    {
        return sound;
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
        play(entity.world, entity.posX, entity.posY, entity.posZ, volume, pitch, distanceDelay);
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
    public void play(World world, double x, double y, double z, float volume, float pitch, boolean distanceDelay)
    {
        world.playSound(x, y, z, getSound(), SoundCategory.BLOCKS, volume, pitch, distanceDelay);
    }

    /**
     * Registers all sounds to event system
     */
    public static void registerAll()
    {
        for (ICBMSounds icbmSounds : values())
        {
            icbmSounds.register();
        }
    }

    //Registers the given sound
    private void register()
    {
        sound = new SoundEvent(location).setRegistryName(location);
        ForgeRegistries.SOUND_EVENTS.register(sound);
    }
}
