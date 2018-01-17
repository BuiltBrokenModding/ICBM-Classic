package icbm.classic;

import net.minecraft.block.Block;
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
        newBlockModel(ICBMClassic.blockGlassPlate, 0, "inventory", "");
        newBlockModel(ICBMClassic.blockGlassButton, 0, "inventory", "");
        newBlockModel(ICBMClassic.blockSpikes, 0, "inventory", "");
        newBlockModel(ICBMClassic.blockSpikes, 1, "inventory", "_poison");
        newBlockModel(ICBMClassic.blockSpikes, 2, "inventory", "_fire");
        //SpikeMeshDefinition.init();
    }

    protected void newBlockModel(Block block, int meta, String varient, String sub)
    {
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(block), meta, new ModelResourceLocation(block.getRegistryName() + sub, varient));
    }
}
