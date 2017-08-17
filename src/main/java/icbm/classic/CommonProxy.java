package icbm.classic;

import com.builtbroken.jlib.data.vector.IPos3D;
import com.builtbroken.mc.framework.mod.AbstractProxy;
import com.builtbroken.mc.imp.transform.vector.Pos;
import icbm.classic.content.entity.EntityMissile;
import net.minecraft.entity.Entity;
import net.minecraft.server.gui.IUpdatePlayerListBox;
import net.minecraft.world.World;

import java.util.List;

public class CommonProxy extends AbstractProxy
{
    public boolean isGaoQing()
    {
        return false;
    }

    public void spawnParticle(String name, World world, IPos3D position, float scale, double distance)
    {
        this.spawnParticle(name, world, position, 0, 0, 0, scale, distance);
    }

    public void spawnParticle(String name, World world, IPos3D position, double motionX, double motionY, double motionZ, float scale, double distance)
    {
        this.spawnParticle(name, world, position, motionX, motionY, motionZ, 1, 1, 1, scale, distance);
    }

    public void spawnParticle(String name, World world, IPos3D position, double motionX, double motionY, double motionZ, float red, float green, float blue, float scale, double distance)
    {

    }

    public IUpdatePlayerListBox getDaoDanShengYin(EntityMissile eDaoDan)
    {
        return null;
    }

    public int getParticleSetting()
    {
        return -1;
    }

    public List<Entity> getEntityFXs()
    {
        return null;
    }

    public void spawnShock(World world, Pos position, Pos target)
    {

    }

    public void spawnShock(World world, IPos3D startVec, IPos3D targetVec, int duration)
    {
        // TODO Auto-generated method stub

    }
}
