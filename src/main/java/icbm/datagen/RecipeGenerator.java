package icbm.datagen;

import icbm.classic.content.blocks.explosive.BlockExplosive;
import icbm.classic.content.reg.BlockReg;
import icbm.classic.content.reg.ItemReg;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.data.RecipeProvider;
import net.minecraft.data.ShapedRecipeBuilder;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.Tags;

import java.util.function.Consumer;

public class RecipeGenerator extends RecipeProvider {
    public RecipeGenerator(DataGenerator generatorIn) {
        super(generatorIn);
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    protected void registerRecipes(Consumer<IFinishedRecipe> pWriter) {
        BlockExplosive explosiveCondensed = BlockReg.EXPLOSIVE_CONDENSED.get();

        // TODO  register the tags properly and add it to some item so that the recipes show up in JEI
        Tag<Item> DUSTS_SULFUR = new ItemTags.Wrapper(new ResourceLocation("forge", "dusts/sulfur")); // needed for Debilitation and Incendiary
        Tag<Item> INGOT_URANIUM = new ItemTags.Wrapper(new ResourceLocation("forge", "ingots/uranium")); // needed for Nuclear
        Tag<Item> INGOT_COPPER = new ItemTags.Wrapper(new ResourceLocation("forge", "ingots/copper")); // needed for defuser
        Tag<Item> PLANK_WOOD = new ItemTags.Wrapper(new ResourceLocation("forge", "plank_wood"));
        Tag<Item> CIRCUIT_BASIC = new ItemTags.Wrapper(new ResourceLocation("forge", "circuit_basic"));
        Tag<Item> INGOT_STEEL = new ItemTags.Wrapper(new ResourceLocation("forge", "ingots/steel"));
        Tag<Item> CIRCUIT_ADVANCED = new ItemTags.Wrapper(new ResourceLocation("forge", "circuit_advanced"));

        // reference for porting recipes: https://github.com/BuiltBrokenModding/ICBM-Classic/tree/prod/1.12/src/main/resources/assets/icbmclassic/recipes
        // ================== EXPLOSIVES ==================
        shaped(BlockReg.EXPLOSIVE_GRAVITY.get())
            .patternLine("eee")
            .patternLine("ere")
            .patternLine("eee")
            .key('e', Items.ENDER_EYE)
            .key('r', BlockReg.EXPLOSIVE_SHRAPNEL.get())
            .addCriterion("item", hasItem(Items.ENDER_EYE))
            .build(pWriter);

        shaped(BlockReg.EXPLOSIVE_ATTRACTIVE.get())
            .patternLine("cc")
            .key('c', explosiveCondensed)
            .addCriterion("item", hasItem(explosiveCondensed))
            .build(pWriter);

        shaped(BlockReg.EXPLOSIVE_BREACHING.get(), 2)
            .patternLine("grg")
            .patternLine("grg")
            .patternLine("grg")
            .key('g', Items.GUNPOWDER)
            .key('r', explosiveCondensed)
            .addCriterion("item", hasItem(explosiveCondensed))
            .build(pWriter);

        shaped(BlockReg.EXPLOSIVE_CHEMICAL.get())
            .patternLine("ppp")
            .patternLine("prp")
            .patternLine("ppp")
            .key('p', ItemReg.DUST_POISON.get())
            .key('r', BlockReg.EXPLOSIVE_DEBILITATION.get())
            .addCriterion("item", hasItem(BlockReg.EXPLOSIVE_DEBILITATION.get()))
            .build(pWriter);

        shaped(BlockReg.EXPLOSIVE_COLOR.get())
            .patternLine("CCC")
            .patternLine("CrC")
            .patternLine("CCC")
            .key('C', Tags.Items.DYES)
            .key('r', BlockReg.EXPLOSIVE_REPULSIVE.get())
            .addCriterion("item", hasItem(BlockReg.EXPLOSIVE_REPULSIVE.get()))
            .build(pWriter);

        shaped(explosiveCondensed, 3)
            .patternLine("trt")
            .patternLine("rtr")
            .patternLine("trt")
            .key('t', Items.TNT)
            .key('r', Items.REDSTONE)
            .addCriterion("item", hasItem(Items.TNT))
            .build(pWriter);

        shaped(BlockReg.EXPLOSIVE_CONTAGIOUS.get(), 2)
            .patternLine(" c ")
            .patternLine("crc")
            .patternLine(" c ")
            .key('r', Items.ROTTEN_FLESH)
            .key('c', BlockReg.EXPLOSIVE_CHEMICAL.get())
            .addCriterion("item", hasItem(BlockReg.EXPLOSIVE_CHEMICAL.get()))
            .build(pWriter);

        shaped(BlockReg.EXPLOSIVE_DEBILITATION.get(), 3)
            .patternLine("sss")
            .patternLine("wrw")
            .patternLine("sss")
            .key('r', BlockReg.EXPLOSIVE_SHRAPNEL.get())
            .key('w', Items.WATER_BUCKET)
            .key('s', DUSTS_SULFUR)
            .addCriterion("item", hasItem(BlockReg.EXPLOSIVE_SHRAPNEL.get()))
            .build(pWriter);

        // TODO need Battery and Wire items
//        shaped(BlockReg.EXPLOSIVE_EMP.get(), 1)
//            .patternLine("rer")
//            .patternLine("sas")
//            .patternLine("sss")
//            .key('a', BlockReg.EXPLOSIVE_REPULSIVE.get())
//            .key('e', ItemReg.Battery.get())
//            .key('r', Items.REDSTONE)
//            .key('s', ItemReg.Wire.get())
//            .addCriterion("item", hasItem(BlockReg.EXPLOSIVE_REPULSIVE.get()))
//            .build(pWriter);

        shaped(BlockReg.EXPLOSIVE_ENDER.get())
            .patternLine("ses")
            .patternLine("eae")
            .patternLine("ses")
            .key('a', BlockReg.EXPLOSIVE_ATTRACTIVE.get())
            .key('e', Items.ENDER_EYE)
            .key('s', Blocks.END_STONE)
            .addCriterion("item", hasItem(BlockReg.EXPLOSIVE_ATTRACTIVE.get()))
            .build(pWriter);

        shaped(BlockReg.EXPLOSIVE_ENDOTHERMIC.get())
            .patternLine("isi")
            .patternLine("sas")
            .patternLine("isi")
            .key('a', BlockReg.EXPLOSIVE_ATTRACTIVE.get())
            .key('i', Blocks.ICE)
            .key('s', Blocks.SNOW)
            .addCriterion("item", hasItem(BlockReg.EXPLOSIVE_ATTRACTIVE.get()))
            .build(pWriter);

        shaped(BlockReg.EXPLOSIVE_EXOTHERMIC.get())
            .patternLine("iii")
            .patternLine("igi")
            .patternLine("iii")
            .key('i', BlockReg.EXPLOSIVE_INCENDIARY.get())
            .key('g', Blocks.GLASS)
            .addCriterion("item", hasItem(BlockReg.EXPLOSIVE_INCENDIARY.get()))
            .build(pWriter);

        shaped(BlockReg.EXPLOSIVE_FRAGMENTATION.get())
            .patternLine(" a ")
            .patternLine("ara")
            .patternLine(" a ")
            .key('a', BlockReg.EXPLOSIVE_SHRAPNEL.get())
            .key('r', BlockReg.EXPLOSIVE_INCENDIARY.get())
            .addCriterion("item", hasItem(BlockReg.EXPLOSIVE_INCENDIARY.get()))
            .build(pWriter);

        shaped(BlockReg.EXPLOSIVE_INCENDIARY.get())
            .patternLine("aaa")
            .patternLine("ara")
            .patternLine("ala")
            .key('a', DUSTS_SULFUR)
            .key('l', Items.LAVA_BUCKET)
            .key('r', BlockReg.EXPLOSIVE_SHRAPNEL.get())
            .addCriterion("item", hasItem(BlockReg.EXPLOSIVE_SHRAPNEL.get()))
            .build(pWriter);

        shaped(BlockReg.EXPLOSIVE_NUCLEAR.get())
            .patternLine("tit")
            .patternLine("iii")
            .patternLine("tit")
            .key('i', INGOT_URANIUM)
            .key('t', BlockReg.EXPLOSIVE_THERMOBARIC.get())
            .addCriterion("item", hasItem(BlockReg.EXPLOSIVE_THERMOBARIC.get()))
            .build(pWriter);

        shaped(BlockReg.EXPLOSIVE_REPULSIVE.get())
            .patternLine("c")
            .patternLine("c")
            .key('c', explosiveCondensed)
            .addCriterion("item", hasItem(explosiveCondensed))
            .build(pWriter);

        shaped(BlockReg.EXPLOSIVE_SHRAPNEL.get())
            .patternLine("aaa")
            .patternLine("aca")
            .patternLine("aaa")
            .key('a', Items.ARROW)
            .key('c', explosiveCondensed)
            .addCriterion("item", hasItem(explosiveCondensed))
            .build(pWriter);

        shaped(BlockReg.EXPLOSIVE_SMOKE.get())
            .patternLine("ccc")
            .patternLine("crc")
            .patternLine("ccc")
            .key('c', Items.COAL)
            .key('r', BlockReg.EXPLOSIVE_REPULSIVE.get())
            .addCriterion("item", hasItem(BlockReg.EXPLOSIVE_REPULSIVE.get()))
            .build(pWriter);

        shaped(BlockReg.EXPLOSIVE_SONIC.get())
            .patternLine("ini")
            .patternLine("nrn")
            .patternLine("ini")
            .key('i', Items.IRON_INGOT)
            .key('n', Blocks.NOTE_BLOCK)
            .key('r', BlockReg.EXPLOSIVE_REPULSIVE.get())
            .addCriterion("item", hasItem(BlockReg.EXPLOSIVE_REPULSIVE.get()))
            .build(pWriter);

        shaped(BlockReg.EXPLOSIVE_THERMOBARIC.get())
            .patternLine("cic")
            .patternLine("iri")
            .patternLine("cic")
            .key('i', BlockReg.EXPLOSIVE_INCENDIARY.get())
            .key('c', BlockReg.EXPLOSIVE_CHEMICAL.get())
            .key('r', BlockReg.EXPLOSIVE_REPULSIVE.get())
            .addCriterion("item", hasItem(BlockReg.EXPLOSIVE_REPULSIVE.get()))
            .build(pWriter);

        // ------ VANILLA implementations ------ // TODO check if we can easily use the mekanism editions instead

        shaped(BlockReg.EXPLOSIVE_ANTIMATTER.get())
            .patternLine("nnn")
            .patternLine("nrn")
            .patternLine("nnn")
            .key('n', Items.NETHER_STAR)
            .key('r', BlockReg.EXPLOSIVE_NUCLEAR.get())
            .addCriterion("item", hasItem(BlockReg.EXPLOSIVE_NUCLEAR.get()))
            .build(pWriter);

        shaped(BlockReg.EXPLOSIVE_REDMATTER.get())
            .patternLine("nnn")
            .patternLine("nrn")
            .patternLine("nnn")
            .key('n', Items.NETHER_STAR)
            .key('r', BlockReg.EXPLOSIVE_ANTIMATTER.get())
            .addCriterion("item", hasItem(BlockReg.EXPLOSIVE_ANTIMATTER.get()))
            .build(pWriter);

        // -------------------------------------
        // ================================================

        // ======== CRAFTING ITEMS ========
        shaped(ItemReg.DUST_POISON.get(), 3)
            .patternLine("er")
            .key('e', Items.SPIDER_EYE)
            .key('r', Items.ROTTEN_FLESH)
            .addCriterion("item", hasItem(Items.SPIDER_EYE))
            .build(pWriter);

        shaped(ItemReg.ANTIDOTE_PILL.get(), 6)
            .patternLine("sss")
            .patternLine("sss")
            .patternLine("sss")
            .key('s', Tags.Items.SEEDS)
            .addCriterion("item", hasItem(Tags.Items.SEEDS))
            .build(pWriter);
        // ================================

        // ======== TOOLS ========
        shaped(ItemReg.TOOL_DEACTIVATION_KIT.get())
            .patternLine("w w")
            .patternLine("rar")
            .patternLine("ici")
            .key('i', INGOT_COPPER)
            .key('c', CIRCUIT_BASIC)
            .key('w', PLANK_WOOD)
            .key('r', Items.REDSTONE)
            .key('a', Items.SHEARS)
            .addCriterion("item", hasItem(INGOT_COPPER))
            .build(pWriter);

        shaped(ItemReg.TOOL_POSITION_LASER.get())
            .patternLine("gci")
            .patternLine(" bw")
            .key('g', Blocks.GLASS)
            .key('b', Blocks.STONE_BUTTON)
            .key('i', INGOT_STEEL)
            .key('c', CIRCUIT_BASIC)
            .key('w', PLANK_WOOD)
            .addCriterion("item", hasItem(INGOT_STEEL))
            .build(pWriter);

        shaped(ItemReg.TOOL_DETONATOR_REMOTE.get())
            .patternLine("iri")
            .patternLine("ici")
            .patternLine("ibi")
            .key('r', Items.REDSTONE_TORCH)
            .key('b', Items.STONE_BUTTON)
            .key('i', INGOT_STEEL)
            .key('c', CIRCUIT_ADVANCED)
            .addCriterion("item", hasItem(INGOT_COPPER))
            .build(pWriter);
        // ================================
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

    private ShapedRecipeBuilder shaped(Block block, int count) {
        return shaped(block.asItem(), count);
    }
}
