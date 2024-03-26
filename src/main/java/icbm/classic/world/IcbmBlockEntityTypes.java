package icbm.classic.world;

import com.mojang.datafixers.types.Type;
import icbm.classic.IcbmConstants;
import icbm.classic.world.block.emptower.EmpTowerBlockEntity;
import icbm.classic.world.block.launcher.base.LauncherBaseBlockEntity;
import icbm.classic.world.block.launcher.connector.LaunchConnectorBlockEntity;
import net.minecraft.Util;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public final class IcbmBlockEntityTypes {

    public static final DeferredRegister<BlockEntityType<?>> REGISTER = DeferredRegister.create(
        Registries.BLOCK_ENTITY_TYPE, IcbmConstants.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<EmpTowerBlockEntity>> EMP_TOWER = register(
        "emp_tower",
        () -> BlockEntityType.Builder.of(EmpTowerBlockEntity::new, IcbmBlocks.EMP_TOWER.get())
    );

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<LaunchConnectorBlockEntity>> LAUNCH_CONNECTOR = register(
        "launch_connector",
        () -> BlockEntityType.Builder.of(LaunchConnectorBlockEntity::new, IcbmBlocks.LAUNCH_CONNECTOR.get())
    );

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<LauncherBaseBlockEntity>> LAUNCHER_BASE = register(
        "launcher_base",
        () -> BlockEntityType.Builder.of(LauncherBaseBlockEntity::new, IcbmBlocks.LAUNCHER_BASE.get())
    );

    private static <T extends BlockEntity> DeferredHolder<BlockEntityType<?>, BlockEntityType<T>> register(
        String name, Supplier<BlockEntityType.Builder<T>> supplier) {

        Type<?> type = Util.fetchChoiceType(References.BLOCK_ENTITY, name);

        return REGISTER.register(name, () -> supplier.get().build(type));
    }
}
