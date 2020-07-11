package icbm.classic.content.items;

import icbm.classic.ICBMClassic;
import icbm.classic.api.ICBMClassicHelpers;
import icbm.classic.api.events.LaserRemoteTriggerEvent;
import icbm.classic.client.ICBMSounds;
import icbm.classic.content.entity.EntityLightBeam;
import icbm.classic.lib.NBTConstants;
import icbm.classic.lib.network.IPacket;
import icbm.classic.lib.network.IPacketIDReceiver;
import icbm.classic.lib.network.packet.PacketPlayerItem;
import icbm.classic.lib.radio.RadioRegistry;
import icbm.classic.lib.transform.vector.Pos;
import icbm.classic.prefab.FakeRadioSender;
import icbm.classic.prefab.item.ItemICBMElectrical;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.*;

/**
 * Extended version of {@link ItemRemoteDetonator} that can target blocks in a line of sight.
 *
 *
 * Created by Dark(DarkGuardsman, Robert) on 3/26/2016.
 */
public class ItemLaserDetonator extends ItemICBMElectrical implements IPacketIDReceiver
{
    public ItemLaserDetonator()
    {
        super("laserDetonator");
        this.setHasSubtypes(true);
        this.setMaxStackSize(1);
        this.setNoRepair();
    }

    private final int maxCooldownTicks = 20;
    private int cooldownRemaining = 0;
    private Map<EntityLightBeam, Integer> ticksRemainingPerLightbeam = new HashMap<>();

    private static Random rand = new Random();

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand handIn)
    {
        ItemStack stack = player.getHeldItem(handIn);
        if (world.isRemote && cooldownRemaining <= 0)
        {
            cooldownRemaining = maxCooldownTicks;
            RayTraceResult objectMouseOver = player.rayTrace(200, 1);
            if (objectMouseOver.typeOfHit != RayTraceResult.Type.MISS) // ignore failed raytraces
            {
                BlockPos mouseOverBlockPos = objectMouseOver.getBlockPos();
                TileEntity tileEntity = world.getTileEntity(mouseOverBlockPos);
                if (!(ICBMClassicHelpers.isLauncher(tileEntity, null)))
                {
                    world.playSound(player,player.posX,player.posY,player.posZ, ICBMSounds.AIRSTRIKE.getSound(), SoundCategory.BLOCKS, 7, rand.nextFloat() * 0.1f + 0.9f);
                    ICBMClassic.packetHandler.sendToServer(new PacketPlayerItem(player).addData(mouseOverBlockPos));
                }
            }// TODO else: add message stating that the raytrace failed
        }
        return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
    }

    @Override
    public ItemStack onItemUseFinish(ItemStack stack, World world, EntityLivingBase entityLiving) {

        if (world.isRemote) // when releasing the right mouse button, reset the cooldown to allow immediate reuse of item
            cooldownRemaining = 0;
        return super.onItemUseFinish(stack, world, entityLiving);
    }

    @Override
    public void onUpdate(ItemStack stack, World world, Entity entity, int itemSlot, boolean isSelected) {
        if (world.isRemote && cooldownRemaining > 0) // when holding the right mouse button, trigger item use every second
            cooldownRemaining--;

        if (!world.isRemote)
        {
            ArrayList<EntityLightBeam> toRemove = new ArrayList<>();
            for (Map.Entry<EntityLightBeam, Integer> entry : ticksRemainingPerLightbeam.entrySet())
            {
                Integer ticksRemaining = entry.getValue();
                if (ticksRemaining > 0)
                {
                    entry.setValue(ticksRemaining-1);
                }
                else
                {
                    toRemove.add(entry.getKey());
                }
            }

            for (EntityLightBeam beamEntity : toRemove)
            {
                ticksRemainingPerLightbeam.remove(beamEntity);
                beamEntity.startDeathCycle();
            }
        }

        super.onUpdate(stack, world, entity, itemSlot, isSelected);
    }

    @Override
    public boolean read(ByteBuf buf, int id, EntityPlayer player, IPacket packet)
    {
        ItemStack stack = player.inventory.getCurrentItem();
        if (stack != null && stack.getItem() == this)
        {
            if (!player.world.isRemote)
            {
                int x = buf.readInt();
                int y = buf.readInt();
                int z = buf.readInt();

                LaserRemoteTriggerEvent event = new LaserRemoteTriggerEvent(player.world, new BlockPos(x, y, z), player);


                // TODO make the beam only visible to the client that spawned it
                // TODO fix: when a beam is visible and alive and you save and reload the map, it turns red and does not despawn quickly anymore.
                EntityLightBeam beam = new EntityLightBeam(player.world)
                        .setPosition(new Pos(x, y+3d, z))
                        .setColor(0f, 1f, 0f);

                beam.beamSize = 1;
                beam.beamGlowSize = 2;
                beam.setTargetBeamProgress(0.1f);
                ticksRemainingPerLightbeam.put(beam, 10 * 20);

                player.world.spawnEntity(beam);

                if(MinecraftForge.EVENT_BUS.post(event)) //event was canceled
                    return false;

                if(event.pos == null) //someone set the pos in the event to null, use original data
                    RadioRegistry.popMessage(player.world, new FakeRadioSender(player, stack, 2000), getBroadCastHz(stack), "activateLauncherWithTarget", new Pos(x, y, z));
                else
                    RadioRegistry.popMessage(player.world, new FakeRadioSender(player, stack, 2000), getBroadCastHz(stack), "activateLauncherWithTarget", new Pos(event.pos.getX(), event.pos.getY(), event.pos.getZ()));
            }
            else
            {
                player.sendMessage(new TextComponentString("Not encoded with launch data! Right click on launcher screen to encode."));
            }
        }
        return true;
    }

    @Override
    public boolean doesSneakBypassUse(ItemStack stack, net.minecraft.world.IBlockAccess world, BlockPos pos, EntityPlayer player)
    {
        return true;
    }

    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean b)
    {
        list.add("Fires missiles remotely");
        list.add("Right click launcher screen to encode");
    }

    /**
     * Gets the frequency this item broadcasts information on
     *
     * @param stack - this item
     * @return frequency
     */
    public float getBroadCastHz(ItemStack stack)
    {
        if (stack.getTagCompound() != null && stack.getTagCompound().hasKey(NBTConstants.HZ))
        {
            return stack.getTagCompound().getFloat(NBTConstants.HZ);
        }
        return 0;
    }

    /**
     * Sets the frequency of this item
     *
     * @param stack - this item
     * @param hz    - value to set
     */
    public void setBroadCastHz(ItemStack stack, float hz)
    {
        if (stack.getTagCompound() == null)
        {
            stack.setTagCompound(new NBTTagCompound());
        }
        stack.getTagCompound().setFloat(NBTConstants.HZ, hz);
    }
}
