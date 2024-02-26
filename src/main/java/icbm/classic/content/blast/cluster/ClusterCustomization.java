package icbm.classic.content.blast.cluster;

import icbm.classic.ICBMConstants;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.explosion.IBlast;
import icbm.classic.api.missiles.parts.IBuildableObject;
import icbm.classic.api.missiles.projectile.IProjectileData;
import icbm.classic.api.missiles.projectile.IProjectileStack;
import icbm.classic.api.reg.IExplosiveCustomization;
import icbm.classic.api.reg.IExplosiveData;
import icbm.classic.api.reg.obj.IBuilderRegistry;
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
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.function.Consumer;

public class ClusterCustomization implements IExplosiveCustomization, INBTSerializable<NBTTagCompound> {

    public static final ResourceLocation NAME = new ResourceLocation(ICBMConstants.DOMAIN, "cluster");

    /**
     * Projectile to user for spawning the entity
     */
    @Getter
    @Setter
    @Accessors(chain = true)
    private IProjectileData projectileData;

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
        if(projectileData != null) {
            name = LanguageUtility.buildToolTipString(new TextComponentTranslation(projectileData.getTranslationKey()));
        }
        collector.accept(LanguageUtility.buildToolTipString(new TextComponentTranslation("explosive.icbmclassic:cluster.projectile.name", Optional.ofNullable(name).orElse("???"))));

        collector.accept(LanguageUtility.buildToolTipString(new TextComponentTranslation("explosive.icbmclassic:cluster.projectile.count", projectilesToSpawn)));
        collector.accept(LanguageUtility.buildToolTipString(new TextComponentTranslation("explosive.icbmclassic:cluster.projectile.layer", projectilesPerLayer)));
    }

    @Nonnull
    @Override
    public ResourceLocation getRegistryKey() {
        return NAME;
    }

    @Nonnull
    @Override
    public IBuilderRegistry<IExplosiveCustomization> getRegistry() {
        return ICBMClassicAPI.EXPLOSIVE_CUSTOMIZATION_REGISTRY;
    }

    @Override
    public void apply(IExplosiveData explosiveData, IBlast blast) {
        if(blast instanceof BlastCluster) {
            ((BlastCluster) blast).setProjectilesToSpawn(projectilesToSpawn);
            ((BlastCluster) blast).setProjectilesPerLayer(projectilesPerLayer);
            ((BlastCluster) blast).setAllowPickupItem(allowPickupItems);
            ((BlastCluster) blast).setProjectileBuilder((integer) -> projectileData);
        }
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
        /* */.nodeBuildableObject("projectile_data", () -> ICBMClassicAPI.PROJECTILE_DATA_REGISTRY, ClusterCustomization::getProjectileData, ClusterCustomization::setProjectileData)
        /* */.nodeBoolean("projectile_allow_pickup", ClusterCustomization::isAllowPickupItems, ClusterCustomization::setAllowPickupItems)
        /* */.nodeInteger("projectile_count", ClusterCustomization::getProjectilesToSpawn, ClusterCustomization::setProjectilesToSpawn)
        /* */.nodeInteger("projectile_layer", ClusterCustomization::getProjectilesPerLayer, ClusterCustomization::setProjectilesPerLayer)
        .base();
}
