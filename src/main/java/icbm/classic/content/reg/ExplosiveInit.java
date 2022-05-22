package icbm.classic.content.reg;

import icbm.classic.ICBMConstants;
import icbm.classic.api.EnumTier;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.caps.IExplosive;
import icbm.classic.api.data.IWorldPosition;
import icbm.classic.api.explosion.IBlastFactory;
import icbm.classic.api.items.IWorldPosItem;
import icbm.classic.api.refs.ICBMExplosives;
import icbm.classic.api.reg.IExplosiveData;
import icbm.classic.config.blast.ConfigBlast;
import icbm.classic.content.blast.BlastAntiGravitational;
import icbm.classic.content.blast.BlastBreach;
import icbm.classic.content.blast.BlastEMP;
import icbm.classic.content.blast.BlastEnderman;
import icbm.classic.content.blast.BlastEndothermic;
import icbm.classic.content.blast.BlastExothermic;
import icbm.classic.content.blast.BlastFire;
import icbm.classic.content.blast.BlastMutation;
import icbm.classic.content.blast.BlastRot;
import icbm.classic.content.blast.BlastShrapnel;
import icbm.classic.content.blast.BlastSonic;
import icbm.classic.content.blast.BlastTNT;
import icbm.classic.content.blast.BlastTNT.PushType;
import icbm.classic.content.blast.gas.BlastChemical;
import icbm.classic.content.blast.gas.BlastColor;
import icbm.classic.content.blast.gas.BlastConfusion;
import icbm.classic.content.blast.gas.BlastContagious;
import icbm.classic.content.blast.redmatter.BlastRedmatterSpawner;
import icbm.classic.content.blast.threaded.BlastAntimatter;
import icbm.classic.content.blast.threaded.BlastNuclear;
import icbm.classic.lib.explosive.reg.ExplosiveRegistry;
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

    //the fuse suppliers all return 100 (default in ExBlockContentReg#getFuseTime) when an unknown enum value is given as a type
    public static void init()
    {
        //=================== Tier 1
        ICBMExplosives.CONDENSED = newEx(0, "condensed", EnumTier.ONE, () -> new BlastTNT().setBlastSize(6));
        ICBMClassicAPI.EX_BLOCK_REGISTRY.setFuseSupplier(ICBMExplosives.CONDENSED.getRegistryName(), (world, x, y, z) -> ConfigBlast.FUSE_TIMES.EXPLOSIVES.CONDENSED);
        ICBMClassicAPI.EX_GRENADE_REGISTRY.setFuseSupplier(ICBMExplosives.CONDENSED.getRegistryName(), (entity) -> ConfigBlast.FUSE_TIMES.GRENADES.CONVENTIONAL);
        ICBMClassicAPI.EX_MINECART_REGISTRY.setFuseSupplier(ICBMExplosives.CONDENSED.getRegistryName(), (entity) -> ConfigBlast.FUSE_TIMES.BOMB_CARTS.CONDENSED);


        ICBMExplosives.SHRAPNEL = newEx(1, "shrapnel", EnumTier.ONE, () -> new BlastShrapnel().setFlaming().setBlastSize(30));
        ICBMClassicAPI.EX_BLOCK_REGISTRY.setFuseSupplier(ICBMExplosives.SHRAPNEL.getRegistryName(), (world, x, y, z) -> ConfigBlast.FUSE_TIMES.EXPLOSIVES.SHRAPNEL);
        ICBMClassicAPI.EX_GRENADE_REGISTRY.setFuseSupplier(ICBMExplosives.SHRAPNEL.getRegistryName(), (entity) -> ConfigBlast.FUSE_TIMES.GRENADES.SHRAPNEL);
        ICBMClassicAPI.EX_MINECART_REGISTRY.setFuseSupplier(ICBMExplosives.SHRAPNEL.getRegistryName(), (entity) -> ConfigBlast.FUSE_TIMES.BOMB_CARTS.SHRAPNEL);


        ICBMExplosives.INCENDIARY = newEx(2, "incendiary", EnumTier.ONE, () -> new BlastFire().setBlastSize(14));
        ICBMClassicAPI.EX_BLOCK_REGISTRY.setFuseTickListener(ICBMExplosives.INCENDIARY.getRegistryName(),
                (world, x, y, z, tick) -> world.spawnParticle(EnumParticleTypes.LAVA, x, y + 0.5D, z, 0.0D, 0.0D, 0.0D)
                );
        ICBMClassicAPI.EX_BLOCK_REGISTRY.setFuseSupplier(ICBMExplosives.INCENDIARY.getRegistryName(), (world, x, y, z) -> ConfigBlast.FUSE_TIMES.EXPLOSIVES.INCENDIARY);
        ICBMClassicAPI.EX_GRENADE_REGISTRY.setFuseSupplier(ICBMExplosives.INCENDIARY.getRegistryName(), (entity) -> ConfigBlast.FUSE_TIMES.GRENADES.INCENDIARY);
        ICBMClassicAPI.EX_MINECART_REGISTRY.setFuseSupplier(ICBMExplosives.INCENDIARY.getRegistryName(), (entity) -> ConfigBlast.FUSE_TIMES.BOMB_CARTS.INCENDIARY);


        ICBMExplosives.DEBILITATION = newEx(3, "debilitation", EnumTier.ONE,
                () -> new BlastConfusion().setBlastSize(20));

        ICBMClassicAPI.EX_BLOCK_REGISTRY.setFuseSupplier(ICBMExplosives.DEBILITATION.getRegistryName(), (world, x, y, z) -> ConfigBlast.FUSE_TIMES.EXPLOSIVES.DEBILITATION);
        ICBMClassicAPI.EX_GRENADE_REGISTRY.setFuseSupplier(ICBMExplosives.DEBILITATION.getRegistryName(), (entity) -> ConfigBlast.FUSE_TIMES.GRENADES.DEBILITATION);
        ICBMClassicAPI.EX_MINECART_REGISTRY.setFuseSupplier(ICBMExplosives.DEBILITATION.getRegistryName(), (entity) -> ConfigBlast.FUSE_TIMES.BOMB_CARTS.DEBILITATION);


        ICBMExplosives.CHEMICAL = newEx(4, "chemical", EnumTier.ONE,
                () -> new BlastChemical().setBlastSize(20));

        ICBMClassicAPI.EX_BLOCK_REGISTRY.setFuseSupplier(ICBMExplosives.CHEMICAL.getRegistryName(), (world, x, y, z) -> ConfigBlast.FUSE_TIMES.EXPLOSIVES.CHEMICAL);
        ICBMClassicAPI.EX_GRENADE_REGISTRY.setFuseSupplier(ICBMExplosives.CHEMICAL.getRegistryName(), (entity) -> ConfigBlast.FUSE_TIMES.GRENADES.CHEMICAL);
        ICBMClassicAPI.EX_MINECART_REGISTRY.setFuseSupplier(ICBMExplosives.CHEMICAL.getRegistryName(), (entity) -> ConfigBlast.FUSE_TIMES.BOMB_CARTS.CHEMICAL);


        ICBMExplosives.ANVIL = newEx(5, "anvil", EnumTier.ONE,
                () -> new BlastShrapnel().setAnvil().setBlastSize(25));

        ICBMClassicAPI.EX_BLOCK_REGISTRY.setFuseSupplier(ICBMExplosives.ANVIL.getRegistryName(), (world, x, y, z) -> ConfigBlast.FUSE_TIMES.EXPLOSIVES.ANVIL);
        ICBMClassicAPI.EX_GRENADE_REGISTRY.setFuseSupplier(ICBMExplosives.ANVIL.getRegistryName(), (entity) -> ConfigBlast.FUSE_TIMES.GRENADES.ANVIL);
        ICBMClassicAPI.EX_MINECART_REGISTRY.setFuseSupplier(ICBMExplosives.ANVIL.getRegistryName(), (entity) -> ConfigBlast.FUSE_TIMES.BOMB_CARTS.ANVIL);


        ICBMExplosives.REPULSIVE = newEx(6, "repulsive", EnumTier.ONE,
                () -> new BlastTNT().setDestroyItems().setPushType(PushType.REPEL).setBlastSize(2));

        ICBMClassicAPI.EX_BLOCK_REGISTRY.setFuseSupplier(ICBMExplosives.REPULSIVE.getRegistryName(), (world, x, y, z) -> ConfigBlast.FUSE_TIMES.EXPLOSIVES.REPULSIVE);
        ICBMClassicAPI.EX_GRENADE_REGISTRY.setFuseSupplier(ICBMExplosives.REPULSIVE.getRegistryName(), (entity) -> ConfigBlast.FUSE_TIMES.GRENADES.REPULSIVE);
        ICBMClassicAPI.EX_MINECART_REGISTRY.setFuseSupplier(ICBMExplosives.REPULSIVE.getRegistryName(), (entity) -> ConfigBlast.FUSE_TIMES.BOMB_CARTS.REPULSIVE);


        ICBMExplosives.ATTRACTIVE = newEx(7, "attractive", EnumTier.ONE,
                () -> new BlastTNT().setDestroyItems().setPushType(PushType.ATTRACT).setBlastSize(2));

        ICBMClassicAPI.EX_BLOCK_REGISTRY.setFuseSupplier(ICBMExplosives.ATTRACTIVE.getRegistryName(), (world, x, y, z) -> ConfigBlast.FUSE_TIMES.EXPLOSIVES.ATTRACTIVE);
        ICBMClassicAPI.EX_GRENADE_REGISTRY.setFuseSupplier(ICBMExplosives.ATTRACTIVE.getRegistryName(), (entity) -> ConfigBlast.FUSE_TIMES.GRENADES.ATTRACTIVE);
        ICBMClassicAPI.EX_MINECART_REGISTRY.setFuseSupplier(ICBMExplosives.ATTRACTIVE.getRegistryName(), (entity) -> ConfigBlast.FUSE_TIMES.BOMB_CARTS.ATTRACTIVE);


        //=================== Tier 2
        ICBMExplosives.FRAGMENTATION = newEx(8, "fragmentation", EnumTier.TWO,
                () -> new BlastShrapnel().setExplosive().setBlastSize(15));
        ICBMClassicAPI.EX_BLOCK_REGISTRY.setFuseSupplier(ICBMExplosives.FRAGMENTATION.getRegistryName(), (world, x, y, z) -> ConfigBlast.FUSE_TIMES.EXPLOSIVES.FRAGMENTATION);
        ICBMClassicAPI.EX_MINECART_REGISTRY.setFuseSupplier(ICBMExplosives.FRAGMENTATION.getRegistryName(), (entity) -> ConfigBlast.FUSE_TIMES.BOMB_CARTS.FRAGMENTATION);
        //TODO add fire version of fragmentation with smaller animated flames

        ICBMExplosives.CONTAGIOUS = newEx(9, "contagious", EnumTier.TWO,
                () -> new BlastContagious().setBlastSize(20));
        ICBMClassicAPI.EX_BLOCK_REGISTRY.setFuseSupplier(ICBMExplosives.CONTAGIOUS.getRegistryName(), (world, x, y, z) -> ConfigBlast.FUSE_TIMES.EXPLOSIVES.CONTAGIOUS);
        ICBMClassicAPI.EX_MINECART_REGISTRY.setFuseSupplier(ICBMExplosives.CONTAGIOUS.getRegistryName(), (entity) -> ConfigBlast.FUSE_TIMES.BOMB_CARTS.CONTAGIOUS);


        ICBMExplosives.SONIC = newEx(10, "sonic", EnumTier.TWO,
                () -> new BlastSonic().setBlastSize(15));
        ICBMClassicAPI.EX_BLOCK_REGISTRY.setFuseSupplier(ICBMExplosives.SONIC.getRegistryName(), (world, x, y, z) -> ConfigBlast.FUSE_TIMES.EXPLOSIVES.SONIC);
        ICBMClassicAPI.EX_MINECART_REGISTRY.setFuseSupplier(ICBMExplosives.SONIC.getRegistryName(), (entity) -> ConfigBlast.FUSE_TIMES.BOMB_CARTS.SONIC);


        ICBMExplosives.BREACHING = newEx(11, "breaching", EnumTier.TWO,
                () -> new BlastBreach(7).setBlastSize(2.5));
        ICBMClassicAPI.EX_BLOCK_REGISTRY.setFuseSupplier(ICBMExplosives.BREACHING.getRegistryName(), (world, x, y, z) -> ConfigBlast.FUSE_TIMES.EXPLOSIVES.BREACHING);
        ICBMClassicAPI.EX_MINECART_REGISTRY.setFuseSupplier(ICBMExplosives.BREACHING.getRegistryName(), (entity) -> ConfigBlast.FUSE_TIMES.BOMB_CARTS.BREACHING);


        //12 -> Regen

        ICBMExplosives.THERMOBARIC = newEx(13, "thermobaric", EnumTier.TWO,
                () -> new BlastNuclear().setEnergy(45).setBlastSize(30));
        ICBMClassicAPI.EX_BLOCK_REGISTRY.setFuseSupplier(ICBMExplosives.THERMOBARIC.getRegistryName(), (world, x, y, z) -> ConfigBlast.FUSE_TIMES.EXPLOSIVES.THERMOBARIC);
        ICBMClassicAPI.EX_MINECART_REGISTRY.setFuseSupplier(ICBMExplosives.THERMOBARIC.getRegistryName(), (entity) -> ConfigBlast.FUSE_TIMES.BOMB_CARTS.THERMOBARIC);

        //14 -> S-Mine

        //=================== Tier 3
        ICBMExplosives.NUCLEAR = newEx(15, "nuclear", EnumTier.THREE,
                () -> new BlastNuclear().setNuclear().setEnergy(80).setBlastSize(50));
        ICBMClassicAPI.EX_BLOCK_REGISTRY.setFuseSupplier(ICBMExplosives.NUCLEAR.getRegistryName(), (world, x, y, z) -> ConfigBlast.FUSE_TIMES.EXPLOSIVES.NUCLEAR);
        ICBMClassicAPI.EX_MINECART_REGISTRY.setFuseSupplier(ICBMExplosives.NUCLEAR.getRegistryName(), (entity) -> ConfigBlast.FUSE_TIMES.BOMB_CARTS.NUCLEAR);


        ICBMExplosives.EMP = newEx(16, "emp", EnumTier.THREE,
                () -> new BlastEMP().setEffectBlocks().setEffectEntities().setBlastSize(50));
        ICBMClassicAPI.EX_BLOCK_REGISTRY.setFuseSupplier(ICBMExplosives.EMP.getRegistryName(), (world, x, y, z) -> ConfigBlast.FUSE_TIMES.EXPLOSIVES.EMP);
        ICBMClassicAPI.EX_MINECART_REGISTRY.setFuseSupplier(ICBMExplosives.EMP.getRegistryName(), (entity) -> ConfigBlast.FUSE_TIMES.BOMB_CARTS.EMP);


        ICBMExplosives.EXOTHERMIC = newEx(17, "exothermic", EnumTier.THREE, () -> new BlastExothermic().setBlastSize(30));
        ICBMClassicAPI.EX_BLOCK_REGISTRY.setFuseTickListener(ICBMExplosives.EXOTHERMIC.getRegistryName(),
                (world, x, y, z, tick) -> world.spawnParticle(EnumParticleTypes.LAVA, x, y + 0.5D, z, 0.0D, 0.0D, 0.0D));
        ICBMClassicAPI.EX_BLOCK_REGISTRY.setFuseSupplier(ICBMExplosives.EXOTHERMIC.getRegistryName(), (world, x, y, z) -> ConfigBlast.FUSE_TIMES.EXPLOSIVES.EXOTHERMIC);
        ICBMClassicAPI.EX_MINECART_REGISTRY.setFuseSupplier(ICBMExplosives.EXOTHERMIC.getRegistryName(), (entity) -> ConfigBlast.FUSE_TIMES.BOMB_CARTS.EXOTHERMIC);


        ICBMExplosives.ENDOTHERMIC = newEx(18, "endothermic", EnumTier.THREE, () -> new BlastEndothermic().setBlastSize(30));
        //TODO add ice fuse animation
        ICBMClassicAPI.EX_BLOCK_REGISTRY.setFuseSupplier(ICBMExplosives.ENDOTHERMIC.getRegistryName(), (world, x, y, z) -> ConfigBlast.FUSE_TIMES.EXPLOSIVES.ENDOTHERMIC);
        ICBMClassicAPI.EX_MINECART_REGISTRY.setFuseSupplier(ICBMExplosives.ENDOTHERMIC.getRegistryName(), (entity) -> ConfigBlast.FUSE_TIMES.BOMB_CARTS.ENDOTHERMIC);


        ICBMExplosives.ANTI_GRAVITATIONAL = newEx(19, "antigravitational", EnumTier.THREE, () -> new BlastAntiGravitational().setBlastSize(30));
        ICBMClassicAPI.EX_BLOCK_REGISTRY.setFuseSupplier(ICBMExplosives.ANTI_GRAVITATIONAL.getRegistryName(), (world, x, y, z) -> ConfigBlast.FUSE_TIMES.EXPLOSIVES.ANTI_GRAVITATIONAL);
        ICBMClassicAPI.EX_MINECART_REGISTRY.setFuseSupplier(ICBMExplosives.ANTI_GRAVITATIONAL.getRegistryName(), (entity) -> ConfigBlast.FUSE_TIMES.BOMB_CARTS.ANTI_GRAVITATIONAL);


        ICBMExplosives.ENDER = newEx(20, "ender", EnumTier.THREE, () -> new BlastEnderman().setBlastSize(30));
        ICBMClassicAPI.EX_MISSILE_REGISTRY.setInteractionListener(ICBMExplosives.ENDER.getRegistryName(), ExplosiveInit::enderMissileCoordSet);
        ICBMClassicAPI.EX_BLOCK_REGISTRY.setActivationListener(ICBMExplosives.ENDER.getRegistryName(), ExplosiveInit::enderBlockCoordSet);
        ICBMClassicAPI.EX_BLOCK_REGISTRY.setFuseSupplier(ICBMExplosives.ENDER.getRegistryName(), (world, x, y, z) -> ConfigBlast.FUSE_TIMES.EXPLOSIVES.ENDER);
        ICBMClassicAPI.EX_MINECART_REGISTRY.setFuseSupplier(ICBMExplosives.ENDER.getRegistryName(), (entity) -> ConfigBlast.FUSE_TIMES.BOMB_CARTS.ENDER);


        ICBMExplosives.HYPERSONIC = newEx(21, "hypersonic", EnumTier.THREE, () -> new BlastSonic().setShockWave().setBlastSize(20)); //TODO find Missile model
        ICBMClassicAPI.EX_BLOCK_REGISTRY.setFuseSupplier(ICBMExplosives.HYPERSONIC.getRegistryName(), (world, x, y, z) -> ConfigBlast.FUSE_TIMES.EXPLOSIVES.HYPERSONIC);
        ICBMClassicAPI.EX_MINECART_REGISTRY.setFuseSupplier(ICBMExplosives.HYPERSONIC.getRegistryName(), (entity) -> ConfigBlast.FUSE_TIMES.BOMB_CARTS.HYPERSONIC);

        //=================== Tier 4
        ICBMExplosives.ANTIMATTER = newEx(22, "antimatter", EnumTier.FOUR,
                () -> new BlastAntimatter().setBlastSize(ConfigBlast.ANTIMATTER_SIZE));
        ICBMClassicAPI.EX_BLOCK_REGISTRY.setFuseSupplier(ICBMExplosives.ANTIMATTER.getRegistryName(), (world, x, y, z) -> ConfigBlast.FUSE_TIMES.EXPLOSIVES.ANTIMATTER);
        ICBMClassicAPI.EX_MINECART_REGISTRY.setFuseSupplier(ICBMExplosives.ANTIMATTER.getRegistryName(), (entity) -> ConfigBlast.FUSE_TIMES.BOMB_CARTS.ANTIMATTER);

        //TODO add config (disable by default) for alarm audio

        ICBMExplosives.REDMATTER = newEx(23, "redMatter", EnumTier.FOUR, () -> new BlastRedmatterSpawner());
        ICBMClassicAPI.EX_BLOCK_REGISTRY.setFuseSupplier(ICBMExplosives.REDMATTER.getRegistryName(), (world, x, y, z) -> ConfigBlast.FUSE_TIMES.EXPLOSIVES.REDMATTER);
        ICBMClassicAPI.EX_MINECART_REGISTRY.setFuseSupplier(ICBMExplosives.REDMATTER.getRegistryName(), (entity) -> ConfigBlast.FUSE_TIMES.BOMB_CARTS.REDMATTER);

        //=================== No content, only blast

        // originally "mutation" had the id 24, but to keep metadata consistent, the missile module now has metadata 24
        ICBMExplosives.MISSILEMODULE = newEx(24, "missile",  EnumTier.NONE, null);

        ICBMExplosives.ROT = newEx(25, "rot", EnumTier.NONE, BlastRot::new);
        ICBMExplosives.MUTATION = newEx(26, "mutation", EnumTier.NONE, BlastMutation::new);

        //New Explosives not part of classic original
        ICBMExplosives.COLOR = newEx(27, "colors", EnumTier.ONE,
                () -> new BlastColor().setBlastSize(10));

        ((ExplosiveRegistry) ICBMClassicAPI.EXPLOSIVE_REGISTRY).lockForce();

        //=================== Missiles
        ///* 24 */MISSILE(new MissileModule());
        ///* 25 */MISSILE_HOMING(new MissileHoming());
        ///* 26 */MISSILE_ANTI(new MissileAnti());
        ///* 27 */MISSILE_CLUSTER(new MissileCluster("cluster", EnumTier.TWO));
        ///* 28 */MISSILE_CLUSTER_NUKE(new MissileNuclearCluster())


    }

    private static IExplosiveData newEx(int id, String name, EnumTier tier, IBlastFactory factory)
    {
        if (id != -1)
        {
            //Setup old IDs so saves work
            ((ExplosiveRegistry) ICBMClassicAPI.EXPLOSIVE_REGISTRY).forceID(new ResourceLocation(ICBMConstants.DOMAIN, name), id);
        }
        return ICBMClassicAPI.EXPLOSIVE_REGISTRY.register(new ResourceLocation(ICBMConstants.DOMAIN, name), tier, factory);
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
                    if (explosive != null)
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
