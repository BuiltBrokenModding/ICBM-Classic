package icbm.classic.content.blast.ender;

import icbm.classic.ICBMConstants;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.explosion.IBlast;
import icbm.classic.api.reg.IExplosiveCustomization;
import icbm.classic.api.reg.IExplosiveData;
import icbm.classic.api.reg.obj.IBuilderRegistry;
import icbm.classic.lib.LanguageUtility;
import icbm.classic.lib.saving.NbtSaveHandler;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import net.neoforged.neoforge.common.util.INBTSerializable;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

@Data
@NoArgsConstructor
public class EnderBlastCustomization implements IExplosiveCustomization, INBTSerializable<CompoundTag> {

    public static final ResourceLocation NAME = ResourceLocation.fromNamespaceAndPath(ICBMConstants.DOMAIN, "ender");

    /** PORT: 1.12 stored the int dimension id, saved as the dimension ResourceLocation string now */
    private ResourceKey<Level> dim;
    private Vec3 pos;

    private String posTooltip;
    private String dimTooltip;

    public EnderBlastCustomization(ResourceKey<Level> dim, Vec3 pos) {
        this.dim = dim;
        this.pos = pos;
    }

    public void setPos(Vec3 pos) {
        if(!Objects.equals(pos, this.pos)) {
            posTooltip = null;
        }
        this.pos = pos;
    }

    public void setDim(ResourceKey<Level> dim) {
        if(!Objects.equals(dim, this.dim)) {
            dimTooltip = null;
        }
        this.dim = dim;
    }

    @Override
    public void collectCustomizationInformation(Consumer<String> collector) {
        if(pos != null) {
            if(posTooltip == null) {
                posTooltip = LanguageUtility.buildToolTipString(Component.translatable("explosive.icbmclassic:ender.pos", pos.x, pos.y, pos.z));
            }
            collector.accept(posTooltip);
        }
        if(dim != null) {
            if(dimTooltip == null) {
                // 1.12 showed the save name of the dimension's world, only known when the server is local
                final String worldName = Optional.ofNullable(ServerLifecycleHooks.getCurrentServer())
                    .filter(server -> server.getLevel(dim) != null)
                    .map(server -> server.getWorldData().getLevelName())
                    .orElse("???");
                dimTooltip = LanguageUtility.buildToolTipString(Component.translatable("explosive.icbmclassic:ender.world", dim.location().toString(), worldName));
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
            // PORT: 1.12 stored the dimension but never applied it
            ((BlastEnder) blast).setTeleportDim(pos != null ? dim : null);
        }
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        return SAVE_LOGIC.save(this);
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        SAVE_LOGIC.load(this, nbt);
    }

    private static final NbtSaveHandler<EnderBlastCustomization> SAVE_LOGIC = new NbtSaveHandler<EnderBlastCustomization>()
        .mainRoot()
        /* */.nodeVec3d("pos", EnderBlastCustomization::getPos, EnderBlastCustomization::setPos)
        /* */.nodeResourceLocation("dim",
            (c) -> c.getDim() != null ? c.getDim().location() : null,
            (c, rl) -> c.setDim(ResourceKey.create(Registries.DIMENSION, rl)))
        .base();
}
// PORTED
