package icbm.classic.content.blast.cluster;

import icbm.classic.ICBMConstants;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.explosion.IBlast;
import icbm.classic.api.missiles.projectile.IProjectileStack;
import icbm.classic.api.reg.IExplosiveCustomization;
import icbm.classic.api.reg.IExplosiveData;
import icbm.classic.lib.LanguageUtility;
import icbm.classic.lib.saving.NbtSaveHandler;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntitySpectralArrow;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

import java.util.Optional;
import java.util.function.Consumer;

public class ClusterCustomization implements IExplosiveCustomization {

    public static final ResourceLocation NAME = new ResourceLocation(ICBMConstants.DOMAIN, "cluster");

    /**
     * Projectile to user for spawning the entity
     */
    @Getter
    @Setter
    @Accessors(chain = true)
    private ItemStack projectileStack;

    /**
     * Number of droplets to spawn
     */
    @Getter
    @Setter
    @Accessors(chain = true)
    private int projectilesToSpawn = 0;

    /**
     * Number of droplets per ejection disc
     */
    @Getter
    @Setter
    @Accessors(chain = true)
    private int projectilesPerLayer = 10;

    /**
     * Allow picking up projectile items, default assumes true as it is assumed
     * a player crafter the items into the cluster.
     */
    @Getter
    @Setter
    @Accessors(chain = true)
    private boolean allowPickupItems = true;

    @Override
    public void collectCustomizationInformation(Consumer<String> collector) {
        // TODO cache to improve performance
        String name = null;
        if(projectileStack != null) {
            if (projectileStack.hasCapability(ICBMClassicAPI.PROJECTILE_STACK_CAPABILITY, null)) {
                final IProjectileStack iProjectileStack = projectileStack.getCapability(ICBMClassicAPI.PROJECTILE_STACK_CAPABILITY, null);
                if (iProjectileStack != null && iProjectileStack.getName() != null) {
                    name = LanguageUtility.buildToolTipString(new TextComponentTranslation(iProjectileStack.getName().toString()));
                }
            }
            if (name == null) {
                name = projectileStack.getDisplayName();
            }
        }
        collector.accept(LanguageUtility.buildToolTipString(new TextComponentTranslation("explosive.icbmclassic:cluster.projectile.name", Optional.ofNullable(name).orElse("???"))));

        collector.accept(LanguageUtility.buildToolTipString(new TextComponentTranslation("explosive.icbmclassic:cluster.projectile.count", projectilesToSpawn)));
        collector.accept(LanguageUtility.buildToolTipString(new TextComponentTranslation("explosive.icbmclassic:cluster.projectile.layer", projectilesPerLayer)));
    }

    @Override
    public ResourceLocation getRegistryName() {
        return NAME;
    }

    @Override
    public void apply(IExplosiveData explosiveData, IBlast blast) {
        if(blast instanceof BlastCluster) {
            ((BlastCluster) blast).setProjectilesToSpawn(projectilesToSpawn);
            ((BlastCluster) blast).setProjectilesPerLayer(projectilesPerLayer);
            ((BlastCluster) blast).setProjectileBuilder(this::spawnProjectile);
        }
    }

    public Entity spawnProjectile(int index, World world) {
        if(projectileStack == null || projectileStack.isEmpty()) {
            return null;
        }

        // Vanilla handling TODO move to some type of 'Item -> spawning registry' or maybe recycle dispenser logic?
        if(projectileStack.getItem() == Items.ARROW) {
            EntityTippedArrow entitytippedarrow = new EntityTippedArrow(world);
            entitytippedarrow.pickupStatus = allowPickupItems ? EntityArrow.PickupStatus.ALLOWED : EntityArrow.PickupStatus.DISALLOWED;
            return entitytippedarrow;
        }
        else if(projectileStack.getItem() == Items.TIPPED_ARROW) {
            EntityTippedArrow entitytippedarrow = new EntityTippedArrow(world);
            entitytippedarrow.setPotionEffect(projectileStack);
            entitytippedarrow.pickupStatus = allowPickupItems ? EntityArrow.PickupStatus.ALLOWED : EntityArrow.PickupStatus.DISALLOWED;
            return entitytippedarrow;
        }
        else if(projectileStack.getItem() == Items.SPECTRAL_ARROW) {
            EntityArrow entityarrow = new EntitySpectralArrow(world);
            entityarrow.pickupStatus = allowPickupItems ? EntityArrow.PickupStatus.ALLOWED : EntityArrow.PickupStatus.DISALLOWED;
            return entityarrow;
        }
        //TODO snowballs
        //TODO tools as projectiles... because diggy diggy dwarf

        if(projectileStack.hasCapability(ICBMClassicAPI.PROJECTILE_STACK_CAPABILITY, null)) {
            final IProjectileStack iProjectileStack = projectileStack.getCapability(ICBMClassicAPI.PROJECTILE_STACK_CAPABILITY, null);
            if(iProjectileStack != null) {
                return iProjectileStack.newEntity(world);
            }
        }
        return null;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        return SAVE_LOGIC.save(this);
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        SAVE_LOGIC.load(this, nbt);
    }

    private static final NbtSaveHandler<ClusterCustomization> SAVE_LOGIC = new NbtSaveHandler<ClusterCustomization>()
        .mainRoot()
        /* */.nodeItemStack("projectile_stack", ClusterCustomization::getProjectileStack, ClusterCustomization::setProjectileStack)
        /* */.nodeBoolean("projectile_allow_pickup", ClusterCustomization::isAllowPickupItems, ClusterCustomization::setAllowPickupItems)
        /* */.nodeInteger("projectile_count", ClusterCustomization::getProjectilesToSpawn, ClusterCustomization::setProjectilesToSpawn)
        /* */.nodeInteger("projectile_layer", ClusterCustomization::getProjectilesPerLayer, ClusterCustomization::setProjectilesPerLayer)
        .base();
}
