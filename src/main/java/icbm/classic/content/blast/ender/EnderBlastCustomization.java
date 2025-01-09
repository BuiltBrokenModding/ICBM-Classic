package icbm.classic.content.blast.ender;

import icbm.classic.ICBMConstants;
import icbm.classic.api.explosion.IBlast;
import icbm.classic.api.reg.IExplosiveData;
import icbm.classic.lib.LanguageUtility;
import icbm.classic.lib.saving.NbtSaveHandler;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.function.Consumer;

@Data
@NoArgsConstructor
public class EnderBlastCustomization implements INBTSerializable<CompoundNBT> {

    public static final ResourceLocation NAME = new ResourceLocation(ICBMConstants.DOMAIN, "ender");

    private ResourceLocation dim;
    private Vec3d pos;

    private String posTooltip;
    private String dimTooltip;

    public EnderBlastCustomization(ResourceLocation dim, Vec3d pos) {
        this.dim = dim;
        this.pos = pos;
    }

    public void setPos(Vec3d pos) {
        if(!Objects.equals(pos, this.pos)) {
            posTooltip = null;
        }
        this.pos = pos;
    }

    public void setDim(ResourceLocation dim) {
        if(!Objects.equals(dim, this.dim)) {
            dimTooltip = null;
        }
        this.dim = dim;
    }

    public void collectCustomizationInformation(Consumer<String> collector) {
        if(pos != null) {
            if(posTooltip == null) {
                posTooltip = LanguageUtility.buildToolTipString(new TranslationTextComponent("explosive.icbmclassic:ender.pos", pos.x, pos.y, pos.z));
            }
            collector.accept(posTooltip);
        }
        if(dim != null) {
            if(dimTooltip == null) {
                dimTooltip = LanguageUtility.buildToolTipString(new TranslationTextComponent("explosive.icbmclassic:ender.world", dim));
            }
            collector.accept(dimTooltip);
        }
    }

    public void apply(IExplosiveData explosiveData, IBlast blast) {
        if(blast instanceof BlastEnder) {
            ((BlastEnder) blast).setTeleportTarget(pos);
        }
    }

    @Override
    public CompoundNBT serializeNBT() {
        return SAVE_LOGIC.save(this);
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        SAVE_LOGIC.load(this, nbt);
    }

    private static final NbtSaveHandler<EnderBlastCustomization> SAVE_LOGIC = new NbtSaveHandler<EnderBlastCustomization>()
        .mainRoot()
        /* */.nodeVec3d("pos", EnderBlastCustomization::getPos, EnderBlastCustomization::setPos)
        /* */.nodeResourceLocation("dim", EnderBlastCustomization::getDim, EnderBlastCustomization::setDim)
        .base();
}
