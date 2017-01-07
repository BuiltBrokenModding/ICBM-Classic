package icbm.classic.content.explosive;

import com.builtbroken.mc.lib.helper.LanguageUtility;
import com.builtbroken.mc.lib.transform.vector.Pos;
import com.builtbroken.mc.prefab.explosive.AbstractExplosiveHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import icbm.classic.prefab.ModelICBM;
import icbm.classic.Settings;
import icbm.explosion.ICBMExplosion;
import icbm.classic.content.explosive.ex.*;
import icbm.classic.content.explosive.ex.missiles.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.model.IModelCustom;
import resonant.lib.flag.FlagRegistry;

/** The explosive registry class. Used to register explosions. */
public abstract class Explosive extends AbstractExplosiveHandler
{
    /** Explosives */
    public static Explosive condensed;
    public static Explosive shrapnel;
    public static Explosive incendiary;
    public static Explosive debilitation;
    public static Explosive chemical;
    public static Explosive anvil;
    public static Explosive replsive;
    public static Explosive attractive;

    public static Explosive fragmentation;
    public static Explosive contagious;
    public static Explosive sonic;
    public static Explosive breaching;
    public static Explosive rejuvenation;
    public static Explosive thermobaric;
    public static Explosive sMine;

    public static Explosive nuclear;
    public static Explosive emp;
    public static Explosive exothermic;
    public static Explosive endothermic;
    public static Explosive antiGrav;
    public static Explosive ender;
    public static Explosive hypersonic;

    public static Explosive antimatter;
    public static Explosive redMatter;

    /** Missiles */
    public static Explosion missileModule;
    public static Explosion homing;
    public static Explosion antiBallistic;
    public static Explosion cluster;
    public static Explosion nuclearCluster;

    public static boolean registered = false;

    public static void registerExplosives()
    {
        condensed = ExplosiveRegistry.register(new ExCondensed("condensed", 1));
        shrapnel = ExplosiveRegistry.register(new ExShrapnel("shrapnel", 1));
        incendiary = ExplosiveRegistry.register(new ExIncendiary("incendiary", 1));
        debilitation = ExplosiveRegistry.register(new ExDebilitation("debilitation", 1));
        chemical = ExplosiveRegistry.register(new ExChemical("chemical", 1));
        anvil = ExplosiveRegistry.register(new ExShrapnel("anvil", 1));
        replsive = ExplosiveRegistry.register(new ExRepulsive("repulsive", 1));
        attractive = ExplosiveRegistry.register(new ExRepulsive("attractive", 1));

        fragmentation = ExplosiveRegistry.register(new ExShrapnel("fragmentation", 2));
        contagious = ExplosiveRegistry.register(new ExChemical("contagious", 2));
        sonic = ExplosiveRegistry.register(new ExSonic("sonic", 2));
        breaching = ExplosiveRegistry.register(new ExBreaching());
        rejuvenation = ExplosiveRegistry.register(new ExRejuvenation());
        thermobaric = ExplosiveRegistry.register(new ExNuclear("thermobaric", 2));
        sMine = ExplosiveRegistry.register(new ExSMine("sMine", 2));

        nuclear = ExplosiveRegistry.register(new ExNuclear("nuclear", 3));
        emp = ExplosiveRegistry.register(new ExEMP());
        exothermic = ExplosiveRegistry.register(new ExExothermic());
        endothermic = ExplosiveRegistry.register(new ExEndothermic());
        antiGrav = ExplosiveRegistry.register(new ExAntiGravitational());
        ender = ExplosiveRegistry.register(new ExEnder());
        hypersonic = ExplosiveRegistry.register(new ExSonic("hypersonic", 3));

        antimatter = ExplosiveRegistry.register(new ExAntimatter());
        redMatter = ExplosiveRegistry.register(new ExRedMatter());

        /** Missiles */
        missileModule = (Explosion) ExplosiveRegistry.register(new MissileModule());
        homing = (Explosion) ExplosiveRegistry.register(new MissileHoming());
        antiBallistic = (Explosion) ExplosiveRegistry.register(new MissileAnti());
        cluster = (Explosion) ExplosiveRegistry.register(new MissileCluster("cluster", 2));
        nuclearCluster = (Explosion) ExplosiveRegistry.register(new MissileNuclearCluster());

        registered = true;
    }

    /** The unique identification name for this explosive. */
    private String nameID;
    /** The tier of this explosive */
    private int tier;
    /** The fuse of this explosive */
    private int fuseTime;
    /** The flag name of this explosive */
    public final String flagName;
    /** Is this explosive disabled? */
    protected boolean isDisabled;
    /** Is this explosive able to be pushed by other explosions? */
    protected boolean isMobile = false;

