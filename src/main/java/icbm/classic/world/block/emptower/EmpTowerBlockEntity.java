package icbm.classic.world.block.emptower;

import icbm.classic.ICBMClassic;
import icbm.classic.IcbmConstants;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.explosion.BlastState;
import icbm.classic.api.explosion.IBlast;
import icbm.classic.api.refs.ICBMExplosives;
import icbm.classic.client.ICBMSounds;
import icbm.classic.config.ConfigMain;
import icbm.classic.config.machines.ConfigEmpTower;
import icbm.classic.lib.data.IMachineInfo;
import icbm.classic.lib.energy.storage.EnergyBuffer;
import icbm.classic.lib.energy.system.EnergySystem;
import icbm.classic.lib.network.lambda.PacketCodex;
import icbm.classic.lib.network.lambda.PacketCodexReg;
import icbm.classic.lib.network.lambda.tile.PacketCodexTile;
import icbm.classic.lib.radio.RadioRegistry;
import icbm.classic.lib.saving.NbtSaveHandler;
import icbm.classic.lib.tile.TickAction;
import icbm.classic.lib.tile.TickDoOnce;
import icbm.classic.prefab.gui.IPlayerUsing;
import icbm.classic.prefab.inventory.InventorySlot;
import icbm.classic.prefab.inventory.InventoryWithSlots;
import icbm.classic.prefab.tile.IGuiTile;
import icbm.classic.prefab.tile.IcbmBlockEntity;
import icbm.classic.world.IcbmBlockEntityTypes;
import icbm.classic.world.blast.BlastEMP;
import icbm.classic.world.block.emptower.gui.ContainerEMPTower;
import icbm.classic.world.block.emptower.gui.GuiEMPTower;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.common.capabilities.Capability;
import net.neoforged.energy.CapabilityEnergy;
import net.neoforged.fml.common.registry.GameRegistry;
import net.neoforged.items.CapabilityItemHandler;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * Logic side of the EMP tower block
 */
public class EmpTowerBlockEntity extends IcbmBlockEntity implements IGuiTile, IMachineInfo, IPlayerUsing {
    public static final ResourceLocation REGISTRY_NAME = new ResourceLocation(IcbmConstants.MOD_ID, "emptower");

    public static final int ROTATION_SPEED = 15;

    /**
     * Tick synced rotation
     */
    public float rotation = 0;

    /**
     * Client side use in render
     */
    public float prevRotation = 0;

    /**
     * Delay before EMP can be fired again
     */
    protected int cooldownTicks = 0;

    /**
     * Radius of the EMP tower
     */
    @Getter
    public int range = 60;

    public final EnergyBuffer energyStorage = new EnergyBuffer(() -> this.getFiringCost() + (this.getTickingCost() * ConfigEmpTower.ENERGY_COST_TICKING_CAP))
        .withOnChange((p, c, s) -> {
            this.markDirty();
        })
        .withCanReceive(() -> this.getCooldown() <= 0)
        .withCanExtract(() -> false)
        .withReceiveLimit(() -> ConfigEmpTower.ENERGY_INPUT);

    public final InventoryWithSlots inventory = new InventoryWithSlots(1)
        .withChangeCallback((s, i) -> markDirty())
        .withSlot(new InventorySlot(0, EnergySystem::isEnergyItem).withTick(this.energyStorage::dischargeItem));

    public final RadioEmpTower radioCap = new RadioEmpTower(this);

    private final List<TileEmpTowerFake> subBlocks = new ArrayList<>();

    private final TickDoOnce descriptionPacketSender = new TickDoOnce((t) -> PACKET_DESCRIPTION.sendToAllAround(this));

    @Getter
    private final List<Player> playersUsing = new LinkedList<>();

    public EmpTowerBlockEntity(BlockPos pos, BlockState state) {
        super(IcbmBlockEntityTypes.EMP_TOWER.get(), pos, state);
        tickActions.add(descriptionPacketSender);
        tickActions.add(new TickAction(3, true, (t) -> PACKET_GUI.sendPacketToGuiUsers(this, playersUsing)));
        tickActions.add(new TickAction(20, true, (t) -> {
            playersUsing.removeIf((player) -> !(player.containerMenu instanceof ContainerEMPTower));
        }));
        tickActions.add(inventory);
        tickActions.add(new TickAction(5, (t) -> updateStructure()));
    }

