package icbm.classic.content.entity;

import com.adelean.inject.resources.junit.jupiter.GivenJsonResource;
import com.adelean.inject.resources.junit.jupiter.TestWithResources;
import icbm.classic.TestBase;
import icbm.classic.world.entity.BombCartEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@TestWithResources
public class EntityBombCartTest extends TestBase {

    @GivenJsonResource("data/saves/4.0.0/entity_ExplosiveCart_sonic.json")
    CompoundTag version4save;

    @Test
    @DisplayName("Loads from old version 4.0.0 save file")
    void loadFromVersion4() {
        final Level level = testManager.getLevel();
        final BombCartEntity bombCart = new BombCartEntity(world);

        // Validate we have a test file
        Assertions.assertNotNull(version4save);

        // Load entity custom save
        bombCart.readEntityFromNBT(version4save);

        // Verify it loaded explosive type
        Assertions.assertEquals(10, bombCart.explosive);
    }
}
