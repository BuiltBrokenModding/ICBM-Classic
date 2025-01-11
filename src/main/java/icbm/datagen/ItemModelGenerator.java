package icbm.datagen;

import icbm.classic.ICBMConstants;
import icbm.classic.content.reg.ItemReg;
import net.minecraft.data.DataGenerator;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.generators.ExistingFileHelper;
import net.minecraftforge.client.model.generators.ItemModelProvider;
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


        singleLayerItem(ItemReg.ANTIDOTE_PILL);
    }

    private void singleLayerItem(RegistryObject<? extends Item> item) {
        this.withExistingParent(name(item), "item/generated")
            .texture("layer0", texture(name(item)));
    }

    private void grenade(RegistryObject<? extends Item> item) {
        this.withExistingParent(name(item), "icbm:item/grenade")
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
