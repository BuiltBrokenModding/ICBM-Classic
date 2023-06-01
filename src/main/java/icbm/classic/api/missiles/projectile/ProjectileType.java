package icbm.classic.api.missiles.projectile;

import lombok.Data;
import net.minecraft.util.ResourceLocation;

@Data
public class ProjectileType {
    private final ResourceLocation ID;
    private final ProjectileType parent;

    public boolean isValidType(ProjectileType type) {
        if(type == this) {
            return true;
        }
        return parent != null && parent.isValidType(type);
    }
}
