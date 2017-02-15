package icbm.classic.content.explosive.blast;

import com.builtbroken.mc.lib.energy.UniversalEnergySystem;
import com.builtbroken.mc.lib.transform.region.Cube;
import com.builtbroken.mc.lib.transform.vector.Pos;
import com.builtbroken.mc.lib.world.radar.RadarRegistry;
import icbm.classic.ICBMClassic;
import icbm.classic.content.entity.EntityExplosive;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import resonant.api.explosion.IEMPBlock;
import resonant.api.explosion.IEMPItem;
import resonant.api.explosion.IMissile;

import java.util.List;

public class BlastEMP extends Blast
{
    private boolean effectEntities = false;
    private boolean effectBlocks = false;

    public BlastEMP(World world, Entity entity, double x, double y, double z, float size)
    {
        super(world, entity, x, y, z, size);
    }

    public BlastEMP setEffectBlocks()
    {
        this.effectBlocks = true;
        return this;
    }

    public BlastEMP setEffectEntities()
    {
        this.effectEntities = true;
        return this;
    }

    @Override
    public void doExplode()
    {
        if (this.effectBlocks)
        {
            for (int x = (int) -this.getRadius(); x < (int) this.getRadius(); x++)
            {
                for (int y = (int) -this.getRadius(); y < (int) this.getRadius(); y++)
                {
                    for (int z = (int) -this.getRadius(); z < (int) this.getRadius(); z++)
                    {
                        double dist = MathHelper.sqrt_double((x * x + y * y + z * z));

                        Pos searchPosition = new Pos(x, y, z).add(position);
                        if (dist > this.getRadius())
                        {
                            continue;
                        }

                        if (Math.round(position.x() + y) == position.yi())
                        {
                            world().spawnParticle("largesmoke", searchPosition.x(), searchPosition.y(), searchPosition.z(), 0, 0, 0);
                        }

                        Block block = searchPosition.getBlock(world());
                        TileEntity tileEntity = searchPosition.getTileEntity(world());
                        //TODO fire EMP event
                        //TODO more EMP effect to UniversalEnergySystem to better support cross mod support
                        if (block != null)
                        {
                            //if (block instanceof IForceFieldBlock)
                            //{
                            //    ((IForceFieldBlock) block).weakenForceField(world(), searchPosition.xi(), searchPosition.yi(), searchPosition.zi(), 1000);
                            //}
                            if (block instanceof IEMPBlock)
                            {
                                ((IEMPBlock) block).onEMP(world(), searchPosition.xi(), searchPosition.yi(), searchPosition.zi(), this);
                            }
                        }

                        if (tileEntity != null)
                        {
                            //if (tileEntity instanceof IFortronStorage)
                            //{
                            //    ((IFortronStorage) tileEntity).provideFortron((int) world().rand.nextFloat() * ((IFortronStorage) tileEntity).getFortronCapacity(), true);
                            //}
                            UniversalEnergySystem.clearEnergy(tileEntity, true);
                        }
                    }
                }
            }
        }

        if (this.effectEntities)
        {
            // Drop all missiles
            List<Entity> entitiesNearby = RadarRegistry.getAllLivingObjectsWithin(world(), new Cube(position.sub(getRadius()), position.add(getRadius())), null);

            for (Entity entity : entitiesNearby)
            {
                if (entity instanceof IMissile && !entity.isEntityEqual(this.controller))
                {
                    if (((IMissile) entity).getTicksInAir() > -1)
                    {
                        ((IMissile) entity).dropMissileAsItem();
                    }
                }
            }

            int maxFx = 10;
            AxisAlignedBB bounds = AxisAlignedBB.getBoundingBox(position.x() - this.getRadius(), position.y() - this.getRadius(), position.z() - this.getRadius(), position.x() + this.getRadius(), position.y() + this.getRadius(), position.z() + this.getRadius());
            List<Entity> entities = world().getEntitiesWithinAABB(Entity.class, bounds);

            for (Entity entity : entities)
            {
                if (entity instanceof EntityLivingBase)
                {
                    if (this.world().isRemote && maxFx > 0)
                    {
                        ICBMClassic.proxy.spawnShock(this.world(), this.position, new Pos(entity), 20);
                        maxFx--;
                    }

                    if (entity instanceof EntityCreeper)
                    {
                        if (!this.world().isRemote)
                        {
                            try
                            {
                                ((EntityCreeper) entity).getDataWatcher().updateObject(17, (byte) 1);
                            }
                            catch (Exception e)
                            {
                                e.printStackTrace();
                            }
                        }
                    }
                    if (entity instanceof EntityPlayer)
                    {
                        IInventory inventory = ((EntityPlayer) entity).inventory;

                        for (int i = 0; i < inventory.getSizeInventory(); i++)
                        {
                            ItemStack itemStack = inventory.getStackInSlot(i);

                            if (itemStack != null)
                            {
                                if (itemStack.getItem() instanceof IEMPItem)
                                {
                                    ((IEMPItem) itemStack.getItem()).onEMP(itemStack, entity, this);
                                }
                                UniversalEnergySystem.clearEnergy(itemStack, true);
                            }
                        }
                    }
                }
                else if (entity instanceof EntityExplosive)
                {
                    entity.setDead();
                }
            }
        }

        ICBMClassic.proxy.spawnParticle("shockwave", world(), position, 0, 0, 0, 0, 0, 255, 10, 3);
        this.world().playSoundEffect(position.x(), position.y(), position.z(), ICBMClassic.PREFIX + "emp", 4.0F, (1.0F + (world().rand.nextFloat() - world().rand.nextFloat()) * 0.2F) * 0.7F);
    }

    @Override
    public long getEnergy()
    {
        return 3000;
    }
}
