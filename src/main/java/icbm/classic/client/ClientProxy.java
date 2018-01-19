package icbm.classic.client;

import icbm.classic.CommonProxy;
import icbm.classic.ICBMClassic;
import icbm.classic.client.render.entity.RenderEntityExplosive;
import icbm.classic.content.entity.EntityExplosive;
import icbm.classic.content.explosive.Explosives;
import icbm.classic.content.explosive.tile.BlockExplosive;
import icbm.classic.prefab.BlockICBM;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.DefaultStateMapper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Map;

@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy
{
    @Override
    public void doLoadModels()
    {
        //Glass
        newBlockModel(ICBMClassic.blockReinforcedGlass, 0, "inventory", "");
        newBlockModel(ICBMClassic.blockGlassPlate, 0, "inventory", "");
        newBlockModel(ICBMClassic.blockGlassButton, 0, "inventory", "");

        //Spikes
        newBlockModel(ICBMClassic.blockSpikes, 0, "inventory", "");
        newBlockModel(ICBMClassic.blockSpikes, 1, "inventory", "_poison");
        newBlockModel(ICBMClassic.blockSpikes, 2, "inventory", "_fire");

        //Concrete
        newBlockModel(ICBMClassic.blockConcrete, 0, "inventory", "");
        newBlockModel(ICBMClassic.blockConcrete, 1, "inventory", "_compact");
        newBlockModel(ICBMClassic.blockConcrete, 2, "inventory", "_reinforced");

        //Explosives
        final String resourcePath = ICBMClassic.blockExplosive.getRegistryName().toString();

        ModelLoader.setCustomStateMapper(ICBMClassic.blockExplosive, new DefaultStateMapper()
        {
            @Override
            protected ModelResourceLocation getModelResourceLocation(IBlockState state)
            {
                return new ModelResourceLocation(resourcePath, getPropertyString(state.getProperties()));
            }
        });
        for (Explosives ex : Explosives.values())
        {
            if (ex.handler.hasBlockForm())
            {
                IBlockState state = ICBMClassic.blockExplosive.getDefaultState().withProperty(BlockExplosive.EX_PROP, ex).withProperty(BlockICBM.ROTATION_PROP, EnumFacing.UP);
                ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ICBMClassic.blockExplosive), ex.ordinal(), new ModelResourceLocation(resourcePath, getPropertyString(state.getProperties())));
            }
        }

        //---------------------------------------
        //Entity renders
        //---------------------------------------
        RenderingRegistry.registerEntityRenderingHandler(EntityExplosive.class, manager -> new RenderEntityExplosive(manager));
    }

    protected void newBlockModel(Block block, int meta, String varient, String sub)
    {
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(block), meta, new ModelResourceLocation(block.getRegistryName() + sub, varient));
    }

    public static void registerWithMapper(Block block)
    {
        if (block != null)
        {
            final String resourcePath = block.getRegistryName().toString();

            ModelLoader.setCustomStateMapper(block, new DefaultStateMapper()
            {
                @Override
                protected ModelResourceLocation getModelResourceLocation(IBlockState state)
                {
                    return new ModelResourceLocation(resourcePath, getPropertyString(state.getProperties()));
                }
            });

            NonNullList<ItemStack> subBlocks = NonNullList.create();
            block.getSubBlocks(null, subBlocks);

            for (ItemStack stack : subBlocks)
            {
                IBlockState state = block.getStateFromMeta(stack.getMetadata()); //TODO check if works correctly with the state system
                ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(block), stack.getMetadata(), new ModelResourceLocation(resourcePath, getPropertyString(state.getProperties())));
            }
        }
    }

    public static String getPropertyString(Map<IProperty<?>, Comparable<?>> values, String... extrasArgs)
    {
        StringBuilder stringbuilder = new StringBuilder();

        for (Map.Entry<IProperty<?>, Comparable<?>> entry : values.entrySet())
        {
            if (stringbuilder.length() != 0)
            {
                stringbuilder.append(",");
            }

            IProperty<?> iproperty = entry.getKey();
            stringbuilder.append(iproperty.getName());
            stringbuilder.append("=");
            stringbuilder.append(getPropertyName(iproperty, entry.getValue()));
        }


        if (stringbuilder.length() == 0)
        {
            stringbuilder.append("inventory");
        }

        for (String args : extrasArgs)
        {
            if (stringbuilder.length() != 0)
            {
                stringbuilder.append(",");
            }
            stringbuilder.append(args);
        }

        return stringbuilder.toString();
    }

    @SuppressWarnings("unchecked")
    private static <T extends Comparable<T>> String getPropertyName(IProperty<T> property, Comparable<?> comparable)
    {
        return property.getName((T) comparable);
    }
}