    @Override
    public void markDirty() {
        super.markDirty();
        if (isServer()) {
            descriptionPacketSender.doNext();
        }
    }


    @Override
    public void provideInformation(BiConsumer<String, Object> consumer) {
        consumer.accept(NEEDS_POWER, ConfigMain.REQUIRES_POWER); //TODO create constant file and helpers for common keys
        consumer.accept(ENERGY_COST_TICK, getTickingCost()); //TODO implement a per tick upkeep
        consumer.accept(ENERGY_COST_ACTION, getFiringCost());
        consumer.accept(ENERGY_RECEIVE_LIMIT, ConfigEmpTower.ENERGY_INPUT);
        consumer.accept("COOLING_TICKS", getMaxCooldown());
        consumer.accept("MAX_RANGE", getMaxRadius());
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (isServer()) {
            RadioRegistry.add(radioCap);
        }
    }

    @Override
    public void invalidate() {
        super.invalidate();
        subBlocks.forEach(tile -> tile.setHost(null));
        subBlocks.clear();
        if (isServer()) {
            RadioRegistry.remove(radioCap);
        }
    }

    protected void updateStructure() {
        //Find tower blocks TODO find a better solution
        subBlocks.clear();
        BlockPos above = getPos().up();
        while (world.getBlockState(above).getBlock() == getBlockType()) {
            final BlockEntity blockEntity = world.getBlockEntity(above);
            if (tile instanceof TileEmpTowerFake) {
                ((TileEmpTowerFake) tile).setHost(this);
                subBlocks.add((TileEmpTowerFake) tile);
            }
            above = above.up();
        }
    }

    @Override
    public void update() {
        super.update();

        if (isServer()) {
            // Eat power
            energyStorage.consumePower(getTickingCost(), false);

            if (ticks % 20 == 0 && isReady()) //TODO convert to a mix of a timer and/or event handler
            {
                ICBMSounds.MACHINE_HUM.play(world, getPos().getX() + 0.5, getPos().getY() + 0.5, getPos().getZ() + 0.5, 0.5F, 0.85F * getChargePercentage(), true);
            } else if (getCooldown() > 0 && ticks % 10 == world.rand.nextInt(10)) {
                //TODO add custom sound so sub-titles match
                world.playSound(null, getPos(), SoundEvents.BLOCK_LAVA_EXTINGUISH, SoundCategory.BLOCKS, 0.5F, world.rand.nextFloat() * 0.15F + 0.6F);
            }

            if (isReady() && world.getStrongPower(getPos()) > 0) //TODO convert to a state handler
            {
                fire();
            }
        }

        prevRotation = rotation;

        if (cooldownTicks > 0) {
            cooldownTicks--;

            if (ticks % 5 == 0) {

                // Spawn particles, do 1 for each height
                for (int i = 0; i <= subBlocks.size(); i++) {

                    // Randomly select one of the 4 sides
                    float rotation = this.rotation;
                    int side = world.rand.nextInt(4);
                    rotation += side * 90f;
                    spawnParticles(rotation + 45, i); // offset 45 for model being rotated
                }
            }
        } else {
            rotation += getChargePercentage() * ROTATION_SPEED;

            clamp(rotation);

            while (this.rotation - this.prevRotation < -180.0F) {
                this.prevRotation -= 360.0F;
            }

            while (this.rotation - this.prevRotation >= 180.0F) {
                this.prevRotation += 360.0F;
            }
        }
    }

