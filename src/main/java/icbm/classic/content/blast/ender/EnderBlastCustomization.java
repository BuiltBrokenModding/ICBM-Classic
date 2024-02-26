package icbm.classic.content.blast.ender;

import icbm.classic.ICBMConstants;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.explosion.IBlast;
import icbm.classic.api.missiles.projectile.IProjectileDataRegistry;
import icbm.classic.api.reg.IExplosiveCustomization;
import icbm.classic.api.reg.IExplosiveData;
import icbm.classic.api.reg.obj.IBuilderRegistry;
import icbm.classic.lib.LanguageUtility;
import icbm.classic.lib.saving.NbtSaveHandler;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

@Data
@NoArgsConstructor
public class EnderBlastCustomization implements IExplosiveCustomization, INBTSerializable<NBTTagCompound> {

    public static final ResourceLocation NAME = new ResourceLocation(ICBMConstants.DOMAIN, "ender");

    private Integer dim;
    private Vec3d pos;

    private String posTooltip;
    private String dimTooltip;

    public EnderBlastCustomization(Integer dim, Vec3d pos) {
        this.dim = dim;
        this.pos = pos;
    }

    public void setPos(Vec3d pos) {
        if(!Objects.equals(pos, this.pos)) {
            posTooltip = null;
        }
        this.pos = pos;
    }

    public void setDim(Integer dim) {
        if(!Objects.equals(dim, this.dim)) {
            dimTooltip = null;
        }
        this.dim = dim;
    }

    @Override
    public void collectCustomizationInformation(Consumer<String> collector) {
        if(pos != null) {
            if(posTooltip == null) {
                posTooltip = LanguageUtility.buildToolTipString(new TextComponentTranslation("explosive.icbmclassic:ender.pos", pos.x, pos.y, pos.z));
            }
            collector.accept(posTooltip);
        }
        if(dim != null) {
            if(dimTooltip == null) {
                final String worldName = Optional.ofNullable(DimensionManager.getWorld(dim)).map(World::getWorldInfo).map(WorldInfo::getWorldName).orElse("???");
                dimTooltip = LanguageUtility.buildToolTipString(new TextComponentTranslation("explosive.icbmclassic:ender.world", dim, worldName));
            }
            collector.accept(dimTooltip);
        }
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
        if(blast instanceof BlastEnder) {
            ((BlastEnder) blast).setTeleportTarget(pos);
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

    private static final NbtSaveHandler<EnderBlastCustomization> SAVE_LOGIC = new NbtSaveHandler<EnderBlastCustomization>()
        .mainRoot()
        /* */.nodeVec3d("pos", EnderBlastCustomization::getPos, EnderBlastCustomization::setPos)
        /* */.nodeInteger("dim", EnderBlastCustomization::getDim, EnderBlastCustomization::setDim)
        .base();
}
