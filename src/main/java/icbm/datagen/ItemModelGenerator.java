package icbm.datagen;

import icbm.classic.ICBMConstants;
import icbm.classic.content.reg.ItemReg;
import net.minecraft.data.DataGenerator;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.generators.ExistingFileHelper;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.fml.RegistryObject;

public class ItemModelGenerator extends ItemModelProvider {
    public ItemModelGenerator(DataGenerator generator, String modid, ExistingFileHelper existingFileHelper) {
        super(generator, modid, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        singleLayerItem(ItemReg.DUST_POISON);
        singleLayerItem(ItemReg.DUST_SULFUR);
        singleLayerItem(ItemReg.DUST_SALTPETER);
        singleLayerItem(ItemReg.CLUMP_SALTPETER);

        grenade(ItemReg.GRENADE_CONDENSED); //TODO consider gradle tool to flatten paths
        grenade(ItemReg.GRENADE_SHRAPNEL);
        grenade(ItemReg.GRENADE_INCENDIARY);
        grenade(ItemReg.GRENADE_DEBILITATION);
        grenade(ItemReg.GRENADE_CHEMICAL);
        grenade(ItemReg.GRENADE_ANVIL);
        grenade(ItemReg.GRENADE_REPULSIVE);
        grenade(ItemReg.GRENADE_ATTRACTIVE);

        singleLayerItem(ItemReg.MINECART_CONDENSED, "icbm:items/explosive_minecart"); //TODO unique texture per cart
        singleLayerItem(ItemReg.MINECART_SHRAPNEL, "icbm:items/explosive_minecart");
        singleLayerItem(ItemReg.MINECART_INCENDIARY, "icbm:items/explosive_minecart");
        singleLayerItem(ItemReg.MINECART_DEBILITATION, "icbm:items/explosive_minecart");
        singleLayerItem(ItemReg.MINECART_CHEMICAL, "icbm:items/explosive_minecart");
        singleLayerItem(ItemReg.MINECART_ANVIL, "icbm:items/explosive_minecart");
        singleLayerItem(ItemReg.MINECART_REPULSIVE, "icbm:items/explosive_minecart");
        singleLayerItem(ItemReg.MINECART_ATTRACTIVE, "icbm:items/explosive_minecart");
        singleLayerItem(ItemReg.MINECART_COLOR, "icbm:items/explosive_minecart");
        singleLayerItem(ItemReg.MINECART_SMOKE, "icbm:items/explosive_minecart");
        singleLayerItem(ItemReg.MINECART_FRAGMENTATION, "icbm:items/explosive_minecart");
        singleLayerItem(ItemReg.MINECART_CONTAGIOUS, "icbm:items/explosive_minecart");
        singleLayerItem(ItemReg.MINECART_SONIC, "icbm:items/explosive_minecart");
        singleLayerItem(ItemReg.MINECART_BREACHING, "icbm:items/explosive_minecart");
        singleLayerItem(ItemReg.MINECART_THERMOBARIC, "icbm:items/explosive_minecart");
        singleLayerItem(ItemReg.MINECART_NUCLEAR, "icbm:items/explosive_minecart");
        singleLayerItem(ItemReg.MINECART_EMP, "icbm:items/explosive_minecart");
        singleLayerItem(ItemReg.MINECART_EXOTHERMIC, "icbm:items/explosive_minecart");
        singleLayerItem(ItemReg.MINECART_ENDOTHERMIC, "icbm:items/explosive_minecart");
        singleLayerItem(ItemReg.MINECART_GRAVITY, "icbm:items/explosive_minecart");
        singleLayerItem(ItemReg.MINECART_ENDER, "icbm:items/explosive_minecart");
        singleLayerItem(ItemReg.MINECART_ANTIMATTER, "icbm:items/explosive_minecart");
        singleLayerItem(ItemReg.MINECART_REDMATTER, "icbm:items/explosive_minecart");

        this.withExistingParent(name(ItemReg.BOMBLET_EMPTY), "icbm:item/base/bomblet");
        this.withExistingParent(name(ItemReg.BOMBLET_CONDENSED), "icbm:item/base/bomblet");

        singleLayerItem(ItemReg.PARACHUTE);
        singleLayerItem(ItemReg.BALLON);

        singleLayerItem(ItemReg.ANTIDOTE_PILL);
        
        //-------------------------------------------

        block(ItemReg.EXPLOSIVE_CONDENSED);
        block(ItemReg.EXPLOSIVE_SHRAPNEL);
        block(ItemReg.EXPLOSIVE_INCENDIARY);
        block(ItemReg.EXPLOSIVE_DEBILITATION);
        block(ItemReg.EXPLOSIVE_CHEMICAL);
        block(ItemReg.EXPLOSIVE_ANVIL);
        block(ItemReg.EXPLOSIVE_REPULSIVE);
        block(ItemReg.EXPLOSIVE_ATTRACTIVE);
        block(ItemReg.EXPLOSIVE_COLOR);
        block(ItemReg.EXPLOSIVE_SMOKE);
        block(ItemReg.EXPLOSIVE_FRAGMENTATION);
        block(ItemReg.EXPLOSIVE_CONTAGIOUS);
        block(ItemReg.EXPLOSIVE_SONIC);
        block(ItemReg.EXPLOSIVE_BREACHING);
        block(ItemReg.EXPLOSIVE_THERMOBARIC);
        block(ItemReg.EXPLOSIVE_NUCLEAR);
        block(ItemReg.EXPLOSIVE_EMP);
        block(ItemReg.EXPLOSIVE_EXOTHERMIC);
        block(ItemReg.EXPLOSIVE_ENDOTHERMIC);
        block(ItemReg.EXPLOSIVE_GRAVITY);
        block(ItemReg.EXPLOSIVE_ENDER);
        block(ItemReg.EXPLOSIVE_ANTIMATTER);
        block(ItemReg.EXPLOSIVE_REDMATTER);

        block(ItemReg.CONCRETE_NORMAL);
        block(ItemReg.CONCRETE_COMPACT);
        block(ItemReg.CONCRETE_REINFORCED);
        block(ItemReg.GLASS_REINFORCED);

        block(ItemReg.SPIKE_NORMAL);
        block(ItemReg.SPIKE_FIRE);
        block(ItemReg.SPIKE_POISON);

        block(ItemReg.RADAR_SCREEN);
        block(ItemReg.LAUNCHER_FRAME_BASE);
        block(ItemReg.LAUNCHER_FRAME_TOP);
        block(ItemReg.LAUNCHER_FRAME);
        block(ItemReg.LAUNCHER_BASE);
        block(ItemReg.LAUNCHER_CONNECTOR);
        block(ItemReg.LAUNCHER_SCREEN);
        block(ItemReg.LAUNCHER_CRUISE);

        block(ItemReg.RADIOACTIVE_DIRT);
        block(ItemReg.RADIOACTIVE_STONE);
    }

    private void block(RegistryObject<? extends Item> item) {
        this.block(item, name(item));
    }

    private void block(RegistryObject<? extends Item> item, String model) {
        this.getBuilder(name(item)).parent(new ModelFile.UncheckedModelFile("icbm:block/" + model));
    }

    private void singleLayerItem(RegistryObject<? extends Item> item) {
        this.singleLayerItem(item, texture(name(item)));
    }

    private void heldItem(RegistryObject<? extends Item> item, ResourceLocation texture) {
        this.withExistingParent(name(item), "item/handheld")
            .texture("layer0", texture);
    }

    private void singleLayerItem(RegistryObject<? extends Item> item, ResourceLocation texture) {
        this.withExistingParent(name(item), "item/generated")
            .texture("layer0", texture);
    }

    private void singleLayerItem(RegistryObject<? extends Item> item, String texture) {
        this.withExistingParent(name(item), "item/generated")
            .texture("layer0", texture);
    }

    private void grenade(RegistryObject<? extends Item> item) {
        this.withExistingParent(name(item), "icbm:item/base/grenade")
            .texture("layer0", texture("grenade" + "/" + name(item).replace("grenade" + "_", "")));
    }

    private String name(RegistryObject<? extends Item> item) {
        return item.getId().getPath();
    }

    private ResourceLocation texture(String path) {
        return new ResourceLocation(ICBMConstants.DOMAIN, "items/" + path);
    }

    @Override
    public String getName() {
        return "ICBM Item Models";
    }
}
