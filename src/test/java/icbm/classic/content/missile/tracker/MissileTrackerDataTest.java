package icbm.classic.content.missile.tracker;

import com.adelean.inject.resources.junit.jupiter.GivenJsonResource;
import com.adelean.inject.resources.junit.jupiter.TestWithResources;
import icbm.classic.TestBase;
import icbm.classic.world.missile.tracker.MissileTrackerData;
import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@TestWithResources
public class MissileTrackerDataTest extends TestBase {

    @GivenJsonResource("data/saves/4.0.0/worldData_missileTracker_data.json")
    CompoundTag missileData400;

    @GivenJsonResource("data/saves/fixer/worldData_missileTracker_missile.json")
    CompoundTag missile;

    @Test
    @DisplayName("Updates v4.0.0 missile data")
    void loadFromVersion4(){
        Assertions.assertNotNull(missileData400);

        final MissileTrackerData trackerData = new MissileTrackerData(missileData400);

        Assertions.assertEquals(958, trackerData.ticksLeftToTarget);
        Assertions.assertEquals(new Pos(9987.054351608229, 0, -20.767897003956243), trackerData.targetPos);
        assertTags(missile, trackerData.missileData);
    }
}
