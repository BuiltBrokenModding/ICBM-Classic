package icbm.classic.content.explosive.tile;

import com.builtbroken.mc.core.Engine;
import com.builtbroken.mc.core.registry.implement.IPostInit;
import com.builtbroken.mc.lib.helper.WrenchUtility;
import com.builtbroken.mc.lib.transform.vector.Pos;
import com.builtbroken.mc.prefab.inventory.InventoryUtility;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import icbm.classic.ICBMClassic;
import icbm.classic.client.render.tile.RenderBombBlock;
import icbm.classic.content.entity.EntityExplosive;
import icbm.classic.content.explosive.Explosive;
import icbm.classic.content.explosive.Explosives;
import icbm.classic.prefab.BlockICBM;
import icbm.classic.prefab.VectorHelper;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.ForgeDirection;
import resonant.api.explosion.ExplosionEvent.ExplosivePreDetonationEvent;
import resonant.api.explosion.ExplosiveType;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class BlockExplosive extends BlockICBM implements IPostInit
{
    public final HashMap<String, IIcon> ICONS = new HashMap();

    public BlockExplosive()
    {
        super("explosives", Material.tnt);
        setHardness(0.0F);
        setStepSound(soundTypeGrass);
    }

    /** gets the way this piston should face for that entity that placed it. */
    private static byte determineOrientation(World world, int x, int y, int z, EntityLivingBase entityLiving)
    {
        if (entityLiving != null)
        {
            if (MathHelper.abs((float) entityLiving.posX - x) < 2.0F && MathHelper.abs((float) entityLiving.posZ - z) < 2.0F)
            {
                double var5 = entityLiving.posY + 1.82D - entityLiving.yOffset;

                if (var5 - y > 2.0D)
                {
                    return 1;
                }

                if (y - var5 > 0.0D)
                {
                    return 0;
                }
            }

            int rotation = MathHelper.floor_double(entityLiving.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;
            return (byte) (rotation == 0 ? 2 : (rotation == 1 ? 5 : (rotation == 2 ? 3 : (rotation == 3 ? 4 : 0))));
        }
        return 0;
    }

    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess par1IBlockAccess, int x, int y, int z)
    {
        TileEntity tileEntity = par1IBlockAccess.getTileEntity(x, y, z);

        if (tileEntity != null)
        {
            if (tileEntity instanceof TileEntityExplosive)
            {
                if (((TileEntityExplosive) tileEntity).explosive == Explosives.SMINE)
                {
                    this.setBlockBounds(0, 0, 0, 1f, 0.2f, 1f);
                    return;
                }
            }
        }

        this.setBlockBounds(0, 0, 0, 1f, 1f, 1f);
    }

    @Override
    public void setBlockBoundsForItemRender()
    {
        this.setBlockBounds(0, 0, 0, 1f, 1f, 1f);
    }

    /**
     * Returns a bounding box from the pool of bounding boxes (this means this box can change after
     * the pool has been cleared to be reused)
     */
    @Override
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World par1World, int x, int y, int z)
    {
        TileEntity tileEntity = par1World.getTileEntity(x, y, z);

        if (tileEntity != null)
        {
            if (tileEntity instanceof TileEntityExplosive)
            {
                if (((TileEntityExplosive) tileEntity).explosive == Explosives.SMINE)
                {
                    return AxisAlignedBB.getBoundingBox(x + this.minX, y + this.minY, z + this.minZ, x + this.maxX, y + 0.2, z + this.maxZ);
                }
            }
        }

        return super.getCollisionBoundingBoxFromPool(par1World, x, y, z);
    }

    /** Called when the block is placed in the world. */
    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entityLiving, ItemStack itemStack)
    {
        ((TileEntityExplosive) world.getTileEntity(x, y, z)).explosive = Explosives.get(itemStack.getItemDamage());
        Explosives explosiveID = ((TileEntityExplosive) world.getTileEntity(x, y, z)).explosive;

        if (!world.isRemote)
        {
            ExplosivePreDetonationEvent evt = new ExplosivePreDetonationEvent(world, x, y, z, ExplosiveType.BLOCK, explosiveID.handler);
            MinecraftForge.EVENT_BUS.post(evt);

            if (evt.isCanceled())
            {
                this.dropBlockAsItem(world, x, y, z, explosiveID.ordinal(), 0);
                world.setBlock(x, y, z, Blocks.air, 0, 2);
                return;
            }
        }

        world.setBlockMetadataWithNotify(x, y, z, VectorHelper.getOrientationFromSide(ForgeDirection.getOrientation(determineOrientation(world, x, y, z, entityLiving)), ForgeDirection.NORTH).ordinal(), 2);

        if (world.isBlockIndirectlyGettingPowered(x, y, z))
        {
            BlockExplosive.yinZha(world, x, y, z, explosiveID, 0);
        }

        // Check to see if there is fire nearby.
        // If so, then detonate.
        for (byte i = 0; i < 6; i++)
        {
            Pos position = new Pos(x, y, z).add(ForgeDirection.getOrientation(i));

            Block blockId = position.getBlock(world);

            if (blockId == Blocks.fire || blockId == Blocks.flowing_lava || blockId == Blocks.lava)
            {
                BlockExplosive.yinZha(world, x, y, z, explosiveID, 2);
            }
        }

        if (entityLiving != null)
        {
            FMLLog.fine(entityLiving.getCommandSenderName() + " placed " + explosiveID.handler.getExplosiveName() + " in: " + x + ", " + y + ", " + z + ".");
        }
    }

    /** Returns the block texture based on the side being looked at. Args: side */
    @Override
    public IIcon getIcon(IBlockAccess par1IBlockAccess, int x, int y, int z, int side)
    {
        return getIcon(side, ((TileEntityExplosive) par1IBlockAccess.getTileEntity(x, y, z)).explosive.ordinal());
    }

    @Override
    public IIcon getIcon(int side, int explosiveID)
    {
        if (side == 0)
        {
            return getIcon(explosiveID + "_bottom");
        }
        else if (side == 1)
        {
            return getIcon(explosiveID + "_top");
        }

        return getIcon(explosiveID + "_side");
    }

    @SideOnly(Side.CLIENT)
    private IIcon getIcon(String name)
    {
        IIcon icon = ICONS.get(name);
        if (icon != null)
        {
            return icon;
        }
        return Blocks.sandstone.getIcon(0, 0);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerBlockIcons(IIconRegister iconRegister)
    {
        /** Register every single texture for all explosives. */
        for (Explosives ex : Explosives.values())
        {
            ICONS.put(ex.ordinal() + "_top", this.getIcon(iconRegister, ex.handler, "_top"));
            ICONS.put(ex.ordinal() + "_side", this.getIcon(iconRegister, ex.handler, "_side"));
            ICONS.put(ex.ordinal() + "_bottom", this.getIcon(iconRegister, ex.handler, "_bottom"));
        }
    }

    @SideOnly(Side.CLIENT)
    public IIcon getIcon(IIconRegister iconRegister, Explosive zhaPin, String suffix)
    {
        String iconName = "explosive_" + zhaPin.getUnlocalizedName() + suffix;

        try
        {
            ResourceLocation resourcelocation = new ResourceLocation(ICBMClassic.DOMAIN, "textures/blocks/" + iconName + ".png");
            InputStream inputstream = Minecraft.getMinecraft().getResourceManager().getResource(resourcelocation).getInputStream();
            BufferedImage bufferedimage = ImageIO.read(inputstream);

            if (bufferedimage != null)
            {
                return iconRegister.registerIcon(ICBMClassic.PREFIX + iconName);
            }
        }
        catch (Exception e)
        {
            if (Engine.runningAsDev)
            {
                e.printStackTrace();
            }
        }

        if (suffix.equals("_bottom"))
        {
            return iconRegister.registerIcon(ICBMClassic.PREFIX + "explosive_bottom_" + zhaPin.getTier());
        }

        return iconRegister.registerIcon(ICBMClassic.PREFIX + "explosive_base_" + zhaPin.getTier());
    }

    /** Called whenever the block is added into the world. Args: world, x, y, z */
    @Override
    public void onBlockAdded(World par1World, int x, int y, int z)
    {
        super.onBlockAdded(par1World, x, y, z);

        //int explosiveID = ((TileExplosive) par1World.getTileEntity(x, y, z)).haoMa;
        par1World.markBlockRangeForRenderUpdate(x, y, z, x, y, z);
    }

    /**
     * Lets the block know when one of its neighbor changes. Doesn't know which neighbor changed
     * (coordinates passed are their own) Args: x, y, z, neighbor blockID
     */
    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block blockId)
    {
        Explosives explosiveID = ((TileEntityExplosive) world.getTileEntity(x, y, z)).explosive;

        if (world.isBlockIndirectlyGettingPowered(x, y, z))
        {
            BlockExplosive.yinZha(world, x, y, z, explosiveID, 0);
        }
        else if (blockId == Blocks.fire || blockId == Blocks.flowing_lava || blockId == Blocks.lava)
        {
            BlockExplosive.yinZha(world, x, y, z, explosiveID, 2);
        }
    }

    /*
     * Called to detonate the TNT. Args: world, x, y, z, metaData, CauseOfExplosion (0, intentional,
     * 1, exploded, 2 burned)
     */
    public static void yinZha(World world, int x, int y, int z, Explosives explosiveID, int causeOfExplosion)
    {
        if (!world.isRemote)
        {
            TileEntity tileEntity = world.getTileEntity(x, y, z);

            if (tileEntity != null)
            {
                if (tileEntity instanceof TileEntityExplosive)
                {
                    ExplosivePreDetonationEvent evt = new ExplosivePreDetonationEvent(world, x, y, z, ExplosiveType.BLOCK, ((TileEntityExplosive) tileEntity).explosive.handler);
                    MinecraftForge.EVENT_BUS.post(evt);

                    if (!evt.isCanceled())
                    {
                        ((TileEntityExplosive) tileEntity).exploding = true;
                        EntityExplosive eZhaDan = new EntityExplosive(world, new Pos(x, y, z).add(0.5), ((TileEntityExplosive) tileEntity).explosive, (byte) world.getBlockMetadata(x, y, z), ((TileEntityExplosive) tileEntity).nbtData);

                        switch (causeOfExplosion)
                        {
                            case 2:
                                eZhaDan.setFire(100);
                                break;
                        }

                        world.spawnEntityInWorld(eZhaDan);
                        world.setBlockToAir(x, y, z);
                    }
                }
            }
        }
    }

    /** Called upon the block being destroyed by an explosion */
    @Override
    public void onBlockExploded(World world, int x, int y, int z, Explosion explosion)
    {
        if (world.getTileEntity(x, y, z) != null)
        {
            BlockExplosive.yinZha(world, x, y, z, ((TileEntityExplosive) world.getTileEntity(x, y, z)).explosive, 1);
        }

        super.onBlockExploded(world, x, y, z, explosion);
    }

    /**
     * Called upon block activation (left or right click on the block.). The three integers
     * represent x,y,z of the block.
     */
    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer entityPlayer, int par6, float par7, float par8, float par9)
    {
        TileEntity tileEntity = world.getTileEntity(x, y, z);

        if (entityPlayer.getHeldItem() != null)
        {
            if (entityPlayer.getHeldItem().getItem() == Items.flint_and_steel)
            {
                Explosives explosiveID = ((TileEntityExplosive) tileEntity).explosive;
                BlockExplosive.yinZha(world, x, y, z, explosiveID, 0);
                return true;
            }
            else if (WrenchUtility.isUsableWrench(entityPlayer, entityPlayer.getCurrentEquippedItem(), x, y, z))
            {
                byte change = 3;

                // Reorient the block
                switch (world.getBlockMetadata(x, y, z))
                {
                    case 0:
                        change = 2;
                        break;
                    case 2:
                        change = 5;
                        break;
                    case 5:
                        change = 3;
                        break;
                    case 3:
                        change = 4;
                        break;
                    case 4:
                        change = 1;
                        break;
                    case 1:
                        change = 0;
                        break;
                }

                world.setBlockMetadataWithNotify(x, y, z, ForgeDirection.getOrientation(change).ordinal(), 3);

                world.notifyBlockChange(x, y, z, this);
                return true;
            }

        }

        if (tileEntity instanceof TileEntityExplosive)
        {
            return ((TileEntityExplosive) tileEntity).explosive.handler.onBlockActivated(world, x, y, z, entityPlayer, par6, par7, par8, par9);
        }

        return false;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public int getRenderType()
    {
        return RenderBombBlock.ID;
    }

    @Override
    public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z)
    {
        if (world.getTileEntity(x, y, z) != null)
        {
            return new ItemStack(this, 1, ((TileEntityExplosive) world.getTileEntity(x, y, z)).explosive.ordinal());
        }

        return null;
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block par5, int par6)
    {
        TileEntity tileEntity = world.getTileEntity(x, y, z);

        if (tileEntity != null)
        {
            if (tileEntity instanceof TileEntityExplosive)
            {
                if (!((TileEntityExplosive) tileEntity).exploding)
                {
                    int explosiveID = ((TileEntityExplosive) tileEntity).explosive.ordinal();
                    InventoryUtility.dropItemStack(world, x, y, z, new ItemStack(ICBMClassic.blockExplosive, 1, explosiveID), 10, 0);
                }
            }
        }

        super.breakBlock(world, x, y, z, par5, par6);
    }

    @Override
    public int quantityDropped(Random par1Random)
    {
        return 0;
    }

    @Override
    public void getSubBlocks(Item par1, CreativeTabs par2CreativeTabs, List par3List)
    {
        for (Explosives zhaPin : Explosives.values())
        {
            if (zhaPin.handler.hasBlockForm())
            {
                par3List.add(new ItemStack(par1, 1, zhaPin.ordinal()));
            }
        }
    }

    @Override
    public TileEntity createTileEntity(World var1, int meta)
    {
        return new TileEntityExplosive();
    }

    @Override
    public boolean isOpaqueCube()
    {
        return false;
    }

    @Override
    public void onPostInit()
    {
        GameRegistry.registerTileEntity(TileEntityExplosive.class, "icbmCTileExplosive");
    }
}
