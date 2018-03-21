package icbm.classic.content.explosive.blast;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.caps.IEMPReceiver;
import icbm.classic.api.events.EmpEvent;
import icbm.classic.lib.emp.CapabilityEMP;
import icbm.classic.lib.emp.CapabilityEmpInventory;
import icbm.classic.client.ICBMSounds;
import icbm.classic.config.ConfigEMP;
import icbm.classic.lib.energy.system.EnergySystem;
import icbm.classic.lib.energy.system.IEnergySystem;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.items.CapabilityItemHandler;

import java.util.List;

public class BlastEMP extends Blast
{
    private boolean effectEntities = false;
    private boolean effectBlocks = false;
    private float power = 1f;

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
            if (this.effectBlocks && ConfigEMP.ALLOW_TILES)
            {
                //Loop through cube to effect blocks TODO replace with ray trace system
                for (int x = (int) -this.getBlastRadius(); x < (int) this.getBlastRadius(); x++)
                {
                    for (int y = (int) -this.getBlastRadius(); y < (int) this.getBlastRadius(); y++)
                    {
                        for (int z = (int) -this.getBlastRadius(); z < (int) this.getBlastRadius(); z++)
                        {
                            final BlockPos blockPos = new BlockPos(x + position.xi(), y + position.yi(), z + position.zi());

                            //Do distance check
                            double dist = MathHelper.sqrt(x * x + y * y + z * z);
                            if (dist > this.getBlastRadius())
                            {
                                continue;
                            }

                            //Apply action on block if loaded
                            if (world.isBlockLoaded(blockPos))
                            {
                                //Generate some effects
                                if (Math.round(position.x() + y) == position.yi())
                                {
                                    world().spawnParticle(EnumParticleTypes.SMOKE_LARGE, blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5, 0, 0, 0);
                                }

                                IBlockState iBlockState = world.getBlockState(blockPos);
                                float powerEntity = 1f;

                                //Fire event to allow canceling action on entity
                                if (!MinecraftForge.EVENT_BUS.post(new EmpEvent.BlockPre(this, world, blockPos, iBlockState)))
                                {
                                    if (ICBMClassicAPI.hasEmpHandler(iBlockState))
                                    {
                                        //TODO implement
                                    }
                                    else
                                    {
                                        TileEntity tileEntity = world.getTileEntity(blockPos);
                                        if (tileEntity != null)
                                        {
                                            boolean doInventory = true;
                                            if (tileEntity.hasCapability(CapabilityEMP.EMP, null))
                                            {
                                                IEMPReceiver receiver = tileEntity.getCapability(CapabilityEMP.EMP, null);
                                                if (receiver != null)
                                                {
                                                    powerEntity = empEntity(tileEntity, powerEntity, receiver);
                                                    doInventory = receiver.shouldEmpSubObjects(world, tileEntity.getPos().getX(), tileEntity.getPos().getY(), tileEntity.getPos().getZ());
                                                }
                                            }
                                            else if (ConfigEMP.DRAIN_ENERGY_TILES)
                                            {
                                                IEnergySystem energySystem = EnergySystem.getSystem(tileEntity, null);
                                                if (energySystem.canSetEnergyDirectly(tileEntity, null))
                                                {
                                                    energySystem.setEnergy(tileEntity, null, 0, true);
                                                }
                                                else
                                                {
                                                    //TODO Spawn tick based effect to drain as much energy as possible over several ticks
                                                }
                                            }

                                            if (doInventory && tileEntity.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null))
                                            {
                                                powerEntity = empEntity(tileEntity, powerEntity, new CapabilityEmpInventory.TileInv(tileEntity));
                                            }
                                        }
                                    }
                                }

                                //Fire post event to allow hooking EMP action
                                MinecraftForge.EVENT_BUS.post(new EmpEvent.BlockPost(this, world, blockPos, iBlockState));
                            }
                        }
                    }
                }
            }

            if (this.effectEntities && ConfigEMP.ALLOW_ENTITY)
            {
                //Calculate bounds
                AxisAlignedBB bounds = new AxisAlignedBB(
                        position.x() - this.getBlastRadius(), position.y() - this.getBlastRadius(), position.z() - this.getBlastRadius(),
                        position.x() + this.getBlastRadius(), position.y() + this.getBlastRadius(), position.z() + this.getBlastRadius());

                //Get entities in bounds
                List<Entity> entities = world().getEntitiesWithinAABB(Entity.class, bounds);

                //Loop entities to apply effects
                for (Entity entity : entities)
                {
                    float powerEntity = 1f;
                    //Fire event to allow canceling action on entity
                    if (!MinecraftForge.EVENT_BUS.post(new EmpEvent.EntityPre(this, entity)))
                    {
                        boolean doInventory = true;
                        if (entity.hasCapability(CapabilityEMP.EMP, null))
                        {
                            IEMPReceiver receiver = entity.getCapability(CapabilityEMP.EMP, null);
                            if (receiver != null)
                            {
                                powerEntity = empEntity(entity, powerEntity, receiver);
                                doInventory = receiver.shouldEmpSubObjects(world, entity.posX, entity.posY, entity.posZ);
                            }
                        }
                        else if (ConfigEMP.DRAIN_ENERGY_ENTITY)
                        {
                            IEnergySystem energySystem = EnergySystem.getSystem(entity, null);
                            if (energySystem.canSetEnergyDirectly(entity, null))
                            {
                                energySystem.setEnergy(entity, null, 0, true);
                            }
                            else
                            {
                                //TODO Spawn tick based effect to drain as much energy as possible over several ticks
                            }
                        }

                        if (doInventory && entity.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null))
                        {
                            powerEntity = empEntity(entity, powerEntity, new CapabilityEmpInventory.EntityInv(entity));
                        }

                        //Fire post event to allow hooking EMP action
                        MinecraftForge.EVENT_BUS.post(new EmpEvent.EntityPost(this, entity));
                    }
                }
            }

            //TODO VEProviderShockWave.spawnEffect(world(), position.x(), position.y(), position.z(), 0, 0, 0, 0, 0, 255, 1, 3);
            //TODO VEProviderShockWave.spawnEffect(world(), position.x(), position.y(), position.z(), 0, 0, 0, 0, 0, 255, 3, 3);
            //TODO VEProviderShockWave.spawnEffect(world(), position.x(), position.y(), position.z(), 0, 0, 0, 0, 0, 255, 5, 3);
            ICBMSounds.EMP.play(world, position.x(), position.y(), position.z(), 4.0F, (1.0F + (world().rand.nextFloat() - world().rand.nextFloat()) * 0.2F) * 0.7F, true);
        }
    }

    protected float empEntity(Entity entity, float powerEntity, IEMPReceiver receiver)
    {
        if (receiver != null)
        {
            powerEntity = receiver.applyEmpAction(world, entity.posX, entity.posY, entity.posZ, this, powerEntity, true);
            //TODO spawn effects on entity if items were effected
            //TODO ICBMClassic.proxy.spawnShock(this.oldWorld(), this.position, new Pos(entity), 20);
        }
        return powerEntity;
    }

    protected float empEntity(TileEntity entity, float powerEntity, IEMPReceiver receiver)
    {
        if (receiver != null)
        {
            powerEntity = receiver.applyEmpAction(world, entity.getPos().getX(), entity.getPos().getY(), entity.getPos().getZ(), this, powerEntity, true);
            //TODO spawn effects on entity if items were effected
            //TODO ICBMClassic.proxy.spawnShock(this.oldWorld(), this.position, new Pos(entity), 20);
        }
        return powerEntity;
    }

    @Override
    public long getEnergy()
    {
        return 3000;
    }
}
