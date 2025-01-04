package icbm.classic.content.cluster.bomblet;

import icbm.classic.api.actions.IActionData;
import icbm.classic.config.missile.ConfigMissile;
import icbm.classic.content.missile.logic.source.ActionSource;
import icbm.classic.content.missile.logic.source.cause.EntityCause;
import icbm.classic.lib.actions.PotentialAction;
import icbm.classic.lib.saving.NbtSaveHandler;
import icbm.classic.lib.saving.NbtSaveNode;
import icbm.classic.lib.projectile.EntityProjectile;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullSupplier;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;

import javax.annotation.Nullable;

public class EntityBombDroplet extends EntityProjectile<EntityBombDroplet> implements IEntityAdditionalSpawnData {

    public static final DamageSource DAMAGE_SOURCE = new DamageSource("icbmclassic:bomblet");

    private final PotentialAction potentialAction = new PotentialAction();
    private final LazyOptional<ItemStack> itemstack;

    public EntityBombDroplet(EntityType<EntityBombDroplet> type, World world, IActionData data, NonNullSupplier<ItemStack> itemstack) {
        super(type, world);
        this.potentialAction.setActionData(data);
        this.itemstack = LazyOptional.of(itemstack);
        //this.setSize(0.25f, 0.25f);
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
    protected void onImpact(RayTraceResult hit) {
       super.onImpact(hit);
        potentialAction.doAction(world, posX, posY, posZ, new EntityCause(this)); //TODO include impact cause
    }

    @Override
    protected boolean shouldCollideWith(Entity entity)
    {
        return super.shouldCollideWith(entity) && !(entity instanceof EntityBombDroplet); //TODO ignore collision only for first few ticks
    }

    @Override
    public float getMaxHealth()
    {
        return Math.max(1, ConfigMissile.bomblet.health); //TODO config per type
    }

    @Override
    protected void onDestroyedBy(DamageSource source, float damage) {
        super.onDestroyedBy(source, damage);
        // TODO add config
        // TODO add random chance modifier
        if (source.isExplosion() || source.isFireDamage()) {
            potentialAction.doAction(world, posX, posY, posZ, new EntityCause(this)); //TODO include source of damage
        }
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction facing)
    {
        //if(capability == ICBMClassicAPI.EXPLOSIVE_CAPABILITY) {
        // TODO    return (LazyOptional<T>) LazyOptional.of(() -> explosive);
        //}
        return super.getCapability(capability, facing);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean isInRangeToRenderDist(double distance)
    {
        double d0 = this.getBoundingBox().getAverageEdgeLength() * 10.0D;

        if (Double.isNaN(d0))
        {
            d0 = 1.0D;
        }

        d0 = d0 * 64.0D * getRenderDistanceWeight();
        return distance < d0 * d0;
    }

    public ItemStack toStack() {
        return this.itemstack.orElse(ItemStack.EMPTY);
    }
}
