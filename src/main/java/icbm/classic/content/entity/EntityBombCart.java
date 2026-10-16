package icbm.classic.content.entity;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.content.missile.logic.source.ActionSource;
import icbm.classic.content.missile.logic.source.cause.EntityCause;
import icbm.classic.content.reg.BlockReg;
import icbm.classic.content.reg.EntityReg;
import icbm.classic.content.reg.ItemReg;
import icbm.classic.lib.capability.ex.CapabilityExplosiveEntity;
import icbm.classic.prefab.tile.BlockICBM;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.MinecartTNT;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.entity.IEntityWithComplexSpawn;

import javax.annotation.Nullable;

/**
 * Minecart carrying an ICBM explosive.
 * <p>
 * PORT: 1.21 {@link MinecartTNT} keeps its fuse private and fixed at 80 ticks. The ICBM fuse (from the minecart
 * content registry) is tracked in {@link #bombFuse}; the vanilla fuse stays unprimed.
 */
public class EntityBombCart extends MinecartTNT implements IEntityWithComplexSpawn
{
    private static final String NBT_FUSE = "bombFuse";

    public final CapabilityExplosiveEntity explosive = new CapabilityExplosiveEntity(this);

    /** ICBM fuse, -1 when not ignited (replaces 1.12 minecartTNTFuse) */
    protected int bombFuse = -1;

    public EntityBombCart(EntityType<? extends EntityBombCart> type, Level level)
    {
        super(type, level);
    }

    public EntityBombCart(Level level)
    {
        this(EntityReg.BOMB_CART.get(), level);
    }

    public EntityBombCart(Level level, double x, double y, double z, ItemStack itemStack) //TODO change to pass in itemstack for capability
    {
        this(level);
        this.setPos(x, y, z);
        this.xo = x;
        this.yo = y;
        this.zo = z;
        this.explosive.setStack(itemStack);
    }

    @Override
    public void writeSpawnData(RegistryFriendlyByteBuf data)
    {
        ItemStack.OPTIONAL_STREAM_CODEC.encode(data, explosive.toStack());
    }

    @Override
    public void readSpawnData(RegistryFriendlyByteBuf data)
    {
        final ItemStack stack = ItemStack.OPTIONAL_STREAM_CODEC.decode(data);
        if (!stack.isEmpty())
        {
            explosive.setStack(stack);
        }
    }

    /** 1.12 explodeCart */
    @Override
    protected void explode(@Nullable DamageSource damageSource, double radiusModifier) // PORT-CHECK: MinecartTNT#explode(DamageSource, double) in 1.21.1
    {
        if (!level().isClientSide())
        {
            explosive.doExplosion(this.getX(), this.getY(), this.getZ(), new ActionSource(level(), new Vec3(getX(), getY(), getZ()), new EntityCause(this))); //TODO handle output and include trigger source & player of the cart
        }
        this.discard();
    }

    /** Vanilla destroy hook, keeps the 1.12 killMinecart logic */
    @Override
    public void destroy(DamageSource source) // PORT-CHECK: VehicleEntity/MinecartTNT#destroy(DamageSource) in 1.21.1
    {
        killMinecart(source);
    }

    public void killMinecart(DamageSource damageSource)
    {
        if (!level().isClientSide())
        {
            this.discard();
            final Vec3 motion = this.getDeltaMovement();
            final double d0 = motion.x * motion.x + motion.z * motion.z;

            final boolean isExplosion = damageSource.is(DamageTypeTags.IS_EXPLOSION);
            if (!isExplosion && level().getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS))
            {
                final ItemStack drop = getPickResult();
                if (hasCustomName())
                {
                    drop.set(DataComponents.CUSTOM_NAME, getCustomName());
                }
                this.spawnAtLocation(drop, 0.0F);
            }

            if (damageSource.is(DamageTypeTags.IS_FIRE) || isExplosion || d0 >= 0.009999999776482582D)
            {
                this.explode(damageSource, d0);
            }
        }
    }

    @Override
    public void tick()
    {
        super.tick();
        if (isPrimed())
        {
            ICBMClassicAPI.EX_MINECART_REGISTRY.tickFuse(this, explosive.getExplosiveData(), bombFuse);

            if (bombFuse > 0)
            {
                --bombFuse;
                level().addParticle(ParticleTypes.SMOKE, this.getX(), this.getY() + 0.5D, this.getZ(), 0.0D, 0.0D, 0.0D);
            }
            else if (bombFuse == 0 && !isRemoved())
            {
                this.explode(null, this.getDeltaMovement().horizontalDistanceSqr());
            }
        }
    }

    /** 1.12 ignite */
    @Override
    public void primeFuse()
    {
        this.bombFuse = ICBMClassicAPI.EX_MINECART_REGISTRY.getFuseTime(this, explosive.getExplosiveData());

        if (!this.level().isClientSide())
        {
            this.level().broadcastEntityEvent(this, (byte) 10);

            if (!this.isSilent())
            {
                this.level().playSound((Player) null, this.getX(), this.getY(), this.getZ(), SoundEvents.TNT_PRIMED, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
        }
    }

    public void ignite()
    {
        primeFuse();
    }

    /** Vanilla checks its own (unused) fuse here, which would re-prime the cart every tick */
    @Override
    public void activateMinecart(int x, int y, int z, boolean receivingPower)
    {
        if (receivingPower && this.bombFuse < 0)
        {
            this.primeFuse();
        }
    }

    @Override
    public void handleEntityEvent(byte id)
    {
        if (id == 10)
        {
            // Client side copy of the fuse for rendering
            this.bombFuse = ICBMClassicAPI.EX_MINECART_REGISTRY.getFuseTime(this, explosive.getExplosiveData());
        }
        else
        {
            super.handleEntityEvent(id);
        }
    }

    @Override
    public boolean isPrimed()
    {
        return bombFuse > -1;
    }

    /** @return ICBM fuse ticks left, -1 if not primed */
    public int getBombFuse()
    {
        return bombFuse;
    }

    @Override
    protected Item getDropItem()
    {
        return ItemReg.itemBombCart.get();
    }

    @Override
    public ItemStack getPickResult()
    {
        return getCartItem();
    }

    public ItemStack getCartItem()
    {
        return explosive.toStack().copy();
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag nbt)
    {
        super.addAdditionalSaveData(nbt);
        nbt.put("explosive", explosive.toStack().saveOptional(registryAccess()));
        nbt.putInt(NBT_FUSE, bombFuse);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag nbt)
    {
        super.readAdditionalSaveData(nbt);

        // PORT: 1.12 legacy int "explosive" (metadata) is not migrated
        final ItemStack stack = ItemStack.parseOptional(registryAccess(), nbt.getCompound("explosive"));
        if (!stack.isEmpty())
        {
            explosive.setStack(stack);
        }
        if (nbt.contains(NBT_FUSE))
        {
            bombFuse = nbt.getInt(NBT_FUSE);
        }
    }

    @Override
    public BlockState getDefaultDisplayBlockState()
    {
        // PORT: the explosive type is no longer a block state property, RenderBombCart picks the model per explosive
        final BlockState state = BlockReg.blockExplosive.get().defaultBlockState();
        return state.hasProperty(BlockICBM.ROTATION_PROP) ? state.setValue(BlockICBM.ROTATION_PROP, Direction.UP) : state;
    }
}
// PORTED
