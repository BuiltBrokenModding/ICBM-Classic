package icbm.classic.content.blocks.emptower;

import icbm.classic.ICBMClassic;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.refs.ICBMExplosives;
import icbm.classic.config.ConfigMain;
import icbm.classic.config.machines.ConfigEmpTower;
import icbm.classic.content.blocks.emptower.gui.ContainerEMPTower;
import icbm.classic.content.blocks.emptower.gui.GuiEMPTower;
import icbm.classic.api.explosion.BlastState;
import icbm.classic.api.explosion.IBlast;
import icbm.classic.client.ICBMSounds;
import icbm.classic.content.blast.BlastEMP;
import icbm.classic.lib.energy.storage.EnergyBuffer;
import icbm.classic.lib.energy.system.EnergySystem;
import icbm.classic.lib.network.IPacket;
import icbm.classic.lib.network.IPacketIDReceiver;
import icbm.classic.lib.network.packet.PacketTile;
import icbm.classic.lib.radio.RadioRegistry;
import icbm.classic.lib.saving.NbtSaveHandler;
import icbm.classic.prefab.inventory.InventorySlot;
import icbm.classic.prefab.inventory.InventoryWithSlots;
import icbm.classic.prefab.tile.IGuiTile;
import icbm.classic.prefab.tile.TileMachine;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.items.CapabilityItemHandler;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

/** Logic side of the EMP tower block */
public class TileEMPTower extends TileMachine implements IPacketIDReceiver, IGuiTile
{
    public static final int ROTATION_SPEED = 15;

    public static final int CHANGE_RADIUS_PACKET_ID = 1; //TODO migrate to its own handler
    public static final int CHANGE_HZ_PACKET_ID = 2;
    public static final int GUI_PACKET_ID = 3;
    public static final int FIRE_PACKET_ID = 4;
    public static final int RADIO_DISABLE_ID = 5;

    /** Tick synced rotation */
    public float rotation = 0;

    /** Client side use in render */
    public float prevRotation = 0;

    /** Delay before EMP can be fired again */
    protected int cooldownTicks = 0;

    /** Radius of the EMP tower */
    @Setter @Getter
    public int range = 60;

    public final EnergyBuffer energyStorage = new EnergyBuffer(() -> this.getFiringCost() + (this.getTickingCost() * ConfigEmpTower.ENERGY_COST_TICKING_CAP))
        .withOnChange((p,c,s) -> {this.updateClient = true; this.markDirty();})
        .withCanReceive(() -> this.getCooldown() <= 0)
        .withCanExtract(() -> false)
        .withReceiveLimit(() -> ConfigEmpTower.ENERGY_INPUT);
    public final InventoryWithSlots inventory = new InventoryWithSlots(1)
        .withChangeCallback((s, i) -> markDirty())
        .withSlot(new InventorySlot(0, EnergySystem::isEnergyItem).withTick(this.energyStorage::dischargeItem));
    public final RadioEmpTower radioCap = new RadioEmpTower(this);

    private final List<TileEmpTowerFake> subBlocks = new ArrayList<>();

