package icbm.classic.content.reg;

import icbm.classic.ICBMClassic;
import icbm.classic.api.EnumTier;
import icbm.classic.api.ExplosiveRefs;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.IWorldPosition;
import icbm.classic.api.caps.IExplosive;
import icbm.classic.api.explosion.IBlastFactory;
import icbm.classic.api.items.IWorldPosItem;
import icbm.classic.api.reg.IExplosiveData;
import icbm.classic.config.ConfigBlast;
import icbm.classic.content.blast.BlastAntiGravitational;
import icbm.classic.content.blast.BlastBreach;
import icbm.classic.content.blast.BlastChemical;
import icbm.classic.content.blast.BlastEMP;
import icbm.classic.content.blast.BlastEnderman;
import icbm.classic.content.blast.BlastEndothermic;
import icbm.classic.content.blast.BlastExothermic;
import icbm.classic.content.blast.BlastFire;
import icbm.classic.content.blast.BlastRedmatter;
import icbm.classic.content.blast.BlastShrapnel;
import icbm.classic.content.blast.BlastSonic;
import icbm.classic.content.blast.BlastTNT;
import icbm.classic.content.blast.BlastTNT.PushType;
import icbm.classic.content.blast.threaded.BlastAntimatter;
import icbm.classic.content.blast.threaded.BlastNuclear;
import icbm.classic.lib.transform.vector.Location;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

/**
 * Created by Dark(DarkGuardsman, Robert) on 1/7/19.
 */
public class ExplosiveInit
{
    //TODO use a datafixer to flatten missiles into (ex, homing, anti, cluster)
    //https://github.com/RS485/LogisticsPipes/blob/dev-mc1122/common/logisticspipes/datafixer/DataFixerSolidBlockItems.java
    //https://github.com/RS485/LogisticsPipes/blob/dev-mc1122/common/logisticspipes/datafixer/LPDataFixer.java#L17-L19

    public static void init()
    {
        ExplosiveRefs.CONDENSED = newEx("condensed", EnumTier.ONE, () -> new BlastTNT().setBlastSize(6));
        ICBMClassicAPI.EX_BLOCK_REGISTRY.setFuseSupplier(ExplosiveRefs.CONDENSED.getRegistryName(), (world, x, y, z) -> 100); //TODO add config

        ExplosiveRefs.SHRAPNEL = newEx("shrapnel", EnumTier.ONE, () -> new BlastShrapnel().setFlaming().setBlastSize(30));

        ExplosiveRefs.INCENDIARY = newEx("incendiary", EnumTier.ONE, () -> new BlastFire().setBlastSize(14));
        ICBMClassicAPI.EX_BLOCK_REGISTRY.setFuseTickListener(ExplosiveRefs.INCENDIARY.getRegistryName(),
                (world, x, y, z, tick) -> world.spawnParticle(EnumParticleTypes.LAVA, x, y + 0.5D, z, 0.0D, 0.0D, 0.0D)
                );


        ExplosiveRefs.DEBILITATION = newEx("debilitation", EnumTier.ONE,
                () -> new BlastChemical(20 * 30, false)
                .setConfuse().setBlastSize(20));

        ExplosiveRefs.CHEMICAL = newEx("chemical", EnumTier.ONE,
                () -> new BlastChemical(20 * 30, false)
                .setPoison().setRGB(0.8f, 0.8f, 0).setBlastSize(20));

        ExplosiveRefs.ANVIL = newEx("anvil", EnumTier.ONE,
                () -> new BlastShrapnel().setAnvil().setBlastSize(25));

        ExplosiveRefs.REPULSIVE = newEx("repulsive", EnumTier.ONE,
                () -> new BlastTNT().setDestroyItems().setPushType(PushType.REPEL).setBlastSize(2));
        ICBMClassicAPI.EX_BLOCK_REGISTRY.setFuseSupplier(ExplosiveRefs.REPULSIVE.getRegistryName(), (world, x, y, z) -> 120);


        ExplosiveRefs.ATTRACTIVE = newEx("attractive", EnumTier.ONE,
                () -> new BlastTNT().setDestroyItems().setPushType(PushType.ATTRACT).setBlastSize(2));
        ICBMClassicAPI.EX_BLOCK_REGISTRY.setFuseSupplier(ExplosiveRefs.ATTRACTIVE.getRegistryName(), (world, x, y, z) -> 120);

        //=================== Tier 2
        ExplosiveRefs.FRAGMENTATION = newEx("fragmentation", EnumTier.TWO,
                () -> new BlastShrapnel().setFlaming().setExplosive().setBlastSize(15));

        ExplosiveRefs.CONTAGIOUS = newEx("contagious", EnumTier.TWO,
                () -> new BlastChemical(20 * 30, false)
                .setContagious().setRGB(0.3f, 0.8f, 0).setBlastSize(20));

        ExplosiveRefs.SONIC = newEx("sonic", EnumTier.TWO,
                () -> new BlastSonic().setBlastSize(15));

        ExplosiveRefs.BREACHING = newEx("breaching", EnumTier.TWO,
                () -> new BlastBreach(7).setBlastSize(2.5));
        ICBMClassicAPI.EX_BLOCK_REGISTRY.setFuseSupplier(ExplosiveRefs.BREACHING.getRegistryName(), (world, x, y, z) -> 0);

        ExplosiveRefs.THERMOBARIC = newEx("thermobaric", EnumTier.TWO,
                () -> new BlastNuclear().setEnergy(45).setBlastSize(30));

        //ExplosiveRefs.SMINE = newEx(new ExSMine("sMine", EnumTier.TWO)); //TODO convert, replace model with JSON and custom entity

        //=================== Tier 3
        ExplosiveRefs.NUCLEAR = newEx("nuclear", EnumTier.THREE,
                () -> new BlastNuclear().setNuclear().setEnergy(80).setBlastSize(50));

        ExplosiveRefs.EMP = newEx("emp", EnumTier.THREE,
                () -> new BlastEMP().setEffectBlocks().setEffectEntities().setBlastSize(50));

        ExplosiveRefs.EXOTHERMIC = newEx("exothermic", EnumTier.THREE, () -> new BlastExothermic().setBlastSize(30));
        ICBMClassicAPI.EX_BLOCK_REGISTRY.setFuseTickListener(ExplosiveRefs.EXOTHERMIC.getRegistryName(),
                (world, x, y, z, tick) -> world.spawnParticle(EnumParticleTypes.LAVA, x, y + 0.5D, z, 0.0D, 0.0D, 0.0D)
                );

        ExplosiveRefs.ENDOTHERMIC = newEx("endothermic", EnumTier.THREE, () -> new BlastEndothermic().setBlastSize(30));
        //TODO add ice fuse animation

        ExplosiveRefs.ANTI_GRAVITATIONAL = newEx("antigravitational", EnumTier.THREE, () -> new BlastAntiGravitational().setBlastSize(30));

        ExplosiveRefs.ENDER = newEx("ender", EnumTier.THREE, () -> new BlastEnderman().setBlastSize(30));
        ICBMClassicAPI.EX_MISSILE_REGISTRY.setInteractionListener(ExplosiveRefs.ENDER.getRegistryName(), ExplosiveInit::enderMissileCoordSet);
        ICBMClassicAPI.EX_BLOCK_REGISTRY.setActivationListener(ExplosiveRefs.ENDER.getRegistryName(), ExplosiveInit::enderBlockCoordSet);

        ExplosiveRefs.HYPERSONIC = newEx("hypersonic", EnumTier.THREE, () -> new BlastSonic().setShockWave().setBlastSize(20)); //TODO find Missile model

        //=================== Tier 4
        ExplosiveRefs.ANTIMATTER = newEx("antimatter", EnumTier.FOUR,
                () -> new BlastAntimatter(ConfigBlast.ANTIMATTER_DESTROY_UNBREAKABLE_BLOCKS).setBlastSize(ConfigBlast.ANTIMATTER_SIZE));
        ICBMClassicAPI.EX_BLOCK_REGISTRY.setFuseSupplier(ExplosiveRefs.ANTIMATTER.getRegistryName(), (world, x, y, z) -> 300);
        //TODO add config (disable by default) for alarm audio

        ExplosiveRefs.REDMATTER = newEx("redMatter", EnumTier.FOUR, () -> new BlastRedmatter().setBlastSize(BlastRedmatter.NORMAL_RADIUS));


        //=================== Missiles
        ///* 24 */MISSILE(new MissileModule());
        ///* 25 */MISSILE_HOMING(new MissileHoming());
        ///* 26 */MISSILE_ANTI(new MissileAnti());
        ///* 27 */MISSILE_CLUSTER(new MissileCluster("cluster", EnumTier.TWO));
        ///* 28 */MISSILE_CLUSTER_NUKE(new MissileNuclearCluster())
    }

