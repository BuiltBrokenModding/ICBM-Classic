package icbm.classic.content.blocks.explosive;

import icbm.classic.ICBMClassic;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.actions.data.ActionFields;
import icbm.classic.api.actions.cause.IActionSource;
import icbm.classic.api.refs.ICBMExplosives;
import icbm.classic.api.reg.IExplosiveCustomization;
import icbm.classic.api.reg.IExplosiveData;
import icbm.classic.api.tile.IRotatable;
import icbm.classic.content.entity.EntityExplosive;
import icbm.classic.content.missile.logic.source.ActionSource;
import icbm.classic.content.reg.BlockReg;
import icbm.classic.content.reg.ComponentReg;
import icbm.classic.content.reg.ItemReg;
import icbm.classic.lib.actions.fields.ActionFieldProvider;
import icbm.classic.lib.capability.ex.CapabilityExplosiveStack;
import icbm.classic.lib.capability.holder.ICapabilityHolderTile;
import icbm.classic.lib.transform.vector.Pos;
import icbm.classic.prefab.tile.BlockICBM;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;

import javax.annotation.Nullable;

/**
 * Explosive block entity, holds the explosive stack used to place the block.
 * <p>
 * PORT: the 1.12 explosive blockstate property is gone. The explosive type lives here and is exposed to the
 * client model through {@link #EXPLOSIVE_PROPERTY} model data, and to loot/pick block through the
 * {@link ComponentReg#EXPLOSIVE} implicit component.
 */
public class TileEntityExplosive extends BlockEntity implements IRotatable, ICapabilityHolderTile
{
    public static final String NBT_EXPLOSIVE_STACK = "explosive_stack";

    /** Model data key holding the explosive type of the block, read by the client explosive block model */
    public static final ModelProperty<IExplosiveData> EXPLOSIVE_PROPERTY = new ModelProperty<>();

    /**
     * Is the tile currently exploding
     */
    public boolean hasBeenTriggered = false;

    public CapabilityExplosiveStack capabilityExplosive = createCapability(null);

    public TileEntityExplosive(BlockPos pos, BlockState state)
    {
        super(BlockReg.TILE_EXPLOSIVE.get(), pos, state);
    }

    /**
     * Creates the explosive capability backed by the given stack.
     * <p>
     * PORT: customizations (ender GPS target) change the held stack in place. 1.21 only saves chunks marked dirty
     * (1.12 always saved on unload), so adding one marks this tile changed.
     */
    protected CapabilityExplosiveStack createCapability(@Nullable ItemStack stack)
    {
        return new CapabilityExplosiveStack(stack)
        {
            @Override
            public void addCustomization(IExplosiveCustomization customization)
            {
                super.addCustomization(customization);
                TileEntityExplosive.this.setChanged();
            }
        };
    }

    /**
     * Sets the explosive stack, used on placement
     *
     * @param stack to copy
     */
    public void setExplosiveStack(ItemStack stack)
    {
        this.capabilityExplosive = createCapability(stack.copy());
        onExplosiveChanged();
    }

    @Nullable
    public IExplosiveData getExplosiveData()
    {
        return capabilityExplosive != null ? capabilityExplosive.getExplosiveData() : null;
    }

    protected void onExplosiveChanged()
    {
        setChanged();
        requestModelDataUpdate();
        if (level != null && !level.isClientSide())
        {
            level.invalidateCapabilities(getBlockPos());
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    public ModelData getModelData()
    {
        final IExplosiveData data = getExplosiveData();
        if (data == null)
        {
            return ModelData.EMPTY;
        }
        return ModelData.builder().with(EXPLOSIVE_PROPERTY, data).build();
    }

    /**
     * Reads a tile entity from NBT.
     */
    @Override
    protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries)
    {
        super.loadAdditional(nbt, registries);
        if (nbt.contains(NBT_EXPLOSIVE_STACK, Tag.TAG_COMPOUND))
        {
            final CompoundTag itemStackTag = nbt.getCompound(NBT_EXPLOSIVE_STACK);
            capabilityExplosive = createCapability(ItemStack.parseOptional(registries, itemStackTag));
            requestModelDataUpdate(); // PORT-CHECK: confirm the chunk section re-renders when model data changes after a client sync
        }
    }

    /**
     * Writes a tile entity to NBT.
     */
    @Override
    protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries)
    {
        if (capabilityExplosive != null)
        {
            final ItemStack stack = capabilityExplosive.toStack();
            if (stack != null && !stack.isEmpty())
            {
                nbt.put(NBT_EXPLOSIVE_STACK, stack.save(registries));
            }
        }
        super.saveAdditional(nbt, registries);
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components)
    {
        super.collectImplicitComponents(components);
        final IExplosiveData data = getExplosiveData();
        if (data != null)
        {
            components.set(ComponentReg.EXPLOSIVE.get(), data.getRegistryKey());
        }
    }

