package icbm.classic.lib.explosive.reg;

import icbm.classic.api.WeaponTier;
import icbm.classic.api.explosion.IBlastFactory;
import icbm.classic.api.reg.ExplosiveType;
import icbm.classic.api.reg.content.IExplosiveContentRegistry;
import lombok.ToString;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

/**
 * Handles storing data about an explosive in the {@link ExplosiveRegistry}
 * <p>
 * <p>
 * Created by Dark(DarkGuardsman, Robert) on 1/4/19.
 */
@ToString
public class SimpleExplosiveType implements ExplosiveType {

    public final WeaponTier tier;
    private final IBlastFactory factory;
    private final Set<ResourceLocation> enabledContent = new HashSet<>();

    private boolean enabled = true;

    public SimpleExplosiveType(WeaponTier tier, IBlastFactory factory) {
        this.tier = tier;
        this.factory = factory;
    }

    @Override
    public IBlastFactory getBlastFactory() {
        return factory;
    }

    @Override
    public @NotNull WeaponTier getTier() {
        return tier;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean b) {
        this.enabled = b;
    }

    @Override
    public boolean onEnableContent(ResourceLocation location, IExplosiveContentRegistry registry) {
        enabledContent.add(location);
        return true;
    }
}
