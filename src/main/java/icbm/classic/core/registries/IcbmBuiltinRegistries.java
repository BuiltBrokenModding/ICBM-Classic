package icbm.classic.core.registries;

import icbm.classic.api.missiles.IMissile;
import icbm.classic.api.missiles.parts.IMissileFlightLogic;
import icbm.classic.api.missiles.parts.IMissileTarget;
import icbm.classic.api.reg.ExplosiveType;
import net.minecraft.core.Registry;
import net.neoforged.neoforge.registries.RegistryBuilder;

public final class IcbmBuiltinRegistries {

    public static final Registry<ExplosiveType> EXPLOSIVES = new RegistryBuilder<>(IcbmRegistries.EXPLOSIVES)
        .create();

    public static final Registry<IMissile> MISSILES = new RegistryBuilder<>(IcbmRegistries.MISSILES)
        .create();

    public static final Registry<ExplosiveType> GRENADES = new RegistryBuilder<>(IcbmRegistries.GRENADES)
        .create();

    public static final Registry<IMissileTarget> MISSILE_TARGETS = new RegistryBuilder<>(IcbmRegistries.MISSILE_TARGETS)
        .create();

    public static final Registry<IMissileFlightLogic> MISSILE_FLIGHT_LOGIC = new RegistryBuilder<>(IcbmRegistries.MISSILE_FLIGHT_LOGIC)
        .create();

    public static final Registry<IMissileFlightLogic> MISSILE_CAUSE = new RegistryBuilder<>(IcbmRegistries.MISSILE_CAUSE)
        .create();
}
