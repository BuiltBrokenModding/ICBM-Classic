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
import icbm.classic.content.blast.BlastEMP;
import icbm.classic.content.blast.BlastEnderman;
import icbm.classic.content.blast.BlastEndothermic;
import icbm.classic.content.blast.BlastExothermic;
import icbm.classic.content.blast.BlastFire;
import icbm.classic.content.blast.BlastGasBase;
import icbm.classic.content.blast.BlastRedmatter;
import icbm.classic.content.blast.BlastShrapnel;
import icbm.classic.content.blast.BlastSonic;
import icbm.classic.content.blast.BlastTNT;
import icbm.classic.content.blast.BlastTNT.PushType;
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

import static icbm.classic.api.EnumExplosiveType.BLOCK;
import static icbm.classic.api.EnumExplosiveType.BOMB_CART;
import static icbm.classic.api.EnumExplosiveType.GRENADE;

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
        ExplosiveRefs.CONDENSED = newEx(0,"condensed", EnumTier.ONE, () -> new BlastTNT().setBlastSize(6));
        ICBMClassicAPI.EX_BLOCK_REGISTRY.setFuseSupplier(ExplosiveRefs.CONDENSED.getRegistryName(),
                (world, type, x, y, z) -> type == BLOCK ? ConfigBlast.FUSE_TIMES.EXPLOSIVES.CONDENSED : type == BOMB_CART ? ConfigBlast.FUSE_TIMES.BOMB_CARTS.CONDENSED : type == GRENADE ? ConfigBlast.FUSE_TIMES.GRENADES.CONVENTIONAL : 100
                );

        ExplosiveRefs.SHRAPNEL = newEx("shrapnel", EnumTier.ONE, () -> new BlastShrapnel().setFlaming().setBlastSize(30));
        ICBMClassicAPI.EX_BLOCK_REGISTRY.setFuseSupplier(ExplosiveRefs.SHRAPNEL.getRegistryName(),
                (world, type, x, y, z) -> type == BLOCK ? ConfigBlast.FUSE_TIMES.EXPLOSIVES.SHRAPNEL : type == BOMB_CART ? ConfigBlast.FUSE_TIMES.BOMB_CARTS.SHRAPNEL : type == GRENADE ? ConfigBlast.FUSE_TIMES.GRENADES.SHRAPNEL : 100
                );

        ExplosiveRefs.INCENDIARY = newEx("incendiary", EnumTier.ONE, () -> new BlastFire().setBlastSize(14));
        ICBMClassicAPI.EX_BLOCK_REGISTRY.setFuseTickListener(ExplosiveRefs.INCENDIARY.getRegistryName(),
                (world, x, y, z, tick) -> world.spawnParticle(EnumParticleTypes.LAVA, x, y + 0.5D, z, 0.0D, 0.0D, 0.0D)
                );
        ICBMClassicAPI.EX_BLOCK_REGISTRY.setFuseSupplier(ExplosiveRefs.INCENDIARY.getRegistryName(),
                (world, type, x, y, z) -> type == BLOCK ? ConfigBlast.FUSE_TIMES.EXPLOSIVES.INCENDIARY : type == BOMB_CART ? ConfigBlast.FUSE_TIMES.BOMB_CARTS.INCENDIARY : type == GRENADE ? ConfigBlast.FUSE_TIMES.GRENADES.INCENDIARY : 100
                );

        ExplosiveRefs.DEBILITATION = newEx("debilitation", EnumTier.ONE,
                () -> new BlastGasBase(20 * 30, false)
                .setConfuse().setBlastSize(20));
        ICBMClassicAPI.EX_BLOCK_REGISTRY.setFuseSupplier(ExplosiveRefs.DEBILITATION.getRegistryName(),
                (world, type, x, y, z) -> type == BLOCK ? ConfigBlast.FUSE_TIMES.EXPLOSIVES.DEBILITATION : type == BOMB_CART ? ConfigBlast.FUSE_TIMES.BOMB_CARTS.DEBILITATION : type == GRENADE ? ConfigBlast.FUSE_TIMES.GRENADES.DEBILITATION : 100
                );

        ExplosiveRefs.CHEMICAL = newEx("chemical", EnumTier.ONE,
                () -> new BlastGasBase(20 * 30, false)
                .setPoison().setRGB(0.8f, 0.8f, 0).setBlastSize(20));
        ICBMClassicAPI.EX_BLOCK_REGISTRY.setFuseSupplier(ExplosiveRefs.CHEMICAL.getRegistryName(),
                (world, type, x, y, z) -> type == BLOCK ? ConfigBlast.FUSE_TIMES.EXPLOSIVES.CHEMICAL : type == BOMB_CART ? ConfigBlast.FUSE_TIMES.BOMB_CARTS.CHEMICAL : type == GRENADE ? ConfigBlast.FUSE_TIMES.GRENADES.CHEMICAL : 100
                );

        ExplosiveRefs.ANVIL = newEx("anvil", EnumTier.ONE,
                () -> new BlastShrapnel().setAnvil().setBlastSize(25));
        ICBMClassicAPI.EX_BLOCK_REGISTRY.setFuseSupplier(ExplosiveRefs.ANVIL.getRegistryName(),
                (world, type, x, y, z) -> type == BLOCK ? ConfigBlast.FUSE_TIMES.EXPLOSIVES.ANVIL : type == BOMB_CART ? ConfigBlast.FUSE_TIMES.BOMB_CARTS.ANVIL : type == GRENADE ? ConfigBlast.FUSE_TIMES.GRENADES.ANVIL : 100
                );

        ExplosiveRefs.REPULSIVE = newEx("repulsive", EnumTier.ONE,
                () -> new BlastTNT().setDestroyItems().setPushType(PushType.REPEL).setBlastSize(2));
        ICBMClassicAPI.EX_BLOCK_REGISTRY.setFuseSupplier(ExplosiveRefs.REPULSIVE.getRegistryName(),
                (world, type, x, y, z) -> type == BLOCK ? ConfigBlast.FUSE_TIMES.EXPLOSIVES.REPULSIVE : type == BOMB_CART ? ConfigBlast.FUSE_TIMES.BOMB_CARTS.REPULSIVE : type == GRENADE ? ConfigBlast.FUSE_TIMES.GRENADES.REPULSIVE : 100
                );

        ExplosiveRefs.ATTRACTIVE = newEx("attractive", EnumTier.ONE,
                () -> new BlastTNT().setDestroyItems().setPushType(PushType.ATTRACT).setBlastSize(2));
        ICBMClassicAPI.EX_BLOCK_REGISTRY.setFuseSupplier(ExplosiveRefs.ATTRACTIVE.getRegistryName(),
                (world, type, x, y, z) -> type == BLOCK ? ConfigBlast.FUSE_TIMES.EXPLOSIVES.ATTRACTIVE : type == BOMB_CART ? ConfigBlast.FUSE_TIMES.BOMB_CARTS.ATTRACTIVE : type == GRENADE ? ConfigBlast.FUSE_TIMES.GRENADES.ATTRACTIVE : 100
                );

        //=================== Tier 2
        ExplosiveRefs.FRAGMENTATION = newEx("fragmentation", EnumTier.TWO,
                () -> new BlastShrapnel().setFlaming().setExplosive().setBlastSize(15));
        ICBMClassicAPI.EX_BLOCK_REGISTRY.setFuseSupplier(ExplosiveRefs.FRAGMENTATION.getRegistryName(),
                (world, type, x, y, z) -> type == BLOCK ? ConfigBlast.FUSE_TIMES.EXPLOSIVES.FRAGMENTATION : type == BOMB_CART ? ConfigBlast.FUSE_TIMES.BOMB_CARTS.FRAGMENTATION : 100
                );

        ExplosiveRefs.CONTAGIOUS = newEx("contagious", EnumTier.TWO,
                () -> new BlastGasBase(20 * 30, false)
                .setContagious().setRGB(0.3f, 0.8f, 0).setBlastSize(20));
        ICBMClassicAPI.EX_BLOCK_REGISTRY.setFuseSupplier(ExplosiveRefs.CONTAGIOUS.getRegistryName(),
                (world, type, x, y, z) -> type == BLOCK ? ConfigBlast.FUSE_TIMES.EXPLOSIVES.CONTAGIOUS : type == BOMB_CART ? ConfigBlast.FUSE_TIMES.BOMB_CARTS.CONTAGIOUS : 100
                );

        ExplosiveRefs.SONIC = newEx("sonic", EnumTier.TWO,
                () -> new BlastSonic().setBlastSize(15));
        ICBMClassicAPI.EX_BLOCK_REGISTRY.setFuseSupplier(ExplosiveRefs.SONIC.getRegistryName(),
                (world, type, x, y, z) -> type == BLOCK ? ConfigBlast.FUSE_TIMES.EXPLOSIVES.SONIC : type == BOMB_CART ? ConfigBlast.FUSE_TIMES.BOMB_CARTS.SONIC : 100
                );

        ExplosiveRefs.BREACHING = newEx("breaching", EnumTier.TWO,
                () -> new BlastBreach(7).setBlastSize(2.5));
        ICBMClassicAPI.EX_BLOCK_REGISTRY.setFuseSupplier(ExplosiveRefs.BREACHING.getRegistryName(),
                (world, type, x, y, z) -> type == BLOCK ? ConfigBlast.FUSE_TIMES.EXPLOSIVES.BREACHING : type == BOMB_CART ? ConfigBlast.FUSE_TIMES.BOMB_CARTS.BREACHING : 100
                );

        ExplosiveRefs.THERMOBARIC = newEx("thermobaric", EnumTier.TWO,
                () -> new BlastNuclear().setEnergy(45).setBlastSize(30));
        ICBMClassicAPI.EX_BLOCK_REGISTRY.setFuseSupplier(ExplosiveRefs.THERMOBARIC.getRegistryName(),
                (world, type, x, y, z) -> type == BLOCK ? ConfigBlast.FUSE_TIMES.EXPLOSIVES.THERMOBARIC : type == BOMB_CART ? ConfigBlast.FUSE_TIMES.BOMB_CARTS.THERMOBARIC : 100
                );

        //=================== Tier 3
        ExplosiveRefs.NUCLEAR = newEx("nuclear", EnumTier.THREE,
                () -> new BlastNuclear().setNuclear().setEnergy(80).setBlastSize(50));
        ICBMClassicAPI.EX_BLOCK_REGISTRY.setFuseSupplier(ExplosiveRefs.NUCLEAR.getRegistryName(),
                (world, type, x, y, z) -> type == BLOCK ? ConfigBlast.FUSE_TIMES.EXPLOSIVES.NUCLEAR : type == BOMB_CART ? ConfigBlast.FUSE_TIMES.BOMB_CARTS.NUCLEAR : 100
                );

        ExplosiveRefs.EMP = newEx("emp", EnumTier.THREE,
                () -> new BlastEMP().setEffectBlocks().setEffectEntities().setBlastSize(50));
        ICBMClassicAPI.EX_BLOCK_REGISTRY.setFuseSupplier(ExplosiveRefs.EMP.getRegistryName(),
                (world, type, x, y, z) -> type == BLOCK ? ConfigBlast.FUSE_TIMES.EXPLOSIVES.EMP : type == BOMB_CART ? ConfigBlast.FUSE_TIMES.BOMB_CARTS.EMP : 100
                );

        ExplosiveRefs.EXOTHERMIC = newEx("exothermic", EnumTier.THREE, () -> new BlastExothermic().setBlastSize(30));
        ICBMClassicAPI.EX_BLOCK_REGISTRY.setFuseTickListener(ExplosiveRefs.EXOTHERMIC.getRegistryName(),
                (world, x, y, z, tick) -> world.spawnParticle(EnumParticleTypes.LAVA, x, y + 0.5D, z, 0.0D, 0.0D, 0.0D)
                );
        ICBMClassicAPI.EX_BLOCK_REGISTRY.setFuseSupplier(ExplosiveRefs.EXOTHERMIC.getRegistryName(),
                (world, type, x, y, z) -> type == BLOCK ? ConfigBlast.FUSE_TIMES.EXPLOSIVES.EXOTHERMIC : type == BOMB_CART ? ConfigBlast.FUSE_TIMES.BOMB_CARTS.EXOTHERMIC : 100
                );

        ExplosiveRefs.ENDOTHERMIC = newEx("endothermic", EnumTier.THREE, () -> new BlastEndothermic().setBlastSize(30));
        //TODO add ice fuse animation
        ICBMClassicAPI.EX_BLOCK_REGISTRY.setFuseSupplier(ExplosiveRefs.ENDOTHERMIC.getRegistryName(),
                (world, type, x, y, z) -> type == BLOCK ? ConfigBlast.FUSE_TIMES.EXPLOSIVES.ENDOTHERMIC : type == BOMB_CART ? ConfigBlast.FUSE_TIMES.BOMB_CARTS.ENDOTHERMIC : 100
                );

        ExplosiveRefs.ANTI_GRAVITATIONAL = newEx("antigravitational", EnumTier.THREE, () -> new BlastAntiGravitational().setBlastSize(30));
        ICBMClassicAPI.EX_BLOCK_REGISTRY.setFuseSupplier(ExplosiveRefs.ANTI_GRAVITATIONAL.getRegistryName(),
                (world, type, x, y, z) -> type == BLOCK ? ConfigBlast.FUSE_TIMES.EXPLOSIVES.ANTI_GRAVITATIONAL : type == BOMB_CART ? ConfigBlast.FUSE_TIMES.BOMB_CARTS.ANTI_GRAVITATIONAL : 100
                );

        ExplosiveRefs.ENDER = newEx("ender", EnumTier.THREE, () -> new BlastEnderman().setBlastSize(30));
        ICBMClassicAPI.EX_MISSILE_REGISTRY.setInteractionListener(ExplosiveRefs.ENDER.getRegistryName(), ExplosiveInit::enderMissileCoordSet);
        ICBMClassicAPI.EX_BLOCK_REGISTRY.setActivationListener(ExplosiveRefs.ENDER.getRegistryName(), ExplosiveInit::enderBlockCoordSet);
        ICBMClassicAPI.EX_BLOCK_REGISTRY.setFuseSupplier(ExplosiveRefs.ENDER.getRegistryName(),
                (world, type, x, y, z) -> type == BLOCK ? ConfigBlast.FUSE_TIMES.EXPLOSIVES.ENDER : type == BOMB_CART ? ConfigBlast.FUSE_TIMES.BOMB_CARTS.ENDER : 100
                );

        ExplosiveRefs.HYPERSONIC = newEx("hypersonic", EnumTier.THREE, () -> new BlastSonic().setShockWave().setBlastSize(20)); //TODO find Missile model
        ICBMClassicAPI.EX_BLOCK_REGISTRY.setFuseSupplier(ExplosiveRefs.HYPERSONIC.getRegistryName(),
                (world, type, x, y, z) -> type == BLOCK ? ConfigBlast.FUSE_TIMES.EXPLOSIVES.HYPERSONIC : type == BOMB_CART ? ConfigBlast.FUSE_TIMES.BOMB_CARTS.HYPERSONIC : 100
                );

        //=================== Tier 4
        ExplosiveRefs.ANTIMATTER = newEx("antimatter", EnumTier.FOUR,
                () -> new BlastAntimatter().setBlastSize(ConfigBlast.ANTIMATTER_SIZE));
        ICBMClassicAPI.EX_BLOCK_REGISTRY.setFuseSupplier(ExplosiveRefs.ANTIMATTER.getRegistryName(),
                (world, type, x, y, z) -> type == BLOCK ? ConfigBlast.FUSE_TIMES.EXPLOSIVES.ANTIMATTER : type == BOMB_CART ? ConfigBlast.FUSE_TIMES.BOMB_CARTS.ANTIMATTER : 100
                );
        //TODO add config (disable by default) for alarm audio

        ExplosiveRefs.REDMATTER = newEx("redMatter", EnumTier.FOUR, () -> new BlastRedmatter().setBlastSize(BlastRedmatter.NORMAL_RADIUS));
        ICBMClassicAPI.EX_BLOCK_REGISTRY.setFuseSupplier(ExplosiveRefs.REDMATTER.getRegistryName(),
                (world, type, x, y, z) -> type == BLOCK ? ConfigBlast.FUSE_TIMES.EXPLOSIVES.REDMATTER : type == BOMB_CART ? ConfigBlast.FUSE_TIMES.BOMB_CARTS.REDMATTER : 100
                );

        //=================== Missiles
        ///* 24 */MISSILE(new MissileModule());
        ///* 25 */MISSILE_HOMING(new MissileHoming());
        ///* 26 */MISSILE_ANTI(new MissileAnti());
        ///* 27 */MISSILE_CLUSTER(new MissileCluster("cluster", EnumTier.TWO));
        ///* 28 */MISSILE_CLUSTER_NUKE(new MissileNuclearCluster())
    }

    private static IExplosiveData newEx(int id, String name, EnumTier tier, IBlastFactory factory)
    {
        if(id != -1)
        {
            //Setup old IDs so saves work
            ((ExplosiveRegistry) ICBMClassicAPI.EX_BLOCK_REGISTRY).forceID(new ResourceLocation(ICBMClassic.DOMAIN, name), id);
        }
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
