package icbm.classic.api.refs;

import icbm.classic.api.reg.ExplosiveType;

/**
 * Created by Dark(DarkGuardsman, Robert) on 1/7/19.
 */
public final class ICBMExplosives {
    //=================== Tier 1
    public static ExplosiveType CONDENSED;
    public static ExplosiveType SHRAPNEL;
    public static ExplosiveType INCENDIARY;
    public static ExplosiveType DEBILITATION;
    public static ExplosiveType CHEMICAL;
    public static ExplosiveType ANVIL;
    public static ExplosiveType REPULSIVE;
    public static ExplosiveType ATTRACTIVE;
    public static ExplosiveType COLOR;

    public static ExplosiveType SMOKE;

    //=================== Tier 2
    public static ExplosiveType FRAGMENTATION;
    public static ExplosiveType CONTAGIOUS;
    public static ExplosiveType SONIC;
    public static ExplosiveType BREACHING;
    public static ExplosiveType THERMOBARIC;

    //=================== Tier 3
    public static ExplosiveType NUCLEAR;
    public static ExplosiveType EMP;
    public static ExplosiveType EXOTHERMIC;
    public static ExplosiveType ENDOTHERMIC;
    public static ExplosiveType ANTI_GRAVITATIONAL;
    public static ExplosiveType ENDER;

    /**
     * @Deprecated for removal in next major MC version, currently placeholder to prevent game save errors
     */
    @Deprecated
    public static ExplosiveType HYPERSONIC;

    //=================== Tier 4
    public static ExplosiveType ANTIMATTER;
    public static ExplosiveType REDMATTER;

    //=================== No content, only blast
    public static ExplosiveType MISSILEMODULE;
    public static ExplosiveType MUTATION;
    public static ExplosiveType ROT;
}
