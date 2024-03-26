package icbm.classic.world;

import icbm.classic.IcbmConstants;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.damagesource.DamageScaling;
import net.minecraft.world.damagesource.DamageType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class IcbmDamageTypes {

    public static final DeferredRegister<DamageType> REGISTER = DeferredRegister.create(Registries.DAMAGE_TYPE, IcbmConstants.MOD_ID);

    public static final DeferredHolder<DamageType, DamageType> CHEMICAL = REGISTER.register("chemical",
        resourceLocation -> new DamageType(resourceLocation.getPath(), DamageScaling.ALWAYS, 0.1F));
}
