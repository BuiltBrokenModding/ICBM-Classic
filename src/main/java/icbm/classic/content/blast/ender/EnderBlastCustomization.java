package icbm.classic.content.blast.ender;

import icbm.classic.ICBMConstants;
import icbm.classic.api.explosion.IBlast;
import icbm.classic.api.reg.IExplosiveCustomization;
import icbm.classic.api.reg.IExplosiveData;
import icbm.classic.lib.saving.NbtSaveHandler;
import icbm.classic.lib.transform.vector.Pos;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;

@Data
@NoArgsConstructor
public class EnderBlastCustomization implements IExplosiveCustomization {

    public static final ResourceLocation NAME = new ResourceLocation(ICBMConstants.DOMAIN, "ender");

    private Integer dim;
    private Vec3d pos;

    public EnderBlastCustomization(Integer dim, Vec3d pos) {
        this.dim = dim;
        this.pos = pos;
    }
    @Override
    public ResourceLocation getRegistryName() {
        return NAME;
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
