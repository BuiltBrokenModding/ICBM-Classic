package icbm.classic.content.entity;

import com.adelean.inject.resources.junit.jupiter.GivenJsonResource;
import com.adelean.inject.resources.junit.jupiter.TestWithResources;
import icbm.classic.TestBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@TestWithResources
public class EntityBombCartTest extends TestBase {

    @GivenJsonResource("data/saves/4.0.0/entity_ExplosiveCart_sonic.json")
    NBTTagCompound version4save;

    @Test
    @DisplayName("Loads from old version 4.0.0 save file")
    void loadFromVersion4() {
        final World world = testManager.getWorld();
        final EntityBombCart bombCart = new EntityBombCart(world);

        // Validate we have a test file
        Assertions.assertNotNull(version4save);

        // Load entity custom save
        bombCart.readEntityFromNBT(version4save);

        // Verify it loaded explosive type
        Assertions.assertEquals(10, bombCart.explosive);
    }
}
