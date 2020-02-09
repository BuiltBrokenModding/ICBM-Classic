package icbm.classic.config.blast;

import net.minecraftforge.common.config.Config;

/**
 * Created by Dark(DarkGuardsman, Robert) on 2/9/2020.
 */
public class ConfigRedmatter
{
    //Constants, do not change as they modify render and effect scales
    public final float NORMAL_RADIUS = 70;
    public final float ENTITY_DESTROY_RADIUS = 6;
    public final int MAX_RUNTIME_MS = 5; //TODO config

    @Config.Name("redmatter_movement")
    @Config.Comment("Allows red matter explosions to be moved")
    public boolean REDMATTER_MOVEMENT = true;

    //Config settings
    public int MAX_BLOCKS_EDITS_PER_TICK = 100;
    public int DEFAULT_BLOCK_EDITS_PER_TICK = 20;
    public int MAX_LIFESPAN = 36000; // 30 minutes
    public float CHANCE_FOR_FLYING_BLOCK = 0.8f;
    public boolean DO_DESPAWN = true;
    public boolean ENABLE_AUDIO = true;
    public boolean SPAWN_FLYING_BLOCKS = true;
    public boolean RENDER_COLORED_BEAMS = true;
}
