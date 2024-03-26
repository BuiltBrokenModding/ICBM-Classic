package icbm.classic.world.entity;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.reg.ExplosiveType;
import icbm.classic.core.registries.IcbmBuiltinRegistries;
import icbm.classic.lib.explosive.ExplosiveHandler;
import icbm.classic.prefab.tile.IcbmBlock;
import icbm.classic.world.IcbmBlocks;
import icbm.classic.world.IcbmItems;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.MinecartTNT;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;

public class BombCartEntity extends MinecartTNT {

    @Getter
    @Setter
    private ExplosiveType explosive;

    @Getter
    @Setter
    private CompoundTag data;

    public BombCartEntity(EntityType<? extends MinecartTNT> type, Level level) {
        super(type, level);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);

        CompoundTag explosiveTag = new CompoundTag();
        if (this.explosive != null) {
            explosiveTag.putString("Name", IcbmBuiltinRegistries.EXPLOSIVES.getKey(this.explosive).toString());
        }
        if (this.data != null) {
            explosiveTag.put("Data", this.data);
        }

        if (!explosiveTag.isEmpty()) {
            tag.put("Explosive", explosiveTag);
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);

        if (tag.contains("Explosive", Tag.TAG_COMPOUND)) {
            CompoundTag explosiveTag = tag.getCompound("Explosive");
            if (tag.contains("Name", Tag.TAG_STRING)) {
                this.explosive = IcbmBuiltinRegistries.EXPLOSIVES.get(new ResourceLocation(explosiveTag.getString("Name")));
            }
            if (tag.contains("Data", Tag.TAG_COMPOUND)) {
                this.data = explosiveTag.getCompound("Data");
            }
        }
    }

    @Override
    protected void explode(double radiusModifier) {
        this.level().addParticle(ParticleTypes.EXPLOSION, this.getX(), this.getY(), this.getZ(), 0.0D, 0.0D, 0.0D);
        ExplosiveHandler.createExplosion(this, level(), this.getX(), this.getY(), this.getZ(), explosive, 1, data);
        this.remove(RemovalReason.KILLED);
    }

    @Override
    public void handleDamageEvent(DamageSource pDamageSource) {
        super.handleDamageEvent(pDamageSource);
    }

    @Override
    public void tick() {
        super.tick();
        if (isPrimed()) {
            ICBMClassicAPI.EX_MINECART_REGISTRY.tickFuse(this, explosive, getFuse());
        }
    }

    @Override
    public void primeFuse() {
        super.primeFuse();
        this.setFuse(ICBMClassicAPI.EX_MINECART_REGISTRY.getFuseTime(this, explosive));
    }

    @Override
    protected Item getDropItem() {
        // FIXME: We need separate items for each explosion type
        return IcbmItems.BOMB_CART.get();
    }

    @Override
    public @NotNull BlockState getDefaultDisplayBlockState() {
        return IcbmBlocks.EXPLOSIVES.get().defaultBlockState()
            .setValue(IcbmBlock.ROTATION_PROP, Direction.UP);
    }

    private void setFuse(int fuse) {
        // Unfortunately the fuse field is private, so we need to use reflection to set it
        try {
            Field field = MinecartTNT.class.getDeclaredField("fuse");
            field.setAccessible(true);
            field.setInt(this, fuse);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
