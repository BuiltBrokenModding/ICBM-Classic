package icbm.classic.client.fx;

import icbm.classic.content.reg.BlockReg;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.SmokeParticle;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ParticleLauncherSmoke extends SmokeParticle
{
    public static Set<Block> blocksToIgnoreCollisions = new HashSet<>();

    public ParticleLauncherSmoke(ClientLevel worldIn, double x, double y, double z, double vx, double vy, double vz, float scale)
    {
        super(worldIn, x, y, z, vx, vy, vz, scale, SmokeSprites.INSTANCE);
    }

    public ParticleLauncherSmoke setAge(int age)
    {
        this.lifetime = age;
        return this;
    }

    public ParticleLauncherSmoke setColor(float r, float g, float b, boolean addColorVariant)
    {
        this.rCol = r;
        this.gCol = g;
        this.bCol = b;

        if (addColorVariant)
        {
            float colorVariant = (float) (Math.random() * 0.90000001192092896D);
            this.rCol *= colorVariant;
            this.bCol *= colorVariant;
            this.gCol *= colorVariant;
        }
        return this;
    }

    //Re-implementing to allow customizing what we can collide with
    @Override
    public void move(double x, double y, double z)
    {
        double d0 = y;
        double origX = x;
        double origZ = z;

        if (this.hasPhysics)
        {
            AABB box = this.getBoundingBox();
            final List<VoxelShape> list = ParticleCollisionLogic.getCollisionBoxes(this.level, box.expandTowards(x, y, z), ParticleLauncherSmoke::shouldAllowCollision);

            y = Shapes.collide(Direction.Axis.Y, box, list, y);
            box = box.move(0.0D, y, 0.0D);

            x = Shapes.collide(Direction.Axis.X, box, list, x);
            box = box.move(x, 0.0D, 0.0D);

            z = Shapes.collide(Direction.Axis.Z, box, list, z);
            box = box.move(0.0D, 0.0D, z);

            this.setBoundingBox(box);
        }
        else
        {
            this.setBoundingBox(this.getBoundingBox().move(x, y, z));
        }

        this.setLocationFromBoundingbox();
        this.onGround = d0 != y && d0 < 0.0D;

        if (origX != x)
        {
            this.xd = 0.0D;
        }

        if (origZ != z)
        {
            this.zd = 0.0D;
        }
    }

    public static boolean shouldAllowCollision(BlockState blockState)
    {
        final Block block = blockState.getBlock();
        return block != BlockReg.blockLaunchBase.get()
            && block != BlockReg.blockLaunchSupport.get()
            && block != BlockReg.blockLaunchScreen.get()
            && !blocksToIgnoreCollisions.contains(block);
    }
}
// PORTED
