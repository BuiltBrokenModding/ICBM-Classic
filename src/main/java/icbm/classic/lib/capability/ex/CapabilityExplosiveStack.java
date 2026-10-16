package icbm.classic.lib.capability.ex;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.caps.IExplosive;
import icbm.classic.api.explosion.IBlast;
import icbm.classic.api.reg.IExplosiveCustomization;
import icbm.classic.api.reg.IExplosiveData;
import icbm.classic.lib.RegistryHelper;
import icbm.classic.lib.StackNbt;
import icbm.classic.content.items.ExplosiveItemHelper;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.INBTSerializable;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Used by any item that has an explosive capability
 * Created by Dark(DarkGuardsman, Robin) on 1/7/19.
 * <p>
 * PORT: item capabilities are rebuilt per lookup. Customizations are read from the stack's custom data
 * (key {@link #STACK_KEY}, same as the 1.12 cap provider key) and written back when added.
 */
public class CapabilityExplosiveStack implements IExplosive, INBTSerializable<CompoundTag>
{
    public static final String STACK_KEY = "explosive";

    @Nullable
    private final ItemStack stack;
    private final List<IExplosiveCustomization> customizationList = new ArrayList();

    public CapabilityExplosiveStack(@Nullable ItemStack stack)
    {
        this.stack = stack;
        if (stack != null && StackNbt.has(stack, STACK_KEY))
        {
            deserializeNBT(RegistryHelper.access(), StackNbt.getCompound(stack, STACK_KEY));
        }
    }

    @Nullable
    @Override
    public IExplosiveData getExplosiveData()
    {
        if (stack == null)
        {
            return null;
        }
        // PORT: 1.12 used the stack metadata as explosive id, now the explosive component (missing = id 0 like meta 0)
        return ExplosiveItemHelper.getExplosiveData(stack);
    }

    @Override
    public void applyCustomizations(IBlast blast) {
        customizationList.forEach(c -> c.apply(getExplosiveData(), blast));
    }

    @Override
    public void addCustomization(IExplosiveCustomization customization) {
        customizationList.add(customization);
        if (stack != null && !stack.isEmpty())
        {
            StackNbt.putCompound(stack, STACK_KEY, serializeNBT(RegistryHelper.access()));
        }
    }

    @Override
    public void collectInformation(Consumer<String> collector){
        //TODO debug explosive id and name if F3+something keybind is enabled
        customizationList.forEach(c -> c.collectCustomizationInformation(collector));
    }

    @Nullable
    @Override
    public ItemStack toStack()
    {
        if (stack == null)
        {
            return ItemStack.EMPTY;
        }
        return stack.copyWithCount(1);
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider)
    {
        final CompoundTag save = new CompoundTag();
        if (!customizationList.isEmpty())
        {
            save.put("customizations", ICBMClassicAPI.EXPLOSIVE_CUSTOMIZATION_REGISTRY.save(customizationList));
        }
        return save;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt)
    {
        if(nbt.contains("customizations")) {
            ICBMClassicAPI.EXPLOSIVE_CUSTOMIZATION_REGISTRY.load(nbt.getList("customizations", Tag.TAG_COMPOUND), customizationList);
        }
    }
}
// PORTED
