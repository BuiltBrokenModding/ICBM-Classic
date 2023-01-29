package icbm.classic.content.missile.source;

import icbm.classic.api.missiles.IMissileSource;
import icbm.classic.lib.saving.NbtSaveHandler;
import icbm.classic.lib.saving.NbtSaveNode;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.util.Optional;
import java.util.UUID;

@Data
@NoArgsConstructor
public abstract class MissileSource implements IMissileSource {

    private World world;
    private Entity firingEntity; //TODO use references so entity can GC
    private Entity actualEntity;
    private EntitySourceData entitySourceData; //TODO add to API when finished

    public MissileSource(World world, EntitySourceData entitySourceData) {
        this.world = world;
        this.entitySourceData = entitySourceData;
    }

    public void setEntitySourceData(EntitySourceData entitySourceData) {
        firingEntity = null;
        actualEntity = null;
        this.entitySourceData = entitySourceData;
    }

    @Override
    public Entity getFiringEntity(boolean actual)    {
        if(getEntitySourceData() != null) {
            if(actual) {
                if(actualEntity == null) {
                    actualEntity = Optional.ofNullable(getEntitySourceData().getFirstSource()).map(s -> findEntity(getWorld(), s)).orElse(null);
                }
                return actualEntity;
            }
            else if (firingEntity == null) {
                firingEntity = Optional.ofNullable(getEntitySourceData()).map(s -> findEntity(getWorld(), s)).orElse(null);
            }
            return firingEntity;
        }
        return null;
    }

    private static Entity findEntity(World world, EntitySourceData source) {
        if(source.getId() != null) {
            final UUID id = source.getId();

            // Get player by ID
            if (source.isPlayer()) {
                if (!world.isRemote) {
                    final MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
                    return server.getPlayerList().getPlayers().stream().filter(player -> player.getUniqueID() == id).findFirst().orElse(null);
                } else {
                    return world.getPlayerEntityByUUID(id);
                }
            }
            else {
                // TODO implement chunk load/unload events, implement scanning all worlds, implement entity movement events to keep track of source
                return world.getEntities(Entity.class, (entity) -> entity.getUniqueID() == id).stream().findFirst().orElse(null);
            }
        }
        return null;
    }

    @Override
    public NBTTagCompound save() {
        return SAVE_LOGIC.save(this, new NBTTagCompound());
    }

    @Override
    public void load(NBTTagCompound save) {
        SAVE_LOGIC.load(this, save);
    }


    private static final NbtSaveHandler<MissileSource> SAVE_LOGIC = new NbtSaveHandler<MissileSource>()
        .mainRoot()
        /* */.nodeWorldDim("dimension", MissileSource::getWorld, MissileSource::setWorld)
        /* */.node(new NbtSaveNode<MissileSource, NBTTagCompound>("entity",
            (missileSource) -> Optional.ofNullable(missileSource.getEntitySourceData()).map(EntitySourceData::save).orElse(null),
            (missileSource, data) -> {
                missileSource.setEntitySourceData(new EntitySourceData());
                missileSource.getEntitySourceData().load(data);
            }
        ))
        .base();
}
