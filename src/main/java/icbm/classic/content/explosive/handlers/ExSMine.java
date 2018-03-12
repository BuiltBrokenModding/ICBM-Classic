package icbm.classic.content.explosive.handlers;

import icbm.classic.lib.transform.vector.Pos;
import icbm.classic.client.models.ModelSMine;
import icbm.classic.content.explosive.Explosive;
import icbm.classic.content.explosive.blast.BlastMine;
import icbm.classic.prefab.tile.EnumTier;
import icbm.classic.client.models.ModelICBM;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ExSMine extends Explosive
{
    public ExSMine(String mingZi, EnumTier tier)
    {
        super(mingZi, tier);
        this.setFuseTime(20);
        this.hasGrenade = false;
        this.hasMinecart = false;
        this.hasMissile = false;
    }

    @Override
    public void onYinZha(World worldObj, Pos position, int fuseTicks)
    {

    }

    @SideOnly(Side.CLIENT)
    @Override
    public ModelICBM getBlockModel()
    {
        return ModelSMine.INSTANCE;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public ResourceLocation getBlockResource()
    {
        return ModelSMine.TEXTURE;
    }

    @Override
    public void doCreateExplosion(World world, BlockPos pos, Entity entity)
    {
        new BlastMine(world, entity, pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f, 5).explode();
    }
}
