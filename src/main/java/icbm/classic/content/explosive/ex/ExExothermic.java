package icbm.classic.content.explosive.ex;

import com.builtbroken.mc.api.edit.IWorldChangeAction;
import com.builtbroken.mc.api.event.TriggerCause;
import com.builtbroken.mc.imp.transform.vector.Pos;
import icbm.classic.ICBMClassic;
import icbm.classic.content.explosive.blast.BlastExothermic;
import icbm.classic.prefab.EnumTier;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.config.Configuration;

public class ExExothermic extends Explosion
{
    public boolean createNetherrack = true;


    public ExExothermic()
    {
        super("exothermic", BlockICBM.EnumTier.FOUR);
        this.createNetherrack = ICBMClassic.INSTANCE.getConfig().get(Configuration.CATEGORY_GENERAL, "Exothermic Create Netherrack", createNetherrack).getBoolean(createNetherrack);
        //this.missileModelPath = "missiles/tier3/missile_head_endothermic.obj";
        super("exothermic", EnumTier.FOUR);
    }

    @Override
    public void onYinZha(World worldObj, Pos position, int fuseTicks)
    {
        super.onYinZha(worldObj, position, fuseTicks);
        worldObj.spawnParticle(EnumParticleTypes.LAVA, position.x(), position.y() + 0.5D, position.z(), 0.0D, 0.0D, 0.0D);
    }

    @Override
    public void doCreateExplosion(World world, BlockPos pos, Entity entity)
    {
        new BlastExothermic(world, entity, pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f, 50).explode();
    }

    @Override
    public IWorldChangeAction createBlastForTrigger(World world, double x, double y, double z, TriggerCause triggerCause, double size, NBTTagCompound tag)
    {
        return null;
    }
}
