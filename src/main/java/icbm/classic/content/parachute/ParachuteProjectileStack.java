package icbm.classic.content.parachute;

import icbm.classic.api.missiles.projectile.IProjectileData;
import icbm.classic.api.missiles.projectile.IProjectileStack;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;

public class ParachuteProjectileStack implements IProjectileStack<EntityParachute>, INBTSerializable<NBTTagCompound> {

    /**
     * ItemStack to use to spawn as a passenger of this parachute
     */
    @Getter @Setter @Accessors(chain = true)
    private ItemStack passengerItemStack = ItemStack.EMPTY;

    @Override
    public NBTTagCompound serializeNBT() {
        final NBTTagCompound save = new NBTTagCompound();
        save.setTag("passengerItemStack", passengerItemStack.serializeNBT());
        return save;
    }

    @Override
    public void deserializeNBT(NBTTagCompound save) {
        if(save.hasKey("passengerItemStack", 10)) {
            passengerItemStack = new ItemStack(save.getCompoundTag("passengerItemStack"));
        }
    }

    @Override
    public IProjectileData<EntityParachute> getProjectileData() {
        return new ParachuteProjectileData().setPassengerItemStack(passengerItemStack);
    }
}
