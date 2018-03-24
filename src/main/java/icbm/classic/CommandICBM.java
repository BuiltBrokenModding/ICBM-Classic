package icbm.classic;

import icbm.classic.content.entity.EntityExplosion;
import icbm.classic.content.entity.EntityFlyingBlock;
import icbm.classic.content.entity.missile.EntityMissile;
import icbm.classic.content.explosive.blast.BlastEMP;
import net.minecraft.command.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;

import javax.annotation.Nullable;
import java.util.List;

public class CommandICBM extends CommandBase
{
    @Override
    public String getName()
    {
        return "icbmc";
    }

    @Override
    public String getUsage(ICommandSender sender)
    {
        return "/icbmc help";
    }


    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        try
        {
            EntityPlayer entityPlayer = (EntityPlayer) sender;
            int dimension = entityPlayer.world.provider.getDimension();
            if (args == null || args.length == 0 || args[0].equalsIgnoreCase("help"))
            {
                ((EntityPlayer) sender).sendMessage(new TextComponentString("/icbmc help"));
                ((EntityPlayer) sender).sendMessage(new TextComponentString("/icbmc lag <radius>"));
                ((EntityPlayer) sender).sendMessage(new TextComponentString("/icbmc remove <All/Missile/Explosion> <radius>"));
                ((EntityPlayer) sender).sendMessage(new TextComponentString("/icbmc emp <radius>"));
                return;
            }
            else if (args.length >= 2 && args[0].equalsIgnoreCase("lag"))
            {
                int radius = parseInt(args[1]);

                if (radius > 0)
                {
                    AxisAlignedBB bounds = new AxisAlignedBB(entityPlayer.posX - radius, entityPlayer.posY - radius, entityPlayer.posZ - radius, entityPlayer.posX + radius, entityPlayer.posY + radius, entityPlayer.posZ + radius);
                    List<Entity> entitiesNearby = entityPlayer.world.getEntitiesWithinAABB(Entity.class, bounds);

                    for (Entity entity : entitiesNearby)
                    {
                        if (entity instanceof EntityFlyingBlock)
                        {
                            ((EntityFlyingBlock) entity).setBlock();
                        }
                        else if (entity instanceof EntityMissile)
                        {
                            entity.setDead();
                        }
                        else if (entity instanceof EntityExplosion)
                        {
                            entity.setDead();
                        }
                    }

                    ((EntityPlayer) sender).sendMessage(new TextComponentString("Removed all ICBM lag sources within " + radius + " radius."));
                    return;
                }
                else
                {
                    throw new WrongUsageException("Radius needs to be higher than zero");
                }
            }
            else if (args.length >= 3 && args[0].equalsIgnoreCase("remove"))
            {
                int radius = parseInt(args[2]);
                boolean all = args[1].equalsIgnoreCase("all");
                boolean missile = args[1].equalsIgnoreCase("missiles");
                boolean explosion = args[1].equalsIgnoreCase("explosion");
                String str = "entities";
                if (missile)
                {
                    str = "missiles";
                }
                if (explosion)
                {
                    str = "explosions";
                }

                if (radius > 0)
                {
                    EntityPlayer player = (EntityPlayer) sender;

                    AxisAlignedBB bounds = new AxisAlignedBB(player.posX - radius, player.posY - radius, player.posZ - radius, player.posX + radius, player.posY + radius, player.posZ + radius);
                    List<Entity> entitiesNearby = player.world.getEntitiesWithinAABB(Entity.class, bounds);

                    for (Entity entity : entitiesNearby)
                    {
                        if ((all || explosion) && entity instanceof EntityFlyingBlock)
                        {
                            ((EntityFlyingBlock) entity).setBlock();
                        }
                        else if ((all || missile) && entity instanceof EntityMissile)
                        {
                            entity.setDead();
                        }
                        else if ((all || explosion) && entity instanceof EntityExplosion)
                        {
                            entity.setDead();
                        }
                    }

                    ((EntityPlayer) sender).sendMessage(new TextComponentString("Removed all ICBM " + str + " within " + radius + " radius."));
                    return;
                }
                else
                {
                    throw new WrongUsageException("Radius needs to be higher than zero");
                }
            }
            else if (args.length >= 2 && args[0].equalsIgnoreCase("emp"))
            {
                int radius = parseInt(args[1]);
                if (radius > 0)
                {
                    new BlastEMP(entityPlayer.world, null, entityPlayer.posX, entityPlayer.posY, entityPlayer.posZ, radius).setEffectBlocks().setEffectEntities().doExplode();
                    switch (entityPlayer.world.rand.nextInt(20))
                    {
                        case 0:
                            ((EntityPlayer) sender).sendMessage(new TextComponentString("Did you pay the power bill?"));
                            return;
                        case 1:
                            ((EntityPlayer) sender).sendMessage(new TextComponentString("See them power their toys now!"));
                            return;
                        case 2:
                            ((EntityPlayer) sender).sendMessage(new TextComponentString("Hey who turned the lights out."));
                            return;
                        case 3:
                            ((EntityPlayer) sender).sendMessage(new TextComponentString("Ha! I run on steam power!"));
                            return;
                        case 4:
                            ((EntityPlayer) sender).sendMessage(new TextComponentString("The power of lighting at my finger tips!"));
                            return;
                        default:
                            ((EntityPlayer) sender).sendMessage(new TextComponentString("Zap!"));
                            return;
                    }
                }
                else
                {
                    throw new WrongUsageException("Radius needs to be higher than zero");
                }
            }
        }
        catch (Exception e)
        {
        }

        throw new WrongUsageException(this.getUsage(sender));
    }

    @Override
    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos)
    {
        return args.length == 1 ? getListOfStringsMatchingLastWord(args, new String[] { "lag" }) : null;
    }

    @Override
    public int compareTo(ICommand par1Obj)
    {
        return super.compareTo(par1Obj);
    }
}