    protected boolean hasBlock;
    protected boolean hasGrenade;
    protected boolean hasMinecart;
    protected boolean hasMissile;

    protected Explosive(String name, int tier)
    {
        super(name);
        this.nameID = name;
        this.tier = tier;
        this.fuseTime = 100;

        this.hasBlock = true;
        this.hasMissile = true;
        this.hasGrenade = this.tier <= 1;
        this.hasMinecart = this.tier <= 2;

        this.flagName = FlagRegistry.registerFlag("ban_" + this.nameID);
        this.isDisabled = Settings.CONFIGURATION.get("Disable_Explosives", "Disable " + this.nameID, false).getBoolean(false);

    }

    //@Override
    //public final int getID()
    //{
    //    return ExplosiveRegistry.getID(this.getUnlocalizedName());
    //}


    public String getUnlocalizedName()
    {
        return this.nameID;
    }


    public String getExplosiveName()
    {
        return LanguageUtility.getLocal("icbm.explosive." + this.nameID + ".name");
    }


    public String getGrenadeName()
    {
        return LanguageUtility.getLocal("icbm.grenade." + this.nameID + ".name");
    }


    public String getMissileName()
    {
        return LanguageUtility.getLocal("icbm.missile." + this.nameID + ".name");
    }


    public String getMinecartName()
    {
        return LanguageUtility.getLocal("icbm.minecart." + this.nameID + ".name");
    }


    public int getTier()
    {
        return this.tier;
    }


    public void setTier(int tier)
    {
        this.tier = tier;
    }

    public Explosive setYinXin(int fuse)
    {
        this.fuseTime = fuse;
        return this;
    }

    /**
     * The fuse of the explosion
     *
     * @return The Fuse
     */
    public int getYinXin()
    {
        return fuseTime;
    }

    /**
     * Called at the before the explosive detonated as a block.
     *
     * @param world
     * @param entity
     */
    public void yinZhaQian(World world, Entity entity)
    {
        world.playSoundAtEntity(entity, "random.fuse", 1.0F, 1.0F);
    }

    /**
     * Called while the explosive is being detonated (fuse ticks) in block form.
     *
     * @param fuseTicks - The amount of ticks this explosive is on fuse
     */
    public void onYinZha(World world, Pos position, int fuseTicks)
    {
        world.spawnParticle("smoke", position.x(), position.y() + 0.5D, position.z(), 0.0D, 0.0D, 0.0D);
    }

    /**
     * Called when the block for of this explosive is destroy by an explosion
     *
     * @return - Fuse left
     */
    public int onBeiZha()
    {
        return (int) (this.fuseTime / 2 + Math.random() * this.fuseTime / 4);
    }


    @SideOnly(Side.CLIENT)
    public ModelICBM getBlockModel()
    {
        return null;
    }

    @SideOnly(Side.CLIENT)
    public ResourceLocation getBlockResource()
    {
        return null;
    }

    @SideOnly(Side.CLIENT)
    public IIcon getIcon()
    {
        return null;
    }

    @SideOnly(Side.CLIENT)
    public IModelCustom getMissileModel()
    {
        return null;
    }

    public boolean hasGrenadeForm()
    {
        return this.hasGrenade;
    }

    public boolean hasMissileForm()
    {
        return this.hasMissile;
    }

    public boolean hasMinecartForm()
    {
        return this.hasMinecart;
    }

    public boolean hasBlockForm()
    {
        return this.hasBlock;
    }

    /** Called to add the recipe for this explosive */
    public void init()
    {

    }

    public ItemStack getItemStack()
    {
        return this.getItemStack(1);
    }

    public ItemStack getItemStack(int amount)
    {
        return new ItemStack(ICBMExplosion.blockExplosive, amount, this.getID());
    }

    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer entityPlayer, int par6, float par7, float par8, float par9)
    {
        return false;
    }

    public void createExplosion(World world, double x, double y, double z, Entity entity)
    {
        if (!this.isDisabled)
        {
            this.doCreateExplosion(world, x, y, z, entity);
        }
    }

    public abstract void doCreateExplosion(World world, double x, double y, double z, Entity entity);

    /**
     * Checks if the explosive is banned in an area
     *
     * @param world - world to check in
     * @param x     - coord
     * @param y     - coord
     * @param z-    coord
     * @return true if it is banned
     */
    public boolean isBannedInRegion(World world, double x, double y, double z)
    {
        //boolean flag_all = FlagRegistry.getModFlag().getFlagWorld(world).containsValue("ban_ICBM", "true", new Pos(x, y, z));
        //boolean flag_missile = FlagRegistry.getModFlag().getFlagWorld(world).containsValue(this.flagName, "true", new Pos(x, y, z));

        //return flag_all || flag_missile;
        return false;
    }
}
