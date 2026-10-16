package icbm.classic.content.reg;

import com.mojang.serialization.Codec;
import icbm.classic.ICBMConstants;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

/**
 * Data attachments, replaces 1.12 capabilities attached to vanilla objects that held state.
 */
public final class AttachmentReg
{
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, ICBMConstants.DOMAIN);

    /**
     * Space chicken state for chickens (1.12 CapSpaceChicken). Client copy is synced with a packet.
     * Only true is saved, like the 1.12 DataParameter a normal chicken carries no extra data.
     */
    public static final Supplier<AttachmentType<Boolean>> SPACE_CHICKEN = ATTACHMENTS.register("space_chicken",
        () -> AttachmentType.builder(() -> false).serialize(Codec.BOOL, space -> space).build());

    private AttachmentReg()
    {
    }

    public static void register(IEventBus modBus)
    {
        ATTACHMENTS.register(modBus);
    }
}
