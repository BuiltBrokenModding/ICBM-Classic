package icbm.classic.world.item.behavior;

import icbm.classic.world.IcbmEntityTypes;
import icbm.classic.world.entity.GrenadeEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import org.jetbrains.annotations.NotNull;

public class GrenadeDispenseBehavior implements DispenseItemBehavior {

    @Override
    public @NotNull ItemStack dispense(@NotNull BlockSource blockSource, ItemStack itemStack) {
        if (itemStack.getCount() > 0) {
            Level level = blockSource.level();
            if (!level.isClientSide()) {
                level.addFreshEntity(create(level, blockSource, itemStack));
            }

            return itemStack.split(itemStack.getCount() - 1);
        }
        return ItemStack.EMPTY;
    }

    private Entity create(Level level, BlockSource blockSource, ItemStack itemStack) {
        final Direction enumFacing = blockSource.state().getValue(DispenserBlock.FACING);

        GrenadeEntity entity = new GrenadeEntity(IcbmEntityTypes.GRENADE.get(), level);
        entity.setPos(blockSource.pos().getX(), blockSource.pos().getY(), blockSource.pos().getZ());
        entity.setItemStack(itemStack);
        entity.setThrowableHeading(enumFacing.getStepX(), 0.1, enumFacing.getStepZ(), 0.5F, 1.0F);
        return entity;
    }
}
