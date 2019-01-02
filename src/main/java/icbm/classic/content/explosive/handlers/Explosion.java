package icbm.classic.content.explosive.handlers;

import icbm.classic.content.explosive.blast.Blast;
import icbm.classic.content.missile.EntityMissile;
import icbm.classic.content.explosive.Explosive;
import icbm.classic.prefab.tile.EnumTier;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class Explosion extends Explosive
{
    private final Supplier<Blast> factory;

    protected Explosion(String name, EnumTier tier)
    {
        super(name, tier);
        factory = null;
    }

    public Explosion(String name, EnumTier tier, Supplier<Blast> factory)
    {
        super(name, tier);
        this.factory = factory;
    }

    @Override
    public void doCreateExplosion(World world, BlockPos pos, Entity entity, float scale)
    {
        if(factory != null)
        {
            createNew(world, pos, entity, scale).runBlast();
        }
    }

    public Blast createNew(World world, BlockPos pos, Entity entity, float scale)
    {
        if(factory != null)
        {
            Blast blast = factory.get();
            blast.world = world; //TODO create set method
            blast.setPosition(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
            blast.size *= scale;
            blast.exploder = entity;

            return blast;
        }
        return null;
    }

    /** Called when launched. */
    public void launch(EntityMissile missileObj)
    {
    }

    /** Called every tick while flying. */
    public void update(EntityMissile missileObj)
    {
    }

    public boolean onInteract(EntityMissile missileObj, EntityPlayer entityPlayer, EnumHand hand)
    {
        return false;
    }

    /**
     * Is this missile compatible with the cruise launcher?
     *
     * @return
     */
    public boolean isCruise()
    {
        return true;
    }
}
