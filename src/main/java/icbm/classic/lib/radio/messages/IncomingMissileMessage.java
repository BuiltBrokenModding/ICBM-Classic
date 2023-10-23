package icbm.classic.lib.radio.messages;

import icbm.classic.api.missiles.IMissile;
import icbm.classic.api.radio.messages.IIncomingMissileMessage;
import lombok.Data;
import net.minecraft.util.math.Vec3d;

import java.util.Optional;

@Data
public class IncomingMissileMessage implements IIncomingMissileMessage {
    private final String channel;
    private final IMissile missile;

    @Override
    public Vec3d getTarget() {
        return Optional.ofNullable(missile).map(IMissile::getVec3d).orElse(null);
    }
}
