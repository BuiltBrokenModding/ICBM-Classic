package icbm.classic.prefab;

import com.builtbroken.mc.framework.block.imp.*;
import com.builtbroken.mc.framework.json.loading.JsonProcessorData;
import com.builtbroken.mc.seven.framework.block.listeners.TileListener;
import icbm.classic.content.explosive.blast.BlastRedmatter;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.Explosion;

import java.util.ArrayList;
import java.util.List;

/**
 * Used to trigger an explosion when a block is broken. For use with JSON listener system.
 *
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 12/4/2017.
 */
public class ListenerExplosiveBreakTrigger extends TileListener implements IDestroyedListener, IActivationListener, IBlockListener
{
    @JsonProcessorData(value = "size", type = "float")
    public float size = 1;

    @JsonProcessorData(value = {"explosiveID", "ex"})
    public String explosiveID;

    @JsonProcessorData(value = "timer", type = "int")
    public int timer = 1000;

    @Override
    public void onDestroyedByExplosion(Explosion ex)
    {
        trigger(ex.exploder);
    }

    @Override
    public void breakBlock(Block block, int meta)
    {
        trigger(null);
        //trigger(null); TODO check if this triggers even on silk harvest
    }

    @Override
    public boolean removedByPlayer(EntityPlayer player, boolean willHarvest)
    {
        trigger(player);
        return world().unwrap().setBlockToAir(xi(), yi(), zi());
    }

    protected void trigger(Entity entity)
    {
        if(isServer() && explosiveID != null)
        {
            if (explosiveID.equalsIgnoreCase("redmatter"))
            {
                BlastRedmatter blast = new BlastRedmatter(world().unwrap(), entity, x() + 0.5, y() + 0.5, z() + 0.5, size);
                blast.lifeSpan = timer;
                blast.explode();
            }
        }
    }


    @Override
    public boolean canHarvest(EntityPlayer player, int meta)
    {
        return false; //TODO setup so can only silk harvest
    }

    @Override
    public boolean onPlayerActivated(EntityPlayer player, int side, float hitX, float hitY, float hitZ)
    {
        return false; //TODO chance to explode
    }

    @Override
    public boolean onPlayerClicked(EntityPlayer player)
    {
        return false; //TODO chance to explode
    }

    @Override
    public List<String> getListenerKeys()
    {
        List<String> list = new ArrayList();
        list.add("activation");
        list.add("break");
        return list;
    }

    public static class Builder implements ITileEventListenerBuilder
    {
        @Override
        public ITileEventListener createListener(Block block)
        {
            return new ListenerExplosiveBreakTrigger();
        }

        @Override
        public String getListenerKey()
        {
            return "explosiveCBreakTrigger";
        }
    }
}
