package icbm.classic.content.explosive.blast;

import icbm.classic.api.explosion.IMissile;
import icbm.classic.api.items.IEMPItem;
import icbm.classic.api.tile.IEMPBlock;
import icbm.classic.client.ICBMSounds;
import icbm.classic.content.entity.EntityExplosive;
import icbm.classic.lib.energy.UniversalEnergySystem;
import icbm.classic.lib.radar.RadarRegistry;
import icbm.classic.lib.transform.region.Cube;
import icbm.classic.lib.transform.vector.Pos;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

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
        if (!world().isRemote)
        {
            if (this.effectBlocks)
            {
                for (int x = (int) -this.getRadius(); x < (int) this.getRadius(); x++)
                {
                    for (int y = (int) -this.getRadius(); y < (int) this.getRadius(); y++)
                    {
                        for (int z = (int) -this.getRadius(); z < (int) this.getRadius(); z++)
                        {
                            double dist = MathHelper.sqrt((x * x + y * y + z * z));

                            Pos searchPosition = new Pos(x, y, z).add(position);
                            if (dist > this.getRadius())
                            {
                                continue;
                            }

                            if (Math.round(position.x() + y) == position.yi())
                            {
                                world().spawnParticle(EnumParticleTypes.SMOKE_LARGE, searchPosition.x(), searchPosition.y(), searchPosition.z(), 0, 0, 0);
                            }

                            Block block = searchPosition.getBlock(world());
                            TileEntity tileEntity = searchPosition.getTileEntity(world());
                            //TODO fire EMP event
                            //TODO more EMP effect to UniversalEnergySystem to better support cross mod support
                            if (block != null)
                            {
                                if (block instanceof IEMPBlock)
                                {
                                    ((IEMPBlock) block).onEMP(world(), searchPosition.toBlockPos(), this);
                                }
                            }

                            if (tileEntity != null)
                            {
                                UniversalEnergySystem.clearEnergy(tileEntity, true);
                            }
                        }
                    }
                }
            }

            if (this.effectEntities)
            {
                // Drop all missiles
                List<Entity> entitiesNearby = RadarRegistry.getAllLivingObjectsWithin(world(), new Cube(position.sub(getRadius()), position.add(getRadius())));

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

                //Calculate bounds
                AxisAlignedBB bounds = new AxisAlignedBB(
                        position.x() - this.getRadius(), position.y() - this.getRadius(), position.z() - this.getRadius(),
                        position.x() + this.getRadius(), position.y() + this.getRadius(), position.z() + this.getRadius());

                //Get entities in bounds
                List<Entity> entities = world().getEntitiesWithinAABB(Entity.class, bounds);

                //Loop entities to apply effects
                for (Entity entity : entities)
                {
                    if (entity instanceof EntityLivingBase)
                    {
                        if (entity instanceof EntityCreeper)
                        {
                            entity.onStruckByLightning(new EntityLightningBolt(world, entity.posX, entity.posY, entity.posZ, false));
                        }
                        if (entity instanceof EntityPlayer)
                        {
                            IInventory inventory = ((EntityPlayer) entity).inventory;

                            for (int i = 0; i < inventory.getSizeInventory(); i++)
                            {
                                ItemStack itemStack = inventory.getStackInSlot(i);

                                if (!itemStack.isEmpty())
                                {
                                    if (itemStack.getItem() instanceof IEMPItem)
                                    {
                                        ((IEMPItem) itemStack.getItem()).onEMP(itemStack, entity, this);
                                    }
                                    else
                                    {
                                        UniversalEnergySystem.clearEnergy(itemStack, true);
                                    }
                                }
                            }

                            //TODO spawn effects on entity if items were effected
                            //TODO ICBMClassic.proxy.spawnShock(this.oldWorld(), this.position, new Pos(entity), 20);
                        }
                    }
                    else if (entity instanceof EntityExplosive)
                    {
                        entity.setDead();
                    }
                }
            }

            //TODO VEProviderShockWave.spawnEffect(world(), position.x(), position.y(), position.z(), 0, 0, 0, 0, 0, 255, 1, 3);
            //TODO VEProviderShockWave.spawnEffect(world(), position.x(), position.y(), position.z(), 0, 0, 0, 0, 0, 255, 3, 3);
            //TODO VEProviderShockWave.spawnEffect(world(), position.x(), position.y(), position.z(), 0, 0, 0, 0, 0, 255, 5, 3);
            ICBMSounds.EMP.play(world, position.x(), position.y(), position.z(), 4.0F, (1.0F + (world().rand.nextFloat() - world().rand.nextFloat()) * 0.2F) * 0.7F, true);
        }
    }

    @Override
    public long getEnergy()
    {
        return 3000;
    }
}