    private void spawnParticles(float rotation, int yOffset) {
        final float faceWidth = 7.0F / 16.0F;
        final float faceHeight = 9.0F / 16.0F;
        final float faceYOffset = 5.0F / 16.0F;
        final float faceWOffset = 3.5F / 16.0F;

        double faceA = faceWidth * world.rand.nextFloat() - (faceWidth / 2);
        double faceB = faceHeight * world.rand.nextFloat();

        double rad = Math.toRadians(clamp(rotation));
        double rad2 = Math.toRadians(clamp(rotation + 90));
        double vecX = Math.sin(rad) * faceWOffset;
        double vecZ = Math.cos(rad) * faceWOffset;
        double faceX = Math.sin(rad2) * faceA;
        double faceZ = Math.cos(rad2) * faceA;

        double x = pos.getX() + 0.5;
        double y = pos.getY() + yOffset + faceYOffset - 0.2f;
        double z = pos.getZ() + 0.5;

        double d0 = x + vecX + faceX;
        double d1 = y + faceYOffset + faceB;
        double d2 = z + vecZ + faceZ;
        world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, d0, d1, d2, 0.0D, 0.0D, 0.0D);
    }

    private float clamp(float rotation) {
        while (rotation > 180.0F) {
            rotation -= 360F;
        }
        return rotation;
    }

    public float getChargePercentage() {
        return Math.max(0, Math.min(1, energyStorage.getEnergyStored() / (float) getFiringCost()));
    }

    public int getFiringCost() {
        return range * range * ConfigEmpTower.ENERGY_COST_AREA;// TODO change this to scale exponentially by area to discourage large area EMPs
    }

    public int getTickingCost() {
        return range * ConfigEmpTower.ENERGY_COST_TICKING;
    }

    public int getMaxRadius() {
        return ConfigEmpTower.MAX_BASE_RANGE + (subBlocks.size() * ConfigEmpTower.BONUS_RADIUS);
    }

    public void setRange(int range) {
        this.range = Math.min(range, getMaxRadius());
    }

    protected IBlast buildBlast() {
        return ((BlastEMP) ICBMExplosives.EMP.create()
            .setBlastLevel(world)
            .setBlastPosition(getPos().getX() + 0.5, getPos().getY() + 1.2, getPos().getZ() + 0.5)
            .setBlastSize(range))
            .clearSetEffectBlocksAndEntities()
            .setEffectBlocks().setEffectEntities()
            .buildBlast();
    }

    //@Callback(limit = 1) TODO add CC support
    public boolean fire() {
        if (this.isReady()) {
            //Finish and trigger
            if (buildBlast().runBlast().state == BlastState.TRIGGERED) {
                //Consume energy
                this.energyStorage.consumePower(getFiringCost(), false);

                //Reset timer
                this.cooldownTicks = getMaxCooldown();

                return true;
            } else {
                ICBMClassic.logger().warn("TileEmpTower( DIM: " + world.provider.getDimension() + ", " + getPos() + ") EMP did not trigger, likely was blocked.");
                //TODO display some info to player to explain why blast failed and more detailed debug
            }
        }
        return false;
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return INFINITE_EXTENT_AABB;
    }

    //@Callback TODO add CC support
    public boolean isReady() {
        return getCooldown() <= 0 && this.energyStorage.consumePower(getFiringCost(), true);
    }

    //@Callback TODO add CC support
    public int getCooldown() {
        return cooldownTicks;
    }

    public float getCooldownPercentage() {
        return 1f - (cooldownTicks / (float) getMaxCooldown());
    }

    //@Callback TODO add CC support
    public int getMaxCooldown() {
        return ConfigEmpTower.COOLDOWN; //TODO add to config
    }

    @Override
    public Object getServerGuiElement(int ID, Player player) {
        return new ContainerEMPTower(player, this);
    }

    @Override
    public Object getClientGuiElement(int ID, Player player) {
        return new GuiEMPTower(player, this);
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable Direction facing) {
        return super.hasCapability(capability, facing)
            || capability == CapabilityEnergy.ENERGY && ConfigMain.REQUIRES_POWER
            || capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY
            || capability == ICBMClassicAPI.RADIO_CAPABILITY;
    }

    @Override
    @Nullable
    public <T> T getCapability(Capability<T> capability, @Nullable Direction facing) {
        if (capability == CapabilityEnergy.ENERGY) {
            return (T) energyStorage;
        } else if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return (T) inventory;
        } else if (capability == ICBMClassicAPI.RADIO_CAPABILITY) {
            return (T) radioCap;
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public void readFromNBT(CompoundTag nbt) {
        super.readFromNBT(nbt);
        SAVE_LOGIC.load(this, nbt);
    }

    @Override
    public CompoundTag writeToNBT(CompoundTag nbt) {
        SAVE_LOGIC.save(this, nbt);
        return super.writeToNBT(nbt);
    }

    private static final NbtSaveHandler<EmpTowerBlockEntity> SAVE_LOGIC = new NbtSaveHandler<EmpTowerBlockEntity>()
        .mainRoot()
        /* */.nodeINBTSerializable("inventory", tile -> tile.inventory)
        /* */.nodeINBTSerializable("radio", tile -> tile.radioCap)
        /* */.nodeInteger("range", tile -> tile.range, (tile, i) -> tile.range = i)
        /* */.nodeInteger("cooldown", tile -> tile.cooldownTicks, (tile, i) -> tile.cooldownTicks = i)
        /* */.nodeInteger("energy", tile -> tile.energyStorage.getEnergyStored(), (tile, i) -> tile.energyStorage.setEnergyStored(i)) //TODO use INBTSerializable on storage instance
        /* */.nodeFloat("rotation", tile -> tile.rotation, (tile, f) -> tile.rotation = f)
        /* */.nodeFloat("prev_rotation", tile -> tile.prevRotation, (tile, f) -> tile.prevRotation = f)
        .base();


    public static void register() {
        GameRegistry.registerBlockEntity(EmpTowerBlockEntity.class, REGISTRY_NAME);
        PacketCodexReg.register(PACKET_RADIUS, PACKET_RADIO_HZ, PACKET_GUI, PACKET_FIRE, PACKET_RADIO_DISABLE, PACKET_DESCRIPTION);
    }

    public static final PacketCodex<EmpTowerBlockEntity, EmpTowerBlockEntity> PACKET_RADIUS = new PacketCodexTile<EmpTowerBlockEntity, EmpTowerBlockEntity>(REGISTRY_NAME, "radius")
        .fromClient()
        .nodeInt(EmpTowerBlockEntity::getRange, EmpTowerBlockEntity::setRange)
        .onFinished((tile, target, player) -> tile.markDirty());

    public static final PacketCodex<EmpTowerBlockEntity, RadioEmpTower> PACKET_RADIO_HZ = new PacketCodexTile<EmpTowerBlockEntity, RadioEmpTower>(REGISTRY_NAME, "radio.frequency", (tile) -> tile.radioCap)
        .fromClient()
        .nodeString(RadioEmpTower::getChannel, RadioEmpTower::setChannel)
        .onFinished((tile, target, player) -> tile.markDirty());

    public static final PacketCodex<EmpTowerBlockEntity, RadioEmpTower> PACKET_RADIO_DISABLE = new PacketCodexTile<EmpTowerBlockEntity, RadioEmpTower>(REGISTRY_NAME, "radio.disable", (tile) -> tile.radioCap)
        .fromClient()
        .toggleBoolean(RadioEmpTower::isDisabled, RadioEmpTower::setDisabled)
        .onFinished((tile, target, player) -> tile.markDirty());

    public static final PacketCodex<EmpTowerBlockEntity, EmpTowerBlockEntity> PACKET_GUI = new PacketCodexTile<EmpTowerBlockEntity, EmpTowerBlockEntity>(REGISTRY_NAME, "gui")
        .fromServer()
        .nodeInt((t) -> t.energyStorage.getEnergyStored(), (t, i) -> t.energyStorage.setEnergyStored(i))
        .nodeString((t) -> t.radioCap.getChannel(), (t, s) -> t.radioCap.setChannel(s))
        .nodeBoolean((t) -> t.radioCap.isDisabled(), (t, b) -> t.radioCap.setDisabled(b))
        .nodeInt(EmpTowerBlockEntity::getRange, EmpTowerBlockEntity::setRange);

    public static final PacketCodex<EmpTowerBlockEntity, EmpTowerBlockEntity> PACKET_FIRE = new PacketCodexTile<EmpTowerBlockEntity, EmpTowerBlockEntity>(REGISTRY_NAME, "fire")
        .fromClient()
        .onFinished((tile, target, player) -> {
            tile.fire();
            tile.markDirty();
        });

    public static final PacketCodexTile<EmpTowerBlockEntity, EmpTowerBlockEntity> PACKET_DESCRIPTION = (PacketCodexTile<EmpTowerBlockEntity, EmpTowerBlockEntity>) new PacketCodexTile<EmpTowerBlockEntity, EmpTowerBlockEntity>(REGISTRY_NAME, "description")
        .fromServer()
        .nodeFloat((t) -> t.rotation, (t, f) -> t.rotation = f)
        .nodeInt((t) -> t.cooldownTicks, (t, f) -> t.cooldownTicks = f);
}