    private static IExplosiveData newEx(String name, EnumTier tier, IBlastFactory factory)
    {
        return ICBMClassicAPI.EXPLOSIVE_REGISTRY.register(new ResourceLocation(ICBMClassic.DOMAIN, name), tier, factory);
    }

    private static boolean enderMissileCoordSet(Entity entity, EntityPlayer player, EnumHand hand)
    {
        if (entity.hasCapability(ICBMClassicAPI.EXPLOSIVE_CAPABILITY, null))
        {
            final IExplosive provider = entity.getCapability(ICBMClassicAPI.EXPLOSIVE_CAPABILITY, null);
            final NBTTagCompound tag = provider.getCustomBlastData();
            if (tag != null)
            {
                final ItemStack heldItem = player.getHeldItem(hand);
                if (heldItem.getItem() instanceof IWorldPosItem)
                {
                    final IWorldPosItem posItem = ((IWorldPosItem) heldItem.getItem());
                    final IWorldPosition link = posItem.getLocation(heldItem);

                    if (link instanceof Location)
                    {
                        ((Location) link).writeIntNBT(tag);
                        if (!entity.world.isRemote)
                        {
                            player.sendMessage(new TextComponentString("Coordinates encoded into entity")); //TODO translate
                        }
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private static boolean enderBlockCoordSet(World world, BlockPos pos, EntityPlayer entityPlayer, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        final ItemStack heldItem = entityPlayer.getHeldItem(hand);
        if (heldItem.getItem() instanceof IWorldPosItem)
        {
            final IWorldPosItem posItem = ((IWorldPosItem) heldItem.getItem());
            final IWorldPosition link = posItem.getLocation(heldItem);

            if (link instanceof Location)
            {
                TileEntity tileEntity = world.getTileEntity(pos);

                if (tileEntity.hasCapability(ICBMClassicAPI.EXPLOSIVE_CAPABILITY, facing))
                {
                    IExplosive explosive = tileEntity.getCapability(ICBMClassicAPI.EXPLOSIVE_CAPABILITY, facing);
                    if( explosive != null)
                    {
                        NBTTagCompound tag = new NBTTagCompound();
                        ((Location) link).writeIntNBT(tag);
                        explosive.getCustomBlastData().setTag("", tag);

                        if (!world.isRemote)
                        {
                            //entityPlayer.sendMessage(new TextComponentString("Synced coordinate with " + this.getExplosiveName())); //TODO translate
                        }

                        return true;
                    }
                }
            }
        }

        return false;
    }
}
