package icbm.datagen;

import icbm.classic.ICBMClassic;
import icbm.classic.ICBMConstants;
import icbm.classic.api.refs.ICBMExplosives;
import icbm.classic.content.reg.BlockReg;
import net.minecraft.block.Block;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.data.RecipeProvider;
import net.minecraft.data.ShapedRecipeBuilder;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

import java.util.function.Consumer;

public class RecipeGenerator extends RecipeProvider {
    public RecipeGenerator(DataGenerator generatorIn) {
        super(generatorIn);
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    protected void registerRecipes(Consumer<IFinishedRecipe> pWriter) {
        shaped(BlockReg.EXPLOSIVE_GRAVITY.get())
            .patternLine("eee")
            .patternLine("ere")
            .patternLine("eee")
            .key('e', Items.ENDER_EYE)
            .key('r', BlockReg.EXPLOSIVE_SHRAPNEL.get())
            .addCriterion("item", hasItem(Items.ENDER_EYE))
            .build(pWriter);
    }

    private ShapedRecipeBuilder shaped(Item item, int count) {
        return ShapedRecipeBuilder.shapedRecipe(item, count);
    }

    private ShapedRecipeBuilder shaped(Item item) {
        return shaped(item, 1);
    }

    private ShapedRecipeBuilder shaped(Block block) {
        return shaped(block.asItem(), 1);
    }
}
