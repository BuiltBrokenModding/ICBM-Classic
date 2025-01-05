package icbm.classic.content.blocks.emptower;

import icbm.classic.ICBMClassic;
import icbm.classic.ICBMConstants;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.actions.cause.IActionCause;
import icbm.classic.api.actions.data.ActionFields;
import icbm.classic.api.actions.status.ActionStatusTypes;
import icbm.classic.api.actions.status.IActionStatus;
import icbm.classic.client.ICBMSounds;
import icbm.classic.config.ConfigMain;
import icbm.classic.config.machines.ConfigEmpTower;
import icbm.classic.content.actions.emp.ActionDataEmpArea;
import icbm.classic.content.blocks.emptower.gui.ContainerEMPTower;
import icbm.classic.content.blocks.emptower.gui.GuiEMPTower;
import icbm.classic.content.reg.BlockReg;
import icbm.classic.content.reg.TileReg;
import icbm.classic.lib.actions.PotentialActionKnown;
import icbm.classic.lib.actions.fields.ActionFieldProvider;
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
import icbm.classic.prefab.tile.TileMachine;
import lombok.Getter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.items.CapabilityItemHandler;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiConsumer;

/** Logic side of the EMP tower block */
public class TileEMPTower extends TileMachine implements IGuiTile, IMachineInfo, IPlayerUsing
{
    public static final ResourceLocation REGISTRY_NAME = new ResourceLocation(ICBMConstants.DOMAIN, "emp_tower_base");

    public static final int ROTATION_SPEED = 15;

    /** Tick synced rotation */
    public float rotation = 0;

    /** Client side use in render */
    public float prevRotation = 0;

    /** Delay before EMP can be fired again */
    protected int cooldownTicks = 0;

    /** Radius of the EMP tower */
    @Getter
    public int range = 60;

    public final EnergyBuffer energyStorage = new EnergyBuffer(() -> this.getFiringCost() + (this.getTickingCost() * ConfigEmpTower.ENERGY_COST_TICKING_CAP))
        .withOnChange((p,c,s) -> {this.markDirty();})
        .withCanReceive(() -> this.getCooldown() <= 0)
        .withCanExtract(() -> false)
        .withReceiveLimit(() -> ConfigEmpTower.ENERGY_INPUT);

    public final InventoryWithSlots inventory = new InventoryWithSlots(1)
        .withChangeCallback((s, i) -> markDirty())
        .withSlot(new InventorySlot(0, EnergySystem::isEnergyItem).withTick(this.energyStorage::dischargeItem));

    public final RadioEmpTower radioCap = new RadioEmpTower(this);

    public final PotentialActionKnown empAction = new PotentialActionKnown(ActionDataEmpArea.REG_NAME)
        //TODO implement conditional preCheck to replace current checks
        .withProvider(new ActionFieldProvider()
            .field(ActionFields.AREA_SIZE, () -> (float)this.getRange())
        );

    private final List<TileEmpTowerFake> subBlocks = new ArrayList<>();

    private final TickDoOnce descriptionPacketSender = new TickDoOnce((t) -> PACKET_DESCRIPTION.sendToAllAround(this));

    @Getter
    private final List<PlayerEntity> playersUsing = new LinkedList<>();

    public TileEMPTower() {
        super(TileReg.EMP_TOWER_BASE.get());
        tickActions.add(descriptionPacketSender);
        tickActions.add(new TickAction(3,true,  (t) -> PACKET_GUI.sendPacketToGuiUsers(this, playersUsing)));
        tickActions.add(new TickAction(20,true,  (t) -> {
            playersUsing.removeIf((player) -> !(player.openContainer instanceof ContainerEMPTower));
        }));
        tickActions.add(inventory);
        tickActions.add(new TickAction(5, (t) -> updateStructure()));
        tickActions.add(empAction);
    }