    @Override
    public void provideInformation(BiConsumer<String, Object> consumer) {
        super.provideInformation(consumer);
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
    public void invalidate()
    {
        super.invalidate();
        subBlocks.forEach(tile -> tile.setHost(null));
        subBlocks.clear();
        if (isServer()) {
            RadioRegistry.remove(radioCap);
        }
    }

    @Override
    public void update()
    {
        super.update();
        if (ticks % 3 == 0) {
            //Find tower blocks TODO find a better solution
            subBlocks.clear();
            BlockPos above = getPos().up();
            while(world.getBlockState(above).getBlock() == getBlockType()) {
                final TileEntity tile = world.getTileEntity(above);
                if(tile instanceof TileEmpTowerFake) {
                    ((TileEmpTowerFake) tile).setHost(this);
                    subBlocks.add((TileEmpTowerFake) tile);
                }
                above = above.up();
            }
        }

        // Tick slots
        inventory.onTick();

        if (isServer())
        {
            // Eat power
            energyStorage.consumePower(getTickingCost(), false);

            if (ticks % 20 == 0 && isReady()) //TODO convert to a mix of a timer and/or event handler
            {
                ICBMSounds.MACHINE_HUM.play(world, xi() + 0.5, yi() + 0.5, zi() + 0.5, 0.5F, 0.85F * getChargePercentage(), true);
            }
            else if(getCooldown() > 0 && ticks % 10 == world.rand.nextInt(10)) {
                //TODO add custom sound so sub-titles match
                world.playSound(null, getPos(), SoundEvents.BLOCK_LAVA_EXTINGUISH, SoundCategory.BLOCKS, 0.5F, world.rand.nextFloat() * 0.15F + 0.6F);
            }

            // Sync every so often to keep clients matching on animations
            if(ticks % 3 == 0) {
                sendDescPacket();
            }

            if (isReady() && world.getStrongPower(getPos()) > 0) //TODO convert to a state handler
            {
                fire();
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
        world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, d0, d1, d2, 0.0D, 0.0D, 0.0D);
    }

    private float clamp(float rotation) {
        while(rotation > 180.0F) {
            rotation -= 360F;
        }
        return rotation;
    }

    @Override
    public void writeDescPacket(ByteBuf buf)
    {
        super.writeDescPacket(buf);
        buf.writeFloat(rotation);
        buf.writeInt(cooldownTicks);
    }

    @Override
    public void readDescPacket(ByteBuf buf)
    {
        super.readDescPacket(buf);
        rotation = buf.readFloat();
        cooldownTicks = buf.readInt();
    }

    public void sendRangePacket(int range) {
        if(isClient())
            ICBMClassic.packetHandler.sendToServer(new PacketTile("range_C>S", TileEMPTower.CHANGE_RADIUS_PACKET_ID, this).addData(range));
    }

    public void sendHzPacket(String channel) {
        if(isClient())
            ICBMClassic.packetHandler.sendToServer(new PacketTile("frequency_C>S", TileEMPTower.CHANGE_HZ_PACKET_ID, this).addData(channel));
    }

    public void sendFirePacket() {
        if(isClient())
            ICBMClassic.packetHandler.sendToServer(new PacketTile("fire_C>S", TileEMPTower.FIRE_PACKET_ID, this));
    }

    public void sendRadioDisabled() {
        if(isClient())
            ICBMClassic.packetHandler.sendToServer(new PacketTile("radioDisabled_C>S", TileEMPTower.RADIO_DISABLE_ID, this).addData(!radioCap.isDisabled()));
    }

    @Override
    public PacketTile getGUIPacket()
    {
        return new PacketTile("gui", GUI_PACKET_ID, this).addData(energyStorage.getEnergyStored(), this.radioCap.getChannel(), this.range, this.radioCap.isDisabled());
    }

    public float getChargePercentage()
    {
        return Math.max(0, Math.min(1, energyStorage.getEnergyStored() / (float) getFiringCost()));
    }

    @Override
    public boolean read(ByteBuf data, int id, EntityPlayer player, IPacket type) //TODO migrate to a packet handler
    {
        if (!super.read(data, id, player, type))
        {
            if (id == CHANGE_RADIUS_PACKET_ID) {
                range = data.readInt();
                updateClient = true;
                return true;
            }
            else if(id == CHANGE_HZ_PACKET_ID) {
                radioCap.setChannel(ByteBufUtils.readUTF8String(data));
                updateClient = true;
                return true;
            }
            else if(id == FIRE_PACKET_ID) {
                fire();
                updateClient = true;
                return true;
            }
            else if(id == RADIO_DISABLE_ID) {
                radioCap.setDisabled(data.readBoolean());
                updateClient = true;
                return true;
            }
            else if(id == GUI_PACKET_ID && isClient()) {
                this.energyStorage.setEnergyStored(data.readInt());
                this.radioCap.setChannel(ByteBufUtils.readUTF8String(data));
                this.range = data.readInt();
                this.radioCap.setDisabled(data.readBoolean());
                return true;
            }
            return false;
        }
        return true;
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

    protected IBlast buildBlast()
    {
        return ((BlastEMP)ICBMExplosives.EMP.create()
                .setBlastWorld(world)
                .setBlastPosition(this.xi() + 0.5, this.yi() + 1.2, this.zi() + 0.5)
                .setBlastSize(range))
                .clearSetEffectBlocksAndEntities()
                .setEffectBlocks().setEffectEntities()
                .buildBlast();
    }

    //@Callback(limit = 1) TODO add CC support
    public boolean fire()
    {
        if (this.isReady())
        {
            //Finish and trigger
            if (buildBlast().runBlast().state == BlastState.TRIGGERED)
            {
                //Consume energy
                this.energyStorage.consumePower(getFiringCost(), false);

                //Reset timer
                this.cooldownTicks = getMaxCooldown();

                return true;
            }
            else
            {
                ICBMClassic.logger().warn("TileEmpTower( DIM: " + world.provider.getDimension() + ", " + getPos() + ") EMP did not trigger, likely was blocked.");
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
    public Object getServerGuiElement(int ID, EntityPlayer player)
    {
        return new ContainerEMPTower(player, this);
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player)
    {
        return new GuiEMPTower(player, this);
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing)
    {
        return super.hasCapability(capability, facing)
            || capability == CapabilityEnergy.ENERGY && ConfigMain.REQUIRES_POWER
            || capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY
            || capability == ICBMClassicAPI.RADIO_CAPABILITY;
    }

    @Override
    @Nullable
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing)
    {
        if(capability == CapabilityEnergy.ENERGY) {
            return (T) energyStorage;
        }
        else if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
        {
            return (T) inventory;
        }
        else if(capability == ICBMClassicAPI.RADIO_CAPABILITY)
        {
            return (T) radioCap;
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        super.readFromNBT(nbt);
        SAVE_LOGIC.load(this, nbt);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt)
    {   SAVE_LOGIC.save(this, nbt);
        return super.writeToNBT(nbt);
    }

    private static final NbtSaveHandler<TileEMPTower> SAVE_LOGIC = new NbtSaveHandler<TileEMPTower>()
        .mainRoot()
        /* */.nodeINBTSerializable("inventory", tile -> tile.inventory)
        /* */.nodeINBTSerializable("radio", tile -> tile.radioCap)
        /* */.nodeInteger("range", tile -> tile.range, (tile, i) -> tile.range = i)
        /* */.nodeInteger("cooldown", tile -> tile.cooldownTicks, (tile, i) -> tile.cooldownTicks = i)
        /* */.nodeInteger("energy", tile -> tile.energyStorage.getEnergyStored(), (tile, i) -> tile.energyStorage.setEnergyStored(i)) //TODO use INBTSerializable on storage instance
        /* */.nodeFloat("rotation", tile -> tile.rotation, (tile, f) -> tile.rotation = f)
        /* */.nodeFloat("prev_rotation", tile -> tile.prevRotation, (tile, f) -> tile.prevRotation = f)
        .base();

}
