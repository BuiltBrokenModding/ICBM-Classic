package icbm.classic.content.missile.source;

import icbm.classic.lib.saving.NbtSaveHandler;
import icbm.classic.lib.saving.NbtSaveNode;
import lombok.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

import java.util.Optional;
import java.util.UUID;

@Data
@NoArgsConstructor
public class EntitySourceData {

    /** Parent of the entity, Ex: Missile -> Cluster missile -> Player  */
    private EntitySourceData parent;
    private String name;
    private UUID id;
    private boolean isPlayer;

    public EntitySourceData(Entity source) {
        name = source.getName();
        id = source.getUniqueID();
        isPlayer = source instanceof EntityPlayer;
    }

    /**
     * Gets the first entity source in the chain
     * @return this if first, or parent in a chain
     */
    public EntitySourceData getFirstSource() {
        if(parent != null) { //TODO add logic to prevent infinite loop
            return parent.getFirstSource();
        }
        return this;
    }

    public NBTTagCompound save() {
        return SAVE_LOGIC.save(this, new NBTTagCompound());
    }

    public void load(NBTTagCompound save) {
        SAVE_LOGIC.load(this, save);
    }


    private static final NbtSaveHandler<EntitySourceData> SAVE_LOGIC = new NbtSaveHandler<EntitySourceData>()
        .mainRoot()
        /* */.nodeString("name", EntitySourceData::getName, EntitySourceData::setName)
        /* */.nodeUUID("uuid", EntitySourceData::getId, EntitySourceData::setId)
        /* */.nodeBoolean("player", EntitySourceData::isPlayer, EntitySourceData::setPlayer)
        /* */.node(new NbtSaveNode<EntitySourceData, NBTTagCompound>("parent",
            (source) -> Optional.ofNullable(source.getParent()).map(EntitySourceData::save).orElse(null),
            (source, data) -> {
                source.setParent(new EntitySourceData());
                source.getParent().load(data);
            }
        ))
        .base();
}
