package icbm.classic.lib.capability.chicken;

import icbm.classic.ICBMConstants;
import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.ChickenEntity;
import net.minecraft.nbt.ByteNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

@Mod.EventBusSubscriber(modid = ICBMConstants.DOMAIN)
public class CapSpaceChicken implements ICapabilityProvider, INBTSerializable<ByteNBT> {

    @CapabilityInject(CapSpaceChicken.class)
    public static Capability<CapSpaceChicken> INSTANCE;

    public static final ResourceLocation CHICKEN_CAP = new ResourceLocation(ICBMConstants.DOMAIN, "space_chicken");
    private static final DataParameter<Boolean> SPACE = EntityDataManager.<Boolean>createKey(AgeableEntity.class, DataSerializers.BOOLEAN);

    private final ChickenEntity chicken;
    private LazyOptional<CapSpaceChicken> capOfSelf = LazyOptional.of(() -> this);

    public CapSpaceChicken(ChickenEntity chicken) {
        this.chicken = chicken;
    }

    public static boolean isSpace(ChickenEntity chicken) {
        if(chicken.getCapability(CapSpaceChicken.INSTANCE, null).isPresent()) {
            final LazyOptional<CapSpaceChicken> cap = chicken.getCapability(CapSpaceChicken.INSTANCE, null);
            if(cap.isPresent()) {
                return cap.orElseThrow(IllegalStateException::new).isSpace();
            }
        }
        return false;
    }

    //TODO @SubscribeEvent
    public static void attachCap(AttachCapabilitiesEvent<Entity> event) {
       if(event.getObject() instanceof ChickenEntity) {
           event.addCapability(CHICKEN_CAP, new CapSpaceChicken((ChickenEntity) event.getObject()));
       }
    }

    //TODO @SubscribeEvent
    public static void createEntityEvent(EntityEvent.EntityConstructing event) {
        if(event.getEntity() instanceof ChickenEntity) {
            event.getEntity().getDataManager().register(SPACE, false);
        }
    }

    @Nullable
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing) {
        if(capability == INSTANCE) {
            return capOfSelf.cast();
        }
        return null;
    }

    public static void register()
    {
        CapabilityManager.INSTANCE.register(CapSpaceChicken.class, new Capability.IStorage<CapSpaceChicken>()
            {
                @Nullable
                @Override
                public INBT writeNBT(Capability<CapSpaceChicken> capability, CapSpaceChicken instance, Direction side) {
                    return new ByteNBT(instance.isSpace() ? (byte)1 : (byte)0);
                }

                @Override
                public void readNBT(Capability<CapSpaceChicken> capability, CapSpaceChicken instance, Direction side, INBT nbt) {
                    if(nbt instanceof ByteNBT) {
                        instance.setSpace(((ByteNBT) nbt).getByte() == 1);
                    }
                }
            },
            () -> new CapSpaceChicken(null));
    }

    @Override
    public ByteNBT serializeNBT() {
        return new ByteNBT(isSpace() ? (byte)1 : (byte)0);
    }

    @Override
    public void deserializeNBT(ByteNBT nbt) {
        setSpace(nbt.getByte() == 1);
    }

    public boolean isSpace() {
        return Optional.ofNullable(chicken).map(c -> c.getDataManager().get(SPACE)).orElse(false);
    }

    public void setSpace(boolean space) {
       Optional.ofNullable(chicken).ifPresent(c -> c.getDataManager().set(SPACE, space));
    }
}
