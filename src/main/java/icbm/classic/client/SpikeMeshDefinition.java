package icbm.classic.client;

import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.client.model.ModelLoader;

/**
 * Created by Dark(DarkGuardsman, Robert) on 6/19/2017.
 */
public class SpikeMeshDefinition implements ItemMeshDefinition {
    public final ModelResourceLocation base;
    public final ModelResourceLocation fire;
    public final ModelResourceLocation poison;

    public static SpikeMeshDefinition INSTANCE;

    private SpikeMeshDefinition() {
        base = new ModelResourceLocation(BlockReg.blockSpikes.getRegistryName(), "inventory");
        fire = new ModelResourceLocation(BlockReg.blockSpikes.getRegistryName() + "_fire", "inventory");
        poison = new ModelResourceLocation(BlockReg.blockSpikes.getRegistryName() + "_poison", "inventory");

        ModelBakery.registerItemVariants(Item.getItemFromBlock(BlockReg.blockSpikes), base);
        ModelBakery.registerItemVariants(Item.getItemFromBlock(BlockReg.blockSpikes), base);
        ModelBakery.registerItemVariants(Item.getItemFromBlock(BlockReg.blockSpikes), fire);
        ModelBakery.registerItemVariants(Item.getItemFromBlock(BlockReg.blockSpikes), poison);
        ModelLoader.setCustomMeshDefinition(Item.getItemFromBlock(BlockReg.blockSpikes), this);
    }

    public static void init() {
        INSTANCE = new SpikeMeshDefinition();
    }

    @Override
    public ModelResourceLocation getModelLocation(ItemStack stack) {
        if (stack.getItemDamage() == 1) {
            return poison;
        } else if (stack.getItemDamage() == 2) {
            return fire;
        }
        return base;
    }
}
