package icbm.classic.api.refs;

import icbm.classic.api.reg.IExplosiveData;

/**
 *
 * Created by Dark(DarkGuardsman, Robin) on 1/7/19.
 * @deprecated will be migrated to IActionData with tags
 */
@Deprecated
public final class ICBMExplosives
{
    //=================== Tier 1
    public static IExplosiveData CONDENSED;
    public static IExplosiveData SHRAPNEL;
    public static IExplosiveData INCENDIARY;
    public static IExplosiveData DEBILITATION;
    public static IExplosiveData CHEMICAL;
    public static IExplosiveData ANVIL;
    public static IExplosiveData REPULSIVE;
    public static IExplosiveData ATTRACTIVE;
    public static IExplosiveData COLOR;

    public static IExplosiveData SMOKE;

    //=================== Tier 2
    public static IExplosiveData FRAGMENTATION;
    public static IExplosiveData CONTAGIOUS;
    public static IExplosiveData SONIC;
    public static IExplosiveData BREACHING;
    public static IExplosiveData THERMOBARIC;

    //=================== Tier 3
    public static IExplosiveData NUCLEAR;
    public static IExplosiveData EMP;
    public static IExplosiveData EXOTHERMIC;
    public static IExplosiveData ENDOTHERMIC;
    public static IExplosiveData GRAVITY;
    public static IExplosiveData ENDER;


    //=================== Tier 4
    public static IExplosiveData ANTIMATTER;
    public static IExplosiveData REDMATTER;

    //=================== No content, only blast
    @Deprecated
    public static IExplosiveData MISSILEMODULE;
    public static IExplosiveData MUTATION;
    public static IExplosiveData ROT;
}
