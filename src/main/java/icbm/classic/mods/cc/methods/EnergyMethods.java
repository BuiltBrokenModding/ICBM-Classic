package icbm.classic.mods.cc.methods;

import dan200.computercraft.api.lua.LuaException;
import icbm.classic.mods.cc.builder.Peripheral;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

@NoArgsConstructor(access = AccessLevel.NONE)
public final class EnergyMethods {

    public static <T extends TileEntity> Object[] getEnergy(Peripheral<T> peripheral) throws LuaException {
        return new Object[] {getStorage(peripheral).getEnergyStored()};
    }

    public static <T extends TileEntity> Object[] getEnergyLimit(Peripheral<T> peripheral) throws LuaException {
        return new Object[] {getStorage(peripheral).getMaxEnergyStored()};
    }

    public static <T extends TileEntity> Object[] getEnergyData(Peripheral<T> peripheral) throws LuaException {
        return new Object[] {getStorage(peripheral).getEnergyStored(), getStorage(peripheral).getMaxEnergyStored()};
    }

    private static <T extends TileEntity> IEnergyStorage getStorage(Peripheral<T> peripheral) throws LuaException {
        if(!peripheral.getTile().hasCapability(CapabilityEnergy.ENERGY, peripheral.getAccessedSide())) { //TODO allow overriding side used
            throw new LuaException("Error: Tile doesn't support IEnergyStorage capability");
        }

        final IEnergyStorage storage = peripheral.getTile().getCapability(CapabilityEnergy.ENERGY, peripheral.getAccessedSide());
        if(storage == null) {
            throw new LuaException("Error: Tile didn't provide IEnergyStorage capability");
        }
        return storage;
    }
}
