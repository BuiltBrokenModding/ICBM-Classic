package icbm.classic.lib.projectile;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.missiles.projectile.IProjectileData;
import icbm.classic.api.missiles.projectile.IProjectileStack;
import icbm.classic.content.cargo.parachute.EntityParachute;
import icbm.classic.lib.saving.NbtSaveHandler;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;

public class ProjectileStack implements IProjectileStack<EntityParachute>, INBTSerializable<NBTTagCompound> {

    @Getter @Setter @Accessors(chain = true)
    private IProjectileData projectileData;


    @Override
    public NBTTagCompound serializeNBT() {
        return SAVE_LOGIC.save(this);
    }

    @Override
    public void deserializeNBT(NBTTagCompound save) {
        SAVE_LOGIC.load(this, save);
    }


    private static final NbtSaveHandler<ProjectileStack> SAVE_LOGIC = new NbtSaveHandler<ProjectileStack>()
        //Stuck in ground data
        .mainRoot()
        .nodeBuildableObject("data", () -> ICBMClassicAPI.PROJECTILE_DATA_REGISTRY, ProjectileStack::getProjectileData, ProjectileStack::setProjectileData)
        .base();
}
