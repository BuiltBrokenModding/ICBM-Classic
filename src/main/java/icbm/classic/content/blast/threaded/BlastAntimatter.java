package icbm.classic.content.blast.threaded;

import icbm.classic.api.events.BlastCancelEvent;
import icbm.classic.client.ICBMSounds;
import icbm.classic.config.ConfigBlast;
import icbm.classic.content.blast.BlastHelpers;
import icbm.classic.content.entity.EntityExplosion;
import icbm.classic.content.blast.BlastRedmatter;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.MinecraftForge;

import java.util.function.Consumer;

public class BlastAntimatter extends BlastThreaded
{
    private final IBlockState replaceState = Blocks.AIR.getDefaultState();

    private boolean antimatterDoBlockUpdates = false;
    private static final int antimatterWaterCleanupRange = 5; // antimatter water cleanup antimatterWaterCleanupRange
    private boolean makeHoles = false;

    public BlastAntimatter()
    {
    }

    /**
     * Called before an explosion happens
     */
    @Override
    public void setupBlast()
    {
        super.setupBlast();
        antimatterDoBlockUpdates = ConfigBlast.BLAST_DO_BLOCKUPDATES;
        ICBMSounds.ANTIMATTER.play(world, this.location.x(), this.location.y(), this.location.z(), 7F, (float) (this.world().rand.nextFloat() * 0.1 + 0.9F), true);
        this.doDamageEntities(this.getBlastRadius() * 2, Integer.MAX_VALUE);
    }

    @Override
    public void destroyBlock(BlockPos blockPos)
    {
        if(!ConfigBlast.ANTIMATTER_BLOCK_DAMAGE)
            return;

        final IBlockState blockState = world.getBlockState(blockPos);
        if (blockState.getBlock() != Blocks.AIR)
        {
            if(blockState.getBlock().getBlockHardness(blockState, world, blockPos) < 0.0F && !ConfigBlast.ANTIMATTER_DESTROY_UNBREAKABLE_BLOCKS) //unbreakable
                return;

            final double deltaX = blockPos.getX()-location.x();
            if(deltaX > getBlastRadius() - 5)
                makeHoles = true;

            //final double deltaZ = blockPos.getZ()-blockPos.getZ();
            //final double dist = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ); // omit Y since its not as important as X,Z and saves some performance
            if(makeHoles) {

                //TODO change to not use sqrt for better performance
                //TODO maybe do the random in the thread? (although rolling the random numbers only happens at the end so it doesn't matter that much)

                if (world().rand.nextFloat() < .6)// * (this.getBlastRadius() - dist))
                    world.setBlockState(blockPos, replaceState, 3);

                return; // dont clean up water anymore from this point on
            }
            else
            {
                world.setBlockState(blockPos, replaceState, antimatterDoBlockUpdates ? 3 : 2);
            }

            if(antimatterDoBlockUpdates) {
                // handle sand and gravel (check if the above block is sand or gravel and then destroy it and check the above
                destroyFallingBlocksRecursively(blockPos.up());

                // remove water ahead of the blast, to significantly reduce the water issues
                for (int x = -antimatterWaterCleanupRange; x <= antimatterWaterCleanupRange; x++) {
                    for (int y = -antimatterWaterCleanupRange; y <= antimatterWaterCleanupRange; y++) {
                        for (int z = -antimatterWaterCleanupRange; z <= antimatterWaterCleanupRange; z++) {
                            final BlockPos bp2 = new BlockPos(blockPos.getX() + x, blockPos.getY() + 1, blockPos.getZ() + z);
                            final IBlockState bs2 = world.getBlockState(bp2);
                            final Block b = bs2.getBlock();
                            if (b == Blocks.WATER || b == Blocks.FLOWING_WATER) {
                                world.setBlockToAir(bp2);
                            }
                        }
                    }
                }
            }
        }
    }

    /*
     *  Checks the current block and if it is sand or gravel then destroys it.
     *  Continues upwards as long as there is sand or gravel.
     */
    private void destroyFallingBlocksRecursively(BlockPos currentBp)
    {
        Block currentBlock = world.getBlockState(currentBp).getBlock();
        if (currentBlock == Blocks.SAND || currentBlock == Blocks.GRAVEL)
        {
            BlockPos above = currentBp.up();
            destroyFallingBlocksRecursively(above); // check above block

            world.setBlockState(currentBp, replaceState, 3); // destroy current
        }
    }

    @Override
    public boolean doRun(int loops, Consumer<BlockPos> edits)
    {
        int ymin = -this.getPos().getY();
        int ymax = 255-this.getPos().getY();
        BlastHelpers.loopInRadius(this.getBlastRadius(), (x, y, z) ->{

            if (y >= ymin && y < ymax)
            {
                edits.accept(new BlockPos(xi() + x, yi() + y, zi() + z));
            }
        });
        return false;
    }

    @Override
    public boolean doExplode(int callCount)
    {
        super.doExplode(callCount);

        // TODO: Render antimatter shockwave
        /*
         * else if (ZhuYao.proxy.isGaoQing()) { for (int x = -this.getRadius(); x <
         * this.getRadius(); x++) { for (int y = -this.getRadius(); y < this.getRadius(); y++) { for
         * (int z = -this.getRadius(); z < this.getRadius(); z++) { Vector3 targetPosition =
         * Vector3.add(position, new Vector3(x, y, z)); double distance =
         * position.distanceTo(targetPosition);
         * if (targetPosition.getBlockID(worldObj) == 0) { if (distance < this.getRadius() &&
         * distance > this.getRadius() - 1 && worldObj.rand.nextFloat() > 0.5) {
         * ParticleSpawner.spawnParticle("antimatter", worldObj, targetPosition); } } } } } }
         */
        return false;
    }

    @Override
    public void onBlastCompleted()
    {
        super.onBlastCompleted();
        this.doDamageEntities(this.getBlastRadius() * 2, Integer.MAX_VALUE);
    }

    @Override
    protected boolean onDamageEntity(Entity entity)
    {
        if (entity instanceof EntityExplosion)
        {
            if (((EntityExplosion) entity).getBlast() instanceof BlastRedmatter)
            {
                if(!MinecraftForge.EVENT_BUS.post(new BlastCancelEvent(this, ((EntityExplosion) entity).getBlast())))
                    entity.setDead();
                return true;
            }
        }

        return !ConfigBlast.ANTIMATTER_ENTITY_DAMAGE; //if entity damage is enabled, return false so the entity damage logic can continue and vice versa
    }
}
