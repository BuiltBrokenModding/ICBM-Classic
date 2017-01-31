package icbm.classic.content.explosive.ex;

import com.builtbroken.mc.api.IWorldPosition;
import com.builtbroken.mc.api.edit.IWorldChangeAction;
import com.builtbroken.mc.api.event.TriggerCause;
import com.builtbroken.mc.api.items.tools.IWorldPosItem;
import com.builtbroken.mc.lib.helper.recipe.RecipeUtility;
import com.builtbroken.mc.lib.transform.vector.Location;
import com.builtbroken.mc.lib.transform.vector.Pos;
import icbm.classic.ICBMClassic;
import icbm.classic.content.entity.EntityMissile;
import icbm.classic.content.explosive.Explosives;
import icbm.classic.content.explosive.blast.BlastEnderman;
import icbm.classic.content.explosive.tile.TileEntityExplosive;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;
import net.minecraftforge.oredict.ShapedOreRecipe;
import resonant.api.explosion.IExplosiveContainer;

public class ExEnder extends Explosion
{
    public ExEnder()
    {
        super("ender", 3);
        this.missileModelPath = "missile_ender.tcn";
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer entityPlayer, int par6, float par7, float par8, float par9)
    {
        if (entityPlayer.inventory.getCurrentItem() != null)
        {
            if (entityPlayer.inventory.getCurrentItem().getItem() instanceof IWorldPosItem)
            {
                IWorldPosition link = ((IWorldPosItem) entityPlayer.inventory.getCurrentItem().getItem()).getLocation(entityPlayer.inventory.getCurrentItem());

                if (link instanceof Location)
                {
                    TileEntity tileEntity = world.getTileEntity(x, y, z);

                    if (tileEntity instanceof TileEntityExplosive)
                    {
                        ((Location) link).writeIntNBT(((TileEntityExplosive) tileEntity).nbtData);

                        if (!world.isRemote)
                        {
                            entityPlayer.addChatMessage(new ChatComponentText("Synced coordinate with " + this.getExplosiveName()));
                        }

                        return true;
                    }
                }
            }
        }

        return false;
    }

    @Override
    public boolean onInteract(EntityMissile missileObj, EntityPlayer entityPlayer)
    {
        if (entityPlayer.inventory.getCurrentItem() != null)
        {
            if (entityPlayer.inventory.getCurrentItem().getItem() instanceof IWorldPosItem)
            {
                IWorldPosition link = ((IWorldPosItem) entityPlayer.inventory.getCurrentItem().getItem()).getLocation(entityPlayer.inventory.getCurrentItem());

                if (link instanceof Location)
                {
                    ((Location) link).writeIntNBT(missileObj.nbtData);
                    if (!missileObj.worldObj.isRemote)
                    {
                        entityPlayer.addChatMessage(new ChatComponentText("Synced coordinate with " + this.getMissileName()));
                    }
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public void init()
    {
        RecipeUtility.addRecipe(new ShapedOreRecipe(Explosives.ENDER.getItemStack(),
                "PPP", "PTP", "PPP",
                'P', Items.ender_pearl,
                'T', Explosives.ATTRACTIVE.getItemStack()), this.getUnlocalizedName(), ICBMClassic.INSTANCE.getConfig(), true);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void doCreateExplosion(World world, double x, double y, double z, Entity entity)
    {
        Pos teleportTarget = null;

        if (entity instanceof IExplosiveContainer)
        {
            if (((IExplosiveContainer) entity).getTagCompound().hasKey("x") && ((IExplosiveContainer) entity).getTagCompound().hasKey("y") && ((IExplosiveContainer) entity).getTagCompound().hasKey("z"))
            {
                teleportTarget = new Pos(((IExplosiveContainer) entity).getTagCompound());
            }
        }

        new BlastEnderman(world, entity, x, y, z, 30, teleportTarget).explode();
    }

    @Override
    public IWorldChangeAction createBlastForTrigger(World world, double x, double y, double z, TriggerCause triggerCause, double size, NBTTagCompound tag)
    {
        return null;
    }
}
