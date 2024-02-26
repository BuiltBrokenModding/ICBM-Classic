package icbm.classic.content.missile.logic.source.cause;

import icbm.classic.ICBMConstants;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.missiles.cause.IMissileCause;
import icbm.classic.api.reg.obj.IBuilderRegistry;
import icbm.classic.lib.saving.NbtSaveHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.UUID;

/**
 * General purpose entity cause
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class EntityCause extends MissileCause implements IMissileCause.IEntityCause {

    public static final ResourceLocation REG_NAME = new ResourceLocation(ICBMConstants.DOMAIN, "entity");

    private String name;
    private UUID id;
    private boolean isPlayer;

    // runtime cache vars
    private Entity entity;

    public EntityCause(Entity source) {
        entity = source;
        name = source.getName();
        id = source.getUniqueID();
        isPlayer = source instanceof EntityPlayer;
    }

    @Override
    public Entity getEntity() {
        return entity;
    }

    @Nonnull
    @Override
    public ResourceLocation getRegistryKey() {
        return REG_NAME;
    }

    @Nonnull
    @Override
    public IBuilderRegistry<IMissileCause> getRegistry() {
        return ICBMClassicAPI.MISSILE_CAUSE_REGISTRY;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        return SAVE_LOGIC.save(this, super.serializeNBT());
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        super.deserializeNBT(nbt);
        SAVE_LOGIC.load(this, nbt);
    }


    private static final NbtSaveHandler<EntityCause> SAVE_LOGIC = new NbtSaveHandler<EntityCause>()
        .mainRoot()
        /* */.nodeString("name", EntityCause::getName, EntityCause::setName)
        /* */.nodeUUID("uuid", EntityCause::getId, EntityCause::setId)
        /* */.nodeBoolean("player", EntityCause::isPlayer, EntityCause::setPlayer)
        .base();
}
