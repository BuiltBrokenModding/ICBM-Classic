package icbm.classic.lib.capability.chicken;

import icbm.classic.IcbmConstants;
import net.minecraft.core.Direction;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityAgeable;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.passive.EntityChicken;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.common.capabilities.Capability;
import net.neoforged.common.capabilities.CapabilityManager;
import net.neoforged.event.AttachCapabilitiesEvent;
import net.neoforged.event.entity.EntityEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.capabilities.EntityCapability;
import net.neoforged.neoforge.common.util.INBTSerializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

@Mod.EventBusSubscriber(modid = IcbmConstants.MOD_ID)
public class CapSpaceChicken implements INBTSerializable<ByteTag> {

    public static final AttachmentType TYPE = AttachmentType.builder(iAttachmentHolder -> new CapSpaceChicken((Chicken) iAttachmentHolder)).build();
    public static final ResourceLocation CHICKEN_CAP = new ResourceLocation(IcbmConstants.MOD_ID, "space_chicken");

    public static EntityCapability<CapSpaceChicken, Void> INSTANCE = EntityCapability.createVoid(CHICKEN_CAP, CapSpaceChicken.class);

    private static final DataParameter<Boolean> SPACE = EntityDataManager.<Boolean>createKey(EntityAgeable.class, DataSerializers.BOOLEAN);

    private final Chicken chicken;

    public CapSpaceChicken(Chicken chicken) {
        this.chicken = chicken;
    }

    public static boolean isSpace(Chicken chicken) {
        if (chicken.hasData(CapSpaceChicken.TYPE)) {
            final CapSpaceChicken cap = chicken.getCapability(CapSpaceChicken.INSTANCE);
            if (cap != null) {
                return cap.isSpace();
            }
        }
        return false;
    }

    @SubscribeEvent
    public static void attachCap(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof EntityChicken) {
            event.addCapability(CHICKEN_CAP, new CapSpaceChicken((EntityChicken) event.getObject()));
        }
    }

    @SubscribeEvent
    public static void createEntityEvent(EntityEvent.EntityConstructing event) {
        if (event.getEntity() instanceof EntityChicken) {
            event.getEntity().getDataManager().register(SPACE, false);
        }
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable Direction facing) {
        return capability == INSTANCE;
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing) {
        if (capability == INSTANCE) {
            return (T) this;
        }
        return null;
    }

    public static void register() {
        CapabilityManager.INSTANCE.register(CapSpaceChicken.class, new Capability.IStorage<CapSpaceChicken>() {
                @Nullable
                @Override
                public NBTBase writeNBT(Capability<CapSpaceChicken> capability, CapSpaceChicken instance, Direction side) {
                    return new NBTTagByte(instance.isSpace() ? (byte) 1 : (byte) 0);
                }

                @Override
                public void readNBT(Capability<CapSpaceChicken> capability, CapSpaceChicken instance, Direction side, NBTBase nbt) {
                    if (nbt instanceof NBTTagByte) {
                        instance.setSpace(((NBTTagByte) nbt).getByte() == 1);
                    }
                }
            },
            () -> new CapSpaceChicken(null));
    }

    @Override
    public NBTTagByte serializeNBT() {
        return new NBTTagByte(isSpace() ? (byte) 1 : (byte) 0);
    }

    @Override
    public void deserializeNBT(ByteTag nbt) {
        setSpace(nbt.getAsByte() == 1);
    }

    public boolean isSpace() {
        return Optional.ofNullable(chicken).map(c -> c.getDataManager().get(SPACE)).orElse(false);
    }

    public void setSpace(boolean space) {
        Optional.ofNullable(chicken).ifPresent(c -> c.getDataManager().set(SPACE, space));
    }
}
