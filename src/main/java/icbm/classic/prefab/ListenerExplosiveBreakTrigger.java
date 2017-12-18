package icbm.classic.prefab;

import com.builtbroken.mc.framework.block.imp.*;
import com.builtbroken.mc.framework.json.loading.JsonProcessorData;
import com.builtbroken.mc.lib.helper.MathUtility;
import com.builtbroken.mc.seven.framework.block.listeners.TileListener;
import icbm.classic.content.explosive.blast.BlastRedmatter;
import net.minecraft.block.Block;
import net.minecraft.enchantment.EnchantmentHelper;
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

    @JsonProcessorData(value = "rightClickTriggerChance", type = "float")
    public float rightClickTriggerChance = 0;

    @JsonProcessorData(value = "leftClickTriggerChance", type = "float")
    public float leftClickTriggerChance = 0;

    @JsonProcessorData(value = {"explosiveID", "ex"})
    public String explosiveID;

    @JsonProcessorData(value = "timer", type = "int")
    public int timer = 1000;

    @JsonProcessorData(value = "triggerOnSilkTouch")
    public boolean triggerOnSilkTouch = false;

    public final Block block;

    private boolean handled = false;

    public ListenerExplosiveBreakTrigger(Block block)
    {
        this.block = block;
    }

    @Override
    public void onDestroyedByExplosion(Explosion ex)
    {
        trigger(ex.exploder);
    }

    @Override
    public void breakBlock(Block block, int meta)
    {
        //Trigger for automation
        if (!handled && getBlock() == block)
        {
            trigger(null);
        }
        //Reset
        handled = false;
    }

    @Override
    public boolean removedByPlayer(EntityPlayer player, boolean willHarvest)
    {
        //Note we have already handled the event
        handled = true;

        //Check for silk touch to avoid triggering
        if (!triggerOnSilkTouch || !block.canSilkHarvest(world().unwrap(), player, xi(), yi(), zi(), getBlockMeta()) || EnchantmentHelper.getSilkTouchModifier(player))
        {
            trigger(player);
            //Remove block
            return world().unwrap().setBlockToAir(xi(), yi(), zi());
        }
        return false;
    }

    protected void trigger(Entity entity)
    {
        if (isServer() && explosiveID != null)
        {
            //TODO add other types
            if (explosiveID.equalsIgnoreCase("redmatter"))
            {
                BlastRedmatter blast = new BlastRedmatter(world().unwrap(), entity, x() + 0.5, y() + 0.5, z() + 0.5, size);
                blast.lifeSpan = timer;
                blast.explode();
            }
        }
    }

    @Override
    public boolean onPlayerActivated(EntityPlayer player, int side, float hitX, float hitY, float hitZ)
    {
        if (MathUtility.rand.nextFloat() < rightClickTriggerChance) //TODO check to make sure mining doesn't trigger with silk touch
        {
            trigger(player);
            world().unwrap().setBlockToAir(xi(), yi(), zi());
        }
        return false;
    }

    @Override
    public boolean onPlayerClicked(EntityPlayer player)
    {
        if (MathUtility.rand.nextFloat() < leftClickTriggerChance)
        {
            trigger(player);
            world().unwrap().setBlockToAir(xi(), yi(), zi());
        }
        return false;
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
            return new ListenerExplosiveBreakTrigger(block);
        }

        @Override
        public String getListenerKey()
        {
            return "explosiveCBreakTrigger";
        }
    }
}
