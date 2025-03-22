package icbm.classic.content.cluster.bomblet;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.reg.IExplosiveData;
import icbm.classic.config.missile.ConfigBomblet;
import icbm.classic.config.missile.ConfigMissile;
import icbm.classic.content.missile.logic.source.ActionSource;
import icbm.classic.content.missile.logic.source.cause.EntityCause;
import icbm.classic.lib.capability.ex.CapabilityExplosiveEntity;
import icbm.classic.lib.saving.NbtSaveHandler;
import icbm.classic.lib.saving.NbtSaveNode;
import icbm.classic.lib.projectile.EntityProjectile;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class EntityBombDroplet extends EntityProjectile<EntityBombDroplet> implements IEntityAdditionalSpawnData {

    public static final DamageSource DAMAGE_SOURCE = new DamageSource("icbmclassic:bomblet");

    public final CapabilityExplosiveEntity explosive = new CapabilityExplosiveEntity(this);
    public EntityBombDroplet(World world) {
        super(world);
        this.setSize(0.25f, 0.25f);
        this.hasHealth = false;
    }

    @Override
    protected float getImpactDamage(Entity entityHit, float velocity, RayTraceResult hit) {
        return MathHelper.ceil(velocity * ConfigMissile.bomblet.impactDamage);
    }

    @Override
    protected DamageSource getImpactDamageSource(Entity entityHit, float velocity, RayTraceResult hit) {
        return EntityBombDroplet.DAMAGE_SOURCE;
    }

    @Override
    protected void onImpact(@Nonnull RayTraceResult hit) {
       super.onImpact(hit);
       explosive.doExplosion(hit.hitVec.x, hit.hitVec.y, hit.hitVec.z, new ActionSource(world, new Vec3d(posX, posY, posZ), new EntityCause(this))); //TODO include impact cause info
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
        return Math.max(1, ConfigMissile.bomblet.health);
    }

    @Override
    protected void onDestroyedBy(DamageSource source, float damage) {
        super.onDestroyedBy(source, damage);
        // TODO add config
        // TODO add random chance modifier
        if (source.isExplosion() || source.isFireDamage()) {
            explosive.doExplosion(posX, posY, posZ, new ActionSource(world, new Vec3d(posX, posY, posZ), new EntityCause(this))); //TODO include source of damage
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
            return I18n.translateToLocal("bomb.droplet." + data.getRegistryKey().toString() + ".name");
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
