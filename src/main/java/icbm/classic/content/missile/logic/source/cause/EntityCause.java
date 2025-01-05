package icbm.classic.content.missile.logic.source.cause;

import icbm.classic.ICBMConstants;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.actions.cause.IActionCause;
import icbm.classic.api.actions.cause.ICausedByEntity;
import icbm.classic.api.reg.obj.IBuilderRegistry;
import icbm.classic.lib.saving.NbtSaveHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nonnull;
import java.util.UUID;

/**
 * General purpose entity cause
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class EntityCause extends ActionCause implements ICausedByEntity {

    public static final ResourceLocation REG_NAME = new ResourceLocation(ICBMConstants.DOMAIN, "entity");

    private String name;
    private UUID id;
    private boolean isPlayer;

    // runtime cache vars
    private Entity entity;

    public EntityCause(Entity source) {
        entity = source;
        name = source.getName().getFormattedText(); //TODO consider a better option
        id = source.getUniqueID();
        isPlayer = source instanceof PlayerEntity;
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
    public IBuilderRegistry<IActionCause> getRegistry() {
        return ICBMClassicAPI.ACTION_CAUSE_REGISTRY;
    }

    @Override
    public CompoundNBT serializeNBT() {
        return SAVE_LOGIC.save(this, super.serializeNBT());
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
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
