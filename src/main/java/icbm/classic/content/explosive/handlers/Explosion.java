package icbm.classic.content.explosive.handlers;

import icbm.classic.api.explosion.IBlast;
import icbm.classic.api.explosion.IBlastFactory;
import icbm.classic.api.explosion.IBlastInit;
import icbm.classic.content.missile.EntityMissile;
import icbm.classic.content.explosive.Explosive;
import icbm.classic.prefab.tile.EnumTier;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class Explosion extends Explosive
{
    private final IBlastFactory factory;

    protected Explosion(String name, EnumTier tier)
    {
        super(name, tier);
        factory = null;
    }

    public Explosion(String name, EnumTier tier, IBlastFactory factory)
    {
        super(name, tier);
        this.factory = factory;
    }

    @Override
    public void doCreateExplosion(World world, BlockPos pos, Entity entity, float scale)
    {
        if (factory != null)
        {
            createNew(world, pos, entity, scale).runBlast();
        }
    }

    public IBlast createNew(World world, BlockPos pos, Entity entity, float scale)
    {
        if (factory != null)
        {
            IBlastInit blast = factory.createNewBlast();
            blast.setBlastWorld(world); //TODO create set method
            blast.setBlastPosition(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
            blast.scaleBlast(scale);
            blast.setBlastSource(entity);

            return blast.buildBlast();
        }
        return null;
    }

    /**
     * Called when launched.
     */
    public void launch(EntityMissile missileObj) //TODO move to interface
    {
    }

    /**
     * Called every tick while flying.
     */
    public void update(EntityMissile missileObj) //TODO move to interface
    {
    }

    public boolean onInteract(EntityMissile missileObj, EntityPlayer entityPlayer, EnumHand hand) //TODO move to interface
    {
        return false;
    }

    /**
     * Is this missile compatible with the cruise launcher?
     *
     * @return
     */
    public boolean isCruise()  //TODO move to interface, or remove
    {
        return true;
    }
}
