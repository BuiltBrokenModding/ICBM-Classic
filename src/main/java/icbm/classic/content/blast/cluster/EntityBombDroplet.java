package icbm.classic.content.blast.cluster;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.explosion.BlastState;
import icbm.classic.api.explosion.responses.BlastResponse;
import icbm.classic.api.reg.IExplosiveData;
import icbm.classic.config.missile.ConfigMissile;
import icbm.classic.content.entity.EntityPlayerSeat;
import icbm.classic.content.missile.entity.explosive.EntityExplosiveMissile;
import icbm.classic.lib.capability.ex.CapabilityExplosiveEntity;
import icbm.classic.lib.explosive.ExplosiveHandler;
import icbm.classic.lib.saving.NbtSaveHandler;
import icbm.classic.lib.saving.NbtSaveNode;
import icbm.classic.lib.transform.rotation.EulerAngle;
import icbm.classic.prefab.entity.EntityProjectile;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

public class EntityBombDroplet extends EntityProjectile<EntityBombDroplet> implements IEntityAdditionalSpawnData {

    public final CapabilityExplosiveEntity explosive = new CapabilityExplosiveEntity(this);
    public EntityBombDroplet(World world) {
        super(world);
        this.setSize(0.25f, 0.25f);
        this.hasHealth = false;
    }

    @Override
    protected void onImpact(Vec3d impactLocation) {
       super.onImpact(impactLocation);
       explosive.doExplosion(impactLocation);
    }

    @Override
    public Vec3d getLook(float partialTicks)
    {
        double mag = Math.sqrt(motionX * motionX + motionY * motionY + motionZ * motionZ);
        return new Vec3d(motionX / mag, motionY / mag, motionZ / mag);
    }

    @Override
    protected boolean shouldCollideWith(Entity entity)
    {
        return super.shouldCollideWith(entity) && !(entity instanceof EntityBombDroplet); //TODO ignore collision only for first few ticks
    }

    @Override
    public float getMaxHealth()
    {
        return 5; //TODO config
    }

    @Override
    protected void onDestroyedBy(DamageSource source, float damage) {
        super.onDestroyedBy(source, damage);
        // TODO add config
        // TODO add random chance modifier
        if (source.isExplosion() || source.isFireDamage()) {
            explosive.doExplosion(this.getPositionVector());
        }
    }

    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing)
    {
        if(capability == ICBMClassicAPI.EXPLOSIVE_CAPABILITY) {
            return ICBMClassicAPI.EXPLOSIVE_CAPABILITY.cast(explosive);
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing)
    {
        return capability == ICBMClassicAPI.EXPLOSIVE_CAPABILITY
            || super.hasCapability(capability, facing);
    }

    @Override
    public String getName()
    {
        final IExplosiveData data = explosive.getExplosiveData();
        if (data != null)
        {
            return I18n.translateToLocal("bomb.droplet." + data.getRegistryName().toString() + ".name");
        }
        return I18n.translateToLocal("bomb.droplet.icbmclassic:generic.name");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean isInRangeToRenderDist(double distance)
    {
        double d0 = this.getEntityBoundingBox().getAverageEdgeLength() * 10.0D;

        if (Double.isNaN(d0))
        {
            d0 = 1.0D;
        }

        d0 = d0 * 64.0D * getRenderDistanceWeight();
        return distance < d0 * d0;
    }

    @Override
    public void writeSpawnData(ByteBuf additionalMissileData)
    {
        super.writeSpawnData(additionalMissileData);
        final NBTTagCompound saveData = SAVE_LOGIC.save(this, new NBTTagCompound());
        ByteBufUtils.writeTag(additionalMissileData, saveData);
    }

    @Override
    public void readSpawnData(ByteBuf additionalMissileData)
    {
        super.readSpawnData(additionalMissileData);
        final NBTTagCompound saveData = ByteBufUtils.readTag(additionalMissileData);
        SAVE_LOGIC.load(this, saveData);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound nbt)
    {
        super.readEntityFromNBT(nbt);
        SAVE_LOGIC.load(this, nbt);
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound nbt)
    {
        super.writeEntityToNBT(nbt);
        SAVE_LOGIC.save(this, nbt);
    }

    private static final NbtSaveHandler<EntityBombDroplet> SAVE_LOGIC = new NbtSaveHandler<EntityBombDroplet>()
        .mainRoot()
        /* */.node(new NbtSaveNode<EntityBombDroplet, NBTTagCompound>("explosive",
            (missile) -> missile.explosive.serializeNBT(),
            (missile, data) -> missile.explosive.deserializeNBT(data))
        )
        .base();

    public ItemStack toStack() {
        return explosive.toStack();
    }
}
