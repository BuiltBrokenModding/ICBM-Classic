package icbm.classic;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Fake tile for use with read/write tests.
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class BlockEntityFakeData extends BlockEntity {
    private int field1;
    private float field2;
    private String field3;

    @Accessors(fluent = true)
    private boolean wasRead;
}
