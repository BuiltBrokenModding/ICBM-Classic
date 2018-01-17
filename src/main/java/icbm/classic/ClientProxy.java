package icbm.classic;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy
{
    @Override
    public void doLoadModels()
    {
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ICBMClassic.blockGlassPlate), 0, new ModelResourceLocation(ICBMClassic.blockGlassPlate.getRegistryName(), "inventory"));
    }
}