    @Override
    public void markDirty()
    {
        super.markDirty();
        if(isServer()) {
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
    public void onLoad()
    {
        super.onLoad();
        if (isServer())
        {
            RadioRegistry.add(radioCap);
        }
    }

    @Override
    public void remove()
    {
        super.remove();
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
        while(world.getBlockState(above).getBlock() == BlockReg.EMP_TOWER_COIL.get()) {
            final TileEntity tile = world.getTileEntity(above);
            if(tile instanceof TileEmpTowerFake) {
                ((TileEmpTowerFake) tile).setHost(this);
                subBlocks.add((TileEmpTowerFake) tile);
            }
            above = above.up();
        }
    }

    @Override
    public void tick()
    {
        super.tick();

        if (isServer())
        {
            // Eat power
            energyStorage.consumePower(getTickingCost(), false);

            if (ticks % 20 == 0 && isReady()) //TODO convert to a mix of a timer and/or event handler
            {
                ICBMSounds.MACHINE_HUM.play(world, getPos().getX() + 0.5, getPos().getY() + 0.5, getPos().getZ() + 0.5, 0.5F, 0.85F * getChargePercentage(), true);
            }
            else if(getCooldown() > 0 && ticks % 10 == world.rand.nextInt(10)) {
                //TODO add custom sound so sub-titles match
                world.playSound(null, getPos(), SoundEvents.BLOCK_LAVA_EXTINGUISH, SoundCategory.BLOCKS, 0.5F, world.rand.nextFloat() * 0.15F + 0.6F);
            }

            if (isReady() && world.getStrongPower(getPos()) > 0) //TODO convert to action conditional
            {
                fire(null); // TODO provide redstone cause by
            }
        }

        prevRotation = rotation;

        if(cooldownTicks > 0) {
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
        }
        else
        {
            rotation += getChargePercentage() * ROTATION_SPEED;

            clamp(rotation);

            while (this.rotation - this.prevRotation< -180.0F)
            {
                this.prevRotation -= 360.0F;
            }

            while (this.rotation - this.prevRotation >= 180.0F)
            {
                this.prevRotation += 360.0F;
            }
        }
    }

    private void spawnParticles(float rotation, int yOffset) {
        final float faceWidth =  7.0F / 16.0F;
        final float faceHeight = 9.0F / 16.0F;
        final float faceYOffset = 5.0F / 16.0F;
        final float faceWOffset = 3.5F / 16.0F;

        double faceA = faceWidth * world.rand.nextFloat() - (faceWidth / 2);
        double faceB = faceHeight * world.rand.nextFloat();

        double rad = Math.toRadians(clamp(rotation));
        double rad2 = Math.toRadians(clamp(rotation + 90));
        double vecX = Math.sin(rad)  * faceWOffset;
        double vecZ = Math.cos(rad)  * faceWOffset;
        double faceX = Math.sin(rad2) * faceA;
        double faceZ = Math.cos(rad2) * faceA;

        double x = pos.getX() + 0.5;
        double y = pos.getY() + yOffset + faceYOffset - 0.2f;
        double z = pos.getZ() + 0.5;

        double d0 = x + vecX + faceX;
        double d1 = y + faceYOffset + faceB;
        double d2 = z + vecZ + faceZ;
        world.addParticle(ParticleTypes.SMOKE, d0, d1, d2, 0.0D, 0.0D, 0.0D);
    }

    private float clamp(float rotation) {
        while(rotation > 180.0F) {
            rotation -= 360F;
        }
        return rotation;
    }

    public float getChargePercentage()
    {
        return Math.max(0, Math.min(1, energyStorage.getEnergyStored() / (float) getFiringCost()));
    }

    public int getFiringCost()
    {
        return range * range * ConfigEmpTower.ENERGY_COST_AREA;// TODO change this to scale exponentially by area to discourage large area EMPs
    }

    public int getTickingCost() {
        return  range * ConfigEmpTower.ENERGY_COST_TICKING;
    }

    public int getMaxRadius() {
        return ConfigEmpTower.MAX_BASE_RANGE + (subBlocks.size() * ConfigEmpTower.BONUS_RADIUS);
    }

    public void setRange(int range) {
        this.range = Math.min(range, getMaxRadius());
    }

    public boolean fire(IActionCause cause)
    {
        if (this.isReady())
        {
            final IActionStatus response = empAction.doAction(world, getPos(), cause);
            if (response.isType(ActionStatusTypes.GREEN))
            {
                //Consume energy
                this.energyStorage.consumePower(getFiringCost(), false);

                //Reset timer
                this.cooldownTicks = getMaxCooldown();

                return true;
            }
            else
            {
                ICBMClassic.logger().warn("TileEmpTower( DIM: " + world.getDimension().getType().getId() + ", " + getPos() + ") EMP did not trigger, likely was blocked.");
                //TODO display some info to player to explain why blast failed and more detailed debug
            }
        }
        return false;
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox()
    {
        return INFINITE_EXTENT_AABB;
    }

    //@Callback TODO add CC support
    public boolean isReady()
    {
        return getCooldown() <= 0 && this.energyStorage.consumePower(getFiringCost(), true);
    }

    //@Callback TODO add CC support
    public int getCooldown()
    {
        return cooldownTicks;
    }

    public float getCooldownPercentage()
    {
        return 1f - (cooldownTicks / (float)getMaxCooldown());
    }

    //@Callback TODO add CC support
    public int getMaxCooldown()
    {
        return ConfigEmpTower.COOLDOWN; //TODO add to config
    }

    @Override
    public Object getServerGuiElement(int ID, PlayerEntity player)
    {
        //return new ContainerEMPTower(player, this);
        return null;
    }

    @Override
    public Object getClientGuiElement(int ID, PlayerEntity player)
    {
        //return new GuiEMPTower(player, this);
        return null;
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction facing)
    {
        if(capability == CapabilityEnergy.ENERGY) {
            return LazyOptional.of(() -> energyStorage).cast();
        }
        else if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
        {
            return LazyOptional.of(() -> inventory).cast();
        }
        else if(capability == ICBMClassicAPI.RADIO_CAPABILITY)
        {
            return LazyOptional.of(() ->  radioCap).cast();
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public void read(CompoundNBT nbt)
    {
        super.read(nbt);
        SAVE_LOGIC.load(this, nbt);
    }

    @Override
    public CompoundNBT write(CompoundNBT nbt)
    {
        SAVE_LOGIC.save(this, nbt);
        return super.write(nbt);
    }

    private static final NbtSaveHandler<TileEMPTower> SAVE_LOGIC = new NbtSaveHandler<TileEMPTower>()
        .mainRoot()
        /* */.nodeINBTSerializable("emp_action", tile -> tile.empAction)
        /* */.nodeINBTSerializable("inventory", tile -> tile.inventory)
        /* */.nodeINBTSerializable("radio", tile -> tile.radioCap)
        /* */.nodeInteger("range", tile -> tile.range, (tile, i) -> tile.range = i)
        /* */.nodeInteger("cooldown", tile -> tile.cooldownTicks, (tile, i) -> tile.cooldownTicks = i)
        /* */.nodeInteger("energy", tile -> tile.energyStorage.getEnergyStored(), (tile, i) -> tile.energyStorage.setEnergyStored(i)) //TODO use INBTSerializable on storage instance
        /* */.nodeFloat("rotation", tile -> tile.rotation, (tile, f) -> tile.rotation = f)
        /* */.nodeFloat("prev_rotation", tile -> tile.prevRotation, (tile, f) -> tile.prevRotation = f)
        .base();



    public static void register() {
        PacketCodexReg.register(PACKET_RADIUS, PACKET_RADIO_HZ, PACKET_GUI, PACKET_FIRE, PACKET_RADIO_DISABLE, PACKET_DESCRIPTION);
    }

    public static final PacketCodex<TileEMPTower, TileEMPTower> PACKET_RADIUS = new PacketCodexTile<TileEMPTower, TileEMPTower>(REGISTRY_NAME, "radius")
        .fromClient()
        .nodeInt(TileEMPTower::getRange, TileEMPTower::setRange)
        .onFinished((tile, target, player) -> tile.markDirty());

    public static final PacketCodex<TileEMPTower, RadioEmpTower> PACKET_RADIO_HZ = new PacketCodexTile<TileEMPTower, RadioEmpTower>(REGISTRY_NAME, "radio.frequency", (tile) -> tile.radioCap)
        .fromClient()
        .nodeString(RadioEmpTower::getChannel, RadioEmpTower::setChannel)
        .onFinished((tile, target, player) -> tile.markDirty());

    public static final PacketCodex<TileEMPTower, RadioEmpTower> PACKET_RADIO_DISABLE = new PacketCodexTile<TileEMPTower, RadioEmpTower>(REGISTRY_NAME, "radio.disable", (tile) -> tile.radioCap)
        .fromClient()
        .toggleBoolean(RadioEmpTower::isDisabled, RadioEmpTower::setDisabled)
        .onFinished((tile, target, player) -> tile.markDirty());

    public static final PacketCodex<TileEMPTower, TileEMPTower> PACKET_GUI = new PacketCodexTile<TileEMPTower, TileEMPTower>(REGISTRY_NAME, "gui")
        .fromServer()
        .nodeInt((t) -> t.energyStorage.getEnergyStored(), (t, i) -> t.energyStorage.setEnergyStored(i))
        .nodeString((t) -> t.radioCap.getChannel(), (t, s) -> t.radioCap.setChannel(s))
        .nodeBoolean((t) -> t.radioCap.isDisabled(), (t, b) -> t.radioCap.setDisabled(b))
        .nodeInt(TileEMPTower::getRange, TileEMPTower::setRange);

    public static final PacketCodex<TileEMPTower, TileEMPTower> PACKET_FIRE = new PacketCodexTile<TileEMPTower, TileEMPTower>(REGISTRY_NAME, "fire")
        .fromClient()
        .onFinished((tile, target, player) -> {
            tile.fire(null); //TODO add GUI cause using player
            tile.markDirty();
        });

    public static final PacketCodexTile<TileEMPTower, TileEMPTower> PACKET_DESCRIPTION = (PacketCodexTile<TileEMPTower, TileEMPTower>) new PacketCodexTile<TileEMPTower, TileEMPTower>(REGISTRY_NAME, "description")
        .fromServer()
        .nodeFloat((t) -> t.rotation, (t, f) -> t.rotation = f)
        .nodeInt((t) -> t.cooldownTicks, (t, f) -> t.cooldownTicks = f);
}
