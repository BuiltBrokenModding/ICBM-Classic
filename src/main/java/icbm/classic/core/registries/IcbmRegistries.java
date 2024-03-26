package icbm.classic.core.registries;

import icbm.classic.IcbmConstants;
import icbm.classic.api.missiles.IMissile;
import icbm.classic.api.missiles.parts.IMissileFlightLogic;
import icbm.classic.api.missiles.parts.IMissileTarget;
import icbm.classic.api.reg.ExplosiveType;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public final class IcbmRegistries {

    public static final ResourceKey<Registry<ExplosiveType>> EXPLOSIVES = ResourceKey.createRegistryKey(new ResourceLocation(IcbmConstants.MOD_ID, "explosives"));

    public static final ResourceKey<Registry<IMissile>> MISSILES = ResourceKey.createRegistryKey(new ResourceLocation(IcbmConstants.MOD_ID, "missiles"));

    public static final ResourceKey<Registry<ExplosiveType>> GRENADES = ResourceKey.createRegistryKey(new ResourceLocation(IcbmConstants.MOD_ID, "grenades"));

    public static final ResourceKey<Registry<IMissileTarget>> MISSILE_TARGETS = ResourceKey.createRegistryKey(new ResourceLocation(IcbmConstants.MOD_ID, "missile_targets"));

    public static final ResourceKey<Registry<IMissileFlightLogic>> MISSILE_FLIGHT_LOGIC = ResourceKey.createRegistryKey(new ResourceLocation(IcbmConstants.MOD_ID, "missile_flight_logic"));

    public static final ResourceKey<Registry<IMissileFlightLogic>> MISSILE_CAUSE = ResourceKey.createRegistryKey(new ResourceLocation(IcbmConstants.MOD_ID, "missile_cause"));
}
