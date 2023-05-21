package icbm.classic;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.tileentity.TileEntity;

/**
 * Fake tile for use with read/write tests.
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class TileEntityFakeData extends TileEntity {
    private int field1;
    private float field2;
    private String field3;
}
