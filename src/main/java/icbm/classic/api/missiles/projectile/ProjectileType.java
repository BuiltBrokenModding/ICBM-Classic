package icbm.classic.api.missiles.projectile;

import lombok.Data;
import net.minecraft.util.ResourceLocation;

@Data
public class ProjectileType {
    /** Applied to types that spawn an entity in place of an ammo object... useful for spawn eggs or minecart item */
    public static final ProjectileType TYPE_ENTITY = new ProjectileType(new ResourceLocation("icbmclassic","entity"), null);
    /** Applied to types that act as holders for other objects or entities... think landing rockets or parachutes */
    public static final ProjectileType TYPE_HOLDER = new ProjectileType(new ResourceLocation("icbmclassic","holder"), null);

    /** Default type, anything that is projectile ammo like... this includes snowballs and arrows */
    public static final ProjectileType TYPE_PROJECTILE = new ProjectileType(new ResourceLocation("icbmclassic","projectile"), null);
    /** Missiles that use the capability {@link icbm.classic.api.missiles.IMissile} */
    public static final ProjectileType TYPE_MISSILE = new ProjectileType(new ResourceLocation("icbmclassic","missile"), TYPE_PROJECTILE);
    /** Projectiles that are explosive and use the capability {@link icbm.classic.api.caps.IExplosive} */
    public static final ProjectileType TYPE_BOMB = new ProjectileType(new ResourceLocation("icbmclassic","bomb"), TYPE_PROJECTILE);

    private final ResourceLocation ID;
    private final ProjectileType parent;

    public boolean isValidType(ProjectileType type) {
        if(type == this) {
            return true;
        }
        return parent != null && parent.isValidType(type);
    }
}
