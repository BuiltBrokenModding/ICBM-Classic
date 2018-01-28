package icbm.classic.content.explosive.blast;

import icbm.classic.prefab.ModelICBM;
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
        if (!this.oldWorld().isRemote)
        {
            this.oldWorld().createExplosion(this.controller, position.x(), position.y(), position.z(), 1.5f, true);
        }

        this.controller.motionX = -0.125 + 0.25 * this.oldWorld().rand.nextFloat();
        this.controller.motionY = 0.7 + 0.4 * this.oldWorld().rand.nextFloat();
        this.controller.motionZ = -0.125 + 0.25 * this.oldWorld().rand.nextFloat();
    }

    @Override
    public void doExplode()
    {
        this.controller.motionY -= 0.045;
        this.controller.rotationPitch += 1.5 * this.oldWorld().rand.nextFloat();

        if (!this.oldWorld().isRemote)
        {
            if (this.callCount < 20 * 2 && !this.controller.collided)
            {
                return;
            }

            if (this.callCount >= 20 * 2 && this.callCount % 2 == 0)
            {
                new BlastShrapnel(this.oldWorld(), this.exploder, this.position.x(), this.position.y(), this.position.z(), this.getRadius(), true, true, false).doExplode();
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

    @Override
    public long getEnergy()
    {
        return 8000;
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
