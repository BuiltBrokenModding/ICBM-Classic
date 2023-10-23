package icbm.classic.lib.capability.chicken;

import icbm.classic.ICBMConstants;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

@Mod.EventBusSubscriber(modid = ICBMConstants.DOMAIN)
public class CapSpaceChicken implements ICapabilityProvider, INBTSerializable<NBTTagByte> {

    @CapabilityInject(CapSpaceChicken.class)
    public static Capability<CapSpaceChicken> INSTANCE;

    public static final ResourceLocation CHICKEN_CAP = new ResourceLocation(ICBMConstants.DOMAIN, "space_chicken");
    private static final DataParameter<Boolean> SPACE = EntityDataManager.<Boolean>createKey(EntityAgeable.class, DataSerializers.BOOLEAN);

    private final EntityChicken chicken;

    public CapSpaceChicken(EntityChicken chicken) {
        this.chicken = chicken;
    }

    public static boolean isSpace(EntityChicken chicken) {
        if(chicken.hasCapability(CapSpaceChicken.INSTANCE, null)) {
            final CapSpaceChicken cap = chicken.getCapability(CapSpaceChicken.INSTANCE, null);
            if(cap != null) {
                return cap.isSpace();
            }
        }
        return false;
    }

    @SubscribeEvent
    public static void attachCap(AttachCapabilitiesEvent<Entity> event) {
       if(event.getObject() instanceof EntityChicken) {
           event.addCapability(CHICKEN_CAP, new CapSpaceChicken((EntityChicken) event.getObject()));
       }
    }

    @SubscribeEvent
    public static void createEntityEvent(EntityEvent.EntityConstructing event) {
        if(event.getEntity() instanceof EntityChicken) {
            event.getEntity().getDataManager().register(SPACE, false);
        }
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == INSTANCE;
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        if(capability == INSTANCE) {
            return (T) this;
        }
        return null;
    }

    public static void register()
    {
        CapabilityManager.INSTANCE.register(CapSpaceChicken.class, new Capability.IStorage<CapSpaceChicken>()
            {
                @Nullable
                @Override
                public NBTBase writeNBT(Capability<CapSpaceChicken> capability, CapSpaceChicken instance, EnumFacing side) {
                    return new NBTTagByte(instance.isSpace() ? (byte)1 : (byte)0);
                }

                @Override
                public void readNBT(Capability<CapSpaceChicken> capability, CapSpaceChicken instance, EnumFacing side, NBTBase nbt) {
                    if(nbt instanceof NBTTagByte) {
                        instance.setSpace(((NBTTagByte) nbt).getByte() == 1);
                    }
                }
            },
            () -> new CapSpaceChicken(null));
    }

    @Override
    public NBTTagByte serializeNBT() {
        return new NBTTagByte(isSpace() ? (byte)1 : (byte)0);
    }

    @Override
    public void deserializeNBT(NBTTagByte nbt) {
        setSpace(nbt.getByte() == 1);
    }

    public boolean isSpace() {
        return Optional.ofNullable(chicken).map(c -> c.getDataManager().get(SPACE)).orElse(false);
    }

    public void setSpace(boolean space) {
       Optional.ofNullable(chicken).ifPresent(c -> c.getDataManager().set(SPACE, space));
    }
}
