package icbm.classic.lib.projectile;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;

import java.util.UUID;


public class ProjectileOwner {


    private final String ownerName;
    private final UUID ownerId;
    private final Integer entityId;

    private Entity owner;

    public ProjectileOwner(Entity entity) {
        this.ownerName = entity.getEntityString();
        this.ownerId = entity.getUniqueID();
        this.entityId = entity.getEntityId();
    }

    public Entity getOwner(World world) {
        if(owner == null) {
            if(ownerId != null) {
                owner = world.getPlayerByUuid(ownerId);
            }
            else if(entityId != null) {
                owner = world.getEntityByID(entityId);
            }
        }
        return owner;
    }
}
