package icbm.classic.content.machines.coordinator;

import com.builtbroken.mc.api.items.tools.IWorldPosItem;
import com.builtbroken.mc.api.tile.IGuiTile;
import com.builtbroken.mc.core.registry.implement.IRecipeContainer;
import com.builtbroken.mc.lib.helper.LanguageUtility;
import com.builtbroken.mc.lib.helper.recipe.UniversalRecipe;
import com.builtbroken.mc.lib.transform.vector.Pos;
import com.builtbroken.mc.prefab.tile.Tile;
import com.builtbroken.mc.prefab.tile.TileModuleMachine;
import com.builtbroken.mc.prefab.tile.module.TileModuleInventory;
import cpw.mods.fml.common.registry.GameRegistry;
import icbm.classic.ICBMClassic;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.oredict.ShapedOreRecipe;

import java.util.List;

/**
 * Missile Coordinator, used to calculate paths between two points to better plan missile actions
 *
 * @author Calclavia
 */
public class TileMissileCoordinator extends TileModuleMachine implements IRecipeContainer, IGuiTile
{
    public TileMissileCoordinator()
    {
        super("missileCoordinator", Material.iron);
    }

    @Override
    protected IInventory createInventory()
    {
        return new TileModuleInventory(this, 2);
    }

    @Override
    public Tile newTile()
    {
        return new TileMissileCoordinator();
    }

    @Override
    public boolean canUpdate()
    {
        return false;
    }

    @Override
    protected boolean useMetaForFacing()
    {
        return true;
    }

    @Override
    public String getInventoryName()
    {
        return LanguageUtility.getLocal("gui.coordinator.name");
    }

    @Override
    public boolean isItemValidForSlot(int i, ItemStack itemstack)
    {
        return itemstack.getItem() instanceof IWorldPosItem;
    }

    @Override
    public boolean onPlayerActivated(EntityPlayer player, int side, Pos hit)
    {
        if (isServer())
        {
            this.worldObj.playSoundEffect(this.xCoord, this.yCoord, this.zCoord, ICBMClassic.PREFIX + "interface", 1, (float) (this.worldObj.rand.nextFloat() * 0.2 + 0.9F));
            openGui(player, ICBMClassic.INSTANCE);
        }

        return true;
    }

    @Override
    public void genRecipes(List<IRecipe> recipes)
    {
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ICBMClassic.blockMissileCoordinator, 1),
                "R R", "SCS", "SSS",
                'C', UniversalRecipe.CIRCUIT_T2.get(),
                'S', UniversalRecipe.PRIMARY_PLATE.get(),
                'R', ICBMClassic.itemRemoteDetonator));
    }

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player)
    {
        return new ContainerMissileCoordinator(player, this);
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player)
    {
        return null;
    }
}
