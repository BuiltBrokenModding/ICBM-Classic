package icbm.classic.client.fx;

import icbm.classic.content.reg.BlockReg;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.particle.ParticleSmokeNormal;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@SideOnly(Side.CLIENT)
public class ParticleLauncherSmoke extends ParticleSmokeNormal
{
    public static Set<Block> blocksToIgnoreCollisions = new HashSet();

    public ParticleLauncherSmoke(World worldIn, double x, double y, double z, double vx, double vy, double vz, float scale)
    {
        super(worldIn, x, y, z, vx, vy, vz, scale);
    }

    public ParticleLauncherSmoke setAge(int age)
    {
        this.particleMaxAge = age;
        return this;
    }

    public ParticleLauncherSmoke setColor(float r, float g, float b, boolean addColorVariant)
    {
        this.particleRed = r;
        this.particleGreen = g;
        this.particleBlue = b;

        if (addColorVariant)
        {
            float colorVariant = (float) (Math.random() * 0.90000001192092896D);
            this.particleRed *= colorVariant;
            this.particleBlue *= colorVariant;
            this.particleGreen *= colorVariant;
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

        if (this.canCollide)
        {
            List<AxisAlignedBB> list = ParticleCollisionLogic.getCollisionBoxes(this.world, this.getBoundingBox().expand(x, y, z), ParticleLauncherSmoke::shouldAllowCollision);

            for (AxisAlignedBB axisalignedbb : list)
            {
                y = axisalignedbb.calculateYOffset(this.getBoundingBox(), y);
            }

            this.setBoundingBox(this.getBoundingBox().offset(0.0D, y, 0.0D));

            for (AxisAlignedBB axisalignedbb1 : list)
            {
                x = axisalignedbb1.calculateXOffset(this.getBoundingBox(), x);
            }

            this.setBoundingBox(this.getBoundingBox().offset(x, 0.0D, 0.0D));

            for (AxisAlignedBB axisalignedbb2 : list)
            {
                z = axisalignedbb2.calculateZOffset(this.getBoundingBox(), z);
            }

            this.setBoundingBox(this.getBoundingBox().offset(0.0D, 0.0D, z));
        } else
        {
            this.setBoundingBox(this.getBoundingBox().offset(x, y, z));
        }

        this.resetPositionToBB();
        this.onGround = d0 != y && d0 < 0.0D;

        if (origX != x)
        {
            this.motionX = 0.0D;
        }

        if (origZ != z)
        {
            this.motionZ = 0.0D;
        }
    }

    public static boolean shouldAllowCollision(IBlockState blockState)
    {
        final Block block = blockState.getBlock();
        return block != BlockReg.blockLaunchBase
            && block != BlockReg.blockLaunchSupport
            && block != BlockReg.blockLaunchScreen
            && block != BlockReg.multiBlock
            && !blocksToIgnoreCollisions.contains(block);
    }
}