    @Override
    protected void applyImplicitComponents(BlockEntity.DataComponentInput componentInput)
    {
        super.applyImplicitComponents(componentInput);
        final ResourceLocation explosiveId = componentInput.get(ComponentReg.EXPLOSIVE.get());
        if (explosiveId != null && (getExplosiveData() == null || !explosiveId.equals(getExplosiveData().getRegistryKey())))
        {
            final ItemStack stack = new ItemStack(ItemReg.itemBlockExplosive.get());
            stack.set(ComponentReg.EXPLOSIVE.get(), explosiveId);
            this.capabilityExplosive = createCapability(stack);
            requestModelDataUpdate();
        }
    }

    @Override
    @Nullable
    public <T> T getCapability(BlockCapability<T, Direction> capability, @Nullable Direction facing)
    {
        if (capability == ICBMClassicAPI.EXPLOSIVE_BLOCK_CAPABILITY)
        {
            return (T) capabilityExplosive;
        }
        return null;
    }

    public void trigger(boolean setFire)
    {
        if (!hasBeenTriggered)
        {
            hasBeenTriggered = true;

            // PORT: 1.12 block state always carried an explosive (default meta 0). Blocks placed without an item
            // (/setblock, /fill, structures) have no stack here, fall back to the default explosive item (id 0)
            if (getExplosiveData() == null)
            {
                capabilityExplosive = createCapability(new ItemStack(ItemReg.itemBlockExplosive.get()));
            }

            final BlockPos pos = getBlockPos();

            // TODO handle this better in 1.13+ as this is a temp work around for direction being funky for breach
            if(capabilityExplosive.getExplosiveData() == ICBMExplosives.BREACHING) {
                final IActionSource source = new ActionSource(level, new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5), null);
                final Direction direction = this.getDirection().getOpposite();
                capabilityExplosive.getExplosiveData()
                    .create(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, source, new ActionFieldProvider().field(ActionFields.HOST_DIRECTION, () -> direction))
                    .doAction();
            }
            else {

                final EntityExplosive entityExplosive = new EntityExplosive(level, new Pos(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5), getDirection(), capabilityExplosive.toStack());
                //TODO check for tick rate, trigger directly if tick is less than 3

                if (setFire) {
                    entityExplosive.igniteForSeconds(100);
                }

                level.addFreshEntity(entityExplosive);
            }
            level.removeBlock(pos, false);

            final IExplosiveData data = capabilityExplosive.getExplosiveData();
            ICBMClassic.logger().info("TileEntityExplosive: Triggered ITEM{" + capabilityExplosive.toStack() + "] " + (data != null ? data.getRegistryKey() : "none") + " at location " + getBlockPos());
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket()
    {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries)
    {
        return saveWithoutMetadata(registries);
    }

    @Override
    public Direction getDirection()
    {
        final BlockState state = getBlockState();
        if (state.hasProperty(BlockICBM.ROTATION_PROP))
        {
            return state.getValue(BlockICBM.ROTATION_PROP);
        }
        return Direction.NORTH;
    }

    @Override
    public void setDirection(Direction facingDirection)
    {
        BlockState state = level.getBlockState(getBlockPos());
        state = state.setValue(BlockICBM.ROTATION_PROP, facingDirection);
        this.level.setBlock(getBlockPos(), state, 2);
    }
}
// PORTED
