package icbm.classic.content.blast;

import icbm.classic.client.models.ModelICBM;
import icbm.classic.client.models.ModelSMine;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;import net.minecraftforge.fml.relauncher.SideOnly;

public class BlastMine extends Blast
{
    public BlastMine(World world, Entity entity, double x, double y, double z, float size)
    {
        super(world, entity, x, y, z, size);
    }

    @Override
    public void doPreExplode()
    {
        if (!this.world().isRemote)
        {
            this.world().createExplosion(this.controller, location.x(), location.y(), location.z(), 1.5f, true);
        }

        this.controller.motionX = -0.125 + 0.25 * this.world().rand.nextFloat();
        this.controller.motionY = 0.7 + 0.4 * this.world().rand.nextFloat();
        this.controller.motionZ = -0.125 + 0.25 * this.world().rand.nextFloat();
    }

    @Override
    public void doExplode()
    {
        this.controller.motionY -= 0.045;
        this.controller.rotationPitch += 1.5 * this.world().rand.nextFloat();

        if (!this.world().isRemote)
        {
            if (this.callCount < 20 * 2 && !this.controller.collided)
            {
                return;
            }

            if (this.callCount >= 20 * 2 && this.callCount % 2 == 0)
            {
                new BlastShrapnel().setFlaming().setExplosive().setBlastSize(getBlastRadius()).setBlastWorld(world).setPosition(x, y, z).runBlast();
            }

            if (this.callCount >= 20 * 2 + 20)
            {
                this.controller.endExplosion();
            }
        }
    }

    @Override
    public boolean isMovable()
    {
        return true;
    }

    @Override
    public int proceduralInterval()
    {
        return 1;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public ModelICBM getRenderModel()
    {
        return ModelSMine.INSTANCE;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public ResourceLocation getRenderResource()
    {
        return ModelSMine.TEXTURE;
    }

}
