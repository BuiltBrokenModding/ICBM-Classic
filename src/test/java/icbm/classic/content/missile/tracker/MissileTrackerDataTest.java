package icbm.classic.content.missile.tracker;

import com.adelean.inject.resources.junit.jupiter.GivenBinaryResource;
import com.adelean.inject.resources.junit.jupiter.GivenJsonResource;
import com.adelean.inject.resources.junit.jupiter.TestWithResources;
import com.ibm.icu.impl.Assert;
import com.lunarshark.nbttool.mod.NBTTool;
import com.lunarshark.nbttool.utils.SaveToJson;
import icbm.classic.TestBase;
import icbm.classic.lib.transform.vector.Pos;
import net.minecraft.nbt.NBTTagCompound;
import org.apache.logging.log4j.LogManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

@TestWithResources
public class MissileTrackerDataTest extends TestBase {

    @GivenJsonResource("data/saves/4.0.0/worldData_missileTracker.json")
    NBTTagCompound fullSave400;

    @GivenJsonResource("data/saves/4.0.0/worldData_missileTracker_data.json")
    NBTTagCompound missileData400;

    @GivenJsonResource("data/saves/fixer/worldData_missileTracker_missile.json")
    NBTTagCompound missile;

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
