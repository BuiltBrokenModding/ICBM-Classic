package icbm.classic.lib.capability.chicken;

import icbm.classic.ICBMClassic;
import icbm.classic.ICBMConstants;
import icbm.classic.content.reg.AttachmentReg;
import icbm.classic.lib.network.lambda.PacketCodexReg;
import icbm.classic.lib.network.lambda.entity.PacketCodexEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.animal.Chicken;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.Optional;

/**
 * Space chicken state (helmet render + missile seat interaction).
 * <p>
 * PORT: 1.12 attached a capability and a DataParameter to every chicken. Now the state lives in the
 * {@link AttachmentReg#SPACE_CHICKEN} data attachment (saved with the entity). NeoForge 21.1 attachments
 * do not sync, so changes and start-tracking send {@link #PACKET_SYNC} to clients.
 */
@EventBusSubscriber(modid = ICBMConstants.DOMAIN)
public class CapSpaceChicken {

    public static final ResourceLocation CHICKEN_CAP = ResourceLocation.fromNamespaceAndPath(ICBMConstants.DOMAIN, "space_chicken");

    public static final PacketCodexEntity<Chicken, Chicken> PACKET_SYNC = (PacketCodexEntity<Chicken, Chicken>) new PacketCodexEntity<Chicken, Chicken>(CHICKEN_CAP, "sync")
        .fromServer()
        .nodeBoolean(CapSpaceChicken::isSpace, (chicken, space) -> chicken.setData(AttachmentReg.SPACE_CHICKEN, space));

    private static boolean registered = false;

    private final Chicken chicken;

    public CapSpaceChicken(Chicken chicken) {
        this.chicken = chicken;
    }

    /** Registers the sync packet codex, must be called during mod construction in a fixed order */
    public static void register() {
        if (!registered) {
            registered = true;
            PacketCodexReg.register(PACKET_SYNC);
        }
    }

    public static boolean isSpace(Chicken chicken) {
        // hasData first, getData would create and save the default attachment on every chicken that is only looked at
        return chicken != null && chicken.hasData(AttachmentReg.SPACE_CHICKEN.get()) && chicken.getData(AttachmentReg.SPACE_CHICKEN);
    }

    public static void setSpace(Chicken chicken, boolean space) {
        if (chicken == null) {
            return;
        }
        chicken.setData(AttachmentReg.SPACE_CHICKEN, space);
        if (!chicken.level().isClientSide()) {
            PACKET_SYNC.sendToAllAround(chicken);
        }
    }

    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking event) {
        if (event.getTarget() instanceof Chicken chicken && event.getEntity() instanceof ServerPlayer player && isSpace(chicken)) {
            ICBMClassic.packetHandler.sendToPlayer(PACKET_SYNC.build(chicken), player);
        }
    }

    public boolean isSpace() {
        return Optional.ofNullable(chicken).map(CapSpaceChicken::isSpace).orElse(false);
    }

    public void setSpace(boolean space) {
        setSpace(chicken, space);
    }
}
// PORTED
